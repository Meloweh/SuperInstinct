/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

class SlipperinessSpot {
    final BlockPos pos;
    final Block block;

    public SlipperinessSpot(final BlockPos pos, final Block block) {
        this.pos = pos;
        this.block = block;
    }

    public Float getSlipperiness() {
        return block.getSlipperiness();
    }

    public boolean isAir() {
        return Double.compare(getSlipperiness(), 1D) >= 0;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SlipperinessSpot)) return false;
        return getSlipperiness().equals(((SlipperinessSpot)obj).getSlipperiness()) && ((SlipperinessSpot)obj).isAir() == isAir();
    }

    @Override
    public String toString() {
        return "{ Slipperiness: " + getSlipperiness() + "; isAir: " + isAir() + "; pos" + getPos().toString() + " }";
    }
}