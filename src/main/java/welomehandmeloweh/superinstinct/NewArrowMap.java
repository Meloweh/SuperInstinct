/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.movement.IMovement;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

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
public class NewArrowMap {
    private final TickableTreeMap tracked;
    private final List<ArrowEntity> checked;
    private Vec3d target;

    private static final int DIST = 70;
    private static final int ENDTICK = 500;

    private long deltas;
    private long fleeDelta;
    private BetterBlockPos prevDest;
    private float fleeYaw;

    private class TraceResultThreadContainerWithYaw {
        final ThreadContainer<TraceResult> container;
        final float yaw;

        public TraceResultThreadContainerWithYaw(final ThreadContainer<TraceResult> container, final float yaw) {
            this.container = container;
            this.yaw = yaw;
        }

        public ThreadContainer<TraceResult> getContainer() {
            return container;
        }

        public float getYaw() {
            return yaw;
        }
    }

    final List<TraceResultThreadContainerWithYaw> routes;

    public NewArrowMap(final AltoClef mod) {
        this.tracked = new TickableTreeMap();
        this.checked = new ArrayList<>();
        this.deltas = -1;
        this.fleeDelta = -1;
        this.routes = new ArrayList<>();
        setNewTarget(mod.getPlayer().getYaw(), mod.getPlayer().getPos());
    }

    private void clear() {
        this.tracked.clear();
        this.checked.clear();
        this.routes.clear();
    }

    public void setNewTarget(final Vec3d target) {
        this.target = target;
        clear();
    }

    public void setNewTarget(final float yaw, final Vec3d startPos) {
        final Vec3d newTarget = YawHelper.yawToVec(yaw).multiply(DIST).add(startPos);
        setNewTarget(newTarget);
    }

    public void watchArrows(final AltoClef mod, final SimMovementState sms) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        for (final ArrowEntity arrow : arrows) {
            if (!checked.contains(arrow) && !arrow.verticalCollision && !arrow.horizontalCollision && arrow.prevX != arrow.getX() && arrow.prevZ != arrow.getZ() && arrow.prevY != arrow.getY()) {
                final long nanos = System.nanoTime();
                final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                        mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), target, mod.getWorld(), sms);
                if (traceResult != null && traceResult.willPiercePlayer()) {
                    tracked.put(traceResult, arrow);
                }
                checked.add(arrow);
                System.out.println("routes size: " + this.routes.size() + " nanos: " + (System.nanoTime()-nanos));
            }
        }
    }

    public void watchArrowsThread(final AltoClef mod, final SimMovementState sms) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        for (final ArrowEntity arrow : arrows) {
            if (!checked.contains(arrow) && !arrow.verticalCollision && !arrow.horizontalCollision && arrow.prevX != arrow.getX() && arrow.prevZ != arrow.getZ() && arrow.prevY != arrow.getY()) {
                final long nanos = System.nanoTime();
                final Thread threadCollisionCheck = new Thread(() -> {
                    final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                            mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), target, mod.getWorld(), sms);
                    if (traceResult != null && traceResult.willPiercePlayer()) {
                        tracked.put(traceResult, arrow);
                    }
                    checked.add(arrow);
                }); threadCollisionCheck.start();

                //final Vec3d orig = mod.getPlayer().getEyePos();
                //final Rotation newRot = RotationUtils.calcRotationFromVec3d(orig, arrow.getPos(), new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()));
                final float yaw = mod.getPlayer().getYaw();
                final Thread threadEscapeRoute1 = new Thread(() -> {
                    float newYaw = yaw + 90 % 180;
                    final Vec3d newTarget = YawHelper.yawToVec(newYaw).multiply(DIST).add(mod.getPlayer().getPos());
                    final ThreadContainer<TraceResult> threadContainer = new ThreadContainer<>();
                    threadContainer.setTraceResult(BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                            mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), newTarget, mod.getWorld(), SimMovementState.SIM_MOVE));
                    final TraceResultThreadContainerWithYaw stuff = new TraceResultThreadContainerWithYaw(threadContainer, newYaw);
                    routes.add(stuff);
                }); threadEscapeRoute1.start();

                final Thread threadEscapeRoute2 = new Thread(() -> {
                    float newYaw = yaw + 180 % 180;
                    final Vec3d newTarget = YawHelper.yawToVec(newYaw).multiply(DIST).add(mod.getPlayer().getPos());
                    final ThreadContainer<TraceResult> threadContainer = new ThreadContainer<>();
                    threadContainer.setTraceResult(BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                            mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), newTarget, mod.getWorld(), SimMovementState.SIM_MOVE));
                    final TraceResultThreadContainerWithYaw stuff = new TraceResultThreadContainerWithYaw(threadContainer, newYaw);
                    routes.add(stuff);
                }); threadEscapeRoute2.start();

                final Thread threadEscapeRoute3 = new Thread(() -> {
                    float newYaw = yaw + 270 % 180;
                    final Vec3d newTarget = YawHelper.yawToVec(newYaw).multiply(DIST).add(mod.getPlayer().getPos());
                    final ThreadContainer<TraceResult> threadContainer = new ThreadContainer<>();
                    threadContainer.setTraceResult(BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                            mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), newTarget, mod.getWorld(), SimMovementState.SIM_MOVE));
                    final TraceResultThreadContainerWithYaw stuff = new TraceResultThreadContainerWithYaw(threadContainer, newYaw);
                    routes.add(stuff);
                }); threadEscapeRoute3.start();

                try {
                    threadCollisionCheck.join();
                    threadEscapeRoute1.join();
                    threadEscapeRoute2.join();
                    threadEscapeRoute3.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("routes size: " + this.routes.size() + " nanos: " + (System.nanoTime()-nanos));
            }
        }
    }

    public boolean hasCollisionsForNotMoving(final AltoClef mod) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        /*for (final ArrowEntity arrow : arrows) {
            if (!arrow.verticalCollision && !arrow.horizontalCollision && arrow.prevX != arrow.getX() && arrow.prevZ != arrow.getZ() && arrow.prevY != arrow.getY()) {
                DebugPrint.println("Has arrow");
                final long curr = System.nanoTime();
                final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                        mod.getPlayer().getVelocity(), ENDTICK, mod.getPlayer().getPos(), target, mod.getWorld(), SimMovementState.SIM_STAND);
                DebugPrint.println("Nanos: " + (System.nanoTime()-curr));
                if (traceResult == null) {
                    return true;
                } else if (traceResult.willPiercePlayer()) {
                    return true;
                }
            }
        }*/
        return false;
    }

    private void go(final AltoClef mod) {
        DebugPrint.println("go?");
        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            DebugPrint.println("go.");
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
        }
    }

    private void stop(final AltoClef mod) {
        DebugPrint.println("stop?");
        if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            DebugPrint.println("stop.");
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            mod.getPlayer().setSprinting(false);
        }
    }

    public boolean willCollide() {
        return this.tracked.size() > 0;
    }

    public Task onTick(AltoClef mod) {
        //debug only
        /*if (1 == 1) {
            mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
            //mod.getPlayer().setYaw(this.fleeYaw);
            go(mod);
            return null;
        }*/

        if (this.fleeDelta > 0) {
            mod.getClientBaritone().getCommandManager().execute("pause");
            go(mod);
        } else if (this.fleeDelta == 0) {
            mod.getClientBaritone().getCommandManager().execute("stop");
        }

        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            if (mod.getClientBaritone().getPathingBehavior().getCurrent() != null
                    && mod.getClientBaritone().getPathingBehavior().getCurrent().getPath() != null
                    && mod.getClientBaritone().getPathingBehavior().getCurrent().getPath().movements().size() > 0) {
                final int index = mod.getClientBaritone().getPathingBehavior().getCurrent().getPosition();
                final IMovement mov = mod.getClientBaritone().getPathingBehavior().getCurrent().getPath().movements().get(index);
                //DebugPrint.println(mov.getSrc() + " ==> " + mov.getDest() + " ::: " + mov.getDirection());

                if (prevDest == null || !prevDest.equals(mov.getDest())) {
                    //DebugPrint.println(mov.getSrc() + " ==> " + mov.getDest() + " ::: " + mov.getDirection());
                    //setNewTarget(new Vec3d(mov.getDest().getX(), mov.getDest().getY(), mov.getDest().getZ()));
                    setNewTarget(mod.getPlayer().getYaw(), mod.getPlayer().getPos());
                    this.prevDest = mov.getDest();
                }
            }
        } else if (Float.compare(mod.getPlayer().prevYaw, mod.getPlayer().getYaw()) != 0) {
            setNewTarget(mod.getPlayer().getYaw(), mod.getPlayer().getPos());
            DebugPrint.println("yaw moved.");
        }

        if (this.fleeDelta-- < 0) {
            watchArrows(mod, SimMovementState.UNDEFINED); //TODO: if new arrow very close and shielding, then focus new arrow
        } else {
            if (this.fleeDelta == 0) {
                stop(mod); //TODO: interferes with all movements at all times
            }

            /*if (!hasCollisionsForNotMoving(mod)) {
                DebugPrint.println("!hasCollisionsForNotMoving(mod)");// STOPSHIP: 23.11.22 t
                stop(mod);
            } else {
                watchArrows(mod, SimMovementState.UNDEFINED); //TODO: if new arrow very close and shielding, then focus new arrow
            }*/
        }

        if (this.fleeDelta > 0) {
            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
            }
            mod.getPlayer().setYaw(this.fleeYaw);
            go(mod);
        }

        if (tracked.size() > 0) {
            final Iterator<List<TickableTraceInfo>> iterator = tracked.getIterator();
            final List<TickableTraceInfo> firstInfo = iterator.next();
            final long firstElapsed = firstInfo.get(0).getTick();
            final long firstDelta = firstInfo.get(0).getTraceInfo().getPiercingEntryHitTick() - firstElapsed;

            boolean shieldable = CombatHelper.hasShield(mod) || CombatHelper.isShieldEquipped();
            DebugPrint.println("CombatHelper.hasShield(mod): " + shieldable);

            if (shieldable) {
                shieldable = firstInfo.stream().filter(e -> e.getTraceInfo().getHitSide() != null && e.getTraceInfo().getHitSide().equals(firstInfo.get(0).getTraceInfo().getHitSide()) && e.getTraceInfo().getBodyHitDetail() != null && e.getTraceInfo().getBodyHitDetail().equals(firstInfo.get(0).getTraceInfo().getBodyHitDetail())).count() == firstInfo.size();
            }

            DebugPrint.println("firstInfo.stream().filter(e: " + shieldable);

            if (shieldable) {
                shieldable = firstDelta > 10 && firstDelta <= 14;
            }

            DebugPrint.println("firstDelta > 10 && firstDelta <= 14: " + shieldable);

            if (iterator.hasNext()) {
                final List<TickableTraceInfo> secondInfo = iterator.next();
                final long secondElapsed = secondInfo.get(0).getTick();
                final long secondDelta = secondInfo.get(0).getTraceInfo().getPiercingEntryHitTick() - secondElapsed;

                if (shieldable) {
                    shieldable = secondDelta > 20 || secondInfo.stream().filter(e -> e.getTraceInfo().getHitSide().equals(firstInfo.get(0)) && e.getTraceInfo().getBodyHitDetail().equals(firstInfo.get(0).getTraceInfo().getBodyHitDetail())).count() == secondInfo.size();
                }
                DebugPrint.println("secondDelta > 20 || secondInfo.stream(): " + shieldable);
            }
            tracked.tick();
            DebugPrint.println("shieldable: " + shieldable);

            shieldable = false;

            if (shieldable) {
                this.deltas = firstDelta;
            }

            /*if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                go(mod);

                //mod.getExtraBaritoneSettings().setInteractionPaused(true);
                //mod.getClientBaritone().getPathingBehavior().requestPause();
                //mod.getClientBaritone().getPathingBehavior().cancelEverything();
                //mod.getClientBaritone().getCommandManager().execute("stop");
                //mod.getTaskRunner().disable();
                //mod.getTaskRunner().
                //mod.getClientBaritone().getPathingBehavior().forceCancel();
                //mod.run
                //mod.getTaskRunner().disable();
                //mod.runUserTask(new IdleTask());
                //mod.getClientBaritone().getCommandManager().execute("pause");
            }*/

            if (shieldable || this.deltas-- >= 0) {
                if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
                    mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
                    mod.getPlayer().setSprinting(false);
                }

                final Vec3d orig = mod.getPlayer().getEyePos();
                final Rotation newRot = RotationUtils.calcRotationFromVec3d(orig, firstInfo.get(0).getArrow().getPos(), new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()));
                mod.getPlayer().setYaw(newRot.getYaw());
                mod.getPlayer().setPitch(newRot.getPitch());
                DebugPrint.println("doing shield task");
                CombatHelper.doShielding(mod);
            } else if (this.fleeDelta < 0) { // shouldn't be called multiple times if escape route found
                if (CombatHelper.isHoldingShield()) {
                    CombatHelper.undoShielding(mod);
                }
                DebugPrint.println("in flee search");
                //go(mod);
                /*if (1 == 1) {
                    mod.getPlayer().setYaw(0);
                    return null;
                }*/

                final List<TraceResultThreadContainerWithYaw> pierceCandidates = this.routes.stream().filter(e -> e.getContainer().getTraceResult().get().willPiercePlayer()).collect(Collectors.toList());
                if (pierceCandidates.size() > 0) {
                    final int index = (new Random()).nextInt(pierceCandidates.size());
                    final TraceResultThreadContainerWithYaw e = pierceCandidates.get(index);
                    System.out.println("setnewyaw: " + e.getYaw());
                    this.fleeYaw = e.getYaw();
                    setNewTarget(this.fleeYaw, mod.getPlayer().getPos());
                    this.fleeDelta = firstDelta;
                }
                /*for (final TraceResultThreadContainerWithYaw e : this.routes) {
                    if (!e.getContainer().getTraceResult().get().willPiercePlayer()) {
                        System.out.println("setnewyaw: " + e.getYaw());
                        this.fleeYaw = e.getYaw();
                        setNewTarget(this.fleeYaw, mod.getPlayer().getPos());
                        this.fleeDelta = firstDelta;
                        break;
                    }
                }*/
            }
        } else {
            if (CombatHelper.isHoldingShield()) {
                CombatHelper.undoShielding(mod);
            }
            //if (this.fleeDelta >= 0) go(mod);
        }
        this.routes.clear();
        return null;
    }

}
