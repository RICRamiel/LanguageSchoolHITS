package com.hits.language_school_back.controller;

import com.hits.language_school_back.dto.LanguageDTO;
import com.hits.language_school_back.service.LanguageService;
import com.hits.language_school_back.model.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/language")
public class LanguageController {

    private final LanguageService languageService;

    @PostMapping("/create")
    ResponseEntity<Language> createLanguage(@RequestBody LanguageDTO languageDTO){
        Language language = languageService.createLanguage(languageDTO);
        return ResponseEntity.ok(language);
    }

    @PutMapping("/{languageId}/edit")
    ResponseEntity<Language> editLanguageName(@RequestBody LanguageDTO languageDTO, @PathVariable Long languageId){
        Language language = languageService.editLanguageName(languageDTO, languageId);
        return ResponseEntity.ok(language);
    }

    @DeleteMapping("/{languageId}/delete")
    void deleteLanguage(@PathVariable Long languageId){
        languageService.deleteLanguage(languageId);
    }

    @GetMapping("/get_all_languages")
    ResponseEntity<List<Language>> getLanguages(){
        return ResponseEntity.ok(languageService.getLanguages());
    }
}
