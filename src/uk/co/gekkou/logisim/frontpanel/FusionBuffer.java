package uk.co.gekkou.logisim.frontpanel;

/**
 *
 * @author komadori
 */
public class FusionBuffer implements Cloneable
{
    private static final int FLD_VALUE = 0;
    private static final int FLD_DURATION = 1;
    private static final int FLD_COUNT = 2;

    int[] _buf;
    int[] _counts;
    int _window;
    int _top;
    int _btm;
    
    public FusionBuffer(int value, int width, int window)
    {
        _buf = new int[2*FLD_COUNT];
        _buf[FLD_VALUE] = value;
        _buf[FLD_DURATION] = window;
        _counts = new int[width];
        updateCounts(value, window);
        _window = window;
        _top = 2;
        _btm = 0;
    }
    
    private FusionBuffer(FusionBuffer src)
    {
        _buf = src._buf;
        _counts = src._counts;
        _window = src._window;
        _top = src._top;
        _btm = src._btm;
    }
    
    private void updateCounts(int bits, int magnitude)
    {
        for (int i=0; i<_counts.length; i++) {
            if (((bits >> i) & 1) == 1) {
                _counts[i] += magnitude;
            }
        }
    }
    
    public void advance(int value, int duration)
    {
        duration = Math.min(duration, _window);
        
        int remove = duration;
        while (remove > 0 && _btm != _top) {
            int btmVal = _buf[_btm+FLD_VALUE];
            int btmDur = _buf[_btm+FLD_DURATION];
            if (btmDur > remove) {
                _buf[_btm+FLD_DURATION] = btmDur - remove;
                updateCounts(btmVal, -remove);
                remove = 0;
            }
            else {
                _buf[_btm+FLD_DURATION] = 0;
                updateCounts(btmVal, -btmDur);
                remove -= btmDur;
                _btm = (_btm + FLD_COUNT) % _buf.length;
            }
        }
        duration -= remove;
        
        if (duration > 0) {
            _buf[_top+FLD_VALUE] = value;
            _buf[_top+FLD_DURATION] = duration;
            updateCounts(value, duration);
            _top = (_top + FLD_COUNT) % _buf.length;
            if (_top == _btm) {
                int[] newBuf = new int[
                    Math.min(FLD_COUNT*(_window+1), 2*_buf.length)];
                int upper = _buf.length-_top;
                System.arraycopy(_buf, _top, newBuf, 0, upper);
                System.arraycopy(_buf, 0, newBuf, upper, _top);
                _top = _buf.length;
                _btm = 0;
                _buf = newBuf;
            }
        }
    }
    
    public int getCount(int i)
    {
        return _counts[i];
    }
    
    @Override
    public Object clone()
    {
        return new FusionBuffer(this);
    }

}
