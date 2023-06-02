/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

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
public final class Interval<T> {
    private final T min;
    private final T max;
    private final Direction enteringPlaneName;

    public Interval(final T min, final T max, final Direction enteringPlaneName) {
        this.min = min;
        this.max = max;
        this.enteringPlaneName = enteringPlaneName;
    }

    public Direction getEnteringPlaneName() {
        return this.enteringPlaneName;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    /*public Interval<Long> toLong() {
        return new Interval<>((long) min, (long) max, enteringPlaneName);
    }*/

    @Override
    public String toString() {
        return "Min: " + getMin() + " Max: " + getMax() + "EnteringPlane: " + getEnteringPlaneName();
    }
}
