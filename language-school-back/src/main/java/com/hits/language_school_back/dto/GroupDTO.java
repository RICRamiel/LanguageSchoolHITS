package com.hits.language_school_back.dto;

import com.hits.language_school_back.enums.Difficulty;
import com.hits.language_school_back.model.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupDTO {
    @NotBlank
    String name;
    String description;
    Difficulty difficulty;
    Language language;
}
