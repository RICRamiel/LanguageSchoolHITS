package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.StudentsInCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentsInCourseRepository extends JpaRepository<StudentsInCourse, UUID> {

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    @Query("SELECT sic.student.id FROM StudentsInCourse sic " +
            "WHERE sic.course.id = :courseId AND sic.student.id IN :studentIds")
    List<UUID> findAlreadyEnrolledStudentIds(@Param("courseId") UUID courseId,
                                             @Param("studentIds") Collection<UUID> studentIds);

    Optional<StudentsInCourse> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    List<StudentsInCourse> findAllByCourseId(UUID courseId);
}
