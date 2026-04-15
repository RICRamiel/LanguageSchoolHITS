package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.infrastructure.NotificationsServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
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
class NotificationsServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private NotificationsServiceImpl notificationsService;

    private UUID courseId;
    private UUID teacherId;
    private UUID studentId;
    private Course course;
    private User teacher;
    private User student;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        studentId = UUID.randomUUID();

        course = Course.builder().id(courseId).name("Course").build();
        teacher = User.builder().id(teacherId).build();
        student = User.builder().id(studentId).build();
    }

    @Test
    void getAllCourseNotifications_returnsRepositoryResult() {
        Notification notification = new Notification();
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(notificationRepository.findByCourse(course)).thenReturn(List.of(notification));

        assertThat(notificationsService.getAllCourseNotifications(courseId)).containsExactly(notification);
    }

    @Test
    void getAllStudentsNotifications_collectsCoursesFromEnrollments() {
        StudentsInCourse enrollment = StudentsInCourse.builder().course(course).student(student).build();
        student.setCourse(List.of(enrollment));
        Notification notification = new Notification();
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(notificationRepository.findByCourseIn(List.of(course))).thenReturn(List.of(notification));

        assertThat(notificationsService.getAllStudentsNotifications(studentId)).containsExactly(notification);
    }

    @Test
    void getAllStudentsNotifications_withoutCourses_returnsEmptyList() {
        student.setCourse(List.of());
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        assertThat(notificationsService.getAllStudentsNotifications(studentId)).isEmpty();
        verify(notificationRepository, never()).findByCourseIn(any());
    }

    @Test
    void createNotification_savesNotificationAndUploadsFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
        NotificationCreationModel model = NotificationCreationModel.builder()
                .text("New notification")
                .courseId(courseId)
                .files(List.of(file))
                .build();
        UserFullDTO me = UserFullDTO.builder().id(teacherId).build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        Notification result = notificationsService.createNotification(model, me);

        assertThat(result.getText()).isEqualTo("New notification");
        assertThat(result.getCourse()).isEqualTo(course);
        assertThat(result.getCreatedBy()).isEqualTo(teacher);
        assertThat(result.getCreationDate()).isEqualTo(LocalDate.now());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        verify(attachmentService).uploadAttachmentForNotification(result.getId(), file, teacherId);
        assertThat(captor.getValue().getCreationDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void createNotification_whenCourseMissing_throws() {
        NotificationCreationModel model = NotificationCreationModel.builder().courseId(courseId).text("x").build();
        UserFullDTO me = UserFullDTO.builder().id(teacherId).build();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationsService.createNotification(model, me))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }
}
