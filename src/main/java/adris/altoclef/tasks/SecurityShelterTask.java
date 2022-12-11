package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;

public class SecurityShelterTask extends Task {
    private boolean finished;
    @Override
    protected void onStart(AltoClef mod) {
        finished = false;
    }

    private static List<BlockPos> getAround(final AltoClef mod) {
        final BlockPos playerPos = mod.getPlayer().getBlockPos().up();
        final List<BlockPos> around = new LinkedList<>();
        if (mod.getWorld().getBlockState(playerPos.north()).isAir()) around.add(playerPos.north());
        if (mod.getWorld().getBlockState(playerPos.west()).isAir()) around.add(playerPos.west());
        if (mod.getWorld().getBlockState(playerPos.east()).isAir()) around.add(playerPos.east());
        if (mod.getWorld().getBlockState(playerPos.south()).isAir()) around.add(playerPos.south());
        if (mod.getWorld().getBlockState(playerPos.up()).isAir()) around.add(playerPos.up());
        if (mod.getWorld().getBlockState(playerPos.north().down()).isAir()) around.add(playerPos.north().down());
        if (mod.getWorld().getBlockState(playerPos.west().down()).isAir()) around.add(playerPos.west().down());
        if (mod.getWorld().getBlockState(playerPos.east().down()).isAir()) around.add(playerPos.east().down());
        if (mod.getWorld().getBlockState(playerPos.south().down()).isAir()) around.add(playerPos.south().down());
        return around;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (mod.getItemStorage().getBlockTypes().size() < 1) {
            finished = true;
            return null;
        }

        final List<BlockPos> around = getAround(mod);
        finished = true;
        for (final BlockPos pos : around) {
            final BlockState state = mod.getWorld().getBlockState(pos);
            if (state.isAir()) {
                finished = false;
                BasicDefenseManager.fill(mod, pos);
            }
        }
        return null;
    }

    public static int minimumBlockCount(final AltoClef mod) {
        final BlockPos playerPos = mod.getPlayer().getBlockPos().up();
        int count = 0;
        if (mod.getWorld().getBlockState(playerPos.north()).isAir()) count++;
        if (mod.getWorld().getBlockState(playerPos.west()).isAir()) count++;
        if (mod.getWorld().getBlockState(playerPos.east()).isAir()) count++;
        if (mod.getWorld().getBlockState(playerPos.south()).isAir()) count++;
        if (mod.getWorld().getBlockState(playerPos.up()).isAir()) count++;
        return count;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        finished = true;
    }

    @Override
    protected boolean isEqual(Task other) {
        return this instanceof SecurityShelterTask;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished;
    }

    @Override
    protected String toDebugString() {
        return this.getClass().getCanonicalName();
    }
}
