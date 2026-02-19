package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.model.Language;

import java.util.List;

public interface LanguageService {
    Language createLanguage(LanguageDTO languageDTO);
    Language editLanguageName(LanguageDTO languageDTO, Long languageId);
    void deleteLanguage(Long languageId);
    List<Language> getLanguages();
}
