package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.filter.specifications.GroupSpecifications;
import com.hits.language_school_back.infrastructure.GroupServiceImpl;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.GroupRepository;
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

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group group1;
    private Group group2;
    private GroupDTO groupDTO;
    private Language language;
    private User teacher;
    private User student;

    @BeforeEach
    void setUp() {
        language = new Language();
        language.setId(1L);
        language.setName("English");

        group1 = new Group();
        group1.setId(1L);
        group1.setName("Group A");
        group1.setDescription("Description A");
        group1.setDifficulty(Difficulty.BEGINNER);
        group1.setLanguage(language);
        group1.setUsers(new ArrayList<>());

        group2 = new Group();
        group2.setId(2L);
        group2.setName("Group B");
        group2.setDescription("Description B");
        group2.setDifficulty(Difficulty.ADVANCED);
        group2.setLanguage(language);
        group2.setUsers(new ArrayList<>());

        groupDTO = new GroupDTO();
        groupDTO.setName("New Group");
        groupDTO.setDescription("New Description");
        groupDTO.setDifficulty(Difficulty.ELEMENTARY);
        groupDTO.setLanguage(language);

        teacher = new User();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setGroups(new ArrayList<>());

        student = new User();
        student.setId(2L);
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setGroups(new ArrayList<>());
    }

    @Test
    @DisplayName("Should create group successfully")
    void createGroup_ShouldSaveAndReturnGroup() {
        // Arrange
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
            Group savedGroup = invocation.getArgument(0);
            savedGroup.setId(3L);
            return savedGroup;
        });

        // Act
        Group result = groupService.createGroup(groupDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo(groupDTO.getName());
        assertThat(result.getDescription()).isEqualTo(groupDTO.getDescription());
        assertThat(result.getDifficulty()).isEqualTo(groupDTO.getDifficulty());
        assertThat(result.getLanguage()).isEqualTo(groupDTO.getLanguage());

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("Should edit group successfully")
    void editGroup_ShouldUpdateAndReturnGroup() {
        // Arrange
        Long groupId = 1L;
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Group result = groupService.editGroup(groupDTO, groupId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(groupId);
        assertThat(result.getName()).isEqualTo(groupDTO.getName());
        assertThat(result.getDescription()).isEqualTo(groupDTO.getDescription());
        assertThat(result.getDifficulty()).isEqualTo(groupDTO.getDifficulty());
        assertThat(result.getLanguage()).isEqualTo(groupDTO.getLanguage());

        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("Should get group by name")
    void getByName_ShouldReturnGroup() {
        // Arrange
        String groupName = "Group A";
        when(groupRepository.findByName(groupName)).thenReturn(group1);

        // Act
        Group result = groupService.getByName(groupName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(group1.getId());
        assertThat(result.getName()).isEqualTo(group1.getName());
        verify(groupRepository).findByName(groupName);
    }

    @Test
    @DisplayName("Should return null when getting group by non-existent name")
    void getByName_WithNonExistentName_ShouldReturnNull() {
        // Arrange
        String groupName = "NonExistent";
        when(groupRepository.findByName(groupName)).thenReturn(null);

        // Act
        Group result = groupService.getByName(groupName);

        // Assert
        assertThat(result).isNull();
        verify(groupRepository).findByName(groupName);
    }

    @Test
    @DisplayName("Should delete group by ID")
    void deleteGroup_ShouldCallRepositoryDelete() {
        // Arrange
        Long groupId = 1L;
        doNothing().when(groupRepository).deleteById(groupId);

        // Act
        groupService.deleteGroup(groupId);

        // Assert
        verify(groupRepository).deleteById(groupId);
    }

    @Test
    @DisplayName("Should get all groups")
    void getGroups_ShouldReturnListOfGroups() {
        // Arrange
        List<Group> groups = Arrays.asList(group1, group2);
        when(groupRepository.findAll()).thenReturn(groups);

        // Act
        List<Group> result = groupService.getGroups();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(group1, group2);
        verify(groupRepository).findAll();
    }

    @Test
    @DisplayName("Should get groups by teacher ID")
    void getGroupsByTeacherId_ShouldReturnListOfGroups() {
        // Arrange
        Long teacherId = 1L;
        List<Group> groups = Arrays.asList(group1, group2);
        when(groupRepository.findByUsersId(teacherId)).thenReturn(groups);

        // Act
        List<Group> result = groupService.getGroupsByTeacherId(teacherId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(group1, group2);
        verify(groupRepository).findByUsersId(teacherId);
    }

    @Test
    @DisplayName("Should get group by ID")
    void getByGroupId_ShouldReturnGroup() {
        // Arrange
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));

        // Act
        Group result = groupService.getByGroupId(groupId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(groupId);
        assertThat(result.getName()).isEqualTo(group1.getName());
        verify(groupRepository).findById(groupId);
    }

    @Test
    @DisplayName("Should throw exception when getting group by non-existent ID")
    void getByGroupId_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> groupService.getByGroupId(groupId))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Should get groups with filters")
    void getGroupsWithFilters_ShouldReturnFilteredGroups() {
        // Arrange
        GroupFilter filter = new GroupFilter("Group", Difficulty.BEGINNER, "English");
        Specification<Group> specification = mock(Specification.class);
        List<Group> filteredGroups = Collections.singletonList(group1);

        try (MockedStatic<GroupSpecifications> mockedStatic = mockStatic(GroupSpecifications.class)) {
            mockedStatic.when(() -> GroupSpecifications.withFilters(filter)).thenReturn(specification);
            when(groupRepository.findAll(specification)).thenReturn(filteredGroups);

            // Act
            List<Group> result = groupService.getGroupsWithFilters(filter);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(group1);
            verify(groupRepository).findAll(specification);
        }
    }

    @Test
    @DisplayName("Should add student to group")
    void addStudentToGroup_ShouldAddStudentAndReturnGroup() {
        // Arrange
        Long groupId = 1L;
        Long studentId = 2L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.save(any(Group.class))).thenReturn(group1);

        // Act
        Group result = groupService.addStudentToGroup(groupId, studentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsers()).contains(student);
        assertThat(student.getGroups()).contains(group1);

        verify(groupRepository).findById(groupId);
        verify(userRepository).findById(studentId);
        verify(groupRepository).save(group1);
    }

    @Test
    @DisplayName("Should throw exception when adding student to non-existent group")
    void addStudentToGroup_WithNonExistentGroup_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        Long studentId = 2L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> groupService.addStudentToGroup(groupId, studentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Group not found");

        verify(groupRepository).findById(groupId);
        verify(userRepository, never()).findById(anyLong());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("Should throw exception when adding non-existent student to group")
    void addStudentToGroup_WithNonExistentStudent_ShouldThrowException() {
        // Arrange
        Long groupId = 1L;
        Long studentId = 999L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> groupService.addStudentToGroup(groupId, studentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Student not found");

        verify(groupRepository).findById(groupId);
        verify(userRepository).findById(studentId);
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    @DisplayName("Should remove student from group")
    void removeStudentFromGroup_ShouldRemoveStudentAndReturnGroup() {
        // Arrange
        Long groupId = 1L;
        Long studentId = 2L;

        group1.getUsers().add(student);
        student.getGroups().add(group1);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.save(any(Group.class))).thenReturn(group1);

        // Act
        Group result = groupService.removeStudentFromGroup(groupId, studentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsers()).doesNotContain(student);
        assertThat(student.getGroups()).doesNotContain(group1);

        verify(groupRepository).findById(groupId);
        verify(userRepository).findById(studentId);
        verify(groupRepository).save(group1);
    }

    @Test
    @DisplayName("Should handle removing student when group has null users list")
    void removeStudentFromGroup_WithNullUsersList_ShouldHandleGracefully() {
        // Arrange
        Long groupId = 1L;
        Long studentId = 2L;

        group1.setUsers(null);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group1));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(groupRepository.save(any(Group.class))).thenReturn(group1);

        // Act
        Group result = groupService.removeStudentFromGroup(groupId, studentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(student.getGroups()).doesNotContain(group1);

        verify(groupRepository).findById(groupId);
        verify(userRepository).findById(studentId);
        verify(groupRepository).save(group1);
    }

    @Test
    @DisplayName("Should throw exception when removing student from non-existent group")
    void removeStudentFromGroup_WithNonExistentGroup_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        Long studentId = 2L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> groupService.removeStudentFromGroup(groupId, studentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Group not found");
    }
}