package me.condolence.text;

public enum TextColor {
    DARK_RED("§4"),
    RED("§c"),
    GOLD("§6"),
    YELLOW("§e"),
    DARK_GREEN("§2"),
    GREEN("§a"),
    AQUA("§b"),
    DARK_AQUA("§3"),
    DARK_BLUE("§1"),
    BLUE("§9"),
    PURPLE("§d"),
    DARK_PURPLE("§5"),
    WHITE("§f"),
    GRAY("§7"),
    DARK_GRAY("§8"),
    BLACK("§0");


    private final String colorCode;

    TextColor(final String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return this.colorCode;
    }
}
