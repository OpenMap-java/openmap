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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/FeatureCacheGraphicList.java,v $
// $RCSfile: FeatureCacheGraphicList.java,v $
// $Revision: 1.1 $
// $Date: 2004/02/01 21:21:59 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.DrawingAttributes;

import java.util.Iterator;

public class FeatureCacheGraphicList extends OMGraphicList implements Cloneable {

    public FeatureCacheGraphicList() {}

    protected String featureName = null;

    public FeatureCacheGraphicList(int initSize) {
        super(initSize);
    }

    public void setFeatureName(String name) {
        featureName = name;
    }

    public String getFeatureName() {
        return featureName;
    }

    public synchronized void setTo(DrawingAttributes da) {
        if (da != null) {
            for (Iterator it = iterator(); it.hasNext();) {
                da.setTo((OMGraphic)it.next());
            }
        }
    }

    public synchronized void setDrawingAttributes(VPFFeatureGraphicWarehouse vfgw) {
        setTo(vfgw.getAttributesForFeature(featureName));
    }

}