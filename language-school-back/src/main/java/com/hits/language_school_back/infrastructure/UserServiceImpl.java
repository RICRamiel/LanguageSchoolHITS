package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.service.UserService;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDTO createStudent(StudentCreateDTO dto) {
        return null;
    }

    @Override
    public UserFullDTO getStudentById(Long id) {
        return null;
    }

    @Override
    public List<UserDTO> getAllStudents(Group group) {
        return List.of();
    }

    @Override
    public UserDTO updateStudent(Long id, StudentUpdateDTO dto) {
        return null;
    }

    @Override
    public void deleteStudent(Long id) {

    }

    @Override
    public UserDTO createTeacher(TeacherCreateDTO dto) {
        return null;
    }

    @Override
    public UserFullDTO getTeacherById(Long id) {
        return null;
    }

    @Override
    public List<UserDTO> getAllTeachers() {
        return List.of();
    }

    @Override
    public UserDTO updateTeacher(Long id, TeacherUpdateDTO dto) {
        return null;
    }

    @Override
    public void deleteTeacher(Long id) {

    }

    @Override
    public User loadUserByUsername(String username) {
        return null;
    }

    @Override
    public List<UserDTO> getUsers(Group group) {
        return List.of();
    }

    @Override
    public UserFullDTO getMe(HttpServletRequest request) {
        return null;
    }
}
