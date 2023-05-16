/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.input.Input;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
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
public class TraceNSLTestTask extends Task {
    private boolean finished;
    private boolean finished2;
    private float yaw;
    private boolean isStartTick = true;
    private Vec3d target;

    private int recordedCounts = 0;

    private Optional<Vec3d> startVel = Optional.empty(), startPos = Optional.empty();
    private Optional<Float> startYaw = Optional.empty();

    boolean isGoing = false;
    @Override
    protected void onStart(AltoClef mod) {
    }

    int t = 0;


    @Override
    protected Task onTick(AltoClef mod) {
        if (startPos.isEmpty()) startPos = Optional.of(mod.getPlayer().getPos());
        if (startVel.isEmpty()) startVel = Optional.of(mod.getPlayer().getVelocity());
        if (startYaw.isEmpty()) startYaw = Optional.of(mod.getPlayer().getYaw());

        if (!isGoing) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, !isGoing);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, !isGoing);
            mod.getPlayer().setSprinting(!isGoing);
            isGoing = true;
            mod.getPlayer().setYaw(startYaw.get());
        }

        final float q = 0.6f * 0.91f;
        final double mX = CSAlgorithm.getM(Axis.X, startYaw.get(), true, true, 0.6f);//(final Axis axis, final float startYaw, final boolean IS_RUNNING, final boolean ON_GROUND, final float SLIPPERINESS) {
        final double mZ = CSAlgorithm.getM(Axis.Z, startYaw.get(), true, true, 0.6f);
        final double tracedX = startPos.get().getX() + CSAlgorithm.tracePlayerPosXDelta(mX, q, startVel.get().getX(), t);
        final double traceY = startPos.get().getY() + CSAlgorithmY.tracePlayerPosY(startVel.get().getY(), startPos.get().getY(), t);
        final double tracedZ = startPos.get().getZ() + CSAlgorithm.tracePlayerPosZDelta(mZ, q, startVel.get().getZ(), t);
        System.out.println("Pos: " + mod.getPlayer().getPos().toString() + " Vel: " + mod.getPlayer().getVelocity().toString() + "tracedPos: (" + tracedX + ", " + traceY + ", " + tracedZ + ")");
        t++;

        if (t > 100) {
            finished = finished2 = true;
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, !isGoing);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, !isGoing);
            mod.getPlayer().setSprinting(!isGoing);
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        finished = true;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IntersectionMethodsBenchmarkTask;
    }

    @Override
    protected String toDebugString() {
        return "PlayerAndArrowState Test Task";
    }
}
