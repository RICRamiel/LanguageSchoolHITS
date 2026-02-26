package com.hits.language_school_back.filter;

import com.hits.language_school_back.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupFilter {
    private String name;
    private Difficulty difficulty;
    private String language;
}
