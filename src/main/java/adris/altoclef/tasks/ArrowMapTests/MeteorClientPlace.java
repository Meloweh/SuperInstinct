/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.util.MovementCounter;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

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
}