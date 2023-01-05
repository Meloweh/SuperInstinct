package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasks.ArrowMapTests.CombatHelper;
import adris.altoclef.tasks.ArrowMapTests.MeteorClientPlace;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.CheckedRandom;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BaitTrapV2 {
    private BlockPos pinnedPos;
    //private final List<BlockPos> toBreak;
    private int timer;
    private static final int WAITING_LIMIT = 20*7;
    private boolean active;
    private boolean onlySkeletons;
    private boolean doneFloor, doneChamber, doneTpInChamber, doneHatch, doneWaiting, doneClosing;
    //private BlockPos waitingSpot;
    public void reset(final AltoClef mod) {
        active = false;
        pinnedPos = null;
        timer = 0;
        onlySkeletons = false;
        //waitingSpot = null;
        CombatHelper.stopShielding(mod);
        doneChamber = false;
        doneTpInChamber = false;
        doneHatch = false;
        doneClosing = false;
        doneFloor = false;
        doneWaiting = false;
    }
    public BaitTrapV2(final AltoClef mod) {
        reset(mod);
    }

    private void pinPlace(final AltoClef mod) {
        //toBreak.add(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 2));
        //toBreak.add(mod.getPlayer().getBlockPos().offset(Direction.DOWN, 3));
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
        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod.getWorld(), toBreak);
        return !mining;
    }
    private void tpChamber(final AltoClef mod) {
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos.offset(Direction.DOWN, 3)));
    }
    private boolean mineHatch(final AltoClef mod) {
        final List<BlockPos> toBreak = new LinkedList<>();
        toBreak.add(mod.getPlayer().getBlockPos().up().north());
        toBreak.add(mod.getPlayer().getBlockPos().up().north().up());
        toBreak.add(mod.getPlayer().getBlockPos().up().north().up().up());
        toBreak.add(mod.getPlayer().getBlockPos().up().north().up().up().up());
        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod.getWorld(), toBreak);
        return !mining;
    }
    private void murderBabies(final AltoClef mod) {
        final List<ZombieEntity> babies = mod.getEntityTracker().getHostiles().stream().filter(e -> e instanceof ZombieEntity && ((ZombieEntity) e).isBaby()).map(e -> (ZombieEntity)e).collect(Collectors.toList());
        if (babies.isEmpty()) return;
        final ZombieEntity entity = babies.get(0);
        KillEntityTask.equipWeapon(mod);
        float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
        if (hitProg >= 1) {
            LookHelper.lookAt(mod, entity.getEyePos());
            mod.getControllerExtras().attack(entity);
        }
    }
    private void leaveChamber(final AltoClef mod) {
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(mod.getPlayer().getBlockPos().offset(Direction.UP, 3)));
    }

    public void init(final AltoClef mod) {
        reset(mod);
        pinnedPos = mod.getPlayer().getBlockPos();
        active = true;
    }
    public boolean trapping(final AltoClef mod) {
        if (!ensureFloor(mod)) return false;
        if (!doneChamber) TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos));
        if (!doneChamber) doneChamber = mineChamber(mod);
        if (!doneChamber) return false;
        if (!doneHatch) tpChamber(mod);
        if (!doneHatch) doneHatch = mineHatch(mod);
        if (!doneHatch) return false;
        if (!doneWaiting) doneWaiting = timer++ > WAITING_LIMIT;
        if (!doneWaiting) murderBabies(mod);
        if (!doneWaiting) return false;
        TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos));
        //System.out.println("BACK TO: " + BlockPosHelper.toVec3dCenter(pinnedPos).toString());
        //Queen.attemptJump(mod);
        doneClosing = !mod.getWorld().getBlockState(pinnedPos.north()).isAir();
        if (!doneClosing) BasicDefenseManager.fill(mod, pinnedPos.north());
        if (!doneClosing) return false;
        Queen.attemptJump(mod);
        active = false;
        System.out.println("active: " + active);
        return true;
    }
    public boolean isActive() {
        return this.active;
    }
}