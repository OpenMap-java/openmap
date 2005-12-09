// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/LabelLayer.java,v $
// $RCSfile: LabelLayer.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Properties;

import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.Taskable;

/**
 * Layer that displays a label. This Layer is a Taskable
 * (ActionListener) object so that it can be prompted by a
 * javax.swing.Timer object. This layer understands the following
 * properties: <code><pre>
 * 
 *  
 *   # display font as a Java font string
 *   label.font=SansSerif-Bold
 *   # like XWindows geometry: [+-]X[+-]Y, `+' indicates relative to
 *   # left edge or top edges, `-' indicates relative to right or bottom
 *   # edges, XX is x coordinate, YY is y coordinate
 *   label.geometry=+20-30
 *   # background rectangle color (ARGB)
 *   label.color.bg=ffb3b3b3
 *   # foreground text color (ARGB)
 *   label.color.fg=ff000000
 *   # date format (using java.text.SimpleDateFormat patterns)
 *   label.text=The Graph
 *   
 *  
 * </pre></code>
 * <p>
 * In addition to the previous properties, you can get this layer to
 * work with the OpenMap viewer by adding/editing the additional
 * properties in your <code>openmap.properties</code> file:
 * <code><pre>
 * 
 *  
 *   # layers
 *   openmap.layers=label ...
 *   # class
 *   label.class=com.bbn.openmap.layer.LabelLayer
 *   # name
 *   label.prettyName=Label Layer
 *   
 *  
 * </pre></code> NOTE: the color properties do not support alpha value if
 * running on JDK 1.1...
 */
public class LabelLayer extends OMGraphicHandlerLayer implements Taskable,
        MapMouseListener {

    // property keys
    public final static transient String fontProperty = "font";
    public final static transient String fgColorProperty = "color.fg";
    public final static transient String bgColorProperty = "color.bg";
    public final static transient String geometryProperty = "geometry";
    public final static transient String labelProperty = "text";

    // properties
    protected String fontString = "SansSerif";
    protected Font font = Font.decode(fontString);
    protected int fgColorValue = 0x000000;//black
    protected Color fgColor = new Color(fgColorValue);
    protected int bgColorValue = 0xffffff;//white
    protected Color bgColor = new Color(bgColorValue);
    protected String geometryString = "+20+20";
    protected String labelText = "";

    protected OMText text;

    protected int xpos = 10;
    protected int ypos = 10;
    protected String xgrav = geometryString.substring(0, 1);
    protected String ygrav = geometryString.substring(3, 4);

    private int dragX;
    private int dragY;
    private boolean dragging = false;

    /**
     * Construct the LabelLayer.
     */
    public LabelLayer() {
        text = new OMText(0, 0, "uninitialized", font, OMText.JUSTIFY_RIGHT);
        text.setLinePaint(fgColor);
        text.setFillPaint(bgColor);
    }

    /**
     * Sets the properties for the <code>Layer</code>.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = com.bbn.openmap.util.PropUtils.getScopedPropertyPrefix(prefix);

        fontString = props.getProperty(prefix + fontProperty, fontString);

        fgColor = ColorFactory.parseColorFromProperties(props, prefix
                + fgColorProperty, Integer.toString(fgColorValue));

        bgColor = ColorFactory.parseColorFromProperties(props, prefix
                + bgColorProperty, Integer.toString(bgColorValue));

        geometryString = props.getProperty(prefix + geometryProperty,
                geometryString);
        parseGeometryString();

        labelText = props.getProperty(prefix + labelProperty, labelText);

        // reset the property values
        font = Font.decode(fontString);
        text.setFont(font);
        text.setLinePaint(fgColor);
        text.setFillPaint(bgColor);
    }

    /** Parse X-like geometry string. */
    protected void parseGeometryString() {
        int i = 0;
        byte[] bytes = geometryString.getBytes();
        xgrav = new String(bytes, 0, 1);
        for (i = 2; i < bytes.length; i++) {
            if ((bytes[i] == '-') || (bytes[i] == '+'))
                break;
        }
        if (i == bytes.length)
            return;
        ygrav = (bytes[i] == '-') ? "-" : "+";
        xpos = Integer.parseInt(new String(bytes, 1, i - 1));
        ++i;
        ypos = Integer.parseInt(new String(bytes, i, bytes.length - i));
    }

    /** Position the text graphic */
    protected void positionText(int w, int h) {
        int xoff, yoff, justify;
        if (xgrav.equals("+")) {
            xoff = xpos;
            justify = OMText.JUSTIFY_LEFT;
        } else {
            xoff = w - xpos;
            justify = OMText.JUSTIFY_RIGHT;
        }
        if (ygrav.equals("+")) {
            yoff = ypos;
        } else {
            yoff = h - ypos;
        }
        text.setX(xoff);
        text.setY(yoff);
        text.setJustify(justify);
    }

    /**
     * Set the text to display
     * 
     * @param s String
     */
    public void setLabelText(String s) {
        labelText = s;
    }

    /**
     * Get the String to display
     * 
     * @return String
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Paints the layer.
     * 
     * @param g the Graphics context for painting
     */
    public void paint(Graphics g) {
        Projection p = getProjection();

        if (p == null)
            return;

        if (Debug.debugging("labellayer")) {
            System.out.println("labelLayer.paint(): " + labelText);
        }

        positionText(p.getWidth(), p.getHeight());
        text.setData(labelText);
        text.generate(p);//to get bounds

        // render graphics
        text.render(g);
    }

    //----------------------------------------------------------------------
    // Taskable Interface
    //----------------------------------------------------------------------

    /**
     * Invoked by a javax.swing.Timer.
     * 
     * @param e ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (Debug.debugging("labellayer")) {
            System.out.println("LabelLayer.actionPerformed()");
        }

        repaint();
    }

    //----------------------------------------------------------------------
    // MapMouseListener Interface
    //----------------------------------------------------------------------

    /**
     * Returns the MapMouseListener object that handles the mouse
     * events.
     * 
     * @return MapMouseListener this
     */
    public MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.
     * 
     * @return String[] { SelectMouseMode.modeID }
     */
    public String[] getMouseModeServiceList() {
        return new String[] { SelectMouseMode.modeID };
    }

    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (text.distance(x, y) <= 0f) {
            dragging = true;
            dragX = x;
            dragY = y;
            return true;
        }
        return false; // did not handle the event
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
        dragging = false;
        return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
        return false;
    }

    /**
     * Invoked when the mouse enters a component.
     * 
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     * 
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {}

    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then
     * dragged. The listener will receive these events if it
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
        Projection proj = getProjection();
        int w = proj.getWidth();
        int h = proj.getHeight();
        int x = e.getX();
        int y = e.getY();

        // limit coordinates
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x > w)
            x = w;
        if (y > h)
            y = h;

        // calculate deltas
        int dx = x - dragX;
        int dy = y - dragY;

        if (dragging) {
            // reset dragging parms
            dragX = x;
            dragY = y;
            // reset graphics positions
            text.setX(text.getX() + dx);
            text.setY(text.getY() + dy);
            repaint();
            return true;
        }
        return false;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * 
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {
        return false;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}

    public int getSleepHint() {
        return 1000000;//update every 1000 seconds
    }

}