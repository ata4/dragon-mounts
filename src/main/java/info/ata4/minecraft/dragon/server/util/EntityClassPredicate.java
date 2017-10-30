/*
 ** 2015 March 08
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.util;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class EntityClassPredicate implements Predicate<Entity> {

	private final Set<Class> classSet;

	public EntityClassPredicate(Class<? extends Entity>... c) {
		classSet = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(c)));
	}

	@Override
	public boolean apply(Entity input) {
		Class c = input.getClass();

		do {
			if (classSet.contains(c)) {
				return true;
			}
		} while ((c = c.getSuperclass()) != null);

		return false;
	}
}
