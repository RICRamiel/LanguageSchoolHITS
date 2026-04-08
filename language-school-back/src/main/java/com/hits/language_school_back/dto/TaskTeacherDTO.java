package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.TaskResolveType;
import com.hits.language_school_back.enums.TeamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTeacherDTO {
    private UUID id;
    private String name;
    private String description;
    private LocalDate deadline;
    private UUID courseId;
    private String courseName;
    private Integer totalPoints;
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private Integer maxTeamsAmount;
    private Integer minTeamsAmount;
    private Integer votesThreshold;
    private TeamType teamType;
    private TaskResolveType resolveType;
    private Boolean submissionClosed;
    private LocalDateTime finalizedAt;
    private List<CommentDTO> commentList;
    private List<AttachmentDownloadInfo> attachmentDownloadInfos;
    private List<TaskTeamDTO> teams;
}
