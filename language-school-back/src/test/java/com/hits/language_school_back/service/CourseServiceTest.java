package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.CourseCreateDTO;
import com.hits.language_school_back.dto.CourseDTO;
import com.hits.language_school_back.dto.CourseEditDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.exception.CourseNotFound;
import com.hits.language_school_back.exception.UserNotFoundException;
import com.hits.language_school_back.infrastructure.CourseServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.StudentsInCourseRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LanguageRepository languageRepository;
    @Mock
    private StudentsInCourseRepository studentsInCourseRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private UUID courseId;
    private UUID teacherId;
    private UUID languageId;
    private User teacher;
    private Language language;
    private Course course;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        teacher = User.builder().id(teacherId).role(Role.TEACHER).build();
        language = new Language();
        language.setId(languageId);
        language.setName("English");

        course = Course.builder()
                .id(courseId)
                .name("Course")
                .description("Description")
                .teacher(teacher)
                .language(language)
                .satisfactorilyMarkThreshold(60)
                .goodMarkThreshold(75)
                .excellentMarkThreshold(90)
                .build();
    }

    @Test
    void createCourse_resolvesTeacherAndLanguageAndReturnsDto() {
        CourseCreateDTO dto = CourseCreateDTO.builder()
                .name("Course")
                .description("Description")
                .teacherId(teacherId)
                .languageId(languageId)
                .satisfactorilyMarkThreshold(60)
                .goodMarkThreshold(75)
                .excellentMarkThreshold(90)
                .build();
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId(courseId);
            return saved;
        });

        CourseDTO result = courseService.createCourse(dto);

        assertThat(result.getId()).isEqualTo(courseId);
        assertThat(result.getName()).isEqualTo("Course");

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTeacher()).isEqualTo(teacher);
        assertThat(courseCaptor.getValue().getLanguage()).isEqualTo(language);
    }

    @Test
    void updateCourse_changesThresholdsAndTextFields() {
        CourseEditDTO dto = CourseEditDTO.builder()
                .name("Updated")
                .description("Updated description")
                .satisfactorilyMarkThreshold(50)
                .goodMarkThreshold(70)
                .excellentMarkThreshold(95)
                .build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.save(course)).thenReturn(course);

        CourseDTO result = courseService.updateCourse(courseId, dto);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getGoodMarkThreshold()).isEqualTo(70);
        assertThat(course.getExcellentMarkThreshold()).isEqualTo(95);
    }

    @Test
    void addStudentsToCourse_whenSomeMissing_throws() {
        UUID studentId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        User student = User.builder().id(studentId).role(Role.STUDENT).build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findAllById(List.of(studentId, missingId))).thenReturn(List.of(student));

        assertThatThrownBy(() -> courseService.addStudentsToCourse(courseId, List.of(studentId, missingId)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(missingId.toString());

        verify(studentsInCourseRepository, never()).saveAll(any());
    }

    @Test
    void addStudentsToCourse_savesOnlyNotEnrolledStudents() {
        UUID firstStudentId = UUID.randomUUID();
        UUID secondStudentId = UUID.randomUUID();
        User firstStudent = User.builder().id(firstStudentId).role(Role.STUDENT).build();
        User secondStudent = User.builder().id(secondStudentId).role(Role.STUDENT).build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findAllById(List.of(firstStudentId, secondStudentId))).thenReturn(List.of(firstStudent, secondStudent));
        when(studentsInCourseRepository.findAlreadyEnrolledStudentIds(courseId, List.of(firstStudentId, secondStudentId)))
                .thenReturn(List.of(firstStudentId));
        when(studentsInCourseRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = courseService.addStudentsToCourse(courseId, List.of(firstStudentId, secondStudentId));

        assertThat(result).isTrue();
        ArgumentCaptor<List<StudentsInCourse>> captor = ArgumentCaptor.forClass(List.class);
        verify(studentsInCourseRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(enrollment -> {
            assertThat(enrollment.getStudent()).isEqualTo(secondStudent);
            assertThat(enrollment.getCourse()).isEqualTo(course);
            assertThat(enrollment.getCourseGrade()).isEqualTo(0D);
        });
    }

    @Test
    void deleteCourse_whenMissing_throws() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse(courseId))
                .isInstanceOf(CourseNotFound.class);
    }
}
