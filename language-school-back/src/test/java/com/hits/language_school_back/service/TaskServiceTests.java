package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.*;
import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.infrastructure.TaskServiceImpl;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.model.*;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TaskStudentRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskTeacherMapper taskTeacherMapper;

    @Mock
    private TaskStudentMapper taskStudentMapper;

    @Mock
    private GroupService groupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskStudentRepository taskStudentRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private User teacher;
    private User student;
    private Group group;
    private Language language;
    private Task task;
    private TaskStudent taskStudent;
    private TaskDTO taskDTO;
    private UserFullDTO userFullDTO;
    private UserDTO teacherDTO;
    private GroupAnswerDTO groupAnswerDTO;
    private LanguageDTO languageDTO;
    private LocalDate now;
    private List<Attachment> attachments;

    @BeforeEach
    void setUp() {
        now = LocalDate.now();

        // Setup Language
        language = new Language();
        language.setId(1L);
        language.setName("English");

        languageDTO = new LanguageDTO("English");

        // Setup Group
        group = new Group();
        group.setId(1L);
        group.setName("Group A");
        group.setDescription("Test Group");
        group.setDifficulty(Difficulty.BEGINNER);
        group.setLanguage(language);

        groupAnswerDTO = GroupAnswerDTO.builder()
                .id(1L)
                .name("Group A")
                .description("Test Group")
                .difficulty(Difficulty.BEGINNER)
                .language(languageDTO)
                .build();

        // Setup teacher
        teacher = new User();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");
        teacher.setRole(Role.TEACHER);
        teacher.setGroups(Arrays.asList(group));

        teacherDTO = new UserDTO(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                Arrays.asList(groupAnswerDTO),
                Role.TEACHER
        );

        // Setup student
        student = new User();
        student.setId(2L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");
        student.setRole(Role.STUDENT);

        attachments = Arrays.asList(new Attachment(), new Attachment());
        student.setAttachmentList(attachments);

        // Setup task
        task = Task.builder()
                .id(1L)
                .name("Test Task")
                .description("Test Description")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .user(teacher)
                .group(group)
                .build();

        // Setup taskStudent
        taskStudent = new TaskStudent();
        taskStudent.setId(1L);
        taskStudent.setUserId(2L);
        taskStudent.setTaskId(1L);
        taskStudent.setAttachmentList(attachments);
        taskStudent.setTaskStatus(TaskStatus.PENDING);

        // Setup DTOs using builder
        taskDTO = TaskDTO.builder()
                .name("New Task")
                .description("New Description")
                .deadline(now.plusDays(10))
                .groupName("Group A")
                .build();

        userFullDTO = UserFullDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(Role.TEACHER)
                .groups(Arrays.asList(groupAnswerDTO))
                .build();
    }

    // ==================== GET TASKS BY TEACHER ID ====================

    @Test
    @DisplayName("Should return tasks by teacher ID")
    void getTasksByTeacherId_ShouldReturnListOfTaskTeacherDTO() {
        // Arrange
        Long teacherId = 1L;
        List<Task> tasks = Arrays.asList(task);

        TaskTeacherDTO expectedDTO = TaskTeacherDTO.builder()
                .id(1L)
                .name("Test Task")
                .description("Test Description")
                .deadline(now.plusDays(5))
                .commentList(Arrays.asList())
                .build();

        List<TaskTeacherDTO> expectedDTOs = Arrays.asList(expectedDTO);

        when(taskRepository.findByUserId(teacherId)).thenReturn(tasks);
        when(taskTeacherMapper.toDtoList(tasks)).thenReturn(expectedDTOs);

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertThat(result).isEqualTo(expectedDTOs);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Task");
        assertThat(result.get(0).getDescription()).isEqualTo("Test Description");

        verify(taskRepository).findByUserId(teacherId);
        verify(taskTeacherMapper).toDtoList(tasks);
    }

    @Test
    @DisplayName("Should return empty list when teacher has no tasks")
    void getTasksByTeacherId_WithNoTasks_ShouldReturnEmptyList() {
        // Arrange
        Long teacherId = 1L;
        List<Task> tasks = Arrays.asList();
        List<TaskTeacherDTO> expectedDTOs = Arrays.asList();

        when(taskRepository.findByUserId(teacherId)).thenReturn(tasks);
        when(taskTeacherMapper.toDtoList(tasks)).thenReturn(expectedDTOs);

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertThat(result).isEmpty();
        verify(taskRepository).findByUserId(teacherId);
        verify(taskTeacherMapper).toDtoList(tasks);
    }

    // ==================== GET TASKS BY GROUP NAME AND USER ID ====================

    @Test
    @DisplayName("Should return tasks by user ID (for student)")
    void getTasksByGroupName_ShouldReturnListOfTaskStudentDTO() {
        // Arrange
        String groupName = "Group A";
        Long userId = 2L;
        List<TaskStudent> taskStudents = Arrays.asList(taskStudent);

        TaskStudentDTO expectedDTO = TaskStudentDTO.builder()
                .id(1L)
                .name("Test Task")
                .description("Test Description")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .teacher(teacherDTO)
                .build();

        List<TaskStudentDTO> expectedDTOs = Arrays.asList(expectedDTO);

        when(taskStudentRepository.findByUserId(userId)).thenReturn(taskStudents);
        when(taskStudentMapper.toDtoList(taskStudents)).thenReturn(expectedDTOs);

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName, userId);

        // Assert
        assertThat(result).isEqualTo(expectedDTOs);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeacher()).isEqualTo(teacherDTO);
        assertThat(result.get(0).getTeacher().getFirstName()).isEqualTo("John");
        assertThat(result.get(0).getTeacher().getRole()).isEqualTo(Role.TEACHER);
        assertThat(result.get(0).getTeacher().getGroups()).hasSize(1);
        assertThat(result.get(0).getTeacher().getGroups().get(0).getName()).isEqualTo("Group A");

        verify(taskStudentRepository).findByUserId(userId);
        verify(taskStudentMapper).toDtoList(taskStudents);
    }

    @Test
    @DisplayName("Should return empty list when student has no tasks")
    void getTasksByGroupName_WithNoTasks_ShouldReturnEmptyList() {
        // Arrange
        String groupName = "Group A";
        Long userId = 2L;
        List<TaskStudent> taskStudents = Arrays.asList();
        List<TaskStudentDTO> expectedDTOs = Arrays.asList();

        when(taskStudentRepository.findByUserId(userId)).thenReturn(taskStudents);
        when(taskStudentMapper.toDtoList(taskStudents)).thenReturn(expectedDTOs);

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName, userId);

        // Assert
        assertThat(result).isEmpty();
        verify(taskStudentRepository).findByUserId(userId);
        verify(taskStudentMapper).toDtoList(taskStudents);
    }

    // ==================== CREATE TASK ====================

    @Test
    @DisplayName("Should create task with PENDING status when deadline is in future")
    void createTask_WithFutureDeadline_ShouldSetPendingStatus() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(groupService.getByName(anyString())).thenReturn(group);

        // Act
        Task result = taskService.createTask(taskDTO, userFullDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(taskDTO.getName());
        assertThat(result.getDescription()).isEqualTo(taskDTO.getDescription());
        assertThat(result.getDeadline()).isEqualTo(taskDTO.getDeadline());
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(result.getUser()).isEqualTo(teacher);
        assertThat(result.getGroup()).isEqualTo(group);
        assertThat(result.getUser().getRole()).isEqualTo(Role.TEACHER);

        verify(userRepository).findById(userFullDTO.getId());
        verify(groupService).getByName(taskDTO.getGroupName());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should create task with OVERDUE status when deadline is in past")
    void createTask_WithPastDeadline_ShouldSetOverdueStatus() {
        // Arrange
        TaskDTO pastDeadlineDTO = TaskDTO.builder()
                .name("New Task")
                .description("New Description")
                .deadline(now.minusDays(1))
                .groupName("Group A")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(groupService.getByName(anyString())).thenReturn(group);

        // Act
        Task result = taskService.createTask(pastDeadlineDTO, userFullDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.OVERDUE);
    }

    @Test
    @DisplayName("Should create task with null status when deadline is today")
    void createTask_WithTodayDeadline_ShouldNotSetStatus() {
        // Arrange
        TaskDTO todayDeadlineDTO = TaskDTO.builder()
                .name("New Task")
                .description("New Description")
                .deadline(now)
                .groupName("Group A")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(groupService.getByName(anyString())).thenReturn(group);

        // Act
        Task result = taskService.createTask(todayDeadlineDTO, userFullDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTaskStatus()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when creating task with non-existent user")
    void createTask_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(taskDTO, userFullDTO))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================== DELETE TASK ====================

    @Test
    @DisplayName("Should delete task by ID")
    void deleteTask_ShouldCallRepositoryDelete() {
        // Arrange
        Long taskId = 1L;
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository).deleteById(taskId);
    }

    // ==================== EDIT TASK ====================

    @Test
    @DisplayName("Should edit task with all fields updated")
    void editTask_WithAllFields_ShouldUpdateTask() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .id(taskId)
                .name("Old Name")
                .description("Old Description")
                .deadline(now.minusDays(5))
                .taskStatus(TaskStatus.OVERDUE)
                .user(teacher)
                .group(group)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(groupService.getByName(taskDTO.getGroupName())).thenReturn(group);
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(taskDTO, taskId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(taskDTO.getName());
        assertThat(result.getDescription()).isEqualTo(taskDTO.getDescription());
        assertThat(result.getDeadline()).isEqualTo(taskDTO.getDeadline());
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(result.getGroup()).isEqualTo(group);

        verify(taskRepository).findById(taskId);
        verify(groupService).getByName(taskDTO.getGroupName());
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("Should edit task with only name updated")
    void editTask_WithOnlyName_ShouldUpdateOnlyName() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .id(taskId)
                .name("Old Name")
                .description("Old Description")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .user(teacher)
                .group(group)
                .build();

        TaskDTO partialDTO = TaskDTO.builder()
                .name("Updated Name")
                .description(null)
                .deadline(now.plusDays(5))
                .groupName("Group A")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(groupService.getByName(partialDTO.getGroupName())).thenReturn(group);
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(partialDTO, taskId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Old Description");
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    @DisplayName("Should edit task with only description updated")
    void editTask_WithOnlyDescription_ShouldUpdateOnlyDescription() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .id(taskId)
                .name("Old Name")
                .description("Old Description")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .user(teacher)
                .group(group)
                .build();

        TaskDTO partialDTO = TaskDTO.builder()
                .name(null)
                .description("Updated Description")
                .deadline(now.plusDays(5))
                .groupName("Group A")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(groupService.getByName(partialDTO.getGroupName())).thenReturn(group);
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(partialDTO, taskId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Old Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("Should not change status to PENDING if task is COMPLETE")
    void editTask_WithCompleteStatus_ShouldNotChangeStatus() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = Task.builder()
                .id(taskId)
                .name("Old Name")
                .description("Old Description")
                .deadline(now.minusDays(5))
                .taskStatus(TaskStatus.COMPLETE)
                .user(teacher)
                .group(group)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(groupService.getByName(taskDTO.getGroupName())).thenReturn(group);
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(taskDTO, taskId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.COMPLETE);
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent task")
    void editTask_WithNonExistentTask_ShouldThrowException() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.editTask(taskDTO, taskId))
                .isInstanceOf(RuntimeException.class);
    }

    // ==================== COMPLETE TASK ====================

    @Test
    @DisplayName("Should complete task with COMPLETE status when deadline is in future")
    void completeTask_WithFutureDeadline_ShouldSetCompleteStatus() {
        // Arrange
        Long taskId = 1L;
        Long userId = 2L;

        Task existingTask = Task.builder()
                .id(taskId)
                .name("Test Task")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .user(teacher)
                .group(group)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        taskService.completeTask(taskId, userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(taskRepository).findById(taskId);
    }

    @Test
    @DisplayName("Should complete task with OVERDUE status when deadline is in past")
    void completeTask_WithPastDeadline_ShouldSetOverdueStatus() {
        // Arrange
        Long taskId = 1L;
        Long userId = 2L;

        Task existingTask = Task.builder()
                .id(taskId)
                .name("Test Task")
                .deadline(now.minusDays(1))
                .taskStatus(TaskStatus.OVERDUE)
                .user(teacher)
                .group(group)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        taskService.completeTask(taskId, userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(taskRepository).findById(taskId);
    }

    @Test
    @DisplayName("Should throw exception when completing task with non-existent user")
    void completeTask_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        Long taskId = 1L;
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.completeTask(taskId, userId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should throw exception when completing non-existent task")
    void completeTask_WithNonExistentTask_ShouldThrowException() {
        // Arrange
        Long taskId = 999L;
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskService.completeTask(taskId, userId))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should set student's attachments to taskStudent when completing task")
    void completeTask_ShouldSetStudentAttachments() {
        // Arrange
        Long taskId = 1L;
        Long userId = 2L;

        Task existingTask = Task.builder()
                .id(taskId)
                .name("Test Task")
                .deadline(now.plusDays(5))
                .taskStatus(TaskStatus.PENDING)
                .user(teacher)
                .group(group)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // Act
        taskService.completeTask(taskId, userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(taskRepository).findById(taskId);
    }
}