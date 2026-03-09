package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserDTO createStudent(StudentCreateDTO dto);
    UserFullDTO getStudentById(Long id);
    List<UserDTO> getAllStudents(Long group);
    UserDTO updateStudent(Long id, StudentUpdateDTO dto);
    void deleteStudent(Long id);
    UserDTO createTeacher(TeacherCreateDTO dto);
    UserFullDTO getTeacherById(Long id);
    List<UserDTO> getAllTeachers();
    UserDTO updateTeacher(Long id, TeacherUpdateDTO dto);
    void deleteTeacher(Long id);
    List<UserDTO> getUsers(Long group);
    UserFullDTO getMe(HttpServletRequest request);
    List<UserDTO> getStudentsByNameOrEmail(String value);
    }
