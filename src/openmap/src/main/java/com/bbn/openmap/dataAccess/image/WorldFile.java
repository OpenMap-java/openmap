//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: WorldFile.java,v $
//$Revision: 1.3 $
//$Date: 2007/01/22 16:39:14 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.image;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A world file is a small text file that describes the geographic location of
 * an image on a map. It looks like this:
 * 
 * <pre>
 *            20.154 &lt;the dimension of a pixel in map units in the x direction&gt;
 *            0.000 &lt;rotation term for row&gt;
 *            0.000 &lt;rotation term for column&gt;
 *            -20.154 &lt;the dimension of a pixel in map units in the y direction&gt;
 *            424178 &lt;the x coordinate of the center of pixel 1,1 (upper-left pixel)&gt;
 *            4313415 &lt;the y coordinate of the center of pixel 1,1 (upper-left pixel)&gt;
 * </pre>
 * 
 * The naming convention of the world file is that it should have the same name
 * as the file it represents, with a modified extension. The extension should be
 * the extension of the image file with a w attached to it, or the first and
 * third letters of the image file extension with a w appended to it. It can
 * also have a .wld extension.
 * 
 * @author dietrick
 */
public class WorldFile {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.image.WorldFile");

    /**
     * The dimension of a pixel in map units in the x direction.
     */
    protected double xDim;
    /**
     * The dimension of a pixel in map units in the y direction.
     */
    protected double yDim;
    /**
     * The rotation term for row.
     */
    protected double rowRot;
    /**
     * The rotation term for column.
     */
    protected double colRot;
    /**
     * The x coordinate of the center of pixel 1,1 (upper-left pixel).
     */
    protected double x;
    /**
     * The y coordinate of the center of pixel 1,1 (upper-left pixel).
     */
    protected double y;

    protected WorldFile() {
    // For subclasses.
    }
    
    public WorldFile(double xDim, double yDim, double rowRot, double colRot, double x, double y) {
        this.xDim = xDim;
        this.yDim = yDim;
        this.rowRot = rowRot;
        this.colRot =colRot;
        this.x = x;
        this.y = y;
    }

    public WorldFile(URL fileURL) throws MalformedURLException, IOException {
        read(fileURL.openStream());
    }

    public WorldFile(InputStream is) throws MalformedURLException, IOException {
        read(is);
    }

    public void read(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        xDim = Double.parseDouble(br.readLine());
        rowRot = Double.parseDouble(br.readLine());
        colRot = Double.parseDouble(br.readLine());
        yDim = Double.parseDouble(br.readLine());
        x = Double.parseDouble(br.readLine());
        y = Double.parseDouble(br.readLine());

        logger.fine(this.toString());
    }
    
    public void write(OutputStream os) throws IOException {
        OutputStreamWriter osr = new OutputStreamWriter(os);
        osr.write(Double.toString(xDim) + "\n");
        osr.write(Double.toString(rowRot) + "\n");
        osr.write(Double.toString(colRot) + "\n");
        osr.write(Double.toString(yDim) + "\n");
        osr.write(Double.toString(x) + "\n");
        osr.write(Double.toString(y) + "\n");
        osr.close();
    }

    public String toString() {
        return "WorldFile[x(" + x + "), y(" + y + "), xDim(" + xDim
                + "), yDim(" + yDim + "), colRot(" + colRot + "), rowRot("
                + rowRot + ")]";
    }

    /**
     * Given a path to a image file, discover the world file which should be
     * next to it.
     * 
     * @param imageFileURL the path to the image file, not the world file.
     * @return WorldFile object for the image.
     */
    public static WorldFile get(URL imageFileURL) {
        WorldFile wf = null;
        try {
            String startingString = imageFileURL.toString();

            int extensionIndex = startingString.lastIndexOf('.');
            String worldFileNameBase = startingString;
            String extension = null;
            InputStream is = null;

            if (extensionIndex != -1) {

                extension = startingString.substring(extensionIndex);
                worldFileNameBase = startingString.substring(0, extensionIndex);

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("base name for image: " + worldFileNameBase);
                    logger.fine("image extension: " + extension);
                }

                // Try adding w to extension and seeing if that file is there.
                is = checkValidityAndGetStream(worldFileNameBase + extension
                        + "w");

                if (is == null && extension.length() >= 4) {
                    is = checkValidityAndGetStream(worldFileNameBase + "."
                            + extension.charAt(1) + extension.charAt(3) + "w");
                }

            }

            if (is == null) {
                // Try world file extension if nothing else has worked, or if
                // their isn't an extension on the image file.
                extension = ".wld";
                is = checkValidityAndGetStream(worldFileNameBase + extension);
            }

            if (is != null) {
                wf = new WorldFile(is);

                // Check for coordinates of pixel to see if they make sense. If
                // they are greater than 180, then they are probably meters, and
                // we can't handle them right now.

                double x = wf.getX();
                double y = wf.getY();

                if (x < -180 || x > 180 || y > 90 || y < -90) {
                    logger.warning("Looks like an unsupported projection: "
                            + wf.toString());
                    wf = new ErrWorldFile("World File (" + worldFileNameBase
                            + extension
                            + ") doesn't contain decimal degree coordinates");
                }
            }

        } catch (MalformedURLException murle) {

        } catch (IOException ioe) {

        }

        return wf;
    }

    protected static InputStream checkValidityAndGetStream(String wfURLString) {
        try {
            logger.fine("checking for world file: " + wfURLString);
            URL wfURL = new URL(wfURLString);
            return wfURL.openStream();
        } catch (MalformedURLException murle) {
            logger.fine("MalformedURLException for " + wfURLString);
        } catch (IOException ioe) {
            logger.fine("IOException for " + wfURLString);
        }
        return null;

    }

    public double getColRot() {
        return colRot;
    }

    public void setColRot(double colRot) {
        this.colRot = colRot;
    }

    public double getRowRot() {
        return rowRot;
    }

    public void setRowRot(double rowRot) {
        this.rowRot = rowRot;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getXDim() {
        return xDim;
    }

    public void setXDim(double dim) {
        xDim = dim;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getYDim() {
        return yDim;
    }

    public void setYDim(double dim) {
        yDim = dim;
    }
}
