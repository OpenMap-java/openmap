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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/DataBoundsViewMenuItem.java,v
// $
// $RCSfile: DataBoundsViewMenuItem.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:49 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.bbn.openmap.InformationDelegator;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;

public class DataBoundsViewMenuItem extends MapHandlerMenuItem implements
        ActionListener {

    protected MapBean map;
    protected InformationDelegator infoDelegator;
    protected DataBoundsProvider provider;

    public DataBoundsViewMenuItem(DataBoundsProvider dbp) {
        super(dbp.getName());
        provider = dbp;
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        if (map != null) {
            Proj proj = (Proj) map.getProjection();
            DataBounds bounds = provider.getDataBounds();

            if (bounds != null) {
                java.awt.geom.Point2D center = bounds.getCenter();
                if (center != null) {
                    proj.setCenter((float) center.getY(), (float) center.getX());
                    LatLonPoint llp1 = new LatLonPoint(bounds.getMax().getY(), bounds.getMin()
                            .getX());

                    LatLonPoint llp2 = new LatLonPoint(bounds.getMin().getY(), bounds.getMax()
                            .getX());

                    // 1.1 buffers the edges for viewing a little, a
                    // little zommed out.
                    proj.setScale(ProjMath.getScale(llp1, llp2, proj) * 1.1f);
                    map.setProjection(proj);
                }
            } else {
                String complaint = "Can't move map over data: "
                        + provider.getName() + " isn't ready.  Add to map?";
                if (infoDelegator != null) {
                    infoDelegator.displayMessage("Go Over Data", complaint);
                } else {
                    Debug.error(complaint);
                }
            }
        }
    }

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof MapHandler) {
            // Check to see if the MapBean is already available.
            map = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");
            infoDelegator = (InformationDelegator) mapHandler.get("com.bbn.openmap.InformationDelegator");
        }

        if (someObj instanceof MapBean) {
            map = (MapBean) someObj;
        }

        if (someObj instanceof InformationDelegator) {
            infoDelegator = (InformationDelegator) someObj;
        }
    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);

        if (someObj instanceof MapBean && map == someObj) {
            map = null;
        }

        if (someObj instanceof InformationDelegator && infoDelegator == someObj) {

            infoDelegator = null;
        }
    }
}

