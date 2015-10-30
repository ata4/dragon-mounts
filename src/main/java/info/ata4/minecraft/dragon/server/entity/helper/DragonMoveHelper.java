package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.MathHelper;

/**
 * Override vanilla - primarily for debugging at this stage
 * Created by TGG on 22/07/2015.
 */
public class DragonMoveHelper extends EntityMoveHelper
{
  public DragonMoveHelper(EntityTameableDragon dragon)
  {
    super(dragon);
  }

  /**
   * Copied from vanilla!
   */
  @Override
  public void onUpdateMoveHelper()
  {
    this.entity.setMoveForward(0.0F);

    if (this.update)
    {
      this.update = false;
      int i = MathHelper.floor_double(this.entity.getEntityBoundingBox().minY + 0.5D);
      double d0 = this.posX - this.entity.posX;
      double d1 = this.posZ - this.entity.posZ;
      double d2 = this.posY - (double)i;
      double d3 = d0 * d0 + d2 * d2 + d1 * d1;

      if (d3 >= 2.500000277905201E-7D)
      {
        float f = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float newRotationYaw = this.limitAngle(this.entity.rotationYaw, f, 30.0F);
        if (newRotationYaw != entity.rotationYaw) {
        }
        this.entity.rotationYaw = newRotationYaw;
                this.entity.setAIMoveSpeed((float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()));

        if (d2 > 0.0D && d0 * d0 + d1 * d1 < 1.0D)
        {
          this.entity.getJumpHelper().setJumping();
        }
      }
    }
  }
}
