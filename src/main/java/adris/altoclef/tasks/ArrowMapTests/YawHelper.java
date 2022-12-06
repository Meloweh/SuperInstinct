/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import baritone.api.utils.Rotation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class YawHelper {
    /**
     * @author Brady, Welomeh, Meloweh
     * @since 9/25/2018
     */
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;
    private static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.getYaw(), target.getPitch());
        }
        return target.subtract(current).normalize().add(current);
    }

    /**
     * @author Brady, Welomeh, Meloweh
     * @since 9/25/2018
     */
    public static float vecToYaw(Vec3d orig, Vec3d dest) {
        double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
        double yaw = Math.atan2(delta[0], -delta[2]);
        return (float) (yaw * RAD_TO_DEG);
    }

    public static Vec3d yawToVec(final float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(0);
        float f3 = MathHelper.sin(0);
        return new Vec3d(f1 * f2, f3, f * f2);
    }
}
