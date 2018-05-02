package uk.co.gekkou.logisim.frontpanel;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author komadori
 */
public class TestFusionBuffer
{
    @Test
    public void testSimple()
    {
        FusionBuffer fb = new FusionBuffer(0, 1, 20);
        assertEquals(0, fb.getCount(0));
        fb.advance(1, 10);
        assertEquals(10, fb.getCount(0));
        fb.advance(1, 10);
        assertEquals(20, fb.getCount(0));
        fb.advance(1, 10);
        assertEquals(20, fb.getCount(0));
        fb.advance(0, 10);
        assertEquals(10, fb.getCount(0));
    }

    @Test
    public void testOverflow()
    {
        FusionBuffer fb = new FusionBuffer(0, 1, 20);
        fb.advance(1, 1000);
        assertEquals(20, fb.getCount(0));
        fb.advance(0, 1000);
        assertEquals(0, fb.getCount(0));
    }
    
    @Test
    public void testTimeless()
    {
        FusionBuffer fb = new FusionBuffer(1, 1, 1);
        fb.advance(0, 0);
        fb.advance(0, 0);
        assertEquals(1, fb.getCount(0));
    }
    
    @Test
    public void testWide()
    {
        FusionBuffer fb = new FusionBuffer(0, 8, 256);
        for (int i=0; i<256; i++) {
            fb.advance(i, 1);
        }
        for (int i=0; i<8; i++) {
            assertEquals(128, fb.getCount(i));
        }
    }
}
