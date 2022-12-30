package adris.altoclef.tasks.defense.chess;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class HorizontalWing {
    /*final Pair<List<BlockPos>, List<BlockPos>> wing;
    final BlockPos parting;*/
    private Optional<BlockPos> founding;
    private static Direction rotateXZ(final Direction dir, final int times) {
        final Direction newDir = dir.equals(Direction.NORTH) ?
                Direction.EAST : dir.equals(Direction.EAST) ?
                Direction.SOUTH: dir.equals(Direction.SOUTH) ?
                Direction.WEST : dir.equals(Direction.WEST) ?
                Direction.NORTH : dir;
        if (times > 1) {
            return rotateXZ(newDir, times-1);
        }
        return newDir;
    }
    public HorizontalWing(final World world, final BlockPos start, final Direction dir) {
        this(world, start, dir, true, Consts.MAX_ABSOLUTE_DISTANCE);
    }
    private HorizontalWing(final World world, final BlockPos start, final Direction dir, final boolean isParting, final int distance) {
        final List<Wing2D> line = new LinkedList<>();
        this.founding = Optional.empty();
        //this.failed = true;
        final BlockPos goal = start.offset(dir, distance);
        for (BlockPos it = start.offset(dir, 1); !it.equals(goal); it = it.offset(dir, 1)) {
            final Wing2D wing2D = new Wing2D(world, it);
            if (wing2D.hasFailed()) {
                if (isParting) {
                    line.add(wing2D);
                }
                break;
            } else {
                line.add(wing2D);
            }
        }
        System.out.println("line size: " + line.size());
        //Collections.reverse(line);
        for (int i = line.size() - 1; i >= 0; i--) {
            final Wing2D wing = line.get(i);
            if (isParting && i > 0) {
                System.out.println("doing curve: i = " + i);
                final HorizontalWing hWingLeft = new HorizontalWing(world, wing.getFeet(), rotateXZ(dir, 1), false, distance-i);
                this.founding = hWingLeft.getFounding();
                if (this.founding.isPresent()) {
                    return;
                }
                final HorizontalWing hWingRight = new HorizontalWing(world, wing.getFeet(), rotateXZ(dir.getOpposite(), 1), false, distance-i);
                this.founding = hWingRight.getFounding();
                if (this.founding.isPresent()) {
                    return;
                }
            }
            final VerticalWing vWing = new VerticalWing(world, wing.getFeet().offset(Direction.DOWN, Consts.MAX_DESCEND), wing.getFeet().offset(Direction.UP, Consts.MAX_ASCEND));
            //this.failed = hWing.hasFailed();
            this.founding = vWing.getFounding();
            if (this.founding.isPresent()) {
                return;
            }

        }

        /*final BlockPos max = start.offset(dir, Consts.MAX_ABSOLUTE_DISTANCE);// TODO: center if not done
        final Box newBox = origBox.offset(origBox.getCenter().multiply(-1)).offset(start);// TODO: center if not done
        if (Double.compare(newBox.getCenter().getX(), (int)newBox.getCenter().getX() + 0.1d) < 0 || Double.compare(newBox.getCenter().getZ(), (int)newBox.getCenter().getZ() + 0.1d) < 0) {
            throw new IllegalStateException("TODO: center if not done");
        }
        final Optional<Vec3d> lowerRaycast = newBox.raycast(new Vec3d(start.getX() + 0.5d, start.getY(), start.getZ() + 0.5d),
                new Vec3d(max.getX() + 0.5d, max.getY(), max.getZ() + 0.5d));


        for (BlockPos it = furthest; !it.equals(start); it = it.offset(dir.getOpposite(), 1)) {
            final Wing2D wing2D = new Wing2D();
        }*/
    }
    public Optional<BlockPos> getFounding() {
        return this.founding;
    }
}
