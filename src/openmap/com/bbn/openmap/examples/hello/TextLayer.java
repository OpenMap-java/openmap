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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/examples/hello/TextLayer.java,v
// $
// $RCSfile: TextLayer.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:46 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.examples.hello;

import java.awt.*;
import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;

public class TextLayer extends Layer {

    // Projection projection; // not needed in this very simple layer

    /**
     * During construction, we'll fill this with this Font we wish to
     * use
     */
    Font font;

    /**
     * Construct a TextLayer instance.
     */
    public TextLayer() {
        font = new Font("TimesRoman", Font.BOLD + Font.ITALIC, 48);
        setName("Hello, World"); // pretty name for menus
    }

    /**
     * In this method we paint on the screen whatever is appropriate
     * for this layer.
     */
    public void paint(Graphics g) {
        Rectangle r = g.getClipBounds();
        int halfHeight = r.height / 2;
        int halfWidth = r.width / 2;
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics(font);
        int halfStringWidth = fm.stringWidth(HelloWorld.message) / 2;
        g.setColor(Color.red);
        g.drawString(HelloWorld.message,
                halfWidth - halfStringWidth,
                halfHeight);
    }

    /**
     * We have to implement this method. In this simple case, it turns
     * out we don't have to reshape our "Hello, World!" display for
     * the projection, so this method becomes a NOP.
     * 
     * Normally in this method we would get the projection, and then
     * either send it a forward message, or send an OMGraphics the
     * project message, and then call repaint().
     */
    public void projectionChanged(ProjectionEvent e) {}

}