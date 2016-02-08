package net.doubledoordev.itemblacklist.util;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void blockPlaceEvent(BlockEvent.PlaceEvent event)
    {
        EntityPlayer player = event.player;
        if (player == null || event.itemInHand == null) return;
        if (!Helper.shouldCare(event.player)) return;
        if (GlobalBanList.isBanned(player.dimension, player.getHeldItem()))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} (Place Block. Banned Item in hand)", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z);
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
        else if (GlobalBanList.isBanned(player.dimension, new ItemStack(event.placedBlock, event.blockSnapshot.meta)))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} (Place Block. Banned Item placed.)", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z);
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        if (event.entityPlayer.getHeldItem() == null) return;
        EntityPlayer player = event.entityPlayer;
        if (!Helper.shouldCare(player)) return;
        if (GlobalBanList.isBanned(player.dimension, player.getHeldItem()))
        {
            player.addChatComponentMessage(new ChatComponentText(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {};{};{} ({})", player.getCommandSenderName(), player.getHeldItem().getDisplayName(), event.x, event.y, event.z, event.action);
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (!Helper.shouldCare(event.player)) return;
        GlobalBanList.process(event.toDim, event.player.inventory);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void itemTossEvent(ItemTossEvent event)
    {
        if (!Helper.shouldCare(event.player)) return;
        event.entityItem.setEntityItemStack(GlobalBanList.process(event.player.dimension, event.entityItem.getEntityItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityItemPickupEvent(EntityItemPickupEvent event)
    {
        if (!Helper.shouldCare(event.entityPlayer)) return;
        event.item.setEntityItemStack(GlobalBanList.process(event.entityPlayer.dimension, event.item.getEntityItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerOpenContainerEvent(PlayerOpenContainerEvent event)
    {
        EntityPlayer player = event.entityPlayer;
        if (!Helper.shouldCare(event.entityPlayer)) return;
        if (player.getEntityData().getInteger(Helper.MODID) != player.openContainer.hashCode()) // Crude is inventory changed
        {
            player.getEntityData().setInteger(Helper.MODID, player.openContainer.hashCode());
            GlobalBanList.process(player.worldObj.provider.dimensionId, player.openContainer, player);
        }
    }
}
