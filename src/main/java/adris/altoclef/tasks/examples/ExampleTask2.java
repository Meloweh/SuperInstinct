package adris.altoclef.tasks.examples;


import adris.altoclef.AltoClef;
import adris.altoclef.tasks.GetToBlockTask;
import adris.altoclef.tasks.misc.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;


public class ExampleTask2 extends Task {

    private BlockPos _target;

    @Override
    public boolean isFinished(AltoClef mod) {
        if (_target != null) {
            return mod.getPlayer().getBlockPos().equals(_target);
        }
        return super.isFinished(mod);
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(Blocks.OAK_LOG);

        // Extra credit: Bot will NOT damage trees.
        mod.getConfigState().push();
        mod.getConfigState().avoidBlockBreaking(blockPos -> {
            BlockState s = mod.getWorld().getBlockState(blockPos);
            return s.getBlock() == Blocks.OAK_LEAVES || s.getBlock() == Blocks.OAK_LOG;
        });
    }

    @Override
    protected Task onTick(AltoClef mod) {

        /*
         * Find a tree
         * Go to its top (above the last leaf block)
         *
         * Locate the nearest log
         * Stand on top of its last leaf
         */

        if (_target != null) {
            return new GetToBlockTask(_target);
        }

        if (mod.getBlockTracker().anyFound(Blocks.OAK_LOG)) {
            BlockPos nearest = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.OAK_LOG);
            // Figure out leaves
            BlockPos check = new BlockPos(nearest);
            while (mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LOG ||
                   mod.getWorld().getBlockState(check).getBlock() == Blocks.OAK_LEAVES) {
                check = check.up();
            }
            _target = check;
            return null;
        }

        return new TimeoutWanderTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.OAK_LOG);
        mod.getConfigState().pop();
    }

    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof ExampleTask2;
    }

    @Override
    protected String toDebugString() {
        return "Standing on a tree";
    }
}
