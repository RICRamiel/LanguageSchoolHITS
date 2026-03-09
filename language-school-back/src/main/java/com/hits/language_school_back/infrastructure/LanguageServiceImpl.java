package com.hits.language_school_back.infrastructure;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.service.LanguageService;
import com.hits.language_school_back.model.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository languageRepository;

    @Override
    public Language createLanguage(LanguageDTO languageDTO) {
        Language language = new Language();
        language.setName(languageDTO.getName());
        languageRepository.save(language);
        return language;
    }

    @Override
    public Language editLanguageName(LanguageDTO languageDTO, Long languageId) {
        Language language = languageRepository.findById(languageId).orElseThrow();
        language.setName(languageDTO.getName());
        languageRepository.save(language);
        return language;
    }

    @Override
    public void deleteLanguage(Long languageId) {
        languageRepository.deleteById(languageId);
    }

    @Override
    public List<Language> getLanguages() {
        return languageRepository.findAll();
    }
}
