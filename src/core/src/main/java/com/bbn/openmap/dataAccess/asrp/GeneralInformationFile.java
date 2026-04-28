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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/GeneralInformationFile.java,v
// $
// $RCSfile: GeneralInformationFile.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.awt.Dimension;
import java.io.IOException;

import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFRecord;
import com.bbn.openmap.util.Debug;

public class GeneralInformationFile extends GeneralASRPFile {

    public final static String RECORD_ID = "001";
    public final static String DATA_SET_ID = "DSI";
    public final static String GENERAL_INFORMATION = "GEN";
    public final static String DATA_SET_PARAMETERS = "SPR";
    public final static String BAND_ID = "BDF";
    public final static String TILE_INDEX_MAP = "TIM";
    public final static String DATASET_DESCRIPTION = "DRF";

    protected Dimension subframeDimensions;

    public GeneralInformationFile(String fileName) throws IOException {
        DDFModule mod = load(fileName);

        if (mod != null) {
            DDFRecord record;
            while ((record = mod.readRecord()) != null) {
                loadField(record, DATA_SET_ID, 0);
                loadField(record, GENERAL_INFORMATION, 0);
                loadField(record, DATA_SET_PARAMETERS, 0);
                loadField(record, BAND_ID, 0);
                loadField(record, TILE_INDEX_MAP, 0);
                loadField(record, DATASET_DESCRIPTION, 0);
            }
        }
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: GeneralInformationFile filename");
        }

        try {
            GeneralInformationFile gen = new GeneralInformationFile(argv[0]);
            gen.dumpFields();
        } catch (IOException ioe) {
            Debug.error(ioe.getMessage());
        }
        System.exit(0);
    }
}