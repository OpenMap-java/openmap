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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/asrp/ASRPLayer.java,v $
// $RCSfile: ASRPLayer.java,v $
// $Revision: 1.1 $
// $Date: 2004/03/04 04:14:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.asrp;

import com.bbn.openmap.dataAccess.asrp.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

import java.io.IOException;

public class ASRPLayer extends OMGraphicHandlerLayer {

    public ASRPLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    ASRPDirectory asrpDir = null;

    public synchronized OMGraphicList prepare() {

        Projection proj = getProjection();

        try {

            if (asrpDir == null) {
                asrpDir = new ASRPDirectory("/data/asrp/36114N");
            }
        
            return asrpDir.getTiledImages(proj);

        } catch (IOException ioe) {
            Debug.error("ASRPLayer(" + getName() + 
                        ") caught exception reading tiles:\n" + 
                        ioe.getMessage());
            return null;
        }
    }
}
