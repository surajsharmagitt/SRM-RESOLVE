package com.srmresolve.dto;

public class AiEnhanceResponse {
    private String enhancedText;
    private boolean fallbackUsed;

    public AiEnhanceResponse() {}

    public AiEnhanceResponse(String enhancedText, boolean fallbackUsed) {
        this.enhancedText = enhancedText;
        this.fallbackUsed = fallbackUsed;
    }

    public String getEnhancedText() { return enhancedText; }
    public void setEnhancedText(String enhancedText) { this.enhancedText = enhancedText; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}

