package com.netflix.conductor.sdk.workflow.def;

public class ValidationError extends RuntimeException {

    public ValidationError(String message) {
        super(message);
    }
}
