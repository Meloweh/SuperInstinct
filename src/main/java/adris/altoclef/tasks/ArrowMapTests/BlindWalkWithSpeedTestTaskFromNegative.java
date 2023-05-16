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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

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
public class BlindWalkWithSpeedTestTaskFromNegative extends Task {

    final BlockPos target;
    boolean finished;
    private Vec3d prevPos;
    boolean isSecondCycle;
    final Random rand = new Random();
    int counter = 0;
    boolean isStarting = true;

    Vec3d startPos;
    Vec3d endPos;

    Rotation rot;

    double prevDist = 0;
    double initDist = 0;

    public BlindWalkWithSpeedTestTaskFromNegative(final BlockPos target) {
        this.target = target;
        this.finished = false;
    }

    Vec3d getDeltaPos(final Vec3d currentPos) {
        return new Vec3d(Math.abs(Math.abs(currentPos.getX())) - Math.abs(prevPos.getX()),
                Math.abs(Math.abs(currentPos.getY())) - Math.abs(prevPos.getY()),
                Math.abs(Math.abs(currentPos.getZ())) - Math.abs(prevPos.getZ()));
    }

    double get2DAbsoluteSpeed(final double x, final double z) {
        return Math.abs(Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)));
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

        if (counter > 60) {
            counter = 0;
            //System.out.println(mod.getPlayer().getYaw());
            //mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(mod.getPlayer().getYaw(), rand.nextInt(180)-90 + rand.nextFloat()), true);
            //mod.getPlayer().setPitch(rand.nextInt(180)-90 + rand.nextFloat());

            //System.out.println(mod.getPlayer().getYaw());///tp @a 11487.15 4 1609    ///tp @a 1609 4 11487.15
        } else {
            counter++;
        }

        if (!isStarting) {
            /*Vec2f start2 = new Vec2f((float)startPos.x, (float)startPos.z);
            Vec2f end2 = new Vec2f((float)endPos.x,(float)endPos.z);
            Vec2f sub = end2.add(new Vec2f(-start2.x, -start2.y));
            Vec2f n = new Vec2f(-sub.y, sub.x);
            double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.y, 2));
            Vec2f otherSub = start2.add(new Vec2f((float)mod.getPlayer().getPos().x, (float)mod.getPlayer().getPos().z));
            double m = otherSub.y / otherSub.x;
            double nAsDouble = n.y / n.x;
            double res = m * nd * nAsDouble;
            double abs = Math.abs(res);
            if (Double.compare(abs, prevDist) > 0) {
                System.out.println(abs + " m: " + abs / prevDist); prevDist = abs;
            }*/

            Vec2f start2 = new Vec2f((float)startPos.x, (float)startPos.z);
            Vec2f end2 = new Vec2f((float)endPos.x,(float)endPos.z);
            Vec2f sub = end2.add(new Vec2f(-start2.x, -start2.y));
            Vec2f n = new Vec2f(-sub.y, sub.x);
            double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.y, 2));
            Vec2f otherSub = start2.add(new Vec2f((float)mod.getPlayer().getPos().x, (float)mod.getPlayer().getPos().z));
            double m = otherSub.y / otherSub.x;
            double nAsDouble = n.y / n.x;
            double res = m * nd * nAsDouble;
            double abs = Math.abs(res);

            if (Double.compare(abs, prevDist) > 0) {
                System.out.println(abs + " m: " + abs / prevDist); prevDist = abs;
            } else {
                System.out.println("lower: " + abs / prevDist);
            }


            /*startPos = new Vec3d(1, 0, 1.5);
            endPos = new Vec3d(2,0,1);
            final Vec3d support = endPos.subtract(startPos);
            final Vec3d n = new Vec3d(-support.getZ(), 0, support.getX());
            //final double nDach = n.getX() * Math.sqrt(Math.pow(n.getX(),2) + Math.pow(n.getZ(), 2)) +
            final Vec3d sub = mod.getPlayer().getPos().subtract(startPos);
            final Vec3d mul = sub.multiply(n);
            final double div = mul.getZ() / mul.getX();
            final double divnormal = div / Math.sqrt(Math.pow(n.getX(),2)+Math.pow(n.getZ(),2));
            //final double div = n.getZ() / n.getX();
            System.out.println(divnormal);
            startPos.normalize()*/

        }

        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(rot.getYaw(), rot.getPitch()), true);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            //BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
        } else if (isStarting) {
            //
            //mod.getPlayer().setPitch(-90);
            startPos = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());
            endPos = new Vec3d(0.5d, mod.getPlayer().getEyeY(), 0.5d);
            isStarting = false;
        }
        return false;
    }

    private boolean walkStepTowardsBlock(final AltoClef mod, final BlockPos blockPos) {
        return walkStepTowardsPoint(mod, new Vec3d(blockPos.getX() + 0.5d, mod.getPlayer().getEyeY(), blockPos.getZ() + 0.5d));
    }

    void print(final AltoClef mod) {
        final Vec3d delta = getDeltaPos(mod.getPlayer().getPos());
        System.out.println("p: " + get2DAbsoluteSpeed(delta.getX(), delta.getZ()));
    }

    @Override
    protected void onStart(AltoClef mod) {
        isSecondCycle = false;
        prevPos = mod.getPlayer().getPos();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        finished = walkStepTowardsBlock(mod, target);
        //print(mod);
        prevPos = mod.getPlayer().getPos();
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished || Math.abs(mod.getPlayer().getPos().getX()) < 0.5 || Math.abs(mod.getPlayer().getPos().getZ()) < 0.5;
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
