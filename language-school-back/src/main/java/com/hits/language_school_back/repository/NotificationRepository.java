package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByGroup(Group group);

    List<Notification> findByGroupIn(Collection<Group> groups);
}