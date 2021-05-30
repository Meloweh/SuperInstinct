package adris.altoclef.tasks.resources;


import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;


public class SatisfyMiningRequirementTask extends Task {
    private final MiningRequirement requirement;

    public SatisfyMiningRequirementTask(MiningRequirement requirement) {
        this.requirement = requirement;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return mod.getInventoryTracker().miningRequirementMet(requirement);
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        switch (requirement) {
            case HAND:
                // Will never happen if you program this right
                break;
            case WOOD:
                return TaskCatalogue.getItemTask("wooden_pickaxe", 1);
            case STONE:
                return TaskCatalogue.getItemTask("stone_pickaxe", 1);
            case IRON:
                return TaskCatalogue.getItemTask("iron_pickaxe", 1);
            case DIAMOND:
                return TaskCatalogue.getItemTask("diamond_pickaxe", 1);
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof SatisfyMiningRequirementTask) {
            SatisfyMiningRequirementTask other = (SatisfyMiningRequirementTask) obj;
            return other.requirement == requirement;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Satisfy Mining Req: " + requirement;
    }
}
