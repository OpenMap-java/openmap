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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/CSVShapeInfoFile.java,v $
// $RCSfile: CSVShapeInfoFile.java,v $
// $Revision: 1.4 $
// $Date: 2007/06/21 21:39:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.net.MalformedURLException;
import java.net.URL;

import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.io.CSVFile;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.util.Debug;

/**
 * The CSVShapeInfoFile holds on to the contents of a CSV file, with
 * the intent of cross referencing the contents of the file with the
 * contents of a shape file. The order that the contents are read
 * should correspond to the order that the shapefile contents are
 * read.
 * <P>
 * NOTE: By default, the numbers that are found in the CSV file are
 * converted to Doubles. Use the load(boolean) method to control this,
 * especially if you are using the fields later as the key in a
 * Hashtable.
 */
public class CSVShapeInfoFile extends CSVFile {

    /**
     * Don't do anything special, since all defaults are set already
     */
    public CSVShapeInfoFile(String name) throws MalformedURLException {
        super(name);
    }

    /**
     * Don't do anything special, since all defaults are set already
     */
    public CSVShapeInfoFile(URL url) throws MalformedURLException {
        super(url);
    }

    /**
     * This function takes an OMGraphicList and loads each one with
     * the vector representing the records in the csv file. Each
     * graphics stores the graphic in its object slot.
     */
    public void loadIntoGraphics(OMGraphicList list) {
        if (list != null && infoRecords != null) {
            int numgraphics = list.size();

            for (int i = 0; i < numgraphics; i++) {
                try {
                    OMGraphic omg = list.getOMGraphicAt(i);
                    Integer recnum = (Integer) (omg.getAppObject());
                    // OFF BY ONE!!! The shape record numbers
                    // assigned to the records start with 1, while
                    // everything else we do starts with 0...
                    Object inforec = getRecord(recnum.intValue() - 1);
                    omg.putAttribute(ShapeConstants.SHAPE_DBF_INFO_ATTRIBUTE,
                            inforec);
                } catch (ClassCastException cce) {
                    if (Debug.debugging("shape")) {
                        cce.printStackTrace();
                    }
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
    }

}