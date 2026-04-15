package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.infrastructure.LanguageServiceImpl;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.repository.LanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageServiceImpl languageService;

    @Test
    void createLanguage_savesLanguageFromDto() {
        LanguageDTO dto = new LanguageDTO(null, "French");
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Language result = languageService.createLanguage(dto);

        assertThat(result.getName()).isEqualTo("French");
        verify(languageRepository).save(any(Language.class));
    }

    @Test
    void editLanguageName_updatesExistingLanguage() {
        UUID languageId = UUID.randomUUID();
        Language language = new Language();
        language.setId(languageId);
        language.setName("English");
        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.save(language)).thenReturn(language);

        Language result = languageService.editLanguageName(new LanguageDTO(null, "Spanish"), languageId);

        assertThat(result.getName()).isEqualTo("Spanish");
        verify(languageRepository).save(language);
    }

    @Test
    void editLanguageName_whenMissing_throws() {
        UUID languageId = UUID.randomUUID();
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> languageService.editLanguageName(new LanguageDTO(null, "Spanish"), languageId))
                .isInstanceOf(RuntimeException.class);

        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    void getLanguages_returnsRepositoryResult() {
        Language first = new Language();
        Language second = new Language();
        when(languageRepository.findAll()).thenReturn(List.of(first, second));

        assertThat(languageService.getLanguages()).containsExactly(first, second);
    }
}
