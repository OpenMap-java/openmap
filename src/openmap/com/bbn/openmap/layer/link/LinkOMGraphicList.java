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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkOMGraphicList.java,v $
// $RCSfile: LinkOMGraphicList.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.*;
import java.awt.Graphics;
import java.io.*;
import java.util.Iterator;

/**
 * This class extends the OMGraphicList by allowing searches on the
 * AppObject contained by the OMGraphics on the list.  The AppObject
 * is where the LinkGraphics store the graphic ID as defined by the
 * server.  It also returns indexes from searches instead of the
 * graphic.  This allows for deletions, replacements and graphic
 * location movement from within the list.
 */
public class LinkOMGraphicList extends OMGraphicList 
    implements LinkPropertiesConstants {

    /**
     * Construct an OMGraphicList.
     */
    public LinkOMGraphicList() {
	super (10);
    };
    
    /**
     * Construct an OMGraphicList with an initial capacity. 
     * @param initialCapacity the initial capacity of the list 
     */
    public LinkOMGraphicList(int initialCapacity) {
	super (initialCapacity);
    };

   /**
     * Get the graphic with the graphic ID. Traverse mode doesn't
     * matter.
     * @param graphicID graphic ID of the wanted graphic.  
     * @return OMGraphic index or Link.UNKNOWN if not found
     */
    public int getOMGraphicIndexWithId(String gid){
	java.util.List targets = getTargets();
	int ret = Link.UNKNOWN;
	int i = 0;
	if (gid != null){
	    Iterator iterator = iterator();
	    while (iterator.hasNext()) {
		OMGraphic graphic = (OMGraphic) iterator.next();
		String id = ((LinkProperties) graphic.getAppObject()).getProperty(LPC_GRAPHICID);
		if (id.equals(gid)){
		    return i;
		}
		i++;
	    }
	}
	return Link.UNKNOWN;
    }

}
