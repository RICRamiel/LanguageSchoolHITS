package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Difficulty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class GroupDTO {
    @NotBlank
    private String name;
    private String description;
    private Difficulty difficulty;
    private LanguageDTO language;
}
