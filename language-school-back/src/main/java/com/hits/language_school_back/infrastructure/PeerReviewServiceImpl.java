package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.AssessmentDTO;
import com.hits.language_school_back.dto.AssessmentItemDTO;
import com.hits.language_school_back.dto.AssessmentItemRequestDTO;
import com.hits.language_school_back.dto.AssessmentSubmitDTO;
import com.hits.language_school_back.dto.PeerReviewAccessDTO;
import com.hits.language_school_back.dto.PeerReviewAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewEnableDTO;
import com.hits.language_school_back.dto.PeerReviewManualAssignmentDTO;
import com.hits.language_school_back.dto.PeerReviewResultDTO;
import com.hits.language_school_back.dto.PeerReviewResultsDTO;
import com.hits.language_school_back.dto.PeerReviewSettingsDTO;
import com.hits.language_school_back.dto.PeerReviewWithoutReviewerWarningDTO;
import com.hits.language_school_back.dto.TaskCriterionDTO;
import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
import com.hits.language_school_back.enums.PeerReviewAssignmentStatus;
import com.hits.language_school_back.enums.PeerReviewDistributionType;
import com.hits.language_school_back.model.Assessment;
import com.hits.language_school_back.model.AssessmentItem;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.PeerReviewAssignment;
import com.hits.language_school_back.model.Participation;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.model.TaskCriterion;
import com.hits.language_school_back.model.Team;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.AssessmentItemRepository;
import com.hits.language_school_back.repository.AssessmentRepository;
import com.hits.language_school_back.repository.ParticipationRepository;
import com.hits.language_school_back.repository.PeerReviewAssignmentRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.TaskCriterionRepository;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TeamRepository;
import com.hits.language_school_back.service.PeerReviewDistributionService;
import com.hits.language_school_back.service.PeerReviewService;
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
public class PeerReviewServiceImpl implements PeerReviewService {
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;
    private final TaskCriterionRepository taskCriterionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentItemRepository assessmentItemRepository;
    private final StudentsInCourseRepository studentsInCourseRepository;
    private final PeerReviewAssignmentRepository peerReviewAssignmentRepository;
    private final PeerReviewDistributionService peerReviewDistributionService;

    @Override
    @Transactional
    public Task enablePeerReview(UUID taskId, PeerReviewEnableDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        validateEnableDto(dto);
        ensureTaskHasTeams(taskId);

        Boolean reviewerVisibleToTeams = Boolean.TRUE.equals(dto.getPeerReviewerVisibleToTeams());
        if (Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            ensurePeerReviewSettingsMatch(task, dto, reviewerVisibleToTeams);
            peerReviewDistributionService.createDistributionIfReady(task);
            return task;
        }

        task.setPeerReviewEnabled(Boolean.TRUE);
        task.setPeerReviewDistributionType(dto.getPeerReviewDistributionType());
        task.setPeerReviewerVisibleToTeams(reviewerVisibleToTeams);

        Task saved = taskRepository.save(task);
        peerReviewDistributionService.createDistributionIfReady(saved);
        return saved;
    }

    @Override
    @Transactional
    public PeerReviewAssignmentDTO assignManualPeerReview(UUID taskId, PeerReviewManualAssignmentDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        validateManualAssignmentDto(dto);
        ensureManualDistributionEnabled(task);
        ensureTaskClosedForAssignments(task);

        Team reviewerTeam = getTeam(taskId, dto.getReviewerTeamId());
        Team reviewedTeam = getTeam(taskId, dto.getReviewedTeamId());
        ensureDifferentTeams(reviewerTeam, reviewedTeam);

        PeerReviewAssignment existingForReviewed = peerReviewAssignmentRepository
                .findByTaskIdAndReviewedTeamId(taskId, reviewedTeam.getId())
                .orElse(null);
        if (existingForReviewed != null) {
            if (sameAssignment(existingForReviewed, reviewerTeam, reviewedTeam)) {
                return toAssignmentDto(existingForReviewed);
            }
            throw new IllegalArgumentException("Reviewed team already has reviewer");
        }

        peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeam.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Reviewer team already has review assignment");
                });

        Participation targetParticipation = reviewedTeam.getSolutionParticipation();
        if (targetParticipation == null) {
            throw new IllegalArgumentException("Reviewed team has no submitted solution");
        }

        PeerReviewAssignment assignment = new PeerReviewAssignment();
        assignment.setTask(task);
        assignment.setReviewerTeam(reviewerTeam);
        assignment.setReviewedTeam(reviewedTeam);
        assignment.setTargetParticipation(targetParticipation);
        assignment.setStatus(PeerReviewAssignmentStatus.ASSIGNED);

        return toAssignmentDto(peerReviewAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public PeerReviewSettingsDTO getPeerReviewSettings(UUID taskId, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());

        List<PeerReviewAssignment> assignments = peerReviewAssignmentRepository.findAllByTaskId(taskId);
        List<PeerReviewWithoutReviewerWarningDTO> warnings = assignments.stream()
                .filter(assignment -> assignment.getStatus() == PeerReviewAssignmentStatus.WITHOUT_REVIEWER)
                .map(this::toWithoutReviewerWarningDto)
                .toList();

        return PeerReviewSettingsDTO.builder()
                .taskId(task.getId())
                .peerReviewEnabled(task.getPeerReviewEnabled())
                .peerReviewDistributionType(task.getPeerReviewDistributionType())
                .peerReviewerVisibleToTeams(task.getPeerReviewerVisibleToTeams())
                .peerReviewConfirmedAt(task.getPeerReviewConfirmedAt())
                .hasTeamsWithoutReviewer(!warnings.isEmpty())
                .assignments(assignments.stream().map(this::toAssignmentDto).toList())
                .teamsWithoutReviewer(warnings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PeerReviewResultsDTO getPeerReviewResults(UUID taskId, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        ensurePeerReviewEnabled(task);

        List<TaskCriterion> activeCriteria = taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId);
        Integer totalMaxPoints = calculateTotalMaxPoints(activeCriteria);

        return PeerReviewResultsDTO.builder()
                .taskId(task.getId())
                .peerReviewEnabled(task.getPeerReviewEnabled())
                .peerReviewDistributionType(task.getPeerReviewDistributionType())
                .peerReviewerVisibleToTeams(task.getPeerReviewerVisibleToTeams())
                .peerReviewConfirmedAt(task.getPeerReviewConfirmedAt())
                .totalMaxPoints(totalMaxPoints)
                .results(peerReviewAssignmentRepository.findAllByTaskId(taskId).stream()
                        .map(assignment -> toResultDto(assignment, totalMaxPoints))
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public PeerReviewResultsDTO confirmPeerReviewResults(UUID taskId, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        ensurePeerReviewEnabled(task);

        List<PeerReviewAssignment> assignments = peerReviewAssignmentRepository.findAllByTaskId(taskId);
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("Task has no peer review assignments");
        }

        for (PeerReviewAssignment assignment : assignments) {
            ensureAssignmentCanBeFinalized(assignment);
            syncPeerAssessmentAsTeamMark(assignment.getReviewedTeam(), assignment.getAssessment().getTotalPoints());
            if (assignment.getStatus() != PeerReviewAssignmentStatus.FINAL) {
                assignment.setStatus(PeerReviewAssignmentStatus.FINAL);
                peerReviewAssignmentRepository.save(assignment);
            }
        }

        if (task.getPeerReviewConfirmedAt() == null) {
            task.setPeerReviewConfirmedAt(LocalDateTime.now());
            taskRepository.save(task);
        }
        return getPeerReviewResults(taskId, teacherId);
    }

    @Override
    @Transactional(readOnly = true)
    public PeerReviewAccessDTO getMyPeerReviewAssignment(UUID taskId, UUID studentId) {
        Task task = getTask(taskId);
        ensurePeerReviewEnabled(task);

        Participation reviewerParticipation = getReviewerCaptainParticipation(taskId, studentId);
        Team reviewerTeam = reviewerParticipation.getTeam();
        PeerReviewAssignment assignment = getReviewerAssignment(taskId, reviewerTeam);

        return toAccessDto(task, reviewerTeam, assignment);
    }

    @Override
    @Transactional
    public PeerReviewAccessDTO submitMyPeerReviewAssignment(UUID taskId, AssessmentSubmitDTO dto, UUID studentId) {
        Task task = getTask(taskId);
        ensurePeerReviewEnabled(task);

        Participation reviewerParticipation = getReviewerCaptainParticipation(taskId, studentId);
        Team reviewerTeam = reviewerParticipation.getTeam();
        PeerReviewAssignment assignment = getReviewerAssignment(taskId, reviewerTeam);
        ensureAssignmentCanBeSubmitted(assignment);

        Assessment assessment = createPeerAssessment(task, assignment, reviewerParticipation.getStudent(), dto);
        assignment.setAssessment(assessment);
        assignment.setStatus(PeerReviewAssignmentStatus.SUBMITTED);
        assignment.setSubmittedAt(LocalDateTime.now());
        PeerReviewAssignment savedAssignment = peerReviewAssignmentRepository.save(assignment);

        syncPeerAssessmentAsTeamMark(savedAssignment.getReviewedTeam(), assessment.getTotalPoints());
        return toAccessDto(task, reviewerTeam, savedAssignment);
    }

    @Override
    @Transactional
    public PeerReviewResultDTO editPeerReviewAssessment(UUID taskId, UUID assignmentId, AssessmentSubmitDTO dto, UUID teacherId) {
        Task task = getTask(taskId);
        ensureTeacherCanManageCourse(teacherId, task.getCourse());
        ensurePeerReviewEnabled(task);

        PeerReviewAssignment assignment = getAssignmentInTask(taskId, assignmentId);
        ensureAssignmentCanBeEditedByTeacher(assignment);

        Assessment assessment = assignment.getAssessment();
        assessment.setTotalPoints(replacePeerAssessmentItems(assessment, dto));
        assessment.setUpdatedAt(LocalDateTime.now());
        Assessment savedAssessment = assessmentRepository.save(assessment);

        assignment.setAssessment(savedAssessment);
        assignment.setStatus(PeerReviewAssignmentStatus.TEACHER_EDITED);
        assignment.setTeacherEditor(task.getCourse().getTeacher());
        assignment.setTeacherEditedAt(LocalDateTime.now());
        PeerReviewAssignment savedAssignment = peerReviewAssignmentRepository.save(assignment);

        syncPeerAssessmentAsTeamMark(savedAssignment.getReviewedTeam(), savedAssessment.getTotalPoints());
        Integer totalMaxPoints = calculateTotalMaxPoints(taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(taskId));
        return toResultDto(savedAssignment, totalMaxPoints);
    }

    private Participation getReviewerCaptainParticipation(UUID taskId, UUID studentId) {
        Participation reviewerParticipation = participationRepository.findAllByTeamTaskId(taskId).stream()
                .filter(participation -> participation.getStudent() != null && participation.getStudent().getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Student is not assigned to a team in this task"));
        if (!Boolean.TRUE.equals(reviewerParticipation.getIsCaptain())) {
            throw new IllegalArgumentException("Only reviewer team captain can access peer review assignment");
        }
        return reviewerParticipation;
    }

    private PeerReviewAssignment getReviewerAssignment(UUID taskId, Team reviewerTeam) {
        return peerReviewAssignmentRepository.findByTaskIdAndReviewerTeamId(taskId, reviewerTeam.getId())
                .orElseThrow(() -> new IllegalArgumentException("Reviewer team has no peer review assignment"));
    }

    private PeerReviewAssignment getAssignmentInTask(UUID taskId, UUID assignmentId) {
        PeerReviewAssignment assignment = peerReviewAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Peer review assignment not found: " + assignmentId));
        if (assignment.getTask() == null || !Objects.equals(assignment.getTask().getId(), taskId)) {
            throw new IllegalArgumentException("Peer review assignment does not belong to this task");
        }
        return assignment;
    }

    private PeerReviewAccessDTO toAccessDto(Task task, Team reviewerTeam, PeerReviewAssignment assignment) {
        Team reviewedTeam = assignment.getReviewedTeam();
        List<TaskCriterion> activeCriteria = taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(task.getId());
        Integer totalMaxPoints = calculateTotalMaxPoints(activeCriteria);
        AssessmentDTO assessment = assignment.getAssessment() == null
                ? null
                : toAssessmentDto(assignment.getAssessment(), totalMaxPoints);

        return PeerReviewAccessDTO.builder()
                .taskId(task.getId())
                .assignment(toAssignmentDto(assignment))
                .assessment(assessment)
                .reviewerTeamId(reviewerTeam.getId())
                .reviewerTeamName(reviewerTeam.getName())
                .reviewedTeamId(reviewedTeam == null ? null : reviewedTeam.getId())
                .reviewedTeamName(reviewedTeam == null ? null : reviewedTeam.getName())
                .targetParticipationId(assignment.getTargetParticipation() == null ? null : assignment.getTargetParticipation().getId())
                .status(assignment.getStatus())
                .canSubmit(assignment.getStatus() == PeerReviewAssignmentStatus.ASSIGNED && assignment.getAssessment() == null)
                .totalMaxPoints(totalMaxPoints)
                .criteria(activeCriteria.stream()
                        .map(this::toCriterionDto)
                        .toList())
                .build();
    }

    private PeerReviewResultDTO toResultDto(PeerReviewAssignment assignment, Integer totalMaxPoints) {
        Team reviewerTeam = assignment.getReviewerTeam();
        Team reviewedTeam = assignment.getReviewedTeam();
        AssessmentDTO assessment = assignment.getAssessment() == null
                ? null
                : toAssessmentDto(assignment.getAssessment(), totalMaxPoints);

        return PeerReviewResultDTO.builder()
                .taskId(assignment.getTask() == null ? null : assignment.getTask().getId())
                .assignment(toAssignmentDto(assignment))
                .assessment(assessment)
                .reviewerTeamId(reviewerTeam == null ? null : reviewerTeam.getId())
                .reviewerTeamName(reviewerTeam == null ? null : reviewerTeam.getName())
                .reviewedTeamId(reviewedTeam == null ? null : reviewedTeam.getId())
                .reviewedTeamName(reviewedTeam == null ? null : reviewedTeam.getName())
                .targetParticipationId(assignment.getTargetParticipation() == null ? null : assignment.getTargetParticipation().getId())
                .status(assignment.getStatus())
                .build();
    }

    private Assessment createPeerAssessment(Task task, PeerReviewAssignment assignment, User assessor, AssessmentSubmitDTO dto) {
        if (assignment.getTargetParticipation() == null) {
            throw new IllegalArgumentException("Peer review assignment has no target participation");
        }
        assessmentRepository.findByParticipationIdAndType(assignment.getTargetParticipation().getId(), AssessmentType.PEER)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Peer review assignment has already been submitted");
                });

        LocalDateTime now = LocalDateTime.now();
        Assessment assessment = new Assessment();
        assessment.setTask(task);
        assessment.setParticipation(assignment.getTargetParticipation());
        assessment.setAssessor(assessor);
        assessment.setType(AssessmentType.PEER);
        assessment.setStatus(AssessmentStatus.SUBMITTED);
        assessment.setTotalPoints(0);
        assessment.setCreatedAt(now);
        assessment.setUpdatedAt(now);

        Assessment saved = assessmentRepository.save(assessment);
        saved.setTotalPoints(replacePeerAssessmentItems(saved, dto));
        saved.setUpdatedAt(LocalDateTime.now());
        return assessmentRepository.save(saved);
    }

    private Integer replacePeerAssessmentItems(Assessment assessment, AssessmentSubmitDTO dto) {
        List<AssessmentItemRequestDTO> requestedItems = dto == null || dto.getItems() == null ? List.of() : dto.getItems();
        Map<UUID, TaskCriterion> criteriaById = taskCriterionRepository.findAllByTaskIdAndActiveTrueOrderByOrderIndexAscTitleAsc(assessment.getTask().getId()).stream()
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
        if (seenCriteria.size() != criteriaById.size()) {
            throw new IllegalArgumentException("Peer assessment must include every active criterion");
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
        return total;
    }

    private void syncPeerAssessmentAsTeamMark(Team reviewedTeam, Integer totalPoints) {
        if (reviewedTeam == null) {
            throw new IllegalArgumentException("Peer review assignment has no reviewed team");
        }
        reviewedTeam.setCommandMark(totalPoints);
        recalculateTeamStats(reviewedTeam);
        if (reviewedTeam.getTask() != null && reviewedTeam.getTask().getCourse() != null) {
            recalculateCourseGrades(reviewedTeam.getTask().getCourse().getId());
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

    private Integer calculateTotalMaxPoints(List<TaskCriterion> criteria) {
        return criteria.stream()
                .map(TaskCriterion::getMaxPoints)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Task getTask(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    private void validateEnableDto(PeerReviewEnableDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Peer-review settings are required");
        }
        if (dto.getPeerReviewDistributionType() == null) {
            throw new IllegalArgumentException("peerReviewDistributionType is required when peer review is enabled");
        }
    }

    private void validateManualAssignmentDto(PeerReviewManualAssignmentDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Peer-review assignment is required");
        }
        if (dto.getReviewerTeamId() == null || dto.getReviewedTeamId() == null) {
            throw new IllegalArgumentException("reviewerTeamId and reviewedTeamId are required");
        }
    }

    private void ensureTaskHasTeams(UUID taskId) {
        if (teamRepository.countByTaskId(taskId) == 0) {
            throw new IllegalArgumentException("Task must have teams before enabling peer review");
        }
    }

    private void ensureTeacherCanManageCourse(UUID teacherId, Course course) {
        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Only the course teacher can manage this task");
        }
    }

    private void ensurePeerReviewSettingsMatch(Task task, PeerReviewEnableDTO dto, Boolean reviewerVisibleToTeams) {
        boolean sameDistributionType = task.getPeerReviewDistributionType() == dto.getPeerReviewDistributionType();
        boolean sameReviewerVisibility = Objects.equals(Boolean.TRUE.equals(task.getPeerReviewerVisibleToTeams()), reviewerVisibleToTeams);
        if (!sameDistributionType || !sameReviewerVisibility) {
            throw new IllegalArgumentException("Peer review is already enabled for this task with different settings");
        }
    }

    private Team getTeam(UUID taskId, UUID teamId) {
        return teamRepository.findByIdAndTaskId(teamId, taskId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found in task: " + teamId));
    }

    private void ensureManualDistributionEnabled(Task task) {
        if (!Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            throw new IllegalArgumentException("Peer review is not enabled for this task");
        }
        if (task.getPeerReviewDistributionType() != PeerReviewDistributionType.MANUAL) {
            throw new IllegalArgumentException("Manual peer-review assignment requires MANUAL distribution type");
        }
    }

    private void ensurePeerReviewEnabled(Task task) {
        if (!Boolean.TRUE.equals(task.getPeerReviewEnabled())) {
            throw new IllegalArgumentException("Peer review is not enabled for this task");
        }
    }

    private void ensureAssignmentCanBeSubmitted(PeerReviewAssignment assignment) {
        if (assignment.getStatus() == PeerReviewAssignmentStatus.SUBMITTED || assignment.getAssessment() != null) {
            throw new IllegalArgumentException("Peer review assignment has already been submitted");
        }
        if (assignment.getStatus() != PeerReviewAssignmentStatus.ASSIGNED) {
            throw new IllegalArgumentException("Peer review assignment is not available for submission");
        }
    }

    private void ensureAssignmentCanBeEditedByTeacher(PeerReviewAssignment assignment) {
        if (assignment.getAssessment() == null) {
            throw new IllegalArgumentException("Peer review assessment is not submitted yet");
        }
        if (assignment.getAssessment().getType() != AssessmentType.PEER) {
            throw new IllegalArgumentException("Assignment assessment is not a peer assessment");
        }
        if (assignment.getStatus() != PeerReviewAssignmentStatus.SUBMITTED
                && assignment.getStatus() != PeerReviewAssignmentStatus.TEACHER_EDITED) {
            throw new IllegalArgumentException("Peer review assessment is not available for teacher edit");
        }
    }

    private void ensureAssignmentCanBeFinalized(PeerReviewAssignment assignment) {
        if (assignment.getAssessment() == null) {
            throw new IllegalArgumentException("Peer review assessment is not submitted yet");
        }
        if (assignment.getAssessment().getType() != AssessmentType.PEER) {
            throw new IllegalArgumentException("Assignment assessment is not a peer assessment");
        }
        if (assignment.getStatus() != PeerReviewAssignmentStatus.SUBMITTED
                && assignment.getStatus() != PeerReviewAssignmentStatus.TEACHER_EDITED
                && assignment.getStatus() != PeerReviewAssignmentStatus.FINAL) {
            throw new IllegalArgumentException("Peer review assessment is not available for finalization");
        }
    }

    private void ensureTaskClosedForAssignments(Task task) {
        if (!Boolean.TRUE.equals(task.getSubmissionClosed())) {
            throw new IllegalArgumentException("Task submissions must be closed before assigning peer reviews");
        }
    }

    private void ensureDifferentTeams(Team reviewerTeam, Team reviewedTeam) {
        if (Objects.equals(reviewerTeam.getId(), reviewedTeam.getId())) {
            throw new IllegalArgumentException("Team cannot review itself");
        }
    }

    private boolean sameAssignment(PeerReviewAssignment assignment, Team reviewerTeam, Team reviewedTeam) {
        return assignment.getReviewerTeam() != null
                && Objects.equals(assignment.getReviewerTeam().getId(), reviewerTeam.getId())
                && assignment.getReviewedTeam() != null
                && Objects.equals(assignment.getReviewedTeam().getId(), reviewedTeam.getId());
    }

    private PeerReviewAssignmentDTO toAssignmentDto(PeerReviewAssignment assignment) {
        return PeerReviewAssignmentDTO.builder()
                .id(assignment.getId())
                .taskId(assignment.getTask() == null ? null : assignment.getTask().getId())
                .reviewerTeamId(assignment.getReviewerTeam() == null ? null : assignment.getReviewerTeam().getId())
                .reviewedTeamId(assignment.getReviewedTeam() == null ? null : assignment.getReviewedTeam().getId())
                .targetParticipationId(assignment.getTargetParticipation() == null ? null : assignment.getTargetParticipation().getId())
                .assessmentId(assignment.getAssessment() == null ? null : assignment.getAssessment().getId())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .submittedAt(assignment.getSubmittedAt())
                .teacherEditorId(assignment.getTeacherEditor() == null ? null : assignment.getTeacherEditor().getId())
                .teacherEditedAt(assignment.getTeacherEditedAt())
                .build();
    }

    private PeerReviewWithoutReviewerWarningDTO toWithoutReviewerWarningDto(PeerReviewAssignment assignment) {
        Team team = assignment.getReviewedTeam();
        String teamName = team == null ? null : team.getName();
        return PeerReviewWithoutReviewerWarningDTO.builder()
                .assignmentId(assignment.getId())
                .teamId(team == null ? null : team.getId())
                .teamName(teamName)
                .message("Team has no peer reviewer: " + (teamName == null ? "unknown team" : teamName))
                .build();
    }

    private TaskCriterionDTO toCriterionDto(TaskCriterion criterion) {
        return TaskCriterionDTO.builder()
                .id(criterion.getId())
                .taskId(criterion.getTask() == null ? null : criterion.getTask().getId())
                .title(criterion.getTitle())
                .description(criterion.getDescription())
                .maxPoints(criterion.getMaxPoints())
                .sectionName(criterion.getSectionName())
                .orderIndex(criterion.getOrderIndex())
                .active(criterion.getActive())
                .build();
    }
}
