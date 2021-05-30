package adris.altoclef.tasks.resources;


import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInTableTask;
import adris.altoclef.tasks.MineAndCollectTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.SmeltInFurnaceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.SmeltTarget;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;


public class CollectGoldIngotTask extends ResourceTask {
    private final int count;

    public CollectGoldIngotTask(int count) {
        super(Items.GOLD_INGOT, count);
        this.count = count;
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
        if (mod.getCurrentDimension() == Dimension.OVERWORLD) {
            return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(Items.GOLD_INGOT, count), new ItemTarget("gold_ore", count)));
        } else if (mod.getCurrentDimension() == Dimension.NETHER) {
            // If we have enough nuggets, craft them.
            int nuggs = mod.getInventoryTracker().getItemCountIncludingTable(Items.GOLD_NUGGET);
            int nuggs_needed = count * 9 - mod.getInventoryTracker().getItemCountIncludingTable(Items.GOLD_INGOT) * 9;
            if (nuggs >= nuggs_needed) {
                ItemTarget n = new ItemTarget(Items.GOLD_NUGGET);
                CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("gold_ingot", new ItemTarget[]{
                        n, n, n, n, n, n, n, n, n
                }, 1);
                return new CraftInTableTask(Items.GOLD_INGOT, count, recipe);
            }
            // Mine nuggets
            return new MineAndCollectTask(new ItemTarget(Items.GOLD_NUGGET, count * 9), new Block[]{ Blocks.NETHER_GOLD_ORE },
                                          MiningRequirement.WOOD);
        }
        return null;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask obj) {
        return obj instanceof CollectGoldIngotTask && ((CollectGoldIngotTask) obj).count == count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + count + " gold.";
    }
}
