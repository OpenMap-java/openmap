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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/OMGraphicList.java,v $
// $RCSfile: OMGraphicList.java,v $
// $Revision: 1.2 $
// $Date: 2003/02/21 18:56:55 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.GraphicList;
import com.bbn.openmap.omGraphics.grid.*;
import com.bbn.openmap.util.Debug;

/**
 * This class encapsulates a List of OMGraphics.
 * <p>
 * There are several things that this list does that make it better that
 * any ol' List.  You can make several common OMGraphic modification
 * calls on the list, and the list handles the iteration and changing of
 * all the graphics while taking into account a travese order.
 * <p>
 * An additional benefit is that because the OMGraphicList extends
 * OMGraphic it can contain other instances of OMGraphicList.  This
 * way you can manage groupings of graphics, (for instance, an
 * OMGraphicList of OMGraphicLists which each have an OMRaster and
 * OMText).
 * <p>
 * Many methods, such as generate() and findClosest() traverse the
 * items in the GraphicsList recursively.  The direction that the list
 * is traversed is controlled by then traverseMode variable.  The
 * traverseMode mode lets you set whether the first or last object
 * added to the list (FIRST_ADDED_ON_TOP or LAST_ADDED_ON_TOP) is
 * drawn on top of the list and considered first for searches.  
 */
public class OMGraphicList extends OMGraphic implements GraphicList, Serializable {


    /**  
     * Used to set the order in which the list is traversed to draw
     * or search the objects. This means that the last things on the
     * list will be on top of the map because they are drawn last, on
     * top of everything else. For searches, objects added last to the
     * list will be considered first for a search match.
     */
    public final transient static int LAST_ADDED_ON_TOP = 0;

    /**
     * Used to set the order in which the list is traversed to draw or
     * search the objects.  This means that the first things on the
     * list will appear on top because they are drawn last, on top of
     * everything else. For searches, objects added first to the list
     * will be considered first for a search match.  This is the
     * default mode for the list.
     */
    public final transient static int FIRST_ADDED_ON_TOP = 1;

    /** 
     * Used for searches, when OMDist doesn't have a graphic.  The
     * index of a null graphic is NONE.  If you try to remove or
     * insert a graphic at NONE, an exception will be thrown.  If you
     * try to get a graphic at NONE, you'll get null;
     */
    public final static int NONE = -1;

    /**
     * List traversal mode.
     * The default is FIRST_ADDED_ON_TOP.
     */
    protected int traverseMode = FIRST_ADDED_ON_TOP;

    /**
     * The list of graphics. Once an OMGraphicList is constructed,
     * this variable should never be null.  
     */
    protected java.util.List graphics = null;

    /**
     * Flag used to allow duplicates in the OMGraphicList.  True by
     * default - this prevents the list from doing the extra work for
     * checking for duplicates at addition time.
     */
    protected boolean allowDuplicates = true;

    /**
     * Construct an OMGraphicList.
     */
    public OMGraphicList() {
	this(10);
    };
    
    /**
     * Construct an OMGraphicList with an initial capacity. 
     *
     * @param initialCapacity the initial capacity of the list 
     */
    public OMGraphicList(int initialCapacity) {
        graphics = new ArrayList(initialCapacity);
    };

    /**
     * Construct an OMGraphicList with an initial capacity and
     * a standard increment value.
     *
     * @param initialCapacity the initial capacity of the list 
     * @param capacityIncrement the capacityIncrement for resizing 
     * @deprecated capacityIncrement no longer used.
     */
    public OMGraphicList(int initialCapacity, int capacityIncrement) {
        this(initialCapacity);
    };

    /**
     * Construct an OMGraphicList around a List of OMGraphics.  The
     * OMGraphicList assumes that all the objects on the list are
     * OMGraphics, and never does checking.  Live with the
     * consequences if you put other stuff in there.
     * @param list List of OMGraphics.
     */
    public OMGraphicList(java.util.List list) {
	graphics = list;
    }

    /**
     * OMGraphic method for returning a simple description of the
     * list.  This is really a debugging method.
     */
    public String getDescription() {
	return getDescription(0);
    }

    /**
     * OMGraphic method, for returning a simple description if the
     * contents of the list.  This method handles the spacing of
     * sub-member descriptions.  This is really a debugging method.
     * @return String that represents the structure of the OMGraphicList.
     */
    public String getDescription(int level) {
	StringBuffer sb = new StringBuffer();

	if (level > 0) {
	    sb.append("|--> ");
	}

	sb.append("OMGraphicList with " + size() + 
		  " OMGraphic" + (size() == 1?"\n":"s\n"));

	synchronized (graphics) {
	    Iterator it = iterator();
	    StringBuffer sb1 = new StringBuffer();

	    for (int i = 0; i < level; i++) {
		sb1.append("     ");
	    }
	    String spacer = sb1.toString();
	
	    while (it.hasNext()) {
		sb.append(spacer + ((OMGraphic)it.next()).getDescription(level+1) + "\n");
	    }
	}
	return sb.toString();
    }
    
    /**
     * Add an OMGraphic to the GraphicList.
     * The OMGraphic must not be null.
     *
     * @param g the non-null OMGraphic to add
     * @exception IllegalArgumentException if OMGraphic is null
     */
    public void addOMGraphic(OMGraphic g) {
	_add(g);
    }

    /**
     * Add an OMGraphic to the list.
     */
    public void add(OMGraphic g) {
	_add(g);
    }

    /**
     * Add an OMGeometry to the list.
     */
    protected void _add(OMGeometry g) {
	synchronized (graphics) {
	    checkForDuplicate(g);
	    graphics.add(g);
	}
    }

    /**
     * Set the order in which the list is traversed to draw or search
     * the objects. The possible modes for the list are
     * FIRST_ADDED_ON_TOP or LAST_ADDED_ON_TOP.
     *
     * @param mode traversal mode
     */
    public void setTraverseMode(int mode) {
	traverseMode = mode;
    }

    /**
     * Get the order in which the list is traversed to draw or search
     * the objects. The possible modes for the list are
     * FIRST_ADDED_ON_TOP or LAST_ADDED_ON_TOP.
     *
     * @return int traversal mode
     */
    public int getTraverseMode() {
	return traverseMode;
    }

    /**
     * Remove all elements from the graphic list.
     */
    public void clear() {
	synchronized (graphics) {
	    graphics.clear();
	}
    }

    /**
     * Find out if the list is empty.
     *
     * @return boolean true if the list is empty, false if not
     */
    public boolean isEmpty() {
	return graphics.isEmpty();
    }

    /**
     * Find out the number of graphics in the list.
     *
     * @return int the number of graphics on the list.
     */
    public int size() {
        return graphics.size();
    }

    /**
     * Set the graphic at the specified location.
     * The OMGraphic must not be null.
     *
     * @param graphic OMGraphic
     * @param index index of the OMGraphic to return
     * @exception ArrayIndexOutOfBoundsException if index is out-of-bounds
     */
    public void setOMGraphicAt(OMGraphic graphic, int index) {
	graphics.set(index, graphic);
    }

    /**
     * Get the graphic at the location number on the list.
     *
     * @param location the location of the OMGraphic to return
     * @return OMGraphic or null if location &gt; list size
     * @exception ArrayIndexOutOfBoundsException if
     * <code>location &lt; 0</code> or <code>location &gt;=
     * this.size()</code>
     */
    public OMGraphic getOMGraphicAt(int location) {
	return (OMGraphic) _getAt(location);
    } 

    /**
     * Get the geometry at the location number on the list.
     *
     * @param location the location of the OMGeometry to return
     * @return OMGraphic or null if location &gt; list size
     * @exception ArrayIndexOutOfBoundsException if
     * <code>location &lt; 0</code> or <code>location &gt;=
     * this.size()</code>
     */
    protected OMGeometry _getAt(int location) {
	java.util.List targets = getTargets();

	if (location < 0 || location >= targets.size()) {
	    return null;
	}

	OMGeometry geom = null;
	synchronized (graphics) {
	    geom = (OMGeometry)targets.get(location);
	}
	return geom;
    }

    /**
     * Set the geometry at the specified location.
     * The OMGeometry must not be null.
     *
     * @param graphic OMGeometry
     * @param index index of the OMGeometry to return
     * @exception ArrayIndexOutOfBoundsException if index is out-of-bounds
     */
    protected void _setAt(OMGeometry graphic, int index) {
	synchronized (graphics) {
	    graphics.set(index, graphic);
	}
    }

    /**
     * Get the graphic with the appObject. Traverse mode doesn't
     * matter.  Tests object identity first, then tries equality.
     *
     * @param appObj appObject of the wanted graphic.  
     * @return OMGraphic or null if not found
     * @see Object#equals
     * @see OMGeometry#setAppObject
     * @see OMGeometry#getAppObject
     */
    public OMGraphic getOMGraphicWithAppObject(Object appObj) {
	return (OMGraphic) _getWithAppObject(appObj);
    }

    /**
     * Get the graphic with the appObject. Traverse mode doesn't
     * matter.  Tests object identity first, then tries equality.
     *
     * @param appObj appObject of the wanted graphic.  
     * @return OMGraphic or null if not found
     * @see Object#equals
     * @see OMGeometry#setAppObject
     * @see OMGeometry#getAppObject
     */
    protected OMGeometry _getWithAppObject(Object appObj) {
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		OMGeometry graphic = (OMGeometry) it.next();
		Object tObj = graphic.getAppObject();
		if ((appObj == tObj) || (appObj.equals(tObj))) {
		    return (OMGeometry)graphic;
		}
	    }
	}
	return null;
    }

    /**
     * Remove the graphic at the location number.
     *
     * @param location the location of the OMGraphic to remove.
     */
    public void removeOMGraphicAt(int location) {
	synchronized (graphics) {
	    graphics.remove(location);
	}
    }

    /**
     * Remove the graphic.
     *
     * @param location the OMGraphic object to remove.
     * @return true if graphic was on the list, false if otherwise.
     */
    public boolean remove(OMGraphic graphic) {
        return _remove(graphic);
    }

    /**
     * Remove the graphic.
     *
     * @param geometry the OMGeometry object to remove.
     * @return true if geometry was on the list, false if otherwise.
     */
    protected boolean _remove(OMGeometry geometry) {
	synchronized (graphics) {
	    return graphics.remove(geometry);
	}
    }

    /**
     * Return the index of the OMGraphic in the list.
     *
     * @param graphic the graphic to look for
     * @return the index in the list of the graphic, -1 if the object
     * is not found.  
     */
    public int indexOf(OMGraphic graphic) {
	return _indexOf(graphic);
    }

    /**
     * Return the index of the OMGeometry in the list.
     *
     * @param geometry the geometry to look for
     * @return the index in the list of the geometry, -1 if the object
     * is not found.  
     */
    protected int _indexOf(OMGeometry geometry) {
	synchronized (graphics) {
	    return graphics.indexOf(geometry);
	}
    }

    /**
     * Insert the graphic at the location number.
     * The OMGraphic must not be null.
     *
     * @param graphic the OMGraphic to insert.
     * @param location the location of the OMGraphic to insert
     * @exception IllegalArgumentException if OMGraphic is null
     * @exception ArrayIndexOutOfBoundsException if index is out-of-bounds
     */
    public void insertOMGraphicAt(OMGraphic graphic, int location) {
	synchronized (graphics) {
	    graphics.add(location, graphic);
	}
    }

    /** 
     * Moves the graphic at the given index to the part of the list
     * where it will be drawn on top of one of the other graphics
     * which is its neighbor on the list.  This method does check to
     * see what the traverseMode of the list is, and calls either
     * moveIndexedToLast or moveIndexedToFirst, depending on what is
     * appropriate.
     *
     * @param location the index location of the graphic to move.
     * @see #moveIndexedOneToFront(int)
     * @see #moveIndexedOneToBack(int)
     */
    public void moveIndexedOneToTop(int location) {
	if (traverseMode == FIRST_ADDED_ON_TOP) {
	    moveIndexedOneToFront(location);
	} else {
	    moveIndexedOneToBack(location);
	}
    }

    /** 
     * Moves the graphic at the given index to the part of the list
     * where it will be drawn on top of the other graphics.  This
     * method does check to see what the traverseMode of the list is,
     * and calls either moveIndexedToLast or
     * moveIndexedToFirst, depending on what is appropriate.
     *
     * @param location the index location of the graphic to move.  
     */
    public void moveIndexedToTop(int location) {
	if (traverseMode == FIRST_ADDED_ON_TOP) {
	    moveIndexedToFirst(location);
	} else {
	    moveIndexedToLast(location);
	}
    }

    /** 
     * Moves the graphic at the given index to the part of the list
     * where it will be drawn under one of the other graphics, its
     * neighbor on the list.  This method does check to see what the
     * traverseMode of the list is, and calls either
     * moveIndexedOneToBack or moveIndexedOneToFront, depending on
     * what is appropriate.
     *
     * @param location the index location of the graphic to move.
     * @see #moveIndexedOneToFront(int)
     * @see #moveIndexedOneToBack(int)
     */
    public void moveIndexedOneToBottom(int location) {
	OMGeometry tmpGraphic = _getAt(location);
	if (traverseMode == FIRST_ADDED_ON_TOP) {
	    moveIndexedOneToBack(location);
	} else {
	    moveIndexedOneToFront(location);
	}
    }

    /** 
     * Moves the graphic at the given index to the part of the list
     * where it will be drawn under all of the other graphics.  This
     * method does check to see what the traverseMode of the list is,
     * and calls either moveIndexedToLast or
     * moveIndexedToFirst, depending on what is appropriate.
     *
     * @param location the index location of the graphic to move.  
     */
    public void moveIndexedToBottom(int location) {
	OMGeometry tmpGraphic = _getAt(location);
	if (traverseMode == FIRST_ADDED_ON_TOP) {
	    moveIndexedToLast(location);
	} else {
	    moveIndexedToFirst(location);
	}
    }

    /** 
     * Moves the graphic at the given index to the front of the list,
     * sliding the other graphics back on in the list in order. If the
     * location is already at the beginning or beyond the end, nothing
     * happens.
     *
     * @param location the index of the graphic to move. 
     * @see #moveIndexedToBottom(int)
     * @see #moveIndexedToTop(int)
     */
    public void moveIndexedToFirst(int location) {
	int listSize = graphics.size();
	if (location > 0 && location < listSize) {
	    OMGeometry tmpGraphic = _getAt(location);
	    for (int i = location; i > 0; i--) {
		_setAt(_getAt(i-1), i);
	    }
	    _setAt(tmpGraphic, 0);
	}
    }

    /** 
     * Moves the graphic at the given index toward the front of the
     * list by one spot, sliding the other graphic back on in the list
     * in order.  If the location is already at the beginning or
     * beyond the end, nothing happens.
     *
     * @param location the index of the graphic to move. 
     */
    public void moveIndexedOneToFront(int location) {
	int listSize = graphics.size();
	if (location > 0 && location < listSize) {
	    OMGeometry tmpGraphic = _getAt(location);
	    _setAt(_getAt(location-1), location);
	    _setAt(tmpGraphic, location-1);
	}
    }

    /** 
     * Moves the graphic at the given index to the end of the list,
     * sliding the other graphics up on in the list in order.  If the
     * location is already at the end or less than zero, nothing
     * happens.
     *
     * @param location the index of the graphic to move.  
     * @see #moveIndexedToBottom(int)
     * @see #moveIndexedToTop(int)
     */
    public void moveIndexedToLast(int location) {
	int listSize = graphics.size();
	if (location < listSize - 1 && location >= 0) {
	    OMGeometry tmpGraphic = _getAt(location);
	    for (int i = location; i < listSize - 1; i++) {
		_setAt(_getAt(i+1), i);
	    }
	    _setAt(tmpGraphic, listSize - 1);
	}
    }

    /** 
     * Moves the graphic at the given index toward the back of the
     * list by one spot, sliding the other graphic up on in the list
     * in order.  If the location is already at the end or
     * less than zero, nothing happens.
     *
     * @param location the index of the graphic to move. 
     */
    public void moveIndexedOneToBack(int location) {
	int listSize = graphics.size();
	if (location < listSize - 1 && location >= 0) {
	    OMGeometry tmpGraphic = _getAt(location);
	    _setAt(_getAt(location+1), location);
	    _setAt(tmpGraphic, location+1);
	}
    }

    /** 
     * Set the stroke of all the graphics on the list.
     *
     * @param stroke the stroke object to use.
     */
    public void setStroke(java.awt.Stroke stroke) {
	super.setStroke(stroke);
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGraphic)it.next()).setStroke(stroke);
	    }
	}
    }
    
    /**
     * Set the fill paint for all the objects on the list.
     *
     * @param paint java.awt.Paint
     */
    public void setFillPaint(Paint paint) {
	super.setFillPaint(paint);
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGraphic)it.next()).setFillPaint(paint);
	    }
	}
    }

    /**
     * Set the texture mask for the OMGraphics on the list.  If not
     * null, then it will be rendered on top of the fill paint.  If
     * the fill paint is clear, the texture mask will not be used.  If
     * you just want to render the texture mask as is, set the fill
     * paint of the graphic instead.  This is really to be used to
     * have a texture added to the graphic, with the fill paint still
     * influencing appearance.
     */
    public void setTextureMask(TexturePaint texture) {
	super.setTextureMask(texture);
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGraphic)it.next()).setTextureMask(texture);
	    }
	}
    }

    /**
     * Set the line paint for all the objects on the list.
     *
     * @param paint java.awt.Paint
     */
    public void setLinePaint(Paint paint) {
	super.setLinePaint(paint);
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGraphic)it.next()).setLinePaint(paint);
	    }
	}
    }

    /**
     * Set the selection paint for all the objects on the list.
     *
     * @param paint java.awt.Paint
     */
    public void setSelectPaint(Paint paint) {
	super.setSelectPaint(paint);
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGraphic)it.next()).setSelectPaint(paint);
	    }
	}
    }

    /**
     * Projects any graphics needing projection.
     * Use this method to project any new or changed OMGeometrys before
     * painting.  to re-project the whole list, use
     * <code>generate(Projection, boolean)</code> with
     * <code>forceProjectAll</code> set to <code>true</code>.  This is
     * the same as calling <code> generate(p, false)</code>
     *
     * @param p a <code>Projection</code>
     * @see #generate(Projection, boolean)
     */
    public void project(Projection p) {
	generate(p, false);
    }

    /**
     * Projects the OMGeometrys on the list.
     * This is the same as calling <code>generate(p, forceProjectAll)</code>.
     *
     * @param p a <code>Projection</code>
     * @param forceProjectAll if true, all the graphics on the list
     * are generated with the new projection.  If false they are only
     * generated if getNeedToRegenerate() returns true
     * @see #generate(Projection, boolean)
     */
    public void project(Projection p, boolean forceProjectAll) {
	generate(p, forceProjectAll);
    }

    /**
     * Prepare the graphics for rendering.
     * This is the same as calling <code>project(p, true)</code>.
     *
     * @param p a <code>Projection</code>
     * @return boolean true
     * @see #generate(Projection, boolean)
     */
    public boolean generate(Projection p) {
	generate(p, true);
	return true;
    }

    /**
     * Prepare the graphics for rendering.
     * This must be done before calling <code>render()</code>!  This
     * recursively calls generate() on the OMGraphics on the list.
     *
     * @param p a <code>Projection</code>
     * @param forceProjectAll if true, all the graphics on the list
     * are generated with the new projection.  If false they are only
     * generated if getNeedToRegenerate() returns true
     * @see OMGraphic#generate
     * @see OMGraphic#regenerate
     */
    public void generate(Projection p, boolean forceProjectAll) {
	synchronized (graphics) {
	    Iterator iterator = iterator();
	    // Check forceProjectAll outside the loop for slight
	    // performance improvement.
	    if (forceProjectAll) {
		while (iterator.hasNext()) {
		    ((OMGraphic) iterator.next()).generate(p);
		}
	    } else {
		while (iterator.hasNext()) {
		    ((OMGraphic) iterator.next()).regenerate(p);
		}
	    }
	}
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
    public void render(Graphics gr) {
	java.util.List targets = getTargets();
	OMGraphic graphic;
	ListIterator iterator;

	synchronized (graphics) {
	    if (traverseMode == FIRST_ADDED_ON_TOP) {
		iterator = targets.listIterator(targets.size());
		while (iterator.hasPrevious()) {
		    graphic = (OMGraphic) iterator.previous();
		    if (graphic.isVisible()) {
			graphic.render(gr);
		    }
		}

	    } else {
		iterator = targets.listIterator();

		while (iterator.hasNext()) {
		    graphic = (OMGraphic) iterator.next();
		    if (graphic.isVisible()) {
			graphic.render(gr);
		    }
		}
	    }
	}
    }

    /**
     * Renders all the objects in the list a graphics context, in
     * their 'selected' mode.  This is the same as
     * <code>paint()</code> for AWT components.  The graphics are
     * rendered in the order of traverseMode.  Any graphics where
     * <code>isVisible()</code> returns false are not rendered.  All
     * of the graphics on the list are returned to their deselected
     * state.
     *
     * @param gr the AWT Graphics context 
     */
    public void renderAllAsSelected(Graphics gr) {
	java.util.List targets = getTargets();
	OMGraphic graphic;
	ListIterator iterator;

	synchronized (graphics) {
	    if (traverseMode == FIRST_ADDED_ON_TOP) {
		iterator = targets.listIterator(targets.size());
		while (iterator.hasPrevious()) {
		    graphic = (OMGraphic) iterator.previous();
		    if (graphic.isVisible()) {
			graphic.select();
			graphic.render(gr);
			graphic.deselect();
		    }
		}

	    } else {
		iterator = targets.listIterator();

		while (iterator.hasNext()) {
		    graphic = (OMGraphic) iterator.next();
		    if (graphic.isVisible()) {
			graphic.select();
			graphic.render(gr);
			graphic.deselect();
		    }
		}
	    }
	}
    }

    /**
     * Finds the distance to the closest OMGeometry.
     *
     * @param x x coord
     * @param y y coord
     * @return float distance
     * @see #findClosest(int, int, float)
     */
    public float distance(int x, int y) {
	return _findClosest(x, y, Float.MAX_VALUE).d;
    }

    /**
     * RetVal for closest object/distance calculations.
     */
    protected static class OMDist {
	public OMGeometry omg = null;
	public float d = Float.POSITIVE_INFINITY;
	public int index = NONE;  // unknown
    }

    /**
     * Find the closest Object and its distance.
     * The search is always conducted from the topmost graphic to the
     * bottommost, depending on the traverseMode.
     *
     * @param x x coord
     * @param y y coord
     * @param limit the max distance that a graphic has to be within
     * to be returned, in pixels.
     * @return OMDist
     */
    protected OMDist _findClosest(int x, int y, float limit) {
	OMDist omd = new OMDist();

	java.util.List targets = getTargets();
	ListIterator iterator;

        OMGeometry graphic, ret = null;
        float closestDistance = Float.MAX_VALUE;
	float currentDistance;


	int index = NONE;
	int i;

	if (size() != 0) {
	    synchronized (graphics) {
		if (traverseMode == FIRST_ADDED_ON_TOP) {
		    i = 0;
		    iterator = targets.listIterator();
		    while (iterator.hasNext()) {
			graphic = (OMGraphic) iterator.next();

			// cannot select a graphic which isn't visible
			if (!graphic.isVisible())
			    continue;
			currentDistance = graphic.distance(x, y);
			if (currentDistance < limit && 
			    currentDistance < closestDistance) {
			    ret = graphic;
			    closestDistance = currentDistance;
			    index = i;

			    if (currentDistance == 0) {
				break;
			    }
			}
			i++;
		    }
		} else {
		    i = targets.size();
		    iterator = targets.listIterator(i);
		    while (iterator.hasPrevious()) {
			graphic = (OMGraphic) iterator.previous();
		
			// cannot select a graphic which isn't visible
			if (!graphic.isVisible())
			    continue;
			currentDistance = graphic.distance(x, y);
			if (currentDistance < limit && 
			    currentDistance < closestDistance) {
			    ret = graphic;
			    closestDistance = currentDistance;
			    index = i;

			    if (currentDistance == 0) {
				break;
			    }
			}
			i--;
		    }
		}
	    }
	}
	omd.omg = ret;
	omd.d = closestDistance;
	omd.index = index;
	return omd;
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit.  The search is always
     * conducted from the topmost graphic to the bottommost, depending
     * on the traverseMode.  Any graphics where
     * <code>isVisible()</code> returns false are not considered.
     *
     * @param x the x coordinate on the component the graphics are
     * displayed on.
     * @param y the y coordinate on the component the graphics are
     * displayed on.
     * @param limit the max distance that a graphic has to be within
     * to be returned, in pixels.
     * @return OMGraphic the closest on the list within the limit, or
     * null if not found.
     */
    public OMGraphic findClosest(int x, int y, float limit) {
	return (OMGraphic) _findClosest(x, y, limit).omg;
    }

    /**
     * Finds the object located the closest to the point, regardless
     * of how far away it is.  This method returns null if the list is
     * not valid.  The search starts at the first-added graphic.<br>
     * This is the same as calling
     * <code>findClosest(x, y, Float.MAX_VALUE)</code>.
     *
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @return the closest graphic to the xy window point.
     * @see #findClosest(int, int, float)
     */
    public OMGraphic findClosest(int x, int y) {
        return findClosest(x, y, Float.MAX_VALUE);
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit.  The search is always
     * conducted from the topmost graphic to the bottommost, depending
     * on the traverseMode.  Any graphics where
     * <code>isVisible()</code> returns false are not considered.
     *
     * @param x the x coordinate on the component the graphics are
     * displayed on.
     * @param y the y coordinate on the component the graphics are
     * displayed on.
     * @param limit the max distance that a graphic has to be within
     * to be returned, in pixels.  
     * @return index of the closest on the list within the limit, or
     * OMGeometryList.NONE if not found.
     */
    public int findIndexOfClosest(int x, int y, float limit) {
	return _findClosest(x, y, limit).index;
    }

    /**
     * Finds the object located the closest to the point, regardless
     * of how far away it is.  This method returns null if the list is
     * not valid.  The search starts at the first-added graphic.<br>
     * This is the same as calling
     * <code>findClosest(x, y, Float.MAX_VALUE)</code>.
     *
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @return index of the closest graphic to the xy window point, or
     * OMGeometryList.NONE if not found.
     * @see #findIndexOfClosest(int, int, float)
     */
    public int findIndexOfClosest(int x, int y) {
        return _findClosest(x, y, Float.MAX_VALUE).index;
    }

    /**
     * Finds the object located the closest to the coordinates,
     * regardless of how far away it is.  Sets the select paint of 
     * that object, and resets the paint of all the other objects.
     * The search starts at the first-added graphic.
     *
     * @param x the x coordinate on the component the graphics are displayed on.
     * @param y the y coordinate on the component the graphics are displayed on.
     * @return the closest OMGraphic on the list.
     */
    public OMGraphic selectClosest(int x, int y) {
        return (OMGraphic)_selectClosest(x, y, Float.MAX_VALUE);
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit, and sets the paint of
     * that graphic to its select paint.  It sets the paints to all
     * the other objects to the regular paint. The search starts at
     * the first-added graphic.  Any graphics where
     * <code>isVisible()</code> returns false are not considered.
     *
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @param limit the max distance that a graphic has to be within
     * to be returned, in pixels.
     * @return the closest OMGraphic on the list, within the limit or null if
     * none found.  
     */
    public OMGraphic selectClosest(int x, int y, float limit) {
	return (OMGraphic)_selectClosest(x, y, limit);
    }

    /**
     * Finds the object located the closest to the point, if the
     * object distance away is within the limit, and sets the paint of
     * that graphic to its select paint.  It sets the paints to all
     * the other objects to the regular paint. The search starts at
     * the first-added graphic.  Any graphics where
     * <code>isVisible()</code> returns false are not considered.
     *
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @param limit the max distance that a graphic has to be within
     * to be returned, in pixels.
     * @return the closest OMGeometry on the list, within the limit or null if
     * none found.  
     */
    protected OMGeometry _selectClosest(int x, int y, float limit) {

	java.util.List targets = getTargets();
	ListIterator iterator;
        OMGeometry ret = null;
	OMGeometry graphic, current;
        float closestDistance = Float.MAX_VALUE;
	float currentDistance;

	if (size() != 0) {
	    synchronized (graphics) {
		if (traverseMode == FIRST_ADDED_ON_TOP) {
		    iterator = targets.listIterator();
		    while (iterator.hasNext()) {
			graphic = (OMGraphic) iterator.next();

			if (!graphic.isVisible()) continue;

			current = graphic;
			currentDistance = current.distance(x, y);
			if (currentDistance < limit && 
			    currentDistance < closestDistance) {
			    if (ret != null) ret.deselect();
			    ret = current;
			    ret.select();
			    closestDistance = currentDistance;
			} else {
			    // in case this graphic was the last closest
			    current.deselect();
			}
		    }
		} else {
		    iterator = targets.listIterator(targets.size());
		    while (iterator.hasPrevious()) {
			graphic = (OMGraphic) iterator.previous();

			if (!graphic.isVisible()) continue;

			current = graphic;
			currentDistance = current.distance(x, y);
			if (currentDistance < limit && 
			    currentDistance < closestDistance) {
			    if (ret != null) ret.deselect();
			    ret = current;
			    ret.select();
			    closestDistance = currentDistance;
			} else {
			    // in case this graphic was the last closest
			    current.deselect();
			}
		    }
		}
	    }
	}
	return ret;
    }

    /**
     * If you call select() on an OMGraphicList, it selects all the
     * graphics it contains.  This is really an OMGraphic method, but
     * it makes OMGraphicLists embedded in other OMGraphicLists act
     * correctly.
     */
    public void select() {
	selectAll();
    }

    /**
     * Finds the first OMGraphic (the one on top) that is under this pixel.
     * 
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @return the graphic that contains the pixel, NONE (null) if none are found.
     */
    public OMGraphic getOMGraphicThatContains(int x, int y) {
	return (OMGraphic)_getContains(x, y);
    }

    /**
     * Finds the first OMGeometry (the one on top) that is under this pixel.
     * 
     * @param x the horizontal pixel position of the window, from the
     * left of the window.
     * @param y the vertical pixel position of the window, from the
     * top of the window.
     * @return the graphic that contains the pixel, NONE (null) if none are found.
     */
    protected OMGeometry _getContains(int x, int y) {

	java.util.List targets = getTargets();
	ListIterator iterator;
        OMGeometry graphic, ret = null;

	if (size() != 0) {
	    synchronized (graphics) {
		if (traverseMode == FIRST_ADDED_ON_TOP) {
		    iterator = targets.listIterator();
		    while (iterator.hasNext()) {
			graphic = (OMGraphic) iterator.next();

			// cannot select a graphic which isn't visible
			if (!graphic.isVisible())
			    continue;
			if (graphic.contains(x, y)) {
			    ret = graphic;
			    break;
			}
		    }
		} else {
		    iterator = targets.listIterator(targets.size());
		    while (iterator.hasPrevious()) {
			graphic = (OMGraphic) iterator.previous();

			// cannot select a graphic which isn't visible
			if (!graphic.isVisible())
			    continue;
			if (graphic.contains(x, y)) {
			    ret = graphic;
			    break;
			}
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * If you call deselect() on an OMGraphicList, it deselects all
     * the graphics it contains.  This is really an OMGraphic method, but
     * it makes OMGraphicLists embedded in other OMGraphicLists act
     * correctly.
     */
    public void deselect() {
	deselectAll();
    }

    /**
     * Deselects all the items on the graphic list.
     */
    public void deselectAll() {
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGeometry)it.next()).deselect();
	    }
	}
    }

    /**
     * Selects all the items on the graphic list.
     */
    public void selectAll() {
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGeometry)it.next()).select();
	    }
	}
    }

    /**
     * Perform an action on the provided graphic.  If the graphic is
     * not currently on the list, it is added (if the action doesn't
     * say to delete it).  If the graphic is null, the list checks for
     * an action to take on the list (deselectAll).  
     */
    public void doAction(OMGraphic graphic, OMAction action) {
	_doAction((OMGeometry)graphic, action);
    }

    /**
     * Perform an action on the provided geometry.  If the geometry is
     * not currently on the list, it is added (if the action doesn't
     * say to delete it).  If the geometry is null, the list checks for
     * an action to take on the list (deselectAll).  
     */
    protected void _doAction(OMGeometry graphic, OMAction action) {

	Debug.message("omgl", "OMGraphicList.doAction()");

	if (graphic == null) {
	    return;
	}

	int i = _indexOf(graphic);

	boolean alreadyOnList = (i != -1);

	if (action == null || action.getValue() == 0 && !alreadyOnList) {
	    Debug.message("omgl", "OMGraphicList.doAction: adding graphic with null action");
	    _add(graphic);
	    return;
	}

	if (action.isMask(ADD_GRAPHIC_MASK) ||
	    action.isMask(UPDATE_GRAPHIC_MASK) && !alreadyOnList) {
	    Debug.message("omgl", "OMGraphicList.doAction: adding graphic");
	    _add(graphic);
	}

	if (action.isMask(DELETE_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: removing graphic");
	    _remove(graphic);
	}
	
	if (action.isMask(RAISE_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: raising graphic");
	    moveIndexedOneToTop(i);
	}

	if (action.isMask(RAISE_TO_TOP_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: raising graphic to top");
	    moveIndexedToTop(i);
	}

	if (action.isMask(LOWER_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: lowering graphic");
	    moveIndexedOneToBottom(i);
	}

	if (action.isMask(LOWER_TO_BOTTOM_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: lowering graphic to bottom");
	    moveIndexedOneToBottom(i);
	}

	if (action.isMask(DESELECTALL_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: deselecting all graphics.");
	    deselectAll();
	}

	if (action.isMask(SELECT_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: selecting graphic");
	    graphic.select();
	}

	if (action.isMask(DESELECT_GRAPHIC_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: deselecting graphic");
	    graphic.deselect();
	}

	if (action.isMask(SORT_GRAPHICS_MASK)) {
	    Debug.message("omgl", "OMGraphicList.doAction: sorting the list");
	    sort();
	}
    }

    /**
     * Set the visibility variable.
     * NOTE:<br>
     * This is checked by the OMGeometryList when it iterates through its list
     * for render and gesturing.  It is not checked by the internal OMGeometry
     * methods, although maybe it should be...
     *
     * @param visible boolean
     */
    public void setVisible(boolean visible) {
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		((OMGeometry) it.next()).setVisible(visible);
	    }
	}
    }

    /**
     * Get the visibility variable.  For the OMGeometryList, if any
     * part of it is visible, then it is considered visible.
     *
     * @return boolean 
     */
    public boolean isVisible() {
	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		if (((OMGeometry) it.next()).isVisible()) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Set whether the list will allow duplicate entries added.  If
     * not, then the copy will be added, and the previous version
     * removed.  
     */
    public void setAllowDuplicates(boolean set) {
	allowDuplicates = set;
    }

    /**
     * Get whether the list will allow duplicate entries added.  If
     * not, then the copy will be added, and the previous version
     * removed.  
     */
    public boolean getAllowDuplicates() {
	return allowDuplicates;
    }

    /**
     * Convenience function for methods that may add a OMGeometry.
     * Method checks to see if duplicates are allowed, and if they are
     * not, it will remove the OMGeometry from the list.  The calling
     * method can be confident that it will be adding a unqiue
     * OMGeometry.  Internal methods that call this method should be
     * synchronized on the graphics list.
     */
    protected void checkForDuplicate(OMGeometry g) {
	if (!allowDuplicates) {
	    int i = graphics.indexOf(g);
	    if (i != -1) {
		graphics.remove(i);
	    }
	}
    }

    /** 
     * Goes through the list, finds the OMGrid objects, and sets the
     * generator for all of them.  If a projection is passed in, the
     * generator will be used to create a displayable graphic within
     * the grid.
     *
     * @param generator an OMGridGenerator to create a renderable
     * graphic from the OMGrid.
     * @param proj a projection to use to generate the graphic.  If
     * null, the generator will create a renderable graphic the next
     * time a projection is handed to the list.
     */
    public void setGridGenerator(OMGridGenerator generator, Projection proj) {
	synchronized (graphics) {
	    Iterator it = iterator();
	    OMGraphic graphic;
	    while (it.hasNext()) {
		graphic = (OMGraphic)it.next();
		if (graphic instanceof OMGrid) {
		    ((OMGrid)graphic).setGenerator(generator);
		    if (proj != null) {
			graphic.generate(proj);
		    }
		}
	    }
	}
    }

    /**
     * Get a reference to the graphics vector.
     * This method is meant for use by methods that need to iterate
     * over the graphics vector, or make at least two invocations on
     * the graphics vector.
     * <p>
     * HACK this method should either return a clone of the graphics
     * list or a quick reference.  Currently it returns the latter for
     * simplicity and minor speed improvement.  We should allow a way
     * for the user to set the desired behavior, depending on whether
     * they want responsibility for list synchronization.  Right now,
     * the user is responsible for synchronizing the OMGeometryList if
     * it's being used in two or more threads...
     *
     * @return a reference of the graphics List.
     */
    public java.util.List getTargets() {
	if (graphics == null) {
	    // make sure that the graphics vector is not null, since
	    // all of the internal methods rely on it.
	    graphics = new ArrayList();
	}

//	synchronized (this) {
//	    return (java.util.List) graphics.clone();
//	}
	return graphics;
    }

    /**
     * Set the List used to hold the OMGraphics.  The OMGraphicList
     * assumes that this list contains OMGraphics.  Make *SURE* this
     * is the case.  The OMGraphicList will behave badly if there are
     * non-OMGraphics on the list.
     */
    public void setTargets(java.util.List list) {
	graphics = list;
    }

    /**
     * Get an Iterator containing the OMGeometrys.
     */
    public Iterator iterator() {
	return graphics.iterator();
    }

    /** 
     * Read a cache of OMGeometrys, given an URL.
     *
     * @param cacheURL URL of serialized graphic list.
     */
    public void readGraphics(URL cacheURL)
	throws java.io.IOException {
	
	try {
	    ObjectInputStream objstream = 
		new ObjectInputStream(cacheURL.openStream());
	    
	    if (Debug.debugging("omgraphics")) {
		Debug.output("OMGeomtryList: Opened " + cacheURL.toString() );
	    }

	    readGraphics(objstream);
	    objstream.close();

	    if (Debug.debugging("omgraphics")) {
		Debug.output("OMGeometryList: closed " + cacheURL.toString() );
	    }
	    
	} catch (ArrayIndexOutOfBoundsException aioobe) {
 	    new com.bbn.openmap.util.HandleError(aioobe);
 	} catch (ClassCastException cce) {
 	    cce.printStackTrace();
 	}
    }

    /** 
     * Read a cache of OMGraphics, given a ObjectInputStream.
     *
     * @param objstream ObjectInputStream of graphic list.
     */
    public void readGraphics(ObjectInputStream objstream) throws IOException {
	
	Debug.message("omgraphics", "OMGraphicList: Reading cached graphics");
	
	try { 
	    while (true) {
		try {
		    OMGraphic omg = (OMGraphic)objstream.readObject();
		    this.add(omg);
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		} catch (OptionalDataException ode) {
		    ode.printStackTrace();
		}
	    }
	} catch (EOFException e) {}
    }
    
    /**
     * Write the graphics out to a file
     * @param graphicsSaveFile
     */
    public void writeGraphics(String graphicsSaveFile) 
	throws IOException {
	
	FileOutputStream ostream = new FileOutputStream(graphicsSaveFile);
	ObjectOutputStream objectstream = new ObjectOutputStream(ostream);
	writeGraphics(objectstream);
	objectstream.close();
    }

    /**
     * Write the graphics out to a ObjectOutputStream
     * @param objectstream ObjectOutputStream
     */
    public void writeGraphics(ObjectOutputStream objectstream) 
	throws IOException {

	synchronized (graphics) {
	    Iterator it = iterator();
	    while (it.hasNext()) {
		OMGeometry g = (OMGeometry)it.next();
		try {
		    objectstream.writeObject(g);
		} catch ( IOException e ) {
		    Debug.error("OMGeometryList: Couldn't write object " + g +
				"\nOMGeometryList: Reason: " + e.toString());
		}
	    }
	}
	objectstream.close();
    }

    /**
     * This sort method is a placeholder for OMGraphicList extensions
     * to implement their own particular criteria for sorting an
     * OMGraphicList.  Does nothing for a generic OMGraphicList.
     */
    public void sort() {}
}
