package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Participation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipationRepository extends JpaRepository<Participation, UUID> {
}
