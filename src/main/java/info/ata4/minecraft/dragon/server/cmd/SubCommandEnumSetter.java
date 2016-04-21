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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.EnumUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubCommandEnumSetter<E extends Enum<E>> extends SubCommand {
    
    private final Class<E> enumClass;
    private final BiConsumer<EntityTameableDragon, E> enumConsumer;
    
    public SubCommandEnumSetter(CommandDragon parent, Class<E> enumClass, BiConsumer<EntityTameableDragon, E> enumConsumer) {
        super(parent);
        this.enumClass = enumClass;
        this.enumConsumer = enumConsumer;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(parent.getCommandUsage(sender));
        }

        String enumName = args[1].toUpperCase();
        E enumValue = EnumUtils.getEnum(enumClass, enumName);
        if (enumValue == null) {
            // default constructor uses "snytax"...
            throw new SyntaxErrorException("commands.generic.syntax");
        }

        parent.applyModifier(sender, args, dragon -> enumConsumer.accept(dragon, enumValue));
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return CommandBase.getListOfStringsMatchingLastWord(args,
            EnumUtils.getEnumList(enumClass).stream()
                .map(e -> e.name().toLowerCase())
                .collect(Collectors.toList())
        );
    }
}
