package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.defense.chess.VerticalWing;
import adris.altoclef.tasks.defense.chess.Wing1D;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.MovementCounter;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class CombatHandler {
    private boolean attacking = false;
    private List<Entity> used = new LinkedList<>();
    private Optional<BlockPos> prev = Optional.empty();

    private static void cancelFall(final AltoClef mod) {
        mod.getPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
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
        return mod.getEntityTracker().getTrackedEntities(CreeperEntity.class).stream().filter(e -> distanceTo(e.getPos(), vec) <= DefenseConstants.CREEPER_RADIUS && isCreeperCritical(e)).collect(Collectors.toList());
    }
    private static boolean explodingCreeperAt(final AltoClef mod, final Vec3d vec) {
        return explodingCreepersAt(mod, vec).size() > 0;
    }
    private Vec3d hCenterOf(final Vec3d vec) {
        return new Vec3d(Math.floor(vec.getX()) + 0.5, vec.getY(), Math.floor(vec.getZ() + 0.5));
    }
    private static boolean isCreeperCritical(final CreeperEntity creeper) {
        final float fusingTime = creeper.getClientFuseTime(1);
        return Float.compare(fusingTime, 0.75f) >= 0;
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

        /*if (nearbyHostiles.size() < 1 && !mod.getPlayer().isOnGround()) {
            final List<Entity> hostilesSomewhatNearby = mod.getEntityTracker().getHostiles().stream()
                    .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.HOSTILE_DISTANCE  && !(e instanceof ProjectileEntity)).collect(Collectors.toList());
            if (hostilesSomewhatNearby.size() > 0) {
                chorusTp(mod, true);
            }
        }*/
        final List<Entity> hostilesSomewhatNearby = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.HOSTILE_DISTANCE  && !(e instanceof ProjectileEntity)).collect(Collectors.toList());
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

        if (nearbyHostiles.size() > 0) {
            MobDefenseChain.safeToEat = false;
            final Iterator<Entity> entityIt = nearbyHostiles.iterator();
            do {
                final Entity entity = entityIt.next();
                //final Vec3d eye = entity.getEyePos();//new Vec3d(mod.getPlayer().getX(), entity.getEyeY(), mod.getPlayer().getZ());//entity.getEyePos();
                //final Vec3d tpGoal = hCenterOf(eye).add(0, 1, 0);

                if (entity instanceof CreeperEntity && entity.distanceTo(mod.getPlayer()) <= DefenseConstants.CREEPER_RADIUS) {
                    final CreeperEntity creeper = (CreeperEntity) entity;
                    if (isCreeperCritical(creeper)) {
                        /*final Wing1D above1 = new Wing1D(mod.getWorld(), mod.getPlayer().getBlockPos().up().up());
                        final Wing1D above2 = new Wing1D(mod.getWorld(), above1.getPos().up());
                        if (above1.hasFailed() || above2.hasFailed()) {
                            if (canTpThere(mod, BlockPosHelper.toVec3dCenter(above2.getPos().up()))) {
                                tp(mod, BlockPosHelper.toVec3dCenter(above2.getPos().up()));
                                return true;
                            }
                        }*/
                        final VerticalWing vWing = new VerticalWing(mod.getWorld(),
                                mod.getPlayer().getBlockPos().offset(Direction.DOWN, DefenseConstants.TP_RADIUS),
                                mod.getPlayer().getBlockPos().offset(Direction.UP, DefenseConstants.TP_RADIUS), true);
                        final Optional<BlockPos> founding = vWing.getFounding();
                        if (founding.isPresent()) {
                            final BlockPos target = founding.get();
                            if (target.getY() != mod.getPlayer().getBlockY()) {
                                tp(mod, BlockPosHelper.toVec3dCenter(target));
                                return true;
                            }
                        }
                        Queen.attemptJump(mod, true);
                        return true;
                    }
                    return false;
                }

                //final boolean canTpUp = canTpThere(tpGoal, mod);
                //System.out.println(canTpUp);
                if (Math.abs(mod.getPlayer().getY() - entity.getY()) < 3) {
                    if (mod.getPlayer().isOnGround()) {
                        mod.getPlayer().jump();
                    }
                    if (entity.isOnGround() && (new Wing1D(mod.getWorld(), mod.getPlayer().getBlockPos().down()).hasFailed())) {
                        BasicDefenseManager.fill(mod, mod.getPlayer().getBlockPos().down());
                    }
                }
                if ((!(entity instanceof SkeletonEntity) || nearbyHostiles.size() < 2 && mod.getPlayer().getHealth() > 10) && (!(entity instanceof CreeperEntity) || !isCreeperCritical((CreeperEntity) entity))) {
                    float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                    // Equip weapon
                    //if (!(entity instanceof SkeletonEntity)) {
                        KillEntityTask.equipWeapon(mod);
                    //}
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
        final boolean space = mod.getWorld().isSpaceEmpty(newBox);
        /*if (space) {
            return true;
        }*/
        if (!space) {
            System.out.println("false cuz !isSpaceEmpty: \nOldBox=" + newBox.toString() + "\nNewBox=" + newBox);
            System.out.println("target: " + target.toString());
            System.out.println("current: " + mod.getPlayer().getPos().toString());
            System.out.println("onGround: " + mod.getPlayer().isOnGround());
            tp(mod, mod.getPlayer().getPos().subtract(0, 0.1, 0));
        }
        return space;
        /*final BlockPos tpGoal = new BlockPos(target);
        final BlockPos headGoal = tpGoal.up();
        final BlockState tpState = mod.getWorld().getBlockState(tpGoal);
        if (!tpState.getBlock().equals(Blocks.AIR)) {
            return false;
        }
        final BlockState headState = mod.getWorld().getBlockState(headGoal);
        if (!headState.getBlock().equals(Blocks.AIR)) {
            return false;
        }
        return true;*/
    }
    public static boolean canTpThere(final AltoClef mod, final Vec3d tpGoal) {
        if (explodingCreeperAt(mod, tpGoal)) {
            System.out.println("false cuz creeper");
            return false;
        }
        if (!isSpaceEmpty(mod, tpGoal, false)) {
            return false;
        }
        return true;
    }
    private boolean canTpThere(final Vec3d tpGoal, final AltoClef mod) {
        /*if (!canTpThere(mod, tpGoal)) {
            return false;
        }*/
        for (final Entity arrow : used) {
            if (targetInSight(new Vec3d(tpGoal.getX(), tpGoal.getY(), tpGoal.getZ()), arrow)) {
                return false;
            }
        }
        return canTpThere(mod, tpGoal);
    }
    private boolean canTpThere(final BlockPos tpGoal, final AltoClef mod) {
        return canTpThere(new Vec3d(tpGoal.getX() + 0.5, tpGoal.getY(), tpGoal.getZ() + 0.5), mod);
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
                    MovementCounter.tpMovements++;
                }
            }
        }

        return bl;
    }

    public static boolean chorusTp(final AltoClef mod, final boolean struggling) {
        return chorusTp(mod, struggling, 16);
    }

    public static boolean chorusTp(final AltoClef mod, final boolean struggling, int attempts) {
        final ClientPlayerEntity user = mod.getPlayer();
        final World world = mod.getWorld();
        for(int i = 0; i < attempts; ++i) {
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
        if (!struggling) {
            return false;
        }
        for(int i = 0; i < 16; ++i) {
            final double angle = user.getRandom().nextFloat() * 360;
            final double r = DefenseConstants.TP_RADIUS; //1.6;// + user.getRandom().nextInt(3);
            final double g = user.getX() + Math.cos(angle) * r;
            final double h = user.getY() + user.getRandom().nextInt(5);
            final double j = user.getZ() + Math.sin(angle) * r;
            final Vec3d tpGoal = new Vec3d(g, h, j);
            if (canTpThere(mod, tpGoal)) {
                tp(mod, tpGoal);
                return true;
            }
        }
        return false;
    }

    public static final void tp(final AltoClef mod, final Vec3d tpGoal) {
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        mod.getPlayer().setVelocity(0d, 0d, 0d);
        cancelFall(mod);
        mod.getPlayer().setPos(tpGoal.getX(), tpGoal.getY(), tpGoal.getZ());
        MovementCounter.tpMovements++;
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

