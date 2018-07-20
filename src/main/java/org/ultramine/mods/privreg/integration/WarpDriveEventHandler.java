package org.ultramine.mods.privreg.integration;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.api.EventWarpDrive;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import org.ultramine.mods.privreg.PrivateRegions;
import org.ultramine.mods.privreg.modules.RegionModuleBasic;
import org.ultramine.mods.privreg.regions.Region;
import org.ultramine.regions.BlockPos;
import org.ultramine.regions.IRegion;
import org.ultramine.regions.Rectangle;

import java.util.Set;

import static org.ultramine.mods.privreg.modules.RegionModuleBasic.RIGHT_BASIC;

@SideOnly(Side.SERVER)
public class WarpDriveEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreJump(EventWarpDrive.Ship.PreJump preJump) {
        TileEntity tileEntity = preJump.worldCurrent.getTileEntity(preJump.xCurrent, preJump.yCurrent, preJump.zCurrent);
        if (tileEntity instanceof TileEntityShipCore) {
            TileEntityShipCore teShipCore = (TileEntityShipCore) tileEntity;
            Rectangle rectangle = new Rectangle(new BlockPos(teShipCore.minX, teShipCore.minY, teShipCore.minZ), new BlockPos(teShipCore.maxX, teShipCore.maxY, teShipCore.maxZ));
            Set<IRegion> regions = PrivateRegions.instance().getServerRegionManager(preJump.worldCurrent.provider.dimensionId).getRegionsInRange(rectangle);

            if (!checkRegionRights(regions, (String[]) preJump.shipController.getAttachedPlayers()[1])) {
                preJump.appendReason("One of the players on the ship don't have permission to departure point");
                preJump.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTargetCheckEvent(EventWarpDrive.Ship.TargetCheck targetCheck) {
        AxisAlignedBB targetAaBb = targetCheck.aabbTarget;
        Rectangle targetRect = new Rectangle(new BlockPos(targetAaBb.minX, targetAaBb.minY, targetAaBb.minZ), new BlockPos(targetAaBb.maxX, targetAaBb.maxY, targetAaBb.maxZ));
        Set<IRegion> regions = PrivateRegions.instance().getServerRegionManager(targetCheck.worldTarget.provider.dimensionId).getRegionsInRange(targetRect);

        if (!checkRegionRights(regions, (String[]) targetCheck.shipController.getAttachedPlayers()[1])) {
            targetCheck.appendReason("One of the players on the ship don't have permission to destination point");
            targetCheck.setCanceled(true);
        }
    }

    private boolean checkRegionRights(Set<IRegion> regions, String[] players) {
        if (!regions.isEmpty()) {
            for (IRegion iRegion : regions) {
                Region region = (Region) iRegion;
                if (region.isActive() && region.hasModule(RegionModuleBasic.class)) {
                    if (players != null && players.length > 0) {
                        for (String playerName : players) {
                            EntityPlayer entityPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(playerName);
                            if (entityPlayer != null && !region.hasRight(entityPlayer.getGameProfile(), RIGHT_BASIC)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}