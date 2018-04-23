package uk.co.gekkou.logisim.frontpanel;

import java.util.*;
import com.cburch.logisim.tools.*;

/**
 *
 * @author komadori
 */
public class FrontPanelComponents extends Library
{
    private final List<AddTool> tools;
    
    public FrontPanelComponents()
    {
        tools = new ArrayList<>();
        tools.add(new AddTool(new RGBMatrix()));
    }

    @Override
    public String getDisplayName()
    {
        return "Front Panel";
    }
    
    @Override
    public List<AddTool> getTools()
    {
        return tools;
    }
}
