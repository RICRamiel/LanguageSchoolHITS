package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.infrastructure.NotificationsServiceImpl;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationsServiceTest {
//
//    @Mock
//    private NotificationRepository notificationRepository;
//
//    @Mock
//    private GroupRepository groupRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @InjectMocks
//    private NotificationsServiceImpl notificationsService;
//
//    @Captor
//    private ArgumentCaptor<Notification> notificationCaptor;
//
//    private Group group;
//    private User student;
//    private User teacher;
//    private Notification notification;
//    private UserFullDTO userFullDTO;
//
//    @BeforeEach
//    void setUp() {
//        group = new Group();
//        group.setId(1L);
//        group.setName("Test Group");
//
//        student = new User();
//        student.setId(1L);
//        student.setFirstName("John");
//        student.setLastName("Doe");
//
//        teacher = new User();
//        teacher.setId(2L);
//        teacher.setFirstName("Jane");
//        teacher.setLastName("Smith");
//
//        notification = new Notification();
//        notification.setId(UUID.randomUUID());
//        notification.setText("Test notification");
//        notification.setGroup(group);
//        notification.setCreatedBy(teacher);
//        notification.setCreationDate(LocalDate.now());
//
//        userFullDTO = new UserFullDTO();
//        userFullDTO.setId(2L);
//        userFullDTO.setFirstName("Jane");
//        userFullDTO.setLastName("Smith");
//    }
//
//    @Nested
//    class GetAllGroupNotificationsTests {
//
//        @Test
//        void shouldReturnAllNotificationsForGroup() {
//            // Arrange
//            Long groupId = 1L;
//            List<Notification> expectedNotifications = List.of(notification);
//
//            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
//            when(notificationRepository.findByGroup(group)).thenReturn(expectedNotifications);
//
//            // Act
//            List<Notification> result = notificationsService.getAllGroupNotifications(groupId);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).hasSize(1);
//            assertThat(result.get(0).getText()).isEqualTo("Test notification");
//            assertThat(result.get(0).getGroup()).isEqualTo(group);
//
//            verify(groupRepository).findById(groupId);
//            verify(notificationRepository).findByGroup(group);
//        }
//
//        @Test
//        void shouldReturnEmptyListWhenNoNotificationsForGroup() {
//            // Arrange
//            Long groupId = 1L;
//
//            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
//            when(notificationRepository.findByGroup(group)).thenReturn(List.of());
//
//            // Act
//            List<Notification> result = notificationsService.getAllGroupNotifications(groupId);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).isEmpty();
//
//            verify(groupRepository).findById(groupId);
//            verify(notificationRepository).findByGroup(group);
//        }
//
//        @Test
//        void shouldThrowExceptionWhenGroupNotFound() {
//            // Arrange
//            Long groupId = 999L;
//
//            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
//
//            // Act & Assert
//            assertThatThrownBy(() -> notificationsService.getAllGroupNotifications(groupId))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("Group not found with id: " + groupId);
//
//            verify(groupRepository).findById(groupId);
//            verify(notificationRepository, never()).findByGroup(any());
//        }
//    }
//
//    @Nested
//    class GetAllStudentsNotificationsTests {
//
//        @Test
//        void shouldReturnAllNotificationsForStudentFromAllHisGroups() {
//            // Arrange
//            Long studentId = 1L;
//
//            Group group2 = new Group();
//            group2.setId(2L);
//            group2.setName("Test Group 2");
//
//            List<Group> studentGroups = List.of(group, group2);
//            student.setGroups(studentGroups);
//
//            Notification notification2 = new Notification();
//            notification2.setId(UUID.randomUUID());
//            notification2.setText("Test notification 2");
//            notification2.setGroup(group2);
//
//            List<Notification> expectedNotifications = List.of(notification, notification2);
//
//            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//            when(notificationRepository.findByGroupIn(studentGroups)).thenReturn(expectedNotifications);
//
//            // Act
//            List<Notification> result = notificationsService.getAllStudentsNotifications(studentId);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).hasSize(2);
//            assertThat(result).containsExactlyInAnyOrder(notification, notification2);
//
//            verify(userRepository).findById(studentId);
//            verify(notificationRepository).findByGroupIn(studentGroups);
//        }
//
//        @Test
//        void shouldReturnEmptyListWhenStudentHasNoGroups() {
//            // Arrange
//            Long studentId = 1L;
//            student.setGroups(List.of());
//
//            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//
//            // Act
//            List<Notification> result = notificationsService.getAllStudentsNotifications(studentId);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).isEmpty();
//
//            verify(userRepository).findById(studentId);
//            verify(notificationRepository, never()).findByGroupIn(any());
//        }
//
//        @Test
//        void shouldReturnEmptyListWhenNoNotificationsInStudentsGroups() {
//            // Arrange
//            Long studentId = 1L;
//            List<Group> studentGroups = List.of(group);
//            student.setGroups(studentGroups);
//
//            when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//            when(notificationRepository.findByGroupIn(studentGroups)).thenReturn(List.of());
//
//            // Act
//            List<Notification> result = notificationsService.getAllStudentsNotifications(studentId);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).isEmpty();
//
//            verify(userRepository).findById(studentId);
//            verify(notificationRepository).findByGroupIn(studentGroups);
//        }
//
//        @Test
//        void shouldThrowExceptionWhenStudentNotFound() {
//            // Arrange
//            Long studentId = 999L;
//
//            when(userRepository.findById(studentId)).thenReturn(Optional.empty());
//
//            // Act & Assert
//            assertThatThrownBy(() -> notificationsService.getAllStudentsNotifications(studentId))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("Student not found with id: " + studentId);
//
//            verify(userRepository).findById(studentId);
//            verify(notificationRepository, never()).findByGroupIn(any());
//        }
//    }
//
//    @Nested
//    class CreateNotificationTests {
//
//        @Test
//        void shouldCreateNotificationSuccessfully() {
//            // Arrange
//            NotificationCreationModel model = new NotificationCreationModel();
//            model.setGroupId(1L);
//            model.setText("New test notification");
//            model.setFiles(List.of());
//
//            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
//            when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
//            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
//                Notification savedNotification = invocation.getArgument(0);
//                savedNotification.setId(UUID.randomUUID());
//                return savedNotification;
//            });
//
//            // Act
//            Notification result = notificationsService.createNotification(model, userFullDTO);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result.getId()).isNotNull();
//            assertThat(result.getText()).isEqualTo("New test notification");
//            assertThat(result.getGroup()).isEqualTo(group);
//            assertThat(result.getCreatedBy()).isEqualTo(teacher);
//            assertThat(result.getCreationDate()).isEqualTo(LocalDate.now());
//
//            verify(groupRepository).findById(1L);
//            verify(userRepository).findById(2L);
//            verify(notificationRepository).save(notificationCaptor.capture());
//
//            Notification capturedNotification = notificationCaptor.getValue();
//            assertThat(capturedNotification.getText()).isEqualTo("New test notification");
//            assertThat(capturedNotification.getGroup()).isEqualTo(group);
//            assertThat(capturedNotification.getCreatedBy()).isEqualTo(teacher);
//            assertThat(capturedNotification.getCreationDate()).isEqualTo(LocalDate.now());
//        }
//
//        @Test
//        void shouldThrowExceptionWhenGroupNotFound() {
//            // Arrange
//            NotificationCreationModel model = new NotificationCreationModel();
//            model.setGroupId(999L);
//            model.setText("New test notification");
//
//            when(groupRepository.findById(999L)).thenReturn(Optional.empty());
//
//            // Act & Assert
//            assertThatThrownBy(() -> notificationsService.createNotification(model, userFullDTO))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("Group not found with id: 999");
//
//            verify(groupRepository).findById(999L);
//            verify(userRepository, never()).findById(any());
//            verify(notificationRepository, never()).save(any());
//        }
//
//        @Test
//        void shouldThrowExceptionWhenCreatorUserNotFound() {
//            // Arrange
//            NotificationCreationModel model = new NotificationCreationModel();
//            model.setGroupId(1L);
//            model.setText("New test notification");
//
//            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
//            when(userRepository.findById(2L)).thenReturn(Optional.empty());
//
//            // Act & Assert
//            assertThatThrownBy(() -> notificationsService.createNotification(model, userFullDTO))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessageContaining("User not found with id: 2");
//
//            verify(groupRepository).findById(1L);
//            verify(userRepository).findById(2L);
//            verify(notificationRepository, never()).save(any());
//        }
//
//        @Test
//        void shouldSetCreationDateToCurrentDate() {
//            // Arrange
//            NotificationCreationModel model = new NotificationCreationModel();
//            model.setGroupId(1L);
//            model.setText("Test");
//            model.setFiles(List.of());
//            LocalDate testDate = LocalDate.now();
//
//            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
//            when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
//            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
//
//            // Act
//            Notification result = notificationsService.createNotification(model, userFullDTO);
//
//            // Assert
//            verify(notificationRepository).save(argThat(n ->
//                n.getCreationDate() != null &&
//                n.getCreationDate().equals(testDate)
//            ));
//        }
//    }
}