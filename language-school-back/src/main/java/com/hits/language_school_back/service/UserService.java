package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    UserDTO createStudent(StudentCreateDTO dto);
    UserFullDTO getStudentById(UUID id);
    List<UserDTO> getAllStudents(UUID group);
    UserDTO updateStudent(UUID id, StudentUpdateDTO dto);
    void deleteStudent(UUID id);
    UserDTO createTeacher(TeacherCreateDTO dto);
    UserFullDTO getTeacherById(UUID id);
    List<UserDTO> getAllTeachers();
    UserDTO updateTeacher(UUID id, TeacherUpdateDTO dto);
    void deleteTeacher(UUID id);
    List<UserDTO> getUsers(UUID group);
    UserFullDTO getMe(HttpServletRequest request);
    List<UserDTO> getStudentsByNameOrEmail(String value);
    }
