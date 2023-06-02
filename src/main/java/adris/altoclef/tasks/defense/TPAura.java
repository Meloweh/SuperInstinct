package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import welomehandmeloweh.superinstinct.*;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.util.MovementCounter;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class TPAura {
    private boolean attacking = false;
    private MobHatV2 mobHat = new MobHatV2();
    private List<Entity> used = new LinkedList<>();
    //private Optional<SkeletonEntity> recentSource = Optional.empty();
    private Optional<BlockPos> prev = Optional.empty();
    private final static Random rand = new Random();
    private BaitTrapV2 baitTrap;

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
        for (Entity nearbyHostile : nearbyHostiles) {
            if (mod.getPlayer().getBlockPos().equals(nearbyHostile.getBlockPos())) {
                Queen.nextJump(mod);
                return true;
            }
        }
        used.removeIf(e -> e == null || e.isRegionUnloaded() || e.horizontalCollision || e.verticalCollision || e.isOnGround() || e.distanceTo(mod.getPlayer()) > 70);
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class).stream()
                .filter(e -> (int)Math.ceil(distanceTo(e.getPos(), mod.getPlayer().getPos())) < DefenseConstants.TP_RADIUS && !e.horizontalCollision && !e.verticalCollision && !used.contains(e))
                .collect(Collectors.toList());
        final List<WitherSkullEntity> skulls = mod.getEntityTracker().getTrackedEntities(WitherSkullEntity.class)
                .stream().filter(e -> (int)Math.ceil(distanceTo(e.getPos(), mod.getPlayer().getPos())) < DefenseConstants.WHITHER_SKULL_DISTANCE).collect(Collectors.toList());
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
        if (baitTrap == null) {
            baitTrap = new BaitTrapV2(mod);
        }
        if (mod.getPlayer().isDead()) {
            baitTrap.reset(mod);
        }
        boolean tryAttack = true;
        if (tryAttack && skulls.size() > 0) {
            /*if (chorusTp(mod, false, 64)) {
                used.addAll(skulls);
                tryAttack = false;
            }*/
            baitTrap.reset(mod);
            if (Queen.attemptJump(mod, true)) {
                used.addAll(skulls);
                tryAttack = false;
            }
        }
        if (tryAttack && arrows.size() > 0) {
            //System.out.println("arrows.size() > 0");
            final ArrowEntity arrow = arrows.get(0);
            final Vec3d vecProj = arrow.getPos();
            final Vec3d velProj = arrow.getVelocity();
            final Vec3d velNormal = velProj.normalize();
            final Vec3d oppositeDir = velNormal.multiply(-1.5);
            final Vec3d rawTpGoal = hCenterOf(vecProj.add(oppositeDir));
            final double fixedY = nearbyHostiles.size() < 1 ? rawTpGoal.getY() : Math.max(rawTpGoal.getY(), nearbyHostiles.get(0).getY());
            final Vec3d tweakedTpGoal = new Vec3d(rawTpGoal.getX(), fixedY, rawTpGoal.getZ());//rawTpGoal.add(0, i, 0);
            if (canTpThere(tweakedTpGoal, mod)) {
                used.add(arrow);
                baitTrap.reset(mod);
                tp(mod, rawTpGoal);
                tryAttack = false;
            }
            if (tryAttack) {
                System.out.println("cannot dodge");
                //chorusTp(mod, true);
                baitTrap.reset(mod);
                Queen.nextJump(mod);
            } else {
                System.out.println("can dodge");
            }
            //final BlockPos tpGoal = new BlockPos(rawTpGoal);
            /*if (canTpThere(rawTpGoal, mod)) {
                used.add(arrow);
                //final double max = nearbyHostiles.size() > 0 ? Math.max(rawTpGoal.getY(), nearbyHostiles.get(0).getY()) : rawTpGoal.getY();
                //tp(mod, hCenterOf(rawTpGoal).add(0, max-rawTpGoal.getY(), 0));
                tp(mod, rawTpGoal);
                tryAttack = false;
                System.out.println("can dodge");
            } else {
                System.out.println("cannot dodge"); // TODO (done): ok but then fight if possible
            }*/
        }
        //String debug = "nothing";
        List<String> debug = new LinkedList<>();
        if (tryAttack && nearbyHostiles.size() > 0) {
            MobDefenseChain.safeToEat = false;
            //nearbyHostiles.sort((a, b) -> (int) ((a.distanceTo(mod.getPlayer()) - b.distanceTo(mod.getPlayer()))*1000));
            //nearbyHostiles.sort((a, b) -> Boolean.compare((a instanceof CreeperEntity), b instanceof CreeperEntity));

            final Iterator<Entity> entityIt = nearbyHostiles.iterator();
            do {
                final Entity entity = entityIt.next();
                final Vec3d eye = entity.getEyePos();
                //final BlockPos eyeBlock = new BlockPos(eye);
                //final BlockPos tpGoal = eyeBlock.up();
                final Vec3d tpGoal = hCenterOf(eye).add(0, 1, 0);
                //final Vec3d aboveTpGoal = tpGoal.add(0, 1, 0);

                if (entity instanceof CreeperEntity && entity.distanceTo(mod.getPlayer()) <= DefenseConstants.CREEPER_RADIUS) {
                    final CreeperEntity creeper = (CreeperEntity) entity;
                    if (isCreeperCritical(creeper)) {
                        //chorusTp(mod, true);
                        if (CombatHelper.isHoldingShield()) {
                            CombatHelper.startShielding(mod);
                        }
                        debug.add("reset bait and tp to escape creeper");
                        baitTrap.reset(mod);
                        if (!Queen.attemptJump(mod, true) && !Queen.attemptJump(mod, false)) {
                            tp(mod, mod.getPlayer().getPos().add(0, 1, 0));
                        }
                        return true;
                    } else if (mod.getPlayer().distanceTo(creeper) <= DefenseConstants.PUNCH_RADIUS) {
                        debug.add("punching nearby due to creeper");
                        CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
                        if (CombatHelper.hasShield(mod)) {
                            CombatHelper.startShielding(mod);
                            LookHelper.lookAt(mod, creeper.getEyePos());
                        }
                    }
                    return true;
                }
                /*
                final boolean canTpUpUp = canTpThere(aboveTpGoal, mod);
                if (canTpUpUp && mobHat.canAttemptHat(mod)) {
                    tp(mod, aboveTpGoal);
                }*/
                /*
                attacking = mobHat.attemptHat(mod);
                final boolean canTpUp = canTpThere(tpGoal, mod);
                //System.out.println(canTpUp);
                if (!attacking && canTpUp && (!(entity instanceof SkeletonEntity) || nearbyHostiles.size() < 2 && mod.getPlayer().getHealth() > 10) && (!(entity instanceof CreeperEntity) || !isCreeperCritical((CreeperEntity) entity))) {
                    final Vec3d newPos3d = new Vec3d(tpGoal.getX(), entity.getPos().getY() + entity.getBoundingBox().getYLength() + 1, tpGoal.getZ());
                    tp(mod, newPos3d);
                    float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                    // Equip weapon
                    if (!(entity instanceof SkeletonEntity)) {
                        KillEntityTask.equipWeapon(mod);
                    }
                    if (hitProg >= 1) {
                        LookHelper.lookAt(mod, entity.getEyePos());
                        mod.getControllerExtras().attack(entity);
                    }
                    attacking = true;
                }*/
            } while (entityIt.hasNext() && !attacking);
        }
        if (CombatHelper.isHoldingShield()) {
            CombatHelper.stopShielding(mod);
        }
        baitTrap.tickCooldown();
        final List<Entity> closeHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.NEARBY_DISTANCE+2 || e instanceof SkeletonEntity && mod.getPlayer().distanceTo(e) <= DefenseConstants.HOSTILE_DISTANCE/*LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.NEARBY_DISTANCE)*/)
                .collect(Collectors.toList());
        if (closeHostiles.size() > 0) {
            final List<Entity> spiders = mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()).stream().filter(e -> e instanceof SpiderEntity).collect(Collectors.toList());
            CombatHelper.punchNearestHostile(mod, false, spiders);
            debug.add("punchNearestHostile");
            if (spiders.stream().filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.PUNCH_RADIUS).count() > 1 && mod.getPlayer().getHealth() < 10) {
                baitTrap.reset(mod);
                Queen.nextJump(mod);
                debug.add("escape from spider");
            }
            if (!baitTrap.isActive()) baitTrap.init(mod, closeHostiles);
            if (!baitTrap.isActive()) {
                CombatHelper.punchNearestHostile(mod, !mobHat.attemptHat(mod), mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
            }
        }
        if (baitTrap.isActive()) {
            debug.add("trapping");
            baitTrap.trapping(mod);
        } else {
            if (closeHostiles.stream().filter(e -> mod.getPlayer().distanceTo(e) < 3).count() > 0) {
                //CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
                Queen.nextJump(mod);
                debug.add("tping because hostiles too close");
            } /*else if (!mobHat.attemptHat(mod)) {
                if (CombatHelper.hasShield(mod)) {
                    CombatHelper.startShielding(mod);
                }
                CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
            }*/
        }
        CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
        String result = "";
        for (String s : debug) {
            result+=" => "+s;
        }
        if (debug.size() > 0) System.out.println(result);
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

    public static final void tp(final AltoClef mod, final Vec3d tpGoal, final boolean shouldCancelFall) {
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        if (shouldCancelFall) {
            mod.getPlayer().setVelocity(0d, 0d, 0d);
            cancelFall(mod);
        }
        mod.getPlayer().setPos(tpGoal.getX(), tpGoal.getY(), tpGoal.getZ());
        MovementCounter.tpMovements++;
    }

    public static final void tp(final AltoClef mod, final Vec3d tpGoal) {
        tp(mod, tpGoal, true);
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
