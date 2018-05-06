package uk.co.gekkou.logisim.frontpanel;

import com.cburch.logisim.data.*;
import com.cburch.logisim.instance.*;
import com.cburch.logisim.std.wiring.*;
import com.cburch.logisim.util.*;
import static com.cburch.logisim.util.StringUtil.*;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author komadori
 */
public class RGBMatrix extends InstanceFactory
{
    private static final AttributeOption UPDATE_DESELECT_ANY =
        new AttributeOption(UpdateMode.DESELECT_ANY, "deselectAny",
            constantGetter("Any Line Deselected"));
    private static final AttributeOption UPDATE_DESELECT_LAST =
        new AttributeOption(UpdateMode.DESELECT_LAST, "deselectLast",
            constantGetter("Last Line Deselected"));
    private static final AttributeOption UPDATE_TRIGGER_RISE =
        new AttributeOption(UpdateMode.RISING_TRIGGER, "triggerRise",
            constantGetter("Rising Trigger"));
    
    private static final Attribute<BitWidth> ATTR_DATA =
        Attributes.forBitWidth("dataWidth", constantGetter("Data Width"));
    private static final Attribute<BitWidth> ATTR_SELECT =
        Attributes.forBitWidth("selectWidth", constantGetter("Selector Width"));
    private static final Attribute<Direction> ATTR_FIRST =
        Attributes.forDirection("firstLine", constantGetter("First Data Line"));
    private static final Attribute<Integer> ATTR_WINDOW =
        new DurationAttribute("window",
            constantGetter("Fusion Window"), 1, Integer.MAX_VALUE);
    private static final Attribute<Integer> ATTR_MAX_DUTY =
        new DurationAttribute("maxDuty",
            constantGetter("Maximum Duty"), 1, Integer.MAX_VALUE);
    private static final Attribute<Boolean> ATTR_SHOW_SELECT =
        Attributes.forBoolean("showSelect",
            constantGetter("Show Selected Lines"));
    private static final Attribute<AttributeOption> ATTR_UPDATE_MODE =
        Attributes.forOption("updateMode", constantGetter("Image Update Mode"),
            new AttributeOption[] {
                UPDATE_DESELECT_ANY,
                UPDATE_DESELECT_LAST,
                UPDATE_TRIGGER_RISE
            });
    
    private static final int PORT_R = 0;
    private static final int PORT_G = 1;
    private static final int PORT_B = 2;
    private static final int PORT_S = 3;
    private static final int PORT_TRIGGER = 4;
    private static final int PORT_COUNT = 5;
    
    private static final int MIN_SIZE = 40;
    private static final int ELEM_SIZE = 10;
    private static final int BORDER_PADDING = 10;
    private static final int MARKER_PADDING = 4;
    
    public RGBMatrix() {
        super("RGB Matrix");
        
        setAttributes(
            new Attribute[] {
                ATTR_DATA,
                ATTR_SELECT,
                ATTR_FIRST,
                ATTR_WINDOW,
                ATTR_MAX_DUTY,
                ATTR_SHOW_SELECT,
                ATTR_UPDATE_MODE
            },
            new Object[] {
                BitWidth.create(8),
                BitWidth.create(8),
                Direction.NORTH,
                1,
                1,
                true,
                UPDATE_DESELECT_ANY
            }
        );
    }
    
    @Override
    protected void configureNewInstance(Instance inst)
    {
        inst.addAttributeListener();
        updatePorts(inst);
    }
    
    @Override
    protected void instanceAttributeChanged(Instance inst, Attribute<?> attr)
    {
        if (attr == ATTR_DATA || attr == ATTR_SELECT || attr == ATTR_FIRST) {
            inst.recomputeBounds();
        }
        else if (attr == ATTR_UPDATE_MODE) {
            updatePorts(inst);
        }
    }
    
    private UpdateMode getUpdateMode(Instance inst)
    {
        Object modeObj = inst.getAttributeValue(ATTR_UPDATE_MODE).getValue();
        if (modeObj instanceof UpdateMode) {
            return (UpdateMode)modeObj;
        }
        else {
            // This can happen when reloading the component library
            return UpdateMode.valueOf(modeObj.toString());
        }
    }
    
    private void updatePorts(Instance inst)
    {
        UpdateMode mode = getUpdateMode(inst);
        Port[] ports = new Port[mode.isTriggeredExternally() ?
            PORT_COUNT : PORT_TRIGGER];
        ports[PORT_R] = new Port(0, -10, Port.INPUT, ATTR_DATA);
        ports[PORT_R].setToolTip(StringUtil.constantGetter("Red Line"));
        ports[PORT_G] = new Port(0, 0, Port.INPUT, ATTR_DATA);
        ports[PORT_G].setToolTip(StringUtil.constantGetter("Green Line"));
        ports[PORT_B] = new Port(0, 10, Port.INPUT, ATTR_DATA);
        ports[PORT_B].setToolTip(StringUtil.constantGetter("Blue Line"));
        ports[PORT_S] = new Port(0, 20, Port.INPUT, ATTR_SELECT);
        ports[PORT_S].setToolTip(StringUtil.constantGetter("Line Selector"));
        if (mode.isTriggeredExternally()) {
            ports[PORT_TRIGGER] = new Port(0, -20, Port.INPUT, 1);
            ports[PORT_TRIGGER].setToolTip(
                StringUtil.constantGetter("External Trigger"));
        }
        inst.setPorts(ports);
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
    
    private void paintSelector(Graphics2D g, RGBMatrixData data,
        Direction first, int pxOffX, int pxOffY, int pxWidth, int pxHeight)
    {
        g.setColor(Color.BLUE);
        int markW = 0, markH = 0, markX = 0, markY = 0, markIX = 0, markIY = 0;
        if (first == Direction.NORTH) {
            markW = BORDER_PADDING/2;
            markH = ELEM_SIZE - MARKER_PADDING;
            markX = pxOffX - markW;
            markY = pxOffY + MARKER_PADDING/2;
            markIX = 0;
            markIY = ELEM_SIZE;
        }
        else if (first == Direction.EAST) {
            markW = ELEM_SIZE - MARKER_PADDING;
            markH = BORDER_PADDING/2;
            markX = pxOffX + pxWidth - ELEM_SIZE + MARKER_PADDING/2;
            markY = pxOffY - markH;
            markIX = -ELEM_SIZE;
            markIY = 0;
        }
        else if (first == Direction.SOUTH) {
            markW = BORDER_PADDING/2;
            markH = ELEM_SIZE - MARKER_PADDING;
            markX = pxOffX + pxWidth;
            markY = pxOffY + pxHeight - ELEM_SIZE + MARKER_PADDING/2;
            markIX = 0;
            markIY = -ELEM_SIZE;
        }
        else if (first == Direction.WEST) {
            markW = ELEM_SIZE - MARKER_PADDING;
            markH = BORDER_PADDING/2;
            markX = pxOffX;
            markY = pxOffY + pxHeight;
            markIX = ELEM_SIZE;
            markIY = 0;
        }
        for (int sel = data.getSelector(), i=0; sel>0; i++) {
            int n = Integer.numberOfTrailingZeros(sel);
            sel >>= n+1;
            i += n;
            g.fillRect(markX+i*markIX, markY+i*markIY, markW, markH);
        }
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
        RGBMatrixData data = RGBMatrixData.get(painter, dataWidth, selectWidth, window);
        data.renderImage(g, first, pxOffX, pxOffY, pxWidth, pxHeight);
        
        if (attrs.getValue(ATTR_SHOW_SELECT)) {
            paintSelector(g, data, first, pxOffX, pxOffY, pxWidth, pxHeight);
        }
        
        painter.drawBounds();
        painter.drawPorts();
    }

    @Override
    public void propagate(InstanceState state)
    {
        AttributeSet attrs = state.getAttributeSet();
        int selectWidth = attrs.getValue(ATTR_SELECT).getWidth();
        int dataWidth = attrs.getValue(ATTR_DATA).getWidth();
        int window = attrs.getValue(ATTR_WINDOW);
        RGBMatrixData data = RGBMatrixData.get(
            state, dataWidth, selectWidth, window);
        
        UpdateMode mode = getUpdateMode(state.getInstance());
        
        int r = state.getPort(PORT_R).toIntValue();
        int g = state.getPort(PORT_G).toIntValue();
        int b = state.getPort(PORT_B).toIntValue();
        int s = state.getPort(PORT_S).toIntValue();
        if (data.loadLines(state.getTickCount(), s, r, g, b, mode) ||
                (mode.isTriggeredExternally() &&
                    data.checkTrigger(state.getPort(PORT_TRIGGER).toIntValue()))) {
            int maxDuty = attrs.getValue(ATTR_MAX_DUTY);
            data.updateImage(maxDuty);
        }
    }
}
