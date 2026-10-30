package zone.moddev.mc.steamadvantage.gui;

import org.junit.Test;
import static org.junit.Assert.*;

public class GUIHelperTest {
    @Test public void readingsKeepTheirScaleButStayInsideTheGauge() {
        assertEquals(0F, GUIHelper.displayLevel(-1F), 0F);
        assertEquals(0F, GUIHelper.displayLevel(0F), 0F);
        assertEquals(0.5F, GUIHelper.displayLevel(0.5F), 0F);
        assertEquals(1F, GUIHelper.displayLevel(1F), 0F);
        assertEquals(1F, GUIHelper.displayLevel(15F), 0F);
    }

    @Test public void nonfiniteReadingsAreSafeToDraw() {
        for (float value : new float[]{Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            assertEquals(0F, GUIHelper.displayLevel(value), 0F);
        }
    }
}
