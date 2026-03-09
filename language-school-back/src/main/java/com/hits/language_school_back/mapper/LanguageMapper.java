package com.hits.language_school_back.mapper;

import com.hits.language_school_back.dto.GroupDTO;
import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.model.Group;
import com.hits.language_school_back.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageMapper {
    public LanguageDTO toDto(Language language) {
        return new LanguageDTO(language.getName());
    }
}
