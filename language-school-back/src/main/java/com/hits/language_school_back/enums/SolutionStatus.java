package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum SolutionStatus {
    DRAFT("Черновик"),
    SUBMITTED("Отправлено"),
    SELECTED("Выбрано"),
    LOCKED("Закреплено"),
    OVERDUE("Просрочено");

    private final String displayName;

    SolutionStatus(String displayName) {
        this.displayName = displayName;
    }
}
