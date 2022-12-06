/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class FloatingPointBresenham {
    private final static int signum(final int x) {
        return (x > 0) ? 1 : (x < 0) ? -1 : 0;
    }
    private final static int signum(final double x) {
        return (Double.compare(x, 0d) > 0) ? 1 : (Double.compare(x, 0d) < 0) ? -1 : 0;
    }


    public static List<BlockPos> floatingPointBresenham(final int iY, final double dStartX, final double dStartZ, final double dEndX, final double dEndZ) {
        final List<BlockPos> blocks = new ArrayList<>();
        /*int iX = (int) Math.floor(dStartX),
            iZ = (int) Math.floor(dStartZ);*/

        int iX = Double.compare(dStartX, dEndX) < 0 ? (int)Math.ceil(dStartX) : (int)dStartX;
        int iZ = Double.compare(dStartZ, dEndZ) < 0 ? (int)Math.ceil(dStartZ) : (int)dStartZ;

        final double dDiffX = dEndX - dStartX,
                dDiffZ = dEndZ - dStartZ,
                dStepX = signum(dDiffX), //sign
                dStepZ = signum(dDiffZ), // sign
                dOffsetX = Double.compare(dEndX, dStartX) > 0 ? Math.ceil(dStartX) - dStartX : dStartX - Math.floor(dStartX),
                dOffsetZ = Double.compare(dEndZ, dStartZ) > 0 ? Math.ceil(dStartZ) - dStartZ : dStartZ - Math.floor(dStartZ),
                //This looks good, but I would prefer not using atan2, sin, cos. I think 1/cos(atan2(-y, x)) is sqrt(x * x + y * y) / x and 1/sin(atan2(-y, x)) is -sqrt(x*x + y*y) / y
                //dAngle = Math.atan2(-dDiffZ, dDiffX),
                //dCosAngle = Math.cos(dAngle),
                //dSinAngle = Math.sin(dAngle),
                //dDeltaX = Double.compare(dCosAngle, 0) == 0 ? 0 : 1d / dCosAngle, // avoid 0 div
                //dDeltaZ = Double.compare(dSinAngle, 0) == 0 ? 0 : 1d / dSinAngle, // avoid 0 div
                dDeltaX = Double.compare(dDiffX, 0) == 0 ? 0 : Math.sqrt(dDiffX * dDiffX + dDiffZ * dDiffZ) / dDiffX,
                dDeltaZ = Double.compare(dDiffZ, 0) == 0 ? 0 : -Math.sqrt(dDiffX * dDiffX + dDiffZ * dDiffZ) / dDiffZ,
                dManhattanDist = Math.abs(Math.floor(dEndX) - Math.floor(dStartX)) + Math.abs(Math.floor(dEndZ) - Math.floor(dStartZ));
        double dMaxX = Double.compare(1d / dDeltaX, 0) == 0 ? 0 : dOffsetX / 1d / dDeltaX, // avoid 0 div
                dMaxZ = Double.compare(1d / dDeltaZ, 0) == 0 ? 0 : dOffsetZ / 1d / dDeltaZ; // avoid 0 div

        for (int t = 0; t <= dManhattanDist; ++t) {
            blocks.add(new BlockPos(iX, iY, iZ));

            if (Double.compare(Math.abs(dMaxX), Math.abs(dMaxZ)) < 0) {
                dMaxX += dDeltaX;
                iX += dStepX;
            } else {
                dMaxZ += dDeltaZ;
                iZ += dStepZ;
            }
            //System.out.println("dMaxX: " + dMaxX + " dMaxZ: " + dMaxZ + " dDeltaX: " + dDeltaX + " dDeltaZ: " + dDeltaZ + " iX: " + iX + " iZ: " + iZ);
        }
        return blocks;
    }

    public static List<BlockPos> apply(final BlockPos start, final BlockPos end) {
        final int startX = start.getX(), startZ = start.getZ(), endX = end.getX(), endZ = end.getZ(), y = start.getY();
        int x, z, t, dx, dz, incx, incz, pdx, pdz, ddx, ddz, deltaslowdirection, deltafastdirection, err;
        final List<BlockPos> blockPosList = new ArrayList<>();
        dx = endX - startX;
        dz = endZ - startZ;
        incx = signum(dx);
        incz = signum(dz);
        if (dx < 0) dx = -dx;
        if (dz < 0) dz = -dz;
        final boolean xFaster = dx > dz;
        if (xFaster) {
            pdx = incx; pdz = 0;
            ddx = incx; ddz = incz;
            deltaslowdirection = dz;
            deltafastdirection = dx;
        } else {
            pdx = 0; pdz = incz;
            ddx = incx; ddz = incz;
            deltaslowdirection = dx;
            deltafastdirection = dz;
        }

        x = startX;
        z = startZ;
        err = deltafastdirection / 2;

        for (t = 0; t < deltafastdirection; ++t) {
            err -= deltaslowdirection;
            if(err < 0) {
                err += deltafastdirection;
                final int xNew = x + ddx;
                final int zNew = z + ddz;
                x = xNew;
                z = zNew;
            } else {
                x += pdx;
                z += pdz;
            }
            blockPosList.add(new BlockPos(x, y, z));

        }
        return blockPosList;
    }

}
