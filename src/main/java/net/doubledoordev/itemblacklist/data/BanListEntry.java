package net.doubledoordev.itemblacklist.data;

import com.google.gson.*;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;

import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

/**
 * @author Dries007
 */
public class BanListEntry
{
    private Item item;
    private int damage = 0;

    public BanListEntry(ResourceLocation uid, int damage)
    {
        this.item = Item.REGISTRY.getObject(uid);
        if (this.item == ItemBlacklisted.I) throw new IllegalArgumentException("You can't ban the banning item.");
        this.damage = damage;
        if (item == null) throw new IllegalArgumentException(uid.toString() + " isn't a valid item.");
    }

    public BanListEntry(String name, int damage)
    {
        this(new ResourceLocation(name), damage);
    }

    public boolean isBanned(int damage)
    {
        return WILDCARD_VALUE == this.damage || damage == this.damage;
    }

    public Item getItem()
    {
        return item;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BanListEntry that = (BanListEntry) o;

        return damage == that.damage && item.equals(that.item);
    }

    @Override
    public int hashCode()
    {
        int result = item.hashCode();
        result = 31 * result + damage;
        return result;
    }

    @Override
    public String toString()
    {
        return item.getRegistryName().toString() + ' ' + (damage == WILDCARD_VALUE ? "*" : String.valueOf(damage));
    }

    public static class Json implements JsonSerializer<BanListEntry>, JsonDeserializer<BanListEntry>
    {
        @Override
        public BanListEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = json.getAsJsonObject();
            int damage = WILDCARD_VALUE;
            if (object.has("damage"))
            {
                String damageString = object.get("damage").getAsString();
                if (!damageString.equals("*")) damage = Integer.parseInt(damageString);
            }
            return new BanListEntry(object.get("item").getAsString(), damage);
        }

        @Override
        public JsonElement serialize(BanListEntry src, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("item", src.item.getRegistryName().toString());
            if (src.damage == WILDCARD_VALUE) jsonObject.addProperty("damage", "*");
            else jsonObject.addProperty("damage", src.damage);
            return jsonObject;
        }
    }
}
