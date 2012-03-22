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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/DrawingTool.java,v $
// $RCSfile: DrawingTool.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.awt.event.MouseEvent;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * A DrawingTool is an object that can take a request for editing an
 * OMGraphic, or for creating an OMGraphic from a classname, and find
 * a EditTool to do the job. The DrawingTool is responsible for
 * providing any user interface that is needed to adjust the
 * OMGraphic.
 */
public interface DrawingTool {

    /**
     * A integer that is looked at internally, bitwise, to determine
     * different behaviors. If you care about specific behavior of the
     * DrawingTool, you should set this to what you want to make sure
     * the tool acts the way you want.
     */
    public void setBehaviorMask(int mask);

    /**
     * A integer that is looked at internally, bitwise, to determine
     * different behaviors.
     */
    public int getBehaviorMask();

    /**
     * Given a classname, provide an OMGraphic for that classname.
     * It's assumed that the DrawingTool will be holding on to an
     * EditableOMGraphic encasing the returned OMGraphic, and that the
     * parameters of the OMGraphic may still change depending on user
     * input.
     * 
     * @param classname the classname of the OMGraphic to create.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic create(String classname, DrawingToolRequestor requestor);

    /**
     * Given a classname, provide an OMGraphic for that classname.
     * It's assumed that the DrawingTool will be holding on to an
     * EditableOMGraphic encasing the returned OMGraphic, and that the
     * parameters of the OMGraphic may still change depending on user
     * input.
     * 
     * @param classname the classname of the OMGraphic to create.
     * @param ga GraphicAttributes object that contains more
     *        information about the type of line to be created.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic create(String classname, GraphicAttributes ga,
                            DrawingToolRequestor requestor);

    /**
     * Same as create(String, GraphicAttributes,
     * DrawingToolRequestor), except that you have to option of
     * suppressing the GUI that could be available from the
     * EditableOMGraphic.
     * 
     * @param classname the classname of the OMGraphic to create.
     * @param ga GraphicAttributes object that contains more
     *        information about the type of line to be created.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute
     *        controls should be displayed.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic create(String classname, GraphicAttributes ga,
                            DrawingToolRequestor requestor, boolean showGUI);

    /**
     * Given an OMGraphic, set things up so that the OMGraphic will be
     * edited. Returns the OMGraphic being edited. Shouldn't assume
     * that the two objects are the same. It's assumed that the
     * DrawingTool will be holding on to an EditableOMGraphic encasing
     * the returned OMGraphic, and that the parameters of the
     * OMGraphic may still change depending on user input.
     * 
     * @param g the OMGraphic to wrap in an EditableOMGraphic, and
     *        therefore to edit.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor);

    /**
     * Same as edit(omGraphic, DrawingToolRequestor), except that you
     * have to option of suppressing the GUI that could be available
     * from the EditableOMGraphic.
     * 
     * @param g the OMGraphic to wrap in an EditableOMGraphic, and
     *        therefore to edit.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @param showGUI set to true (default) if a GUI showing attribute
     *        controls should be displayed.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor,
                          boolean showGUI);

    /**
     * Given an EditableOMGraphic, direct events to the
     * EditableOMGraphic so that it can modify its OMGraphic. With
     * this method, the loaders are not needed.
     * 
     * @param eomg and EditableOMGraphic to manipulate.
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @return OMGraphic if everything's OK, null if the request can't
     *         be fulfilled.
     */
    public OMGraphic edit(EditableOMGraphic eomg, DrawingToolRequestor requestor);

    /**
     * A slightly different edit method, where the EditableOMGraphic
     * is put directly into edit mode, and the mouse events
     * immediately start making modifications to the OMGraphic.
     * 
     * @param g OMGraphic to modify
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified.
     */
    public OMGraphic edit(OMGraphic g, DrawingToolRequestor requestor,
                          MouseEvent e);

    /**
     * A slightly different edit method, where the EditableOMGraphic
     * is put directly into edit mode, and the mouse events
     * immediately start making modifications to the OMGraphic.
     * 
     * @param eomg EditableOMGraphic to modify
     * @param requestor the Component that is requesting the
     *        OMGraphic. The requestor gets notified when the user is
     *        finished with the DrawingTool and the graphic is ready.
     * @param e MouseEvent to use to start editing with.
     * @return OMGraphic being modified contained within the
     *         EditableOMGraphic.
     */
    public OMGraphic edit(EditableOMGraphic eomg,
                          DrawingToolRequestor requestor, MouseEvent e);

    /**
     * Check to see if the class type can be created/edited by the
     * DrawingTool.
     */
    public boolean canEdit(Class clas);

    /**
     * Add an EditToolLoader to the DrawingTool, expanding the
     * DrawingTool's capability to handle more graphic types.
     */
    public void addLoader(EditToolLoader loader);

    /**
     * Remove an EditToolLoader from the DrawingTool.
     */
    public void removeLoader(EditToolLoader loader);

    /**
     * Get an array of EditToolLoaders that the DrawingTool knows
     * about.
     */
    public EditToolLoader[] getLoaders();

    /**
     * Set the loaders within the DrawingTool.
     */
    public void setLoaders(EditToolLoader[] loaders);

}