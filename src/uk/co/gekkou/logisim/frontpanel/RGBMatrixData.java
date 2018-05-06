package uk.co.gekkou.logisim.frontpanel;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *
 * @author komadori
 */
public class RGBMatrixData implements InstanceData, Cloneable
{
    private static final int COL_R = 0;
    private static final int COL_G = 1;
    private static final int COL_B = 2;
    private static final int COL_COUNT = 3;
    
    public static enum SelectorUpdateMode
    {
        UPDATE_IGNORE_SELECTOR,
        UPDATE_DESELECT_ANY,
        UPDATE_DESELECT_LAST
    }
    
    int _dataWidth;
    int _selectWidth;
    int _fusionWindow;
    FusionBuffer[] _lineBufs;
    BufferedImage _image;
    AffineTransform _cachedTx;
    long _oldTickCount;
    int _oldTrigger;
    int _oldSelector;
    int _oldR;
    int _oldG;
    int _oldB;

    public static RGBMatrixData get(InstanceState inst, int dataWidth, int selectWidth, int fusionWindow)
    {
        RGBMatrixData data = (RGBMatrixData)inst.getData();
        if (data == null ||
            data._dataWidth != dataWidth ||
            data._selectWidth != selectWidth ||
            data._fusionWindow != fusionWindow)
        {
            data = new RGBMatrixData(dataWidth, selectWidth, fusionWindow);
            inst.setData(data);
        }
        return data;
    }
    
    public RGBMatrixData(int dataWidth, int selectWidth, int fusionWindow)
    {
        _dataWidth = dataWidth;
        _selectWidth = selectWidth;
        _fusionWindow = fusionWindow;
        _lineBufs = new FusionBuffer[selectWidth*COL_COUNT];
        for (int i=0; i<_lineBufs.length; i++) {
            _lineBufs[i] = new FusionBuffer(0, dataWidth, fusionWindow);
        }
        _image = new BufferedImage(dataWidth, selectWidth,
            BufferedImage.TYPE_INT_RGB);
    }
    
    private FusionBuffer getLineBuffer(int line, int colour)
    {
        return _lineBufs[COL_COUNT*line+colour];
    }
    
    public boolean loadLines(
        long tickCount, int selector, int r, int g, int b,
        SelectorUpdateMode updateMode)
    {
        int duration = (int)Math.min(Integer.MAX_VALUE, tickCount - _oldTickCount);
        for (int i=0; i<_selectWidth; i++) {
            boolean active = ((_oldSelector >> i) & 1) == 1;
            getLineBuffer(i, COL_R).advance(active ? _oldR : 0, duration);
            getLineBuffer(i, COL_G).advance(active ? _oldG : 0, duration);
            getLineBuffer(i, COL_B).advance(active ? _oldB : 0, duration);
        }
        boolean update = false;
        switch (updateMode)
        {
            case UPDATE_DESELECT_ANY:
                update = (~selector & _oldSelector) != 0;
                break;
            case UPDATE_DESELECT_LAST:
                update = (~selector & _oldSelector & (1 << (_dataWidth-1))) != 0;
                break;
        }
        _oldTickCount = tickCount;
        _oldSelector = selector;
        _oldR = r;
        _oldG = g;
        _oldB = b;
        return update;
    }
    
    public boolean checkTrigger(int trigger) {
        boolean update = (trigger & ~_oldTrigger) != 0;
        _oldTrigger = trigger;
        return update;
    }
    
    public void updateImage(int maxDuty)
    {
        for (int line=0; line<_selectWidth; line++) {
            for (int i=0; i<_dataWidth; i++) {
                int r = (Math.min(maxDuty, getLineBuffer(line, COL_R).getCount(i))*0xff)/maxDuty;
                int g = (Math.min(maxDuty, getLineBuffer(line, COL_G).getCount(i))*0xff)/maxDuty;
                int b = (Math.min(maxDuty, getLineBuffer(line, COL_B).getCount(i))*0xff)/maxDuty;
                _image.setRGB(i, line, r << 16 | g << 8 | b);
            }
        }
    }
    
    public void renderImage(Graphics2D g, Direction first, int x, int y, int width, int height)
    {
        Object oldHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);        
        int bearing;
        double scaleX, scaleY;
        if (first == Direction.NORTH || first == Direction.SOUTH) {
            scaleX = width/(double)_dataWidth;
            scaleY = height/(double)_selectWidth;
            bearing = first == Direction.NORTH ? 0 : 180;
        }
        else {
            scaleX = width/(double)_selectWidth;
            scaleY = height/(double)_dataWidth;
            bearing = first == Direction.EAST ? 90 : 270;
        }
        if (_cachedTx == null) {
            _cachedTx = new AffineTransform();
        }
        else {
            _cachedTx.setToIdentity();
        }
        _cachedTx.translate(width/2.0, height/2.0);
        _cachedTx.rotate(Math.toRadians((double)bearing), x, y);
        _cachedTx.translate(x - _dataWidth*scaleX/2.0, y - _selectWidth*scaleY/2.0);
        _cachedTx.scale(scaleX, scaleY);
        g.drawRenderedImage(_image, _cachedTx);
        if (oldHint != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
        }
    }
    
    public int getSelector()
    {
        return _oldSelector;
    }
    
    private RGBMatrixData(RGBMatrixData state)
    {
        _dataWidth = state._dataWidth;
        _selectWidth = state._selectWidth;
        _fusionWindow = state._fusionWindow;
        _lineBufs = new FusionBuffer[state._lineBufs.length];
        for (int i=0; i<_lineBufs.length; i++) {
            _lineBufs[i] = (FusionBuffer)state._lineBufs[i].clone();
        }
        _image = new BufferedImage(
            state._image.getColorModel(),
            state._image.copyData(null),
            state._image.isAlphaPremultiplied(),
            null);
        _oldTickCount = state._oldTickCount;
        _oldSelector = state._oldSelector;
        _oldR = state._oldR;
        _oldG = state._oldG;
        _oldB = state._oldB;
    }
    
    @Override
    public Object clone()
    {
        return new RGBMatrixData(this);
    }
}
