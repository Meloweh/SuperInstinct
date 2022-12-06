/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import net.minecraft.util.math.Vec3d;

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
