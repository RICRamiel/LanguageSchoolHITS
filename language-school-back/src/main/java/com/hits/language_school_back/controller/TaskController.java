package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.mapper.TaskMapper;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskMapper taskMapper;

    @PostMapping("/create")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO, HttpServletRequest request) {
        Task task = taskService.createTask(taskDTO, userService.getMe(request));
        return ResponseEntity.ok(taskMapper.toDto(task));
    }

    @PutMapping("/{taskId}/edit")
    public ResponseEntity<TaskDTO> editTask(@RequestBody TaskDTO taskDTO, @PathVariable Long taskId) {
        Task task = taskService.editTask(taskDTO, taskId);
        return ResponseEntity.ok(taskMapper.toDto(task));
    }

    @DeleteMapping("/{taskId}/delete")
    void deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
    }

    @GetMapping("/{teacherId}/get_by_teacher")
    public ResponseEntity<List<TaskTeacherDTO>> getByTeacherId(@PathVariable Long teacherId) {
        List<TaskTeacherDTO> tasks = taskService.getTasksByTeacherId(teacherId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/get_by_group_name")
    public ResponseEntity<List<TaskStudentDTO>> getByGroupName(@RequestParam String groupName, HttpServletRequest request) {
        List<TaskStudentDTO> tasks = taskService.getTasksByGroupName(groupName, userService.getMe(request).getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/get_by_group_name_real")
    public ResponseEntity<List<TaskStudentDTO>> getByGroupNameNew(@RequestParam String groupName) {
        List<TaskStudentDTO> tasks = taskService.getTasksByGroupNameReal(groupName);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/complete_task")
    public void completeTask(@PathVariable Long taskId, HttpServletRequest request) {
        taskService.completeTask(taskId, userService.getMe(request).getId());
    }
}
