/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.Vec3d;

final class VecXZ {
    Vec3d vec3d;

    public VecXZ(final double x, final double y) {
        this.vec3d = new Vec3d(x, 0, y);
    }

    public VecXZ(final Vec3d vec3d) {
        this(vec3d.x, vec3d.z);
    }

    public double getX() {
        return vec3d.getX();
    }

    public double getZ() {
        return vec3d.getZ();
    }

    public void add(final double x, final double z) {
        this.vec3d = this.vec3d.add(x, 0, z);
    }

    public boolean isXZero() {
        return Double.compare(getX(), 0.0d) <= 0 && Double.compare(getX(), -0.0d) >= 0;
    }

    public boolean isZZero() {
        return Double.compare(getZ(), 0.0d) <= 0 && Double.compare(getZ(), -0.0d) >= 0;
    }

    public boolean isZero() {
        return isXZero() && isZZero();
    }

    @Override
    public String toString() {
        return vec3d.toString();
    }
}