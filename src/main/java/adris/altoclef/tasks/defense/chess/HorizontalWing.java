package adris.altoclef.tasks.defense.chess;

import adris.altoclef.util.helpers.BlockPosHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

//TODO: diagonal tp with 4 block radius DONE
//FIXME: First horizontal wing must have at least the same length of the second wing DONE
//FIXME: Total cubic meters of a jump has to be limited to 256 blocks total
//FIXME: Descend on horizontal straight line max is 11
public class HorizontalWing {
    /*final Pair<List<BlockPos>, List<BlockPos>> wing;
    final BlockPos parting;*/
    private Optional<BlockPos> founding;
    private Vec3d paddedStart;
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
    public HorizontalWing(final World world, final BlockPos start, final Direction dir, final boolean aboveHeadBlocked, final boolean floorRequired) {
        this(world, start, dir, true, Consts.MAX_ABSOLUTE_DISTANCE, aboveHeadBlocked, floorRequired, 0);
    }
    private Vec3d calculatePaddedPos(final BlockPos start, final BlockPos end, final Direction dir) {
        final Vec3d centeredStart = BlockPosHelper.toVec3dCenter(start);
        //this.paddedStart = centeredStart;
        final int deltaX = Math.abs(start.getX() - end.getX());
        final int deltaZ = Math.abs(start.getZ() - end.getZ());
        if (deltaX == 0 || deltaZ == 0) {
            return centeredStart;
        }
        final Direction left = rotateXZ(dir.getOpposite(), 1);
        final BlockPos offsetX = start.offset(left, 1);
        final int deltaLeftX = Math.abs(offsetX.getX() - end.getX());
        final int deltaLeftZ = Math.abs(offsetX.getZ() - end.getZ());
        if (deltaLeftX < deltaX || deltaLeftZ < deltaZ) {
            final Vec3d padding = BlockPosHelper.toVec3dCenter(offsetX.subtract(start)).multiply(0.15d);
            return centeredStart.add(padding);
        }
        final Direction right = rotateXZ(dir, 1);
        final BlockPos offsetZ = start.offset(right, 1);
        final int deltaRightX = Math.abs(offsetZ.getX() - end.getX());
        final int deltaRightZ = Math.abs(offsetZ.getZ() - end.getZ());
        if (deltaRightX < deltaX || deltaRightZ < deltaZ) {
            final Vec3d padding = BlockPosHelper.toVec3dCenter(offsetZ.subtract(start)).multiply(0.15d);
            return centeredStart.add(padding);
        }
        return centeredStart;
    }
    private HorizontalWing(final World world, final BlockPos start, final Direction dir, final boolean isParting, final int distance, final boolean aboveHeadBlocked, final boolean floorRequired, final int partingDistanceUsage) {
        final List<Wing2D> line = new LinkedList<>();
        this.founding = Optional.empty();
        //this.failed = true;
        final BlockPos goal = start.offset(dir, distance);
        //boolean alreadyElevated = false;
        boolean prevElevated = false;
        for (BlockPos it = start/*.offset(dir, 1)*/; !it.equals(goal); it = it.offset(dir, 1)) {
            final Wing2D wing2D = new Wing2D(world, it, aboveHeadBlocked, prevElevated);
            prevElevated = wing2D.isElevated();
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
        //final int remaining = distance - line.size() - 1 <= 0 ? 0 : distance - line.size() - 1;
        for (int i = line.size() - 1; i > 0; i--) {
        //for (int i = 0; i < line.size(); i++) {
            final Wing2D wing = line.get(i);
            //if (remaining > i)
            //final int deltaDist = Math.min(i, line.size() - i - 1);//i < line.size() / 2 ? i : line.size() - 1 - i;//distance-i < i ? distance-i : i; //FIXME: +1?
            if (isParting && i < line.size() - 1 && !wing.hasFailed()/*(line.size() - 1 != i || !wing.hasFailed())*/) {
                //System.out.println("doing curve: i = " + i);
                //final Direction rotatedDir = rotateXZ(dir, 1);
                final Stack<Direction> stack = new Stack<>();
                stack.push(rotateXZ(dir, 1));
                stack.push(rotateXZ(dir.getOpposite(), 1));
                Collections.shuffle(stack);
                final int j = Consts.MAX_ABSOLUTE_DISTANCE - i;
                int deltaDist = Math.min(j, i); //i <= distance ? i : distance;//i < line.size()-i ? i : line.size()-i;//!isParting ? (i < distance / 2 ? ) : (i < line.size())
                //System.out.println("size " + (line.size()) + " i " + i + " --- " + (line.size() - i) + " delta " + deltaDist );
                while (!stack.empty()) {
                    final HorizontalWing leftOrRightWing = new HorizontalWing(world, wing.getFeet(), stack.pop(), false, deltaDist, aboveHeadBlocked, floorRequired, i);
                    this.founding = leftOrRightWing.getFounding();
                    if (this.founding.isPresent()) {
                        this.paddedStart = calculatePaddedPos(start, this.founding.get(), dir);
                        return;
                    }
                }

                /*
                final HorizontalWing hWingLeft = new HorizontalWing(world, wing.getFeet(), rotateXZ(dir, 1), false, distance-i, aboveHeadBlocked);
                this.founding = hWingLeft.getFounding();
                if (this.founding.isPresent()) {
                    this.paddedStart = calculatePaddedPos(start, this.founding.get(), dir);
                    return;
                }
                final HorizontalWing hWingRight = new HorizontalWing(world, wing.getFeet(), rotateXZ(dir.getOpposite(), 1), false, distance-i, aboveHeadBlocked);
                this.founding = hWingRight.getFounding();
                if (this.founding.isPresent()) {
                    this.paddedStart = calculatePaddedPos(start, this.founding.get(), dir);
                    return;
                }*/
            }

            final int dim2D = partingDistanceUsage * i;
            int masssummeDifferenz = Consts.MAX_ABSOLUTE_DISTANCE - (partingDistanceUsage + i);
            System.out.println("masssummeDifferenz: " + masssummeDifferenz);
            if (masssummeDifferenz < 0) masssummeDifferenz = 0;
            int remainingScendingDist = dim2D < 1 ? 0 : Consts.MAX_PASSABLE_VOLUME / dim2D;
            int remainingAscendingDist = Math.min(masssummeDifferenz, dim2D < 1 ? Consts.MAX_ASCEND : Math.min(remainingScendingDist, Consts.MAX_ASCEND )); //remainingScendingDist < Consts.MAX_ASCEND ? remainingScendingDist : Consts.MAX_ASCEND;
            int remainingDescendingDist = Math.min(masssummeDifferenz, Consts.MAX_ABSOLUTE_DISTANCE-11 <= i ?
                    0
                    :
                    dim2D < 1 ? Consts.MAX_DESCEND : Math.min(remainingScendingDist, Consts.MAX_DESCEND));//remainingScendingDist < Consts.MAX_DESCEND ? remainingScendingDist : Consts.MAX_DESCEND;
            System.out.println("remainingAscendingDist: " + remainingAscendingDist);
            System.out.println("remainingDescendingDist: " + remainingDescendingDist);
            final VerticalWing vWing = new VerticalWing(world, wing.getFeet().offset(Direction.DOWN, remainingDescendingDist), wing.getFeet().offset(Direction.UP, remainingAscendingDist), floorRequired);
            //this.failed = hWing.hasFailed();
            this.founding = vWing.getFounding();
            if (this.founding.isPresent()) {
                this.paddedStart = calculatePaddedPos(start, this.founding.get(), dir);
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

    public Vec3d getPaddedStart() {
        return paddedStart;
    }
}
