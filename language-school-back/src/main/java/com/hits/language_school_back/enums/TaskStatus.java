package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    COMPLETE("Сдано"),
    OVERDUE("Просрочено"),
    PENDING("Ожидает решения");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
}
