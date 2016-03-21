package net.doubledoordev.itemblacklist.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.doubledoordev.itemblacklist.Helper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

/**
 * @author Dries007
 */
public class ItemBlacklisted extends Item
{
    public static final String NAME = "blacklisted";
    public static final ItemBlacklisted I = new ItemBlacklisted();

    private ItemBlacklisted()
    {
        setUnlocalizedName(NAME);
        setTextureName(Helper.MODID.concat(":").concat(NAME).toLowerCase());
        setMaxStackSize(1);
    }

    public static ItemStack pack(ItemStack in)
    {
        ItemStack out = new ItemStack(ItemBlacklisted.I);
        out.setTagInfo("item", in.writeToNBT(new NBTTagCompound()));
        return out;
    }

    public static boolean canUnpack(ItemStack in)
    {
        return in != null && in.hasTagCompound() && in.getTagCompound().hasKey("item");
    }

    /**
     * Avoids returning null as best as possible
     */
    public static ItemStack unpack(ItemStack in)
    {
        ItemStack out = null;
        try
        {
            out = ItemStack.loadItemStackFromNBT(in.getTagCompound().getCompoundTag("item"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return out == null ? in : out;
    }

    @Override
    public String getUnlocalizedName(ItemStack in)
    {
        if (!canUnpack(in)) return "_ERROR_";
        ItemStack unpack = unpack(in);
        if (unpack == in || unpack == null) return "_ERROR_";
        return unpack.getUnlocalizedName();
    }

    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (canUnpack(stack) && pass == 0)
        {
            ItemStack unpack = unpack(stack);
            if (unpack.getItemSpriteNumber() == this.getSpriteNumber()) return unpack.getIconIndex();
        }
        return itemIcon;
    }
}
