/**
 * @author Welomeh, Meloweh
 */
package welomehandmeloweh.superinstinct;

import adris.altoclef.AltoClef;
import net.minecraft.util.math.*;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

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
public class SpeedRecreation2_3 {
    public double startVelY;
    public double startPosY;
    Vec3d velocity;
    Vec3d position;
    double startPosX, startPosZ;
    double startVelX, startVelZ;
    int tickCounter = 0;
    double startPosX_2 = 0;
    double startPosZ_2 = 0;
    float startYaw = 0;
    Vec3d startPos;

    void setVelocity(Vec3d vec) {
        velocity = vec;
    }
    void setVelocity(double x, double y, double z) {
        velocity = new Vec3d(x,y,z);
    }

    Vec3d getVelocity() {
        return velocity;
    }

    /*private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return new Vec3d(movementInput.multiply(speed).x * (double)MathHelper.cos(yaw * 0.017453292F) - movementInput.multiply(speed).z * (double)MathHelper.sin(yaw * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(yaw * 0.017453292F) + movementInput.multiply(speed).x * (double)MathHelper.sin(yaw * 0.017453292F));
    }*/

    /*public void updateVelocity(float speed, Vec3d movementInput, AltoClef mod) {
        //Vec3d vec3d = movementInputToVelocity(movementInput, speed, mod.getPlayer().getYaw());
        Vec3d vec3d = new Vec3d(movementInput.multiply(speed).x * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) - movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) + movementInput.multiply(speed).x * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        this.setVelocity(this.getVelocity().add(vec3d));
    }*/

    /*float getMovementSpeed(float slipperiness, AltoClef mod) {
        return mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
    }*/

    /*Vec3d method_26318(Vec3d movementInput, float slipperiness, AltoClef mod) {
        //this.updateVelocity(getMovementSpeed(f, mod), vec3d, mod);
        //this.updateVelocity(mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed(), vec3d, mod);
        float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        Vec3d vec3d_ = new Vec3d(movementInput.multiply(speed).x * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) - movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) + movementInput.multiply(speed).x * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        this.setVelocity(this.getVelocity().add(vec3d_));
        Vec3d vec3d2 = this.getVelocity();
        return vec3d2;
    }*/

    void travelWorkingV1(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        Vec3d movementInput = new Vec3d((double)mod.getPlayer().sidewaysSpeed, (double)mod.getPlayer().upwardSpeed, (double)mod.getPlayer().forwardSpeed);
        float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        Vec3d vec3d_ = new Vec3d(movementInput.multiply(speed).x * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) - movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F) + movementInput.multiply(speed).x * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        this.setVelocity(this.getVelocity().add(vec3d_));
        Vec3d vec3d2 = this.getVelocity();
        Vec3d g = vec3d2;
        double h = g.y;
        h -= 0.08;
        setVelocity(g.x * (double)f, h * 0.9800000190734863, g.z * (double)f);
        System.out.println("recr vel x: " + getVelocity().x + " recr vel y: " + getVelocity().y + " recr vel z: " + getVelocity().z);
    }

    void travelWorkingV2(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        Vec3d movementInput = new Vec3d((double)mod.getPlayer().sidewaysSpeed, (double)mod.getPlayer().upwardSpeed, (double)mod.getPlayer().forwardSpeed);
        System.out.println("FROM TRAVEL sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed + " upwardSpeed: " + mod.getPlayer().upwardSpeed + " forwardSpeed: " + mod.getPlayer().forwardSpeed);
        float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        Vec3d vec3d_ = new Vec3d(- movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        System.out.println("FROM TRAVEL vec3d_ sidewaysSpeed: " + vec3d_.x + " upwardSpeed: " + vec3d_.y + " forwardSpeed: " + vec3d_.z);
        this.setVelocity(this.getVelocity().add(vec3d_));
        setVelocity(this.getVelocity().x * (double)f, (this.getVelocity().y - 0.08) * 0.9800000190734863, this.getVelocity().z * (double)f);
        System.out.println("recr vel x: " + getVelocity().x + " recr vel y: " + getVelocity().y + " recr vel z: " + getVelocity().z);
    }

    void travelWorkingV3(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        //Vec3d movementInput = new Vec3d(0, 0, mod.getPlayer().forwardSpeed);
        double movementInputZ = mod.getPlayer().forwardSpeed;


        System.out.println("FROM TRAVEL sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed + " upwardSpeed: " + mod.getPlayer().upwardSpeed + " forwardSpeed: " + mod.getPlayer().forwardSpeed);
        //float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        //Vec3d vec3d_ = new Vec3d(- movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        double vx1 = this.getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
        double vy1 = this.getVelocity().y + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed());
        double vz1 = this.getVelocity().z + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F);
        //Vec3d vec3d_ = new Vec3d(vx1,vy1,vz1);

        //System.out.println("FROM TRAVEL vec3d_ sidewaysSpeed: " + vec3d_.x + " upwardSpeed: " + vec3d_.y + " forwardSpeed: " + vec3d_.z);
        //this.setVelocity(this.getVelocity().add(new Vec3d(vx1,vy1,vz1)));
        this.setVelocity(vx1, vy1, vz1);
        setVelocity(this.getVelocity().x * f, (this.getVelocity().y - 0.08) * 0.9800000190734863, this.getVelocity().z * f);
        System.out.println("recr vel x: " + getVelocity().x + " recr vel y: " + getVelocity().y + " recr vel z: " + getVelocity().z);
    }

    void travelSolution1(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        //double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        //Vec3d movementInput = new Vec3d(0, 0, mod.getPlayer().forwardSpeed);
        double movementInputZ = mod.getPlayer().forwardSpeed;


        System.out.println("FROM TRAVEL sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed + " upwardSpeed: " + mod.getPlayer().upwardSpeed + " forwardSpeed: " + mod.getPlayer().forwardSpeed);
        //float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        //Vec3d vec3d_ = new Vec3d(- movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        double vx1 = (this.getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F)) * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F);
        double vy1 = ((this.getVelocity().y + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed())) - 0.08) * 0.9800000190734863;
        double vz1 = (this.getVelocity().z + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F)) * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F);
        //Vec3d vec3d_ = new Vec3d(vx1,vy1,vz1);

        //System.out.println("FROM TRAVEL vec3d_ sidewaysSpeed: " + vec3d_.x + " upwardSpeed: " + vec3d_.y + " forwardSpeed: " + vec3d_.z);
        //this.setVelocity(this.getVelocity().add(new Vec3d(vx1,vy1,vz1)));
        //this.setVelocity(vx1, vy1, vz1);
        setVelocity(vx1, vy1, vz1);
        System.out.println("recr vel x: " + getVelocity().x + " recr vel y: " + getVelocity().y + " recr vel z: " + getVelocity().z);
    }

    double sum(final int i, final int n, final double k) {
        double res = 0;
        for (int j = i; j <= n; j++) {
            res += Math.pow(k,j);
        }
        return res;
    }

    double f(double start, double v, float q, double m, double n) {
        double res = start;
        for (int i = 1; i <= (int)n; i++) {
            res += v * Math.pow(q, i-1) + m * ((1-Math.pow(q,i))/(1-q));
        }
        return res;
    }

    enum Side { LOW, HIGH, NONE }
    /*
    private class Crossing {
        final Side side;
        final Side nextSide;
        final Optional<Double> tick;
        final Axis axis;

        public Crossing(final Side side, final Optional<Double> tick, final Axis axis, final float q, final double v_0) {
            this.side = side;
            this.tick = tick;
            this.nextSide = side.equals(Side.HIGH) ? Side.LOW : Side.HIGH;
            this.axis = axis;
        }

        final Side getSide() {
            return side;
        }

        final Side getPostIntersectionSiding() {
            if (getTick().isEmpty()) return Side.NONE;
            return nextSide;
        }

        final Optional<Double> getTick() {
            return tick;
        }

        private static double getDeltaPos(final Axis axis, final Side side, final double m, final float q, final double v_0, final double endTick) {
            if (axis.equals(Axis.X)) {
                if (side.equals(Side.HIGH)) {
                    return localEnd = v_0 * ((1 - Math.pow(q, endTick)) / (1-q)) - (m * endTick) / (1-q) + (m / (1-q)) * ((1-Math.pow(q, endTick+1)) / (1-q)) - (m / (1-q));
                }

            }

            if (side.equals(Side.HIGH)) {

            }

        }

        final double getCrossingPositionDelta(final double m, final float q, final double v_0) {
            //wenn v drunter dann -m*deltaT ansonsten lange Formel
            return startPosition + getDeltaPos(axis, side, m, q, v_0, endTick);
        }
    }*/

  /*  Crossing neglectableSpeedLimit(final double m, final float q, final double v_0, final Axis axis) {
        final double a = 1 / (v_0 + (m*q)/(1-q));
        final double b = (m*q) / (1-q);
        final double a_positive = a * (b + 0.003d);
        final double a_negative = a * (b - 0.003d);
        final boolean isV0Positive = Double.compare(v_0, 0.003) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        return new Crossing(isV0Positive ? Side.HIGH : Side.LOW, isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty(), axis, m, q, v_0);

        //return Double.compare(v_0, 0.003) > 0 ?
        //        Double.compare(a_positive, 0) > 0 ? Optional.of(Math.log(a_positive) / Math.log(q)) : Optional.empty()
        //        : Double.compare(a_negative, 0) > 0 ? Optional.of(Math.log(a_negative) / Math.log(q)) : Optional.empty();
//
        //return Double.compare(a_positive, 0) > 0 ? Optional.of(Math.log(a_positive) / Math.log(q))

    }*/

    //FIXME: is m for x equal to -mX or mX????
    public static double deltaVelFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        //if (Double.compare(v, 0d) == 0)System.out.println("YES..");
        if (axis.equals(Axis.X)) {
            //FIXME: is m for x equal to -mX or mX????
            final double v_0 = (Double.compare(v, 0d) == 0) ? ((-m * q - m) * q) : v;
            return v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q)); //FIXME: TODO: is still formular up-to-date? if v0 == 0, we should use the v0 formular i think
        }
        final double v_0 = (Double.compare(v, 0d) == 0) ? ((m * q + m) * q) : v;
        //FIXME: is m for z equal to -mZ or mZ????
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q)); //FIXME: TODO: is still formular up-to-date? if v0 == 0, we should use the v0 formular i think

        //if (Double.compare(v_0, 0d) == 0) return (m * q + m * Math.pow(q, 2));

        /*
        //TODO: test if required
        if (Double.compare(t, 0d) == 0) return v_0;
        //TODO: test me because of: => //FIXME: is m for z equal to -mZ or mZ????
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q));*/
    }

    public static double velFormular(final double t, final Axis axis, final double v, final float q, final double m) {
        return deltaVelFormular(t, axis, v, q, m) * q;
    }

    /*
    private static double velFormularWithV0Considered(final double t, final Axis axis, final double v_0, final float q, final double m) {
        //final double new_v0 = Double.compare(v_0, 0d) == 0 ? (m * q + m * Math.pow(q, 2)) : v_0;
        if (Double.compare(v_0, 0d) == 0) return (m * q + m * Math.pow(q, 2));
        return velFormular(t, axis, v_0, q, m);
    }*/

    int acounter = 0;
    int msum = 0;

    enum VelocityState {
        Positive,
        Middle,
        Negative,
        NULL
    }
/*    double neglectableSpeedLimit(final double m, final float q, final double v_0, final Axis axis, final double endTick, final double walkedTicks) {
        if (Double.compare(walkedTicks, endTick) >= 0) return 0;

        final double a = 1 / (v_0 + (m*q)/(1-q));
        final double b = (m*q) / (1-q);
        final double a_positive = a * (b + 0.003d);
        final double a_negative = a * (b - 0.003d);
        final boolean isV0Positive = Double.compare(v_0, 0.003d) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        final Side side = isV0Positive ? Side.HIGH : Side.LOW;
        final Optional<Double> intersection = isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty();
        //System.out.println(intersection.isPresent() ? "yes :) :" + intersection.get() : "no :(");
        //if (intersection.isEmpty()) return 0;
        final double t = intersection.isEmpty() || intersection.get().equals(Double.NaN) ? endTick : intersection.get();
        acounter++;
        msum += m;
        System.out.println("[ngs at " + acounter + ": " + msum + "]");
        final double formular = axis.equals(Axis.X) ?
            side.equals(Side.HIGH) ?
                1 / (1 - q) * (v_0 * (1 - Math.pow(q, t)) + m * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1))
                :
                -m * t
            :
            (side.equals(Side.HIGH)) ?
                v_0 * ((1-Math.pow(q,t))/(1-q)) + ((m*t)/(1-q)) - ((m)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + m/(1-q)
                :
                m * t;


        final double v_0new = velFormular(t, axis, v_0, q, m);
        return formular + neglectableSpeedLimit(m, q, v_0new, axis, endTick, walkedTicks + t);

    }*/

    static final Optional<Double> getIntersectionOutsideToInsideZ(final double m, final float q, final double v_0) {
        final double a = 1 / (v_0 - (m*q)/(1-q));
        final double b = -((m*q) / (1-q));
        final double a_positive = a * (b + (-0.003d));
        final double a_negative = a * (b - (-0.003d));
        //It is expected that this method is called only if v_0 is ensured not to be between -0.003 and 0.003
        final boolean isV0Positive = Double.compare(v_0, 0.003d) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        return isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty();
    }

    static final Optional<Double> getIntersectionOutsideToInsideX(final double m, final float q, final double v_0) {
        final double a = 1 / (v_0 + (m*q)/(1-q));
        final double b = (m*q) / (1-q);
        final double a_positive = a * (b + 0.003d);
        final double a_negative = a * (b - 0.003d);
        //It is expected that this method is called only if v_0 is ensured not to be between -0.003 and 0.003
        final boolean isV0Positive = Double.compare(v_0, 0.003d) > 0;
        final double a_wanted = isV0Positive ? a_positive : a_negative;
        final boolean isAWantedPositive = Double.compare(a_positive, 0) > 0;
        return isAWantedPositive ? Optional.of(Math.log(a_wanted) / Math.log(q)) : Optional.empty();
    }

    static final Optional<Double> getIntersectionInsideToOutside(final double m, final float q) {
        // m sollte als mX bzw mZ bereits durch den Aufrufer von neglectableSpeedLimit passend weitergegeben sein
        //return Double.compare(m, 0d) != 0 ? Optional.of(Math.abs(0.003d / m)) : Optional.empty();
        return Math.abs(m * q) >= 0.003 ? Optional.empty() : Optional.of(0.003 / (m * q));
    }


    //FIXME: recursion is not viable due to stack overflow
    static double neglectableSpeedLimit(final double m, final float q, final double v_0prev, final Axis axis, final double endTick, final double walkedTicks, final boolean shouldConsiderStartVelWhenInMiddle) {
        //if (Double.compare(walkedTicks, endTick) > 0) return 0;
        //if (Double.compare(Math.abs(walkedTicks), Math.abs(endTick)) >= 0) return 0;

        final double v_1 = shouldConsiderStartVelWhenInMiddle ? (axis.equals(Axis.X) ? (-m * q - m * Math.pow(q, 2)) : (m * q + m * Math.pow(q, 2))) : v_0prev;
        //System.out.println("shouldConsiderStartVelWhenInMiddle: " + shouldConsiderStartVelWhenInMiddle);

        System.out.println("v_0prev: " + v_0prev);
        final VelocityState starterVelocityState = Double.compare(v_0prev, 0d) >= 0 ? VelocityState.Positive : VelocityState.Negative;

        final VelocityState nonStarterVelocityState = (Double.compare(v_1, 0.003d) >= 0 ? VelocityState.Positive :
                Double.compare(v_1, -0.003d) > 0 ? VelocityState.Middle :
                        VelocityState.Negative);

        //---debug---//
        //System.out.println("VelocityState: " + velocityState.name());
        //if (true) return 0;
        //-----------//
        System.out.println("starterVelocityState: " + starterVelocityState.name() + " ---- " + "nonStarterVelocityState: " + nonStarterVelocityState);
        //final boolean velIsMiddle = velocityState.equals(VelocityState.Middle);
        final boolean nonStarterMiddle = nonStarterVelocityState.equals(VelocityState.Middle);
        //final boolean starterMiddle = starterVelocityState.equals(VelocityState.Middle);

        final double v_0 = nonStarterMiddle ? 0 : v_0prev;
        //System.out.println("v_0: " + v_0);

        System.out.println("m: " + m + " q: " + q + " v_0: " + v_0);

        //final Optional<Double> intersection = nonStarterMiddle ? getIntersectionInsideToOutside(m) :
        //        axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_0) : getIntersectionOutsideToInsideZ(m,q,v_0);

        final Optional<Double> intersection = nonStarterMiddle ? getIntersectionInsideToOutside(m, q) :
                axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_1) : getIntersectionOutsideToInsideZ(m,q,v_1);

        final Optional<Double> debug = nonStarterMiddle ? getIntersectionInsideToOutside(m, q) :
                axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_0) : getIntersectionOutsideToInsideZ(m,q,v_0);
        System.out.println("debug: " + (debug.isPresent() ? debug.get() : "{}"));

        //FIXME: nee wir können nicht endtick nehmen, da wir nur in deltaschritten arbeiten können.
        final double rawt = (intersection.isEmpty() || intersection.get().equals(Double.NaN) || Double.compare(intersection.get(), 0) < 0) ? (endTick - walkedTicks) : intersection.get();
        //final double t = Double.compare(rawt, walkedTicks) < 0 ? endTick : rawt;
        final double t = Double.compare(walkedTicks + rawt, endTick) > 0 ? (endTick - walkedTicks) : rawt;
        //acounter++;
        //msum += m;
        //final double formula = playerFormularXZ(m,q,v_0,axis,t, shouldConsiderStartVelWhenInMiddle ? VelocityState.Positive : velocityState) - formularOld; // FIXME: This may not be delta but always the trace from the very start to delta t
        //final double newLocalTick = prevState.equals(VelocityState.Middle) ? 0 :
        //FIXME IMPORTANT!!!!!!: m of the past needs to be reconstructed for "playerFormularXZ(m..."!!!!!!!!!!!!!
        final double formula = (shouldConsiderStartVelWhenInMiddle && nonStarterMiddle) ?
                playerFormularXZ(m,q,v_0,axis,0, starterVelocityState) + playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState)
                :
                playerFormularXZ(m,q,v_0,axis,t, nonStarterVelocityState); // FIXME: This may not be delta but always the trace from the very start to delta t

        /*double formula = Double.NaN;
        if (shouldConsiderStartVelWhenInMiddle) {
            formula += playerFormularXZ(m,q,v_0,axis,0, velocityState);
            if (t > 0)
        }*/

        System.out.println("[ngs at " + "v_0: " + v_0 + " v_0prev: " + v_0prev + " rawt: " + rawt + " formula: " + formula + " acounter" + ": " + "msum" + "]" + " (debug= t:" +t+ " walkedTicks: " + walkedTicks + " endTick:" + endTick + " intersec: " + (intersection.isPresent() ? intersection.get() : "empty") + " bool: " + ((intersection.isEmpty() || intersection.get().equals(Double.NaN))));
        if (Double.compare(t, 0d) == 0 || Double.compare(t, -0d) == 0 || Double.compare(walkedTicks + t, endTick) >= 0) return formula;
        //final double v_0new = velFormular(t, axis, v_0, q, m);
        //final double v_0new = /*velocityState.equals(VelocityState.Middle) ? 0d :*/ velocityState.equals(VelocityState.Middle) ? (axis.equals(Axis.X) ? -m : m) : velFormular(walkedTicks, axis, v_0, q, m);
        final double v_0new = nonStarterMiddle ? (axis.equals(Axis.X) ? -m : m)*t*q : velFormular(t, axis, v_0, q, m);
        return formula + neglectableSpeedLimit(m, q, v_0new, axis, endTick, walkedTicks + t, false);
    }

    static double neglectableSpeedLimitUsage(final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks) {
        //acounter = 0;
        //msum = 0;
        final double v_0 = 0d;
        return startPos + neglectableSpeedLimit(m, q, v_0, axis, endTick, walkedTicks, true);
    }
    /*
    Optional<Double> splittedFormular(final double startTick, final double endTick, final double m, final float q, final double v_0, final Axis axis) {
        Optional<Double> result = Optional.empty();
        double currentTick = startTick;
        double currentV_0 = v_0;
        for (Crossing crossing; (crossing = neglectableSpeedLimit(m, q, currentV_0, axis)).getTick().isPresent() && Double.compare(crossing.getTick().get(), endTick) < 0;) {
            //result = Optional.of((result.isEmpty() ? 0 : result.get()) + (crossing.getSide().equals(Side.LOW) ?
            //        (axis.equals(Axis.X) ? -1 : 1) * (pre) * m
            //        :

            //));
        }
        return result;
    }*/

    private class SlipperinessPath {
        final SlipperinessSpot start, end;

        public SlipperinessPath(final SlipperinessSpot start, final SlipperinessSpot end) {
            this.start = start;
            this.end = end;
        }

        public SlipperinessSpot getEnd() {
            return end;
        }

        public SlipperinessSpot getStart() {
            return start;
        }

        public Vec3i getCuttingLine() {
            return getEnd().getPos().subtract(getStart().getPos());
        }
    }

    private static double playerFormularXZ(final double m, final float q, final double v_0, final Axis axis, final double t, final VelocityState velocityState) {
        return axis.equals(Axis.X) ?
                !velocityState.equals(VelocityState.Middle) ?
                        CSAlgorithm.tracePlayerPosXDelta(m, q, v_0, t)//1 / (1 - q) * (v_0 * (1 - Math.pow(q, t)) + m * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1)) //FIXME: This old, use new formular here => DONE!
                        :
                        -m * t
                :
                (!velocityState.equals(VelocityState.Middle)) ?
                        CSAlgorithm.tracePlayerPosZDelta(m, q, v_0, t)//v_0 * ((1-Math.pow(q,t))/(1-q)) + ((m*t)/(1-q)) - ((m)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + m/(1-q)//FIXME: This old, use new formular here => DONE!
                        :
                        m * t;
    }

    private static double getDiffPlayerVelVsLineVel(final double t, final Axis axis, final double v_0, final float q, final double m) {
        return velFormular(t, axis, v_0, q, m);
    }

    static final Bisection getPlayerVelVsLineVelBisection(final double t, final Axis axis, final double v_0, final float q, final double m) {
        return new Bisection() {
            @Override
            public double f(double t) {
                return getDiffPlayerVelVsLineVel(t, axis, v_0, q, m);
            }
        };
    }

    private static double getDiffPlayerVsLineXZ(final double t, final Axis axis, final double v_0, final float q, final double m, final double xOrZOfLine) {
        final VelocityState velocityState = Double.compare(v_0, 0.003d) > 0 ? VelocityState.Positive :
                Double.compare(v_0, -0.003d) > 0 ? VelocityState.Middle :
                        VelocityState.Negative;
        return playerFormularXZ(m,q,v_0,axis,t, velocityState) - xOrZOfLine;
    }

    static final Bisection getPlayerVsLineBisection(final double t, final Axis axis, final double v_0, final float q, final double m, final double xOrZOfLine) {
        return new Bisection() {
            @Override
            public double f(double t) {
                return getDiffPlayerVsLineXZ(t, axis, v_0, q, m, xOrZOfLine);
            }
        };
    }

    /**
     * TODO: implement this idea
     */
    static final boolean isInRemainingRunningStaminaTicks() {
        return true;
    }

    //final double startPos, final double m, final float q, final Axis axis, final double endTick, final double walkedTicks
    static final Vec3d slipperinessSplitter(final Vec3d playerStartPos, final List<SlipperinessSpot> spots, final double endTick, final Optional<Double> optPrevV0FromPrevPath, final float yaw) {
        //final BlockPos startBlock = new BlockPos(playerStartPos).down();
        //final BlockPos unpreciseEndBlock = new BlockPos(unpreciseEndPos);

        //TODO: add driftoff check every like 100 blocks and add those candidates to path list.
        // also meanwhile the error distance increases, we may recalibrate
        // the Bresenham line and resume with this adjustment to the error.
        // It would be also interesting to consider creating a formula to get t by error distance as input.

        //Pass center of player
        //final List<BlockPos> path = VanillaBresenham.normalBresenham((int)playerStartPos.getY() - 1, playerStartPos.getX(), playerStartPos.getZ(), unpreciseEndBlock.getX(), unpreciseEndBlock.getZ()); //FIXME: Y-1? Probably too much

        /*
        if (path.size() < 1) return 0;

        final List<SlipperinessSpot> spots = new ArrayList<>();
        path.forEach(e -> spots.add(new SlipperinessSpot(e, world.getBlockState(e).getBlock())));*/

        /*if (spots.size() < 2) {

            return 0;
        }*/

        SlipperinessSpot currentStartPoint = spots.get(0);
        SlipperinessSpot prev = currentStartPoint;
        //Vec3d totalDeltaPosition = Vec3d.ZERO;
        VecXZ totalDeltaPosition = new VecXZ(0.0d, 0.0d);
        float currentSlipperiness = currentStartPoint.getSlipperiness();
        //boolean usedEarliestStartMonoPos = false;
        //Optional<Vec3d> optCurrentStartPos = Optional.of(new Vec3d(playerStartPos.getX(), playerStartPos.getY(), playerStartPos.getZ()));
        Optional<Double> optPrevV0 = optPrevV0FromPrevPath;
        boolean shouldConsiderStartVelWhenInMiddle = true;
        double lastProgressTick = 0;
        for (final SlipperinessSpot current : spots) {
            if (spots.indexOf(current) == spots.size() - 1 || !prev.equals(current)) { //FIXME: spots.indexOf(current) == spots.size() - 1 wants to cut after but !prev.equals(current) wants prior
                    /*
                north: neg z
                south: pos z
                west:  neg x
                east:  pos x
                 */
                //final Axis lineAsAxis = (prev.getPos().up().equals(current.getPos()) || prev.getPos().down().equals(current.getPos())) ? Axis.Z : Axis.X;
                /*final Axis prevToCurrentLineAsAxis =
                        prev.getPos().north().equals(current.getPos()) ? Axis.Z :
                        prev.getPos().south().equals(current.getPos()) ? Axis.Z : Axis.X;*/

                /*
                if (prevToCurrentLineAsAxis.equals(Axis.Z)) { // if line is like z axis, we look at where player.x cuts line.x
                    double line;
                    if (current.getPos().getX() < prev.getPos().getX()) {// if following is towards negative
                        line = //prev.getPos().getX(); // take the edge closer to player but therefore further away from block origin because origin is towards negative while player comming from towards positive
                        if (current.isAir()) line -= CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                    } else {
                        line = //current.getPos().getX();
                        if (current.isAir()) line += CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                    }



                } else {
                    double line;
                    if (current.getPos().getZ() < prev.getPos().getZ()) {
                        line = prev.getPos().getZ();
                        if (current.isAir()) line -= CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                    } else {
                        line = current.getPos().getZ();
                        if (current.isAir()) line += CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                    }
                }*/

              /*north: neg z
                south: pos z
                west:  neg x
                east:  pos x
                 */
                final Direction prevToCurrentLineAsDirection =
                        prev.getPos().north().equals(current.getPos()) ? Direction.NORTH :
                                prev.getPos().south().equals(current.getPos()) ? Direction.SOUTH :
                                        prev.getPos().west().equals(current.getPos()) ? Direction.WEST : Direction.EAST;
                double intersectionPos;
                if (prevToCurrentLineAsDirection.equals(Direction.NORTH)) { // if line is like z axis, we look at where player.x cuts line.x
                    intersectionPos = prev.getPos().getZ();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos -= Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.SOUTH)) {
                    intersectionPos = prev.getPos().getZ();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos += Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.WEST)) {
                    intersectionPos = prev.getPos().getX();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos -= Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else if (prevToCurrentLineAsDirection.equals(Direction.EAST)) {
                    intersectionPos = prev.getPos().getX();
                    //Tipping point is then further toward curr
                    if (current.isAir()) intersectionPos += Double.MIN_VALUE + CSAlgorithm.DEFAULT_HITBOX_WIDTH / 2;
                } else throw new IllegalStateException("We should not be here. prevToCurrentLineAsDirection is: " + prevToCurrentLineAsDirection == null ? "missing." : "present.");

                final Axis axisOfLine =
                        (prevToCurrentLineAsDirection.equals(Direction.NORTH) || prevToCurrentLineAsDirection.equals(Direction.SOUTH)) ?
                                Axis.Z : Axis.X;

                //final double playerEarliestStartMonoPos = axisOfLine.equals(Axis.X) ? playerStartPos.getX() : playerStartPos.getZ();

                //FIXME: this works only for an error distance below a total of 0.5 meter.
                /*final double currentStartMonoPos = !usedEarliestStartMonoPos ?
                        (axisOfLine.equals(Axis.X) ? playerStartPos.getX() : playerStartPos.getZ())
                        :
                        (axisOfLine.equals(Axis.X) ? p.getX() : p.getZ());*/


                final VecXZ currPlayerStartPos = new VecXZ(playerStartPos.getX() + totalDeltaPosition.getX(), playerStartPos.getZ() + totalDeltaPosition.getZ());
                final double currentStartMonoPos = axisOfLine.equals(Axis.X) ? currPlayerStartPos.getX() : currPlayerStartPos.getZ();

                final float q = !current.isAir() ? 0.91F * currentSlipperiness : 0.91F;
                final double m = CSAlgorithm.getM(axisOfLine, yaw, isInRemainingRunningStaminaTicks(), !current.isAir(), currentSlipperiness);

                final TreeMap<Double, SplittedProgress> progress = new TreeMap<>();
                final Optional<Double>[] optCurrentTickAtMonoPos = CSAlgorithm.getPointIntersectionMovingPlane(lastProgressTick, endTick, intersectionPos, optPrevV0.get() /*TODO: ensure get() can be called here*/, currentStartMonoPos, axisOfLine, currentSlipperiness,
                        !current.isAir(), isInRemainingRunningStaminaTicks(), yaw, shouldConsiderStartVelWhenInMiddle, m, q, progress);


                //FIXME IMPORTANT!: optCurrentTickAtMonoPos[0].get() needs check for non-intersection cases => DONE <<BUT>> check if it is complete
                if (optCurrentTickAtMonoPos.length > 0 && optCurrentTickAtMonoPos[0].isPresent()) {
                    //TODO: can be optimized?
                    final double dx = neglectableSpeedLimit(m, q, optPrevV0.isPresent() ? optPrevV0.get() : 0d, Axis.X, optCurrentTickAtMonoPos[0].get(), 0, shouldConsiderStartVelWhenInMiddle);
                    final double dz = neglectableSpeedLimit(m, q, optPrevV0.isPresent() ? optPrevV0.get() : 0d, Axis.Z, optCurrentTickAtMonoPos[0].get(), 0, shouldConsiderStartVelWhenInMiddle);
                    //totalDeltaPosition += trace stuff from monoCurrentStartPointPos to monoIntersectionPos here
                    totalDeltaPosition.add(dx, dz);
                }

                optPrevV0 = Optional.of(progress.get(progress.lastKey()).getCurrVel());
                //optCurrentStartPos = the new start pos; //optCurrentStartPos = Optional.of(progress.get(progress.lastKey()).getCurrPos());
                lastProgressTick = optCurrentTickAtMonoPos[0].get();
                currentStartPoint = current;
                currentSlipperiness = currentStartPoint.getSlipperiness();
                shouldConsiderStartVelWhenInMiddle = false;
            }

            prev = current;
        }

        return playerStartPos.add(totalDeltaPosition.getX(), 0, totalDeltaPosition.getZ());
        /*
        //final List<SlipperinessSpot> spots = new ArrayList<>();
        final List<SlipperinessPath> transitions = new ArrayList<>();
        SlipperinessSpot prev = null;
        for (final BlockPos e : path) {
            final SlipperinessSpot newSpot = new SlipperinessSpot(e, world.getBlockState(e).getBlock());
            if (!transitions.isEmpty()) {

            } else if (prev != null) {

            }
            prev = newSpot;
        }
        path.forEach(e -> {
            final SlipperinessSpot newSpot = new SlipperinessSpot(e, world.getBlockState(e).getBlock());
            if (!transitions.isEmpty()) {

            }
            //prev = newSpot;
            //if (spots.size() < 1 || !spots.get(spots.size() - 1).equals(newSpot)) {
            //    if (prev != null) spots.add(newSpot);
            //    spots.add(newSpot);
            //}

        });*/

        /*
        final List<SlipperinessPath> cuttingLines = new ArrayList<>();
        for (int i = 1; i < spots.size(); i++) {
            cuttingLines.add(new SlipperinessPath(spots.get(i-1), spots.get(i)));
        }*/

    }

    void travel(final AltoClef mod) {
        if (tickCounter == 0) {
            startPosX_2 = mod.getPlayer().getPos().x;
            startYaw = mod.getPlayer().getYaw();
            startPos = mod.getPlayer().getPos();
            startPosZ_2 = mod.getPlayer().getPos().z;
        }

        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        //double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        //Vec3d movementInput = new Vec3d(0, 0, mod.getPlayer().forwardSpeed);
        double movementInputZ = mod.getPlayer().forwardSpeed;
        final float q = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        final double t = tickCounter;

        /*final double oldStuff = Math.abs(mod.getPlayer().getVelocity().x * q);
        if (oldStuff < 0.003) {
            setVelocity(0, getVelocity().y, getVelocity().z);
            System.out.println("YE IT*S SMOLL: " + oldStuff);
        }*/



        //System.out.println("FROM TRAVEL sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed + " upwardSpeed: " + mod.getPlayer().upwardSpeed + " forwardSpeed: " + mod.getPlayer().forwardSpeed);
        //float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        //Vec3d vec3d_ = new Vec3d(- movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        //double vx1 = (this.getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        //double vy1 = ((this.getVelocity().y + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed())) - 0.08) * 0.9800000190734863;
        //double vz1 = (this.getVelocity().z + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        //Vec3d vec3d_ = new Vec3d(vx1,vy1,vz1);

        //System.out.println("FROM TRAVEL vec3d_ sidewaysSpeed: " + vec3d_.x + " upwardSpeed: " + vec3d_.y + " forwardSpeed: " + vec3d_.z);
        //this.setVelocity(this.getVelocity().add(new Vec3d(vx1,vy1,vz1)));
        //this.setVelocity(vx1, vy1, vz1);
        //setVelocity(vx1, vy1, vz1);
        /*System.out.println("VEL SOL x: " + getVelocity().x + " VEL SOL y: " + getVelocity().y + " VEL SOL z: " + getVelocity().z);
        setVelocity(getVelocity().x  * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F), getVelocity().y, getVelocity().z  * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F));
        System.out.println("VEL POSTSOL x: " + getVelocity().x + " VEL POSTSOL y: " + getVelocity().y + " VEL POSTSOL z: " + getVelocity().z);


        //System.out.println("movementInputZ: " + movementInputZ);
       // double vx1 = (this.getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        final double mX = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
        final double mZ = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F);


        System.out.println("mX: " + mX);
        final double result =  startPosX + (1 / (1 - q)) * (Math.pow(q, t) * (-startVelX - (mX * q) / (1 - q)) - t * mX - mX + startVelX + mX / (1 - q));
        System.out.println("traced x for tick="+tickCounter+": " + result);
        System.out.println("real x: " + mod.getPlayer().getPos().x);

        final double trackedTick = (startPosX * (-q) + startPosX + q * mod.getPlayer().getPos().x - mod.getPlayer().getPos().x) / mX;
        System.out.println("tracked x tick: " + (trackedTick));//((int)Math.ceil(trackedTick)));

        final double trackedTick2 = (- (startPosX * q - startPosX - q * mod.getPlayer().getPos().x + mod.getPlayer().getPos().x - startVelX) / (startVelX)) / (Math.log(q));
        System.out.println("other tracked x tick: " + (trackedTick2));//((int)Math.ceil(trackedTick)));

        final double endPosX = mod.getPlayer().getPos().x;

        final double a = (endPosX - startPosX) * (1 - q) + mX - startVelX - mX / (1 - q);
        final double b = -startVelX - mX * q / (1 - q);
        final double c = -mX;

        final double newTracePosXByTick = startPosX + startVelX * ((1 - Math.pow(q,t)) / (1-q)) - (mX*t) / (1-q) + (mX / (1-q)) * ((1-Math.pow(q,t+1)) / (1-q)) - (mX / (1-q));
        //System.out.println("newTracePosXByTick x: " + (newTracePosXByTick));
        final double newTracePosXByTick2 = startPosX + 1 / (1 - q) * (startVelX * (1 - Math.pow(q, t)) + mX * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1));
        System.out.println("newTracePosXByTick2 x: " + (newTracePosXByTick2));

        System.out.println("real z: " + mod.getPlayer().getPos().z);
        final double newTracePosZByTick = startPosZ + startVelZ * ((1-Math.pow(q,t))/(1-q)) + ((mZ*t)/(1-q)) - ((mZ)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + mZ/(1-q);//startPosZ + f(startVelZ, q, mX, (int)t);//startPosZ + startVelZ * sum(1,(int)t,q) + m * (1 / (1-q)) * (n sum(1,(int)t, 1-Math.pow());//
        System.out.println("newTracePosZByTick x: " + (newTracePosZByTick));
        //final double newTracePosZByTick2 = startPosZ + 1 / (1 - q) * (startVelX * (1 - Math.pow(q, t)) - m * (t + Math.pow(q, t + 1)));
        //System.out.println("newTracePosZByTick2 x: " + (newTracePosZByTick2));
        System.out.println("traced y: " + CSAlgorithmY.tracePlayerPosY(startVelY, startPosY, t) + " vs real y: " + mod.getPlayer().getPos().y);*/

        // System.out.println("neglectableLimit for X: " + neglectableSpeedLimitUsage(startPosX, mX, q, startVelX, Axis.X, tickCounter, 0));
        //mod.getPlayer().getBlockPos()
        final double mX = CSAlgorithm.getM(Axis.X, startYaw, startPos, mod.getWorld(), mod.getPlayer().getBoundingBox(), mod.getPlayer().isSprinting());
        final double mZ = CSAlgorithm.getM(Axis.Z, startYaw, startPos, mod.getWorld(), mod.getPlayer().getBoundingBox(), mod.getPlayer().isSprinting());
        System.out.println("Current PX: " + mod.getPlayer().getPos().x + " Current PZ: " + mod.getPlayer().getPos().z + " Current VelX: " + mod.getPlayer().getVelocity().x + " Current VelZ: " + mod.getPlayer().getVelocity().z + " Our mX: " + mX + " Our mZ: " + mZ);
        System.out.println("neglectableLimit2 for X next tick: " + neglectableSpeedLimitUsage(startPosX_2, mX, q, Axis.X, tickCounter, 0));
        System.out.println("neglectableLimit2 for Z next tick: " + neglectableSpeedLimitUsage(startPosZ_2, mZ, q, Axis.Z, tickCounter, 0));

        /* ------------ USEFUL ------------
        System.out.println("Traced DeltaVel: " + deltaVelFormular(tickCounter, Axis.X, 0, q, m));
        System.out.println("Traced Vel: " + velFormular(tickCounter, Axis.X, 0, q, m));
        System.out.println("Traced Vel2: " + velFormular(0, Axis.X, 0, q, m));
        System.out.println("Traced: " +(startPosX_2+CSAlgorithm.tracePlayerPosXDelta(m, q, 0, tickCounter)));
        System.out.println("Traced2: " +(startPosX_2+CSAlgorithm.tracePlayerPosXDelta(m, q, 0, tickCounter)));
        //tracePlayerPosX(final double currentVelX, final double currentPosX, final double deltaTick, final boolean ON_GROUND, final boolean IS_RUNNING, final float SLIPPERINESS , final float startYaw) {
        System.out.println("Traced3: " +(CSAlgorithm.tracePlayerPosX(0, startPosX_2, tickCounter, true, true, 0.6f, startYaw)));
        ----------------USEFUL-----------------*/
        /*System.out.println("Traced DeltaVel: " + deltaVelFormular(tickCounter, Axis.Z, 0, q, mZ));
        System.out.println("Traced Vel: " + velFormular(tickCounter, Axis.Z, 0, q, mZ));
        System.out.println("Traced Vel2: " + velFormular(0, Axis.Z, 0, q, mZ));
        System.out.println("Traced: " +(startPosZ_2+CSAlgorithm.tracePlayerPosZDelta(mZ, q, 0, tickCounter)));
        System.out.println("Traced2: " +(startPosZ_2+CSAlgorithm.tracePlayerPosZDelta(mZ, q, 0, tickCounter)));
        //tracePlayerPosX(final double currentVelX, final double currentPosX, final double deltaTick, final boolean ON_GROUND, final boolean IS_RUNNING, final float SLIPPERINESS , final float startYaw) {
        System.out.println("Traced3: " +(CSAlgorithm.tracePlayerPosZ(0, startPosZ_2, tickCounter, true, true, 0.6f, startYaw)));*/



        //final double mXX = CSAlgorithm.getM(Axis.X, startYaw, mod.getPlayer().getPos().x, mod.getWorld(), mod.getPlayer().getBoundingBox(), true);
        //double vxB = SpeedRecreation2_3.velFormular(tC-1, Axis.X, 0, q, mXX);
        //System.out.println("real x: " + mod.getPlayer().getPos().x);
        //System.out.println("start x: " + startPosX);
        //System.out.println("default track: " + velFormular(tickCounter, Axis.X, 0, q, m));
        //System.out.println("extra track: " + (m * q + m * Math.pow(q, 2) + velFormular(tickCounter, Axis.X, 0, q, m)));
        //System.out.println("extra2 track: " + (velFormular(tickCounter, Axis.X, m * q + m * Math.pow(q, 2), q, m)));
        //f(double v, float q, double m, double n) {
        //System.out.println("traceXViaLoop: " + f(startPosX, startVelX, q, mX, t));
        System.out.println("//////////////////////////////////////");
        /*final double mX = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
        System.out.println("neglectableLimit2 for X: " + neglectableSpeedLimitUsage(startPosX, mX, q, Axis.X, tickCounter, 0));
        System.out.println("real x: " + mod.getPlayer().getPos().x);
        System.out.println("start x: " + startPosX);
        System.out.println("default track: " + velFormular(tickCounter, Axis.X, 0, q, mX));
        System.out.println("extra track: " + (mX * q + mX * Math.pow(q, 2) + velFormular(tickCounter, Axis.X, 0, q, mX)));
        System.out.println("extra2 track: " + (velFormular(tickCounter, Axis.X, mX * q + mX * Math.pow(q, 2), q, mX)));*/
        tickCounter++;
    }


}
