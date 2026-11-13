package com.airporttaxi.exception;

public class RuleViolationException extends RuntimeException {

    private final String rule;

    public RuleViolationException(String rule, String message) {
        super(String.format("[%s Violation] %s", rule, message));
        this.rule = rule;
    }

    public RuleViolationException(String message) {
        super(message);
        this.rule = "GENERAL";
    }

    public String getRule() {
        return rule;
    }
}
