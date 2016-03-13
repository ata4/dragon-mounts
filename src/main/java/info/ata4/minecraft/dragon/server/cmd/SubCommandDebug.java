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

import info.ata4.minecraft.dragon.client.gui.GuiDragonDebug;
import info.ata4.minecraft.dragon.server.entity.helper.EnumDragonLifeStage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubCommandDebug extends SubCommand {
    
    private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();

    public SubCommandDebug(CommandDragon parent) {
        super(parent);
        
        subCommands.put("toItem", new SubCommandSimple(parent, dragon -> {
            dragon.getLifeStageHelper().transformToEgg();
        }));
        
        subCommands.put("dumpNBT", new SubCommandSimple(parent, dragon -> {
            File dumpFile = new File(Minecraft.getMinecraft().mcDataDir,
                    String.format("dragon_%08x.nbt", dragon.getEntityId()));

            try {
                NBTTagCompound nbt = dragon.serializeNBT();
                CompressedStreamTools.write(nbt, dumpFile);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }));
        
        subCommands.put("toggleOverlay", new SubCommandSimple(parent, (sender, args) -> {
            GuiDragonDebug.enabled = !GuiDragonDebug.enabled;
        }));
        
        subCommands.put("testAge", new SubCommandSimple(parent, dragon -> {
            dragon.getLifeStageHelper().setTicksSinceCreation(18000);
        }));
        
        subCommands.put("testName", new SubCommandSimple(parent, dragon -> {
            dragon.setCustomNameTag("Puff");
        }));
        
        subCommands.put("testSaddle", new SubCommandSimple(parent, dragon -> {
            dragon.setSaddled(true);
        }));
        
        subCommands.put("kill", new SubCommandSimple(parent, dragon -> {
            dragon.setHealth(0);
        }));
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String cmd = args[1];
        
        if (!subCommands.containsKey(cmd)) {
            throw new WrongUsageException(parent.getCommandUsage(sender));
        }
        
        subCommands.get(cmd).processCommand(sender, args);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return CommandBase.getListOfStringsMatchingLastWord(args, subCommands.keySet());
    }
    
}
