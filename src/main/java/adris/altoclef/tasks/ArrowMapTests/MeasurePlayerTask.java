/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
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
public class MeasurePlayerTask extends Task {
    private Vec3d initialVel, initialPos;

    /*class TestPackage {
        final Vec3d pos;
        final long tick;

        public TestPackage(final Vec3d pos, final long tick) {
            this.pos = pos;
            this.tick = tick;
        }

        final Vec3d getPos() {
            return pos;
        }

        final long getTick() {
            return tick;
        }
    }*/

    private List<Vec3d> positions;
    private long startTime;

    private boolean finished;

    private boolean wasOnGround;

    private boolean airTick;


    @Override
    protected void onStart(AltoClef mod) {
        positions = new ArrayList<>();
        initialVel = null;
        initialPos = null;
        finished = false;
        wasOnGround = false;
        airTick = false;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!wasOnGround) wasOnGround = mod.getPlayer().isOnGround();
        if (wasOnGround && !mod.getPlayer().isOnGround()) {
            if (!airTick) {
                initialVel = mod.getPlayer().getVelocity();
                initialPos = mod.getPlayer().getPos();
                airTick = true;
                startTime = System.currentTimeMillis();
            }

            /*if (current != null) {
                positions.add(current.getPos());
            }*/

            positions.add(mod.getPlayer().getPos());

            /*if (arrows.contains(current)) {
                positions.add(current.getPos());
            } else {
                finished = true;
            }*/
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (mod.getPlayer().isSneaking() || finished || airTick && mod.getPlayer().isOnGround()) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>STOPPED>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<");

            final long deltaTicks = ((System.currentTimeMillis() - startTime) / 1000) * 20;

            System.out.println("Elapsed ticks: " + deltaTicks);
            System.out.println("Size of list: " + positions.size());

            System.out.println("");

            for (int t = 0; t < positions.size(); t++) {
                System.out.println("Current tick: " + t);
                System.out.println("Traced Y: " + CSAlgorithmY.tracePlayerPosY(initialVel.getY(), initialPos.getY(), t));
                System.out.println("Real Y  : " + positions.get(t).getY());
                System.out.println("");
            }

            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            return true;
        }
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof MeasurePlayerTask;
    }

    @Override
    protected String toDebugString() {
        return null;
    }
}
