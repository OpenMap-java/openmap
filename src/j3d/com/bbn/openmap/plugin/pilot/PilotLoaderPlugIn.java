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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/plugin/pilot/PilotLoaderPlugIn.java,v $
// $RCSfile: PilotLoaderPlugIn.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:38 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.pilot;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;

import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.plugin.OMGraphicHandlerPlugIn;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * The PilotLoaderPlugIn receives OMGraphics from the PilotLoader and displays
 * them. It just waits and listens, and redraws when necessary.
 */
public class PilotLoaderPlugIn extends OMGraphicHandlerPlugIn {

    protected PilotLoader loader = null;
    protected OMDrawingTool dt = null;

    public PilotLoaderPlugIn() {
        super();
        setPilotLoader(new PilotLoader(this));
    }

    public PilotLoaderPlugIn(Component comp) {
        super(comp);
        setPilotLoader(new PilotLoader(this));
    }

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to fill the graphics list with objects that are within the
     * screen parameters passed.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width.
     */
    public OMGraphicList getRectangle(Projection p) {
        if (loader != null) {
            loader.setProjection(p);
        }
        OMGraphicList list = (OMGraphicList) super.getList();
        list.generate(p);

        if (Debug.debugging("pilotloader")) {
            Debug.output("GraphicLoaderPlugIn returning list of " + list.size()
                    + " objects.");
        }

        return list;
    } // end getRectangle

    public synchronized void setList(OMGraphicList graphics) {
        super.setList(graphics);
        doPrepare();
    }

    public synchronized boolean doAction(OMGraphic graphic, OMAction action) {
        boolean ret = super.doAction(graphic, action);
        doPrepare();
        return ret;
    }

    /**
     * Get the path/point loader.
     */
    public void setPilotLoader(PilotLoader pl) {
        loader = pl;
        setMapMouseListener(loader);

        try {
            loader.setBeanContext(getBeanContext());
        } catch (PropertyVetoException pve) {
        }
    }

    /**
     * Get the path/point loader.
     */
    public PilotLoader getPilotLoader() {
        return loader;
    }

    public Component getGUI() {
        if (loader != null) {
            return loader.getGUI();
        } else {
            return null;
        }
    }

    /**
     * Invoked when the mouse has been clicked on a component. The listener will
     * receive this event if it successfully processed
     * <code>mousePressed()</code>, or if no other listener processes the event.
     * If the listener successfully processes <code>mouseClicked()</code>, then
     * it will receive the next <code>mouseClicked()</code> notifications that
     * have a click count greater than one.
     * <p>
     * NOTE: We have noticed that this method can sometimes be erroneously
     * invoked. It seems to occur when a light-weight AWT component (like an
     * internal window or menu) closes (removes itself from the window
     * hierarchy). A specific OpenMap example is when you make a menu selection
     * when the MenuItem you select is above the MapBean canvas. After making
     * the selection, the mouseClicked() gets invoked on the MouseDelegator,
     * which passes it to the appropriate listeners depending on the MouseMode.
     * The best way to avoid this problem is to not implement anything crucial
     * in this method. Use a combination of <code>mousePressed()</code> and
     * <code>mouseReleased()</code> instead.
     * 
     * @param e MouseEvent
     * @return true if the listener was able to process the event.
     */
    public boolean mouseClicked(MouseEvent e) {

        OMGraphicList list = getList();
        
        if (list != null) {
            OMGraphic graphic = list.getContains(e.getX(), e.getY());
            if (graphic instanceof Pilot) {
                Pilot mp = (Pilot) graphic;
                mp.showPalette();
                return true;
            }
        }
        return false;
    }

    /**
     * Method for BeanContextChild interface. Adds this object as a
     * BeanContextMembership listener, set the BeanContext in this objects
     * BeanContextSupport, and receives the initial list of objects currently
     * contained in the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {
        super.setBeanContext(in_bc);
        if (loader != null) {
            loader.setBeanContext(in_bc);
        }
    }

} // end GraphicLoaderPlugin

