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
public class CSAlgorithmY {
    private static double playerVelY(final double v, final double origT) {
        final double t = origT - 1;
        final double gt = Math.pow(PCons.GRAVITY_PLAYER, t);
        return gt * v - PCons.PLAYER_D * PCons.GRAVITY_PLAYER * (gt - 1) / (PCons.GRAVITY_PLAYER - 1);
    }

    private static double playerVelYAbl(final double v, final double origT) {
        final double t = origT - 1;
        return Math.pow(PCons.GRAVITY_PLAYER, t) * (Math.log(PCons.GRAVITY_PLAYER) * (v - PCons.PLAYER_D * PCons.GRAVITY_PLAYER / (PCons.GRAVITY_PLAYER - 1)));
    }

    private static double arrowVelY(final double v, final double origT) {
        final double t = origT - 1;
        return Math.pow(PCons.J, t) * (v - (PCons.J * PCons.D) / (PCons.J - 1)) + PCons.D + PCons.D / (PCons.J - 1);
    }

    private static double arrowVelYAbl(final double v, final double origT) {
        final double t = origT - 1;
        return Math.pow(PCons.J, t) * Math.log(PCons.J) * (v - PCons.J * PCons.D / (PCons.J - 1));
    }

    private static double nullstelleAblVels(final double v_s, final double  v_p) {
        return -(Math.log((Math.log(PCons.GRAVITY_PLAYER) * (v_s - PCons.AIR_RESISTANCE_PLAYER * PCons.GRAVITY_PLAYER / (PCons.GRAVITY_PLAYER - 1))) / (Math.log(PCons.J) * (v_p - PCons.J * PCons.GRAVITY_ARROW / (PCons.J - 1)))) / (Math.log(PCons.GRAVITY_PLAYER) - Math.log(PCons.J))) + 1;
        //return -Math.log((Math.log(PCons.GRAVITY_PLAYER) * (v_s - (PCons.PLAYER_D * PCons.GRAVITY_PLAYER) / (PCons.GRAVITY_PLAYER - 1))) / (Math.log(PCons.J * (v_p - PCons.J * PCons.D / (PCons.J - 1))))) / (Math.log(PCons.GRAVITY_PLAYER) - Math.log(PCons.J)) + 1;
    }

    static Interval<Double>[] velAblIntervals(final double startTick, final double v_s, final double v_p, final double endTick) {
        final double nullstelle = nullstelleAblVels(v_s, v_p);
        System.out.println("nullstelle velAblIntervals: " + nullstelle);
        //TODO: Directions
        if (Double.compare(nullstelle, startTick) < 0 || Double.compare(nullstelle, endTick) > 0) {
            System.out.println("nullstelle ausserhalb von [startTick, endTick]");
            return new Interval[] {new Interval<>(startTick, endTick, null)};
        }
        System.out.println("nullstelle innerhalb von [startTick, endTick]");
        //TODO: Directions ergänzen
        final Interval<Double> first = new Interval<>(startTick, Math.ceil(nullstelle), null);
        //TODO: Directions
        final Interval<Double> second = new Interval<>(Math.floor(nullstelle), endTick , null);

        return new Interval[] {first, second};
    }

    /*
    NL2G — heute um 19:17 Uhr
    D = 0.08
    PLAYER_G = 0.9800000190734863
     return currentPosY + (Math.pow(PCons.GRAVITY_PLAYER, deltaTick) - 1) / (PCons.GRAVITY_PLAYER - 1) * (currentVelY - D * PLAYER_G / (PLAYER_G - 1)) + D * PLAYER_G / (PLAYER_G - 1) * deltaTick
     */
    public static double tracePlayerPosY(final double currentVelY, final double currentPosY, final double deltaTick) {
      //return currentPosY + (Math.pow(PCons.GRAVITY_PLAYER, deltaTick) - 1) / (PCons.GRAVITY_PLAYER - 1) * (currentVelY -             D               *           PLAYER_G   /             (PLAYER_G - 1)) +                 D           *             PLAYER_G /             (PLAYER_G - 1) * deltaTick
        return currentPosY + (Math.pow(PCons.GRAVITY_PLAYER, deltaTick) - 1) / (PCons.GRAVITY_PLAYER - 1) * (currentVelY - PCons.AIR_RESISTANCE_PLAYER * PCons.GRAVITY_PLAYER / (PCons.GRAVITY_PLAYER - 1)) + PCons.AIR_RESISTANCE_PLAYER * PCons.GRAVITY_PLAYER / (PCons.GRAVITY_PLAYER - 1) * deltaTick;
    }

    private static double yDiffPlayerVelVsArrowVel(final double v_p, final double v_s, final double t) {
        return playerVelY(v_s, t) - arrowVelY(v_p, t);
    }

    //for helper class
    static final Bisection getVelBisection(final double v_p, final double v_s) {
        return new Bisection() {
            @Override
            public double f(double t) {
                return yDiffPlayerVelVsArrowVel(v_p, v_s, t);
            }
        };
    }

    static Optional<Double> getZeroOrOneIntersectionMovingPlanesForVels(final double startTick, final double endTick, final double v_p, final double v_s) {
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                return yDiffPlayerVelVsArrowVel(v_p, v_s, t);
            }
        };
        return bisection.optIteration(startTick, endTick);
    }

    private static Optional<Double[]> getVelIntersections(final double startTick, final double v_s, final double  v_p, final double endTick) {
        final Interval<Double>[] intervals = velAblIntervals(startTick, v_s, v_p, endTick);
        System.out.println("len velAblIntervals: " + intervals.length);
        //Punkt 3:
        final Optional<Double> intersectionMovingPlane1 = getZeroOrOneIntersectionMovingPlanesForVels(intervals[0].getMin(), intervals[0].getMax(), v_p, v_s);
        final Optional<Double> intersectionMovingPlane2 = intervals.length > 1 ? getZeroOrOneIntersectionMovingPlanesForVels(intervals[1].getMin(), intervals[1].getMax(), v_p, v_s) : Optional.empty();

        /*final ThreadContainer<Optional<Double>> containerA = new ThreadContainer<>();
        final Thread threadA = new Thread(() -> {
            final Optional<Double> intersectionMovingPlane1 = getZeroOrOneIntersectionMovingPlanesForVels(intervals[0].getMin(), intervals[0].getMax(), v_p, v_s);
            containerA.setTraceResult(intersectionMovingPlane1);
        }); threadA.start();

        final ThreadContainer<Optional<Double>> containerB = new ThreadContainer<>();
        final Thread threadB = new Thread(() -> {
            final Optional<Double> intersectionMovingPlane2 = intervals.length > 1 ? getZeroOrOneIntersectionMovingPlanesForVels(intervals[1].getMin(), intervals[1].getMax(), v_p, v_s) : Optional.empty();
            containerB.setTraceResult(intersectionMovingPlane2);
        });
        threadB.start();

        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        final Optional<Double> intersectionMovingPlane1 = containerA.getTraceResult().get();
        final Optional<Double> intersectionMovingPlane2 = containerB.getTraceResult().get();*/

        System.out.println("intersectionMovingPlane1 present: " + intersectionMovingPlane1.isPresent());
        System.out.println("intersectionMovingPlane2 present: " + intersectionMovingPlane2.isPresent());
        if (intersectionMovingPlane1.isPresent()) {
            if (intersectionMovingPlane2.isPresent())
                return Optional.of(new Double[]{intersectionMovingPlane1.get(), intersectionMovingPlane2.get()});
            return Optional.of(new Double[]{intersectionMovingPlane1.get()});
        }
        if (intersectionMovingPlane2.isPresent()) return Optional.of(new Double[]{intersectionMovingPlane2.get()});
        return Optional.empty();
    }

    private static Interval<Double>[] punkt4Und5(final double startTick, final double v_s, final double  v_p, final double endTick) {
        final Optional<Double[]> velIntersections = getVelIntersections(startTick, v_s, v_p, endTick);
        //Punkt 4 & 5:
        //TODO: Directions
        if (velIntersections.isEmpty()) {
            System.out.println("punkt4Und5 => [startTick, endTick]");
            return new Interval[] {new Interval<>(startTick, endTick, null)};
        }
        final Optional<Double> opt_v0 = (startTick < velIntersections.get()[0] && velIntersections.get()[0] < endTick) ? Optional.of(velIntersections.get()[0]) : Optional.empty();
        final Optional<Double> opt_v1 = (velIntersections.get().length > 1 && opt_v0.get().intValue() != velIntersections.get()[1].intValue() // TODO: java.util.NoSuchElementException: No value present
                && startTick < velIntersections.get()[1] && velIntersections.get()[1] < endTick)
                ? Optional.of(velIntersections.get()[1]) : Optional.empty();

        if (opt_v0.isPresent() && opt_v1.isPresent()) {
            //TODO: Directions
            System.out.println("punkt4Und5 => [startTick, opt_v0],[opt_v0.get(), opt_v1]");
            return new Interval[]{new Interval<>(startTick, opt_v0.get(), null), new Interval<>(opt_v0.get(), opt_v1.get(), null), new Interval<>(opt_v1.get(), endTick, null)};
        }
        if (opt_v0.isPresent()) {
            System.out.println("punkt4Und5 => [startTick, opt_v0],[opt_v0.get(), endTick]");
            return new Interval[] {new Interval<>(startTick, opt_v0.get(), null), new Interval<>(opt_v0.get(), endTick, null)};
        }
        if (opt_v1.isPresent()) {
            System.out.println("punkt4Und5 => [startTick, opt_v1],[opt_v1.get(), endTick]");
            return new Interval[] {new Interval<>(startTick, opt_v1.get(), null), new Interval<>(opt_v1.get(), endTick, null)};
        }

        System.out.println("punkt4Und5 => [startTick, endTick]");
        return new Interval[] {new Interval<>(startTick, endTick, null)};
    }

    private static double yDiffPlayerVsArrowPositions(final double v_s, final double pos_s, final double v_p, final double pos_p, final double t) {
        return tracePlayerPosY(v_s, pos_s, t) - BowArrowIntersectionTracer.tracePosY(v_p, pos_p, t);
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

    private static Optional<List<Double>> punkt6Und7Und8(final double startTick, final double endTick, final double v_s, final double pos_s, final double v_p, final double pos_p, final boolean optimized) {
        if (optimized) return OptimizedIntersectionY.optimizedPunkt6Und7Und8(startTick, endTick, v_s, pos_s, v_p, pos_p);

        final Interval<Double>[] schnittpunktIntervals = punkt4Und5(startTick, v_s, v_p, endTick);
        System.out.println("punkt6Und7Und8 len schnittpunktIntervals: " + schnittpunktIntervals.length);
        //Punkt 6:
        final List<Double> schnittpunkte = new ArrayList<>();
        for (final Interval<Double> interval : schnittpunktIntervals) {
            System.out.println("interval : schnittpunktIntervals = " + interval.getMin() + " " + interval.getMax() + " " + interval.getEnteringPlaneName());
            final Optional<Double> optResult = getZeroOrOneIntersectionMovingPlanesForPositions(interval.getMin(), interval.getMax(), v_s, pos_s, v_p, pos_p);
            if (optResult.isPresent()) {
                System.out.println("getZeroOrOneIntersectionMovingPlanesForPositions present: " + optResult.get());
                schnittpunkte.add(optResult.get());
            }
        }
        if (schnittpunkte.isEmpty()) {
            System.out.println("schnittpunkte isEmpty");
            return Optional.empty();
        }

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

    public static CollisionFeedback calculateCollisionY(final double startTick, final double endTick, final double arrowVel,
                                                                 final double arrowPos, final double playerVel, final double playerStartPos,
                                                                 final Box playerAABB, final boolean optimized){
        final double lowerPlane = playerStartPos - playerAABB.getYLength()/2;
        final double higherPlane = playerStartPos + playerAABB.getYLength()/2;
        final double deltaTicks = endTick - startTick;
        final double arrowPosEnd = BowArrowIntersectionTracer.tracePosY(arrowVel, arrowPos, deltaTicks);
        final double lowerPlaneEnd = CSAlgorithmY.tracePlayerPosY(playerVel, lowerPlane, deltaTicks);
        final double higherPlaneEnd = CSAlgorithmY.tracePlayerPosY(playerVel, higherPlane, deltaTicks);
        final CollisionFeedback collisionFeedback = new CollisionFeedback();

        // ------ Pfeil startet oberhalb der oberen plane ------
        if (arrowPos > higherPlane) {
            System.out.println("------ Pfeil startet oberhalb der oberen plane ------");
            // --- Pfeil endet oben ---
            if (arrowPosEnd > higherPlaneEnd) {
                System.out.println("--- Pfeil endet oben ---");
                //TODO: check if usage of punkt6Und7Und8 is optimally placed
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                // - Kein Schnittpunkt obere plane -
                if (intersectionsHigherPlane.isEmpty()) {System.out.println("- Kein Schnittpunkt obere plane -");
                    // => return empty
                    //return new Interval[] {};
                    return collisionFeedback;
                }
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                // - Kein SP untere plane -
                if (intersectionsLowerPlane.isEmpty()) {System.out.println("- Kein SP untere plane -");
                    //TODO: Fix return Intervals
                    //=> return [a,b]
                    //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP)};
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP));
                } else {System.out.println("- SONST (2 SP untere plane) -");
                    // - SONST (2 SP untere plane) -
                    // => return [a,b],[c,d]
                    //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP),
                    //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN)};
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP)); //TODO: ERROR POSSIBLE: "Index 0 out of bounds for length 0"
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN));
                }
                return collisionFeedback;

            }
            // --- Pfeil endet mittig ---
            if (higherPlaneEnd > arrowPosEnd && arrowPosEnd > lowerPlaneEnd) {System.out.println("--- Pfeil endet mittig ---");
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                // - Kein SP untere plane -
                if (intersectionsLowerPlane.isEmpty()) {System.out.println("- Kein SP untere plane -");
                    // * 1 SP oben *
                    if (intersectionsHigherPlane.get().size() == 1) {System.out.println("* 1 SP oben *");
                        // => [a, endTick]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(endTick), Direction.UP)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(endTick), Direction.UP));
                        return collisionFeedback;
                    } else { // SONST (3 SP oben)
                        System.out.println("* SONST (3 SP oben) *");
                        // => [ao,bo], [co,E]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(3)), Math.ceil(endTick), Direction.UP)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(3)), Math.ceil(endTick), Direction.UP)); //TODO: Index 3 out of bounds for length 3
                        return collisionFeedback;
                    }
                } else { // SONST (SP unten)
                    System.out.println("- SONST (SP unten) -");
                    // * 1 SP oben *
                    if (intersectionsHigherPlane.get().size() == 1) {System.out.println("* 1 SP oben *");
                        // => [ao,au], [bu,E]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP),
                        //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(endTick), Direction.DOWN)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(endTick), Direction.DOWN));
                        return collisionFeedback;
                    } else { // * SONST (3 SP oben) *
                        System.out.println("* SONST (3 SP oben) *");
                        // => [ao, au],[bu, bo],[co, E]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP),
                        //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(endTick), Direction.UP)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN));
                        collisionFeedback.setThird(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(endTick), Direction.UP));
                        return collisionFeedback;
                    }
                }
            }
            // --- Pfeil endet unten ---
            if (lowerPlaneEnd > arrowPosEnd) { System.out.println("--- Pfeil endet unten ---");
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                // - 1 SP untere plane -
                if (intersectionsLowerPlane.get().size() == 1) {System.out.println("- 1 SP untere plane -");// TODO: No value present on get
                    final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                    // * 1 SP oben *
                    if (intersectionsHigherPlane.get().size() == 1) {System.out.println("* 1 SP oben *");
                        // => [o1, u1]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP));
                    } else { // * SONST (3 SP oben) *
                        System.out.println("* SONST (3 SP oben) *");
                        // => [o1, o2],[o3, u1]
                        //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP)};
                        collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.UP));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP)); // TODO: Index 2 out of bounds for length 2
                    }
                } else { // - SONST (3 SP untere plane) -
                    System.out.println("- SONST (3 SP untere plane) -");
                    final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                    // => [o0, u0],[u1,o1],[o2,u2]
                    //return new Interval[] {new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP),
                    //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN),
                    //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(intersectionsLowerPlane.get().get(2)), Direction.UP)};
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(1)), Direction.DOWN));
                    collisionFeedback.setThird(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(2)), Math.ceil(intersectionsLowerPlane.get().get(2)), Direction.UP)); // FIXME: here too
                }
                return collisionFeedback;
            }
        }
        // ------ Pfeil startet in der Mitte ------
        if (higherPlane > arrowPos && arrowPos > lowerPlane) {System.out.println("------ Pfeil startet in der Mitte ------");
            // --- Pfeil endet oben ---
            if (arrowPosEnd > higherPlaneEnd) {System.out.println("--- Pfeil endet oben ---");
                //final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                final Optional<Double> optIntersectionHigherPlane = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                // - Kein SP untere plane -
                /*if (intersectionsLowerPlane.isEmpty()) {
                    // => [S, o0]
                    return new Interval[]{new Interval<>(startTick, intersectionsHigherPlane.get().get(0), null)}; // from inside
                } else { // - SONST (2 SP untere plane) -
                    // => [S, o0],[u0, o1]
                    return new Interval[] {new Interval<>(startTick, intersectionsLowerPlane.get().get(0), null),// from inside
                            new Interval<>(intersectionsLowerPlane.get().get(1), intersectionsHigherPlane.get().get(0), Direction.DOWN)};
                }*/
                if (intersectionsLowerPlane.isEmpty()) {System.out.println("- Kein SP untere plane -");
                    // => [S, o0]
                    //return new Interval[]{new Interval<>(Math.floor(startTick), Math.ceil(optIntersectionHigherPlane.get()), null)}; // from inside
                    collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(optIntersectionHigherPlane.get()), null));
                    return collisionFeedback;
                } else { // - SONST (2 SP untere plane) -
                    System.out.println("- SONST (2 SP untere plane) -");
                    // => [S, o0],[u0, o1]
                    //return new Interval[] {new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null),// from inside
                    //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(optIntersectionHigherPlane.get()), Direction.DOWN)};

                    collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null)); // TODO: Index 0 out of bounds for length 0
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(optIntersectionHigherPlane.get()), Direction.DOWN));
                    return collisionFeedback;
                }
            }
            // --- Pfeil endet mittig ---
            if (higherPlaneEnd > arrowPosEnd && arrowPosEnd > lowerPlaneEnd) {System.out.println("--- Pfeil endet mittig ---");
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                // - (implizit) 2 SP obere plane  -
                if (!intersectionsHigherPlane.isEmpty()) {System.out.println("- (implizit) 2 SP obere plane -");
                    // * (implizit) 2 SP unten *
                    if (!intersectionsLowerPlane.isEmpty()) {System.out.println("* (implizit) 2 SP unten *");
                        // => [S,u0],[u1,o0],[o1,E]
                        //return new Interval[] {new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null),
                        //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(endTick), Direction.UP)};

                        collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN));
                        collisionFeedback.setThird(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(endTick), Direction.UP));
                        return collisionFeedback;
                    } else { // SONST * Keine SP unten *
                        System.out.println("* SONST Keine SP unten *");
                        // => [S, o0, M],[o1, E, UP]
                        //return new Interval[] {new Interval<>(Math.floor(startTick), Math.ceil(intersectionsHigherPlane.get().get(0)), null),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(endTick), Direction.UP)};

                        collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsHigherPlane.get().get(0)), null));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(endTick), Direction.UP));
                        return collisionFeedback;
                    }
                } else { // - SONST (Keine SP obere plane) -
                    System.out.println("- SONST (Keine SP obere plane) -");
                    // * (implizit) 2 SP unten *
                    if (!intersectionsLowerPlane.isEmpty()) {System.out.println("* (implizit) 2 SP unten *");
                        // => [S, u0, M],[u1, E, DOWN]
                        //return new Interval[] {new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null),
                        //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(endTick), Direction.DOWN)};

                        collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(endTick), Direction.DOWN));
                        return collisionFeedback;
                    } else { // * Keine SP unten *
                        System.out.println("* Keine SP unten *");
                        //return new Interval[] {};
                        return collisionFeedback;
                    }
                }
            }
            // --- Pfeil endet unten ---
            if (lowerPlaneEnd > arrowPosEnd) {System.out.println("--- Pfeil endet unten ---");
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                //TODO: more local?
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                // - 1 SP untere plane  -
                if (intersectionsLowerPlane.get().size() == 1) {System.out.println("- 1 SP untere plane -");
                    //final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                    // * Keine SP oben *
                    if (intersectionsHigherPlane.isEmpty()) {System.out.println("* Keine SP oben *");
                        // => [S, u0, M]
                        //return new Interval[]{new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null)};

                        collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null));
                        return collisionFeedback;
                    } else {// * SONST (2 SP oben) *
                        System.out.println("* SONST (2 SP oben) *");
                        // => [S, o0, M],[o1, u0, UP]
                        //return new Interval[]{new Interval<>(Math.floor(startTick), Math.ceil(intersectionsHigherPlane.get().get(0)), null),
                        //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP)};

                        collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsHigherPlane.get().get(0)), null));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(0)), Direction.UP));
                        return collisionFeedback;
                    }
                } else {// - SONST (3 SP untere plane) -
                    System.out.println("- SONST (3 SP untere plane) -");
                    //final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                    // => [S, u0, M],[u1, o0, DOWN],[o1, u2, UP]
                    //return new Interval[]{new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null),
                    //        new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN),
                    //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(2)), Direction.UP)};

                    collisionFeedback.setFirst(new Interval<>(Math.floor(startTick), Math.ceil(intersectionsLowerPlane.get().get(0)), null));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(1)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN));
                    collisionFeedback.setThird(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(2)), Direction.UP)); // FIXME: Info unter NeuerYFehler151122.txt auf Desktop
                    return collisionFeedback;
                }
            }
        }
        // ------ Pfeil startet unterhalb der unteren plane ------
        if (lowerPlane > arrowPos) {System.out.println("------ Pfeil startet unterhalb der unteren plane ------");
            // --- Pfeil endet oben ---
            if (arrowPosEnd > higherPlaneEnd) {System.out.println("--- Pfeil endet oben ---");
                //final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos);
                //final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                //return new Interval[] {new Interval<>(intersectionsLowerPlane.get().get(0), intersectionsHigherPlane.get().get(0), Direction.DOWN)};
                final Optional<Double> optIntersectionHigherPlane = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos);
                final Optional<Double> optIntersectionLowerPlane = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos);
                //return new Interval[] {new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(optIntersectionHigherPlane.get()), Direction.DOWN)};

                collisionFeedback.setFirst(new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(optIntersectionHigherPlane.get()), Direction.DOWN));
                return collisionFeedback;
            }
            // --- Pfeil endet mittig ---
            if (higherPlaneEnd > arrowPosEnd && arrowPosEnd > lowerPlaneEnd) {System.out.println("--- Pfeil endet mittig ---");
                final Optional<Double> optIntersectionLowerPlane = getZeroOrOneIntersectionMovingPlanesForPositions(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos);
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                // - (implizit) 2 SP obere plane -
                if (!intersectionsHigherPlane.isEmpty()) {System.out.println("- (implizit) 2 SP obere plane -");
                    // => [u0, o0, DOWN],[o1, E, UP]
                    //return new Interval[] {new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN),
                    //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(endTick), Direction.UP)};

                    collisionFeedback.setFirst(new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(0)), Math.ceil(endTick), Direction.UP));
                    return collisionFeedback;
                } else { // - SONST (Keine SP untere plane) -
                    System.out.println("- SONST (Keine SP untere plane) -");
                    // => [u0, E, DOWN]
                    //return new Interval[] {new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(endTick), Direction.DOWN)};

                    collisionFeedback.setFirst(new Interval<>(Math.floor(optIntersectionLowerPlane.get()), Math.ceil(endTick), Direction.DOWN));
                    return collisionFeedback;
                }
            }
            // --- Pfeil endet unten ---
            if (lowerPlaneEnd > arrowPosEnd) {System.out.println("--- Pfeil endet unten ---");
                final Optional<List<Double>> intersectionsLowerPlane = punkt6Und7Und8(startTick, endTick, playerVel, lowerPlane, arrowVel, arrowPos, optimized);
                final Optional<List<Double>> intersectionsHigherPlane = punkt6Und7Und8(startTick, endTick, playerVel, higherPlane, arrowVel, arrowPos, optimized);
                // - (implizit) SP obere plane -
                if (!intersectionsHigherPlane.isEmpty()) {System.out.println("- (implizit) SP obere plane -");
                    // => [u0, o0, DOWN],[o1, u1, UP]
                    //return new Interval[] {new Interval<>(Math.floor(intersectionsLowerPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN),
                    //        new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(1)), Direction.UP)};

                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get().get(0)), Math.ceil(intersectionsHigherPlane.get().get(0)), Direction.DOWN));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get().get(1)), Math.ceil(intersectionsLowerPlane.get().get(1)), Direction.UP));
                } else { // - SONST (Keine SP obere plane) -
                    System.out.println("- SONST (Keine SP obere plane) -");
                    //return new Interval[] {};
                }
                return collisionFeedback;
            }
        }

        throw new IllegalStateException("Missed some cases?");
    }
}
