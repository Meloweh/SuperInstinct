package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import welomehandmeloweh.superinstinct.CombatHelper;
import welomehandmeloweh.superinstinct.MeteorClientPlace;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BaitTrapV2 {
    private BlockPos pinnedPos;
    private int timer;
    private static final int WAITING_LIMIT = 20*7;
    private boolean active;
    private boolean onlySkeletons;
    private boolean doneChamber, doneTpInChamber, doneHatch, doneWaiting, doneClosing;
    private int timerClosingHatch;
    private Direction dir;
    private final static int INIT_COOLDOWN = 7;
    private int cooldownInit;
    public void reset(final AltoClef mod) {
        active = false;
        pinnedPos = null;
        timer = 0;
        onlySkeletons = false;
        CombatHelper.stopShielding(mod);
        doneChamber = false;
        doneTpInChamber = false;
        doneHatch = false;
        doneClosing = false;
        doneWaiting = false;
        dir = null;
        timerClosingHatch = 0;
    }
    public BaitTrapV2(final AltoClef mod) {
        reset(mod);
    }

    private boolean ensureFloor(final AltoClef mod) {
        System.out.println("ensureFloor");
        if (mod.getWorld().getBlockState(pinnedPos.offset(Direction.DOWN, 4)).isAir()) {
            if (mod.getItemStorage().getBlockCount() < 1) {
                return false;
            }
            BasicDefenseManager.fill(mod, pinnedPos.offset(Direction.DOWN, 4));
        }
        return true;
    }
    private boolean mineChamber(final AltoClef mod) {
        final List<BlockPos> toBreak = new LinkedList<>();
        toBreak.add(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 2));
        toBreak.add(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 3));
        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod, toBreak);
        return !mining;
    }
    private void tpChamber(final AltoClef mod) {
        System.out.println("tpChamber");
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos.offset(Direction.DOWN, 3)));
    }
    private boolean mineHatch(final AltoClef mod) {
        final List<BlockPos> toBreak = new LinkedList<>();
        toBreak.add(mod.getPlayer().getBlockPos().up().offset(dir, 1));
        toBreak.add(mod.getPlayer().getBlockPos().up().offset(dir, 1).up());
        toBreak.add(mod.getPlayer().getBlockPos().up().offset(dir, 1).up().up());
        toBreak.add(mod.getPlayer().getBlockPos().up().offset(dir, 1).up().up().up());

        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod, toBreak);
        return !mining;
    }
    private boolean murderBabies(final AltoClef mod) {
        final List<ZombieEntity> babies = mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()).stream().filter(e -> e instanceof ZombieEntity && ((ZombieEntity) e).isBaby()).map(e -> (ZombieEntity)e).collect(Collectors.toList());
        if (babies.isEmpty()) return false;
        final ZombieEntity entity = babies.get(0);
        KillEntityTask.equipWeapon(mod);
        float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
        if (hitProg >= 1) {
            LookHelper.lookAt(mod, entity.getEyePos());
            mod.getControllerExtras().attack(entity);
        }
        return true;
    }
    public void tickCooldown() {
        if (cooldownInit > 0) {
            cooldownInit--;
        }
    }
    public void init(final AltoClef mod, final List<Entity> entities) {
        System.out.println("iunit");
        if (cooldownInit > 0) {
            System.out.println("cooldown not 0");
            return;
        }
        reset(mod);
        pinnedPos = mod.getPlayer().getBlockPos();
        dir = LookHelper.randomDirection2D();
        active = canTrapHere(mod, entities);
        if (!active) {
            System.out.println(" => init !canTrapHere");
            reset(mod);
            Queen.nextJump(mod);
        }
    }
    public boolean trapping(final AltoClef mod) {
        //System.out.println("trapping");
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        //if (!doneChamber && !canTrapHere(mod)) return false;
        String debug = "";
        if (mod.getWorld().getBlockState(pinnedPos.down()).isAir()) {
            BasicDefenseManager.fill(mod, pinnedPos.down());
            System.out.println("filling pinnedPos");

        }
        if (!ensureFloor(mod)) {
            if (debug != "") System.out.println(debug);
            return false;
        }
        MobDefenseChain.safeToEat = false;
        debug += " => doneChamber = " + doneChamber;
        if (!doneChamber) TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos));
        /*if (!doneChamber) {
            final List<Entity> sees = mod.getEntityTracker().getHostiles().stream().filter(e -> LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.HOSTILE_DISTANCE)).collect(Collectors.toList());
            if (!sees.isEmpty()) {
                reset(mod);
                Queen.nextJump(mod);
                System.out.println("STILL SEES PLAYER");
                return false;
            }
        }*/
        if (!doneChamber && mod.getPlayer().getHealth() < 6) {
            reset(mod);
            Queen.nextJump(mod);
        }
        if (!doneChamber) doneChamber = mineChamber(mod);
        if (!doneChamber) {
            System.out.println(debug);
            return false;
        }
        debug += " => doneHatch = " + doneHatch;

        if (!mod.getPlayer().getBlockPos().equals(pinnedPos.offset(Direction.DOWN, 3))) tpChamber(mod);
        if (!mod.getPlayer().getBlockPos().equals(pinnedPos.offset(Direction.DOWN, 3))) {
            System.out.println(debug);
            return false;
        }
        if (!doneHatch || !doneWaiting) {
            final List<Entity> sees = mod.getEntityTracker().getHostiles().stream().filter(e -> LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.HOSTILE_DISTANCE)).collect(Collectors.toList());
            if (!sees.isEmpty()) {
                reset(mod);
                Queen.nextJump(mod);
                System.out.println("STILL SEES PLAYER");
                System.out.println(debug);
                return false;
            }
        }
        MobDefenseChain.safeToEat = true;
        if (!doneHatch) doneHatch = mineHatch(mod);
        if (!doneHatch) {
            System.out.println(debug);
            return false;
        }
        debug += " => doneWaiting = " + doneWaiting;
        if (!doneWaiting) {
            doneWaiting = timer++ > WAITING_LIMIT;
            if (mod.getFoodChain().needsToEat()) {
                timer = 0;
            }
            if (mod.getEntityTracker().getHostiles().size() > 0 && mod.getPlayer().getHealth() < 7) {
                timer = WAITING_LIMIT*2;
            }
        }
        if (!doneWaiting) CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));//murderBabies(mod);
        //if (!doneWaiting && !murderBabies(mod)) CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getHostiles().stream().filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.PUNCH_RADIUS).collect(Collectors.toList()));//murderBabies(mod);
        if (!doneWaiting) {
            System.out.println(debug);
            return false;
        }
        debug += " => doneClosing = " + doneClosing;
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos));
        doneClosing = !mod.getWorld().getBlockState(pinnedPos.offset(dir, 1)).isAir() || timerClosingHatch++ >= 5;
        if (!doneClosing) BasicDefenseManager.fill(mod, pinnedPos.offset(dir, 1));
        if (!doneClosing) {
            System.out.println(debug);
            return false;
        }
        debug += " => finishing";

        cooldownInit = INIT_COOLDOWN;
        Queen.attemptJump(mod);
        CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getPunchableHostiles(mod.getPlayer()));
        active = false;
        System.out.println("active: " + active);
        if (debug != "") System.out.println(debug);
        return true;
    }
    public boolean isActive() {
        return this.active;
    }
    private boolean canTrapHere(final AltoClef mod, final List<Entity> entities) {
        if (mod.getPlayer().getHealth() < 7 && entities.stream().filter(e -> mod.getPlayer().distanceTo(e) < 3).count() > 0) return false;
        for (BlockPos blockPos = mod.getPlayer().getBlockPos().offset(Direction.DOWN, 2); !blockPos.equals(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 3)); blockPos = blockPos.down()) {
            final BlockState state = mod.getWorld().getBlockState(blockPos);
            if (!StorageHelper.miningRequirementMet(mod, MiningRequirement.getMinimumRequirementForBlock(state.getBlock())) || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        for (BlockPos blockPos = mod.getPlayer().getBlockPos().offset(Direction.DOWN, 2).offset(dir, 1); !blockPos.equals(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 2).offset(dir, 1).offset(Direction.UP, 3)); blockPos = blockPos.up()) {
            final BlockState state = mod.getWorld().getBlockState(blockPos);
            if (!StorageHelper.miningRequirementMet(mod, MiningRequirement.getMinimumRequirementForBlock(state.getBlock())) || !state.getFluidState().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}