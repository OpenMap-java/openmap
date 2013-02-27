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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMScalingRaster.java,v $
// $RCSfile: EditableOMScalingRaster.java,v $
// $Revision: 1.13 $
// $Date: 2009/02/25 22:34:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JOptionPane;

import com.bbn.openmap.Environment;
import com.bbn.openmap.omGraphics.editable.GraphicEditState;
import com.bbn.openmap.omGraphics.editable.GraphicSelectedState;
import com.bbn.openmap.omGraphics.editable.GraphicSetOffsetState;
import com.bbn.openmap.omGraphics.editable.ScalingRasterStateMachine;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

/**
 * The EditableOMScalingRaster encompasses an OMScalingRaster, providing methods
 * for modifying or creating it. This class only modifies OMScaling Rasters in
 * lat/lon space (RENDERTYPE_LATLON). When you grab at the raster, you change
 * the size of the entire rect. Grabbing the center point moves the raster. If
 * there is an offset point, moving the center point changes the rect's position
 * in relation to the offset point. Moving the offset point moves the rect,
 * keeping the distance to the center point constant.
 */
public class EditableOMScalingRaster extends EditableOMGraphic {

    protected GrabPoint gpnw;
    protected GrabPoint gpne;
    protected GrabPoint gpsw;
    protected GrabPoint gpse;
    protected OffsetGrabPoint gpc;
    protected OffsetGrabPoint gpo; // offset

    protected OMScalingRaster raster;

    public final static String OffsetResetCmd = "OffsetResetCmd";
    public final static int CENTER_POINT_INDEX = 0;
    public final static int NW_POINT_INDEX = 1;
    public final static int NE_POINT_INDEX = 2;
    public final static int SW_POINT_INDEX = 3;
    public final static int SE_POINT_INDEX = 4;
    public final static int OFFSET_POINT_INDEX = 5;

    /**
     * Create the EditableOMRect, setting the state machine to create the rect
     * off of the gestures.
     */
    public EditableOMScalingRaster() {
        createGraphic(null);
    }

    /**
     * Create an EditableOMScalingRaster with the rectType and renderType
     * parameters in the GraphicAttributes object.
     */
    public EditableOMScalingRaster(GraphicAttributes ga) {
        createGraphic(ga);
    }

    /**
     * Create the EditableOMScalingRaster with an OMScalingRaster already
     * defined, ready for editing.
     * 
     * @param omsr OMScalingRaster that should be edited.
     */
    public EditableOMScalingRaster(OMScalingRaster omsr) {
        setGraphic(omsr);
    }

    /**
     * Create and initialize the state machine that interprets the modifying
     * gestures/commands, as well as initialize the grab points. Also allocates
     * the grab point array needed by the EditableOMScalingRaster.
     */
    public void init() {
        Debug.message("eomg", "EditableOMScalingRaster.init()");
        setCanGrabGraphic(false);
        setStateMachine(new ScalingRasterStateMachine(this));
        gPoints = new GrabPoint[6];
    }

    /**
     * Set the graphic within the state machine. If the graphic is null, then
     * one shall be created, and located off screen until the gestures driving
     * the state machine place it on the map.
     */
    public void setGraphic(OMGraphic graphic) {
        init();
        if (graphic instanceof OMScalingRaster) {
            raster = (OMScalingRaster) graphic;
            stateMachine.setSelected();
            setGrabPoints(raster);
        } else {
            createGraphic(null);
        }
    }

    /**
     * Create and set the graphic within the state machine. The
     * GraphicAttributes describe the type of rect to create.
     */
    public void createGraphic(GraphicAttributes ga) {
        init();
        stateMachine.setUndefined();

        String pathToFile = null;

        // / This would be an ideal place to bring up a chooser!
        if (!Environment.isApplet()) {
            pathToFile = com.bbn.openmap.util.FileUtils.getFilePathToOpenFromUser("Choose Image File for Raster");
        } else {
            JOptionPane.showMessageDialog(null, "Can't search for images in an applet!", "Can't Choose Image", JOptionPane.ERROR_MESSAGE);
        }

        if (pathToFile == null)
            return;

        try {
            javax.swing.ImageIcon ii = new javax.swing.ImageIcon(pathToFile);
            raster = new OMScalingRaster(90f, -180f, 89f, -179f, ii);

        } catch (IllegalArgumentException iae) {
            // Not an image file, punch
            Debug.error("EditableOMScalingRaster:  " + pathToFile
                    + " doesn't appear to be an image file");
            raster = new OMScalingRaster(90f, -180f, 89f, -179f, new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        }

        if (ga != null) {
            ga.setTo(raster, true);
        }

        assertGrabPoints();
    }

    /**
     * Get the OMGraphic being created/modified by the EditableOMScalingRaster.
     */
    public OMGraphic getGraphic() {
        return raster;
    }

    /**
     * Attach to the Moving OffsetGrabPoint so if it moves, it will move this
     * EditableOMGraphic with it. EditableOMGraphic version doesn't do anything,
     * each subclass has to decide which of its OffsetGrabPoints should be
     * attached to it.
     */
    public void attachToMovingGrabPoint(OffsetGrabPoint gp) {
        gp.addGrabPoint(gpo);
    }

    /**
     * Detach from a Moving OffsetGrabPoint. The EditableOMGraphic version
     * doesn't do anything, each subclass should remove whatever GrabPoint it
     * would have attached to an OffsetGrabPoint.
     */
    public void detachFromMovingGrabPoint(OffsetGrabPoint gp) {
        gp.removeGrabPoint(gpo);
    }

    /**
     * Set the GrabPoint that is in the middle of being modified, as a result of
     * a mouseDragged event, or other selection process.
     */
    // public void setMovingPoint(GrabPoint gp) {
    // super.setMovingPoint(gp);
    // }

    double diffx;
    double diffy;

    // Called from the state machine...
    public void initRectSize() {
        diffx = Math.abs(raster.getLRLon() - raster.getULLon()) / 2;
        diffy = Math.abs(raster.getULLat() - raster.getLRLat()) / 2;
        // Debug.output("initRectSize(): diffx:" + diffx + ", diffy:"
        // + diffy);
    }

    protected int lastRenderType = -1;

    /**
     * Check to make sure the grab points are not null. If they are, allocate
     * them, and them assign them to the array.
     */
    public void assertGrabPoints() {
        OMGraphic omg = getGraphic();
        if (omg == null)
            return;

        int rt = omg.getRenderType();
        if (rt != lastRenderType) {
            clearGrabPoints();
            lastRenderType = rt;
        }

        if (gpnw == null) {
            gpnw = new GrabPoint(-1, -1);
            gPoints[NW_POINT_INDEX] = gpnw;
            // gpnw.setFillPaint(Color.yellow);
        }
        if (gpne == null) {
            gpne = new GrabPoint(-1, -1);
            gPoints[NE_POINT_INDEX] = gpne;
            // gpne.setFillPaint(Color.blue);
        }
        if (gpsw == null) {
            gpsw = new GrabPoint(-1, -1);
            gPoints[SW_POINT_INDEX] = gpsw;
            // gpsw.setFillPaint(Color.green);
        }
        if (gpse == null) {
            gpse = new GrabPoint(-1, -1);
            gPoints[SE_POINT_INDEX] = gpse;
            // gpse.setFillPaint(Color.orange);
        }

        if (gpc == null) {
            gpc = new OffsetGrabPoint(-1, -1);
            // gpc.setFillPaint(Color.red);
            gPoints[CENTER_POINT_INDEX] = gpc;
            if (getGraphic().getRenderType() != OMGraphic.RENDERTYPE_LATLON) {
                gpc.addGrabPoint(gpnw);
                gpc.addGrabPoint(gpne);
                gpc.addGrabPoint(gpsw);
                gpc.addGrabPoint(gpse);
            }
        }

        if (gpo == null) {
            gpo = new OffsetGrabPoint(-1, -1);
            gPoints[OFFSET_POINT_INDEX] = gpo;
            gpo.addGrabPoint(gpc);
        }
    }

    protected void clearGrabPoints() {

        gpc = null;
        gpnw = null;
        gpne = null;
        gpsw = null;
        gpse = null;
        gpo = null;

        gPoints[CENTER_POINT_INDEX] = gpc;
        gPoints[NW_POINT_INDEX] = gpnw;
        gPoints[NE_POINT_INDEX] = gpne;
        gPoints[SW_POINT_INDEX] = gpsw;
        gPoints[SE_POINT_INDEX] = gpse;
        gPoints[OFFSET_POINT_INDEX] = gpo;
    }

    /**
     * Set the grab points for the graphic provided, setting them on the extents
     * of the graphic. Called when you want to set the grab points off the
     * location of the graphic.
     */
    public void setGrabPoints(OMGraphic graphic) {
        Debug.message("eomg", "EditableOMScalingRaster.setGrabPoints(graphic)");
        if (!(graphic instanceof OMScalingRaster)) {
            return;
        }

        assertGrabPoints();

        OMScalingRaster raster = (OMScalingRaster) graphic;

        if (graphic instanceof OMScalingIcon) {
            setGrabPointsForOMSI((OMScalingIcon) graphic);
            return;
        }

        boolean ntr = raster.getNeedToRegenerate();
        int renderType = raster.getRenderType();

        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        LatLonPoint llp;
        int latoffset = 0;
        int lonoffset = 0;

        boolean doStraight = true;

        if (ntr == false) {

            if (renderType == OMGraphic.RENDERTYPE_LATLON
                    || renderType == OMGraphic.RENDERTYPE_OFFSET) {

                if (projection != null) {
                    double wlon = raster.getULLon();
                    double nlat = raster.getULLat();
                    double elon = raster.getLRLon();
                    double slat = raster.getLRLat();

                    llp = new LatLonPoint.Double(nlat, wlon);
                    Point2D p = projection.forward(llp);
                    if (renderType == OMGraphic.RENDERTYPE_LATLON) {
                        doStraight = false;
                        top = (int) p.getY();
                        left = (int) p.getX();
                        gpnw.set((int) p.getX(), (int) p.getY());

                        p = projection.forward(slat, elon);
                        gpse.set((int) p.getX(), (int) p.getY());

                        p = projection.forward(nlat, elon);
                        gpne.set((int) p.getX(), (int) p.getY());

                        p = projection.forward(slat, wlon);
                        gpsw.set((int) p.getX(), (int) p.getY());

                        p = projection.forward(nlat - (nlat - slat) / 2f, wlon + (elon - wlon) / 2f);
                        gpc.set((int) p.getX(), (int) p.getY());

                    } else {
                        latoffset = (int) p.getY();
                        lonoffset = (int) p.getX();
                        gpo.set(lonoffset, latoffset);
                    }
                }
            }

            if (doStraight) {

                Debug.message("eomg", "EditableOMScalingRaster: drawing straight line rectangle");

                top = raster.getY() + latoffset;
                bottom = raster.getY() + raster.getHeight() + latoffset;
                right = raster.getX() + raster.getWidth() + lonoffset;
                left = raster.getX() + lonoffset;

                // We have to do some fancy point wrangling to keep
                // from messing up the next setGrabPoints().
                if (movingPoint == gpc || movingPoint == gpo || movingPoint == null) {
                    gpne.set(right, top);
                    gpnw.set(left, top);
                    gpse.set(right, bottom);
                    gpsw.set(left, bottom);
                } else if (movingPoint == gpnw) {
                    gpne.set(gpse.getX(), gpnw.getY());
                    gpsw.set(gpnw.getX(), gpse.getY());
                } else if (movingPoint == gpse) {
                    gpne.set(gpse.getX(), gpnw.getY());
                    gpsw.set(gpnw.getX(), gpse.getY());
                } else if (movingPoint == gpsw) {
                    gpnw.set(gpsw.getX(), gpne.getY());
                    gpse.set(gpne.getX(), gpsw.getY());
                } else if (movingPoint == gpne) {
                    gpnw.set(gpsw.getX(), gpne.getY());
                    gpse.set(gpne.getX(), gpsw.getY());
                }

                int middlex = (right - left) / 2;
                int middley = (bottom - top) / 2;
                gpc.set(left + middlex, top + middley);
                gpc.updateOffsets();
                // Debug.output("Center setting x: " + gpc.getX() + ",
                // y:" + gpc.getY());
            }

            if (renderType == OMGraphic.RENDERTYPE_OFFSET) {
                gpo.updateOffsets();
            }

        } else {
            Debug.message("eomg", "EditableOMScalingRaster.setGrabPoints: graphic needs to be regenerated");
        }
    }

    /**
     * @param icon
     */
    protected void setGrabPointsForOMSI(OMScalingIcon icon) {
        if (projection != null) {
            double lon = icon.getLon();
            double lat = icon.getLat();
            int renderType = icon.getRenderType();
            LatLonPoint llp = new LatLonPoint.Double(lat, lon);
            Point2D p = projection.forward(llp);
            if (renderType == OMGraphic.RENDERTYPE_LATLON) {
                gpc.set((int) p.getX(), (int) p.getY());
            }
        }
    }

    protected void setGrabPointsForOMSI() {

        if (projection != null) {
            // movingPoint == gpc
            LatLonPoint llp1 = projection.inverse(gpc.getX(), gpc.getY(), new LatLonPoint.Double());
            raster.setLat(llp1.getY());
            raster.setLon(llp1.getX());
            // point.setNeedToRegenerate set
        }

        if (projection != null) {
            regenerate(projection);
        }
    }

    /**
     * Take the current location of the GrabPoints, and modify the location
     * parameters of the OMScalingRaster with them. Called when you want the
     * graphic to change according to the grab points.
     */
    public void setGrabPoints() {

        int renderType = raster.getRenderType();
        LatLonPoint llp1;

        Debug.message("eomg", "EditableOMScalingRaster.setGrabPoints()");

        // Do center point for lat/lon or offset rects
        if (renderType == OMGraphic.RENDERTYPE_LATLON) {

            if (projection != null) {
                if (raster instanceof OMScalingIcon) {
                    setGrabPointsForOMSI();
                    return;
                }

                // Need to figure out which point was moved, and then
                // set the upper left and lower right points
                // accordingly.
                if (movingPoint == gpne) {
                    llp1 = projection.inverse(gpne.getX(), gpne.getY(), new LatLonPoint.Double());
                    raster.setULLat(llp1.getY());
                    raster.setLRLon(llp1.getX());
                } else if (movingPoint == gpnw) {
                    llp1 = projection.inverse(gpnw.getX(), gpnw.getY(), new LatLonPoint.Double());
                    raster.setULLat(llp1.getY());
                    raster.setULLon(llp1.getX());
                } else if (movingPoint == gpsw) {
                    llp1 = projection.inverse(gpsw.getX(), gpsw.getY(), new LatLonPoint.Double());
                    raster.setLRLat(llp1.getY());
                    raster.setULLon(llp1.getX());
                } else if (movingPoint == gpse) {
                    llp1 = projection.inverse(gpse.getX(), gpse.getY(), new LatLonPoint.Double());
                    LatLonPoint llp2 = projection.inverse(gpnw.getX(), gpnw.getY(), new LatLonPoint.Double());
                    raster.setULLat(llp2.getY());
                    raster.setULLon(llp2.getX());
                    raster.setLRLat(llp1.getY());
                    raster.setLRLon(llp1.getX());
                } else {
                    // movingPoint == gpc
                    llp1 = projection.inverse(gpc.getX(), gpc.getY(), new LatLonPoint.Double());
                    raster.setULLat(llp1.getY() + diffy);
                    raster.setULLon(llp1.getX() - diffx);
                    raster.setLRLat(llp1.getY() - diffy);
                    raster.setLRLon(llp1.getX() + diffx);
                }
                raster.setNeedToRegenerate(true);
            }
        }

        boolean settingOffset = getStateMachine().getState() instanceof GraphicSetOffsetState
                && movingPoint == gpo;

        // If the center point is moving, the offset distance changes
        if (renderType == OMGraphic.RENDERTYPE_OFFSET) {

            llp1 = projection.inverse(gpo.getX(), gpo.getY(), new LatLonPoint.Double());

            raster.setULLat(llp1.getY());
            raster.setULLon(llp1.getX());

            if (settingOffset || movingPoint == gpc) {
                int halfheight = (gpse.getY() - gpnw.getY()) / 2;
                int halfwidth = (gpse.getX() - gpnw.getX()) / 2;

                // Don't call rect.setLocation because we only want to
                // setNeedToRegenerate if !settingOffset.
                llp1 = projection.inverse(gpc.getX() - halfwidth - gpo.getX(), gpc.getY()
                        - halfheight - gpo.getY(), new LatLonPoint.Double());
                LatLonPoint llp2 = projection.inverse(gpc.getX() + halfwidth - gpo.getX(), gpc.getY()
                        + halfheight - gpo.getY(), new LatLonPoint.Double());

                raster.setULLat(llp1.getY());
                raster.setULLon(llp1.getX());

                raster.setLRLat(llp2.getY());
                raster.setLRLon(llp2.getX());
            }

            if (!settingOffset) {
                Debug.message("eomg", "EditableOMScalingRaster: updating offset rect");
                if (movingPoint == gpnw || movingPoint == gpse) {
                    llp1 = projection.inverse(gpnw.getX() - gpo.getX(), gpnw.getY() - gpo.getY(), new LatLonPoint.Double());
                    LatLonPoint llp2 = projection.inverse(gpse.getX() - gpo.getX(), gpse.getY()
                            - gpo.getY(), new LatLonPoint.Double());

                    raster.setULLat(llp1.getY());
                    raster.setULLon(llp1.getX());

                    raster.setLRLat(llp2.getY());
                    raster.setLRLon(llp2.getX());
                } else if (movingPoint == gpne || movingPoint == gpsw) {
                    llp1 = projection.inverse(gpsw.getX() - gpo.getX(), gpne.getY() - gpo.getY(), new LatLonPoint.Double());
                    LatLonPoint llp2 = projection.inverse(gpne.getX() - gpo.getX(), gpsw.getY()
                            - gpo.getY(), new LatLonPoint.Double());

                    raster.setULLat(llp1.getY());
                    raster.setULLon(llp1.getX());

                    raster.setLRLat(llp2.getY());
                    raster.setLRLon(llp2.getX());
                }
                raster.setNeedToRegenerate(true);
            }

            // Set Location has reset the rendertype, but provides
            // the convenience of setting the max and min values
            // for us.
            raster.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
        }

        // Do the rect height and width for XY and OFFSET render
        // types.
        if (renderType == OMGraphic.RENDERTYPE_XY) {
            Debug.message("eomg", "EditableOMScalingRaster: updating x/y rect");

            if (movingPoint == gpc) {
                int halfheight = (gpse.getY() - gpnw.getY()) / 2;
                int halfwidth = (gpse.getX() - gpnw.getX()) / 2;

                llp1 = projection.inverse(gpc.getX() - halfwidth, gpc.getY() - halfheight, new LatLonPoint.Double());
                LatLonPoint llp2 = projection.inverse(gpc.getX() + halfwidth, gpc.getY()
                        + halfheight, new LatLonPoint.Double());

                raster.setULLat(llp1.getY());
                raster.setULLon(llp1.getX());

                raster.setLRLat(llp2.getY());
                raster.setLRLon(llp2.getX());
            } else if (movingPoint == gpnw || movingPoint == gpse) {
                llp1 = projection.inverse(gpnw.getX(), gpnw.getY(), new LatLonPoint.Double());
                LatLonPoint llp2 = projection.inverse(gpse.getX(), gpse.getY(), new LatLonPoint.Double());

                raster.setULLat(llp1.getY());
                raster.setULLon(llp1.getX());

                raster.setLRLat(llp2.getY());
                raster.setLRLon(llp2.getX());
            } else if (movingPoint == gpne || movingPoint == gpsw) {
                llp1 = projection.inverse(gpsw.getX(), gpne.getY(), new LatLonPoint.Double());
                LatLonPoint llp2 = projection.inverse(gpne.getX(), gpsw.getY(), new LatLonPoint.Double());

                raster.setULLat(llp1.getY());
                raster.setULLon(llp1.getX());

                raster.setLRLat(llp2.getY());
                raster.setLRLon(llp2.getX());
            }
        }

        if (projection != null) {
            regenerate(projection);
        }
    }

    /**
     * Called to set the OffsetGrabPoint to the current mouse location, and
     * update the OffsetGrabPoint with all the other GrabPoint locations, so
     * everything can shift smoothly. Should also set the OffsetGrabPoint to the
     * movingPoint. Should be called only once at the beginning of the general
     * movement, in order to set the movingPoint. After that, redraw(e) should
     * just be called, and the movingPoint will make the adjustments to the
     * graphic that are needed.
     */
    public void move(java.awt.event.MouseEvent e) {
    }

    /**
     * Use the current projection to place the graphics on the screen. Has to be
     * called to at least assure the graphics that they are ready for rendering.
     * Called when the graphic position changes.
     * 
     * @param proj com.bbn.openmap.proj.Projection
     * @return true
     */
    public boolean generate(Projection proj) {
        Debug.message("eomgdetail", "EditableOMScalingRaster.generate()");
        if (raster != null)
            raster.generate(proj);

        for (int i = 0; i < gPoints.length; i++) {
            GrabPoint gp = gPoints[i];
            if (gp != null) {
                gp.generate(proj);
            }
        }
        return true;
    }

    /**
     * Given a new projection, the grab points may need to be repositioned off
     * the current position of the graphic. Called when the projection changes.
     */
    public void regenerate(Projection proj) {
        Debug.message("eomg", "EditableOMScalingRaster.regenerate()");
        if (raster != null)
            raster.regenerate(proj);

        setGrabPoints(raster);
        generate(proj);
    }

    /**
     * Draw the EditableOMScalingRaster parts into the java.awt.Graphics object.
     * The grab points are only rendered if the rect machine state is
     * RectSelectedState.RECT_SELECTED.
     * 
     * @param graphics java.awt.Graphics.
     */
    public void render(java.awt.Graphics graphics) {
        Debug.message("eomgdetail", "EditableOMScalingRaster.render()");

        State state = getStateMachine().getState();

        if (raster == null) {
            Debug.message("eomg", "EditableOMScalingRaster.render: null rect.");
            return;
        }

        raster.setVisible(true);
        raster.render(graphics);
        raster.setVisible(false);

        int renderType = raster.getRenderType();

        if (state instanceof GraphicSelectedState || state instanceof GraphicEditState) {

            for (int i = 0; i < gPoints.length; i++) {

                GrabPoint gp = gPoints[i];
                if (gp != null) {
                    if ((i == OFFSET_POINT_INDEX && renderType == OMGraphic.RENDERTYPE_OFFSET && movingPoint == gpo)
                            || (state instanceof GraphicSelectedState && ((i != OFFSET_POINT_INDEX && renderType != OMGraphic.RENDERTYPE_OFFSET) || (renderType == OMGraphic.RENDERTYPE_OFFSET)))) {

                        gp.setVisible(true);
                        gp.render(graphics);
                        gp.setVisible(false);
                    }
                }
            }
        }
    }
}