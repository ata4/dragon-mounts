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
import java.util.function.BiConsumer;
import net.minecraft.command.*;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandDragon extends CommandBaseNested implements IDragonModifier {
    
    public CommandDragon() {
        BiConsumer<EntityTameableDragon, EnumDragonBreed> breedConsumer =
            (dragon, enumValue) -> dragon.setBreedType(enumValue);
        addCommand(new CommandDragonEnumSetter("breed", EnumDragonBreed.class, breedConsumer));
        
        BiConsumer<EntityTameableDragon, EnumDragonLifeStage> lifeStageConsumer =
            (dragon, enumValue) -> dragon.getLifeStageHelper().setLifeStage(enumValue);
        addCommand(new CommandDragonEnumSetter("stage", EnumDragonLifeStage.class, lifeStageConsumer));
        
        addCommand(new CommandDragonTame());

        if (DragonMounts.instance.getConfig().isDebug()) {
            addCommand(new CommandDragonDebug());
        }
    }

    @Override
    public String getCommandName() {
        return "dragon";
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("/%s [global]", super.getCommandUsage(sender));
    }
    
    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }
}
