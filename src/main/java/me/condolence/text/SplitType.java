package me.condolence.text;

public enum SplitType {
    DEFAULT("default", ">", new String[]{}),
    DOUBLE_RIGHT_ARROW("double_right_arrow", "»", new String[]{"hive.sexy", "hivemc.eu", "hivemc.us", "j2o.xyz"}),
    COLON("colon", ":", new String[]{"hypixel.net", "roxbot.com", "gommehd.net"});

    private final String typeName;
    private final String splitSymbol;
    private final String[] defaultSupportedIPs;

    SplitType(String typeName, String splitSymbol, String[] defaultSupportedIPs) {
        this.typeName = typeName;
        this.splitSymbol = splitSymbol;
        this.defaultSupportedIPs = defaultSupportedIPs;
    }

    public String getTypeName() { return typeName; }

    public String getSplitSymbol() { return splitSymbol; }

    public String[] getDefaultSupportedIPs() { return defaultSupportedIPs; }
}
