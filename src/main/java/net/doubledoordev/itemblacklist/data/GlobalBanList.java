package net.doubledoordev.itemblacklist.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;

import static net.doubledoordev.itemblacklist.Helper.MODID;

/**
 * @author Dries007
 */
public class GlobalBanList
{
    public static final String GLOBAL_NAME = "__GLOBAL__";

    public static GlobalBanList worldInstance;
    public static GlobalBanList packInstance;
    public final Multimap<Integer, BanList> dimesionMap = HashMultimap.create();
    private BanList global = new BanList(GLOBAL_NAME);
    private File file;

    public static void init(MinecraftServer server)
    {
        File file = new File(server.worldServers[0].getSaveHandler().getWorldDirectory(), MODID.concat(".json"));
        if (file.exists())
        {
            try
            {
                String string = FileUtils.readFileToString(file, "UTF-8");
                worldInstance = Helper.GSON.fromJson(string, GlobalBanList.class);
            }
            catch (Exception e)
            {
                throw new RuntimeException("There was an error loading your config file. To prevent damage, the server will be closed.", e);
            }
        }
        else
        {
            worldInstance = new GlobalBanList();
            ItemBlacklist.getLogger().warn("No config file present.");
        }
        worldInstance.file = file;

        file = new File(Loader.instance().getConfigDir(), MODID.concat(".json"));
        if (file.exists())
        {
            try
            {
                String string = FileUtils.readFileToString(file, "UTF-8");
                packInstance = Helper.GSON.fromJson(string, GlobalBanList.class);
                packInstance.file = file;
            }
            catch (Exception e)
            {
                throw new RuntimeException("There was an error loading the modpack config file. To prevent damage, the server will be closed.", e);
            }
        }
        else
        {
            packInstance = null;
        }
    }

    public void save()
    {
        try
        {
            if (!this.file.exists()) this.file.createNewFile();
            FileUtils.writeStringToFile(this.file, Helper.GSON.toJson(this), "UTF-8");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean isBanned(int dimensionId, ItemStack item)
    {
        if (worldInstance == null) throw new IllegalStateException("Ban list not initialized.");
        if (item == null || item.getItem() == null) return false;
        if (packInstance != null)
        {
            if (packInstance.global.isBanned(item)) return true;
            for (BanList banList : packInstance.dimesionMap.get(dimensionId)) if (banList.isBanned(item)) return true;
        }
        if (worldInstance.global.isBanned(item)) return true;
        for (BanList banList : worldInstance.dimesionMap.get(dimensionId)) if (banList.isBanned(item)) return true;
        return false;
    }

    public static int process(int dim, Container container, EntityPlayer player)
    {
        return process(dim, container, player, false);
    }

    public static int process(int dim, Container container, EntityPlayer player, boolean unpackOnly)
    {
        int count = 0;
        for (Slot slot : container.inventorySlots)
        {
            if (!slot.canTakeStack(player)) continue;
            ItemStack oldStack = slot.getStack();
            ItemStack newStack = process(dim, oldStack, unpackOnly);
            if (newStack == oldStack) continue;
            if (slot.isItemValid(oldStack))
            {
                slot.putStack(newStack);
            }
            else
            {
                slot.putStack(null);
                if (!player.inventory.addItemStackToInventory(newStack) || newStack.stackSize > 0)
                {
                    EntityItem entityitem = player.dropItem(newStack, false);
                    if (entityitem != null)
                    {
                        entityitem.setNoPickupDelay();
                        entityitem.setOwner(player.getName());
                    }
                }
            }
            count++;
        }
        return count;
    }

    public static int process(int dim, IInventory inventory)
    {
        return process(dim, inventory, false);
    }

    public static int process(int dim, IInventory inventory, boolean unpackOnly)
    {
        int count = 0;
        final int size = inventory.getSizeInventory();
        for (int i = 0; i < size; i++)
        {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack == null) continue;
            ItemStack processed = process(dim, itemStack, unpackOnly);
            if (processed != itemStack)
            {
                count++;
                inventory.setInventorySlotContents(i, processed);
            }
        }
        return count;
    }

    public static ItemStack process(int dim, ItemStack itemStack)
    {
        return process(dim, itemStack, false);
    }

    public static ItemStack process(int dim, ItemStack itemStack, boolean unpackOnly)
    {
        if (itemStack == null) return null;

        boolean packed = itemStack.getItem() == ItemBlacklisted.I && ItemBlacklisted.canUnpack(itemStack);
        ItemStack unpacked = packed ? ItemBlacklisted.unpack(itemStack) : itemStack;
        boolean banned = !unpackOnly && isBanned(dim, unpacked);

        if (packed && !banned) return unpacked;
        else if (banned && !packed) return ItemBlacklisted.pack(itemStack);

        return itemStack;
    }

    public BanList checkDuplicate(String dimensions)
    {
        if (dimensions.equals(GLOBAL_NAME))
        {
            return global;
        }
        else
        {
            BanList match = null;
            for (BanList banList : new HashSet<>(dimesionMap.values()))
            {
                if (banList.getDimension().equals(dimensions))
                {
                    if (match != null) throw new IllegalStateException("Duplicate banlist key. This is a serious issue. You should manually try to fix the json file!");
                    match = banList;
                }
            }
            return match;
        }
    }

    public void add(String dimensions, BanListEntry banListEntry)
    {
        BanList match = checkDuplicate(dimensions);
        if (match == null)
        {
            match = new BanList(dimensions);
            for (int i : match.getDimIds())
            {
                dimesionMap.put(i, match);
            }
        }
        if (match.banListEntryMap.containsEntry(banListEntry.getItem(), banListEntry)) throw new IllegalArgumentException("Duplicate ban list entry.");
        match.banListEntryMap.put(banListEntry.getItem(), banListEntry);
        save();
    }

    public boolean remove(String dimensions, BanListEntry banListEntry)
    {
        BanList match = checkDuplicate(dimensions);
        if (match == null) return false;
        if (!match.banListEntryMap.containsEntry(banListEntry.getItem(), banListEntry)) return false;
        match.banListEntryMap.remove(banListEntry.getItem(), banListEntry);
        save();
        return true;
    }

    public BanList getGlobal()
    {
        return global;
    }

    public static class Json implements JsonSerializer<GlobalBanList>, JsonDeserializer<GlobalBanList>
    {
        @Override
        public GlobalBanList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            GlobalBanList list = new GlobalBanList();
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet())
            {
                if (list.global.getDimension().equals(entry.getKey()))
                {
                    list.global = context.deserialize(entry.getValue(), BanList.class);
                    list.global.setDimension(entry.getKey());
                }
                else
                {
                    BanList banList = context.deserialize(entry.getValue(), BanList.class);
                    banList.setDimension(entry.getKey());
                    for (int i : banList.getDimIds())
                    {
                        list.dimesionMap.put(i, banList);
                    }
                }
            }
            return list;
        }

        @Override
        public JsonElement serialize(GlobalBanList src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject root = new JsonObject();
            root.add(src.global.getDimension(), context.serialize(src.global));
            for (BanList banList : src.dimesionMap.values())
            {
                if (!root.has(banList.getDimension())) root.add(banList.getDimension(), context.serialize(banList));
            }
            return root;
        }
    }
}
