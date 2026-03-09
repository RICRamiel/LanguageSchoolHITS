package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Notification;
import java.util.List;

public interface NotificationsService {
    List<Notification> getAllGroupNotifications(Long groupId);

    List<Notification> getAllStudentsNotifications(Long studentId);

    Notification createNotification(NotificationCreationModel model, UserFullDTO me);
}