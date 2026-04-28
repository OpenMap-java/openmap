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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/SourceFile.java,v
// $
// $RCSfile: SourceFile.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.io.IOException;

import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFRecord;
import com.bbn.openmap.util.Debug;

public class SourceFile extends GeneralASRPFile {

    public final static String SOURCE_SUMMARY = "SGF";
    public final static String SOURCE = "SOR";
    public final static String MAGNETIC_INFORMATION = "MAG";
    public final static String BOUNDING_POLYGON_COORDINATES = "RCI";
    public final static String PROJECTION_FIELD = "PRR";
    public final static String SECURITY_AND_RELEASE = "QSR";
    public final static String INSET = "INS";
    public final static String COPYRIGHT = "CPY";
    public final static String LEGEND = "LGI";
    public final static String DATA_SET_PARAMETERS = "SPR";
    public final static String TILE_INDEX_MAP = "TIM";
    public final static String NORMALIZATION_CONSTANTS = "NCD";
    public final static String SOURCE_DATUM_COEFFICIENTS_DATA = "SDC";
    public final static String MAP_PROJECTIONS_COEFFICIENTS_DATA = "MPC";
    public final static String SUPPLEMENTARY_TEXT = "SUP";

    public SourceFile(String fileName) throws IOException {
        DDFModule mod = load(fileName);

        if (mod != null) {
            DDFRecord record;
            while ((record = mod.readRecord()) != null) {
                loadField(record, SOURCE_SUMMARY, 0);
                loadField(record, SOURCE, 0);
                loadField(record, MAGNETIC_INFORMATION, 0);
                loadField(record, BOUNDING_POLYGON_COORDINATES, 0);
                loadField(record, PROJECTION_FIELD, 0);
                loadField(record, SECURITY_AND_RELEASE, 0);
                loadField(record, INSET, 0);
                loadField(record, COPYRIGHT, 0);
                loadField(record, LEGEND, 0);
                loadField(record, DATA_SET_PARAMETERS, 0);
                loadField(record, TILE_INDEX_MAP, 0);
                loadField(record, NORMALIZATION_CONSTANTS, 0);
                loadField(record, SOURCE_DATUM_COEFFICIENTS_DATA, 0);
                loadField(record, MAP_PROJECTIONS_COEFFICIENTS_DATA, 0);
                loadField(record, SUPPLEMENTARY_TEXT, 0);
            }
        }
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: SourceFile filename");
        }

        try {
            SourceFile thf = new SourceFile(argv[0]);
            thf.dumpFields();
        } catch (IOException ioe) {
            Debug.error(ioe.getMessage());
        }
        System.exit(0);
    }
}