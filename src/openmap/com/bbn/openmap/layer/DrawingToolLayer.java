// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DrawingToolLayer.java,v $
// $RCSfile: DrawingToolLayer.java,v $
// $Revision: 1.25 $
// $Date: 2004/09/30 22:39:29 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import com.bbn.openmap.dataAccess.shape.EsriShapeExport;
import com.bbn.openmap.event.*;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.omGraphics.event.MapMouseInterpreter;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import javax.swing.*;

/**
 * This layer can receive graphics from the OMDrawingToolLauncher, and
 * also sent it's graphics to the OMDrawingTool for editing.
 * <P>
 * 
 * The projectionChanged() and paint() methods are taken care of in
 * the OMGraphicHandlerLayer superclass.
 * <P>
 * 
 * This class responds to all the properties that the
 * OMGraphicHandlerLayer repsonds to, including the mouseModes
 * property. If the mouseModes property isn't set, the
 * SelectMouseMode.modeID mode ID is set. When the MapMouseInterpreter
 * calls select(OMGraphic), the OMGraphic is passed to the
 * DrawingTool. This class also responds to the showHints property
 * (true by default), which dictates if tooltips and information
 * delegator text is displayed when the layer's contents are moused
 * over.
 */
public class DrawingToolLayer extends OMGraphicHandlerLayer implements
        DrawingToolRequestor {

    /** Get a handle on the DrawingTool. */
    protected OMDrawingTool drawingTool;

    /**
     * A flag to provide a tooltip over OMGraphics to click to edit.
     */
    protected boolean showHints = true;

    public final static String ShowHintsProperty = "showHints";

    protected boolean DTL_DEBUG = false;

    public DrawingToolLayer() {
        setList(new OMGraphicList());
        setAddToBeanContext(true);

        DTL_DEBUG = Debug.debugging("dtl");
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        showHints = PropUtils.booleanFromProperties(props, realPrefix
                + ShowHintsProperty, showHints);

        if (getMouseModeIDsForEvents() == null) {
            setMouseModeIDsForEvents(new String[] { SelectMouseMode.modeID });
        }
    }

    public OMDrawingTool getDrawingTool() {
        return drawingTool;
    }

    public void setDrawingTool(OMDrawingTool dt) {
        drawingTool = dt;
    }

    /**
     * DrawingToolRequestor method.
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {

        if (DTL_DEBUG) {
            String cname = omg.getClass().getName();
            int lastPeriod = cname.lastIndexOf('.');
            if (lastPeriod != -1) {
                cname = cname.substring(lastPeriod + 1);
            }
            Debug.output("DrawingToolLayer: DrawingTool complete for " + cname
                    + " > " + action);
        }
        // First thing, release the proxy MapMouseMode, if there is
        // one.
        releaseProxyMouseMode();

        // GRP, assuming that selection is off.
        OMGraphicList omgl = new OMGraphicList();
        omgl.add(omg);
        deselect(omgl);

        OMGraphicList list = getList();
        if (list != null) {
            doAction(omg, action);
            repaint();
        } else {
            Debug.error("Layer " + getName() + " received " + omg + " and " + action
                    + " with no list ready");
        }
    }

    /**
     * If the DrawingToolLayer is using a hidden OMDrawingTool,
     * release the proxy lock on the active MapMouseMode.
     */
    public void releaseProxyMouseMode() {
        MapMouseMode pmmm = getProxyMouseMode();
        OMDrawingTool dt = getDrawingTool();
        if (pmmm != null && dt != null) {
            if (pmmm.isProxyFor(dt.getMouseMode())) {
                if (DTL_DEBUG) {
                    Debug.output("DTL: releasing proxy on " + pmmm.getID());
                }

                pmmm.releaseProxy();
                setProxyMouseMode(null);
                fireRequestInfoLine(""); // hidden drawing tool put up
                                         // coordinates, clean up.
            }

            if (dt.isActivated()) {
                dt.deactivate();
            }
        }
    }

    /**
     * Called by findAndInit(Iterator) so subclasses can find objects,
     * too.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof OMDrawingTool) {
            Debug.message("dtl", "DrawingToolLayer: found a drawing tool");
            setDrawingTool((OMDrawingTool) someObj);
        }
    }

    /**
     * BeanContextMembershipListener method. Called when a new object
     * is removed from the BeanContext of this object.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof DrawingTool && getDrawingTool() == someObj) {

            setDrawingTool(null);
        }
    }

    protected MapMouseMode proxyMMM = null;

    /**
     * Set the ProxyMouseMode for the internal drawing tool, if there
     * is one. Can be null. Used to reset the mouse mode when
     * drawing's complete.
     */
    protected synchronized void setProxyMouseMode(MapMouseMode mmm) {
        proxyMMM = mmm;
    }

    /**
     * Get the ProxyMouseMode for the internal drawing tool, if there
     * is one. May be null. Used to reset the mouse mode when
     * drawing's complete.
     */
    protected synchronized MapMouseMode getProxyMouseMode() {
        return proxyMMM;
    }

    /**
     * A method called from within different MapMouseListener methods
     * to check whether an OMGraphic *should* be edited if the
     * OMDrawingTool is able to edit it. Can be used by subclasses to
     * delineate between OMGraphics that are non-relocatable versus
     * those that can be moved. This method should work together with
     * the getToolTipForOMGraphic() method so that OMGraphics that
     * shouldn't be edited don't provide tooltips that suggest that
     * they can be.
     * <P>
     * 
     * By default, this method always returns true because the
     * DrawingToolLayer always thinks the OMGraphic should be edited.
     */
    public boolean shouldEdit(OMGraphic omgr) {
        return true;
    }

    public Component getGUI() {

        JPanel box = PaletteHelper.createVerticalPanel("Save Layer Graphics");
        box.setLayout(new java.awt.GridLayout(0, 1));
        JButton button = new JButton("Save As Shape File");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                OMGraphicList list = getList();
                if (list != null) {
                    EsriShapeExport ese = new EsriShapeExport(list, getProjection(),
                            null);
                    ese.export();
                } else {
                    fireRequestMessage("There's nothing on the map for this layer to save.");
                }
            }
        });
        box.add(button);

        return box;
    }

    /**
     * A flag to provide a tooltip over OMGraphics to click to edit.
     */
    public void setShowHints(boolean show) {
        showHints = show;
    }

    public boolean getShowHints() {
        return showHints;
    }

    /**
     * Query that an OMGraphic can be highlighted when the mouse moves
     * over it. If the answer is true, then highlight with this
     * OMGraphics will be called, in addition to getInfoText() and
     * getToolTipTextFor()
     */
    public boolean isHighlightable(OMGraphic omg) {
        return showHints;
    }

    /**
     * Query that an OMGraphic is selectable.
     */
    public boolean isSelectable(OMGraphic omg) {
        DrawingTool dt = getDrawingTool();
        return (shouldEdit(omg) && dt != null && dt.canEdit(omg.getClass()));
    }

    /**
     * Query for what text should be placed over the information bar
     * when the mouse is over a particular OMGraphic.
     */
    public String getInfoText(OMGraphic omg) {
        DrawingTool dt = getDrawingTool();
        if (dt != null && dt.canEdit(omg.getClass())) {
            return "Click to edit.";
        } else {
            return null;
        }
    }

    /**
     * Query for what tooltip to display for an OMGraphic when the
     * mouse is over it.
     */
    public String getToolTipTextFor(OMGraphic omgr) {
        OMDrawingTool dt = getDrawingTool();
        if (shouldEdit(omgr) && dt.canEdit(omgr.getClass()) && !dt.isActivated()) {
            return "Click to Edit";
        } else {
            return null;
        }
    }

    /**
     * GestureResponsePolicy method.
     */
    public void select(OMGraphicList omgl) {
        super.select(omgl);
        if (omgl != null && omgl.size() > 0) {
            if (omgl.size() == 1) {
                edit(omgl.getOMGraphicAt(0));
            } else {
                edit(omgl);
            }
        }
    }

    public void edit(OMGraphic omg) {

        OMDrawingTool dt = getDrawingTool();

        if (dt != null && dt.canEdit(omg.getClass())) {

            //          if (dt.isEditing(omg)) {
            //              dt.deselect(omg);
            //              return;
            //          }

            dt.resetBehaviorMask();

            MapMouseMode omdtmm = dt.getMouseMode();
            if (!omdtmm.isVisible()) {
                dt.setMask(OMDrawingTool.PASSIVE_MOUSE_EVENT_BEHAVIOR_MASK);
            }

            MapMouseInterpreter mmi = (MapMouseInterpreter) getMapMouseListener();

            MouseEvent mevent = null;
            if (mmi != null) {
                mevent = mmi.getCurrentMouseEvent();
            }

            if (omg.isSelected()) {
                omg.deselect();
            }

            if (dt.select(omg, this, mevent)) {
                // OK, means we're editing - let's lock up the
                // MouseMode
                if (DTL_DEBUG) {
                    Debug.output("DTL: starting edit of OMGraphic...");
                }

                // Check to see if the DrawingToolMouseMode wants to
                // be invisible. If it does, ask the current
                // active MouseMode to be the proxy for it...
                if (!omdtmm.isVisible() && mevent instanceof MapMouseEvent) {
                    MapMouseMode mmm = ((MapMouseEvent) mevent).getMapMouseMode();
                    if (mmm
                            .actAsProxyFor(
                                           omdtmm,
                                           MapMouseSupport.PROXY_DISTRIB_MOUSE_MOVED
                                                   & MapMouseSupport.PROXY_DISTRIB_MOUSE_DRAGGED)) {
                        if (DTL_DEBUG) {
                            Debug.output("DTL: Setting " + mmm.getID()
                                    + " as proxy for drawing tool");
                        }
                        setProxyMouseMode(mmm);
                    } else {
                        // WHOA, couldn't get proxy lock - bail
                        if (DTL_DEBUG) {
                            Debug.output("DTL: couldn't get proxy lock on "
                                    + mmm.getID()
                                    + " deactivating internal drawing tool");
                        }
                        dt.deactivate();
                    }
                } else {
                    if (DTL_DEBUG) {
                        Debug.output("DTL: MouseMode wants to be visible("
                                + (omdtmm.isVisible())
                                + "), or MouseEvent is not a MapMouseEvent("
                                + !(mevent instanceof MapMouseEvent) + ")");
                    }
                }
            } else {
                if (DTL_DEBUG) {
                    Debug
                            .output("DTL.edit: dt.select returns false, avoiding modification over "
                                    + omg.getClass().getName());
                }
            }
        }
    }
}

