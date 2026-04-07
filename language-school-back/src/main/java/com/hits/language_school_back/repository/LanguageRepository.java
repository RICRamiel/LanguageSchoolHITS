package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {
    List<Language> findAllByName(String name);
}
