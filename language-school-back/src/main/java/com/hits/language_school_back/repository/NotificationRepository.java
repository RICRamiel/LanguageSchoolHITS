package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Course;
import com.hits.language_school_back.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByCourse(Course course);

    List<Notification> findByCourseIn(Collection<Course> courses);
}
