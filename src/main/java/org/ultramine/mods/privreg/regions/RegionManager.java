package org.ultramine.mods.privreg.regions;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ultramine.mods.privreg.InitCommon;
import org.ultramine.mods.privreg.RegionConfig;
import org.ultramine.mods.privreg.data.IRegionDataProvider;
import org.ultramine.mods.privreg.owner.BasicOwner;
import org.ultramine.mods.privreg.packets.PacketRegionExpand;
import org.ultramine.mods.privreg.tiles.TileBlockRegion;
import org.ultramine.network.UMPacket;
import org.ultramine.regions.BlockPos;
import org.ultramine.regions.IRegion;
import org.ultramine.regions.IRegionManager;
import org.ultramine.regions.Rectangle;
import org.ultramine.regions.RegionMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.SERVER)
public class RegionManager implements IRegionManager
{
	public static final Logger log = LogManager.getLogger();

	private final MinecraftServer server;
	private final int dimension;
	private final IRegionDataProvider dataProvider;
	private Region[] regions;
	private int lastID;

	private final RegionMap regionMap = new RegionMap();

	private RegionTracker tracker = new RegionTracker(this);

	public RegionManager(MinecraftServer server, int dimension, IRegionDataProvider dataProvider)
	{
		this.server = server;
		this.dimension = dimension;
		this.dataProvider = dataProvider;
	}

	public void loadRegions()
	{
		dataProvider.init(this);
		List<Region> list = new ArrayList<Region>();
		dataProvider.loadAll(list);
		int maxID = 0;
		for(Region reg : list)
			if(reg.getID() > maxID)
				maxID = reg.getID();
		lastID = maxID;
		int size = 128;
		while(size < maxID)
			size <<= 1;
		regions = new Region[size];
		for(Region reg : list)
		{
			reg.setWorld(dimension);
			regions[reg.getID()] = reg;
			regionMap.add(reg);
		}

		for(Region reg : list)
			if(reg.parentWaiting != -1)
				reg.setParent(regions[reg.parentWaiting]);

		for(Region reg : list)
		{
			if(checkOrRestore(reg))
				reg.onLoad();
			else
				destroyRegion(reg);
		}
	}

	private boolean checkOrRestore(Region reg)
	{
		BlockPos b = reg.getBlock();
		MinecraftServer mcserver = server;
		if (mcserver != null)
		{
			World world = mcserver.worldServerForDimension(reg.getWorld());
			if(world.getBlock(b.x, b.y, b.z) != InitCommon.region)
			{
				log.warn("Loaded region ID:{} for NOT RegionBlock [{}]({}, {}, {}) Trying to restore block", reg.getID(), reg.getWorld(), b.x, b.y, b.z);
				world.setBlockSilently(b.x, b.y, b.z, InitCommon.region, 0, 3);
				TileBlockRegion te = (TileBlockRegion) world.getTileEntity(b.x, b.y, b.z);
				if(te != null)
				{
					te.unsafeSetRegion(reg);
					log.info("RegionBlock successfuly restored");
				}
				else
				{
					log.warn("Failed to restore RegionBlock (TileEntity was NOT created)");
					return false;
				}
			}
		}

		return true;
	}

	private int getUniqueRegionID()
	{
		for(int i = 0; i < regions.length; i++)
			if(regions[i] == null) return i;

		int length = regions.length;
		Region[] regionsnew = new Region[length * 2];
		System.arraycopy(regions, 0, regionsnew, 0, length);
		regions = regionsnew;
		return length;
	}

	public Region createRegion(TileBlockRegion te, GameProfile player)
	{
		final int cd = RegionConfig.CheckDistance;
		BlockPos block = BlockPos.fromTileEntity(te);
		Rectangle shape = block.toRect().expandAll(1);
		int world = te.getWorldObj().provider.dimensionId;
		if(world != dimension)
			throw new IllegalArgumentException("Wrong RegionManager for dimension "+world+", used "+dimension);

		Region parent = null;
		if(hasRegionsInRange(shape))
		{
			Region found = getRegion(block);
			if(found != null)
			{
				BlockPos b = found.getBlock();
				if(found.getShape().contains(shape) && !shape.isIntersects(b.toRect().expandAll(1)))
				{
					if(found.hasRight(player, RegionRights.PLACE_SUBREGIONS))
					{
						parent = found;
					}
				}
			}
		}

		if(parent == null)
			shape = shape.setSide(ForgeDirection.UP, 255).setSide(ForgeDirection.DOWN, 0);
		if (parent == null && hasRegionsInRange(shape.expandAll(cd)))
			return null;

		Region region = new Region(this, getUniqueRegionID(), true);
		region.setBlock(block);
		region.setShape(shape);
		region.setWorld(world);
		if (parent != null)
			region.setParent(parent);
		region.onCreate();

		BasicOwner owner = new BasicOwner(player);
		owner.setRight(RegionRights.CREATOR, true);
		region.getOwnerStorage().add(owner);

		regions[region.getID()] = region;
		dataProvider.createRegion(region);
		if (region.getID() > lastID)
			lastID = region.getID();

		regionMap.add(region);
		tracker.onRegionCreate(region);

		return region;
	}

	public Region dangerousCreateRegion(Rectangle shape, BlockPos block, int dimension)
	{
		Region region = new Region(this, getUniqueRegionID(), true);

		region.setShape(shape);
		region.setWorld(dimension);
		region.setBlock(block);

		region.onCreate();

		regions[region.getID()] = region;
		dataProvider.createRegion(region);
		if (region.getID() > lastID)
			lastID = region.getID();

		regionMap.add(region);
		tracker.onRegionCreate(region);

		return region;
	}

	public void saveRegion(Region region)
	{
		dataProvider.saveRegion(region);
		region.setChanged(false);
	}

	public void saveAllRegion()
	{
		dataProvider.saveAll(regions);
	}

	public void onTick(int tick)
	{
		if(tick % 101 == 0)
		{
			for(int i = 0; i <= lastID; i++)
			{
				Region region = regions[i];
				if (region != null)
					region.onUpdate();
			}
		}
	}

	public void unload()
	{
		saveAllRegion();
		for(int i = 0; i <= lastID; i++)
		{
			Region region = regions[i];
			if (region != null)
				region.onUnload();
		}
		dataProvider.close();
	}

	public void destroyRegion(Region region)
	{
		regionMap.remove(region);
		region.onDestroy();
		regions[region.getID()] = null;
		dataProvider.destroyRegion(region);
		tracker.onRegionDestroy(region);
		if(region.getID() == lastID)
			lastID--;
	}

	public Region getRegion(int id)
	{
		if(id >= regions.length || id < 0)
			return null;

		return regions[id];
	}

	@Override
	public Region getRegion(BlockPos point)
	{
		return (Region)regionMap.get(point);
	}

	@Override
	public Region getRegion(int x, int y, int z)
	{
		return getRegion(new BlockPos(x, y, z));
	}

	@Override
	public Set<IRegion> getRegionsInRange(Rectangle range)
	{
		return regionMap.getInRange(range);
	}

	@Override
	public boolean hasRegionsInRange(Rectangle range)
	{
		return regionMap.hasInRange(range);
	}

	public Region[] unsafeGetRegions()
	{
		return regions;
	}

	public RegionChangeResult expandRegion(Region region, ForgeDirection dir, int amount)
	{
		RegionChangeResult res = region.canExpand(dir, amount);
		if(res != RegionChangeResult.ALLOW)
			return res;

		if(amount > 0)
		{
			regionMap.onRegionExpand(region, region.getShape().compress(dir.getOpposite(), region.getShape().getLen(dir)).expand(dir, amount));
			region.doExpand(dir, amount);
		}
		else
		{
			regionMap.remove(region);
			region.doExpand(dir, amount);
			regionMap.add(region);
		}

		tracker.sendToListeners(region, new PacketRegionExpand(region, dir, amount));

		return RegionChangeResult.ALLOW;
	}

	public void sendToListeners(Region region, UMPacket packet)
	{
		tracker.sendToListeners(region, packet);
	}

	RegionTracker getTracker()
	{
		return tracker;
	}
}
