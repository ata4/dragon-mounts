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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CommandDragonEnumSetter<E extends Enum<E>> extends CommandBaseDragon {
    
    private final Class<E> enumClass;
    private final BiConsumer<EntityTameableDragon, E> enumConsumer;
    
    public CommandDragonEnumSetter(String name, Class<E> enumClass, BiConsumer<EntityTameableDragon, E> enumConsumer) {
        super(name);
        this.enumClass = enumClass;
        this.enumConsumer = enumConsumer;
    }
    
    private List<String> getEnumNames() {
        return EnumUtils.getEnumList(enumClass).stream()
            .map(e -> e.name().toLowerCase())
            .collect(Collectors.toList());
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("%s <%s>", getCommandName(), StringUtils.join(getEnumNames(), '|'));
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        String enumName = args[0].toUpperCase();
        E enumValue = EnumUtils.getEnum(enumClass, enumName);
        if (enumValue == null) {
            // default constructor uses "snytax"...
            throw new SyntaxErrorException("commands.generic.syntax");
        }

        applyModifier(server, sender, dragon -> enumConsumer.accept(dragon, enumValue));
    }
    
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return CommandBase.getListOfStringsMatchingLastWord(args, getEnumNames());
    }
}
