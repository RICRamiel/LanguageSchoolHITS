package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long teacherId);

    List<Task> findByGroupName(String groupName);
}
