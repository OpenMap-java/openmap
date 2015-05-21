// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: GeoCrossDemoLayer.java,v $
//$Revision: 1.3 $
//$Date: 2009/01/21 01:24:42 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.test;

import java.awt.BasicStroke;
import java.awt.Color;

import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.Intersection;
import com.bbn.openmap.layer.editor.EditorLayer;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.util.Debug;

/**
 * This layer was developed to provide a simple picture of how vector cross
 * products work with Geos to solve intersection problems. The best way to use
 * this class is to turn off all other layers and put the map in Orthographic
 * projection. When the layer is active, a series of buttons will appear in the
 * ToolPanel, allowing you to draw Great Circle lines. It's helpful to change
 * the color of each line before you create it, so you can see the relationship
 * of the line to the previous one drawn.
 * <P>
 * 
 * This layer will iterate through the list if drawn lines, looking for the
 * intersection point between consecutive lines. For each line, a point will be
 * drawn in that line's color representing the cross-normalized point for the
 * line's enpoints - that is, it's the point perpendicular to the great circle
 * plane created by the line. You can also think of it as the point that is 90
 * degrees away from every point on the line. The dotted lines are the path from
 * the previous line's crossNornmalized point and the current line's
 * crossNormalized point. Lastly, the cross-normalization of the end points of
 * the dashed line will result in the point where the original lines would
 * intersect if they are close enough. Depending on the direction that the lines
 * were drawn, that point may actually be the antipode (on the other side of the
 * sphere) of the point you are interested in, but this layer doesn't bother to
 * investigate that yet.
 * <P>
 * 
 * That's all this layer does. The properties for this layer are:
 * 
 * <pre>
 *          geocross.class=com.bbn.openmap.layer.test.GeoCrossDemoLayer
 *          geocross.prettyName=GEO Cross Demonstration
 *          geocross.editor=com.bbn.openmap.layer.editor.DrawingEditorTool
 *          geocross.showAttributes=true
 *          geocross.loaders=lines
 *          geocross.mouseModes=Gestures
 *          geocross.lines.class=com.bbn.openmap.tools.drawing.OMLineLoader
 * </pre>
 * 
 * @author dietrick
 */

public class GeoCrossDemoLayer extends EditorLayer {

    protected OMGraphicList lines = new OMGraphicList();

    /**
     * 
     */
    public GeoCrossDemoLayer() {
        super();
    }

    public synchronized OMGraphicList prepare() {
        OMGraphicList list = getList();

        if (list == null) {
            list = new OMGraphicList();
        } else {
            list.clear();
        }

        OMLine oldLine = null;
        Geo ogc = null;

        for (OMGraphic omg : lines) {

            OMLine line = (OMLine) omg;
            double[] ll = line.getLL();
            Geo g1 = new Geo(ll[0], ll[1]);
            Geo g2 = new Geo(ll[2], ll[3]);

            Geo gc = g1.crossNormalize(g2);

            OMPoint p = new OMPoint((float) gc.getLatitude(), (float) gc
                    .getLongitude(), 3);
            p.setLinePaint(line.getLinePaint());
            p.setFillPaint(line.getFillPaint());
            p.setStroke(line.getStroke());

            line.addArrowHead(true);

            list.add(line);
            list.add(p);

            if (oldLine != null && ogc != null) {

                double[] ll2 = oldLine.getLL();
                Geo g3 = new Geo(ll2[0], ll2[1]);
                Geo g4 = new Geo(ll2[2], ll2[3]);

                OMLine line2 = new OMLine((float) ogc.getLatitude(),
                        (float) ogc.getLongitude(), (float) gc.getLatitude(),
                        (float) gc.getLongitude(),
                        OMGraphic.LINETYPE_GREATCIRCLE);
                line2.setLinePaint(line.getLinePaint());
                line2
                        .setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_BEVEL, 0f, new float[] { 10,
                                        10 }, 0f));
                line2.addArrowHead(true);
                list.add(line2);

                Geo i = gc.crossNormalize(ogc);
                Color iColor = Color.white;
                if (!(Intersection.isOnSegment(g1, g2, i) || Intersection
                        .isOnSegment(g3, g4, i))) {
                    i = i.antipode();
                    iColor = Color.black;
                }

                p = new OMPoint((float) i.getLatitude(), (float) i
                        .getLongitude(), 3);
                p.setOval(true);
                p.setLinePaint(line.getLinePaint());
                p.setFillPaint(iColor);
                p.setStroke(line.getStroke());

                list.add(p);
            }

            oldLine = line;
            ogc = gc;
        }

        list.generate(getProjection());

        return list;

    }

    public void drawingComplete(OMGraphic omg, OMAction action) {

        releaseProxyMouseMode();

        if (omg instanceof OMLine && lines != null) {
            lines.doAction(omg, action);
            deselect(lines);
            doPrepare();
        } else {
            Debug.error("Layer " + getName() + " received " + omg + " and "
                    + action + " with no list ready");
        }

        // This is important!!
        if (editorTool != null) {
            editorTool.drawingComplete(omg, action);
        }
    }

}
