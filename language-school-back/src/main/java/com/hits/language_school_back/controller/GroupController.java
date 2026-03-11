package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.GroupAnswerDTO;
import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.filter.GroupFilter;
import com.hits.language_school_back.mapper.GroupMapper;
import com.hits.language_school_back.service.GroupService;
import com.hits.language_school_back.model.Group;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @PostMapping("/create")
    public ResponseEntity<GroupAnswerDTO> createGroup(@RequestBody GroupDTO groupDTO) {
        Group group = groupService.createGroup(groupDTO);
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @PutMapping("/{groupId}/edit")
    public ResponseEntity<GroupAnswerDTO> editGroup(@RequestBody GroupDTO groupDTO, @PathVariable Long groupId) {
        Group group = groupService.editGroup(groupDTO, groupId);
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @PostMapping("/{groupId}/add/{userId}")
    public ResponseEntity<GroupAnswerDTO> addStudentToGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Group group = groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @DeleteMapping("/{groupId}/add/{userId}")
    public ResponseEntity<GroupAnswerDTO> removeStudentFromGroup(@PathVariable Long groupId, @PathVariable Long userId) {
        Group group = groupService.removeUserFromGroup(groupId, userId);
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @DeleteMapping("/{groupId}/delete")
    void deleteGroup(Long groupId) {
        groupService.deleteGroup(groupId);
    }

    @GetMapping("/get_all_groups")
    ResponseEntity<List<GroupAnswerDTO>> getGroups() {
        List<Group> groups = groupService.getGroups();
        return ResponseEntity.ok(groups.stream().map(groupMapper::toDto).toList());
    }

    @GetMapping("/{teacherId}/get_groups_by_teacher")
    ResponseEntity<List<GroupAnswerDTO>> getGroupsByTeacherId(@PathVariable Long teacherId) {
        List<Group> groups = groupService.getGroupsByTeacherId(teacherId);
        return ResponseEntity.ok(groups.stream().map(groupMapper::toDto).toList());
    }

    @GetMapping("/{groupId}/get_group_by_id")
    ResponseEntity<GroupAnswerDTO> getByGroupId(@PathVariable Long groupId) {
        Group group = groupService.getByGroupId(groupId);
        return ResponseEntity.ok(groupMapper.toDto(group));
    }

    @GetMapping("/get-groups-with-filters")
    ResponseEntity<List<GroupAnswerDTO>> getWithFilters(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) Difficulty difficulty,
                                               @RequestParam(required = false) String language) {
        GroupFilter groupFilter = new GroupFilter(name, difficulty, language);
        return ResponseEntity.ok(groupService.getGroupsWithFilters(groupFilter).stream().map(groupMapper::toDto).toList());
    }
}
