package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import welomehandmeloweh.superinstinct.CombatHelper;
import welomehandmeloweh.superinstinct.MeteorClientPlace;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

public class BaitTrap {
    private BlockPos pinnedPos;
    private final List<BlockPos> toBreak;
    private int timer;
    private static final int WAITING_LIMIT = 20*7;
    private boolean active;
    private boolean onlySkeletons;
    //private BlockPos waitingSpot;
    public void reset(final AltoClef mod) {
        active = false;
        pinnedPos = null;
        toBreak.clear();
        timer = 0;
        onlySkeletons = false;
        //waitingSpot = null;
        CombatHelper.stopShielding(mod);
    }
    public BaitTrap(final AltoClef mod) {
        this.toBreak = new LinkedList<>();
        reset(mod);
    }
    public void fixateTrap(final AltoClef mod, final List<Entity> nearbyHostiles) {
        toBreak.add(mod.getPlayer().getBlockPos().down().down());
        MeteorClientPlace.packetBreakBlocks(mod, toBreak);
        toBreak.clear();
        if (1 == 1) return;
        reset(mod);
        mod.getSlotHandler().forceDeequipRightClickableItem();
        onlySkeletons = nearbyHostiles.stream().noneMatch(e -> !(e instanceof SkeletonEntity));
        final boolean hasShield = CombatHelper.hasShield(mod);
        pinnedPos = mod.getPlayer().getBlockPos();
        toBreak.add(pinnedPos.down());
        toBreak.add(pinnedPos.down().down());
        if (!hasShield/* && !onlySkeletons*/) {
            reset(mod);
            return;
        }
        if (!onlySkeletons) {
            toBreak.add(pinnedPos.down().down().north());
            toBreak.add(pinnedPos.down().down().north().down());
            //waitingSpot = pinnedPos.down().down().north().down();
        }
        toBreak.clear();
        for (BlockPos blockPos : toBreak) {
            final BlockState state = mod.getWorld().getBlockState(blockPos);
            if (!StorageHelper.miningRequirementMet(mod, MiningRequirement.getMinimumRequirementForBlock(state.getBlock()))) {
                reset(mod);
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
        final boolean mining = MeteorClientPlace.packetBreakBlocks(mod, toBreak);
        if (timer < WAITING_LIMIT) {
            System.out.println("waiting");
            /*if (!onlySkeletons && !mining) {
                pinnedPos = toBreak.get(toBreak.size() - 1);
            }*/
            if (onlySkeletons) {
                final Vec3d center3d = BlockPosHelper.toVec3dCenter(pinnedPos);
                final Vec3d fixed3d = new Vec3d(center3d.getX(), Math.min(mod.getPlayer().getY(), center3d.getY()), center3d.getZ());
                TPAura.tp(mod, fixed3d, false);
                pinnedPos = new BlockPos(fixed3d);
            } else if (!mining) {
                TPAura.tp(mod, BlockPosHelper.toVec3dCenter(toBreak.get(toBreak.size() - 1)), false);
                System.out.println(toBreak.get(toBreak.size() - 1).toString());
                pinnedPos = new BlockPos(toBreak.get(toBreak.size() - 1));
            }

        } else if (pinnedPos.equals(mod.getPlayer().getBlockPos())) {
            System.out.println("tping");
            /*if (onlySkeletons) {
                Queen.attemptJump(mod);
            } else {
                TPAura.tp(mod, BlockPosHelper.toVec3dCenter(pinnedPos.offset(Direction.UP, 3)), false);
            }*/
            Queen.attemptJump(mod);
        } else if (mod.getWorld().getBlockState(pinnedPos.up().up()).isAir()) {
            System.out.println("filling");
            BasicDefenseManager.fill(mod, pinnedPos.up().up());
            Queen.attemptJump(mod);
        } else {
            System.out.println("finishing");
            reset(mod);
            return false;
        }

        final boolean onlySkeletons = nearbyHostiles.stream().noneMatch(e -> !(e instanceof SkeletonEntity));
        final boolean hasShield = CombatHelper.hasShield(mod);
        if (!hasShield && !onlySkeletons) {
            reset(mod);
            Queen.attemptJump(mod);
            return false;
        }

        if (!mining) {
            //CombatHelper.doShielding(mod);
            /*if (!mod.getPlayer().getBlockPos().equals(waitingSpot)) {
                TPAura.tp(mod, BlockPosHelper.toVec3dCenter(waitingSpot));
            }*/
            /*if (!onlySkeletons) {
                pinnedPos = waitingSpot;
            }*/
            //mod.getSlotHandler().forceDeequip(e -> e.isStackable());//mod.getItemStorage().getBlockTypes().contains(e.getItem())
            if (!CombatHelper.isShieldEquipped()) {
                mod.getSlotHandler().forceEquipItem(Items.SHIELD);
            }
            CombatHelper.equipShield(mod);
            timer++;
            System.out.println("!mining");
        } else {
            System.out.println("mining");
            mod.getPlayer().setSneaking(true);
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