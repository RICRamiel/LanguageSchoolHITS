package com.hits.language_school_back.exception;

public class CourseNotFound extends RuntimeException {
    public CourseNotFound(String message) {
        super(message);
    }
}
