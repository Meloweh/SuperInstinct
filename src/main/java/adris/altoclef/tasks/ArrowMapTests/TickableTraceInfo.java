/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.entity.projectile.ArrowEntity;

class TickableTraceInfo {
    private final TraceInfo traceInfo;
    private int tick;
    private final ArrowEntity arrow;

    public TickableTraceInfo(final TraceInfo traceInfo, final ArrowEntity arrow) {
        this.traceInfo = traceInfo;
        this.tick = 0;
        this.arrow = arrow;
    }

    public void tick() {
        this.tick++;
    }

    public TraceInfo getTraceInfo() {
        return traceInfo;
    }

    public int getTick() {
        return tick;
    }

    public int getRemainingTicks() {
        final int result = (int)this.traceInfo.getPiercingEntryHitTick() - tick;
        return result < 0 ? 0 : result;
    }

    public boolean hasTimeLeft() {
        return getRemainingTicks() > 0;
    }

    public ArrowEntity getArrow() {
        return this.arrow;
    }
}