/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Direction;

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
public final class TraceInfo {
    private final Interval<Long> interval;
    private final Direction hitSide;
    private final BodyHitDetail bodyHitDetail;

    public TraceInfo(final Interval<Long> interval, BodyHitDetail bodyHitDetail) {
        this.interval = interval;
        this.hitSide = interval.getEnteringPlaneName();
        this.bodyHitDetail = bodyHitDetail;
    }
    public long getPiercingEntryHitTick() {
        return interval.getMin();
    }

    public long getPiercingExitHitTick() {
        return interval.getMax();
    }

    public Direction getHitSide() {
        return hitSide;
    }

    public BodyHitDetail getBodyHitDetail() {return bodyHitDetail;}
}
