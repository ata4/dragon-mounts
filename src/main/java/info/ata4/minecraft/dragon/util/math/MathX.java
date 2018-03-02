/*
 ** 2012 Januar 8
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.util.math;

/**
 * Math helper class.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class MathX {

	public static final double PI_D = Math.PI;
	public static final float PI_F = (float) Math.PI;

	/**
	 * You no take constructor!
	 */
	private MathX() {
	}

	// float sine function, may use LUT
	public static float sin(float a) {
		return (float) Math.sin(a);
	}

	// float cosine function, may use LUT
	public static float cos(float a) {
		return (float) Math.cos(a);
	}

	// float tangent function
	public static float tan(float a) {
		return (float) Math.tan(a);
	}

	// float atan2 function
	public static float atan2(float y, float x) {
		return (float) Math.atan2(y, x);
	}

	// float degrees to radians conversion
	public static float toRadians(float angdeg) {
		return (float) Math.toRadians(angdeg);
	}

	// float radians to degrees conversion
	public static float toDegrees(float angrad) {
		return (float) Math.toDegrees(angrad);
	}

	// normalizes a float degrees angle to between +180 and -180
	public static float normDeg(float a) {
		a %= 360;
		if (a >= 180) {
			a -= 360;
		}
		if (a < -180) {
			a += 360;
		}
		return a;
	}

	// normalizes a double degrees angle to between +180 and -180
	public static double normDeg(double a) {
		a %= 360;
		if (a >= 180) {
			a -= 360;
		}
		if (a < -180) {
			a += 360;
		}
		return a;
	}

	// normalizes a float radians angle to between +π and -π
	public static float normRad(float a) {
		a %= PI_F * 2;
		if (a >= PI_F) {
			a -= PI_F * 2;
		}
		if (a < -PI_F) {
			a += PI_F * 2;
		}
		return a;
	}

	// normalizes a double radians angle to between +π and -π
	public static double normRad(double a) {
		a %= PI_D * 2;
		if (a >= PI_D) {
			a -= PI_D * 2;
		}
		if (a < -PI_D) {
			a += PI_D * 2;
		}
		return a;
	}

	// float square root
	public static float sqrtf(float f) {
		return (float) Math.sqrt(f);
	}

	// numeric float clamp
	public static float clamp(float value, float min, float max) {
		return (value < min ? min : (value > max ? max : value));
	}

	// numeric double clamp
	public static double clamp(double value, double min, double max) {
		return (value < min ? min : (value > max ? max : value));
	}

	// numeric integer clamp
	public static int clamp(int value, int min, int max) {
		return (value < min ? min : (value > max ? max : value));
	}

	public static float updateRotation(float r1, float r2, float step) {
		return r1 + clamp(normDeg(r2 - r1), -step, step);
	}
}
