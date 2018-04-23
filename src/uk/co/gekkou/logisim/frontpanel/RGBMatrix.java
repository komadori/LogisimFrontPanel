package uk.co.gekkou.logisim.frontpanel;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;

/**
 *
 * @author komadori
 */
public class RGBMatrix extends InstanceFactory
{
    private static final Attribute<BitWidth> ATTR_ROWS =
        Attributes.forBitWidth("Matrix Rows");
    private static final Attribute<BitWidth> ATTR_COLS =
        Attributes.forBitWidth("Matrix Columns");
    
    public RGBMatrix() {
        super("RGB Matrix");
        
        setAttributes(
            new Attribute[] { ATTR_ROWS, ATTR_COLS },
            new Object[] { BitWidth.create(8), BitWidth.create(8) }
        );
        
        setPorts(new Port[] {
            new Port(0, -10, Port.INPUT, ATTR_ROWS),
            new Port(0, 10, Port.INPUT, ATTR_COLS)
        });
    }
    
    @Override
    protected void configureNewInstance(Instance inst)
    {
        inst.addAttributeListener();
    }
    
    @Override
    protected void instanceAttributeChanged(Instance inst, Attribute<?> attr)
    {
        if (attr == ATTR_ROWS || attr == ATTR_COLS) {
            inst.recomputeBounds();
        }
    }
    
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs)
    {
        int rows = attrs.getValue(ATTR_ROWS).getWidth();
        int cols = attrs.getValue(ATTR_COLS).getWidth();
        return Bounds.create(0, -5 * rows, 10 * cols, 10 * rows);
    }
    
    @Override
    public void paintInstance(InstancePainter painter)
    {
        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state)
    {
    }
}
