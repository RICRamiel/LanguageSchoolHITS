package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.NotificationDto;
import com.hits.language_school_back.mapper.NotificationMapper;
import com.hits.language_school_back.service.NotificationsService;
import com.hits.language_school_back.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/by-course/{courseId}")
    public List<NotificationDto> getAllCourseNotifications(@PathVariable UUID courseId) {
        return service.getAllCourseNotifications(courseId).stream().map(mapper::toDto).toList();
    }

    @GetMapping("/for-students/{studentId}")
    public List<NotificationDto> getAllStudentsNotifications(@PathVariable UUID studentId) {
        return service.getAllStudentsNotifications(studentId).stream().map(mapper::toDto).toList();
    }
}
