package adris.altoclef.util.csharpisbetter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Action<T> {

    private final List<ActionListener<T>> _consumers = new ArrayList<>();

    private boolean _lock = false;

    private final List<ActionListener<T>> _toAdd = new ArrayList<>();
    private final List<ActionListener<T>> _toRemove = new ArrayList<>();

    public void addListener(ActionListener<T> listener) {

        Method m;

        if (_lock) {
            _toAdd.add(listener);
        } else {
            _consumers.add(listener);
        }
    }

    public void removeListener(ActionListener<T> listener) {
        // TODO: Maybe use a linked list with stored nodes?
        if (_lock) {
            _toRemove.add(listener);
        } else {
            _consumers.remove(listener);

        }
    }

    public void invoke(T value) {
        _lock = true;
        for(ActionListener<T> consumer : _consumers) {
            consumer.invoke(value);
        }
        _lock = false;

        // If we made modifications while iterating, do the thing.

        _consumers.addAll(_toAdd);
        for (ActionListener<T> consumer : _toRemove) {
            _consumers.remove(consumer);
        }
        _toAdd.clear();
        _toRemove.clear();
    }

    public void invoke() {
        invoke(null);
    }

}