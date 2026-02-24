package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepositoryImplementation<Task, Long> {
}
