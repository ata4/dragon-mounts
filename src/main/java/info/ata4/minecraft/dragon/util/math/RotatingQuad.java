package info.ata4.minecraft.dragon.util.math;

/**
 * Created by TGG on 22/06/2015.
 * Used to rotate a texture quad.
 * Usage:
 * initialise with minU, minV, maxU, maxV
 * call rotate90(n) to rotate by n times 90, anti-clockwise
 * call mirrorLR to flip the quad left<--> right
 * call mirrorUD to flip the quad up<->down
 * then use getU and getV to return the coordinates of each corner, numbered as
 *  anticlockwise from maxU, maxV, ie
 * 0 = maxU, maxV
 * 1 = maxU, minV
 * 2 = minU, minV
 * 3 = minU, maxV
 */
public class RotatingQuad
{
  public RotatingQuad(double minU, double minV, double maxU, double maxV)
  {
    u[0] = maxU; u[1] = maxU; u[2] = minU; u[3] = minU;
    v[0] = maxV; v[1] = minV; v[2] = minV; v[3] = maxV;
  }

  public double getU(int point) {return u[point];}
  public double getV(int point) {return v[point];}

  public void mirrorLR()
  {
    double [] newU = new double[4];
    double [] newV = new double[4];
    newU[0] = u[3]; newU[1] = u[2]; newU[2] = u[1]; newU[3] = u[0];
    newV[0] = v[3]; newV[1] = v[2]; newV[2] = v[1]; newV[3] = v[0];
    u = newU;
    v = newV;
  }

  public void mirrorUD()
  {
    double [] newU = new double[4];
    double [] newV = new double[4];
    newU[0] = u[1]; newU[1] = u[0]; newU[2] = u[3]; newU[3] = u[2];
    newV[0] = v[1]; newV[1] = v[0]; newV[2] = v[3]; newV[3] = v[2];
    u = newU;
    v = newV;
  }

  public void rotate90(int numberOf90Rotations)
  {
    numberOf90Rotations %= 4;
    if (numberOf90Rotations < 0) numberOf90Rotations += 4;

    double [] newU = new double[4];
    double [] newV = new double[4];
    for (int i = 0; i < 4; ++i) {
      newU[i] = u[(i + numberOf90Rotations) % 4];
      newV[i] = v[(i + numberOf90Rotations) % 4];
    }
  }

  private double [] u = new double[4];
  private double [] v = new double[4];
}
