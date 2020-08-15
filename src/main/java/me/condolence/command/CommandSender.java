package me.condolence.command;

import me.condolence.PlayerMentionAddon;
import net.labymod.api.LabyModAPI;

public class CommandSender {
    private final LabyModAPI API;

    public CommandSender() {
        this.API = PlayerMentionAddon.getLabyAPI();
    }

    public String getName() {
        return API.getPlayerUsername();
    }

    public void sendMessage(String message) {
        API.displayMessageInChat(message);
    }
}
