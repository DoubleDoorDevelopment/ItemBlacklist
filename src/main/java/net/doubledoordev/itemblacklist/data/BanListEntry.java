package net.doubledoordev.itemblacklist.data;

import com.google.gson.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Type;

/**
 * @author Dries007
 */
public class BanListEntry
{
    private Item item;
    private int damage = 0;

    public BanListEntry(String name, int damage)
    {
        GameRegistry.UniqueIdentifier uid = new GameRegistry.UniqueIdentifier(name);
        this.item = GameRegistry.findItem(uid.modId, uid.name);
        this.damage = damage;
        if (item == null) throw new IllegalArgumentException(name + " isn't a valid item.");
    }

    public boolean isBanned(int damage)
    {
        return OreDictionary.WILDCARD_VALUE == this.damage || damage == this.damage;
    }

    public Item getItem()
    {
        return item;
    }

    public static class Json implements JsonSerializer<BanListEntry>, JsonDeserializer<BanListEntry>
    {
        @Override
        public BanListEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject object = json.getAsJsonObject();
            int damage = OreDictionary.WILDCARD_VALUE;
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
            jsonObject.addProperty("item", GameRegistry.findUniqueIdentifierFor(src.item).toString());
            if (src.damage == OreDictionary.WILDCARD_VALUE) jsonObject.addProperty("damage", "*");
            else jsonObject.addProperty("damage", src.damage);
            return jsonObject;
        }
    }
}
