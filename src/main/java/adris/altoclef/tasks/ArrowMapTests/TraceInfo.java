/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Direction;

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
