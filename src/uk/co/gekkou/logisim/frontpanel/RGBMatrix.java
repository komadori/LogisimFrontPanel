package uk.co.gekkou.logisim.frontpanel;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import java.awt.Graphics2D;

/**
 *
 * @author komadori
 */
public class RGBMatrix extends InstanceFactory
{
    private static final Attribute<BitWidth> ATTR_DATA =
        Attributes.forBitWidth("Data Width");
    private static final Attribute<BitWidth> ATTR_SELECT =
        Attributes.forBitWidth("Selector Width");
    private static final Attribute<Direction> ATTR_FIRST =
        Attributes.forDirection("First Data Line");
    private static final Attribute<Integer> ATTR_WINDOW =
        Attributes.forIntegerRange("Fusion Window", 1, 32);
    
    private static final int PORT_R = 0;
    private static final int PORT_G = 1;
    private static final int PORT_B = 2;
    private static final int PORT_S = 3;
    private static final int PORT_COUNT = 4;
    
    private static final int MIN_SIZE = 40;
    private static final int ELEM_SIZE = 10;
    private static final int BORDER_PADDING = 10;
    
    public RGBMatrix() {
        super("RGB Matrix");
        
        setAttributes(
            new Attribute[] {
                ATTR_DATA,
                ATTR_SELECT,
                ATTR_FIRST,
                ATTR_WINDOW
            },
            new Object[] {
                BitWidth.create(8),
                BitWidth.create(8),
                Direction.NORTH,
                1
            }
        );
        
        Port[] ports = new Port[PORT_COUNT];
        ports[PORT_R] = new Port(0, -10, Port.INPUT, ATTR_DATA);
        ports[PORT_G] = new Port(0, 0, Port.INPUT, ATTR_DATA);
        ports[PORT_B] = new Port(0, 10, Port.INPUT, ATTR_DATA);
        ports[PORT_S] = new Port(0, 20, Port.INPUT, ATTR_SELECT);
        setPorts(ports);
    }
    
    @Override
    protected void configureNewInstance(Instance inst)
    {
        inst.addAttributeListener();
    }
    
    @Override
    protected void instanceAttributeChanged(Instance inst, Attribute<?> attr)
    {
        if (attr == ATTR_DATA || attr == ATTR_SELECT || attr == ATTR_FIRST) {
            inst.recomputeBounds();
        }
    }
    
    private int getRows(AttributeSet attrs)
    {
        Direction first = attrs.getValue(ATTR_FIRST);
        Attribute<BitWidth> rows =
            (first == Direction.NORTH || first == Direction.SOUTH)
            ? ATTR_SELECT : ATTR_DATA;
        return attrs.getValue(rows).getWidth();
    }
    
    private int getCols(AttributeSet attrs)
    {
        Direction first = attrs.getValue(ATTR_FIRST);
        Attribute<BitWidth> cols =
            (first == Direction.NORTH || first == Direction.SOUTH)
            ? ATTR_DATA : ATTR_SELECT;
        return attrs.getValue(cols).getWidth();
    }
    
    @Override
    public Bounds getOffsetBounds(AttributeSet attrs)
    {
        int rows = getRows(attrs);
        int cols = getCols(attrs);
        int width = Math.max(MIN_SIZE, ELEM_SIZE * cols + BORDER_PADDING);
        int height = Math.max(MIN_SIZE, ELEM_SIZE * rows + BORDER_PADDING);
        return Bounds.create(0, -height/2, width, height);
    }
    
    @Override
    public void paintInstance(InstancePainter painter)
    {
        Instance inst = painter.getInstance();
        AttributeSet attrs = inst.getAttributeSet();
        int selectWidth = attrs.getValue(ATTR_SELECT).getWidth();
        int dataWidth = attrs.getValue(ATTR_DATA).getWidth();
        Direction first = attrs.getValue(ATTR_FIRST);
        int window = attrs.getValue(ATTR_WINDOW);

        int rows = getRows(attrs);
        int cols = getCols(attrs);
        Bounds bounds = painter.getBounds();
        int pxWidth = ELEM_SIZE * cols;
        int pxHeight = ELEM_SIZE * rows;
        int pxOffX = bounds.getX() + (bounds.getWidth() - pxWidth) / 2;
        int pxOffY = bounds.getY() + (bounds.getHeight() - pxHeight) / 2;
        
        Graphics2D g = (Graphics2D)painter.getGraphics();
        RGBMatrixData data = new RGBMatrixData(dataWidth, selectWidth, window);
        data.loadLine(0, 1, 2, 4);
        data.updateImageLine(0);
        data.renderImage(g, first, pxOffX, pxOffY, pxWidth, pxHeight);
        
        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state)
    {
        //
    }
}
