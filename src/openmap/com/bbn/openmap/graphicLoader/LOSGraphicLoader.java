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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/LOSGraphicLoader.java,v $
// $RCSfile: LOSGraphicLoader.java,v $
// $Revision: 1.6 $
// $Date: 2006/02/16 16:22:45 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.omGraphics.BasicStrokeEditor;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicHandler;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.DrawUtil;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.terrain.LOSGenerator;

/**
 * A managing object of GLPoints and PathGLPoints. Has a timer to move
 * them around and uses the DTED data to figure out which GLPoints can
 * see each other.
 */
public class LOSGraphicLoader extends MMLGraphicLoader implements
        ActionListener, DrawingToolRequestor {

    protected DTEDFrameCache dfc = null;
    protected LOSGenerator los;
    protected OMDrawingTool dt = null;

    public final static String AddNodeCmd = "AddNodeCommand";
    public final static String AddPathCmd = "AddPathCommand";

    HashMap points = new HashMap();

    public static Logger logger = Logger.getLogger("com.bbn.openmap.graphicLoader.LOSGraphicLoader");
    
    public LOSGraphicLoader() {
        super();
        setName("LOS Demo");
    }

    public LOSGraphicLoader(OMGraphicHandler receiver) {
        super();
        setReceiver(receiver);
    }

    public LOSGraphicLoader(DTEDFrameCache dfc, OMGraphicHandler receiver) {
        this(receiver);

        setDTEDFrameCache(dfc);

        //      GLPoint mp1 = new GLPoint(42f, -72.5f, 5, true);
        //      mp1.setName("First");
        //      mp1.setHeight(100);
        //      points.put(mp1.getName(), mp1);

        //      GLPoint mp2 = new GLPoint(42f, -72.5f, 5, true);
        //      mp2.setName("Second");
        //      mp2.setStationary(false);
        //      points.put(mp2.getName(), mp2);

        //      GLPoint mp3 = new GLPoint(42f, -72.5f, 5, true);
        //      mp3.setName("Third");
        //      mp3.setStationary(false);
        //      points.put(mp3.getName(), mp3);

        manageGraphics();
    }

    public void manageGraphics() {
        OMGraphicList list = new OMGraphicList();

        Iterator it = points.values().iterator();
        GLPoint mp;

        while (it.hasNext()) {
            mp = (GLPoint) it.next();
            mp.move(40f);
            mp.resetConnected();
            list.add(mp);
        }

        it = points.keySet().iterator();
        while (it.hasNext()) {
            String mpName = (String) it.next();
            mp = (GLPoint) points.get(mpName);

            Iterator it2 = points.values().iterator();
            while (it2.hasNext()) {
                GLPoint mp2 = (GLPoint) it2.next();
                if (mp2 != mp) {
                    isLOS(mp, mp2, list);
                }
            }
        }

        if (receiver != null) {
            logger.fine("Updating graphics.");
            receiver.setList(list);
        } else {
            logger.fine("no receiver to notify.");
        }

    }

    public boolean isLOS(GLPoint pt1, GLPoint pt2, OMGraphicList list) {
        boolean ret = false;
        if (los != null) {
            int numPoints = 2;
            if (proj != null) {
                Point2D p1 = proj.forward(pt1.getLat(), pt1.getLon());
                Point2D p2 = proj.forward(pt2.getLat(), pt2.getLon());
                numPoints = (int) DrawUtil.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY()) / 2;
            }

            boolean isLOS = los.isLOS(new LatLonPoint.Double(pt1.getLat(), pt1.getLon()),
                    pt1.getHeight(), true,
                    new LatLonPoint.Double(pt2.getLat(), pt2.getLon()),
                    pt2.getHeight(),
                    numPoints);

            if (isLOS) {
                OMLine line = new OMLine(pt1.getLat(), pt1.getLon(), pt2.getLat(), pt2.getLon(), OMGraphic.LINETYPE_GREATCIRCLE);

                line.setLinePaint(GLPoint.CONNECTED_COLOR);
                list.add(line);

                ret = isLOS;
            }
        } else {
            logger.fine("LOSGraphicLoader doesn't have a LOSGenerator");
        }
        pt1.connected(ret);
        pt2.connected(ret);
        return ret;
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
                    addNodeButton.setEnabled(false);
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

                    addNodeButton.setEnabled(false);
                    addPathButton.setEnabled(false);
                }
            }
        } else {
            manageGraphics();
        }

    }

    public void setDrawingTool(OMDrawingTool drawingTool) {
        dt = drawingTool;
        if (addNodeButton != null) {
            addNodeButton.setEnabled(drawingTool != null);
        }
        if (addPathButton != null) {
            addPathButton.setEnabled(drawingTool != null);
        }
    }

    public OMDrawingTool getDrawingTool() {
        return dt;
    }

    public void setDTEDFrameCache(DTEDFrameCache cache) {
        dfc = cache;
        if (cache != null) {
            getTimer().start();
            logger.fine("LOSGraphicLoader starting timer");
            if (los == null) {
                los = new LOSGenerator(dfc);
            } else {
                los.setDtedCache(dfc);
            }
        }
    }

    public DTEDFrameCache getDTEDFrameCache() {
        return dfc;
    }

    JCheckBox timerButton = null;
    JButton addNodeButton = null;
    JButton addPathButton = null;

    public Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        if (addNodeButton == null) {
            addNodeButton = new JButton("Add Node...");
            addNodeButton.addActionListener(this);
            addNodeButton.setActionCommand(AddNodeCmd);
        }

        addNodeButton.setEnabled(getDrawingTool() != null);

        if (addPathButton == null) {
            addPathButton = new JButton("Add Path for Node...");
            addPathButton.addActionListener(this);
            addPathButton.setActionCommand(AddPathCmd);
        }

        addPathButton.setEnabled(getDrawingTool() != null);

        panel.add(addNodeButton);
        panel.add(addPathButton);

        // Only want to do this once...
        if (timerButton == null && getTimer() != null) {
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

            GLPoint mp = new GLPoint(p.getLat(), p.getLon(), p.getRadius(), true);
            mp.setName("Added Node " + (pointCount++));
            mp.setStationary(true);
            mp.showPalette();
            points.put(mp.getName(), mp);
            manageGraphics();
        } else if (omg instanceof OMPoly) {
            OMPoly poly = (OMPoly) omg;
            PathGLPoint pmp = new PathGLPoint(poly, 5, true);
            pmp.setName("Added Node " + (pointCount++));
            pmp.showPalette();
            points.put(pmp.getName(), pmp);
            manageGraphics();
        }

        addNodeButton.setEnabled(true);
        addPathButton.setEnabled(true);
    }

    /**
     * MapHandlerChild methods modified to look for the DTEDFrameCache
     * and OMDrawingTool.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof DTEDFrameCache) {
            logger.fine("LOSGraphicLoader: found DTEDFrameCache");
            setDTEDFrameCache((DTEDFrameCache) obj);
        }
        if (obj instanceof OMDrawingTool) {
            logger.fine("LOSGraphicLoader: found OMDrawingTool");
            setDrawingTool((OMDrawingTool) obj);
        }
    }

    /**
     * MapHandlerChild methods modified to look for the DTEDFrameCache
     * and OMDrawingTool.
     */
    public void findAndUndo(Object obj) {
        if (obj instanceof DTEDFrameCache) {
            logger.fine("removing DTEDFrameCache");
            DTEDFrameCache dfc = getDTEDFrameCache();
            if (dfc == obj) { // Check to see if they are the same
                              // object
                setDTEDFrameCache(null);
            }
        }
        if (obj instanceof OMDrawingTool) {
            logger.fine("removing OMDrawingTool");
            OMDrawingTool odt = getDrawingTool();
            if (odt == obj) {
                setDrawingTool(null);
            }
        }
    }

}