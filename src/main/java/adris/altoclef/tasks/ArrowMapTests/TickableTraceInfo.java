/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.entity.projectile.ArrowEntity;

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
class TickableTraceInfo {
    private final TraceInfo traceInfo;
    private int tick;
    private final ArrowEntity arrow;

    public TickableTraceInfo(final TraceInfo traceInfo, final ArrowEntity arrow) {
        this.traceInfo = traceInfo;
        this.tick = 0;
        this.arrow = arrow;
    }

    public void tick() {
        this.tick++;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public int getTick() {
        return tick;
    }

    public int getRemainingTicks() {
        final int result = (int)this.traceInfo.getPiercingEntryHitTick() - tick;
        return result < 0 ? 0 : result;
    }

    public boolean hasTimeLeft() {
        return getRemainingTicks() > 0;
    }

    public ArrowEntity getArrow() {
        return this.arrow;
    }
}