package me.condolence;

import me.condolence.command.MentionCommand;
import me.condolence.config.ConfigHandler;
import me.condolence.config.settings.HighlightingConfig;
import me.condolence.config.settings.MainConfig;
import me.condolence.config.settings.SoundConfig;
import me.condolence.listener.ChatListener;
import me.condolence.listener.ServerListener;
import me.condolence.text.TextColor;
import net.labymod.api.LabyModAPI;
import net.labymod.api.LabyModAddon;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;

import java.util.HashMap;
import java.util.List;

public class PlayerMentionAddon extends LabyModAddon {
    // Using gson for a far more organised config file that's not just limited to a single JsonObject
    private static final ConfigHandler configHandler = new ConfigHandler();
    private static LabyModAPI API;
    private static boolean isOnForge;

    @Override
    public void onEnable() {
        API = getApi();

        try {
            net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new MentionCommand());
            isOnForge = true;
        } catch (NoClassDefFoundError e) {
            isOnForge = false;
        }

        // Register listener events
        ChatListener.init();
        ServerListener.init();
    }

    @Override
    public void loadConfig() {
        configHandler.loadConfig();
    }

    @Override
    protected void fillSettings(List<SettingsElement> subSettings) {
        // Method call to getMainConfig() is required each time in order to update the config correctly
        // However, some values don't need to be constantly updated (current/default value for control elements)
        final MainConfig mainConfig = configHandler.getMainConfig();
        final HighlightingConfig highlightingConfig = mainConfig.getHighlightConfig();
        final SoundConfig soundConfig = mainConfig.getSoundConfig();
        final HashMap<String, Boolean> textStyles = highlightingConfig.getStyles();

        //-- Create general settings (mod enabled, match case, mention self) --//
        subSettings.add(new HeaderElement("General"));

        subSettings.add(new BooleanElement("Enabled", new ControlElement.IconData(Material.EMERALD), enabled -> {
            configHandler.getMainConfig().setEnabled(enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, mainConfig.isEnabled()));

        subSettings.add(new BooleanElement("Match case", new ControlElement.IconData(Material.REDSTONE_TORCH_ON), enabled -> {
            configHandler.getMainConfig().setMatchCaseEnabled(enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, mainConfig.isMatchCaseEnabled()));

        subSettings.add(new BooleanElement("Allow mentioning yourself", new ControlElement.IconData(Material.REDSTONE_TORCH_ON), enabled -> {
            configHandler.getMainConfig().setMentionSelfEnabled(enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, mainConfig.isMentionSelfEnabled()));

        //-- Create highlight settings (color, text styles) --//

        // Color drop down menu
        subSettings.add(new HeaderElement("Highlighting"));

        final DropDownMenu<TextColor> dropDownMenu = new DropDownMenu<TextColor>("Highlight Color", 0, 0, 0, 0)
                .fill(TextColor.values());

        DropDownElement<TextColor> dropDown  = new DropDownElement<>("Highlight Color", dropDownMenu);

        TextColor selectedColor;

        try {
            selectedColor = TextColor.valueOf(highlightingConfig.getTextColor());
        } catch (IllegalArgumentException | NullPointerException e) {
            selectedColor = TextColor.RED;

            configHandler.getMainConfig().getHighlightConfig().setTextColor("RED");
            configHandler.saveConfig();
            configHandler.loadConfig();
        }

        dropDownMenu.setSelected(selectedColor);

        dropDown.setChangeListener(textColor -> {
            configHandler.getMainConfig().getHighlightConfig().setTextColor(textColor.name());
            configHandler.saveConfig();
            configHandler.loadConfig();
        });

        subSettings.add(dropDown);

        // Text style boolean elements
        subSettings.add(new BooleanElement("Bold", new ControlElement.IconData(Material.PAPER), (enabled) -> {
            configHandler.getMainConfig().getHighlightConfig().getStyles().put("bold", enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, textStyles.get("bold")));

        subSettings.add(new BooleanElement("Italic", new ControlElement.IconData(Material.PAPER), (enabled) -> {
            configHandler.getMainConfig().getHighlightConfig().getStyles().put("italic", enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, textStyles.get("italic")));

        subSettings.add(new BooleanElement("Underline", new ControlElement.IconData(Material.PAPER), (enabled) -> {
            configHandler.getMainConfig().getHighlightConfig().getStyles().put("underline", enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, textStyles.get("underline")));

        subSettings.add(new BooleanElement("Strikethrough", new ControlElement.IconData(Material.PAPER), (enabled) -> {
            configHandler.getMainConfig().getHighlightConfig().getStyles().put("strikethrough", enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, textStyles.get("strikethrough")));

        //-- Create sound settings (enabled, sound path, volume) --//
        subSettings.add(new HeaderElement("Mention Sound"));

        // Sound boolean elements
        subSettings.add(new BooleanElement("Play Sound", new ControlElement.IconData(Material.NOTE_BLOCK), (enabled) -> {
            configHandler.getMainConfig().getSoundConfig().setEnabled(enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, soundConfig.isEnabled()));

        subSettings.add(new StringElement("Sound", new ControlElement.IconData(Material.NOTE_BLOCK), soundConfig.getSoundPath(), (soundPath) -> {
           configHandler.getMainConfig().getSoundConfig().setSoundPath(soundPath);
           configHandler.saveConfig();
           configHandler.loadConfig();
        }));

        // Volume slider element
        final SliderElement volumeSliderElement = new SliderElement("Volume", new ControlElement.IconData(Material.NOTE_BLOCK), soundConfig.getVolume());

        volumeSliderElement.setRange(0, 100);
        volumeSliderElement.setSteps(100 / 20);

        volumeSliderElement.addCallback(integer -> {
            configHandler.getMainConfig().getSoundConfig().setVolume(integer);
            configHandler.saveConfig();
            configHandler.loadConfig();
        });

        subSettings.add(volumeSliderElement);

        //-- Debug Setting --//
        subSettings.add(new HeaderElement("Debug"));

        subSettings.add(new BooleanElement("Print 'Sender/Message' to console", new ControlElement.IconData(Material.COMMAND), (enabled) -> {
            configHandler.getMainConfig().setDebugEnabled(enabled);
            configHandler.saveConfig();
            configHandler.loadConfig();
        }, mainConfig.isDebugEnabled()));
    }

    public static ConfigHandler getConfigHandler() { return configHandler; }

    public static LabyModAPI getLabyAPI() { return API; }

    public static boolean isOnForge() { return isOnForge; }

    public static String getVersion() {
        return "1.0";
    }
}
