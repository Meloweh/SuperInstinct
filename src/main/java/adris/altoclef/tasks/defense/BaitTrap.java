package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasks.ArrowMapTests.CombatHelper;
import adris.altoclef.tasks.ArrowMapTests.MeteorClientPlace;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BaitTrap {
    private BlockPos pinnedPos;
    private final List<BlockPos> toBreak;
    private int timer;
    private static final int WAITING_LIMIT = 20*7;
    private boolean active;
    public void reset() {
        active = false;
        pinnedPos = null;
        toBreak.clear();
        timer = 0;
    }
    public BaitTrap() {
        this.toBreak = new LinkedList<>();
        this.active = false;
    }
    public void fixateTrap(final AltoClef mod, final List<Entity> nearbyHostiles) {
        reset();
        final boolean onlySkeletons = nearbyHostiles.stream().noneMatch(e -> !(e instanceof SkeletonEntity));
        final boolean hasShield = CombatHelper.hasShield(mod);
        pinnedPos = mod.getPlayer().getBlockPos();
        toBreak.add(pinnedPos.down());
        toBreak.add(pinnedPos.down().down());
        if (!hasShield && !onlySkeletons) {
            reset();
            return;
        }
        for (BlockPos blockPos : toBreak) {
            final BlockState state = mod.getWorld().getBlockState(blockPos);
            if (!StorageHelper.miningRequirementMet(mod, MiningRequirement.getMinimumRequirementForBlock(state.getBlock()))) {
                reset();
                return;
            }
        }
        active = true;
        //final boolean hasBucket = mod.getItemStorage().hasItem(Items.BUCKET);
        //final boolean hasLavaNeightbour =
        /*if (!mod.getItemStorage().hasItem(Items.LAVA_BUCKET)) {
            toBreak.add(pinnedPos.down().down().north());
            toBreak.add(pinnedPos.down().down().north().down());
        }*/
    }
    public boolean trapping(final AltoClef mod, final List<Entity> nearbyHostiles) {
        if (timer < WAITING_LIMIT) {
            System.out.println("waiting");
            final Vec3d center3d = BlockPosHelper.toVec3dCenter(pinnedPos);
            final Vec3d fixed3d = new Vec3d(center3d.getX(), Math.min(mod.getPlayer().getY(), center3d.getY()), center3d.getZ());
            TPAura.tp(mod, fixed3d, false);
            pinnedPos = new BlockPos(fixed3d);
        } else if (pinnedPos.equals(mod.getPlayer().getBlockPos())) {
            System.out.println("tping");
            Queen.attemptJump(mod);
        } else if (mod.getWorld().getBlockState(pinnedPos.up().up()).isAir()) {
            System.out.println("filling");
            BasicDefenseManager.fill(mod, pinnedPos.up().up());
        } else {
            System.out.println("finishing");
            reset();
            return false;
        }

        final boolean onlySkeletons = nearbyHostiles.stream().noneMatch(e -> !(e instanceof SkeletonEntity));
        final boolean hasShield = CombatHelper.hasShield(mod);
        if (!hasShield && !onlySkeletons) {
            reset();
            Queen.attemptJump(mod);
            return false;
        }

        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod.getWorld(), toBreak);
        if (!mining) {
            //CombatHelper.doShielding(mod);
            CombatHelper.equipShield(mod);
            timer++;
        } else {
            System.out.println("mining");
        }
        //final boolean finished = toBreak.stream().filter(e -> !mod.getWorld().getBlockState(e).isAir()).count() > 0;
        //System.out.println(finished + " ^^^^^^^ " + mining);
        //return toBreak.stream().filter(e -> !mod.getWorld().getBlockState(e).isAir()).count() > 0;
        return true;
    }
    public boolean isActive() {
        return toBreak.size() > 0;
    }
}
