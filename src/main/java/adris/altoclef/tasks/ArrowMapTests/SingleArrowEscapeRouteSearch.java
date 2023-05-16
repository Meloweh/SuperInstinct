package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

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
public class SingleArrowEscapeRouteSearch {

    final private ThreadContainer<TraceResult> G90, G180, G270, G45, G135, G225, G315;
    private static final int DIST = 70;
    final Rotation newRot;

    public ThreadContainer<TraceResult> getG90() {
        return G90;
    }

    public ThreadContainer<TraceResult> getG180() {
        return G180;
    }

    public ThreadContainer<TraceResult> getG270() {
        return G270;
    }

    public boolean completed() {
        return G90.hasTraceResult()
                && G180.hasTraceResult()
                && G270.hasTraceResult();
    }

    public List<Float> getEscapeYaws() {
        final List<Float> results = new ArrayList<>();
        if (getG90().hasTraceResult()) {
            results.add((newRot.getYaw() + 90) % 180);
        }
        if (getG180().hasTraceResult()) {
            results.add((newRot.getYaw() + 180) % 180);
        }
        if (getG270().hasTraceResult()) {
            results.add((newRot.getYaw() + 270) % 180);
        }

        return results;
    }

    public SingleArrowEscapeRouteSearch(final ArrowEntity arrow, final AltoClef mod) {

        G90 = new ThreadContainer<>();
        G180 = new ThreadContainer<>();
        G270 = new ThreadContainer<>();
        G45 = new ThreadContainer<>();
        G135 = new ThreadContainer<>();
        G225 = new ThreadContainer<>();
        G315 = new ThreadContainer<>();

        final Box box = mod.getPlayer().getBoundingBox();
        final Vec3d playerVel = mod.getPlayer().getVelocity();
        final Vec3d playerPos = mod.getPlayer().getPos();
        final World world = mod.getWorld();
        final float yaw = mod.getPlayer().getYaw();
        final float pitch = mod.getPlayer().getPitch();


        //final Vec3d newTarget = YawHelper.yawToVec(newYaw).multiply(DIST);
        final Vec3d orig = mod.getPlayer().getEyePos();
        newRot = RotationUtils.calcRotationFromVec3d(orig, arrow.getPos(), new Rotation(yaw, pitch));
        float newYaw = newRot.getYaw();

        /*
        newYaw = (newYaw + 90) % 180;
        BowArrowIntersectionTracer.calculateCollisionThread(arrow, box, playerVel, playerPos, YawHelper.yawToVec(newYaw).multiply(DIST), world, SimMovementState.SIM_MOVE, G90);

        newYaw = (newYaw + 90) % 180;
        BowArrowIntersectionTracer.calculateCollisionThread(arrow, box, playerVel, playerPos, YawHelper.yawToVec(newYaw).multiply(DIST), world, SimMovementState.SIM_MOVE, G180);

        newYaw = (newYaw + 90) % 180;
        BowArrowIntersectionTracer.calculateCollisionThread(arrow, box, playerVel, playerPos, YawHelper.yawToVec(newYaw).multiply(DIST), world, SimMovementState.SIM_MOVE, G270);*/

        //newYaw = newRot.getYaw();


    }
}