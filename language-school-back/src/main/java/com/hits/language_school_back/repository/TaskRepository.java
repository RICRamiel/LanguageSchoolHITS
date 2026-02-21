package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
