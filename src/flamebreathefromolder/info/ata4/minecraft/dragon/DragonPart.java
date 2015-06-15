/*
** 2011 December 10
**
** The author disclaims copyright to this source code.  In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
*/

package info.ata4.minecraft.dragon;

import net.minecraft.src.*;

/**
 * Entity proxy for a dragon hitbox.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonPart extends Entity {

    public final Dragon base;
    public final String id;

    public DragonPart(Dragon base, String id, float width, float height) {
        super(base.worldObj);
        this.base = base;
        this.id = id;
        
        isImmuneToFire = true;
        
        setSize(width, height);
    }
    
    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (base == null || !base.isEntityAlive()) {
            setEntityDead();
        }
    }
    
    @Override
    protected void setSize(float width, float height) {
        super.setSize(width * base.getBodySize(), height * base.getBodySize());
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return base.canBeCollidedWith();
    }

    @Override
    public boolean attackEntityFrom(DamageSource damagesource, int i) {
        return base.attackPartFrom(this, damagesource, i);
    }

    @Override
    public boolean isEntityEqual(Entity entity) {
        return this == entity || base == entity;
    }

    @Override
    public boolean interact(EntityPlayer player) {
        return base.interact(player);
    }
    
    public float getBodySize() {
        return base.getBodySize();
    }

    @Override
    public String toString() {
        return id;
    }
}
