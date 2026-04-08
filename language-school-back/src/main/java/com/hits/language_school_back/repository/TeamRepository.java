package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    long countByTaskId(UUID taskId);

    List<Team> findAllByTaskId(UUID taskId);

    Optional<Team> findByIdAndTaskId(UUID teamId, UUID taskId);
}
