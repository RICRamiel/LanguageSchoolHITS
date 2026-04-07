package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID teacherId);

    List<Task> findByGroupName(String groupName);

    @Query(nativeQuery = true, value = "select * from attachments a where a.task_id = :taskid;")
    List<Attachment> getAttachmentsById(UUID taskid);
}
