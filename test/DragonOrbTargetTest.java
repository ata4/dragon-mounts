import info.ata4.minecraft.dragon.server.network.DragonOrbTarget;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.Vec3;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by TGG on 7/07/2015.
 */
public class DragonOrbTargetTest
{

  @Test
  public void testConstructors() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    DragonOrbTarget test = DragonOrbTarget.targetDirection(testVec1);
    DragonOrbTarget test2 = DragonOrbTarget.targetLocation(testVec1);
    int entityID = 3523;
    DragonOrbTarget test3 = DragonOrbTarget.targetEntityID(entityID);
  }


  @Test
  public void testByteTransmission() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    DragonOrbTarget testd1 = DragonOrbTarget.targetDirection(testVec1);
    DragonOrbTarget testl1 = DragonOrbTarget.targetLocation(testVec1);
    int entityID = 3523;
    DragonOrbTarget teste1 = DragonOrbTarget.targetEntityID(entityID);

    ByteBuf byteBuf = Unpooled.buffer(1000);
    testd1.toBytes(byteBuf);
    DragonOrbTarget received = DragonOrbTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(testd1));

    byteBuf.clear();
    testl1.toBytes(byteBuf);
    received = DragonOrbTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(testl1));

    byteBuf.clear();
    teste1.toBytes(byteBuf);
    received = DragonOrbTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(teste1));
  }

  @Test
  public void testEncodedStringTransmission() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    DragonOrbTarget testd1 = DragonOrbTarget.targetDirection(testVec1);
    DragonOrbTarget testl1 = DragonOrbTarget.targetLocation(testVec1);
    int entityID = 3523;
    DragonOrbTarget teste1 = DragonOrbTarget.targetEntityID(entityID);

    ByteBuf byteBuf = Unpooled.buffer(1000);
    String string = testd1.toEncodedString();
    DragonOrbTarget received = DragonOrbTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(testd1));

    string = testl1.toEncodedString();
    received = DragonOrbTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(testl1));

    string = teste1.toEncodedString();
    received = DragonOrbTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(teste1));
  }

  @Test
  public void testApproximatelyMatches() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    DragonOrbTarget testd1 = DragonOrbTarget.targetDirection(testVec1);
    DragonOrbTarget testl1 = DragonOrbTarget.targetLocation(testVec1);
    int entityID = 3523;
    DragonOrbTarget teste1 = DragonOrbTarget.targetEntityID(entityID);

    assertFalse(testd1.exactlyMatches(testl1));
    assertFalse(testd1.approximatelyMatches(testl1));

    assertFalse(teste1.exactlyMatches(testl1));
    assertFalse(teste1.approximatelyMatches(testl1));

    assertFalse(testd1.exactlyMatches(testl1));
    assertFalse(testd1.approximatelyMatches(testl1));

    Vec3 testVec2 = new Vec3(3.0, 2.0, 1.0);
    DragonOrbTarget testd2 = DragonOrbTarget.targetDirection(testVec2);
    DragonOrbTarget testl2 = DragonOrbTarget.targetLocation(testVec2);
    DragonOrbTarget teste2 = DragonOrbTarget.targetEntityID(entityID+1);
    assertFalse(testd1.exactlyMatches(testd2));
    assertFalse(testd1.approximatelyMatches(testd2));

    assertFalse(testl1.exactlyMatches(testl2));
    assertFalse(testl1.approximatelyMatches(testl2));

    assertFalse(teste1.exactlyMatches(teste2));
    assertFalse(teste1.approximatelyMatches(teste2));

    Vec3 testVec3 = new Vec3(1.0, 2.0, 3.0);
    DragonOrbTarget testd3 = DragonOrbTarget.targetDirection(testVec3);
    DragonOrbTarget testl3 = DragonOrbTarget.targetLocation(testVec3);
    DragonOrbTarget teste3 = DragonOrbTarget.targetEntityID(entityID);
    assertTrue(testd1.exactlyMatches(testd3));
    assertTrue(testd1.approximatelyMatches(testd3));

    assertTrue(testl1.exactlyMatches(testl3));
    assertTrue(testl1.approximatelyMatches(testl3));

    assertTrue(teste1.exactlyMatches(teste3));
    assertTrue(teste1.approximatelyMatches(teste3));

    Vec3 testVec4 = new Vec3(1.01, 2.0, 3.0);
    DragonOrbTarget testd4 = DragonOrbTarget.targetDirection(testVec4);
    DragonOrbTarget testl4 = DragonOrbTarget.targetLocation(testVec4);
    assertFalse(testd1.exactlyMatches(testd4));
    assertTrue(testd1.approximatelyMatches(testd4));

    assertFalse(testl1.exactlyMatches(testl4));
    assertTrue(testl1.approximatelyMatches(testl4));
  }
}