package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.GroupDTO;
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

    @PostMapping("/create")
    public ResponseEntity<Group> createGroup(@RequestBody GroupDTO groupDTO){
        Group group = groupService.createGroup(groupDTO);
        return ResponseEntity.ok(group);
    }

    @PutMapping("/{groupId}/edit")
    public ResponseEntity<Group> editGroup(@RequestBody GroupDTO groupDTO, @PathVariable Long groupId){
        Group group = groupService.editGroup(groupDTO, groupId);
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{groupId}/delete")
    void deleteGroup(Long groupId){
        groupService.deleteGroup(groupId);
    }

    @GetMapping("/get_all_groups")
    ResponseEntity<List<Group>> getGroups(){
        List<Group> groups = groupService.getGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{teacherId}/get_groups_by_teacher")
    ResponseEntity<List<Group>> getGroupsByTeacherId(@PathVariable Long teacherId){
        List<Group> groups = groupService.getGroupsByTeacherId(teacherId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}/get_group_by_id")
    ResponseEntity<Group> getByGroupId(@PathVariable Long groupId){
        Group group = groupService.getByGroupId(groupId);
        return ResponseEntity.ok(group);
    }
}
