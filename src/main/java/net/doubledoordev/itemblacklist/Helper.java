package net.doubledoordev.itemblacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.doubledoordev.itemblacklist.data.BanList;
import net.doubledoordev.itemblacklist.data.BanListEntry;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.FakePlayer;

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

    public static int[] parseDimIds(String dimension)
    {
        try
        {
            String[] split = dimension.split(", ?");
            int[] ids = new int[split.length];
            for (int i = 0; i < split.length; i++) ids[i] = Integer.parseInt(split[i]);
            return ids;
        }
        catch (NumberFormatException ignored)
        {
        }
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
        catch (NumberFormatException ignored)
        {
        }
        throw new IllegalArgumentException(dimension + " isn't a valid dimension range.");
    }

    public static boolean shouldCare(EntityPlayer player)
    {
        return MinecraftServer.getServer().isSinglePlayer() || player instanceof FakePlayer || player.getGameProfile().getId() == null|| !MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
    }
}
