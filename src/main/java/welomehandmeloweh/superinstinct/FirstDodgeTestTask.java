/**
 * @author Welomeh, Meloweh
 */

package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.input.Input;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;

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
public class FirstDodgeTestTask extends Task {
    private boolean finished;
    private Vec3d target;
    private int elapsedTicks;
    //private Optional<Double> startVelX = Optional.empty(), startPosX = Optional.empty();
    private Optional<ArrowEntity> optDodgingArrow = Optional.empty();
    private Optional<TraceResult> optTraceResult = Optional.empty();
    private enum Maneuver {
        UNDEFINED,
        FORWARD,
        BACKWARD,
        STANDING,
        JUMPING,
        SNEAKING,
        STOPPED
    }
    private Maneuver maneuver = Maneuver.UNDEFINED;

    private void stopPlayer(AltoClef mod) {
        /*if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getPlayer().setSprinting(false);
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }*/
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        maneuver = Maneuver.STOPPED;
    }

    private void startPlayer(AltoClef mod) {
        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
        }
        if (!mod.getPlayer().isSprinting()) {
            mod.getPlayer().setSprinting(true);
        }
        if (maneuver.equals(Maneuver.UNDEFINED)) maneuver = Maneuver.FORWARD;
    }

    private void otherWay(AltoClef mod) {
        final Maneuver prev = maneuver;
        stopPlayer(mod);
        /*stopPlayer(mod);
        mod.getPlayer().setYaw(mod.getPlayer().getYaw() + 180 % 180);
        startPlayer(mod);*/
        if (prev.equals(Maneuver.FORWARD)) maneuver = Maneuver.BACKWARD;
        else if (prev.equals(Maneuver.BACKWARD)) maneuver = Maneuver.FORWARD;
        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_BACK)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_BACK, true);
        }
    }

    @Override
    protected void onStart(AltoClef mod) {
        target = new Vec3d(0.5, mod.getPlayer().getY(), 0.5);
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());

        final float yaw = YawHelper.vecToYaw(eyeCenter, new Vec3d(target.getX(), mod.getPlayer().getEyeY(), target.getZ()));
        mod.getPlayer().setYaw(yaw);

        startPlayer(mod);
        this.finished = false;
        this.elapsedTicks = 0;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        //System.out.println(mod.getPlayer().prevYaw);
        //System.out.println(mod.getPlayer().getYaw());
        //System.out.println("....");
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        if (arrows.size() > 0 && optDodgingArrow.isEmpty()) {
            for (final ArrowEntity arrow : arrows) {
                if (!arrow.verticalCollision && !arrow.horizontalCollision && arrow.prevX != arrow.getX() && arrow.prevZ != arrow.getZ() && arrow.prevY != arrow.getY()) {
                    final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(), mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), target, mod.getWorld(), SimMovementState.UNDEFINED);
                    if (traceResult == null) continue;
                    if (traceResult.willPiercePlayer()) {
                        optTraceResult = Optional.of(traceResult);
                        optDodgingArrow = Optional.of(arrow);
                        System.out.println(traceResult.toAlertMsg(0));
                    }
                }
            }
        }

        if (optDodgingArrow.isPresent()) {
            final Optional<TraceInfo> nextPiercing = optTraceResult.get().getNextPiercingInfo(elapsedTicks);

            if (nextPiercing.isEmpty()) {
                elapsedTicks = 0;
                optTraceResult = Optional.empty();
                optDodgingArrow = Optional.empty();
                stopPlayer(mod);
                startPlayer(mod);
            } else {

                if (maneuver.equals(Maneuver.JUMPING) && mod.getPlayer().isOnGround()) maneuver = Maneuver.FORWARD;

                final long delta = nextPiercing.get().getPiercingEntryHitTick() - elapsedTicks;
                System.out.println("delta: " + delta);
                if (delta > 40) {

                } else if (delta < 20 && !maneuver.equals(Maneuver.JUMPING) && !maneuver.equals(Maneuver.STANDING) && nextPiercing.get().getBodyHitDetail().equals(BodyHitDetail.LOW)) {
                    System.out.println("Too late...");
                    //mod.getPlayer().jump();
                    //maneuver = Maneuver.JUMPING;
                    otherWay(mod);
                } /*else if (delta < 7 && !maneuver.equals(Maneuver.STANDING)) {
                    otherWay(mod);
                } else*/ if (delta <= 30 && !maneuver.equals(Maneuver.STANDING)) {
                    stopPlayer(mod);
                }

                elapsedTicks++;
            }
        }

        /*final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        if (arrows.size() > 0) {
            final ArrowEntity arrow = arrows.get(arrows.size() - 1);
            if (startVelX.isEmpty()) {
                startVelX = Optional.of(arrow.getVelocity().getX());
                startPosX = Optional.of(arrow.getX());
            }
            //System.out.println(arrow.getBoundingBox().getXLength() + " " + arrow.getBoundingBox().getYLength() + " " + arrow.getBoundingBox().getZLength());
            tracings.add(new IntersectionTestTask.PlayerAndArrowState(tracings.size(), arrow.getPos(), mod.getPlayer().getBoundingBox()));
            recordedCounts++;
        }
        if (!finished) {
            if (arrows.size() > 0) {
                final ArrowEntity arrow = arrows.get(arrows.size() - 1);
                final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(), mod.getPlayer().getVelocity(),
                        1000d, mod.getPlayer().getPos(), target, mod.getWorld());
                System.out.println("INTERSECTION RESULT: " + traceResult.toString());
                finished = true;
            }
        } else if (recordedCounts > 40 && printed == false) {
            System.out.println("Debug tracings:");
            for (final IntersectionTestTask.PlayerAndArrowState tr : tracings) {
                System.out.println(tr.toString());
                System.out.println("tracePosX: " + BowArrowIntersectionTracer.tracePosX(startVelX.get(), startPosX.get(), tr.getT()));
            }
            //if (true) throw new IllegalStateException("");
            finished2 = true;
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            printed = true;
        }*/

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        finished = true;
        stopPlayer(mod);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (finished) {
            stopPlayer(mod);
        }
        return finished;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IntersectionMethodsBenchmarkTask;
    }

    @Override
    protected String toDebugString() {
        return this.getClass().getCanonicalName();
    }
}
