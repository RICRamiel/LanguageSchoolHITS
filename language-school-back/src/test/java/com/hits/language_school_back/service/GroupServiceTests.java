package com.hits.language_school_back.service;

import com.hits.language_school_back.infrastructure.GroupServiceImpl;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.enums.Role;
import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.dto.GroupDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTests {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    private GroupDTO groupDTO;
    private Group group;
    private User user1;
    private User user2;
    private User user3;
    private Language language;
    private Language language2;

    @BeforeEach
    void setUp() {
        language = new Language();
        language.setId(1L);
        language.setName("Английский");

        language2 = new Language();
        language2.setId(2L);
        language2.setName("Испанский");

        user1 = new User();
        user1.setId(1L);
        user1.setFirstName("Иван");
        user1.setLastName("Петров");
        user1.setEmail("ivan.petrov@example.com");
        user1.setRole(Role.TEACHER);
        user1.setLanguages(List.of(language));

        user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Анна");
        user2.setLastName("Сидорова");
        user2.setEmail("anna.sidorova@example.com");
        user2.setRole(Role.STUDENT);
        user2.setLanguages(List.of(language));

        user3 = new User();
        user3.setId(3L);
        user3.setFirstName("Петр");
        user3.setLastName("Иванов");
        user3.setEmail("petr.ivanov@example.com");
        user3.setRole(Role.STUDENT);
        user3.setLanguages(List.of(language, language2));

        group = new Group();
        group.setId(1L);
        group.setName("Группа А-1");
        group.setDescription("Описание группы А-1");
        group.setDifficulty(Difficulty.UPPER_INTERMEDIATE);
        group.setLanguage(language);
        group.setUsers(List.of(user1, user2, user3));

        groupDTO = new GroupDTO();
        groupDTO.setName("Новая группа");
        groupDTO.setDescription("Описание новой группы");
        groupDTO.setDifficulty(Difficulty.BEGINNER);
        groupDTO.setLanguage(language);
    }

    @Test
    void createGroup_ShouldCreateAndReturnGroup() {
        // Arrange
        when(languageRepository.findById(language.getId())).thenReturn(Optional.of(language));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> {
            Group savedGroup = invocation.getArgument(0);
            savedGroup.setId(1L);
            return savedGroup;
        });

        // Act
        Group result = groupService.createGroup(groupDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(groupDTO.getName(), result.getName());
        assertEquals(groupDTO.getDescription(), result.getDescription());
        assertEquals(groupDTO.getDifficulty(), result.getDifficulty());
        assertEquals(groupDTO.getLanguage(), result.getLanguage());

        verify(languageRepository, times(1)).findById(language.getId());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void createGroup_WhenLanguageNotFound_ShouldThrowException() {
        // Arrange
        when(languageRepository.findById(language.getId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> groupService.createGroup(groupDTO));
        assertEquals("Language not found with id: " + language.getId(), exception.getMessage());

        verify(languageRepository, times(1)).findById(language.getId());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void editGroup_ShouldUpdateAndReturnGroup() {
        // Arrange
        Long groupId = 1L;
        Group existingGroup = new Group();
        existingGroup.setId(groupId);
        existingGroup.setName("Старое название");
        existingGroup.setDescription("Старое описание");
        existingGroup.setDifficulty(Difficulty.PROFICIENCY);
        existingGroup.setLanguage(language2);
        existingGroup.setUsers(List.of(user1, user2));

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(existingGroup));
        when(languageRepository.findById(language.getId())).thenReturn(Optional.of(language));
        when(groupRepository.save(any(Group.class))).thenReturn(existingGroup);

        // Act
        Group result = groupService.editGroup(groupDTO, groupId);

        // Assert
        assertNotNull(result);
        assertEquals(groupId, result.getId());
        assertEquals(groupDTO.getName(), result.getName());
        assertEquals(groupDTO.getDescription(), result.getDescription());
        assertEquals(groupDTO.getDifficulty(), result.getDifficulty());
        assertEquals(groupDTO.getLanguage(), result.getLanguage());

        verify(groupRepository, times(1)).findById(groupId);
        verify(languageRepository, times(1)).findById(language.getId());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void editGroup_WhenGroupNotFound_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> groupService.editGroup(groupDTO, groupId));
        assertEquals("Group not found with id: 999", exception.getMessage());

        verify(groupRepository, times(1)).findById(groupId);
        verify(languageRepository, never()).findById(anyLong());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void editGroup_WhenLanguageNotFound_ShouldThrowException() {
        // Arrange
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(languageRepository.findById(language.getId())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> groupService.editGroup(groupDTO, groupId));
        assertEquals("Language not found with id: " + language.getId(), exception.getMessage());

        verify(groupRepository, times(1)).findById(groupId);
        verify(languageRepository, times(1)).findById(language.getId());
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void deleteGroup_ShouldDeleteGroup() {
        // Arrange
        Long groupId = 1L;
        when(groupRepository.existsById(groupId)).thenReturn(true);
        doNothing().when(groupRepository).deleteById(groupId);

        // Act
        groupService.deleteGroup(groupId);

        // Assert
        verify(groupRepository, times(1)).existsById(groupId);
        verify(groupRepository, times(1)).deleteById(groupId);
    }

    @Test
    void deleteGroup_WhenGroupDoesNotExist_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        when(groupRepository.existsById(groupId)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> groupService.deleteGroup(groupId));
        assertEquals("Group not found with id: 999", exception.getMessage());

        verify(groupRepository, times(1)).existsById(groupId);
        verify(groupRepository, never()).deleteById(anyLong());
    }

    @Test
    void getGroups_ShouldReturnAllGroups() {
        // Arrange
        Group group2 = new Group();
        group2.setId(2L);
        group2.setName("Группа Б-2");
        group2.setDescription("Описание группы Б-2");
        group2.setDifficulty(Difficulty.ELEMENTARY);
        group2.setLanguage(language2);
        group2.setUsers(List.of(user1, user3));

        List<Group> groups = Arrays.asList(group, group2);
        when(groupRepository.findAll()).thenReturn(groups);

        // Act
        List<Group> result = groupService.getGroups();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(group.getId(), result.get(0).getId());
        assertEquals(group2.getId(), result.get(1).getId());

        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void getGroups_WhenNoGroups_ShouldReturnEmptyList() {
        // Arrange
        when(groupRepository.findAll()).thenReturn(List.of());

        // Act
        List<Group> result = groupService.getGroups();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(groupRepository, times(1)).findAll();
    }

    @Test
    void getGroupsByTeacherId_ShouldReturnGroupsForTeacher() {
        // Arrange
        Long teacherId = 1L;

        List<Group> expectedGroups = Arrays.asList(group);
        when(groupRepository.findByUsersId(teacherId)).thenReturn(expectedGroups);

        // Act
        List<Group> result = groupService.getGroupsByTeacherId(teacherId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(groupRepository, times(1)).findByUsersId(teacherId);
    }

    @Test
    void getGroupsByTeacherId_WhenNoGroups_ShouldReturnEmptyList() {
        // Arrange
        Long teacherId = 1L;
        when(groupRepository.findByUsersId(teacherId)).thenReturn(List.of());

        // Act
        List<Group> result = groupService.getGroupsByTeacherId(teacherId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(groupRepository, times(1)).findByUsersId(teacherId);
    }

    @Test
    void getByGroupId_ShouldReturnGroup() {
        // Arrange
        Long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        // Act
        Group result = groupService.getByGroupId(groupId);

        // Assert
        assertNotNull(result);
        assertEquals(groupId, result.getId());
        assertEquals(group.getName(), result.getName());
        assertEquals(group.getDescription(), result.getDescription());
        assertEquals(group.getDifficulty(), result.getDifficulty());
        assertEquals(group.getLanguage(), result.getLanguage());
        assertEquals(3, result.getUsers().size());

        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void getByGroupId_WhenGroupNotFound_ShouldThrowException() {
        // Arrange
        Long groupId = 999L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class,
                () -> groupService.getByGroupId(groupId));
        assertEquals("Group not found with id: 999", exception.getMessage());

        verify(groupRepository, times(1)).findById(groupId);
    }
}