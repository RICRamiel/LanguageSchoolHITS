package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.filter.specifications.GroupSpecifications;
import com.hits.language_school_back.service.GroupService;
import com.hits.language_school_back.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final LanguageRepository languageRepository;

    @Override
    public Group createGroup(GroupDTO groupDTO) {
        Language language = languageRepository.findAllByName(groupDTO.getLanguage().getName()).get(0);

        Group group = new Group();
        group.setDescription(groupDTO.getDescription());
        group.setName(groupDTO.getName());
        group.setDifficulty(groupDTO.getDifficulty());
        group.setLanguage(language);
        groupRepository.save(group);
        return group;
    }

    @Override
    public Group editGroup(GroupDTO groupDTO, Long groupId) {
        Language language = languageRepository.findAllByName(groupDTO.getLanguage().getName()).get(0);

        Group group = new Group();
        group.setId(groupId);
        group.setDescription(groupDTO.getDescription());
        group.setName(groupDTO.getName());
        group.setDifficulty(groupDTO.getDifficulty());
        group.setLanguage(language);
        groupRepository.save(group);
        return group;
    }

    public Group getByName(String name){
        return groupRepository.findByName(name);
    }

    @Override
    public void deleteGroup(Long groupId) {
        groupRepository.deleteById(groupId);
    }

    @Override
    public List<Group> getGroups() {
        return groupRepository.findAll();
    }

    @Override
    public List<Group> getGroupsByTeacherId(Long teacherId) {

        return groupRepository.findByUsersId(teacherId);
    }

    @Override
    public Group getByGroupId(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow();
    }

    @Override
    public List<Group> getGroupsWithFilters(GroupFilter groupFilter) {
        return groupRepository.findAll(GroupSpecifications.withFilters(groupFilter));
    }

    @Override
    public Group addStudentToGroup(Long groupId, Long studentId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        group.getUsers().add(student);
        student.getGroups().add(group);

        return groupRepository.save(group);
    }

    @Override
    public Group removeStudentFromGroup(Long groupId, Long studentId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("Group not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found"));

        if (group.getUsers() != null) {
            group.getUsers().remove(student);
        }

        student.getGroups().remove(group);

        return groupRepository.save(group);
    }
}