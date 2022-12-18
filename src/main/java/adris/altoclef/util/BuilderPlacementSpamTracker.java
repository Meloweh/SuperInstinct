package adris.altoclef.util;

import adris.altoclef.AltoClef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class BuilderPlacementSpamTracker {
    final class BlockData {
        private final BlockPos pos;
        private Block block;
        private int changeCount;
        private final int TOLERANCE = 15;

        public BlockData(final BlockPos pos, final BlockState state) {
            this.pos = pos;
            this.block = state.getBlock();
            this.changeCount = 0;
        }
        public void updateChange(final BlockState state) {
            if (!this.block.equals(state.getBlock())) {
                System.out.println(state.getBlock().toString() + " vs " + block.toString() + " with getChangeCount = " + getChangeCount());
                this.changeCount++;
                this.block = state.getBlock();
            }
        }
        public int getChangeCount() {
            return this.changeCount;
        }
        public Block getPrevBlock() {
            return this.block;
        }
        public BlockPos getPos() {
            return this.pos;
        }
        public boolean toleranceExceeded() {
            return getChangeCount() > TOLERANCE;
        }
    }
    private final int RANGE = 6;
    private List<BlockData> bData = new LinkedList<>();
    //private final int RESET_TIMEOUT = 60;
    //private int timout = 0;
    public void tick(final AltoClef mod) {
        /*timout++;
        if (timout > RESET_TIMEOUT) {
            bData.clear();
            timout = 0;
        }*/
        if (mod.getPlayer() == null || mod.getWorld() == null || !mod.getClientBaritone().getBuilderProcess().isActive() || !mod.getClientBaritone().getBuilderProcess().isFromAltoclef()) {
            return;
        }

        final int halfRange = RANGE / 2;
        final Vec3i halfRangeV = new BlockPos(halfRange, halfRange, halfRange);
        final BlockPos center = mod.getPlayer().getBlockPos();
        final BlockPos startPos = center.subtract(halfRangeV);
        final BlockPos endPos = center.add(halfRangeV);
        final CubeBounds cubeBounds = new CubeBounds(startPos, endPos);

        bData.removeIf(e -> !cubeBounds.inside(e.getPos()));

        for (int x = startPos.getX(); x <= endPos.getX(); x++) {
            for (int y = startPos.getY(); y <= endPos.getY(); y++) {
                for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                    final BlockPos curr = new BlockPos(x, y, z);
                    final BlockState state = mod.getWorld().getBlockState(curr);
                    if (bData.stream().anyMatch(e -> e.getPos().equals(curr))) {
                        final Optional<BlockData> optBDate = bData.stream().filter(e -> e.getPos().equals(curr)).findFirst();
                        if (optBDate.isEmpty()) {
                            throw new IllegalStateException("this is dirty code too");
                        }
                        final BlockData bDate = optBDate.get();

                        if (!mod.getBlockTracker().unreachable(bDate.getPos()) && bDate.toleranceExceeded() && bDate.getPrevBlock().equals(Blocks.AIR) && !state.getBlock().equals(Blocks.AIR)) {
                            System.out.println("getChangeCount(): " + bDate.getChangeCount() + "");
                            mod.getBlockTracker().requestBlockUnreachable(bDate.getPos(), 0);
                            mod.getClientBaritone().getBuilderProcess().assumeValid(bDate.getPos());
                        }

                        bDate.updateChange(state);
                    } else {
                        bData.add(new BlockData(curr, state));
                    }
                }
            }
        }
    }
}
