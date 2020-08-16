package me.condolence.command;

import com.mojang.authlib.GameProfile;
import me.condolence.PlayerMentionAddon;
import me.condolence.config.ConfigHandler;
import me.condolence.text.SplitType;

import java.util.*;

public class MentionCommand {
    final String commandUsage;
    final ConfigHandler configHandler = PlayerMentionAddon.getConfigHandler();
    final CommandSender commandSender;

    public MentionCommand() {
        // Add all split type options to be used in the command usage string
        // Over-engineered? Probably. I'm able to add more split types in the future without having to change this, at least.
        StringBuilder splitTypesBuilder = new StringBuilder("Split Options:\n§9");
        for (SplitType type : SplitType.values()) {
            splitTypesBuilder
                    .append("   ")
                    .append(type.getTypeName())
                    .append(" (")
                    .append(type.getSplitSymbol())
                    .append(")\n");
        }

        // Build command usage string
        commandUsage = "§b§lMention Addon Usage:§r\n" +
                "   §r§9/mention add <split_type> <ip>\n" +
                "   /mention remove <ip>\n" +
                "§3The IP argument should be the IP of a server\n\n" +
                splitTypesBuilder.toString();

        commandSender = new CommandSender();
    }

    public List<String> getAliases() {
        return Arrays.asList("mention", "mention_addon");
    }

    public void processCommand(String[] args) {
        final String INVALID_COMMAND_USAGE = "§cInvalid command usage! Use /mention to list all commands.";
        final HashMap<String, List<String>> splitTypes = configHandler.getMainConfig().getSplitTypes();

        boolean configChanged = false;

        switch(args.length) {
            case 0:
                commandSender.sendMessage(commandUsage);
                
                break;
            case 1:
                if (args[0].equalsIgnoreCase("list")) {
                    StringBuilder listStringBuilder = new StringBuilder("§b§lSplit Types:§r\n");

                    for (Map.Entry<String, List<String>> entry : splitTypes.entrySet()) {
                        listStringBuilder
                                .append("§9")
                                .append(entry.getKey())
                                .append(": §3")
                                .append(String.join(", ", entry.getValue()))
                                .append("§r\n");
                    }

                    commandSender.sendMessage(listStringBuilder.toString());
                } else {
                    commandSender.sendMessage(INVALID_COMMAND_USAGE);
                }

                break;
            case 2:
                if (args[0].equalsIgnoreCase("remove")) {
                    for (SplitType type : SplitType.values()) {
                        for (String defaultIP : type.getDefaultSupportedIPs()) {
                            if (args[1].equalsIgnoreCase(defaultIP)) {
                                commandSender.sendMessage("§cYou cannot remove default config IPs!");
                                return;
                            }
                        }
                    }

                    boolean removed = false;
                    for (Map.Entry<String, List<String>> entrySet : splitTypes.entrySet()) {
                        Iterator<String> iterator = entrySet.getValue().iterator();

                        while (iterator.hasNext()) {
                            if (args[1].equalsIgnoreCase(iterator.next().toLowerCase())) {
                                iterator.remove();
                                removed = true;
                                break;
                            }
                        }
                    }

                    commandSender.sendMessage(removed ? "§aSuccessfully removed IP from config!" : "§cCould not find an IP with that name to remove!");
                    configChanged = removed;
                } else {
                    commandSender.sendMessage(INVALID_COMMAND_USAGE);
                }

                break;
            case 3:
                if (args[0].equalsIgnoreCase("add")) {
                    final String splitType = args[1].toLowerCase();
                    final String serverIP = args[2].toLowerCase();

                    if (splitTypes.get(splitType) != null) {
                        if (serverIP.isEmpty()) { commandSender.sendMessage("§cInvalid IP given!"); return; }

                        for (Map.Entry<String, List<String>> entry : splitTypes.entrySet()) {
                            for (String listIP : entry.getValue()) {
                                if (serverIP.endsWith(listIP)) {
                                    commandSender.sendMessage("§cA split type for that IP already exists! (" + entry.getKey() + ")");
                                    return;
                                }
                            }
                        }

                        splitTypes.get(splitType).add(serverIP);
                        commandSender.sendMessage("§aSuccessfully added IP to config!");
                        configChanged = true;
                    } else {
                        commandSender.sendMessage("§cInvalid split type given!");
                    }
                } else {
                    commandSender.sendMessage(INVALID_COMMAND_USAGE);
                }

                break;
            default:
                commandSender.sendMessage(INVALID_COMMAND_USAGE);
        }

        if (configChanged) {
            configHandler.saveConfig();
            configHandler.loadConfig();
        }
    }

    public List<String> getTabCompletionOptions(GameProfile sender, String[] args) {
        if (args.length == 1) {
            return TabComplete.getMatching(args, Arrays.asList("add", "list", "remove"));
        }

        return new ArrayList<>();
    }
}
