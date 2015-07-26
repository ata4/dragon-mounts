import info.ata4.minecraft.dragon.server.network.BreathWeaponTarget;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by TGG on 7/07/2015.
 */
public class BreathWeaponTargetTest
{

  @Test
  public void testConstructors() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    BreathWeaponTarget test = BreathWeaponTarget.targetDirection(testVec1);
    BreathWeaponTarget test2 = BreathWeaponTarget.targetLocation(testVec1);
    int entityID = 3523;
    BreathWeaponTarget test3 = BreathWeaponTarget.targetEntityID(entityID);
  }


  @Test
  public void testByteTransmission() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    BreathWeaponTarget testd1 = BreathWeaponTarget.targetDirection(testVec1);
    BreathWeaponTarget testl1 = BreathWeaponTarget.targetLocation(testVec1);
    int entityID = 3523;
    BreathWeaponTarget teste1 = BreathWeaponTarget.targetEntityID(entityID);

    ByteBuf byteBuf = Unpooled.buffer(1000);
    testd1.toBytes(byteBuf);
    BreathWeaponTarget received = BreathWeaponTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(testd1));

    byteBuf.clear();
    testl1.toBytes(byteBuf);
    received = BreathWeaponTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(testl1));

    byteBuf.clear();
    teste1.toBytes(byteBuf);
    received = BreathWeaponTarget.fromBytes(byteBuf);
    assertTrue(received.exactlyMatches(teste1));


  }

  @Test
  public void testEncodedStringTransmission() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    BreathWeaponTarget testd1 = BreathWeaponTarget.targetDirection(testVec1);
    BreathWeaponTarget testl1 = BreathWeaponTarget.targetLocation(testVec1);
    int entityID = 3523;
    BreathWeaponTarget teste1 = BreathWeaponTarget.targetEntityID(entityID);

    ByteBuf byteBuf = Unpooled.buffer(1000);
    String string = testd1.toEncodedString();
    BreathWeaponTarget received = BreathWeaponTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(testd1));

    string = testl1.toEncodedString();
    received = BreathWeaponTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(testl1));

    string = teste1.toEncodedString();
    received = BreathWeaponTarget.fromEncodedString(string);
    assertTrue(received.exactlyMatches(teste1));
  }

  @Test
  public void testApproximatelyMatches() throws Exception
  {
    Vec3 testVec1 = new Vec3(1.0, 2.0, 3.0);
    BreathWeaponTarget testd1 = BreathWeaponTarget.targetDirection(testVec1);
    BreathWeaponTarget testl1 = BreathWeaponTarget.targetLocation(testVec1);
    int entityID = 3523;
    BreathWeaponTarget teste1 = BreathWeaponTarget.targetEntityID(entityID);

    assertFalse(testd1.exactlyMatches(testl1));
    assertFalse(testd1.approximatelyMatches(testl1));

    assertFalse(teste1.exactlyMatches(testl1));
    assertFalse(teste1.approximatelyMatches(testl1));

    assertFalse(testd1.exactlyMatches(testl1));
    assertFalse(testd1.approximatelyMatches(testl1));

    Vec3 testVec2 = new Vec3(3.0, 2.0, 1.0);
    BreathWeaponTarget testd2 = BreathWeaponTarget.targetDirection(testVec2);
    BreathWeaponTarget testl2 = BreathWeaponTarget.targetLocation(testVec2);
    BreathWeaponTarget teste2 = BreathWeaponTarget.targetEntityID(entityID + 1);
    assertFalse(testd1.exactlyMatches(testd2));
    assertFalse(testd1.approximatelyMatches(testd2));

    assertFalse(testl1.exactlyMatches(testl2));
    assertFalse(testl1.approximatelyMatches(testl2));

    assertFalse(teste1.exactlyMatches(teste2));
    assertFalse(teste1.approximatelyMatches(teste2));

    Vec3 testVec3 = new Vec3(1.0, 2.0, 3.0);
    BreathWeaponTarget testd3 = BreathWeaponTarget.targetDirection(testVec3);
    BreathWeaponTarget testl3 = BreathWeaponTarget.targetLocation(testVec3);
    BreathWeaponTarget teste3 = BreathWeaponTarget.targetEntityID(entityID);
    assertTrue(testd1.exactlyMatches(testd3));
    assertTrue(testd1.approximatelyMatches(testd3));

    assertTrue(testl1.exactlyMatches(testl3));
    assertTrue(testl1.approximatelyMatches(testl3));

    assertTrue(teste1.exactlyMatches(teste3));
    assertTrue(teste1.approximatelyMatches(teste3));

    Vec3 testVec4 = new Vec3(1.01, 2.0, 3.0);
    BreathWeaponTarget testd4 = BreathWeaponTarget.targetDirection(testVec4);
    BreathWeaponTarget testl4 = BreathWeaponTarget.targetLocation(testVec4);
    assertFalse(testd1.exactlyMatches(testd4));
    assertTrue(testd1.approximatelyMatches(testd4));

    assertFalse(testl1.exactlyMatches(testl4));
    assertTrue(testl1.approximatelyMatches(testl4));
  }
}