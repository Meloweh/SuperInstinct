package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TPAura {
    private boolean attacking = false;
    private List<ArrowEntity> used = new LinkedList<>();
    public boolean attemptAura(final AltoClef mod) {
        final List<SkeletonEntity> skels = mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class);
        final List<SkeletonEntity> nearbySkels = skels.stream().filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE).collect(Collectors.toList());
        final List<Entity> nearbyHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.TP_RADIUS
                        && !(e instanceof SkeletonEntity)
                        && !(e instanceof CreeperEntity)
                        && !(e instanceof ProjectileEntity))
                .collect(Collectors.toList());
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class).stream()
                .filter(e -> (int)Math.ceil(distanceTo(e.getPos(), mod.getPlayer().getPos())) < DefenseConstants.TP_RADIUS && !used.contains(e))
                .collect(Collectors.toList());

        if (nearbyHostiles.size() < 1) {
            attacking = false;
            //System.out.println("nearbyHostiles.size() < 1");
        }

        if (arrows.size() > 0) {
            System.out.println("arrows.size() > 0");
            final ArrowEntity arrow = arrows.get(0);
            final Vec3d vecProj = arrow.getPos();
            final Vec3d velProj = arrow.getVelocity();
            final Vec3d velNormal = velProj.normalize();
            final Vec3d oppositeDir = velNormal.multiply(-1.5);
            final Vec3d rawTpGoal = vecProj.add(oppositeDir);
            final BlockPos tpGoal = new BlockPos(rawTpGoal);
            if (canTpThere(tpGoal, mod)) {
                System.out.println("can dodge");
                used.removeIf(e -> e == null || e.isRegionUnloaded() || e.horizontalCollision || e.verticalCollision || e.distanceTo(mod.getPlayer()) > 70);
                used.add(arrow);
                mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5);
            } else {
                System.out.println("cannot dodge");
            }
        } else if (nearbyHostiles.size() > 0 && nearbySkels.size() < 1) {
            //System.out.println("nearbySkels.size() < 1");
            final Entity entity = nearbyHostiles.get(0);
            final Vec3d eye = entity.getEyePos();
            final BlockPos eyeBlock = new BlockPos(eye);
            final BlockPos tpGoal = eyeBlock.up();
            if (canTpThere(tpGoal, mod)) {
                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5);
                //mod.getMobDefenseChain().setTask(new KillEntityTask(entity));
                float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                // Equip weapon
                KillEntityTask.equipWeapon(mod);
                if (hitProg >= 1) {
                    if (mod.getPlayer().isOnGround() || mod.getPlayer().getVelocity().getY() < 0 || mod.getPlayer().isTouchingWater()) {
                        LookHelper.lookAt(mod, entity.getEyePos());
                        mod.getControllerExtras().attack(entity);
                    }
                }
                attacking = true;
            }
        }
        return true;
    }

    private boolean canTpThere(final BlockPos tpGoal, final AltoClef mod) {
        final BlockPos headGoal = tpGoal.up();
        final BlockState tpState = mod.getWorld().getBlockState(tpGoal);
        if (!tpState.getBlock().equals(Blocks.AIR)) {
            return false;
        }
        final BlockState headState = mod.getWorld().getBlockState(headGoal);
        if (!headState.getBlock().equals(Blocks.AIR)) {
            return false;
        }
        return true;
    }

    private float distanceTo(Vec3d a, Vec3d b) {
        float f = (float)(a.getX() - b.getX());
        float g = (float)(a.getY() - b.getY());
        float h = (float)(a.getZ() - b.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public boolean isAttacking() {
        return this.attacking;
    }
}
