/*
 ** 2012 August 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.cmd;

import info.ata4.minecraft.dragon.DragonMounts;
import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import info.ata4.minecraft.dragon.server.entity.breeds.EnumDragonBreed;
import info.ata4.minecraft.dragon.server.entity.helper.EnumDragonLifeStage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandDragon extends CommandBase {
    
    private static final double ENTITY_RANGE = 64;
    
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
    
    public CommandDragon() {
        BiConsumer<EntityTameableDragon, EnumDragonBreed> breedConsumer =
            (dragon, enumValue) -> dragon.setBreedType(enumValue);
        subCommands.put("breed", new SubCommandEnumSetter(this,
            EnumDragonBreed.class, breedConsumer));
        
        BiConsumer<EntityTameableDragon, EnumDragonLifeStage> lifeStageConsumer =
            (dragon, enumValue) -> dragon.getLifeStageHelper().setLifeStage(enumValue);
        subCommands.put("stage", new SubCommandEnumSetter(this,
            EnumDragonLifeStage.class, lifeStageConsumer));
        
        subCommands.put("tame", new SubCommandSimple(this, (sender, args) -> {
            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) sender;
                applyModifier(sender, args, dragon -> dragon.tamedFor(player, true));
            } else {
                // console can't tame dragons
                throw new CommandException("commands.dragon.canttame");
            }
        }));
        
        if (DragonMounts.instance.getConfig().isDebug()) {
            subCommands.put("debug", new SubCommandDebug(this));
        }
    }

    @Override
    public String getCommandName() {
        return "dragon";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        String commands = StringUtils.join(subCommands.keySet(), '|');
        return String.format("/dragon <%s> [global]", commands);
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, subCommands.keySet());
        } else if (args.length == 2) {
            String cmd = args[0];
            if (subCommands.containsKey(cmd)) {
                return subCommands.get(cmd).addTabCompletionOptions(sender, args, pos);
            }
        }
        
        return null;
    }
    
    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1 || args[0].isEmpty()) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        
        String cmd = args[0].toLowerCase();
        
        if (!subCommands.containsKey(cmd)) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        
        subCommands.get(cmd).processCommand(sender, args);
    }
    
    void applyModifier(ICommandSender sender, String[] args, Consumer<EntityTameableDragon> modifier) throws CommandException {
        boolean global = args[args.length - 1].equalsIgnoreCase("global");
        
        if (!global && sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            
            AxisAlignedBB aabb = player.getEntityBoundingBox()
                .expand(ENTITY_RANGE, ENTITY_RANGE, ENTITY_RANGE);
            
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
            MinecraftServer server = MinecraftServer.getServer();
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
