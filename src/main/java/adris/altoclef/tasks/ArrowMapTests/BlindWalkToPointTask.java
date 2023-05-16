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

package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;

import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
public class BlindWalkToPointTask extends Task {

    final BlockPos target;
    boolean finished;

    Rotation rot;

    public BlindWalkToPointTask(final BlockPos target) {
        this.target = target;
        this.finished = false;
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;
    public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.getYaw(), target.getPitch());
        }
        return target.subtract(current).normalize().add(current);
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest, Rotation current) {
        return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    private static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
        double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
        double yaw = Math.atan2(delta[0], -delta[2]);
        double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
        double pitch = Math.atan2(delta[1], dist);
        return new Rotation(
                (float) (yaw * RAD_TO_DEG),
                (float) (pitch * RAD_TO_DEG)
        );
    }

    private boolean walkStepTowardsPoint(final AltoClef mod, final Vec3d target) {
        //if (mod.getPlayer().prevYaw != neededYaw || mod.getPlayer().prevPitch != neededPitch) {
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());

        if (target.distanceTo(eyeCenter) < 0.5d) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            return true;
        }

        /*final Vec3d deltaVec = eyeCenter.subtract(target);
        final Double yaw = Math.atan2(deltaVec.getZ(), deltaVec.getX());
        final Double pitch = Math.atan2(Math.sqrt(deltaVec.getZ() * deltaVec.getZ() + deltaVec.getX() * deltaVec.getX()), deltaVec.getY()) + Math.PI;
        neededYaw = yaw.floatValue();
        neededPitch = pitch.floatValue();*/
        rot = calcRotationFromVec3d(eyeCenter, target);

        //}
        //System.out.println(mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.SPRINT));
        mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(rot.getYaw(), rot.getPitch()), true);

        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            //BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
        }
        return false;
    }

    private boolean walkStepTowardsBlock(final AltoClef mod, final BlockPos blockPos) {
        return walkStepTowardsPoint(mod, new Vec3d(blockPos.getX() + 0.5d, mod.getPlayer().getEyeY(), blockPos.getZ() + 0.5d));
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        finished = walkStepTowardsBlock(mod, target);
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BlindWalkToPointTask;
    }

    @Override
    protected String toDebugString() {
        return "Blind Walk";
    }
}
