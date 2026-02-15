package com.hits.language_school_back.enums;

import lombok.Getter;

@Getter
public enum Role {
    TEACHER("Преподаватель"),
    STUDENT("Студент"),
    ADMIN("Админ");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }
}