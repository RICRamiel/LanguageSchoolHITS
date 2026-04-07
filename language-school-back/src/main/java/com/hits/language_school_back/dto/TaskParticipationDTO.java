package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.SolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskParticipationDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private Boolean captain;
    private Integer mark;
    private Double averageMark;
    private Integer votesCount;
    private SolutionStatus solutionStatus;
    private LocalDateTime submittedAt;
    private Boolean selectedSolution;
    private List<AttachmentDownloadInfo> attachments;
}
