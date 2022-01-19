package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import net.minecraft.item.Item;

public class CraftWithMatchingPlanksTask extends CraftWithMatchingMaterialsTask {

    private final ItemTarget _visualTarget;

    public CraftWithMatchingPlanksTask(Item[] validTargets, CraftingRecipe recipe, boolean[] sameMask, int count) {
        super(new ItemTarget(validTargets, count), recipe, sameMask);
        _visualTarget = new ItemTarget(validTargets, count);
    }


    @Override
    protected int getExpectedTotalCountOfSameItem(AltoClef mod, Item sameItem) {
        // Include logs
        return mod.getItemStorage().getItemCount(sameItem) + mod.getItemStorage().getItemCount(ItemHelper.planksToLog(sameItem)) * 4;
    }

    @Override
    protected Task getSpecificSameResourceTask(AltoClef mod, Item[] toGet) {
        for (Item plankToGet : toGet) {
            Item log = ItemHelper.planksToLog(plankToGet);
            // Convert logs to planks
            if (mod.getItemStorage().getItemCount(log) >= 1) {
                ItemTarget empty = null;
                return new CraftInInventoryTask(new ItemTarget(plankToGet, 1), CraftingRecipe.newShapedRecipe("planks", new ItemTarget[]{new ItemTarget(log, 1), empty, empty, empty}, 4), false, true);
            }
        }
        Debug.logError("CraftWithMatchingPlanks: Should never happen!");
        return null;
    }


    @Override
    protected boolean isEqualResource(ResourceTask other) {
        if (other instanceof CraftWithMatchingPlanksTask task) {
            return task._visualTarget.equals(_visualTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Crafting: " + _visualTarget;
    }


    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

}
