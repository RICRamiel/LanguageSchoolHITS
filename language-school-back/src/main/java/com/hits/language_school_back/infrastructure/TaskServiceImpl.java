package com.hits.language_school_back.infrastructure;

import com.google.common.base.Strings;
import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.enums.TaskStatus;
import com.hits.language_school_back.mapper.TaskStudentMapper;
import com.hits.language_school_back.mapper.TaskTeacherMapper;
import com.hits.language_school_back.mapper.UserMapper;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.TaskStudent;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.TaskRepository;
import com.hits.language_school_back.repository.TaskStudentRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.GroupService;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskTeacherMapper taskTeacherMapper;
    private final TaskStudentMapper taskStudentMapper;
    private final GroupService groupService;
    private final UserRepository userRepository;
    private final TaskStudentRepository taskStudentRepository;

    @Override
    public List<TaskTeacherDTO> getTasksByTeacherId(Long teacherId) {
        return taskTeacherMapper.toDtoList(taskRepository.findByUserId(teacherId));
    }

    @Override
    public List<TaskStudentDTO> getTasksByGroupName(String name, Long userId) {
        return taskStudentMapper.toDtoList(taskStudentRepository.findByUserId(userId));
    }

    @Override
    public Task createTask(TaskDTO taskDTO, UserFullDTO userFullDTO) {
        User user = userRepository.findById(userFullDTO.getId()).orElseThrow();
        Task task = new Task();
        task.setUser(user);
        task.setDeadline(taskDTO.getDeadline());
        task.setDescription(taskDTO.getDescription());
        if (taskDTO.getDeadline().isAfter(LocalDate.now())) {
            task.setTaskStatus(TaskStatus.PENDING);
        }
        if (taskDTO.getDeadline().isBefore(LocalDate.now())) {
            task.setTaskStatus(TaskStatus.OVERDUE);
        }
        task.setName(taskDTO.getName());
        task.setGroup(groupService.getByName(taskDTO.getGroupName()));
        Task saveTask = taskRepository.save(task);
        List<User> users = task.getGroup().getUsers();

        users.forEach(user1 -> {
            TaskStudent taskStudent = new TaskStudent();
            taskStudent.setTaskStatus(saveTask.getTaskStatus());
            taskStudent.setUserId(user1.getId());
            taskStudent.setTaskId(saveTask.getId());
            taskStudentRepository.save(taskStudent);
        });

        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Task editTask(TaskDTO taskDTO, Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (!Strings.isNullOrEmpty(taskDTO.getName())) {
            task.setName(taskDTO.getName());
        }
        if (!Strings.isNullOrEmpty(taskDTO.getDescription())) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getDeadline().isAfter(LocalDate.now()) && task.getTaskStatus() != TaskStatus.COMPLETE) {
            task.setTaskStatus(TaskStatus.PENDING);
        }
        if (taskDTO.getDeadline().isBefore(LocalDate.now()) && task.getTaskStatus() != TaskStatus.COMPLETE) {
            task.setTaskStatus(TaskStatus.OVERDUE);
        }
        task.setDeadline(taskDTO.getDeadline());
        task.setGroup(groupService.getByName(taskDTO.getGroupName()));
        taskRepository.save(task);
        return task;
    }

    @Override
    public void completeTask(Long taskId, Long userId) {
        TaskStudent taskStudent = new TaskStudent();
        taskStudent.setUserId(userId);
        taskStudent.setTaskId(taskId);
        taskStudent.setAttachmentList(userRepository.findById(userId).orElseThrow().getAttachmentList());
        Task task = taskRepository.findById(taskId).orElseThrow();
        if (task.getDeadline().isBefore(LocalDate.now())) {
            task.setTaskStatus(TaskStatus.OVERDUE);
            taskStudent.setTaskStatus(TaskStatus.OVERDUE);
        } else if (task.getDeadline().isAfter(LocalDate.now())) {
            taskStudent.setTaskStatus(TaskStatus.COMPLETE);
        }
    }
}
