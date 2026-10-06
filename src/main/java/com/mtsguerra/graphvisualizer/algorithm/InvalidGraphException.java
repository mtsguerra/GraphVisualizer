package com.mtsguerra.graphvisualizer.algorithm;

/** Thrown when the input graph can't be processed; mapped to HTTP 400. */
public class InvalidGraphException extends RuntimeException {

    public InvalidGraphException(String message) {
        super(message);
    }
}
