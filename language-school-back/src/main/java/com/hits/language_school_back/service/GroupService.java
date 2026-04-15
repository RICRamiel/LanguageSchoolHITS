package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.model.Course;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    Course createGroup(GroupDTO groupDTO);
    Course editGroup(GroupDTO groupDTO, UUID groupId);
    void deleteGroup(UUID groupId);
    List<Course> getGroups();
    List<Course> getGroupsByTeacherId(UUID teacherId);
    Course getByGroupId(UUID groupId);
    List<Course> getGroupsWithFilters(GroupFilter groupFilter);
    Course getByName(String name);
    Course addUserToGroup(UUID groupId, UUID studentId);
    Course removeUserFromGroup(UUID groupId, UUID studentId);
}
