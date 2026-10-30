package zone.moddev.mc.steamadvantage.gui;

import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.*;

public class GUIHelperTest {
    @Test public void readingsKeepTheirScaleButStayInsideTheGauge() throws Exception {
        assertEquals(0F, displayLevel(-1F), 0F);
        assertEquals(0F, displayLevel(0F), 0F);
        assertEquals(0.5F, displayLevel(0.5F), 0F);
        assertEquals(1F, displayLevel(1F), 0F);
        assertEquals(1F, displayLevel(15F), 0F);
    }

    @Test public void nonfiniteReadingsAreSafeToDraw() throws Exception {
        for (float value : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertEquals(0F, displayLevel(value), 0F);
        }
    }

    private static float displayLevel(float value) throws Exception {
        Method method;
        try { method = GUIHelper.class.getDeclaredMethod("displayLevel", float.class); }
        catch (NoSuchMethodException missing) { throw new AssertionError("Steam gauge readings need one finite, bounded display scale", missing); }
        method.setAccessible(true);
        return (Float) method.invoke(null, value);
    }
}
