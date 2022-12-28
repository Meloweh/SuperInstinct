package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;

public class SecurityShelterTask {

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

    public static boolean attemptShelter(AltoClef mod) {
        final List<BlockPos> around = getAround(mod);
        if (/*around.size() < 1 || */mod.getItemStorage().getBlockCount() < around.size()) {
            return false;
        }
        for (final BlockPos pos : around) {
            final BlockState state = mod.getWorld().getBlockState(pos);
            if (state.isAir()) {
                BasicDefenseManager.fill(mod, pos);
            }
        }
        return true;
    }
    public static boolean canAttemptShelter(final AltoClef mod) {
        return mod.getItemStorage().getBlockTypes().size() >= getAround(mod).size();
    }
    public static boolean isFilled(final AltoClef mod) {
        return getAround(mod).size() == 0;
    }
}
