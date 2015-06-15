/*
 ** 2012 Januar 8
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft;

import net.minecraft.src.MathHelper;

/**
 * Simple float-based math helper class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MathF {

    public static final float PI = (float) Math.PI;
    
    /**
     * You no take constructor!
     */
    private MathF() {
    }

    public static float normAngles(double a) {
        for (; a >= 180; a -= 360) {
        }
        for (; a < -180; a += 360) {
        }
        return (float) a;
    }

    public static float sinL(double a) {
        return MathHelper.sin((float) a);
    }

    public static float sin(double a) {
        return (float) Math.sin(a);
    }

    public static float cosL(double a) {
        return MathHelper.cos((float) a);
    }

    public static float cos(double a) {
        return (float) Math.cos(a);
    }

    public static float tan(double a) {
        return (float) Math.tan(a);
    }

    public static float atan2(double y, double x) {
        return (float) Math.atan2(y, x);
    }

    public static float toRadians(double angdeg) {
        return (float) Math.toRadians(angdeg);
    }

    public static float toDegrees(double angrad) {
        return (float) Math.toDegrees(angrad);
    }

    public static float sqrt(double f) {
        return (float) Math.sqrt(f);
    }

    public static float interpLin(double y1, double y2, double mu) {
        return (float) (y1 * (1 - mu) + y2 * mu);
    }

    public static float interpCos(double y1, double y2, double mu) {
        if (mu == 0.0) {
            return (float) y1;
        }
        if (mu == 1.0) {
            return (float) y2;
        }
        
        double mu2 = (1 - Math.cos(mu * Math.PI)) / 2.0;
        return (float) (y1 * (1 - mu2) + y2 * mu2);
    }

    public static float interpCubic(double y0, double y1, double y2, double y3, double mu) {
        double mu2 = mu * mu;
        double a0 = y3 - y2 - y0 + y1;
        double a1 = y0 - y1 - a0;
        double a2 = y2 - y0;
        double a3 = y1;

        return (float) (a0 * mu * mu2 + a1 * mu2 + a2 * mu + a3);
    }

    /*
    Tension: 1 is high, 0 normal, -1 is low
    Bias: 0 is even,
    positive is towards first segment,
    negative towards the other
     */
    public static float interpHermite(double y0, double y1, double y2, double y3,
            double mu, double tension, double bias) {
        double m0, m1, mu2, mu3;
        double a0, a1, a2, a3;

        mu2 = mu * mu;
        mu3 = mu2 * mu;
        m0  = (y1 - y0) * (1 + bias) * (1 - tension) / 2.0;
        m0 += (y2 - y1) * (1 - bias) * (1 - tension) / 2.0;
        m1  = (y2 - y1) * (1 + bias) * (1 - tension) / 2.0;
        m1 += (y3 - y2) * (1 - bias) * (1 - tension) / 2.0;
        a0 = 2 * mu3 - 3 * mu2 + 1;
        a1 = mu3 - 2 * mu2 + mu;
        a2 = mu3 - mu2;
        a3 = -2 * mu3 + 3 * mu2;

        return (float) (a0 * y1 + a1 * m0 + a2 * m1 + a3 * y2);
    }
    
    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }
}
