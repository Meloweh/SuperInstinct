/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import java.util.Optional;

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
public class NSLSplitter {
    //put q and m and lastProgressTick and v0 in a list with key = lastProgressTick
    private final float q;
    private final double m, localStartTick, startVel, localStartPos, localEndPos;
    private final boolean middle;
    private Optional<Double> optLocalEndTick;

    public NSLSplitter(final float q, final double m, final double localStartTick, final Optional<Double> optLocalEndTick, final double startVel, final double localStartPos, final double localEndPos, final boolean middle) {
        this.q = q;
        this.m = m;
        this.localStartTick = localStartTick;
        this.optLocalEndTick = optLocalEndTick;
        this.startVel = startVel;
        this.localStartPos = localStartPos;
        this.localEndPos = localEndPos;
        this.middle = middle;
    }

    public double getLocalEndTick() {
        if (optLocalEndTick.isEmpty()) throw new IllegalStateException("me dummy doing ugly calculations wrong, sorry");
        return optLocalEndTick.get();
    }

    public void setLocalEndTick(final double tick) {
        this.optLocalEndTick = Optional.of(tick);
    }

    public double getLocalStartTick() {
        return localStartTick;
    }

    public double getStartVel() {
        return startVel;
    }

    public float getQ() {
        return q;
    }

    public double getM() {
        return m;
    }

    public double getLocalStartPos() {
        return localStartPos;
    }

    public double getLocalEndPos() {
        return localEndPos;
    }

    public boolean isMiddle() {
        return middle;
    }

    @Override
    public String toString() {
        return "Min t: " + getLocalStartTick() + " Max t: " + getLocalEndTick();
    }
}
