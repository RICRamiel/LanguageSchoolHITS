package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long teacherId);

    List<Task> findByGroupName(String groupName);

    @Query(nativeQuery = true, value = "select * from attachments a where a.task_id = :taskid;")
    List<Attachment> getAttachmentsById(Long taskid);
}
