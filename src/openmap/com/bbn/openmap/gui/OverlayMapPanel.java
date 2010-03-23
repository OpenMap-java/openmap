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

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Properties;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.proj.ProjectionStack;

/**
 * An extension of the BasicMapPanel that uses an OverlayLayout on the panel in
 * the BorderLayout.CENTER position. Contains a transparent widgets JPanel for
 * placing components floating on top of the map. The default implementation of
 * layoutPanel() adds an EmbeddedNavPanel in the upper left position of the map,
 * as well as a ProjectionStack for it to use.
 */
public class OverlayMapPanel extends BasicMapPanel {

    public static Logger logger = Logger.getLogger(OverlayMapPanel.class.getName());
    /**
     * May be null, in which case the widgets should decide.
     */
    protected DrawingAttributes activeWidgetColors;
    /**
     * May be null, in which case the widgets should decide.
     */
    protected DrawingAttributes inactiveWidgetColors; 
    /**
     * Defaults to 15;
     */
    protected int widgetButtonSize = 15;
    
    /**
     * A transparent JPanel with a border layout, residing on top of the
     * MapBean.
     */
    protected JPanel widgets;

    /**
     * Creates an empty OverlayMapPanel that creates its own empty
     * PropertyHandler. The MapPanel will contain a MapBean, a MapHandler,
     * EmbeddedNavPanel and a PropertyHandler with no properties. The
     * constructor to use to create a blank map framework to add components to.
     */
    public OverlayMapPanel() {
        super(new PropertyHandler(new Properties()), false);
    }

    /**
     * Create a OverlayMapPanel with the option of delaying the search for
     * properties until the <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the artful
     *        programmer will call <code>create()</code>
     */
    public OverlayMapPanel(boolean delayCreation) {
        super(null, delayCreation);
    }

    /**
     * Create a OverlayMapPanel that configures itself with the properties
     * contained in the PropertyHandler provided. If the PropertyHandler is
     * null, a new one will be created.
     */
    public OverlayMapPanel(PropertyHandler propertyHandler) {
        super(propertyHandler, false);
    }

    /**
     * Create a OverlayMapPanel that configures itself with properties contained
     * in the PropertyHandler provided, and with the option of delaying the
     * search for properties until the <code>create()</code> call is made.
     * 
     * @param delayCreation true to let the MapPanel know that the artful
     *        programmer will call <code>create()</code>
     */
    public OverlayMapPanel(PropertyHandler propertyHandler,
            boolean delayCreation) {
        super(propertyHandler, delayCreation);
    }

    protected void addMapBeanToPanel(MapBean map) {
        layoutPanel(map);
    }

    public DrawingAttributes getActiveWidgetColors() {
		return activeWidgetColors;
	}

	public void setActiveWidgetColors(DrawingAttributes activeWidgetColors) {
		this.activeWidgetColors = activeWidgetColors;
	}

	public DrawingAttributes getInactiveWidgetColors() {
		return inactiveWidgetColors;
	}

	public void setInactiveWidgetColors(DrawingAttributes inactiveWidgetColors) {
		this.inactiveWidgetColors = inactiveWidgetColors;
	}

	public int getWidgetButtonSize() {
		return widgetButtonSize;
	}

	public void setWidgetButtonSize(int widgetButtonSize) {
		this.widgetButtonSize = widgetButtonSize;
	}

	/**
     * New method added, called from addMapBeanToPanel(MapBean).
     * 
     * @param map
     */
    protected void layoutPanel(MapBean map) {
        Dimension minimumSize = new Dimension(MapBean.DEFAULT_WIDTH, MapBean.DEFAULT_HEIGHT);

        JPanel centerContainer = new JPanel();
        
        JPanel hackPanel = new JPanel();
        hackPanel.setLayout(new BorderLayout());
        hackPanel.setOpaque(false);
        hackPanel.add(map, BorderLayout.CENTER);
        
        centerContainer.setLayout(new OverlayLayout(centerContainer));

        EmbeddedNavPanel navPanel = new EmbeddedNavPanel(activeWidgetColors, inactiveWidgetColors, widgetButtonSize);
        navPanel.setBounds(12,
                12,
                navPanel.getMinimumSize().width,
                navPanel.getMinimumSize().height);

        addMapComponent(navPanel);
        addMapComponent(new ProjectionStack());

        EmbeddedScaleDisplayPanel scaleDisplay = new EmbeddedScaleDisplayPanel();
        addMapComponent(scaleDisplay);
        
        widgets = new JPanel();
        widgets.setLayout(new BorderLayout());
        widgets.setBackground(OMGraphicConstants.clear);
        widgets.setOpaque(false);
        widgets.setBounds(0, 0, map.getWidth(), map.getHeight());
        widgets.setMinimumSize(minimumSize);
        widgets.add(navPanel, BorderLayout.WEST);
        widgets.add(scaleDisplay, BorderLayout.EAST);
        
        setBorders(map, widgets);

        centerContainer.add(widgets);
        centerContainer.add(hackPanel);

        add(centerContainer, BorderLayout.CENTER);
    }

    /**
     * If you want different borders or color them differently, override this
     * method.
     * 
     * @param map
     * @param widgets
     */
    protected void setBorders(MapBean map, JPanel widgets) {

        if (map != null) {
            map.setBorder(null);
        }

        if (widgets != null) {
            widgets.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                    Color.GRAY,
                    Color.DARK_GRAY));
        }
    }

    /** Include exit in the File menu. Call this before create(). */
    public void includeExitMenuItem() {
        addProperty("quitMenu.class", "com.bbn.openmap.gui.map.QuitMenuItem");
        appendProperty("fileMenu.items", "quitMenu");
    }

    /** A main() method that just brings up a JFrame containing the MapPanel. */
    public static void main(String argv[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                JFrame f = new JFrame("Map");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                OverlayMapPanel map = new OverlayMapPanel(new PropertyHandler(new Properties()), true);
                map.create();

                map.getMapBean().setBackgroundColor(new Color(0x99b3cc));
                map.addMapComponent(new LayerHandler());
                map.addMapComponent(new MouseDelegator());
                map.addMapComponent(new OMMouseMode());
                ShapeLayer shapeLayer = new ShapeLayer("share/data/shape/cntry02/cntry02.shp");
                // shapeLayer.setAddAsBackground(true);
                map.addMapComponent(shapeLayer);
                map.includeExitMenuItem();
                f.setJMenuBar(map.getMapMenuBar());
                f.getContentPane().add(map);
                f.setSize(800, 600);
                f.setVisible(true);
            }

        });

    }
}
