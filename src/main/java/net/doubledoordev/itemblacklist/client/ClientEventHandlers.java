package net.doubledoordev.itemblacklist.client;

import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * @author Dries007
 */
public class ClientEventHandlers
{
    public static final ClientEventHandlers I = new ClientEventHandlers();

    private static final Field guiLeftField = ReflectionHelper.findField(GuiContainer.class, "guiLeft", "field_147003_i");
    private static final Field guiTopField = ReflectionHelper.findField(GuiContainer.class, "guiTop", "field_147009_r");

    private ClientEventHandlers()
    {

    }

    public static void init()
    {
        final RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        renderItem.getItemModelMesher().register(ItemBlacklisted.I, 0, new ModelResourceLocation(ItemBlacklisted.I.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public void drawScreenEvent(final GuiScreenEvent.DrawScreenEvent.Post event) throws IllegalAccessException
    {
        if (event.getGui() instanceof GuiContainer)
        {
            GuiContainer container = (GuiContainer) event.getGui();
            if (container.inventorySlots == null) return;

            final int guiLeft = guiLeftField.getInt(container);
            final int guiTop = guiTopField.getInt(container);

            for (Slot slot : container.inventorySlots.inventorySlots)
            {
                if (slot == null) continue;
                ItemStack stack = slot.getStack();
                if (stack.isEmpty() || stack.getItem() != ItemBlacklisted.I) continue;
                if (!ItemBlacklisted.canUnpack(stack)) return;
                ItemStack unpacked = ItemBlacklisted.unpack(stack);

                GlStateManager.pushMatrix();
                GlStateManager.translate(guiLeft, guiTop, 1);
                GlStateManager.translate(slot.xPos + 8, slot.yPos + 8, 1);
                GlStateManager.scale(15F, -15F, 10F);
                Minecraft.getMinecraft().getRenderItem().renderItem(unpacked, ItemCameraTransforms.TransformType.FIXED);
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void itemTooltipEvent(final ItemTooltipEvent event)
    {
        if (event.getItemStack().getItem() == ItemBlacklisted.I)
        {
            event.getToolTip().add(1, TextFormatting.RED + "Banned Item");
        }
    }

    @SubscribeEvent
    public void renderItemInFrameEvent(final RenderItemInFrameEvent event)
    {
        ItemStack stack = event.getItem();
        if (stack.isEmpty() || stack.getItem() != ItemBlacklisted.I) return;
        if (!ItemBlacklisted.canUnpack(stack)) return;
        ItemStack unpacked = ItemBlacklisted.unpack(stack);

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.pushAttrib();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate(0.001, -0.001, 0.001);
        Minecraft.getMinecraft().getRenderItem().renderItem(unpacked, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderGUI(final RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR)
        {
            final Minecraft mc = Minecraft.getMinecraft();
            final ScaledResolution res = event.getResolution();

            if (!mc.player.isSpectator())
            {
                for (int slot = 0; slot < 9; ++slot)
                {
                    final ItemStack stack = mc.player.inventory.mainInventory.get(slot);
                    if (!stack.isEmpty() && stack.getItem() == ItemBlacklisted.I)
                    {
                        final int x = res.getScaledWidth() / 2 - 90 + slot * 20 + 2;
                        final int y = res.getScaledHeight() - 16 - 3;

                        if (!ItemBlacklisted.canUnpack(stack)) continue;
                        ItemStack unpacked = ItemBlacklisted.unpack(stack);
                        mc.getRenderItem().renderItemIntoGUI(unpacked, x, y);
                    }
                }
            }
        }
    }
}
