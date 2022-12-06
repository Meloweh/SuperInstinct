package adris.altoclef.tasks.ArrowMapTests;

import java.util.Optional;

public class ThreadContainer<T> {
    private Optional<T> traceResult;
    private boolean touched;

    public ThreadContainer() {
        reset();
    }

    public void setTraceResult(T traceResult) {
        if (traceResult != null) {
            this.traceResult = Optional.of(traceResult);
        }
        this.touched = true;
    }

    public boolean hasTraceResult() {
        return traceResult.isPresent();
    }

    public Optional<T> getTraceResult() {
        return traceResult;
    }

    public boolean isTouched() {
        return touched;
    }

    public void reset() {
        this.traceResult = Optional.empty();
        this.touched = false;
    }
}