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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ShapeLinkServer.java,v $
// $RCSfile: ShapeLinkServer.java,v $
// $Revision: 1.6 $
// $Date: 2008/07/20 05:46:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.layer.link.Link;
import com.bbn.openmap.layer.link.LinkBoundingPoly;
import com.bbn.openmap.layer.link.LinkGraphicList;
import com.bbn.openmap.layer.link.LinkMapRequest;
import com.bbn.openmap.layer.link.LinkProperties;
import com.bbn.openmap.layer.link.LinkPropertiesConstants;
import com.bbn.openmap.layer.link.LinkServer;
import com.bbn.openmap.layer.shape.ESRIRecord;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;

/**
 * This LinkServer provides graphics from ShapeFiles. The LinkLayer can provide
 * several properties that can control how these graphics are to be rendered
 * (defined in the .propertiesURL file for the layer):
 * <P>
 * 
 * <pre>
 *   
 *    
 *    # Graphic edge color
 *    lineColor=AARRGGBB
 *    # Graphic fill Color
 *    fillColor=AARRGGBB
 *    # Graphic selected edge color
 *    highlightColor=AARRGGBB
 *    # Graphic edge pixel width
 *    lineWidth=pixel width
 *    
 *    
 * </pre>
 */
public class ShapeLinkServer extends LinkServer implements
        LinkPropertiesConstants {

    protected LinkSpatialIndex spatialIndex;

    /** The color to outline the shapes. */
    protected Color lineColor = Color.black;

    /** The color to fill the shapes. */
    protected Color fillColor = ColorFactory.createColor(0, 0, 0, 0, true);

    protected LinkProperties lineProperties;

    public ShapeLinkServer(Socket socket, String shapeFile) {
        super(socket);
        locateAndSetShapeData(shapeFile);
    }

    protected void finalize() {
        if (Debug.debugging("gc")) {
            Debug.output("ShapeLinkServer instance gc'd - client gone");
        }
    }

    /**
     * handleClient is a method that listens to the link to a client, and
     * responds to requests that are made.
     */
    public void handleClient() throws IOException {
        boolean validQuery;
        try {

            while (true) {
                if (Debug.debugging("shape")) {
                    link.clearBytesWritten();
                }

                link.readAndParse();
                validQuery = false;

                // For instance, you could do something like this...

                LinkMapRequest graphicsQuery = link.getMapRequest();
                // LinkActionRequest gestureQuery =
                // link.getActionRequest();

                if (graphicsQuery != null) {
                    getRectangle(graphicsQuery, link);
                    validQuery = true;
                }
                graphicsQuery = null;

                // if (gestureQuery != null){
                // handleGesture(gestureQuery, link);
                // validQuery = true;
                // }

                if (!validQuery) {
                    huh(link);
                }

                if (Debug.debugging("shape")) {
                    System.out.println("ShapeLinkServer: bytes written for response: "
                            + link.getBytesWritten());
                }
            }

        } catch (IOException ioe) {
            spatialIndex = null;
            lineColor = null;
            fillColor = null;
            lineProperties = null;
            throw ioe;
        }
    }

    public void setFillColor(Color fColor) {
        fillColor = fColor;
    }

    public void setLineColor(Color lColor) {
        lineColor = lColor;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void getRectangle(LinkMapRequest query, Link link)
            throws IOException {
        // String value;

        if (spatialIndex == null)
            link.end(Link.END_TOTAL);

        LinkProperties args = query.getProperties();
        LinkGraphicList lgl = new LinkGraphicList(link, args);
        // System.out.println(args);
        lineProperties = new LinkProperties();
        // Tell the LinkProperties to reuse what has shown up previously, which
        // will be set in the LinkGraphicList.
        lineProperties.setReuseProperties(Boolean.TRUE);

        // value = args.getProperty(LPC_LINECOLOR);
        // if (value != null)
        // lineProperties.setProperty(LPC_LINECOLOR, value);
        // value = args.getProperty(LPC_FILLCOLOR);
        // if (value != null)
        // lineProperties.setProperty(LPC_FILLCOLOR, value);
        // value = args.getProperty(LPC_HIGHLIGHTCOLOR);
        // if (value != null)
        // lineProperties.setProperty(LPC_HIGHLIGHTCOLOR, value);
        // value = args.getProperty(LPC_LINEWIDTH);
        // if (value != null)
        // lineProperties.setProperty(LPC_LINEWIDTH, value);

        LinkBoundingPoly[] bounds = query.getBoundingPolys();

        for (int i = 0; i < bounds.length; i++) {
            fetchGraphics((double) bounds[i].minX,
                    (double) bounds[i].minY,
                    (double) bounds[i].maxX,
                    (double) bounds[i].maxY,
                    lgl,
                    lineProperties);
        }

        lgl.end(Link.END_TOTAL);
    }

    /**
     * 
     */
    protected void fetchGraphics(double xmin, double ymin, double xmax,
                                 double ymax, LinkGraphicList lgl,
                                 LinkProperties properties) throws IOException {

        Debug.message("shape", "fetchGraphics: " + xmin + ", " + ymin + ", "
                + xmax + ", " + ymax);

        try {
            ESRIRecord records[] = spatialIndex.locateRecords(xmin,
                    ymin,
                    xmax,
                    ymax);
            int nRecords = records.length;
            ESRILinkRecord rec;
            for (int i = 0; i < nRecords; i++) {
                if (records[i] instanceof ESRILinkRecord) {
                    rec = (ESRILinkRecord) records[i];
                    rec.writeLinkGraphics(lgl, properties);
                }
            }
        } catch (java.io.IOException ex) {
            if (Debug.debugging("shape")) {
                ex.printStackTrace();
            }
            return;
        } catch (com.bbn.openmap.io.FormatException fe) {
            Debug.error("ShapeLinkServer caught FormatException in the file");
            fe.printStackTrace();
        }
    }

    protected void locateAndSetShapeData(String shapeFileName) {
        Debug.message("shape", "ShapeLinkServer: Reading shape file.");
        
        String spatialIndexFileName = shapeFileName.substring(0,
                shapeFileName.indexOf(".shp"))
                + ".ssx";

        
        File spatialIndexFile = new File(spatialIndexFileName);

        if (spatialIndexFile.isAbsolute()) {
            // System.out.println("Absolute!");
            try {
                spatialIndex = new LinkSpatialIndex(shapeFileName);
            } catch (java.io.IOException e) {
                if (Debug.debugging("shape")) {
                    e.printStackTrace();
                }
            }
        } else {
            // System.out.println("Relative!");
            Vector<String> dirs = Environment.getClasspathDirs();
            int nDirs = dirs.size();
            if (nDirs > 0) {
                for (String dir : dirs) {
                    File sif = new File(dir, spatialIndexFileName);
                    if (sif.isFile()) {
                        File sf = new File(dir, shapeFileName);
                        try {
                            // System.out.println(sif.toString());
                            // System.out.println(sf.toString());
                            spatialIndex = new LinkSpatialIndex(sf.toString());
                            break;
                        } catch (java.io.IOException e) {
                            if (Debug.debugging("shape")) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (spatialIndex == null) {
                    System.err.println("Unable to find file: " + shapeFileName);
                    System.err.println("Unable to find file: "
                            + spatialIndexFileName);
                }
            } else {
                System.err.println("No directories in CLASSPATH!");
                System.err.println("Unable to locate file: " + shapeFileName);
                System.err.println("Unable to locate file: "
                        + spatialIndexFileName);
            }
        }
    }
}