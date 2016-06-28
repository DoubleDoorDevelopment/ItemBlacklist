package net.doubledoordev.itemblacklist.util;

import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import java.util.List;

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
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
    {
        return p_71519_1_ instanceof EntityPlayer;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Use '/unpack' to remove the lock from any banned items in your inventory.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        int count = GlobalBanList.process(player.dimension, player.inventory, true);
        sender.addChatMessage(new ChatComponentText("Unlocked " + count + " items."));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int arg)
    {
        return false;
    }
}
