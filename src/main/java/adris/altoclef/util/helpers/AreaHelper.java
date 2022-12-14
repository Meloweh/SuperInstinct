package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import baritone.Baritone;
import baritone.api.utils.Rotation;
import net.minecraft.util.math.Vec3d;

public class AreaHelper {
    public static double orth(final AltoClef mod, final Vec3d startPos, final Vec3d endPos) {
        Vec3d sub = endPos.subtract(startPos);
        Vec3d n = new Vec3d(-sub.z, 0, sub.x);
        double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.z, 2));
        Vec3d x1minux0 = mod.getPlayer().getPos().subtract(startPos);
        double result = x1minux0.x * n.x * nd + x1minux0.z * n.z * nd;
        return result;
    }
    public static boolean targetInsideCone(final AltoClef mod, final Vec3d toLook, final float coneAngle) {
        final Rotation target = LookHelper.getLookRotation(mod, toLook);
        final Rotation current = LookHelper.getLookRotation();
        final float targetYaw360 = target.getYaw() + 180;
        final float currentYaw360 = target.getYaw() + 180;
        final Vec3d right = LookHelper.toVec3d(new Rotation(current.getYaw() + coneAngle, 0));
        final Vec3d left = LookHelper.toVec3d(new Rotation(current.getYaw() - coneAngle, 0));
        //final double orthLeft = orth(mod, )

        return false;
    }
}
