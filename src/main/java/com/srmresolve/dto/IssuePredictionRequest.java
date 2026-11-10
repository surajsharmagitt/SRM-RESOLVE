package com.srmresolve.dto;

public class IssuePredictionRequest {
    private String text;
    private String description;

    public IssuePredictionRequest() {}

    public IssuePredictionRequest(String text) {
        this.text = text;
        this.description = text;
    }

    public String getText() {
        return (text != null && !text.isBlank()) ? text : description;
    }

    public void setText(String text) {
        this.text = text;
        if (this.description == null) {
            this.description = text;
        }
    }

    public String getDescription() {
        return (description != null && !description.isBlank()) ? description : text;
    }

    public void setDescription(String description) {
        this.description = description;
        if (this.text == null) {
            this.text = description;
        }
    }
}
