package ru.practicum.error.model;


public class ErrorResponse {

    private final String error;
    private final String description;

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
