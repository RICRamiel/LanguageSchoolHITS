package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.filter.specifications.GroupSpecifications;
import com.hits.language_school_back.infrastructure.GroupServiceImpl;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTests {
//
//    @Mock
//    private GroupRepository groupRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private LanguageRepository languageRepository;
//
//    @InjectMocks
//    private GroupServiceImpl groupService;
//
//    private Group group1;
//    private Group group2;
//    private GroupDTO groupDTO;
//    private Language language;
//    private Language defaultLanguage;
//    private LanguageDTO languageDTO;
//    private User teacher;
//    private User student;
//    private final Long teacherId = 1L;
//    private final Long studentId = 2L;
//    private final Long groupId = 1L;
//
//    @BeforeEach
//    void setUp() {
//        // Setup Languages
//        language = new Language();
//        language.setId(1L);
//        language.setName("English");
//
//        defaultLanguage = new Language();
//        defaultLanguage.setId(2L);
//        defaultLanguage.setName("Spanish");
//
//        languageDTO = new LanguageDTO();
//        languageDTO.setName("English");
//
//        // Setup Groups
//        group1 = new Group();
//        group1.setId(1L);
//        group1.setName("Group A");
//        group1.setDescription("Description A");
//        group1.setDifficulty(Difficulty.BEGINNER);
//        group1.setLanguage(language);
//        group1.setUsers(new ArrayList<>());
//
//        group2 = new Group();
//        group2.setId(2L);
//        group2.setName("Group B");
//        group2.setDescription("Description B");
//        group2.setDifficulty(Difficulty.ADVANCED);
//        group2.setLanguage(language);
//        group2.setUsers(new ArrayList<>());
//
//        // Setup GroupDTO with LanguageDTO
//        groupDTO = GroupDTO.builder()
//                .name("New Group")
//                .description("New Description")
//                .difficulty(Difficulty.ELEMENTARY)
//                .language(languageDTO)
//                .build();
//
//        // Setup Users
//        teacher = new User();
//        teacher.setId(teacherId);
//        teacher.setFirstName("John");
//        teacher.setLastName("Doe");
//        teacher.setGroups(new ArrayList<>());
//
//        student = new User();
//        student.setId(studentId);
//        student.setFirstName("Jane");
//        student.setLastName("Smith");
//        student.setGroups(new ArrayList<>());
//    }
//
//    // ==================== CREATE GROUP ====================
//
//    @Test
//    @DisplayName("Should create group with existing language")
//    void createGroup_WithExistingLanguage_ShouldUseThatLanguage() {
//        // Arrange
//        List<Language> languages = Arrays.asList(language);
//        when(languageRepository.findAllByName(groupDTO.getLanguage().getName())).thenReturn(languages);
//        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
//            Group savedGroup = invocation.getArgument(0);
//            savedGroup.setId(3L);
//            return savedGroup;
//        });
//
//        // Act
//        Group result = groupService.createGroup(groupDTO);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(3L);
//        assertThat(result.getName()).isEqualTo(groupDTO.getName());
//        assertThat(result.getDescription()).isEqualTo(groupDTO.getDescription());
//        assertThat(result.getDifficulty()).isEqualTo(groupDTO.getDifficulty());
//        assertThat(result.getLanguage()).isEqualTo(language);
//
//        verify(languageRepository).findAllByName(groupDTO.getLanguage().getName());
//        verify(groupRepository).save(any(Group.class));
//        verify(languageRepository, never()).findAll();
//    }
//
//    @Test
//    @DisplayName("Should edit group with existing language")
//    void editGroup_WithExistingLanguage_ShouldUpdateGroup() {
//        // Arrange
//        List<Language> languages = Arrays.asList(language);
//
//        when(languageRepository.findAllByName(groupDTO.getLanguage().getName())).thenReturn(languages);
//        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // Act
//        Group result = groupService.editGroup(groupDTO, groupId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(groupId);
//        assertThat(result.getName()).isEqualTo(groupDTO.getName());
//        assertThat(result.getDescription()).isEqualTo(groupDTO.getDescription());
//        assertThat(result.getDifficulty()).isEqualTo(groupDTO.getDifficulty());
//        assertThat(result.getLanguage()).isEqualTo(language);
//
//        verify(languageRepository).findAllByName(groupDTO.getLanguage().getName());
//        verify(groupRepository).save(any(Group.class));
//    }
//
//    @Test
//    @DisplayName("Should get group by name")
//    void getByName_ShouldReturnGroup() {
//        // Arrange
//        String groupName = "Group A";
//        when(groupRepository.findByName(groupName)).thenReturn(group1);
//
//        // Act
//        Group result = groupService.getByName(groupName);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(group1.getId());
//        assertThat(result.getName()).isEqualTo(group1.getName());
//        assertThat(result.getLanguage()).isEqualTo(language);
//        verify(groupRepository).findByName(groupName);
//    }
//
//    @Test
//    @DisplayName("Should return null when getting group by non-existent name")
//    void getByName_WithNonExistentName_ShouldReturnNull() {
//        // Arrange
//        String groupName = "NonExistent";
//        when(groupRepository.findByName(groupName)).thenReturn(null);
//
//        // Act
//        Group result = groupService.getByName(groupName);
//
//        // Assert
//        assertThat(result).isNull();
//        verify(groupRepository).findByName(groupName);
//    }
//
//    // ==================== DELETE GROUP ====================
//
//    @Test
//    @DisplayName("Should delete group by ID")
//    void deleteGroup_ShouldCallRepositoryDelete() {
//        // Arrange
//        Long groupId = 1L;
//        doNothing().when(groupRepository).deleteById(groupId);
//
//        // Act
//        groupService.deleteGroup(groupId);
//
//        // Assert
//        verify(groupRepository).deleteById(groupId);
//    }
//
//    // ==================== GET ALL GROUPS ====================
//
//    @Test
//    @DisplayName("Should get all groups")
//    void getGroups_ShouldReturnListOfGroups() {
//        // Arrange
//        List<Group> groups = Arrays.asList(group1, group2);
//        when(groupRepository.findAll()).thenReturn(groups);
//
//        // Act
//        List<Group> result = groupService.getGroups();
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(2);
//        assertThat(result).containsExactly(group1, group2);
//        verify(groupRepository).findAll();
//    }
//
//    // ==================== GET GROUPS BY TEACHER ID ====================
//
//    @Test
//    @DisplayName("Should get groups by teacher ID")
//    void getGroupsByTeacherId_ShouldReturnListOfGroups() {
//        // Arrange
//        List<Group> groups = Arrays.asList(group1, group2);
//        when(groupRepository.findByUsersId(teacherId)).thenReturn(groups);
//
//        // Act
//        List<Group> result = groupService.getGroupsByTeacherId(teacherId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result).hasSize(2);
//        assertThat(result).containsExactly(group1, group2);
//        verify(groupRepository).findByUsersId(teacherId);
//    }
//
//    // ==================== GET GROUP BY ID ====================
//
//    @Test
//    @DisplayName("Should get group by ID")
//    void getByGroupId_ShouldReturnGroup() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//
//        // Act
//        Group result = groupService.getByGroupId(groupId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(groupId);
//        assertThat(result.getName()).isEqualTo(group1.getName());
//        assertThat(result.getLanguage()).isEqualTo(language);
//        verify(groupRepository).findById(groupId);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when getting group by non-existent ID")
//    void getByGroupId_WithNonExistentId_ShouldThrowException() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> groupService.getByGroupId(groupId))
//                .isInstanceOf(NoSuchElementException.class);
//    }
//
//    // ==================== GET GROUPS WITH FILTERS ====================
//
//    @Test
//    @DisplayName("Should get groups with filters")
//    void getGroupsWithFilters_ShouldReturnFilteredGroups() {
//        // Arrange
//        GroupFilter filter = new GroupFilter("Group", Difficulty.BEGINNER, "English");
//        Specification<Group> specification = mock(Specification.class);
//        List<Group> filteredGroups = Collections.singletonList(group1);
//
//        try (MockedStatic<GroupSpecifications> mockedStatic = mockStatic(GroupSpecifications.class)) {
//            mockedStatic.when(() -> GroupSpecifications.withFilters(filter)).thenReturn(specification);
//            when(groupRepository.findAll(specification)).thenReturn(filteredGroups);
//
//            // Act
//            List<Group> result = groupService.getGroupsWithFilters(filter);
//
//            // Assert
//            assertThat(result).isNotNull();
//            assertThat(result).hasSize(1);
//            assertThat(result.get(0)).isEqualTo(group1);
//            verify(groupRepository).findAll(specification);
//        }
//    }
//
//    // ==================== ADD STUDENT TO GROUP ====================
//
//    @Test
//    @DisplayName("Should add student to group")
//    void addUserToGroup_ShouldAddUserAndReturnGroup() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//        when(groupRepository.save(any(Group.class))).thenReturn(group1);
//
//        // Act
//        Group result = groupService.addUserToGroup(groupId, studentId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getUsers()).contains(student);
//        assertThat(student.getGroups()).contains(group1);
//
//        verify(groupRepository).findById(groupId);
//        verify(userRepository).findById(studentId);
//        verify(groupRepository).save(group1);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when adding student to non-existent group")
//    void addUserToGroup_WithNonExistentGroup_ShouldThrowException() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> groupService.addUserToGroup(groupId, studentId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessageContaining("Group not found");
//
//        verify(groupRepository).findById(groupId);
//        verify(userRepository, never()).findById(anyLong());
//        verify(groupRepository, never()).save(any(Group.class));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when adding non-existent student to group")
//    void addUserToGroup_WithNonExistentUser_ShouldThrowException() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//        when(userRepository.findById(studentId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> groupService.addUserToGroup(groupId, studentId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessageContaining("User not found");
//
//        verify(groupRepository).findById(groupId);
//        verify(userRepository).findById(studentId);
//        verify(groupRepository, never()).save(any(Group.class));
//    }
//
//    // ==================== REMOVE STUDENT FROM GROUP ====================
//
//    @Test
//    @DisplayName("Should remove student from group")
//    void removeUserFromGroup_ShouldRemoveUserAndReturnGroup() {
//        // Arrange
//        group1.getUsers().add(student);
//        student.getGroups().add(group1);
//
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//        when(groupRepository.save(any(Group.class))).thenReturn(group1);
//
//        // Act
//        Group result = groupService.removeUserFromGroup(groupId, studentId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getUsers()).doesNotContain(student);
//        assertThat(student.getGroups()).doesNotContain(group1);
//
//        verify(groupRepository).findById(groupId);
//        verify(userRepository).findById(studentId);
//        verify(groupRepository).save(group1);
//    }
//
//    @Test
//    @DisplayName("Should handle removing user when group has null users list")
//    void removeUserFromGroup_WithNullUsersList_ShouldHandleGracefully() {
//        // Arrange
//        group1.setUsers(null);
//        student.getGroups().add(group1);
//
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
//        when(groupRepository.save(any(Group.class))).thenReturn(group1);
//
//        // Act
//        Group result = groupService.removeUserFromGroup(groupId, studentId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(student.getGroups()).doesNotContain(group1);
//
//        verify(groupRepository).findById(groupId);
//        verify(userRepository).findById(studentId);
//        verify(groupRepository).save(group1);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when removing student from non-existent group")
//    void removeUserFromGroup_WithNonExistentGroup_ShouldThrowException() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> groupService.removeUserFromGroup(groupId, studentId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessageContaining("Group not found");
//    }
//
//    @Test
//    @DisplayName("Should throw exception when removing non-existent student from group")
//    void removeStudentFromGroup_WithNonExistentStudent_ShouldThrowException() {
//        // Arrange
//        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
//        when(userRepository.findById(studentId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> groupService.removeUserFromGroup(groupId, studentId))
//                .isInstanceOf(NoSuchElementException.class)
//                .hasMessageContaining("Student not found");
//    }
}