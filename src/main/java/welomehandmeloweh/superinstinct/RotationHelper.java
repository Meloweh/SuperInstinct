/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import baritone.api.utils.Rotation;
import net.minecraft.util.math.Vec3d;

/**
 * SuperInstinct is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperInstinct is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperInstinct.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2023 MelowehAndWelomeh
 */
public class RotationHelper {
    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;
    public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.getYaw(), target.getPitch());
        }
        return target.subtract(current).normalize().add(current);
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest, Rotation current) {
        return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    private static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
        double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
        double yaw = Math.atan2(delta[0], -delta[2]);
        double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
        double pitch = Math.atan2(delta[1], dist);
        return new Rotation(
                (float) (yaw * RAD_TO_DEG),
                (float) (pitch * RAD_TO_DEG)
        );
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static Vec3d calcVector3dFromRotation(Rotation rotation) {
        double f = Math.cos(-rotation.getYaw() * DEG_TO_RAD - Math.PI);
        double f1 = Math.sin(-rotation.getYaw() * DEG_TO_RAD - Math.PI);
        double f2 = -Math.cos(-rotation.getPitch() * DEG_TO_RAD);
        double f3 = Math.sin(-rotation.getPitch() * DEG_TO_RAD);
        return new Vec3d(f1 * f2, f3, f * f2);
    }
}
