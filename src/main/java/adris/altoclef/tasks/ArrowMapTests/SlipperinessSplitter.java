/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Direction;

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
public class SlipperinessSplitter {
    //FIXME: is m for x equal to -mX or mX????
    private static double deltaVelFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        //if (Double.compare(v, 0d) == 0)DebugPrint.println("YES..");
        if (axis.equals(Axis.X)) {
            //FIXME: is m for x equal to -mX or mX????
            final double v_0 = (Double.compare(v, 0d) == 0) ? ((-m * q - m) * q) : v;
            return v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q)); //FIXME: TODO: is still formular up-to-date? if v0 == 0, we should use the v0 formular i think
        }
        final double v_0 = (Double.compare(v, 0d) == 0) ? ((m * q + m) * q) : v;
        //FIXME: is m for z equal to -mZ or mZ????
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q)); //FIXME: TODO: is still formular up-to-date? if v0 == 0, we should use the v0 formular i think

        //if (Double.compare(v_0, 0d) == 0) return (m * q + m * Math.pow(q, 2));

        /*
        //TODO: test if required
        if (Double.compare(t, 0d) == 0) return v_0;
        //TODO: test me because of: => //FIXME: is m for z equal to -mZ or mZ????
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q));*/
    }

    private static double velFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        return deltaVelFormular(t, axis, v, q, m) * q;
    }
    private static double playerFormularXZ(final double m, final float q, final double v_0, final Axis axis, final double t, final SpeedRecreation2_3.VelocityState velocityState) {
        return axis.equals(Axis.X) ?
                !velocityState.equals(SpeedRecreation2_3.VelocityState.Middle) ?
                        CSAlgorithm.tracePlayerPosXDelta(m, q, v_0, t)//1 / (1 - q) * (v_0 * (1 - Math.pow(q, t)) + m * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1)) //FIXME: This old, use new formular here => DONE!
                        :
                        -m * t
                :
                (!velocityState.equals(SpeedRecreation2_3.VelocityState.Middle)) ?
                        CSAlgorithm.tracePlayerPosZDelta(m, q, v_0, t)//v_0 * ((1-Math.pow(q,t))/(1-q)) + ((m*t)/(1-q)) - ((m)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + m/(1-q)//FIXME: This old, use new formular here => DONE!
                        :
                        m * t;
    }

    private static double getDiffPlayerVelVsLineVel(final double t, final Axis axis, final double v_0, final float q, final double m) {
        return velFormular(t, axis, v_0, q, m);
    }

    private static final Bisection getPlayerVelVsLineVelBisection(final double t, final Axis axis, final double v_0, final float q, final double m) {
        return new Bisection() {
            @Override
            public double f(double t) {
                return getDiffPlayerVelVsLineVel(t, axis, v_0, q, m);
            }
        };
    }

    private static double getDiffPlayerVsLineXZ(final double t, final Axis axis, final double v_0, final float q, final double m, final double xOrZOfLine) {
        final SpeedRecreation2_3.VelocityState velocityState = Double.compare(v_0, 0.003d) > 0 ? SpeedRecreation2_3.VelocityState.Positive :
                Double.compare(v_0, -0.003d) > 0 ? SpeedRecreation2_3.VelocityState.Middle :
                        SpeedRecreation2_3.VelocityState.Negative;
        return playerFormularXZ(m,q,v_0,axis,t, velocityState) - xOrZOfLine;
    }

    private static final Bisection getPlayerVsLineBisection(final double t, final Axis axis, final double v_0, final float q, final double m, final double xOrZOfLine) {
        return new Bisection() {
            @Override
            public double f(double t) {
                return getDiffPlayerVsLineXZ(t, axis, v_0, q, m, xOrZOfLine);
            }
        };
    }

    /**
     * TODO: implement this idea
     */
    private static final boolean isInRemainingRunningStaminaTicks() {
        return true;
    }

    //final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks
    public static final VecXZ slipperinessSplitter(final VecXZ playerStartPos, final List<SlipperinessSpot> spots, final double endTick, final Optional<VecXZ> optPrevV0FromPrevPath, final float yaw, final TreeMap<Double, Splitter> splitters,
                                                   final List<NSLSplitter> nslSplittersX, final List<NSLSplitter> nslSplittersZ) {
        //final BlockPos startBlock = new BlockPos(playerStartPos).down();
        //final BlockPos unpreciseEndBlock = new BlockPos(unpreciseEndPos);

        //TODO: add driftoff check every like 100 blocks and add those candidates to path list.
        // also meanwhile the error distance increases, we may recalibrate
        // the Bresenham line and resume with this adjustment to the error.
        // It would be also interesting to consider creating a formula to get t by error distance as input.

        //Pass center of player
        //final List<BlockPos> path = VanillaBresenham.normalBresenham((int)playerStartPos.getY() - 1, playerStartPos.getX(), playerStartPos.getZ(), unpreciseEndBlock.getX(), unpreciseEndBlock.getZ()); //FIXME: Y-1? Probably too much

        /*
        if (path.size() < 1) return 0;

        final List<SlipperinessSpot> spots = new ArrayList<>();
        path.forEach(e -> spots.add(new SlipperinessSpot(e, world.getBlockState(e).getBlock())));*/

        /*if (spots.size() < 2) {

            return 0;
        }*/

        SlipperinessSpot currentStartPoint = spots.get(0);
        SlipperinessSpot prev = currentStartPoint;
        //Vec3d totalDeltaPosition = Vec3d.ZERO;
        VecXZ totalDeltaPosition = new VecXZ(0.0d, 0.0d);
        float currentSlipperiness = currentStartPoint.getSlipperiness();
        //boolean usedEarliestStartMonoPos = false;
        //Optional<Vec3d> optCurrentStartPos = Optional.of(new Vec3d(playerStartPos.getX(), playerStartPos.getY(), playerStartPos.getZ()));
        Optional<VecXZ> optPrevV0 = optPrevV0FromPrevPath;
        boolean shouldConsiderStartVelWhenInMiddle = true;
        double lastProgressTick = 0;
        //final TreeMap<Double, Splitter> splitters = new TreeMap<>();
        int walkedIndices = 0;
        for (final SlipperinessSpot current : spots) {
            //System.out.print("slipp for loop it");
            if (walkedIndices++ == spots.size() - 2 || !prev.equals(current)) { //FIXME: spots.indexOf(current) == spots.size() - 1 wants to cut after but !prev.equals(current) wants prior
                DebugPrint.println("");
                DebugPrint.println("SLIP PRODUCTION: !prev = " +  !prev.equals(current) );
                if ( !prev.equals(current)) {
                    DebugPrint.println(prev.toString()); DebugPrint.println("vs");
                    DebugPrint.println(current.toString());
                }

              /*north: neg z
                south: pos z
                west:  neg x
                east:  pos x
                 */
                final Direction prevToCurrentLineAsDirection =
                        prev.getPos().north().equals(current.getPos()) ? Direction.NORTH :
                                prev.getPos().south().equals(current.getPos()) ? Direction.SOUTH :
                                        prev.getPos().west().equals(current.getPos()) ? Direction.WEST : Direction.EAST;
                DebugPrint.println("playerStartPos: " + playerStartPos.toString());
                DebugPrint.println("prevPos: " + prev.getPos().toString());
                DebugPrint.println("currPos: " + current.getPos().toString());
                DebugPrint.println("prevToCurrentLineAsDirection: " + prevToCurrentLineAsDirection.toString());
                double intersectionPos;
                if (prevToCurrentLineAsDirection.equals(Direction.NORTH)) { // if line is like z axis, we look at where player.x cuts line.x
                    intersectionPos = prev.getPos().getZ();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos -= Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.SOUTH)) {
                    intersectionPos = current.getPos().getZ();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos += Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.WEST)) {
                    intersectionPos = prev.getPos().getX();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos -= Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.EAST)) {
                    intersectionPos = current.getPos().getX();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos += Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else throw new IllegalStateException("We should not be here. prevToCurrentLineAsDirection is: " + prevToCurrentLineAsDirection == null ? "missing." : "present.");
                DebugPrint.println("intersectionPos: " + intersectionPos);
                final Axis axisOfLine =
                        (prevToCurrentLineAsDirection.equals(Direction.NORTH) || prevToCurrentLineAsDirection.equals(Direction.SOUTH)) ?
                                Axis.Z : Axis.X;

                final VecXZ currPlayerStartPos = new VecXZ(playerStartPos.getX() + totalDeltaPosition.getX(), playerStartPos.getZ() + totalDeltaPosition.getZ());
                final double currentStartMonoPos = axisOfLine.equals(Axis.X) ? currPlayerStartPos.getX() : currPlayerStartPos.getZ();

                final float q = !current.isAir() ? 0.91F * currentSlipperiness : 0.91F;
                final double mX = CSAlgorithm.getM(Axis.X, yaw, isInRemainingRunningStaminaTicks(), !current.isAir(), currentSlipperiness);//axisOfLine
                final double mZ = CSAlgorithm.getM(Axis.Z, yaw, isInRemainingRunningStaminaTicks(), !current.isAir(), currentSlipperiness);
                DebugPrint.println("mX: " + mX + " mZ: " + mZ);

                final double prevV0X = optPrevV0.isEmpty() ? 0 : optPrevV0.get().getX();
                final double prevV0Z = optPrevV0.isEmpty() ? 0 : optPrevV0.get().getZ();

                DebugPrint.println("lastProgressTick: " + lastProgressTick);
                DebugPrint.println("endTick: " + endTick);
                DebugPrint.println("currentStartMonoPos: " + currentStartMonoPos);
                DebugPrint.println("prevV0X: " + prevV0X);
                DebugPrint.println("prevV0Z: " + prevV0Z);

                final TreeMap<Double, SplittedProgress> progress = new TreeMap<>();
                final Optional<Double>[] optCurrentTickAtMonoPos = CSAlgorithm.getPointIntersectionMovingPlane(lastProgressTick, endTick, intersectionPos, axisOfLine.equals(Axis.X) ? prevV0X : prevV0Z, currentStartMonoPos,
                        axisOfLine, currentSlipperiness, !current.isAir(), isInRemainingRunningStaminaTicks(), yaw, shouldConsiderStartVelWhenInMiddle, axisOfLine.equals(Axis.X) ? mX : mZ, q, progress);

                DebugPrint.println("optCurrentTickAtMonoPos: " + (optCurrentTickAtMonoPos.length > 0 && optCurrentTickAtMonoPos[0].isPresent() ? optCurrentTickAtMonoPos[0].get() : "empty"));
                //put q and m and lastProgressTick and v0 in a list with key = lastProgressTick


                //final Optional<SplittedProgress> optOtherSplittedProgress = Optional.empty();
                //final double otherNewVel = NegectableSpeedLimit.neglectableSpeedLimit(m, q, !axisOfLine.equals(Axis.X) ? prevV0X : prevV0Z, axisOfLine, endTick, 0, shouldConsiderStartVelWhenInMiddle, optOtherSplittedProgress);

                //FIXME IMPORTANT!: optCurrentTickAtMonoPos[0].get() needs check for non-intersection cases => DONE <<BUT>> check if it is complete
                //if (optCurrentTickAtMonoPos.length > 0 && optCurrentTickAtMonoPos[0].isPresent()) {
                final Optional<SplittedProgress> optOtherSplittedProgress = Optional.of(new SplittedProgress());
                final VecXZ localStartPos = new VecXZ(playerStartPos.getX() + totalDeltaPosition.getX(), playerStartPos.getZ() + totalDeltaPosition.getZ());
                //TODO: can be optimized?
                //FIXME: wait... did getPointIntersectionMovingPlane already calculated this?
                DebugPrint.println("Now checking Optional.of(nslSplittersX)");
                final long nanosdx = System.nanoTime();
                final double dx = NegectableSpeedLimit.neglectableSpeedLimit(mX, q, prevV0X/*optPrevV0.isPresent() ? optPrevV0.get().getX() : 0d*/, Axis.X, optCurrentTickAtMonoPos[0].get(), 0, shouldConsiderStartVelWhenInMiddle,
                        axisOfLine.equals(Axis.X) ? Optional.empty() : optOtherSplittedProgress, Optional.of(nslSplittersX), Optional.of(localStartPos.getX()));
                System.out.println("ss dx: " + (System.nanoTime()-nanosdx));
                final long nanosdz = System.nanoTime();
                final double dz = NegectableSpeedLimit.neglectableSpeedLimit(mZ, q, prevV0Z/*optPrevV0.isPresent() ? optPrevV0.get().getZ() : 0d*/, Axis.Z, optCurrentTickAtMonoPos[0].get(), 0, shouldConsiderStartVelWhenInMiddle,
                        axisOfLine.equals(Axis.Z) ? Optional.empty() : optOtherSplittedProgress, Optional.of(nslSplittersZ), Optional.of(localStartPos.getZ()));
                System.out.println("ss dz: " + (System.nanoTime()-nanosdz));
                //totalDeltaPosition += trace stuff from monoCurrentStartPointPos to monoIntersectionPos here
                DebugPrint.println("debug localStartPos: " + localStartPos.toString());
                DebugPrint.println("dx: " + dx + " dz: " + dz);

                totalDeltaPosition.add(dx, dz);
                splitters.put(lastProgressTick,
                        new Splitter(q, mX, mZ, lastProgressTick, optCurrentTickAtMonoPos[0].get(), prevV0X, prevV0Z, localStartPos, new VecXZ(playerStartPos.getX() + totalDeltaPosition.getX(), playerStartPos.getZ() + totalDeltaPosition.getZ())));
                DebugPrint.println("splitters getLocalStartPosXZ: " + splitters.values().iterator().next().getLocalStartPosXZ().toString() + " splitters getLocalEndPosXZ: " + splitters.values().iterator().next().getLocalEndPosXZ().toString() );
                //} else {
                //    DebugPrint.println("If it crashes now i knew it...");
                //}
                //---------------FIXME: kommt das in die if?---------------
                optPrevV0 = Optional.of(new VecXZ(
                        axisOfLine.equals(Axis.X) ? progress.get(progress.lastKey()).getCurrVel() : optOtherSplittedProgress.get().getCurrVel()
                        ,
                        axisOfLine.equals(Axis.Z) ? progress.get(progress.lastKey()).getCurrVel() : optOtherSplittedProgress.get().getCurrVel()
                ));
                //optCurrentStartPos = the new start pos; //optCurrentStartPos = Optional.of(progress.get(progress.lastKey()).getCurrPos());
                lastProgressTick = optCurrentTickAtMonoPos[0].get();
                //---------------------------------------------------------

                currentStartPoint = current;
                currentSlipperiness = currentStartPoint.getSlipperiness();
                shouldConsiderStartVelWhenInMiddle = false;
            }

            prev = current;
        }

        DebugPrint.println("RETURNED SLIPPERINESS");
        return new VecXZ(playerStartPos.getX() + totalDeltaPosition.getX(), playerStartPos.getZ() + totalDeltaPosition.getZ());
        /*
        //final List<SlipperinessSpot> spots = new ArrayList<>();
        final List<SlipperinessPath> transitions = new ArrayList<>();
        SlipperinessSpot prev = null;
        for (final BlockPos e : path) {
            final SlipperinessSpot newSpot = new SlipperinessSpot(e, world.getBlockState(e).getBlock());
            if (!transitions.isEmpty()) {

            } else if (prev != null) {

            }
            prev = newSpot;
        }
        path.forEach(e -> {
            final SlipperinessSpot newSpot = new SlipperinessSpot(e, world.getBlockState(e).getBlock());
            if (!transitions.isEmpty()) {

            }
            //prev = newSpot;
            //if (spots.size() < 1 || !spots.get(spots.size() - 1).equals(newSpot)) {
            //    if (prev != null) spots.add(newSpot);
            //    spots.add(newSpot);
            //}

        });*/

        /*
        final List<SlipperinessPath> cuttingLines = new ArrayList<>();
        for (int i = 1; i < spots.size(); i++) {
            cuttingLines.add(new SlipperinessPath(spots.get(i-1), spots.get(i)));
        }*/

    }
}
