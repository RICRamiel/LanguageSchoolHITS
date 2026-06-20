package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.PeerReviewDistributionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeerReviewEnableDTO {
    @NotNull
    private PeerReviewDistributionType peerReviewDistributionType;

    private Boolean peerReviewerVisibleToTeams;
}
