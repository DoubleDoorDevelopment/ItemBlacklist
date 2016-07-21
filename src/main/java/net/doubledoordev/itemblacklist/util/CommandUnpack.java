package net.doubledoordev.itemblacklist.util;

import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

/**
 * @author Dries007
 */
public class CommandUnpack extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "unpack";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return sender instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Use '/unpack' to remove the lock from any banned items in your inventory.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        int count = GlobalBanList.process(player.dimension, player.inventory, true);
        sender.addChatMessage(new TextComponentString("Unlocked " + count + " items."));
    }
}
