package me.condolence.command;

import net.labymod.api.LabyModAPI;

public class CommandSender {
    private final LabyModAPI API;

    public CommandSender(LabyModAPI API) {
        this.API = API;
    }

    public String getName() {
        return API.getPlayerUsername();
    }

    public void sendMessage(String message) {
        API.displayMessageInChat(message);
    }
}
