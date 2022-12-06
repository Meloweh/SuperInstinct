/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Direction;

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
