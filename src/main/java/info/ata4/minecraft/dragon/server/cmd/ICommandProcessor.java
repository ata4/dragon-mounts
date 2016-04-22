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

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@FunctionalInterface
public interface ICommandProcessor {
    
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;
}
