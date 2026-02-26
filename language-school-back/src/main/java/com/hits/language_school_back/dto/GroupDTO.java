package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.model.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupDTO {
    @NotBlank
    private String name;
    private String description;
    private Difficulty difficulty;
    private Language language;
}
