package adris.altoclef.tasks.defense.chess;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Wing2D {
    private final Wing1D lower, higher;
    private final boolean elevated;
    private final boolean failed;
    private final BlockPos feet;

    public Wing2D(final World world, final BlockPos feet, final boolean aboveHeadBlocked) {
        this.feet = feet;
        final Wing1D lower = new Wing1D(world, feet);
        this.elevated = lower.hasFailed();
        final BlockPos fixedFeet = isElevated() ? feet.up() : feet;
        this.lower = isElevated() ? new Wing1D(world, fixedFeet) : lower;
        this.higher = new Wing1D(world, fixedFeet.up());
        this.failed = this.lower.hasFailed() || this.higher.hasFailed() || (aboveHeadBlocked && this.elevated);
    }
    public boolean isElevated() {
        return this.elevated;
    }
    public boolean hasFailed() {
        return this.failed;
    }
    public BlockState getHigherState() {
        return this.higher.getState();
    }
    public BlockState getLowerState() {
        return this.lower.getState();
    }
    public BlockPos getFeet() {
        return feet;
    }
}
