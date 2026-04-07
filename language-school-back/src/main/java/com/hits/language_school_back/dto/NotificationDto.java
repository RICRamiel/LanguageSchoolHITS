package com.hits.language_school_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private UUID id;

    private String text;

    private UUID groupId;

    private LocalDate creationDate;

    private UUID createdByTeacherWithId;

    private List<AttachmentDownloadInfo> attachments;
}
