package net.doubledoordev.itemblacklist.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author Dries007
 */
public class BanList
{
    public String dimension;
    private final Multimap<Item, BanListEntry> banListEntryMap = HashMultimap.create();

    public BanList(String dimension)
    {
        this.dimension = dimension;
    }

    public BanList()
    {

    }

    public boolean isBanned(ItemStack itemStack)
    {
        for (BanListEntry banListEntry : banListEntryMap.get(itemStack.getItem())) if (banListEntry.isBanned(itemStack.getItemDamage())) return true;
        return false;
    }

    public int[] getDimIds()
    {
        try
        {
            String[] split = dimension.split(", ?");
            int[] ids = new int[split.length];
            for (int i = 0; i < split.length; i++) ids[i] = Integer.parseInt(split[i]);
            return ids;
        }
        catch (NumberFormatException ignored) {}
        try
        {
            String[] split = dimension.split(" ?# ?", 2);
            int start = Integer.parseInt(split[0]);
            int end = Integer.parseInt(split[1]);
            if (end < start) throw new IllegalArgumentException(end + "  < " + start);

            int[] ids = new int[end - start];
            for (int i = 0; i < ids.length; i++) ids[i] = start + i;
            return ids;
        }
        catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException(dimension + " isn't a valid dimension range.");
    }

    public static class Json implements JsonSerializer<BanList>, JsonDeserializer<BanList>
    {
        @Override
        public BanList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            BanList banList = new BanList();
            for (JsonElement element : json.getAsJsonArray())
            {
                BanListEntry entry = context.deserialize(element, BanListEntry.class);
                banList.banListEntryMap.put(entry.getItem(), entry);
            }
            return banList;
        }

        @Override
        public JsonElement serialize(BanList src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonArray array = new JsonArray();
            for (BanListEntry banList : src.banListEntryMap.values())
            {
                array.add(context.serialize(banList));
            }
            return array;
        }
    }
}
