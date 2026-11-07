package aqa.enums;

public enum ActionType {
    LOGIN("LOGIN"),
    ACTION("ACTION"),
    LOGOUT("LOGOUT");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}