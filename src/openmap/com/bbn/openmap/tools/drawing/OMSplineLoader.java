package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.AbstractToolLoader;
import com.bbn.openmap.tools.drawing.EditClassWrapper;
import com.bbn.openmap.tools.drawing.EditToolLoader;
import com.bbn.openmap.omGraphics.EditableOMSpline;
import com.bbn.openmap.omGraphics.OMSpline;
import com.bbn.openmap.omGraphics.labeled.EditableLabeledOMSpline;

/**
 * OMSplineLoader
 * 
 * @author Eric LEPICIER
 * @version 22 juil. 2002
 */
public class OMSplineLoader extends AbstractToolLoader
    implements EditToolLoader {

    protected String graphicClassName =
	"com.bbn.openmap.omGraphics.OMSpline";
    protected String labeledClassName =
	"com.bbn.openmap.omGraphics.labeled.LabeledOMSpline";

    public OMSplineLoader() {
	init();
    }

    public void init() {
	EditClassWrapper ecw =
	    new EditClassWrapper(
		graphicClassName,
		"com.bbn.openmap.omGraphics.EditableOMSpline",
		"editablespline.gif",
		"Splines");
	addEditClassWrapper(ecw);

	// A class wrapper isn't added here for the LabeledOMSpline
	// because currently they are only created programmatically.
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.  The GraphicAttributes
     * object lets you set some of the initial parameters of the spline,
     * like spline type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(
	String classname,
	GraphicAttributes ga) {
	if (classname.intern() == graphicClassName) {
	    return new EditableOMSpline(ga);
	}
	if (classname.intern() == labeledClassName) {
	    return new EditableLabeledOMSpline(ga);
	}
	return null;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
	if (graphic instanceof OMSpline) {
	    return new EditableOMSpline((OMSpline) graphic);
	}
	return null;
    }
}
