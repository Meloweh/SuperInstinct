/**
 * @author Meloweh, Welomeh
 */

package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;

import adris.altoclef.tasksystem.Task;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlindWalkWithSpeedTestTask extends Task {
    int startingCounter = 0;
    final BlockPos target;
    boolean finished;
    private Vec3d prevPos;
    boolean isSecondCycle;
    final Random rand = new Random();
    int counter = 0;
    boolean isStarting = true;

    Vec3d startVel;

    float startYaw;

    Vec3d startPos;
    Vec3d endPos;
    double tmp_pos;

    Rotation rot;

    double prevDist = 0;

    int tickCounter = 0;

    float pitchCounter = 0;

    Vec3d v0;

    public BlindWalkWithSpeedTestTask(final BlockPos target) {
        this.target = target;
        this.finished = false;
    }

    Vec3d getDeltaPos(final Vec3d currentPos) {
        return new Vec3d(Math.abs(Math.abs(currentPos.getX())) - Math.abs(prevPos.getX()),
                Math.abs(Math.abs(currentPos.getY())) - Math.abs(prevPos.getY()),
                Math.abs(Math.abs(currentPos.getZ())) - Math.abs(prevPos.getZ()));
    }

    double get2DAbsoluteSpeed(final double x, final double z) {
        return Math.abs(Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)));
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static final double DEG_TO_RAD = Math.PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / Math.PI;
    public static Rotation wrapAnglesToRelative(Rotation current, Rotation target) {
        if (current.yawIsReallyClose(target)) {
            return new Rotation(current.getYaw(), target.getPitch());
        }
        return target.subtract(current).normalize().add(current);
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    public static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest, Rotation current) {
        return wrapAnglesToRelative(current, calcRotationFromVec3d(orig, dest));
    }

    /**
     * @author Brady
     * @since 9/25/2018
     */
    private static Rotation calcRotationFromVec3d(Vec3d orig, Vec3d dest) {
        double[] delta = {orig.x - dest.x, orig.y - dest.y, orig.z - dest.z};
        double yaw = Math.atan2(delta[0], -delta[2]);
        double dist = Math.sqrt(delta[0] * delta[0] + delta[2] * delta[2]);
        double pitch = Math.atan2(delta[1], dist);
        return new Rotation(
                (float) (yaw * RAD_TO_DEG),
                (float) (pitch * RAD_TO_DEG)
        );
    }
    double res3; double prevVelX = 0; List<Double> uhList = new ArrayList<>();
    private boolean walkStepTowardsPoint(final AltoClef mod, final Vec3d target) {
        System.out.println("//////////////////////////////////////////");
        //if (mod.getPlayer().prevYaw != neededYaw || mod.getPlayer().prevPitch != neededPitch) {
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());

        if (target.distanceTo(eyeCenter) < 0.5d) {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
            return true;
        }

        /*final Vec3d deltaVec = eyeCenter.subtract(target);
        final Double yaw = Math.atan2(deltaVec.getZ(), deltaVec.getX());
        final Double pitch = Math.atan2(Math.sqrt(deltaVec.getZ() * deltaVec.getZ() + deltaVec.getX() * deltaVec.getX()), deltaVec.getY()) + Math.PI;
        neededYaw = yaw.floatValue();
        neededPitch = pitch.floatValue();*/
        rot = calcRotationFromVec3d(eyeCenter, target);

        //}
        //System.out.println(mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.SPRINT));

        if (counter > 60) {
            counter = 0;
            //System.out.println(mod.getPlayer().getYaw());
            //mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(mod.getPlayer().getYaw(), rand.nextInt(180)-90 + rand.nextFloat()), true);
            //mod.getPlayer().setPitch(rand.nextInt(180)-90 + rand.nextFloat());

            //System.out.println(mod.getPlayer().getYaw());///tp @a 11487.15 4 1609    ///tp @a 1609 4 11487.15 /tp @a -160.9 4 -1148.715
        } else {
            counter++;
        }
        //tickCounter++;

        if (!isStarting) {
            /*Vec2f start2 = new Vec2f((float)startPos.x, (float)startPos.z);
            Vec2f end2 = new Vec2f((float)endPos.x,(float)endPos.z);
            Vec2f sub = end2.add(new Vec2f(-start2.x, -start2.y));
            Vec2f n = new Vec2f(-sub.y, sub.x);
            double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.y, 2));
            Vec2f otherSub = start2.add(new Vec2f((float)mod.getPlayer().getPos().x, (float)mod.getPlayer().getPos().z));
            double m = otherSub.y / otherSub.x;
            double nAsDouble = n.y / n.x;
            double res = m * nd * nAsDouble;
            double abs = Math.abs(res);
            if (Double.compare(abs, prevDist) > 0) {
                System.out.println(abs + " m: " + abs / prevDist); prevDist = abs;
            }*/

            /*
            pitchCounter += 0.5f;
            if (pitchCounter > 90) pitchCounter = -90;
            System.out.println("pitchCounter: " + pitchCounter);
            mod.getPlayer().setPitch(pitchCounter);*/

            /*
            Vec2f start2 = new Vec2f((float)startPos.x, (float)startPos.z);
            Vec2f end2 = new Vec2f((float)endPos.x,(float)endPos.z);
            Vec2f sub = end2.add(new Vec2f(-start2.x, -start2.y));
            Vec2f n = new Vec2f(-sub.y, sub.x);
            double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.y, 2));
            Vec2f x1minusx0 = mod.getPlayer().getPos().add(start2.x)
            //Vec2f otherSub = start2.add(new Vec2f((float)mod.getPlayer().getPos().x, (float)mod.getPlayer().getPos().z));
            //double m = otherSub.y / otherSub.x;
            //double nAsDouble = n.y / n.x;
            double res = m * nd * nAsDouble;
            double abs = Math.abs(res);

            if (Double.compare(abs, prevDist) > 0) {
                System.out.println(abs + " m1: " + abs / prevDist); prevDist = abs;
            }*/ /*else {
                if (Double.compare(abs, prevDist2) < 0)
                System.out.println("m2: " + prevDist2 / abs);
            }*/

            Vec3d sub = endPos.subtract(startPos);
            Vec3d n = new Vec3d(-sub.z, 0, sub.x);
            double nd = 1 / Math.sqrt(Math.pow(n.x, 2) + Math.pow(n.z, 2));
            Vec3d x1minux0 = mod.getPlayer().getPos().subtract(startPos);
            double result = x1minux0.x * n.x * nd + x1minux0.z * n.z * nd;
            System.out.println("SVM: " + result);
            System.out.println(" diff: " + ((Math.abs(result) / prevDist > 1) ? "pre<curr" + Math.abs(result) / prevDist : "curr<pre" + prevDist / Math.abs(result)));
            prevDist = Math.abs(result);

            final boolean bl = mod.getPlayer().getVelocity().x * MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F)
                    <= -mod.getPlayer().getVelocity().z * MathHelper.sin((mod.getPlayer().getYaw() * 0.017453292F));
            String msg = "monoton " + (bl ? "fallend" : "steigend");
            System.out.println(msg);

            System.out.println("spx: " + startPos.getX() + " spz: " + startPos.getZ());
            System.out.println("epx: " + endPos.getX() + " epz: " + endPos.getZ());
            System.out.println("pvx: " + mod.getPlayer().prevX+  " pvz: " + mod.getPlayer().prevZ);
            //System.out.println("vx: " + mod.getPlayer().getPos().getX() + " vy: " +" vz: " + mod.getPlayer().getPos().getZ());
            //System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());
            System.out.println("px: " + mod.getPlayer().getPos().x);

            //BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
            //float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
            //double movementInputZ = mod.getPlayer().forwardSpeed;
            //final double mX = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().flyingSpeed) * (double) MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
            //final double mZ = movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().flyingSpeed) * (double)MathHelper.cos(mod.getPlayer().getYaw() * 0.017453292F);
            //System.out.println("traced x: " + 0.6 * 0.91*(mod.getPlayer().getVelocity().x - movementInputZ * (mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().flyingSpeed) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F)));
            //System.out.println("mX=" + mX + " mZ=" + mZ);
            //double q = mod.getPlayer().isOnGround() ? slipperiness * 0.91F : 0.91F;
            //System.out.println("q: " + q);
            //System.out.println("m*q: " + mX * q);

            final boolean IS_RUNNING = true;
            final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
            final boolean ON_GROUND = true;
            final float SLIPPERINESS = 0.6f;
            float q = ON_GROUND ? SLIPPERINESS * 0.91F : 0.91F;
            final float DEFAULT_FLYING_SPEED = IS_RUNNING ? 0.025999999f : 0.02f;
            final double INPUT_CONSTANT = 0.9800000190734863;
            //final float YAW = mod.getPlayer().getYaw();
            double res3 = (- INPUT_CONSTANT * MOVEMENT_SPEED * MathHelper.sin(rot.getYaw() * 0.017453292F) * ((SLIPPERINESS * 0.91F) +1.0d)) * (SLIPPERINESS * 0.91F);
            System.out.println("MAYBE startVelX traced: " + res3);
            double res3Z = (INPUT_CONSTANT * MOVEMENT_SPEED * MathHelper.cos(rot.getYaw() * 0.017453292F) * ((SLIPPERINESS * 0.91F) +1.0d)) * (SLIPPERINESS * 0.91F);
            System.out.println("MAYBE startVelZ traced: " + res3Z);

            final boolean monotonie = res3 * MathHelper.cos(rot.getYaw() * 0.017453292F) <= res3Z * MathHelper.sin(rot.getYaw() * 0.017453292F);
            System.out.println("Monoton " + (monotonie ? "fallend" : "steigend"));
            /*final double mX = INPUT_CONSTANT * (ON_GROUND ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(rot.getYaw() * 0.017453292F);
            final double POSITION_TRACKER = startPos.x + mod.getPlayer().getVelocity().x * ((1 - Math.pow(q,tickCounter)) / (1-q)) - (mX*tickCounter) / (1-q) + (mX / (1-q)) * ((1-Math.pow(q,tickCounter+1)) / (1-q)) - (mX / (1-q));
            System.out.println("POSITION_TRACKER: " + POSITION_TRACKER);*/
            double a = Math.atan2(endPos.x - startPos.x, endPos.z - startPos.z);
            System.out.println("alpha: " + a + " deg: " + Math.toDegrees(a));
            System.out.println("yaw: " + mod.getPlayer().getYaw() + " pitch: " + mod.getPlayer().getPitch());
            //double abs = Math.abs(res);
            //System.out.println(res);

            final double mX = INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(startYaw * 0.017453292F);
            System.out.println("mX: " + mX + " m/q-1: " + (mX/(q-1)));
            final double mZ = INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.cos(startYaw * 0.017453292F);
            System.out.println("mZ: " + mZ);
            final double result2 =  startPos.x + (1 / (1 - q)) * (Math.pow(q, tickCounter) * (-startVel.x - (mX * q) / (1 - q)) - tickCounter * mX - mX + startVel.x + mX / (1 - q));
            System.out.println("traced x2 for tick="+tickCounter+": " + result2);

            double vx1 = SpeedRecreation2.debugNonDeltaVelFormular(tickCounter+++1, Axis.X, startVel.x, q, mX);
            //double vx1 = (prevVelX - INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(startYaw * 0.017453292F)) * q;
            tickCounter--;
            double uh = SpeedRecreation2.otherVelFormularTest(1, Axis.X, prevVelX, q, mX);
            uhList.add(uh);
            //System.out.println(uh);
            //System.out.println("traced velx for tick="+tickCounter+": " + vx1);
            //System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());
            System.out.println(uh);
            prevVelX = mod.getPlayer().getVelocity().x;
            final double vx = mod.getPlayer().getVelocity().getX();

            // b = p_sx
            // a = p_sz
            // c = v_sx
            // o = v_sz
            // k = p_px
            // u = p_pz
            // f = v_px
            // g = v_pz
            // n = m_x
            // h = m_z
            // x=b+(1/(q-1))*(q^(1+ln((u+100*g-z)/(100*g))/(ln(100/99))*(c-n*q/(q-1))-c+n*()  )  )
            // x=b+(1/(q-1))*(q^(1+ln((u+100*g-z)/(100*g))/(ln(100/99)))*(c-n*q/(q-1))-c+n*(ln((u+100*g-z)/(100*g))/(ln(100/99))+1-q/(q-1)))

            System.out.println("traced velx for tick="+tickCounter+": " + vx1);
            System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());

            if (Double.compare(vx, vx1)!= 0){
                System.out.println("--------------is different-------------------");

                System.out.println("traced velx for tick="+tickCounter+": " + vx1);
                System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());
            }

            if (!uhList.contains(vx)) {
                System.out.println("****************+VERY DIFFERENT***************");
                System.out.println("traced velx for tick="+tickCounter+": " + vx1);
                System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());
            }

            double pos1 = startPos.x;
            for (int i=1; i<=tickCounter+1; i++){
                //pos1 += (mod.getPlayer().getVelocity()).x * Math.pow(q, i-1) - mX * (1-Math.pow(q, i))/(1-q);
                //v_0 * Math.pow(q, t-1) - m * ((1-Math.pow(q,t)) / (1-q));
               // pos1 += SpeedRecreation2.velFormular(i, Axis.X, startVel.x, q, mX);
                pos1 += (startVel.x) * Math.pow(q, i-1) - mX * ((1-Math.pow(q,i)) / (1-q));
            }
            double test_pos = CSAlgorithm.tracePlayerPosX(startVel.x, startPos.x, tickCounter, ON_GROUND, IS_RUNNING, SLIPPERINESS, startYaw);
            System.out.println("POS_Korrekt for tick " + tickCounter + ": " + (mod.getPlayer().getPos()).x);
            System.out.println("pos with new method: " + test_pos);
            double posz = CSAlgorithm.tracePlayerPosZ(startVel.z, startPos.z, tickCounter, ON_GROUND, IS_RUNNING, SLIPPERINESS, startYaw);
            System.out.println("correct pos z: " + (mod.getPlayer().getPos()).z);
            System.out.println("our pos z: " + posz);
            double pos0 = startPos.x + startVel.x * (Math.pow(q, tickCounter+1) - 1) / (q-1) - mX / (1-q) * (tickCounter + 1 - (q * (Math.pow(q, tickCounter+1)-1))/(q-1));
            double pos0_schöner = startPos.x + startVel.x * (Math.pow(q, tickCounter+1) - 1) / (q-1) - mX / (1-q) * (tickCounter + 1 - (Math.pow(q, tickCounter+2)-q)/(q-1));
            double pos0_stabiler = startPos.x + startVel.x * (Math.pow(q, tickCounter+1) - 1) / (q-1) + mX / (q-1) * (tickCounter + 1 - (Math.pow(q, tickCounter+2)-q)/(q-1));
            //doch nicht, macht keinen Stabilitätsunterschied :(
            double pos2 = startPos.x + startVel.x * (1-Math.pow(q, tickCounter+1)) / (1-q) - mX * tickCounter / (1-q) + mX / (1-q) * (1 - Math.pow(q, tickCounter+2)) / (1-q) - mX / (1-q);
            double pos3 = startPos.x + startVel.x * (1-Math.pow(q, tickCounter)) / (1-q) - mX * tickCounter / (1-q) + mX / (1-q) * (1 - Math.pow(q, tickCounter+1)) / (1-q) - mX / (1-q);
            double pos4 = startPos.x + startVel.x * (1-Math.pow(q, tickCounter-1)) / (1-q) - mX * tickCounter / (1-q) + mX / (1-q) * (1 - Math.pow(q, tickCounter)) / (1-q) - mX / (1-q);
            double pos5 = startPos.x + startVel.x * (1-Math.pow(q, tickCounter-2)) / (1-q) - mX * tickCounter / (1-q) + mX / (1-q) * (1 - Math.pow(q, tickCounter-1)) / (1-q) - mX / (1-q);
            tmp_pos = tmp_pos + SpeedRecreation2.velFormular(tickCounter+1, Axis.X, startVel.x, q, mX);
            System.out.println("here for tick " + tickCounter);
   //         System.out.println("start Pos: " + startPos.x);
     //       System.out.println("velx: " + mod.getPlayer().getVelocity().getX());
       //     System.out.println("our vel x" + SpeedRecreation2.debugNonDeltaVelFormular(tickCounter+1, Axis.X, startVel.x, q, mX));
           // System.out.println("better for our vel x: " + SpeedRecreation2.velFormular253(tickCounter+1, Axis.X, startVel.x, q, mX));
            //System.out.println("delta vel x " + SpeedRecreation2.velFormular(tickCounter+1, Axis.X, startVel.x, q, mX));
           // System.out.println("tmp pos: " + tmp_pos);
         //   System.out.println("POS0: " + pos0);
          //  System.out.println("POS0 V2: " + pos0_schöner);
         //   System.out.println("POS0 vielleicht stabiler?: " + pos0_stabiler);
         //   System.out.println("POS3: " + pos3);
        //    System.out.println("POS4: " + pos4);
       //     System.out.println("POS5: " + pos5);

            tickCounter++;
            /*startPos = new Vec3d(1, 0, 1.5);
            endPos = new Vec3d(2,0,1);
            final Vec3d support = endPos.subtract(startPos);
            final Vec3d n = new Vec3d(-support.getZ(), 0, support.getX());
            //final double nDach = n.getX() * Math.sqrt(Math.pow(n.getX(),2) + Math.pow(n.getZ(), 2)) +
            final Vec3d sub = mod.getPlayer().getPos().subtract(startPos);
            final Vec3d mul = sub.multiply(n);
            final double div = mul.getZ() / mul.getX();
            final double divnormal = div / Math.sqrt(Math.pow(n.getX(),2)+Math.pow(n.getZ(),2));
            //final double div = n.getZ() / n.getX();
            System.out.println(divnormal);
            startPos.normalize()*/

        }

        if (!mod.getClientBaritone().getInputOverrideHandler().isInputForcedDown(Input.MOVE_FORWARD)) {
            //mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(rot.getYaw(), rot.getPitch()), true);


            //double a = Math.atan2(endPos.x - startPos.x, endPos.z - startPos.z);
            //System.out.println("alpha: " + a + " deg: " + Math.toDegrees(a));
            //mod.getPlayer().setYaw((float)Math.toDegrees(a)); mod.getPlayer().setPitch(rot.getPitch());
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, true);
            //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            //BaritoneAPI.getProvider().getPrimaryBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, true);
            mod.getPlayer().setSprinting(true);
        } else if (isStarting) {
            final boolean IS_RUNNING = true;
            final float MOVEMENT_SPEED = IS_RUNNING ? 0.13000001f : 0.1f;
            final boolean ON_GROUND = true;
            final float SLIPPERINESS = 0.6f;
            float q = ON_GROUND ? SLIPPERINESS * 0.91F : 0.91F;
            final float DEFAULT_FLYING_SPEED = IS_RUNNING ? 0.025999999f : 0.02f;
            final double INPUT_CONSTANT = 0.98000001;
            final float YAW = mod.getPlayer().getYaw();
            final double SPEED = ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED;
            final double newInput = INPUT_CONSTANT * SPEED;
            final double dx = newInput * MathHelper.sin(YAW * 0.017453292F);
            final double ddx = dx * q;

            System.out.println("YAW: " + YAW + " vs " + mod.getPlayer().getYaw());
            final double xres = (ddx + dx) * q;
            System.out.println("XRES: " + xres);
            double vx1 = ((0 - INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(YAW * 0.017453292F)) * q + (- INPUT_CONSTANT * (ON_GROUND ? MOVEMENT_SPEED * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(YAW * 0.017453292F))) * q;
            System.out.println("XRES2: " + vx1);


            startVel = mod.getPlayer().getVelocity();

            res3 = (- CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * CSAlgorithm.DEFAULT_MOVEMENT_SPEED * MathHelper.sin(rot.getYaw() * 0.017453292F) * ((SLIPPERINESS * 0.91F) +1.0d)) * (SLIPPERINESS * 0.91F);
            final double mX = CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * (ON_GROUND ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : CSAlgorithm.DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(rot.getYaw() * 0.017453292F);
            final double POSITION_TRACKER = startPos.x + mod.getPlayer().getVelocity().x * ((1 - Math.pow(q,tickCounter)) / (1-q)) - (mX*tickCounter) / (1-q) + (mX / (1-q)) * ((1-Math.pow(q,tickCounter+1)) / (1-q)) - (mX / (1-q));
            System.out.println("POSITION_TRACKER: " + POSITION_TRACKER);


            //IZ * (ON_GROUND ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : FLY_SPEED) * sin(yaw * 0.017453292F);
            //0.98000001 * (ON_GROUND ? (IS_RUNNING ? 0.13000001f : 0.1f) * (0.21600002F / slip³) : (IS_RUNNING ? 0.025999999f : 0.02f)) * sin(yaw * 0.017453292F);

            //0.98000001 * (ON_GROUND ? (IS_RUNNING ? 0.13000001f : 0.1f) * (0.21600002F / (0.6 oder 0.8 oder 0.98)³) : (IS_RUNNING ? 0.025999999f : 0.02f)) * sin(yaw * 0.017453292F);

            //final double pos2 = startPos.x + (mod.getPlayer().getVelocity()).x * (1-Math.pow(q, tickCounter))/ (1-q) - mX * (1/(1-q)) * (tickCounter - (1-Math.pow(q, tickCounter+1))/ (1-q) + 1);
            /*double pos2 = startPos.x;
            for (int i=1; i<=tickCounter; i++){
                pos2 += (mod.getPlayer().getVelocity()).x * Math.pow(q, i-1) - mX * (1-Math.pow(q, i))/(1-q);
            }
            System.out.println("POS2: " + pos2);
            System.out.println("POS_Korrekt: " + (mod.getPlayer().getPos()).x);*/
            //mod.getPlayer().setPitch(-90);
            startPos = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());
            endPos = new Vec3d(0.5d, mod.getPlayer().getEyeY(), 0.5d);
            isStarting = false;
            tmp_pos = startPos.x;
            t(mod);
        }
        return false;
    }

    private boolean walkStepTowardsBlock(final AltoClef mod, final BlockPos blockPos) {
        return walkStepTowardsPoint(mod, new Vec3d(blockPos.getX() + 0.5d, mod.getPlayer().getEyeY(), blockPos.getZ() + 0.5d));
    }

    void print(final AltoClef mod) {
        final Vec3d delta = getDeltaPos(mod.getPlayer().getPos());
        System.out.println("p: " + get2DAbsoluteSpeed(delta.getX(), delta.getZ()));
    }

    void t(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float slipperiness = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        double movementInputZ = mod.getPlayer().forwardSpeed;
        double vx1 = (0 - 0.98 * (mod.getPlayer().isOnGround() ? 0.13000001f * (0.21600002F / (0.6 * 0.6 * 0.6)) : 0.025999999f) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F));
        vx1 *= 0.6 * 0.91;
        vx1 += 0.98 * (mod.getPlayer().isOnGround() ? 0.13000001f * (0.21600002F / (0.6 * 0.6 * 0.6)) : 0.025999999f) * (double)MathHelper.sin(mod.getPlayer().getYaw() * 0.017453292F);
        vx1 *= 0.6 * 0.91;
        System.out.println("First velx =" + vx1);
    }

    @Override
    protected void onStart(AltoClef mod) {///tp @a 432 4 446 //

        isSecondCycle = false;
        prevPos = mod.getPlayer().getPos();
        final Vec3d playerCenter = mod.getPlayer().getBoundingBox().getCenter();
        final Vec3d eyeCenter = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());
        startPos = new Vec3d(playerCenter.getX(), mod.getPlayer().getEyeY(), playerCenter.getZ());
        endPos = new Vec3d(0.5d, mod.getPlayer().getEyeY(), 0.5d);
        System.out.println("-----------------ON START---------------");
        rot = calcRotationFromVec3d(eyeCenter, new Vec3d(target.getX() + 0.5d, mod.getPlayer().getEyeY(), target.getZ() + 0.5d));
        System.out.println("ROT: " + rot.getYaw());
        mod.getPlayer().setYaw(rot.getYaw());
        //mod.getPlayer().setPitch(rot.getPitch());
        startYaw = rot.getYaw();
        //mod.getClientBaritone().getLookBehavior().updateTarget(new Rotation(rot.getYaw(), rot.getPitch()), true);
        System.out.println("ROT2: " + rot.getYaw());
        //System.out.println("ROT3: " + ((Entity)mod.getPlayer()).getYaw());
        final float SLIPPERINESS = 0.6F;
        double res2 = (- CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * CSAlgorithm.DEFAULT_MOVEMENT_SPEED * (float) MathHelper.sin(rot.getYaw() * 0.017453292F) * ((float) (SLIPPERINESS* 0.91F)+1.0d)) * (float) (SLIPPERINESS * 0.91F);
        System.out.println("res2: " + res2);
        double res3 = (- CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * CSAlgorithm.DEFAULT_MOVEMENT_SPEED * MathHelper.sin(rot.getYaw() * 0.017453292F) * ((SLIPPERINESS * 0.91F) +1.0d)) * (SLIPPERINESS * 0.91F);
        System.out.println("res3: " + res3);

        double squared = CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * CSAlgorithm.DEFAULT_MOVEMENT_SPEED;
        float s = MathHelper.sin(rot.getYaw() * 0.017453292F);
        double subres = -squared * s;

        final boolean ON_GROUND = true;

        System.out.println(s);

        //double subres2 = subres + subres;
        System.out.println(subres);

        final float q = SLIPPERINESS * 0.91F;
        System.out.println(q);

        double resA = subres * q;
        double resB = (resA + subres) * q;

        System.out.println("resB: " + resB);

        //double vx1 = (res3 - CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * (ON_GROUND ? CSAlgorithm.DEFAULT_MOVEMENT_SPEED * (0.21600002F / (0.6F * 0.6F * 0.6F)) : CSAlgorithm.DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(rot.getYaw() * 0.017453292F));
        final double mX = CSAlgorithm.DEFAULT_MOVEMENT_INPUT_Z * (ON_GROUND ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (SLIPPERINESS * SLIPPERINESS * SLIPPERINESS)) : CSAlgorithm.DEFAULT_FLYING_SPEED) * (double)MathHelper.sin(rot.getYaw() * 0.017453292F);
        final double POSITION_TRACKER = startPos.x + mod.getPlayer().getVelocity().x * ((1 - Math.pow(q,tickCounter)) / (1-q)) - (mX*tickCounter) / (1-q) + (mX / (1-q)) * ((1-Math.pow(q,tickCounter+1)) / (1-q)) - (mX / (1-q));
        System.out.println("POSITION_TRACKER: " + POSITION_TRACKER);

        System.out.println("spx: " + startPos.getX() + " spz: " + startPos.getZ());
        System.out.println("epx: " + endPos.getX() + " epz: " + endPos.getZ());
        //mod.getPlayer().prev
        //System.out.println("pvx: " + mod.getPlayer().prev+  " pvz: " + mod.getPlayer().prevZ);
        System.out.println("vx: " + mod.getPlayer().getVelocity().getX() + " vy: " +" vz: " + mod.getPlayer().getVelocity().getZ());
        startingCounter = 0;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (startingCounter > 100) {
            finished = walkStepTowardsBlock(mod, target);
            //print(mod);
            prevPos = mod.getPlayer().getPos();
        } else {
            startingCounter++;
            System.out.println(startingCounter);
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD, false);
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SPRINT, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return finished || Math.abs(mod.getPlayer().getPos().getX()) < 0.5 || Math.abs(mod.getPlayer().getPos().getZ()) < 0.5;//finished || (mod.getPlayer().getX() < 0 || mod.getPlayer().getZ() < 0) && mod.getPlayer().squaredDistanceTo(0, 4, 0) < 1;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BlindWalkToPointTask;
    }

    @Override
    protected String toDebugString() {
        return "Blind Walk";
    }
}
