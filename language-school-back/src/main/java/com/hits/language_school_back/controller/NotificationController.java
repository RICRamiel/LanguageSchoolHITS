package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.*;
import com.hits.language_school_back.mapper.NotificationMapper;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.service.NotificationsService;
import com.hits.language_school_back.service.TaskService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationsService service;
    private final UserService userService;
    private final NotificationMapper mapper;

    @PostMapping("/create")
    public NotificationDto createNotification(
            @RequestBody NotificationCreationModel model,
            HttpServletRequest request
    ) {
        return mapper.toDto(service.createNotification(model, userService.getMe(request)));
    }

    @GetMapping("/by-group/{groupId}")
    public List<NotificationDto> getAllGroupNotifications(@PathVariable Long groupId) {
        return service.getAllGroupNotifications(groupId).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/for-students/{studentId}")
    public List<NotificationDto> getAllStudentsNotifications(@PathVariable Long studentId) {
        return service.getAllStudentsNotifications(studentId).stream().map(mapper::toDto).toList();
    }
}
