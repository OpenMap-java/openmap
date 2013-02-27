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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/rpf/corba/CorbaRpfLayer.java,v $
// $RCSfile: CorbaRpfLayer.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 20:57:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf.corba;

/*  Java Core  */

import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.layer.rpf.RpfFrameProvider;
import com.bbn.openmap.layer.rpf.RpfLayer;
import com.bbn.openmap.layer.rpf.RpfViewAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * An RpfLayer that uses a CORBA-based RpfFrameProvider. JDK 1.2 and
 * the <BR>
 * com.sun.image.codec.jpeg package is required, as well as some <BR>
 * CORBA implementation. Tested with Visibroker 3.3.
 * 
 * <BR>
 * #----------------------------- <BR>
 * # Additional Properties for RpfLayer <BR>
 * #----------------------------- <BR>
 * layer.jpegQuality= <quality between 0.4 and 1.0> # ior | name
 * property, ior takes precidence if both are listed. layer.ior= <URL
 * for ior file> layer.name= <CORBA name for server> <BR>
 */
public class CorbaRpfLayer extends RpfLayer {

   private static final long serialVersionUID = 1L;

   /** Property to change the quickRedraw setting. T/F */
    public static final String QuickRedrawProperty = ".quickRedraw";

    /**
     * Flag to attempt to redraw the images already in the cache while
     * waiting for new frames. Works better for slower servers.
     * Default value is false.
     */
    protected boolean quickRedraw = false;

    /**
     * Keep a copy in case the frame provider goes away. It's
     * happened.
     */
    protected Properties props = null;

    /**
     * The default constructor for the Layer. All of the attributes
     * are set to their default values. Use this construct if you are
     * going to use a standard properties file, which will set the
     * paths.
     */
    public CorbaRpfLayer() {
        super();
        setFrameProvider((RpfFrameProvider) new CRFPClient());
    }

    /**
     * Set all the RPF properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {
        super.setProperties(prefix, properties);
        props = properties;

        ((CRFPClient) frameProvider).setProperties(prefix, properties);
        quickRedraw = PropUtils.booleanFromProperties(properties, prefix
                + QuickRedrawProperty, false);
    }

    public void removed(java.awt.Container cont) {
        super.removed(cont);
        dispose();
    }

    /**
     * Clear the frame cache.
     */
    public void clearCache() {

        if (this.cache != null) {
            // This is bad, and is changed from the RpfLayer. Make
            // sure this never happens.
            //          this.cache.setViewAttributes(null);
            //          this.cache.setFrameProvider(null);

            this.cache.clearCaches();
        }

        // This, too must never happen.
        frameProvider = null;

        setGraphicList(null);
        this.cache = null;
    }

    /**
     * When the layer is deleted, it should sign off from the server,
     * so that it can free up it's cache for it.
     */
    public void dispose() {
        // Check just in case, although this should never happen.
        if (frameProvider != null) {
            ((CRFPClient) frameProvider).dispose();
        }
    }

    /**
     * Creates the RpfFrameProvider. If one is already here, nothing
     * happens.
     * 
     * @param pathsToRPFDirs Array of strings that list the paths to
     *        RPF directories.
     */
    public void setPaths(String[] pathsToRPFDirs) {
        RpfFrameProvider frameProvider = getFrameProvider();

        if (!(frameProvider instanceof CRFPClient)) {
            return;
        }

        frameProvider = (RpfFrameProvider) new CRFPClient();
        setFrameProvider(frameProvider);

        if (props != null) {
            // Set default settings...
            ((CRFPClient) frameProvider).setProperties(getPropertyPrefix(),
                    props);
        }

        this.cache = null;
    }

    /**
     * Prepares the graphics for the layer. The only thing this method
     * does that is different than the RpfLayer is that if the current
     * OMGraphicList is not null, then it is reprojected and redrawn.
     * 
     * @return OMGraphicList of images and text.
     */
    public OMGraphicList prepare() {
        OMGraphicList oldList = getGraphicList();
        if (oldList != null) {
            oldList.generate(getProjection());
            if (getCoverage() != null) {
                getCoverage().generate(getProjection());
            }
            repaint();
        }
        return super.prepare();
    }

    /**
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
        JCheckBox showMapsCheck, showInfoCheck, lockSeriesCheck;

        Box box = Box.createVerticalBox();

        Box box1 = Box.createVerticalBox();
        Box box2 = Box.createVerticalBox();
        JPanel topbox = new JPanel();

        showMapsCheck = new JCheckBox("Show Images", viewAttributes.showMaps);
        showMapsCheck.setActionCommand(showMapsCommand);
        showMapsCheck.addActionListener(this);

        showInfoCheck = new JCheckBox("Show Attributes", viewAttributes.showInfo);
        showInfoCheck.setActionCommand(showInfoCommand);
        showInfoCheck.addActionListener(this);

        boolean locked = viewAttributes.chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY) ? false
                : true;
        String lockedTitle = locked ? (lockedButtonTitle + " - " + viewAttributes.chartSeries)
                : unlockedButtonTitle;

        lockSeriesCheck = new JCheckBox(lockedTitle, locked);
        lockSeriesCheck.setActionCommand(lockSeriesCommand);
        lockSeriesCheck.addActionListener(this);

        box1.add(showMapsCheck);
        box1.add(showInfoCheck);
        box1.add(lockSeriesCheck);

        if (coverage != null) {
            JCheckBox showCoverageCheck = new JCheckBox("Show Coverage Tool", false);
            showCoverageCheck.setActionCommand(showCoverageCommand);
            showCoverageCheck.addActionListener(this);
            box1.add(showCoverageCheck);
        }

        topbox.add(box1);
        topbox.add(box2);
        box.add(topbox);

        JPanel opaquePanel = PaletteHelper.createPaletteJPanel("Map Opaqueness");
        JSlider opaqueSlide = new JSlider(JSlider.HORIZONTAL, 0/* min */, 255/* max */, viewAttributes.opaqueness/* inital */);
        java.util.Hashtable dict = new java.util.Hashtable();
        dict.put(new Integer(0), new JLabel("clear"));
        dict.put(new Integer(255), new JLabel("opaque"));
        opaqueSlide.setLabelTable(dict);
        opaqueSlide.setPaintLabels(true);
        opaqueSlide.setMajorTickSpacing(50);
        opaqueSlide.setPaintTicks(true);
        opaqueSlide.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ce) {
                JSlider slider = (JSlider) ce.getSource();
                if (!slider.getValueIsAdjusting()) {
                    getViewAttributes().opaqueness = slider.getValue();
                    // Notify the server...
                    getFrameProvider().setViewAttributes(getViewAttributes());
                    fireRequestInfoLine("RPF Opaqueness set to "
                            + getViewAttributes().opaqueness
                            + " for future requests.");
                }
            }
        });
        opaquePanel.add(opaqueSlide);
        box.add(opaquePanel);

        if (getViewAttributes().colorModel == com.bbn.openmap.omGraphics.OMRasterObject.COLORMODEL_DIRECT) {

            JPanel qualityPanel = PaletteHelper.createPaletteJPanel("Image JPEG Quality/Time");
            JSlider qualitySlide = new JSlider(JSlider.HORIZONTAL, 0/* min */, 100/* max */, (int) (((CRFPClient) frameProvider).jpegQuality * 100)/* inital */);
            java.util.Hashtable dict2 = new java.util.Hashtable();
            dict2.put(new Integer(0), new JLabel("Less"));
            dict2.put(new Integer(100), new JLabel("More"));
            qualitySlide.setLabelTable(dict2);
            qualitySlide.setPaintLabels(true);
            qualitySlide.setMajorTickSpacing(20);
            qualitySlide.setPaintTicks(true);
            qualitySlide.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider = (JSlider) ce.getSource();
                    if (!slider.getValueIsAdjusting()) {
                        ((CRFPClient) getFrameProvider()).jpegQuality = (float) (slider.getValue()) / 100f;
                        fireRequestInfoLine("RPF Image JPEG Quality set to "
                                + ((CRFPClient) getFrameProvider()).jpegQuality
                                + " for future requests.");
                    }
                }
            });
            qualityPanel.add(qualitySlide);

            box.add(qualityPanel);
        }

        JPanel subbox2 = new JPanel();
        JButton redraw = new JButton("Redraw RPF Layer");
        redraw.addActionListener(this);
        subbox2.add(redraw);
        box.add(subbox2);

        return box;
    }
}