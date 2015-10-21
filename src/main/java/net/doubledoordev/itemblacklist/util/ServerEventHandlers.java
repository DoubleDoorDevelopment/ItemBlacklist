package net.doubledoordev.itemblacklist.util;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * @author Dries007
 */
public class ServerEventHandlers
{
    public static final ServerEventHandlers I = new ServerEventHandlers();

    private ServerEventHandlers()
    {

    }

    @SubscribeEvent
    public void blockPlaceEvent(BlockEvent.PlaceEvent event)
    {
        EntityPlayer player = event.player;
        if (player == null || event.itemInHand == null) return;
        if (GlobalBanList.isBanned(player.dimension, player.getHeldItem()))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} (Place Block. Banned Item in hand)", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z);
            if (!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()))
            {
                event.setCanceled(true);
                GlobalBanList.process(player.dimension, player.inventory);
            }
        }
        else if (GlobalBanList.isBanned(player.dimension, new ItemStack(event.placedBlock, event.blockSnapshot.meta)))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} (Place Block. Banned Item placed.)", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z);
            if (!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()))
            {
                event.setCanceled(true);
                GlobalBanList.process(player.dimension, player.inventory);
            }
        }
    }

    @SubscribeEvent
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        if (event.entityPlayer.getHeldItem() == null) return;
        EntityPlayer player = event.entityPlayer;
        if (GlobalBanList.isBanned(player.dimension, player.getHeldItem()))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} ({})", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z, event.action);
            if (!MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile()))
            {
                event.setCanceled(true);
                GlobalBanList.process(player.dimension, player.inventory);
            }
        }
    }

    @SubscribeEvent
    public void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (MinecraftServer.getServer().getConfigurationManager().func_152596_g(event.player.getGameProfile())) return;
        GlobalBanList.process(event.toDim, event.player.inventory);
    }

    @SubscribeEvent
    public void itemPickupEvent(PlayerEvent.ItemPickupEvent event)
    {
        if (MinecraftServer.getServer().getConfigurationManager().func_152596_g(event.player.getGameProfile())) return;
        event.pickedUp.setEntityItemStack(GlobalBanList.process(event.player.dimension, event.pickedUp.getEntityItem()));
    }

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event)
    {
        if (event.itemStack.getItem() == ItemBlacklisted.I)
        {
            event.toolTip.add("Banned Item");
        }
    }
}
