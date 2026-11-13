package com.airporttaxi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String rule;

    private String message;

    public ErrorResponse() {}

    public ErrorResponse(LocalDateTime timestamp, String rule, String message) {
        this.timestamp = timestamp;
        this.rule = rule;
        this.message = message;
    }

    public static ErrorResponse of(LocalDateTime timestamp, String rule, String message) {
        return new ErrorResponse(timestamp, rule, message);
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getRule() { return rule; }
    public void setRule(String rule) { this.rule = rule; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime timestamp;
        private String rule;
        private String message;

        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder rule(String rule) { this.rule = rule; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public ErrorResponse build() { return new ErrorResponse(timestamp, rule, message); }
    }
}
