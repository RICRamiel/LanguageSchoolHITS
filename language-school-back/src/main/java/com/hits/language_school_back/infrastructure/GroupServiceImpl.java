package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.GroupService;
import com.hits.language_school_back.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Override
    public Group createGroup(GroupDTO groupDTO) {
        return null;
    }

    @Override
    public Group editGroup(GroupDTO groupDTO, Long groupId) {
        return null;
    }

    @Override
    public void deleteGroup(Long groupId) {

    }

    @Override
    public List<Group> getGroups() {
        return List.of();
    }

    @Override
    public List<Group> getGroupsByTeacherId(Long teacherId) {
        return List.of();
    }

    @Override
    public Group getByGroupId(Long groupId) {
        return null;
    }

    @Override
    public List<Group> getGroupsWithFilters(GroupFilter groupFilter) {
        return List.of();
    }

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