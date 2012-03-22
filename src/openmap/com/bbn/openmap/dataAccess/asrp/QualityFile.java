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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/QualityFile.java,v
// $
// $RCSfile: QualityFile.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.io.IOException;

import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFRecord;
import com.bbn.openmap.util.Debug;

public class QualityFile extends GeneralASRPFile {

    public final static String SECURITY_AND_RELEASE = "QSR";
    public final static String UP_TO_DATENESS = "QUV";
    public final static String COLOUR_CODE_ID = "COL";
    public final static String OTHER_QUALITY_INFORMATION = "QOI";
    public final static String HORIZONTAL_ACCURACY = "ASH";
    public final static String BOUNDING_POLYGON_COORDINATES = "RCI";
    public final static String VERTICAL_ACCURACY = "ASV";

    public QualityFile(String fileName) throws IOException {
        DDFModule mod = load(fileName);

        if (mod != null) {
            DDFRecord record;
            while ((record = mod.readRecord()) != null) {
                loadField(record, SECURITY_AND_RELEASE, 0);
                loadField(record, UP_TO_DATENESS, 0);
                loadField(record, COLOUR_CODE_ID, 0);
                loadField(record, OTHER_QUALITY_INFORMATION, 0);
                loadField(record, HORIZONTAL_ACCURACY, 0);
                loadField(record, BOUNDING_POLYGON_COORDINATES, 0);
                loadField(record, VERTICAL_ACCURACY, 0);
            }
        }
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: QualityFile filename");
        }

        try {
            QualityFile thf = new QualityFile(argv[0]);
            thf.dumpFields();
        } catch (IOException ioe) {
            Debug.error(ioe.getMessage());
        }

        System.exit(0);
    }
}