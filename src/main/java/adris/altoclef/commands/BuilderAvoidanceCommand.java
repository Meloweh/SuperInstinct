package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.util.filestream.AvoidanceFile;

public class BuilderAvoidanceCommand extends Command {
    public BuilderAvoidanceCommand() throws CommandException {
        super("avoidances", "Actions to the builders avoidance list", new Arg(String.class, "flag", "", 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String flag = "";
        try {
            flag = parser.get(String.class);
        } catch (CommandException e) {
            Debug.logError("Cannot parse parameter. Input format: '@build house.schem'");
        }

        switch (flag) {
            case "clear": AvoidanceFile.clear(); break;
            default: Debug.logWarning("Unknown specification.");;
        }
    }
}
