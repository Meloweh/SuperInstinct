/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.util.MovementCounter;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */
public class MeteorClientPlace {
    private static Vec3d hitPos = new Vec3d(0, 0, 0);
    private final static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean place(BlockPos blockPos, Hand hand, int slot, boolean swingHand, boolean checkEntities) {
        if (slot < 0 || slot > 8) return false;
        if (!canPlace(blockPos, checkEntities)) return false;

        hitPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

        Direction side = Direction.UP;
        Direction s = side;

        place(new BlockHitResult(hitPos, s, blockPos, false), hand, swingHand);
        return true;
    }

    private static void place(BlockHitResult blockHitResult, Hand hand, boolean swing) {
        boolean wasSneaking = mc.player.input.sneaking;
        mc.player.input.sneaking = false;

        ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, blockHitResult);

        if (result.shouldSwingHand()) {
            if (swing) mc.player.swingHand(hand);
            else mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(hand));
        }

        mc.player.input.sneaking = wasSneaking;
        MovementCounter.fillMovements++;
    }

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        if (blockPos == null) return false;

        // Check y level
        if (!World.isValid(blockPos)) return false;

        // Check if current block is replaceable
        if (!mc.world.getBlockState(blockPos).getMaterial().isReplaceable()) return false;

        // Check if intersects entities
        return !checkEntities || mc.world.canPlace(mc.world.getBlockState(blockPos), blockPos, ShapeContext.absent());
    }

    public static boolean canPlace(BlockPos blockPos) {
        return canPlace(blockPos, true);
    }

    public static boolean canBreak(BlockPos blockPos, BlockState state) {
        if (!mc.player.isCreative() && state.getHardness(mc.world, blockPos) < 0) return false;
        return state.getOutlineShape(mc.world, blockPos) != VoxelShapes.empty();
    }
    public static boolean canBreak(BlockPos blockPos) {
        return canBreak(blockPos, mc.world.getBlockState(blockPos));
    }

    public static boolean canInstaBreak(BlockPos blockPos, BlockState state) {
        return mc.player.isCreative() || state.calcBlockBreakingDelta(mc.player, mc.world, blockPos) >= 1;
    }
    public static boolean canInstaBreak(BlockPos blockPos) {
        return canInstaBreak(blockPos, mc.world.getBlockState(blockPos));
    }

    public static void packetBreakBlock(final World world, BlockPos block) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, block, Direction.UP));
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, block, Direction.UP));
    }
    /*public static void equipNeededTool(final AltoClef mod, final BlockPos pos) {
        final BlockState state = mod.getWorld().getBlockState(pos);

        BlockState state = mod.getWorld().getBlockState(pos);
        Optional<Slot> bestToolSlot = StorageHelper.getBestToolSlot(mod, state);
        Slot currentEquipped = PlayerSlot.getEquipSlot();

        // if baritone is running, only accept tools OUTSIDE OF HOTBAR!
        // Baritone will take care of tools inside the hotbar.
        if (bestToolSlot.isPresent() && !bestToolSlot.get().equals(currentEquipped)) {
            // ONLY equip if the item class is STRICTLY different (otherwise we swap around a lot)
            if (StorageHelper.getItemStackInSlot(currentEquipped).getItem() != StorageHelper.getItemStackInSlot(bestToolSlot.get()).getItem()) {
                boolean isAllowedToManage = !mod.getClientBaritone().getPathingBehavior().isPathing() || bestToolSlot.get().getInventorySlot() >= 9;
                if (isAllowedToManage) {
                    //Debug.logMessage("Found better tool in inventory, equipping.");
                    ItemStack bestToolItemStack = StorageHelper.getItemStackInSlot(bestToolSlot.get());
                    Item bestToolItem = bestToolItemStack.getItem();
                    mod.getSlotHandler().forceEquipItem(bestToolItem);
                }
            }
        }
    }*/
    public static boolean packetBreakBlocks(final AltoClef mod, List<BlockPos> blocks) {
        final World world = mod.getWorld();
        boolean mining = false;
        //mod.getSlotHandler().holdDefaultTool(mod);
        for (BlockPos block : blocks) {
            final BlockState state = world.getBlockState(block);
            mod.getSlotHandler().equipBestToolFor(state);
            //System.out.println(block.toString() + " state: " + state.isAir());
            if (state.isAir()) {
                continue;
            }
            packetBreakBlock(world, block);
            //mining = true;
            return true;
        }
        return mining;
    }

}