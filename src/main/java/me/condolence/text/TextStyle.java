package me.condolence.text;

public enum TextStyle {
    BOLD("bold", true,"\u00a7l"),
    ITALIC("italic", false,"\u00a7o"),
    UNDERLINE( "underline", false,"\u00a7n"),
    STRIKETHROUGH("strikethrough", false,"\u00a7m");

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
