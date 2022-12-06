/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import adris.altoclef.AltoClef;
import net.minecraft.block.Blocks;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpeedRecreation {
    Vec3d velocity;

    void setVelocity(Vec3d vec) {
        velocity = vec;
    }
    void setVelocity(double x, double y, double z) {
        velocity = new Vec3d(x,y,z);
    }

    Vec3d getVelocity() {
        return velocity;
    }

    private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).multiply((double)speed);
            float f = MathHelper.sin(yaw * 0.017453292F);
            float g = MathHelper.cos(yaw * 0.017453292F);
            return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
        }
    }

    public void updateVelocity(float speed, Vec3d movementInput, AltoClef mod) {
        Vec3d vec3d = movementInputToVelocity(movementInput, speed, mod.getPlayer().getYaw());
        this.setVelocity(this.getVelocity().add(vec3d));
    }

    float getMovementSpeed(float slipperiness, AltoClef mod) {
        return mod.getPlayer().isOnGround() ? mod.getPlayer().getMovementSpeed() * (0.21600002F / (slipperiness * slipperiness * slipperiness)) : mod.getPlayer().getAbilities().getFlySpeed();
    }

    Vec3d method_26318(Vec3d vec3d, float f, AltoClef mod) {
        this.updateVelocity(getMovementSpeed(f, mod), vec3d, mod);
        //this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        //this.move(MovementType.SELF, this.getVelocity());
        Vec3d vec3d2 = this.getVelocity();
        //if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.getBlockStateAtPos().isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this))) {
        //    vec3d2 = new Vec3d(vec3d2.x, 0.2, vec3d2.z);
        //}

        return vec3d2;
    }

    void travel(final AltoClef mod) {
        BlockPos e = new BlockPos(mod.getPlayer().getPos().x, mod.getPlayer().getBoundingBox().minY - 0.5000001, mod.getPlayer().getPos().z);
        float vec3d3 = mod.getWorld().getBlockState(e).getBlock().getSlipperiness();
        double f = mod.getPlayer().isOnGround() ? vec3d3 * 0.91F : 0.91F;
        Vec3d g = this.method_26318(new Vec3d((double)mod.getPlayer().sidewaysSpeed, (double)mod.getPlayer().upwardSpeed, (double)mod.getPlayer().forwardSpeed), vec3d3, mod);

        double h = g.y;
        if (mod.getPlayer().hasStatusEffect(StatusEffects.LEVITATION)) {
            h += (0.05 * (double)(mod.getPlayer().getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) - g.y) * 0.2;
            mod.getPlayer().fallDistance = 0.0F;
        } else if (mod.getWorld().isClient && !mod.getWorld().isChunkLoaded(e)) {
            if (mod.getPlayer().getY() > (double)mod.getWorld().getBottomY()) {
                h = -0.1;
            } else {
                h = 0.0;
            }
        } else if (!mod.getPlayer().hasNoGravity()) {
            h -= 0.08;
        }

        setVelocity(g.x * (double)f, h * 0.9800000190734863, g.z * (double)f);
        System.out.println("recr vel x: " + getVelocity().x + " recr vel y: " + getVelocity().y + " recr vel z: " + getVelocity().z);
    }
}
