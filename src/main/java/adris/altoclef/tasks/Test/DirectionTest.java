package adris.altoclef.tasks.Test;

import adris.altoclef.util.helpers.LookHelper;
import org.junit.Test;

public class DirectionTest {
    @Test
    public void d() {
        System.out.println(LookHelper.randomDirection2D().asString());
    }
}
