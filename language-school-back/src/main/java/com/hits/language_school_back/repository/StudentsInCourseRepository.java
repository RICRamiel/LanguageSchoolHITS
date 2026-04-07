package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.StudentsInCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentsInCourseRepository extends JpaRepository<StudentsInCourse, UUID> {

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
