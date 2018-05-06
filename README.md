# Logisim Front Panel

This library contains additional I/O components for the Logisim simulator.

It is compatible with version of 2.7.1 of [classic Logisim](http://www.cburch.com/logisim/) and has not been tested with any of the subsequent forks.

[Home Page](https://www.gekkou.co.uk/software/frontpanel/)

## Components
### RGB Matrix

The RGB Matrix component represents a mutliplexed grid of tri-colour LEDs. Images can be built up over time by scanning through the display lines. The component simulates the "flicker fusion" which takes place in the human visual system at high frequencies, allowing the image of inactive lines to persist and shades of colour to be produced via pulse-width modulation.

| Port | Description |
| --- | --- |
| External Trigger | This port only appears when the Image Update Mode requires it. |
| Red Line | Bit vector indicating the active red LEDs in the selected line(s). |
| Green Line | Bit vector indicating the active green LEDs in the selected line(s). |
| Blue Line | Bit vector indicating the active blue LEDs in the selected line(s). |
| Line Selector | Bit vector which selects the active lines. |

| Attribute | Description |
| --- | --- |
| Data Width | The number of pixels in a line. |
| Selector Width | The number of lines in the matrix. |
| First Data Line | The orientation of the first line of pixels. |
| Fusion Window | The number of ticks over which the intensity of each pixel is calculated. |
| Maximum Duty | The number of ticks a pixel must be driven in order to achieve maximum intensity. |
| Show Selected Lines | Whether the component visually indicates which lines are currently selected.  |
| Image Update Mode | The condition under which the buffered display image is updated. |
