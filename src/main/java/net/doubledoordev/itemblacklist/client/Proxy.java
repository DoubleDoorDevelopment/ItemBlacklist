package net.doubledoordev.itemblacklist.client;

import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
public class Proxy extends ItemBlacklist
{
    @GameRegistry.ObjectHolder("itemblacklist:blacklisted")
    public static ItemBlacklisted itemItemBlacklisted;

    @SideOnly(Side.CLIENT)
    public static void initModels()
    {
        itemItemBlacklisted.initModel();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        initModels();
    }
}
