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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/etopo/ETOPOJarLayer.java,v $
// $RCSfile: ETOPOJarLayer.java,v $
// $Revision: 1.8 $
// $Date: 2008/02/27 01:57:17 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.etopo;

/**
 * Creation date: (1/12/2001 9:41:59 PM)
 * @author John Watts from nextjet.com: 
 */
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * This subclass of ETOPOLayer reads data from jar files and * interpolates
 * elevations to provide more smooth color transitions using * low resolution
 * data.
 */
public class ETOPOJarLayer extends ETOPOLayer {

	/**
	 * ETOPOJarLayer constructor comment.
	 */
	public ETOPOJarLayer() {
		super();
	}

	/**
	 * ETOPOJarLayer constructor comment.
	 * 
	 * @param pathToETOPODir
	 *            java.lang.String
	 */
	public ETOPOJarLayer(String pathToETOPODir) {
		super(pathToETOPODir);
	}

	/**
	 * Loads the database from the appropriate file based on the current
	 * resolution. The data files are in INTEL format (must call
	 * BinaryBufferedFile.byteOrder(true)).
	 */
	protected void loadBuffer() {

		// get the resolution index
		int resIdx = minuteSpacing / 5 - 1;
		if (resIdx < 0)
			resIdx = 0;
		else if (resIdx > 2)
			resIdx = 2;

		// build file name
		String fileName = path + etopoFileNames[resIdx];

		try {

			// open etopo file as resource stream
			BufferedInputStream bis = new BufferedInputStream(ClassLoader
					.getSystemResourceAsStream(fileName));

			// set width/height
			bufferWidth = etopoWidths[resIdx];
			bufferHeight = etopoHeights[resIdx];

			int spacer = 1;
			// don't know why I have to do this, but there seems to be
			// a wrapping thing going on with different data sets.
			switch (minuteSpacing) {
			case (2):
				spacer = 1 + this.spacer;
				break;
			case (5):
				spacer = 0 + this.spacer;
				break;
			default:
				spacer = 1 + this.spacer;
			}

			int numberOfWords = (bufferWidth + spacer) * bufferHeight;

			// allocate storage
			dataBuffer = new short[numberOfWords];

			// read data
			iv_buffer = new byte[2 * numberOfWords];

			iv_bytesinbuffer = bis.read(iv_buffer, 0, 2 * numberOfWords);
			iv_curptr = 0;

			for (int i = 0; i < bufferWidth * bufferHeight; i++)
				dataBuffer[i] = readShort();

			// done
			bis.close();

			// don't know why I have to do this, but...
			bufferWidth += spacer;

		} catch (FileNotFoundException e) {
			Debug.error("ETOPOLayer loadBuffer(): file " + fileName
					+ " not found");
		} catch (IOException e) {
			Debug.error("ETOPOLayer loadBuffer(): File IO Error!\n"
					+ e.toString());
		}

	}

	byte iv_buffer[];
	int iv_bytesinbuffer;
	int iv_curptr;

	/**
	 * Reads and returns a short
	 * 
	 * @return the 2 bytes merged into a short, according to the current byte
	 *         ordering
	 * @exception EOFException
	 *                there were less than 2 bytes left in the file
	 */
	public short readShort() throws EOFException {
		// MSBFirst must be set when we are called
		if (iv_bytesinbuffer < 2) {
			throw new EOFException();
		}
		iv_curptr += 2;
		iv_bytesinbuffer -= 2;
		return MoreMath.BuildShort(iv_buffer, iv_curptr - 2, true);
	}

	/*
	 * Builds the raster image that has the dimensions of the current
	 * projection. The algorithm is is follows:
	 * 
	 * allocate storage the size of the projection (use ints for RGBA)
	 * 
	 * for each screen point
	 * 
	 * inverse project screen point to get lat/lon (world coords) get altitude
	 * and/or slope at the world coord compute (lookup) color at the world coord
	 * set color value into screen coord location
	 * 
	 * end
	 * 
	 * create OMRaster from the int array data.
	 * 
	 * The code contains a HACK (primarily for the Orthographic projection)
	 * since * x/y values which would return an "Outer Space" value actually
	 * return lat/lon values for the center of the projection (see
	 * Orthographic.inverse(...)). This resulted in the "Outer Space" being
	 * painted the color of whatever the center lat/lon was. The HACK turns any
	 * center lat/lon value into black. Of course, this causes valid center
	 * lat/lon values to be painted black, but the trade off is worth it
	 * visually. The appropriate method may be to have Projection.inverse and
	 * its variants raise an exception for "Outer Space" values.
	 */
	protected OMRaster buildRaster() {
		// initialize the return
		OMRaster ret = null;
		Projection projection = getProjection();
		// work with the slopeMap
		if (slopeMap != null) {

			// compute our deltas
			int pixelColumns = projection.getWidth();
			int pixelRows = projection.getHeight();

			// create int array to hold colors
			int[] colors = new int[pixelColumns * pixelRows];

			// compute scalers for lat/lon indicies
			float yPixPerDataPt = (float) bufferHeight / 180F;
			float xPixPerDataPt = (float) bufferWidth / 360F;

			// starting and ending indices
			int sx = 0, sy = 0, ex = pixelColumns, ey = pixelRows;

			// handle CADRG
			if (projection instanceof CADRG) {

				// get corners
				Point2D ul = projection.getUpperLeft();
				Point2D lr = projection.getLowerRight();

				// set start/end indicies
				Point2D ulp = projection.forward(ul);
				Point2D lrp = projection.forward(lr);
				sx = (int) ulp.getX();
				ex = (int) lrp.getX();
				sy = (int) ulp.getY();
				ey = (int) lrp.getY();

			}

			// get the center lat/lon (used by the HACK, see above in
			// method description)
			Point2D center = projection.getCenter();
			LatLonPoint llp = new LatLonPoint.Double();

			// build array
			float lat;
			float lon;
			int lat_idx;
			int lon_idx;
			float latWt;
			float lonWt;

			// offset
			int ofs;
			int ofsRight;
			int ofsDown;
			int ofsDownRight;

			for (int y = sy; y < ey; y++) {

				// process each column
				for (int x = sx; x < ex; x++) {

					// inverse project x,y to lon,lat
					projection.inverse(x, y, llp);

					// get point values
					lat = llp.getLatitude();
					lon = llp.getLongitude();

					// check
					if (lon < 0.) {
						lon += 360.;
					}

					// find indicies
					lat_idx = (int) ((90. - lat) * yPixPerDataPt);
					lon_idx = (int) (lon * xPixPerDataPt);

					// most pixels fall between data points. The data
					// point originally used is the one immediately
					// above and to the left of the pixel. The amount
					// by which the pixel is offset from the data
					// point can be used to weight the elevation
					// contribution of the four data points
					// surrounding the pixel ie. the weights. The
					// truncated decimal part of the index computation
					// is the weight.
					latWt = ((90f - lat) * yPixPerDataPt) - (float) lat_idx;
					lonWt = (lon * xPixPerDataPt) - (float) lon_idx;

					// offsets of the four surrounding data points.
					ofs = lon_idx + lat_idx * bufferWidth;
					ofsRight = ofs + 1;
					if (lat_idx + 1 < bufferHeight) {
						ofsDown = lon_idx + (1 + lat_idx) * bufferWidth;
					} else {
						ofsDown = ofs;
					}
					ofsDownRight = ofsDown + 1;

					// make a color
					int idx = 0;
					int gray = 0;
					short el = 0;
					byte sl = 0;

					try {

						try {
							float ulwt = (1f - lonWt + 1f - latWt);
							float urwt = (lonWt + 1f - latWt);
							float llwt = (1f - lonWt + latWt);
							float lrwt = (lonWt + latWt);
							// get elevation
							el = (short) ((float) dataBuffer[ofs] * ulwt
									+ (float) dataBuffer[ofsRight] * urwt
									+ (float) dataBuffer[ofsDown] * llwt + (float) dataBuffer[ofsDownRight]
									* lrwt);

							// slope
							sl = (byte) ((float) slopeMap[ofs] * ulwt
									+ (float) slopeMap[ofsRight] * urwt
									+ (float) slopeMap[ofsDown] * llwt + (float) slopeMap[ofsDownRight]
									* lrwt);
							float exagFactor = 1f / (el > 0 ? 1.5f : 3f);
							el = (short) ((float) el * exagFactor);
							sl = (byte) ((float) sl * exagFactor);

							// bad index
						} catch (ArrayIndexOutOfBoundsException e) {
							Debug.error(e.toString() + ":" + ofs + " limit="
									+ dataBuffer.length);
						}
						// our index
						idx = y * pixelColumns + x;

						// create a color
						Color pix = null;
						if (viewType == SLOPESHADING) {
							// HACK (see method description above)
							if ((llp.getLatitude() == center.getY())
									&& (llp.getLongitude() == center.getX()))
								gray = 0;
							else
								gray = 127 + sl;
							pix = new Color(gray, gray, gray, opaqueness);
						} else if (viewType == COLOREDSHADING) {
							// HACK (see method description above)
							if ((llp.getLatitude() == center.getY())
									&& (llp.getLongitude() == center.getX()))
								pix = new Color(0, 0, 0, opaqueness);
							else
								pix = getColor(el, sl);
						}

						// set
						if (pix != null) {
							colors[idx] = pix.getRGB();
						}

					}

					// tried to set a bad color level
					catch (IllegalArgumentException e) {
						Debug.error(e.toString() + ":" + gray);
					}

					// bad index
					catch (ArrayIndexOutOfBoundsException e) {
						Debug.error(e.toString() + ":" + idx);
					}
				}
			}

			// create the raster
			ret = new OMRaster(0, 0, pixelColumns, pixelRows, colors);

		}

		// return or raster
		return ret;

	}

	protected Color getColor(short elevation, byte slopeVal) {
		// build first time
		if (slopeColors == null) {// || slopeColors[0][0].getAlpha() !=
									// opaqueness) {
			System.out.println("loading colors");
			// allocate storage for elevation bands, 8 slope bands
			slopeColors = new Color[elevLimitCnt][8];

			// process each elevation band
			for (int i = 0; i < elevLimitCnt; i++) {

				// get base color (0 slope color)
				Color base = new Color(redElev[i], greenElev[i], blueElev[i],
						opaqueness);

				// call the "brighter" method on the base color for
				// positive slope
				for (int j = 4; j < 8; j++) {

					// set
					if (j == 4)
						slopeColors[i][j] = base;
					else
						slopeColors[i][j] = slopeColors[i][j - 1].brighter();
				}

				// call the "darker" method on the base color for negative
				// slopes
				for (int k = 3; k >= 0; k--) {
					// set
					slopeColors[i][k] = slopeColors[i][k + 1].darker();
				}
			}
		}

		// get the elevation band index
		int elIdx = getElevIndex(elevation);

		// compute slope idx
		int slopeIdx = ((int) slopeVal + 127) >> 5;
		// int slopeIdx = ((int)slopeVal+127)/32;

		// return color
		return slopeColors[elIdx][slopeIdx];
		// Color norm = slopeColors[elIdx][slopeIdx];

		// set alpha
		// return new
		// Color(norm.getRed(),norm.getGreen(),norm.getBlue(),opaqueness);

	}
}
