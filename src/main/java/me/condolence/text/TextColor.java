package me.condolence.text;

public enum TextColor {
    DARK_RED("\u00a74"),
    RED("\u00a7c"),
    GOLD("\u00a76"),
    YELLOW("\u00a7e"),
    DARK_GREEN("\u00a72"),
    GREEN("\u00a7a"),
    AQUA("\u00a7b"),
    DARK_AQUA("\u00a73"),
    DARK_BLUE("\u00a71"),
    BLUE("\u00a79"),
    PURPLE("\u00a7d"),
    DARK_PURPLE("\u00a75"),
    WHITE("\u00a7f"),
    GRAY("\u00a77"),
    DARK_GRAY("\u00a78"),
    BLACK("\u00a70");


    private final String colorCode;

    TextColor(final String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return this.colorCode;
    }
}
