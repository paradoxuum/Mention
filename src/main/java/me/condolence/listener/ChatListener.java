package me.condolence.listener;

import me.condolence.util.Debug;
import me.condolence.PlayerMentionAddon;
import me.condolence.command.MentionCommand;
import me.condolence.config.ConfigHandler;
import me.condolence.config.settings.HighlightingConfig;
import me.condolence.config.settings.MainConfig;
import me.condolence.config.settings.SoundConfig;
import me.condolence.util.EnumUtil;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatListener {
    private static final MentionCommand mentionCommand = new MentionCommand();
    private static final Pattern formatPattern = Pattern.compile("§[\\dabcdefklmnor]", Pattern.DOTALL);

    private static List<Integer> findWordOccurrences(String textString, String word) {
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

    public static void init() {
        LabyModAPI labyModAPI = PlayerMentionAddon.getLabyAPI();
        EventManager eventManager = labyModAPI.getEventManager();
        ConfigHandler configHandler = PlayerMentionAddon.getConfigHandler();

        if (!PlayerMentionAddon.isOnForge()) {
            eventManager.register((MessageSendEvent) message -> {
                if (!message.startsWith("/")) { return false; }
                message = message.trim().substring(1);
                String[] splitMessage = message.split(" ");
                String[] args = new String[splitMessage.length - 1];
                String commandName = splitMessage[0];
                System.arraycopy(splitMessage, 1, args, 0, args.length);

                if (!commandName.equals("mention")) { return false; }

                try {
                    mentionCommand.processCommand(Minecraft.getMinecraft().thePlayer, args);
                } catch (Exception e) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cAn error occurred attempting to perform this command."));
                }

                return true;
            });
        }

        eventManager.register((MessageModifyChatEvent) o -> {
            // If the config didn't load properly then return the object - otherwise the game would crash due to a NullPointerException
            if(configHandler.getMainConfig() == null) { return o; }

            // If the addon isn't enabled then nothing should be done
            if (!configHandler.getMainConfig().isEnabled()) { return o; }
            if (!(o instanceof IChatComponent)) { return o; }

            // Get split symbol
            final String splitSymbol = ServerListener.getCurrentSplitSymbol();
            if ((splitSymbol == null) && (ServerListener.getCurrentServerIP() != null)) { return o; }

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

            // Get sender & message using String.split()
            // Use ">" for singleplayer - the chat format should be "<Username> Message"
            String[] splitMessage = unformattedText.split((splitSymbol == null) ? ">" : splitSymbol, 2);
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
            List<Integer> matchingIndexes = findWordOccurrences(formattedText.substring(chatMessageIndex), playerUsername);

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
                    TextStyle textStyle = EnumUtil.getEnumFromName(TextStyle.class, entry.getKey());

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
                messageStringBuilder.insert(startingIndex + nameFormattingCode.length() + playerUsername.length(), "§r");
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
