package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.model.Group;

import java.util.List;

public interface GroupService {
    Group createGroup(GroupDTO groupDTO);
    Group editGroup(GroupDTO groupDTO, Long groupId);
    void deleteGroup(Long groupId);
    List<Group> getGroups();
    List<Group> getGroupsByTeacherId(Long teacherId);
    Group getByGroupId(Long groupId);
}
