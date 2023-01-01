package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;

public class DefenseTower {
    private static List<BlockPos> getAround(final AltoClef mod) {
        final BlockPos playerPos = mod.getPlayer().getBlockPos().up();
        final List<BlockPos> around = new LinkedList<>();
        //if (mod.getWorld().getBlockState(playerPos.north()).isAir()) around.add(playerPos.north());
        if (mod.getWorld().getBlockState(playerPos.north().east()).isAir()) around.add(playerPos.north().east());
        if (mod.getWorld().getBlockState(playerPos.east()).isAir()) around.add(playerPos.east());
        if (mod.getWorld().getBlockState(playerPos.east().south()).isAir()) around.add(playerPos.east().south());
        if (mod.getWorld().getBlockState(playerPos.south()).isAir()) around.add(playerPos.south());
        if (mod.getWorld().getBlockState(playerPos.south().west()).isAir()) around.add(playerPos.south().west());
        if (mod.getWorld().getBlockState(playerPos.west()).isAir()) around.add(playerPos.west());
        if (mod.getWorld().getBlockState(playerPos.west().north()).isAir()) around.add(playerPos.west().north());

        if (mod.getWorld().getBlockState(playerPos.up()).isAir()) around.add(playerPos.up());
        if (mod.getWorld().getBlockState(playerPos.up().north()).isAir()) around.add(playerPos.up().north());
        //if (mod.getWorld().getBlockState(playerPos.up().north().east()).isAir()) around.add(playerPos.up().north().east());
        if (mod.getWorld().getBlockState(playerPos.up().east()).isAir()) around.add(playerPos.up().east());
        //if (mod.getWorld().getBlockState(playerPos.up().east().south()).isAir()) around.add(playerPos.up().east().south());
        if (mod.getWorld().getBlockState(playerPos.up().south()).isAir()) around.add(playerPos.up().south());
        //if (mod.getWorld().getBlockState(playerPos.up().south().west()).isAir()) around.add(playerPos.up().south().west());
        if (mod.getWorld().getBlockState(playerPos.up().west()).isAir()) around.add(playerPos.up().west());
        //if (mod.getWorld().getBlockState(playerPos.up().west().north()).isAir()) around.add(playerPos.up().west().north());

        if (mod.getWorld().getBlockState(playerPos.north().down()).isAir()) around.add(playerPos.north().down());
        //if (mod.getWorld().getBlockState(playerPos.north().down().east()).isAir()) around.add(playerPos.north().down().east());
        if (mod.getWorld().getBlockState(playerPos.east().down()).isAir()) around.add(playerPos.east().down());
        //if (mod.getWorld().getBlockState(playerPos.east().down().south()).isAir()) around.add(playerPos.east().down().south());
        if (mod.getWorld().getBlockState(playerPos.south().down()).isAir()) around.add(playerPos.south().down());
        //if (mod.getWorld().getBlockState(playerPos.south().down().west()).isAir()) around.add(playerPos.south().down().west());
        if (mod.getWorld().getBlockState(playerPos.west().down()).isAir()) around.add(playerPos.west().down());
        //if (mod.getWorld().getBlockState(playerPos.west().down().north()).isAir()) around.add(playerPos.west().down().north());
        return around;
    }

    public boolean attemptTower(AltoClef mod) {
        final List<BlockPos> around = getAround(mod);
        if (around.size() < 1 || mod.getItemStorage().getBlockCount() < around.size()) {
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
}
