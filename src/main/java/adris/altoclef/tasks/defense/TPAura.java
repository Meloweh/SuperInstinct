package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.tasks.ArrowMapTests.BowArrowIntersectionTracer;
import adris.altoclef.tasks.ArrowMapTests.CollisionFeedback;
import adris.altoclef.tasks.ArrowMapTests.SimMovementState;
import adris.altoclef.tasks.ArrowMapTests.TraceResult;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TPAura {
    private boolean attacking = false;
    private MobHatV2 mobHat = new MobHatV2();
    private List<ArrowEntity> used = new LinkedList<>();
    //private Optional<SkeletonEntity> recentSource = Optional.empty();
    private Optional<BlockPos> prev = Optional.empty();
    private final static Random rand = new Random();

    private static void cancelFall(final AltoClef mod) {
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

    private static List<Entity> explodingCreepersAt(final AltoClef mod, final Vec3d vec) {
        return mod.getEntityTracker().getTrackedEntities(CreeperEntity.class).stream().filter(e -> distanceTo(e.getPos(), vec) > DefenseConstants.CREEPER_RADIUS).collect(Collectors.toList());
    }

    private static boolean explodingCreeperAt(final AltoClef mod, final Vec3d vec) {
        return explodingCreepersAt(mod, vec).size() > 0;
    }

    public boolean attemptAura(final AltoClef mod) {
        //final List<SkeletonEntity> skels = mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class);
        //final List<SkeletonEntity> nearbySkels = skels.stream().filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE).collect(Collectors.toList());
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
            do {
                final Entity entity = entityIt.next();
                final Vec3d eye = entity.getEyePos();
                final BlockPos eyeBlock = new BlockPos(eye);
                final BlockPos tpGoal = eyeBlock.up();

                if (entity instanceof CreeperEntity && entity.distanceTo(mod.getPlayer()) <= DefenseConstants.CREEPER_RADIUS) {
                    final CreeperEntity creeper = (CreeperEntity) entity;
                    final float fusingTime = creeper.getClientFuseTime(1);
                    if (Float.compare(fusingTime, 0.75f) >= 0) {
                        chorusTp(mod);
                        /*MobDefenseChain.safeToEat = false;
                        for (byte dy = 0; dy <= 3; dy++) {
                            for (byte i = 0; i <= 2; i++) {
                                final float r = 4 + i;
                                final float theta = rand.nextFloat() * 2 * MathHelper.PI;
                                final double x = creeper.getX() + r * MathHelper.cos(theta) * MathHelper.sin(theta);
                                final double z = creeper.getZ() + r * MathHelper.sin(theta);
                                final double y = creeper.getY() + r * MathHelper.cos(theta) * MathHelper.cos(theta);
                                final BlockPos b = new BlockPos(x, y, z);
                                if (canTpThere(b, mod)) {
                                    MobDefenseChain.safeToEat = true;
                                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                                    mod.getPlayer().setVelocity(0d, 0d, 0d);
                                    cancelFall(mod);
                                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.up().getY(), tpGoal.getZ() + 0.5);
                                    return true;
                                }
                            }
                        }
                        continue;*/
                    }
                }

                final boolean canTpUpUp = canTpThere(tpGoal.up(), mod);
                if (canTpUpUp && mobHat.canAttemptHat(mod)) {
                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                    mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.up().getY(), tpGoal.getZ() + 0.5);
                }
                attacking = mobHat.attemptHat(mod);
                if (!attacking && canTpThere(tpGoal, mod) && (!(entity instanceof SkeletonEntity) || nearbyHostiles.size() < 2 && mod.getPlayer().getHealth() > 10) && !(entity instanceof CreeperEntity)) {
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

    /*private boolean canTpThere(final BlockPos tpGoal, final AltoClef mod) {
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
            if (targetInSight(new Vec3d(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5), arrow)) {
                return false;
            }
        }
        return true;
    }
    public static boolean canTpThere(final AltoClef mod, final BlockPos tpGoal) {
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
    }*/
    public static boolean isSpaceEmpty(final AltoClef mod, final Vec3d target, final boolean isOffset) {
        final Box box = mod.getPlayer().getBoundingBox();
        final Box newBox = isOffset ? box.offset(target) : box.offset(mod.getPlayer().getPos().multiply(-1)).offset(target);//new Box(box.minX + x, box.minY + g, box.minZ + z, box.maxX + x, box.maxY + g, box.maxZ + z);
        return mod.getWorld().isSpaceEmpty(newBox);
    }
    public static boolean canTpThere(final AltoClef mod, final Vec3d tpGoal) {
        if (explodingCreeperAt(mod, tpGoal)) {
            return false;
        }
        if (!isSpaceEmpty(mod, tpGoal, false)) {
            return false;
        }
        return true;
    }
    private boolean canTpThere(final Vec3d tpGoal, final AltoClef mod) {
        if (!canTpThere(mod, tpGoal)) {
            return false;
        }
        for (final ArrowEntity arrow : used) {
            if (targetInSight(new Vec3d(tpGoal.getX(), tpGoal.getY(), tpGoal.getZ()), arrow)) {
                return false;
            }
        }
        return true;
    }


    public static boolean floorTp(final AltoClef mod, final Vec3d newPos) {
        final ClientPlayerEntity player = mod.getPlayer();
        //final double x = newPos.getX();
        //final double y = newPos.getY();
        //final double z = newPos.getZ();
        double g = newPos.getY();
        boolean bl = false;
        BlockPos blockPos = new BlockPos(newPos);
        World world = mod.getWorld();
        if (world.isChunkLoaded(blockPos)) {
            boolean bl2 = false;

            while(!bl2 && blockPos.getY() > world.getBottomY()) {
                BlockPos blockPos2 = blockPos.down();
                BlockState blockState = world.getBlockState(blockPos2);
                if (blockState.getMaterial().blocksMovement()) {
                    bl2 = true;
                } else {
                    --g;
                    blockPos = blockPos2;
                }
            }

            if (bl2) {
                //this.requestTeleport(x, g, z);
                //player.setPos(x, g, z);
                /*final Box box = player.getBoundingBox();
                final Box newBox = new Box(box.minX + x, box.minY + g, box.minZ + z, box.maxX + x, box.maxY + g, box.maxZ + z);

                if (world.isSpaceEmpty(newBox)) { // && !world.containsFluid(player.getBoundingBox())) {
                    System.out.println("place empty");
                    player.setPos(x, g, z);
                    bl = true;
                } else {
                    System.out.println("place not empty");

                }*/
                if (isSpaceEmpty(mod, newPos, true)) {
                    player.setPos(newPos.getX(), g, newPos.getZ());
                }
            }
        }

        return bl;
    }

    public static boolean chorusTp(final AltoClef mod) {
        final ClientPlayerEntity user = mod.getPlayer();
        final World world = mod.getWorld();
        for(int i = 0; i < 16; ++i) {
            /*double rx = (user.getRandom().nextDouble() - 0.5) * 12.0;
            if (Math.abs(rx) < 4) {
                rx = user.getRandom().nextInt(2) == 0 ? -4 : 4;
            }
            double rz = (user.getRandom().nextDouble() - 0.5) * 12.0;
            if (Math.abs(rz) < 4) {
                rz = user.getRandom().nextInt(2) == 0 ? -4 : 4;
            }
            double g = user.getX() + rx;
            double h = MathHelper.clamp(user.getY() + (double)(user.getRandom().nextInt(16) - 8), world.getBottomY(), world.getBottomY() + 512 - 1);
            double j = user.getZ() + rz;*/
            /*double g = user.getX() + (user.getRandom().nextDouble() - 0.5) * 12.0;
            double h = MathHelper.clamp(user.getY() + (double)(user.getRandom().nextInt(16) - 8), world.getBottomY(), world.getBottomY() + 512);
            double j = user.getZ() + (user.getRandom().nextDouble() - 0.5) * 12.0;*/

            final double angle = user.getRandom().nextFloat() * 360;
            final double r = DefenseConstants.CREEPER_RADIUS; //1.6;// + user.getRandom().nextInt(3);
            final double g = user.getX() + Math.cos(angle) * r;
            final double h = MathHelper.clamp(user.getY() + (double)(user.getRandom().nextInt(16) - 8), world.getBottomY(), world.getBottomY() + 512);
            final double j = user.getZ() + Math.sin(angle) * r;

            /*if (user.hasVehicle()) {
                user.stopRiding();
            }*/

            //final BlockPos newPos = new BlockPos(g, h, j);
            if (floorTp(mod, new Vec3d(g, h, j))) {
                return true;
            }
            /*if (canTpThere(mod, newPos)) {
                tp(mod, newPos);
                return true;
            }*/
        }
        return false;
    }

    public static final void tp(final AltoClef mod, final BlockPos tpGoal) {
        mod.getPlayer().setVelocity(0d, 0d, 0d);
        cancelFall(mod);
        mod.getPlayer().setPos(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5);
    }

    private static float distanceTo(Vec3d a, Vec3d b) {
        float f = (float)(a.getX() - b.getX());
        float g = (float)(a.getY() - b.getY());
        float h = (float)(a.getZ() - b.getZ());
        return MathHelper.sqrt(f * f + g * g + h * h);
    }

    public boolean isAttacking() {
        return this.attacking;
    }
}
