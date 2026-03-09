package com.hits.language_school_back.service;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.infrastructure.LanguageServiceImpl;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageServiceImpl languageService;

    private Language language1;
    private Language language2;
    private LanguageDTO languageDTO;

    @BeforeEach
    void setUp() {
        language1 = new Language();
        language1.setId(1L);
        language1.setName("English");

        language2 = new Language();
        language2.setId(2L);
        language2.setName("Spanish");

        languageDTO = new LanguageDTO();
        languageDTO.setName("French");
    }

    @Test
    @DisplayName("Should create language successfully")
    void createLanguage_ShouldSaveAndReturnLanguage() {
        // Arrange
        when(languageRepository.save(any(Language.class))).thenAnswer(invocation -> {
            Language savedLanguage = invocation.getArgument(0);
            savedLanguage.setId(3L);
            return savedLanguage;
        });

        // Act
        Language result = languageService.createLanguage(languageDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getName()).isEqualTo(languageDTO.getName());

        verify(languageRepository).save(any(Language.class));
    }

    @Test
    @DisplayName("Should edit language name successfully")
    void editLanguageName_ShouldUpdateAndReturnLanguage() {
        // Arrange
        Long languageId = 1L;
        Language existingLanguage = new Language();
        existingLanguage.setId(languageId);
        existingLanguage.setName("Old Name");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(existingLanguage));
        when(languageRepository.save(any(Language.class))).thenReturn(existingLanguage);

        // Act
        Language result = languageService.editLanguageName(languageDTO, languageId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(languageId);
        assertThat(result.getName()).isEqualTo(languageDTO.getName());

        verify(languageRepository).findById(languageId);
        verify(languageRepository).save(existingLanguage);
    }

    @Test
    @DisplayName("Should throw exception when editing non-existent language")
    void editLanguageName_WithNonExistentId_ShouldThrowException() {
        // Arrange
        Long languageId = 999L;
        when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> languageService.editLanguageName(languageDTO, languageId))
                .isInstanceOf(RuntimeException.class);

        verify(languageRepository).findById(languageId);
        verify(languageRepository, never()).save(any(Language.class));
    }

    @Test
    @DisplayName("Should delete language by ID")
    void deleteLanguage_ShouldCallRepositoryDelete() {
        // Arrange
        Long languageId = 1L;
        doNothing().when(languageRepository).deleteById(languageId);

        // Act
        languageService.deleteLanguage(languageId);

        // Assert
        verify(languageRepository).deleteById(languageId);
    }

    @Test
    @DisplayName("Should get all languages")
    void getLanguages_ShouldReturnListOfLanguages() {
        // Arrange
        List<Language> languages = Arrays.asList(language1, language2);
        when(languageRepository.findAll()).thenReturn(languages);

        // Act
        List<Language> result = languageService.getLanguages();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(language1, language2);
        verify(languageRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no languages exist")
    void getLanguages_WhenNoLanguages_ShouldReturnEmptyList() {
        // Arrange
        when(languageRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Language> result = languageService.getLanguages();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(languageRepository).findAll();
    }
}