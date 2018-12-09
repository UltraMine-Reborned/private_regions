package org.ultramine.mods.privreg;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.ultramine.mods.privreg.regions.RegionManagerClient;
import org.ultramine.mods.privreg.render.*;
import org.ultramine.mods.privreg.tiles.TileBlockRegion;
import org.ultramine.mods.privreg.tiles.TileRentStand;

@SideOnly(Side.CLIENT)
public class InitClient extends InitCommon {
    private volatile boolean clear = false;

    @Override
    void initSided() {
        if (RegionConfig.enableDragonModel) {
            RenderingRegistry.registerBlockHandler(new BlockRegionRender(region.getRenderType()));
            ClientRegistry.bindTileEntitySpecialRenderer(TileBlockRegion.class, new TileBlockRegionRenderer());
        }

        RenderingRegistry.registerBlockHandler(new BlockBorderRender(barrier.getRenderType()));
        ClientRegistry.bindTileEntitySpecialRenderer(TileRentStand.class, new TileRentStandRender());

        EventHandlerClient ehc = new EventHandlerClient();
        FMLCommonHandler.instance().bus().register(ehc);
        MinecraftForge.EVENT_BUS.register(ehc);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            if (clear) {
                clear = false;
                ClientSelectionRenderer.clear();
                RegionManagerClient.getInstance().clearRegions();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        ClientSelectionRenderer.renderAllRegions();
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        clear = true;
    }
}
