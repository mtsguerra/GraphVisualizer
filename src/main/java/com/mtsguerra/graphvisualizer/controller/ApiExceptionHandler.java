package com.mtsguerra.graphvisualizer.controller;

import com.mtsguerra.graphvisualizer.algorithm.InvalidGraphException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidGraphException.class)
    public ProblemDetail handleInvalidGraph(InvalidGraphException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
