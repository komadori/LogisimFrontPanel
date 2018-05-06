package uk.co.gekkou.logisim.frontpanel;

/**
 *
 * @author komadori
 */
public enum UpdateMode
{
    DESELECT_ANY {
        @Override
        public boolean testSelector(int width, int old, int current) {
            return (old & ~current) != 0;
        }
    },
    DESELECT_LAST {
        @Override
        public boolean testSelector(int width, int old, int current) {
            return (old & ~current & (1 << (width-1))) != 0;
        }
    },
    RISING_TRIGGER {
        @Override
        public boolean isTriggeredExternally()
        {
            return true;
        }
        
        @Override
        public boolean testExternalTrigger(int old, int current)
        {
            return (~old & current) != 0;
        }
    };
    
    public boolean testSelector(int width, int old, int current)
    {
        return false;
    }
    
    public boolean isTriggeredExternally()
    {
        return false;
    }

    public boolean testExternalTrigger(int old, int current)
    {
        return false;
    }
}
