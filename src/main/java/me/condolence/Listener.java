package me.condolence;

import me.condolence.command.CommandSender;
import me.condolence.command.MentionCommand;
import me.condolence.config.ConfigHandler;
import me.condolence.config.settings.HighlightingConfig;
import me.condolence.config.settings.MainConfig;
import me.condolence.config.settings.SoundConfig;
import me.condolence.text.SplitType;
import me.condolence.text.TextColor;
import me.condolence.text.TextStyle;
import net.labymod.api.EventManager;
import net.labymod.api.LabyModAPI;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.MessageSendEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listener {
    private final LabyModAPI labyModAPI = PlayerMentionAddon.getLabyAPI();
    private final ConfigHandler configHandler  = PlayerMentionAddon.getConfigHandler();

    private final MentionCommand mentionCommand;

    // Chat patterns
    private String currentServerIP;
    private String currentSplitTypeSymbol;

    private final Pattern commandPattern = Pattern.compile("(?:/(?<c>\\w+)) ?(?<a>.*)?");
    private final Pattern formatPattern = Pattern.compile("\u00a7[\\dabcdefklmnor]", Pattern.DOTALL);

    public Listener() { mentionCommand = new MentionCommand(); }

    // Method to get all occurrences of a given word/string in a string
    private List<Integer> findWord(String textString, String word) {
        List<Integer> indexes = new ArrayList<>();

        String lowerCaseTextString = textString.toLowerCase();
        String lowerCaseWord = word.toLowerCase();
        int wordLength = 0;

        int index = 0;
        while(index != -1){
            index = lowerCaseTextString.indexOf(lowerCaseWord, index + wordLength);
            if (index != -1) {
                indexes.add(index);
            }
            wordLength = word.length();
        }
        return indexes;
    }

    private <T extends Enum<?>> T getEnum(Class<T> enumeration, String constantName) {
        for (T each : enumeration.getEnumConstants()) {
            if (each.name().compareToIgnoreCase(constantName) == 0) {
                return each;
            }
        }

        return null;
    }

    // Method to register LabyMod events
    public void registerEvents() {
        final EventManager eventManager = labyModAPI.getEventManager();
        Debug.log("Registering events...");

        // Server Join/Quit events
        eventManager.registerOnJoin(serverData -> {
            if(configHandler.getMainConfig() == null) { return; }

            // Get server IP
            final String serverIP = serverData.getIp().toLowerCase();
            currentServerIP = serverIP;

            // Get split types (key) and the accepted server IPs for each type (value)
            HashMap<String, List<String>> splitTypeMap = configHandler.getMainConfig().getSplitTypes();

            for (Map.Entry<String, List<String>> entry : splitTypeMap.entrySet()) {
                // Get split type & accepted IPs
                SplitType splitType = getEnum(SplitType.class, entry.getKey());
                List<String> acceptedIPs = entry.getValue();

                // Check that the current server IP ends with the accepted server IP instead of checking if it equals it.
                // This means that servers like Hypixel or The Hive which allow you to set a custom IP (e.g. AnyTextHere.hypixel.net) are supported.
                for (String acceptedIP : acceptedIPs) {
                    if (serverIP.endsWith(acceptedIP.toLowerCase())) {
                        if (splitType != null) {
                            currentSplitTypeSymbol = splitType.getSplitSymbol();
                            Debug.log("Found split type '" + splitType.getTypeName() + "' for server '" + serverIP + "'!");
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
            currentSplitTypeSymbol = null;
        });

        // MessageSend/MessageModify Events
        eventManager.register((MessageSendEvent) message -> {
            Debug.log("FORGE: " + PlayerMentionAddon.isOnForge());
            // Forge's command registry will handle this instead if the client is a forge client
            if (PlayerMentionAddon.isOnForge()) { return false; }

            // Check if config exists and that the addon is enabled
            if(configHandler.getMainConfig() == null) { return false; }
            if(!configHandler.getMainConfig().isEnabled()) { return false; }

            Matcher matcher = commandPattern.matcher(message);
            if (!(matcher.matches())) { return false; }

            String commandName;
            String commandArgString;

            try {
                commandName = message.substring(matcher.start("c"), matcher.end("c"));
                commandArgString = message.substring(matcher.start("a"), matcher.end("a"));
            } catch (StringIndexOutOfBoundsException e) {
                return false;
            }

            if (!(commandName.isEmpty())) {
                // Trim string and replace 2 or more spaces with a single space
                commandArgString = commandArgString.trim().replaceAll(" +", " ");

                // Split arg string at every space or create empty string array if no args were given
                String[] commandArgs = !(commandArgString.isEmpty()) ? commandArgString.split(" ") : new String[]{};

                boolean commandFound = false;
                for (String alias : mentionCommand.getAliases()) {
                    if (commandName.equalsIgnoreCase(alias)) {
                        commandFound = true;
                        break;
                    }
                }

                if (!commandFound) { return false; }

                mentionCommand.processCommand(commandArgs);

                return true;
            }

            return false;
        });

        eventManager.register((MessageModifyChatEvent) o -> {
            // If the config didn't load properly then return the object - otherwise the game would crash due to a NullPointerException
            if(configHandler.getMainConfig() == null) { return o; }

            // If the addon isn't enabled then nothing should be done
            if (!configHandler.getMainConfig().isEnabled()) { return o; }
            if (!(o instanceof IChatComponent)) { return o; }

            // Cast object to IChatComponent
            final IChatComponent component = (IChatComponent) o;

            // Get general settings
            final MainConfig mainConfig = configHandler.getMainConfig();
            final boolean matchCase = mainConfig.isMatchCaseEnabled();
            final boolean mentionSelf = mainConfig.isMentionSelfEnabled();
            final boolean debugMode = mainConfig.isDebugEnabled();

            // Get player's username
            final String playerUsername = matchCase ? labyModAPI.getPlayerUsername() : labyModAPI.getPlayerUsername().toLowerCase();

            // Get formatted & unformatted versions of the entire chat message
            final String formattedText = matchCase ? component.getFormattedText() : component.getFormattedText().toLowerCase();
            String unformattedText = matchCase ? component.getUnformattedText() : component.getUnformattedText().toLowerCase();

            // Same as .replaceAll(String regex, String replacement) except the pattern is pre-compiled, which should improve performance
            // Removes all formatting codes (section symbol + format number/letter)
            unformattedText = formatPattern.matcher(unformattedText).replaceAll("");

            // Check if the player was mentioned - no need to execute anything else if the player wasn't mentioned in the message
            // If debug mode is enabled, but the player wasn't mentioned, split the message and then return the chat object.
            if (!(unformattedText.contains(playerUsername)) && !debugMode) { return o; }

            // Get sender & message using String.split() - I've used regex for this previously but I've found splitting the string is more readable and (should be) more performant.
            // Use ">" for singleplayer - the chat format should be "<Username> Message"
            String[] splitMessage = unformattedText.split((currentServerIP == null) ? ">" : currentSplitTypeSymbol, 2);
            if (splitMessage.length <= 1) { return o; }

            String sender = splitMessage[0].trim();
            String message = splitMessage[1].trim();

            // Print sender & message to console if debug mode is enabled
            if (debugMode) {
                Debug.log(sender + " / " + message);

                if (!(unformattedText.contains(playerUsername))) { return o; }
            }

            // Check if the player was mentioned or if they mentioned themselves
            boolean playerMentioned = mentionSelf ? message.contains(playerUsername) : (!(sender.contains(playerUsername)) && message.contains(playerUsername));
            if (!playerMentioned) { return o; }

            // Get starting index of chat message (for the formatted text)
            int chatMessageIndex = formattedText.indexOf(message, formattedText.indexOf(message) + 1);

            if (chatMessageIndex == -1) {
                chatMessageIndex = formattedText.indexOf(message);
                if (chatMessageIndex == -1) { return o; }
            }

            final String formattedTextMatchCase = component.getFormattedText();

            // Get the indexes of every instance of the player's name in the chat message
            List<Integer> matchingIndexes = findWord(formattedText.substring(chatMessageIndex), playerUsername);

            // If no occurrences of the player's name was found, return the chat message object (they were not mentioned)
            // This should not happen, but there should be a check for it anyway to avoid any errors being thrown below, or an empty chat message being returned
            if (matchingIndexes.size() == 0) { return o; }

            // Get highlighting config and sound config
            final HighlightingConfig highlightingConfig = mainConfig.getHighlightConfig();
            final SoundConfig soundConfig = mainConfig.getSoundConfig();

            // Add correct formatting codes (colors & styles) for the player's name
            String colorCode = TextColor.valueOf(highlightingConfig.getTextColor()).getColorCode();
            StringBuilder formattingStringBuilder = new StringBuilder("\u00a7r" + colorCode);

            for (Map.Entry<String, Boolean> entry : mainConfig.getHighlightConfig().getStyles().entrySet()) {
                if (entry.getValue()) {
                    TextStyle textStyle = getEnum(TextStyle.class, entry.getKey());

                    if (textStyle != null) {
                        formattingStringBuilder.append(textStyle.getFormattingCode());
                    } else {
                        Debug.log("Unable to find TextStyle enum for: " + entry.getKey());
                    }
                }
            }

            String nameFormattingCode = formattingStringBuilder.toString();

            // Use StringBuilder to insert strings at certain positions
            // This might be (somewhat?) slow but it's far better than any other method of doing this (e.g. substring or String.format())
            StringBuilder messageStringBuilder = new StringBuilder(formattedTextMatchCase.substring(chatMessageIndex));

            // Iterate through matching indexes and add formatting code before player's username
            for (int i = 0 ; i < matchingIndexes.size(); i++) {
                Integer currentIndex = matchingIndexes.get(i);

                int startingIndex = currentIndex + (i * (nameFormattingCode.length() + 2));

                messageStringBuilder.insert(startingIndex, nameFormattingCode);
                messageStringBuilder.insert(startingIndex + nameFormattingCode.length() + playerUsername.length(), "\u00a7r");
            }

            // Create new chat component with the new formatting codes added for any occurrence of the player's username
            final ChatComponentText newChatComponent = new ChatComponentText(formattedTextMatchCase.substring(0, chatMessageIndex) + messageStringBuilder.toString());

            // Set the chat style to the original chat component's chat style in case any click actions or whatever were added
            newChatComponent.setChatStyle(component.getChatStyle());

            // Play sound to player if the setting is enabled
            if (soundConfig.isEnabled()) {
                final EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                if (player == null) { return newChatComponent; }

                player.playSound(soundConfig.getSoundPath(), (float) soundConfig.getVolume() / 100, 1.0f);
            }

            // Finally, return the new chat component
            return newChatComponent;
        });
    }
}
