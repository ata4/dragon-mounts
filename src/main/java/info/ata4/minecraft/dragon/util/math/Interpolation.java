/*
** 2016 March 05
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.util.math;

/**
 * Interpolation utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class Interpolation {
    
    private static final float[][] CR = {
        {-0.5f,  1.5f, -1.5f,  0.5f},
        { 1.0f, -2.5f,  2.0f, -0.5f},
        {-0.5f,  0.0f,  0.5f,  0.0f},
        { 0.0f,  1.0f,  0.0f,  0.0f}
    };
    
    public static float linear(float a, float b, float x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }
        return a * (1 - x) + b * x;
    }
    
    public static float smoothStep(float a, float b, float x) {
        if (x <= 0) {
            return a;
        }
        if (x >= 1) {
            return b;
        }
        x = x * x * (3 - 2 * x);
        return a * (1 - x) + b * x;
    }

    // http://www.java-gaming.org/index.php?topic=24122.0
    public static void catmullRomSpline(float x, float[] result, float[]... knots) {
        int nknots = knots.length;
        int nspans = nknots - 3;
        int knot = 0;
        if (nspans < 1) {
            throw new IllegalArgumentException("Spline has too few knots");
        }
        x = MathX.clamp(x, 0, 0.9999f) * nspans;
        
        int span = (int) x;
        if (span >= nknots - 3) {
            span = nknots - 3;
        }

        x -= span;
        knot += span;

        int dimension = result.length;
        for (int i = 0; i < dimension; i++) {
            float knot0 = knots[knot][i];
            float knot1 = knots[knot + 1][i];
            float knot2 = knots[knot + 2][i];
            float knot3 = knots[knot + 3][i];

            float c3 = CR[0][0] * knot0 + CR[0][1] * knot1 + CR[0][2] * knot2 + CR[0][3] * knot3;
            float c2 = CR[1][0] * knot0 + CR[1][1] * knot1 + CR[1][2] * knot2 + CR[1][3] * knot3;
            float c1 = CR[2][0] * knot0 + CR[2][1] * knot1 + CR[2][2] * knot2 + CR[2][3] * knot3;
            float c0 = CR[3][0] * knot0 + CR[3][1] * knot1 + CR[3][2] * knot2 + CR[3][3] * knot3;

            result[i] = ((c3 * x + c2) * x + c1) * x + c0;
        }
    }

    private Interpolation() {
    }
}
