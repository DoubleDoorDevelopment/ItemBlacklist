package net.doubledoordev.itemblacklist.util;

import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.data.GlobalBanList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

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
    public void multiPlaceEvent(BlockEvent.MultiPlaceEvent event)
    {
        EntityPlayer player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
        if (player == null || itemInHand == null) return;
        if (!Helper.shouldCare(event.getPlayer())) return;
        if (GlobalBanList.isBanned(player.dimension, itemInHand))
        {
            player.addChatComponentMessage(new TextComponentString(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {} (Place Block. Banned Item in hand)", player.getName(), itemInHand.getDisplayName(), event.getPos());
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
        else
        {
            for (BlockSnapshot blockSnapshot : event.getReplacedBlockSnapshots())
            {
                IBlockState blockState = blockSnapshot.getCurrentBlock();
                Block block = blockState.getBlock();
                Item item = Item.getItemFromBlock(block);
                if (item == null) continue;
                ItemStack stack = new ItemStack(item, 1, block.damageDropped(blockState));
                if (!GlobalBanList.isBanned(player.dimension, stack)) continue;
                player.addChatComponentMessage(new TextComponentString(ItemBlacklist.message));
                if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {} (Place Block. Banned Item placed.)", player.getName(), itemInHand.getDisplayName(), event.getPos());
                event.setCanceled(true);
                GlobalBanList.process(player.dimension, player.inventory);
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void blockPlaceEvent(BlockEvent.PlaceEvent event)
    {
        EntityPlayer player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();
        if (player == null || itemInHand == null) return;
        if (!Helper.shouldCare(player)) return;
        IBlockState blockState = event.getBlockSnapshot().getCurrentBlock();
        Block block = blockState.getBlock();
        Item item = Item.getItemFromBlock(block);
        ItemStack stack = item == null ? null : new ItemStack(item, 1, block.damageDropped(blockState));
        if (GlobalBanList.isBanned(player.dimension, itemInHand))
        {
            player.addChatComponentMessage(new TextComponentString(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {} (Place Block. Banned Item in hand)", player.getName(), itemInHand.getDisplayName(), event.getPos());
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
        else if (GlobalBanList.isBanned(player.dimension, stack))
        {
            player.addChatComponentMessage(new TextComponentString(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {} (Place Block. Banned Item placed.)", player.getName(), itemInHand.getDisplayName(), event.getPos());
            event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (stack == null || !Helper.shouldCare(player)) return;
        if (GlobalBanList.isBanned(player.dimension, stack))
        {
            player.addChatComponentMessage(new TextComponentString(ItemBlacklist.message));
            if (ItemBlacklist.log) ItemBlacklist.logger.info("{} tried to use {} at {} ({})", player.getName(), stack.getDisplayName(), event.getPos(), event.getFace());
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
        EntityPlayer player = event.getPlayer();
        if (!Helper.shouldCare(player)) return;
        event.getEntityItem().setEntityItemStack(GlobalBanList.process(player.dimension, event.getEntityItem().getEntityItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityItemPickupEvent(EntityItemPickupEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if (!Helper.shouldCare(player)) return;
        event.getItem().setEntityItemStack(GlobalBanList.process(player.dimension, event.getItem().getEntityItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerOpenContainerEvent(PlayerOpenContainerEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if (!Helper.shouldCare(player)) return;
        if (player.getEntityData().getInteger(Helper.MODID) != player.openContainer.hashCode()) // Crude is inventory changed
        {
            player.getEntityData().setInteger(Helper.MODID, player.openContainer.hashCode());
            GlobalBanList.process(player.dimension, player.openContainer, player);
        }
    }
}
