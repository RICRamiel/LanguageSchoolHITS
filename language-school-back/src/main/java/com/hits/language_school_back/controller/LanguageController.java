package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.mapper.LanguageMapper;
import com.hits.language_school_back.model.Language;
import com.hits.language_school_back.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/language")
public class LanguageController {

    private final LanguageService languageService;
    private final LanguageMapper languageMapper;

    @PostMapping("/create")
    ResponseEntity<LanguageDTO> createLanguage(@RequestBody LanguageDTO languageDTO) {
        Language language = languageService.createLanguage(languageDTO);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @PutMapping("/{languageId}/edit")
    ResponseEntity<LanguageDTO> editLanguageName(@RequestBody LanguageDTO languageDTO, @PathVariable UUID languageId) {
        Language language = languageService.editLanguageName(languageDTO, languageId);
        return ResponseEntity.ok(languageMapper.toDto(language));
    }

    @DeleteMapping("/{languageId}/delete")
    void deleteLanguage(@PathVariable UUID languageId) {
        languageService.deleteLanguage(languageId);
    }

    @GetMapping("/get_all_languages")
    ResponseEntity<List<LanguageDTO>> getLanguages() {
        return ResponseEntity.ok(languageService.getLanguages().stream().map(languageMapper::toDto).toList());
    }
}
