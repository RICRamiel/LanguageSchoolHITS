package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
    long countByParticipationId(UUID participationId);

    boolean existsByParticipationIdAndUserId(UUID participationId, UUID userId);
}
