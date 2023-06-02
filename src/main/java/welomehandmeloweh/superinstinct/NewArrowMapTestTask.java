/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.input.Input;
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
public class NewArrowMapTestTask extends Task {
    private boolean finished2;
    private float yaw;
    private boolean isStartTick = true;
    private Vec3d target;
    private NewArrowMap arrowMap;


    public NewArrowMapTestTask() {
        this.finished2 = false;
    }

    private boolean printed = false;

    @Override
    protected void onStart(AltoClef mod) {
        target = new Vec3d(0.5, mod.getPlayer().getY(), 0.5);
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());

        yaw = YawHelper.vecToYaw(eyeCenter, new Vec3d(target.getX(), mod.getPlayer().getEyeY(), target.getZ()));
        mod.getPlayer().setYaw(yaw);
        this.arrowMap = new NewArrowMap(mod);
    }

    private void go(final AltoClef mod) {
        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            mod.getPlayer().setYaw(yaw);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
            arrowMap.setNewTarget(target);
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!this.arrowMap.willCollide()) {
            go(mod);
        }
        return arrowMap.onTick(mod);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        finished2 = true;
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
        mod.getPlayer().setSprinting(false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {

        if (finished2) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            mod.getPlayer().setSprinting(false);
        }
        return finished2;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IntersectionMethodsBenchmarkTask;
    }

    @Override
    protected String toDebugString() {
        return this.getClass().getCanonicalName();
    }
}
