package net.doubledoordev.itemblacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.doubledoordev.itemblacklist.data.BanList;
import net.doubledoordev.itemblacklist.data.BanListEntry;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

import java.io.File;

/**
 * @author Dries007
 */
public class Helper
{
    public static final String MODID = "ItemBlacklist";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeHierarchyAdapter(BanList.class, new BanList.Json())
            .registerTypeHierarchyAdapter(BanListEntry.class, new BanListEntry.Json())
            .registerTypeHierarchyAdapter(GlobalBanList.class, new GlobalBanList.Json())
            .create();

    private Helper()
    {
    }

    public static File getDataFile()
    {
        return new File(MinecraftServer.getServer().worldServers[0].getSaveHandler().getWorldDirectory(), MODID.concat(".json"));
    }

}
