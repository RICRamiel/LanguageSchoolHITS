package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.AssessmentStatus;
import com.hits.language_school_back.enums.AssessmentType;
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
public class AssessmentDTO {
    private UUID id;
    private UUID taskId;
    private UUID participationId;
    private UUID assessorId;
    private AssessmentType type;
    private AssessmentStatus status;
    private Integer totalPoints;
    private Integer totalMaxPoints;
    private LocalDateTime updatedAt;
    private List<AssessmentItemDTO> items;
}
