package info.ata4.minecraft.dragon.server.entity.helper;

/**
* Created by TGG on 30/08/2015.
 * Encapsulates the Size and Position of a model segment, with default values.
 *   Typical usage:
 * 1) An instance is created
 * 2) One or more of the values are changed
 * 3) copyIfValid is used on all values, to only copy/update those which were changed
*/
public class SegmentSizePositionRotation implements Cloneable
{
  public float rotationPointX = Float.NaN;   // NaN means the value is unused.
  public float rotationPointY = Float.NaN;
  public float rotationPointZ = Float.NaN;
  public float rotateAngleX = Float.NaN;
  public float rotateAngleY = Float.NaN;
  public float rotateAngleZ = Float.NaN;
  public float scaleX = Float.NaN;
  public float scaleY = Float.NaN;
  public float scaleZ = Float.NaN;

  public SegmentSizePositionRotation getCopy()
  {
    try {
      return (SegmentSizePositionRotation)super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new UnsupportedOperationException();
    }
  }

  /** returns newValue, or oldValue if newValue is not valid (NaN)
   * @param newValue
   * @param oldValue
   * @return newValue if not NAN, oldValue otherwise
   */
  public float copyIfValid(float newValue, float oldValue)
  {
    return Float.isNaN(newValue) ? oldValue : newValue;
  }

  public void setScale(float scaleX, float scaleY, float scaleZ) {
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.scaleZ = scaleZ;
  }
  public void setScale(float scale) {
    setScale(scale, scale, scale);
  }

  @Override
  public String toString()
  {
    return "rotationPoint [" + rotationPointX + ", " + rotationPointY + ", " + rotationPointZ + "], "
            + "rotateAngle [" + rotateAngleX + ", " + rotateAngleY + ", " + rotateAngleZ + "]"
            + "scale [" + scaleX + ", " + scaleY + ", " + scaleZ + "]";
  }
}
