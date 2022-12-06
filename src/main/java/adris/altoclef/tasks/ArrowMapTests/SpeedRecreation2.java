/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SpeedRecreation2 {
    public double startVelY;
    public double startPosY;
    Vec3d velocity;
    Vec3d position;
    double startPosX, startPosZ;
    double startVelX, startVelZ;
    int tickCounter = 0;

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

    double f(double start, double v, double q, double m, double n) {
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

        public Crossing(final Side side, final Optional<Double> tick, final Axis axis, final double q, final double v_0) {
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

        private static double getDeltaPos(final Axis axis, final Side side, final double m, final double q, final double v_0, final double endTick) {
            if (axis.equals(Axis.X)) {
                if (side.equals(Side.HIGH)) {
                    return localEnd = v_0 * ((1 - Math.pow(q, endTick)) / (1-q)) - (m * endTick) / (1-q) + (m / (1-q)) * ((1-Math.pow(q, endTick+1)) / (1-q)) - (m / (1-q));
                }

            }

            if (side.equals(Side.HIGH)) {

            }

        }

        final double getCrossingPositionDelta(final double m, final double q, final double v_0) {
            //wenn v drunter dann -m*deltaT ansonsten lange Formel
            return startPosition + getDeltaPos(axis, side, m, q, v_0, endTick);
        }
    }*/

  /*  Crossing neglectableSpeedLimit(final double m, final double q, final double v_0, final Axis axis) {
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

    public static double velFormular(final double t, final Axis axis, final double v_0, final float q, final double m) {
        if (axis.equals(Axis.X)) {
            double r = v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q));
            return r;
        }
        return v_0 * Math.pow(q, t-1) + m * ((1-Math.pow(q,t)) / (1-q));
    }

    public static double otherVelFormularTest(final double tt, final Axis axis, final double v_0, final double q, final double m) {
        double t = 2;
        return (v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q))) * q;
    }

    public static double debugNonDeltaVelFormular(final double t, final Axis axis, final double v_0, final float q, final double m) {
        return velFormular(t, axis, v_0, q, m) * q;
    }

    public static double velRecFormular(final double t, final Axis axis, final double v_0, final float q, final double m) {
        // works
        double prevV = v_0;
        double v = v_0;
        for (int i = 1; i <= t; i++) {
            v = (prevV - m) * q;
            prevV = v;
        }
        return v;
    }

    public static double velRecFormularDup(final double t, final Axis axis, final double v_0, final double q, final double m) {
        double prevV = v_0;
        double v = v_0;
        for (int i = 1; i <= t; i++) {
            v = (prevV - m) * q;
            prevV = v;
        }
        return v;
    }

    public static double velFormular253(final double t, final Axis axis, final double v_0, final double q, final double m) {
        double prevV = v_0;
        double v = v_0;
        for (int i = 1; i <= t; i++) {
            v = (prevV - m) * q;
            prevV = v;
        }
        return v;
    }

    public static double floatPow(double a, int b){
        double res = 1.0f;
        for (int i = 0; i<b; i++){
            res = res * a;
        }
        return res;
    }
    public static double velFormularUhAnders(final double t, final Axis axis, final double v_0, final double q, final double m) {
        double qq = 0;
        for (int i = 0; i < t; i++) {
            qq += floatPow(q, i);
            //System.out.print("qq: " + qq + " qq ~~~ ");
        }
        return (v_0 * floatPow(q, (int)(t-1)) - m * qq) * q;
    }

    int acounter = 0;
    int msum = 0;

    enum VelocityState {
        Positive,
        Middle,
        Negative
    }
/*    double neglectableSpeedLimit(final double m, final double q, final double v_0, final Axis axis, final double endTick, final double walkedTicks) {
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

    final Optional<Double> getIntersectionOutsideToInsideZ(final double m, final double q, final double v_0) {
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

    final Optional<Double> getIntersectionOutsideToInsideX(final double m, final double q, final double v_0) {
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

    final Optional<Double> getIntersectionInsideToOutside(final double m) {
        // m sollte als mX bzw mZ bereits durch den Aufrufer von neglectableSpeedLimit passend weitergegeben sein
        return Double.compare(m, 0d) != 0 ? Optional.of(Math.abs(0.003d / m)) : Optional.empty();
    }

    double neglectableSpeedLimit(final double m, final double q, final double v_0, final Axis axis, final double endTick, final double walkedTicks) {
        if (Double.compare(walkedTicks, endTick) >= 0) return 0;

        final VelocityState velocityState = Double.compare(v_0, 0.003d) > 0 ? VelocityState.Positive :
                Double.compare(v_0, -0.003d) > 0 ? VelocityState.Middle :
                        VelocityState.Negative;

        final Optional<Double> intersection = velocityState.equals(VelocityState.Middle) ? getIntersectionInsideToOutside(m) :
                axis.equals(Axis.X) ? getIntersectionOutsideToInsideX(m,q,v_0) : getIntersectionOutsideToInsideZ(m,q,v_0);

        final double t = intersection.isEmpty() || intersection.get().equals(Double.NaN) ? endTick : intersection.get();
        acounter++;
        msum += m;
        System.out.println("[ngs at " + acounter + ": " + msum + "]");
        final double formular = axis.equals(Axis.X) ?
                !velocityState.equals(VelocityState.Middle) ?
                        1 / (1 - q) * (v_0 * (1 - Math.pow(q, t)) + m * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1))
                        :
                        -m * t
                :
                (!velocityState.equals(VelocityState.Middle)) ?
                        v_0 * ((1-Math.pow(q,t))/(1-q)) + ((m*t)/(1-q)) - ((m)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + m/(1-q)
                        :
                        m * t;


        final double v_0new = velFormular(t, axis, v_0, (float)q, m);
        return formular + neglectableSpeedLimit(m, q, v_0new, axis, endTick, walkedTicks + t);

    }

    double neglectableSpeedLimitUsage(final double startPos, final double m, final double q, final double v_0, final Axis axis, final double endTick, final double walkedTicks) {
        acounter = 0;
        msum = 0;
        return startPos + neglectableSpeedLimit(m, q, v_0, axis, endTick, walkedTicks);
    }
    /*
    Optional<Double> splittedFormular(final double startTick, final double endTick, final double m, final double q, final double v_0, final Axis axis) {
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

    void travel(final AltoClef mod) {
        tickCounter++;
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        //double f = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        //Vec3d movementInput = new Vec3d(0, 0, mod.getPlayer().forwardSpeed);
        double movementInputZ = mod.getPlayer().forwardSpeed;
        final double q = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
        final double t = tickCounter;

        /*final double oldStuff = Math.abs(mod.getPlayer().getVelocity().x * q);
        if (oldStuff < 0.003) {
            setVelocity(0, getVelocity().y, getVelocity().z);
            System.out.println("YE IT*S SMOLL: " + oldStuff);
        }*/


        System.out.println("FROM TRAVEL sidewaysSpeed: " + mod.getPlayer().sidewaysSpeed + " upwardSpeed: " + mod.getPlayer().upwardSpeed + " forwardSpeed: " + mod.getPlayer().forwardSpeed);
        //float speed = mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
        //Vec3d vec3d_ = new Vec3d(- movementInput.multiply(speed).z * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F), movementInput.multiply(speed).y, movementInput.multiply(speed).z * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        double vx1 = (this.getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        double vy1 = ((this.getVelocity().y + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed())) - 0.08) * 0.9800000190734863;
        double vz1 = (this.getVelocity().z + movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F));
        //Vec3d vec3d_ = new Vec3d(vx1,vy1,vz1);

        //System.out.println("FROM TRAVEL vec3d_ sidewaysSpeed: " + vec3d_.x + " upwardSpeed: " + vec3d_.y + " forwardSpeed: " + vec3d_.z);
        //this.setVelocity(this.getVelocity().add(new Vec3d(vx1,vy1,vz1)));
        //this.setVelocity(vx1, vy1, vz1);
        setVelocity(vx1, vy1, vz1);
        System.out.println("VEL SOL x: " + getVelocity().x + " VEL SOL y: " + getVelocity().y + " VEL SOL z: " + getVelocity().z);
        setVelocity(getVelocity().x  * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F), getVelocity().y, getVelocity().z  * (mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F));
        System.out.println("VEL POSTSOL x: " + getVelocity().x + " VEL POSTSOL y: " + getVelocity().y + " VEL POSTSOL z: " + getVelocity().z);


        System.out.println("movementInputZ: " + movementInputZ);
        final double mX = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
        final double mZ = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed()) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F);


        final double result =  startPosX + (1 / (1 - q)) * (Math.pow(q, t) * (-startVelX - (mX * q) / (1 - q)) - t * mX - mX + startVelX + mX / (1 - q));
        System.out.println("traced x for tick="+tickCounter+": " + result);
        System.out.println("real x: " + mod.getPlayer().getPos().x);

        ///////////////////////////

        final double trackedTick = (startPosX * (-q) + startPosX + q * mod.getPlayer().getPos().x - mod.getPlayer().getPos().x) / mX;
        System.out.println("tracked x tick: " + (trackedTick)/*((int)Math.ceil(trackedTick))*/);

        final double trackedTick2 = (- (startPosX * q - startPosX - q * mod.getPlayer().getPos().x + mod.getPlayer().getPos().x - startVelX) / (startVelX)) / (Math.log(q));
        System.out.println("other tracked x tick: " + (trackedTick2)/*((int)Math.ceil(trackedTick))*/);

        final double endPosX = mod.getPlayer().getPos().x;

        final double a = (endPosX - startPosX) * (1 - q) + mX - startVelX - mX / (1 - q);
        final double b = -startVelX - mX * q / (1 - q);
        final double c = -mX;
        /*final double paramForW = (b * Math.pow(q, a / c) * Math.log(q)) / c;
        final double trackedTick3_W0 = (a * Math.log(q) - c * LambertW.branch0(paramForW)) / (c * Math.log(q));



        System.out.println("other tracked x tick W0: " + (trackedTick3_W0));
        if (!(Double.compare(paramForW, 0) > 0)) {
            final double trackedTick3_Wminus1 = (a * Math.log(q) - c * LambertW.branchNeg1(paramForW)) / (c * Math.log(q));
            System.out.println("other tracked x tick WNeg1: " + (trackedTick3_Wminus1));
            System.out.println("detective game: " + (trackedTick3_W0 - trackedTick3_Wminus1));
        }*/

        final double newTracePosXByTick = startPosX + startVelX * ((1 - Math.pow(q,t)) / (1-q)) - (mX*t) / (1-q) + (mX / (1-q)) * ((1-Math.pow(q,t+1)) / (1-q)) - (mX / (1-q));
        System.out.println("newTracePosXByTick x: " + (newTracePosXByTick));
        final double newTracePosXByTick2 = startPosX + 1 / (1 - q) * (startVelX * (1 - Math.pow(q, t)) + mX * (-t + ((1 - Math.pow(q,t+1))/(1-q)) - 1));
        System.out.println("newTracePosXByTick2 x: " + (newTracePosXByTick2));

        System.out.println("real z: " + mod.getPlayer().getPos().z);
        final double newTracePosZByTick = startPosZ + startVelZ * ((1-Math.pow(q,t))/(1-q)) + ((mZ*t)/(1-q)) - ((mZ)/(1-q)) * ((1-Math.pow(q,t+1))/(1-q)) + mZ/(1-q);//startPosZ + f(startVelZ, q, mX, (int)t);//startPosZ + startVelZ * sum(1,(int)t,q) + m * (1 / (1-q)) * (n sum(1,(int)t, 1-Math.pow());//
        System.out.println("newTracePosZByTick x: " + (newTracePosZByTick));
        //final double newTracePosZByTick2 = startPosZ + 1 / (1 - q) * (startVelX * (1 - Math.pow(q, t)) - m * (t + Math.pow(q, t + 1)));
        //System.out.println("newTracePosZByTick2 x: " + (newTracePosZByTick2));
        System.out.println("traced y: " + CSAlgorithmY.tracePlayerPosY(startVelY, startPosY, t) + " vs real y: " + mod.getPlayer().getPos().y);

        //System.out.println("neglectableLimit for X: " + neglectableSpeedLimitUsage(startPosX, mX, q, startVelX, Axis.X, tickCounter, 0));

        //f(double v, double q, double m, double n) {
        //System.out.println("traceXViaLoop: " + f(startPosX, startVelX, q, mX, t));
    }


}
