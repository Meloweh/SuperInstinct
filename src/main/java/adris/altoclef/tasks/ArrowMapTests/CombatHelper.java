/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.defense.DefenseConstants;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.slot.MoveItemToSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public static void startShielding(AltoClef mod) {
        ItemStack handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot());
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (handItem.isFood()) {
            mod.getSlotHandler().clickSlot(PlayerSlot.getEquipSlot(), 0, SlotActionType.PICKUP);
        }
        if (cursor.isFood()) {
            Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
            if (toMoveTo.isPresent()) {
                Slot garbageSlot = toMoveTo.get();
                mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
            }
        }
        IS_HOLDING_SHIELD = true;
        mod.getInputControls().hold(Input.SNEAK);
        mod.getInputControls().hold(Input.CLICK_RIGHT);
        mod.getExtraBaritoneSettings().setInteractionPaused(true);
    }

    public static void stopShielding(AltoClef mod) {
        if (IS_HOLDING_SHIELD) {
            ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
            if (cursor.isFood()) {
                Optional<Slot> toMoveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
                if (toMoveTo.isPresent()) {
                    Slot garbageSlot = toMoveTo.get();
                    mod.getSlotHandler().clickSlot(garbageSlot, 0, SlotActionType.PICKUP);
                }
            }
            mod.getInputControls().release(Input.SNEAK);
            mod.getInputControls().release(Input.CLICK_RIGHT);
            mod.getExtraBaritoneSettings().setInteractionPaused(false);
            IS_HOLDING_SHIELD = false;
        }
    }
    public static boolean equipShield(final AltoClef mod) {
        if (!hasShield(mod)) return false;
        ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
        if (shieldSlot.getItem() != Items.SHIELD) {
            mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
            return true;
        } else {
            startShielding(mod);
        }
        return false;
    }
    private static void attack(final AltoClef mod, final Entity entity) {
        LookHelper.lookAt(mod, entity.getEyePos());
        mod.getControllerExtras().attack(entity);
    }
    public static void punchNearestHostile(final AltoClef mod, final boolean forceField, List<Entity> entities) {
        final List<Entity> hostiles = entities.stream().filter(e -> !(e instanceof ProjectileEntity) && mod.getPlayer().distanceTo(e) <= DefenseConstants.PUNCH_RADIUS).collect(Collectors.toList());
        if (hostiles.isEmpty()) return;
        if (forceField) {
            mod.getSlotHandler().forceDeequipHitTool();
            hostiles.forEach(e -> attack(mod, e));
        } else {
            final Entity entity = hostiles.get(0);
            KillEntityTask.equipWeapon(mod);
            float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
            if (hitProg >= 1) {
                attack(mod, entity);
            }
        }
    }
}
