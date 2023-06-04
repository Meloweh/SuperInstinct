package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ItemStorageTracker;
import adris.altoclef.util.CubeBounds;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.schematic.ISchematic;
import baritone.process.BuilderProcess;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.*;
import java.util.List;

public class SchematicBuildTask extends Task {
    private boolean finished;
    private BuilderProcess builder;
    private String schematicFileName;
    private BlockPos startPos;
    private int allowedResourceStackCount;
    private Map<BlockState, Integer> needToSource;
    private boolean gotBackup;
    private boolean needBackup;
    private Vec3i schemSize;
    private CubeBounds bounds;
    private Map<BlockState, Integer> missing;
    private boolean sourced;
    private boolean pause;
    private boolean addedAvoidance;
    private BlockPos _currentTry = null;
    private boolean clearRunning = false;
    private String name;
    private ISchematic schematic;
    private static final int FOOD_UNITS = 80;
    private static final int MIN_FOOD_UNITS = 10;
    private final TimerGame _clickTimer = new TimerGame(120);
    private final MovementProgressChecker _moveChecker = new MovementProgressChecker(4, 0.1, 4, 0.01);
    private Task walkAroundTask;

    public SchematicBuildTask(final String schematicFileName) {
        this(schematicFileName, new BlockPos(MinecraftClient.getInstance().player.getPos()));
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos) {
        this(schematicFileName, startPos, 3);
    }

    public SchematicBuildTask(final String schematicFileName, final BlockPos startPos, final int allowedResourceStackCount) {
        this();
        this.schematicFileName = schematicFileName;
        this.startPos = startPos;
        this.allowedResourceStackCount = allowedResourceStackCount;
    }

    public SchematicBuildTask() {
        this.needToSource = new HashMap<>();
        this.gotBackup = false;
        this.needBackup = false;
        this.sourced = false;
        this.addedAvoidance = false;
    }

    public SchematicBuildTask(String name, ISchematic schematic, final BlockPos startPos) {
        this();
        this.name = name;
        this.schematic = schematic;
        this.startPos = startPos;
    }

    @Override
    protected void onStart(AltoClef mod) {
        this.finished = false;

        if (builder == null) {
            builder = mod.getClientBaritone().getBuilderProcess();
        }

        final File file = new File("schematics/" + schematicFileName);
        if (!file.exists()) {
            Debug.logMessage("Could not locate schematic file. Terminating...");
            this.finished = true;
            return;
        }

        builder.clearState();

        if (this.schematic == null) {
            builder.build(schematicFileName, startPos, true); //TODO: I think there should be a state queue in baritone
        } else {
            builder.build(this.name, this.schematic, startPos);
        }

        if (schemSize == null) {
            this.schemSize = builder.getSchemSize();
        }

        if (schemSize != null && builder.isFromAltoclef() && !this.addedAvoidance) {
            this.bounds = new CubeBounds(mod.getPlayer().getBlockPos(), this.schemSize.getX(), this.schemSize.getY(), this.schemSize.getZ());
            this.addedAvoidance = true;
            mod.addToAvoidanceFile(this.bounds);
            mod.reloadAvoidanceFile();
            mod.unsetAvoidanceOf(this.bounds);
        }
        this.pause = false;

        _moveChecker.reset();
        _clickTimer.reset();
    }

    private List<BlockState> getTodoList(final AltoClef mod, final Map<BlockState, Integer> missing) {
        final ItemStorageTracker inventory = mod.getItemStorage();
        int finishedStacks = 0;
        final List<BlockState> listOfFinished = new ArrayList<>();

        for (final BlockState state : missing.keySet()) {
            final Item item = state.getBlock().asItem();
            final int count = inventory.getItemCount(item);
            final int maxCount = item.getMaxCount();

            if (finishedStacks < this.allowedResourceStackCount) {
                listOfFinished.add(state);
                if (count >= missing.get(state)) {
                    finishedStacks++;
                    listOfFinished.remove(state);
                } else if (count >= maxCount) {
                    finishedStacks += Math.ceil(count / maxCount);

                    if (finishedStacks >= this.allowedResourceStackCount) {
                        listOfFinished.remove(state);
                    }
                }
            }
        }

        return listOfFinished;
    }

    private void overrideMissing() {
        this.missing = builder.getMissing();
    }

    private Map<BlockState, Integer> getMissing() {
        if (this.missing == null) {
            overrideMissing();
        }
        return this.missing;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (clearRunning && builder.isActive()) {
            return null;
        }

        clearRunning = false;
        overrideMissing();
        this.sourced = false;

        if (getMissing() != null && !getMissing().isEmpty() && (builder.isPaused() || !builder.isFromAltoclef()) || !builder.isActive()) {
            if (!mod.inAvoidance(this.bounds)) {
                mod.setAvoidanceOf(this.bounds);
            }
            //if (mod.getFoodChain().hasFood() < MIN_FOOD_UNITS) {
            /*if (!mod.getFoodChain().hasFood()) {
                return new CollectFoodTask(FOOD_UNITS);
            }*/
            for (final BlockState state : getTodoList(mod, missing)) {
                return TaskCatalogue.getItemTask(state.getBlock().asItem(), missing.get(state));
            }
            this.sourced = true;
        }

        mod.unsetAvoidanceOf(this.bounds);

        if (this.sourced == true && !builder.isActive()) {
            if (mod.inAvoidance(this.bounds)) {
                mod.unsetAvoidanceOf(this.bounds);
            }

            builder.resume();
            //Debug.logMessage("Resuming build process...");
            //System.out.println("Resuming builder...");
        }

        if (_moveChecker.check(mod)) {
            _clickTimer.reset();
        }
        if (walkAroundTask == null) {
            if (_clickTimer.elapsed()) {
                Debug.logMessage("Timer elapsed.");
                walkAroundTask = new RandomRadiusGoalTask(mod.getPlayer().getBlockPos(), 5d).next(mod.getPlayer().getBlockPos());
            }
        } else if (!walkAroundTask.isFinished(mod)) {
            return walkAroundTask;
        } else {
            walkAroundTask = null;
            builder.popStack();
            _clickTimer.reset();
            _moveChecker.reset();
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        builder.pause();
        this.pause = true;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SchematicBuildTask;
    }

    @Override
    protected String toDebugString() {
        return "SchematicBuilderTask";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (builder != null && builder.isFromAltoclefFinished() || this.finished == true) {
            mod.loadAvoidanceFile();
            return true;
        }
        return false;
    }
}
