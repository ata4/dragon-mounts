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
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@GameRegistry.ObjectHolder(DragonMounts.ID)
public class DragonMountsSoundEvents {

	@GameRegistry.ObjectHolder("mob.enderdragon.step")
	public static final SoundEvent ENTITY_DRAGON_MOUNT_STEP = createSoundEvent("mob.enderdragon.step");
	@GameRegistry.ObjectHolder("mob.enderdragon.breathe")
	public static final SoundEvent ENTITY_DRAGON_MOUNT_BREATHE = createSoundEvent("mob.enderdragon.breathe");
	@GameRegistry.ObjectHolder("mob.enderdragon.death")
	public static final SoundEvent ENTITY_DRAGON_MOUNT_DEATH = createSoundEvent("mob.enderdragon.death");

	private DragonMountsSoundEvents() {
	}

	private static SoundEvent createSoundEvent(final String soundName) {
		final ResourceLocation soundID = new ResourceLocation(DragonMounts.ID, soundName);
		return new SoundEvent(soundID).setRegistryName(soundID);
	}
}
