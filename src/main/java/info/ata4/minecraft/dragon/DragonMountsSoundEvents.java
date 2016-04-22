/*
** 2016 April 22
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsSoundEvents {
    
    public static final SoundEvent entity_dragon_mount_step = getRegisteredSoundEvent("mob.enderdragon.step");
    public static final SoundEvent entity_dragon_mount_breathe = getRegisteredSoundEvent("mob.enderdragon.breathe");
    public static final SoundEvent entity_dragon_mount_death = getRegisteredSoundEvent("mob.enderdragon.step");

    private static SoundEvent getRegisteredSoundEvent(String id) {
        SoundEvent soundevent = SoundEvent.soundEventRegistry.getObject(new ResourceLocation(DragonMounts.AID, id));

        if (soundevent == null) {
            throw new IllegalStateException("Invalid sound requested: " + id);
        } else {
            return soundevent;
        }
    }

    private DragonMountsSoundEvents() {
    }
}
