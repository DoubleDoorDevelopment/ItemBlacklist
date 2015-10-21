package net.doubledoordev.itemblacklist.util;

import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import java.util.List;

/**
 * @author Dries007
 */
public class CommandBlockItem extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "blockitem";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Use '/blockitem help' for more info.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if (args.length == 0)
        {
            return;
        }
        if (args[0].equalsIgnoreCase("reload"))
        {
            GlobalBanList.init();
        }
        else if (args[0].equalsIgnoreCase("unpack"))
        {
            EntityPlayer player = args.length < 2 ? getCommandSenderAsPlayer(sender) : getPlayer(sender, args[1]);
            GlobalBanList.process(player.dimension, player.inventory, true);
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "reload", "unpack");
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int arg)
    {
        if (args.length == 0) return false;
        if (args[0].equalsIgnoreCase("unpack")) return arg == 1;
        return false;
    }
}
