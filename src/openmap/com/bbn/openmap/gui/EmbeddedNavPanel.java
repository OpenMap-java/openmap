/* *****************************************************************************
 *
 * <rrl>
 * =========================================================================
 *                                  LEGEND
 *
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014, with the exception of
 * third party software known as Sun Microsystems' Java Runtime Environment
 * (JRE), Quest Software's JClass, Oracle's JDBC, and JGoodies which are
 * separately governed under their commercial licenses.  Refer to the
 * license directory for information regarding the open source packages used
 * by this software.
 *
 * Copyright 2009 by BBN Technologies Corporation.
 * =========================================================================
 * </rrl>
 *
 * $Id: NavigationPanel.java 29356 2009-04-21 02:35:27Z rmacinty $
 *
 * ****************************************************************************/

package com.bbn.openmap.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.SoloMapComponent;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.event.PanListener;
import com.bbn.openmap.event.PanSupport;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionStack;
import com.bbn.openmap.proj.ProjectionStackTrigger;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.tools.icon.OpenMapAppPartCollection;
import com.bbn.openmap.util.PropUtils;

/**
 * A panel with map navigation widgets.
 * <p>
 * Portions of the implementation were ripped from
 * com.bbn.openmap.gui.NavigatePanel, com.bbn.openmap.gui.ProjectionStackTool,
 * and com.bbn.openmap.gui.ZoomPanel.
 * </p>
 */
public class EmbeddedNavPanel extends OMComponentPanel implements
		ProjectionListener, ProjectionStackTrigger, SoloMapComponent {
	public static Logger logger = Logger
			.getLogger("com.bbn.openmap.gui.EmbeddedNavPanel");

	private static final long serialVersionUID = 1L;

	public static final int SLIDER_MAX = 17;
	public final static String FADE_ATTRIBUTES_PROPERTY = "fade";
	public final static String LIVE_ATTRIBUTES_PROPERTY = "live";
	public static final String PanDistanceProperty = "panDistance";
	public static final String ZoomFactorProperty = "zoomFactor";

	public final static int DEFAULT_BUTTON_SIZE = 15;

	protected final static float defaultPanDistance = Float.NaN;
	protected final static float defaultZoomFactor = 2.0f;

	protected static Color CONTROL_BACKGROUND = OMGraphicConstants.clear;
	protected DrawingAttributes fadeAttributes;
	protected DrawingAttributes liveAttributes;
	protected int buttonSize = DEFAULT_BUTTON_SIZE;
	protected ImageIcon backIcon;
	protected ImageIcon backDimIcon;
	protected ImageIcon forwardIcon;
	protected ImageIcon forwardDimIcon;

	protected MapBean map;
	protected CenterSupport centerDelegate;
	protected PanSupport panDelegate;
	protected ZoomSupport zoomDelegate;
	protected JButton forwardProjectionButton;
	protected JButton backProjectionButton;
	protected JSlider slider;

	private float panDistance = defaultPanDistance;
	private float zoomFactor = defaultZoomFactor;

	protected float MIN_TRANSPARENCY = .25f;
	protected float SEMI_TRANSPARENCY = .65f;
	protected float MAX_TRANSPARENCY = 1.0f;
	protected boolean fade = false;

	protected Point2D recenterPoint;

	protected AlphaComposite ac = AlphaComposite.getInstance(
			AlphaComposite.SRC_ATOP, MAX_TRANSPARENCY);

	public EmbeddedNavPanel() {
		this(null, null, DEFAULT_BUTTON_SIZE);
	}

	/**
	 * Make one.
	 * 
	 * @param buttonColors
	 *            The live button colors when active.
	 * @param fadeColors
	 *            The faded button colors, when inactive.
	 * @param buttonSize
	 *            The relative pixel button sizes.
	 */
	public EmbeddedNavPanel(DrawingAttributes buttonColors,
			DrawingAttributes fadeColors, int buttonSize) {
		super();
		centerDelegate = new CenterSupport(this);
		panDelegate = new PanSupport(this);
		zoomDelegate = new ZoomSupport(this);
		// the two commands required to make this panel transparent
		setBackground(OMGraphicConstants.clear);
		setOpaque(false);

		initColors(buttonColors, fadeColors, buttonSize);

		// Checks the openmap.Latitude and openmap.Longitude properties, and
		// initializes the re-center point to that.
		float lat = Environment.getFloat(Environment.Latitude, 0f);
		float lon = Environment.getFloat(Environment.Longitude, 0f);
		setRecenterPoint(new Point2D.Float(lon, lat));

		layoutPanel();
	}

	protected void initColors(DrawingAttributes buttonColors,
			DrawingAttributes fadeColors, int buttonSize) {

		fadeAttributes = fadeColors;
		liveAttributes = buttonColors;

		if (buttonSize >= 10) {
			this.buttonSize = buttonSize;
		}

		if (fadeAttributes == null) {
			fadeAttributes = DrawingAttributes.getDefaultClone();
			Color fadeColor = new Color(0xffaaaaaa);
			fadeAttributes.setFillPaint(fadeColor);
			fadeAttributes.setLinePaint(fadeColor.darker());
		}

		if (buttonColors == null) {
			liveAttributes = DrawingAttributes.getDefaultClone();
			Color liveColor = new Color(0xDDF3F3F3);
			liveAttributes.setFillPaint(liveColor);
			liveAttributes.setMattingPaint(liveColor);
			liveAttributes.setMatted(true);
		}
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		fadeAttributes.setProperties(prefix + FADE_ATTRIBUTES_PROPERTY, props);
		liveAttributes.setProperties(prefix + LIVE_ATTRIBUTES_PROPERTY, props);

		panDistance = PropUtils.floatFromProperties(props, prefix
				+ PanDistanceProperty, defaultPanDistance);

		zoomFactor = PropUtils.floatFromProperties(props, prefix
				+ ZoomFactorProperty, defaultZoomFactor);
	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);

		fadeAttributes.getProperties(props);
		liveAttributes.getProperties(props);

		String prefix = PropUtils.getScopedPropertyPrefix(this);
		props.put(prefix + PanDistanceProperty, String.valueOf(panDistance));
		props.put(prefix + ZoomFactorProperty, String.valueOf(zoomFactor));

		return props;
	}

	/**
	 * TODO: This is not complete, the drawing attributes need to be separated
	 * out and scoped, so they can be set individually.
	 */
	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);
		// fadeAttributes.getPropertyInfo(props);
		// liveAttributes.getPropertyInfo(props);

		String interString;
		props.put(initPropertiesProperty,
			PanDistanceProperty + " " + ZoomFactorProperty);

		interString = i18n.get(EmbeddedNavPanel.class,
			PanDistanceProperty,
			com.bbn.openmap.I18n.TOOLTIP,
			"Panning Distance.");
		props.put(PanDistanceProperty, interString);
		interString = i18n.get(EmbeddedNavPanel.class,
			PanDistanceProperty,
			"Panning Distance");
		props.put(PanDistanceProperty + LabelEditorProperty,
			interString);
		props.put(PanDistanceProperty + ScopedEditorProperty,
			"com.bbn.openmap.util.propertyEditor.TextPropertyEditor");

		interString = i18n.get(EmbeddedNavPanel.class,
			ZoomFactorProperty,
			com.bbn.openmap.I18n.TOOLTIP,
			"Zoom Factor.");
		props.put(ZoomFactorProperty, interString);
		interString = i18n.get(EmbeddedNavPanel.class,
			ZoomFactorProperty,
			"Zoom Factor");
		props.put(ZoomFactorProperty + LabelEditorProperty,
			interString);
		props.put(ZoomFactorProperty + ScopedEditorProperty,
			"com.bbn.openmap.util.propertyEditor.TextPropertyEditor");

		return props;
	}

	protected void layoutPanel() {

		removeAll();
		int projStackButtonSize = (int) (buttonSize * 1.25);
		int rosetteButtonSize = buttonSize;
		int zoomButtonSize = buttonSize;
		setLayout(new GridBagLayout());

		GridBagConstraints layoutConstraints = new GridBagConstraints();
		int baseY = 0;

		IconPart bigArrow = OpenMapAppPartCollection.BIG_ARROW.getIconPart();
		bigArrow.setRenderingAttributes(fadeAttributes);
		backDimIcon = OMIconFactory.getIcon(projStackButtonSize,
				projStackButtonSize, bigArrow, null, Length.DECIMAL_DEGREE
						.toRadians(270.0));
		bigArrow.setRenderingAttributes(liveAttributes);
		backIcon = OMIconFactory.getIcon(projStackButtonSize,
				projStackButtonSize, bigArrow, null, Length.DECIMAL_DEGREE
						.toRadians(270.0));
		backProjectionButton = makeButton(backDimIcon,
				"Show Previous Projection");
		backProjectionButton.setActionCommand(ProjectionStack.BackProjCmd);

		bigArrow.setRenderingAttributes(fadeAttributes);
		forwardDimIcon = OMIconFactory.getIcon(projStackButtonSize,
				projStackButtonSize, bigArrow, null, Length.DECIMAL_DEGREE
						.toRadians(90.0));
		bigArrow.setRenderingAttributes(liveAttributes);
		forwardIcon = OMIconFactory.getIcon(projStackButtonSize,
				projStackButtonSize, bigArrow, null, Length.DECIMAL_DEGREE
						.toRadians(90.0));
		forwardProjectionButton = makeButton(forwardDimIcon,
				"Show Next Projection");
		forwardProjectionButton
				.setActionCommand(ProjectionStack.ForwardProjCmd);

		JPanel projStackButtonPanel = new JPanel();
		projStackButtonPanel.setOpaque(false);
		projStackButtonPanel.setBackground(CONTROL_BACKGROUND);
		projStackButtonPanel.add(backProjectionButton);
		projStackButtonPanel.add(forwardProjectionButton);

		layoutConstraints.anchor = GridBagConstraints.CENTER;
		layoutConstraints.gridwidth = GridBagConstraints.REMAINDER;
		layoutConstraints.gridy = baseY++;

		add(projStackButtonPanel, layoutConstraints);

		JPanel rosette = new JPanel();
		GridBagLayout internalGridbag = new GridBagLayout();
		GridBagConstraints c2 = new GridBagConstraints();
		rosette.setLayout(internalGridbag);

		rosette.setOpaque(false);
		rosette.setBackground(CONTROL_BACKGROUND);

		c2.gridx = 0;
		c2.gridy = 0;
		rosette.add(makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI
				.getIconPart(), liveAttributes, rosetteButtonSize, 0.0,
				"Pan Northwest", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(-45f, panDistance);
					}
				}), c2);
		c2.gridx = 1;
		rosette.add(makeButton(
				OpenMapAppPartCollection.MED_ARROW.getIconPart(),
				liveAttributes, rosetteButtonSize, 0.0, "Pan North",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(0f, panDistance);
					}
				}));
		c2.gridx = 2;
		rosette.add(makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI
				.getIconPart(), liveAttributes, rosetteButtonSize, 90.0,
				"Pan Northeast", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(45f, panDistance);
					}
				}), c2);

		c2.gridx = 0;
		c2.gridy = 1;
		rosette.add(makeButton(
				OpenMapAppPartCollection.MED_ARROW.getIconPart(),
				liveAttributes, rosetteButtonSize, 270.0, "Pan West",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(-90f, panDistance);
					}
				}), c2);
		c2.gridx = 1;
		IconPartList ipl = new IconPartList();
		ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
		ipl.add(OpenMapAppPartCollection.DOT.getIconPart());
		rosette.add(makeButton(ipl, liveAttributes, rosetteButtonSize, 0.0,
				"Center Map", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						Point2D centerPnt = getRecenterPoint();
						if (centerPnt == null) {
							centerDelegate.fireCenter(0, 0);
						} else {
							centerDelegate.fireCenter(centerPnt.getY(),
									centerPnt.getX());
						}
					}
				}), c2);
		c2.gridx = 2;
		rosette.add(makeButton(
				OpenMapAppPartCollection.MED_ARROW.getIconPart(),
				liveAttributes, rosetteButtonSize, 90.0, "Pan East",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(90f, panDistance);
					}
				}), c2);

		c2.gridx = 0;
		c2.gridy = 2;
		rosette.add(makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI
				.getIconPart(), liveAttributes, rosetteButtonSize, 270.0,
				"Pan Southwest", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(-135f, panDistance);
					}
				}), c2);
		c2.gridx = 1;
		rosette.add(makeButton(
				OpenMapAppPartCollection.MED_ARROW.getIconPart(),
				liveAttributes, rosetteButtonSize, 180.0, "Pan South",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(180f, panDistance);
					}
				}), c2);
		c2.gridx = 2;
		rosette.add(makeButton(OpenMapAppPartCollection.OPP_CORNER_TRI
				.getIconPart(), liveAttributes, rosetteButtonSize, 180.0,
				"Pan Southeast", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						panDelegate.firePan(135f, panDistance);
					}
				}), c2);

		layoutConstraints.gridy = baseY++;
		add(rosette, layoutConstraints);

		layoutConstraints.gridy = baseY++;
		layoutConstraints.insets = new Insets(6, 0, 6, 0);
		ipl = new IconPartList();
		// ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
		ipl.add(OpenMapAppPartCollection.PLUS.getIconPart());
		add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, "Zoom In",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						zoomDelegate.fireZoom(ZoomEvent.RELATIVE, 1.0f / zoomFactor);
					}
				}), layoutConstraints);

		layoutConstraints.gridy = baseY++;
		layoutConstraints.insets = new Insets(0, 0, 0, 0);
		add(makeScaleSlider(liveAttributes), layoutConstraints);

		layoutConstraints.gridy = baseY++;
		layoutConstraints.insets = new Insets(6, 0, 6, 0);
		ipl = new IconPartList();
		// ipl.add(OpenMapAppPartCollection.CIRCLE.getIconPart());
		ipl.add(OpenMapAppPartCollection.MINUS.getIconPart());
		add(makeButton(ipl, liveAttributes, zoomButtonSize, 0.0, "Zoom Out",
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						zoomDelegate.fireZoom(ZoomEvent.RELATIVE, zoomFactor);
					}
				}), layoutConstraints);

		// We could drop this, but I think it's needed to play well with other
		// containers when needed.
		layoutConstraints.fill = GridBagConstraints.VERTICAL;
		layoutConstraints.gridy = baseY++;
		layoutConstraints.weighty = 1;
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		filler.setBackground(OMGraphicConstants.clear);
		add(filler, layoutConstraints);

		setMinimumSize(new Dimension(75, (projStackButtonSize + 3
				* rosetteButtonSize + 2 * zoomButtonSize + 24 + 200)));
	}

	public Point2D getRecenterPoint() {
		return recenterPoint;
	}

	public void setRecenterPoint(Point2D recenterPoint) {
		this.recenterPoint = recenterPoint;
	}

	public float getPanDistance() {
		return panDistance;
	}

	public void setPanDistance(float panDistance) {
		this.panDistance = panDistance;
	}

	public float getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	protected JButton makeButton(IconPart iconPart, DrawingAttributes da,
			int size, double ddRot, String tooltip, ActionListener ac) {
		iconPart.setRenderingAttributes(da);
		return makeButton(OMIconFactory.getIcon(size, size, iconPart, null,
				Length.DECIMAL_DEGREE.toRadians(ddRot)), tooltip, ac);
	}

	protected JButton makeButton(ImageIcon icon, String toolTip,
			ActionListener listener) {
		JButton button = makeButton(icon, toolTip);
		button.addActionListener(listener);
		// KNOX -- don't let buttons get focus and add transparency listener
		button.setFocusable(false);
		button.addMouseListener(new NavPanelMouseListener());
		return button;
	}

	protected JButton makeButton(ImageIcon icon, String toolTip) {
		JButton button = new JButton(icon);
		// MAGIC: required to make background transparent!
		button.setBackground(CONTROL_BACKGROUND);
		button.setBorder(null);
		button.setMargin(new Insets(0, 0, 0, 0));
		// No surprise: also required to make background transparent.
		button.setOpaque(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(icon.getIconWidth(), icon
				.getIconHeight()));
		button.setToolTipText(toolTip);
		// KNOX -- don't let buttons get focus and add transparency listener
		button.setFocusable(false);
		button.addMouseListener(new NavPanelMouseListener());
		return button;
	}

	protected JComponent makeScaleSlider(DrawingAttributes da) {
		slider = new JSlider(SwingConstants.VERTICAL, 0, SLIDER_MAX, SLIDER_MAX);
		slider.setUI(new NavPanelSliderUI(slider, (Color) da.getFillPaint()));
		// MAGIC: required to make background transparent!
		slider.setBackground((Color) da.getFillPaint());
		slider.setBorder(BorderFactory.createLineBorder((Color) da
				.getFillPaint(), 1));
		slider.setForeground((Color) da.getFillPaint());
		slider.setInverted(true);
		slider.setMinorTickSpacing(1);
		// No surprise: also required to make background transparent.
		slider.setOpaque(false);
		slider.setPaintTicks(true);
		slider.setSnapToTicks(true);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// Need the check to avoid resetting the map scale if the window
				// is
				// resized. Only want this to happen of someone is moving the
				// slider
				// lever.
				if (slider.getValueIsAdjusting()) {
					changeMapScale(slider.getValue());
				}
			}
		});
		// KNOX -- don't let slider get focus and add transparency listener
		slider.setFocusable(false);
		slider.addMouseListener(new NavPanelMouseListener());
		return slider;
	}

	protected void changeMapScale(int sliderValue) {
		float newScale = sliderToScale(sliderValue);

		if (map.getScale() != newScale) {
			map.setScale(newScale);
		}
	}

	protected void changeSliderValue(Projection projection) {
		int newValue = scaleToSlider(projection.getScale());

		if (slider.getValue() != newValue) {
			slider.setValue(newValue);
		}
	}

	protected float sliderToScale(int sliderValue) {
		return (float) (getMapMaxScale() / Math.pow(2,
				(SLIDER_MAX - sliderValue)));
	}

	protected int scaleToSlider(float mapScale) {
		return (SLIDER_MAX - logBase2(getMapMaxScale() / mapScale));
	}

	/** Returns the largest integer n, such that 2^n <= the specified number. */
	public final static int logBase2(double number) {
		int log = 0;

		while (number > 1) {
			number = Math.floor(number / 2);
			++log;
		}

		return log;
	}

	public Color getScaleSliderBackground() {
		return slider.getBackground();
	}

	public void setScaleSliderBackground(Color sliderBackground) {
		slider.setBackground(sliderBackground);
	}

	public Color getScaleSliderForeground() {
		return slider.getForeground();
	}

	public void setScaleSliderForeground(Color sliderForeground) {
		slider.setForeground(sliderForeground);
	}

	private final float getMapMaxScale() {
		return map.getProjection().getMaxScale();
	}

	// OMComponentPanel
	public void findAndInit(Object someObject) {
		if (someObject instanceof MapBean) {
			map = (MapBean) someObject;
			map.addProjectionListener(this);
		}
		if (someObject instanceof PanListener) {
			addPanListener((PanListener) someObject);
		}
		if (someObject instanceof CenterListener) {
			addCenterListener((CenterListener) someObject);
		}
		if (someObject instanceof ZoomListener) {
			addZoomListener((ZoomListener) someObject);
		}
		if (someObject instanceof ProjectionStack) {
			((ProjectionStack) someObject).addProjectionStackTrigger(this);
		}
	}

	// OMComponentPanel
	public void findAndUndo(Object someObject) {
		if (someObject instanceof MapBean) {
			map.removeProjectionListener(this);
		}
		if (someObject instanceof PanListener) {
			removePanListener((PanListener) someObject);
		}
		if (someObject instanceof CenterListener) {
			removeCenterListener((CenterListener) someObject);
		}
		if (someObject instanceof ZoomListener) {
			removeZoomListener((ZoomListener) someObject);
		}
		if (someObject instanceof ProjectionStack) {
			((ProjectionStack) someObject).removeProjectionStackTrigger(this);
		}
	}

	public synchronized void addCenterListener(CenterListener listener) {
		centerDelegate.add(listener);
	}

	public synchronized void removeCenterListener(CenterListener listener) {
		centerDelegate.remove(listener);
	}

	public synchronized void addPanListener(PanListener listener) {
		panDelegate.add(listener);
	}

	public synchronized void removePanListener(PanListener listener) {
		panDelegate.remove(listener);
	}

	public synchronized void addZoomListener(ZoomListener listener) {
		zoomDelegate.add(listener);
	}

	public synchronized void removeZoomListener(ZoomListener listener) {
		zoomDelegate.remove(listener);
	}

	// ProjectionListener
	public void projectionChanged(ProjectionEvent event) {
		changeSliderValue(event.getProjection());
	}

	/** Adds a listener for events that shift the Projection stack. */
	// ProjectionStackTrigger
	public void addActionListener(ActionListener listener) {
		forwardProjectionButton.addActionListener(listener);
		backProjectionButton.addActionListener(listener);
	}

	/** Removes the listener for events that shift the Projection stack. */
	// ProjectionStackTrigger
	public void removeActionListener(ActionListener listener) {
		forwardProjectionButton.addActionListener(listener);
		backProjectionButton.addActionListener(listener);
	}

	/**
	 * Respond to changes in the contents of the forward and back projection
	 * stacks.
	 * 
	 * @param haveBackProjections
	 *            true if there is at least one back projection available
	 * @param haveForwardProjections
	 *            true if there is at least one forward projection available
	 */
	// ProjectionStackTrigger
	public void updateProjectionStackStatus(boolean haveBackProjections,
			boolean haveForwardProjections) {
		forwardProjectionButton.setIcon(haveForwardProjections ? forwardIcon
				: forwardDimIcon);
		backProjectionButton.setIcon(haveBackProjections ? backIcon
				: backDimIcon);
		forwardProjectionButton.setEnabled(haveForwardProjections);
		backProjectionButton.setEnabled(haveBackProjections);
	}

	public void paint(Graphics g) {
		if (ac != null) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setComposite(ac);
			super.paint(g2);
			g2.dispose();
		} else {
			super.paint(g);
		}
	}

	public void setTransparency(float transparency) {
		if (ac != null) {
			if (transparency > MAX_TRANSPARENCY) {
				transparency = MAX_TRANSPARENCY;
			}

			ac = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
					transparency);
			repaint();
		}
	}
	
	public void setMinimumTransparency(float minTransparency) {
	   MIN_TRANSPARENCY = minTransparency;
	}
	
	public void setSemiTransparency(float semiTransparency) {
           SEMI_TRANSPARENCY = semiTransparency;
        }

	public DrawingAttributes getFadeAttributes() {
		return fadeAttributes;
	}

	public void setFadeAttributes(DrawingAttributes fadeAttributes) {
		this.fadeAttributes = fadeAttributes;
	}

	public DrawingAttributes getLiveAttributes() {
		return liveAttributes;
	}

	public void setLiveAttributes(DrawingAttributes liveAttributes) {
		this.liveAttributes = liveAttributes;
	}

	public AlphaComposite getAc() {
		return ac;
	}

	public void setAc(AlphaComposite ac) {
		this.ac = ac;
	}

	public MapBean getMap() {
		return map;
	}

	// KNOX -- using this to paint ticks on slider
	private class NavPanelSliderUI extends BasicSliderUI {
		Color sliderTickColor = Color.white;

		public NavPanelSliderUI(JSlider slider, Color tickColor) {
			super(slider);
			sliderTickColor = tickColor;
		}

		@Override
		protected void paintMinorTickForVertSlider(Graphics g,
				Rectangle tickBounds, int y) {
			g.setColor(sliderTickColor);
			super.paintMinorTickForVertSlider(g, tickBounds, y);
		}

	}

	// KNOX -- using this to change level of transparency when mousing over
	// buttons/slider
	private class NavPanelMouseListener extends MouseAdapter {
		@Override
		public void mouseEntered(MouseEvent e) {
			if (ac.getAlpha() < SEMI_TRANSPARENCY) {
				setTransparency(SEMI_TRANSPARENCY);
				getTopLevelAncestor().repaint();
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (ac.getAlpha() > MIN_TRANSPARENCY) {
				setTransparency(MIN_TRANSPARENCY);
				getTopLevelAncestor().repaint();
			}
		}
	}
}
