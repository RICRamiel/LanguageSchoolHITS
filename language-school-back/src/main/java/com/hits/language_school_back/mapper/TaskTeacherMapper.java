package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.infrastructure.AttachmentServiceImpl;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class TaskTeacherMapper {

    private final CommentMapper commentMapper;
    private final AttachmentServiceImpl attachmentService;
    private final TaskRepository taskRepository;

    public TaskTeacherDTO toDto(Task task) {
        if (task == null) {
            return null;
        }
        List<Attachment> attachments = taskRepository.getAttachmentsById(task.getId());

        TaskTeacherDTO dto = new TaskTeacherDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setDeadline(task.getDeadline());

        List<AttachmentDownloadInfo> attachmentsInfo = new java.util.ArrayList<>(List.of());
        attachments.forEach(attachment -> {
            AttachmentDownloadInfo attachmentInfo = attachmentService.getDownloadInfo(attachment.getId());
            attachmentsInfo.add(attachmentInfo);
        });

        dto.setAttachmentDownloadInfos(attachmentsInfo);
        dto.setCommentList(task.getCommentList().stream().map(commentMapper::toDto).toList());

        return dto;
    }

    public List<TaskTeacherDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}