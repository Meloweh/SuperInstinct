package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MobHat {
    private boolean nearbyPresent = false;

    private static List<BlockPos> getAround(final Entity entity) {
        final List<BlockPos> around = new LinkedList<>();
        if (entity instanceof SpiderEntity) return around;
        final BlockPos origPos = entity.getBlockPos().up();
        if (entity.getWorld().getBlockState(origPos.north()).isAir()) around.add(origPos.north());
        if (entity.getWorld().getBlockState(origPos.west()).isAir()) around.add(origPos.west());
        if (entity.getWorld().getBlockState(origPos.east()).isAir()) around.add(origPos.east());
        if (entity.getWorld().getBlockState(origPos.south()).isAir()) around.add(origPos.south());
        if (entity.getWorld().getBlockState(origPos.up()).isAir()) around.add(origPos.up());
        return around;
    }

    public boolean attemptHat(AltoClef mod) {
        final List<Entity> hostiles = mod.getEntityTracker().getHostiles().stream().filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE && LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.NEARBY_DISTANCE)).collect(Collectors.toList());
        nearbyPresent = hostiles.size() > 0;
        for (final Entity e : hostiles) {
            final List<BlockPos> around = getAround(e);
            if (around.size() < 1 || mod.getItemStorage().getBlockCount() < around.size()) {
                return false;
            }
            for (final BlockPos pos : around) {
                final BlockState state = mod.getWorld().getBlockState(pos);
                if (state.isAir()) {
                    BasicDefenseManager.fill(mod, pos);
                }
            }
        }
        return true;
    }

    public boolean isNearbyPresent() {
        return nearbyPresent;
    }
}
