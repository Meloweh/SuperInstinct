package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class CollectRedSandstoneTask extends ResourceTask {

    private final int _count;

    public CollectRedSandstoneTask(int targetCount) {
        super(Items.RED_SANDSTONE, targetCount);
        _count = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (mod.getInventoryTracker().getItemCountIncludingTable(false, Items.RED_SAND) >= 4) {
            int target = mod.getInventoryTracker().getItemCount(Items.RED_SANDSTONE) + 1;
            ItemTarget s = new ItemTarget(Items.RED_SAND, 1);
            return new CraftInInventoryTask(new ItemTarget(Items.RED_SANDSTONE, target), CraftingRecipe.newShapedRecipe("red_sandstone", new ItemTarget[]{s, s, s, s}, 1));
        }
        return new MineAndCollectTask(new ItemTarget(new Item[]{Items.RED_SANDSTONE, Items.RED_SAND}), new Block[]{Blocks.RED_SANDSTONE, Blocks.RED_SAND}, MiningRequirement.WOOD).forceDimension(Dimension.OVERWORLD);
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectRedSandstoneTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " red sandstone.";
    }
}
