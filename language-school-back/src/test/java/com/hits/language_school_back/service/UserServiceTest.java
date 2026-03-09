package com.hits.language_school_back.service;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.infrastructure.UserServiceImpl;
import com.hits.language_school_back.mapper.UserMapper;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;  // Add this mock
    @InjectMocks private UserServiceImpl userService;

    @Mock private HttpServletRequest request;

    private Group groupA;
    private Group groupB;

    @BeforeEach
    void setUp() {
        groupA = new Group(); // если Group — enum, замени на Group.A
        groupB = new Group();
    }

    private User user(Long id, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        u.setFirstName("Name " + id);
        u.setLastName("Last Name " + id);
        return u;
    }

    private UserFullDTO userFullDto(Long id) {
        UserFullDTO dto = new UserFullDTO();
        return dto;
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.loadUserByUsername("x@x.com"));
    }

    @Test
    void loadUserByUsername_found_returnsUser() {
        User u = user(1L, "x@x.com", Role.STUDENT);
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.of(u));

        UserDetails result = userService.loadUserByUsername("x@x.com");

        assertSame(u, result);
    }

    // -------------------------
    // getMe
    // -------------------------

    @Test
    void getMe_wrongPrefix_throwsBadCredentials() {
        when(request.getHeader("Authorization")).thenReturn("Token abc");
        assertThrows(BadCredentialsException.class, () -> userService.getMe(request));
    }

    // =========================
    // CRUD STUDENT
    // =========================
    @Test
    void createStudent_emailExists_throws() {
        StudentCreateDTO dto = new StudentCreateDTO();
        dto.setEmail("s@mail.com");
        dto.setFirstName("Student");
        dto.setPassword("pass");
        dto.setGroups(List.of(groupA));

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () -> userService.createStudent(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getStudentById_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> userService.getStudentById(1L));
    }

    @Test
    void updateStudent_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> userService.updateStudent(5L, new StudentUpdateDTO()));
    }

    @Test
    void updateStudent_success_updatesFields_savesAndMaps() {
        User s = user(5L, "s@mail.com", Role.STUDENT);
        s.setGroups(List.of(groupA));

        when(userRepository.findById(5L)).thenReturn(Optional.of(s));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        StudentUpdateDTO dto = new StudentUpdateDTO();
        dto.setFirstName("New Name");

        userService.updateStudent(5L, dto);

        assertEquals("New Name", s.getFirstName());
        verify(userRepository).save(s);
    }

    @Test
    void deleteStudent_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> userService.deleteStudent(9L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteStudent_success_deletes() {
        User s = user(9L, "s@mail.com", Role.STUDENT);

        when(userRepository.findById(9L)).thenReturn(Optional.of(s));

        userService.deleteStudent(9L);

        verify(userRepository).findById(9L);
        verify(userRepository).delete(s);
    }

    // =========================
    // CRUD TEACHER
    // =========================
    @Test
    void createTeacher_emailExists_throws() {
        TeacherCreateDTO dto = new TeacherCreateDTO();
        dto.setEmail("t@mail.com");
        dto.setFirstName("Teacher");
        dto.setPassword("pass");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () -> userService.createTeacher(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createTeacher_success_setsRole_savesAndMaps() {
        // Given - Test data
        TeacherCreateDTO dto = new TeacherCreateDTO();
        dto.setEmail("t@mail.com");
        dto.setFirstName("Teacher");
        dto.setLastName("Smith");  // Added if you have last name
        dto.setPassword("pass");

        // Stub password encoding
        when(passwordEncoder.encode("pass")).thenReturn("encodedPassword123");

        // Stub save with ID assignment
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(200L);  // Simulate database ID generation
            return user;
        });

        // Stub mapper if createTeacher returns DTO
        UserDTO expectedDto = new UserDTO();
        expectedDto.setId(200L);
        expectedDto.setEmail("t@mail.com");
        expectedDto.setFirstName("Teacher");
        expectedDto.setRole(Role.TEACHER);

        // When - Execute the service method
        UserDTO result = userService.createTeacher(dto);

        // Then - Verify all interactions

        // 1. Verify email check was performed
        verify(userRepository).findByEmail("t@mail.com");

        // 2. Verify password was encoded
        verify(passwordEncoder).encode("pass");

        // 3. Capture and verify the saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("t@mail.com", savedUser.getEmail());
        assertEquals("Teacher", savedUser.getFirstName());
        assertEquals("Smith", savedUser.getLastName());
        assertEquals(Role.TEACHER, savedUser.getRole());
        assertEquals("encodedPassword123", savedUser.getPassword()); // Should be encoded
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getEmail(), result.getEmail());
    }


    @Test
    void getTeacherById_notFound_throws() {
        when(userRepository.findByIdAndRole(1L, Role.TEACHER)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.getTeacherById(1L));
    }

    @Test
    void updateTeacher_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> userService.updateTeacher(5L, new TeacherUpdateDTO()));
    }

    @Test
    void updateTeacher_success_updatesFields_savesAndMaps() {
        User t = user(5L, "t@mail.com", Role.TEACHER);

        when(userRepository.findById(5L)).thenReturn(Optional.of(t));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        TeacherUpdateDTO dto = new TeacherUpdateDTO();
        dto.setFirstName("New Teacher Name");

        userService.updateTeacher(5L, dto);

        assertEquals("New Teacher Name", t.getFirstName());
        verify(userRepository).save(t);
    }

    @Test
    void deleteTeacher_notFound_throws() {
        assertThrows(NoSuchElementException.class, () -> userService.deleteTeacher(9L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteTeacher_success_deletes() {
        User t = user(9L, "t@mail.com", Role.TEACHER);
        when(userRepository.findById(9L)).thenReturn(Optional.of(t));

        userService.deleteTeacher(9L);

        verify(userRepository).delete(t);
    }
}