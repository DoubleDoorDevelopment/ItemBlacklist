package net.doubledoordev.itemblacklist.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Dries007
 */
public class GlobalBanList
{
    public static GlobalBanList instance;

    public static void init()
    {
        File file = Helper.getDataFile();
        if (file.exists())
        {
            try
            {
                instance = Helper.GSON.fromJson(FileUtils.readFileToString(file, "UTF-8"), GlobalBanList.class);
            }
            catch (Exception e)
            {
                throw new RuntimeException("There was an error loading your config file. To prevent damage, the server will be closed.");
            }
        }
        else
        {
            instance = new GlobalBanList();
            ItemBlacklist.logger.warn("No config file present.");
        }
    }

    private Multimap<Integer, BanList> dimesionMap = HashMultimap.create();
    private BanList global = new BanList("__GLOBAL__");

    public static boolean isBanned(int dimensionId, ItemStack item)
    {
        if (instance == null) throw new IllegalStateException("Ban list not initialized.");
        if (item == null || item.getItem() == null) return false;
        if (instance.global.isBanned(item)) return true;
        for (BanList banList : instance.dimesionMap.get(dimensionId)) if (banList.isBanned(item)) return true;
        return false;
    }

    public static void process(int dim, IInventory inventory)
    {
        process(dim, inventory, false);
    }

    public static void process(int dim, IInventory inventory, boolean unpackOnly)
    {
        final int size = inventory.getSizeInventory();
        for (int i = 0; i < size; i++)
        {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (itemStack == null) continue;
            boolean blocked = itemStack.getItem() == ItemBlacklisted.I;
            boolean banned = !unpackOnly && isBanned(dim, itemStack);
            if (blocked && !banned)
            {
                inventory.setInventorySlotContents(i, ItemBlacklisted.unpack(itemStack));
            }
            else if (banned && !blocked)
            {
                inventory.setInventorySlotContents(i, ItemBlacklisted.pack(itemStack));
            }
        }
    }

    public static ItemStack process(int dim, ItemStack itemStack)
    {
        if (itemStack == null) return null;

        boolean blocked = itemStack.getItem() == ItemBlacklisted.I;
        boolean banned = isBanned(dim, itemStack);

        if (blocked && !banned)
        {
            return ItemBlacklisted.unpack(itemStack);
        }
        else if (banned && !blocked)
        {
            return ItemBlacklisted.pack(itemStack);
        }

        return itemStack;
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
                if (list.global.dimension.equals(entry.getKey()))
                {
                    list.global = context.deserialize(entry.getValue(), BanList.class);
                    list.global.dimension = entry.getKey();
                }
                else
                {
                    BanList banList = context.deserialize(entry.getValue(), BanList.class);
                    banList.dimension = entry.getKey();
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
            root.add(src.global.dimension, context.serialize(src.global));
            for (BanList banList : src.dimesionMap.values())
            {
                if (!root.has(banList.dimension)) root.add(banList.dimension, context.serialize(banList));
            }
            return root;
        }
    }
}
