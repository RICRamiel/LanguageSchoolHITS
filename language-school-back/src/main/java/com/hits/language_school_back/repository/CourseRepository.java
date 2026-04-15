package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    @Query("SELECT COUNT(e) = :size FROM User e WHERE e.id IN :ids")
    boolean existsAllById(@Param("ids") List<UUID> ids, @Param("size") long size);
}
