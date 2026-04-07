package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.AttachmentDownloadInfo;
import com.hits.language_school_back.dto.TaskTeacherDTO;
import com.hits.language_school_back.model.Attachment;
import com.hits.language_school_back.model.Task;
import com.hits.language_school_back.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskTeacherMapper {

    private final CommentMapper commentMapper;
    private final AttachmentRepository attachmentRepository;
    private final TaskTeamMapper taskTeamMapper;

    public TaskTeacherDTO toDto(Task task) {
        List<AttachmentDownloadInfo> attachmentsInfo = attachmentRepository.findAllByTaskId(task.getId()).stream()
                .map(this::mapAttachment)
                .toList();

        return TaskTeacherDTO.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .deadline(task.getDeadline())
                .courseId(task.getCourse() == null ? null : task.getCourse().getId())
                .courseName(task.getCourse() == null ? null : task.getCourse().getName())
                .totalPoints(task.getTotalPoints())
                .maxTeamSize(task.getMaxTeamSize())
                .minTeamSize(task.getMinTeamSize())
                .maxTeamsAmount(task.getMaxTeamsAmount())
                .minTeamsAmount(task.getMinTeamsAmount())
                .votesThreshold(task.getVotesThreshold())
                .taskType(task.getTaskType())
                .teamType(task.getTeamType())
                .resolveType(task.getResolveType())
                .submissionClosed(task.getSubmissionClosed())
                .finalizedAt(task.getFinalizedAt())
                .attachmentDownloadInfos(attachmentsInfo)
                .commentList(task.getCommentList() == null ? List.of() : task.getCommentList().stream().map(commentMapper::toDto).toList())
                .teams(task.getTeamList() == null ? List.of() : task.getTeamList().stream().map(taskTeamMapper::toDto).toList())
                .build();
    }

    public List<TaskTeacherDTO> toDtoList(List<Task> tasks) {
        if (tasks == null) {
            return null;
        }

        return tasks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AttachmentDownloadInfo mapAttachment(Attachment attachment) {
        return AttachmentDownloadInfo.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .objectKey(attachment.getObjectKey())
                .build();
    }
}
