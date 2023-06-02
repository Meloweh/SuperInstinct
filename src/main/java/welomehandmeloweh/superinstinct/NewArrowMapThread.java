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
public class NewArrowMapThread {
    private final TickableTreeMap tracked;
    private final List<ArrowEntity> checked;
    private Vec3d target;

    private static final int DIST = 70;
    private static final int ENDTICK = 500;

    private long deltas;
    private long fleeDelta;
    private BetterBlockPos prevDest;
    private final Map<ArrowEntity, SingleArrowEscapeRouteSearch> asyncMovingCollisionList;
    private float fleesTo;

    public NewArrowMapThread(final AltoClef mod) {
        this.asyncMovingCollisionList = new HashMap<>();
        this.tracked = new TickableTreeMap();
        this.checked = new ArrayList<>();
        setNewTarget(mod.getPlayer().getYaw());
        this.deltas = -1;
        this.fleeDelta = -1;
    }

    private void clear() {
        this.tracked.clear();
        this.checked.clear();
    }

    public void setNewTarget(final Vec3d target) {
        this.target = target;
        clear();
    }

    public void setNewTarget(final float yaw) {
        final Vec3d newTarget = YawHelper.yawToVec(yaw).multiply(DIST);
        setNewTarget(newTarget);
    }

    private void garbageCollection(final ArrowEntity arrow) {
        if (this.asyncMovingCollisionList.containsKey(arrow)) {
            this.asyncMovingCollisionList.remove(arrow);
        }
        if (this.checked.contains(arrow)) {
            this.checked.remove(arrow);
        }
    }

    public void watchArrows(final AltoClef mod, final SimMovementState sms) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        for (final ArrowEntity arrow : arrows) {
            if (!checked.contains(arrow) && !arrow.verticalCollision && !arrow.horizontalCollision && arrow.prevX != arrow.getX() && arrow.prevZ != arrow.getZ() && arrow.prevY != arrow.getY()) {
                /*final long curr = System.nanoTime();
                final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(arrow, mod.getPlayer().getBoundingBox(),
                        mod.getPlayer().getVelocity(), ENDTICK, mod.getPlayer().getPos(), target, mod.getWorld(), sms);
                DebugPrint.println("Nanos: " + (System.nanoTime()-curr));
                if (traceResult == null) {
                    //tracked.put(TraceResult.EXPIRED(arrow, mod.getPlayer().getBoundingBox()), arrow);
                } else if (traceResult.willPiercePlayer()) {
                    tracked.put(traceResult, arrow);
                }
                checked.add(arrow);*/
                //BowArrowIntersectionTracer.calculateCollisionThread(arrow, mod.getPlayer().getBoundingBox(),
                //        mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), target, mod.getWorld(), sms, tracked);
            }

            if (arrow.verticalCollision || arrow.horizontalCollision || arrow.prevX == arrow.getX() || arrow.prevZ == arrow.getZ() || arrow.prevY == arrow.getY()) {
                garbageCollection(arrow);
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
        if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
            if (mod.getClientBaritone().getPathingBehavior().getCurrent() != null
                    && mod.getClientBaritone().getPathingBehavior().getCurrent().getPath() != null
                    && mod.getClientBaritone().getPathingBehavior().getCurrent().getPath().movements().size() > 0) {
                final int index = mod.getClientBaritone().getPathingBehavior().getCurrent().getPosition();
                final IMovement mov = mod.getClientBaritone().getPathingBehavior().getCurrent().getPath().movements().get(index);
                //DebugPrint.println(mov.getSrc() + " ==> " + mov.getDest() + " ::: " + mov.getDirection());

                if ((prevDest == null || !prevDest.equals(mov.getDest())) && this.fleeDelta < 0) {
                    //DebugPrint.println(mov.getSrc() + " ==> " + mov.getDest() + " ::: " + mov.getDirection());
                    //setNewTarget(new Vec3d(mov.getDest().getX(), mov.getDest().getY(), mov.getDest().getZ()));
                    setNewTarget(mod.getPlayer().getYaw());
                    this.prevDest = mov.getDest();
                }
            }
        } else if (Float.compare(mod.getPlayer().prevYaw, mod.getPlayer().getYaw()) != 0) {
            setNewTarget(mod.getPlayer().getYaw());
            DebugPrint.println("yaw moved.");
        }

        if (this.fleeDelta-- < 0) {
            watchArrows(mod, SimMovementState.UNDEFINED); //TODO: if new arrow very close and shielding, then focus new arrow
        } else {
            if (this.fleeDelta == 0) {
                stop(mod); //TODO: interferes with all movements at all times
            }

            if (!hasCollisionsForNotMoving(mod)) {
                DebugPrint.println("!hasCollisionsForNotMoving(mod)");// STOPSHIP: 23.11.22 t
                stop(mod);
            } else {
                watchArrows(mod, SimMovementState.UNDEFINED); //TODO: if new arrow very close and shielding, then focus new arrow
            }
        }

        /*if (!mod.getClientBaritone().getPathingBehavior().isPathing() && tracked.size() < 1) {
            mod.getClientBaritone().getCommandManager().execute("stop");
        }*/

        if (this.asyncMovingCollisionList.size() > 1) {
            final List<Float> yaws = this.asyncMovingCollisionList.values().iterator().next().getEscapeYaws();
            if (yaws.size() > 0) {
                System.out.println("yaw size big");
                if (yaws.stream().filter(e -> Float.compare(e, this.fleesTo) == 0).count() == 0) {
                    final int chosenIndex = (new Random()).nextInt(yaws.size());
                    setNewTarget(yaws.get(chosenIndex));
                    this.fleesTo = yaws.get(chosenIndex);
                }
                if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                }
                go(mod);
            }
        } else {
            this.fleesTo = Float.NaN;
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

            if (mod.getClientBaritone().getPathingBehavior().isPathing()) {
                mod.getClientBaritone().getPathingBehavior().softCancelIfSafe();
                go(mod);
                /*mod.getExtraBaritoneSettings().setInteractionPaused(true);
                mod.getClientBaritone().getPathingBehavior().requestPause();
                mod.getClientBaritone().getPathingBehavior().cancelEverything();*/
                //mod.getClientBaritone().getCommandManager().execute("stop");
                //mod.getTaskRunner().disable();
                //mod.getTaskRunner().
                //mod.getClientBaritone().getPathingBehavior().forceCancel();
                //mod.run
                //mod.getTaskRunner().disable();
                //mod.runUserTask(new IdleTask());
                //mod.getClientBaritone().getCommandManager().execute("pause");
            }


            final Vec3d orig = mod.getPlayer().getEyePos();
            final Rotation newRot = RotationUtils.calcRotationFromVec3d(orig, firstInfo.get(0).getArrow().getPos(), new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()));
            if (shieldable || this.deltas-- >= 0) {
                if (mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
                    mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
                    mod.getPlayer().setSprinting(false);
                }

                mod.getPlayer().setYaw(newRot.getYaw());
                mod.getPlayer().setPitch(newRot.getPitch());
                DebugPrint.println("doing shield task");
                CombatHelper.doShielding(mod);
            } else if (this.fleeDelta < 0) { // shouldn't be called multiple times if escape route found
                if (CombatHelper.isHoldingShield()) {
                    CombatHelper.undoShielding(mod);
                }
                DebugPrint.println("in flee search");

                if (!this.asyncMovingCollisionList.containsKey(firstInfo.get(0).getArrow())) {
                    this.asyncMovingCollisionList.put(firstInfo.get(0).getArrow(), new SingleArrowEscapeRouteSearch(firstInfo.get(0).getArrow(), mod));
                }
                this.fleeDelta = firstDelta;

                /*if (1 == 1) {
                    mod.getPlayer().setYaw(0);
                    go(mod);
                    return null;
                }

                //TODO: should choose random dirs but for this test i won't
                float newYaw;
                for (int i = 0; i < 3; i++) {
                    newYaw = newRot.getYaw() + 90 % 180;
                    mod.getPlayer().setYaw(newYaw);
                    setNewTarget(newYaw);
                    watchArrows(mod, SimMovementState.SIM_MOVE);
                    if (tracked.size() < 1) {
                        go(mod);
                        this.fleeDelta = firstDelta;
                        return null;
                    }
                }

                newYaw = mod.getPlayer().getYaw() + 45 % 180;
                mod.getPlayer().setYaw(newYaw);
                setNewTarget(newYaw);
                watchArrows(mod, SimMovementState.SIM_MOVE);
                if (tracked.size() < 1) {
                    go(mod);
                    this.fleeDelta = firstDelta;
                    return null;
                }

                for (int i = 0; i < 3; i++) {
                    newYaw = newRot.getYaw() + 90 % 180;
                    mod.getPlayer().setYaw(newYaw);
                    setNewTarget(newYaw);
                    watchArrows(mod, SimMovementState.SIM_MOVE);
                    if (tracked.size() < 1) {
                        go(mod);
                        this.fleeDelta = firstDelta;
                        return null;
                    }
                }*/

            }
        } else {
            if (CombatHelper.isHoldingShield()) {
                CombatHelper.undoShielding(mod);
            }
            //if (this.fleeDelta >= 0) go(mod);
        }
        return null;
    }

}
