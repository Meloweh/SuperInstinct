package adris.altoclef.tasks.defense.chess;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class VerticalWing {
    /*private final Optional<BlockPos> founding;
    private final boolean failed;
    private final Wing2D wing2D;*/
    private boolean failed;
    private Optional<BlockPos> founding;
    public VerticalWing(final World world, final BlockPos end, final BlockPos start, final boolean floorRequired) {
        //this.wing2D = new Wing2D(world, start, prevLowerAsHigher);
        /*final Optional<VerticalWing> nextUpWing = !end.equals(start) ?
                Optional.of(new VerticalWing(world, end, start.down(), Optional.of(this.wing2D.getLowerState())))
                :
                Optional.empty();
        final boolean nextFailed = nextUpWing.isEmpty() || nextUpWing.get().hasFailed();
        final boolean nextHasFounding = nextFailed ? false : nextUpWing.get().getFounding().isPresent();
        this.failed = nextFailed && this.wing2D.hasFailed();
        this.founding = nextHasFounding ? nextUpWing.get().getFounding() : this.failed ? Optional.empty() : Optional.of(start);*/
        this.failed = true;
        this.founding = Optional.empty();
        Wing1D prev = new Wing1D(world, start);
        for (BlockPos it = start.down(); it.getY() >= end.getY(); it = it.down()) {
            final Wing1D wing1D = new Wing1D(world, it);
            if (!wing1D.hasFailed() && !prev.hasFailed()) {
                boolean resultValid = true;
                if (floorRequired) {
                    resultValid = !world.getBlockState(it.down()).getBlock().equals(Blocks.AIR);
                }
                if (resultValid) {
                    this.failed = false;
                    this.founding = Optional.of(it);
                    break;
                }

            }
            prev = wing1D;
        }
    }
    public Optional<BlockPos> getFounding() {
        return this.founding;
    }
    public boolean hasFailed() {
        return this.failed;
    }
}
