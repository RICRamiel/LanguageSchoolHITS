package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.service.UserService;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<UserDTO> getUsers(@RequestParam(required = false) Long groupId) {
        return userService.getUsers(groupId);
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createStudent(@RequestBody StudentCreateDTO dto) {
        return userService.createStudent(dto);
    }

    @GetMapping("/students")
    public List<UserDTO> getAllStudents(@RequestParam(required = false) Long groupId) {
        return userService.getAllStudents(groupId);
    }

    @GetMapping("/students/search")
    public List<UserDTO> searchStudents(@RequestParam String value) {
        return userService.getStudentsByNameOrEmail(value);
    }

    @GetMapping("/students/{id}")
    public UserFullDTO getStudentById(@PathVariable Long id) {
        return userService.getStudentById(id);
    }

    @PutMapping("/students/{id}")
    public UserDTO updateStudent(@PathVariable Long id, @RequestBody StudentUpdateDTO dto) {
        return userService.updateStudent(id, dto);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        userService.deleteStudent(id);
    }

    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createTeacher(@RequestBody TeacherCreateDTO dto) {
        return userService.createTeacher(dto);
    }

    @GetMapping("/teachers")
    public List<UserDTO> getAllTeachers() {
        return userService.getAllTeachers();
    }

    @GetMapping("/teachers/{id}")
    public UserFullDTO getTeacherById(@PathVariable Long id) {
        return userService.getTeacherById(id);
    }

    @PutMapping("/teachers/{id}")
    public UserDTO updateTeacher(@PathVariable Long id, @RequestBody TeacherUpdateDTO dto) {
        return userService.updateTeacher(id, dto);
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable Long id) {
        userService.deleteTeacher(id);
    }
}