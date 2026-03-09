package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.RegisterStudentDTO;
import com.hits.language_school_back.dto.TokenDTO;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    TokenDTO registerAdmin(RegisterDTO request);
    TokenDTO registerTeacher(RegisterDTO request);
    TokenDTO registerStudent(RegisterStudentDTO request);
    TokenDTO login(String email, String password);
    void logout(HttpServletRequest request);
}
