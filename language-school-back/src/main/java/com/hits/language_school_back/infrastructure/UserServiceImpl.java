package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.config.JwtUtil;
import com.hits.language_school_back.dto.UserDTO;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.dto.users.StudentCreateDTO;
import com.hits.language_school_back.dto.users.StudentUpdateDTO;
import com.hits.language_school_back.dto.users.TeacherCreateDTO;
import com.hits.language_school_back.dto.users.TeacherUpdateDTO;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.UserService;
import com.hits.language_school_back.model.Group;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found: " + username));
    }

    @Override
    public UserDTO createStudent(StudentCreateDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.STUDENT);
        user.setGrade(dto.getGrade());
//        user.setGroup(dto.getGroup());

        userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    public UserFullDTO getStudentById(Long id) {

        User user = userRepository.findByIdAndRole(id, Role.STUDENT)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        return mapToFullDTO(user);
    }

    @Override
    public List<UserDTO> getAllStudents(Group group) {

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .filter(u -> group == null || u.getGroups().contains(group))
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<UserDTO> getStudentsByNameOrEmail(String value) {

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .filter(u ->
                        (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(value.toLowerCase()))
                                ||
                                (u.getEmail() != null && u.getEmail().toLowerCase().contains(value.toLowerCase()))
                )
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserDTO updateStudent(Long id, StudentUpdateDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGrade(dto.getGrade());

        userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    public void deleteStudent(Long id) {
        log.debug("Attempting to delete student with id: {}", id);

        User teacher = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("student not found with id: {}", id);
                    return new NoSuchElementException("student not found with id: " + id);
                });

        userRepository.delete(teacher);
        log.debug("Successfully deleted student with id: {}", id);    }

    // ---------- TEACHERS ----------

    @Override
    public UserDTO createTeacher(TeacherCreateDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.TEACHER);

        userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    public UserFullDTO getTeacherById(Long id) {

        User user = userRepository.findByIdAndRole(id, Role.TEACHER)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found"));

        return mapToFullDTO(user);
    }

    @Override
    public List<UserDTO> getAllTeachers() {

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TEACHER)
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserDTO updateTeacher(Long id, TeacherUpdateDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        userRepository.save(user);

        return mapToDTO(user);
    }

    @Override
    public void deleteTeacher(Long id) {
        log.debug("Attempting to delete teacher with id: {}", id);

        User teacher = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Teacher not found with id: {}", id);
                    return new NoSuchElementException("Teacher not found with id: " + id);
                });

        userRepository.delete(teacher);
        log.debug("Successfully deleted teacher with id: {}", id);
    }

    // ---------- USERS ----------

    @Override
    public List<UserDTO> getUsers(Group group) {

        return userRepository.findAll().stream()
                .filter(u -> group == null || u.getGroups().contains(group))
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserFullDTO getMe(HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            throw new BadCredentialsException("Invalid token");
        }

        token = token.substring(7);

        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return mapToFullDTO(user);
    }

    // ---------- MAPPERS ----------

    private UserDTO mapToDTO(User user) {

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        return dto;
    }

    private UserFullDTO mapToFullDTO(User user) {

        UserFullDTO dto = new UserFullDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setGroups(user.getGroups());

        return dto;
    }
}
