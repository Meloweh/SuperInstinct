/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */
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