package net.doubledoordev.itemblacklist.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import net.doubledoordev.itemblacklist.Helper;
import net.doubledoordev.itemblacklist.ItemBlacklist;
import net.doubledoordev.itemblacklist.data.GlobalBanList;

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
        ItemStack itemInMainHand = player.getHeldItemMainhand();
        ItemStack itemInOffHand = player.getHeldItemOffhand();
        if (itemInMainHand.isEmpty() || itemInOffHand.isEmpty()) return;
        if (!Helper.shouldCare(event.getPlayer())) return;
        if (GlobalBanList.isBanned(player.dimension, itemInMainHand) || GlobalBanList.isBanned(player.dimension, itemInOffHand))
        {
            player.sendStatusMessage(new TextComponentString(ItemBlacklist.message), true);
            if (ItemBlacklist.log)
                ItemBlacklist.getLogger().info("{} tried to use {} or {} at {} (Place Block. Banned Item in hand)", player.getName(), itemInMainHand.getDisplayName(), itemInOffHand.getDisplayName(), event.getPos());
            if (event.isCancelable())
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
                ItemStack stack = new ItemStack(item, 1, block.damageDropped(blockState));
                if (!GlobalBanList.isBanned(player.dimension, stack)) continue;
                player.sendStatusMessage(new TextComponentString(ItemBlacklist.message), true);
                if (ItemBlacklist.log)
                    ItemBlacklist.getLogger().info("{} tried to use {} or {} at {} (Place Block. Banned Item placed)", player.getName(), itemInMainHand.getDisplayName(), itemInOffHand.getDisplayName(), event.getPos());
                if (event.isCancelable())
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
        ItemStack itemInMainHand = player.getHeldItemMainhand();
        ItemStack itemInOffHand = player.getHeldItemOffhand();
        if (itemInMainHand.isEmpty() || itemInOffHand.isEmpty()) return;
        if (!Helper.shouldCare(player)) return;
        IBlockState blockState = event.getBlockSnapshot().getCurrentBlock();
        Block block = blockState.getBlock();
        Item item = Item.getItemFromBlock(block);
        ItemStack stack = new ItemStack(item, 1, block.damageDropped(blockState));
        if (GlobalBanList.isBanned(player.dimension, itemInMainHand) || GlobalBanList.isBanned(player.dimension, itemInOffHand))
        {
            player.sendStatusMessage(new TextComponentString(ItemBlacklist.message), true);
            if (ItemBlacklist.log)
                ItemBlacklist.getLogger().info("{} tried to use {} or {} at {} (Place Block. Banned Item in hand)", player.getName(), itemInMainHand.getDisplayName(), itemInOffHand.getDisplayName(), event.getPos());
            if (event.isCancelable())
                event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
        else if (GlobalBanList.isBanned(player.dimension, stack))
        {
            player.sendStatusMessage(new TextComponentString(ItemBlacklist.message), true);
            if (ItemBlacklist.log)
                ItemBlacklist.getLogger().info("{} tried to use {} or {} at {} (Place Block. Banned Item placed)", player.getName(), itemInMainHand.getDisplayName(), itemInOffHand.getDisplayName(), event.getPos());
            if (event.isCancelable())
                event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerInteractEvent(PlayerInteractEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !Helper.shouldCare(player)) return;
        if (GlobalBanList.isBanned(player.dimension, stack))
        {
            player.sendStatusMessage(new TextComponentString(ItemBlacklist.message), true);
            if (ItemBlacklist.log)
                ItemBlacklist.getLogger().info("{} tried to use {} at {} ({})", player.getName(), stack.getDisplayName(), event.getPos(), event.getFace());
            if (event.isCancelable())
                event.setCanceled(true);
            GlobalBanList.process(player.dimension, player.inventory);
            ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void changeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        if (!Helper.shouldCare(event.player)) return;
        {
            GlobalBanList.process(event.toDim, event.player.inventory);
            ((EntityPlayerMP) event.player).sendContainerToPlayer(event.player.inventoryContainer);
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void itemTossEvent(ItemTossEvent event)
    {
        EntityPlayer player = event.getPlayer();
        if (!Helper.shouldCare(player)) return;
        event.getEntityItem().setItem(GlobalBanList.process(player.dimension, event.getEntityItem().getItem()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityItemPickupEvent(EntityItemPickupEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        if (!Helper.shouldCare(player)) return;
        event.getItem().setItem(GlobalBanList.process(player.dimension, event.getItem().getItem()));
    }
//      TODO: Find a fix for this that works
//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void playerOpenContainerEvent(PlayerOpenContainerEvent event)
//    {
//        EntityPlayer player = event.getEntityPlayer();
//        if (!Helper.shouldCare(player)) return;
//        if (player.getEntityData().getInteger(Helper.MODID) != player.openContainer.hashCode()) // Crude is inventory changed
//        {
//            player.getEntityData().setInteger(Helper.MODID, player.openContainer.hashCode());
//            GlobalBanList.process(player.dimension, player.openContainer, player);
//        }
//    }
}
