package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.control.KillAura;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import adris.altoclef.tasks.defense.*;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasks.speedrun.DragonBreathTracker;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.MovementCounter;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.time.TimerGame;
import baritone.Baritone;
import baritone.api.utils.input.Input;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

public class MobDefenseChain extends SingleTaskChain {
    private static final double DANGER_KEEP_DISTANCE = 30;
    private static final double CREEPER_KEEP_DISTANCE = 10;
    private static final double ARROW_KEEP_DISTANCE_HORIZONTAL = 2;//4;
    private static final double ARROW_KEEP_DISTANCE_VERTICAL = 10;//15;
    private static final double SAFE_KEEP_DISTANCE = 8;
    private final DragonBreathTracker _dragonBreathTracker = new DragonBreathTracker();
    private final KillAura _killAura = new KillAura();
    private final HashMap<Entity, TimerGame> _closeAnnoyingEntities = new HashMap<>();
    private Entity _targetEntity;
    private boolean _shielding = false;
    private boolean _doingFunkyStuff = false;
    private boolean _wasPuttingOutFire = false;
    private CustomBaritoneGoalTask _runAwayTask;
    private float _cachedLastPriority;
    private BasicDefenseManager basicDefenseManager = new BasicDefenseManager();
    private boolean attacking = false;
    public static boolean safeToEat = true;
    private boolean shelterMode = false;
    private IdleTask idleTask = new IdleTask();
    private Optional<GetToXZTask> optXZTask = Optional.empty();
    private Optional<KillEntityTask> killEntity = Optional.empty();
    private Optional<RunAwayFromHostilesTask> runFromHostiles = Optional.empty();
    private BaitTrapV2 baitTrap;

    public TPAura tpAura = new TPAura();
    //private CombatHandler combatHandler = new CombatHandler();
    private MobHatV2 mobHat = new MobHatV2();

    public MobDefenseChain(TaskRunner runner) {
        super(runner);
    }

    public static double getCreeperSafety(Vec3d pos, CreeperEntity creeper) {
        double distance = creeper.squaredDistanceTo(pos);
        float fuse = creeper.getClientFuseTime(1);

        // Not fusing.
        if (fuse <= 0.001f) return distance;
        return distance * 0.2; // less is WORSE
    }

    public static boolean safeToEat() {
        //System.out.println("safeToEat: " + safeToEat);
        return safeToEat;
    }

    @Override
    public float getPriority(AltoClef mod) {
        _cachedLastPriority = getPriorityInner(mod);
        return _cachedLastPriority;
    }

    boolean escapeDragonBreath(AltoClef mod) {
        _dragonBreathTracker.updateBreath(mod);
        for (BlockPos playerIn : WorldHelper.getBlocksTouchingPlayer(mod)) {
            if (_dragonBreathTracker.isTouchingDragonBreath(playerIn)) {
                return true;
            }
        }
        return false;
    }

    public float getPriorityInner(AltoClef mod) {
        if (!AltoClef.inGame()) {
            return Float.NEGATIVE_INFINITY;
        }

        if (!mod.getModSettings().isMobDefense()) {
            return Float.NEGATIVE_INFINITY;
        }

        /*final boolean noTaskFoundButWhy = mod.getTaskRunner().getCurrentTaskChain() != null
                                       && mod.getTaskRunner().getCurrentTaskChain().getTasks() != null
                                       && mod.getTaskRunner().getCurrentTaskChain().getTasks().size() < 1;*/

        // Apply avoidance if we're vulnerable, avoiding mobs if at all possible.
        // mod.getClientBaritoneSettings().avoidance.value = isVulnurable(mod);
        // Doing you a favor by disabling avoidance


        // Pause if we're not loaded into a world.
        if (!AltoClef.inGame()) return Float.NEGATIVE_INFINITY;
        if (MovementCounter.fillMovements > 3 || MovementCounter.tpMovements > 3) {
            //Debug.logMessage("fillMovements: " + MovementCounter.fillMovements);
            //Debug.logMessage("tpMovements: " + MovementCounter.tpMovements);
            //System.out.println("fillMovements: " + MovementCounter.fillMovements);
            //System.out.println("tpMovements: " + MovementCounter.tpMovements);
            MovementCounter.fillMovements = 0;
            MovementCounter.tpMovements = 0;

        }

        if (mod.getPlayer().isSubmergedInWater() && mod.getPlayer().getAir() < 1) {
            Queen.nextJump(mod);
        }

        if (mod.getPlayer().isDead()) mod.getWorld().disconnect();
        //System.out.println(mod.getWorld().getRegistryKey().getValue().getPath());
        //if (mod.getTaskRunner().getCurrentTaskChain() != null && mod.getTaskRunner().getCurrentTaskChain().getTasks() != null)
        //    System.out.println(mod.getTaskRunner().getCurrentTaskChain().getTasks().size());
        _doingFunkyStuff = false;
        if (mod.getPlayer().getHealth() < 7 && SecurityShelterTask.canAttemptShelter(mod)) {
            if (mod.getFoodChain().hasFood()) {
                /*final List<Entity> closeHostiles = mod.getEntityTracker().getHostiles().stream()
                        .filter(e -> e.distanceTo(mod.getPlayer()) <= 15
                                //&& !(e instanceof SkeletonEntity)
                                //&& !(e instanceof CreeperEntity)
                                && !(e instanceof ProjectileEntity))
                        .collect(Collectors.toList());*/
                final List<Entity> veryCloseHostiles = mod.getEntityTracker().getCloseEntities().stream()
                        .filter(e -> e instanceof HostileEntity && e.distanceTo(mod.getPlayer()) < 4
                                //&& !(e instanceof SkeletonEntity)
                                //&& !(e instanceof CreeperEntity)
                                && !(e instanceof ProjectileEntity))
                        .collect(Collectors.toList());

                final boolean isFilled = SecurityShelterTask.isFilled(mod);
                safeToEat = isFilled;
                if (!isFilled) {
                    if (veryCloseHostiles.size() > 0) {
                        /*
                        final BlockPos overPlayer = new BlockPos(mod.getPlayer().getEyePos()).up().up();
                        final Vec3d tpGoal = new Vec3d(overPlayer.getX() + 0.5, overPlayer.getY(), overPlayer.getZ() + 0.5);
                        if (mod.getPlayer().isOnGround() && TPAura.canTpThere(mod, tpGoal)) {
                            TPAura.tp(mod, tpGoal);
                        } else {
                            // hit 'em
                        }*/
                        Queen.nextJump(mod);
                    } else {
                        SecurityShelterTask.attemptShelter(mod);
                    }

                } else {

                }
            }
        } else {
            final List<PhantomEntity> stupidPhantoms = mod.getEntityTracker().getTrackedEntities(PhantomEntity.class).stream()
                    .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.PUNCH_RADIUS).collect(Collectors.toList());
            if (stupidPhantoms.size() < 1) {
                safeToEat = true;
            } else {
                final PhantomEntity entity = stupidPhantoms.get(0);
                KillEntityTask.equipWeapon(mod);
                float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                if (hitProg >= 1) {
                    LookHelper.lookAt(mod, entity.getEyePos());
                    mod.getControllerExtras().attack(entity);
                }
            }

        }


        // Run away from creepers
        /*Optional<CreeperEntity> blowingUp = getClosestFusingCreeper(mod);
        if (blowingUp.isPresent()) {
            //System.out.println("creeper: " + blowingUp.get().getClientFuseTime(1));
            safeToEat = false;//mod.getClientBaritone().getBuilderProcess()
            if ((mod.getItemStorage().hasItem(Items.SHIELD) ||
                    mod.getItemStorage().hasItemInOffhand(Items.SHIELD)) &&
                    !mod.getEntityTracker().entityFound(PotionEntity.class) && _runAwayTask == null &&
                    mod.getClientBaritone().getPathingBehavior().isSafeToCancel()) {
                _doingFunkyStuff = true;
                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                LookHelper.lookAt(mod, blowingUp.get().getEyePos());
                ItemStack shieldSlot = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
                if (shieldSlot.getItem() != Items.SHIELD) {
                    mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
                } else {
                    startShielding(mod);
                }
            } else {
                _doingFunkyStuff = true;
                //Debug.logMessage("RUNNING AWAY!");
                _runAwayTask = new RunAwayFromCreepersTask(CREEPER_KEEP_DISTANCE);
                setTask(_runAwayTask);
                return 100 + blowingUp.get().getClientFuseTime(1) * 50;
            }
        } else {
            if (!isProjectileClose(mod)) {
                stopShielding(mod);
                safeToEat = true;
            }
        }*/
        /*
        basicDefenseManager.onTick(mod);
        if (!combatHandler.attemptAura(mod)) {
            if (!mobHat.attemptHat(mod)) {
                final List<Entity> hostiles = mod.getEntityTracker().getHostiles();
                if (hostiles.size() > 0) {
                    hostiles.sort((a, b) -> (int) ((a.distanceTo(mod.getPlayer()) - b.distanceTo(mod.getPlayer()))*1000));
                    final Entity nearest = hostiles.get(0);
                    if (nearest.distanceTo(mod.getPlayer()) < 3) {
                        if (killEntity.isEmpty() || !killEntity.get().isActive()) {
                            killEntity = Optional.of(new KillEntityTask(nearest));
                        }
                        setTask(killEntity.get());
                    } else {
                        if (runFromHostiles.isEmpty() || !runFromHostiles.get().isActive()) {
                            runFromHostiles = Optional.of(new RunAwayFromHostilesTask(7, true));
                        }
                        setTask(runFromHostiles.get());
                    }
                } else {
                    safeToEat = true;
                }
            }
        }*/
        /*if (tpAura.isAttacking()) {
            return 70;
        }*/
        /*
        if (baitTrap == null) {
            baitTrap = new BaitTrap(mod);
        }
        final List<Entity> closeHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.HOSTILE_DISTANCE))
                .collect(Collectors.toList());
        //final List<Entity> withoutSeeing = mod.getEntityTracker().getCloseEntities().stream().filter(e -> e instanceof HostileEntity && mod.getPlayer().distanceTo(e) < DefenseConstants.HOSTILE_DISTANCE).collect(Collectors.toList());
        if (closeHostiles.size() > 0) {
            System.out.println("fixate");
            if (!baitTrap.isActive()) {
                baitTrap.fixateTrap(mod, closeHostiles);
            }
        }
        if (baitTrap.isActive()) {
            System.out.println("trapping");
            baitTrap.trapping(mod, closeHostiles);
        } else {
            System.out.println("reset");
            baitTrap.reset(mod);
        }*/
        /*
        if (baitTrap == null) {
            baitTrap = new BaitTrapV2(mod);
        }
        final List<Entity> closeHostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.NEARBY_DISTANCE+2 || e instanceof SkeletonEntity && mod.getPlayer().distanceTo(e) <= DefenseConstants.HOSTILE_DISTANCE)
                .collect(Collectors.toList());
        if (closeHostiles.size() > 0) {
            final List<Entity> spiders = closeHostiles.stream().filter(e -> e instanceof SpiderEntity).collect(Collectors.toList());
            punchNearestHostile(mod, false, spiders);
            if (spiders.stream().filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.PUNCH_RADIUS).count() > 1 && mod.getPlayer().getHealth() < 10) {
                baitTrap.reset(mod);
                Queen.nextJump(mod);
            }
            if (!baitTrap.isActive()) baitTrap.init(mod, closeHostiles);
        }
        if (baitTrap.isActive()) {
            baitTrap.trapping(mod);
        } else {
            if (mod.getPlayer().getHealth() < 7) {
                if (SecurityShelterTask.canAttemptShelter(mod)) {
                    punchNearestHostile(mod, !SecurityShelterTask.isFilled(mod), closeHostiles);
                    SecurityShelterTask.attemptShelter(mod);
                }
            } else if (closeHostiles.stream().filter(e -> mod.getPlayer().distanceTo(e) < 3).count() > 0) {
                punchNearestHostile(mod, false, closeHostiles);
                Queen.nextJump(mod);
            }
        }*/
        if (!tpAura.attemptAura(mod)) {
            Debug.logMessage("struggling...");
            Queen.nextJump(mod);
        }
        basicDefenseManager.onTick(mod);

        return 0;
    }

    private BlockPos isInsideFireAndOnFire(AltoClef mod) {
        boolean onFire = mod.getPlayer().isOnFire();
        if (!onFire) return null;
        BlockPos p = mod.getPlayer().getBlockPos();
        BlockPos[] toCheck = new BlockPos[]{
                p,
                p.add(1, 0, 0),
                p.add(1, 0, -1),
                p.add(0, 0, -1),
                p.add(-1, 0, -1),
                p.add(-1, 0, 0),
                p.add(-1, 0, 1),
                p.add(0, 0, 1),
                p.add(1, 0, 1)
        };
        for (BlockPos check : toCheck) {
            Block b = mod.getWorld().getBlockState(check).getBlock();
            if (b instanceof AbstractFireBlock) {
                return check;
            }
        }
        return null;
    }

    private void putOutFire(AltoClef mod, BlockPos pos) {
        LookHelper.lookAt(mod, pos);
        Baritone b = mod.getClientBaritone();
        if (LookHelper.isLookingAt(mod, pos)) {
            b.getPathingBehavior().requestPause();
            b.getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        }
    }

    private void doForceField(AltoClef mod) {

        _killAura.tickStart();

        // Hit all hostiles close to us.
        List<Entity> entities = mod.getEntityTracker().getCloseEntities();
        try {
            for (Entity entity : entities) {
                boolean shouldForce = false;
                if (mod.getBehaviour().shouldExcludeFromForcefield(entity)) continue;
                if (entity instanceof MobEntity) {
                    if (EntityHelper.isGenerallyHostileToPlayer(mod, entity)) {
                        if (LookHelper.seesPlayer(entity, mod.getPlayer(), 10)) {
                            shouldForce = true;
                        }
                    }
                } else if (entity instanceof FireballEntity) {
                    // Ghast ball
                    shouldForce = true;
                } else if (entity instanceof PlayerEntity player && mod.getBehaviour().shouldForceFieldPlayers()) {
                    if (!player.equals(mod.getPlayer())) {
                        String name = player.getName().getString();
                        if (!mod.getButler().isUserAuthorized(name)) {
                            shouldForce = true;
                        }
                    }
                }
                if (shouldForce) {
                    applyForceField(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        _killAura.tickEnd(mod);
    }

    private void applyForceField(Entity entity) {
        _killAura.applyAura(entity);
    }

    public static Optional<CreeperEntity> getClosestFusingCreeper(AltoClef mod) {
        double worstSafety = Float.POSITIVE_INFINITY;
        Optional target = Optional.empty();
        try {
            List<CreeperEntity> creepers = mod.getEntityTracker().getTrackedEntities(CreeperEntity.class);
            for (CreeperEntity creeper : creepers) {

                if (creeper == null) continue;
                if (creeper.getClientFuseTime(1) < 0.001) continue;

                // We want to pick the closest creeper, but FIRST pick creepers about to blow
                // At max fuse, the cost goes to basically zero.
                double safety = getCreeperSafety(mod.getPlayer().getPos(), creeper);
                if (safety < worstSafety) {
                    target = Optional.of(creeper);
                }
            }
        } catch (ConcurrentModificationException | ArrayIndexOutOfBoundsException | NullPointerException e) {
            // IDK why but these exceptions happen sometimes. It's extremely bizarre and I have no idea why.
            Debug.logWarning("Weird Exception caught and ignored while scanning for creepers: " + e.getMessage());
        }
        return target;
    }

    private boolean isProjectileClose(AltoClef mod) {
        List<CachedProjectile> projectiles = mod.getEntityTracker().getProjectiles();
        try {
            for (CachedProjectile projectile : projectiles) {
                if (projectile.position.squaredDistanceTo(mod.getPlayer().getPos()) < 150) {
                    boolean isGhastBall = projectile.projectileType == FireballEntity.class;
                    if (isGhastBall) {
                        Optional<Entity> ghastBall = mod.getEntityTracker().getClosestEntity(FireballEntity.class);
                        Optional<Entity> ghast = mod.getEntityTracker().getClosestEntity(GhastEntity.class);
                        if (ghastBall.isPresent() && ghast.isPresent() && _runAwayTask == null) {
                            mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                            LookHelper.lookAt(mod, ghast.get().getEyePos());
                        }
                        return false;
                        // Ignore ghast balls
                    }
                    if (projectile.projectileType == DragonFireballEntity.class) {
                        // Ignore dragon fireballs
                        return false;
                    }

                    Vec3d expectedHit = ProjectileHelper.calculateArrowClosestApproach(projectile, mod.getPlayer());

                    Vec3d delta = mod.getPlayer().getPos().subtract(expectedHit);

                    //Debug.logMessage("EXPECTED HIT OFFSET: " + delta + " ( " + projectile.gravity + ")");

                    double horizontalDistanceSq = delta.x * delta.x + delta.z * delta.z;
                    double verticalDistance = Math.abs(delta.y);
                    if (horizontalDistanceSq < ARROW_KEEP_DISTANCE_HORIZONTAL * ARROW_KEEP_DISTANCE_HORIZONTAL && verticalDistance < ARROW_KEEP_DISTANCE_VERTICAL) {
                        if (_runAwayTask == null) {
                            mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                            LookHelper.lookAt(mod, projectile.position);
                        }
                        return true;
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }
        return false;
    }

    private Optional<Entity> getUniversallyDangerousMob(AltoClef mod) {
        // Wither skeletons are dangerous because of the wither effect. Oof kinda obvious.
        // If we merely force field them, we will run into them and get the wither effect which will kill us.
        Optional<Entity> warden = mod.getEntityTracker().getClosestEntity(WardenEntity.class);
        if (warden.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (warden.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, warden.get())) {
                return warden;
            }
        }
        Optional<Entity> wither = mod.getEntityTracker().getClosestEntity(WitherEntity.class);
        if (wither.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (wither.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, wither.get())) {
                return wither;
            }
        }
        Optional<Entity> witherSkeleton = mod.getEntityTracker().getClosestEntity(WitherSkeletonEntity.class);
        if (witherSkeleton.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (witherSkeleton.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, witherSkeleton.get())) {
                return witherSkeleton;
            }
        }
        // Hoglins are dangerous because we can't push them with the force field.
        // If we merely force field them and stand still our health will slowly be chipped away until we die
        Optional<Entity> hoglin = mod.getEntityTracker().getClosestEntity(HoglinEntity.class);
        if (hoglin.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (hoglin.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, hoglin.get())) {
                return hoglin;
            }
        }
        Optional<Entity> zoglin = mod.getEntityTracker().getClosestEntity(ZoglinEntity.class);
        if (zoglin.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (zoglin.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, zoglin.get())) {
                return zoglin;
            }
        }
        Optional<Entity> piglinBrute = mod.getEntityTracker().getClosestEntity(PiglinBruteEntity.class);
        if (piglinBrute.isPresent()) {
            double range = SAFE_KEEP_DISTANCE - 2;
            if (piglinBrute.get().squaredDistanceTo(mod.getPlayer()) < range * range && EntityHelper.isAngryAtPlayer(mod, piglinBrute.get())) {
                return piglinBrute;
            }
        }
        return Optional.empty();
    }

    private boolean isInDanger(AltoClef mod) {
        Optional<Entity> witch = mod.getEntityTracker().getClosestEntity(WitchEntity.class);
        boolean hasFood = mod.getFoodChain().hasFood();
        float health = mod.getPlayer().getHealth();
        if (health <= 10 && hasFood && witch.isEmpty()) {
            return true;
        }
        if (mod.getPlayer().hasStatusEffect(StatusEffects.WITHER) ||
                (mod.getPlayer().hasStatusEffect(StatusEffects.POISON) && witch.isEmpty())) {
            return true;
        }
        if (isVulnurable(mod)) {
            // If hostile mobs are nearby...
            try {
                ClientPlayerEntity player = mod.getPlayer();
                List<Entity> hostiles = mod.getEntityTracker().getHostiles();
                for (Entity entity : hostiles) {
                    if (entity.isInRange(player, SAFE_KEEP_DISTANCE) && !mod.getBehaviour().shouldExcludeFromForcefield(entity) && EntityHelper.isAngryAtPlayer(mod, entity)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Debug.logWarning("Weird multithread exception. Will fix later.");
            }
        }
        return false;
    }

    private boolean isVulnurable(AltoClef mod) {
        int armor = mod.getPlayer().getArmor();
        float health = mod.getPlayer().getHealth();
        if (armor <= 15 && health < 3) return true;
        if (armor < 10 && health < 10) return true;
        return armor < 5 && health < 18;
    }

    public void setTargetEntity(Entity entity) {
        _targetEntity = entity;
    }

    public void resetTargetEntity() {
        _targetEntity = null;
    }

    public void setForceFieldRange(double range) {
        _killAura.setRange(range);
    }

    public void resetForceField() {
        _killAura.setRange(Double.POSITIVE_INFINITY);
    }

    public boolean isDoingAcrobatics() {
        return _doingFunkyStuff;
    }

    public boolean isPuttingOutFire() {
        return _wasPuttingOutFire;
    }

    @Override
    public boolean isActive() {
        // We're always checking for mobs
        return true;
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        // Task is done, so I guess we move on?
    }

    @Override
    public String getName() {
        return "Mob Defense";
    }
}