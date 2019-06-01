package org.ultramine.mods.privreg.blocks;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.ultramine.mods.privreg.PrivRegCreativeTab;
import org.ultramine.mods.privreg.RegionConfig;
import org.ultramine.mods.privreg.tiles.TileBlockRegion;

public class BlockRegion extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    private static int rednerID = RenderingRegistry.getNextAvailableRenderId();

    public BlockRegion() {
        super(Material.iron);
        setBlockUnbreakable();
        setResistance(6000000F);
        setStepSound(Block.soundTypeMetal);
        setCreativeTab(PrivRegCreativeTab.instance);
        setBlockName("um_privreg_pregion");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister r) {
        icons = new IIcon[3];
        icons[0] = r.registerIcon("privreg:machine_hull");
        icons[1] = r.registerIcon("privreg:region_controller_top");
        icons[2] = r.registerIcon("privreg:region_controller_side");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1)
            return icons[1];

        if (meta == 2 && side == 2 || meta == 3 && side == 5 || meta == 0 && side == 3 || meta == 1 && side == 4)
            return icons[2];

        return icons[0];
    }

    @Override
    public boolean isOpaqueCube() {
        return !RegionConfig.enableDragonModel;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return !RegionConfig.enableDragonModel;
    }

    @Override
    public int getRenderType() {
        return RegionConfig.enableDragonModel ? rednerID : super.getRenderType();
    }

    @Override
    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileBlockRegion();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack is) {
        world.setBlockMetadataWithNotify(x, y, z, MathHelper.floor_double((double) ((entity.rotationYaw * 4F) / 360F) + 2.5D) & 3, 3);
        if (!world.isRemote) {
            TileBlockRegion te = (TileBlockRegion) world.getTileEntity(x, y, z);
            if (te != null)
                te.onBlockPlacedBy(entity);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int data) {
        if (!world.isRemote) {
            TileBlockRegion te = (TileBlockRegion) world.getTileEntity(x, y, z);
            if (te != null)
                te.onBlockBreak();
        }
        super.breakBlock(world, x, y, z, block, data);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileBlockRegion te = (TileBlockRegion) world.getTileEntity(x, y, z);
        if (te != null) {
            if (world.isRemote) {
                return te.activateClient(player);
            } else/**/ if (FMLCommonHandler.instance().getSide().isServer()) {
                return te.activateServer(player);
            }
        }

        return true;
    }
}
