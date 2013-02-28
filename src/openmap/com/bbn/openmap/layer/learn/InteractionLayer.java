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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/learn/InteractionLayer.java,v $
// $RCSfile: InteractionLayer.java,v $
// $Revision: 1.2 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.learn;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMTextLabeler;

/**
 * This layer demonstrates how to interact with your OMGraphics on the map,
 * getting them to change appearance with mouse events and provide additional
 * information about themselves. This layer builds on the example demonstrated
 * in the BasicDisplayLayer, which involved creating OMGraphics for the map. For
 * any of the methods listed below that are designed to assist with responding
 * to mouse events, simply return null from them if you want a no-op, which is
 * the default implementation in the OMGraphicHandlerLayer (so don't even
 * override them).
 * 
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#isHighlightable
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#highlight
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#unhighlight
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#getInfoText
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#getToolTipText
 * 
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#isSelectable
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#select
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#deselect
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#getItemsForMapMenu
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#getItemsForOMGraphicMenu
 * 
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#receivesMapEvents
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#mouseOver(MapMouseEvent)
 * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#leftClick
 */
public class InteractionLayer extends BasicLayer {

    public InteractionLayer() {
        // Sets the name of the layer that is visible in the GUI. Can also be
        // set with properties with the 'prettyName' property.
        setName("Interaction Layer");
        // This is how to set the ProjectionChangePolicy, which
        // dictates how the layer behaves when a new projection is
        // received. The StandardPCPolicy is the default policy and you don't
        // need to set it, this method call is here to illustrate where and how
        // you would make that call with a different policy.
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, true));
        // Making the setting so this layer receives events from the
        // SelectMouseMode, which has a modeID of "Gestures". Other
        // IDs can be added as needed. You need to tell the layer which
        // MouseMode it should listen to, so it can tell the MouseModes to send
        // events to it.
        setMouseModeIDsForEvents(new String[] { "Gestures" });
        
        // Instead of "Gestures", you can also use SelectMouseMode.modeID or OMMouseMode.modeID    
    }

    /**
     * Called from the prepare() method if the layer discovers that its
     * OMGraphicList is null. This method is being overridden so that TOOLTIPS
     * can be set as attributes on the OMGraphics, and retrieved later in the
     * gesturing queries.
     * 
     * @return new OMGraphicList with OMGraphics that you always want to display
     *         and reproject as necessary.
     * 
     * @see BasicLayer#prepare
     */
    public OMGraphicList init() {

        // This layer keeps a pointer to an OMGraphicList that it uses
        // for painting. It's initially set to null, which is used as
        // a flag in prepare() to signal that the OMGraphcs need to be
        // created. The list returned from prepare() gets set in the
        // layer.
        // This layer uses the StandardPCPolicy for new
        // projections, which keeps the list intact and simply calls
        // generate() on it with the new projection, and repaint()
        // which calls paint().

        /*
         * Note that the OMGraphics have their select paint set in order to
         * react to highlight calls, and an OMGraphicConstants.TOOLTIP
         * attribute set to provide tooltip text when needed.
         */

        OMGraphicList omList = new OMGraphicList();

        // Add an OMLine
        OMLine line = new OMLine(40f, -75f, 42f, -70f, OMGraphic.LINETYPE_GREATCIRCLE);
        line.setStroke(new BasicStroke(2));
        line.putAttribute(OMGraphicConstants.LABEL,
                new OMTextLabeler("Line Label"));
        line.setLinePaint(Color.red);
        line.setSelectPaint(Color.blue);
        line.putAttribute(OMGraphicConstants.TOOLTIP, "This is an OMLine.");

        omList.add(line);

        // Add a list of OMPoints.
        OMGraphicList pointList = new OMGraphicList();
        for (int i = 0; i < 100; i++) {
            OMPoint point = new OMPoint((float) (Math.random() * 89f), (float) (Math.random() * -179f), 3);
            point.putAttribute(OMGraphicConstants.TOOLTIP, "This is OMPoint #"
                    + i);
            point.setLinePaint(Color.green);
            point.setSelectPaint(Color.yellow);
            pointList.add(point);
        }
        omList.add(pointList);

        return omList;
    }

    /**
     * Query that an OMGraphic can be highlighted when the mouse moves over it.
     * If the answer is true, then highlight with this OMGraphics will be
     * called, and unhighlight will be called with the mouse is moved off of it.
     * 
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#highlight
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#unhighlight
     */
    public boolean isHighlightable(OMGraphic omg) {
        return true;
    }

    /**
     * Query that an OMGraphic is selectable. Examples of handing selection are
     * in the EditingLayer. The default OMGraphicHandlerLayer behavior is to add
     * the OMGraphic to an OMGraphicList called selectedList. If you aren't
     * going to be doing anything in particular with the selection, then return
     * false here to reduce the workload of the layer.
     * 
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#select
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#deselect
     */
    public boolean isSelectable(OMGraphic omg) {
        return true;
    }

    /**
     * Designate a list of OMGraphics as selected.
     * 
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#select
     */
    public void select(OMGraphicList list) {
        super.select(list);

        // selectedList is a member variable held by OMGraphicHandlerLayer.
        if (selectedList != null) {
            System.out.println("Current selection list: " + selectedList.getDescription());
        }
    }

    /**
     * Designate a list of OMGraphics as deselected.
     * 
     * @see com.bbn.openmap.layer.OMGraphicHandlerLayer#deselect
     */
    public void deselect(OMGraphicList list) {
        super.deselect(list);

        // selectedList is a member variable held by OMGraphicHandlerLayer.
        if (selectedList != null) {
            System.out.println("Current selection list: " + selectedList.getDescription());
        }
    }

    /**
     * Query for what text should be placed over the information bar when the
     * mouse is over a particular OMGraphic.
     */
    public String getInfoText(OMGraphic omg) {
        String classname = omg.getClass().getName();
        return "Interaction Layer OMGraphic - "
                + classname.substring(classname.lastIndexOf('.') + 1);
    }

    /**
     * Query for what tooltip to display for an OMGraphic when the mouse is over
     * it.
     */
    public String getToolTipTextFor(OMGraphic omg) {
        Object tt = omg.getAttribute(OMGraphic.TOOLTIP);
        if (tt instanceof String) {
            return (String) tt;
        } else {
            return null;
        }
    }

    /**
     * This method is called when a right mouse click is detected over the map
     * and not over an OMGraphic. You can provide a List of components to be
     * displayed in a popup menu. You have to do the wiring for making the list
     * components do something, though.
     */
    public List<Component> getItemsForMapMenu(MapMouseEvent me) {
        List<Component> l = new ArrayList<Component>();
        JMenuItem when = new JMenuItem("When");
        when.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fireRequestMessage("When was chosen.");
            }
        });
        JMenuItem where = new JMenuItem("Where");
        where.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fireRequestMessage("Where was chosen.");
            }
        });
        JMenuItem how = new JMenuItem("How");
        how.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                fireRequestMessage("How was chosen.");
            }
        });

        l.add(when);
        l.add(where);
        l.add(how);

        return l;
    }

    /**
     * This method is called when a right mouse click is detected over an
     * OMGraphic. You can provide a List of components to be displayed in a
     * popup menu. You have to do the wiring for making the list components do
     * something, though.
     */
    public List<Component> getItemsForOMGraphicMenu(OMGraphic omg) {
        final OMGraphic chosen = omg;
        List<Component> l = new ArrayList<Component>();
        JMenuItem which = new JMenuItem("Which");
        which.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String classname = chosen.getClass().getName();
                fireRequestMessage("Which was chosen over "
                        + classname.substring(classname.lastIndexOf('.') + 1));
            }
        });
        JMenuItem why = new JMenuItem("Why");
        why.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String classname = chosen.getClass().getName();
                fireRequestMessage("Why was chosen over "
                        + classname.substring(classname.lastIndexOf('.') + 1));
            }
        });

        l.add(which);
        l.add(why);
        return l;
    }
}