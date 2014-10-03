
package com.bbn.openmap.layer.learn;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.DataBounds;

/**
 * This layer demonstrates how to use the projection to update the OMGraphics a
 * Layer is managing when the map view changes.
 * 
 * If you aren't sure how Layers manage OMGraphics, check out the BasicLayer
 * first.
 */
public class ProjectionResponseLayer
        extends OMGraphicHandlerLayer {

    /**
     * The empty constructor is necessary for any layer being created using the
     * openmap.properties file, via the openmap.layers property. This method
     * needs to be public, too. Don't try to do too much in the constructor -
     * remember, this code gets executed whether the user uses the layer or not.
     * Performance-wise, it's better to do most initialization the first time
     * the layer is made part of the map. You can test for that in the prepare()
     * method, by testing whether the OMGraphicList for the layer is null or
     * not.
     * 
     * @see #prepare
     */
    public ProjectionResponseLayer() {
        // Sets the name of the layer that is visible in the GUI. Can also be
        // set with properties with the 'prettyName' property.
        setName("Projection Response Layer");

        // This is how to set the ProjectionChangePolicy, which
        // dictates how the layer behaves when a new projection is
        // received. The ListResetPCPolicy is a policy that clears out the old
        // OMGraphicList when the projection changes so that old data doesn't
        // get redrawn on the map before this layer has the chance to update the
        // list it renders.

        // Remember, you don't have control over when paint() gets called on
        // this layer. All layers work independently, so if other layers finish
        // handling the projection change before this one, they will request to
        // be painted. This will cause paint() to be called on all
        // layers that are on/visible/part of the map. If the layer is still in
        // the prepare() method when those paint() calls are made, you want to
        // make sure your layer doesn't render anything if your layer's list is
        // from the old projection.
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
        // Improves performance
        setRenderPolicy(new BufferedImageRenderPolicy());

    }

    /**
     * This is an important Layer method to override. The prepare method gets
     * called when the layer is added to the map, or when the map projection
     * changes. We need to make sure the OMGraphicList returned from this method
     * is what we want painted on the map. The OMGraphics need to be generated
     * with the current projection.
     */
    public synchronized OMGraphicList prepare() {

        // We're going to create a new OMGraphicList to return from this
        // projection change. The ListRestPCPolicy has already taken care of
        // removing the OMGraphics from the current list (if it exists) so we
        // can just ignore that for now.

        OMGraphicList list = new OMGraphicList();

        Projection proj = getProjection();

        // Just a safety check in case someone calls prepare without ever adding
        // it to the map.
        if (proj == null) {
            return list;
        }

        Point2D upperLeft = proj.getUpperLeft();
        Point2D lowerRight = proj.getLowerRight();

        /**
         * Now, upperLeft and lowerRight are the coordinate bounds covered by
         * the map. You can use those bounds to filter data.
         * 
         * One other thing. OpenMap projections can only cover a maximum of 360
         * degrees. So to test for the date line, you can check the longitudes
         * of the corners. If the x value of the left side is greater than the x
         * value of the right side, the map is covering the date line.
         */

        if (upperLeft.getX() > lowerRight.getX()) {
            // crossing the date line. Depending on your data source, you may
            // need to make two queries for your data, one for the left side of
            // date line, one for the right side. If you want to see what
            // happens without this check, go ahead and comment these two lines
            // out.

            // Make query for map on left side of date line
            getPoints(new DataBounds(upperLeft, new Point2D.Double(180, lowerRight.getY())), list, proj);
            // Make query for map on right side if date line
            getPoints(new DataBounds(new Point2D.Double(-180, upperLeft.getY()), lowerRight), list, proj);

        } else {
            getPoints(new DataBounds(upperLeft, lowerRight), list, proj);
        }

        // Let's add a little statement in the lower left corner of the map
        // describing the number of OMPoints created and drawn.
        OMText statement =
                new OMText(10, proj.getHeight() - 10, getName() + " displaying " + list.size() + "/" + dataSource.size()
                        + " points", OMText.JUSTIFY_LEFT);
        // Just to add a little background behind the letters.
        statement.setFillPaint(Color.gray);
        // Have to generate the OMText, too.
        statement.generate(proj);
        // Add it to the front of the list, so it's on top.
        list.add(0, statement);

        return list;
    }

    /** Some make-believe source of data. */
    protected List<Point2D> dataSource;

    /**
     * Using this method to look at data source and create OMGraphics based on
     * DataBounds, and render them differently depending on where they are.
     * 
     * @param dataBounds the bounds of the map projection.
     * @param retList the list to add new OMGraphics to.
     * @param proj the projection to use for generating OMGraphics. If we pass
     *        the projection to the point where the OMGraphics are created and
     *        generate them at creation time, we save ourselves another loop
     *        through the OMGraphicList to generate them later.
     */
    protected void getPoints(DataBounds dataBounds, OMGraphicList retList, Projection proj) {

        if (dataSource == null) {
            // For lack of having a data source, lets just create one on the
            // fly.
            dataSource = initSource();
        }

        for (Point2D point : dataSource) {
            if (dataBounds.contains(point)) {
                OMPoint newPoint = new OMPoint(point.getY(), point.getX());
                // Color western points green, eastern points yellow
                newPoint.setFillPaint(point.getX() < 0 ? Color.green : Color.yellow);
                newPoint.generate(proj);
                retList.add(newPoint);
            }
        }
    }

    /**
     * Just creating a set of Points to use as data source.
     * 
     * @return List of Point2D objects in a grid.
     */
    protected List<Point2D> initSource() {
        List<Point2D> source = new ArrayList<Point2D>();

        for (double y = -85; y <= 85; y++) {
            for (double x = -180; x < 180; x++) {
                source.add(new Point2D.Double(x, y));
            }
        }

        logger.fine("created source with " + source.size() + " points");

        return source;
    }

}