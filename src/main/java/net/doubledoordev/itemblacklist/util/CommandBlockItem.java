package net.doubledoordev.itemblacklist.util;

import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.data.BanList;
import net.doubledoordev.itemblacklist.data.BanListEntry;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * @author Dries007
 */
public class CommandBlockItem extends CommandBase
{
    public static final CommandBlockItem I = new CommandBlockItem();

    private CommandBlockItem() {}

    @Override
    public String getName()
    {
        return "blockitem";
    }

    @Override
    public List<String> getAliases()
    {
        return Arrays.asList("itemblacklist", "blacklist");
    }

    @Override
    public String getUsage(ICommandSender p_71518_1_)
    {
        return "Use '/blockitem help' for more info.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 0)
        {
            sender.sendMessage(new TextComponentString("Possible subcommands:").setStyle(new Style().setColor(GOLD)));
            sender.sendMessage(makeHelpText("reload", "Reloads the config file from disk."));
            sender.sendMessage(makeHelpText("pack [player]", "Lock banned items in targets inventory."));
            sender.sendMessage(makeHelpText("unpack [player]", "Unlock banned items in targets inventory."));
            sender.sendMessage(makeHelpText("list [dim|player]", "List banned items of all, player, or dim"));
            sender.sendMessage(makeHelpText("ban [dim list] [item[:*|meta]]", "Ban an item."));
            sender.sendMessage(makeHelpText("unban [dim list] [item[:*|meta]]", "Unban an item."));
            return;
        }
        String arg0 = args[0].toLowerCase();
        boolean unpack = false;
        switch (arg0)
        {
            default:
                throw new WrongUsageException("Unknown subcommand. Use '/blockitem' to get some help.");
            case "reload":
                GlobalBanList.init(server);
                // No break
                sender.sendMessage(new TextComponentString("Reloaded!").setStyle(new Style().setColor(GREEN)));
            case "list":
                list(server, sender, args);
                break;
            case "unpack":
                unpack = true;
            case "pack":
                EntityPlayer player = args.length > 1 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
                int count = GlobalBanList.process(player.dimension, player.inventory, unpack);
                sender.sendMessage(new TextComponentString((unpack ? "Unlocked " : "Locked ") + count + " items."));
                break;
            case "ban":
                try
                {
                    Pair<String, BanListEntry> toBan = parse(sender, args);
                    GlobalBanList.worldInstance.add(toBan.k, toBan.v);
                    sender.sendMessage(new TextComponentString("Banned " + toBan.v.toString() + " in " + toBan.k).setStyle(new Style().setColor(GREEN)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new WrongUsageException(e.getMessage());
                }
                break;
            case "unban":
                try
                {
                    Pair<String, BanListEntry> toBan = parse(sender, args);
                    if (GlobalBanList.worldInstance.remove(toBan.k, toBan.v))
                        sender.sendMessage(new TextComponentString("Unbanned " + toBan.v.toString() + " in " + toBan.k).setStyle(new Style().setColor(GREEN)));
                    else
                        sender.sendMessage(new TextComponentString("Can't unban " + toBan.v.toString() + " in " + toBan.k).setStyle(new Style().setColor(RED)));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new WrongUsageException(e.getMessage());
                }
                break;
        }
    }

    private Pair<String, BanListEntry> parse(ICommandSender sender, String[] args) throws PlayerNotFoundException, WrongUsageException
    {
        String dimensions = null;
        boolean wildcardOverride = false;
        int meta = OreDictionary.WILDCARD_VALUE;
        BanListEntry banListEntry = null;

        for (int i = 1; i < args.length; i++)
        {
            if (args[i].equals(GlobalBanList.GLOBAL_NAME))
            {
                dimensions = GlobalBanList.GLOBAL_NAME;
                continue;
            }
            try
            {
                Helper.parseDimIds(args[i]);
                if (dimensions != null)
                    throw new WrongUsageException("Double dimension specifiers: " + dimensions + " AND " + args[i]);
                dimensions = args[i];
                continue;
            }
            catch (Exception ignored) {}
            try
            {
                String[] split = args[i].split(":");
                if (split.length > 3) throw new WrongUsageException("Item name not valid.");
                meta = split.length == 3 ? parseInt(split[2]) : OreDictionary.WILDCARD_VALUE;
                if (banListEntry != null)
                    throw new WrongUsageException("Double item specifiers: " + banListEntry + " AND " + args[i]);
                banListEntry = new BanListEntry(split[0] + ":" + split[1], meta);
                continue;
            }
            catch (Exception ignored) {}
            if (args[i].equals("*"))
            {
                wildcardOverride = true;
                continue;
            }
            throw new IllegalArgumentException("Not a dimension specifier or valid item: " + args[i]);
        }
        // Default to current dimension and held item
        if (dimensions == null) dimensions = String.valueOf(getCommandSenderAsPlayer(sender).dimension);
        if (banListEntry == null)
        {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
            if (stack.isEmpty()) throw new WrongUsageException("No item specified and no item held.");
            if (wildcardOverride) meta = OreDictionary.WILDCARD_VALUE;
            banListEntry = new BanListEntry(stack.getItem().getRegistryName(), meta);
        }
        return new Pair<>(dimensions, banListEntry);
    }

    private void list(ICommandSender sender, HashSet<BanList> set)
    {
        for (BanList list : set)
        {
            sender.sendMessage(new TextComponentString("Dimension " + list.getDimension()).setStyle(new Style().setColor(AQUA)));
            for (BanListEntry entry : list.banListEntryMap.values())
            {
                sender.sendMessage(new TextComponentString(entry.toString()));
            }
        }
    }

    private void list(MinecraftServer server, ICommandSender sender, String[] args) throws WrongUsageException
    {
        HashSet<BanList> packSet = new HashSet<>();
        HashSet<BanList> worldSet = new HashSet<>();
        if (args.length == 1)
        {
            worldSet.addAll(GlobalBanList.worldInstance.dimesionMap.values());
            worldSet.add(GlobalBanList.worldInstance.getGlobal());

            if (GlobalBanList.packInstance != null)
            {
                packSet.addAll(GlobalBanList.packInstance.dimesionMap.values());
                packSet.add(GlobalBanList.packInstance.getGlobal());
            }
        }
        else
        {
            if (args[1].equalsIgnoreCase(GlobalBanList.GLOBAL_NAME))
                worldSet.add(GlobalBanList.worldInstance.getGlobal());
            else worldSet.addAll(GlobalBanList.worldInstance.dimesionMap.get(getDimension(server, sender, args[1])));

            if (GlobalBanList.packInstance != null)
            {
                if (args[1].equalsIgnoreCase(GlobalBanList.GLOBAL_NAME))
                    packSet.add(GlobalBanList.packInstance.getGlobal());
                else packSet.addAll(GlobalBanList.packInstance.dimesionMap.get(getDimension(server, sender, args[1])));
            }
        }
        if (worldSet.isEmpty())
            sender.sendMessage(new TextComponentString("No world banned items.").setStyle(new Style().setColor(YELLOW)));
        else
        {
            sender.sendMessage(new TextComponentString("World banned items:").setStyle(new Style().setColor(YELLOW)));
            list(sender, worldSet);
        }

        if (packSet.isEmpty())
            sender.sendMessage(new TextComponentString("No pack banned items. ").setStyle(new Style().setColor(YELLOW)).appendSibling(new TextComponentString("[unchangeable]").setStyle(new Style().setColor(RED))));
        else
        {
            sender.sendMessage(new TextComponentString("Pack banned items: ").setStyle(new Style().setColor(YELLOW)).appendSibling(new TextComponentString("[unchangeable]").setStyle(new Style().setColor(RED))));
            list(sender, packSet);
        }
    }

    private int getDimension(MinecraftServer server, ICommandSender sender, String arg) throws WrongUsageException
    {
        try
        {
            return Integer.parseInt(arg);
        }
        catch (Exception ignored)
        {

        }
        try
        {
            return getEntity(server, sender, arg).dimension;
        }
        catch (EntityNotFoundException ignored)
        {

        }
        catch (CommandException e)
        {
            e.printStackTrace();
        }
        throw new WrongUsageException("%s is not an entity or a number", arg);
    }

    public ITextComponent makeHelpText(String name, String text)
    {
        return new TextComponentString(name).setStyle(new Style().setColor(AQUA)).appendSibling(new TextComponentString(": " + text).setStyle(new Style().setColor(WHITE)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (isUsernameIndex(args, args.length))
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "reload", "pack", "unpack", "list", "ban", "unban");
        if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban"))
        {
            HashSet<String> set = new HashSet<>();
            set.add(GlobalBanList.GLOBAL_NAME);
            // Large item sets will cause the player to lose connection from the server, I don't think there is a way around this.
            for (ResourceLocation rl : Item.REGISTRY.getKeys())
                set.add(rl.toString());
            return getListOfStringsMatchingLastWord(args, set);
        }
        return super.getTabCompletions(server, sender, args, pos);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int arg)
    {
        if (args.length == 0) return false;
        switch (args[0].toLowerCase())
        {
            case "unpack":
            case "pack":
            case "list":
                return arg == 2;
        }
        return false;
    }

    public static class Pair<K, V>
    {
        public K k;
        public V v;

        public Pair(K k, V v)
        {
            this.k = k;
            this.v = v;
        }
    }
}
