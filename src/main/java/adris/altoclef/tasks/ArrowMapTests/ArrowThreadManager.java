package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
public class ArrowThreadManager {
    final static int THREAD_MAX_COUNT = 10;
    final BlockingQueue<TraceResult> resultQueue;
    final Thread threads[];
    boolean active;
    final List<ArrowEntity> checked;
    final BlockingQueue<TracerParams> paramQueue;
    final TickableTreeMap map;

    public ArrowThreadManager() {
        this.resultQueue = new LinkedBlockingQueue<>();
        this.threads = new Thread[THREAD_MAX_COUNT];
        this.active = true;
        this.checked = new LinkedList<>();
        this.paramQueue = new LinkedBlockingQueue<>();
        this.map = new TickableTreeMap();

        for (int i = 0; i < THREAD_MAX_COUNT; i++) {
            this.threads[i] = new Thread(() -> {
                while (this.active) {
                    if (!this.paramQueue.isEmpty()) {
                        try {
                            final TracerParams params = this.paramQueue.take();
                            final TraceResult traceResult = BowArrowIntersectionTracer.calculateCollision(params.getArrow(), params.getAabb(),
                                    params.getVel(), params.getPos(), params.getTarget(), params.getWorld(), params.getSms());
                            System.out.println("calculateCollision did a thing");
                            if (traceResult.willPiercePlayer()) {
                                resultQueue.put(traceResult);
                                System.out.println("resultQueue.put");
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }); this.threads[i].start();
        }
    }

    /*public Optional<List<TraceResult>> generateResults() throws InterruptedException {
        if (this.resultQueue.isEmpty()) return Optional.empty();
        final List<TraceResult> outResults = new LinkedList<>();
        while (!this.resultQueue.isEmpty()) {
            outResults.add(this.resultQueue.take());
        }
        return Optional.of(outResults);
    }*/

    private void moveResultsToMap() {
        try {
            while (!this.resultQueue.isEmpty()) {
                System.out.println("!this.resultQueue.isEmpty()");
                final TraceResult traceResult = this.resultQueue.take();
                map.put(traceResult, traceResult.getArrow());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class TracerParams {
        final private ArrowEntity arrow;
        final private Box aabb;
        final private Vec3d vel;
        final private Vec3d pos;
        final private Vec3d target;
        final private World world;
        final private SimMovementState sms;

        public TracerParams(final ArrowEntity arrow, final Box aabb, final Vec3d vel, final Vec3d pos, final Vec3d target,
                            final World world, final SimMovementState sms) {
            this.arrow = arrow;
            this.aabb = aabb;
            this.vel = vel;
            this.pos = pos;
            this.target = target;
            this.world = world;
            this.sms = sms;
        }
        public ArrowEntity getArrow() {
            return arrow;
        }
        public Box getAabb() {
            return aabb;
        }
        public SimMovementState getSms() {
            return sms;
        }
        public Vec3d getPos() {
            return pos;
        }
        public Vec3d getTarget() {
            return target;
        }

        public Vec3d getVel() {
            return vel;
        }

        public World getWorld() {
            return world;
        }
    }

    public void tick(final AltoClef mod) {
        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        this.checked.removeIf(old -> !arrows.contains(old));
        this.map.tick();
        arrows.forEach(arrow -> {
            if (!checked.contains(arrow)
                    && !arrow.verticalCollision
                    && !arrow.horizontalCollision
                    && arrow.prevX != arrow.getX()
                    && arrow.prevZ != arrow.getZ()
                    && arrow.prevY != arrow.getY()
                    && arrow.distanceTo(mod.getPlayer()) < 64) {
                this.checked.add(arrow);

                final Vec3d newTarget = YawHelper.yawToVec(mod.getPlayer().getYaw()).multiply(CSAlgorithm.DIST).add(mod.getPlayer().getPos());
                try {
                    //FIXME: jiggly baritone camera provokes imprecise tracings
                    paramQueue.put(new TracerParams(arrow, mod.getPlayer().getBoundingBox(), mod.getPlayer().getVelocity(), mod.getPlayer().getPos(), newTarget, mod.getWorld(), SimMovementState.UNDEFINED));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        moveResultsToMap();
    }

    public Optional<List<TickableTraceInfo>> nearest() {
        return this.map.nearest();
    }

    public Iterator<List<TickableTraceInfo>> getIterator() {
        return this.map.getIterator();
    }

    public void stop() {
        this.active = false;
        this.map.clear();
        this.paramQueue.clear();
        this.checked.clear();
        this.resultQueue.clear();
    }
}
