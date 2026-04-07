package com.hits.language_school_back.enums;

import java.util.EnumSet;

public enum TaskType {
    INDIVIDUAL(EnumSet.of(TeamType.SOLO)),
    TEAMWORK(EnumSet.of(TeamType.RANDOM, TeamType.FREEROAM, TeamType.DRAFT));

    private final EnumSet<TeamType> supportedTeamTypes;

    TaskType(EnumSet<TeamType> supportedTeamTypes) {
        this.supportedTeamTypes = supportedTeamTypes;
    }

    public boolean supports(TeamType teamType) {
        return supportedTeamTypes.contains(teamType);
    }
}
