package com.bbn.openmap.layer.test;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.DataBounds;

/**
 * A little layer to test out the DataBounds intersections. You draw OMRects
 * using the drawing tool, and any rectangles that overlap fill up with red.
 * 
 * @author dietrick
 */
public class BoundsTestLayer extends OMGraphicHandlerLayer implements DrawingToolRequestor {

    OMGraphicList holder = new OMGraphicList();
    OMDrawingTool drawingTool = null;
    String BOUNDS = "bounds";
    String HIT = "hit";

    public BoundsTestLayer() {
        setMouseModeIDsForEvents(new String[] { "Gestures" });
    }

    public OMGraphicList prepare() {

        OMGraphicList list = new OMGraphicList();
        list.addAll(holder);

        for (OMGraphic omg : holder) {
            omg.removeAttribute(HIT);
            omg.setFillPaint(OMColor.clear);
        }

        for (OMGraphic omg : holder) {
            if (omg instanceof OMRect) {
                DataBounds bnds = (DataBounds) omg.getAttribute(BOUNDS);

                for (OMGraphic omg2 : list) {
                    if (omg2 instanceof OMRect && !omg.equals(omg2)) {
                        DataBounds bnds2 = (DataBounds) omg2.getAttribute(BOUNDS);

                        if (bnds.intersects(bnds2)) {
                            omg.putAttribute(HIT, true);
                            omg2.putAttribute(HIT, true);
                        }
                    }
                }
            }
        }

        for (OMGraphic omg : holder) {
            if (omg.getAttribute(HIT) != null) {
                omg.setFillPaint(OMColor.red);
            }
        }

        list.generate(getProjection());
        return list;
    }

    public void findAndInit(Object obj) {
        super.findAndInit(obj);

        if (obj instanceof OMDrawingTool) {
            drawingTool = (OMDrawingTool) obj;
        }
    }

    public boolean isSelectable(OMGraphic omg) {
        return (drawingTool != null && drawingTool.canEdit(omg.getClass()));
    }

    /**
     * Called if isSelectable(OMGraphic) was true, so the list has the
     * OMGraphic. A list is used in case underlying code is written to handle
     * more than one OMGraphic being selected at a time.
     */
    public void select(OMGraphicList list) {
        if (list != null && !list.isEmpty()) {
            OMGraphic omg = list.getOMGraphicAt(0);

            if (drawingTool != null && drawingTool.canEdit(omg.getClass())) {
                drawingTool.setBehaviorMask(OMDrawingTool.QUICK_CHANGE_BEHAVIOR_MASK);
                if (drawingTool.edit(omg, this) == null) {
                    // Shouldn't see this because we checked, but ...
                    fireRequestInfoLine("Can't figure out how to modify this object.");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.bbn.openmap.tools.drawing.DrawingToolRequestor#drawingComplete(com
     * .bbn.openmap.omGraphics.OMGraphic, com.bbn.openmap.omGraphics.OMAction)
     */
    public void drawingComplete(OMGraphic omg, OMAction action) {
        if (omg instanceof OMRect) {
            OMRect rect = (OMRect) omg;
            DataBounds bounds = new DataBounds(rect.getWestLon(), rect.getNorthLat(), rect.getEastLon(), rect.getSouthLat());
            rect.putAttribute(BOUNDS, bounds);
        }
        holder.doAction(omg, action);
        doPrepare();
    }
}
