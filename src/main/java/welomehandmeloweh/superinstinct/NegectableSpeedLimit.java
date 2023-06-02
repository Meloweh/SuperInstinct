/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

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
public class NegectableSpeedLimit {
    private static final Optional<Double> getIntersectionOutsideToInsideZ(final double m, final float q, final double v_0) {
        final double a = 1 / (v_0 - (m*q)/(1-q));
        final double b = -((m*q) / (1-q));
        final double a_positive = a * (b + (-0.003d));
        final double a_negative = a * (b - (-0.003d));
        //It is expected that this method is called only if v_0 is ensured not to be between -0.003 and 0.003
        final boolean isV0Positive = Double.compare(v_0, 0.003d) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        return isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty();
    }

    private static final Optional<Double> getIntersectionOutsideToInsideX(final double m, final float q, final double v_0) {
        final double a = 1 / (v_0 + (m*q)/(1-q));
        final double b = (m*q) / (1-q);
        final double a_positive = a * (b + 0.003d);
        final double a_negative = a * (b - 0.003d);
        //It is expected that this method is called only if v_0 is ensured not to be between -0.003 and 0.003
        final boolean isV0Positive = Double.compare(v_0, 0.003d) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        return isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty();
    }

    private static final Optional<Double> getIntersectionInsideToOutside(final double m, final float q) {
        // m sollte als mX bzw mZ bereits durch den Aufrufer von neglectableSpeedLimit passend weitergegeben sein
        return Math.abs(m * q) >= 0.003 ? Optional.empty() : Optional.of(0.003 / (m * q));
    }

    private static double playerFormularXZ(final double m, final float q, final double v_0, final Axis axis, final double t, final SpeedRecreation2_3.VelocityState velocityState) {
        return axis.equals(Axis.X) ?
                !velocityState.equals(SpeedRecreation2_3.VelocityState.Middle) ?
                        CSAlgorithm.tracePlayerPosXDelta(m, q, v_0, t)
                        :
                        -m * t
                :
                (!velocityState.equals(SpeedRecreation2_3.VelocityState.Middle)) ?
                        CSAlgorithm.tracePlayerPosZDelta(m, q, v_0, t)
                        :
                        m * t;
    }

    private static double deltaVelFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        if (axis.equals(Axis.X)) {
            final double v_0 = (Double.compare(v, 0d) == 0) ? ((-m * q - m) * q) : v;
            return v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q));
        }
        final double v_0 = (Double.compare(v, 0d) == 0) ? ((m * q + m) * q) : v;
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q));
    }

    private static double velFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        return deltaVelFormular(t, axis, v, q, m) * q;
    }

    /*private static double neglectableSpeedLimit(final double m, final float q, final double v_0prev, final Axis axis, final double endTick, final double walkedTicks, final boolean shouldConsiderStartVelWhenInMiddle) {
        final double v_1 = shouldConsiderStartVelWhenInMiddle ? (axis.equals(Axis.X) ? (-m * q - m * Math.pow(q, 2)) : (m * q + m * Math.pow(q, 2))) : v_0prev;
        final SpeedRecreation2_3.VelocityState starterVelocityState = Double.compare(v_0prev, 0d) >= 0 ? SpeedRecreation2_3.VelocityState.Positive : SpeedRecreation2_3.VelocityState.Negative;
        final SpeedRecreation2_3.VelocityState nonStarterVelocityState = (Double.compare(v_1, 0.003d) >= 0 ? SpeedRecreation2_3.VelocityState.Positive :
                Double.compare(v_1, -0.003d) > 0 ? SpeedRecreation2_3.VelocityState.Middle :
                        SpeedRecreation2_3.VelocityState.Negative);
        final boolean nonStarterMiddle = nonStarterVelocityState.equals(SpeedRecreation2_3.VelocityState.Middle);

        final double v_0 = nonStarterMiddle ? 0 : v_0prev;
        DebugPrint.println("m: " + m + " q: " + q + " v_0: " + v_0);
        final Optional<Double> intersection = nonStarterMiddle ? getIntersectionInsideToOutside(m, q) :
                axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_1) : getIntersectionOutsideToInsideZ(m,q,v_1);
        final double rawt = (intersection.isEmpty() || intersection.get().equals(Double.NaN) || Double.compare(intersection.get(), 0) < 0) ? (endTick - walkedTicks) : intersection.get();
        final double t = Double.compare(walkedTicks + rawt, endTick) > 0 ? (endTick - walkedTicks) : rawt;
        //FIXME IMPORTANT!!!!!!: m of the past needs to be reconstructed for "playerFormularXZ(m..."!!!!!!!!!!!!!
        final double formula = (shouldConsiderStartVelWhenInMiddle && nonStarterMiddle) ?
                playerFormularXZ(m,q,v_0,axis,0, starterVelocityState) + playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState)
                :
                playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState);
        DebugPrint.println("[ngs at " + "v_0: " + v_0 + " v_0prev: " + v_0prev + " rawt: " + rawt + " formula: " + formula + " acounter" + ": " + "msum" + "]" + " (debug= t:" +t+ " walkedTicks: " + walkedTicks + " endTick:" + endTick + " intersec: " + (intersection.isPresent() ? intersection.get() : "empty") + " bool: " + ((intersection.isEmpty() || intersection.get().equals(Double.NaN))));
        if (Double.compare(t, 0d) == 0 || Double.compare(t, -0d) == 0 || Double.compare(walkedTicks + t, endTick) >= 0) return formula;
        final double v_0new = nonStarterMiddle ? (axis.equals(Axis.X) ? -m : m)*t*q : velFormular(t, axis, v_0, q, m);
        return formula + neglectableSpeedLimit(m, q, v_0new, axis, endTick, walkedTicks + t, false);
    }*/

    public static double neglectableSpeedLimit(final double m, final float q, final double v_0prev, final Axis axis, final double endTick, final double walkedTicks,
                                               final boolean shouldConsiderStartVelWhenInMiddle, final Optional<SplittedProgress> optSplittedProgress,
                                               final Optional<List<NSLSplitter>> optNslSplitters, final Optional<Double> optLocalStartPos) {
        final double v_1 = shouldConsiderStartVelWhenInMiddle ? (axis.equals(Axis.X) ? (-m * q - m * Math.pow(q, 2)) : (m * q + m * Math.pow(q, 2))) : v_0prev;
        final SpeedRecreation2_3.VelocityState starterVelocityState = Double.compare(v_0prev, 0d) >= 0 ? SpeedRecreation2_3.VelocityState.Positive : SpeedRecreation2_3.VelocityState.Negative;
        final SpeedRecreation2_3.VelocityState nonStarterVelocityState = (Double.compare(v_1, 0.003d) >= 0 ? SpeedRecreation2_3.VelocityState.Positive :
                Double.compare(v_1, -0.003d) > 0 ? SpeedRecreation2_3.VelocityState.Middle :
                        SpeedRecreation2_3.VelocityState.Negative);
        final boolean nonStarterMiddle = nonStarterVelocityState.equals(SpeedRecreation2_3.VelocityState.Middle);

        final double v_0 = nonStarterMiddle ? 0 : v_0prev;

        final Optional<Double> intersection = nonStarterMiddle ? getIntersectionInsideToOutside(m, q) :
                axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_1) : getIntersectionOutsideToInsideZ(m,q,v_1);
        DebugPrint.println("m: " + m + " q: " + q + " v_0: " + v_0 + " v_1: " + v_1 + " intersection: " + (intersection.isPresent() ? intersection.get() : "null"));
        final double rawt = (intersection.isEmpty() || intersection.get().equals(Double.NaN) || Double.compare(intersection.get(), walkedTicks) < 0) ? (endTick - walkedTicks) : intersection.get();
        final double t = Double.compare(walkedTicks + rawt, endTick) > 0 ? (endTick - walkedTicks) : rawt;
        //FIXME IMPORTANT!!!!!!: m of the past needs to be reconstructed for "playerFormularXZ(m..."!!!!!!!!!!!!!
        final double formula = (shouldConsiderStartVelWhenInMiddle && nonStarterMiddle) ?
                playerFormularXZ(m,q,v_0,axis,0, starterVelocityState) + playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState)
                :
                playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState);
        DebugPrint.println("[ngs at " + "v_0: " + v_0 + " v_0prev: " + v_0prev + " rawt: " + rawt + " formula: " + formula + " acounter" + ": " + "msum" + "]" + " (debug= t:" +t+ " walkedTicks: " + walkedTicks + " endTick:" + endTick + " intersec: " + (intersection.isPresent() ? intersection.get() : "empty") + " bool: " + ((intersection.isEmpty() || intersection.get().equals(Double.NaN))));
        //if (nslSplitters.size() > 0) nslSplitters.get(nslSplitters.size() - 1).setLocalEndTick(walkedTicks + t);

        Optional<Double> optNewerLocalStartPos = optLocalStartPos;
        if (optNslSplitters.isPresent()) {
            if (optLocalStartPos.isEmpty()) throw new IllegalArgumentException("When optNslSplitters present then optLocalStartPos should be too");
            optNewerLocalStartPos = Optional.of(formula + optLocalStartPos.get());
            optNslSplitters.get().add(new NSLSplitter(q, m, walkedTicks, Optional.of(walkedTicks + t), v_0prev, optLocalStartPos.get(), optNewerLocalStartPos.get(), nonStarterMiddle));
            DebugPrint.println("optNslSplitters for: " + axis + " has size: " + optNslSplitters.get().size());
        } else {
            DebugPrint.println("optNslSplitters.isEmpty()");
        }

        if (Double.compare(t, 0d) == 0 || Double.compare(t, -0d) == 0 || Double.compare(walkedTicks + t, endTick) >= 0) return formula;
        //debug only
        /*if (1 == 1) {
            throw new IllegalStateException("More than one recursion but why?...");
        }*/
        final double v_0new = nonStarterMiddle ? (axis.equals(Axis.X) ? -m : m)*t*q : velFormular(t, axis, v_0, q, m);
        if (optSplittedProgress.isPresent()) optSplittedProgress.get().setCurrVel(v_0new);
        return formula + neglectableSpeedLimit(m, q, v_0new, axis, endTick, walkedTicks + t, false, optSplittedProgress, optNslSplitters, optNewerLocalStartPos);
    }

    public static double apply(final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks,
                               final double v_0, final boolean shouldConsiderStartVelWhenInMiddle, final TreeMap<Double, SplittedProgress> progress,
                               final double tickProgressUntilPreviousIS) {
        final SplittedProgress splittedProgress = new SplittedProgress();
        final double result = startPos + neglectableSpeedLimit(m, q, v_0, axis, endTick, walkedTicks, shouldConsiderStartVelWhenInMiddle, Optional.of(splittedProgress), Optional.empty(), Optional.empty());
        //FIXME: wollen wir hier nur die Deltaticks speichern oder die Ticks seit dem Originalstartpunkt?
        splittedProgress.setTick(endTick + tickProgressUntilPreviousIS);
        splittedProgress.setCurrPos(result);
        progress.put(splittedProgress.getTick(), splittedProgress);
        return result;
    }

    public static double apply(final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks,
                               final TreeMap<Double, SplittedProgress> progress, final double tickProgressUntilPreviousIS) {
        return apply(startPos, m, q, axis, endTick, walkedTicks, 0d, true, progress, tickProgressUntilPreviousIS);
    }
}
