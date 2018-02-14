package ic2.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

@Cancelable
public class ExplosionEvent extends WorldEvent {
    /**
     * Entity representing the explosive, may be null.
     */
    public final Entity entity;
    /**
     * Entity causing the explosion, may be null.
     */
    public final EntityLivingBase igniter;
    public final int radiationRange;
    public final double rangeLimit;
    public double x;
    public double y;
    public double z;
    public double power;
    public ExplosionEvent(World world, Entity entity,
                          double x, double y, double z,
                          double power,
                          EntityLivingBase igniter,
                          int radiationRange, double rangeLimit) {
        super(world);

        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.igniter = igniter;
        this.radiationRange = radiationRange;
        this.rangeLimit = rangeLimit;
    }
}
