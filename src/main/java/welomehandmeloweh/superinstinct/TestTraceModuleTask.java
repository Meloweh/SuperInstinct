/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

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
public class TestTraceModuleTask extends Task {
    private ArrowEntity current;
    private Vec3d initialVel, initialPos;
    private List<Vec3d> positions;
    private long startTime;
    private boolean finished;
    TraceResult traceResult;

    @Override
    protected void onStart(AltoClef mod) {
        positions = new ArrayList<>();
        initialVel = null;
        current = null;
        initialPos = null;
        startTime = System.currentTimeMillis();
        finished = false;
        traceResult = null;
    }

    @Override
    protected Task onTick(AltoClef mod) {

        final List<ArrowEntity> arrows = mod.getEntityTracker().getTrackedEntities(ArrowEntity.class);
        final List<PlayerEntity> entities = mod.getEntityTracker().getTrackedEntities(PlayerEntity.class);

        if (entities.size() < 1) {
            finished = true;
            return null;
        }

        if (!arrows.isEmpty()) {
            if (current == null) {
                current = arrows.get(0);
                initialVel = current.getVelocity();
                initialPos = current.getPos();
                //(final ArrowEntity arrow, final LivingEntity player, final Box playerAABB, final double startTick, final double endTick) {
                System.out.println(entities.stream().filter(e -> e.getDisplayName().getString().equals("AuraXP")).findFirst().get());
                traceResult = null;//BowArrowIntersectionTracer.calculateCollision(current, entities.stream().filter(e -> e.getDisplayName().asString().equals("AuraXP")).findFirst().get(), 0d, 1000d);
            }
            positions.add(arrows.get(0).getPos());
        }
        return null;
    }
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {}
    @Override
    public boolean isFinished(AltoClef mod) {
        if (mod.getPlayer().isSneaking() || finished) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>STOPPED>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<");

            final long deltaTicks = ((System.currentTimeMillis() - startTime) / 1000) * 20;

            System.out.println("Elapsed ticks: " + deltaTicks);
            System.out.println("Size of list: " + positions.size());
            System.out.println("");

            for (int t = 0; t < positions.size(); t++) {
                System.out.println("Current tick: " + t);
                System.out.println("Traced Y: " + BowArrowIntersectionTracer.tracePosY(initialVel.getY(), initialPos.getY(), t));
                System.out.println("Real Y  : " + positions.get(t).getY());
                System.out.println("");
            }
            System.out.println("TraceResult: " + traceResult);

            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            return true;
        }
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof TestTraceModuleTask;
    }

    @Override
    protected String toDebugString() {
        return this.getClass().getCanonicalName();
    }
}
