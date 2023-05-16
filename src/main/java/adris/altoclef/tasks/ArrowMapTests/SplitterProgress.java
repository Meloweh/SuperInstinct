/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

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
final class SplittedProgress {
    private double tick, currVel, currPos;

    public SplittedProgress(final double tick, final double currVel, final double currPos) {
        this.tick = tick; this.currVel = currVel; this.currPos = currPos;
    }

    public SplittedProgress() {
    }

    public double getCurrPos() {
        return currPos;
    }

    public double getCurrVel() {
        return currVel;
    }

    public double getTick() {
        return tick;
    }

    public void setCurrPos(double currPos) {
        this.currPos = currPos;
    }

    public void setCurrVel(double currVel) {
        this.currVel = currVel;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }
}