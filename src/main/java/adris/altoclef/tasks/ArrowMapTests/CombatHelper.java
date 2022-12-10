/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.slot.MoveItemToSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import baritone.api.utils.input.Input;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CombatHelper {
    private static boolean IS_HOLDING_SHIELD = false;

    public static boolean hasShield(final AltoClef mod) {
        return mod.getItemStorage().hasItem(Items.SHIELD) || mod.getItemStorage().hasItemInOffhand(Items.SHIELD);
    }

    public static boolean isShieldEquipped() {
        final ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
        return shieldSlot.getItem() != Items.SHIELD;
    }

    /*public static Task equipShieldTask(final AltoClef mod) {
        if (!isShieldEquipped()) {
            return new MoveItemToSlotTask(new ItemTarget(Items.SHIELD), PlayerSlot.OFFHAND_SLOT);
        }
        return null;
    }*/

    public static void doShielding(final AltoClef mod) {
        if (!isShieldEquipped()) {
            mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
        }

        if (isShieldEquipped()) {
            if (!mod.getInputControls().isHeldDown(Input.CLICK_RIGHT)) mod.getInputControls().hold(Input.CLICK_RIGHT);
            //System.out.println("Should hold");
            IS_HOLDING_SHIELD = true;
        } else {
            IS_HOLDING_SHIELD = false;
            if (mod.getInputControls().isHeldDown(Input.CLICK_RIGHT)) {
                mod.getInputControls().release(Input.CLICK_RIGHT);
            }
        }
    }

    public static void undoShielding(final AltoClef mod) {
        if (mod.getInputControls().isHeldDown(Input.CLICK_RIGHT)) mod.getInputControls().release(Input.CLICK_RIGHT);
        IS_HOLDING_SHIELD = false;
    }

    public static boolean isHoldingShield() {
        return IS_HOLDING_SHIELD;
    }
}
