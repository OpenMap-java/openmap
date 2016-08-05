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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfCoverage.java,v $
// $RCSfile: RpfCoverage.java,v $
// $Revision: 1.9 $
// $Date: 2005/12/09 21:09:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This is a tool that provides coverage information on the Rpf data. It is
 * supposed to be a simple tool that lets you see the general location of data,
 * to guide you to the right place and scale of coverage. The layer really uses
 * the properties passed in to it to determine which RPF/A.TOC should be scanned
 * for the data. There is a palette for this layer, that lets you turn off the
 * coverage for different levels of Rpf. Right now, only City Graphics, TLM,
 * JOG, TPC, ONC, JNC, GNC and 5/10 meter CIB scales are are handled. All other
 * scales are tossed together under the misc setting. The City Graphics setting
 * shows all charts for scales greater than than 1:15k.
 * <P>
 * 
 * <pre>
 *       The properties for this file are:
 *        # Java Rpf properties
 *        # Number between 0-255: 0 is transparent, 255 is opaque
 *        jrpf.coverageOpaque=255
 *        #Default is true, don't need this entry if you like it...
 *        jrpf.CG.showcov=true
 *        #Default colors don't need this entry
 *        jrpf.CG.color=CE4F3F
 *        # Other types can be substituted for CG (TLM, JOG, TPC, ONC, JNC, GNC, CIB10, CIB5, MISC)
 *        # Fill the rectangle, default is true
 *        jrpf.coverageFill=true
 * </pre>
 */
public class RpfCoverage extends OMGraphicList implements RpfConstants, PropertyConsumer {

	/** Property to use for filled rectangles (when java supports it). */
	public static final String CoverageOpaquenessProperty = "coverageOpaque";
	/** Property to use to fill rectangles. */
	public static final String FillProperty = "coverageFill";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected RpfCoverageManager coverageManager = null;
	protected RpfFrameProvider frameProvider = null;
	protected Map<RpfProductInfo, RpfCoverageControl> coverages = new TreeMap<RpfProductInfo, RpfCoverageControl>(
			new RCCScaleComparator());
	protected String propertyPrefix = null;
	/**
	 * A setting for how transparent to make the images. The default is 255,
	 * which is totally opaque. Not used right now.
	 */
	protected int opaqueness = RpfColortable.DEFAULT_OPAQUENESS;
	/** Flag to fill the coverage rectangles. */
	protected boolean fillRects = true;
	/** The parent layer. */
	protected Layer layer;
	/** Flag to track when the RpfCoverage is active. */
	protected boolean inUse = false;
	/**
	 * Show the palette when showing coverage. Probably not needed for layers
	 * limiting chart seriestypes for display.
	 */
	protected boolean showPalette = true;

	protected I18n i18n = Environment.getI18n();

	/**
	 * The default constructor for the Layer. All of the attributes are set to
	 * their default values.
	 */
	public RpfCoverage(Layer l, RpfFrameProvider frameProvider) {
		this.layer = l;
		setFrameProvider(frameProvider);

		super.setTraverseMode(LAST_ADDED_ON_TOP);
	}

	public void setFrameProvider(RpfFrameProvider frameProvider) {
		this.frameProvider = frameProvider;

		if (frameProvider != null) {
			List<RpfCoverageBox> rpfCoverages = frameProvider.getCatalogCoverage(90f, -180f, -90f, 180f,
					new CADRG(new LatLonPoint.Double(), 10000000f, 200, 200), null);
			for (RpfCoverageBox rcb : rpfCoverages) {
				RpfProductInfo rpfPI = RpfProductInfo.get(rcb.chartCode);
				if (rpfPI != null) {
					RpfCoverageControl control = coverages.get(RpfProductInfo.get(rcb.chartCode));
					if (control == null) {
						control = new RpfCoverageControl(rpfPI, layer);
						coverages.put(rpfPI, control);
						control.setFilled(fillRects);
						control.setFillPaint(getModifiedColor((Color) control.getFillPaint()));
					}
				}

			}
		}
	}

	/** Method that sets all the variables to the default values. */
	protected void setDefaultValues() {
		allCoveragesOn();
		opaqueness = RpfColortable.DEFAULT_OPAQUENESS / 2;
		fillRects = true;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean iu) {
		inUse = iu;
		this.setVisible(iu); // Show OMGraphics or not
		if (showPalette || !inUse) {
			// Always want it hidden if not in use.
			JFrame covPalette = getPaletteWindow();
			covPalette.setLocationRelativeTo(layer.getPalette());
			covPalette.setVisible(inUse);
		}
	}

	public boolean isShowPalette() {
		return showPalette;
	}

	public void setShowPalette(boolean sp) {
		showPalette = sp;
		if (!showPalette) {
			allCoveragesOn();
		}
	}

	public void allCoveragesOn() {
		for (RpfCoverageControl rcc : coverages.values()) {
			rcc.setVisible(true);
		}
	}

	public void setProperties(java.util.Properties props) {
		setProperties(null, props);
	}

	/**
	 * Set all the Rpf properties from a properties object.
	 * 
	 * @param prefix
	 *            string prefix used in the properties file for this layer.
	 * @param properties
	 *            the properties set in the properties file.
	 */
	public void setProperties(String prefix, java.util.Properties properties) {
		setPropertyPrefix(prefix);

		setDefaultValues();

		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		fillRects = PropUtils.booleanFromProperties(properties, prefix + FillProperty, fillRects);

		showPalette = PropUtils.booleanFromProperties(properties, prefix + CoverageProperty, showPalette);

		opaqueness = PropUtils.intFromProperties(properties, prefix + CoverageOpaquenessProperty, opaqueness);

		for (RpfCoverageControl rcc : coverages.values()) {
			String abbrdot = PropUtils.getScopedPropertyPrefix(rcc.rpfProduct.abbr.replace(' ', '_'));
			rcc.setColorValue(properties.getProperty(prefix + abbrdot + ColorProperty, rcc.getColorValue()));
			rcc.setFilled(fillRects);
			rcc.setFillPaint(getModifiedColor((Color) rcc.getFillPaint()));
			rcc.setVisible(PropUtils.booleanFromProperties(properties, prefix + abbrdot + ShowCoverageProperty,
					rcc.isVisible()));
		}
	}

	/**
	 * PropertyConsumer method, to fill in a Properties object, reflecting the
	 * current values of the layer. If the layer has a propertyPrefix set, the
	 * property keys should have that prefix plus a separating '.' prepended to
	 * each property key it uses for configuration.
	 * 
	 * @param props
	 *            a Properties object to load the PropertyConsumer properties
	 *            into. If props equals null, then a new Properties object
	 *            should be created.
	 * @return Properties object containing PropertyConsumer property values. If
	 *         getList was not null, this should equal getList. Otherwise, it
	 *         should be the Properties object created by the PropertyConsumer.
	 */
	public Properties getProperties(Properties props) {
		if (props == null) {
			props = new Properties();
		}

		String prefix = PropUtils.getScopedPropertyPrefix(propertyPrefix);

		props.put(prefix + FillProperty, new Boolean(fillRects).toString());
		props.put(prefix + CoverageProperty, new Boolean(showPalette).toString());
		props.put(prefix + CoverageOpaquenessProperty, Integer.toString(opaqueness));

		for (RpfCoverageControl rcc : coverages.values()) {
			String abbr = rcc.rpfProduct.abbr.replace(' ', '_');
			String abbrdot = PropUtils.getScopedPropertyPrefix(abbr);
			props.put(prefix + abbrdot + ColorProperty, Integer.toHexString(((Color) rcc.getLinePaint()).getRGB()));
		}

		return props;
	}

	/**
	 * Method to fill in a Properties object with values reflecting the
	 * properties able to be set on this PropertyConsumer. The key for each
	 * property should be the raw property name (without a prefix) with a value
	 * that is a String that describes what the property key represents, along
	 * with any other information about the property that would be helpful
	 * (range, default value, etc.). For Layer, this method should at least
	 * return the 'prettyName' property.
	 * 
	 * @param list
	 *            a Properties object to load the PropertyConsumer properties
	 *            into. If getList equals null, then a new Properties object
	 *            should be created.
	 * @return Properties object containing PropertyConsumer property values. If
	 *         getList was not null, this should equal getList. Otherwise, it
	 *         should be the Properties object created by the PropertyConsumer.
	 */
	public Properties getPropertyInfo(Properties list) {
		if (list == null) {
			list = new Properties();
		}

		PropUtils.setI18NPropertyInfo(i18n, list, RpfLayer.class, FillProperty, "Fill Coverage Rectangles",
				"Flag to set if the coverage rectangles should be filled.",
				"com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
		PropUtils.setI18NPropertyInfo(i18n, list, RpfLayer.class, CoverageProperty, "Show Coverage Palette",
				"Flag to set the coverage palette should be shown.",
				"com.bbn.openmap.util.propertyEditor.OnOffPropertyEditor");
		PropUtils.setI18NPropertyInfo(i18n, list, RpfLayer.class, CoverageOpaquenessProperty, "Coverage Opaqueness",
				"Integer representing opaqueness level (0-255, 0 is clear) of coverage rectangles.", null);

		for (RpfCoverageControl rcc : coverages.values()) {
			String abbr = rcc.rpfProduct.abbr.replace(' ', '_');
			String abbrdot = PropUtils.getScopedPropertyPrefix(abbr);
			PropUtils.setI18NPropertyInfo(i18n, list, RpfLayer.class, abbrdot + ColorProperty,
					rcc.rpfProduct.abbr + " Coverage Color", "Color for " + abbr + " chart coverage.",
					"com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
		}

		return list;
	}

	/**
	 * Specify what order properties should be presented in an editor.
	 */
	public String getInitPropertiesOrder() {
		StringBuilder sb = new StringBuilder(" " + FillProperty + " " + CoverageOpaquenessProperty);
		for (RpfProductInfo rpi : coverages.keySet()) {
			sb.append(" ").append(rpi.abbr.replace(' ', '_')).append(".").append(ColorProperty);
		}
		return sb.toString();
	}

	/**
	 * Set the property key prefix that should be used by the PropertyConsumer.
	 * The prefix, along with a '.', should be prepended to the property keys
	 * known by the PropertyConsumer.
	 * 
	 * @param prefix
	 *            the prefix String.
	 */
	public void setPropertyPrefix(String prefix) {
		propertyPrefix = prefix;
	}

	/**
	 * Get the property key prefix that is being used to prepend to the property
	 * keys for Properties lookups.
	 * 
	 * @return the property prefix
	 */
	public String getPropertyPrefix() {
		return propertyPrefix;
	}

	/**
	 * Prepares the graphics for the layer. This is where the getRectangle()
	 * method call is made on the rpfcov.
	 * <p>
	 * Occasionally it is necessary to abort a prepare call. When this happens,
	 * the map will set the cancel bit in the LayerThread, (the thread that is
	 * running the prepare). If this Layer needs to do any cleanups during the
	 * abort, it should do so, but return out of the prepare asap.
	 */
	public void prepare(Projection projection, String chartSeries) {

		double ullat = 90;
		double ullon = -180;
		double lrlat = -90;
		double lrlon = 180;

		if (projection != null) {
			ullat = projection.getUpperLeft().getY();
			ullon = projection.getUpperLeft().getX();
			lrlat = projection.getLowerRight().getY();
			lrlon = projection.getLowerRight().getX();
		}

		Debug.message("basic", "RpfCoverage.prepare(): doing it");

		// Setting the OMGraphicsList for this layer. Remember, the
		// OMGraphicList is made up of OMGraphics, which are generated
		// (projected) when the graphics are added to the list. So,
		// after this call, the list is ready for painting.

		// IF the data arrays have not been set up yet, do it!
		if (coverageManager == null) {
			coverageManager = new RpfCoverageManager(frameProvider);
		}

		clearLayerAndCoverages();
		coverageManager.getCatalogCoverage(ullat, ullon, lrlat, lrlon, projection, chartSeries, coverages);
		resetCoveragesOnLayer();
	}

	protected void clearLayerAndCoverages() {
		this.clear();
		for (RpfCoverageControl rcc : coverages.values()) {
			rcc.clear();
		}
	}

	protected void resetCoveragesOnLayer() {
		this.clear();
		for (RpfCoverageControl rcc : coverages.values()) {
			if (rcc.getVisibilityToggle().isSelected()) {
				this.addAll(rcc);
			}
		}
	}

	/**
	 * @return Returns the opaqueness.
	 */
	public int getOpaqueness() {
		return opaqueness;
	}

	/**
	 * @param opaqueness
	 *            The opaqueness to set.
	 */
	public void setOpaqueness(int opaqueness) {
		this.opaqueness = opaqueness;

		for (RpfCoverageControl rcc : coverages.values()) {
			rcc.setFilled(fillRects);
			rcc.setFillPaint(getModifiedColor((Color) rcc.getFillPaint()));
		}

	}

	/**
	 * @return the fillRects
	 */
	public boolean isFillRects() {
		return fillRects;
	}

	/**
	 * @param fillRects
	 *            the fillRects to set
	 */
	public void setFillRects(boolean fillRects) {
		this.fillRects = fillRects;

		if (coverages != null) {
			for (RpfCoverageControl rcc : coverages.values()) {
				rcc.setFilled(fillRects);
				rcc.setFillPaint(getModifiedColor((Color) rcc.getFillPaint()));
			}
		}
	}

	protected Color getModifiedColor(Color color) {
		if (opaqueness < 255) {
			int opa = opaqueness << 24;
			return ColorFactory.createColor(((color.getRGB() & 0x00FFFFFF) | opa), true);
		} else {
			return ColorFactory.createColor(color.getRGB(), true);
		}
	}

	// ----------------------------------------------------------------------
	// GUI
	// ----------------------------------------------------------------------
	/**
	 * Provides the palette widgets to control the options of showing maps, or
	 * attribute text.
	 * 
	 * @return Component object representing the palette widgets.
	 */
	public java.awt.Component getGUI() {
		Box box = Box.createVerticalBox();

		box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Coverage Controls"));

		if (coverages != null) {
			for (RpfProductInfo rpi : coverages.keySet()) {
				box.add(coverages.get(rpi).getVisibilityToggle());
			}
		}

		return box;
	}

	public static class RpfCoverageControl extends OMGraphicList {

		private static final long serialVersionUID = 1L;

		RpfProductInfo rpfProduct;
		Layer layer;
		JCheckBox controlToggle;
		String colorValue;

		public RpfCoverageControl(RpfProductInfo rpfPro, Layer layer) {
			this.rpfProduct = rpfPro;
			this.layer = layer;

			Color c = DefaultColors.getColor(rpfPro);
			setLinePaint(c);
			setColorValue(Integer.toHexString(c.getRGB()));
		}

		public JCheckBox getVisibilityToggle() {
			if (controlToggle == null) {
				StringBuilder title = new StringBuilder("Show ");
				title.append(rpfProduct.abbr).append(" Coverage");

				controlToggle = new JCheckBox(title.toString(), true);
				controlToggle.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						RpfCoverageControl.this.setVisible(((JCheckBox) ae.getSource()).isSelected());
						if (layer != null) {
							if (layer instanceof RpfLayer && ((RpfLayer)layer).coverage != null) {
								((RpfLayer)layer).coverage.resetCoveragesOnLayer();
							}
							layer.repaint();
						}
					}
				});

			}
			return controlToggle;
		}

		public boolean add(OMGraphic omg) {
			omg.setLinePaint(getLinePaint());
			omg.setFillPaint(getFillPaint());
			return super.add(omg);
		}

		public void setFilled(boolean filled) {
			if (filled) {
				setFillPaint(getLinePaint());
			} else {
				setFillPaint(OMColor.clear);
			}
		}

		public boolean isFilled() {
			Paint fPaint = getFillPaint();
			return fPaint == null || fPaint == OMColor.clear;
		}

		public void setVisible(boolean set) {
			super.setVisible(set);
			if (controlToggle != null) {
				controlToggle.setSelected(set);
			}
		}

		/**
		 * @return the colorValue
		 */
		public String getColorValue() {
			return colorValue;
		}

		/**
		 * @param colorValue
		 *            the colorValue to set
		 */
		public void setColorValue(String colorValue) {
			try {
				this.colorValue = colorValue;
				setLinePaint(PropUtils.parseColor(colorValue));
			} catch (NumberFormatException nfe) {
			}
		}

	}

	public static class RCCScaleComparator implements Comparator<RpfProductInfo> {
		public int compare(RpfProductInfo c1, RpfProductInfo c2) {
			double diff = c1.scale - c2.scale;
			if (diff == 0) {
				return 0;
			} else if (diff < 0) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	protected JFrame paletteWindow = null;

	/**
	 * Get RpfCoverage's associated palette as a top-level window
	 * 
	 * @return the frame that the palette is in
	 */
	public JFrame getPaletteWindow() {

		if (paletteWindow == null) {
			// create the palette's scroll pane
			Component pal = getGUI();

			if (pal == null) {
				pal = new JLabel("No Coverage Information Available.");
			}

			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
			p.setAlignmentX(Component.LEFT_ALIGNMENT);
			p.setAlignmentY(Component.BOTTOM_ALIGNMENT);
			p.add(pal);

			JScrollPane scrollPane = new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

			// create the palette internal window
			paletteWindow = new JFrame("RPF Coverage Palette");

			paletteWindow.setContentPane(scrollPane);
			paletteWindow.pack();// layout all the components
		}
		return paletteWindow;
	}

	protected enum DefaultColors {

		CG(RpfProductInfo.CG, 0xAC4853), 
		TLM(RpfProductInfo.TL, 0xCE4F3F), 
		JOG(RpfProductInfo.JG, 0xAC7D74), 
		TPC(RpfProductInfo.TP, 0xACCD10), 
		ONC(RpfProductInfo.ON, 0xFCCDE5), 
		JNC(RpfProductInfo.JN, 0x7386E5), 
		GNC(RpfProductInfo.GN, 0x55866B), 
		CIB10(RpfProductInfo.I1, 0x07516B), 
		CIB5(RpfProductInfo.I2, 0x071CE0), 
		MISC(RpfProductInfo.MM, 0xF2C921);

		private RpfProductInfo rpi;
		private int defaultColorInt;

		DefaultColors(RpfProductInfo rpi, int defaultColorInt) {
			this.rpi = rpi;
			this.defaultColorInt = defaultColorInt;
		}

		static int getColorInt(RpfProductInfo rpi) {
			for (DefaultColors dc : DefaultColors.values()) {
				if (dc.rpi.equals(rpi)) {
					return dc.defaultColorInt;
				}
			}
			return MISC.defaultColorInt;
		}

		static Color getColor(RpfProductInfo rpi) {
			return new Color(DefaultColors.getColorInt(rpi));
		}
	}

}