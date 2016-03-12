/*
** 2016 March 11
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.dragon.server.cmd;

import info.ata4.minecraft.dragon.server.entity.EntityTameableDragon;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubCommandSimple extends SubCommand {
    
    private final Optional<Consumer<EntityTameableDragon>> consumer;
    private final Optional<ICommandProcessor> processor;

    public SubCommandSimple(CommandDragon parent, Consumer<EntityTameableDragon> consumer) {
        super(parent);
        this.consumer = Optional.of(consumer);
        this.processor = Optional.empty();
    }
    
    public SubCommandSimple(CommandDragon parent, ICommandProcessor processor) {
        super(parent);
        this.consumer = Optional.empty();
        this.processor = Optional.of(processor);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (processor.isPresent()) {
            processor.get().processCommand(sender, args);
        }
        
        if (consumer.isPresent()) {
            parent.applyModifier(sender, consumer.get(), parent.isGlobal(args));
        }
    }
    
}
