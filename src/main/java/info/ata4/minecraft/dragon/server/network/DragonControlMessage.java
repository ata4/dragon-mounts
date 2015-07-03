/*
 ** 2014 March 19
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.BitSet;
/**
 * Dragon control message packet.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonControlMessage implements IMessage {
    
    private final BitSet bits = new BitSet(Byte.SIZE);
    private int previous;
    
    public BitSet getFlags() {
        return bits;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        fromInteger(buf.readUnsignedByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(toInteger());
    }
    
    public void fromInteger(int value) { // ? BitSet already has inbuilt to do this? leave as is...
        bits.clear();
        for (int i = 0; i < bits.size(); i++) {
            if ((value & (1 << i)) != 0) {
                bits.set(i);
            }
        }
    }
    
    public int toInteger() {    // ? BitSet already has inbuilt to do this?
        int value = 0;
        for (int i = 0; i < bits.length(); i++) {
            value += bits.get(i) ? (1 << i) : 0;
        }
        return value;
    }

    public boolean hasChanged() {
        int current = toInteger();
        boolean changed = previous != current;
        previous = current;
        return changed;
    }
}
