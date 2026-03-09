package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.TaskStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskStudentRepository extends JpaRepository<TaskStudent, Long> {
    List<TaskStudent> findByUserId(Long userId);
}
