// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/TransmittalHeaderFile.java,v $
// $RCSfile: TransmittalHeaderFile.java,v $
// $Revision: 1.1 $
// $Date: 2004/03/04 04:14:29 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.asrp;

import com.bbn.openmap.dataAccess.iso8211.*;

import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

import java.io.IOException;

public class TransmittalHeaderFile extends GeneralASRPFile {

    public final static String TRANSMITTAL_RECORD = "VDR";
    public final static String DATA_SET_DESCRIPTION = "FDR";
    public final static String SECURITY_AND_RELEASE = "QSR";
    public final static String UP_TO_DATENESS = "QUV";

    public TransmittalHeaderFile(String fileName) {
        DDFModule mod = load(fileName);

        if (mod != null) {
            DDFRecord record;
            while ((record = mod.readRecord()) != null) {
                loadField(record, TRANSMITTAL_RECORD, 0);
                loadField(record, DATA_SET_DESCRIPTION, 0);
                loadField(record, SECURITY_AND_RELEASE, 0);
                loadField(record, UP_TO_DATENESS, 0);
            }
        }
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: TransmittalHeaderFile filename");
        }
        
        TransmittalHeaderFile thf = new TransmittalHeaderFile(argv[0]);
        thf.dumpFields();
        System.exit(0);
    }
}