package com.bbn.openmap.tools.drawing;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.tools.drawing.AbstractToolLoader;
import com.bbn.openmap.tools.drawing.EditClassWrapper;
import com.bbn.openmap.tools.drawing.EditToolLoader;

import com.bbn.openmap.omGraphics.EditableOMDecoratedSpline;
import com.bbn.openmap.omGraphics.EditableOMSpline;
import com.bbn.openmap.omGraphics.OMDecoratedSpline;
import com.bbn.openmap.omGraphics.OMSpline;
import com.bbn.openmap.omGraphics.labeled.EditableLabeledOMSpline;

/**
 * OMDecoratedSplineLoader
 * 
 * @author Eric LEPICIER
 * @version 22 juil. 2002
 */
public class OMDecoratedSplineLoader
        extends AbstractToolLoader
        implements EditToolLoader {

        protected String graphicClassName =
                "com.bbn.openmap.omGraphics.OMDecoratedSpline";

        public OMDecoratedSplineLoader() {
                init();
        }

        public void init() {
                EditClassWrapper ecw =
                        new EditClassWrapper(
                                graphicClassName,
                                "com.bbn.openmap.omGraphics.EditableOMDecoratedSpline",
                                "editablespline.gif",
                                "Decorated Splines");
                addEditClassWrapper(ecw);
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
                        return new EditableOMDecoratedSpline(ga);
                }
                return null;
        }

        /**
         * Give an OMGraphic to the EditToolLoader, which will create an
         * EditableOMGraphic for it.
         */
        public EditableOMGraphic getEditableGraphic(OMGraphic graphic) {
                if (graphic instanceof OMDecoratedSpline) {
                        return new EditableOMDecoratedSpline((OMSpline) graphic);
                }
                return null;
        }
}
