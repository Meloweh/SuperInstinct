package adris.altoclef.tasks.ArrowMapTests;

public class DebugPrint {
    private static final boolean print = false;

    public static final void println(final Object str) {
        if (print) System.out.println(str);
    }
}
