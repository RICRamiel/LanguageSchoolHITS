package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.repository.GroupRepository;
import com.hits.language_school_back.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsServiceImpl implements NotificationsService {
    private final NotificationRepository notificationRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    public List<Notification> getAllGroupNotifications(Long groupId) {
        log.info("Fetching all notifications for group with id: {}", groupId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        List<Notification> notifications = notificationRepository.findByGroup(group);
        log.info("Found {} notifications for group {}", notifications.size(), groupId);

        return notifications;
    }

    public List<Notification> getAllStudentsNotifications(Long studentId) {
        log.info("Fetching all notifications for student with id: {}", studentId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Group> studentGroups = student.getGroups();

        if (studentGroups.isEmpty()) {
            log.info("Student {} is not in any groups", studentId);
            return List.of();
        }

        List<Notification> notifications = notificationRepository.findByGroupIn(studentGroups);
        log.info("Found {} notifications for student {} across {} groups",
                notifications.size(), studentId, studentGroups.size());

        return notifications;
    }

    @Transactional
    public Notification createNotification(NotificationCreationModel model, UserFullDTO me) {
        log.info("Creating new notification in group {} by user {}", model.getGroupId(), me.getId());

        Group group = groupRepository.findById(model.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + model.getGroupId()));

        User creator = userRepository.findById(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + me.getId()));

        Notification notification = new Notification();
        notification.setText(model.getText());
        notification.setGroup(group);
        notification.setCreatedBy(creator);
        notification.setCreationDate(LocalDate.now());

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with id: {}", savedNotification.getId());

        model.getFiles().forEach(file -> attachmentService.uploadAttachmentForNotification(savedNotification.getId(), file, me.getId()));

        return savedNotification;
    }
}
