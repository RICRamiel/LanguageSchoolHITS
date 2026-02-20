package com.hits.language_school_back.languageServiceTests;

import com.hits.language_school_back.repository.LanguageRepository;
import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.infrastructure.LanguageServiceImpl;
import com.hits.language_school_back.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private LanguageServiceImpl languageService;

    private LanguageDTO languageDTO;
    private Language language;
    private Long languageId;

    @BeforeEach
    void setUp() {
        languageId = 1L;

        languageDTO = new LanguageDTO();
        languageDTO.setName("Russian");

        language = new Language();
        language.setId(languageId);
        language.setName("Russian");
    }

    @Test
    void createLanguage_ShouldReturnCreatedLanguage() {
        // Arrange
        when(languageRepository.save(any(Language.class))).thenReturn(language);

        // Act
        Language createdLanguage = languageService.createLanguage(languageDTO);

        // Assert
        assertNotNull(createdLanguage);
        assertEquals(language.getName(), createdLanguage.getName());
        verify(languageRepository, times(1)).save(any(Language.class));
    }

    @Test
    void editLanguageName_ShouldReturnUpdatedLanguage() {
        // Arrange
        LanguageDTO updateDTO = new LanguageDTO();
        updateDTO.setName("English");

        Language updatedLanguage = new Language();
        updatedLanguage.setId(languageId);
        updatedLanguage.setName("English");

        when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
        when(languageRepository.save(any(Language.class))).thenReturn(updatedLanguage);

        // Act
        Language result = languageService.editLanguageName(updateDTO, languageId);

        // Assert
        assertNotNull(result);
        assertEquals("English", result.getName());
        verify(languageRepository, times(1)).findById(languageId);
        verify(languageRepository, times(1)).save(any(Language.class));
    }

    @Test
    void deleteLanguage_ShouldDeleteLanguage() {
        // Arrange
        doNothing().when(languageRepository).deleteById(languageId);

        // Act
        languageService.deleteLanguage(languageId);

        // Assert
        verify(languageRepository, times(1)).deleteById(languageId);
    }

    @Test
    void getLanguages_ShouldReturnListOfLanguages() {
        // Arrange

        Language language1 = new Language();
        language1.setId(2L);
        language1.setName("English");

        Language language2 = new Language();
        language2.setId(3L);
        language2.setName("Spanish");

        List<Language> languages = Arrays.asList(
                language,
                language1,
                language2
        );

        when(languageRepository.findAll()).thenReturn(languages);

        // Act
        List<Language> result = languageService.getLanguages();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Russian", result.get(0).getName());
        assertEquals("English", result.get(1).getName());
        assertEquals("Spanish", result.get(2).getName());
        verify(languageRepository, times(1)).findAll();
    }
}