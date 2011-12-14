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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/ProfileGenerator.java,v $
// $RCSfile: ProfileGenerator.java,v $
// $Revision: 1.11 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.image.AcmeGifFormatter;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.stateMachine.State;

/**
 * This tool lets the user draw a line on the map, and then presents
 * the profile of the path in a GIF picture. The line can be drawn in
 * a series if clicks, or the mouse button can be held down as the
 * mouse is dragged around. The lines are drawn as great circle lines,
 * which represent the straight geographical line between clicks.
 * 
 * <P>
 * The profile tool uses the ProfileStateMachine, and the Profile
 * States, to keep track of the proper actions and reactions of user
 * input.
 */
public class ProfileGenerator implements TerrainTool {
    /** The color of the line that is drawn on the screen. */
    Color toolColor = new Color(255, 0, 0);
    /** The state machine for user gestures. */
    protected ProfileStateMachine stateMachine;
    /** The layer that the tool is serving. */
    protected TerrainLayer layer;
    /** The list of graphics to draw. Contains the drawn line. */
    protected OMGraphicList graphics = new OMGraphicList();
    /**
     * Array of LatLonPoints. The points are the clicked points, and
     * the points in between, on a great circle. Have to figure these
     * points out, and not rely only on the poly line points, because
     * we need to get the elevations for all the points for the
     * profile.
     */
    public Vector<LatLonPoint> coords;
    /**
     * These are the raw x-y points of the gestures, for the great
     * circle line points, too. These are used to construct the
     * profile image. An array of java.awt.Points.
     */
    public Vector<Point> xypoints;
    /**
     * The line drawn on the screen representing the profile line
     * path.
     */
    public OMPoly profileLine;
    /**
     * General gesture tracking, Used to track the last place of
     * interest on the screen for the creation of hte profile.
     */
    MouseEvent lastMouse;
    /**
     * A copy of the most current projection to use to update the
     * drawn line.
     */
    Projection proj;

    public ProfileGenerator(TerrainLayer tLayer) {
        layer = tLayer;
        init();
    }

    public synchronized OMGraphicList getGraphics() {
        profileLine.setLocation(setLLPoints(), OMGraphic.RADIANS);
        profileLine.generate(proj);
        return graphics;
    }

    /**
     * Create the line object, the state machine, and the vectors used
     * to keep track of the line being drawn.
     */
    public void init() {
        lastMouse = null;

        coords = new Vector<LatLonPoint>();
        xypoints = new Vector<Point>();

        profileLine = new OMPoly(setLLPoints(), OMGraphic.RADIANS, OMGraphic.LINETYPE_GREATCIRCLE);
        profileLine.setLinePaint(toolColor);

        graphics.add(profileLine);
        //      System.loadLibrary("com_bbn_openmap_terrain_ProfileGenerator");

        stateMachine = new ProfileStateMachine(this);
    }

    /**
     * Clears the line from the screen, and resets the state machine.
     */
    public void reset() {
        coords.removeAllElements();
        xypoints.removeAllElements();
        profileLine.setLocation(setLLPoints(), OMGraphic.RADIANS);
        stateMachine.reset();
        layer.repaint();
        lastMouse = null;
    }

    public void setScreenParameters(Projection p) {
        proj = p;
        graphics.generate(p);
    }

    /**
     * Returns a set of lat lon points that represent the line as it
     * was drawn. The lat lon points are in an array of floats, that
     * alternate, lat, lon, etc.
     */
    public double[] setLLPoints() {
        double[] points;
        int num_points = coords.size();

        if (num_points <= 1) {

            points = new double[4];
            if (num_points == 0) {
                points[0] = 0f;
                points[1] = -6f;
            } else {
                points[0] = ((LatLonPoint) coords.elementAt(0)).getRadLat();
                points[1] = ((LatLonPoint) coords.elementAt(0)).getRadLon();
            }

            points[2] = points[0];
            points[3] = points[1];
        } else {
            points = new double[coords.size() * 2];
            for (int i = 0; i < coords.size(); i++) {
                points[i * 2] = (float)((LatLonPoint) coords.elementAt(i)).getRadLat();
                points[(i * 2) + 1] = (float)((LatLonPoint) coords.elementAt(i)).getRadLon();
            }
        }
        return points;
    }

    /**
     * Returns the current state of the state machine.
     */
    public State getState() {
        return stateMachine.getState();
    }

    /**
     * Creates the line points for the path drawn on the screen, and
     * collects the elevation values for those points. Makes the call
     * to write the new gif file to disk.
     */
    public void createProfileImage() {

        Debug.message("terrain",
                "ProfileGenerator:createProfileImage(): Creating image");

        if (layer == null || layer.frameCache == null) {
            Debug.error("ProfileGenerator:  can't access the DTED data through the terrain layer.");
            return;
        }

        // Set the final line, as it was drawn.
        profileLine.setLocation(setLLPoints(), OMGraphic.RADIANS);

        int total_distance = 0;
        int[] distances = new int[xypoints.size()];
        Point tmpPoint1, tmpPoint2;
        distances[0] = 0;
        for (int j = 1; j < xypoints.size(); j++) {
            tmpPoint1 = (Point) xypoints.elementAt(j);
            tmpPoint2 = (Point) xypoints.elementAt(j - 1);
            // Needed for the GIF, the number of pixels (distance)
            // between points of the line. The distances array is the
            // distance between this point and the next in the xy
            // point array.
            distances[j] = TerrainLayer.numPixelsBetween(tmpPoint1.x,
                    tmpPoint1.y,
                    tmpPoint2.x,
                    tmpPoint2.y);
            total_distance += distances[j];
        }

        int tmp = 0;
        int max = 0;
        int[] heights = new int[xypoints.size()];
        // Go through the points and get the heights
        for (int i = 0; i < heights.length; i++) {
            LatLonPoint llp = ((LatLonPoint) coords.elementAt(i));
            // Ask the cache for the elevation
            tmp = layer.frameCache.getElevation(llp.getLatitude(),
                    llp.getLongitude());

            if (tmp == DTEDFrameCache.NO_DATA)
                tmp = -1;
            if (tmp > max)
                max = tmp;
            heights[i] = tmp;
        }
        // get the picture drawn and written
        createGIFFile(total_distance, max, distances, heights);
    }

    /**
     * Create the image and write it the location.
     * 
     * @param distance total length of line, in pixels
     * @param max highest point, in meters of all the heights in the
     *        line.
     * @param post_dist array of pixel distances between the points
     * @param post_height the array of heights
     */
    protected void createGIFFile(int distance, int max, int[] post_dist,
                                 int[] post_height) {

        int box_height_buffer = 20;
        int gif_height_buffer = 20;
        int gif_width_buffer = 20;
        int text_width = 100;

        int box_height = max + (box_height_buffer * 2);
        int box_width = distance;
        int gif_height = box_height + (gif_height_buffer * 2);
        int gif_width = box_width + (gif_width_buffer * 2) + text_width;

        AcmeGifFormatter formatter = new AcmeGifFormatter();
        java.awt.Graphics graphics = formatter.getGraphics(gif_width,
                gif_height);

//        Color gray10 = new Color(25, 25, 25);
        Color gray50 = new Color(128, 128, 128);
//        Color gray75 = new Color(191, 191, 191);
        Color gray90 = new Color(230, 230, 230);

        Debug.message("terrain",
                "ProfileGenerator gif creation: drawing boundaries");
        /* Fill in the generic colors */
        graphics.setColor(gray90);
        graphics.fillRect(0, 0, gif_width, gif_height);

        graphics.setColor(gray50);
        graphics.fillRect(gif_width_buffer,
                gif_height_buffer,
                box_width,
                box_height);

        Debug.message("terrain", "ProfileGenerator gif creation: drawing edges");
        // outside edge
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, gif_width - 1, gif_height - 1);
        // inside edge
        graphics.drawRect(gif_width_buffer,
                gif_height_buffer,
                box_width,
                box_height);

        graphics.setColor(Color.yellow);
        // 0 height line
        graphics.drawLine(gif_width_buffer + 1,
                gif_height_buffer + box_height - box_height_buffer,
                gif_width_buffer + box_width - 1,
                gif_height_buffer + box_height - box_height_buffer);

        // These are the horizontal reference lines in the image.
        graphics.setColor(Color.black);
        FontMetrics f = graphics.getFontMetrics();

        Debug.message("terrain",
                "ProfileGenerator gif creation: drawing level lines");
        for (int i = 1; i < 9; i++) {

            graphics.drawLine(gif_width_buffer, gif_height_buffer + box_height
                    - box_height_buffer - (max * i / 8), gif_width_buffer
                    + box_width + 5, gif_height_buffer + box_height
                    - box_height_buffer - (max * i / 8));

            int meters = max * i / 8;
            int feet = (int) (meters * 3.2);
            String lineLabel = meters + "m / " + feet + "ft";
//            byte[] lineLabelBytes = lineLabel.getBytes();

            graphics.drawString(lineLabel,
                    gif_width_buffer + box_width + 10,
                    gif_height_buffer + box_height - box_height_buffer
                            - (max * i / 8) + (f.getAscent() / 2));
        }

//        int last_x = gif_width_buffer + 1;
//        int last_height = gif_height_buffer + box_height - box_height_buffer
//                - post_height[0];

        int total_distance = 0;
        Debug.message("terrain",
                "ProfileGenerator gif creation: drawing profile");

        graphics.setColor(Color.red);
        for (int i = 1; i < post_height.length; i++) {
            graphics.drawLine(gif_width_buffer + total_distance,
                    gif_height_buffer + box_height - box_height_buffer
                            - post_height[i - 1],
                    gif_width_buffer + post_dist[i] + total_distance,
                    gif_height_buffer + box_height - box_height_buffer
                            - post_height[i]);

            total_distance += post_dist[i];
        }

        javax.swing.ImageIcon ii = new javax.swing.ImageIcon(formatter.getBufferedImage());
        javax.swing.JFrame jf = com.bbn.openmap.util.PaletteHelper.getPaletteWindow(new javax.swing.JLabel(ii),
                "Path Profile",
                (ComponentListener) null);

        jf.setVisible(true);

        //      byte[] imageBytes = formatter.getImageBytes();
        //      String tmppath = null;
        //      try {
        //          String tmpDir = Environment.get(Environment.TmpDir);
        //          if (tmpDir != null) {
        //              tmppath = tmpDir + File.separator + "openmap-" +
        //                  Environment.timestamp() + ".gif";
        //              FileOutputStream fs = new FileOutputStream(tmppath);
        //              fs.write(imageBytes);
        //              fs.close(); // close the streams

        //              String url = "file://" + tmppath;
        //              layer.fireRequestURL(url);
        //          } else {
        //              Debug.error("ProfileGenerator: can't create image file,
        // because the openmap.TempDirectory was not set.");
        //          }
        //      } catch (IOException e) {
        //          Debug.error("ProfileGenerator: Cannot write to temp file:"
        // +
        //                      Environment.get("line.separator") +
        //                      "\"" + tmppath + "\"");
        //      }
        //      String imageString = new String(imageBytes);
        //      layer.fireRequestBrowserContent(imageString);
    }

    /**
     * Used to keep track of another point for the line, as determined
     * by the state machine.
     * 
     * @param event Mouse event that supplies the location
     */
    protected void addProfileEvent(MouseEvent event) {
        LatLonPoint llp = proj.inverse(event.getX(), event.getY(), new LatLonPoint.Double());
        if (lastMouse != null) {
            // Check for proximity of the click, since a double
            // click means the end of the line.
            if ((Math.abs(lastMouse.getX() - event.getX()) > MAX_SPACE_BETWEEN_PIXELS)
                    || (Math.abs(lastMouse.getY() - event.getY()) > MAX_SPACE_BETWEEN_PIXELS)) {
                // The line may need to be broken up into smaller
                // segments in order for it to be a true straight
                // line, to figure out the segments. The interior
                // points are added to the vector.
                addGreatCirclePoints(lastMouse, event);
                // Now add the end point to the vector
                coords.addElement(llp);
                // The xy points don't need the interior points, the
                // line gets these points and figures them out for
                // itself. This may be redundant is some way.
                xypoints.addElement(event.getPoint());
            }
        } else {
            coords.addElement(llp);
            xypoints.addElement(event.getPoint());
        }
        lastMouse = event;
        // Reset the line to have all the new points
        profileLine.setLocation(setLLPoints(), OMGraphic.RADIANS);
        profileLine.generate(proj);
    }

    /**
     * Figure out the internal points to create a great circle line
     * between two points on the screen. The interior points are added
     * to the coords array, but not to the xy points array.
     * 
     * @param beginning the starting mouse event
     * @param ending the ending mouse event
     */
    protected void addGreatCirclePoints(MouseEvent beginning, MouseEvent ending) {
        LatLonPoint beg = proj.inverse(beginning.getX(), beginning.getY(), new LatLonPoint.Double());
        LatLonPoint end = proj.inverse(ending.getX(), ending.getY(), new LatLonPoint.Double());

        int num_points = (TerrainLayer.numPixelsBetween(beginning.getX(),
                beginning.getY(),
                ending.getX(),
                ending.getY()) - 2)
                / MAX_SPACE_BETWEEN_PIXELS;

        float[] radPoints = GreatCircle.greatCircle((float)beg.getRadLat(),
                (float)beg.getRadLon(),
                (float)end.getRadLat(),
                (float)end.getRadLon(),
                num_points,
                true);
        boolean geoProj = proj instanceof GeoProj;
        for (int i = 0; i < radPoints.length; i++) {
            coords.addElement(new LatLonPoint.Double(radPoints[i], radPoints[i + 1], true));
            Point pt = new Point();
            if (geoProj) {
                ((GeoProj)proj).forward(radPoints[i], radPoints[i + 1], pt, true);
            } else {
                proj.forward(Math.toDegrees(radPoints[i]), Math.toDegrees(radPoints[i + 1]), pt);
            }
            xypoints.addElement(pt);

            //        System.out.println("addCGPoints: point " + i + " lat="
            // +
            //                           RadianPoint.radToDeg(radPoints[i].lat) + ", lon=" +
            //                           RadianPoint.radToDeg(radPoints[i].lon) + ", x=" +
            //                           (short)pt.x + ", y=" + (short)pt.y);
            i++;
        }
    }

}

