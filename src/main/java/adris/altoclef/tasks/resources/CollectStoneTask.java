package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.container.SmeltInBlastFurnaceTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.SmeltTarget;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class CollectStoneTask extends ResourceTask {

    private final int _count;

    public CollectStoneTask(int count) {
        super(Items.STONE, count);
        _count = count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBehaviour().push();
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (mod.getItemStorage().hasItem(Items.BLAST_FURNACE) ||
                mod.getBlockTracker().anyFound(Blocks.BLAST_FURNACE) ||
                mod.getEntityTracker().itemDropped(Items.BLAST_FURNACE)) {
            return new SmeltInBlastFurnaceTask(new SmeltTarget(new ItemTarget(Items.STONE, _count), new ItemTarget(Items.COBBLESTONE, _count)));
        } else {
            return TaskCatalogue.getItemTask(Items.BLAST_FURNACE, 1);
        }
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectStoneTask && ((CollectStoneTask) other)._count == _count;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + _count + " " + Items.STONE.toString();
    }
}
