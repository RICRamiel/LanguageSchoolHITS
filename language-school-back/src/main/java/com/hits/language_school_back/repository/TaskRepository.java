package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findAllByOrderByDeadlineAsc();

    List<Task> findAllByCourseTeacherIdOrderByDeadlineAsc(UUID teacherId);

    List<Task> findAllByCourseNameOrderByDeadlineAsc(String courseName);

    List<Task> findAllByCourseIdOrderByDeadlineAsc(UUID courseId);
}
