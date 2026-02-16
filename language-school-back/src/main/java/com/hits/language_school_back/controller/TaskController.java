package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.inteface.TaskService;
import com.hits.language_school_back.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody TaskDTO taskDTO){
        Task task = taskService.createTask(taskDTO);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/{taskId}/edit")
    public ResponseEntity<Task> editTask(@RequestBody TaskDTO taskDTO, @PathVariable Long taskId){
        Task task = taskService.editTask(taskDTO, taskId);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}/delete")
    void deleteTask(@PathVariable Long taskId){
        taskService.deleteTask(taskId);
    }

    @GetMapping("/{teacherId}/get_by_teacher")
    public ResponseEntity<List<TaskTeacherDTO>> getByTeacherId(@PathVariable Long teacherId){
        List<TaskTeacherDTO> tasks = taskService.getTasksByTeacherId(teacherId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/get_by_group_name")
    public ResponseEntity<List<TaskStudentDTO>> getByGroupName(@RequestParam String groupName){
        List<TaskStudentDTO> tasks = taskService.getTasksByGroupName(groupName);
        return ResponseEntity.ok(tasks);
    }
}
