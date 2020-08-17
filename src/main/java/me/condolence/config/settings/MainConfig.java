package me.condolence.config.settings;

import com.google.gson.annotations.SerializedName;
import me.condolence.PlayerMentionAddon;
import me.condolence.text.SplitType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainConfig {
    // Simple fields (booleans)
    @SerializedName("enabled") private boolean enabled = true;
    @SerializedName("version") private final String version = PlayerMentionAddon.getVersion();
    @SerializedName("match_case") private boolean matchCase = true;
    @SerializedName("mention_self") private boolean mentionSelf = false;
    private boolean debug = false;

    // Class fields (nested objects)
    @SerializedName("highlighting") private final HighlightingConfig highlightingConfig = new HighlightingConfig();
    @SerializedName("sound") private final SoundConfig soundConfig = new SoundConfig();
    @SerializedName("split_types")  private final HashMap<String, List<String>> splitTypes = new HashMap<>();

    public MainConfig() {
        for (SplitType type : SplitType.values()) {
            splitTypes.put(type.getTypeName(), Arrays.asList(type.getDefaultSupportedIPs()));
        }
    }

    // Setters/Getters for config values
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getVersion() { return version; }

    public boolean isMatchCaseEnabled() { return matchCase; }
    public void setMatchCaseEnabled(boolean enabled) { matchCase = enabled; }

    public boolean isMentionSelfEnabled() { return mentionSelf; }
    public void setMentionSelfEnabled(boolean enabled) { mentionSelf = enabled; }

    public boolean isDebugEnabled() { return debug; }
    public void setDebugEnabled(boolean enabled) { debug = enabled; }

    public HighlightingConfig getHighlightConfig() { return highlightingConfig; }
    public SoundConfig getSoundConfig() { return soundConfig; }

    public HashMap<String, List<String>> getSplitTypes() { return splitTypes; }
}
