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

import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class SubCommand implements ICommandProcessor {
    
    protected final CommandDragon parent;
    
    public SubCommand(CommandDragon parent) {
        this.parent = parent;
    }
    
    @Override
    public abstract void processCommand(ICommandSender sender, String[] args) throws CommandException;
    
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }
}
