package com.hits.language_school_back.service;

import com.hits.language_school_back.infrastructure.TaskServiceImpl;
import com.hits.language_school_back.model.*;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTests {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    private TaskDTO taskDTO;
    private Task task;
    private User teacher;
    private User student;
    private Group group;
    private Comment comment;
    private Attachment attachment;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных
        teacher = new User();
        teacher.setId(1L);
        teacher.setFirstName("Иван");
        teacher.setLastName("Петров");
        teacher.setEmail("ivan.petrov@example.com");
        teacher.setRole(Role.TEACHER);

        student = new User();
        student.setId(2L);
        student.setFirstName("Анна");
        student.setLastName("Сидорова");
        student.setEmail("anna.sidorova@example.com");
        student.setRole(Role.STUDENT);

        group = new Group();
        group.setId(1L);
        group.setName("Группа А-1");

        comment = new Comment();
        comment.setId(1L);
        comment.setText("Тестовый комментарий");
        comment.setUser(student);
        comment.setPrivateStatus(false);

        attachment = new Attachment();
        attachment.setId(1L);
        attachment.setFileName("test.pdf");
        attachment.setFileType("application/pdf");
        attachment.setFileSize(1024L);
        attachment.setObjectKey("tasks/test.pdf");
        attachment.setBucketName("tasks-bucket");

        task = new Task();
        task.setId(1L);
        task.setName("Тестовое задание");
        task.setDescription("Описание тестового задания");
        task.setUser(teacher);
        task.setGroup(group);
        task.setDeadline(LocalDate.now().plusDays(7));
        task.setTaskStatus(TaskStatus.COMPLETE); // Используем COMPLETE из enum

        // Устанавливаем связи
        task.setCommentList(List.of(comment));
        task.setAttachmentList(List.of(attachment));

        // Устанавливаем обратные связи
        attachment.setTask(task);
        comment.setTask(task);

        taskDTO = new TaskDTO();
        taskDTO.setName("Новое задание");
        taskDTO.setDescription("Описание нового задания");
        taskDTO.setGroupName("Группа А-1");
        taskDTO.setDeadline(LocalDate.now().plusDays(5));
    }

    @Test
    void getTasksByTeacherId_ShouldReturnListOfTaskTeacherDTO() {
        // Arrange
        Long teacherId = 1L;
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByUserId(teacherId)).thenReturn(tasks);

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        TaskTeacherDTO dto = result.get(0);
        assertEquals(task.getId(), dto.getId());
        assertEquals(task.getName(), dto.getName());
        assertEquals(task.getDescription(), dto.getDescription());
        assertEquals(task.getDeadline(), dto.getDeadline());

        // Проверяем комментарии
        assertNotNull(dto.getCommentList());
        assertEquals(1, dto.getCommentList().size());
        Comment resultComment = dto.getCommentList().get(0);
        assertEquals(comment.getId(), resultComment.getId());
        assertEquals(comment.getText(), resultComment.getText());
        assertEquals(comment.getUser(), resultComment.getUser());
        assertEquals(comment.isPrivateStatus(), resultComment.isPrivateStatus());

        verify(taskRepository, times(1)).findByUserId(teacherId);
    }

    @Test
    void getTasksByTeacherId_WhenNoTasks_ShouldReturnEmptyList() {
        // Arrange
        Long teacherId = 1L;
        when(taskRepository.findByUserId(teacherId)).thenReturn(List.of());

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByUserId(teacherId);
    }

    @Test
    void getTasksByTeacherId_WithMultipleTasks_ShouldReturnAllTasks() {
        // Arrange
        Long teacherId = 1L;

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setText("Второй комментарий");
        comment2.setUser(student);
        comment2.setPrivateStatus(true);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Второе задание");
        task2.setDescription("Описание второго задания");
        task2.setUser(teacher);
        task2.setGroup(group);
        task2.setDeadline(LocalDate.now().plusDays(5));
        task2.setTaskStatus(TaskStatus.OVERDUE);
        task2.setCommentList(List.of(comment2));
        task2.setAttachmentList(List.of());

        List<Task> tasks = Arrays.asList(task, task2);
        when(taskRepository.findByUserId(teacherId)).thenReturn(tasks);

        // Act
        List<TaskTeacherDTO> result = taskService.getTasksByTeacherId(teacherId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(task.getId(), result.get(0).getId());
        assertEquals(1, result.get(0).getCommentList().size());

        assertEquals(task2.getId(), result.get(1).getId());
        assertEquals(1, result.get(1).getCommentList().size());
        assertTrue(result.get(1).getCommentList().get(0).isPrivateStatus());

        verify(taskRepository, times(1)).findByUserId(teacherId);
    }

    @Test
    void getTasksByGroupName_ShouldReturnListOfTaskStudentDTO() {
        // Arrange
        String groupName = "Группа А-1";
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByGroupName(groupName)).thenReturn(tasks);

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        TaskStudentDTO dto = result.get(0);
        assertEquals(task.getId(), dto.getId());
        assertEquals(task.getName(), dto.getName());
        assertEquals(task.getDescription(), dto.getDescription());
        assertEquals(task.getDeadline(), dto.getDeadline());
        assertEquals(task.getTaskStatus(), dto.getTaskStatus());
        assertEquals("Сдано", dto.getTaskStatus().getDisplayName()); // Проверяем displayName
        assertEquals(teacher, dto.getTeacher());
        assertEquals(teacher.getFirstName() + " " + teacher.getLastName(),
                dto.getTeacher().getFirstName() + " " + dto.getTeacher().getLastName());

        verify(taskRepository, times(1)).findByGroupName(groupName);
    }

    @Test
    void getTasksByGroupName_WhenNoTasks_ShouldReturnEmptyList() {
        // Arrange
        String groupName = "Группа А-1";
        when(taskRepository.findByGroupName(groupName)).thenReturn(List.of());

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByGroupName(groupName);
    }

    @Test
    void getTasksByGroupName_WithMultipleTasks_ShouldReturnAllTasks() {
        // Arrange
        String groupName = "Группа А-1";

        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Второе задание");
        task2.setDescription("Описание второго задания");
        task2.setUser(teacher);
        task2.setGroup(group);
        task2.setDeadline(LocalDate.now().plusDays(5));
        task2.setTaskStatus(TaskStatus.OVERDUE);
        task2.setCommentList(List.of());
        task2.setAttachmentList(List.of());

        List<Task> tasks = Arrays.asList(task, task2);
        when(taskRepository.findByGroupName(groupName)).thenReturn(tasks);

        // Act
        List<TaskStudentDTO> result = taskService.getTasksByGroupName(groupName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(task.getId(), result.get(0).getId());
        assertEquals(TaskStatus.COMPLETE, result.get(0).getTaskStatus());
        assertEquals("Сдано", result.get(0).getTaskStatus().getDisplayName());

        assertEquals(task2.getId(), result.get(1).getId());
        assertEquals(TaskStatus.OVERDUE, result.get(1).getTaskStatus());
        assertEquals("Просрочено", result.get(1).getTaskStatus().getDisplayName());

        verify(taskRepository, times(1)).findByGroupName(groupName);
    }

    @Test
    void createTask_ShouldCreateAndReturnTask() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findByName("Группа А-1")).thenReturn(Optional.of(group));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L);
            // По умолчанию статус может быть не установлен, или устанавливается в сервисе
            return savedTask;
        });

        // Act
        Task result = taskService.createTask(taskDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(taskDTO.getName(), result.getName());
        assertEquals(taskDTO.getDescription(), result.getDescription());
        assertEquals(taskDTO.getDeadline(), result.getDeadline());
        assertEquals(teacher, result.getUser());
        assertEquals(group, result.getGroup());
        assertNotNull(result.getCommentList());
        assertNotNull(result.getAttachmentList());
        assertTrue(result.getCommentList().isEmpty());
        assertTrue(result.getAttachmentList().isEmpty());

        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findByName("Группа А-1");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WhenUserIsNotTeacher_ShouldThrowException() {
        // Arrange
        teacher.setRole(Role.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> taskService.createTask(taskDTO));
        assertEquals("User with id 1 is not a teacher", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, never()).findByName(anyString());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_WhenDeadlineIsInPast_ShouldThrowException() {
        // Arrange
        taskDTO.setDeadline(LocalDate.now().minusDays(1));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findByName("Группа А-1")).thenReturn(Optional.of(group));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> taskService.createTask(taskDTO));
        assertEquals("Deadline cannot be in the past", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findByName("Группа А-1");
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).existsById(taskId);
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    void editTask_ShouldUpdateAndReturnTask() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Старое название");
        existingTask.setDescription("Старое описание");
        existingTask.setUser(teacher);
        existingTask.setGroup(group);
        existingTask.setDeadline(LocalDate.now().minusDays(1));
        existingTask.setTaskStatus(TaskStatus.COMPLETE);
        existingTask.setCommentList(List.of(comment));
        existingTask.setAttachmentList(List.of(attachment));

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findByName("Группа А-1")).thenReturn(Optional.of(group));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(taskDTO, taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals(taskDTO.getName(), result.getName());
        assertEquals(taskDTO.getDescription(), result.getDescription());
        assertEquals(taskDTO.getDeadline(), result.getDeadline());
        assertEquals(teacher, result.getUser());
        assertEquals(group, result.getGroup());
        assertEquals(TaskStatus.COMPLETE, result.getTaskStatus());
        assertEquals(1, result.getCommentList().size());
        assertEquals(1, result.getAttachmentList().size());

        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findByName("Группа А-1");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void editTask_WhenUserIsNotTeacher_ShouldThrowException() {
        // Arrange
        Long taskId = 1L;
        teacher.setRole(Role.STUDENT);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> taskService.editTask(taskDTO, taskId));
        assertEquals("User with id 1 is not a teacher", exception.getMessage());

        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, never()).findByName(anyString());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void editTask_WhenDeadlineIsInPast_ShouldThrowException() {
        // Arrange
        Long taskId = 1L;
        taskDTO.setDeadline(LocalDate.now().minusDays(1));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(groupRepository.findByName("Группа А-1")).thenReturn(Optional.of(group));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> taskService.editTask(taskDTO, taskId));
        assertEquals("Deadline cannot be in the past", exception.getMessage());

        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findByName("Группа А-1");
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void editTask_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        Long taskId = 1L;
        LocalDate originalDeadline = LocalDate.now().plusDays(10);
        User originalTeacher = teacher;
        Group originalGroup = group;
        TaskStatus originalStatus = TaskStatus.COMPLETE;

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setName("Старое название");
        existingTask.setDescription("Старое описание");
        existingTask.setUser(originalTeacher);
        existingTask.setGroup(originalGroup);
        existingTask.setDeadline(originalDeadline);
        existingTask.setTaskStatus(originalStatus);
        existingTask.setCommentList(List.of(comment));
        existingTask.setAttachmentList(List.of(attachment));

        TaskDTO partialUpdateDTO = new TaskDTO();
        partialUpdateDTO.setName("Обновленное название");

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        // Act
        Task result = taskService.editTask(partialUpdateDTO, taskId);

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertEquals("Обновленное название", result.getName());
        assertEquals("Старое описание", result.getDescription());
        assertEquals(originalDeadline, result.getDeadline());
        assertEquals(originalTeacher, result.getUser());
        assertEquals(originalGroup, result.getGroup());
        assertEquals(originalStatus, result.getTaskStatus());
        assertEquals(1, result.getCommentList().size());
        assertEquals(1, result.getAttachmentList().size());

        verify(taskRepository, times(1)).findById(taskId);
        verify(userRepository, never()).findById(anyLong());
        verify(groupRepository, never()).findByName(anyString());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

//    @Test
//    void updateTaskStatus_ShouldUpdateStatus() {
//        // Arrange
//        Long taskId = 1L;
//        TaskStatus newStatus = TaskStatus.OVERDUE;
//
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
//        when(taskRepository.save(any(Task.class))).thenReturn(task);
//
//        // Act
//        Task result = taskService.updateTaskStatus(taskId, newStatus);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(newStatus, result.getTaskStatus());
//        assertEquals("Просрочено", result.getTaskStatus().getDisplayName());
//
//        verify(taskRepository, times(1)).findById(taskId);
//        verify(taskRepository, times(1)).save(any(Task.class));
//    }


    @Test
    void getTaskWithAttachments_ShouldReturnTaskWithAttachments() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        Task result = taskRepository.findById(taskId).orElseThrow();

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getId());
        assertNotNull(result.getAttachmentList());
        assertEquals(1, result.getAttachmentList().size());

        Attachment resultAttachment = result.getAttachmentList().get(0);
        assertEquals(attachment.getId(), resultAttachment.getId());
        assertEquals(attachment.getFileName(), resultAttachment.getFileName());
        assertEquals(attachment.getFileType(), resultAttachment.getFileType());
        assertEquals(attachment.getFileSize(), resultAttachment.getFileSize());
        assertEquals(attachment.getObjectKey(), resultAttachment.getObjectKey());
        assertEquals(attachment.getBucketName(), resultAttachment.getBucketName());
        assertEquals(task, resultAttachment.getTask());

        verify(taskRepository, times(1)).findById(taskId);
    }
}