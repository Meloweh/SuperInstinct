package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.chains.MobDefenseChain;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasks.ArrowMapTests.CombatHelper;
import adris.altoclef.tasks.ArrowMapTests.MeteorClientPlace;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
        final List<ZombieEntity> babies = mod.getEntityTracker().getHostiles().stream().filter(e -> e instanceof ZombieEntity && ((ZombieEntity) e).isBaby()).map(e -> (ZombieEntity)e).collect(Collectors.toList());
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
    public void init(final AltoClef mod, final List<Entity> entities) {
        System.out.println("iunit");

        reset(mod);
        pinnedPos = mod.getPlayer().getBlockPos();
        dir = LookHelper.randomDirection2D();
        active = canTrapHere(mod, entities);
        if (!active) {
            reset(mod);
            Queen.nextJump(mod);
        }
    }
    public boolean trapping(final AltoClef mod) {
        //System.out.println("trapping");
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        //if (!doneChamber && !canTrapHere(mod)) return false;
        if (mod.getWorld().getBlockState(pinnedPos.down()).isAir()) {
            BasicDefenseManager.fill(mod, pinnedPos.down());
        }
        if (!ensureFloor(mod)) return false;
        MobDefenseChain.safeToEat = false;
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
        if (!doneChamber) return false;
        if (!mod.getPlayer().getBlockPos().equals(pinnedPos.offset(Direction.DOWN, 3))) tpChamber(mod);
        if (!mod.getPlayer().getBlockPos().equals(pinnedPos.offset(Direction.DOWN, 3))) return false;
        if (!doneHatch || !doneWaiting) {
            final List<Entity> sees = mod.getEntityTracker().getHostiles().stream().filter(e -> LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.HOSTILE_DISTANCE)).collect(Collectors.toList());
            if (!sees.isEmpty()) {
                reset(mod);
                Queen.nextJump(mod);
                System.out.println("STILL SEES PLAYER");
                return false;
            }
        }
        MobDefenseChain.safeToEat = true;
        if (!doneHatch) doneHatch = mineHatch(mod);
        if (!doneHatch) return false;
        if (!doneWaiting) doneWaiting = timer++ > WAITING_LIMIT;
        if (!doneWaiting && !murderBabies(mod)) CombatHelper.punchNearestHostile(mod, false, mod.getEntityTracker().getHostiles().stream().filter(e -> mod.getPlayer().distanceTo(e) <= DefenseConstants.PUNCH_RADIUS).collect(Collectors.toList()));//murderBabies(mod);
        if (!doneWaiting) return false;
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos));
        doneClosing = !mod.getWorld().getBlockState(pinnedPos.offset(dir, 1)).isAir() || timerClosingHatch++ >= 5;
        if (!doneClosing) BasicDefenseManager.fill(mod, pinnedPos.offset(dir, 1));
        if (!doneClosing) return false;
        Queen.attemptJump(mod);
        active = false;
        System.out.println("active: " + active);
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