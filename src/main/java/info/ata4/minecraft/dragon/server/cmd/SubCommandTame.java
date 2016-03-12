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
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubCommandTame extends SubCommand {

    public SubCommandTame(CommandDragon parent) {
        super(parent);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            parent.applyModifier(sender, dragon -> dragon.tamedFor(player, true), parent.isGlobal(args));
        } else {
            // console can't tame dragons
            throw new CommandException("commands.dragon.canttame");
        }
    }
    
}
