package net.doubledoordev.itemblacklist.util;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Dries007
 */
public class ItemBlacklisted extends Item
{
    public static final String NAME = "blacklisted";
    public static final ItemBlacklisted I = new ItemBlacklisted();

    private ItemBlacklisted()
    {
        setTranslationKey("blacklisted");
        setRegistryName("itemblacklist", NAME);
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
            out = new ItemStack(in.getTagCompound().getCompoundTag("item"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return out == null ? in : out;
    }

    @Override
    public String getTranslationKey(ItemStack in)
    {
        if (!canUnpack(in)) return "_ERROR_";
        ItemStack unpack = unpack(in);
        if (unpack == in || unpack == null) return "_ERROR_";
        return unpack.getTranslationKey();
    }

    @SideOnly(Side.CLIENT)
    public void initModel()
    {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation("itemblacklist:blacklisted", "inventory"));
    }
}
