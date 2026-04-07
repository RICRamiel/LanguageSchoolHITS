package com.hits.language_school_back.exception;

public class LanguageNotFoundException extends RuntimeException {
    public LanguageNotFoundException(String message) {
        super(message);
    }
}
