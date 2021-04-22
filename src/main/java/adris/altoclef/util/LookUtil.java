package adris.altoclef.util;

import adris.altoclef.AltoClef;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class LookUtil {

    public static EntityHitResult raycast(Entity from, Entity to, double reachDistance) {
        Vec3d fromPos = from.getCameraPosVec(1f),
                toPos = to.getCameraPosVec(1f);
        Vec3d direction = (toPos.subtract(fromPos).normalize().multiply(reachDistance));
        Box box = to.getBoundingBox();
        return ProjectileUtil.raycast(from, fromPos, fromPos.add(direction), box, entity -> entity.equals(to), 0);
    }

    public static boolean seesPlayer(Entity entity, Entity player, double maxRange, Vec3d entityOffs, Vec3d playerOffs) {
        return seesPlayerOffset(entity, player, maxRange, entityOffs, playerOffs) || seesPlayerOffset(entity, player, maxRange, entityOffs, new Vec3d(0, -1, 0).add(playerOffs));
    }
    public static boolean seesPlayer(Entity entity, Entity player, double maxRange) {
        return seesPlayer(entity, player, maxRange, Vec3d.ZERO, Vec3d.ZERO);
    }

    public static Vec3d getCameraPos(Entity entity) {
        boolean isSneaking = false;
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            isSneaking = player.isSneaking();
        }
        return isSneaking? RayTraceUtils.inferSneakingEyePosition(entity) : entity.getCameraPosVec(1.0F);
    }

    //  1: Looking straight at pos
    //  0: pos is 90 degrees to the side
    // -1: pos is 180 degrees away (looking away completely)
    public static double getLookCloseness(Entity entity, Vec3d pos) {
        Vec3d rotDirection = entity.getRotationVecClient();
        Vec3d lookStart = getCameraPos(entity);
        Vec3d deltaToPos = pos.subtract(lookStart);
        Vec3d deltaDirection = deltaToPos.normalize();
        return rotDirection.dotProduct(deltaDirection);
    }

    public static boolean tryAvoidingInteractable(AltoClef mod) {
        if (isCollidingContainer(mod)) {
            randomOrientation(mod);
            return false;
        }
        return true;
    }

    private static boolean seesPlayerOffset(Entity entity, Entity player, double maxRange, Vec3d offsetEntity, Vec3d offsetPlayer) {
        Vec3d start = entity.getCameraPosVec(1f).add(offsetEntity);
        Vec3d end = player.getCameraPosVec(1f).add(offsetPlayer);
        Vec3d delta = end.subtract(start);
        if (delta.lengthSquared() > maxRange*maxRange) {
            end = start.add(delta.normalize().multiply(maxRange));
        }
        BlockHitResult b =  entity.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
        return b.getType() == HitResult.Type.MISS;
    }

    private static boolean isCollidingContainer(AltoClef mod) {

        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
            mod.getPlayer().closeHandledScreen();
            return true;
        }

        IPlayerContext ctx = mod.getClientBaritone().getPlayerContext();
        HitResult result = MinecraftClient.getInstance().crosshairTarget;
        if (result == null) return false;
        if (result.getType() == HitResult.Type.BLOCK) {
            Block block = mod.getWorld().getBlockState(new BlockPos(result.getPos())).getBlock();
            if (block instanceof ChestBlock
                    || block instanceof EnderChestBlock
                    || block instanceof CraftingTableBlock
                    || block instanceof AbstractFurnaceBlock
                    || block instanceof LoomBlock
                    || block instanceof CartographyTableBlock
                    || block instanceof EnchantingTableBlock
            ) {
                return true;
            }
        } else if (result.getType() == HitResult.Type.ENTITY) {
            if (result instanceof EntityHitResult) {
                Entity entity = ((EntityHitResult) result).getEntity();
                if (entity instanceof MerchantEntity) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void randomOrientation(AltoClef mod) {
        Rotation r = new Rotation((float)Math.random() * 360f, -90 + (float)Math.random() * 180f);
        mod.getClientBaritone().getLookBehavior().updateTarget(r, true);
    }

    public static void lookAt(AltoClef mod, Vec3d toLook) {
        Rotation targetRotation = RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), toLook, mod.getClientBaritone().getPlayerContext().playerRotations());
        mod.getClientBaritone().getLookBehavior().updateTarget(targetRotation, true);
    }
}
