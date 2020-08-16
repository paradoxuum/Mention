package me.condolence.text;

public enum TextStyle {
    BOLD("bold", true,"§l"),
    ITALIC("italic", false,"§o"),
    UNDERLINE( "underline", false,"§n"),
    STRIKETHROUGH("strikethrough", false,"§m");

    private final String styleName;
    private final boolean defaultValue;
    private final String formattingCode;

    TextStyle(String styleName, boolean defaultValue, String formattingCode) {
        this.styleName = styleName;
        this.defaultValue = defaultValue;
        this.formattingCode = formattingCode;
    }

    public String getStyleName() { return styleName; }
    public boolean getDefaultValue() { return defaultValue; }
    public String getFormattingCode() { return formattingCode; }
}
