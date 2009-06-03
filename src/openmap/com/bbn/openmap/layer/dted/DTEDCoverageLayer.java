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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCoverageLayer.java,v $
// $RCSfile: DTEDCoverageLayer.java,v $
// $Revision: 1.3.2.2 $
// $Date: 2004/10/14 18:27:03 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

/*  Java Core  */
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This is a Layer that provides coverage information on the DTED data. There is
 * a palette for this layer, that lets you turn off the coverage for different
 * levels of DTED. Right now, only level 0, 1 and 2 are handled.
 * 
 * <pre>
 * 
 *  The properties for this file are:
 *  
 *  # Java DTED properties
 *  dtedcov.class=com.bbn.openmap.layer.dted.DTEDCoverageLayer
 *  dtedcov.prettyName=DTED Coverage
 *  # This property should reflect the paths to the DTED directories
 *  #dtedcov.paths=/tmp/data/dted
 * </pre>
 */
public class DTEDCoverageLayer extends OMGraphicHandlerLayer {

    /** The paths to the DTED directories, telling where the data is. */
    protected String[] paths;

    protected DTEDCoverageManager coverageManager = null;

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public DTEDCoverageLayer() {
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /** Method that sets all the variables to the default values. */
    protected void setDefaultValues() {
        paths = null;
    }

    /**
     * Set all the DTED properties from a properties object.
     * 
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

        super.setProperties(prefix, properties);
        setDefaultValues();

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        paths = PropUtils.initPathsFromProperties(properties, prefix
                + DTEDLayer.DTEDPathsProperty);
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the dtedcov.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     */
    public synchronized OMGraphicList prepare() {

        if (isCancelled()) {
            Debug.message("dtedcov", getName()
                    + "|DTEDCoverageLayer.prepare(): aborted.");
            return null;
        }

        Debug.message("basic", getName()
                + "|DTEDCoverageLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        Projection projection = getProjection();
        // call getRectangle();
        if (Debug.debugging("dtedcov")) {
            Debug.output(getName() + "|DTEDCoverageLayer.prepare(): "
                    + "calling prepare with projection: " + projection
                    + " ul = " + projection.getUpperLeft() + " lr = "
                    + projection.getLowerRight());
        }

        // IF the coverage manager has not been set up yet, do it!
        if (coverageManager == null) {
            coverageManager = new DTEDCoverageManager(paths);

            if (Debug.debugging("dtedcov")) {
                Debug.output(getName()
                        + "|DTEDCoverageLayer.prepare(): created DTEDCoverageManager");
            }
        }

//        float[] coverage = coverageManager.getCoverage(projection);
        
        return coverageManager.getCoverageRects(projection);
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
        JPanel panel = null;
        if (coverageManager != null) {
            panel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            panel.setLayout(gridbag);

            Component gui = coverageManager.getGUI();
            c.gridx = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0f;
            c.weighty = 1.0f;
            gridbag.setConstraints(gui, c);
            panel.add(gui);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0f;
            String interString = i18n.get(DTEDCoverageManager.class,
                    "reset",
                    "Reset");
            JButton reset = new JButton(interString);
            reset.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    coverageManager.clear();
                    doPrepare();
                }
            });
            gridbag.setConstraints(reset, c);
            panel.add(reset);
        }
        return panel;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public DTEDCoverageManager getCoverageManager() {
        return coverageManager;
    }

    public void setCoverageManager(DTEDCoverageManager coverageManager) {
        this.coverageManager = coverageManager;
    }

}