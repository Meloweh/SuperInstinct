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

package welomehandmeloweh.superinstinct;

import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

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
public final class BowArrowIntersectionTracer {
    public final static double AIR_RESISTANZ = 0.99f;
    public final static double ARROW_GRAVITY = 0.05000000074505806d;

    public static double[] getIntersectionY(double velInit, double yInit, double yCollision) {
        final double a = (yCollision-yInit) * ((1 - AIR_RESISTANZ) * (1 - AIR_RESISTANZ)) - velInit + velInit * AIR_RESISTANZ - ARROW_GRAVITY;
        final double b = -velInit + velInit * AIR_RESISTANZ - ARROW_GRAVITY;
        final double c = ARROW_GRAVITY * AIR_RESISTANZ - ARROW_GRAVITY;
        final Optional<double[]> result = approxTicks(a, b, c);
        if (result.isEmpty()) {
            return new double[] {Double.NaN, Double.NaN};
        }
        return result.get();
    }

    /**
     * Löse eine Gleichung der Form a = (j^t) * b + t * c nach t auf
     * @return approximierte beide t in Ticks
     */
    private static Optional<double[]> approxTicks(final double a, final double b, final double c) {
        final double lamb_arg = (b * Math.pow(AIR_RESISTANZ, a/c) * Math.log(AIR_RESISTANZ)) / c;
        // DebugPrint.println("a: " + a);
        // DebugPrint.println("b: " + b);
        // DebugPrint.println("c: " + c);
        DebugPrint.println("lamb_arg: " + lamb_arg);
        //return new double[] {0, 0};
        if (Double.isNaN(lamb_arg) || Double.isInfinite(lamb_arg)) return Optional.empty();
        final double t_z_0 = a * Math.log(AIR_RESISTANZ) - c * LambertW.branch0(lamb_arg);
        final double t_n = c * Math.log(AIR_RESISTANZ);
        final double t_0 = t_z_0 / t_n;
        // Wenn lamb_arg > 0 ist, dann brauchen wir nur t_0 auszugeben, weil beide Branches das selbe zurückgeben.
        if (Double.compare(lamb_arg, 0) > 0) return Optional.of(new double[] {t_0});
        final double t_z_minus1 = a * Math.log(AIR_RESISTANZ) - c * LambertW.branchNeg1(lamb_arg);
        final double t_minus1 = t_z_minus1 / t_n;
        return Optional.of(new double[] {t_minus1, t_0});
    }

    public static double getIntersectionX(final double velInit, final double xInit, final double xCollision) {
        DebugPrint.println("vel: " + velInit + " pos: " + xInit + " col: " + xCollision);
        //final Complex complex = new Complex((((xCollision - xInit) * (AIR_RESISTANZ - 1)) / (velInit)) + 1);
        //final Complex complexLog = complex.log();
        DebugPrint.println("RESULT OF INTERSX: " + Math.log((((xCollision - xInit) * (AIR_RESISTANZ - 1)) / (velInit)) + 1));
        //DebugPrint.println("RESULT OF INTERSX (complex): " + complexLog.getReal());
        //DebugPrint.println("Total result of intersx (complex): " + (complexLog.getReal() / Math.log(AIR_RESISTANZ)));
        return Math.log((((xCollision - xInit) * (AIR_RESISTANZ - 1)) / (velInit)) + 1) / Math.log(AIR_RESISTANZ);
        //return complexLog.getReal() / Math.log(AIR_RESISTANZ);
    }

    public static double getIntersectionZ(final double velInit, final  double zInit, final double zCollision) {
        return getIntersectionX(velInit, zInit, zCollision);
    }

    public static double tracePosY(final double currentVelY, final double currentPosY, final double t) {
        //return             currentPosY * ((1 - Math.pow(AIR_RESISTANZ, t)) / (1 - AIR_RESISTANZ))   - ARROW_GRAVITY * (Math.pow(AIR_RESISTANZ, t) - AIR_RESISTANZ * t + t - 1) / (Math.pow(AIR_RESISTANZ - 1, 2));
        return currentPosY + currentVelY * ((Math.pow(AIR_RESISTANZ, t) - 1)  / (AIR_RESISTANZ  - 1))
                - ARROW_GRAVITY * ((Math.pow(AIR_RESISTANZ, t) - AIR_RESISTANZ * t + t - 1 ) / (Math.pow(AIR_RESISTANZ - 1, 2)));
    }

    // TODO: check that everywhere tracePos X, Y, Z is used correctly with deltaTicks, not absolute ticks
    public static double tracePosX(final double currentVelX, final double currentPosX, final double deltaTick) {
        return currentPosX + currentVelX * ((Math.pow(AIR_RESISTANZ, deltaTick) - 1) / (AIR_RESISTANZ - 1));
    }

    public static double tracePosZ(final double currentVelZ, final double currentPosZ, final double t) {
        return tracePosX(currentVelZ, currentPosZ, t);
    }

    /**
     * Assuming that player motion is linear to the current direction.
     */
    /*public static Optional<double[]> getPlayerMotionIntersectionX(final double velInitPlayerX, final double velInitArrowX, final double arrowPosX, final double playerAABB_x) {
        final double a = playerAABB_x - arrowPosX + (velInitArrowX / (AIR_RESISTANZ - 1));
        final double b = velInitArrowX / (AIR_RESISTANZ - 1);
        final double c = -velInitPlayerX;
        if (Double.compare(c, 0.0d) == 0)
            return Optional.of(new double[] {getIntersectionX(velInitArrowX, arrowPosX, playerAABB_x)});//new double[] {getIntersectionX(velInitArrowX, arrowPosX, playerAABB_x)};
        return approxTicks(a, b, c);
    }

    public static Optional<double[]> getPlayerMotionIntersectionZ(final double velInitPlayerZ, final double velInitArrowZ, final double arrowPosZ, final double playerAABB_z) {
        return getPlayerMotionIntersectionX(velInitPlayerZ, velInitArrowZ, arrowPosZ, playerAABB_z);
    }*/

    /*private enum YNames {
        minYminus1,
        minY0,
        maxYminus1,
        maxY0,
        minX,
        maxX,
        minZ,
        maxZ
    }*/

    public static CollisionFeedback calculateCollisionY(ArrowEntity arrow, Box playerAABB) {
        DebugPrint.println("calculateCollisionY without movement");
        // up to 2 collisions per y-plane
        //final double[] minY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.minY);
        //final double[] maxY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.maxY);

        /*final ThreadContainer<double[]> containerMinY = new ThreadContainer<>();
        final Thread threadMinY = new Thread(() -> {
            final long nanos = System.nanoTime();
            final double[] minY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.minY);
            System.out.println("calculateCollisionY threadMinY: " + (System.nanoTime()-nanos));
            containerMinY.setTraceResult(minY);
        }); threadMinY.start();

        final ThreadContainer<double[]> containerMaxY = new ThreadContainer<>();
        final Thread threadMaxY = new Thread(() -> {
            final double[] maxY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.maxY);
            containerMaxY.setTraceResult(maxY);
        }); threadMaxY.start();

        try {
            threadMinY.join();
            threadMaxY.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        final double[] minY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.minY);
        final double[] maxY = getIntersectionY(arrow.getVelocity().getY(), arrow.getBoundingBox().getCenter().getY(), playerAABB.maxY);

        final double minYminus1 = minY[0];
        final double minY0 = minY[1];
        final double maxYminus1 = maxY[0];
        final double maxY0 = maxY[1];

        /*final Map<YNames, Double> map = new HashMap<>();
        map.put(YNames.minYminus1, minYminus1);
        map.put(YNames.minY0, minY0);
        map.put(YNames.maxYminus1, maxYminus1);
        map.put(YNames.maxY0, maxY0);
        final Stream<Map.Entry<YNames, Double>> sorted = map.entrySet().stream().sorted(Map.Entry.comparingByValue());
        final Stream<Map.Entry<YNames, Double>> sorted2 = map.entrySet().stream().sorted(Map.Entry.comparingByValue());
        final List<Double> sortedValues = sorted2.map(e -> e.getValue()).collect(Collectors.toList());*/
        final List<Double> sortedValues = Arrays.asList(new Double[]{minYminus1, minY0, maxYminus1, maxY0 })
                .stream().sorted().collect(Collectors.toList());

        final CollisionFeedback feedback = new CollisionFeedback();
        Direction enteringPlaneMin = null;
        Direction enteringPlaneMax = null;

        double minIntv_of_minY = 0d;
        double maxIntv_of_minY = 0d;
        double minIntv_of_maxY = 0d;
        double maxIntv_of_maxY = 0d;

        if (Double.compare(sortedValues.get(0), 0) >= 0) {
            // Pfeil von unten hoch und wieder runter durch jeweils beide y Ebenen.
            minIntv_of_minY = sortedValues.get(0);
            maxIntv_of_minY = sortedValues.get(1);
            minIntv_of_maxY = sortedValues.get(2);
            maxIntv_of_maxY = sortedValues.get(3);
            enteringPlaneMin = Direction.DOWN;
            enteringPlaneMax = Direction.UP;
            DebugPrint.println("// Pfeil von unten hoch und wieder runter durch jeweils beide y Ebenen.");
            DebugPrint.println(minIntv_of_minY + " " + maxIntv_of_minY + " " + minIntv_of_maxY + " " + maxIntv_of_maxY);
        } else if (Double.compare(sortedValues.get(0), 0) < 0 && Double.compare(sortedValues.get(1), 0) >= 0) {
            //Pfeil von Mitte hoch und dann wieder runter durch jeweils beide y Ebenen hindurch.
            minIntv_of_minY = 0d;
            maxIntv_of_minY = sortedValues.get(1);
            minIntv_of_maxY = sortedValues.get(2);
            maxIntv_of_maxY = sortedValues.get(3);
            enteringPlaneMax = Direction.UP;
            DebugPrint.println("//Pfeil von Mitte hoch und dann wieder runter durch jeweils beide y Ebenen hindurch.");
            DebugPrint.println(minIntv_of_minY + " " + maxIntv_of_minY + " " + minIntv_of_maxY + " " + maxIntv_of_maxY);
        } else if (Double.compare(sortedValues.get(1), 0) < 0 && Double.compare(sortedValues.get(2), 0) >= 0) {
            // Von oben hoch und irgendwann wieder runter durch y1&2; Bzw. ist auch Fall von oben runter einfach gesagt.
            // Außerdem von unten durch min Ebene aber nicht mehr max Ebene und wieder runter.
            minIntv_of_minY = Double.NaN; //Leeres Intervall -> sollte nachher durch die 0-Differenz übersprungen werden.
            maxIntv_of_minY = Double.NaN;
            minIntv_of_maxY = sortedValues.get(2);
            maxIntv_of_maxY = sortedValues.get(3);
            // vergleich 1ne ebene mit pfeil start pos => was nachher rauskommt eintritt obere ebene oder untere
            enteringPlaneMax = Direction.DOWN;
            if (Double.compare(arrow.getY() + arrow.getBoundingBox().getCenter().getY(), playerAABB.maxY) >= 0) {
                enteringPlaneMax = Direction.UP;
            }
            DebugPrint.println("/ Von oben hoch und irgendwann wieder runter durch y1&2; Bzw. ist auch Fall von oben runter einfach gesagt. // Außerdem von unten durch min Ebene aber nicht mehr max Ebene und wieder runter.");
            DebugPrint.println(minIntv_of_minY + " " + maxIntv_of_minY + " " + minIntv_of_maxY + " " + maxIntv_of_maxY);
        } else if (Double.compare(sortedValues.get(2), 0) < 0 && Double.compare(sortedValues.get(3), 0) >= 0) {
            //Pfeil geht von Mitte nach unten durch min Ebene.
            minIntv_of_minY = Double.NaN; //Leeres Intervall -> sollte nachher durch die 0-Differenz übersprungen werden.
            maxIntv_of_minY = Double.NaN;
            minIntv_of_maxY = 0d;
            maxIntv_of_maxY = sortedValues.get(3);
            DebugPrint.println("//Pfeil geht von Mitte nach unten durch min Ebene.");
            DebugPrint.println(minIntv_of_minY + " " + maxIntv_of_minY + " " + minIntv_of_maxY + " " + maxIntv_of_maxY);
        } else {
            DebugPrint.println("//Pfeil geht durch nichts");
            DebugPrint.println(minIntv_of_minY + " " + maxIntv_of_minY + " " + minIntv_of_maxY + " " + maxIntv_of_maxY);
        }            /* else if (Double.compare(sortedValues.get(3), 0) < 0) {
            minIntv_of_minY = 0d; //Leeres Intervall -> sollte nachher durch die 0-Differenz übersprungen werden.
            maxIntv_of_minY = 0d;
            minIntv_of_maxY = 0d; //Leeres Intervall -> sollte nachher durch die 0-Differenz übersprungen werden.
            maxIntv_of_maxY = 0d;
        }*/

        if(!Double.isNaN(minIntv_of_minY) && !Double.isNaN(maxIntv_of_minY)) {
            feedback.setFirst(new Interval<>(Math.floor(minIntv_of_minY), Math.floor(maxIntv_of_minY), enteringPlaneMin));
        }
        if(!Double.isNaN(minIntv_of_maxY) && !Double.isNaN(maxIntv_of_maxY)) {
            if (feedback.getFirst() != null) feedback.setSecond(new Interval<>(Math.floor(minIntv_of_maxY), Math.floor(maxIntv_of_maxY), enteringPlaneMax)); //FIXME: Done? insertion order
            else feedback.setFirst(new Interval<>(Math.floor(minIntv_of_maxY), Math.floor(maxIntv_of_maxY), enteringPlaneMax));
        }
        if (feedback.getFirst() != null) {
            DebugPrint.println("first calculateCollision " + feedback.getFirst().getMin() + " " + feedback.getFirst().getMax());
        }
        if (feedback.getSecond() != null) {
            DebugPrint.println("second calculateCollision " + feedback.getSecond().getMin() + " " + feedback.getSecond().getMax());
        }
        DebugPrint.println("Y feedback: " + feedback.toString());
        //DebugPrint.println("Y feedback: " + feedback);
        return feedback;
    }

    public enum DirXZ {
        X,
        Z
    }

    public static CollisionFeedback calculateCollisionXZ(final double x1, final double x2, final ArrowEntity arrow, final Box playerAABB, final DirXZ dir) {
        //xCollision
        // order points of time
        // special cases: arrow does not collide with planes --> result either negative number or NaN
        double xmin = Double.NaN;
        double xmax = Double.NaN;
        final CollisionFeedback feedback = new CollisionFeedback();
        Direction enteringPlane = null;
        final double minPos = dir.equals(DirXZ.X) ? playerAABB.minX : playerAABB.minZ;
        final double maxPos = dir.equals(DirXZ.X) ? playerAABB.maxX : playerAABB.maxZ;
        final double arrowPos = dir.equals(DirXZ.X) ? arrow.getX() : arrow.getZ();
        if (Double.isNaN(x1) && Double.isNaN(x2)) {
            if (minPos <= arrowPos && arrowPos <= maxPos) {
                xmin = 0d;
                xmax = Double.POSITIVE_INFINITY;
                DebugPrint.println("Pfeil bewegt sich zwischen minPos und maxPos von " + dir.name() + " ||xmin: " + xmin + "xmax: " + xmax);
            } else {
                DebugPrint.println("Pfeil bewegt sich zwischen nichts" + " ||xmin: " + xmin + "xmax: " + xmax);
            }
        } else if (Double.isNaN(x1) && Double.compare(x2, 0) >= 0) {
            xmin = x2;
            xmax = Double.POSITIVE_INFINITY;
            enteringPlane = dir.equals(DirXZ.X) ? Direction.EAST : Direction.SOUTH;
            DebugPrint.println("Pfeil bewegt sich Richtung max und tauchte innen auf" + " ||xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.isNaN(x1) && Double.compare(x2, 0) < 0) {
            xmin = 0;
            xmax = Double.POSITIVE_INFINITY;
            enteringPlane = null;
            DebugPrint.println("Pfeil taucht in der Mitte auf und bleibt dort" + " ||xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.compare(x1, 0) >= 0 && Double.isNaN(x2)) {
            xmin = x1;
            xmax = Double.POSITIVE_INFINITY;
            enteringPlane = dir.equals(DirXZ.X) ? Direction.WEST : Direction.NORTH;
            DebugPrint.println("Pfeil bewegt sich Richtung min und tauchte innen auf" + " || xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.compare(x1, 0) < 0 && Double.isNaN(x2)) {
            xmin = 0;
            xmax = Double.POSITIVE_INFINITY;
            enteringPlane = null;
            DebugPrint.println("Pfeil taucht in der Mitte auf und bleibt dort" + " || xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.compare(x1, 0) >= 0 && Double.compare(x2, 0) >= 0) {
            DebugPrint.println("Min und max stehen noch vor Pfeil" + " ||xmin: " + xmin + "xmax: " + xmax);
            if (Double.compare(x1, x2) <= 0) {
                xmin = x1;
                xmax = x2;
                enteringPlane = dir.equals(DirXZ.X) ? Direction.WEST : Direction.NORTH;
                DebugPrint.println("Min ist aber näher" + " ||xmin: " + xmin + "xmax: " + xmax);
            } else {
                xmin = x2;
                xmax = x1;
                enteringPlane = dir.equals(DirXZ.X) ? Direction.EAST : Direction.SOUTH;
                DebugPrint.println("Max ist aber näher" + " ||xmin: " + xmin + "xmax: " + xmax);
            }
        } else if (Double.compare(x1, 0) < 0 && Double.compare(x2, 0) < 0) {
            //xmin = Double.NaN; //Already assigned
            //xmax = Double.NaN;
            DebugPrint.println("Alles liegt hinter Pfeil" + " ||xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.compare(x1, 0) < 0 && Double.compare(x2, 0) >= 0) {
            // komisches Bild, oder wir stehen vorm Dispenser
            xmin = 0d;
            xmax = x2;
            DebugPrint.println("Pfeil fliegt auf max zu, hat aber min hinter sich" + " ||xmin: " + xmin + "xmax: " + xmax);
        } else if (Double.compare(x1, 0) >= 0 && Double.compare(x2, 0) < 0) {
            // komisches Bild oder wir stehen vorm Dispenser
            xmin = 0d;
            xmax = x1;
            DebugPrint.println("Pfeil fliegt auf min zu, hat aber max hinter sich" + " ||xmin: " + xmin + "xmax: " + xmax);
        } else if ((Double.compare(x1, 0) < 0 && Double.isNaN(x2)) || (Double.compare(x2, 0) < 0 && Double.isNaN(x1))) {
            xmin = 0d;
            xmax = Double.POSITIVE_INFINITY;
            DebugPrint.println("Pfeil hat alles hinter sich und ist plötzlich aufgetaucht oder verschwunden" + " ||xmin: " + xmin + "xmax: " + xmax);
        }

        if(!Double.isNaN(xmin) && !Double.isNaN(xmax)) {
            feedback.setFirst(new Interval<>(Math.floor(xmin), Math.floor(xmax), enteringPlane));
        }

        DebugPrint.println(dir + " feedback: " + feedback);
        return feedback;
    }

    //FIXME: Build independence from AABB location 22.11.22
    public static CollisionFeedback calculateCollisionX(ArrowEntity arrow, Box playerAABB) {
        DebugPrint.println("calculateCollisionX without movement");
        // collision with x-plane (in ticks)
        //final double x1 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.minX);
        //final double x2 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.maxX);

        /*
        final ThreadContainer<Double> containerX1 = new ThreadContainer<>();
        final Thread threadX1 = new Thread(() -> {
            final long nanos = System.nanoTime();
            final double x1 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.minX);
            System.out.println("calculateCollisionX threadX1: " + (System.nanoTime()-nanos));
            containerX1.setTraceResult(x1);
        }); threadX1.start();

        final ThreadContainer<Double> containerX2 = new ThreadContainer<>();
        final Thread threadX2 = new Thread(() -> {
            final long nanos = System.nanoTime();
            final double x2 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.maxX);
            System.out.println("calculateCollisionX threadX2: " + (System.nanoTime()-nanos));
            containerX2.setTraceResult(x2);
        }); threadX2.start();

        try {
            threadX1.join();
            threadX2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        final double x1 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.minX);
        final double x2 = getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), playerAABB.maxX);

        DebugPrint.println("x1: " + x1);
        DebugPrint.println("x2: " + x2);
        return calculateCollisionXZ(x1, x2, arrow, playerAABB, DirXZ.X);
    }

    //FIXME: Build independence from AABB location 22.11.22
    public static CollisionFeedback calculateCollisionZ(ArrowEntity arrow, Box playerAABB) {
        DebugPrint.println("calculateCollisionZ without movement");
        // collision with z-plane (in ticks)
        //final double z1 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.minZ);
        //final double z2 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.maxZ);

        /*final ThreadContainer<Double> containerZ1 = new ThreadContainer<>();
        final Thread threadZ1 = new Thread(() -> {
            final long nanos = System.nanoTime();
            final double z1 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.minZ);
            System.out.println("calculateCollisionZ threadX1: " + (System.nanoTime()-nanos));
            containerZ1.setTraceResult(z1);
        }); threadZ1.start();

        final ThreadContainer<Double> containerZ2 = new ThreadContainer<>();
        final Thread threadZ2 = new Thread(() -> {
            final long nanos = System.nanoTime();
            final double z2 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.maxZ);
            System.out.println("calculateCollisionZ threadX1: " + (System.nanoTime()-nanos));
            containerZ2.setTraceResult(z2);
        }); threadZ2.start();

        try {
            threadZ1.join();
            threadZ2.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        final double z1 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.minZ);
        final double z2 = getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), playerAABB.maxZ);

        DebugPrint.println("z1: " + z1);
        DebugPrint.println("z2: " + z2);
        return calculateCollisionXZ(z1, z2, arrow, playerAABB, DirXZ.Z);
    }

    /*private static Interval<Long> calcIntervalIntersection(final Interval<Double> first, final Interval<Double> second) {
        final long min = ((Double)Math.max(first.getMin(), second.getMin())).longValue();
        final long max = ((Double)Math.min(first.getMax(), second.getMax())).longValue();
        final Direction enteringPlane = Double.compare(first.getMin(), second.getMin()) >= 0 ? first.getEnteringPlaneName() : second.getEnteringPlaneName();
        return max < min ? null : new Interval<>(min, max, enteringPlane);
    }

    private static Interval<Long> calcFinalCollisionInterval(final FeedbackXYZ feedback, final boolean firstInterval) {
        final Interval<Double> yInterval = (firstInterval ? feedback.getY().getFirst() : feedback.getY().getSecond());
        if (feedback.getX().getFirst() == null || yInterval == null || feedback.getZ().getFirst() == null) {
            return null;
        } else {
            final Interval<Long> i1 = calcIntervalIntersection(feedback.getX().getFirst(), yInterval);
            return (i1 == null) ? null : calcIntervalIntersection(new Interval<>(i1.getMin().doubleValue(),
                    i1.getMax().doubleValue(), i1.getEnteringPlaneName()), feedback.getZ().getFirst());
        }
    }*/

    private static void printFeed(FeedbackXYZ feedback) {
        CollisionFeedback cx = feedback.getX();
        CollisionFeedback cy = feedback.getY();
        CollisionFeedback cz = feedback.getZ();

        if (cx != null) {
            if (cx.getFirst() != null) {
                DebugPrint.println("////cx first////");
                DebugPrint.println(cx.getFirst().getMin());
                DebugPrint.println(cx.getFirst().getMax());
                DebugPrint.println(cx.getFirst().getEnteringPlaneName());
                DebugPrint.println("////cx////");
            }
            if (cx.getSecond() != null) {
                DebugPrint.println("////cx second////");
                DebugPrint.println(cx.getSecond().getMin());
                DebugPrint.println(cx.getSecond().getMax());
                DebugPrint.println(cx.getSecond().getEnteringPlaneName());
                DebugPrint.println("////cx////");
            }
        }

        if (cy != null) {
            if (cy.getFirst() != null) {
                DebugPrint.println("////cy first////");
                DebugPrint.println(cy.getFirst().getMin());
                DebugPrint.println(cy.getFirst().getMax());
                DebugPrint.println(cy.getFirst().getEnteringPlaneName());
                DebugPrint.println("////cy////");
            }
            if (cy.getSecond() != null) {
                DebugPrint.println("////cy second////");
                DebugPrint.println(cy.getSecond().getMin());
                DebugPrint.println(cy.getSecond().getMax());
                DebugPrint.println(cy.getSecond().getEnteringPlaneName());
                DebugPrint.println("////cy////");
            }
        }

        if (cz != null) {
            if (cz.getFirst() != null) {
                DebugPrint.println("////cz first////");
                DebugPrint.println(cz.getFirst().getMin());
                DebugPrint.println(cz.getFirst().getMax());
                DebugPrint.println(cz.getFirst().getEnteringPlaneName());
                DebugPrint.println("////cz////");
            }
            if (cz.getSecond() != null) {
                DebugPrint.println("////cz second////");
                DebugPrint.println(cz.getSecond().getMin());
                DebugPrint.println(cz.getSecond().getMax());
                DebugPrint.println(cz.getSecond().getEnteringPlaneName());
                DebugPrint.println("////cz////");
            }
        }
    }

    public static TraceResult getTraceResultFromFeedback(final CollisionFeedback feedbackX,
                                                          final CollisionFeedback feedbackY,
                                                          final CollisionFeedback feedbackZ,
                                                          final ArrowEntity arrow,
                                                          final Box box,
                                                          final double startTick, final double endTick) {
        //final FeedbackXYZ feedback = new FeedbackXYZ(feedbackX, feedbackY, feedbackZ);
        //final Interval<Long> first = calcFinalCollisionInterval(feedback, true);
        //final Interval<Long> second = calcFinalCollisionInterval(feedback, false);
        //return new TraceResult(first, second, arrow, box);
        if (feedbackX.isEmpty() || feedbackY.isEmpty() || feedbackZ.isEmpty()) {
            DebugPrint.println("NO INTERSECTION WITH X AND Y AND Z.");
            if (feedbackX.isEmpty()) DebugPrint.println("feedbackX is empty"); else DebugPrint.println("feedbackX: " + feedbackX.toString());
            if (feedbackY.isEmpty()) DebugPrint.println("feedbackY is empty"); else DebugPrint.println("feedbackY: " + feedbackY.toString());
            if (feedbackZ.isEmpty()) DebugPrint.println("feedbackZ is empty"); else DebugPrint.println("feedbackZ: " + feedbackZ.toString());
            return new TraceResult(null, null, arrow, box);
        } else {
            if (feedbackX.isEmpty()) DebugPrint.println("feedbackX is empty"); else DebugPrint.println("feedbackX: " + feedbackX.toString());
            if (feedbackY.isEmpty()) DebugPrint.println("feedbackY is empty"); else DebugPrint.println("feedbackY: " + feedbackY.toString());
            if (feedbackZ.isEmpty()) DebugPrint.println("feedbackZ is empty"); else DebugPrint.println("feedbackZ: " + feedbackZ.toString());
        }

        final Interval<Long>[] res = IntersectionHelper.calcIntervalIntersection(
                feedbackX.toArray().get(),//TODO: ERROR: No value present
                feedbackY.toArray().get(),//TODO: ERROR: No value present
                feedbackZ.toArray().get(), startTick, endTick); //TODO: ERROR: java.util.NoSuchElementException: No value present # ERROR: Pfeil fliegt auf max zu, hat aber min hinter sich ||xmin: 0.0xmax: NaN
        DebugPrint.println("Len of res: " + res.length);
        if (res.length > 3) throw new IllegalStateException("more than 3 intervals??");
        DebugPrint.println("<= 3");
        if (res.length > 2) return new TraceResult(res[0], res[1], res[2], arrow, box);
        DebugPrint.println("<= 2");
        if (res.length > 1) return new TraceResult(res[0], res[1], arrow, box);
        DebugPrint.println("<= 1");
        if (res.length > 0) return new TraceResult(res[0], null, arrow, box);
        DebugPrint.println("= 0");
        return new TraceResult(null, null, arrow, box);
        //TODO essential: abrunden der Intervalle erst nach bestimmen der entering plane (sonst wissen wir bei gleichem gerundeten Eintrittstick nicht, wo wir den Blockier-Block setzen müssen)
    }

    public static TraceResult calculateCollision(final ArrowEntity arrow, final BlockPos pos, final CollisionFeedback feedbackY) {
        Box box = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
        return getTraceResultFromFeedback(calculateCollisionX(arrow, box), feedbackY, calculateCollisionZ(arrow, box), arrow, box, 0d, 200d);
        /*final FeedbackXYZ feedback = new FeedbackXYZ(calculateCollisionX(arrow, box), feedbackY, calculateCollisionZ(arrow, box));
        final Interval<Long> first = calcFinalCollisionInterval(feedback, true);
        final Interval<Long> second = calcFinalCollisionInterval(feedback, false);
        return new TraceResult(first, second, arrow, box);*/
    }

    public static final double APPROX_WALKING_DIST = 0.21585d;

    public static TraceResult calculateCollision(final ArrowEntity arrow, final Box playerAABB, final Vec3d manualPlayerVel, final Vec3d playerStartPos, final Vec3d target, final World world, final SimMovementState sms) {
        try {
            DebugPrint.println("playerStartPos: " + playerStartPos.toString());
            final float yaw = YawHelper.vecToYaw(playerStartPos, target);
            DebugPrint.println("Target: " + target.toString());
            DebugPrint.println("yaw:" + yaw);

            final double dist = playerStartPos.distanceTo(target);
            final double endTick = dist / APPROX_WALKING_DIST + 20;

            DebugPrint.println("dist: " + dist);
            DebugPrint.println("endTick: " + endTick);

            return calculateCollision(arrow, playerAABB, manualPlayerVel, endTick, yaw, playerStartPos, world, sms, target);
        } catch (IllegalStateException e) {
            DebugPrint.println(e);
        }
        return null;
    }

    public static TraceResult calculateCollision(final ArrowEntity arrow, final Box playerAABB, final Vec3d manualPlayerVel/*, final Box playerAABB*//*, final double startTick*/,
                                                       final double endTick, final float yaw, final Vec3d playerStartPos, final World world, final SimMovementState sms, final Vec3d target) {
        DebugPrint.println("calculateCollision executed");
        final long totalnanos = System.nanoTime();
        //final Box playerAABB = player.getBoundingBox();
        //final Box expandedPlayerAABB = playerAABB.expand(0.2f);
        final Box expandedPlayerAABB = playerAABB.expand(0.45f); //FIXME: actually or perhaps, actually 0.55f... maybe idk

        final long nanosy = System.nanoTime();
        final CollisionFeedback cfy = (!sms.equals(SimMovementState.SIM_STAND) && Double.compare(Math.abs(manualPlayerVel.getY()), 0.1d) > 0) ? CSAlgorithm.calculateCollisionsY(0, endTick, arrow.getVelocity(), arrow.getBoundingBox().getCenter(), manualPlayerVel, playerStartPos, expandedPlayerAABB, true) : calculateCollisionY(arrow, expandedPlayerAABB);
        System.out.println("nanosy: " + (System.nanoTime()-nanosy));

        if (!sms.equals(SimMovementState.SIM_STAND) && (Double.compare(Math.abs(manualPlayerVel.getX()), 0.003d) >= 0 || Double.compare(Math.abs(manualPlayerVel.getZ()), 0.003d) >= 0) || sms.equals(SimMovementState.SIM_MOVE)) {
            //                                                                       endTick, arrowVel,              arrowPos,            optPlayerVel, playerStartPos,playerAABB, yaw, unpreciseEndPosDistanceMultiplicator,final World world
            final Optional<VecXZ> optManualPlayerVel = Optional.of(new VecXZ(manualPlayerVel.getX(), manualPlayerVel.getZ()));
            //FIXME: ok so we choose to have manual player vel but not manual arrow vel
            final VecXZ arrowVelXZ = new VecXZ(arrow.getVelocity().getX(), arrow.getVelocity().getZ());
            final VecXZ arrowPosXZ = new VecXZ(arrow.getPos().getX(), arrow.getPos().getZ());

            final long nanos = System.nanoTime();
            final CollisionFeedbackXZ feedbackXZ = CSAlgorithm.calculateCollisionsXZ(endTick, arrowVelXZ, arrowPosXZ, optManualPlayerVel, playerStartPos, expandedPlayerAABB, yaw, world, target);
            System.out.println("CSAlgorithm.calculateCollisionsXZ nanos: " + (System.nanoTime()-nanos));
            final CollisionFeedback cfx = feedbackXZ.getX();
            final CollisionFeedback cfz = feedbackXZ.getZ();
            System.out.println("total nanos vA1: " + (System.nanoTime()-totalnanos));
            final TraceResult traceResult = getTraceResultFromFeedback(cfx, cfy, cfz, arrow, playerAABB, 0, endTick);
            System.out.println("total nanos vA2: " + (System.nanoTime()-totalnanos));
            return traceResult;
        }
        DebugPrint.println("calc Collision of still player");

        final CollisionFeedback cfx = calculateCollisionX(arrow, expandedPlayerAABB);
        final CollisionFeedback cfz = calculateCollisionX(arrow, expandedPlayerAABB);

        System.out.println("total nanos vB1: " + (System.nanoTime()-totalnanos));
        final TraceResult traceResult = getTraceResultFromFeedback(cfx, cfy, cfz, arrow, playerAABB, 0, endTick);
        System.out.println("total nanos vB2: " + (System.nanoTime()-totalnanos));
        return traceResult;
    }

    /*
    public static TraceResult calculateCollisionThread2(final ArrowEntity arrow, final Box playerAABB, final Vec3d manualPlayerVel,
                                                 final double endTick, final float yaw, final Vec3d playerStartPos, final World world, final SimMovementState sms, final Vec3d target) {
        DebugPrint.println("calculateCollision executed");
        //final Box playerAABB = player.getBoundingBox();
        //final Box expandedPlayerAABB = playerAABB.expand(0.2f);
        final Box expandedPlayerAABB = playerAABB.expand(0.25f); //FIXME: actually or perhaps, actually 0.55f... maybe idk

        //final CollisionFeedback cfy = null;
        final ThreadContainer<CollisionFeedback> cfyContainer = new ThreadContainer<>();
        final Runnable cfyRunnable = () -> {
            final long nanos = System.nanoTime();
            final CollisionFeedback cfy = (!sms.equals(SimMovementState.SIM_STAND) && Double.compare(Math.abs(manualPlayerVel.getY()), 0.1d) > 0) ? CSAlgorithm.calculateCollisionsY(0, endTick, arrow.getVelocity(), arrow.getBoundingBox().getCenter(), manualPlayerVel, playerStartPos, expandedPlayerAABB, true) : calculateCollisionY(arrow, expandedPlayerAABB);
            System.out.println("y calc nanos: " + (System.nanoTime()-nanos));
            cfyContainer.setTraceResult(cfy);
        };
        final Thread cfyThread = new Thread(cfyRunnable);
        cfyThread.start();

        if (!sms.equals(SimMovementState.SIM_STAND) && (Double.compare(Math.abs(manualPlayerVel.getX()), 0.003d) >= 0 || Double.compare(Math.abs(manualPlayerVel.getZ()), 0.003d) >= 0) || sms.equals(SimMovementState.SIM_MOVE)) {
            //                                                                       endTick, arrowVel,              arrowPos,            optPlayerVel, playerStartPos,playerAABB, yaw, unpreciseEndPosDistanceMultiplicator,final World world
            final Optional<VecXZ> optManualPlayerVel = Optional.of(new VecXZ(manualPlayerVel.getX(), manualPlayerVel.getZ()));
            //FIXME: ok so we choose to have manual player vel but not manual arrow vel
            final VecXZ arrowVelXZ = new VecXZ(arrow.getVelocity().getX(), arrow.getVelocity().getZ());
            final VecXZ arrowPosXZ = new VecXZ(arrow.getPos().getX(), arrow.getPos().getZ());

            final long threadnanos = System.nanoTime();
            final ThreadContainer<CollisionFeedbackXZ> cfxzContainer = new ThreadContainer<>();
            final Runnable cfxzRunnable = () -> {
                final long nanos = System.nanoTime();
                final CollisionFeedbackXZ feedbackXZ = CSAlgorithm.calculateCollisionsXZ(endTick, arrowVelXZ, arrowPosXZ, optManualPlayerVel, playerStartPos, expandedPlayerAABB, yaw, world, target);
                System.out.println("CSAlgorithm.calculateCollisionsXZ nanos: " + (System.nanoTime()-nanos));
                cfxzContainer.setTraceResult(feedbackXZ);
            };
            final Thread cfxzThread = new Thread(cfxzRunnable);
            cfxzThread.start();

            DebugPrint.println("calc Collision of moving player");
            try {
                cfxzThread.join();
                System.out.println("xz thread nanos: " + (System.nanoTime()-threadnanos));
                cfyThread.join();

                final CollisionFeedback cfy = cfyContainer.getTraceResult().get();
                final CollisionFeedback cfx = cfxzContainer.getTraceResult().get().getX();
                final CollisionFeedback cfz = cfxzContainer.getTraceResult().get().getZ();
                return getTraceResultFromFeedback(cfx, cfy, cfz, arrow, playerAABB, 0, endTick);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        DebugPrint.println("calc Collision of still player");

        final ThreadContainer<CollisionFeedback> cfxContainer = new ThreadContainer<>();
        final Runnable cfxRunnable = () -> {
            final CollisionFeedback cfx = calculateCollisionX(arrow, expandedPlayerAABB);
            cfxContainer.setTraceResult(cfx);
        };
        final Thread cfxThread = new Thread(cfxRunnable);
        cfxThread.start();

        final ThreadContainer<CollisionFeedback> cfzContainer = new ThreadContainer<>();
        final Runnable cfzRunnable = () -> {
            final CollisionFeedback cfz = calculateCollisionX(arrow, expandedPlayerAABB);
            cfzContainer.setTraceResult(cfz);
        };
        final Thread cfzThread = new Thread(cfzRunnable);
        cfzThread.start();

        try {
            cfyThread.join();
            cfxThread.join();
            cfzThread.join();
            final CollisionFeedback cfy = cfyContainer.getTraceResult().get();
            final CollisionFeedback cfx = cfxContainer.getTraceResult().get();
            final CollisionFeedback cfz = cfzContainer.getTraceResult().get();
            return getTraceResultFromFeedback(cfx, cfy, cfz, arrow, playerAABB, 0, endTick);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/

    /*public static Thread calculateCollisionThread(final ArrowEntity arrow, final Box playerAABB, final Vec3d manualPlayerVel, final Vec3d playerStartPos, final Vec3d target, final World world, final SimMovementState sms,
                                                final ThreadContainer<TraceResult> threadContainer) {
        final Runnable runnable = () -> {
            final TraceResult traceResult = calculateCollision(arrow, playerAABB, manualPlayerVel, playerStartPos, target, world, sms);
            if (traceResult != null) {
                threadContainer.setTraceResult(traceResult);
            }
        };
        final Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    public static Thread calculateCollisionThread(final ArrowEntity arrow, final Box playerAABB, final Vec3d manualPlayerVel, final Vec3d playerStartPos, final Vec3d target, final World world, final SimMovementState sms,
                                                final TickableTreeMap tickableTreeMap) {
        final Runnable runnable = () -> {
            final TraceResult traceResult = calculateCollision(arrow, playerAABB, manualPlayerVel, playerStartPos, target, world, sms);
            if (traceResult != null) {
                tickableTreeMap.put(traceResult, arrow);
            }
        };
        final Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }*/

        /*
        public static TraceResult calculateCollision(final ArrowEntity arrow, final LivingEntity player, final double startTick, final double endTick) {
        DebugPrint.println("calculateCollision executed");
        final Box playerAABB = player.getBoundingBox();
        //final Box expandedPlayerAABB = playerAABB.expand(0.2f);
        final Box expandedPlayerAABB = playerAABB.expand(0.2f);
        final Vec3d playerVel = player.getVelocity();
        final Vec3d inMotionPlayerVel = VelocityHelper.getExpectedVelocityFromPlayer(player);
        final CollisionFeedback cfx = Double.compare(Math.abs(playerVel.getX()), -10d) > 0 ? CSAlgorithm.calculateCollisionX(arrow, inMotionPlayerVel, player.getPos(), startTick, endTick, expandedPlayerAABB.expand(0.2f)) : calculateCollisionX(arrow, expandedPlayerAABB);
        final CollisionFeedback cfy = Double.compare(Math.abs(playerVel.getY()), 10d) > 0 ? CSAlgorithm.calculateCollisionY(arrow, playerVel, player.getPos(), startTick, endTick, expandedPlayerAABB.expand(0.2f), false) : calculateCollisionY(arrow, expandedPlayerAABB);
        final CollisionFeedback cfz = Double.compare(Math.abs(playerVel.getZ()), -10d) > 0 ? CSAlgorithm.calculateCollisionZ(arrow, inMotionPlayerVel, player.getPos(), startTick, endTick, expandedPlayerAABB.expand(0.2f)) : calculateCollisionZ(arrow, expandedPlayerAABB);
        return getTraceResultFromFeedback(cfx, cfy, cfz, arrow, playerAABB, startTick, endTick);
    }
     */

    //TODO: die Intervalle, die wir ausrechnen, sind völlig unabhängig von starttick und endtick. Wenn ein Intervall also (teilweise) außerhalb liegt, müssen wir das anpassen
}