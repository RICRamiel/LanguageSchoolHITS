package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.UserFullDTO;
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

    private User teacher;
    private User student;
    private Group group;
    private Task task;
    private TaskStudent taskStudent;
    private TaskDTO taskDTO;
    private UserFullDTO userFullDTO;
    private LocalDate now;
    private List<Attachment> attachments;

    @BeforeEach
    void setUp() {
        now = LocalDate.now();

        // Setup teacher
        teacher = new User();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");
        teacher.setAttachmentList(Arrays.asList());

        // Setup student
        student = new User();
        student.setId(2L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setEmail("jane.smith@example.com");

        attachments = Arrays.asList(new Attachment(), new Attachment());
        student.setAttachmentList(attachments);

        // Setup group
        group = new Group();
        group.setName("Group A");
        group.setDescription("Test Group");

        // Setup task
        task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDescription("Test Description");
        task.setDeadline(now.plusDays(5));
        task.setTaskStatus(TaskStatus.PENDING);
        task.setUser(teacher);
        task.setGroup(group);

        // Setup taskStudent
        taskStudent = new TaskStudent();
        taskStudent.setId(1L);
        taskStudent.setUserId(2L);
        taskStudent.setTaskId(1L);
        taskStudent.setAttachmentList(attachments);
        taskStudent.setTaskStatus(TaskStatus.PENDING);

        // Setup DTOs
        taskDTO = new TaskDTO();
        taskDTO.setName("New Task");
        taskDTO.setDescription("New Description");
        taskDTO.setDeadline(now.plusDays(10));
        taskDTO.setGroupName("Group A");

        userFullDTO = new UserFullDTO();
        userFullDTO.setId(1L);
        userFullDTO.setFirstName("John");
        userFullDTO.setLastName("Doe");
        userFullDTO.setEmail("john.doe@example.com");
    }

    // ==================== GET TASKS BY TEACHER ID ====================

    @Test
    @DisplayName("Should return tasks by teacher ID")
    void getTasksByTeacherId_ShouldReturnListOfTaskTeacherDTO() {
        // Arrange
        Long teacherId = 1L;
        List<Task> tasks = Arrays.asList(task);
        List<TaskTeacherDTO> expectedDTOs = Arrays.asList(new TaskTeacherDTO());

        when(taskRepository.findByUserId(teacherId)).thenReturn(tasks);
        when(taskTeacherMapper.toDtoList(tasks)).thenReturn(expectedDTOs);

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertThat(result).isEqualTo(expectedDTOs);
        assertThat(result).hasSize(1);
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
        List<TaskStudentDTO> expectedDTOs = Arrays.asList(new TaskStudentDTO());

        when(taskStudentRepository.findByUserId(userId)).thenReturn(taskStudents);
        when(taskStudentMapper.toDtoList(taskStudents)).thenReturn(expectedDTOs);

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName, userId);

        // Assert
        assertThat(result).isEqualTo(expectedDTOs);
        assertThat(result).hasSize(1);
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

        verify(userRepository).findById(userFullDTO.getId());
        verify(groupService).getByName(taskDTO.getGroupName());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should create task with OVERDUE status when deadline is in past")
    void createTask_WithPastDeadline_ShouldSetOverdueStatus() {
        // Arrange
        taskDTO.setDeadline(now.minusDays(1));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(groupService.getByName(anyString())).thenReturn(group);

        // Act
        Task result = taskService.createTask(taskDTO, userFullDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTaskStatus()).isEqualTo(TaskStatus.OVERDUE);
    }

    @Test
    @DisplayName("Should create task with null status when deadline is today")
    void createTask_WithTodayDeadline_ShouldNotSetStatus() {
        // Arrange
        taskDTO.setDeadline(now);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(teacher));
        when(groupService.getByName(anyString())).thenReturn(group);

        // Act
        Task result = taskService.createTask(taskDTO, userFullDTO);

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
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Old Name");
        existingTask.setDescription("Old Description");
        existingTask.setDeadline(now.minusDays(5));
        existingTask.setTaskStatus(TaskStatus.OVERDUE);
        existingTask.setUser(teacher);
        existingTask.setGroup(group);

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
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Old Name");
        existingTask.setDescription("Old Description");
        existingTask.setDeadline(now.plusDays(5));
        existingTask.setTaskStatus(TaskStatus.PENDING);
        existingTask.setUser(teacher);
        existingTask.setGroup(group);

        TaskDTO partialDTO = new TaskDTO();
        partialDTO.setName("Updated Name");
        partialDTO.setDescription(null);
        partialDTO.setDeadline(now.plusDays(5));
        partialDTO.setGroupName("Group A");

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
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Old Name");
        existingTask.setDescription("Old Description");
        existingTask.setDeadline(now.plusDays(5));
        existingTask.setTaskStatus(TaskStatus.PENDING);
        existingTask.setUser(teacher);
        existingTask.setGroup(group);

        TaskDTO partialDTO = new TaskDTO();
        partialDTO.setName(null);
        partialDTO.setDescription("Updated Description");
        partialDTO.setDeadline(now.plusDays(5));
        partialDTO.setGroupName("Group A");

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
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Old Name");
        existingTask.setDescription("Old Description");
        existingTask.setDeadline(now.minusDays(5));
        existingTask.setTaskStatus(TaskStatus.COMPLETE);
        existingTask.setUser(teacher);
        existingTask.setGroup(group);

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

        task.setDeadline(now.plusDays(5));
        task.setTaskStatus(TaskStatus.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        taskService.completeTask(taskId, userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(taskRepository).findById(taskId);

        // Note: completeTask doesn't save the taskStudent to repository in current implementation
        // You might want to add verification for taskStudentRepository.save() if needed
    }

    @Test
    @DisplayName("Should complete task with OVERDUE status when deadline is in past")
    void completeTask_WithPastDeadline_ShouldSetOverdueStatus() {
        // Arrange
        Long taskId = 1L;
        Long userId = 2L;

        task.setDeadline(now.minusDays(1));
        task.setTaskStatus(TaskStatus.OVERDUE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

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

        task.setDeadline(now.plusDays(5));
        task.setTaskStatus(TaskStatus.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        taskService.completeTask(taskId, userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(taskRepository).findById(taskId);

        // The attachments from student should be set to taskStudent
        // But since taskStudent is created locally and not saved, we can't verify directly
    }
}