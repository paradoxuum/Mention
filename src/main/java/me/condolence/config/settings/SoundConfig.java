package me.condolence.config.settings;

import com.google.gson.annotations.SerializedName;

public class SoundConfig {
    @SerializedName("enabled")
    private boolean enabled = true;
    @SerializedName("sound")
    private String soundPath = "note.harp";

    private Integer volume = 50;

    public SoundConfig() {
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getSoundPath() { return soundPath; }
    public void setSoundPath(String soundPath) { this.soundPath = soundPath; }

    public Integer getVolume() { return volume; }
    public void setVolume(Integer volume) { this.volume = volume; }
}
