package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.CourseCreateDTO;
import com.hits.language_school_back.dto.CourseDTO;
import com.hits.language_school_back.dto.CourseEditDTO;
import com.hits.language_school_back.exception.CourseNotFound;
import com.hits.language_school_back.exception.LanguageNotFoundException;
import com.hits.language_school_back.exception.UserNotFoundException;
import com.hits.language_school_back.mapper.CourseMapper;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;
    private final StudentsInCourseRepository studentsInCourseRepository;

    //Сначала всегда создаётся пустой курс, потом отдельно наполняется
    public CourseDTO createCourse(CourseCreateDTO dto) {
        User teacher = userRepository.findById(dto.getTeacherId()).orElseThrow(() -> new UserNotFoundException("User with id" + dto.getTeacherId() + " not found"));
        Language lang = languageRepository.findById(dto.getLanguageId()).orElseThrow(() -> new LanguageNotFoundException("language with id" + dto.getLanguageId() + " not found"));

        Course course = Course.builder().name(dto.getName()).description(dto.getDescription()).teacher(teacher).language(lang).satisfactorilyMarkThreshold(dto.getSatisfactorilyMarkThreshold()).goodMarkThreshold(dto.getGoodMarkThreshold()).excellentMarkThreshold(dto.getExcellentMarkThreshold()).build();
        courseRepository.save(course);
        return CourseMapper.courseToCourseDTO(course);
    }

    public void deleteCourse(UUID courseId) {
        Course toDel = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFound("Course with id" + courseId + " not found"));
        courseRepository.delete(toDel);
    }

    public CourseDTO updateCourse(UUID courseId, CourseEditDTO dto) {
        Course toUpdate = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFound("Course with id" + courseId + " not found"));
        toUpdate.setName(dto.getName());
        toUpdate.setDescription(dto.getDescription());
        toUpdate.setGoodMarkThreshold(dto.getGoodMarkThreshold());
        toUpdate.setExcellentMarkThreshold(dto.getExcellentMarkThreshold());
        toUpdate.setSatisfactorilyMarkThreshold(dto.getSatisfactorilyMarkThreshold());
        Course saved = courseRepository.save(toUpdate);
        return CourseMapper.courseToCourseDTO(saved);
    }

    public List<CourseDTO> findAll() {
        return courseRepository.findAll().stream().map(CourseMapper::courseToCourseDTO).toList();
    }

    public boolean checkStudentInCourse(UUID courseId, UUID studentId) {
        return userRepository.existsById(studentId)
                && courseRepository.existsById(courseId)
                && studentsInCourseRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional
    public boolean addStudentsToCourse(UUID courseId, List<UUID> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            return false;
        }

        Course currCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFound("Course with id " + courseId + " not found"));

        List<User> studentsToAdd = userRepository.findAllById(studentIds);
        if (studentsToAdd.size() != studentIds.size()) {
            Set<UUID> foundIds = studentsToAdd.stream().map(User::getId).collect(Collectors.toSet());
            List<UUID> missingIds = studentIds.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new UserNotFoundException("Students with ids " + missingIds + " not found");
        }

        Set<UUID> alreadyEnrolledIds = new HashSet<>(
                studentsInCourseRepository.findAlreadyEnrolledStudentIds(courseId, studentIds)
        );

        List<User> actualStudentsToAdd = studentsToAdd.stream()
                .filter(student -> !alreadyEnrolledIds.contains(student.getId()))
                .toList();
        if (actualStudentsToAdd.isEmpty()) {
            return false;
        }
        List<StudentsInCourse> newEnrollments = actualStudentsToAdd.stream()
                .map(student -> StudentsInCourse.builder()
                        .courseGrade(0D)
                        .course(currCourse)
                        .student(student)
                        .build())
                .toList();
        List<StudentsInCourse> saved = studentsInCourseRepository.saveAll(newEnrollments);
        return !saved.isEmpty();
    }
}
