package adris.altoclef.tasks.defense.chess;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.defense.TPAura;
import adris.altoclef.util.helpers.BlockPosHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class Queen {
    public static Optional<BlockPos> nextJump(final AltoClef mod, final boolean floorRequired) {
        final Stack<Direction> stack = new Stack<>();
        stack.push(Direction.NORTH);
        stack.push(Direction.EAST);
        stack.push(Direction.SOUTH);
        stack.push(Direction.WEST);
        Collections.shuffle(stack);
        final boolean aboveHeadBlocked = !mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().up().up()).getBlock().equals(Blocks.AIR);
        while (!stack.empty()) {
            final HorizontalWing wing = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), stack.pop(), aboveHeadBlocked, floorRequired);
            if (wing.getFounding().isPresent()) {
                return wing.getFounding();
            }
        }
        /*final HorizontalWing north = new HorizontalWing(mod.getWorld(), mod.getPlayer().getBlockPos(), Direction.NORTH, aboveHeadBlocked);
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
        }*/
        return Optional.empty();
    }
    public static Optional<BlockPos> nextJump(final AltoClef mod) {
        return nextJump(mod, false);
    }

    public static boolean attemptJump(final AltoClef mod, final boolean floorRequired) {
        Optional<BlockPos> pos = Queen.nextJump(mod, floorRequired);
        if (pos.isPresent()) {
            System.out.println("candidate at " + BlockPosHelper.toVec3dCenter(pos.get()).toString());
            TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pos.get()));
            return true;
        }
        System.out.println("pos empty");
        return false;
    }

    /**
     * Floored.
     */
    public static boolean attemptJump(final AltoClef mod) {
        return attemptJump(mod, true);
    }
}
