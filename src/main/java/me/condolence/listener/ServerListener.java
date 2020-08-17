package me.condolence.listener;

import me.condolence.util.Debug;
import me.condolence.PlayerMentionAddon;
import me.condolence.config.ConfigHandler;
import me.condolence.util.EnumUtil;
import me.condolence.text.SplitType;
import net.labymod.api.EventManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerListener {
    private static String currentServerIP = null;
    private static String currentSplitSymbol = null;

    public static String getCurrentServerIP() { return currentServerIP; }
    public static String getCurrentSplitSymbol() { return currentSplitSymbol; }

    public static void setSplitType(String splitTypeName) {
        SplitType splitType = EnumUtil.getEnumFromName(SplitType.class, splitTypeName);
        if (splitType == null) { return; }

        currentSplitSymbol = splitType.getSplitSymbol();
    }
    public static void setSplitType() { currentSplitSymbol = null; }

    public static void init() {
        EventManager eventManager = PlayerMentionAddon.getLabyAPI().getEventManager();
        ConfigHandler configHandler = PlayerMentionAddon.getConfigHandler();

        eventManager.registerOnJoin(serverData -> {
            if(configHandler.getMainConfig() == null) { return; }

            // Get server IP
            currentServerIP = serverData.getIp().toLowerCase();

            // Get split types (key) and the accepted server IPs for each type (value)
            HashMap<String, List<String>> splitTypeMap = configHandler.getMainConfig().getSplitTypes();

            for (Map.Entry<String, List<String>> entry : splitTypeMap.entrySet()) {
                // Get split type & accepted IPs
                SplitType splitType = EnumUtil.getEnumFromName(SplitType.class, entry.getKey());
                List<String> acceptedIPs = entry.getValue();

                // Check that the current server IP ends with the accepted server IP instead of checking if it equals it.
                // This means that servers like Hypixel or The Hive which allow you to set a custom IP (e.g. AnyTextHere.hypixel.net) are supported.
                for (String acceptedIP : acceptedIPs) {
                    if (currentServerIP.endsWith(acceptedIP.toLowerCase())) {
                        if (splitType != null) {
                            currentSplitSymbol = splitType.getSplitSymbol();
                            Debug.log("Found split type '" + splitType.getTypeName() + "' for server '" + currentServerIP + "'!");
                        } else {
                            Debug.log("Could not get SplitType enum for: " + entry.getKey());
                        }

                        break;
                    }
                }
            }
        });

        eventManager.registerOnQuit(serverData -> {
            currentServerIP = null;
            currentSplitSymbol = null;
        });
    }
}
