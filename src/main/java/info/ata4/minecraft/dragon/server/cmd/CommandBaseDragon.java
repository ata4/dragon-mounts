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

import net.minecraft.command.CommandBase;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class CommandBaseDragon extends CommandBase implements IDragonModifier  {
    
    private final String name;

    public CommandBaseDragon(String name) {
        this.name = name;
    }
    
    @Override
    public String getCommandName() {
        return name;
    }
}
