/*
 ** 2012 August 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.entity.ai.ground;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.ai.EntityAIPanic;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityAIPanicChild extends EntityAIPanic {
    
    private EntityTameableDragon dragon;

    public EntityAIPanicChild(EntityTameableDragon dragon, double speed) {
        super(dragon, speed);
        this.dragon = dragon;
    }

    @Override
    public boolean shouldExecute() {
      boolean flag = super.shouldExecute() && dragon.isHatchling();
//      System.out.println("EntityAIPanicChild::shouldExecute:" + flag);
      return flag;
    }

  @Override
  public void startExecuting() {
    super.startExecuting();
//    System.out.println("EntityAIPanicChild::startExecuting");
  }

  @Override
  public boolean continueExecuting() {
    boolean flag = super.continueExecuting();
//    System.out.println("EntityAIPanicChild::continueExecuting" + flag);
    return flag;
  }
}
