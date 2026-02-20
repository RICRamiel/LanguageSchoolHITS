package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.service.GroupService;
import com.hits.language_school_back.model.Group;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {
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
}
