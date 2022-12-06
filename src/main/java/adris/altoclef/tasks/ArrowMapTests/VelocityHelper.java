/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import baritone.api.utils.Rotation;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class VelocityHelper {
    public static final double PLAYER_SPRINT_SPEED = 0.28061d;
    public static final double PLAYER_WALK_SPEED = 0.21585d;

    public static Vec3d getExpectedVelocityFromPlayer(final LivingEntity player) {
        return getVelocityFromRotation(new Rotation(player.getYaw(), player.getPitch()), player.isSprinting());
    }

    public static Vec3d getVelocityFromRotation(final Rotation rotation, final boolean sprinting) {
        return RotationHelper.calcVector3dFromRotation(rotation).multiply(sprinting ? PLAYER_SPRINT_SPEED : PLAYER_WALK_SPEED);
    }

    /*public static Vec3d getVelocityFromRotationTry2(final Rotation rotation, final boolean sprinting) {
        final double k = sprinting ? PLAYER_SPRINT_SPEED : PLAYER_WALK_SPEED;
        final double xzLen = Math.cos(rotation.getPitch()) * k;
        final double dx = xzLen * Math.cos(rotation.getYaw());
        final double dz = xzLen * Math.sin(rotation.getYaw());
        final double dy = k * Math.sin(rotation.getPitch());
        return new Vec3d(dx, dy, dz);
    }*/

    /*public static Vec3d getVelocityFromRotationTry3(final Rotation rotation, final boolean sprinting) {
        final double distance = sprinting ? PLAYER_SPRINT_SPEED : PLAYER_WALK_SPEED;
        final double dx = distance * Math.cos(rotation.getPitch() * (Math.PI / 180.0D));
        final double dz = distance * Math.sin(rotation.getPitch() * (Math.PI / 180.0D));
        return new Vec3d(dx, 0, dz);
    }*/
}
