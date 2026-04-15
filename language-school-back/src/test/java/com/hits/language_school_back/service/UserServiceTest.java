package com.hits.language_school_back.service;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.infrastructure.UserServiceImpl;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID studentId;
    private UUID teacherId;
    private UUID courseId;
    private Course course;

    @BeforeEach
    void setUp() {
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        Language language = new Language();
        language.setId(UUID.randomUUID());
        language.setName("English");

        course = Course.builder()
                .id(courseId)
                .name("Course")
                .description("Description")
                .language(language)
                .build();
    }

    @Test
    void loadUserByUsername_whenMissing_throws() {
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("x@x.com"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createStudent_encodesPasswordAndMapsDto() {
        StudentCreateDTO dto = new StudentCreateDTO();
        dto.setEmail("student@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setPassword("secret");
        dto.setGrade("A2");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.createStudent(dto);

        assertThat(result.getEmail()).isEqualTo("student@example.com");
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().getGrade()).isEqualTo("A2");
    }

    @Test
    void getAllStudents_filtersByGroup() {
        StudentsInCourse enrollment = StudentsInCourse.builder().course(course).build();
        User student = User.builder()
                .id(studentId)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .course(List.of(enrollment))
                .build();
        User teacher = User.builder().role(Role.TEACHER).build();
        when(userRepository.findAll()).thenReturn(List.of(student, teacher));

        List<UserDTO> result = userService.getAllStudents(courseId);

        assertThat(result).singleElement().satisfies(dto -> {
            assertThat(dto.getId()).isEqualTo(studentId);
            assertThat(dto.getGroups()).singleElement().satisfies(group -> assertThat(group.getId()).isEqualTo(courseId));
        });
    }

    @Test
    void updateTeacher_updatesNames() {
        TeacherUpdateDTO dto = new TeacherUpdateDTO();
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        User teacher = User.builder().id(teacherId).email("teacher@example.com").role(Role.TEACHER).build();
        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(userRepository.save(teacher)).thenReturn(teacher);

        UserDTO result = userService.updateTeacher(teacherId, dto);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void createTeacher_whenEmailExists_throws() {
        TeacherCreateDTO dto = new TeacherCreateDTO();
        dto.setEmail("teacher@example.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createTeacher(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getMe_extractsUserFromBearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = User.builder()
                .id(studentId)
                .email("student@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.STUDENT)
                .course(List.of(StudentsInCourse.builder().course(course).build()))
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));

        UserFullDTO result = userService.getMe(request);

        assertThat(result.getId()).isEqualTo(studentId);
        assertThat(result.getGroups()).singleElement().satisfies(group -> assertThat(group.getName()).isEqualTo("Course"));
    }

    @Test
    void getMe_whenHeaderInvalid_throws() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Token abc");

        assertThatThrownBy(() -> userService.getMe(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void getMe_whenUserMissing_throws() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtUtil.extractUsername("token")).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }
}
