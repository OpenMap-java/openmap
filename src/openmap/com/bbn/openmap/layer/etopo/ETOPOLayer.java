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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/etopo/ETOPOLayer.java,v $
// $RCSfile: ETOPOLayer.java,v $
// $Revision: 1.11 $
// $Date: 2008/02/27 01:57:17 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.etopo;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.ListResetPCPolicy;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * ETOPOLayer extends Layer to provide rendering of the ETOPO5 world elevation
 * data set. The ETOPO5 data consists of terrain altitude and ocean depth
 * measurements at 5 minute intervals for the entire globe. Rendering is allowed
 * in any projection that implements the inverse(int,int) method. Two types of
 * rendering are provided: grayscale slope-shaded and colored slope-shaded.
 * <p>
 * The distribution consists of the following:
 * <ul>
 * <li>1. ETOPOLayer.java</li>
 * <li>2. ETOPO5 (5 minute spacing data set, 4320x2160 shorts, ~18MB)</li>
 * <li>3. ETOPO10 (10 minute spacing data set, sampled from ETOPO5, ~4.6MB)</li>
 * <li>4. ETOPO15 (15 minute spacing data set, sampled from ETOPO5, ~2MB)</li>
 * <li>5. ETOPOLayer.properties (example properties for openmap.properties)</li>
 * </ul>
 * <p>
 * The sampled ETOPO data sets are provided to speed up the loading of data to
 * compute the slope shading. The algorithm inverse projects the x/y screen
 * coords (for the entire projection screen space) to get the corresponding
 * lat/lon coords then samples the database to get altitude/depth and slope
 * values. While this method is slower than the forward projection method, it
 * does provide a more attractive screen presentation and will support all
 * projections (not just the equidistant cylindrical). A palette provides the
 * ability to choose between the 5,10, or 15 minute resolutions, as well as
 * color or grayscale selection, transparency, and slope contrast.
 * <p>
 * The ETOPOLayer also relies on properties to set its variables, such as the
 * etopo frame paths (there can be several at a time), the opaqueness of the
 * frame images, number of colors to use, and some other display variables. The
 * ETOPOLayer properties look something like this:
 * <P>
 * 
 * #------------------------------ <BR>
 * # Properties for ETOPOLayer <BR>
 * #------------------------------ <BR>
 * # This property should reflect the paths to the etopo directory <BR>
 * etopo.path=c:/openmap/share <BR>
 * <BR>
 * # Number between 0-255: 0 is transparent, 255 is opaque <BR>
 * etopo.opaque=255 <BR>
 * <BR>
 * # Number of colors to use on the maps - 16, 32, 216 <BR>
 * etopo.number.colors=216 <BR>
 * <BR>
 * # Type of display for the data <BR>
 * # 0 = grayscale slope shading <BR>
 * # 1 = colored slope shading <BR>
 * etopo.view.type=1 <BR>
 * <BR>
 * # Contrast setting, 1-5 <BR>
 * etopo.contrast=3 <BR>
 * <BR>
 * # lat/lon spacing in minutes <BR>
 * # must be 5, 10, or 15 <BR>
 * etopo.minute.spacing=10 <BR>
 * <BR>
 * #------------------------------------- <BR>
 * # End of properties for ETOPOLayer <BR>
 * #------------------------------------- <BR>
 * 
 */
public class ETOPOLayer extends OMGraphicHandlerLayer implements ActionListener {

	/** Gray scale slope shading, sun from the Northwest. */
	public static final int SLOPESHADING = 0;

	/**
	 * Colorized slope shading. Color bands are based on elevation, and are
	 * accented by shaded indications.
	 */
	public static final int COLOREDSHADING = 1;

	/** Default contrast setting for slope shading. */
	public static final int DEFAULT_SLOPE_ADJUST = 3;

	/** Default minute spacing */
	public static final int DEFAULT_MINUTE_SPACING = 10;

	/** for colorizing */
	public final static int DEFAULT_OPAQUENESS = 255;

	/**
	 * The paths to the ETOPO directory, telling where the data is.
	 */
	protected String path;

	/** The etopo elevation data */
	protected short[] dataBuffer = null;
	protected int bufferWidth;
	protected int bufferHeight;

	/** The current resolution (in minutes) */
	protected int minuteSpacing;

	/** ETOPO elevation files */
	protected final static String[] etopoFileNames = { "/ETOPO2", "/ETOPO5",
			"/ETOPO10", "/ETOPO15" }; // ep-g

	/** dimensions of the ETOPO files (don't mess with these!) */
	protected final static int[] etopoWidths = { 10800, 4320, 2160, 1440 };// ep-g
	protected final static int[] etopoHeights = { 5400, 2160, 1080, 720 }; // ep-g

	/**
	 * Spacings (in meters) between adjacent lon points at the equator. The
	 * values here were aesthetically defined (they are not the actual spacings)
	 */
	protected double[] etopoSpacings = { 1800., 3500., 7000.,
			10500. }; // ep-g

	/**
	 * The display type for the etopo images. Slope shading is grayscale terrain
	 * modeling with highlights and shading, with the 'sun' being in the
	 * NorthWest. Colored Elevation shading is the same thing, except colors are
	 * added to indicate the elevation. Band shading colors the pixels according
	 * to a range of elevations.
	 */
	protected int viewType;

	/** The elevation range to use for each color in band shading. */
	protected int bandHeight;

	/** A contrast adjustment, for slope shading (1-5). */
	protected int slopeAdjust;

	/** transparency control */
	protected int opaqueness;

	/**
	 * Number of pixel spacers that should be added to a data file, per line, to
	 * adjust for skewing.
	 */
	protected int spacer = 0;

	/** property suffixes */
	public static final String ETOPOPathProperty = "path";
	public static final String OpaquenessProperty = "opaque";
	public static final String ETOPOViewTypeProperty = "view.type";
	public static final String ETOPOSlopeAdjustProperty = "contrast";
	public static final String ETOPOMinuteSpacingProperty = "minute.spacing";
	public static final String ETOPOPixelSpacerProperty = "spacer";

	/**
	 * Holds the slope values, updated when the resolution changes or the slope
	 * adjustment (contrast) is changed. Slope values are scaled between -127 to
	 * 127.
	 */
	protected byte[] slopeMap = null;

	/** elevation bands */
	protected int[] elevLimit = { -11000, -9000, -7000, -5000,
			-3000, -1500, 0, 250, 500, 750, 1000, 2000, 3500, 5000 };
	/** number of elevation bands */
	protected int elevLimitCnt = 14;

	/** elevation band colors (one for each elevation band) */
	protected int[] redElev = { 0, 0, 4, 20, 124, 130, 135, 117,
			252, 253, 229, 244, 252, 132 };
	protected int[] greenElev = { 2, 12, 51, 159, 235, 255, 235,
			255, 236, 162, 115, 50, 20, 132 };
	protected int[] blueElev = { 76, 145, 242, 249, 252, 255, 110,
			58, 29, 35, 5, 14, 46, 132 };

	/** for slope shading colors, indexed by elevation band then slope */
	protected Color[][] slopeColors = null;

	/* flag to recompute slope map */
	protected boolean slopeReset = true;

	/* flag to load new elevation file */
	protected boolean spacingReset = true;

	/**
	 * The default constructor for the Layer. All of the attributes are set to
	 * their default values.
	 */
	public ETOPOLayer() {
		this(null);
	}

	/**
	 * The default constructor for the Layer. All of the attributes are set to
	 * their default values.
	 * 
	 * @param pathToETOPODir
	 *            path to the directory holding the ETOPO data
	 */
	public ETOPOLayer(String pathToETOPODir) {
		setName("ETOPO");
		setDefaultValues();
		path = pathToETOPODir;
		setProjectionChangePolicy(new ListResetPCPolicy(this));
	}

	public void setPath(String pathToETOPODir) {
		path = pathToETOPODir;
	}

	protected void setDefaultValues() {
		// defaults
		path = null;
		dataBuffer = null;
		opaqueness = DEFAULT_OPAQUENESS;
		slopeAdjust = DEFAULT_SLOPE_ADJUST;
		viewType = COLOREDSHADING;
		minuteSpacing = DEFAULT_MINUTE_SPACING;
	}

	/* returns the color lookup index based on elevation */
	protected int getElevIndex(short el) {
		for (int i = 0; i < elevLimitCnt - 1; i++)
			if (el < elevLimit[i + 1])
				return i;
		return elevLimitCnt - 1;
	}

	/* returns a color based on slope and elevation */
	protected Color getColor(short elevation, byte slopeVal) {
		// build first time
		if (slopeColors == null) {

			// allocate storage for elevation bands, 8 slope bands
			slopeColors = new Color[elevLimitCnt][8];

			// process each elevation band
			for (int i = 0; i < elevLimitCnt; i++) {

				// get base color (0 slope color)
				Color base = new Color(redElev[i], greenElev[i], blueElev[i]);

				// call the "brighter" method on the base color for
				// positive slope
				for (int j = 4; j < 8; j++) {
					// set
					if (j == 4)
						slopeColors[i][j] = base;
					else
						slopeColors[i][j] = slopeColors[i][j - 1].brighter();
				}

				// call the "darker" method on the base color for
				// negative slopes
				for (int k = 3; k >= 0; k--) {
					slopeColors[i][k] = slopeColors[i][k + 1].darker();
				}
			}
		}

		// get the elevation band index
		int elIdx = getElevIndex(elevation);

		// compute slope idx
		int slopeIdx = ((int) slopeVal + 127) / 32;

		// return color
		Color norm = slopeColors[elIdx][slopeIdx];

		// set alpha
		return new Color(norm.getRed(), norm.getGreen(), norm.getBlue(),
				opaqueness);

	}

	/**
	 * Set all the ETOPO properties from a properties object.
	 */
	public void setProperties(String prefix, java.util.Properties properties) {

		super.setProperties(prefix, properties);

		prefix = PropUtils.getScopedPropertyPrefix(this);

		path = properties.getProperty(prefix + ETOPOPathProperty);

		opaqueness = PropUtils.intFromProperties(properties, prefix
				+ OpaquenessProperty, DEFAULT_OPAQUENESS);

		viewType = PropUtils.intFromProperties(properties, prefix
				+ ETOPOViewTypeProperty, COLOREDSHADING);

		slopeAdjust = PropUtils.intFromProperties(properties, prefix
				+ ETOPOSlopeAdjustProperty, DEFAULT_SLOPE_ADJUST);

		minuteSpacing = PropUtils.intFromProperties(properties, prefix
				+ ETOPOMinuteSpacingProperty, DEFAULT_MINUTE_SPACING);

		spacer = PropUtils.intFromProperties(properties, prefix
				+ ETOPOPixelSpacerProperty, spacer);

	}

	/**
	 * Builds the slope index map. This method is called when the ETOPO
	 * resolution changes and when the slope contrast changes. The slope of the
	 * terrain is clipped; slopes are between the range of +/- 45 deg. The
	 * calculated slope value is then linearly scaled to the range +/- 127.
	 */
	protected void buildSlopeMap() {
		// this should never happen, but...
		if (dataBuffer == null)
			return;

		// get resolution index
		int resIdx = minuteSpacing / 5; // ep-g
		if (resIdx < 0)
			resIdx = 0;
		else if (resIdx > 3) // ep-g
			resIdx = 3; // ep-g

		// Set deltaX constant. The deltaX is actually is smaller at
		// latitude
		// extremes, but
		double deltaX = etopoSpacings[resIdx];

		// allocate storage for slope map
		slopeMap = new byte[bufferWidth * bufferHeight];

		// process dataBuffer to create slope
		for (int y = 0; y < bufferHeight; y++) {

			// compute the latitude of this
			double lat = 90. - 180. * (double) y / (double) bufferHeight;

			// get cosine of the latitude. This is used because the
			// spacing between minutes gets smaller in high latitude
			// extremes.
			double coslat = Math.cos(Math.toRadians(lat));

			// for scaling the slope
			double slopeScaler = (double) slopeAdjust * coslat / deltaX;

			// indeces
			int idx0 = y * bufferWidth;

			// do each row
			for (int x = 0; x < bufferWidth; x++) {

				// indeces
				int idx1 = idx0 + x;
				int idx2 = idx1 + bufferWidth;

				// special case at end
				if (y == bufferHeight - 1)
					idx2 = idx1;

				// get altitudes
				double d1 = (double) dataBuffer[idx1];
				double d2 = (double) dataBuffer[idx2];

				// compute (lookup) slope
				double slope = slopeScaler * (d2 - d1);

				// clip
				if (slope > 0.99)
					slope = 0.99;
				else if (slope < -0.99)
					slope = -0.99;

				// scale
				int islope = (int) (slope * 127.);

				// store
				slopeMap[idx1] = (byte) islope;

			}
		}
	}

	/**
	 * Loads the database from the appropriate file based on the current
	 * resolution. The data files are in INTEL format (must call
	 * BinaryBufferedFile.byteOrder(true)).
	 */
	protected void loadBuffer() {

		// get the resolution index
		int resIdx = minuteSpacing / 5; // ep-g
		if (resIdx < 0)
			resIdx = 0;
		else if (resIdx > 3) // ep-g
			resIdx = 3; // ep-g

		// build file name
		String fileName = path + etopoFileNames[resIdx];

		// Clean this out...dfd
		dataBuffer = null;

		try {

			// treat as buffered binary
			BinaryBufferedFile binFile = new BinaryBufferedFile(fileName);
			binFile.byteOrder(true);

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

			// allocate storage
			dataBuffer = new short[(bufferWidth + spacer) * bufferHeight];

			// read data
			for (int i = 0; i < bufferWidth * bufferHeight; i++)
				dataBuffer[i] = binFile.readShort();

			// done
			binFile.close();

			// This is important for image creation.
			bufferWidth += spacer;

		} catch (FileNotFoundException e) {
			Debug.error("ETOPOLayer loadBuffer(): file " + fileName
					+ " not found");
		} catch (IOException e) {
			Debug.error("ETOPOLayer loadBuffer(): File IO Error!\n"
					+ e.toString());
		} catch (FormatException e) {
			Debug.error("ETOPOLayer loadBuffer(): Format exception!\n"
					+ e.toString());
		}

	}

	/*
	 * Builds the raster image that has the dimensions of the current
	 * projection. The algorithm is is follows: <P><pre> allocate storage the
	 * size of the projection (use ints for RGBA)
	 * 
	 * for each screen point
	 * 
	 * inverse project screen point to get lat/lon (world coords) get altitude
	 * and/or slope at the world coord compute (lookup) color at the world coord
	 * set color value into screen coord location
	 * 
	 * end
	 * 
	 * create OMRaster from the int array data. </pre>
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
			int width = projection.getWidth();
			int height = projection.getHeight();

			// create int array to hold colors
			int[] colors = new int[width * height];

			// compute scalers for lat/lon indicies
			float scy = (float) bufferHeight / 180F;
			float scx = (float) bufferWidth / 360F;

			// starting and ending indices
			int sx = 0, sy = 0, ex = width, ey = height;

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
			for (int y = sy; y < ey; y++) {

				// process each column
				for (int x = sx; x < ex; x++) {

					// inverse project x,y to lon,lat
					projection.inverse(x, y, llp);

					// get point values
					float lat = llp.getLatitude();
					float lon = llp.getLongitude();

					// check... dfd
					if (minuteSpacing == 2) {
						lon += 180.;
					} else {
						if (lon < 0.)
							lon += 360.;
					}

					// find indicies
					int lat_idx = (int) ((90. - lat) * scy);
					int lon_idx = (int) (lon * scx);

					// offset
					int ofs = lon_idx + lat_idx * bufferWidth;

					// make a color
					int idx = 0;
					int gray = 0;
					try {

						// get elevation
						short el = dataBuffer[ofs];

						// slope
						byte sl = slopeMap[ofs];

						// our index
						idx = y * width + x;

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
			ret = new OMRaster(0, 0, width, height, colors);

		}

		// return or raster
		return ret;

	}

	/**
	 * Prepares the graphics for the layer. This is where the getRectangle()
	 * method call is made on the etopo.
	 * <p>
	 * Occasionally it is necessary to abort a prepare call. When this happens,
	 * the map will set the cancel bit in the LayerThread, (the thread that is
	 * running the prepare). If this Layer needs to do any cleanups during the
	 * abort, it should do so, but return out of the prepare asap.
	 */
	public synchronized OMGraphicList prepare() {

		if (isCancelled()) {
			Debug.message("etopo", getName()
					+ "|ETOPOLayer.prepare(): aborted.");
			return null;
		}
		Projection projection = getProjection();
		if (projection == null) {
			Debug
					.error("ETOPO Layer needs to be added to the MapBean before it can draw images!");
			return new OMGraphicList();
		}

		// load the buffer
		if (dataBuffer == null || spacingReset) {
			loadBuffer();
			spacingReset = false;
			slopeReset = true;
		}

		// re-do the slope map
		if (slopeReset) {
			buildSlopeMap();
			slopeReset = false;
		}

		Debug.message("basic", getName() + "|ETOPOLayer.prepare(): doing it");

		// Setting the OMGraphicsList for this layer. Remember, the
		// OMGraphicList is made up of OMGraphics, which are generated
		// (projected) when the graphics are added to the list. So,
		// after this call, the list is ready for painting.

		// call getRectangle();
		if (Debug.debugging("etopo")) {
			Debug.output(getName() + "|ETOPOLayer.prepare(): "
					+ "calling getRectangle " + " with projection: "
					+ projection + " ul = " + projection.getUpperLeft()
					+ " lr = " + projection.getLowerRight());
		}

		// build graphics list
		OMGraphicList omGraphicList = new OMGraphicList();
		omGraphicList.add(buildRaster());

		// ///////////////////
		// safe quit
		int size = 0;
		if (omGraphicList != null) {
			size = omGraphicList.size();
			Debug.message("basic", getName()
					+ "|ETOPOLayer.prepare(): finished with " + size
					+ " graphics");
		} else {
			Debug
					.message(
							"basic",
							getName()
									+ "|ETOPOLayer.prepare(): finished with null graphics list");
			omGraphicList = new OMGraphicList();
		}

		// Don't forget to project them. Since they are only being
		// recalled if the projection has changed, then we need to
		// force a reprojection of all of them because the screen
		// position has changed.
		omGraphicList.project(projection, true);
		return omGraphicList;
	}

	// ----------------------------------------------------------------------
	// GUI
	// ----------------------------------------------------------------------

	/** The user interface palette for the ETOPO layer. */
	protected Box paletteBox = null;

	/** Creates the interface palette. */
	public Component getGUI() {

		if (paletteBox == null) {
			if (Debug.debugging("etopo"))
				Debug.output("ETOPOLayer: creating ETOPO Palette.");

			paletteBox = Box.createVerticalBox();
			Box subbox0 = Box.createHorizontalBox();
			Box subbox1 = Box.createHorizontalBox();
			Box subbox2 = Box.createVerticalBox();
			Box subbox3 = Box.createHorizontalBox();

			// The ETOPO resolution selector
			JPanel resPanel = PaletteHelper
					.createPaletteJPanel("Lat/Lon Spacing");
			String[] resStrings = { "2 Minute", "5 Minute", "10 Minute",
					"15 Minute" }; // ep-g

			JComboBox resList = new JComboBox(resStrings);
			resList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox jcb = (JComboBox) e.getSource();
					int newRes = jcb.getSelectedIndex();
					int curRes = minuteSpacing / 5; // ep-g
					if (curRes != newRes)
						spacingReset = true;
					switch (newRes) {
					case 0:
						minuteSpacing = 2;
						break; // ep-g
					case 1:
						minuteSpacing = 5;
						break; // ep-g
					case 2:
						minuteSpacing = 10;
						break; // ep-g
					case 3:
						minuteSpacing = 15;
						break; // ep-g
					}

				}
			});

			resList.setSelectedIndex(minuteSpacing / 5); // ep-g
			resPanel.add(resList);

			// The ETOPO view selector
			JPanel viewPanel = PaletteHelper.createPaletteJPanel("View Type");
			String[] viewStrings = { "Grayscale Shading", "Color Shading" };

			JComboBox viewList = new JComboBox(viewStrings);
			viewList.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox jcb = (JComboBox) e.getSource();
					int newView = jcb.getSelectedIndex();
					if (newView != viewType)
						slopeReset = true;
					switch (newView) {
					case 0:
						viewType = SLOPESHADING;
						break;
					case 1:
						viewType = COLOREDSHADING;
						break;
					}

				}
			});

			viewList.setSelectedIndex(viewType);
			viewPanel.add(viewList);

			// The ETOPO Contrast Adjuster
			JPanel contrastPanel = PaletteHelper
					.createPaletteJPanel("Contrast Adjustment");
			JSlider contrastSlide = new JSlider(JSlider.HORIZONTAL, 1/* min */,
					5/* max */, 3/* initial */);
			java.util.Hashtable<Integer, JLabel> dict = new java.util.Hashtable<Integer, JLabel>();
			dict.put(new Integer(1), new JLabel("min"));
			dict.put(new Integer(5), new JLabel("max"));
			contrastSlide.setLabelTable(dict);
			contrastSlide.setPaintLabels(true);
			contrastSlide.setMajorTickSpacing(1);
			contrastSlide.setPaintTicks(true);
			contrastSlide.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					JSlider slider = (JSlider) ce.getSource();
					if (slider.getValueIsAdjusting()) {
						Debug.output("ETOPOLayer - Contrast Slider value = "
								+ slider.getValue());
						slopeAdjust = slider.getValue();
					}
				}
			});
			contrastPanel.add(contrastSlide);

			// The ETOPO Opaqueness
			JPanel opaquenessPanel = PaletteHelper
					.createPaletteJPanel("Opaqueness");
			JSlider opaquenessSlide = new JSlider(JSlider.HORIZONTAL,
					0/* min */, 255/* max */, opaqueness/* initial */);
			opaquenessSlide.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent ce) {
					JSlider slider = (JSlider) ce.getSource();
					if (slider.getValueIsAdjusting()) {
						fireRequestInfoLine("ETOPOLayer - Opaqueness Slider value = "
								+ slider.getValue());
						opaqueness = slider.getValue();
					}
				}
			});

			opaquenessPanel.add(opaquenessSlide);

			JButton redraw = new JButton("Redraw ETOPO Layer");
			redraw.addActionListener(this);
			redraw.setActionCommand(RedrawCmd);

			subbox0.add(resPanel);
			paletteBox.add(subbox0);
			subbox1.add(viewPanel);
			paletteBox.add(subbox1);
			subbox2.add(contrastPanel);
			subbox2.add(opaquenessPanel);
			paletteBox.add(subbox2);
			subbox3.add(redraw);
			paletteBox.add(subbox3);
		}

		return paletteBox;
	}

	// ----------------------------------------------------------------------
	// ActionListener interface implementation
	// ----------------------------------------------------------------------

	/**
	 * Used just for the redraw button.
	 */
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getActionCommand() == RedrawCmd) {
			doPrepare();
		}
	}
}