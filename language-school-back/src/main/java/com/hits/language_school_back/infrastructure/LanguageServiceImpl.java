package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.service.LanguageService;
import com.hits.language_school_back.model.Language;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LanguageServiceImpl implements LanguageService {
    @Override
    public Language createLanguage(LanguageDTO languageDTO) {
        return null;
    }

    @Override
    public Language editLanguageName(LanguageDTO languageDTO, Long languageId) {
        return null;
    }

    @Override
    public void deleteLanguage(Long languageId) {

    }

    @Override
    public List<Language> getLanguages() {
        return List.of();
    }
}
