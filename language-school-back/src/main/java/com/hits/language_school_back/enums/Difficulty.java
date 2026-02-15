package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum Difficulty {
    BEGINNER("Beginner"),
    ELEMENTARY("Elementary"),
    UPPER_INTERMEDIATE("Upper Intermediate"),
    ADVANCED("Advanced"),
    PROFICIENCY("Proficiency"),;

    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }
}
