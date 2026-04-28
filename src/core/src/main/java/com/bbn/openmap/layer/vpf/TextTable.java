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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/TextTable.java,v $
// $Revision: 1.7 $ $Date: 2009/01/21 01:24:41 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Read VPF format text tables to generate text graphics for OpenMap.
 */
public class TextTable extends PrimitiveTable {

    /** the column that our coordinates are in */
    final private int coordColumn;

    /** the column that the text info is in */
    final private int textColumn;

    /**
     * Construct a TextTable for reading VPF text features.
     * 
     * @param cov the CoverageTable for the tile
     * @param tile the tile to parse
     * @exception FormatException if something goes wrong reading the text
     */
    public TextTable(CoverageTable cov, TileDirectory tile) throws FormatException {
        super(cov, tile, "txt");
        if ((coordColumn = whatColumn("shape_line")) == -1) {
            throw new FormatException("texttable couldn't get " + "shape_line column");
        }
        if ((textColumn = whatColumn("string")) == -1) {
            throw new FormatException("texttable couldn't get " + "string column");
        }
    }

    /**
     * Returns the coordinate string for the text primitive
     * 
     * @param textprim the text primitive
     */
    public CoordFloatString getCoordinates(List<Object> textprim) {
        return (CoordFloatString) textprim.get(coordColumn);
    }

    /**
     * Parse the text records for this tile, calling warehouse.createText once
     * for each record in the selection region.
     * 
     * @param warehouse the warehouse used for createText calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @see VPFGraphicWarehouse#createText
     */
    public void drawTile(VPFGraphicWarehouse warehouse, double dpplat, double dpplon,
                         LatLonPoint ll1, LatLonPoint ll2) {

        double ll1lat = ll1.getY();
        double ll1lon = ll1.getX();
        double ll2lat = ll2.getY();
        double ll2lon = ll2.getX();

        try {
            for (List<Object> text = new ArrayList<Object>(); parseRow(text);) {
                String textval = (String) text.get(textColumn);
                CoordFloatString coords = (CoordFloatString) text.get(coordColumn);
                double lat = coords.getYasFloat(0);
                double lon = coords.getXasFloat(0);

                if ((lat > ll2lat) && (lat < ll1lat) && (lon > ll1lon) && (lon < ll2lon)) {
                    warehouse.createText(covtable, this, text, lat, lon, textval);
                }
            }
        } catch (FormatException f) {
            System.out.println("Exception: " + f.getClass() + " " + f.getMessage());
        }
    }

    /**
     * Use the warehouse to create a graphic from a feature in the TextTable.
     * 
     * @param warehouse the warehouse used for createText calls (must not be
     *        null)
     * @param dpplat threshold for latitude thinning (passed to warehouse)
     * @param dpplon threshold for longitude thinngin (passed to warehouse)
     * @param ll1 upperleft of selection region (passed to warehouse)
     * @param ll2 lowerright of selection region (passed to warehouse)
     * @param text a list with the TextTable row contents.
     * @param featureType the string representing the feature type, in case the
     *        warehouse wants to do some intelligent rendering.
     * @param primID the primitive ID of the feature, in order to gather attributes if
     *        necessary.
     * @see VPFGraphicWarehouse#createText
     */
    public OMGraphic drawFeature(VPFFeatureWarehouse warehouse, double dpplat, double dpplon,
                                 LatLonPoint ll1, LatLonPoint ll2, List<Object> text,
                                 String featureType, int primID) {

        if (warehouse != null) {

            double ll1lat = ll1.getY();
            double ll1lon = ll1.getX();
            double ll2lat = ll2.getY();
            double ll2lon = ll2.getX();

            String textval = (String) text.get(textColumn);
            CoordFloatString coords = (CoordFloatString) text.get(coordColumn);

            double lat = coords.getYasFloat(0);
            double lon = coords.getXasFloat(0);
            if ((lat > ll2lat) && (lat < ll1lat) && (lon > ll1lon) && (lon < ll2lon)) {
                return warehouse.createText(covtable, this, text, lat, lon, textval, featureType, primID);
            }
        }
        return null;
    }
}