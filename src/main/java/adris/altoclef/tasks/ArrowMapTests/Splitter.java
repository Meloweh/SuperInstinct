/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

public class Splitter {
    //put q and m and lastProgressTick and v0 in a list with key = lastProgressTick
    private final float q;
    private final double mX, mZ, localStartTick, localEndTick, startVelX, startVelZ;
    private final VecXZ localStartPosXZ, localEndPosXZ;

    public Splitter(final float q, final double mX, final double mZ, final double localStartTick, final double localEndTick, final double startVelX, final double startVelZ, final VecXZ startPosXZ, final VecXZ localEndPosXZ) {
        this.q = q;
        this.mX = mX;
        this.mZ = mZ;
        this.localStartTick = localStartTick;
        this.localEndTick = localEndTick;
        this.startVelX = startVelX;
        this.startVelZ = startVelZ;
        this.localStartPosXZ = startPosXZ;
        this.localEndPosXZ = localEndPosXZ;
    }

    public double getLocalEndTick() {
        return localEndTick;
    }

    public double getLocalStartTick() {
        return localStartTick;
    }

    public double getStartVelX() {
        return startVelX;
    }

    public float getQ() {
        return q;
    }

    public double getStartVelZ() {
        return startVelZ;
    }

    public double getMX() {
        return mX;
    }

    public double getMZ() {
        return mZ;
    }

    public VecXZ getLocalStartPosXZ() {
        return localStartPosXZ;
    }

    public VecXZ getLocalEndPosXZ() {
        return localEndPosXZ;
    }

    @Override
    public String toString() {
        return "Min t: " + getLocalStartTick() + " Max t: " + getLocalEndTick();
    }
}
