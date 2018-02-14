package org.ultramine.mods.privreg.integration;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.events.TeleporterUsingEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import org.ultramine.mods.privreg.PrivateRegions;
import org.ultramine.mods.privreg.modules.RegionModuleBasic;
import org.ultramine.mods.privreg.regions.Region;

import static org.ultramine.mods.privreg.modules.RegionModuleBasic.RIGHT_BASIC;

@SideOnly(Side.SERVER)
public class GT5EventHandler {

    private static Region getRegion(int world, int x, int y, int z) {
        return PrivateRegions.instance().getServerRegion(world, x, y, z);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onTeleporterUsingEvent(TeleporterUsingEvent event) {
        if (event.mEntity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.mEntity;
            Region region = getRegion(event.mTargetD, event.mTargetX, event.mTargetY, event.mTargetZ);
            if (region != null && region.isActive()) {
                if (region.hasModule(RegionModuleBasic.class)) {
                    if (!region.hasRight(player.getGameProfile(), RIGHT_BASIC)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

}
