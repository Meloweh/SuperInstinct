/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;

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
public class IntersectionMethodsBenchmarkTask extends Task {
    boolean finished;

    Rotation rot;

    public IntersectionMethodsBenchmarkTask() {
        this.finished = false;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        /*final CollisionFeedback feedbackY = CSAlgorithm.calculateCollisionsY(0d, 200d, new Vec3d(0d, 0.42d, 0d), new Vec3d(0d, -50d, 0d), new Vec3d(0, 0.42d, 0d), Vec3d.ZERO, mod.getPlayer().getBoundingBox().expand(0.2f), true);
        final CollisionFeedback feedbackX = CSAlgorithm.calculateCollisionsXZ(0, 800, new Vec3d(5d, 1d, 1d), new Vec3d(0d, 0d, 0d), new Vec3d(1d,1d,1d), new Vec3d(30d, 0d, 0d), mod.getPlayer().getBoundingBox().expand(0.2f));
        final CollisionFeedback feedbackZ = CSAlgorithm.calculateCollisionsZ(0, 200, new Vec3d(5d, 1d, -0.7d), new Vec3d(0d, 0d, 0d), new Vec3d(1d,1d,-0.1d), new Vec3d(30d, 0d, -20d), mod.getPlayer().getBoundingBox().expand(0.2f));

        //BowArrowIntersectionTracer.calculateCollision()
        System.out.println(feedbackX);
        System.out.println(feedbackY);
        System.out.println(feedbackZ);*/
        finished = true;
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
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
        return "Intersection Benchmark";
    }
}
