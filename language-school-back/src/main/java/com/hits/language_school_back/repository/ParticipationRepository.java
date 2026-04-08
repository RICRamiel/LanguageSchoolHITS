package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipationRepository extends JpaRepository<Participation, UUID> {
    Optional<Participation> findByTeamIdAndStudentId(UUID teamId, UUID studentId);

    List<Participation> findAllByTeamId(UUID teamId);

    List<Participation> findAllByTeamTaskId(UUID taskId);

    List<Participation> findAllByStudentId(UUID studentId);
}
