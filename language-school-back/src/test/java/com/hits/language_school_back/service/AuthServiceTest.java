package com.hits.language_school_back.service;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.RegisterDTO;
import com.hits.language_school_back.dto.TokenDTO;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.RevokedToken;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.RevokedTokenRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.h2.engine.Role;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RevokedTokenRepository revokedTokenRepository;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterDTO registerDTO;
    private Group group;

    @BeforeEach
    void setUp() {
        group = new Group();

        registerDTO = new RegisterDTO(
                "Иван",
                "Иванов",
                "ivan@example.com",
                "Qwerty12!",
                List.of(group)
        );
    }

    @Test
    void register_success_shouldSaveUser_encodePassword_andReturnToken() {
        // arrange
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encoded-pass");
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // act
        TokenDTO result = authService.register(registerDTO);

        // assert
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        // проверяем что сохранили пользователя с нужными полями
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("Иван", saved.getFirstName());
        assertEquals("Иванов", saved.getLastName());
        assertEquals("ivan@example.com", saved.getEmail());
        assertEquals("encoded-pass", saved.getPassword());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(group, saved.getGroups());

        verify(passwordEncoder).encode("Qwerty12!");
        verify(jwtUtil).generateToken(saved);
        verifyNoMoreInteractions(revokedTokenRepository);
    }

    @Test
    void register_userAlreadyExists_shouldThrowBadCredentials_andNotSave() {
        // arrange
        when(userRepository.findByEmail(registerDTO.getEmail()))
                .thenReturn(Optional.of(mock(User.class)));

        // act + assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.register(registerDTO)
        );
        assertEquals("Пользователь уже существует.", ex.getMessage());

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_success_shouldAuthenticate_findUser_andReturnToken() {
        // arrange
        String email = "ivan@example.com";
        String password = "Qwerty12!";

        // authenticate() ничего не возвращает — просто не кидает исключение
        doNothing().when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        User user = mock(User.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user)).thenReturn("jwt-login");

        // act
        TokenDTO result = authService.login(email, password);

        // assert
        assertNotNull(result);
        assertEquals("jwt-login", result.getToken());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(authCaptor.capture());
        UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
        assertEquals(email, authToken.getPrincipal());
        assertEquals(password, authToken.getCredentials());

        verify(userRepository).findByEmail(email);
        verify(jwtUtil).generateToken(user);
    }

    @Test
    void login_userNotFound_shouldThrowNoSuchElement() {
        // arrange
        String email = "missing@example.com";
        String password = "Qwerty12!";

        doNothing().when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // act + assert
        NoSuchElementException ex = assertThrows(
                NoSuchElementException.class,
                () -> authService.login(email, password)
        );
        assertEquals("Пользователь не найден.", ex.getMessage());

        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void login_badCredentialsFromAuthManager_shouldPropagate_andNotQueryUserRepo() {
        // arrange
        String email = "ivan@example.com";
        String password = "wrong";

        doThrow(new BadCredentialsException("Bad creds"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // act + assert
        assertThrows(BadCredentialsException.class, () -> authService.login(email, password));

        verify(userRepository, never()).findByEmail(any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void logout_success_shouldExtractBearerToken_andSaveRevokedToken() {
        // arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        // act
        authService.logout(request);

        // assert
        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(captor.capture());
        RevokedToken saved = captor.getValue();

        assertEquals("abc.def.ghi", saved.getToken());
    }

    @Test
    void logout_noAuthorizationHeader_shouldThrowBadCredentials() {
        // arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // act + assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.logout(request)
        );
        assertEquals("Неверный токен.", ex.getMessage());

        verify(revokedTokenRepository, never()).save(any());
    }

    @Test
    void logout_notBearer_shouldThrowBadCredentials() {
        // arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        // act + assert
        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.logout(request)
        );
        assertEquals("Неверный токен.", ex.getMessage());

        verify(revokedTokenRepository, never()).save(any());
    }
}