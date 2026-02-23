package com.hits.language_school_back.service;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.enums.Role;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private UserService userService;

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

        User result = userService.loadUserByUsername("x@x.com");

        assertSame(u, result);
    }

    // -------------------------
    // getMe
    // -------------------------
    @Test
    void getMe_noAuthHeader_throwsBadCredentials() {
        when(request.getHeader("Authorization")).thenReturn(null);
        assertThrows(BadCredentialsException.class, () -> userService.getMe(request));
    }

    @Test
    void getMe_wrongPrefix_throwsBadCredentials() {
        when(request.getHeader("Authorization")).thenReturn("Token abc");
        assertThrows(BadCredentialsException.class, () -> userService.getMe(request));
    }

    @Test
    void getMe_success_extractsUsername_loadsUser_maps() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(jwtUtil.extractUsername("token123")).thenReturn("me@mail.com");

        User me = user(7L, "me@mail.com", Role.STUDENT);
        when(userRepository.findByEmail("me@mail.com")).thenReturn(Optional.of(me));

        UserFullDTO dto = new UserFullDTO();
        when(userMapper.userToUserFullDto(me)).thenReturn(dto);

        UserFullDTO res = userService.getMe(request);

        assertSame(dto, res);
        verify(jwtUtil).extractUsername("token123");
        verify(userRepository).findByEmail("me@mail.com");
        verify(userMapper).userToUserFullDto(me);
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

        when(userRepository.existsByEmail("s@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createStudent(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createStudent_success_setsRoleAndGroup_savesAndMaps() {
        StudentCreateDTO dto = new StudentCreateDTO();
        dto.setEmail("s@mail.com");
        dto.setFirstName("Student");
        dto.setPassword("pass");
        dto.setGroups(List.of(groupA));

        when(userRepository.existsByEmail("s@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        UserDTO mapped = new UserDTO();
        when(userMapper.userToUserDto(any(User.class))).thenReturn(mapped);

        UserDTO res = userService.createStudent(dto);

        assertSame(mapped, res);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("s@mail.com", saved.getEmail());
        assertEquals("Student", saved.getFirstName());
        assertEquals(Role.STUDENT, saved.getRole());
        assertEquals(groupA, saved.getGroups());
    }

    @Test
    void getStudentById_notFound_throws() {
        when(userRepository.findByIdAndRole(1L, Role.STUDENT)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.getStudentById(1L));
    }

    @Test
    void getStudentById_success_maps() {
        User s = user(1L, "s@mail.com", Role.STUDENT);
        when(userRepository.findByIdAndRole(1L, Role.STUDENT)).thenReturn(Optional.of(s));

        UserFullDTO dto = new UserFullDTO();
        when(userMapper.userToUserFullDto(s)).thenReturn(dto);

        assertSame(dto, userService.getStudentById(1L));
    }

    @Test
    void getAllStudents_success_optionalGroupFilter() {
        User s1 = user(1L, "1@mail.com", Role.STUDENT); s1.setGroups(List.of(groupA));
        User s2 = user(2L, "2@mail.com", Role.STUDENT); s2.setGroups(List.of(groupB));

        when(userRepository.findAllByRole(Role.STUDENT)).thenReturn(List.of(s1, s2));
        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        assertEquals(2, userService.getAllStudents(null).size());
        assertEquals(1, userService.getAllStudents(groupA).size());
    }

    @Test
    void updateStudent_notFound_throws() {
        when(userRepository.findByIdAndRole(5L, Role.STUDENT)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.updateStudent(5L, new StudentUpdateDTO()));
    }

    @Test
    void updateStudent_success_updatesFields_savesAndMaps() {
        User s = user(5L, "s@mail.com", Role.STUDENT);
        s.setGroups(List.of(groupA));

        when(userRepository.findByIdAndRole(5L, Role.STUDENT)).thenReturn(Optional.of(s));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        StudentUpdateDTO dto = new StudentUpdateDTO();
        dto.setFirstName("New Name");

        userService.updateStudent(5L, dto);

        assertEquals("New Name", s.getFirstName());
        verify(userRepository).save(s);
    }

    @Test
    void deleteStudent_notFound_throws() {
        when(userRepository.findByIdAndRole(9L, Role.STUDENT)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.deleteStudent(9L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteStudent_success_deletes() {
        User s = user(9L, "s@mail.com", Role.STUDENT);
        when(userRepository.findByIdAndRole(9L, Role.STUDENT)).thenReturn(Optional.of(s));

        userService.deleteStudent(9L);

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

        when(userRepository.existsByEmail("t@mail.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createTeacher(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createTeacher_success_setsRole_savesAndMaps() {
        TeacherCreateDTO dto = new TeacherCreateDTO();
        dto.setEmail("t@mail.com");
        dto.setFirstName("Teacher");
        dto.setPassword("pass");

        when(userRepository.existsByEmail("t@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(200L);
            return u;
        });

        UserDTO mapped = new UserDTO();
        when(userMapper.userToUserDto(any(User.class))).thenReturn(mapped);

        UserDTO res = userService.createTeacher(dto);
        assertSame(mapped, res);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("t@mail.com", saved.getEmail());
        assertEquals("Teacher", saved.getFirstName());
        assertEquals(Role.TEACHER, saved.getRole());
    }

    @Test
    void getTeacherById_notFound_throws() {
        when(userRepository.findByIdAndRole(1L, Role.TEACHER)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.getTeacherById(1L));
    }

    @Test
    void getTeacherById_success_maps() {
        User t = user(1L, "t@mail.com", Role.TEACHER);
        when(userRepository.findByIdAndRole(1L, Role.TEACHER)).thenReturn(Optional.of(t));

        UserFullDTO dto = new UserFullDTO();
        when(userMapper.userToUserFullDto(t)).thenReturn(dto);

        assertSame(dto, userService.getTeacherById(1L));
    }

    @Test
    void getAllTeachers_success_mapsList() {
        User t1 = user(1L, "1@mail.com", Role.TEACHER);
        User t2 = user(2L, "2@mail.com", Role.TEACHER);

        when(userRepository.findAllByRole(Role.TEACHER)).thenReturn(List.of(t1, t2));
        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        assertEquals(2, userService.getAllTeachers().size());
        verify(userRepository).findAllByRole(Role.TEACHER);
    }

    @Test
    void updateTeacher_notFound_throws() {
        when(userRepository.findByIdAndRole(5L, Role.TEACHER)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.updateTeacher(5L, new TeacherUpdateDTO()));
    }

    @Test
    void updateTeacher_success_updatesFields_savesAndMaps() {
        User t = user(5L, "t@mail.com", Role.TEACHER);

        when(userRepository.findByIdAndRole(5L, Role.TEACHER)).thenReturn(Optional.of(t));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.userToUserDto(any(User.class))).thenReturn(new UserDTO());

        TeacherUpdateDTO dto = new TeacherUpdateDTO();
        dto.setFirstName("New Teacher Name");

        userService.updateTeacher(5L, dto);

        assertEquals("New Teacher Name", t.getFirstName());
        verify(userRepository).save(t);
    }

    @Test
    void deleteTeacher_notFound_throws() {
        when(userRepository.findByIdAndRole(9L, Role.TEACHER)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> userService.deleteTeacher(9L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteTeacher_success_deletes() {
        User t = user(9L, "t@mail.com", Role.TEACHER);
        when(userRepository.findByIdAndRole(9L, Role.TEACHER)).thenReturn(Optional.of(t));

        userService.deleteTeacher(9L);

        verify(userRepository).delete(t);
    }
}