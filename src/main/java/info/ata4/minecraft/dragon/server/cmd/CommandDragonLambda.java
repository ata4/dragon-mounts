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
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandDragonLambda extends CommandBaseDragon {
    
    private final Optional<Consumer<EntityTameableDragon>> consumer;
    private final Optional<ICommandProcessor> processor;

    public CommandDragonLambda(String name, Consumer<EntityTameableDragon> consumer) {
        super(name);
        this.consumer = Optional.of(consumer);
        this.processor = Optional.empty();
    }
    
    public CommandDragonLambda(String name, ICommandProcessor processor) {
        super(name);
        this.consumer = Optional.empty();
        this.processor = Optional.of(processor);
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (processor.isPresent()) {
            processor.get().execute(server, sender, args);
        }
        
        if (consumer.isPresent()) {
            applyModifier(server, sender, consumer.get());
        }
    }
    
}
