package adris.altoclef.tasks.Test;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.defense.BaitTrap;
import adris.altoclef.tasks.defense.DefenseConstants;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BaitTrapTest extends Task {
    BaitTrap trap;
    boolean finished = false;
    @Override
    protected void onStart(AltoClef mod) {
        trap = new BaitTrap(mod);
        final List<Entity> nearbyHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.HOSTILE_DISTANCE
                        //&& !(e instanceof SkeletonEntity)
                        //&& !(e instanceof CreeperEntity)
                        && !(e instanceof ProjectileEntity))
                .collect(Collectors.toList());
        nearbyHostiles.sort((a, b) -> {
            int result = Boolean.compare((a instanceof CreeperEntity), b instanceof CreeperEntity);
            if (result == 0) {
                result = Double.compare(b.getBoundingBox().getYLength(), a.getBoundingBox().getYLength());
            }
            if (result == 0) {
                result = Float.compare(a.distanceTo(mod.getPlayer()), b.distanceTo(mod.getPlayer()));//(int) ((a.distanceTo(mod.getPlayer()) - b.distanceTo(mod.getPlayer()))*1000);
            }
            return result;
        });
        trap.fixateTrap(mod, nearbyHostiles);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        finished = !trap.trapping(mod, null);
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
        return false;
    }

    @Override
    protected String toDebugString() {
        return null;
    }
}
