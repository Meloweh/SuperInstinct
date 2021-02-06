package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;

public class IdleTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        // Do nothing
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(AltoClef mod) {
        // Never finish
        return false;
    }

    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof IdleTask;
    }

    @Override
    protected String toDebugString() {
        return "Idle";
    }
}
