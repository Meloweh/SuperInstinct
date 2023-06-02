/**
 * @author Welomeh, Meloweh
 */

package welomehandmeloweh.superinstinct;

import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
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
public class IntersectionHelper {
    private static Interval<Long>[] startEndTickIntervalIntersection(final Interval<Double>[] intervals, final double startTick, final double endTick) {
        final List<Interval<Long>> list = new ArrayList<>();
        for (final Interval<Double> interval : intervals) {
            if (endTick < interval.getMin()) break; // falls alle Folgeintervalle nach endTick
            if (startTick > interval.getMax()) continue; // falls Interval noch vor startTick
            final long min = ((Double)Math.max(interval.getMin(), startTick)).longValue();
            final long max = ((Double)Math.min(interval.getMax(), endTick)).longValue();
            final Direction enteringPlane = Double.compare(interval.getMin(), startTick) >= 0 ? interval.getEnteringPlaneName() : null;
            //FIXME: Ich weiß max kann als double kleiner als min sein, aber halt auch eben andersrum, oder? Weil das beim Testen tatsächlich relevant zu sein scheint, mach ich es temporär "sensibler"
            if(max >= min) list.add(new Interval<>(min, max, enteringPlane));
            /*if (startTick < interval.getMax() && startTick >= interval.getMin() || endTick > interval.getMin() && endTick <= interval.getMax()) {
                final long min = ((Double)Math.max(interval.getMin(), startTick)).longValue();
                final long max = ((Double)Math.min(interval.getMax(), endTick)).longValue();
                final Direction enteringPlane = Double.compare(interval.getMin(), startTick) >= 0 ? interval.getEnteringPlaneName() : null;
                return max < min ? Optional.empty() : Optional.of(new Interval<>[] {new Interval<>(min, max, enteringPlane)});
            }*/
        }
        final Interval<Long>[] res = new Interval[list.size()];
        return list.toArray(res);
    }

    private static Optional<Interval<Long>> intersect(final Interval<Long> first, final Interval<Long> second, final int tolerance) {
        final long min = Math.max(first.getMin(), second.getMin());
        final long max = Math.min(first.getMax(), second.getMax());
        final Direction enteringPlane = Double.compare(first.getMin(), second.getMin()) >= 0 ? first.getEnteringPlaneName() : second.getEnteringPlaneName();
        final Optional<Interval<Long>> result = max < min ? Optional.empty()
                :
                Optional.of(new Interval<>(min, max, enteringPlane));

        //FIXME: ----------------this is debug only----------------//
        /*if (result.isEmpty() && tolerance > 0 && min <= max + tolerance) {
            return Optional.of(new Interval<>(max, min, enteringPlane));
        }*/
        //FIXME: --------------------------------------------------//
        return result;
    }

    private static Interval<Long>[] intersect(final Interval<Long>[] i1, final Interval<Long>[] i2, final int tolerance) {
        final List<Interval<Long>> list = new ArrayList<>();
        for (int i = 0; i < i1.length; i++) {
            for (int j = 0; j < i2.length; j++) {
                final Optional<Interval<Long>> optIntersection = intersect(i1[i], i2[j], tolerance);
                if (optIntersection.isPresent()) list.add(optIntersection.get());
            }
        }
        final Interval<Long>[] res = new Interval[list.size()];
        return list.toArray(res);
    }

    private static Interval<Long>[] intersect(final Interval<Long>[] i1, final Interval<Long>[] i2) {
        return intersect(i1, i2, 0);
    }

    /*
    private static List<Interval<Long>> calcIntervalIntersection(final Interval<Double>[] i1, final Interval<Double>[] i2, final Double startTick, final Double endTick) {
        final Interval<Double>[] intersections = intersect(i1, i2);
        return startEndTickIntervalIntersection(intersections, startTick, endTick);
    }*/

    public static Interval<Long>[] calcIntervalIntersection(final Interval<Double>[] i1, final Interval<Double>[] i2, final Interval<Double>[] i3, final Double startTick, final Double endTick) {
        final Interval<Long>[] framedI1 = startEndTickIntervalIntersection(i1, startTick, endTick);
        DebugPrint.println("len framedI1: " + framedI1.length);
        DebugPrint.println("start: " + startTick);
        DebugPrint.println("end: " + endTick);
        for (final Interval<Double> el : i1) {
            DebugPrint.println("i1: " + el);
        }
        for (final Interval<Long> el : framedI1) {
            DebugPrint.println("framedI1: " + el);
        }
        final Interval<Long>[] framedI2 = startEndTickIntervalIntersection(i2, startTick, endTick);
        DebugPrint.println("len framedI2: " + framedI2.length);
        for (final Interval<Long> el : framedI2) {
            DebugPrint.println(el);
        }
        final Interval<Long>[] framedI3 = startEndTickIntervalIntersection(i3, startTick, endTick);
        DebugPrint.println("len framedI3: " + framedI3.length);
        for (final Interval<Long> el : framedI3) {
            DebugPrint.println(el);
        }

        final int tolerance = 1;

        final Interval<Long>[] intersections2D = intersect(framedI1, framedI3);
        DebugPrint.println("len intersections2D: " + intersections2D.length);
        for (final Interval<Long> el : intersections2D) {
            DebugPrint.println(el);
        }

        final Interval<Long>[] intersections3D = intersect(intersections2D, framedI2);
        DebugPrint.println("len intersections3D: " + intersections3D.length);
        for (final Interval<Long> el : intersections3D) {
            DebugPrint.println(el);
        }
        //return startEndTickIntervalIntersection(intersections3D, startTick, endTick);
        return intersections3D;
    }
}
