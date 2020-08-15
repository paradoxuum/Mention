package me.condolence.config.settings;

import com.google.gson.annotations.SerializedName;
import me.condolence.text.TextStyle;

import java.util.HashMap;

public class HighlightingConfig {
    @SerializedName("color") private String textColor = "RED";
    private final HashMap<String, Boolean> styles = new HashMap<>();

    public HighlightingConfig() {
        for (TextStyle style : TextStyle.values()) {
            styles.put(style.getStyleName(), style.getDefaultValue());
        }
    }

    public String getTextColor() { return textColor; }
    public void setTextColor(String textColor) { this.textColor = textColor; }

    public HashMap<String, Boolean> getStyles() { return styles; }
}
