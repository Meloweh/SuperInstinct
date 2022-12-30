package adris.altoclef.tasks.defense.chess;

import adris.altoclef.AltoClef;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Horse {
    public static Optional<BlockPos> nextJump(final AltoClef mod) {
        final boolean aboveHeadBlocked = !mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().up().up()).getBlock().equals(Blocks.AIR);
        final HorizontalWing north = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), Direction.NORTH, aboveHeadBlocked);
        if (north.getFounding().isPresent()) {
            return north.getFounding();
        }
        final HorizontalWing east = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), Direction.EAST, aboveHeadBlocked);
        if (east.getFounding().isPresent()) {
            return east.getFounding();
        }
        final HorizontalWing south = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), Direction.SOUTH, aboveHeadBlocked);
        if (south.getFounding().isPresent()) {
            return south.getFounding();
        }
        final HorizontalWing west = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), Direction.WEST, aboveHeadBlocked);
        if (west.getFounding().isPresent()) {
            return west.getFounding();
        }
        return Optional.empty();
    }
}
