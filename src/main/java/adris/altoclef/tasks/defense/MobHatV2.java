package adris.altoclef.tasks.defense;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ArrowMapTests.BasicDefenseManager;
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
        System.out.println("around.size(): " + around.size());

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

        System.out.println("around.size(): " + around.size());
        /*around.add(minEdgeNorth);
        around.add(minEdgeWest);
        around.add(maxEdgeSouth);
        around.add(maxEdgeEast);*/

        /*
        final double xLen = box.getXLength();
        final double yLen = box.getYLength();
        final double zLen = box.getZLength();

        final Vec3d cornerNegXNegYNegZ = new Vec3d(box.minX, box.minY, box.minZ);
        final Vec3d cornerNegXNegYPosZ = new Vec3d(box.minX, box.minY, box.maxZ);
        final Vec3d cornerNegXPosYNegZ = new Vec3d(box.minX, box.maxY, box.minZ);
        final Vec3d cornerNegXPosYPosZ = new Vec3d(box.minX, box.maxY, box.maxZ);
        final Vec3d cornerPosXNegYNegZ = new Vec3d(box.maxX, box.minY, box.minZ);
        final Vec3d cornerPosXNegYPosZ = new Vec3d(box.maxX, box.minY, box.maxZ);
        final Vec3d cornerPosXPosYNegZ = new Vec3d(box.maxX, box.maxY, box.minZ);
        final Vec3d cornerPosXPosYPosZ = new Vec3d(box.maxX, box.maxY, box.maxZ);

        final BlockPos blockNegXNegYNegZ = new BlockPos(cornerNegXNegYNegZ);
        final BlockPos blockNegXNegYPosZ = new BlockPos(cornerNegXNegYPosZ);
        final BlockPos blockNegXPosYNegZ = new BlockPos(cornerNegXPosYNegZ);
        final BlockPos blockNegXPosYPosZ = new BlockPos(cornerNegXPosYPosZ);
        final BlockPos blockPosXNegYNegZ = new BlockPos(cornerPosXNegYNegZ);
        final BlockPos blockPosXNegYPosZ = new BlockPos(cornerPosXNegYPosZ);
        final BlockPos blockPosXPosYNegZ = new BlockPos(cornerPosXPosYNegZ);
        final BlockPos blockPosXPosYPosZ = new BlockPos(cornerPosXPosYPosZ);*/

        //if (entity instanceof SpiderEntity) return around;
        /*final BlockPos origPos = entity.getBlockPos().up();
        if (entity.getWorld().getBlockState(origPos.north()).isAir()) around.add(origPos.north());
        if (entity.getWorld().getBlockState(origPos.west()).isAir()) around.add(origPos.west());
        if (entity.getWorld().getBlockState(origPos.east()).isAir()) around.add(origPos.east());
        if (entity.getWorld().getBlockState(origPos.south()).isAir()) around.add(origPos.south());
        if (entity.getWorld().getBlockState(origPos.up()).isAir()) around.add(origPos.up());*/
        return around;
    }

    public boolean attemptHat(AltoClef mod) {
        final List<Entity> hostiles = mod.getEntityTracker().getHostiles().stream().filter(e ->
                e.distanceTo(mod.getPlayer()) <= DefenseConstants.NEARBY_DISTANCE && LookHelper.seesPlayer(e, mod.getPlayer(), DefenseConstants.NEARBY_DISTANCE)).collect(Collectors.toList());
        nearbyPresent = hostiles.size() > 0;
        for (final Entity e : hostiles) {
            final List<BlockPos> around = getAround(e);
            around.sort((a, b) -> b.getManhattanDistance(mod.getPlayer().getBlockPos()) - a.getManhattanDistance(mod.getPlayer().getBlockPos()));
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
