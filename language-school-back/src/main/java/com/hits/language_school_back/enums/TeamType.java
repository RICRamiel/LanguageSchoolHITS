package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum TeamType {
    RANDOM("Случайно"),
    FREEROAM("Свободный выбор"),
    DRAFT("Драфт");

    private final String displayName;

    TeamType(String displayName) {
        this.displayName = displayName;
    }
}
