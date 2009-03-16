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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/GraphicList.java,v $
// $RCSfile: GraphicList.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.Comp;
import com.bbn.openmap.corba.CSpecialist.UGraphic;

/**
 * This class implements a basic GraphicList class that [OpenMap]
 * Specialists can utilize. It maintains a list of SGraphics and
 * UGraphics, and can return an array of UGraphics that corresponds to
 * its contained list
 */
public class GraphicList implements com.bbn.openmap.util.GraphicList {
    /** our list of graphics */
    protected Vector graphics = new Vector();

    /**
     * Create a new empty GraphicList
     */
    public GraphicList() {}

    /**
     * Add a new SGraphic to the GraphicList
     * 
     * @param g the SGraphic to add
     */
    public void addSGraphic(SGraphic g) {
        graphics.addElement(g);
    }

    /**
     * Add a new UGraphic to the GraphicList
     * 
     * @param ug the UGraphic to add
     */
    public void addUGraphic(UGraphic ug) {
        graphics.addElement(ug);
    }

    /**
     * Add a new UGraphic to the GraphicList
     * 
     * @param sg add sg.ufill() to the graphic list
     * @see SGraphic#ufill()
     */
    public void addUGraphic(SGraphic sg) {
        graphics.addElement(sg.ufill());
    }

    /**
     * Remove all elements from the graphic list
     */
    public void clear() {
        graphics.removeAllElements();
    }

    /**
     * pack() the graphics into a UGraphic[]
     * 
     * @return the packed graphic list
     */
    public UGraphic[] packGraphics() {
        UGraphic retval[] = new UGraphic[graphics.size()];
        for (int i = 0; i < graphics.size(); i++) {
            Object o = graphics.elementAt(i);
            if (o instanceof SGraphic) {
                retval[i] = ((SGraphic) o).ufill();
            } else {
                retval[i] = (UGraphic) o;
            }
        }
        return retval;
    }

    /**
     * pack() the graphics into a UGraphic[]
     * 
     * @return the packed graphic list
     */
    public Comp[] getComps() {
        int len = graphics.size();
        Comp retval[] = new Comp[len];
        for (int i = 0; i < len; i++) {
            Object o = graphics.elementAt(i);
            if (o instanceof SGraphic) {
                retval[i] = ((SGraphic) o).object();
            }
        }
        return retval;
    }
}