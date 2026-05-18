package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserFullDTO getMe(HttpServletRequest request) {
        return userService.getMe(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDTO> getUsers(@RequestParam(required = false) UUID groupId) {
        return userService.getUsers(groupId);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDTO createStudent(@RequestBody StudentCreateDTO dto) {
        return userService.createStudent(dto);
    }

    @GetMapping("/students")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public List<UserDTO> getAllStudents(@RequestParam(required = false) UUID groupId) {
        return userService.getAllStudents(groupId);
    }

    @GetMapping("/students/search")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public List<UserDTO> searchStudents(@RequestParam String value) {
        return userService.getStudentsByNameOrEmail(value);
    }

    @GetMapping("/students/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TEACHER')")
    public UserFullDTO getStudentById(@PathVariable UUID id) {
        return userService.getStudentById(id);
    }

    @PutMapping("/students/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDTO updateStudent(@PathVariable UUID id, @RequestBody StudentUpdateDTO dto) {
        return userService.updateStudent(id, dto);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteStudent(@PathVariable UUID id) {
        userService.deleteStudent(id);
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDTO createTeacher(@RequestBody TeacherCreateDTO dto) {
        return userService.createTeacher(dto);
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserDTO> getAllTeachers() {
        return userService.getAllTeachers();
    }

    @GetMapping("/teachers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserFullDTO getTeacherById(@PathVariable UUID id) {
        return userService.getTeacherById(id);
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserDTO updateTeacher(@PathVariable UUID id, @RequestBody TeacherUpdateDTO dto) {
        return userService.updateTeacher(id, dto);
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteTeacher(@PathVariable UUID id) {
        userService.deleteTeacher(id);
    }
}
