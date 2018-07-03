package org.ultramine.mods.privreg.integration;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.api.EventWarpDrive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import org.ultramine.mods.privreg.PrivateRegions;
import org.ultramine.mods.privreg.modules.RegionModuleBasic;
import org.ultramine.mods.privreg.regions.Region;
import org.ultramine.regions.BlockPos;
import org.ultramine.regions.IRegion;
import org.ultramine.regions.Rectangle;

import java.util.ArrayList;
import java.util.Set;

import static org.ultramine.mods.privreg.modules.RegionModuleBasic.RIGHT_BASIC;

@SideOnly(Side.SERVER)
public class WarpDriveEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTargetCheckEvent(EventWarpDrive.Ship.TargetCheck targetCheck) {
        AxisAlignedBB targetAaBb = targetCheck.aabbTarget;
        Rectangle targetRect = new Rectangle(new BlockPos(targetAaBb.minX, targetAaBb.minY, targetAaBb.minZ), new BlockPos(targetAaBb.maxX, targetAaBb.maxY, targetAaBb.maxZ));
        Set<IRegion> regions = PrivateRegions.instance().getServerRegionManager(targetCheck.worldTarget.provider.dimensionId).getRegionsInRange(targetRect);

        if (!regions.isEmpty()) {
            for (IRegion iRegion : regions) {
                Region region = (Region) iRegion;
                if (region.isActive() && region.hasModule(RegionModuleBasic.class)) {
                    ArrayList<String> players = (ArrayList<String>) targetCheck.shipController.getAttachedPlayers()[1];
                    if(!players.isEmpty()) {
                        for(String playerName : players) {
                            EntityPlayer entityPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(playerName);
                            if(entityPlayer != null) {
                                if (!region.hasRight(entityPlayer.getGameProfile(), RIGHT_BASIC)) {
                                    targetCheck.appendReason("One of the players on the ship don't have permission to private region");
                                    targetCheck.setCanceled(true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
