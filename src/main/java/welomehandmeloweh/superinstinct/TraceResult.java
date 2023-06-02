/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public final class TraceResult {
    private final Interval<Long> first;
    private final Interval<Long> second;
    private final Interval<Long> third;
    private final TraceInfo firstTraceInfo;
    private final TraceInfo secondTraceInfo;
    private final TraceInfo thirdTraceInfo;
    private final ArrowEntity arrow;

    /*public static TraceResult EXPIRED(final ArrowEntity arrow, final Box playerAABB) {
        return new TraceResult(null, null, arrow, playerAABB);
    }*/

    public TraceResult(final Interval<Long> first, final Interval<Long> second, final ArrowEntity arrow, final Box playerAABB) {
        this(first, second, null, arrow, playerAABB);
    }

    public TraceResult(final Interval<Long> first, final Interval<Long> second, final Interval<Long> third, final ArrowEntity arrow, final Box playerAABB) {
        System.out.println((first == null) && (second == null) && (third == null) ? "TRACERESULT IS NOT SET" : "TRACERESULT SHOULD HAVE ELEMENTS");
        if (arrow == null) throw new IllegalArgumentException("arrow should never be null.");
        this.arrow = arrow;
        this.first = first;
        this.second = second;
        this.third = third;

        if (hasFirstPiercing()) {
            final double hitY = BowArrowIntersectionTracer.tracePosY(arrow.getVelocity().getY(), arrow.getY(), first.getMin());
            final BodyHitDetail bodyHitDetail = Double.compare(hitY, playerAABB.getCenter().getY()) > 0 ? BodyHitDetail.HIGH : BodyHitDetail.LOW;
            this.firstTraceInfo = new TraceInfo(first, bodyHitDetail);
        } else {
            this.firstTraceInfo = null;
        }

        if (hasSecondPiercing()) {
            final double hitY = BowArrowIntersectionTracer.tracePosY(arrow.getVelocity().getY(), arrow.getY(), second.getMin());
            final BodyHitDetail bodyHitDetail = Double.compare(hitY, playerAABB.getCenter().getY()) > 0 ? BodyHitDetail.HIGH : BodyHitDetail.LOW;
            this.secondTraceInfo = new TraceInfo(second, bodyHitDetail);
        } else {
            this.secondTraceInfo = null;
        }

        if (hasThirdPiercing()) {
            final double hitY = BowArrowIntersectionTracer.tracePosY(arrow.getVelocity().getY(), arrow.getY(), third.getMin());
            final BodyHitDetail bodyHitDetail = Double.compare(hitY, playerAABB.getCenter().getY()) > 0 ? BodyHitDetail.HIGH : BodyHitDetail.LOW;
            this.thirdTraceInfo = new TraceInfo(third, bodyHitDetail);
        } else {
            this.thirdTraceInfo = null;
        }
    }

    public boolean hasFirstPiercing() {
        return first != null;
    }

    public TraceInfo getFirstPiercing() {
        return firstTraceInfo;
    }

    public boolean hasSecondPiercing() {
        return second != null;
    }

    public TraceInfo getSecondPiercing() {
        return secondTraceInfo;
    }

    public boolean hasThirdPiercing() {
        return third != null;
    }

    public TraceInfo getThirdPiercing() {
        return thirdTraceInfo;
    }

    public boolean willPiercePlayer() {
        return hasFirstPiercing() || hasSecondPiercing() || hasThirdPiercing();
    }

    public ArrowEntity getArrow() {
        return arrow;
    }

    public Optional<TraceInfo> getLastPiercingInfo() {
        if (hasThirdPiercing()) return Optional.of(getThirdPiercing());
        if (hasSecondPiercing()) return Optional.of(getSecondPiercing());
        if (hasFirstPiercing()) return Optional.of(getFirstPiercing());
        return Optional.empty();
    }

    public Optional<TraceInfo> getNextPiercingInfo(final int deltaT) {
        if (hasFirstPiercing() && getFirstPiercing().getPiercingEntryHitTick() > deltaT) return Optional.of(getFirstPiercing());
        if (hasSecondPiercing() && getSecondPiercing().getPiercingEntryHitTick() > deltaT) return Optional.of(getSecondPiercing());
        if (hasThirdPiercing() && getThirdPiercing().getPiercingEntryHitTick() > deltaT) return Optional.of(getThirdPiercing());
        return Optional.empty();
    }

    public String toAlertMsg(final int deltaTick) {
        if (!willPiercePlayer()) return "Super Instinct: Not on a colliding course";
        final String third = hasThirdPiercing() ? "\nThird Collision in " + (getThirdPiercing().getPiercingEntryHitTick() - deltaTick) + " ticks." : "";
        final String second = hasSecondPiercing() ? "\nSecond Collision in " + (getSecondPiercing().getPiercingEntryHitTick() - deltaTick) + " ticks." : "";
        final String first = hasFirstPiercing() ? "\nFirst Collision in " + (getFirstPiercing().getPiercingEntryHitTick() - deltaTick) + " ticks." : "";
        return ">>Super Instinct<<" + first + second + third;
    }

    public List<TraceInfo> traceInfosAsList() {
        final List<TraceInfo> infos = new ArrayList<>();
        if (hasFirstPiercing()) infos.add(getFirstPiercing());
        if (hasSecondPiercing()) infos.add(getSecondPiercing());
        if (hasThirdPiercing()) infos.add(getThirdPiercing());
        return infos;
    }

    @Override
    public String toString() {
        String str = "Trace result: ";
        if (hasFirstPiercing())
            str += "first[" + Objects.requireNonNull(getFirstPiercing()).getPiercingEntryHitTick() + "; "
                    + getFirstPiercing().getHitSide()
                    + "; " + getFirstPiercing().getBodyHitDetail().name() + "] ";
        if (hasSecondPiercing())
            str += "second[" + Objects.requireNonNull(getSecondPiercing()).getPiercingEntryHitTick() + "; "
                    + getSecondPiercing().getHitSide() + "; " + getSecondPiercing().getBodyHitDetail() + "]";
        if (hasThirdPiercing())
            str += "third[" + Objects.requireNonNull(getThirdPiercing()).getPiercingEntryHitTick() + "; "
                    + getSecondPiercing().getHitSide() + "; " + getThirdPiercing().getBodyHitDetail() + "]";
        return str;
    }
}