package uk.co.gekkou.logisim.frontpanel;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

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
    
    int _dataWidth;
    int _selectWidth;
    int _fusionWindow;
    int[] _data;
    int[] _dataIndices;    
    int[] _accumBuf;
    BufferedImage _image;
    AffineTransform _cachedTx;

    private int getDataIndex(int s, int t, int col)
    {
        return col*_fusionWindow*COL_COUNT + t*COL_COUNT + col;
    }
    
    private int getAccumIndex(int d, int col)
    {
        return d*COL_COUNT + col;
    }

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
        _data = new int[selectWidth*fusionWindow*COL_COUNT];
        _dataIndices = new int[selectWidth];
        _accumBuf = new int[dataWidth*COL_COUNT];
        _image = new BufferedImage(dataWidth, selectWidth,
            BufferedImage.TYPE_INT_RGB);
    }
    
    public void loadLine(int s, int r, int g, int b)
    {
        int t = _dataIndices[s];
        _data[getDataIndex(s, t, COL_R)] = r;
        _data[getDataIndex(s, t, COL_G)] = g;
        _data[getDataIndex(s, t, COL_B)] = b;
        _dataIndices[s] = (t+1) % _fusionWindow;
    }
    
    public void updateImageLine(int s)
    {
        for (int i=0; i<_accumBuf.length; i++) {
            _accumBuf[i] = 0;
        }
        for (int t=0; t<_fusionWindow; t++) {
            for (int col=0; col<COL_COUNT; col++) {
                int data = _data[getDataIndex(s, t, col)];
                for (int d=0; d<_dataWidth; d++) {
                    _accumBuf[getAccumIndex(d, col)] += data & 1;
                    data >>= 1;
                }
            }
        }
        for (int d=0; d<_dataWidth; d++) {
            int r = (_accumBuf[getAccumIndex(d, COL_R)]*0xff)/_fusionWindow;
            int g = (_accumBuf[getAccumIndex(d, COL_G)]*0xff)/_fusionWindow;
            int b = (_accumBuf[getAccumIndex(d, COL_B)]*0xff)/_fusionWindow;
            _image.setRGB(d, s, r << 16 | g << 8 | b);
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
    
    private RGBMatrixData(RGBMatrixData state)
    {
        _dataWidth = state._dataWidth;
        _selectWidth = state._selectWidth;
        _fusionWindow = state._fusionWindow;
        _data = Arrays.copyOf(state._data, state._data.length);
        _dataIndices = Arrays.copyOf(
            state._dataIndices, state._dataIndices.length);
        _accumBuf = new int[state._accumBuf.length];
        _image = new BufferedImage(
            state._image.getColorModel(),
            state._image.copyData(null),
            state._image.isAlphaPremultiplied(),
            null);
    }
    
    @Override
    public Object clone()
    {
        return new RGBMatrixData(this);
    }
}
