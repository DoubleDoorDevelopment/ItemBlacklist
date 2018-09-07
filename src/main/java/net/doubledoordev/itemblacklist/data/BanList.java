package net.doubledoordev.itemblacklist.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import net.doubledoordev.itemblacklist.Helper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Type;

/**
 * @author Dries007
 */
public class BanList
{
    public final Multimap<Item, BanListEntry> banListEntryMap = HashMultimap.create();
    private String dimension;

    public BanList(String dimension)
    {
        setDimension(dimension);
        if (!GlobalBanList.GLOBAL_NAME.equals(dimension)) getDimIds(); // Sanity check
    }

    private BanList()
    {

    }

    public boolean isBanned(ItemStack itemStack)
    {
        for (BanListEntry banListEntry : banListEntryMap.get(itemStack.getItem()))
            if (banListEntry.isBanned(itemStack.getItemDamage())) return true;
        return false;
    }

    public int[] getDimIds()
    {
        return Helper.parseDimIds(dimension);
    }

    public String getDimension()
    {
        return dimension;
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension.replaceAll(" ", "");
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
                if (banList.banListEntryMap.containsValue(entry))
                    throw new IllegalArgumentException("Duplicate ban list entry.");
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
