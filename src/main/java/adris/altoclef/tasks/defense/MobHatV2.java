package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
import adris.altoclef.tasks.SchematicBuildTask;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MobHatV2 {
    private boolean nearbyPresent = false;
    private static List<BlockPos> getAround(final Entity entity) {
        final List<BlockPos> around = new LinkedList<>();

      /*north: neg z
        south: pos z
        west:  neg x
        east:  pos x
         */

        /*
                final Box oldBox = entity.getBoundingBox();
        final Vec3d eye = entity.getEyePos();
        final BlockPos eyeBlock = new BlockPos(eye);
        final Vec3d vel = entity.getVelocity();
        final double dx = Double.compare(Math.abs(vel.getX()), 0d) > 0.03 ? Math.signum(vel.getX()) : 0;
        //final double dy = Double.compare(Math.abs(vel.getY()), 0) > 0.03 ? Math.signum(vel.getY()) : 0;
        final double dz = Double.compare(Math.abs(vel.getZ()), 0d) > 0.03 ? Math.signum(vel.getZ()) : 0;
        final double dMinX = Double.compare(dx, 0d) < 0 ? dx : 0;
        final double dMaxX = Double.compare(dx, 0d) > 0 ? dx : 0;
        final double dMinZ = Double.compare(dz, 0d) < 0 ? dz : 0;
        final double dMaxZ = Double.compare(dz, 0d) > 0 ? dz : 0;

        final Box box = new Box(oldBox.minX + dMinX, oldBox.minY, oldBox.minZ + dMinZ, oldBox.maxX + dMaxX, oldBox.maxY, oldBox.maxZ + dMaxZ);

        final Vec3d minSpan = new Vec3d(box.minX, eye.getY(), box.minZ);
        final Vec3d maxSpan = new Vec3d(box.maxX, eye.getY(), box.maxZ);
         */
        final Box box = entity.getBoundingBox();
        final Vec3d eye = entity.getEyePos();
        final BlockPos eyeBlock = new BlockPos(eye);
        final Vec3d minSpan = new Vec3d(box.minX, eye.getY(), box.minZ);
        final Vec3d maxSpan = new Vec3d(box.maxX, eye.getY(), box.maxZ);

        final BlockPos minSpanBlock = new BlockPos(minSpan);
        final BlockPos maxSpanBlock = new BlockPos(maxSpan);

        final BlockPos overMinSpanBlock = minSpanBlock.up();
        final BlockPos overMaxSpanBlock = maxSpanBlock.up();

        final int xDiffDach = overMaxSpanBlock.getX() - overMinSpanBlock.getX();
        final int zDiffDach = overMaxSpanBlock.getZ() - overMinSpanBlock.getZ();

        for (int x = 0; x <= xDiffDach; x++) {
            for (int z = 0; z <= zDiffDach; z++) {
                final BlockPos pos = new BlockPos(overMinSpanBlock.getX() + x, eyeBlock.up().getY(), overMinSpanBlock.getZ() + z);
                around.add(pos);
            }
        }
        //System.out.println("around.size(): " + around.size());

        final BlockPos minEdgeNorth = minSpanBlock.north();
        final BlockPos minEdgeWest = minSpanBlock.west();
        final BlockPos maxEdgeSouth = maxSpanBlock.south();
        final BlockPos maxEdgeEast = maxSpanBlock.east();

        final int zDiff = maxEdgeEast.getZ() - minEdgeWest.getZ();
        final int xDiff = maxEdgeSouth.getX() - minEdgeNorth.getX();
      /*north: neg z
        south: pos z
        west:  neg x
        east:  pos x
         */

        for (int z = 0; z <= zDiff; z++) {
            /*for (int z = 0; z <= zDiff; z++) {
                final BlockPos pos = new BlockPos(minSpanBlock.getX() + x, eyeBlock.getY(), minSpanBlock.getZ() + z);
                around.add(pos);
            }*/
            final BlockPos first = new BlockPos(minEdgeWest.getX(), eyeBlock.getY(), minEdgeWest.getZ() + z);
            final BlockPos second = new BlockPos(maxEdgeEast.getX(), eyeBlock.getY(), maxEdgeEast.getZ() - z);
            around.add(first);
            around.add(second);
        }
        for (int x = 0; x <= xDiff; x++) {
            final BlockPos first = new BlockPos(minEdgeNorth.getX() + x, eyeBlock.getY(), minEdgeNorth.getZ());
            final BlockPos second = new BlockPos(maxEdgeSouth.getX() - x, eyeBlock.getY(), maxEdgeSouth.getZ());
            around.add(first);
            around.add(second);
        }
        return around;
    }

    public boolean canAttemptHat(final AltoClef mod) {
        final List<Entity> hostiles = mod.getEntityTracker().getHostiles().stream().filter(e ->
                e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE && LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.NEARBY_DISTANCE)).collect(Collectors.toList());
        if (mod.getItemStorage().getBlockCount() < 1) {
            return false;
        }
        for (final Entity e : hostiles) {
            final List<BlockPos> around = getAround(e);
            around.sort((a, b) -> b.getManhattanDistance(mod.getPlayer().getBlockPos()) - a.getManhattanDistance(mod.getPlayer().getBlockPos()));
            if (around.size() < 1) {
                continue;
            }
            if (mod.getItemStorage().getBlockCount() < around.size()) {
                continue;
            }
            return true;
        }
        return false;
    }

    public boolean attemptHat(AltoClef mod) {
        final List<Entity> hostiles = mod.getEntityTracker().getHostiles().stream().filter(e ->
                e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE && LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.NEARBY_DISTANCE) && e.distanceTo(mod.getPlayer()) > 2).collect(Collectors.toList());
        nearbyPresent = hostiles.size() > 0;
        for (final Entity e : hostiles) {
            final List<BlockPos> around = getAround(e);
            around.sort((a, b) -> b.getManhattanDistance(mod.getPlayer().getBlockPos()) - a.getManhattanDistance(mod.getPlayer().getBlockPos()));
            if (around.size() < 1 || mod.getItemStorage().getBlockCount() < around.size()) {
                return false;
            }
            for (final BlockPos pos : around) {
                if (SchematicBuildTask.getBounds().isPresent() && SchematicBuildTask.getBounds().get().expand(3).inside(pos)) {
                    return false;
                }
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
