package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum TaskResolveType {
    FIRST_SUBMITTED_SOLUTION("Первое решение"),
    LAST_SUBMITTED_SOLUTION("Последнее решение"),
    CAPTAINS_SOLUTION("Решение капитана"),
    MOST_VOTES_SOLUTION("Решение с большинством голосов"),
    AT_LEAST_VOTES_SOLUTION("Решение с минимальным количество голосов");

    private final String displayName;

    TaskResolveType(String displayName) {
        this.displayName = displayName;
    }
}
