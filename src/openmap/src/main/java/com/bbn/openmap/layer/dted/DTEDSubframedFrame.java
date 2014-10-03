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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDFrame.java,v $
// $RCSfile: DTEDFrame.java,v $
// $Revision: 1.8 $
// $Date: 2008/02/29 00:51:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

import java.awt.Color;

import com.bbn.openmap.dataAccess.dted.DTEDFrame;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * The DTEDSubframedFrame is the representation of the DTED (Digital Terrain
 * Elevation Data) data from a single dted data file. It keeps track of all the
 * attribute information of it's data, and also maintains an array of images
 * (DTEDFrameSubframe) that represent views of the elevation posts.
 */
public class DTEDSubframedFrame
        extends DTEDFrame {

    /** The colortable used to create the images. */
    public DTEDFrameColorTable colorTable;
    /** The subframe presentation attributes. */
    public DTEDFrameSubframeInfo subframeInfo; // master

    /**
     * The frame image is divided into 200x200 pixel subframes, with a leftover
     * frame at the end. This is how many horizontal subframes there are.
     */
    public int number_horiz_subframes;
    /**
     * The frame image is divided into 200x200 pixel subframes, with a leftover
     * frame at the end. This is how many vertical subframes there are.
     */
    public int number_vert_subframes;

    /** The image array for the subframes. */
    public DTEDFrameSubframe subframes[][];

    // ////////////////
    // Administrative methods
    // ////////////////

    /**
     * Simplest constructor.
     * 
     * @param filePath complete path to the DTED frame.
     */
    public DTEDSubframedFrame(String filePath) {
        this(filePath, null, null, false);
    }

    /**
     * Constructor with colortable and presentation information.
     * 
     * @param filePath complete path to the DTED frame.
     * @param cTable the colortable to use for the images.
     * @param info presentation parameters.
     */
    public DTEDSubframedFrame(String filePath, DTEDFrameColorTable cTable, DTEDFrameSubframeInfo info) {
        this(filePath, cTable, info, false);
    }

    /**
     * Constructor with colortable and presentation information.
     * 
     * @param filePath complete path to the DTED frame.
     * @param readWholeFile If true, all of the elevation data will be read at
     *        load time. If false, elevation post data will be read in per
     *        longitude column depending on the need. False is recommended for
     *        DTEd level 1 and 2.
     */
    public DTEDSubframedFrame(String filePath, boolean readWholeFile) {
        this(filePath, null, null, readWholeFile);
    }

    /**
     * Constructor with colortable and presentation information.
     * 
     * @param filePath complete path to the DTED frame.
     * @param cTable the colortable to use for the images.
     * @param info presentation parameters.
     * @param readWholeFile If true, all of the elevation data will be read at
     *        load time. If false, elevation post data will be read in per
     *        longitude column depending on the need. False is recommended for
     *        DTED level 1 and 2.
     */
    public DTEDSubframedFrame(String filePath, DTEDFrameColorTable cTable, DTEDFrameSubframeInfo info, boolean readWholeFile) {

        super(filePath, readWholeFile);

        colorTable = cTable;
        subframeInfo = info;
    }

    public void setColorTable(DTEDFrameColorTable c_Table) {
        colorTable = c_Table;
    }

    public DTEDFrameColorTable getColorTable() {
        return colorTable;
    }

    /**
     * Sets the subframe array. Blows away any images that may already be there.
     */
    public void initSubframes(int numHorizSubframes, int numVertSubframes) {
        number_horiz_subframes = numHorizSubframes;
        number_vert_subframes = numVertSubframes;
        subframes = new DTEDFrameSubframe[numHorizSubframes][numVertSubframes];
        if (Debug.debugging("dted")) {
            Debug.output("///////// DTEDFrame: subframe array initialized, " + numHorizSubframes + "x" + numVertSubframes);
        }
    }

    /**
     * If you just want to get an image for the DTEDFrame, then call this. One
     * OMRaster for the entire DTEDFrame will be returned, with the default
     * rendering parameters (Colored shading) and the default colortable. Use
     * the other getOMRaster method if you want something different. This method
     * actually calls that other method, so read the documentation for that as
     * well.
     * 
     * @param proj EqualArc projection to use to create image.
     * @return raster image to display in OpenMap.
     */
    public OMGraphic getImage(Projection proj) {
        return getImage(null, null, proj);
    }

    /**
     * If you just want to get an image for the DTEDFrame, then call this. One
     * OMRaster for the entire DTEDFrame will be returned. In the
     * DTEDFrameSubframeInfo, you need to set the color type and all the
     * parameters that are assiociated with the rendering parameters. The
     * projection parameters of the DFSI (image height, width, pixel intervals)
     * will be set in this method based on the projection. If you want a
     * different sized image, scale the thing you get back from this method, or
     * change the scale of the projection that is passed in. Calling this method
     * will cause the DTEDFrame subframe cache to reset itself to hold one
     * subframe covering the entire frame. Just so you know.
     * 
     * @param dfsi the DTEDFrameSubframeInfo describing the subframe.
     * @param colortable the colortable to use when building the image.
     * @return raster image to display in OpenMap.
     * @param proj EqualArc projection to use to create image.
     */
    public OMGraphic getImage(DTEDFrameSubframeInfo dfsi, DTEDFrameColorTable colortable, Projection proj) {
        if (proj == null) {
            Debug.error("DTEDFrame.getOMRaster: need projection to create image.");
            return null;
        }

        if (colortable == null) {
            colortable = new DTEDFrameColorTable();
        }

        if (dfsi == null) {
            dfsi =
                    new DTEDFrameSubframeInfo(DTEDFrameSubframe.COLOREDSHADING, DTEDFrameSubframe.DEFAULT_BANDHEIGHT,
                                              DTEDFrameSubframe.LEVEL_1, // Doesn't
                                              // matter
                                              DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST);
        }

        CADRG cadrg = CADRG.convertProjection(proj);

        dfsi.xPixInterval = 360 / cadrg.getXPixConstant(); // degrees/pixel
        dfsi.yPixInterval = 90 / cadrg.getYPixConstant();
        dfsi.height = (int) (1 / dfsi.yPixInterval);
        dfsi.width = (int) (1 / dfsi.xPixInterval);

        // Will trigger the right thing in getSubframeOMRaster;
        subframes = null;

        return getSubframeImage(dfsi, colortable, proj);
    }

    /**
     * Return the subframe image as described in the DTEDFrameSubframeInfo. This
     * is called by the DTEDCacheHandler, which has in turn set the
     * DTEDFrameSubframeInfo parameters to match the projection parameters. This
     * turns out to be kinda important.
     * 
     * @param dfsi the DTEDFrameSubframeInfo describing the subframe.
     * @param colortable the colortable to use when building the image.
     * @return raster image to display in OpenMap.
     */
    public OMGraphic getSubframeImage(DTEDFrameSubframeInfo dfsi, DTEDFrameColorTable colortable, Projection proj) {
        if (!frame_is_valid)
            return null;

        OMGraphic raster = null;

        if (dfsi.viewType == DTEDFrameSubframe.NOSHADING)
            return null;

        if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING)
            colortable.setGreyScale(false);
        else
            colortable.setGreyScale(true);

        float lat_origin = dfsi.lat;
        float lon_origin = dfsi.lon;

        if (subframes == null) {
            // Need to set a couple of things up if the DTEDFrameCache
            // isn't being used to set up the subframe information in
            // the dfsi.
            initSubframes(1, 1);
            // NOTE!! The algorithm uses the coordinates of the top
            // left corner as a reference!!!!!!!!
            lat_origin = dsi.lat_origin + 1;
            lon_origin = dsi.lon_origin;
        }

        DTEDFrameSubframe subframe = subframes[dfsi.subx][dfsi.suby];

        if (Debug.debugging("dteddetail")) {
            Debug.output("Subframe lat/lon => lat= " + lat_origin + " vs. " + dfsi.lat + " lon= " + lon_origin + " vs. " + dfsi.lon
                    + " subx = " + dfsi.subx + " suby = " + dfsi.suby);
            Debug.output("Height/width of subframe => height= " + dfsi.height + " width= " + dfsi.width);
        }

        if (subframe != null) {

            raster = subframe.getImageIfCurrent(proj, dfsi);
            if (raster != null) {
                if (Debug.debugging("dted")) {
                    Debug.output("######## DTEDFrame: returning cached subframe");
                }
                return raster;
            }

            if (Debug.debugging("dted")) {
                Debug.output("   *** DTEDFrame: changing image of cached subframe");
            }

            /*
             * If there is an image, the types are different and it needs to be
             * redrawn
             */
            subframe.dfsi = dfsi.makeClone();

        } else {

            if (Debug.debugging("dted")) {
                Debug.output("   +++ DTEDFrame: creating subframe");
            }

            subframe = new DTEDFrameSubframe(dfsi);
            subframes[dfsi.subx][dfsi.suby] = subframe;
        }

        // lat/lon_post_intervals are *10 too big - // extra 0 in
        // 36000 to counteract
        // start in lower left of subframe
        double start_lat_index = (lat_origin - (double) dsi.sw_lat) * 36000.0 / (double) uhl.lat_post_interval;
        double start_lon_index = (lon_origin - (double) dsi.sw_lon) * 36000.0 / (double) uhl.lon_post_interval;
        double end_lat_index =
                ((lat_origin - ((double) dfsi.height * dfsi.yPixInterval)) - (double) dsi.sw_lat) * 36000.0
                        / (double) uhl.lat_post_interval;
        double end_lon_index =
                ((lon_origin + ((double) dfsi.width * dfsi.xPixInterval)) - (double) dsi.sw_lon) * 36000.0
                        / (double) uhl.lon_post_interval;
        double lat_interval = (start_lat_index - end_lat_index) / (double) dfsi.height;
        double lon_interval = (end_lon_index - start_lon_index) / (double) dfsi.width;

        if (Debug.debugging("dteddetail"))
            Debug.output("  start_lat_index => " + start_lat_index + "\n" + "  end_lat_index => " + end_lat_index + "\n"
                    + "  start_lon_index => " + start_lon_index + "\n" + "  end_lon_index => " + end_lon_index + "\n"
                    + "  lat_interval => " + lat_interval + "\n" + "  lon_interval => " + lon_interval);

        short e1, e2;
        short xc = 0;
        short yc = 0;
        short xnw = 0;
        short ynw = 0;
        short xse = 0;
        short yse = 0;
        double slope;
        double distance = 1.0;
        float value = 0.0f;
        int assignment = 0;
        double modifier = (double) 0;
        double xw_offset = 0;
        double xe_offset = 0;
        double yn_offset = 0;
        double ys_offset = 0;
        int elevation = (int) 0;

        // Calculations needed once for slope shading
        if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING
                || (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {
            // to get to the right part of the frame, kind of like a
            // subframe
            // indexing thing
            xw_offset = start_lon_index - Math.ceil(lon_interval);
            xe_offset = start_lon_index + Math.ceil(lon_interval);
            yn_offset = start_lat_index + Math.ceil(lat_interval);
            ys_offset = start_lat_index - Math.ceil(lat_interval);

            switch (dfsi.dtedLevel) {
                // larger numbers make less contrast
                case 0:
                    modifier = (double) 4;
                    break;// 1000 ideal
                case 1:
                    modifier = (double) .02;
                    break;// 2 ideal
                case 2:
                    modifier = (double) .0001;
                    break;
                case 3:
                    modifier = (double) .000001;
                    break;
                default:
                    modifier = (double) 1;
            }
            // With more colors, contrast tends to be a little light
            // for the
            // default - brighten it up more
            if (colortable.colors.length > 215)
                modifier /= 10;

            for (int h = dfsi.slopeAdjust; h < 5; h++)
                modifier *= 10;
            distance = Math.sqrt((modifier * lon_interval * lon_interval) + (modifier * lat_interval * lat_interval));
        }

        ImageData imageData = ImageData.getImageData(dfsi.colorModel, dfsi.width, dfsi.height, colortable.colors);

        for (short x = 0; x < dfsi.width; x++) {

            // used for both elevation banding and slope
            xc = (short) (start_lon_index + ((x) * lon_interval));
            if (xc < 0)
                xc = 0;
            if (xc > dsi.num_lon_points - 1)
                xc = (short) (dsi.num_lon_points - 1);

            if ((elevations[xc] == null) && !readDataRecord(xc)) {
                Debug.error("DTEDFrame: Problem reading lat point line in data record");
                return null;
            }
            if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING
                    || (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {
                // This is actually finding the right x post for this
                // pixel,
                // within the subframe measurements.
                xnw = (short) (xw_offset + Math.floor(x * lon_interval));
                xse = (short) (xe_offset + Math.floor(x * lon_interval));

                // trying to smooth out the edge of the frame
                if (xc == 0 || xnw < 0) {
                    xnw = xc;
                    xse = (short) (xnw + 2.0 * Math.ceil(lon_interval));
                }
                if (xc == dsi.num_lon_points - 1 || xse > dsi.num_lon_points - 1) {
                    xse = (short) (dsi.num_lon_points - 1);
                    xnw = (short) (xse - 2.0 * Math.ceil(lon_interval));
                }

                if (((elevations[xnw] == null) && !readDataRecord(xnw)) || ((elevations[xse] == null) && !readDataRecord(xse))) {
                    Debug.error("DTEDFrame: Problem reading lat point line in data record");
                    return null;
                }
            }

            // Now, calculate the data and assign the pixels based on
            // y
            for (short y = 0; y < dfsi.height; y++) {

                // used for elevation band and slope
                yc = (short) (start_lat_index - ((y) * lat_interval));
                if (yc < 0)
                    yc = 0;
                elevation = (int) elevations[xc][yc];

                // elevation shading
                if (dfsi.viewType == DTEDFrameSubframe.METERSHADING || dfsi.viewType == DTEDFrameSubframe.FEETSHADING) {

                    // Just use the top two-thirds of the colors
                    if (elevation == 0)
                        assignment = 0; // color water Blue
                    else {
                        if (elevation < 0)
                            elevation *= -1; // Death Valley
                        if (dfsi.viewType == DTEDFrameSubframe.FEETSHADING)
                            elevation = (int) (elevation * 3.2);
                        // Start at the darkest color, and then go up
                        // through the
                        // colormap for each band height, the start
                        // back at the
                        // darkest when you get to the last color. To
                        // make this
                        // more useful, I limit the number of colors
                        // (10) used - if
                        // there isn;t enough contrast between the
                        // colors, you can't
                        // see the bands. The contrast adjustment in
                        // 24-bit color
                        // mode(216 colors) lets you add a few colors.
                        if (colortable.colors.length < 216) {
                            try {
                                assignment = (int) ((elevation / dfsi.bandHeight) % (colortable.colors.length - 6) + 6);
                            } catch (java.lang.ArithmeticException ae) {
                                assignment = 1;
                            }
                        } else {
                            try {
                                assignment =
                                        (int) (((elevation / dfsi.bandHeight) % (10 - 2 * (3 - dfsi.slopeAdjust)) * (colortable.colors.length / (10 - 2 * (3 - dfsi.slopeAdjust)))) + 6);
                            } catch (java.lang.ArithmeticException ae) {
                                assignment = 1;
                            }
                        }
                    }
                    
                    imageData.set(x, y, assignment);
                }

                // Slope shading
                else if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING
                        || (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {

                    // find the y post indexes within the subframe
                    ynw = (short) (yn_offset - Math.floor(y * lat_interval));
                    yse = (short) (ys_offset - Math.floor(y * lat_interval));

                    // trying to smooth out the edge of the frame by
                    // handling the
                    // frame limits
                    if (yse < 0)
                        yse = 0;
                    if (yc == dsi.num_lat_lines - 1 || ynw > dsi.num_lat_lines - 1)
                        ynw = (short) (dsi.num_lat_lines - 1);

                    e2 = elevations[xse][yse]; // down & right
                    // elevation
                    e1 = elevations[xnw][ynw]; // up and left
                    // elevation

                    slope = (e2 - e1) / distance; // slope relative to
                    // nw sun
                    // colormap value darker for negative slopes,
                    // brighter for
                    // positive slopes

                    if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING) {
                        assignment = 1;
                        elevation = (int) (elevation * 3.2);// feet
                        for (int l = 1; l < DTEDFrameColorTable.NUM_ELEVATION_COLORS; l++)
                            if (elevation <= colortable.elevation_color_cutoff[l]) {
                                if (slope < 0)
                                    assignment = (int) (l + DTEDFrameColorTable.NUM_ELEVATION_COLORS);
                                else if (slope > 0)
                                    assignment = (int) (l + (DTEDFrameColorTable.NUM_ELEVATION_COLORS * 2));
                                else
                                    assignment = (int) l;
                                break;
                            }
                        if (elevation == 0)
                            assignment = 0;
                        
                        imageData.set(x, y, assignment);
                    }

                    else {
                        value = (float) (((colortable.colors.length - 1) / 2) + slope);

                        // not water, but close in the colormap - max
                        // dark
                        if (slope != 0 && value < 1)
                            value = 1;
                        if (elevation == 0)
                            value = 0; // water?!?
                        if (value > (colortable.colors.length - 1))
                            value = colortable.colors.length - 1; // max
                        // bright

                        assignment = (int) value;
                        
                        imageData.set(x, y, assignment);
                    }
                }
                // Subframe outlines - different colors for each side
                // of the frame
                // This is really for debugging purposes, really.
                else if (dfsi.viewType == DTEDFrameSubframe.BOUNDARYSHADING) {
                    int c;
                    if (x < 1)
                        c = 1;
                    else if (x > dfsi.width - 2)
                        c = 12;
                    else if (y < 1)
                        c = 1;
                    else if (y > dfsi.height - 2)
                        c = 12;
                    else
                        c = 7;

                    imageData.set(x, y, c);

                } else if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING) {
                    assignment = 1;
                    elevation = (int) (elevation * 3.2);// feet
                    for (int l = 1; l < DTEDFrameColorTable.NUM_ELEVATION_COLORS; l++)
                        if (elevation <= colortable.elevation_color_cutoff[l]) {
                            assignment = (int) l;
                            break;
                        }

                    if (elevation == 0)
                        assignment = 0;
                    if (elevation < 0)
                        assignment = 1;
                    if (elevation > 33000)
                        assignment = 1;

                    imageData.set(x, y, assignment);
                }
            }
            
        }

        imageData.updateData(subframe);

        if (Debug.debugging("dteddetail"))
            Debug.output("DTEDFrame: leaving raster");

        
        return subframe.getImage(proj);
    }

    public static void main(String args[]) {
        Debug.init();
        if (args.length < 1) {
            System.out.println("DTEDFrame:  Need a path/filename");
            System.exit(0);
        }

        System.out.println("DTEDFrame: " + args[0]);
        DTEDSubframedFrame df = new DTEDSubframedFrame(args[0]);
        if (df.frame_is_valid) {
            System.out.println(df.uhl);
            System.out.println(df.dsi);
            System.out.println(df.acc);

            // int startx = 5;
            // int starty = 6;
            // int endx = 10;
            // int endy = 30;

            // short[][] e = df.getElevations(startx, starty, endx,
            // endy);
            // for (int i = e[0].length-1; i >= 0; i--) {
            // for (int j = 0; j < e.length; j++) {
            // System.out.print(e[j][i] + " ");
            // }
            // System.out.println();
            // }
        }
        float lat = df.dsi.lat_origin + .5f;
        float lon = df.dsi.lon_origin + .5f;

        CADRG crg = new CADRG(new LatLonPoint.Double(lat, lon), 1500000, 600, 600);
        final OMGraphic ras = df.getImage(crg);

        // Pushes the image to the left top of the frame.
        if (ras instanceof OMRaster) {
            crg.setHeight(((OMRaster) ras).getHeight());
            crg.setWidth(((OMRaster) ras).getWidth());
        }

        ras.generate(crg);

        java.awt.Frame window = new java.awt.Frame(args[0]) {
            public void paint(java.awt.Graphics g) {
                if (ras != null) {
                    ras.render(g);
                }
            }
        };

        window.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                // need a shutdown event to notify other gui beans and
                // then exit.
                System.exit(0);
            }
        });

        window.setSize(crg.getWidth(), crg.getHeight());
        window.setVisible(true);
        window.repaint();
    }

    protected static abstract class ImageData {
        protected abstract void set(short x, short y, int value);
        protected abstract void updateData(DTEDFrameSubframe dfs);
        
        int width = 0;
        int height = 0;
        Color[] colors;

        protected ImageData(int w, int h, Color[] colors) {
            this.width = w;
            this.height = h;
            this.colors = colors;
        }

        protected static ImageData getImageData(int colorModel, int width, int height, Color[] colors) {
            if (colorModel == OMRasterObject.COLORMODEL_DIRECT) {
                return new Pixel(width, height, colors);
            } else {
                return new Byte(width, height, colors);
            }
        }

        protected static class Pixel
                extends ImageData {
            int[] pixels;
            
            int ranColor;

            protected Pixel(int w, int h, Color[] colors) {
                super(w, h, colors);
                pixels = new int[w * h];
                
                int red = (int) (Math.random() * 255);
                int green = (int) (Math.random() * 255);
                int blue = (int) (Math.random() * 255);
                
                Color color = new Color(red, green, blue);
                ranColor = color.getRGB();
            }

            protected void set(short x, short y, int value) {
                pixels[(y * width) + x] = colors[value].getRGB();
//                pixels[(y * width) + x] = ranColor;
            }

            protected  void updateData(DTEDFrameSubframe dfs) {
                dfs.setPixels(pixels);
            }
        }

        protected static class Byte
                extends ImageData {
            byte[] bytes;

            protected Byte(int w, int h, Color[] colors) {
                super(w, h, colors);
                bytes = new byte[w * h];
            }

            protected void set(short x, short y, int value) {
                bytes[(y * width) + x] = (byte) value;
            }

            protected  void updateData(DTEDFrameSubframe dfs) {
                dfs.setBitsAndColors(bytes, colors);
            }
        }
    }
}
