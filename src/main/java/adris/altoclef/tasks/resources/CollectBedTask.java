package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.MineAndCollectTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.ItemUtil;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.csharpisbetter.Util;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class CollectBedTask extends CraftWithMatchingWoolTask {

    public static final Block[] BEDS = Util.itemsToBlocks(ItemUtil.BED);

    private final ItemTarget _visualBedTarget;

    public CollectBedTask(Item[] beds, ItemTarget wool, int count) {
        // Top 3 are wool, must be the same.
        super(new ItemTarget(beds, count), createBedRecipe(wool), new boolean[]{true, true, true, false, false, false, false, false, false});
        _visualBedTarget = new ItemTarget(beds, count);
    }
    public CollectBedTask(Item bed, String woolCatalogueName, int count) {
        this(new Item[]{bed}, new ItemTarget(woolCatalogueName, 1), count);
    }
    public CollectBedTask(int count) {
        this(ItemUtil.BED, TaskCatalogue.getItemTarget("wool", 1), count);
    }


    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(BEDS);
        super.onResourceStart(mod);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(BEDS);
        super.onStop(mod, interruptTask);
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // Break beds from the world if possible, that would be pretty fast.
        if (mod.getBlockTracker().anyFound(BEDS)) {
            // Failure + blacklisting is encapsulated within THIS task
            return new MineAndCollectTask(new ItemTarget(ItemUtil.BED, 1), BEDS, MiningRequirement.HAND);
        }
        return super.onResourceTick(mod);
    }

    @Override
    protected boolean isEqualResource(ResourceTask obj) {
        if (obj instanceof CollectBedTask) {
            CollectBedTask task = (CollectBedTask) obj;
            return task._visualBedTarget.equals(_visualBedTarget);
        }
        return false;
    }

    @Override
    protected String toDebugStringName() {
        return "Crafting bed: " + _visualBedTarget;
    }

    private static CraftingRecipe createBedRecipe(ItemTarget wool) {
        ItemTarget w = wool;
        ItemTarget p = TaskCatalogue.getItemTarget("planks", 1);
        return CraftingRecipe.newShapedRecipe(new ItemTarget[]{w, w, w, p, p, p, null, null, null}, 1);
    }
}
