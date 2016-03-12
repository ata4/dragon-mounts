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

import info.ata4.minecraft.dragon.util.math.Interpolation;
import java.util.Arrays;

/**
 * Very simple fixed size circular buffer implementation for animation purposes.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CircularBuffer {

    private float buffer[];
    private int index = 0;

    public CircularBuffer(int size) {
        buffer = new float[size];
    }
    
    public void fill(float value) {
        Arrays.fill(buffer, value);
    }
    
    public void update(float value) {
        // move forward
        index++;
        
        // restart pointer at the end to form a virtual ring
        index %= buffer.length;
        
        buffer[index] = value;
    }
    
    public float get(float x, int offset) {
        int i = index - offset;
        int len = buffer.length - 1;
        return Interpolation.linear(buffer[i - 1 & len], buffer[i & len], x);
    }
    
    public float get(float x, int offset1, int offset2) {
        return get(x, offset2) - get(x, offset1);
    }
}
