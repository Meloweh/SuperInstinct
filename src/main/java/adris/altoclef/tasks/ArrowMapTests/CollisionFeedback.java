/**
 * @author Welomeh, Meloweh
 */

package adris.altoclef.tasks.ArrowMapTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class CollisionFeedback {
    private List<Interval<Double>> intervals;

    public CollisionFeedback() {
        this.intervals = new ArrayList<>();
    }

    public void set(final int intendedIndex, final Interval<Double> intv) {
        if (intv == null) throw new IllegalArgumentException("Interval was null");
        if (intendedIndex == 0) throw new IllegalArgumentException("intendedIndex starts at 1");
        if (intendedIndex > this.intervals.size() + 1) throw new IllegalArgumentException("interval insertion needs to be done chronologically");
        final int realTargetIndex = intendedIndex - 1;
        if (realTargetIndex == this.intervals.size()) {
            this.intervals.add(intv);
        } else {
            this.intervals.set(realTargetIndex, intv);
        }
    }

    public void setFirst(final Interval<Double> intv) {
        set(1, intv);
    }

    public void setSecond(final Interval<Double> intv) {
        set(2, intv);
    }

    public void setThird(final Interval<Double> intv) {
        set(3, intv);
    }

    private void addAll(final List<Interval<Double>> intvs) {
        this.intervals.addAll(intvs);
    }

    private void addAll(final Interval<Double>[] intvs) {
        this.addAll(Arrays.stream(intvs).toList());
    }

    public Interval<Double> get(final int intendedIndex) {
        if (intendedIndex == 0) throw new IllegalArgumentException("intendedIndex starts at 1");
        if (intendedIndex > this.intervals.size()) throw new IllegalArgumentException("interval not present");
        return this.intervals.get(intendedIndex - 1);
    }

    public Interval<Double> getFirst() {
        if (this.intervals.size() < 1 || Double.isNaN(this.get(1).getMin())) return null;
        return this.intervals.get(0);
    }

    public Interval<Double> getSecond() {
        if (this.intervals.size() < 2 || Double.isNaN(this.get(2).getMin())) return null;
        return this.intervals.get(1);
    }

    public Interval<Double> getThird() {
        if (this.intervals.size() < 3 || Double.isNaN(this.get(3).getMin())) return null;
        return this.intervals.get(2);
    }



    /*
    public boolean is1DEmpty() {
        return first == null;
    }

    public boolean is2DEmpty() {
        return second == null || first == null;
    }

    public boolean is3DEmpty() {
        return is2DEmpty() || third == null;
    }*/

    /*public List<Interval<Double>> toList() {
        final List<Interval<Double>> list = new ArrayList<>();
        if (!is2DEmpty()) {
            list.add(getFirst());
            list.add(getSecond());
        }
        if (!is3DEmpty()) {
            list.add(getThird());
        }
        return list;
    }*/

    public Optional<List<Interval<Double>>> toList() {
        return this.intervals.size() < 0 ? Optional.empty() : Optional.of(this.intervals);
    }

    public Optional<Interval<Double>[]> toArray() {
        final Interval<Double> arr[] = new Interval[this.intervals.size()];
        return this.intervals.size() < 0 ? Optional.empty() : Optional.of(this.intervals.toArray(arr));
    }

    public static CollisionFeedback ofList(final List<Interval<Double>> list) {
        final CollisionFeedback feedback = new CollisionFeedback();
        feedback.addAll(list);
        return feedback;
    }

    @Override
    public String toString() {
        String str = "[";
        for (int i = 0; i <= this.intervals.size() - 1; i++) {
            final Interval<Double> intv = this.intervals.get(i);
            str += "Intv num: " + i + " = " + intv.toString() + ",";
        }
        /*if (first != null) str += "First: " + getFirst().toString() + " ";
        if (second != null) str += "Second: " + getSecond().toString() + " ";
        if (third != null) str += "Third: " + getThird().toString() + " ";*/
        str += "]";
        return str;
    }

    public boolean isEmpty() {
        return this.intervals.isEmpty();
    }
}
