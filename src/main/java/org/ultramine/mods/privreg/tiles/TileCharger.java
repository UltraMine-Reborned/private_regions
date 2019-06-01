package org.ultramine.mods.privreg.tiles;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import org.ultramine.core.economy.account.Account;
import org.ultramine.core.economy.holdings.Holdings;
import org.ultramine.core.economy.service.DefaultCurrencyService;
import org.ultramine.core.economy.service.Economy;
import org.ultramine.core.service.InjectService;
import org.ultramine.gui.IGui;
import org.ultramine.mods.privreg.PrivateRegions;
import org.ultramine.mods.privreg.RegionConfig;
import org.ultramine.mods.privreg.gui.GuiCharger;
import org.ultramine.mods.privreg.gui.inv.ContainerCharger;
import org.ultramine.mods.privreg.packets.PacketChargerAction;
import org.ultramine.network.ITEPacketHandler;
import org.ultramine.util.IHasGui;
import org.ultramine.util.SimpleInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TileCharger extends TileEntity implements ITEPacketHandler<PacketChargerAction>, IHasGui {
    private final Map<UUID, InventoryCharger> invs = new HashMap<UUID, InventoryCharger>();

    @InjectService
    private static Economy economy;
    @InjectService
    private static DefaultCurrencyService defaultCurrency;

    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
                player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
    }

    private InventoryCharger getInventory(EntityPlayer player) {
        UUID id = player.getGameProfile().getId();
        InventoryCharger inv = invs.get(id);
        if (inv == null) {
            inv = new InventoryCharger(this, 0);
            invs.put(id, inv);
        }
        return inv;
    }

    public boolean activateServer(EntityPlayer player) {
        player.openGui(PrivateRegions.instance(), 0, worldObj, xCoord, yCoord, zCoord);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IGui getGui(int id, EntityPlayer player) {
        return new GuiCharger(player.inventory, new InventoryCharger(this, 0));
    }

    @Override
    public Container getGuiContainer(int id, EntityPlayer player) {
        return new ContainerCharger(player.inventory, getInventory(player));
    }

    private void buyAntimatter(EntityPlayer player, InventoryCharger inv, int count) {

        Account account = economy.getPlayerAccount(player.getGameProfile());
        Holdings holdings = account.getHoldings(defaultCurrency.getDefaultCurrency());

        double cost = count * RegionConfig.moneyPerEA;
        if (cost > 0 && holdings.hasEnough(cost)) {
            holdings.withdraw(cost);
            if (inv.charge + count > inv.maxCharge) {
                inv.maxCharge = inv.charge + count;
            }

            inv.charge += count;
        }
    }

    @Override
    public void handlePacketClient(PacketChargerAction pkt) {

    }

    @Override
    public void handlePacketServer(PacketChargerAction pkt, EntityPlayerMP player) {
        buyAntimatter(player, getInventory(player), pkt.amount);
    }

    public static class InventoryCharger extends SimpleInventory {
        public final TileCharger te;
        public int charge;
        public int maxCharge = RegionConfig.ChargerMaxCharge;

        public InventoryCharger(TileCharger te, int charge) {
            super(1);
            this.te = te;
            this.charge = charge;
        }

        public TileCharger getTile() {
            return te;
        }
    }
}
