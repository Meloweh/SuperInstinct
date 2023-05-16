/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Box;
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
public class OptimizedIntersectionY {

    private static Optional<Double[]> getVelIntersections(final double v_s, final double  v_p, final Interval<Double>[] intervals) {
        //Punkt 3:
        final Optional<Double> intersectionMovingPlane1 = CSAlgorithmY.getZeroOrOneIntersectionMovingPlanesForVels(intervals[0].getMin(), intervals[0].getMax(), v_p, v_s);
        final Optional<Double> intersectionMovingPlane2 = CSAlgorithmY.getZeroOrOneIntersectionMovingPlanesForVels(intervals[1].getMin(), intervals[1].getMax(), v_p, v_s);
        if (intersectionMovingPlane1.isPresent()) {
            if (intersectionMovingPlane2.isPresent())
                return Optional.of(new Double[]{intersectionMovingPlane1.get(), intersectionMovingPlane2.get()});
            return Optional.of(new Double[]{intersectionMovingPlane1.get()});
        }
        if (intersectionMovingPlane2.isPresent()) return Optional.of(new Double[]{intersectionMovingPlane2.get()});
        return Optional.empty();
    }

    private static Interval<Double>[] punkt4Und5(final double startTick, final double endTick, final Optional<Double[]> velIntersections) {
        //Punkt 4 & 5:
        //TODO: Directions
        if (velIntersections.isEmpty()) return new Interval[] {new Interval<>(startTick, endTick, null)};
        final Optional<Double> opt_v0 = (startTick < velIntersections.get()[0] && velIntersections.get()[0] < endTick) ? Optional.of(velIntersections.get()[0]) : Optional.empty();
        final Optional<Double> opt_v1 = (velIntersections.get().length > 1 && opt_v0.get().intValue() != velIntersections.get()[1].intValue()
                && startTick < velIntersections.get()[1] && velIntersections.get()[1] < endTick)
                ? Optional.of(velIntersections.get()[1]) : Optional.empty();

        if (opt_v0.isPresent() && opt_v1.isPresent()) {
            //TODO: Directions
            return new Interval[]{new Interval<>(startTick, opt_v0.get(), null), new Interval<>(opt_v0.get(), opt_v1.get(), null), new Interval<>(opt_v1.get(), endTick, null)};
        }
        if (opt_v0.isPresent()) {
            return new Interval[] {new Interval<>(startTick, opt_v0.get(), null), new Interval<>(opt_v0.get(), endTick, null)};
        }
        if (opt_v1.isPresent()) {
            return new Interval[] {new Interval<>(startTick, opt_v1.get(), null), new Interval<>(opt_v1.get(), endTick, null)};
        }

        return new Interval[] {new Interval<>(startTick, endTick, null)};
    }

    private static double yDiffPlayerVsArrowPositions(final double v_s, final double pos_s, final double v_p, final double pos_p, final double t) {
        return CSAlgorithmY.tracePlayerPosY(v_s, pos_s, t) - BowArrowIntersectionTracer.tracePosY(v_p, pos_p, t);
    }

    private static Optional<Double> getZeroOrOneIntersectionMovingPlanesForPositions(final double startTick, final double endTick, final double v_s, final double pos_s, final double v_p, final double pos_p) {
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                return yDiffPlayerVsArrowPositions(v_s, pos_s, v_p, pos_p, t);
            }
        };
        return bisection.optIteration(startTick, endTick);
    }

    private static Optional<List<Double>> punkt6Und7Und8(final double startTick, final double endTick, final double v_s, final double pos_s, final double v_p, final double pos_p, final Interval<Double>[] schnittpunktIntervals) {
        //Punkt 6:
        final List<Double> schnittpunkte = new ArrayList<>();
        for (final Interval<Double> interval : schnittpunktIntervals) {
            final Optional<Double> optResult = getZeroOrOneIntersectionMovingPlanesForPositions(interval.getMin(), interval.getMax(), v_s, pos_s, v_p, pos_p);
            if (optResult.isPresent()) schnittpunkte.add(optResult.get());
        }
        if (schnittpunkte.isEmpty()) Optional.empty();

        schnittpunkte.removeIf(e -> e <= startTick || e >= endTick);
        //if (schnittpunkte.size() == 1) return Optional.of(schnittpunkte);

        for (int i = 0; i < schnittpunkte.size() - 2;) {
            if (schnittpunkte.get(i).intValue() == schnittpunkte.get(i+1).intValue()) {
                schnittpunkte.remove(i);
            } else {
                i++;
            }

        }

        return Optional.of(schnittpunkte);
    }

    static Optional<List<Double>> optimizedPunkt6Und7Und8(final double startTick, final double endTick, final double v_s, final double pos_s, final double v_p, final double pos_p) {
        final Bisection velBisection = CSAlgorithmY.getVelBisection(v_p, v_s);
        final List<Double> intersections = new ArrayList<>();
        // Intervalle Schritt 2
        final Interval<Double>[] intervalsVelAbl = CSAlgorithmY.velAblIntervals(startTick, v_s, v_p, endTick);

        if (velBisection.hasIntersection(startTick, endTick)) { // TODO: ungerade?
            if (intervalsVelAbl.length == 1) {
                final Optional<Double> optIntersection = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, v_s, pos_s, v_p, pos_p);
                intersections.add(optIntersection.get());
                return Optional.of(intersections);
            } else /*if (intervalsVelAbl.length == 2)*/ {
                // Nullstellen Diff Vel
                final Optional<Double[]> velIntersections = getVelIntersections(v_s, v_p, intervalsVelAbl);
                if (velIntersections.isEmpty() || velIntersections.get().length < 2) {
                    final Optional<Double> optIntersection = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, v_s, pos_s, v_p, pos_p);
                    intersections.add(optIntersection.get()); //TODO: ERROR possible!: "No value present" ==> update: yeah it happened
                    return Optional.of(intersections);
                }
                // Intervalle Schritt 5
                final Interval<Double>[] schnittpunktIntervals = punkt4Und5(startTick, endTick, velIntersections);
                if (schnittpunktIntervals.length < 3) {
                    final Optional<Double> optIntersection = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, v_s, pos_s, v_p, pos_p);
                    intersections.add(optIntersection.get());
                    return Optional.of(intersections);
                }
                return punkt6Und7Und8(startTick, endTick, v_s, pos_s, v_p, pos_p, schnittpunktIntervals);
            }
        } else { // TODO: gerade?
            // Nullstellen Diff Vel
            final Optional<Double[]> velIntersections = getVelIntersections(v_s, v_p, intervalsVelAbl);
            if (velIntersections.isEmpty()) return Optional.empty();
            // Intervalle Schritt 5
            final Interval<Double>[] schnittpunktIntervals = punkt4Und5(startTick, endTick, velIntersections);
            if (schnittpunktIntervals.length == 1) return Optional.empty();
            return punkt6Und7Und8(startTick, endTick, v_s, pos_s, v_p, pos_p, schnittpunktIntervals);
        }
    }
}
