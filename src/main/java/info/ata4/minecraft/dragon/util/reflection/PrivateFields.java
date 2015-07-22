/*
 ** 2012 July 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.util.reflection;

/**
 * Static collection of private field names that are accessed via ReflectionHelper.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class PrivateFields {
    
    public static final String[] DATAWATCHER_WATCHEDOBJECTS = new String[] {"watchedObjects", "field_75695_b"};
    public static final String[] ENTITYAITASKS_EXECUTINGTASKENTRIES = new String[] {"executingTaskEntries", "field_75780_b"};
    public static final String[] ENTITYAITASKS_TICKRATE = new String[] {"tickRate", "field_75779_e"};
    public static final String[] ENTITYLIVING_BODYHELPER = new String[] {"bodyHelper", "field_70762_j"};
    public static final String[] ENTITYLIVING_LOOKHELPER = new String[] {"lookHelper", "field_70749_g"};
    public static final String[] ENTITYRENDERER_THIRDPERSONDISTANCE = new String[] {"thirdPersonDistance", "field_78490_B"};
    public static final String[] GUIMAINMENU_SPLASHTEXT = new String[] {"splashText", "field_73975_c"};

    private PrivateFields() {
    }
}
