/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
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
public class TraceArrowTestTask extends Task {
    private boolean finished;
    private boolean finished2;
    private float yaw;
    private boolean isStartTick = true;
    private Vec3d target;

    private int recordedCounts = 0;

    private Optional<Vec3d> startVel = Optional.empty(), startPos = Optional.empty();

    public TraceArrowTestTask() {
        this.finished = false; this.finished2 = false;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    int t = 0;

    public double tracePosXIteratively(final double currentVelX, final double currentPosX, final double deltaTick) {
        double pos = currentPosX;
        double vel = currentVelX;
        for (int it = 0; it<deltaTick; it++){
            double i = pos + vel;
            float j = 0.99F;
            vel =  vel * (double) j;
            pos = i;
        }
        return pos;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        if (arrows.size() > 0) {
            ArrowEntity arrow = arrows.get(arrows.size() - 1);
            if (arrow.getY() <= 4.07) finished = true;
            if (startPos.isEmpty()) startPos = Optional.of(arrow.getPos());
            if (startVel.isEmpty()) startVel = Optional.of(arrow.getVelocity());
            System.out.println("Pos: " + arrow.getPos().toString() + " Vel: " + arrow.getVelocity().toString() + "tracedPos: (" + BowArrowIntersectionTracer.tracePosX(startVel.get().getX(), startPos.get().getX(), t) + ", " + BowArrowIntersectionTracer.tracePosY(startVel.get().getY(), startPos.get().getY(), t)
                    + ", " + BowArrowIntersectionTracer.tracePosZ(startVel.get().getZ(), startPos.get().getZ(), t) + " <><> " + tracePosXIteratively(startVel.get().getX(), startPos.get().getX(), t) + " >>> +1: " + tracePosXIteratively(startVel.get().getX(), startPos.get().getX(), t+1));
            t++;
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
