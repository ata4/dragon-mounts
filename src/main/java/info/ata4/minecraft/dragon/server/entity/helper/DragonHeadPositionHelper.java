package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.util.Vec3;

/**
 * Created by TGG on 24/06/2015.
 */
public class DragonHeadPositionHelper
{
  public DragonHeadPositionHelper(EntityTameableDragon parent)
  {
    dragon = parent;
  }

  public Vec3 getThroatPosition()
  {
    float eyeHeight = dragon.getEyeHeight();
    Vec3 posVec = dragon.getPositionVector();
    float yaw = dragon.rotationYaw;
    float yawHead = dragon.rotationYawHead;
    float getYawHead = dragon.getRotationYawHead();
    float pitch = dragon.rotationPitch;

//    System.out.println("getThroatPosition():" + (dragon.worldObj.isRemote ? "client" : "server")
//            + " eyeHeight:" + eyeHeight + " posVec:" + posVec + " yaw:" + yaw + " yawHead:" + yawHead + " getYawHead:" + getYawHead
//           + " pitch:" + pitch);

    return new Vec3(0, 0, 0);
  }

  private EntityTameableDragon dragon;
}
