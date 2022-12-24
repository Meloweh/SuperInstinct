package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BowArrowIntersectionTracer;
import adris.altoclef.tasks.ArrowMapTests.CollisionFeedback;
import adris.altoclef.tasks.ArrowMapTests.SimMovementState;
import adris.altoclef.tasks.ArrowMapTests.TraceResult;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

public class TPAura {
    private boolean attacking = false;
    private MobHatV2 mobHat = new MobHatV2();
    private List<ArrowEntity> used = new LinkedList<>();
    //private Optional<SkeletonEntity> recentSource = Optional.empty();
    private Optional<BlockPos> prev = Optional.empty();

    private void cancelFall(final AltoClef mod) {
        mod.getPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
    }
    private double SVM2d(final Vec3d startPos, final Vec3d endPos, final Vec3d orthPoint) {
        Vec3d sub = endPos.subtract(startPos);
        Vec3d n = new Vec3d(-sub.z, 0, sub.x);
        double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.z, 2));
        Vec3d x1minux0 = orthPoint.subtract(startPos);
        double result = x1minux0.x * n.x * nd + x1minux0.z * n.z * nd;
        return result;
    }

    private boolean targetInSight(final Vec3d target, final Entity arrow) {
        final Vec3d arrowVec = new Vec3d(1, 0, 0);//arrow.getVelocity();
        final Vec3d arrowPos = new Vec3d(0, 5, 0);//arrow.getPos();

        Vec3d vec = arrowPos.subtract(target);
        double n = vec.length() * arrowVec.length();
        double phi = Math.acos(vec.dotProduct(arrowVec) / n);
        //System.out.println(phi);
        return phi > 3 * Math.PI / 4;
    }

    public boolean attemptAura(final AltoClef mod) {
        final List<SkeletonEntity> skels = mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class);
        final List<SkeletonEntity> nearbySkels = skels.stream().filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE).collect(Collectors.toList());
        final List<Entity> nearbyHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.TP_RADIUS
                        //&& !(e instanceof SkeletonEntity)
                        //&& !(e instanceof CreeperEntity)
                        && !(e instanceof ProjectileEntity))
                .collect(Collectors.toList());
        nearbyHostiles.sort((a, b) -> {
            int result = Boolean.compare((a instanceof CreeperEntity), b instanceof CreeperEntity);
            if (result == 0) {
                result = Double.compare(b.getBoundingBox().getYLength(), a.getBoundingBox().getYLength());
            }
            if (result == 0) {
                result = Float.compare(a.distanceTo(mod.getPlayer()), b.distanceTo(mod.getPlayer()));//(int) ((a.distanceTo(mod.getPlayer()) - b.distanceTo(mod.getPlayer()))*1000);
            }
            return result;
        });
        used.removeIf(e -> e == null || e.isRegionUnloaded() || e.horizontalCollision || e.verticalCollision || e.distanceTo(mod.getPlayer()) > 70);
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class).stream()
                .filter(e -> (int)Math.ceil(distanceTo(e.getPos(), mod.getPlayer().getPos())) < DefenseConstants.TP_RADIUS && !e.horizontalCollision && !e.verticalCollision && !used.contains(e))
                .collect(Collectors.toList());
        /*arrows.forEach(e -> {
            if (e.horizontalCollision || e.verticalCollision) {
                used.add(e);
            }
        });*/
        //arrows.removeIf(e -> used.contains(e));

        /*if (nearbyHostiles.size() > 0) {
            System.out.println(targetInSight(mod.getPlayer().getPos(), nearbyHostiles.get(0)));
        }*/

        if (nearbyHostiles.size() < 1) {
            attacking = false;
            //System.out.println("nearbyHostiles.size() < 1");
        }

        boolean tryAttack = true;
        if (arrows.size() > 0) {
            //System.out.println("arrows.size() > 0");
            final ArrowEntity arrow = arrows.get(0);
            final Vec3d vecProj = arrow.getPos();
            final Vec3d velProj = arrow.getVelocity();
            final Vec3d velNormal = velProj.normalize();
            final Vec3d oppositeDir = velNormal.multiply(-1.5);
            final Vec3d rawTpGoal = vecProj.add(oppositeDir);
            final BlockPos tpGoal = new BlockPos(rawTpGoal);
            if (canTpThere(tpGoal, mod)) {
                used.add(arrow);
                if (nearbyHostiles.size() > 0) {
                    final double max = Math.max(tpGoal.getY(), nearbyHostiles.get(0).getY());
                    mod.getPlayer().setVelocity(0d, 0d, 0d);
                    cancelFall(mod);
                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, max, tpGoal.getZ() + 0.5);
                } else {
                    mod.getPlayer().setVelocity(0d, 0d, 0d);
                    cancelFall(mod);
                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5);
                }

                tryAttack = false;
            } else {
                System.out.println("cannot dodge"); // TODO (done): ok but then fight if possible
            }
        }
        if (tryAttack && nearbyHostiles.size() > 0) {
            //nearbyHostiles.sort((a, b) -> (int) ((a.distanceTo(mod.getPlayer()) - b.distanceTo(mod.getPlayer()))*1000));
            //nearbyHostiles.sort((a, b) -> Boolean.compare((a instanceof CreeperEntity), b instanceof CreeperEntity));

            final Iterator<Entity> entityIt = nearbyHostiles.iterator();
            final Random rand = new Random();
            do {
                final Entity entity = entityIt.next();
                final Vec3d eye = entity.getEyePos();
                final BlockPos eyeBlock = new BlockPos(eye);
                final BlockPos tpGoal = eyeBlock.up();

                if (entity instanceof CreeperEntity) {
                    final CreeperEntity creeper = (CreeperEntity) entity;
                    final float fusingTime = creeper.getClientFuseTime(1);
                    if (Float.compare(fusingTime, 0.75f) >= 0) {

                        for (byte dy = 0; dy <= 3; dy++) {
                            for (byte i = 0; i < 8; i++) {
                                final float r = 4 + MathHelper.sqrt(rand.nextFloat());
                                final float theta = rand.nextFloat() * 2 * MathHelper.PI;
                                final double x = creeper.getX() + r * MathHelper.cos(theta);
                                final double z = creeper.getZ() + r * MathHelper.sin(theta);
                                final double y = creeper.getY() + dy;
                                final BlockPos b = new BlockPos(x, y, z);
                                if (canTpThere(b, mod)) {
                                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                                    mod.getPlayer().setVelocity(0d, 0d, 0d);
                                    cancelFall(mod);
                                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.up().getY(), tpGoal.getZ() + 0.5);
                                    return true;
                                }
                            }
                        }
                        continue;
                    }
                }

                final boolean canTpUpUp = canTpThere(tpGoal.up(), mod);
                if (canTpUpUp && mobHat.canAttemptHat(mod)) {
                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.up().getY(), tpGoal.getZ() + 0.5);
                }
                attacking = mobHat.attemptHat(mod);
                if (!attacking && canTpThere(tpGoal, mod) && !(entity instanceof SkeletonEntity) && !(entity instanceof CreeperEntity)) {
                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();

                    final Vec3d newPos3d = new Vec3d(tpGoal.getX() + 0.5, entity.getPos().getY() + entity.getBoundingBox().getYLength() + 1, tpGoal.getZ() + 0.5);
                    final BlockPos newpos = new BlockPos(newPos3d.getX(), newPos3d.getY(), newPos3d.getZ());
                    /*if (prev.isEmpty() || !prev.get().equals(newpos)) {
                        mod.getPlayer().setPos(newPos3d.getX(), newPos3d.getY(), newPos3d.getZ());
                        prev = Optional.of(newpos);
                    }*/
                    mod.getPlayer().setVelocity(0d, 0d, 0d);
                    cancelFall(mod);
                    mod.getPlayer().setPos(newPos3d.getX(), newpos.getY(), newPos3d.getZ());
                    //mod.getMobDefenseChain().setTask(new KillEntityTask(entity));
                    float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                    // Equip weapon
                    KillEntityTask.equipWeapon(mod);
                    if (hitProg >= 1) {
                        LookHelper.lookAt(mod, entity.getEyePos());
                        mod.getControllerExtras().attack(entity);
                    }
                    attacking = true;
                }
            } while (entityIt.hasNext() && !attacking);
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
        for (final ArrowEntity arrow : used) {
            /*final TraceResult result = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                    mod.getPlayer().getVelocity(), 20, 0, new Vec3d(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5), mod.getWorld(), SimMovementState.SIM_STAND, null);
            if (result.willPiercePlayer()) {
                return false;
            }*/
            /*final CollisionFeedback cx = BowArrowIntersectionTracer.calculateCollisionX(arrow, mod.getPlayer().getBoundingBox().expand(0.2f));
            final CollisionFeedback cz = BowArrowIntersectionTracer.calculateCollisionZ(arrow, mod.getPlayer().getBoundingBox().expand(0.2f));
            final TraceResult result = BowArrowIntersectionTracer.getTraceResultFromFeedback(cx, cx, cz, arrow, mod.getPlayer().getBoundingBox().expand(0.2f), 0, 20);
            if (result.willPiercePlayer()) {
                return false;
            }*/
            //System.out.println(targetInSight(new Vec3d(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5), arrow));
            if (targetInSight(new Vec3d(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5), arrow)) {
                return false;
            }
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
