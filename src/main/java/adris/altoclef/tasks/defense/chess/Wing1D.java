package adris.altoclef.tasks.defense.chess;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Wing1D {
    private final BlockPos pos;
    private final BlockState state;
    public Wing1D(final World world, final BlockPos pos) {
        this.pos = pos;
        this.state = world.getBlockState(pos);
    }
    public Wing1D(final BlockPos pos, final BlockState state) {
        this.pos = pos;
        this.state = state;
    }
    public boolean hasFailed() {
        return !state.getBlock().equals(Blocks.AIR);
    }
    public BlockState getState() {
        return this.state;
    }
    public BlockPos getPos() {
        return pos;
    }
}
