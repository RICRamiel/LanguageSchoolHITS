package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.NotificationCreationModel;
import com.hits.language_school_back.dto.UserFullDTO;
import com.hits.language_school_back.exception.ResourceNotFoundException;
import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Notification;
import com.hits.language_school_back.model.StudentsInCourse;
import com.hits.language_school_back.model.User;
import com.hits.language_school_back.repository.CourseRepository;
import com.hits.language_school_back.repository.NotificationRepository;
import com.hits.language_school_back.repository.UserRepository;
import com.hits.language_school_back.service.AttachmentService;
import com.hits.language_school_back.service.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationsServiceImpl implements NotificationsService {
    private final NotificationRepository notificationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    @Override
    public List<Notification> getAllCourseNotifications(UUID courseId) {
        log.info("Fetching all notifications for course with id: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + courseId));

        return notificationRepository.findByCourse(course);
    }

    @Override
    public List<Notification> getAllStudentsNotifications(UUID studentId) {
        log.info("Fetching all notifications for student with id: {}", studentId);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        List<Course> studentCourses = student.getCourse() == null
                ? List.of()
                : student.getCourse().stream()
                .map(StudentsInCourse::getCourse)
                .distinct()
                .toList();

        if (studentCourses.isEmpty()) {
            return List.of();
        }

        return notificationRepository.findByCourseIn(studentCourses);
    }

    @Override
    @Transactional
    public Notification createNotification(NotificationCreationModel model, UserFullDTO me) {
        log.info("Creating new notification in course {} by user {}", model.getCourseId(), me.getId());

        Course course = courseRepository.findById(model.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + model.getCourseId()));

        User creator = userRepository.findById(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + me.getId()));

        Notification notification = new Notification();
        notification.setText(model.getText());
        notification.setCourse(course);
        notification.setCreatedBy(creator);
        notification.setCreationDate(LocalDate.now());

        Notification savedNotification = notificationRepository.save(notification);

        if (model.getFiles() != null) {
            model.getFiles().forEach(file -> attachmentService.uploadAttachmentForNotification(savedNotification.getId(), file, me.getId()));
        }

        return savedNotification;
    }
}
