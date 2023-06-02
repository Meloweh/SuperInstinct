/**
 * @author Welomeh, Meloweh
 */

package welomehandmeloweh.superinstinct;

import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

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
public class CSAlgorithm {
    /*private static void getIntersectionMovingPlayerWithArrow(final Vec3d arrowVel, final Vec3d arrowPos, final Vec3d playerVel, final Vec3d playerPos, final Box playerAABB) {

    }*/

    public static final boolean SHOULD_SPRINT = true;
    public static final float DEFAULT_SLIPPERINESS = 0.6f;
    public static final float DEFAULT_MOVEMENT_SPEED = SHOULD_SPRINT ? 0.13000001f : 0.1f;
    public static final float DEFAULT_FLYING_SPEED = SHOULD_SPRINT ? 0.025999999f : 0.02f;
    public static final double DEFAULT_MOVEMENT_INPUT_Z = 0.9800000190734863d;
    public static final double DEFAULT_HITBOX_WIDTH = 0.6000000238418579d;
    public static final double INPUT_CONSTANT = 0.9800000190734863;
    //public static final double HITBOX_WIDTH(final PlayerEntity player) {return player.getBoundingBox().getXLength();}


    /*
    TODO:
     Erstelle Diff Funktion aus 2. Ableitung der Positionsformel, AKA 1. Ableitung der Vel Formel mit 2. Ableitung der Pfeilformel als Subtraktionspartner
     ACHTUNG ACHTUNG: Es könnte ein Offset von -1 oder +1 Tick geben
     ACHTUNG: Wenn wir im Neglectable Speed Limit sind dann können wir die Diff Funktion nicht einfach verwenden
     ABER: Eventuell funktioniert die Diff Gleichsetzung, über die erste Ableitung oder sogar komplett ohne Ableitung
        Wenn wir ohne Ableitung machen, dann sollte es sogar ohne Intervallschachtelung gehen
     Über die zweite Ableitung berechnen wir eine Nullstelle(Wendepunkt), woraus wir dann 2 Intervalle [Starttick;Nullstelltick], [Nullstelle,Endtick]
        (IN Y BEREITS GEMACHT => Reinspickeln)
     Wenn der Wendepunkt außerhalb von start bis end tick ist dann gibt es nur ein Intervall von Start bis Endtick
     Wenn der WP nicht existiert dann dito
     Wir haben nun bis zu 2 Intervallen worauf wir die Intervallschalchtelung von einer Diff Velocities anwenden,
        die Diff Velocity ist eine andere Diff wo nur die Velocity Spieler von Velocity Pfeil abgezogen wird.
        (Hier wieder in CSAY schauen weil da so ähnlich schonmal gemacht)
        WICHTIG: Vergleiche ob Schnittpunkt von benachbarten Intervallen mit Double.compare gleich sind und werfe ggf. einen raus
     Nun haben wir bis zu 3 Intervallen worauf wir jeweils die Intervallschachtelung mit der Position Diff FUnktion ausführen
        (Auch wieder in Y schauen)
        Hier wieder abfangen falls Schnittpunkt leer oder 2 Benachbarte Schnittpunkte gleich
     Wenn wir 3 Schnittpunkte haben dann FEHLER WERFEN und eine Umarmung holen.
     Optimierungen:
      - Wenn wir in den ersten 2 Intervallen je ein SP gefunden haben dann brauchen wir nicht in das dritte reingucken.
      - Wenn wir im Fall 0 oder 2 (0 oder 2 Schnittpunkte der Positionen) sind kann es sein dass wir frühzeitig sehen dass der fall 2 nicht eintreten kann und wir im Fall 0 sein müssen (siehe Optimised Zeug in Y)

     */
    /*private static Optional<Double> getExtremum(final double arrowVel, final double playerVel) {
        final double logPart = - ((1 - BowArrowIntersectionTracer.AIR_RESISTANZ) * playerVel) / (arrowVel * Math.log(BowArrowIntersectionTracer.AIR_RESISTANZ));
        final boolean hasExtremum = logPart > 0;
        if (hasExtremum) return Optional.of(Math.log(logPart) / Math.log(BowArrowIntersectionTracer.AIR_RESISTANZ));
        return Optional.empty();
    }*/
    /*private static double tracePlayerPosX(final double currentVelX, final double currentPosX, final double deltaTick){
        return 0;
    }*/

    private static double arrowVelAbl(final double currentVelX, final double dt) {
        return currentVelX * ((Math.pow(BowArrowIntersectionTracer.AIR_RESISTANZ, dt))/(BowArrowIntersectionTracer.AIR_RESISTANZ - 1)) * Math.pow(Math.log(BowArrowIntersectionTracer.AIR_RESISTANZ), 2);
    }

    private static double arrowVel(final double currentVelX, final double dt) {
        return currentVelX * ((Math.pow(BowArrowIntersectionTracer.AIR_RESISTANZ, dt))/(BowArrowIntersectionTracer.AIR_RESISTANZ - 1)) * Math.log(BowArrowIntersectionTracer.AIR_RESISTANZ);
    }

    private static Optional<Double> getExtremumForMiddleCase(final double arrowVel, final double m, final Axis axis) {
        final double airResistance = BowArrowIntersectionTracer.AIR_RESISTANZ;
        final double logPart = axis.equals(Axis.X) ? - ((1-airResistance) * m) / (arrowVel * Math.log(airResistance)) : ((1-airResistance) * m) / (arrowVel * Math.log(airResistance));
        if (Double.compare(logPart, 0.0d) > 0) {
            return Optional.of(Math.log(logPart) / Math.log(airResistance));
        } else {
            return Optional.empty();
        }
    }


    private static Optional<Double[]> getZeroOrTwoNulltickForMiddleCase(final double startTick, final double endTick, final double arrowPos, final double playerPos, final double arrowVel, final double m, final Axis axis) {
        final Optional<Double> extremumTick = getExtremumForMiddleCase(arrowVel, m, axis);
        DebugPrint.println("getZeroOrTwoNulltickForMiddleCase: startTick" + startTick + " endTick: " + endTick + " arrowPos: " + arrowPos + " playerPos: " + playerPos + " arrowVel: " + arrowVel + " m: " + m + " axis: " + axis);
        if (extremumTick.isPresent()) {
            DebugPrint.println("EP: " + extremumTick.get());
        }
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                final double player = playerPos + (axis.equals(Axis.X) ? - m * t : m * t);
                final double arrow = arrowPos + (axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t));
                DebugPrint.println("player: " + player + " arrow: " + arrow + " --- " + (arrow-player) + " t: " + t);
                return player - arrow;
            }
        };
        // TODO: maybe optimization for case zero or two, but better test first
        if (extremumTick.isPresent() && ((Double.compare(extremumTick.get(), startTick)>=0) && (Double.compare(endTick, extremumTick.get())>=0))){
            final Optional<Double> b1 = bisection.optIteration(startTick, extremumTick.get());
            final Optional<Double> b2 = bisection.optIteration(extremumTick.get(), endTick);
            if (b1.isPresent()){
                if(b2.isPresent()) {
                    return Optional.of(new Double[] {b1.get(), b2.get()});
                } else {
                    return Optional.of(new Double[] {b1.get()});
                }
            } else {
                if(b2.isPresent()) {
                    return Optional.of(new Double[] {b2.get()});
                } else {
                    return Optional.empty();
                }
            }

        } else {
            final Optional<Double> b = bisection.optIteration(startTick, endTick);
            if (b.isPresent()){
                return Optional.of(new Double[] {b.get()});
            } else {
                return Optional.empty();
            }
        }
    }

    //(axis.equals(Axis.X) ? (-m * q - m * Math.pow(q, 2)) : (m * q + m * Math.pow(q, 2)))
    private static double getWendepunktForNormalCase(final double arrowVel, final double playerVel, final float q, final double m, final Axis axis) {
        final double airResistance = BowArrowIntersectionTracer.AIR_RESISTANZ;

        final double c = (playerVel / q-1) * Math.pow(Math.log(q), 2) + (axis.equals(Axis.X) ? -1 : 1) * (m / Math.pow(q-1, 2))*q*Math.pow(Math.log(q), 2);
        //TODO: Calculations like log(AIR_RESISTANCE) could be precalculated once for optimization
        return (Math.log((airResistance - 1) * c * q / (arrowVel * Math.pow(Math.log(airResistance), 2)))) / (Math.log(airResistance) - Math.log(q));

        /*
        if (axis.equals(Axis.X)) {
            final double c = (playerVel / q-1) * Math.pow(Math.log(q), 2) - (m / Math.pow(q-1, 2))*q*Math.pow(Math.log(q), 2);
            //TODO: Calculations like log(AIR_RESISTANCE) could be precalculated once for optimization
            return (Math.log((airResistance - 1) * c * q / (arrowVel * Math.pow(Math.log(airResistance), 2)))) / (Math.log(airResistance) - Math.log(q));
        }
        final double c = (playerVel / q-1) * Math.pow(Math.log(q), 2) + (m / Math.pow(q-1, 2))*q*Math.pow(Math.log(q), 2);
        return Z*/
    }

    private static double playerVel(final double tDelta, final double playerVel, final float q, final double m, final Axis axis) {
        final double t = tDelta - 1;
        return playerVel / (q-1) * Math.pow(q, t+1) * Math.log(q) +
                (axis.equals(Axis.X)?
                        m/(q-1) - m/(Math.pow(q-1, 2)) * Math.pow(q, t+2) * Math.log(q) :
                        m/(1-q) + m/(Math.pow(q-1, 2)) * Math.pow(q, t+2) * Math.log(q));
    }

    private static double diffPlayerAblMinusArrowAbl(final double t, final double arrowVel, final double playerVel, final float q, final double m, final Axis axis) {
        final double airResistance = BowArrowIntersectionTracer.AIR_RESISTANZ;
        final double curArrowAbl = arrowVel * Math.pow(airResistance, t) / (airResistance - 1) * Math.log(airResistance);
        final double curPlayerAbl = playerVel(t, playerVel, q, m, axis);
        DebugPrint.println("curPlayerAbl: " + curPlayerAbl + " curArrowAbl: " + curArrowAbl + " --- " + (curPlayerAbl - curArrowAbl));
        return curPlayerAbl - curArrowAbl;
    }

    private static Optional<Double[]> getExtremaForNormalCase(final double startTick, final double endTick, final double arrowVel, final double playerVel, final float q, final double m, final Axis axis){
        final double wendepunktTick = getWendepunktForNormalCase(arrowVel, playerVel, q, m, axis);

        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                return diffPlayerAblMinusArrowAbl(t, arrowVel, playerVel, q, m, axis);
            }
        };

        if((Double.compare(wendepunktTick, startTick)>=0) && (Double.compare(endTick, wendepunktTick)>=0)) {
            // 2 Intervals
            final Optional<Double> b1 = bisection.optIteration(startTick, wendepunktTick);
            final Optional<Double> b2 = bisection.optIteration(wendepunktTick, endTick);
            //final Optional<Double>[] result = new Optional[] {b1.isPresent() ? b1 : (b2.isPresent() ? b2 : Optional.empty()), b1.isPresent() && b2.isPresent() ? b2 : Optional.empty()};
            if (b1.isPresent()){
                if(b2.isPresent()) {
                    return Optional.of(new Double[] {b1.get(), b2.get()});
                } else {
                    return Optional.of(new Double[] {b1.get()});
                }
            } else {
                if(b2.isPresent()) {
                    return Optional.of(new Double[] {b2.get()});
                } else {
                    return Optional.empty();
                }
            }

        } else {
            final Optional<Double> b = bisection.optIteration(startTick, endTick);
            if (b.isPresent()){
                return Optional.of(new Double[] {b.get()});
            } else {
                return Optional.empty();
            }
        }
    }

    private static Optional<Double[]> getZeroOrTwoNulltickForNormalCase(final double startTick, final double endTick, final double arrowPos, final double playerPos, final double arrowVel, final double playerVel, final float q, final double m, final Axis axis){
        final Optional<Double[]> extremumTicks = getExtremaForNormalCase(startTick, endTick, arrowVel, playerVel, q, m, axis);
        DebugPrint.println("getZeroOrTwoNulltickForNormalCase: startTick" + startTick + " endTick: " + endTick + " arrowPos: " + arrowPos + " playerPos: " + playerPos + " arrowVel: " + arrowVel + " playerVel: " + playerVel + "q: " + q + " m: " + m + " axis: " + axis);
        if (extremumTicks.isPresent()) {
            for (final double d : extremumTicks.get()) {
                DebugPrint.println("EP: " + d);
            }
        }
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                final double player = playerPos + (axis.equals(Axis.X) ? tracePlayerPosXDelta(m, q, playerVel, t) : tracePlayerPosZDelta(m,q, playerVel, t));
                final double arrow = axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t);
                DebugPrint.println("player: " + player + " arrow: " + arrow + " --- " + (arrow-player) + " t: " + t);
                return arrow - player; //player - arrow;
            }
        };
        if (extremumTicks.isEmpty()) {
            // TODO: maybe optimization: if be are in case zeroOrTwo we can skip this and return Optional.empty() ??? --> done
            //final Optional<Double> b = bisection.optIteration(startTick, endTick);
            //return refactorReturnValueForGetNulltickForNormalCase(b, Optional.empty(), Optional.empty());
            DebugPrint.println("getZeroOrTwoNulltickForNormalCase: empty");
            return Optional.empty();
        } else if (extremumTicks.get().length == 1) {
            final double extremumTick = extremumTicks.get()[0];
            //final Optional<Double> b1 = bisection.optIteration(startTick, extremumTick);
            //final Optional<Double> b2 = bisection.optIteration(extremumTick, endTick);

            /*final ThreadContainer<Optional<Double>> containerA = new ThreadContainer<>();
            final Thread threadA = new Thread(() -> {
                final long nanos = System.nanoTime();
                final Optional<Double> b1 = bisection.optIteration(startTick, extremumTick);
                System.out.println("bisection: " + (System.nanoTime()-nanos));
                containerA.setTraceResult(b1);
            }); threadA.start();

            final ThreadContainer<Optional<Double>> containerB = new ThreadContainer<>();
            final Thread threadB = new Thread(() -> {
                final long nanos = System.nanoTime();
                final Optional<Double> b2 = bisection.optIteration(extremumTick, endTick);
                System.out.println("bisection: " + (System.nanoTime()-nanos));
                containerB.setTraceResult(b2);
            });
            threadB.start();

            try {
                threadA.join();
                threadB.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }*/

            final Optional<Double> b1 = bisection.optIteration(startTick, extremumTick);
            final Optional<Double> b2 = bisection.optIteration(extremumTick, endTick);

            return refactorReturnValueForGetNulltickForNormalCase(b1, b2);
        } else {
            final Optional<Double> b1 = bisection.optIteration(startTick, extremumTicks.get()[0]);
            if (b1.isPresent()) {
                final Optional<Double> b2 = bisection.optIteration(extremumTicks.get()[0], endTick);
                return refactorReturnValueForGetNulltickForNormalCase(b1, b2);
            } else {
                //final Optional<Double> b2 = bisection.optIteration(extremumTicks.get()[0], extremumTicks.get()[1]);
                //final Optional<Double> b3 = bisection.optIteration(extremumTicks.get()[1], endTick);

                /*final ThreadContainer<Optional<Double>> containerA = new ThreadContainer<>();
                final Thread threadA = new Thread(() -> {
                    final long nanos = System.nanoTime();
                    final Optional<Double> b2 = bisection.optIteration(extremumTicks.get()[0], extremumTicks.get()[1]);
                    System.out.println("bisection: " + (System.nanoTime()-nanos));
                    containerA.setTraceResult(b2);
                }); threadA.start();

                final ThreadContainer<Optional<Double>> containerB = new ThreadContainer<>();
                final Thread threadB = new Thread(() -> {
                    final long nanos = System.nanoTime();
                    final Optional<Double> b3 = bisection.optIteration(extremumTicks.get()[1], endTick);
                    System.out.println("bisection: " + (System.nanoTime()-nanos));
                    containerB.setTraceResult(b3);
                });
                threadB.start();

                try {
                    threadA.join();
                    threadB.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/

                final Optional<Double> b2 = bisection.optIteration(extremumTicks.get()[0], extremumTicks.get()[1]);
                final Optional<Double> b3 = bisection.optIteration(extremumTicks.get()[1], endTick);

                return refactorReturnValueForGetNulltickForNormalCase(b2, b3);
            }
        }

    }

    private static Optional<Double[]> refactorReturnValueForGetNulltickForNormalCase(Optional<Double> b1, Optional<Double> b2) {
        if (b1.isPresent()){
            if(b2.isPresent()) {
                DebugPrint.println("refactorReturnValueForGetNulltickForNormalCase: b1=" + b1.get() + " b2=" + b2.get());
                return Optional.of(new Double[] {b1.get(), b2.get()});
            } else {
                DebugPrint.println("refactorReturnValueForGetNulltickForNormalCase: b1=" + b1.get());
                return Optional.of(new Double[] {b1.get()});
            }
        } else {
            if(b2.isPresent()) {
                DebugPrint.println("refactorReturnValueForGetNulltickForNormalCase: b2=" + b2.get());
                return Optional.of(new Double[] {b2.get()});
            } else {
                DebugPrint.println("refactorReturnValueForGetNulltickForNormalCase: empty");
                return Optional.empty();
            }
        }
    }

    //(final double m, final float q, final double v_0, final Axis axis, final double t, final VelocityState velocityState) {
    public static double tracePlayerPosXDelta(final double mX, final float q, final double v, final double tDelta) {
        final double t = tDelta-1;
        if (t < 0) return 0;
        return v * (Math.pow(q, t+1) - 1) / (q-1) + mX / (q-1) * (t + 1 - (Math.pow(q, t+2)-q)/(q-1));
    }

    public static float getSlipperiness(final Vec3d feat, final World world) {
        final BlockPos e = new BlockPos(feat.x, feat.y - 0.5000001, feat.z); //TODO: look at blocks ahead
        return world.getBlockState(e).getBlock().getSlipperiness();
    }

    /*
    north: neg z
    south: pos z
    west:  neg x
    east:  pos x
     */
    public static boolean isOnGround(final Vec3d feat, final World world, final Box playerAABB) {
        final BlockPos e = new BlockPos(feat.x, feat.y - 0.5000001, feat.z); //TODO: look at blocks ahead
        final float slip = world.getBlockState(e).getBlock().getSlipperiness();
        if (Float.compare(slip, 1f) < 0) return true;

        final BlockPos north = e.north();
        if (Double.compare(north.getZ() + 1, feat.z - playerAABB.getZLength() / 2) > 0) {
            final float neighSlip = world.getBlockState(north).getBlock().getSlipperiness();
            if (Float.compare(neighSlip, 1f) < 0) return true;

            final BlockPos west = north.west();
            if (Double.compare(west.getZ() + 1, feat.z - playerAABB.getZLength() / 2) > 0) {
                final float neighSlipWest = world.getBlockState(west).getBlock().getSlipperiness();
                if (Float.compare(neighSlipWest, 1f) < 0) return true;
            }
            final BlockPos east = north.east();
            if (Double.compare(east.getZ(), feat.z + playerAABB.getZLength() / 2) < 0) {
                final float neighSlipEast = world.getBlockState(east).getBlock().getSlipperiness();
                if (Float.compare(neighSlipEast, 1f) < 0) return true;
            }

        }
        final BlockPos west = e.west();
        if (Double.compare(west.getZ() + 1, feat.z - playerAABB.getZLength() / 2) > 0) {
            final float neighSlip = world.getBlockState(west).getBlock().getSlipperiness();
            if (Float.compare(neighSlip, 1f) < 0) return true;

            //no check for north because already checked as west in north
            final BlockPos south = west.south();
            if (Double.compare(south.getZ(), feat.z + playerAABB.getZLength() / 2) < 0) {
                final float neighSlipSouth = world.getBlockState(south).getBlock().getSlipperiness();
                if (Float.compare(neighSlipSouth, 1f) < 0) return true;
            }
        }

        final BlockPos south = e.south();
        if (Double.compare(south.getZ(), feat.z + playerAABB.getZLength() / 2) < 0) {
            final float neighSlip = world.getBlockState(south).getBlock().getSlipperiness();
            if (Float.compare(neighSlip, 1f) < 0) return true;

            //same here
            final BlockPos east = south.east();
            if (Double.compare(east.getZ(), feat.z + playerAABB.getZLength() / 2) < 0) {
                final float neighSlipEast = world.getBlockState(east).getBlock().getSlipperiness();
                if (Float.compare(neighSlipEast, 1f) < 0) return true;
            }
        }
        final BlockPos east = e.east();
        if (Double.compare(east.getZ(), feat.z + playerAABB.getZLength() / 2) < 0) {
            final float neighSlip = world.getBlockState(east).getBlock().getSlipperiness();
            if (Float.compare(neighSlip, 1f) < 0) return true;
        }

        return false;
    }

    /*
    public static boolean isOnGround(final Vec3d feat, final World world, final Box playerAABB) {
        final BlockPos e = new BlockPos(feat.x, feat.y - 0.5000001, feat.z); //TODO: look at blocks ahead
        final float slip = world.getBlockState(e).getBlock().getSlipperiness();
        if (Float.compare(slip, 1f) < 0) return true;
        return isOnGround(feat, world, playerAABB, slip);
    }*/

    //TODO: prevent dupe calls of getBlockState when using isOnGround & getSlipperiness at the same time
    public static double getM(final Axis axis, final float startYaw, final Vec3d feat, final World world, final Box playerAABB, final boolean IS_RUNNING) {
        final boolean ON_GROUND = isOnGround(feat, world, playerAABB);
        DebugPrint.println("isOnGround: " + ON_GROUND);
        final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
        final float SLIPPERINESS = getSlipperiness(feat, world);
        DebugPrint.println("SLIPPERINESS: " + SLIPPERINESS);
        if (axis.equals(Axis.X)) return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(startYaw * 0.017453292F);
        return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.cos(startYaw * 0.017453292F);
        //return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (axis.equals(Axis.X) ? (double)MathHelper.sin(yaw * 0.017453292F) : (double)MathHelper.cos(yaw * 0.017453292F));
    }

    //final double startTick, final double endTick, final double pointPos, final double playerVel, final double playerPos, final Axis axis, final float slipperiness, final boolean ON_GROUND, final boolean IS_RUNNING
    public static double getM(final Axis axis, final float startYaw, final boolean IS_RUNNING, final boolean ON_GROUND, final float SLIPPERINESS) {
        DebugPrint.println("isOnGround: " + ON_GROUND);
        final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
        DebugPrint.println("SLIPPERINESS: " + SLIPPERINESS);
        if (axis.equals(Axis.X)) return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(startYaw * 0.017453292F);
        return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.cos(startYaw * 0.017453292F);
        //return INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (axis.equals(Axis.X) ? (double)MathHelper.sin(yaw * 0.017453292F) : (double)MathHelper.cos(yaw * 0.017453292F));
    }


    //TODO: set later private
    public static double tracePlayerPosX(final double currentVelX, final double currentPosX, final double tDelta, final boolean ON_GROUND, final boolean IS_RUNNING, final float SLIPPERINESS , final float startYaw) {
        final double deltaTick = tDelta-1;
        if (deltaTick < 0) return currentPosX;
        //TODO: neglegtable speed limit
        final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
        final double mX = INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(startYaw * 0.017453292F);
        float q = ON_GROUND ? SLIPPERINESS * 0.91F : 0.91F;
        return currentPosX + currentVelX * (Math.pow(q, deltaTick+1) - 1) / (q-1) + mX / (q-1) * (deltaTick + 1 - (Math.pow(q, deltaTick+2)-q)/(q-1));


        //return currentPosX + tracePlayerPosXDelta(mX, q, currentVelX, deltaTick); //TODO: try me


        //return 0;
        //return currentPosX + deltaTick * currentVelX;
        /*BlockPos e = new BlockPos(player.getPos().x, player.getBoundingBox().minY - 0.5000001, player.getPos().z); //TODO: look at blocks ahead
        float slipperiness = world.getBlockState(e).getBlock().getSlipperiness();
        final float q = player.isOnGround() ? slipperiness * 0.91F : 0.91F;
        double movementInputZ = player.forwardSpeed;
                              1 / (1 - q) * (v_0 *         (1 - Math.pow(q, t))        + m  * (-t +         ((1 - Math.pow(q,            t+1))/(1-q)) - 1))
        final double mX = movementInputZ * (player.isOnGround() ? player.getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : player.flyingSpeed) * (double) MathHelper.sin(player.getYaw() * 0.017453292F);
        return currentPosX + 1 / (1 - q) * (currentVelX * (1 - Math.pow(q, deltaTick)) + mX * (-deltaTick + ((1 - Math.pow(q, deltaTick + 1)) / (1 - q)) - 1));*/

        //final double mX = movementInputZ * (mod.getPlayer().isOnGround() ? DEFAULT_MOVEMENT_SPEED * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().flyingSpeed) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);


    }

    public static double tracePlayerPosZDelta(final double mZ, final float q, final double v, final double tDelta) {
        final double t = tDelta-1;
        if (t < 0) return 0;
        return     v * (Math.pow(q, t        +1) - 1) / (q-1) - mZ / (q-1) * (t         + 1 - (Math.pow(q,         t+2)-q)/(q-1));
//return currentVelZ * (Math.pow(q, deltaTick+1) - 1) / (q-1) - mZ / (q-1) * (deltaTick + 1 - (Math.pow(q, deltaTick+2)-q)/(q-1));
    }
    //TODO: set private later
    public static double tracePlayerPosZ(final double currentVelZ, final double currentPosZ, final double tDelta, final boolean ON_GROUND, final boolean IS_RUNNING, final float SLIPPERINESS , final float startYaw) {
        final double deltaTick = tDelta-1;
        if (deltaTick < 0) return currentPosZ;
        //TODO: neglegtable speed limit
        final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
        final double mZ = INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.cos(startYaw * 0.017453292F);
        float q = ON_GROUND ? SLIPPERINESS * 0.91F : 0.91F;
        return currentPosZ + currentVelZ * (Math.pow(q, deltaTick+1) - 1) / (q-1) - mZ / (q-1) * (deltaTick + 1 - (Math.pow(q, deltaTick+2)-q)/(q-1));
        //return currentPosZ + tracePlayerPosZDelta(mZ, q, currentVelZ, deltaTick); //TODO: try me
    }

    //private static double tracePlayerPosZ(final double currentVelZ, final double currentPosZ, final double deltaTick/*, final PlayerEntity player, final World world*/) {
    //    return 0;
        /*BlockPos e = new BlockPos(player.getPos().x, player.getBoundingBox().minY - 0.5000001, player.getPos().z); //TODO: look at blocks ahead
        float slipperiness = world.getBlockState(e).getBlock().getSlipperiness();
        final float q = player.isOnGround() ? slipperiness * 0.91F : 0.91F;
        double movementInputZ = player.forwardSpeed;
        final double mZ = movementInputZ * (player.isOnGround() ? player.getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : player.flyingSpeed) * (double) MathHelper.cos(player.getYaw() * 0.017453292F);
        return currentPosZ + currentVelZ * ((1-Math.pow(q,deltaTick))/(1-q)) + ((mZ*deltaTick)/(1-q)) - ((mZ)/(1-q)) * ((1-Math.pow(q,deltaTick+1))/(1-q)) + mZ/(1-q);*/
    //final double mZ = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().flyingSpeed) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F);

    //}

    //WHAT? D should be G and G should be D
    private static double D = 0.08d, PLAYER_G = 0.9800000190734863;
    //double h = g.y;
    //h -= d;
    //setVelY(h * 0.9800000190734863)

    /*private static double getDiffArrowVsPlayer(final double arrowVel, final double arrowPos, final double playerVel, final double playerPos, final double t, final Axis axis) {
        return (axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t))
                - NegectableSpeedLimit.apply(playerPos, m, q, axis, endTick);
        //if (axis.equals(Axis.X)) {
        //    return BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) - NegectableSpeedLimit.apply(playerPos, m, q, axis, endTick)//tracePlayerPosX(playerVel, playerPos, t);
        //}
        //return BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t) - tracePlayerPosZ(playerVel, playerPos, t);
    }*/
    private static double getDiffArrowVsPlayer(final double arrowPos, final double arrowVel, final double playerPos, final double playerVel, final double t, final double tButOnlyForArrowTracer, final Axis axis,
                                               final double m, final float q, final boolean shouldConsiderStartVelWhenInMiddle,
                                               final TreeMap<Double, SplittedProgress> progress, final double tickProgressUntilPreviousIS) {
        return (axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, tButOnlyForArrowTracer) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t))
                - NegectableSpeedLimit.apply(playerPos, m, q, axis, t, 0, playerVel, shouldConsiderStartVelWhenInMiddle, progress, tickProgressUntilPreviousIS);
    }

    private static double getDiffPointVsPlayer1D(final double pointPos, final double playerPos, final double playerVel, final double t, final Axis axis,
                                                 final double m, final float q, final boolean shouldConsiderStartVelWhenInMiddle,
                                                 final TreeMap<Double, SplittedProgress> progress, final double tickProgressUntilPreviousIS) {
        /*if (axis.equals(Axis.X)) {
            return pointPos - NegectableSpeedLimit.apply(playerPos, m, q, axis, endTick)//tracePlayerPosX(playerVel, playerPos, t);
        }
        return pointPos - tracePlayerPosZ(playerVel, playerPos, t);*/
        //(final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks)
        //return pointPos - NegectableSpeedLimit.apply(playerPos, m, q, axis, t, 0);
        //(final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks, final double v_0, final boolean shouldConsiderStartVelWhenInMiddle) {

        final double nsl = NegectableSpeedLimit.apply(playerPos, m, q, axis, t, 0, playerVel, shouldConsiderStartVelWhenInMiddle, progress, tickProgressUntilPreviousIS);
        DebugPrint.println("pointPos: " + pointPos + " vs NSL: " + nsl + " (playerPos="+playerPos+") || shouldConsiderStartVelWhenInMiddle: " + shouldConsiderStartVelWhenInMiddle + " || t=" + t + "axis: " + axis.toString());
        return pointPos - nsl;
    }

    public static Optional<Double>[] getPointIntersectionMovingPlane(final double localStartTick, final double endTick, final double pointPos, final double playerVel, final double playerPos, final Axis axis, final float slipperiness,
                                                                     final boolean ON_GROUND, final boolean IS_RUNNING, final float yaw, final boolean shouldConsiderStartVelWhenInMiddle, final double m, final float q,
                                                                     final TreeMap<Double, SplittedProgress> progress){
        //DebugPrint.println("localStartTick: " + localStartTick + " endTick: " + endTick + " pointPos: " + pointPos + " playerVel: " + playerVel + " playerPos: " + playerPos);
        //FIXME: Globalize for calculateCollisionXBzwZ?
        //final TreeMap<Double, SplittedProgress> progress = new TreeMap<>();

        //TODO: maybe that is not what we want
        progress.clear();
        progress.put(localStartTick, new SplittedProgress(localStartTick, playerVel, playerPos));

        /*
        final float q = ON_GROUND ? 0.91F * slipperiness : 0.91F;
        final double m = getM(axis, yaw, IS_RUNNING, ON_GROUND, slipperiness);
         */
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                final SplittedProgress currProg = progress.get(progress.floorKey(t));
                final double currPlayerPos = currProg.getCurrPos();
                final double tickProgressUntilPreviousIS = currProg.getTick();
                final double currVel = currProg.getCurrVel();
                //FIXME: HMMMMM... shouldConsiderStartVelWhenInMiddle may be true for more than the start span? uh... ok maybe that is fine
                return getDiffPointVsPlayer1D(pointPos, currPlayerPos, currVel, t - tickProgressUntilPreviousIS, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress, tickProgressUntilPreviousIS);
                //return getDiffPointVsPlayer1D(pointPos, playerPos, playerVel, t, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress);
                //return getDiffPointVsPlayer1D(pointPos, playerPos, playerVel, t - localStartTick, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress, localStartTick);
            }
        };
        final Optional<Double>[] result = new Optional[] {bisection.optIteration(localStartTick, endTick)};
        if (result.length < 1) DebugPrint.println("empty getPointIntersectionMovingPlane");
            //FIXME: this is ugly, better do lastProgressTick+=delta in splitter
        else if (result[0].isPresent()) result[0] = Optional.of(result[0].get() + localStartTick);
        for (final Optional<Double> el : result) {
            if (el.isEmpty()) {
                throw new IllegalStateException("empty opt" + "\\033[0;31mAt the moment i excpect opt to never be empty, therefore this error is raised.");
            }
            else DebugPrint.println("el: " + el.get());
        }
        return result;
    }

    // maximum one intersection
    /*private static Optional<Double>[] getOneIntersectionMovingPlane(final double startTick, final double endTick, final double arrowVel,
                                                                   final double arrowPos, final double playerVel, final double playerPos, final Axis axis){
        DebugPrint.println("startTick: " + startTick + " endTick: " + endTick + " arrovVel: " + arrowVel + " arrowPos: " + arrowPos + " playerVel: " + playerVel + " playerPos: " + playerPos);
        final float q = ON_GROUND ? 0.91F * slipperiness : 0.91F;
        final double m = getM(axis, yaw, IS_RUNNING, ON_GROUND, slipperiness);
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                return getDiffArrowVsPlayer(arrowVel, arrowPos, playerVel, playerPos, t, axis);
            }
        };
        final Optional<Double>[] result = new Optional[] {bisection.optIteration(startTick, endTick)};
        if (result.length < 1) DebugPrint.println("empty getOneIntersectionMovingPlane");
        for (final Optional<Double> el : result) {
            if (el.isEmpty()) DebugPrint.println("empty opt");
            else DebugPrint.println("el: " + el.get());
        }
        return result;
    }*/

    public static Optional<Double[]> getOneIntersectionMovingPlane(final double localStartTick, final double localEndTick, final double arrowVel,
                                                                   final double arrowPos, final double playerVel, final double playerPos, final Axis axis, final boolean shouldConsiderStartVelWhenInMiddle, final double m, final float q, final boolean isMiddle){
        /*final TreeMap<Double, SplittedProgress> progress = new TreeMap<>();
        //TODO: maybe that is not what we want
        progress.clear();
        progress.put(localStartTick, new SplittedProgress(localStartTick, playerVel, playerPos));

        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
            final SplittedProgress currProg = progress.get(progress.floorKey(t));
            final double currPlayerPos = currProg.getCurrPos();
            final double tickProgressUntilPreviousIS = currProg.getTick();
            final double currVel = currProg.getCurrVel();
            //FIXME: HMMMMM... shouldConsiderStartVelWhenInMiddle may be true for more than the start span? uh... ok maybe that is fine
            return getDiffArrowVsPlayer(arrowPos, arrowVel, currPlayerPos, currVel, t - tickProgressUntilPreviousIS, t, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress, tickProgressUntilPreviousIS);
            //return getDiffPointVsPlayer1D(pointPos, playerPos, playerVel, t, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress);
            }
        };
        final Optional<Double> b = bisection.optIteration(localStartTick, localEndTick);
        final Optional<Double[]> result = b.isEmpty() ? Optional.empty() : Optional.of(new Double[]{b.get()});
        if (result.isEmpty() || result.get().length < 1) DebugPrint.println("empty getOneIntersectionMovingPlane");
            //FIXME: this is ugly, better do lastProgressTick+=delta in splitter
        else if (result.get().length > 0) result.get()[0] = result.get()[0] + localStartTick;
        return result;*/
        return getOneIntersectionForOneSplitter(localStartTick, localEndTick, arrowPos, playerPos, arrowVel, playerVel, q, m, axis, isMiddle);
    }

    //final double localStartTick,                                                               final double localEndTick, final double arrowVel, final double arrowPos,  final double playerVel, final double playerPos, final Axis axis, final boolean shouldConsiderStartVelWhenInMiddle, final double m, final float q
    private static Optional<Double[]> getZeroOrTwoIntersectionsForOneSplitter(final double startTick, final double endTick, final double arrowVel, final double arrowPos,
                                                                              final double playerVel, final double playerPos, final Axis axis, final double m, final float q, final boolean isMiddle) {
        final long nanos = System.nanoTime();
        Optional<Double[]> result;
        if (isMiddle) {
            result = getZeroOrTwoNulltickForMiddleCase(startTick, endTick, arrowPos, playerPos, arrowVel, m, axis);
        } else {
            result = getZeroOrTwoNulltickForNormalCase(startTick, endTick, arrowPos, playerPos, arrowVel, playerVel, q, m, axis);
        }
        System.out.println("getZeroOrTwoIntersectionsForOneSplitter: " + (System.nanoTime()-nanos));

        if ((result.isEmpty()) || result.get().length == 2) {
            return result;
        } else {
            throw new IllegalStateException("Non zero or two intersections in state ZeroOrTwo: >> len: " + (result.get().length)
                    + ">> el0: " + result.get()[0] + "\n"
                    + ">> startTick: " + startTick + "\n"
                    + ">> endTick: " + endTick + "\n"
                    + ">> arrowVel: " + arrowVel + "\n"
                    + ">> arrowPos: " + arrowPos + "\n"
                    + ">> playerVel: " + playerVel + "\n"
                    + ">> playerPos: " + playerPos + "\n"
                    + ">> axis: " + axis + "\n"
                    + ">> m: " + m + "\n"
                    + ">> q: " + q + "\n"
                    + ">> isMiddle: " + isMiddle + "\n"
            );
        }
    }

    private static Optional<Double[]> getOneIntersectionForOneSplitter(final double startTick, final double endTick, final double arrowPos, final double playerPos, final double arrowVel, final double playerVel, final float q, final double m, final Axis axis, final boolean isMiddle) {
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                if (Double.compare(t, 0.0d) <= 0) {
                    final double player = playerPos;
                    final double arrow = (axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t));
                    DebugPrint.println("t0: " + t + " origArrow: " + arrowPos + " arrow: " + arrow + " vs origPlayer: " + playerPos + " player: " + player + " = " + (arrow-player));
                    return arrow - player;
                }
                final double player = playerPos + (isMiddle ?
                        (axis.equals(Axis.X) ? - m * t : m * t)
                        :
                        (axis.equals(Axis.X) ? tracePlayerPosXDelta(m, q, playerVel, t) : tracePlayerPosZDelta(m,q, playerVel, t)));
                final double arrow = (axis.equals(Axis.X) ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, t) : BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, t));
                DebugPrint.println("t: " + t + " origArrow: " + arrowPos + " arrow: " + arrow + " vs origPlayer: " + playerPos + " player: " + player + " = " + (arrow-player));
                return arrow - player;
            }
        };
        final long nanos = System.nanoTime();
        final Optional<Double> result = bisection.optIteration(startTick, endTick);
        System.out.println("getOneIntersectionForOneSplitter nanos: " + (System.nanoTime()-nanos));

        if (result.isPresent()) {
            DebugPrint.println("getOneIntersectionForOneSplitter: " + result.get());
            return Optional.of(new Double[] {result.get()});
        } else {
            /*final Optional<Double[]> debug = getZeroOrTwoIntersectionsForOneSplitter(startTick, endTick, arrowVel, arrowPos, playerVel, playerPos, axis, m, q, isMiddle);
            DebugPrint.println("Test 2 intersections before crash => debug present: " + debug.isPresent());
            if (debug.isPresent()) {
                for (Double d : debug.get()) DebugPrint.println("Intersection: " + d);
            }*/

            throw new IllegalStateException("Not exactly one intersections in state One");
        }
    }



    // either zero or two intersections
    /*private static Optional<Double>[] getZeroOrTwoIntersectionsMovingPlane(final double localStartTick, final double localEndTick, final double arrowVel,
                                                                    final double arrowPos, final double playerVel, final double playerPos, final Axis axis, final boolean shouldConsiderStartVelWhenInMiddle, final double m, final float q){


        //TODO: maybe that is not what we want
        final TreeMap<Double, SplittedProgress> progress = new TreeMap<>();
        progress.clear();
        progress.put(localStartTick, new SplittedProgress(localStartTick, playerVel, playerPos));
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                final SplittedProgress currProg = progress.get(progress.floorKey(t));
                final double currPlayerPos = currProg.getCurrPos();
                final double tickProgressUntilPreviousIS = currProg.getTick();
                final double currVel = currProg.getCurrVel();
                //FIXME: HMMMMM... shouldConsiderStartVelWhenInMiddle may be true for more than the start span? uh... ok maybe that is fine
                return getDiffArrowVsPlayer(arrowPos, arrowVel, currPlayerPos, currVel, t - tickProgressUntilPreviousIS, t, axis, m, q, shouldConsiderStartVelWhenInMiddle, progress, tickProgressUntilPreviousIS);
            }
        };
        final Optional<Double> extremumTick = getExtremum(arrowVel, playerVel);
        if(extremumTick.isEmpty()) {
            // max 1 Schnittpunkt
            final Optional<Double>[] result = new Optional[]{bisection.optIteration(localStartTick, localEndTick)};
            if (result.length < 1) DebugPrint.println("empty getZeroOrTwoIntersectionsMovingPlane");
            for (final Optional<Double> el : result) {
                if (el.isEmpty()) DebugPrint.println("empty opt");
                else DebugPrint.println("el: " + el.get());
            }
            return result;
        }

        Optional<Double> optIntersectionFirst = Optional.empty();
        if (localStartTick <= extremumTick.get()){
            optIntersectionFirst = bisection.optIteration(localStartTick, extremumTick.get());
            if (optIntersectionFirst.isEmpty()) DebugPrint.println("empty getZeroOrTwoIntersectionsMovingPlane");
            else DebugPrint.println("el: " + optIntersectionFirst.get());
        }

        Optional<Double> optIntersectionSecond = Optional.empty();

        if (optIntersectionFirst.isPresent() && extremumTick.get() <= localEndTick){
            optIntersectionSecond = Optional.of(bisection.iteration(extremumTick.get(), localEndTick));
            if (optIntersectionSecond.isEmpty()) DebugPrint.println("empty getZeroOrTwoIntersectionsMovingPlane");
            else DebugPrint.println("el: " + optIntersectionSecond.get());
        }
        return new Optional[] {optIntersectionFirst, optIntersectionSecond};
    }*/

    /*private static Optional<Double>[] getIntersectionMovingPlaneWithArrowX(final double startTick, final double endTick, final double arrowVel,
                                                                          final double arrowPos, final double playerVel, final double playerPos) {

        final Optional<Double> extremumTick = getExtremum(arrowVel, playerVel);
        final Bisection bisection = new Bisection() {
            @Override
            public double f(double t) {
                return getDiffArrowVsPlayer(arrowVel, arrowPos, playerVel, playerPos, t);
            }
        };
        if(extremumTick.isEmpty())
            // max 1 Schnittpunkt
            return new Optional[] {Optional.of(bisection.optIteration(startTick, endTick))};

            //if (optIntersection.isEmpty()) {

            //} else {

            //}
        // max 2 Schnittpunkte

        Optional optIntersectionFirst = Optional.empty();
        if (startTick <= extremumTick.get()){
            optIntersectionFirst = bisection.optIteration(startTick, extremumTick.get());
        }


        Optional optIntersectionSecond = Optional.empty();

        if (extremumTick.get() <= endTick){
            optIntersectionSecond = bisection.optIteration(extremumTick.get(), endTick);
        }
        return new Optional[] {optIntersectionFirst, optIntersectionSecond};
    }*/

    private static boolean has2SP(Optional<Double[]> intersections) {
        final boolean bl = intersections.isPresent() && intersections.get().length >= 2;
        return bl;
    }

    /*public static CollisionFeedbackXZ calculateCollisionsXZ(final double endTick, final VecXZ arrowVel,
                                                            final VecXZ arrowPos, final Optional<VecXZ> optPlayerVel, final Vec3d playerStartPos,
                                                            final Box playerAABB, final Vec3d origin, final Vec3d target,
                                                            final World world){
        final float yaw = YawHelper.vecToYaw(origin, target);
        return calculateCollisionsXZ(endTick, arrowVel, arrowPos, optPlayerVel, playerStartPos, playerAABB, yaw, origin.distanceTo(target), world);
    }*/

    private static List<NSLSplitter> createSubSplitterOfSlipAndNSL(final Collection<Splitter> slipperinessSplitter, final List<NSLSplitter> nslSplitters, final Axis axis) {
        DebugPrint.println("createSubSplitterOfSlipAndNSL called");
        DebugPrint.println("slipperinessSplitter:");
        for (final Splitter s : slipperinessSplitter) {
            DebugPrint.println(s.toString());
        }
        DebugPrint.println("nslSplitters:");
        for (final NSLSplitter s : nslSplitters) {
            DebugPrint.println(s.toString());
        }

        //Sanity check
        assert (Double.compare(slipperinessSplitter.iterator().next().getLocalStartTick(), nslSplitters.get(0).getLocalStartTick()) == 0);
        double startTick = nslSplitters.get(0).getLocalStartTick();

        final Iterator<Splitter> slipIt = slipperinessSplitter.iterator();
        final Iterator<NSLSplitter> nslIt = nslSplitters.iterator();

        Splitter currSlip = slipIt.next();
        NSLSplitter currNsl = nslIt.next();

        final List<NSLSplitter> notOnlyNSLSplitters = new ArrayList<>();

        boolean startIsSL = true;

        do {

            DebugPrint.println("currNsl.getStartVel(): " + currNsl.getStartVel());
            DebugPrint.println("currNsl.getLocalEndPos(): " + currNsl.getLocalEndPos());
            DebugPrint.println("currSlip.getLocalStartPosXZ().getX(): " + currSlip.getLocalStartPosXZ().getX());
            DebugPrint.println("currSlip.getLocalStartPosXZ().getZ(): " + currSlip.getLocalStartPosXZ().getZ());
            DebugPrint.println("currSlip.getLocalEndPosXZ().getX(): " + currSlip.getLocalEndPosXZ().getX());
            DebugPrint.println("currSlip.getLocalEndPosXZ().getZ(): " + currSlip.getLocalEndPosXZ().getZ());
            DebugPrint.println("Axis: " + axis);

            //final double minEnd = Double.compare(currSlip.getLocalEndTick(), currNsl.getLocalEndTick()) > 0 ? currNsl.getLocalEndTick() : currSlip.getLocalEndTick();
            if (Double.compare(currSlip.getLocalEndTick(), currNsl.getLocalEndTick()) > 0) {
                final double minEnd = currNsl.getLocalEndTick();

                final double localStartVel = axis.equals(Axis.X) ?
                        startIsSL ? currSlip.getStartVelX() : currNsl.getStartVel()
                        :
                        startIsSL ? currSlip.getStartVelZ() : currNsl.getStartVel();
                final double localStartPos = axis.equals(Axis.X) ?
                        startIsSL ? currSlip.getLocalStartPosXZ().getX() : currNsl.getLocalStartPos()
                        :
                        startIsSL ? currSlip.getLocalStartPosXZ().getZ() : currNsl.getLocalStartPos();
                notOnlyNSLSplitters.add(new NSLSplitter(currNsl.getQ(), currNsl.getM(), startTick, Optional.of(currNsl.getLocalEndTick()), localStartVel, localStartPos, currNsl.getLocalEndPos(), currNsl.isMiddle()));
                DebugPrint.println("AnotOnlyNSLSplitters start pos: " + notOnlyNSLSplitters.get(notOnlyNSLSplitters.size() - 1).getLocalStartPos() + " end pos: " + notOnlyNSLSplitters.get(notOnlyNSLSplitters.size() - 1).getLocalEndPos());
                if (nslIt.hasNext()) currNsl = nslIt.next(); else break;

                startIsSL = false;
                startTick = minEnd;
            } else {
                final double minEnd = currSlip.getLocalEndTick();

                final double localStartVel = axis.equals(Axis.X) ?
                        startIsSL ? currSlip.getStartVelX() : currNsl.getStartVel()
                        :
                        startIsSL ? currSlip.getStartVelZ() : currNsl.getStartVel();
                final double localStartPos = axis.equals(Axis.X) ?
                        startIsSL ? currSlip.getLocalStartPosXZ().getX() : currNsl.getLocalStartPos()
                        :
                        startIsSL ? currSlip.getLocalStartPosXZ().getZ() : currNsl.getLocalStartPos();
                notOnlyNSLSplitters.add(new NSLSplitter(currNsl.getQ(), currNsl.getM(), startTick, Optional.of(currSlip.getLocalEndTick()), localStartVel, localStartPos, axis.equals(Axis.X) ? currSlip.getLocalEndPosXZ().getX() : currSlip.getLocalEndPosXZ().getZ(), currNsl.isMiddle()));
                DebugPrint.println("BnotOnlyNSLSplitters start pos: " + notOnlyNSLSplitters.get(notOnlyNSLSplitters.size() - 1).getLocalStartPos() + " end pos: " + notOnlyNSLSplitters.get(notOnlyNSLSplitters.size() - 1).getLocalEndPos());
                if (nslIt.hasNext()) currNsl = nslIt.next(); else break;

                startIsSL = true;
                startTick = minEnd;
            }
        } while (slipIt.hasNext() || nslIt.hasNext());
        return notOnlyNSLSplitters;
    }

    private static CollisionFeedback getCollisionFeedbackFromSplitters(final VecXZ arrowPosXZ, final VecXZ arrowVelXZ, final TreeMap<Double, Splitter> centerSplitters,
                                                                       final List<NSLSplitter> nslSplitters, final Axis axis,
                                                                       final Optional<VecXZ> optPlayerVel, final double endTick, final Vec3d playerStartPos, final Box playerAABB) {
        final long nanos4 = System.nanoTime();
        final List<Interval<Double>> ivalsList = new ArrayList<>();
        double localArrowStartPos = axis.equals(Axis.X) ? arrowPosXZ.getX() : arrowPosXZ.getZ();
        final double arrowVel = axis.equals(Axis.X) ? arrowVelXZ.getX() : arrowVelXZ.getZ();
        final double arrowPos = axis.equals(Axis.X) ? arrowPosXZ.getX() : arrowPosXZ.getZ();
        final List<NSLSplitter> subsplitter = createSubSplitterOfSlipAndNSL(centerSplitters.values(), nslSplitters, axis);

        //containerA.setTraceResult(subsplitterX);

        DebugPrint.println("subsplitter " + axis + " size: " + subsplitter.size());

        boolean shouldConsiderStartVelWhenInMiddle = axis.equals(Axis.X) ?
                (optPlayerVel.isEmpty() || optPlayerVel.get().isXZero())
                :
                (optPlayerVel.isEmpty() || optPlayerVel.get().isZZero()); //TODO: check if isZero actually works
        System.out.println("nanos4: " + (System.nanoTime()-nanos4));
        for (final NSLSplitter nslSplitter : subsplitter) {
            //TODO: Zum Auslagern
            final long nanos5 = System.nanoTime();
            final boolean isAxisX = axis.equals(Axis.X);

            final float q = nslSplitter.getQ();
            DebugPrint.println("q: " + q + " splip: " + (q/0.91f));
            final double m = nslSplitter.getM();
            final double localVel = nslSplitter.getStartVel();
            DebugPrint.println("localVel: " + localVel + " vs startvel " + (optPlayerVel.isPresent() ?(isAxisX ? optPlayerVel.get().getX() : optPlayerVel.get().getZ()) : ""));
            final double localStartTick = nslSplitter.getLocalStartTick();
            DebugPrint.println("localStartTick: " + localStartTick + " vs starttick " + 0);
            final double localEndTick = nslSplitter.getLocalEndTick();
            DebugPrint.println("localEndTick: " + localEndTick + " vs endtick " + endTick);
            final double localPlayerStartPos = nslSplitter.getLocalStartPos();//FIXME: wenn ich das hier nicht brauche hab ich was falsch gemacht
            DebugPrint.println("localPlayerStartPos: " + localPlayerStartPos + " vs startpos " + playerStartPos.toString());
            final double localPlayerEndPos = nslSplitter.getLocalEndPos();
            DebugPrint.println("localPlayerEndPos: " + localPlayerEndPos + " vs endpos " + localPlayerEndPos);
            DebugPrint.println("Orig Arrow Vel " + axis + ": " + arrowVel + " Delta t: " + (localEndTick - localStartTick));

            final double localArrowEndPos = isAxisX ? BowArrowIntersectionTracer.tracePosX(arrowVel, arrowPos, localEndTick)
                    :
                    BowArrowIntersectionTracer.tracePosZ(arrowVel, arrowPos, localEndTick);
            DebugPrint.println("localArrowEndPos: " + localArrowEndPos);

            final double halfPlayerLen = (isAxisX ? playerAABB.getXLength() / 2 : playerAABB.getZLength() / 2);

            final double localLowerPlane = localPlayerStartPos - halfPlayerLen;
            final double localHigherPlane = localPlayerStartPos + halfPlayerLen;

            final double localLowerPlaneEnd = localPlayerEndPos - halfPlayerLen;
            final double localHigherPlaneEnd = localPlayerEndPos + halfPlayerLen;
            System.out.println("nslSplitter nanos: " + (System.nanoTime()-nanos5));

            final long nanos6 = System.nanoTime();
            final CollisionFeedback cf = calculateCollisionsXBzwZ(localStartTick, localEndTick, arrowVel /*FIXME: do i need delta vel now?*/, localVel, axis,
                    localLowerPlane, localHigherPlane, localLowerPlaneEnd /*TODO/FIXME: Maybe we could also save endpos in splitter*/, localHigherPlaneEnd, localArrowStartPos,
                    localArrowEndPos, m, q, arrowPos, shouldConsiderStartVelWhenInMiddle, nslSplitter.isMiddle());
            System.out.println("calculateCollisionsXBzwZ nanos: " + (System.nanoTime()-nanos6));
            //TODO: save in list und dann intervalle nach zeit sortieren und dann schauen ob trefferintervalle über die Splitter direkt anschließen und das ist dann die Lösung
            final Optional<List<Interval<Double>>> optIvals = cf.toList();

            if (optIvals.isPresent()) ivalsList.addAll(optIvals.get());
            localArrowStartPos = localArrowEndPos;
            shouldConsiderStartVelWhenInMiddle = false;
        }
        final long nanos7 = System.nanoTime();
        DebugPrint.println("Raw ivalsList " + axis + ": ");
        for (final Interval<Double> intv : ivalsList) {
            DebugPrint.println(intv.toString());
        }

        final List<Interval<Double>> ivalResults = new ArrayList<>();
        final Iterator<Interval<Double>> it = ivalsList.iterator();
        if (it.hasNext()) {
            Interval<Double> prev = it.next();
            if (!it.hasNext()) {
                ivalResults.add(prev);
            } else {
                while (it.hasNext()) { //TODO: check if this works
                    final Interval<Double> curr = it.next();
                    final int lower = prev.getMax().intValue();//Math.ceil(prev.getMax());
                    final int higher = curr.getMin().intValue();
                    if (lower+1 >= higher) {
                        prev = new Interval<>(prev.getMin(), curr.getMax(), prev.getEnteringPlaneName());
                    } else {
                        ivalResults.add(prev);
                        prev = curr;
                    }
                }
            }
        }

        DebugPrint.println("Concat ivalResults " + axis + ":");
        for (final Interval<Double> intv : ivalResults) {
            DebugPrint.println(intv.toString());
        }

        final CollisionFeedback cf = CollisionFeedback.ofList(ivalResults);

        DebugPrint.println("Concat cf " + axis + ":");
        if (cf.toList().isPresent()) {
            for (final Interval<Double> intv : cf.toList().get()) {
                DebugPrint.println(intv.toString());
            }
        }
        System.out.println("nanos7: " + (System.nanoTime()-nanos7));
        return cf;
    }

    public static final int DIST = 70;

    public static CollisionFeedbackXZ calculateCollisionsXZ(final double endTick, final VecXZ arrowVel,
                                                            final VecXZ arrowPos, final Optional<VecXZ> optPlayerVel, final Vec3d playerStartPos,
                                                            final Box playerAABB, final float yaw,
                                                            final World world, final Vec3d unpreciseEndSoilPos){
        final long nanos1 = System.nanoTime();
        DebugPrint.println("Called calculateCollisionsXZ");
        DebugPrint.println("endTick: " + endTick);
        DebugPrint.println("playerStartPos: " + playerStartPos.toString());
        DebugPrint.println("yaw: " + yaw);

        //final VecXZ arrowVel = new VecXZ(arrowVelWithoutQ.getX() * q, arrowVelWithoutQ.getZ() * q);

        final Vec3d rawYawVec3d = YawHelper.yawToVec(yaw);
        final Vec3d normalizedYawVec3d = rawYawVec3d.normalize();
        //FIXME: SOMETIMES EXPANSION AT WRONG DIRECTION?? when going from 70 down to zero, the path array instead goes bigger towards 200
        //gggggggggg
        //final Vec3d unpreciseEndSoilPosWithoutY = normalizedYawVec3d.multiply(unpreciseEndPosDistanceMultiplicator);
        //DebugPrint.println("unpreciseEndSoilPosWithoutY: " + unpreciseEndSoilPosWithoutY.toString());
        //final Vec3d unpreciseEndSoilPos = new Vec3d(unpreciseEndSoilPosWithoutY.getX(), playerStartPos.getY(), unpreciseEndSoilPosWithoutY.getZ());
        DebugPrint.println("unpreciseEndSoilPos: " + unpreciseEndSoilPos.toString());
        final BlockPos unpreciseEndSoilBlock = new BlockPos(unpreciseEndSoilPos);
        DebugPrint.println("unpreciseEndSoilBlock: " + unpreciseEndSoilBlock.toString());

        //FIXME: Je nach startpunkt wird falsch gerundet
        //final boolean shouldCeilX =
        //final BlockPos startSoilBlockBelowFeet = new BlockPos(Double.compare(playerStartPos.getX(), 0) < 0 ? Math.cei).down();
        //DebugPrint.println("startSoilBlockBelowFeet: " + startSoilBlockBelowFeet.toString());

        final List<BlockPos> path = FloatingPointBresenham.floatingPointBresenham((int)((int)playerStartPos.getY() - 0.6), playerStartPos.getX(), playerStartPos.getZ(), unpreciseEndSoilPos.getX(), unpreciseEndSoilPos.getZ()); //FIXME: Y-1? Probably too much
        //System.out.print("path elements: "); for (final BlockPos bp : path) System.out.print(bp.toString()); DebugPrint.println("");
        System.out.println("path size: " + path.size());
        final List<SlipperinessSpot> spots = new ArrayList<>();

        //TODO: EXPENSIVE STATE CHECK
        final long nanos = System.nanoTime();
        path.forEach(e -> spots.add(new SlipperinessSpot(e, world.getBlockState(e).getBlock())));
        System.out.println("average nanos getBlockState: " + (System.nanoTime()-nanos)/path.size());
        //path.forEach(e -> spots.add(new SlipperinessSpot(e, Blocks.GRASS)));


        //DebugPrint.println("spots elements: "); for (final SlipperinessSpot ss : spots) DebugPrint.println(ss.toString()); DebugPrint.println("");
        DebugPrint.println("spots size: " + spots.size());

        final TreeMap<Double, Splitter> centerSplitters = new TreeMap<>();
        final List<NSLSplitter> nslSplittersX = new ArrayList<>();
        final List<NSLSplitter> nslSplittersZ = new ArrayList<>();
        final VecXZ preciseCenterPlaneEnd = SlipperinessSplitter.slipperinessSplitter(new VecXZ(playerStartPos.getX(), playerStartPos.getZ()), spots, endTick, optPlayerVel, yaw, centerSplitters, nslSplittersX, nslSplittersZ);
        DebugPrint.println("nslSplittersX size: " + nslSplittersX.size());
        DebugPrint.println("nslSplittersZ size: " + nslSplittersZ.size());
        DebugPrint.println("centerSplitters size: " + centerSplitters.size());
        System.out.println("Part 1: " + (System.nanoTime()-nanos1));

        /*final ThreadContainer<CollisionFeedback> containerA = new ThreadContainer<>();
        final Thread threadA = new Thread(() -> {
            final long nanos = System.nanoTime();
            final CollisionFeedback feedback = getCollisionFeedbackFromSplitters(arrowPos, arrowVel, centerSplitters, nslSplittersX, Axis.X, optPlayerVel, endTick, playerStartPos, playerAABB);
            System.out.println("getCollisionFeedbackFromSplitters nanos: " + (System.nanoTime()-nanos));
            containerA.setTraceResult(feedback);
        }); threadA.start();

        final ThreadContainer<CollisionFeedback> containerB = new ThreadContainer<>();
        final Thread threadB = new Thread(() -> {
            final long nanos = System.nanoTime();
            final CollisionFeedback feedback = getCollisionFeedbackFromSplitters(arrowPos, arrowVel, centerSplitters, nslSplittersZ, Axis.Z, optPlayerVel, endTick, playerStartPos, playerAABB);
            System.out.println("getCollisionFeedbackFromSplitters nanos: " + (System.nanoTime()-nanos));
            containerB.setTraceResult(feedback);
        }); threadB.start();

        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        final long nanos2 = System.nanoTime();
        final CollisionFeedback cfx = getCollisionFeedbackFromSplitters(arrowPos, arrowVel, centerSplitters, nslSplittersX, Axis.X, optPlayerVel, endTick, playerStartPos, playerAABB);
        System.out.println("Part 2: " + (System.nanoTime()-nanos2));

        final long nanos3 = System.nanoTime();
        final CollisionFeedback cfz = getCollisionFeedbackFromSplitters(arrowPos, arrowVel, centerSplitters, nslSplittersZ, Axis.Z, optPlayerVel, endTick, playerStartPos, playerAABB);
        System.out.println("Part 3: " + (System.nanoTime()-nanos3));

        return new CollisionFeedbackXZ(cfx, cfz); //CollisionFeedback[] {cfx, cfz};
    }

    private static CollisionFeedback calculateCollisionsXBzwZ(final double localStartTick, final double localEndTick, final double arrowVel, final double playerVel, final Axis xz,
                                                              final double lowerPlane, final double higherPlane, final double lowerPlaneEnd, final double higherPlaneEnd, final double localArrowPosStart, final double localArrowPosEnd,
                                                              final double m, final float q, final double preSplitterCalcArrowStartPos, final boolean shouldConsiderStartVelWhenInMiddle, boolean middle){
        DebugPrint.println("lowerPlane: " + lowerPlane + " higherPlane: " + higherPlane + " lowerPlaneEnd: " + lowerPlaneEnd + " higherPlaneEnd: " + higherPlaneEnd + " localArrowPosStart: " + localArrowPosStart + " localArrowPosEnd: " + localArrowPosEnd);
        DebugPrint.println(" localStartTick: " + localStartTick + " localEndTick: " + localEndTick + " arrowVel: " + arrowVel + " playerVel: " + playerVel + " Axis: " + xz);

        final CollisionFeedback collisionFeedback = new CollisionFeedback();
        final Direction towardsPositiveDir = xz.equals(Axis.X) ? Direction.EAST : Direction.SOUTH;
        final Direction towardsNegativeDir = xz.equals(Axis.X) ? Direction.WEST : Direction.NORTH;

        DebugPrint.println("-----------------------");
        //Pfeil startet oberhalb der obersten Plane
        if (Double.compare(localArrowPosStart, higherPlane) > 0) {
            DebugPrint.println("Pfeil startet oberhalb der obersten Plane");
            //Pfeil endet oben
            if (Double.compare(localArrowPosEnd, higherPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet oben");
                final Optional<Double[]> intersectionsLowerPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, m, q, middle);
                final Optional<Double[]> intersectionsHigherPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, m, q, middle);
                //2 SP unten
                if (has2SP(intersectionsLowerPlane)) {
                    DebugPrint.println("2 SP unten");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get()[0]), Math.floor(intersectionsLowerPlane.get()[0]), towardsNegativeDir));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get()[1]), Math.floor(intersectionsHigherPlane.get()[1]), towardsPositiveDir));
                } else if (has2SP(intersectionsHigherPlane)) {
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get()[0]), Math.floor(intersectionsHigherPlane.get()[1]), towardsNegativeDir));
                    //Kein SP
                } else {
                    DebugPrint.println("Kein SP");
                }
                //Pfeil endet mittig
            } else if (Double.compare(localArrowPosEnd, higherPlaneEnd) < 0 && Double.compare(localArrowPosEnd, lowerPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet mittig");
                final Optional<Double[]> intersectionsLowerPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, m, q, middle);
                //localStartTick localEndTick arrowVel,             arrowPos,           playerVel, playerPos, axis, shouldConsiderStartVelWhenInMiddle, m, q)
                final Optional<Double[]> intersectionsHigherPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                //2 SP unten
                if (has2SP(intersectionsLowerPlane)) {
                    DebugPrint.println("2 SP unten");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get()[0]), Math.floor(intersectionsLowerPlane.get()[0]), towardsNegativeDir));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get()[1]), localEndTick, towardsPositiveDir));
                    //1 SP oben
                } else {
                    DebugPrint.println("1 SP oben");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get()[0]), localEndTick, towardsNegativeDir));
                }
                //Pfeil endet unten
            } else {
                DebugPrint.println("Pfeil endet unten");
                final Optional<Double[]> intersectionsHigherPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                final Optional<Double[]> intersectionsLowerPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);

                collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsHigherPlane.get()[0]), Math.floor(intersectionsLowerPlane.get()[0]), towardsNegativeDir));
            }
            //Pfeil startet mittig
        } else if (Double.compare(localArrowPosStart, higherPlane) < 0 && Double.compare(localArrowPosStart, lowerPlane) > 0) {
            DebugPrint.println("Pfeil startet mittig");
            //Pfeil endet oben
            if (Double.compare(localArrowPosEnd, higherPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet oben");
                final Optional<Double[]> intersectionsLowerPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, m, q, middle);
                final Optional<Double[]> intersectionsHigherPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                //2 SP unten
                if (has2SP(intersectionsLowerPlane)) {
                    DebugPrint.println("2 SP unten");
                    collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsLowerPlane.get()[0]), towardsNegativeDir));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get()[1]), Math.floor(intersectionsHigherPlane.get()[0]), towardsPositiveDir));
                } else {
                    DebugPrint.println("1 SP oben");
                    collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsHigherPlane.get()[0]), null)); //FIXME: another
                }
                //Pfeil endet mittig
            } else if (Double.compare(localArrowPosEnd, higherPlaneEnd) < 0 && Double.compare(localArrowPosEnd, lowerPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet mittig");
                final Optional<Double[]> intersectionsLowerPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, m, q, middle);
                //2 SP unten
                if (has2SP(intersectionsLowerPlane)) {
                    DebugPrint.println("2 SP unten");
                    collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsLowerPlane.get()[0]), null));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsLowerPlane.get()[1]), localEndTick, towardsPositiveDir));
                } else {
                    final Optional<Double[]> intersectionsHigherPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, m, q, middle);
                    //2 SP oben
                    if (has2SP(intersectionsHigherPlane)) {
                        DebugPrint.println("2 SP oben");
                        collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsHigherPlane.get()[0]), null));
                        collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get()[1]), localEndTick, towardsNegativeDir));
                        //0 SP
                    } else {
                        DebugPrint.println("0 SP");
                    }
                }
                //Pfeil endet unten
            } else {
                DebugPrint.println("Pfeil endet unten");
                final Optional<Double[]> intersectionsHigherPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, m, q, middle);
                final Optional<Double[]> intersectionsLowerPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                //2 SP oben
                if (has2SP(intersectionsHigherPlane)) {
                    DebugPrint.println("2 SP oben");
                    collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsHigherPlane.get()[0]), null));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get()[1]), Math.floor(intersectionsLowerPlane.get()[0]), towardsNegativeDir));
                    //1 SP unten
                } else {
                    DebugPrint.println("1 SP unten");
                    collisionFeedback.setFirst(new Interval<>(localStartTick, Math.floor(intersectionsLowerPlane.get()[0]), null));
                }
            }
            //Pfeil startet unten
        } else {
            DebugPrint.println("Pfeil startet unten");
            //Pfeil endet oben
            if (Double.compare(localArrowPosEnd, higherPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet oben");
                final Optional<Double[]> intersectionsLowerPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                final Optional<Double[]> intersectionsHigherPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get()[0]), Math.floor(intersectionsHigherPlane.get()[0]), towardsPositiveDir)); // FIXME: crash
                //Pfeil endet mittig
            } else if (Double.compare(localArrowPosEnd, higherPlaneEnd) < 0 && Double.compare(localArrowPosEnd, lowerPlaneEnd) > 0) {
                DebugPrint.println("Pfeil endet mittig");
                final Optional<Double[]> intersectionsHigherPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, m, q, middle);
                final Optional<Double[]> intersectionsLowerPlane = getOneIntersectionMovingPlane(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, shouldConsiderStartVelWhenInMiddle, m, q, middle);
                //2 SP oben
                if (has2SP(intersectionsHigherPlane)) {
                    DebugPrint.println("2 SP oben");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get()[0]), Math.floor(intersectionsHigherPlane.get()[0]), towardsPositiveDir));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get()[1]), localEndTick, towardsNegativeDir));
                    //1 SP unten
                } else {
                    DebugPrint.println("1 SP unten");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get()[0]), localEndTick, towardsPositiveDir));
                }
                //Pfeil endet unten
            } else {
                DebugPrint.println("Pfeil endet unten");
                final Optional<Double[]> intersectionsHigherPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, higherPlane, xz, m, q, middle);
                final Optional<Double[]> intersectionsLowerPlane = getZeroOrTwoIntersectionsForOneSplitter(localStartTick, localEndTick, arrowVel, preSplitterCalcArrowStartPos, playerVel, lowerPlane, xz, m, q, middle);
                //2 SP oben
                if (has2SP(intersectionsHigherPlane)) {
                    DebugPrint.println("2 SP oben");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get()[0]), Math.floor(intersectionsHigherPlane.get()[0]), towardsPositiveDir));
                    collisionFeedback.setSecond(new Interval<>(Math.floor(intersectionsHigherPlane.get()[1]), Math.floor(intersectionsLowerPlane.get()[1]), towardsNegativeDir));
                    //2 SP unten
                } else if (has2SP(intersectionsLowerPlane)) {
                    DebugPrint.println("2 SP unten");
                    collisionFeedback.setFirst(new Interval<>(Math.floor(intersectionsLowerPlane.get()[0]), Math.floor(intersectionsLowerPlane.get()[1]), towardsPositiveDir));
                    //0 SP
                } else {
                    DebugPrint.println("0 SP");
                }
            }
        }
        DebugPrint.println(xz + " feedback(csa)v2: " + collisionFeedback);
        return collisionFeedback;//*/ return null;
    }

    public static CollisionFeedback calculateCollisionsY(final double startTick, final double endTick, final Vec3d arrowVel,
                                                         final Vec3d vecArrowPos, final Vec3d playerVel, final Vec3d vecPlayerStartPos,
                                                         final Box playerAABB, final boolean optimized) {
        return CSAlgorithmY.calculateCollisionY(startTick, endTick, arrowVel.getY(), vecArrowPos.getY(), playerVel.getY(), vecPlayerStartPos.getY(), playerAABB, optimized);
    }
}
