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
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DragonMountsSoundEvents {
    
    public static final SoundEvent ENTITY_DRAGON_MOUNT_STEP = registerSound("mob.enderdragon.step");
    public static final SoundEvent ENTITY_DRAGON_MOUNT_BREATHE = registerSound("mob.enderdragon.breathe");
    public static final SoundEvent ENTITY_DRAGON_MOUNT_DEATH = registerSound("mob.enderdragon.death");

    private static SoundEvent registerSound(String soundName) {
        ResourceLocation soundID = new ResourceLocation(DragonMounts.AID, soundName);
        return GameRegistry.register(new SoundEvent(soundID).setRegistryName(soundID));
    }

    private DragonMountsSoundEvents() {
    }
}
