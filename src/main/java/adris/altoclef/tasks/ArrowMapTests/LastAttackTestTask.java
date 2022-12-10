package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.mob.SkeletonEntity;

import java.util.List;

public class LastAttackTestTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        final List<SkeletonEntity> l = mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class);
        if (l.size() > 0) {
            System.out.println(l.get(0).getLastAttackedTime() + " ... " + l.get(0).getLastAttackTime());
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return null;
    }
}
