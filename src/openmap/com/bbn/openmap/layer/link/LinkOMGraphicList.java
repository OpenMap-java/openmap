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
// $Revision: 1.2 $
// $Date: 2003/08/14 22:28:46 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.proj.Projection;

import java.awt.Graphics;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

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

    private HashMap hash = new HashMap(541);  // Why 541?  Hmm, don't know.
    protected Projection currentProjection = null;

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

    public void setProjection(Projection proj) {
	currentProjection = proj;
    }

    public Projection getProjection() {
	return currentProjection;
    }

    /**
     * Check whether the list needs to be regenerated, considering the
     * projection that the OMGraphics were projected with when the
     * list was read.  The projection equality projection is lazy,
     * just checks objects.
     */
    public boolean getNeedToRegenerate(Projection proj) {
	return super.getNeedToRegenerate() || currentProjection != proj;
    }

    /**
     * Add an OMGraphic to the GraphicList.
     * The OMGraphic must not be null.
     *
     * @param g the non-null OMGraphic to add
     * @exception IllegalArgumentException if OMGraphic is null
     */
    public synchronized void _add(OMGeometry g) {
	super._add(g);
	String id = ((LinkProperties) g.getAppObject()).getProperty(LPC_GRAPHICID);
	if (Debug.debugging("linkdetail")) {
	    Debug.output("LinkOMGraphicList: Adding graphic, id(" + id + ")");
	}
	if (id != null) {
	    hash.put(id, g);
	}
    }

    /**
     * Remove the graphic at a location in the list.
     *
     * @param location the OMGraphic object to remove.
     * @return true if graphic was on the list, false if otherwise.
     */
    protected synchronized Object _remove(int location) {
	Object ret = super._remove(location);
	if (ret != null) {
	    String id = ((LinkProperties) ((OMGeometry)ret).getAppObject()).getProperty(LPC_GRAPHICID);
	    if (id != null) {
		hash.remove(id);
		if (Debug.debugging("link")) {
		    Debug.output("LinkOMGraphicList: Removing graphic " + id);
		}
	    }
	}

	return ret;
    }

    /**
     * Remove the graphic.  If this list is not vague, it will also
     * ask sub-OMGraphicLists to remove it if the geometry isn't found
     * on this OMGraphicList.
     *
     * @param geometry the OMGeometry object to remove.
     * @return true if geometry was on the list, false if otherwise.
     */
    protected synchronized boolean _remove(OMGeometry geometry) {
	boolean ret = super._remove(geometry);
	if (ret != false) {
	    String id = ((LinkProperties)geometry.getAppObject()).getProperty(LPC_GRAPHICID);
	    hash.remove(id);
	    if (Debug.debugging("link")) {
		Debug.output("LinkOMGraphicList: Removing graphic " + id);
	    }
	}
	return ret;
    }

    /**
     * Set the graphic at the specified location.  The OMGraphic must
     * not be null, the AppObject in the OMGraphic must be null or a
     * LinkProperties object.
     *
     * @param graphic OMGraphic
     * @param index index of the OMGraphic to return
     * @exception ArrayIndexOutOfBoundsException if index is out-of-bounds
     */
    public synchronized void setOMGraphicAt(OMGraphic graphic, int index) {
	LinkProperties linkp = (LinkProperties)graphic.getAppObject();
	String id = null;
	if (linkp != null) {
	    id = linkp.getProperty(LPC_GRAPHICID);
	    Debug.message("link", "Updating graphic " + id);
	}

	OMGraphic old = null;
	if (id != null) {
	    old = (OMGraphic) hash.put(id, graphic);
	}

	// old will be any previous OMGraphic with the same ID
	// that might not be in the list, but is still in the
	// HashMap.
	if (old != null) {
	    index = indexOf(old);
	    if (index < 0) {
		addOMGraphic(graphic);
	    } else {
		setOMGraphicAt(graphic, index);
	    }
	} else {
	    // If there wasn't anything with the same ID, just add
	    // the new OMGraphic.
	    super.setOMGraphicAt(graphic, index);
	}
    }

    /**
      * Get the graphic with the graphic ID.
      * @param id graphic ID of the wanted graphic.
      * @return OMGraphic or null if not found
      */
     public OMGraphic getOMGraphicWithId(String gid) {
          return (OMGraphic) hash.get(gid);
     }

    /**
     * Get the graphic with the graphic ID. Traverse mode doesn't
     * matter.
     * @param graphicID graphic ID of the wanted graphic.  
     * @return OMGraphic index or Link.UNKNOWN if not found
     */
    public int getOMGraphicIndexWithId(String gid) {
	OMGraphic graphic = getOMGraphicWithId(gid);
	if (graphic != null) {
	    return super._indexOf(graphic);
	} else {
	    return Link.UNKNOWN;
	}
    }

    /**
     * Remove all elements from the graphic list.
     */
    public synchronized void clear() {
	super.clear();
	hash.clear();
    }

    /**
     * Renders all the objects in the list a graphics context.  This
     * is the same as <code>paint()</code> for AWT components.  The
     * graphics are rendered in the order of traverseMode.  Any
     * graphics where <code>isVisible()</code> returns false are not
     * rendered.
     *
     * @param gr the AWT Graphics context
     */
    public synchronized void render(Graphics gr) {
	OMGraphic graphic;
	ListIterator iterator;

	if (traverseMode == FIRST_ADDED_ON_TOP) {
	    iterator = graphics.listIterator(graphics.size());
	    while (iterator.hasPrevious()) {
		graphic = (OMGraphic) iterator.previous();
		if (graphic.isVisible()) {
		    Object obj = graphic.getAppObject();
		    if (Debug.debugging("linkdetail") &&
			obj instanceof LinkProperties) {
			String id = ((LinkProperties)obj).getProperty(LPC_GRAPHICID);
			Debug.output("LinkOMGraphicList: Rendering graphic " + id);
		    }
		    graphic.render(gr);
		}
	    }

	} else {
	    iterator = graphics.listIterator();

	    while (iterator.hasNext()) {
		graphic = (OMGraphic) iterator.next();
		if (graphic.isVisible()) {
		    Object obj = graphic.getAppObject();
		    if (Debug.debugging("linkdetail") &&
			obj instanceof LinkProperties) {
			String id = ((LinkProperties)obj).getProperty(LPC_GRAPHICID);
			Debug.output("LinkOMGraphicList: Rendering graphic " + id);
		    }
		    graphic.render(gr);
		}
	    }
	}
    }
}
