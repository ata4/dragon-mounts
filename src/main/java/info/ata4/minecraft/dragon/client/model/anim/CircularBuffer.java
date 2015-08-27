/*
 ** 2012 March 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.client.model.anim;

import info.ata4.minecraft.dragon.util.math.MathX;
import net.minecraft.util.MathHelper;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Very simple fixed size circular buffer implementation for animation purposes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CircularBuffer {

    private double buffer[];
    private int index = 0;  // points to the most recent value in the buffer.

    public CircularBuffer(int size) {
        buffer = new double[size];
    }

    /**
     * Initialise the circular buffer with the given constant value
     * @param value initial value to fill the buffer with
     */
    public void fill(double value) {
        Arrays.fill(buffer, value);
    }

    /** Add a new value to the circular buffer.  The oldest value is discarded.
     * @param value value to be added.
     */
    public void update(double value) {
        // move forward
        index++;
        
        // restart pointer at the end to form a virtual ring
        index %= buffer.length;
        
        buffer[index] = value;
    }

    /**
     * Retrieves a value from the buffer at a given age. Interpolates between the two nearest values
     * @param x interpolation value [0 to 1].  0 = newer value --> 1 = older value
     * @param offset the number of places to look back in time.  0 = most recent value.  must be 0 to buffersize-2 inclusive
     * @return the interpolated historical values
     */
    public double getInterpolatedValue(float x, int offset) {
        checkArgument(x >= 0.0F);
        checkArgument(x <= 1.0F);
        checkArgument(offset >= 0);
        checkArgument(offset <= buffer.length - 2);
        int newerIndex = MathX.modulus(index - offset, buffer.length);
        int olderIndex = MathX.modulus(index - offset - 1, buffer.length);
//        return MathX.lerp(buffer[i - 1 & len], buffer[i & len], x);  bug fix... & only works if buffer is a power of 2
        return MathX.lerp(buffer[newerIndex], buffer[olderIndex], x);
    }

    /**
     * Calculates the difference between two values from the buffer, [offset2 minus offset1]
     * @param x interpolation value [0 to 1].  0 = newer value --> 1 = older value
     * @param offset1 the number of places to look back in time.  0 = most recent value.  must be 0 to buffersize-2 inclusive
     * @param offset2 as offset1.
     * @return the difference between the two nominated historical values
     */
    public double getChangeInValue(float x, int offset1, int offset2) {
        return getInterpolatedValue(x, offset2) - getInterpolatedValue(x, offset1);
    }
}
