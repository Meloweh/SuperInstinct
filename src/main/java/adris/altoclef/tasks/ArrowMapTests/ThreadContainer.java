package adris.altoclef.tasks.ArrowMapTests;

import java.util.Optional;

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