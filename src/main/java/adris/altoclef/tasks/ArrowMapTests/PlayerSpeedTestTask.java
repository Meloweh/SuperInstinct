/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.util.math.BlockPos;
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
public class PlayerSpeedTestTask extends Task {
    private Vec3d prevPos;
    boolean isSecondCycle;
    Vec3d prevVelocity;
    SpeedRecreation2_3 sr = new SpeedRecreation2_3();
    boolean finished, pre_finished, prepre_finished, preprepre_finished;
    int finishCounter;

    @Override
    protected void onStart(AltoClef mod) {
        isSecondCycle = false;
        prevPos = mod.getPlayer().getPos();
        finished = pre_finished = false;
        prepre_finished = false;
        preprepre_finished = false;
        finishCounter = 0;
    }

    Vec3d getDeltaPos(final Vec3d currentPos) {
        return new Vec3d(Math.abs(Math.abs(currentPos.getX())) - Math.abs(prevPos.getX()),
                Math.abs(Math.abs(currentPos.getY())) - Math.abs(prevPos.getY()),
                Math.abs(Math.abs(currentPos.getZ())) - Math.abs(prevPos.getZ()));
    }

    double get2DAbsoluteSpeed(final double x, final double z) {
        return Math.abs(Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)));
    }



    void func(AltoClef mod) {
        if (prevVelocity == null) return;
        double d = prevVelocity.x + 0.98 * 0.1 * (0.21600002F / (0.6 * 0.6 * 0.6)) * Math.sin(mod.getPlayer().getYaw() * 0.017453292F) * 0.6 * 0.91;
        System.out.println("eigener xVel: " + d);
    }

    void print(final AltoClef mod) {
        final Vec3d delta = getDeltaPos(mod.getPlayer().getPos());
        final Vec3d vel = mod.getPlayer().getVelocity();
        //System.out.println(mod.getPlayer().getMovementSpeed() + "     " + mod.getPlayer().getSpeed());
        /*System.out.println("");
        System.out.println("p: " + get2DAbsoluteSpeed(delta.getX(), delta.getZ()) + " v: " + get2DAbsoluteSpeed(vel.getX(), vel.getZ()) + " rxz: " + mod.getPlayer().getYaw());
        System.out.println("px: " + delta.getX() + " pz: " + delta.getZ());
        System.out.println("px2: " + (Math.abs(Math.abs(mod.getPlayer().getPos().getX())) - Math.abs(mod.getPlayer().prevX)) + " pz2: " + (Math.abs(Math.abs(mod.getPlayer().getPos().getZ())) - Math.abs(mod.getPlayer().prevZ)));

        System.out.println("dx: " + delta.getX() + " dz: " + delta.getZ());
        //System.out.println("dy: " + delta.getY());
        //System.out.println("dz: " + delta.getZ());
        System.out.println("pvx: " + mod.getPlayer().prevX+  " pvz: " + mod.getPlayer().prevZ);

        System.out.println("vx: " + vel.getX() + " vy: " + vel.getY() + " vz: " + vel.getZ());
        System.out.println("hx: " + mod.getPlayer().getRotationVector().getX() + " hy: " + mod.getPlayer().getRotationVector().getY() + " hz: " + mod.getPlayer().getRotationVector().getZ());

        System.out.println("hx2: " + mod.getPlayer().getRotationVector().getX() * VelocityHelper.PLAYER_SPRINT_SPEED + " hz2: " + mod.getPlayer().getRotationVector().getZ() * VelocityHelper.PLAYER_SPRINT_SPEED);
        final Vec3d helper = VelocityHelper.getVelocityFromRotation(new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()), mod.getPlayer().isSprinting());
        System.out.println("hx3: " + helper.getX() + " hz3: " + helper.getZ());
        System.out.println("Speed: " + mod.getPlayer().getSpeed());
        System.out.println("Movement Speed: " + mod.getPlayer().getMovementSpeed());
        System.out.println("Movement Dir: " + mod.getPlayer().getMovementDirection().asString());
        func(mod);
        System.out.println("Slipperyness: " + mod.getWorld().getBlockState(new BlockPos(mod.getPlayer().getPos().getX(), mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().getZ())).getBlock().getSlipperiness());
        System.out.println("flyingSpeed: " + mod.getPlayer().flyingSpeed);
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        System.out.println("BlockPos:"  + e);
        System.out.println("AABB: x=" + mod.getPlayer().getBoundingBox().getXLength() + " y=" + mod.getPlayer().getBoundingBox().getZLength());*/
        /*System.out.println("VelX: " + mod.getPlayer().getVelocity().x);
        if (sr.velocity == null) {
            if (mod.getPlayer().isSprinting() && Math.abs(mod.getPlayer().getVelocity().x) > 0 || !mod.getPlayer().isOnGround()) {
            //if (!mod.getPlayer().isOnGround()) {
                sr.velocity = new Vec3d(mod.getPlayer().getVelocity().x, mod.getPlayer().getVelocity().y, mod.getPlayer().getVelocity().z);
                sr.startVelX = mod.getPlayer().getVelocity().x;
                sr.startVelZ = mod.getPlayer().getVelocity().z;
                sr.startPosX = mod.getPlayer().getPos().x;
                sr.startPosZ = mod.getPlayer().getPos().z;
                sr.startVelY = mod.getPlayer().getVelocity().y;
                sr.startPosY = mod.getPlayer().getPos().y;
            }
        } else {
            if (!finished) {
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
                mod.getPlayer().setSprinting(true);
            }

            sr.travel(mod);
            if (pre_finished) finished = true;
            if (prepre_finished) pre_finished = true;
            prepre_finished = true;
        }*/
        if (!finished) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            mod.getPlayer().setSprinting(true);
        }

        sr.travel(mod);
        if (finishCounter++ > 20*30) finished = true;

        //mod.getPlayer().travel(new Vec3d(0, 0, 1.8));
        //mod.getPlayer().travel(new Vec3d(1.2, 0, 0));
        //System.out.println("forwardSpeed: " + mod.getPlayer().forwardSpeed);
        //System.out.println("sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed);
        //System.out.println("upwardSpeed: " + mod.getPlayer().upwardSpeed);

        //final Vec3d helper2 = VelocityHelper.getVelocityFromRotationTry2(new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()), mod.getPlayer().isSprinting());
        //System.out.println("hxA: " + helper2.getX() + " hzA: " + helper2.getZ());
        //final Vec3d helper3 = VelocityHelper.getVelocityFromRotationTry3(new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch()), mod.getPlayer().isSprinting());
        //System.out.println("hxB: " + helper.getX() + " hzB: " + helper.getZ());

        /*
        Vel verändert sich nicht bei kopf hoch und runter, alle anderen schon.
        Vel ähnelt pi/2
        Die anderen scheinen korrekt zu sein, solange man genau gerade aus schaut
        Frage: kann man die h's so ändern, dass vertikal nicht werte ändert? Würde da vielleicht dann sogar genau vel rauskommen als Konsequenz?

         */
    }

    @Override
    protected Task onTick(AltoClef mod) {
        print(mod);
        prevPos = mod.getPlayer().getPos();
        prevVelocity = new Vec3d(mod.getPlayer().getVelocity().getX(), mod.getPlayer().getVelocity().getY(), mod.getPlayer().getVelocity().getZ());
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        if (finished) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            return true;
        }
        return mod.getPlayer().isDead() || finished;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PlayerSpeedTestTask;
    }

    @Override
    protected String toDebugString() {
        return "PlayerSpeedTest";
    }
}
