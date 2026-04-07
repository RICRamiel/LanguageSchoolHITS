package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum SolutionStatus {
    NEW("Новый"),
    COMMITED("Отправлено"),
    COMPLETED("Сдано"),
    OVERDUE("Просрочено"),
    ;

    private final String displayName;

    SolutionStatus(String displayName) {
        this.displayName = displayName;
    }
}
