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
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class CollectCoarseDirtTask extends ResourceTask {

    private static final float CLOSE_ENOUGH_COARSE_DIRT = 128;
    private final int _count;

    public CollectCoarseDirtTask(int targetCount) {
        super(Items.COARSE_DIRT, targetCount);
        _count = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(Blocks.COARSE_DIRT);
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        double c = Math.ceil(Double.valueOf(_count-mod.getInventoryTracker().getItemCount(Items.COARSE_DIRT)) / 4) * 2; // Minimum number of dirt / gravel needed to complete the recipe, accounting for coarse dirt already collected.
        BlockPos closest = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.COARSE_DIRT);

        // If not enough dirt and gravel for the recipe, and coarse dirt within a certain distance, collect coarse dirt
        if (!(mod.getInventoryTracker().getItemCount(Items.DIRT) >= c  && 
            mod.getInventoryTracker().getItemCount(Items.GRAVEL) >= c) && 
            closest != null && closest.isWithinDistance(mod.getPlayer().getPos(), CLOSE_ENOUGH_COARSE_DIRT)) { 
            return new MineAndCollectTask(new ItemTarget(Items.COARSE_DIRT), new Block[]{Blocks.COARSE_DIRT}, MiningRequirement.HAND).forceDimension(Dimension.OVERWORLD);
        }
        else {
            int target = _count;
            ItemTarget d = new ItemTarget(Items.DIRT, 1);
            ItemTarget g = new ItemTarget(Items.GRAVEL, 1);
            return new CraftInInventoryTask(new ItemTarget(Items.COARSE_DIRT, target), CraftingRecipe.newShapedRecipe("coarse_dirt", new ItemTarget[]{d, g, g, d}, 4));
        }
    }
    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.COARSE_DIRT);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectCoarseDirtTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " Coarse Dirt.";
    }
}
