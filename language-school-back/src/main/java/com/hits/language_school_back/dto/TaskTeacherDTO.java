package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTeacherDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate deadline;
    private List<CommentDTO> commentList;
    private List<AttachmentDownloadInfo> attachmentDownloadInfos;
}
