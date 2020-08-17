package me.condolence.util;

public class EnumUtil {
    public static <T extends Enum<?>> T getEnumFromName(Class<T> enumeration, String constantName) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(constantName) == 0) {
                return each;
            }
        }
        return null;
    }
}
