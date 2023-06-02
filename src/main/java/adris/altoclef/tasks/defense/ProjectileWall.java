package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import welomehandmeloweh.superinstinct.BasicDefenseManager;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectileWall {
    private boolean nearbyPresent = false;
    public boolean attemptWall(final AltoClef mod) {
        final List<Entity> hostiles = mod.getEntityTracker().getHostiles().stream()
                .filter(e -> e.distanceTo(mod.getPlayer()) <= DefenseConstants.PROJECTILE_NEARBY_DISTANCE && LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.PROJECTILE_NEARBY_DISTANCE))
                .filter(e -> e instanceof SkeletonEntity || e instanceof PillagerEntity)
                .collect(Collectors.toList());
        nearbyPresent = hostiles.size() > 0;
        for (final Entity e : hostiles) {
            if (mod.getItemStorage().getBlockCount() < 2) {
                return false;
            }
            //LookHelper.lookAt(mod, e.getEyePos());
            final List<BlockPos> posList = new LinkedList<>();
            final BlockHitResult bhr = LookHelper.raycast(mod.getPlayer(), e.getEyePos(), 1);
            posList.add(bhr.getBlockPos());
            if (e.getBlockPos().getY() > mod.getPlayer().getBlockPos().getY()) {
                posList.add(bhr.getBlockPos().up());
            } else if (e.getBlockPos().getY() == mod.getPlayer().getBlockPos().getY()) {
                posList.add(bhr.getBlockPos().down());
            }
            posList.forEach(b -> BasicDefenseManager.fill(mod, b));
        }
        return true;
    }
}
