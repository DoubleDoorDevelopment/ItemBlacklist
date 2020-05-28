package net.doubledoordev.itemblacklist;

import org.apache.logging.log4j.Logger;
import net.minecraft.command.CommandHandler;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.doubledoordev.itemblacklist.client.ClientEventHandlers;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.doubledoordev.itemblacklist.util.CommandBlockItem;
import net.doubledoordev.itemblacklist.util.CommandUnpack;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.doubledoordev.itemblacklist.util.ServerEventHandlers;

import static net.doubledoordev.itemblacklist.Helper.MODID;
import static net.doubledoordev.itemblacklist.Helper.MOD_GUI_FACTORY;
import static net.minecraftforge.common.ForgeVersion.MOD_ID;
import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@SuppressWarnings("DefaultAnnotationParam")
@Mod(modid = MODID, dependencies = "required:forge@[14.23.5.2779,);before:*", useMetadata = false, guiFactory = MOD_GUI_FACTORY)
public class ItemBlacklist
{
    @SidedProxy(clientSide = "net.doubledoordev.itemblacklist.client.Proxy", serverSide = "net.doubledoordev.itemblacklist.ItemBlacklist")
    public static ItemBlacklist proxy;

    @Mod.Instance
    public static ItemBlacklist instance;
    public static String message;
    public static boolean log;
    public boolean containerban;

    private boolean unpack4all;
    private Configuration configuration;
    private Logger logger;
    private boolean pastStart;

    public static Configuration getConfig()
    {
        return instance.configuration;
    }

    public static Logger getLogger()
    {
        return instance.logger;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        if (event.getSide().isClient()) MinecraftForge.EVENT_BUS.register(ClientEventHandlers.I);

        configuration = new Configuration(event.getSuggestedConfigurationFile());
        updateConfig();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        if (event.getSide().isClient()) ClientEventHandlers.init();
    }

    private void updateConfig()
    {
        message = configuration.getString("message", CATEGORY_GENERAL, "Now is not the time to use that. ~Prof. Oak", "The message you get when using an item that is banned.");
        log = configuration.getBoolean("log", CATEGORY_GENERAL, false, "Log every instance of any banned item used. (SPAM WARNING!)");
        unpack4all = configuration.getBoolean("unpack4all", CATEGORY_GENERAL, true, "Let everyone unpack items by using the 'unpack' command. So items can be used in crafting.");
        containerban = configuration.getBoolean("containerban", CATEGORY_GENERAL, false, "Got a really stubborn item to ban? Enable this! Items will be banned whenever an inventory closes.");


        if (configuration.hasChanged()) configuration.save();

        if (pastStart)
        {
            CommandHandler ch = (CommandHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
            if (unpack4all) ch.registerCommand(CommandUnpack.I);
            else
            {
                ch.getCommands().remove(CommandUnpack.I.getName());
                for (String s : CommandUnpack.I.getAliases()) ch.getCommands().remove(s);
            }
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(CommandBlockItem.I);
        if (unpack4all) event.registerServerCommand(CommandUnpack.I);
        GlobalBanList.init(event.getServer());

        MinecraftForge.EVENT_BUS.register(ServerEventHandlers.I);

        pastStart = true;
    }

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event)
    {
        MinecraftForge.EVENT_BUS.unregister(ServerEventHandlers.I);
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (event.getModID().equals(MODID)) updateConfig();
    }

    @GameRegistry.ObjectHolder(MOD_ID)
    public static class Items
    {
        @GameRegistry.ObjectHolder("itemblacklisted")
        public static ItemBlacklisted itemItemBlacklisted;
    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler
    {
        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event)
        {
            event.getRegistry().register(ItemBlacklisted.I);
        }
    }
}
