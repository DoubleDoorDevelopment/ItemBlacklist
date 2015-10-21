package net.doubledoordev.itemblacklist.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.itemblacklist.util.ItemBlacklisted;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/**
 * @author Dries007
 */
public class ClientEventHandlers
{
    public static final ClientEventHandlers I = new ClientEventHandlers();

    private ClientEventHandlers()
    {

    }

    @SubscribeEvent
    public void itemTooltipEvent(ItemTooltipEvent event)
    {
        if (event.itemStack.getItem() == ItemBlacklisted.I)
        {
            event.toolTip.add(1, EnumChatFormatting.RED + "Banned Item");
        }
    }
}
