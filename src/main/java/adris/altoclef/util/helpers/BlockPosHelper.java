package adris.altoclef.util.helpers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockPosHelper {
    public static Vec3d toVec3dCenter(final BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
    }
}
