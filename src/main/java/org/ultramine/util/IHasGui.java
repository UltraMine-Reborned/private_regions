package org.ultramine.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.ultramine.gui.IGui;

public interface IHasGui {
    @SideOnly(Side.CLIENT)
    IGui getGui(int id, EntityPlayer player);

    Container getGuiContainer(int id, EntityPlayer player);
}
