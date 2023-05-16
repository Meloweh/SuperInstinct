/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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
public class IntersectionTestTask extends Task {
    private boolean finished;
    private boolean finished2;
    private float yaw;
    private boolean isStartTick = true;
    private Vec3d target;

    private int recordedCounts = 0;

    private Optional<Double> startVelX = Optional.empty(), startPosX = Optional.empty();

    public IntersectionTestTask() {
        this.finished = false; this.finished2 = false;
    }

    private class PlayerAndArrowState {
        final int t;
        final Vec3d arrowPos;
        final Box playerBox;

        public PlayerAndArrowState(final int t, final Vec3d arrowPos, final Box playerBox) {
            this.t = t;
            this.arrowPos = arrowPos;
            this.playerBox = playerBox;
        }

        public int getT() {
            return t;
        }

        public Box getPlayerBox() {
            return playerBox;
        }

        public Vec3d getArrowPos() {
            return arrowPos;
        }

        @Override
        public String toString() {

            String minXState = "minX " + (arrowPos.getX() < playerBox.minX ? "lower" : "higher") + ": " + (getPlayerBox().minX + "; " + "<arrowX: " + arrowPos.getX() + "> \n");
            String maxXState = "maxX " + (arrowPos.getX() > playerBox.maxX ? "lower" : "higher") + ": " + (getPlayerBox().maxX + "; " + "<arrowX: " + arrowPos.getX() + "> \n");
            String minYState = "minY " + (arrowPos.getX() < playerBox.minY ? "lower" : "higher") + ": " + (getPlayerBox().minY + "; " + "<arrowY: " + arrowPos.getY() + "> \n");
            String maxYState = "maxY " + (arrowPos.getX() > playerBox.maxY ? "lower" : "higher") + ": " + (getPlayerBox().maxY + "; " + "<arrowY: " + arrowPos.getY() + "> \n");
            String minZState = "minZ " + (arrowPos.getZ() < playerBox.minZ ? "lower" : "higher") + ": " + (getPlayerBox().minZ + "; " + "<arrowZ: " + arrowPos.getZ() + "> \n");
            String maxZState = "maxZ " + (arrowPos.getZ() > playerBox.maxZ ? "lower" : "higher") + ": " + (getPlayerBox().maxZ + "; " + "<arrowZ: " + arrowPos.getZ() + "> \n");
            return "[t: "+ t + " " + minXState + maxXState + minYState + maxYState + minZState + maxZState +"]";
        }
    }

    private final List<PlayerAndArrowState> tracings = new ArrayList<>();
    private boolean printed = false;

    @Override
    protected void onStart(AltoClef mod) {
        target = new Vec3d(0.5, mod.getPlayer().getY(), 0.5);
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());

        yaw = YawHelper.vecToYaw(eyeCenter, new Vec3d(target.getX(), mod.getPlayer().getEyeY(), target.getZ()));
        mod.getPlayer().setYaw(yaw);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        if (arrows.size() > 0 && printed == false) {
            final ArrowEntity arrow = arrows.get(arrows.size() - 1);
            if (startVelX.isEmpty()) {
                startVelX = Optional.of(arrow.getVelocity().getX());
                startPosX = Optional.of(arrow.getX());
            }
            //System.out.println(arrow.getBoundingBox().getXLength() + " " + arrow.getBoundingBox().getYLength() + " " + arrow.getBoundingBox().getZLength());
            tracings.add(new PlayerAndArrowState(tracings.size(), arrow.getPos(), mod.getPlayer().getBoundingBox()));
            recordedCounts++;
        }
        if (!finished) {
            if (isStartTick) {
                if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
                    mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
                    mod.getPlayer().setSprinting(true);
                }
                isStartTick = false;
            } else if (arrows.size() > 0) {
                final ArrowEntity arrow = arrows.get(arrows.size() - 1);
                final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(), mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), target, mod.getWorld(), SimMovementState.UNDEFINED);
                System.out.println("INTERSECTION RESULT: " + traceResult.toString());
                finished = true;
            }
        } else if (recordedCounts > 40 && printed == false) {
            System.out.println("Debug tracings:");
            for (final PlayerAndArrowState tr : tracings) {
                System.out.println(tr.toString());
                System.out.println("tracePosX: " + BowArrowIntersectionTracer.tracePosX(startVelX.get(), startPosX.get(), tr.getT()));
            }
            //if (true) throw new IllegalStateException("");
            finished2 = true;
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            printed = true;
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        finished2 = true;
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {

        if (finished2) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
        }
        return finished2;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IntersectionMethodsBenchmarkTask;
    }

    @Override
    protected String toDebugString() {
        return "Intersection Test Task";
    }
}
