package me.condolence.command;

import me.condolence.PlayerMentionAddon;
import me.condolence.config.ConfigHandler;
import me.condolence.listener.ServerListener;
import me.condolence.text.SplitType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.*;

public class MentionCommand extends CommandBase {
    final String commandUsage;
    final ConfigHandler configHandler = PlayerMentionAddon.getConfigHandler();
    final ArrayList<String> availableSplitTypes = new ArrayList<>();

    public MentionCommand() {
        // Add all split type options to be used in the command usage string
        // Over-engineered? Probably. I'm able to add more split types in the future without having to change this, at least.
        StringBuilder splitTypesBuilder = new StringBuilder("§3Split Options:\n");
        for (SplitType type : SplitType.values()) {
            splitTypesBuilder
                    .append("   §9")
                    .append(type.getTypeName())
                    .append(" (")
                    .append(type.getSplitSymbol())
                    .append(")\n");

            availableSplitTypes.add(type.getTypeName());
        }

        // Build command usage string
        commandUsage = "\n§b§lMention Addon Usage:\n" +
                "   §9/mention add <ip> <split_type>\n" +
                "   §9/mention remove <ip>\n" +
                "§3The IP argument should be the IP of a server\n" +
                splitTypesBuilder.toString();
    }

    @Override
    public String getCommandName() { return "mention"; }

    @Override
    public String getCommandUsage(ICommandSender sender) { return ""; }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        final String INVALID_COMMAND_USAGE = "§cInvalid command usage! Use /mention to list all commands.";
        final HashMap<String, List<String>> splitTypes = configHandler.getMainConfig().getSplitTypes();

        boolean configChanged = false;

        switch(args.length) {
            case 0:
                sender.addChatMessage(new ChatComponentText(commandUsage));

                break;
            case 1:
                if (args[0].equalsIgnoreCase("list")) {
                    StringBuilder listStringBuilder = new StringBuilder("\n§b§lSplit Types:§r\n");

                    for (Map.Entry<String, List<String>> entry : splitTypes.entrySet()) {
                        listStringBuilder
                                .append("§9")
                                .append(entry.getKey())
                                .append(": §3")
                                .append((entry.getValue().size() > 0) ? String.join(", ", entry.getValue()) : "NONE")
                                .append("§r\n");
                    }

                    sender.addChatMessage(new ChatComponentText(listStringBuilder.toString()));
                } else {
                    sender.addChatMessage(new ChatComponentText(INVALID_COMMAND_USAGE));
                }

                break;
            case 2:
                if (args[0].equalsIgnoreCase("remove")) {
                    String serverIP = args[1].toLowerCase();
                    final String currentServerIP = ServerListener.getCurrentServerIP();

                    if (serverIP.equals("current")) {
                        serverIP = currentServerIP;
                        if (serverIP == null) {
                            sender.addChatMessage(new ChatComponentText("§cCould not set split type for current server! (Player is in singleplayer)"));
                            return;
                        }
                    }

                    for (SplitType type : SplitType.values()) {
                        for (String defaultIP : type.getDefaultSupportedIPs()) {
                            if (serverIP.equals(defaultIP)) {
                                sender.addChatMessage(new ChatComponentText("§cYou cannot remove default config IPs!"));

                                return;
                            }
                        }
                    }

                    boolean removed = false;
                    for (Map.Entry<String, List<String>> entrySet : splitTypes.entrySet()) {
                        Iterator<String> iterator = entrySet.getValue().iterator();

                        while (iterator.hasNext()) {
                            if (serverIP.equals(iterator.next().toLowerCase())) {
                                iterator.remove();
                                removed = true;
                                break;
                            }
                        }
                    }

                    sender.addChatMessage(new ChatComponentText(removed ? "§aSuccessfully removed IP from config!" : "§cCould not find an IP with that name to remove!"));
                    configChanged = removed;

                    if (removed && ((currentServerIP != null) && (currentServerIP.equals(serverIP)))) {
                        ServerListener.setSplitType();
                        sender.addChatMessage(new ChatComponentText("§aUnset split type for current server!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText(INVALID_COMMAND_USAGE));
                }

                break;
            case 3:
                if (args[0].equalsIgnoreCase("add")) {
                    String serverIP = args[1].toLowerCase();
                    final String splitType = args[2].toLowerCase();


                    if (splitTypes.get(splitType) != null) {
                        if (serverIP.isEmpty()) {
                            sender.addChatMessage(new ChatComponentText("§cInvalid IP given!"));
                            return;
                        }

                        String currentServerIP = ServerListener.getCurrentServerIP();

                        if (serverIP.equals("current")) {
                            serverIP = currentServerIP;
                            if (serverIP == null) {
                                sender.addChatMessage(new ChatComponentText("§cCould not set split type for current server! (Player is in singleplayer)"));
                                return;
                            }
                        }

                        for (Map.Entry<String, List<String>> entry : splitTypes.entrySet()) {
                            for (String listIP : entry.getValue()) {
                                if (serverIP.endsWith(listIP)) {
                                    sender.addChatMessage(new ChatComponentText("§cA split type for that IP already exists! (" + entry.getKey() + ")"));
                                    return;
                                }
                            }
                        }

                        splitTypes.get(splitType).add(serverIP);
                        sender.addChatMessage(new ChatComponentText("§aSuccessfully added IP to config!"));

                        if ((currentServerIP != null) && (currentServerIP.equals(serverIP))) {
                            ServerListener.setSplitType(splitType);
                            sender.addChatMessage(new ChatComponentText("§aSet split type for current server to " + splitType + "!"));
                        }

                        configChanged = true;
                    } else {
                        sender.addChatMessage(new ChatComponentText("§cInvalid split type given!"));
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText(INVALID_COMMAND_USAGE));
                }

                break;
            default:
                sender.addChatMessage(new ChatComponentText(INVALID_COMMAND_USAGE));
        }

        if (configChanged) {
            configHandler.saveConfig();
            configHandler.loadConfig();
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "list", "add", "remove");
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                return getListOfStringsMatchingLastWord(args, "current");
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")) {
                return getListOfStringsMatchingLastWord(args, availableSplitTypes);
            }
        }

        return null;
    }
}
