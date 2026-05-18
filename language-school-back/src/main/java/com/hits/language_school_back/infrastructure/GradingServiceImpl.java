package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.AssessmentDTO;
import com.hits.language_school_back.dto.AssessmentItemDTO;
import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.ParticipationAssessmentDTO;
import com.hits.language_school_back.dto.TaskCriterionDTO;
import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.Assessment;
import com.hits.language_school_back.model.AssessmentItem;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AssessmentItemRepository;
import com.hits.language_school_back.repository.AssessmentRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {
    private final TaskRepository taskRepository;
    private final TaskCriterionRepository criterionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentItemRepository assessmentItemRepository;
    private final ParticipationRepository participationRepository;
    private final StudentsInCourseRepository studentsInCourseRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TaskCriterionDTO createCriterion(UUID taskId, TaskCriterionDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        validateCriterionDto(dto);

        TaskCriterion criterion = new TaskCriterion();
        criterion.setTask(task);
        applyCriterionDto(criterion, dto);
        criterion.setActive(Boolean.TRUE);
        TaskCriterion saved = criterionRepository.save(criterion);
        syncTaskTotalPoints(task);
        return toCriterionDto(saved);
    }

    @Override
    @Transactional
    public TaskCriterionDTO editCriterion(UUID taskId, UUID criterionId, TaskCriterionDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        TaskCriterion criterion = getCriterion(taskId, criterionId);
        validateCriterionDto(dto);
        if (assessmentItemRepository.existsByCriterionIdAndPointsGreaterThan(criterionId, dto.getMaxPoints())) {
            throw new IllegalArgumentException("Existing assessment points exceed new criterion maxPoints");
        }

        applyCriterionDto(criterion, dto);
        TaskCriterion saved = criterionRepository.save(criterion);
        syncTaskTotalPoints(task);
        return toCriterionDto(saved);
    }

    @Override
    @Transactional
    public void deactivateCriterion(UUID taskId, UUID criterionId, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        TaskCriterion criterion = getCriterion(taskId, criterionId);
        criterion.setActive(Boolean.FALSE);
        criterionRepository.save(criterion);
        syncTaskTotalPoints(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCriterionDTO> getCriteria(UUID taskId) {
        return criterionRepository.findAllByTaskIdOrderByOrderIndexAscTitleAsc(taskId).stream()
                .map(this::toCriterionDto)
                .toList();
    }

    @Override
    @Transactional
    public AssessmentDTO submitTeacherAssessment(UUID taskId, UUID participationId, AssessmentSubmitDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        Participation participation = getParticipationInTask(participationId, taskId);
        User teacher = getUser(teacherId);

        Assessment assessment = upsertAssessment(task, participation, teacher, AssessmentType.TEACHER);
        AssessmentDTO result = replaceAssessmentItems(assessment, dto);

        participation.setMark(result.getTotalPoints());
        participationRepository.save(participation);
        recalculateTeamStats(participation.getTeam());
        recalculateCourseGrades(task.getCourse().getId());
        return result;
    }

    @Override
    @Transactional
    public AssessmentDTO submitSelfAssessment(UUID taskId, UUID participationId, AssessmentSubmitDTO dto, UUID studentId) {
        Task task = getTask(taskId);
        Participation participation = getParticipationInTask(participationId, taskId);
        if (participation.getStudent() == null || !participation.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Students can self-assess only their own participation");
        }

        Assessment assessment = upsertAssessment(task, participation, getUser(studentId), AssessmentType.SELF);
        return replaceAssessmentItems(assessment, dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationAssessmentDTO getParticipationAssessment(UUID taskId, UUID participationId, UUID actorId) {
        Task task = getTask(taskId);
        Participation participation = getParticipationInTask(participationId, taskId);
        User actor = getUser(actorId);
        ensureCanViewAssessment(task, participation, actor);

        Integer totalMaxPoints = calculateTotalMaxPoints(taskId);
        AssessmentDTO teacherAssessment = assessmentRepository.findByParticipationIdAndType(participationId, AssessmentType.TEACHER)
                .map(assessment -> toAssessmentDto(assessment, totalMaxPoints))
                .orElse(null);
        AssessmentDTO selfAssessment = assessmentRepository.findByParticipationIdAndType(participationId, AssessmentType.SELF)
                .map(assessment -> toAssessmentDto(assessment, totalMaxPoints))
                .orElse(null);

        return ParticipationAssessmentDTO.builder()
                .taskId(taskId)
                .participationId(participationId)
                .totalMaxPoints(totalMaxPoints)
                .teacherTotal(teacherAssessment == null ? null : teacherAssessment.getTotalPoints())
                .selfTotal(selfAssessment == null ? null : selfAssessment.getTotalPoints())
                .criteria(buildCriteriaComparison(taskId, teacherAssessment, selfAssessment))
                .teacherAssessment(teacherAssessment)
                .selfAssessment(selfAssessment)
                .build();
    }

    private Assessment upsertAssessment(Task task, Participation participation, User assessor, AssessmentType type) {
        Assessment assessment = assessmentRepository.findByParticipationIdAndType(participation.getId(), type)
                .orElseGet(Assessment::new);
        if (assessment.getId() == null) {
            assessment.setCreatedAt(LocalDateTime.now());
        }
        assessment.setTask(task);
        assessment.setParticipation(participation);
        assessment.setAssessor(assessor);
        assessment.setType(type);
        assessment.setStatus(AssessmentStatus.SUBMITTED);
        assessment.setUpdatedAt(LocalDateTime.now());
        assessment.setTotalPoints(0);
        return assessmentRepository.save(assessment);
    }

    private AssessmentDTO replaceAssessmentItems(Assessment assessment, AssessmentSubmitDTO dto) {
        List<AssessmentItemRequestDTO> requestedItems = dto == null || dto.getItems() == null ? List.of() : dto.getItems();
        Map<UUID, TaskCriterion> criteriaById = criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(assessment.getTask().getId()).stream()
                .collect(Collectors.toMap(TaskCriterion::getId, Function.identity()));
        if (criteriaById.isEmpty()) {
            throw new IllegalArgumentException("Task has no active grading criteria");
        }

        Set<UUID> seenCriteria = new HashSet<>();
        for (AssessmentItemRequestDTO itemDto : requestedItems) {
            if (itemDto.getCriterionId() == null) {
                throw new IllegalArgumentException("criterionId is required");
            }
            if (!seenCriteria.add(itemDto.getCriterionId())) {
                throw new IllegalArgumentException("Duplicate criterion in assessment");
            }
            TaskCriterion criterion = criteriaById.get(itemDto.getCriterionId());
            if (criterion == null) {
                throw new IllegalArgumentException("Criterion does not belong to this task or is inactive");
            }
            validatePoints(itemDto.getPoints(), criterion);
        }

        Map<UUID, AssessmentItem> currentItems = assessmentItemRepository.findAllByAssessmentId(assessment.getId()).stream()
                .collect(Collectors.toMap(item -> item.getCriterion().getId(), Function.identity()));
        List<AssessmentItem> itemsToDelete = currentItems.entrySet().stream()
                .filter(entry -> !seenCriteria.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        assessmentItemRepository.deleteAll(itemsToDelete);

        int total = 0;
        for (AssessmentItemRequestDTO itemDto : requestedItems) {
            TaskCriterion criterion = criteriaById.get(itemDto.getCriterionId());
            AssessmentItem item = currentItems.getOrDefault(itemDto.getCriterionId(), new AssessmentItem());
            item.setAssessment(assessment);
            item.setCriterion(criterion);
            item.setPoints(itemDto.getPoints());
            item.setComment(itemDto.getComment());
            assessmentItemRepository.save(item);
            total += itemDto.getPoints();
        }

        assessment.setTotalPoints(total);
        assessment.setUpdatedAt(LocalDateTime.now());
        Assessment saved = assessmentRepository.save(assessment);
        return toAssessmentDto(saved, calculateTotalMaxPoints(saved.getTask().getId()));
    }

    private List<AssessmentItemDTO> buildCriteriaComparison(UUID taskId, AssessmentDTO teacherAssessment, AssessmentDTO selfAssessment) {
        Map<UUID, AssessmentItemDTO> teacherItems = teacherAssessment == null ? Map.of() : teacherAssessment.getItems().stream()
                .collect(Collectors.toMap(AssessmentItemDTO::getCriterionId, Function.identity()));
        Map<UUID, AssessmentItemDTO> selfItems = selfAssessment == null ? Map.of() : selfAssessment.getItems().stream()
                .collect(Collectors.toMap(AssessmentItemDTO::getCriterionId, Function.identity()));

        return criterionRepository.findAllByTaskIdOrderByOrderIndexAscTitleAsc(taskId).stream()
                .map(criterion -> {
                    AssessmentItemDTO teacherItem = teacherItems.get(criterion.getId());
                    AssessmentItemDTO selfItem = selfItems.get(criterion.getId());
                    return AssessmentItemDTO.builder()
                            .criterionId(criterion.getId())
                            .title(criterion.getTitle())
                            .description(criterion.getDescription())
                            .maxPoints(criterion.getMaxPoints())
                            .sectionName(criterion.getSectionName())
                            .orderIndex(criterion.getOrderIndex())
                            .active(criterion.getActive())
                            .points(teacherItem == null ? null : teacherItem.getPoints())
                            .comment(teacherItem == null ? null : teacherItem.getComment())
                            .teacherPoints(teacherItem == null ? null : teacherItem.getPoints())
                            .selfPoints(selfItem == null ? null : selfItem.getPoints())
                            .teacherComment(teacherItem == null ? null : teacherItem.getComment())
                            .selfComment(selfItem == null ? null : selfItem.getComment())
                            .build();
                })
                .toList();
    }

    private AssessmentDTO toAssessmentDto(Assessment assessment, Integer totalMaxPoints) {
        List<AssessmentItemDTO> items = assessmentItemRepository.findAllByAssessmentId(assessment.getId()).stream()
                .sorted(Comparator.comparing((AssessmentItem item) -> item.getCriterion().getOrderIndex(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(item -> item.getCriterion().getTitle(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toAssessmentItemDto)
                .toList();

        return AssessmentDTO.builder()
                .id(assessment.getId())
                .taskId(assessment.getTask().getId())
                .participationId(assessment.getParticipation().getId())
                .assessorId(assessment.getAssessor().getId())
                .type(assessment.getType())
                .status(assessment.getStatus())
                .totalPoints(assessment.getTotalPoints())
                .totalMaxPoints(totalMaxPoints)
                .updatedAt(assessment.getUpdatedAt())
                .items(items)
                .build();
    }

    private AssessmentItemDTO toAssessmentItemDto(AssessmentItem item) {
        TaskCriterion criterion = item.getCriterion();
        return AssessmentItemDTO.builder()
                .criterionId(criterion.getId())
                .title(criterion.getTitle())
                .description(criterion.getDescription())
                .maxPoints(criterion.getMaxPoints())
                .sectionName(criterion.getSectionName())
                .orderIndex(criterion.getOrderIndex())
                .active(criterion.getActive())
                .points(item.getPoints())
                .comment(item.getComment())
                .build();
    }

    private TaskCriterionDTO toCriterionDto(TaskCriterion criterion) {
        return TaskCriterionDTO.builder()
                .id(criterion.getId())
                .taskId(criterion.getTask().getId())
                .title(criterion.getTitle())
                .description(criterion.getDescription())
                .maxPoints(criterion.getMaxPoints())
                .sectionName(criterion.getSectionName())
                .orderIndex(criterion.getOrderIndex())
                .active(criterion.getActive())
                .build();
    }

    private void applyCriterionDto(TaskCriterion criterion, TaskCriterionDTO dto) {
        criterion.setTitle(dto.getTitle());
        criterion.setDescription(dto.getDescription());
        criterion.setMaxPoints(dto.getMaxPoints());
        criterion.setSectionName(dto.getSectionName());
        criterion.setOrderIndex(dto.getOrderIndex());
    }

    private void validateCriterionDto(TaskCriterionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Criterion payload is required");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Criterion title is required");
        }
        if (dto.getMaxPoints() == null || dto.getMaxPoints() <= 0) {
            throw new IllegalArgumentException("Criterion maxPoints must be positive");
        }
    }

    private void validatePoints(Integer points, TaskCriterion criterion) {
        if (points == null) {
            throw new IllegalArgumentException("Assessment points are required");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Assessment points cannot be negative");
        }
        if (points > criterion.getMaxPoints()) {
            throw new IllegalArgumentException("Assessment points cannot exceed criterion maxPoints");
        }
    }

    private void syncTaskTotalPoints(Task task) {
        task.setTotalPoints(calculateTotalMaxPoints(task.getId()));
        taskRepository.save(task);
    }

    private Integer calculateTotalMaxPoints(UUID taskId) {
        return criterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId).stream()
                .map(TaskCriterion::getMaxPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private TaskCriterion getCriterion(UUID taskId, UUID criterionId) {
        TaskCriterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new IllegalArgumentException("Criterion not found: " + criterionId));
        if (criterion.getTask() == null || !criterion.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Criterion does not belong to this task");
        }
        return criterion;
    }

    private Participation getParticipationInTask(UUID participationId, UUID taskId) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new IllegalArgumentException("Participation not found: " + participationId));
        if (participation.getTeam() == null
                || participation.getTeam().getTask() == null
                || !participation.getTeam().getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Participation does not belong to this task");
        }
        return participation;
    }

    private void ensureTeacherCanManageCourse(UUID teacherId, Course course) {
        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Only the course teacher can manage grading");
        }
    }

    private void ensureCanViewAssessment(Task task, Participation participation, User actor) {
        boolean teacher = actor.getRole() == Role.TEACHER
                && task.getCourse() != null
                && task.getCourse().getTeacher() != null
                && task.getCourse().getTeacher().getId().equals(actor.getId());
        boolean owner = participation.getStudent() != null && participation.getStudent().getId().equals(actor.getId());
        if (!teacher && !owner) {
            throw new IllegalArgumentException("Only the student or course teacher can view this assessment");
        }
    }

    private void recalculateTeamStats(Team team) {
        List<Participation> participations = participationRepository.findAllByTeamId(team.getId());
        for (Participation participation : participations) {
            Integer individualMark = participation.getMark();
            Integer teamMark = team.getCommandMark();
            if (individualMark == null && teamMark == null) {
                participation.setAverageMark(null);
            } else if (individualMark == null) {
                participation.setAverageMark(round(teamMark));
            } else if (teamMark == null) {
                participation.setAverageMark(round(individualMark));
            } else {
                participation.setAverageMark(round((individualMark + teamMark) / 2D));
            }
        }
        participationRepository.saveAll(participations);

        team.setAverageMark(round(participations.stream()
                .map(Participation::getAverageMark)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D)));
        teamRepository.save(team);
    }

    private void recalculateCourseGrades(UUID courseId) {
        List<StudentsInCourse> courseStudents = studentsInCourseRepository.findAllByCourseId(courseId);
        for (StudentsInCourse relation : courseStudents) {
            List<Participation> participations = participationRepository.findAllByStudentId(relation.getStudent().getId()).stream()
                    .filter(p -> p.getTeam() != null
                            && p.getTeam().getTask() != null
                            && p.getTeam().getTask().getCourse() != null
                            && p.getTeam().getTask().getCourse().getId().equals(courseId))
                    .filter(p -> p.getAverageMark() != null)
                    .toList();
            double average = participations.stream()
                    .mapToDouble(Participation::getAverageMark)
                    .average()
                    .orElse(0D);
            relation.setCourseGrade(round(average));
        }
        studentsInCourseRepository.saveAll(courseStudents);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
