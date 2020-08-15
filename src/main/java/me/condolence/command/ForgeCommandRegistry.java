package me.condolence.command;

import me.condolence.Debug;
import me.condolence.PlayerMentionAddon;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ForgeCommandRegistry {
    public static void registerMentionCommand() {
        MentionCommand command = PlayerMentionAddon.getMentionCommand();

        List<String> aliasesList = command.getAliases();

        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "mention";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "";
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                try {
                    command.processCommand(args);
                } catch (Exception e) {
                    sender.addChatMessage(new ChatComponentText("\u00a7cAn error occurred attempting to execute this command!"));
                    Debug.log("An error occurred attempting to execute the mention command!");
                    e.printStackTrace();
                }
            }

            @Override
            public boolean canCommandSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                if (!(sender instanceof EntityPlayer)) { return super.addTabCompletionOptions(sender, args, pos); }

                return command.getTabCompletionOptions(((EntityPlayer)sender).getGameProfile(), args);
            }

            @Override
            public List<String> getCommandAliases() {
                return aliasesList;
            }
        });
    }
}
