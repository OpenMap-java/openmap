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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/plugin/pilot/PilotLoader.java,v $
// $RCSfile: PilotLoader.java,v $
// $Revision: 1.5 $
// $Date: 2009/02/23 22:37:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.pilot;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.graphicLoader.MMLGraphicLoader;
import com.bbn.openmap.omGraphics.BasicStrokeEditor;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * The PilotLoader is a managing object that pushes Pilots and
 * PilotPaths to the PilotLoaderPlugIn for display on the map.
 */
public class PilotLoader extends MMLGraphicLoader implements ActionListener,
        DrawingToolRequestor {

    protected OMDrawingTool dt = null;

    public final static String AddNodeCmd = "AddNodeCommand";
    public final static String AddPathCmd = "AddPathCommand";

    HashMap points = new HashMap();

    public PilotLoader() {
        super();
    }

    public PilotLoader(OMGraphicHandler receiver) {
        this();

        this.receiver = receiver;
        //      getTimer().start();
        //      Debug.message("pilotLoader", "PilotLoader starting timer");
    }

    public void manageGraphics() {
        OMGraphicList list = new OMGraphicList();

        Iterator it = points.values().iterator();
        Pilot mp;

        while (it.hasNext()) {
            mp = (Pilot) it.next();
            mp.move(40f);
            mp.resetConnected();
            list.add(mp);
        }

        if (receiver != null) {
            Debug.message("pilotloader",
                    "PilotLoader.manageGraphics: Updating graphics.");
            receiver.setList(list);
        } else {
            Debug.message("pilotloader",
                    "PilotLoader.manageGraphics: no receiver to notify.");
        }
    }

    public void actionPerformed(java.awt.event.ActionEvent ae) {
        String cmd = ae.getActionCommand();
        if (cmd == TimerCmd) {
            JCheckBox check = (JCheckBox) ae.getSource();
            if (check.isSelected()) {
                timer.restart();
            } else {
                timer.stop();
            }
        } else if (cmd == AddNodeCmd) {
            if (dt != null) {
                GraphicAttributes ga = new GraphicAttributes();
                ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
                OMPoint pt = (OMPoint) dt.create("com.bbn.openmap.omGraphics.OMPoint",
                        ga,
                        this,
                        false);
                if (pt != null) {
                    getTimer().stop();
                    pt.setRadius(5);
                    pt.setOval(true);
                    pt.setFillPaint(Color.red);
//                    addNodeButton.setEnabled(false);
                    addPathButton.setEnabled(false);
                }
            }
        } else if (cmd == AddPathCmd) {
            if (dt != null) {
                GraphicAttributes ga = new GraphicAttributes();
                ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
                ga.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
                OMPoly poly = (OMPoly) dt.create("com.bbn.openmap.omGraphics.OMPoly",
                        ga,
                        this,
                        true);
                if (poly != null) {
                    getTimer().stop();
                    BasicStrokeEditor bse = new BasicStrokeEditor();
                    bse.setDash(new float[] { 5, 5 });
                    ga.setStroke(bse.getBasicStroke());
                    ga.setLinePaint(Color.yellow);

                    //                  addNodeButton.setEnabled(false);
                    addPathButton.setEnabled(false);
                }
            }
        }

        manageGraphics();
    }

    /**
     * MapHandlerChild methods modified to look for the OMDrawingTool.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof OMDrawingTool) {
            Debug.message("graphicloader",
                    "LOSGraphicLoader: found OMDrawingTool");
            setDrawingTool((OMDrawingTool) obj);
        }
    }

    /**
     * MapHandlerChild methods modified to look for the OMDrawingTool.
     */
    public void findAndUndo(Object obj) {
        if (obj instanceof OMDrawingTool) {
            Debug.message("graphicloader",
                    "LOSGraphicLoader: removing OMDrawingTool");
            OMDrawingTool odt = getDrawingTool();
            if (odt == obj) {
                setDrawingTool(null);
            }
        }
    }

    public void setDrawingTool(OMDrawingTool drawingTool) {
        dt = drawingTool;
//        if (addNodeButton != null) {
//            addNodeButton.setEnabled(drawingTool != null);
//        }
        if (addPathButton != null) {
            addPathButton.setEnabled(drawingTool != null);
        }
    }

    public OMDrawingTool getDrawingTool() {
        return dt;
    }

    JCheckBox timerButton = null;
    //JButton addNodeButton = null;
    JButton addPathButton = null;

    public Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        //      if (addNodeButton == null) {
        //          addNodeButton = new JButton("Add Node...");
        //          addNodeButton.addActionListener(this);
        //          addNodeButton.setActionCommand(AddNodeCmd);
        //      }

        //      addNodeButton.setEnabled(getDrawingTool() != null);

        if (addPathButton == null) {
            addPathButton = new JButton("Add Path for Pilot");
            addPathButton.addActionListener(this);
            addPathButton.setActionCommand(AddPathCmd);
        }

        if (getDrawingTool() == null) {
            addPathButton.setEnabled(false);
            addPathButton.setToolTipText("Drawing Tool not connected, can't create Pilot path.");
        } else {
            addPathButton.setToolTipText("Click to use Drawing Tool to create Pilot path.");
        }

        //      panel.add(addNodeButton);
        panel.add(addPathButton);

        // Only want to do this once...
        if (timerButton == null) {
            timerButton = new JCheckBox("Run Timer", getTimer().isRunning());
            timerButton.addActionListener(this);
            timerButton.setActionCommand(TimerCmd);
        }

        panel.add(timerButton);

        return panel;
    }

    public static int pointCount = 1;

    /**
     * The method where a graphic, and an action to take on the
     * graphic, arrives.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        if (timerButton.isSelected()) {
            timer.restart();
        }

        if (omg instanceof OMPoint) {

            OMPoint p = (OMPoint) omg;

            Pilot mp = new Pilot(p.getLat(), p.getLon(), p.getRadius(), true);
            mp.setName("Added Node " + (pointCount++));
            mp.setStationary(true);
            mp.showPalette();
            points.put(mp.getName(), mp);
            manageGraphics();
        } else if (omg instanceof OMPoly) {
            OMPoly poly = (OMPoly) omg;
            PilotPath pmp = new PilotPath(poly, 5, true);
            pmp.setName("Added Node " + (pointCount++));
            pmp.setStationary(true);
            pmp.showPalette();
            points.put(pmp.getName(), pmp);
            pmp.setMapHandler((MapHandler) getBeanContext());
            manageGraphics();
        }

        //      addNodeButton.setEnabled(true);
        addPathButton.setEnabled(true);
    }

    /**
     * Needed to fill in a GUI with a receiver's name, to enable the
     * user to send a graphic to a specific object. Should be a pretty
     * name, suitable to let a user know what it is.
     */
    public String getName() {
        return "";
    }
}