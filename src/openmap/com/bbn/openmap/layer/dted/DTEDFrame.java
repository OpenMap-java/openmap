// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.2 $
// $Date: 2003/11/14 20:32:37 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.dted;
import java.io.*;

import com.bbn.openmap.io.*;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMRasterObject;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.EqualArc;
import com.bbn.openmap.util.Debug;

/** 
 * The DTEDFrame is the representation of the DTED (Digital Terrain
 * Elevation Data) data from a single dted data file.  It keeps track
 * of all the attribute information of it's data, and also maintains
 * an array of images (DTEDFrameSubframe) that represent views of the
 * elevation posts.
 */
public class DTEDFrame implements Closable {

    public final static int UHL_SIZE = 80;
    public final static int DSI_SIZE = 648;
    public final static int ACC_SIZE = 2700;
    public final static int ACC_SR_SIZE = 284;
    /** The binary buffered file to read the data from the file. */
    protected BinaryFile binFile;
    /** The path to the frame, including the frame name. */    
    protected String path;
    /** 
     * The array of elevation posts. Note: the 0 index of the array in
     * both directions is in the lower left corner of the matrix.  As
     * you increase indexes in both dimensions, you go up-right.  
     */
    protected short[][] elevations; // elevation posts

    /** Data set indentification section of the file. */
    public DTEDFrameDSI dsi;
    /** User header label section of the file. */
    public DTEDFrameUHL uhl;
    /** The colortable used to create the images. */
    public DTEDFrameColorTable colorTable;
    /** The subframe presentation attributes. */
    public DTEDFrameSubframeInfo subframeInfo; // master
    /** Validity flag for the quality of the data file. */
    public boolean frame_is_valid = false;

    /**
     * The frame image is divided into 200x200 pixel subframes, with
     * a leftover frame at the end.  This is how many horizontal
     * subframes there are.
     */
    public int number_horiz_subframes;
    /**
     * The frame image is divided into 200x200 pixel subframes, with
     * a leftover frame at the end.  This is how many vertical
     * subframes there are.
     */
    public int number_vert_subframes;

    /** The image array for the subframes. */
    public DTEDFrameSubframe subframes[][];

    //////////////////
    // Administrative methods
    //////////////////

    /**
     * Simplest constructor.
     * @param filePath complete path to the DTED frame.
     */
    public DTEDFrame(String filePath) {
	this(filePath, null, null, false);
    }

    /**
     * Constructor with colortable and presentation information.
     * @param filePath complete path to the DTED frame.
     * @param cTable the colortable to use for the images.
     * @param info presentation parameters.
     */
    public DTEDFrame(String filePath, 
		     DTEDFrameColorTable cTable, 
		     DTEDFrameSubframeInfo info) {
	this(filePath, cTable, info, false);
    }

    /**
     * Constructor with colortable and presentation information.
     *
     * @param filePath complete path to the DTED frame.
     * @param readWholeFile If true, all of the elevation data will be
     * read at load time.  If false, elevation post data will be read
     * in per longitude column depending on the need.  False is
     * recommended for DTEd level 1 and 2.
     */
    public DTEDFrame(String filePath, 
		     boolean readWholeFile) {
	this(filePath, null, null, readWholeFile);
    }

    /**
     * Constructor with colortable and presentation information.
     * 
     * @param filePath complete path to the DTED frame.
     * @param cTable the colortable to use for the images.
     * @param info presentation parameters.
     * @param readWholeFile If true, all of the elevation data will be
     * read at load time.  If false, elevation post data will be read
     * in per longitude column depending on the need.  False is
     * recommended for DTED level 1 and 2.
     */
    public DTEDFrame(String filePath, 
		     DTEDFrameColorTable cTable, 
		     DTEDFrameSubframeInfo info,
		     boolean readWholeFile) {

	try {
 	    binFile = new BinaryBufferedFile(filePath);

	    read(binFile, readWholeFile);
	    if (readWholeFile) close(true);
	    else binFile.addClosable(this);

	} catch (FileNotFoundException e) {
	    Debug.error("DTEDFrame: file "+filePath+" not found");
	} catch (java.io.IOException e) {
	    Debug.error("DTEDFrame: File IO Error!\n" + e.toString());
	}

	colorTable = cTable;
	subframeInfo = info;
	path = filePath;
    }

    public void setColorTable(DTEDFrameColorTable c_Table) {
	colorTable = c_Table;
    }

    public DTEDFrameColorTable getColorTable() {
	return colorTable;
    }

    /**
     * Reads the DTED frame file.  Assumes that the File f is valid/exists. 
     *
     * @param binFile the binary buffere file opened on the DTED frame file
     * @param readWholeFile flag controlling whether all the row
     * data is read at this time.  Otherwise, the rows are read as
     * needed.
     */
    protected void read(BinaryFile binFile, boolean readWholeFile) {
	binFile.byteOrder(true); //boolean msbfirst
	dsi = new DTEDFrameDSI(binFile);
	uhl = new DTEDFrameUHL(binFile);
	//  Allocate just the columns now - we'll do the rows as needed...
	elevations = new short[uhl.num_lon_lines][];
	if (readWholeFile) read_data_records();
	frame_is_valid = true;
    }

    /**
     * This must get called to break a reference cycle that prevents
     * the garbage collection of frames.  
     */
    public void dispose() {
        //System.out.println("DTED Frame Disposed " + me);
        BinaryFile.removeClosable(this);
    }

//     public void finalize() {
//         System.out.println("DTED Frame Finalized!" + me);
//     }

    /**
     * Part of the Closable interface.  Closes the BinaryFile
     * pointer, because someone else needs another file open, and the
     * system needs a file pointer.  Sets the binFile variable to
     * null.
     */
    public boolean close(boolean done) {
	try {
	    binFile.close();
	    binFile = null;
	    return true;
	} catch (java.io.IOException e) {
	    Debug.error("DTEDFrame close(): File IO Error!\n" + e.toString());
	    return false;
	}
    }

    /**
     * If the BinaryBufferedFile was closed, this method attempts to
     * reopen it. 
     *
     * @return true if the opening was successful.
     */
    protected boolean reopen() {
	try {
 	    binFile = new BinaryBufferedFile(path);
// 	    binFile = new BinaryFile(path);
	    return true;
	} catch (FileNotFoundException e) {
	    Debug.error("DTEDFrame reopen(): file "+path+" not found");
	    return false;
	} catch (java.io.IOException e) {
	    Debug.error("DTEDFrame close(): File IO Error!\n" + e.toString());
	    return false;
	}
    }

    //////////////////
    // These functions can be called from the outside,
    // as queries about the data
    //////////////////

    /**
     * The elevation at the closest SW post to the given
     * lat/lon. This is just a go-to-the-closest-post solution.
     *
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     * @return elevation at lat/lon in meters.
     */
    public int elevationAt(float lat, float lon) {
       if (frame_is_valid == true) {
          if (lat >= dsi.sw_lat && lat <= dsi.ne_lat
	        && lon >= dsi.sw_lon && lon <= dsi.ne_lon) {
	    
	      // lat/lon_post_intervals are *10 too big -
	      // extra 0 in 36000 to counteract
	      int lat_index = Math.round((lat - dsi.sw_lat)*36000/uhl.lat_post_interval);
	      int lon_index = Math.round((lon - dsi.sw_lon)*36000/uhl.lon_post_interval);
	    
	      if (elevations[lon_index]==null) read_data_record(lon_index);
	    
	      return (int) elevations[lon_index][lat_index];
	  }
       }
       return -32767;  // Considered a null elevation value
    }

    /** 
     * Interpolated elevation at a given lat/lon - should be more
     * precise than elevationAt(), but that depends on the resolution
     * of the data.  
     * 
     * @param lat latitude in decimal degrees.
     * @param lon longitude in decimal degrees.
     * @return elevation at lat/lon in meters.
     */
    public int interpElevationAt(float lat, float lon) {
       if (frame_is_valid == true) {
          if (lat >= dsi.sw_lat && lat <= dsi.ne_lat
	     && lon >= dsi.sw_lon && lon <= dsi.ne_lon) {
	    
	    // lat/lon_post_intervals are *10 too big -
	    // extra 0 in 36000 to counteract
	    float lat_index = (lat - dsi.sw_lat)*36000F/uhl.lat_post_interval;
	    float lon_index = (lon - dsi.sw_lon)*36000F/uhl.lon_post_interval;
	    
	    int lflon_index = (int)Math.floor(lon_index);
	    int lclon_index = (int)Math.ceil(lon_index);
	    int lflat_index = (int)Math.floor(lat_index);
	    int lclat_index = (int)Math.ceil(lat_index);
	    
	    if (elevations[lflon_index]==null) read_data_record(lflon_index);
	    if (elevations[lclon_index]==null) read_data_record(lclon_index);
	    
	    //////////////////////////////////////////////////////
	    // Print out grid of 20x20 elevations with
	    // the "asked for" point being in the middle
// 	    System.out.println("***Elevation Map***");
// 	    for(int l = lclat_index + 5; l > lflat_index - 5; l--) {
// 		System.out.println();
// 		for(int k = lflon_index - 5; k < lclon_index + 5; k++) {
// 		    if (elevations[k]==null) read_data_record(k);
// 		    System.out.print(elevations[k][l] + " ");
// 		}
// 	    }
// 	    System.out.println();System.out.println();
	    //////////////////////////////////////////////////////
	    
	    int ul = elevations[lflon_index][lclat_index];
	    int ur = elevations[lclon_index][lclat_index];
	    int ll = elevations[lflon_index][lclat_index];
	    int lr = elevations[lclon_index][lclat_index];
	    
	    float answer = resolve_four_points(ul, ur, lr, ll, 
					       lat_index, lon_index);
	    return Math.round(answer);
	  }
       }
       return -32767;  // Considered a null elevation value
    }

    /** 
     * Return an index of ints representing the starting x, y and
     * ending x, y of elevation posts given a lat lon box.  It does
     * check to make sure that the upper lat is larger than the lower,
     * and left lon is less than the right.
     *
     * @param ullat upper latitude in decimal degrees.
     * @param ullon left longitude in decimal degrees.
     * @param lrlat lower latitude in decimal degrees.
     * @param lrlon right longitude in decimal degrees.
     * @return int[4] array of start x, start y, end x, and end y.
     */
    public int[] getIndexesFromLatLons(float ullat, float ullon, 
				       float lrlat, float lrlon) {
	float upper = ullat;
	float lower = lrlat;
	float right = lrlon;
	float left = ullon;
 
	// Since matrix indexes depend on these being in the right
	// order, we'll double check and flip values, just to make
	// sure lower is lower, and higher is higher.
	if (ullon > lrlon) {
	    right = ullon;
	    left = lrlon;
	}

	if (lrlat > ullat) {
	    upper = lrlat;
	    lower = ullat;
	}

	int[] ret = new int[4];
	float ullat_index = (upper - dsi.sw_lat)*36000F/uhl.lat_post_interval;
	float ullon_index = (left - dsi.sw_lon)*36000F/uhl.lon_post_interval;
	float lrlat_index = (lower - dsi.sw_lat)*36000F/uhl.lat_post_interval;
	float lrlon_index = (right - dsi.sw_lon)*36000F/uhl.lon_post_interval;
	
	ret[0] = (int)Math.round(ullon_index);
	ret[1] = (int)Math.round(lrlat_index);
	ret[2] = (int)Math.round(lrlon_index);
	ret[3] = (int)Math.round(ullat_index);

	if (ret[0] < 0) ret[0] = 0;
	if (ret[0] > uhl.num_lon_lines - 2) ret[0] = uhl.num_lon_lines - 2;
	if (ret[1] < 0) ret[1] = 0;
	if (ret[1] > uhl.num_lat_points - 2) ret[1] = uhl.num_lat_points - 2;
	if (ret[2] < 0) ret[2] = 0;
	if (ret[2] > uhl.num_lon_lines - 2) ret[2] = uhl.num_lon_lines - 2;
	if (ret[3] < 0) ret[3] = 0;
	if (ret[3] > uhl.num_lat_points - 2) ret[3] = uhl.num_lat_points - 2;
	return ret;

    }

    /**
     * Return a two dimensional array of posts between lat lons.
     *
     * @param ullat upper latitude in decimal degrees.
     * @param ullon left longitude in decimal degrees.
     * @param lrlat lower latitude in decimal degrees.
     * @param lrlon right longitude in decimal degrees.
     * @return array of elevations in meters.  The spacing of the
     * posts depends on the DTED level.  
     */
    public short[][] getElevations(float ullat, float ullon, float lrlat, float lrlon) {
	int[] indexes = getIndexesFromLatLons(ullat, ullon, lrlat, lrlon);
	return getElevations(indexes[0], indexes[1], indexes[2], indexes[3]);
    }

    /**  
     * Return a two dimensional array of posts between lat lons.
     * Assumes that the indexes are checked to not exceed their bounds
     * as defined in the file.  getIndexesFromLatLons() checks this.
     *
     * @param startx starting index (left) of the greater matrix to
     * make the left side of the returned matrix.
     * @param starty starting index (lower) of the greater matrix to
     * make the bottom side of the returned matrix.
     * @param endx ending index (right) of the greater matrix to make
     * the left side of the returned matrix.
     * @param endy ending index (top) of the greater matrix to make
     * the top side of the returned matrix.
     * @return array of elevations in meters.  The spacing of the
     * posts depends on the DTED level.  
     */
    public short[][] getElevations(int startx, int starty, int endx, int endy) {
	int upper = endy;
	int lower = starty;
	int right = endx;
	int left = startx;
 
	// Since matrix indexes depend on these being in the right
	// order, we'll double check and flip values, just to make
	// sure lower is lower, and higher is higher.
	if (startx > endx) {
	    right = startx;
	    left = endx;
	}

	if (starty > endy) {
	    upper = starty;
	    lower = endy;
	}

	short[][] matrix = new short[right - left + 1][upper - lower + 1];
	int matrixColumn = 0;
	for (int x = left; x <= right; x++) {
	    if (elevations[x]==null) read_data_record(x);
	    System.arraycopy(elevations[x], lower, 
			     matrix[matrixColumn], 0, 
			     (upper - lower + 1));
	    matrixColumn++;
	}
	return matrix;
    }
  
    //////////////////
    // Internal methods
    //////////////////

    /**
     * A try at interoplating the corners of the surrounding posts,
     * given a lat lon.  Called from a function where the data for
     * the lon has been read in.
     */
    private float resolve_four_points(int ul, int ur, int lr, int ll,
 				      float lat_index, float lon_index) {
        float top_avg = 
	  ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * 
	   (float)(ur - ul)) + ul;
	float bottom_avg = 
	  ((lon_index - new Double(Math.floor(lon_index)).floatValue()) * 
	   (float)(lr - ll)) + ll;
	float right_avg = 
	  ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * 
	   (float)(ur - lr)) + lr;
	float left_avg = 
	  ((lat_index - new Double(Math.floor(lat_index)).floatValue()) * 
	   (float)(ul - ll))/100.0F + ll;
	
	float lon_avg = 
	  ((lat_index - new Double(Math.floor(lat_index)).floatValue()) *
	   (top_avg - bottom_avg)) + bottom_avg;
	float lat_avg = 
	  ((lon_index - new Double(Math.floor(lon_index)).floatValue()) *
	   (right_avg - left_avg)) + left_avg;
	
	float result = (lon_avg + lat_avg) / 2.0F;
	return result;
    }

    /** 
     * Reads one longitude line of posts. Assumes that the binFile is valid.
     *
     * @return true if the column of data was successfully read
     */
    protected boolean read_data_record(int lon_index) {
	try {
	    if (binFile == null) 
		if (!reopen()) return false;

	    // Set to beginning of file section, then skip to index data
	    // 12 = 1+3+2+2+4 = counts and checksum
	    // 2*uhl....size of elevation post space
	    binFile.seek(UHL_SIZE + DSI_SIZE + ACC_SIZE + 
			 (lon_index*(12+(2*uhl.num_lat_points))));
	    int sent = binFile.read();
	    binFile.skipBytes(3);  // 3 byte data_block_count
	    short lon_count = binFile.readShort();
	    short lat_count = binFile.readShort();
	    // Allocate the rows of the row
	    elevations[lon_index] = new short[uhl.num_lat_points];
	    for (int j=0; j<uhl.num_lat_points; j++)
		elevations[lon_index][j] = binFile.readShort();

	} catch(IOException e3) {
	    Debug.error("DTEDFrame.RDR: Error reading file.");
	    e3.printStackTrace();
	    elevations[lon_index] = null;
            return false;
	} catch (FormatException f) {
	    Debug.error("DTEDFrame.RDR: File IO Format error!");
	    elevations[lon_index] = null;
            return false;
	}
	return true;
    }
    
    /** 
     * Read all the elevation posts, at one time. Assumes that the
     * file is open and ready.
     * 
     * @return true if the elevation columns were read.
     */
    protected boolean read_data_records() {
	boolean ret = true;
	for (int lon_index = 0; lon_index < uhl.num_lon_lines; lon_index++) {
	    if (read_data_record(lon_index) == false) 
		ret = false;
	}
	return ret;
    }

    /** 
     * Sets the subframe array.  Blows away any images that may
     * already be there. 
     */
    public void initSubframes(int numHorizSubframes, int numVertSubframes) {
	number_horiz_subframes = numHorizSubframes;
	number_vert_subframes = numVertSubframes;
	subframes = new DTEDFrameSubframe[numHorizSubframes][numVertSubframes];
	if (Debug.debugging("dted")) {
	    Debug.output("///////// DTEDFrame: subframe array initialized, " +
			 numHorizSubframes + "x" + numVertSubframes);
	}
    }

    /**
     * If you just want to get an image for the DTEDFrame, then call
     * this.  One OMRaster for the entire DTEDFrame will be returned,
     * with the default rendering parameters (Colored shading) and the
     * default colortable.  Use the other getOMRaster method if you
     * want something different.  This method actually calls that
     * other method, so read the documentation for that as well.
     *
     * @param proj EqualArc projection to use to create image.  
     * @return raster image to display in OpenMap.  
     */
    public OMRaster getOMRaster(EqualArc proj) {
	return getOMRaster(null, null, proj);
    }

    /**
     * If you just want to get an image for the DTEDFrame, then call
     * this.  One OMRaster for the entire DTEDFrame will be returned.
     * In the DTEDFrameSubframeInfo, you need to set the color type
     * and all the parameters that are assiociated with the rendering
     * parameters.  The projection parameters of the DFSI (image
     * height, width, pixel intervals) will be set in this method
     * based on the projection.  If you want a different sized image,
     * scale the thing you get back from this method, or change the
     * scale of the projection that is passed in.  Calling this method
     * will cause the DTEDFrame subframe cache to reset itself to hold
     * one subframe covering the entire frame.  Just so you know.
     *
     * @param dfsi the DTEDFrameSubframeInfo describing the subframe.
     * @param colortable the colortable to use when building the image.
     * @return raster image to display in OpenMap.  
     * @param proj EqualArc projection to use to create image.  
     */
    public OMRaster getOMRaster(DTEDFrameSubframeInfo dfsi, 
				DTEDFrameColorTable colortable,
				EqualArc proj) {
	if (proj == null) {
	    Debug.error("DTEDFrame.getOMRaster: need projection to create image.");
	    return null;
	}

	if (colortable == null) {
	    colortable = new DTEDFrameColorTable();
	}

	if (dfsi == null) {
	    dfsi = new DTEDFrameSubframeInfo(DTEDFrameSubframe.COLOREDSHADING,
					     DTEDFrameSubframe.DEFAULT_BANDHEIGHT,
					     DTEDFrameSubframe.LEVEL_1, // Doesn't matter
					     DTEDFrameSubframe.DEFAULT_SLOPE_ADJUST);
	}

	dfsi.xPixInterval = 360/proj.getXPixConstant(); //degrees/pixel
	dfsi.yPixInterval = 90/proj.getYPixConstant();
	dfsi.height = (int)(1/dfsi.yPixInterval);
	dfsi.width = (int)(1/dfsi.xPixInterval);

	// Will trigger the right thing in getSubframeOMRaster;
	subframes = null;

	return getSubframeOMRaster(dfsi, colortable);
    }

    /** 
     * Return the subframe image as described in the
     * DTEDFrameSubframeInfo.  This is called by the DTEDCacheHandler,
     * which has in turn set the DTEDFrameSubframeInfo parameters to
     * match the projection parameters.  This turns out to be kinda
     * important.
     *
     * @param dfsi the DTEDFrameSubframeInfo describing the subframe.
     * @param colortable the colortable to use when building the image.
     * @return raster image to display in OpenMap.
     */
    public OMRaster getSubframeOMRaster(DTEDFrameSubframeInfo dfsi, 
					DTEDFrameColorTable colortable) {
	if (!frame_is_valid) return null;
	
	OMRaster raster = null;

	if (dfsi.viewType == DTEDFrameSubframe.NOSHADING) return null;

	if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING)
	    colortable.setGreyScale(false);
	else colortable.setGreyScale(true);

	float lat_origin = dfsi.lat;
	float lon_origin = dfsi.lon;

	if (subframes == null) {
	    // Need to set a couple of things up if the DTEDFrameCache
	    // isn't being used to set up the subframe information in
	    // the dfsi.
	    initSubframes(1, 1);
	    // NOTE!! The algorithm uses the cordinates of the top
	    // left corner as a reference!!!!!!!!
	    lat_origin = dsi.lat_origin + 1;
	    lon_origin = dsi.lon_origin;
	}

	DTEDFrameSubframe subframe = subframes[dfsi.subx][dfsi.suby];

	if (Debug.debugging("dteddetail")) {
	    Debug.output("Subframe lat/lon => lat= " + lat_origin +
			 " vs. " + dfsi.lat + 
			 " lon= " + lon_origin + " vs. " + dfsi.lon +
			 " subx = " + dfsi.subx + " suby = " + dfsi.suby);
	    Debug.output("Height/width of subframe => height= " + dfsi.height +
			 " width= " + dfsi.width);
	}

	if (subframe != null) {

	    // The subframe section will not be null, because it is
	    // created when the subframe is.
	    if (subframe.image != null && subframe.si.equals(dfsi)) {
		// IF there is an image in the cache and the drawing
		// parameters are correct
		raster = subframe.image;
		
		if (Debug.debugging("dted")) 
		    Debug.output( "######## DTEDFrame: returning cached subframe");
		return raster;
	    }

	    if (Debug.debugging("dted")) {
		Debug.output( "   *** DTEDFrame: changing image of cached subframe");
	    }
	    
	    if (subframe.image == null) {
		if (Debug.debugging("dted")) {
		    Debug.output( "   +++ DTEDFrame: creating subframe image");
		}
		if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
		    subframe.image = new OMRaster(lat_origin, lon_origin, 
						  dfsi.width, dfsi.height,
						  new int[dfsi.height*dfsi.width]);
		else subframe.image = new OMRaster(lat_origin, lon_origin, 
						   dfsi.width, dfsi.height,
						   null, colortable.colors, 255);
	    }

	    //  If there is an image, the types are different and it needs to be
	    //  redrawn
	    subframe.si = dfsi.makeClone();

	} else {

	    if (Debug.debugging("dted")) {
		Debug.output( "   +++ DTEDFrame: creating subframe");
	    }

	    subframes[dfsi.subx][dfsi.suby] = new DTEDFrameSubframe(dfsi);
	    subframe = subframes[dfsi.subx][dfsi.suby];
	    if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT) {
		subframe.image = new OMRaster(lat_origin, lon_origin, 
					      dfsi.width, dfsi.height, 
					      new int[dfsi.height*dfsi.width]);
	    } else {
		subframe.image = new OMRaster(lat_origin, lon_origin, 
					      dfsi.width, dfsi.height, 
					      null, colortable.colors, 255);
	    }
	}

	raster = subframe.image;

	// lat/lon_post_intervals are *10 too big -  // extra 0 in 36000 to counteract
	// start in lower left of subframe 
	double start_lat_index = (lat_origin-(double)dsi.sw_lat)*36000.0/(double)uhl.lat_post_interval;
	double start_lon_index = (lon_origin-(double)dsi.sw_lon)*36000.0/(double)uhl.lon_post_interval;
	double end_lat_index = ((lat_origin-((double)dfsi.height*dfsi.yPixInterval)) -(double)dsi.sw_lat)*36000.0/(double)uhl.lat_post_interval;
	double end_lon_index = ((lon_origin+((double)dfsi.width*dfsi.xPixInterval))	-(double)dsi.sw_lon)*36000.0/(double)uhl.lon_post_interval;
	double lat_interval = (start_lat_index-end_lat_index)/(double)dfsi.height;
	double lon_interval = (end_lon_index-start_lon_index)/(double)dfsi.width;
	
	
	if (Debug.debugging("dteddetail")) 
	    Debug.output("  start_lat_index => " + start_lat_index + "\n" +
			 "  end_lat_index => " + end_lat_index + "\n" +
			 "  start_lon_index => " + start_lon_index + "\n" + 
			 "  end_lon_index => " + end_lon_index + "\n" +
			 "  lat_interval => " + lat_interval + "\n" +
			 "  lon_interval => " + lon_interval);
	
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
	double modifier = (double)0;
	double xw_offset = 0;
	double xe_offset = 0;
	double yn_offset = 0;
	double ys_offset = 0;
	int elevation = (int)0;
	
	//  Calculations needed once for slope shading
	if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING || 
	   (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && 
	    colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {
	    // to get to the right part of the frame, kind of like a subframe
	    // indexing thing
	    xw_offset = start_lon_index - Math.ceil(lon_interval);
	    xe_offset = start_lon_index + Math.ceil(lon_interval);
	    yn_offset = start_lat_index + Math.ceil(lat_interval);
	    ys_offset = start_lat_index - Math.ceil(lat_interval);
	    
	    switch (dfsi.dtedLevel) {
		//larger numbers make less contrast
	    case 0: modifier = (double) 4; break;//1000 ideal
	    case 1: modifier = (double) .02; break;//2 ideal
	    case 2: modifier = (double) .0001; break;
	    case 3: modifier = (double) .000001; break; 
	    default: modifier = (double) 1;
	    }
	    // With more colors, contrast tends to be a little light for the
	    // default - brighten it up more
	    if (colortable.colors.length > 215) modifier = modifier/10;
	    
	    for(int h=dfsi.slopeAdjust; h<5; h++) modifier = modifier*10;
	    distance = Math.sqrt((modifier*lon_interval*lon_interval)+
				 (modifier*lat_interval*lat_interval));
	}
	for (short x = 0; x < dfsi.width; x++) {
	    
	    //used for both elevation banding and slope
	    xc = (short)(start_lon_index + ((x)*lon_interval));
	    if (xc < 0) xc = 0;
	    if (xc > dsi.num_lon_points-1) xc = (short)(dsi.num_lon_points-1);
	    
	    if ((elevations[xc] == null) && !read_data_record(xc)) {
		Debug.error("DTEDFrame: Problem reading lat point line in data record");
		return null;
	    }
	    if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING || 
	       (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && 
		colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {
		//  This is actually finding the right x post for this pixel,
		//  within the subframe measurements.
		xnw = (short)(xw_offset + Math.floor(x*lon_interval));
		xse = (short)(xe_offset + Math.floor(x*lon_interval));
		
		// trying to smooth out the edge of the frame
		if (xc == 0 || xnw < 0) {
		    xnw = xc;
		    xse = (short)(xnw + 2.0*Math.ceil(lon_interval));
		}
		if (xc == dsi.num_lon_points-1 ||  xse > dsi.num_lon_points - 1) {
		    xse = (short)(dsi.num_lon_points - 1);
		    xnw = (short)(xse - 2.0*Math.ceil(lon_interval));
		}
		
		if (((elevations[xnw] == null) && !read_data_record(xnw)) ||
		    ((elevations[xse] == null) && !read_data_record(xse))) {
		    Debug.error("DTEDFrame: Problem reading lat point line in data record");
		    return null;
		}
	    }
	    
	    // Now, calculate the data and assign the pixels based on y    
	    for (short y = 0; y < dfsi.height; y++) {
		
		// used for elevation band and slope
		yc = (short)(start_lat_index - ((y)*lat_interval));
		if (yc<0) yc = 0;
		elevation = (int)elevations[xc][yc];

		// elevation shading 
		if (dfsi.viewType == DTEDFrameSubframe.METERSHADING || 
		    dfsi.viewType == DTEDFrameSubframe.FEETSHADING) {
		    
		    // Just use the top two-thirds of the colors
		    if (elevation == 0) assignment = 0; // color water Blue
		    else{
			if (elevation < 0) elevation *= -1;  // Death Valley
			if (dfsi.viewType == DTEDFrameSubframe.FEETSHADING) 
			    elevation = (int)(elevation*3.2);
			// Start at the darkest color, and then go up through the
			// colormap for each band height, the start back at the
			// darkest when you get to the last color.  To make this
			// more useful, I limit the number of colors (10) used - if
			// there isn;t enough contrast between the colors, you can't
			// see the bands.  The contrast adjustment in 24-bit color
			// mode(216 colors) lets you add a few colors.
			if (colortable.colors.length < 216) {
			    try {
				assignment = (int)((elevation/dfsi.bandHeight)%(colortable.colors.length - 6) + 6);
			    } catch (java.lang.ArithmeticException ae) {
				assignment = 1;
			    }
			} else {
			    try {
				assignment = (int)(((elevation/dfsi.bandHeight)%(10-2*(3-dfsi.slopeAdjust))*(colortable.colors.length/(10-2*(3-dfsi.slopeAdjust)))) + 6);
			    } catch (java.lang.ArithmeticException ae) {
				assignment = 1;
			    }
			}
		    }
		    if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
			raster.setPixel(x, y, colortable.colors[assignment].getRGB());
		    else raster.setByte(x, y, (byte)assignment);
		}
		
		// Slope shading 
		else if (dfsi.viewType == DTEDFrameSubframe.SLOPESHADING || 
			(dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING && 
			 colortable.colors.length > DTEDFrameColorTable.NUM_ELEVATION_COLORS)) {
		    
		    // find the y post indexes within the subframe
		    ynw = (short)(yn_offset - Math.floor(y*lat_interval));
		    yse = (short)(ys_offset - Math.floor(y*lat_interval));
		    
		    //  trying to smooth out the edge of the frame by handling the
		    //  frame limits
		    if (yse < 0) yse = 0;
		    if (yc == dsi.num_lat_lines-1 || ynw > dsi.num_lat_lines-1)
			ynw = (short)(dsi.num_lat_lines-1);
		    
		    e2 = elevations[xse][yse];  // down & right elevation
		    e1 = elevations[xnw][ynw];  // up and left elevation
		    
		    slope = (e2 - e1)/distance; // slope relative to nw sun
		    // colormap value darker for negative slopes, brighter for
		    // positive slopes
		    
		    if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING) {
			assignment = 1;
			elevation = (int)(elevation*3.2);// feet
			for (int l=1; l < DTEDFrameColorTable.NUM_ELEVATION_COLORS; l++)
			    if (elevation <= colortable.elevation_color_cutoff[l]) {
				if (slope < 0) 
				    assignment = (int)(l + DTEDFrameColorTable.NUM_ELEVATION_COLORS);
				else if (slope > 0) 
				    assignment = (int)(l + (DTEDFrameColorTable.NUM_ELEVATION_COLORS*2));
				else assignment = (int) l;
				break;
			    }
			if (elevation == 0) assignment = 0;
			if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
			    raster.setPixel(x, y, colortable.colors[assignment].getRGB());
			else raster.setByte(x, y, (byte)assignment);
		    }
		    
		    else{
			value = (float)(((colortable.colors.length-1)/2) + slope);
			
			// not water, but close in the colormap - max dark
			if (slope != 0 && value < 1) value = 1;
			if (elevation == 0) value = 0; // water?!?
			if (value > (colortable.colors.length - 1)) 
			    value = colortable.colors.length - 1; // max bright
			
			assignment = (int) value;
			if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
			    raster.setPixel(x, y, colortable.colors[assignment].getRGB());
			else raster.setByte(x, y, (byte)assignment);

		    }
		}
		// Subframe outlines - different colors for each side of the frame
		// This is really for debugging purposes, really.
		else if (dfsi.viewType ==  DTEDFrameSubframe.BOUNDARYSHADING) {
		    int c;
		    if (x < 1) c = 1;
		    else if (x > dfsi.width - 2) c = 12;
		    else if (y < 1) c = 1;
		    else if (y > dfsi.height - 2) c = 12;
		    else c = 7;

		    if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
			raster.setPixel(x, y, colortable.colors[c].getRGB());
		    else raster.setByte(x, y, (byte)c);

		}
		else if (dfsi.viewType == DTEDFrameSubframe.COLOREDSHADING) {
		    assignment = 1;
		    elevation = (int)(elevation*3.2);// feet
		    for (int l=1; l < DTEDFrameColorTable.NUM_ELEVATION_COLORS; l++)
			if (elevation <= colortable.elevation_color_cutoff[l]) {
			    assignment = (int)l;
			    break;
			}
		    
		    if (elevation == 0) assignment = 0;
		    if (elevation < 0) assignment = 1;
		    if (elevation > 33000) assignment = 1;

		    if (dfsi.colorModel == OMRasterObject.COLORMODEL_DIRECT)
			raster.setPixel(x, y, colortable.colors[assignment].getRGB());
		    else raster.setByte(x, y, (byte)assignment);
		}
	    }
	}
	
	if (Debug.debugging("dteddetail")) Debug.output("DTEDFrame: leaving raster");
	return raster;
	
    }

    public static void main(String args[]) {
	Debug.init();
	if (args.length < 1) {
            System.out.println( "DTEDFrame:  Need a path/filename");
            System.exit(0);
        }
	
        System.out.println( "DTEDFrame: " + args[0]);
        DTEDFrame df = new DTEDFrame(args[0]);
	if (df.frame_is_valid) {
	    System.out.println(df.uhl);
	    System.out.println(df.dsi);
        
// 	    int startx = 5;
// 	    int starty = 6;
// 	    int endx = 10;
// 	    int endy = 30;

// 	    short[][] e = df.getElevations(startx, starty, endx, endy);
// 	    for (int i = e[0].length-1; i >= 0; i--) {
// 		for (int j = 0; j < e.length; j++) {
// 		    System.out.print(e[j][i] + " ");
// 		}
// 		System.out.println("");
// 	    }
	}	
	float lat = df.dsi.lat_origin + .5f;
	float lon = df.dsi.lon_origin + .5f;

	CADRG crg = new CADRG(new com.bbn.openmap.LatLonPoint(lat, lon), 
			      1500000, 600, 600);
       	final OMRaster ras = df.getOMRaster(crg);

	// Pushes the image to the left top of the frame.
	crg.setHeight(ras.getHeight());
	crg.setWidth(ras.getWidth());

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
		
	window.setSize(ras.getWidth(), ras.getHeight());
	window.show();
	window.repaint();
    }
}



