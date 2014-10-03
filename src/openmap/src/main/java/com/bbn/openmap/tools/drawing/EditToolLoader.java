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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/EditToolLoader.java,v $
// $RCSfile: EditToolLoader.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import javax.swing.ImageIcon;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * An EditToolLoader is interface that describes an object that
 * creates something that can create or edit an OMGraphic. The
 * EditToolLoader should be able to be tossed at a DrawingTool, and
 * the DrawingTool should be able find out what kind of objects it can
 * adjust or create, and then use it if any requests come in that fit.
 */
public interface EditToolLoader {

    /**
     * Get the classnames that the loader is able to create
     * EditableOMGraphics for.
     */
    public String[] getEditableClasses();

    /**
     * Give the classname of a graphic to create, returning a default
     * EditableOMGraphic for that graphic. If you don't want a graphic
     * type to be part of the GUI, then just return an
     * EditableOMGraphic for that classname here, and don't return it
     * for other queries (getEditableClasses, etc.).
     */
    public EditableOMGraphic getEditableGraphic(String classname);

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic. The GraphicAttributes
     * object lets you set various other parameters for the graphic.
     * If you don't want a graphic type to be part of the GUI, then
     * just return an EditableOMGraphic for that classname here, and
     * don't return it for other queries (getEditableClasses, etc.).
     */
    public EditableOMGraphic getEditableGraphic(String classname,
                                                GraphicAttributes ga);

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic);

    /**
     * Get an Icon that is suitable for representing the class given
     * by the classname. OpenMap is going with a 20 x 20 icon.
     * 
     * @param classname the classname to get the icon for.
     * @return Image classname icon.
     */
    public ImageIcon getIcon(String classname);

    /**
     * Provide a pretty name to use for a particular class. Intended
     * to be part of a GUI, either on a list, or as a tool tip.
     * 
     * @param classname the classname to get the icon for.
     */
    public String getPrettyName(String classname);
}