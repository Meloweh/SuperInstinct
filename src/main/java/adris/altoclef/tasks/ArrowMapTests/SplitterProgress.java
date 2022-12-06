/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

final class SplittedProgress {
    private double tick, currVel, currPos;

    public SplittedProgress(final double tick, final double currVel, final double currPos) {
        this.tick = tick; this.currVel = currVel; this.currPos = currPos;
    }

    public SplittedProgress() {
    }

    public double getCurrPos() {
        return currPos;
    }

    public double getCurrVel() {
        return currVel;
    }

    public double getTick() {
        return tick;
    }

    public void setCurrPos(double currPos) {
        this.currPos = currPos;
    }

    public void setCurrVel(double currVel) {
        this.currVel = currVel;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }
}