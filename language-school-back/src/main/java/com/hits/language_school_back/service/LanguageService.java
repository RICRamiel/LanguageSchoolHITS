package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.model.Language;

import java.util.List;
import java.util.UUID;

public interface LanguageService {
    Language createLanguage(LanguageDTO languageDTO);
    Language editLanguageName(LanguageDTO languageDTO, UUID languageId);
    void deleteLanguage(UUID languageId);
    List<Language> getLanguages();
}
