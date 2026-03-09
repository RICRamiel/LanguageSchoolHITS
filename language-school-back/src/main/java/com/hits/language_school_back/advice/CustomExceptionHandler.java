package com.hits.language_school_back.advice;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception e) {
        ProblemDetail errorDetail = null;

        log.warn(e.getMessage(), e);

        if (e instanceof BadCredentialsException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(401), e.getMessage());
            errorDetail.setProperty("access_denied_reason", "Authentication Failure");
        }
        if (e instanceof InternalAuthenticationServiceException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(401), "Bad Credentials");
            errorDetail.setProperty("access_denied_reason", "Authentication Failure");
        }

        if (e instanceof AccessDeniedException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(403), e.getMessage());
            errorDetail.setProperty("access_denied_reason", "Not Authorized");
        }

        if (e instanceof AuthorizationDeniedException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(403), e.getMessage());
            errorDetail.setProperty("access_denied_reason", "Not Authorized");
        }

        if (e instanceof SignatureException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(403), e.getMessage());
            errorDetail.setProperty("access_denied_reason", "JWT Signature Error");
        }

        if (e instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail
                    .forStatusAndDetail(HttpStatusCode.valueOf(403), e.getMessage());
            errorDetail.setProperty("access_denied_reason", "JWT Token Expired");
        }

        return errorDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException e) {
        AtomicReference<String> errors = new AtomicReference<>("");
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.set(errors + String.join(": ", error.getField(), error.getDefaultMessage()) + " \n ")
        );

        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), errors.get());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(404), "Resource not found: " + e.getMessage());
    }

//    @ExceptionHandler(HibernateException.class)
//    public ProblemDetail handleHibernateException(HibernateException e) {
//        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Resource not found: " + e.getMessage());
//    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(400), "Invalid argument: " + e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ProblemDetail handleNullPointerException(NullPointerException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Unexpected null value encountered." + e.getMessage());
    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ProblemDetail handleRuntimeException(RuntimeException e) {
//        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), "Unexpected error encountered." + e.getMessage());
//    }
}