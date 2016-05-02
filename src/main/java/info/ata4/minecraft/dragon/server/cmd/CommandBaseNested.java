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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class CommandBaseNested extends CommandBase {
    
    private final Map<String, CommandBase> commands = new LinkedHashMap<>();
    
    protected final void addCommand(CommandBase cmd) {
        commands.put(cmd.getCommandName().toLowerCase(), cmd);
    }
    
    protected List<String> getCommandNames() {
        return commands.values().stream().map(cmd -> cmd.getCommandName()).collect(Collectors.toList());
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("%s <%s>", getCommandName(), StringUtils.join(getCommandNames(), '|'));
    }
    
    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 0 || args[0].isEmpty()) {
            return getCommandNames();
        }
        
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, getCommandNames());
        }
        
        String cmd = args[0].toLowerCase();
        if (!commands.containsKey(cmd)) {
            return null;
        }
        
        String[] argsSub = Arrays.copyOfRange(args, 1, args.length);
        return commands.get(cmd).getTabCompletionOptions(server, sender, argsSub, pos);
    }
    
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || args[0].isEmpty()) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        
        String cmd = args[0].toLowerCase();
        if (!commands.containsKey(cmd)) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        
        String[] argsSub = Arrays.copyOfRange(args, 1, args.length);
        commands.get(cmd).execute(server, sender, argsSub);
    }
}
