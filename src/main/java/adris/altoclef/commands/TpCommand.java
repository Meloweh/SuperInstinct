package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.defense.TPAura;
import adris.altoclef.util.filestream.RoundtripMacroListFile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TpCommand extends Command {
    private static final Double EMPTY = 0d;
    public TpCommand() throws CommandException {
        super("tp", "Attempt tp to coords.",
                new Arg(Double.class, "x", EMPTY, 0, false),
                new Arg(Double.class, "y", EMPTY, 1, false),
                new Arg(Double.class, "z", EMPTY, 2, false)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        final Double x = parser.get(Double.class);
        final Double y = parser.get(Double.class);
        final Double z = parser.get(Double.class);
        Debug.logWarning("tp to " + x + " " + y + " " + z);
        TPAura.tp(mod, new Vec3d(x, y, z));
    }
}
