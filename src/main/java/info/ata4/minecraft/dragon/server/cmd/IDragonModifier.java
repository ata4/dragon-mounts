/*
** 2016 April 27
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.cmd;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static net.minecraft.command.CommandBase.getCommandSenderAsPlayer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public interface IDragonModifier {
    
    static final double MODIFIER_RANGE = 64;
    
    default void applyModifier(MinecraftServer server, ICommandSender sender, Consumer<EntityTameableDragon> modifier) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            
            AxisAlignedBB aabb = player.getEntityBoundingBox()
                .expand(MODIFIER_RANGE, MODIFIER_RANGE, MODIFIER_RANGE);
            
            List<EntityTameableDragon> dragons = player.worldObj
                .getEntitiesWithinAABB(EntityTameableDragon.class, aabb);

            // get closest dragon
            Optional<EntityTameableDragon> closestDragon = dragons.stream()
                .max((dragon1, dragon2) -> Float.compare(
                    dragon1.getDistanceToEntity(player),
                    dragon2.getDistanceToEntity(player))
                );

            if (!closestDragon.isPresent()) {
                throw new CommandException("commands.dragon.nodragons");
            }
            
            modifier.accept(closestDragon.get());
        } else {
            // scan all entities on all dimensions
            for (WorldServer worldServer : server.worldServers) {
                // need a copy of all dragon entities before applying modifier,
                // since it could delete from the server entity list during iteration
                List<EntityTameableDragon> dragons = worldServer.loadedEntityList
                    .stream()
                    .filter(entity -> entity instanceof EntityTameableDragon)
                    .map(entity -> (EntityTameableDragon) entity)
                    .collect(Collectors.toList());
                
                dragons.forEach(modifier);
            }
        }
    }
}
