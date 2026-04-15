package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.TaskDTO;
import com.hits.language_school_back.dto.TaskParticipationGradeDTO;
import com.hits.language_school_back.dto.TaskSolutionSubmitDTO;
import com.hits.language_school_back.dto.TaskStudentDTO;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.dto.TaskTeamCreateDTO;
import com.hits.language_school_back.dto.TaskTeamDTO;
import com.hits.language_school_back.dto.TaskTeamGradeDTO;
import com.hits.language_school_back.mapper.TaskMapper;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<TaskDTO> editTask(@RequestBody TaskDTO taskDTO, @PathVariable UUID taskId) {
        Task task = taskService.editTask(taskDTO, taskId);
        return ResponseEntity.ok(taskMapper.toDto(task));
    }

    @DeleteMapping("/{taskId}/delete")
    void deleteTask(@PathVariable UUID taskId) {
        taskService.deleteTask(taskId);
    }

    @GetMapping("/{teacherId}/get_by_teacher")
    public ResponseEntity<List<TaskTeacherDTO>> getByTeacherId(@PathVariable UUID teacherId) {
        return ResponseEntity.ok(taskService.getTasksByTeacherId(teacherId));
    }

    @GetMapping("/get_by_group_name")
    public ResponseEntity<List<TaskStudentDTO>> getByGroupName(@RequestParam String groupName, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.getTasksByGroupName(groupName, userService.getMe(request).getId()));
    }

    @GetMapping("/get_by_group_name_real")
    public ResponseEntity<List<TaskStudentDTO>> getByGroupNameNew(@RequestParam String groupName) {
        return ResponseEntity.ok(taskService.getTasksByGroupNameReal(groupName));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TaskStudentDTO>> getByCourseId(@PathVariable UUID courseId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.getTasksByCourseId(courseId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/teams")
    public ResponseEntity<TaskTeamDTO> createTeam(@PathVariable UUID taskId, @RequestBody TaskTeamCreateDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.createTeam(taskId, dto, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/teams/{teamId}/join")
    public ResponseEntity<TaskTeamDTO> joinTeam(@PathVariable UUID taskId, @PathVariable UUID teamId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.joinTeam(taskId, teamId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/teams/{teamId}/students/{studentId}")
    public ResponseEntity<TaskTeamDTO> addStudentToTeam(@PathVariable UUID taskId, @PathVariable UUID teamId, @PathVariable UUID studentId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.addStudentToTeam(taskId, teamId, studentId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/teams/{teamId}/captain/{studentId}")
    public ResponseEntity<TaskTeamDTO> appointCaptain(@PathVariable UUID taskId, @PathVariable UUID teamId, @PathVariable UUID studentId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.appointCaptain(taskId, teamId, studentId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/submit_solution")
    public ResponseEntity<TaskTeamDTO> submitSolution(@PathVariable UUID taskId, @RequestBody(required = false) TaskSolutionSubmitDTO dto, HttpServletRequest request) {
        TaskSolutionSubmitDTO payload = dto == null ? TaskSolutionSubmitDTO.builder().build() : dto;
        return ResponseEntity.ok(taskService.submitSolution(taskId, payload, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/participations/{participationId}/vote")
    public ResponseEntity<TaskTeamDTO> voteForSolution(@PathVariable UUID taskId, @PathVariable UUID participationId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.voteForSolution(taskId, participationId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/participations/{participationId}/grade")
    public ResponseEntity<TaskTeamDTO> gradeParticipation(@PathVariable UUID taskId, @PathVariable UUID participationId, @RequestBody TaskParticipationGradeDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.gradeParticipation(taskId, participationId, dto, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/teams/{teamId}/grade")
    public ResponseEntity<TaskTeamDTO> gradeTeam(@PathVariable UUID taskId, @PathVariable UUID teamId, @RequestBody TaskTeamGradeDTO dto, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.gradeTeam(taskId, teamId, dto, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/finalize")
    public ResponseEntity<TaskTeacherDTO> finalizeTask(@PathVariable UUID taskId, HttpServletRequest request) {
        return ResponseEntity.ok(taskService.finalizeTask(taskId, userService.getMe(request).getId()));
    }

    @PostMapping("/{taskId}/complete_task")
    public void completeTask(@PathVariable UUID taskId, HttpServletRequest request) {
        taskService.completeTask(taskId, userService.getMe(request).getId());
    }
}
