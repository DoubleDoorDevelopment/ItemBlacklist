package net.doubledoordev.itemblacklist;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.doubledoordev.itemblacklist.client.ClientEventHandlers;
import net.doubledoordev.itemblacklist.client.Renderer;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.doubledoordev.itemblacklist.util.CommandBlockItem;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.doubledoordev.itemblacklist.util.ServerEventHandlers;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import static net.doubledoordev.itemblacklist.Helper.MODID;
import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@SuppressWarnings("DefaultAnnotationParam")
@Mod(modid = MODID, dependencies = "before:*", useMetadata = false)
public class ItemBlacklist
{
    @Mod.Instance
    public static ItemBlacklist instance;
    public static String message;
    public static Logger logger;
    public static boolean log;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        GameRegistry.registerItem(ItemBlacklisted.I, ItemBlacklisted.NAME);

        if (event.getSide().isClient())
        {
            MinecraftForge.EVENT_BUS.register(ClientEventHandlers.I);
            FMLCommonHandler.instance().bus().register(ClientEventHandlers.I);
            MinecraftForgeClient.registerItemRenderer(ItemBlacklisted.I, new Renderer());
        }

        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());

        message = configuration.getString("message", CATEGORY_GENERAL, "Now is not the time to use that. ~Prof. Oak", "The message you get when using an item that is banned.");
        log = configuration.getBoolean("log", CATEGORY_GENERAL, false, "Log every instance of any banned item used. (SPAM WARNING!)");

        if (configuration.hasChanged()) configuration.save();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBlockItem());
        GlobalBanList.init();

        MinecraftForge.EVENT_BUS.register(ServerEventHandlers.I);
        FMLCommonHandler.instance().bus().register(ServerEventHandlers.I);
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
        MinecraftForge.EVENT_BUS.unregister(ServerEventHandlers.I);
        FMLCommonHandler.instance().bus().register(ServerEventHandlers.I);
    }
}
