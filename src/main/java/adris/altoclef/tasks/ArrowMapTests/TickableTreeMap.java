/**
 * @author Welomeh, Meloweh
 */
package adris.altoclef.tasks.ArrowMapTests;

import net.minecraft.entity.projectile.ArrowEntity;

import java.util.*;

public class TickableTreeMap {
    TreeMap<Long, List<TickableTraceInfo>> map;

    public TickableTreeMap() {
        this.map = new TreeMap<>();
    }

    public void tick() {
        for (final List<TickableTraceInfo> traceResult : map.values()) {
            for (final TickableTraceInfo ti : traceResult) {
                ti.tick();
            }
        }
        final TreeMap<Long, List<TickableTraceInfo>> newMap = new TreeMap<>();
        final Iterator<Map.Entry<Long, List<TickableTraceInfo>>> it = map.entrySet().iterator();
        //if (it.hasNext()) it.next();
        while (it.hasNext()) {
            final Map.Entry<Long, List<TickableTraceInfo>> entry = it.next();
            if (entry.getKey() > 1) newMap.put(entry.getKey()-1, entry.getValue());
        }
        this.map = newMap;
    }

    public void put(final TraceResult traceResult, final ArrowEntity arrow) {
        for (final TraceInfo info : traceResult.traceInfosAsList()) {
            final TickableTraceInfo tickable = new TickableTraceInfo(info, arrow);
            if (this.map.containsKey(info.getPiercingEntryHitTick())) {
                this.map.get(info.getPiercingEntryHitTick()).add(tickable);
            } else {
                final List<TickableTraceInfo> l = new ArrayList<>();
                l.add(tickable);
                this.map.put(info.getPiercingEntryHitTick(), l);
            }

        }
    }

    /*public TreeMap<Long, List<TickableTraceInfo>> getMap() {
        return this.map;
    } */

    public long floorKey(long key) {
        return this.map.floorKey(key);
    }

    public List<TickableTraceInfo> floorValue(final long key) {
        final long flooredKey = floorKey(key);
        return this.map.get(flooredKey);
    }

    public int size() {
        return this.map.size();
    }

    public Optional<List<TickableTraceInfo>> nearest() {
        if (size() < 1) return Optional.empty();
        return Optional.of(this.map.firstEntry().getValue());
    }

    public void clear() {
        this.map.clear();
    }

    public Iterator<List<TickableTraceInfo>> getIterator() {
        return this.map.values().iterator();
    }

    public List<TickableTraceInfo> getByIndex(final int index) {
        final Iterator<List<TickableTraceInfo>> it = this.map.values().iterator();
        for (int i = 0; i < index; i++) it.next();
        return it.next();
    }
}
