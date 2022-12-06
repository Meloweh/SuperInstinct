package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicDefenseManager {
    private final ArrowThreadManager manager;
    private enum Strategy {
        SHIELD,
        RUN,
        PLACE_SIDE,
        PLACE_BELOW,
        DIG,
        JUMP,
        NONE
    }
    private Strategy strategy;
    private Optional<List<TickableTraceInfo>> focus;
    private Optional<BlockPos> breakCandidate;
    private List<BlockPos> placeCandidates;

    public BasicDefenseManager() {
        this.manager = new ArrowThreadManager();
        this.strategy = Strategy.NONE;
        this.focus = Optional.empty();
        this.breakCandidate = Optional.empty();
        this.placeCandidates = new LinkedList<>();
    }

    static void lookAt(AltoClef mod, BlockPos toLook, Direction side) {
        Vec3d target = new Vec3d(toLook.getX() + 0.5, toLook.getY() + 0.5, toLook.getZ() + 0.5);
        if (side != null) {
            target.add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        }
        Rotation targetRotation = LookHelper.getLookRotation(mod, toLook);
        mod.getPlayer().setYaw(targetRotation.getYaw());
        mod.getPlayer().setPitch(targetRotation.getPitch());
    }

    private void lookOnTopTarget(final AltoClef mod, final BlockPos target) {
        //mod.getPlayer().setPitch(-85);
        //LookHelper.lookAt(mod, target, Direction.UP);
        lookAt(mod, target, Direction.UP);
    }

    private Task run(final AltoClef mod) {
        return null;
    }

    private Vec3d centroid(final List<Vec3d> vecs) {
        Vec3d sum = Vec3d.ZERO;
        for (final Vec3d vec : vecs) {
            sum = sum.add(vec);
        }
        final Vec3d result = sum.multiply(1/vecs.size());
        return result;
    }

    private Optional<List<Vec3d>> focusToVec3dList() {
        if (focus.isEmpty()) return Optional.empty();
        final List<Vec3d> arrowPosList = focus.get().stream().map(e -> e.getArrow().getPos()).collect(Collectors.toList());
        return Optional.of(arrowPosList);
    }

    private void shield(final AltoClef mod) {
        mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getPlayer().setSprinting(false);
        }

        final Vec3d orig = mod.getPlayer().getEyePos();

        final Optional<List<Vec3d>> optFocusPosList = focusToVec3dList();
        if (optFocusPosList.isEmpty()) throw new IllegalStateException("This should never happen.");
        final Vec3d centroid = centroid(optFocusPosList.get());
        final Rotation newRot = RotationUtils.calcRotationFromVec3d(orig, centroid, new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()));
        mod.getPlayer().setYaw(newRot.getYaw());
        mod.getPlayer().setPitch(newRot.getPitch());
        DebugPrint.println("doing shield task");
        CombatHelper.doShielding(mod);
    }

    public boolean isWorking() {
        return manager.getIterator().hasNext();
    }

    private void right_mouse(final AltoClef mod, final boolean press) {
        if (press && !mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_RIGHT)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true);
        } else if (!press && mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_RIGHT)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        }
    }

    /*private Task fill(final AltoClef mod) {
        if (!placeCandidate.isPresent()) throw new IllegalStateException("placeCandidate should not be empty at this point.");
        if (isFloor(mod, this.placeCandidate.get())) {
            right_mouse(mod, false);
            return null;
        }

        if (!mod.getSlotHandler().equipBlock()) {
            throw new IllegalStateException("Blocks should be present at this point.");
        }
        lookOnTopTarget(mod, this.placeCandidate.get().down());
        //right_mouse(mod, true);
        if (MeteorClientPlace.canPlace(this.placeCandidate.get())) {
            MeteorClientPlace.place(this.placeCandidate.get(), Hand.MAIN_HAND, MinecraftClient.getInstance().player.getInventory().selectedSlot, true, true);
        }

        return null;
    }*/

    private Task fill(final AltoClef mod) {
        this.placeCandidates.forEach(pos -> {
            /*if (isFloor(mod, pos)) {
                right_mouse(mod, false);
                return null;
            }*/

            if (!mod.getSlotHandler().equipBlock()) {
                throw new IllegalStateException("Blocks should be present at this point."); //FIXME: bei der Entscheidung wird nur nach 1 Block im inv gefragt, aber wir setzen hier potentiell mehr
            }
            lookOnTopTarget(mod, pos.down());
            //right_mouse(mod, true);
            if (MeteorClientPlace.canPlace(pos)) {
                MeteorClientPlace.place(pos, Hand.MAIN_HAND, MinecraftClient.getInstance().player.getInventory().selectedSlot, true, true);
            }
        });
        return null;
    }

    private Task placeSide(final AltoClef mod) {
        halt(mod);
        return fill(mod);
    }

    private Task placeBelow(final AltoClef mod) {
        haltMovement(mod);
        mod.getPlayer().jump();
        return fill(mod);
    }

    private boolean hasBlock(final AltoClef mod) {
        return mod.getItemStorage().getBlockTypes().size() > 0;
    }

    private boolean hasFloorBreakTool(final AltoClef mod) {
        final BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().down());
        /*final MiningRequirement requirement = MiningRequirement.getMinimumRequirementForBlock(block);
        if (!requirement.equals(MiningRequirement.HAND)) {
            return mod.getInventoryTracker().miningRequirementMet(MiningRequirement.getMinimumRequirementForBlock(block));
        }*/
        return mod.getSlotHandler().equipBestToolFor(state); //FIXME: fix redundancy
    }

    private boolean isFluid(final BlockState state) {
        Fluid fluid = state.getFluidState().getFluid();
        return fluid.matchesType(Fluids.WATER) || fluid.matchesType(Fluids.FLOWING_WATER) || fluid.matchesType(Fluids.LAVA) || fluid.matchesType(Fluids.FLOWING_LAVA);
    }

    private boolean isFloor(final AltoClef mod, final BlockPos pos) {
        final BlockState state = mod.getWorld().getBlockState(pos);
        return !state.isAir() && !isFluid(state);
    }

    private boolean isFloorThick(final AltoClef mod, final BlockPos pos) {
        return isFloor(mod, pos.down());
    }

    private boolean isPlayerFloorThick(final AltoClef mod) {
        return isFloorThick(mod, mod.getPlayer().getBlockPos().down());
    }

    private void left_mouse(final AltoClef mod, final boolean press) {
        if (press && !mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_LEFT)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, true);
        } else if (!press && mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.CLICK_LEFT)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_LEFT, false);
        }
    }

    private void haltMovement(final AltoClef mod) {
        if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            mod.getPlayer().setSprinting(false);
        }
    }

    private void go(final AltoClef mod) {
        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
        }
    }

    private void halt(final AltoClef mod) {
        haltBaritone(mod);
        haltMovement(mod);
    }

    private boolean isPlayerApproxInHole(final AltoClef mod) {
        final double dist = mod.getPlayer().getPos().distanceTo(new Vec3d(this.breakCandidate.get().getX() + 0.5f, this.breakCandidate.get().getY(), this.breakCandidate.get().getZ() + 0.5f));
        return Double.compare(dist, 0.2d) <= 0;
    }

    private Task dig(final AltoClef mod) {
        haltBaritone(mod);
        if (!breakCandidate.isPresent()) throw new IllegalStateException("breakCandidate should not be empty at this point.");
        if (!isFloor(mod, breakCandidate.get())) {
            left_mouse(mod, false);
            return null;
        }
        final BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos().down());
        if (mod.getSlotHandler().equipBestToolFor(state)) {
            lookOnTopTarget(mod, this.breakCandidate.get());
            left_mouse(mod, true);
        }

        if (!isPlayerApproxInHole(mod)) {
            go(mod); //TODO: make isPlaceFullyInBlock(blockpos)
        } else {
            haltMovement(mod);
        }
        return null;
    }

    private Task jump(final AltoClef mod) {
        halt(mod);
        mod.getPlayer().jump();
        return null;
    }

    /*north: neg z
      south: pos z
      west:  neg x
      east:  pos x
       */
    /*private void fixDiagonal(final TickableTraceInfo info) {
        final Direction hitSide = inverse(info.getTraceInfo().getHitSide());
        if (hitSide.equals(Direction.SOUTH) || hitSide.equals(Direction.NORTH)) {
            final int borderToPlayer = hitSide.equals(Direction.SOUTH) ? this.placeCandidate.get().getZ() : (this.placeCandidate.get().south().getZ());
            //final BlockPos playerPos = hitSide.equals(Direction.SOUTH) ? this.placeCandidate.get().north() : this.placeCandidate.get().south();
            final ArrowEntity arrow = info.getArrow();
            final boolean arrowFastToPositive = Double.compare(arrow.getVelocity().getX(), 0) > 0;
            final double tx = arrowFastToPositive ?
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), this.placeCandidate.get().getX())
                    :
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), this.placeCandidate.get().getX() + 1D);
            final double tz = BowArrowIntersectionTracer
                    .getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), borderToPlayer);

            if (Double.compare(tx, tz) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach W
                    this.placeCandidate = Optional.of(this.placeCandidate.get().west());
                } else {
                    //diagonal nach E
                    this.placeCandidate = Optional.of(this.placeCandidate.get().east());
                }
            }
        }
        if (hitSide.equals(Direction.EAST) || hitSide.equals(Direction.WEST)) {
            final int borderToPlayer = hitSide.equals(Direction.EAST) ? this.placeCandidate.get().getX() : (this.placeCandidate.get().east().getX());
            //final BlockPos playerPos = hitSide.equals(Direction.EAST) ? this.placeCandidate.get().east() : this.placeCandidate.get().east();
            final ArrowEntity arrow = info.getArrow();
            final boolean arrowFastToPositive = Double.compare(arrow.getVelocity().getZ(), 0) > 0;
            final double tz = arrowFastToPositive ?
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), this.placeCandidate.get().getZ())
                    :
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), this.placeCandidate.get().getZ() + 1D);
            final double tx = BowArrowIntersectionTracer
                    .getIntersectionZ(arrow.getVelocity().getX(), arrow.getX(), borderToPlayer);

            if (Double.compare(tz, tx) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach N
                    this.placeCandidate = Optional.of(this.placeCandidate.get().north());
                } else {
                    //diagonal nach S
                    this.placeCandidate = Optional.of(this.placeCandidate.get().south());
                }

            }
        }
    }*/

    private BlockPos fixDiagonal(final Direction hitSide, final ArrowEntity arrow, final BlockPos candidate) {
      /*north: neg z
        south: pos z
        west:  neg x
        east:  pos x
         */
        //final Direction origHitSide = info.getTraceInfo().getHitSide();
        //final Direction hitSide = inverse(origHitSide);

        if (hitSide.equals(Direction.SOUTH) || hitSide.equals(Direction.NORTH)) {
            final int borderToPlayer = hitSide.equals(Direction.SOUTH) ? candidate.getZ() : (candidate.south().getZ());
            //final BlockPos playerPos = hitSide.equals(Direction.SOUTH) ? this.placeCandidate.get().north() : this.placeCandidate.get().south();
            //final ArrowEntity arrow = info.getArrow();
            final boolean isArrowApproxStraight = Double.compare(arrow.getVelocity().getX(), 0.1) < 0 && Double.compare(arrow.getVelocity().getX(), -0.1) > 0;
            if (isArrowApproxStraight) return candidate;
            final boolean arrowFastToPositive = Double.compare(arrow.getVelocity().getX(), 0) > 0;
            final double tx = arrowFastToPositive ?
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), candidate.getX())
                    :
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), candidate.getX() + 1D);
            final double tz = BowArrowIntersectionTracer
                    .getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), borderToPlayer);

            if (Double.compare(tx, tz) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach W
                    return candidate.west();
                } else {
                    //diagonal nach E
                    return candidate.east();
                }
            }
        } else if (hitSide.equals(Direction.EAST) || hitSide.equals(Direction.WEST)) {
          /*north: neg z
            south: pos z
            west:  neg x
            east:  pos x
             */
            final int borderToPlayer = hitSide.equals(Direction.EAST) ? candidate.getX() : (candidate.east().getX());
            //final BlockPos playerPos = hitSide.equals(Direction.EAST) ? this.placeCandidate.get().east() : this.placeCandidate.get().east();
            //final ArrowEntity arrow = info.getArrow();
            final boolean isArrowApproxStraight = Double.compare(arrow.getVelocity().getZ(), 0.1) < 0 && Double.compare(arrow.getVelocity().getZ(), -0.1) > 0;
            if (isArrowApproxStraight) return candidate;
            final boolean arrowFastToPositive = Double.compare(arrow.getVelocity().getZ(), 0) > 0;
            final double tz = arrowFastToPositive ?
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.getZ())
                    :
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.getZ() + 1D);
            final double tx = BowArrowIntersectionTracer
                    .getIntersectionZ(arrow.getVelocity().getX(), arrow.getX(), borderToPlayer);

            if (Double.compare(tz, tx) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach N
                    return candidate.north();
                } else {
                    //diagonal nach S
                    return candidate.south();
                }
            }
        } else {
            final int borderToPlayer = hitSide.equals(Direction.UP) ? candidate.getY() : (candidate.up().getY());
            //final ArrowEntity arrow = info.getArrow();
            final boolean isArrowApproxStraight = Double.compare(arrow.getVelocity().getX(), 0.1) < 0
                                                && Double.compare(arrow.getVelocity().getX(), -0.1) > 0
                                                && Double.compare(arrow.getVelocity().getZ(), 0.1) < 0
                                                && Double.compare(arrow.getVelocity().getZ(), -0.1) > 0;
            if (isArrowApproxStraight) return candidate;
            //final boolean arrowFastToPositive = Double.compare(arrow.getVelocity().getY(), 0) > 0;
            final double[] ty = BowArrowIntersectionTracer.getIntersectionY(arrow.getVelocity().getY(), arrow.getY(), borderToPlayer);
          /*north: neg z
            south: pos z
            west:  neg x
            east:  pos x
             */
            final Direction hitX = Double.compare(arrow.getVelocity().getX(), 0) < 0 ? Direction.EAST : Direction.WEST;
            final Direction hitZ = Double.compare(arrow.getVelocity().getX(), 0) < 0 ? Direction.SOUTH : Direction.NORTH;
            final double tx = hitX.equals(Direction.EAST) ?
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), candidate.east().getX())
                    :
                    BowArrowIntersectionTracer.getIntersectionX(arrow.getVelocity().getX(), arrow.getX(), candidate.getX());
            final double tz = hitZ.equals(Direction.SOUTH) ?
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.south().getZ())
                    :
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.getZ());

            if (Double.compare(tx, ty[0]) < 0 || Double.compare(tz, ty[0]) < 0) {
                return candidate;
            }
            return neighbour(hitX, neighbour(hitZ, candidate));

            /*if (Double.compare(tz, tx) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach N
                    return candidate.north();
                } else {
                    //diagonal nach S
                    return candidate.south();
                }
            }*/

            /*final double tz = arrowFastToPositive ?
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.getZ())
                    :
                    BowArrowIntersectionTracer.getIntersectionZ(arrow.getVelocity().getZ(), arrow.getZ(), candidate.getZ() + 1D);
            final double tx = BowArrowIntersectionTracer
                    .getIntersectionZ(arrow.getVelocity().getX(), arrow.getX(), borderToPlayer);

            if (Double.compare(tz, tx) > 0) {
                if (arrowFastToPositive) {
                    //diagonal nach N
                    return candidate.north();
                } else {
                    //diagonal nach S
                    return candidate.south();
                }
            }*/
        }
        return candidate;
    }

    private void clear(final AltoClef mod) {
        if (this.focus.isPresent()) {
            haltMovement(mod);
            left_mouse(mod, false);
            right_mouse(mod, false);
            CombatHelper.undoShielding(mod);
            mod.getSlotHandler().forceDeequipRightClickableItem();
        }
        this.focus = Optional.empty();
        this.strategy = Strategy.NONE;
        this.breakCandidate = Optional.empty();
        this.placeCandidates.clear();
        //left_mouse(mod, false);
    }

    private void haltBaritone(final AltoClef mod) {
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
        }
    }

    private BlockPos neighbour(final Direction hitSide, final BlockPos origin) {
        if (hitSide == null) return origin;
        return hitSide.equals(Direction.NORTH) ? origin.north() :
                hitSide.equals(Direction.EAST) ? origin.east() :
                hitSide.equals(Direction.SOUTH) ? origin.south() :
                hitSide.equals(Direction.WEST) ? origin.west() :
                hitSide.equals(Direction.UP) ? origin.up() : origin.down();
    }

    private boolean hasFloorOnSide(final AltoClef mod, final Direction hitSide) {
        if (hitSide == null) return false; //throw new IllegalStateException("hitSide should not be null at this point.");
        if (hitSide.equals(Direction.UP) || hitSide.equals(Direction.DOWN)) return false;
        final BlockPos origin = mod.getPlayer().getBlockPos().down();
        final BlockPos target = neighbour(hitSide, origin);
        return isFloor(mod, target);
    }

    private boolean playerAtJumpableSpot(final AltoClef mod) {
        final BlockPos above = mod.getPlayer().getBlockPos().up();
        //TODO: benachbarte Blöcke können auch stören
        return mod.getWorld().getBlockState(above).isAir();
    }

    private Direction inverse(final Direction old) {
        if (old == null) return null;
        switch (old) {
            case NORTH: return Direction.SOUTH;
            case SOUTH: return Direction.NORTH;
            case EAST: return Direction.WEST;
            case WEST: return Direction.EAST;

            //CSAY is not inversed
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
        }
        throw new IllegalStateException("what? how?");
    }

    public void onTick(AltoClef mod) {
        manager.tick(mod);
        final Iterator<List<TickableTraceInfo>> hitIterator = manager.getIterator();
        if (hitIterator.hasNext()) {
            final List<TickableTraceInfo> infoList = hitIterator.next();
            //sanity check
            if (infoList.size() < 1) throw new IllegalStateException("infoList should never be empty");
            final TickableTraceInfo info0 = infoList.get(0);
            final Optional<List<TickableTraceInfo>> followingInfoList = hitIterator.hasNext() ? Optional.of(hitIterator.next()) : Optional.empty();
            if (followingInfoList.isPresent() && followingInfoList.get().size() < 1) throw new IllegalStateException("followingInfoList should never be empty");
            //final Optional<TickableTraceInfo> followingInfo0 = followingInfoList.isPresent() ? Optional.of(followingInfoList.get().get(0)) : Optional.empty();
            final boolean allFromSameSide = infoList.stream().filter(e ->
                    infoList.get(0).getTraceInfo().getBodyHitDetail() != null
                            &&
                    infoList.get(0).getTraceInfo().getHitSide() != null
                            &&
                    infoList.get(0).getTraceInfo().getBodyHitDetail().equals(e.getTraceInfo().getBodyHitDetail())
                            &&
                    infoList.get(0).getTraceInfo().getHitSide().equals(e.getTraceInfo().getHitSide())
            ).count() == infoList.size();
            /*final boolean allLow = infoList.stream().filter(e ->
                    e.getTraceInfo().getBodyHitDetail() != null
                            &&
                    e.getTraceInfo().getBodyHitDetail().equals(BodyHitDetail.LOW)
            ).count() == infoList.size();*/
            final boolean allHigh = infoList.stream().filter(e ->
                    e.getTraceInfo().getBodyHitDetail() != null
                            &&
                            e.getTraceInfo().getBodyHitDetail().equals(BodyHitDetail.HIGH)
            ).count() == infoList.size();
            final boolean allNonVertical = infoList.stream().filter(e ->
                    e.getTraceInfo().getHitSide() != null
                            &&
                    !e.getTraceInfo().getHitSide().equals(Direction.UP)
                            &&
                    !e.getTraceInfo().getHitSide().equals(Direction.DOWN)
            ).count() == infoList.size();
            final boolean isShieldStrategy = this.strategy.equals(Strategy.SHIELD);
            final boolean hasEnoughShieldingTime = info0.getRemainingTicks() > 5;
            final boolean isBlockInInv = hasBlock(mod);
            final boolean isSideFloored = hasFloorOnSide(mod, info0.getTraceInfo().getHitSide());
            final boolean isJumpableSpot = playerAtJumpableSpot(mod);
            final boolean isFloorThickUnderPlayer = isPlayerFloorThick(mod);
            final boolean canBreakFloor = hasFloorBreakTool(mod);

            System.out.println("isShieldStrategy: " + isShieldStrategy);
            System.out.println("hasEnoughShieldingTime: " + hasEnoughShieldingTime);
            System.out.println("isBlockInInv: " + isBlockInInv);
            System.out.println("isSideFloored: " + isSideFloored);
            System.out.println("isJumpableSpot: " + isJumpableSpot);
            System.out.println("isFloorThickUnderPlayer: " + isFloorThickUnderPlayer);
            System.out.println("canBreakFloor: " + canBreakFloor);

            if (allFromSameSide && (isShieldStrategy || hasEnoughShieldingTime)) {
                this.strategy = Strategy.SHIELD;
            } else if (allFromSameSide && isBlockInInv/* && isSideFloored*/) {
                if (this.placeCandidates.isEmpty()) {
                    infoList.forEach(e -> {
                        final Direction dir = inverse(e.getTraceInfo().getHitSide());
                        final BlockPos origPlayer = mod.getPlayer().getBlockPos();
                        final BlockPos heightFixed = allHigh ? origPlayer.up() : origPlayer; //FIXME: DAS HIER MUSS VORHER SCHON GEMACHT WERDEN
                        //final BlockPos heightFixed2 = dir.equals(Direction.UP) ? heightFixed1.up() : heightFixed1; //FIXME: DAS HIER MUSS VORHER SCHON GEMACHT WERDEN
                        final BlockPos diagonalFixed = fixDiagonal(dir, e.getArrow(), neighbour(dir, heightFixed));
                        this.placeCandidates.add(diagonalFixed);
                    });
                    //final Direction inversed = inverse(info0.getTraceInfo().getHitSide());
                    /*this.placeCandidate = Optional.of(neighbour(inversed, mod.getPlayer().getBlockPos()));
                    fixDiagonal(info0); //TODO: not only info0 but all
                    if (allHigh) {
                        this.placeCandidate = Optional.of(this.placeCandidate.get().up());
                    }*/
                }
                this.strategy = Strategy.PLACE_SIDE;
            } else if (hasBlock(mod) && isJumpableSpot) {
                if (this.placeCandidates.isEmpty()) {
                    this.placeCandidates.add(mod.getPlayer().getBlockPos());
                }
                this.strategy = Strategy.PLACE_BELOW;
            /*} else if (allHigh && allHigh && isFloorThickUnderPlayer && canBreakFloor) {
                if (this.breakCandidate.isEmpty()) {
                    this.breakCandidate = Optional.of(mod.getPlayer().getBlockPos().down());
                }
                this.strategy = Strategy.DIG;*/
            } else if (allNonVertical && isJumpableSpot) {
                this.strategy = Strategy.JUMP;
            } else {
                this.strategy = Strategy.RUN;
            }
            System.out.println("Strategy: " + this.strategy.toString() + " delta: " + info0.getRemainingTicks());
            this.focus = Optional.of(infoList);
        } else {
            clear(mod);
        }

        switch (this.strategy) {
            case RUN: run(mod); break;
            case SHIELD: shield(mod); break;
            case PLACE_SIDE: placeSide(mod); break;
            case PLACE_BELOW: placeBelow(mod); break;
            case DIG: dig(mod); break;
            case JUMP: jump(mod); break;
        }
        //return null;
    }


    public void onStop() {
        this.manager.stop();
    }

    protected String toDebugString() {
        return this.getClass().getCanonicalName();
    }
}
