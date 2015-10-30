package info.ata4.minecraft.dragon.server.entity.helper;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.util.MathHelper;

/**
 * For debugging.  Just copied the base class (too many private fields!)
 * Created by TGG on 22/07/2015.
 */
public class DragonLookHelper extends EntityLookHelper
{

  public DragonLookHelper(EntityTameableDragon i_dragon)
  {
    super(i_dragon);
    entity = i_dragon;
  }

    private EntityLiving entity;
    /** The amount of change that is made each update for an entity facing a direction. */
    private float deltaLookYaw;
    /** The amount of change that is made each update for an entity facing a direction. */
    private float deltaLookPitch;
    /** Whether or not the entity is trying to look at something. */
    private boolean isLooking;
    private double posX;
    private double posY;
    private double posZ;

    /**
     * Sets position to look at using entity
     */
    @Override
    public void setLookPositionWithEntity(Entity p_75651_1_, float p_75651_2_, float p_75651_3_)
    {
      this.posX = p_75651_1_.posX;

      if (p_75651_1_ instanceof EntityLivingBase)
      {
        this.posY = p_75651_1_.posY + (double)p_75651_1_.getEyeHeight();
      }
      else
      {
        this.posY = (p_75651_1_.getEntityBoundingBox().minY + p_75651_1_.getEntityBoundingBox().maxY) / 2.0D;
      }

      this.posZ = p_75651_1_.posZ;
      this.deltaLookYaw = p_75651_2_;
      this.deltaLookPitch = p_75651_3_;
      this.isLooking = true;
    }

    /**
     * Sets position to look at
     */
    @Override
    public void setLookPosition(double p_75650_1_, double p_75650_3_, double p_75650_5_, float p_75650_7_, float p_75650_8_)
    {
      this.posX = p_75650_1_;
      this.posY = p_75650_3_;
      this.posZ = p_75650_5_;
      this.deltaLookYaw = p_75650_7_;
      this.deltaLookPitch = p_75650_8_;
      this.isLooking = true;
    }

    /**
     * Updates look
     */
    @Override
    public void onUpdateLook()
    {
      this.entity.rotationPitch = 0.0F;

      if (this.isLooking)
      {
        this.isLooking = false;
        double d0 = this.posX - this.entity.posX;
        double d1 = this.posY - (this.entity.posY + (double)this.entity.getEyeHeight());
        double d2 = this.posZ - this.entity.posZ;
        double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
        float f = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f1 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
        this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, f1, this.deltaLookPitch);
        this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, f, this.deltaLookYaw);
      }
      else
      {
        this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, this.entity.renderYawOffset, 10.0F);
      }

      float f2 = MathHelper.wrapAngleTo180_float(this.entity.rotationYawHead - this.entity.renderYawOffset);

      if (!this.entity.getNavigator().noPath())
      {
        if (f2 < -75.0F)
        {
          this.entity.rotationYawHead = this.entity.renderYawOffset - 75.0F;
        }

        if (f2 > 75.0F)
        {
          this.entity.rotationYawHead = this.entity.renderYawOffset + 75.0F;
        }
      }
    }

  private float updateRotation(float p_75652_1_, float p_75652_2_, float p_75652_3_)
  {
    float f3 = MathHelper.wrapAngleTo180_float(p_75652_2_ - p_75652_1_);

    if (f3 > p_75652_3_)
    {
      f3 = p_75652_3_;
    }

    if (f3 < -p_75652_3_)
    {
      f3 = -p_75652_3_;
    }

    return p_75652_1_ + f3;
  }

  @Override
    public boolean func_180424_b()
    {
      return this.isLooking;
    }

  @Override
    public double func_180423_e()
    {
      return this.posX;
    }

  @Override
    public double func_180422_f()
    {
      return this.posY;
    }

  @Override
    public double func_180421_g()
    {
      return this.posZ;
    }

}
