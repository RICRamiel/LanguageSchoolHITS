package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationsService {
    List<Notification> getAllCourseNotifications(UUID courseId);

    List<Notification> getAllStudentsNotifications(UUID studentId);

    Notification createNotification(NotificationCreationModel model, UserFullDTO me);
}
