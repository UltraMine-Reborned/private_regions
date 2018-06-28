package org.ultramine.mods.privreg.integration;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.api.EventWarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.ultramine.mods.privreg.PrivateRegions;
import org.ultramine.mods.privreg.regions.Region;
import org.ultramine.regions.BlockPos;
import org.ultramine.regions.IRegion;
import org.ultramine.regions.Rectangle;

import java.util.Set;

@SideOnly(Side.SERVER)
public class WarpDriveEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreJumpEvent(EventWarpDrive.Ship.PreJump preJumpEvent) {
        World world = preJumpEvent.worldCurrent;
        TileEntity tileEntity = world.getTileEntity(preJumpEvent.xCurrent, preJumpEvent.yCurrent, preJumpEvent.zCurrent);

        if(tileEntity instanceof TileEntityShipCore) {
            TileEntityShipCore shipCore = (TileEntityShipCore) tileEntity;
            Rectangle warpBox = new Rectangle(new BlockPos(shipCore.minX, shipCore.minY, shipCore.minZ), new BlockPos(shipCore.maxX, shipCore.maxY, shipCore.maxZ));
            Set<IRegion> regions = PrivateRegions.instance().getServerRegionManager(world.provider.dimensionId).getRegionsInRange(warpBox);

            if(!regions.isEmpty()) {
                for (IRegion iRegion : regions) {
                    Region region = (Region) iRegion;
                    if(region.isActive()) {
                        preJumpEvent.appendReason("You are don't have permissions to warp into protected region!");
                        preJumpEvent.setCanceled(true);
                    }
                }
            }
        }
    }
}
