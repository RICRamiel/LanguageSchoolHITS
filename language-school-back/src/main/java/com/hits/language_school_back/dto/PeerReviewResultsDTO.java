package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.PeerReviewDistributionType;
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
public class PeerReviewResultsDTO {
    private UUID taskId;
    private Boolean peerReviewEnabled;
    private PeerReviewDistributionType peerReviewDistributionType;
    private Boolean peerReviewerVisibleToTeams;
    private LocalDateTime peerReviewConfirmedAt;
    private Integer totalMaxPoints;
    private List<PeerReviewResultDTO> results;
}
