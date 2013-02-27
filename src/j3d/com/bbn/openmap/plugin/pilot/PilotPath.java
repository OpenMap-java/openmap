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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/plugin/pilot/PilotPath.java,v $
// $RCSfile: PilotPath.java,v $
// $Revision: 1.8 $
// $Date: 2009/02/23 22:37:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.pilot;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.geom.Point2D;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Vector3d;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.j3d.ControlledManager;
import com.bbn.openmap.tools.j3d.NavBehaviorProvider;
import com.bbn.openmap.tools.j3d.OM3DConstants;
import com.bbn.openmap.tools.j3d.OMKeyBehavior;
import com.bbn.openmap.util.Debug;

/**
 * The PilotPath is a definition of a path that a Java 3D window will take. This
 * is a demonstration class that will have improvements as time goes on. Right
 * now, height does not have an effect on the view.
 * <P>
 * 
 * The PlugIn palette lets the user use the OMDrawingTool to define the path.
 * The path also provides a button on the palette to launch the J3D viewer.
 */
public class PilotPath extends Pilot implements NavBehaviorProvider {

    double[] pathPoints = null;
    OMPoly poly = null;
    int pathIndex = 0;
    double currentSegDist = 0.0;
    double nextSegOffset = 0.0;
    double rate = Length.METER.toRadians(10000.0);

    protected boolean DEBUG = false;

    /**
     * Define a path, with the radius and isOval referring to the marker for
     * marking the pilot's position on the path.
     */
    public PilotPath(OMPoly path, int radius, boolean isOval) {
        super(0f, 0f, radius, isOval);
        setPoly(path);
        DEBUG = Debug.debugging("pilot");

        setHeight(10.3f);
    }

    /**
     * Tell the pilot to move along the path. The factor is not currently used.
     */
    public void move(float factor) {
        if (!stationary) {
            moveAlong();
        }
    }

    /**
     * Returns the coordinates for the current poly segment.
     */
    public double[] getSegmentCoordinates(int currentPathIndex) {
        double[] latlons = new double[4];

        if (pathIndex > pathPoints.length - 2 || pathIndex < 0) {
            pathIndex = 0;
        }

        if (pathPoints.length >= 4) {

            int la1 = pathIndex;
            int lo1 = pathIndex + 1;

            int la2 = pathIndex + 2;
            int lo2 = pathIndex + 3;

            if (lo2 >= pathPoints.length) {
                if (poly.isPolygon()) {
                    if (DEBUG)
                        Debug.output("PilotPath.moveAlong(): index too big, wrapping... ");
                    la2 = 0;
                    lo2 = 1;
                } else {
                    pathIndex = 0;
                    if (DEBUG)
                        Debug.output("PilotPath.moveAlong(): index too big, no wrapping, starting over... ");
                    return getSegmentCoordinates(pathIndex);
                }
            }

            latlons[0] = pathPoints[la1];
            latlons[1] = pathPoints[lo1];
            latlons[2] = pathPoints[la2];
            latlons[3] = pathPoints[lo2];
        }

        return latlons;
    }

    /**
     * Figures out the next position of the pilot, given the distance the pilot
     * should move for this turn.
     */
    public void moveAlong() {
        if (DEBUG) {
            Debug.output("PilotPath.moveAlong(): segment " + (pathIndex / 2) + " of "
                    + (pathPoints.length / 2));
        }

        double[] latlons = getSegmentCoordinates(pathIndex);

        double segLength = GreatCircle.sphericalDistance(latlons[0], latlons[1], latlons[2], latlons[3]);
        if (DEBUG) {
            Debug.output("PilotPath.moveAlong(): segment Length " + segLength
                    + ", and already have " + currentSegDist + " of it.");
        }
        double needToTravel = rate;
        int originalPathIndex = pathIndex;
        int loopingTimes = 0;
        while (needToTravel >= segLength - currentSegDist) {

            needToTravel -= (segLength - currentSegDist);
            currentSegDist = 0f;

            pathIndex += 2;
            // Move to the next segment of the poly

            if (DEBUG) {
                Debug.output("PilotPath to next segment(" + (pathIndex / 2) + "), need to travel "
                        + needToTravel);
            }
            latlons = getSegmentCoordinates(pathIndex);

            if (pathIndex == originalPathIndex) {
                loopingTimes++;
                if (loopingTimes > 1) {
                    if (DEBUG)
                        Debug.output("PilotPath looping on itself, setting to stationary");
                    setStationary(true);
                    return;
                }
            }

            segLength = GreatCircle.sphericalDistance(latlons[0], latlons[1], latlons[2], latlons[3]);
        }

        if (DEBUG) {
            Debug.output("Moving PilotPath within current(" + (pathIndex / 2)
                    + ") segment, segLength: " + segLength + ", ntt: " + needToTravel);
        }

        // Staying on this segment, just calculate where the
        // next point on the segment is.
        double azimuth = GreatCircle.sphericalAzimuth(latlons[0], latlons[1], latlons[2], latlons[3]);

        Point2D newPoint = GreatCircle.sphericalBetween(latlons[0], latlons[1], currentSegDist
                + needToTravel, azimuth);

        setLat(newPoint.getY());
        setLon(newPoint.getX());

        currentSegDist = GreatCircle.sphericalDistance(latlons[0], latlons[1], Math.toRadians(newPoint.getY()), Math.toRadians(newPoint.getX()));

        // OK, now move the camera accordingly...

        if (DEBUG)
            Debug.output("moveAlong: azimuth = " + azimuth);

        if (viewProjection == null) {
            return;
        }

        Point2D newLoc = viewProjection.forward(newPoint);

        if (DEBUG)
            Debug.output(newLoc.toString() + ", compared with lastX, lastY: " + lastX + ", "
                    + lastY + ", scaleFactor= " + scaleFactor);

        double centerXOffset = newLoc.getX() * scaleFactor;
        double centerYOffset = newLoc.getY() * scaleFactor;

        Vector3d translate = new Vector3d();

        // 0f can be changed to account for any height change.
        translate.set(centerXOffset - lastX, 0, centerYOffset - lastY);
        lastX = centerXOffset;
        lastY = centerYOffset;

        if (DEBUG)
            Debug.output("PP moving: " + translate);

        // translateTransform.set(scaleFactor, translate);

        // cameraTransformGroup.getTransform(translateTransform);
        // Transform3D toMove = new Transform3D();

        // toMove.setTranslation(translate);
        // translateTransform.mul(toMove);

        // cameraTransformGroup.setTransform(translateTransform);

        if (platformBehavior != null) {

            platformBehavior.doMove(translate);
            if (lastAzimuth != azimuth) {
                platformBehavior.doLookY(lastAzimuth - azimuth);
                lastAzimuth = azimuth;
            }
        }
    }

    protected Transform3D translateTransform = new Transform3D();
    protected Projection viewProjection;
    protected TransformGroup cameraTransformGroup;
    protected float scaleFactor = 1f;
    protected double lastX;
    protected double lastY;
    protected double lastAzimuth = 0;

    /**
     * Standard generate method, generating all the OMGraphics with the current
     * position.
     */
    public boolean generate(Projection p) {
        // At least try to keep the current version, in case things
        // get set up with the 3D viewer out of order.
        viewProjection = p;

        boolean ret = super.generate(p);
        if (poly != null) {
            poly.generate(p);
        }
        return ret;
    }

    public void render(Graphics g) {
        if (poly != null) {
            poly.render(g);
        }
        super.render(g);
    }

    /**
     * Set the polygon for the path.
     */
    public void setPoly(OMPoly p) {
        poly = p;

        if (poly.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            pathPoints = poly.getLatLonArray();
            setLat(ProjMath.radToDeg(pathPoints[0]));
            setLon(ProjMath.radToDeg(pathPoints[1]));
            setStationary(false);
        } else {
            setStationary(true);
        }
    }

    public OMPoly getPoly() {
        return poly;
    }

    OMKeyBehavior platformBehavior;

    public Behavior setViewingPlatformBehavior(TransformGroup ctg, Projection projection,
                                               float scaleFactor) {

        if (DEBUG)
            Debug.output("PilotPath setting viewing platform behavior");
        cameraTransformGroup = ctg;

        platformBehavior = new OMKeyBehavior(cameraTransformGroup, viewProjection, locateWorld(projection, scaleFactor));
        // Trying to look down a little, didn't work. Should have,
        // though.
        // platformBehavior.doLookX(com.bbn.openmap.MoreMath.HALF_PI/2f);
        return platformBehavior;
    }

    /**
     */
    public Vector3d locateWorld(Projection projection, float scaleFactor) {

        // Set the view parameters.
        this.viewProjection = projection;
        translateTransform = new Transform3D();
        this.scaleFactor = scaleFactor;

        cameraTransformGroup.getTransform(translateTransform);

        if (DEBUG)
            Debug.output("PilotPath setting camera location, scaleFactor = " + this.scaleFactor);
        Vector3d translate = new Vector3d();

        if (projection != null) {

            Point2D pilotPoint = projection.forward(getLat(), getLon());

            // scaleFactor of < 1 shrinks the object.(.5) is the scale.

            // So, this lays out where the land is, in relation to the
            // viewer. We should get the projection from the MapBean, and
            // offset the transform to the middle of the map.

            double centerXOffset = pilotPoint.getX() * scaleFactor;
            double centerYOffset = pilotPoint.getY() * scaleFactor;

            if (DEBUG)
                Debug.output("OM3DViewer with projection " + projection
                        + ", setting center of scene to " + centerXOffset + ", " + centerYOffset);

            translate.set(centerXOffset, (double) height, centerYOffset);
            lastX = centerXOffset;
            lastY = centerYOffset;
        } else {
            translate.set(0, height, 0);
        }

        return translate;
    }

    /** Needed for J3D world. */
    protected MapHandler mapHandler;

    public void setMapHandler(MapHandler mh) {
        mapHandler = mh;
    }

    public void launch3D() {
        JFrame viewer = ControlledManager.getFrame("OpenMap 3D", 500, 500, mapHandler, (NavBehaviorProvider) this, new javax.media.j3d.Background(.3f, .3f, .3f), OM3DConstants.CONTENT_MASK_OMGRAPHICHANDLERLAYERS
                | OM3DConstants.CONTENT_MASK_OM3DGRAPHICHANDLERS);
        viewer.setVisible(true);
    }

    public final static String Launch3DCmd = "Launch3D";

    /**
     * Gets the gui controls associated with the Pilot. This default
     * implementation returns null indicating that the Pilot has no gui
     * controls.
     * 
     * @return java.awt.Component or null
     */
    public java.awt.Component getGUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        // Only want to do this once...
        if (movementButton == null) {
            movementButton = new JCheckBox("Stationary", getStationary());
            movementButton.addActionListener(this);
            movementButton.setActionCommand(MoveCmd);
        }

        panel.add(movementButton);

        JPanel heightPanel = new JPanel(new GridLayout(0, 3));

        heightPanel.add(new JLabel("Object height: "));
        if (heightField == null) {
            heightField = new JTextField(Double.toString(height), 10);
            heightField.setHorizontalAlignment(JTextField.RIGHT);
            heightField.addActionListener(this);
            heightField.addFocusListener(this);
        }
        heightPanel.add(heightField);
        // There aren't any units to this yet - we need a good
        // translation between meters and the j3d world elevations.
        heightPanel.add(new JLabel(" "));
        panel.add(heightPanel);

        JButton launch3DButton = new JButton("Launch 3D");
        launch3DButton.setActionCommand(Launch3DCmd);
        launch3DButton.addActionListener(this);

        panel.add(launch3DButton);

        return panel;
    }

    public void actionPerformed(java.awt.event.ActionEvent ae) {
        super.actionPerformed(ae);
        String cmd = ae.getActionCommand();
        if (cmd == Launch3DCmd) {
            launch3D();
        }
    }

}