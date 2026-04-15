package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findAllByTaskId(UUID taskId);

    List<Attachment> findAllByParticipationId(UUID participationId);
}
