package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.RegisterStudentDTO;
import com.hits.language_school_back.dto.TokenDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.RevokedToken;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.RevokedTokenRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public TokenDTO registerAdmin(RegisterDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadCredentialsException("Пользователь уже существует.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);

        userRepository.save(user);
        return new TokenDTO(jwtUtil.generateToken(user));
    }

    @Override
    public TokenDTO registerStudent(RegisterStudentDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadCredentialsException("Пользователь уже существует.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setGrade(request.getGrade());
        userRepository.save(user);
        return new TokenDTO(jwtUtil.generateToken(user));
    }

    @Override
    public TokenDTO registerTeacher(RegisterDTO request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadCredentialsException("Пользователь уже существует.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TEACHER);

        userRepository.save(user);
        return new TokenDTO(jwtUtil.generateToken(user));
    }

    public TokenDTO login(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("Пользователь не найден."));
        return new TokenDTO(jwtUtil.generateToken(user));
    }


    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadCredentialsException("Неверный токен.");
        }

        String token = authHeader.substring(7);
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);
        revokedTokenRepository.save(revokedToken);
    }

}