package com.hits.language_school_back.repository;

import com.hits.language_school_back.model.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language,Long> {
    List<Language> findAllByName(String name);
}
