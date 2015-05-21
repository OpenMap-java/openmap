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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/BasicLocationHandler.java,v $
// $RCSfile: BasicLocationHandler.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.location;

/*  Java Core  */
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import com.bbn.openmap.omGraphics.OMGraphicList;

/**
 * A basic location handler, that just returns simple testing locations.
 */
public class BasicLocationHandler extends AbstractLocationHandler {

    protected final OMGraphicList graphicList = new OMGraphicList();

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public BasicLocationHandler() {
        reloadData();
    }

    public void reloadData() {
        Color[] colors = new Color[8];
        colors[0] = Color.red;
        colors[1] = Color.green;
        colors[2] = Color.yellow;
        colors[3] = Color.blue;
        colors[4] = Color.black;
        colors[5] = Color.white;
        colors[6] = Color.orange;
        colors[7] = Color.pink;

        graphicList.clear();

        for (int i = 0; i < 10; i++) {
            // Sprinkle some randomness in the values
            double ran = Math.random() * 10;
            boolean dir = Math.random() > .5;
            if (!dir) {
                ran *= -1;
            }

            Location location = new BasicLocation(42f + ran, -72f + ran, "testing" + i, null);
            location.setLocationHandler(this);
            location.getLabel().setLinePaint(colors[i % 8]);
            // location.getLabel().setShowBounds(true);
            location.setShowName(true);
            location.setShowLocation(true);
            graphicList.add(location);
        }
    }

    /**
     * Called by the LocationLayer when the layer is removed from the map. The
     * LocationHandler should release expensive resources if this is called.
     */
    public void removed(java.awt.Container cont) {
    }

    public OMGraphicList get(double nwLat, double nwLon, double seLat, double seLon,
                             OMGraphicList graphicList) {
        graphicList.addAll(this.graphicList);
        return graphicList;
    }

    protected Box box = null;

    /**
     * Provides the palette widgets to control the options of showing maps, or
     * attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public Component getGUI() {
        if (box == null) {
            JCheckBox showLocationCheck, showNameCheck;
            JButton rereadFilesButton;

            showLocationCheck = new JCheckBox("Show Locations", isShowLocations());
            showLocationCheck.setActionCommand(showLocationsCommand);
            showLocationCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox locationCheck = (JCheckBox) ae.getSource();
                    setShowLocations(locationCheck.isSelected());
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("CSVLocationHandler::actionPerformed showLocations is "
                                + isShowLocations());
                    }
                    getLayer().repaint();
                }
            });
            showLocationCheck.setToolTipText("<HTML><BODY>Show location markers on the map.</BODY></HTML>");

            showNameCheck = new JCheckBox("Show Location Names", isShowNames());
            showNameCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox namesCheck = (JCheckBox) ae.getSource();
                    setShowNames(namesCheck.isSelected());
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("CSVLocationHandler::actionPerformed showNames is "
                                + isShowNames());
                    }

                    LocationLayer ll = getLayer();
                    if (namesCheck.isSelected() && ll.getDeclutterMatrix() != null
                            && ll.getUseDeclutterMatrix()) {
                        ll.doPrepare();
                    } else {
                        ll.repaint();
                    }
                }
            });
            showNameCheck.setToolTipText("<HTML><BODY>Show location names on the map.</BODY></HTML>");

            rereadFilesButton = new JButton("Reload Data From Source");
            rereadFilesButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Re-reading Locations file");
                    }
                    reloadData();
                    getLayer().doPrepare();
                }
            });
            rereadFilesButton.setToolTipText("<HTML><BODY>Reload the data file, and put these settings<br>on the individual map objects.</BODY></HTML>");

            box = Box.createVerticalBox();
            box.add(showLocationCheck);
            box.add(showNameCheck);
            box.add(rereadFilesButton);
        }
        return box;
    }

}