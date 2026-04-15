package com.hits.language_school_back.service;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.RegisterStudentDTO;
import com.hits.language_school_back.dto.TokenDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.infrastructure.AuthServiceImpl;
import com.hits.language_school_back.model.RevokedToken;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.RevokedTokenRepository;
import com.hits.language_school_back.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RevokedTokenRepository revokedTokenRepository;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterStudentDTO registerStudentDTO;
    private RegisterDTO registerTeacherDTO;

    @BeforeEach
    void setUp() {
        registerStudentDTO = new RegisterStudentDTO("Ivan", "Ivanov", "ivan@example.com", "Qwerty12!", "A1");
        registerTeacherDTO = new RegisterDTO("Jane", "Smith", "jane@example.com", "Qwerty12!");
    }

    @Test
    void registerStudent_savesStudentAndReturnsToken() {
        when(userRepository.findByEmail(registerStudentDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerStudentDTO.getPassword())).thenReturn("encoded");
        when(jwtUtil.generateToken(any(User.class))).thenReturn("student-token");

        TokenDTO result = authService.registerStudent(registerStudentDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(result.getToken()).isEqualTo("student-token");
        assertThat(savedUser.getEmail()).isEqualTo("ivan@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded");
        assertThat(savedUser.getGrade()).isEqualTo("A1");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void registerTeacher_whenUserExists_throws() {
        when(userRepository.findByEmail(registerTeacherDTO.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registerTeacher(registerTeacherDTO))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Пользователь уже существует.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerAdmin_setsAdminRole() {
        when(userRepository.findByEmail(registerTeacherDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerTeacherDTO.getPassword())).thenReturn("encoded-admin");
        when(jwtUtil.generateToken(any(User.class))).thenReturn("admin-token");

        authService.registerAdmin(registerTeacherDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void login_authenticatesAndReturnsToken() {
        User user = User.builder().email("ivan@example.com").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("ivan@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("login-token");

        TokenDTO result = authService.login("ivan@example.com", "Qwerty12!");

        assertThat(result.getToken()).isEqualTo("login-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_whenUserMissing_throws() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("missing@example.com", "Qwerty12!"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Пользователь не найден.");
    }

    @Test
    void logout_extractsBearerTokenAndSavesRevokedToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-123");

        authService.logout(request);

        ArgumentCaptor<RevokedToken> tokenCaptor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getToken()).isEqualTo("token-123");
    }

    @Test
    void logout_whenHeaderInvalid_throws() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic token-123");

        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Неверный токен.");

        verify(revokedTokenRepository, never()).save(any(RevokedToken.class));
    }
}
