/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

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