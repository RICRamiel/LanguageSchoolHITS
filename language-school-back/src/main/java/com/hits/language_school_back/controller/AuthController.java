package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.LoginDTO;
import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.RegisterStudentDTO;
import com.hits.language_school_back.dto.TokenDTO;
import com.hits.language_school_back.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/admin")
    public ResponseEntity<TokenDTO> registerAdmin(@RequestBody @Valid RegisterDTO request) {
        return ResponseEntity.ok(authService.registerAdmin(request));
    }

    @PostMapping("/register/teacher")
    public ResponseEntity<TokenDTO> registerTeacher(@RequestBody @Valid RegisterDTO request) {
        return ResponseEntity.ok(authService.registerTeacher(request));
    }

    @PostMapping("/register/student")
    public ResponseEntity<TokenDTO> registerStudent(@RequestBody @Valid RegisterStudentDTO request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody @Valid LoginDTO request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("User logged out successfully");
    }


}