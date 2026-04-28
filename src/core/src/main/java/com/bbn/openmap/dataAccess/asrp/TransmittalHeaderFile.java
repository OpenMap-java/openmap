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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/TransmittalHeaderFile.java,v $
// $RCSfile: TransmittalHeaderFile.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.bbn.openmap.dataAccess.iso8211.DDFField;
import com.bbn.openmap.dataAccess.iso8211.DDFModule;
import com.bbn.openmap.dataAccess.iso8211.DDFRecord;
import com.bbn.openmap.dataAccess.iso8211.DDFSubfield;
import com.bbn.openmap.util.Debug;

/**
 * The transmitter header file contains information about various ASRP
 * directories, their names and coverages. This object can be used to
 * coordinate the display of ASRP images from different directories.
 */
public class TransmittalHeaderFile extends GeneralASRPFile implements
        ASRPConstants {

    public final static String TRANSMITTAL_RECORD = "VDR";
    public final static String DATA_SET_DESCRIPTION = "FDR";
    public final static String SECURITY_AND_RELEASE = "QSR";
    public final static String UP_TO_DATENESS = "QUV";

    protected String rootDir;

    public TransmittalHeaderFile(String fileName) throws IOException {
        DDFModule mod = load(fileName);
        rootDir = fileName.substring(0, fileName.indexOf(TRANS));

        if (mod != null) {
            DDFRecord record;
            while ((record = mod.readRecord()) != null) {
                for (Iterator it = record.iterator(); it.hasNext(); addField((DDFField) it.next())) {
                }
            }
        }
    }

    /**
     * Returns the root directory of the ASRP data, with the trailing
     * file separator still attached.
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * Return a java.util.List of ASRPDirectories known by this THF
     * file.
     */
    public List getASRPDirectories() {
        LinkedList asrpDirs = new LinkedList();
        List fields = getFields(TransmittalHeaderFile.DATA_SET_DESCRIPTION);
        for (Iterator it = fields.iterator(); it.hasNext();) {
            DDFField ddf = (DDFField) it.next();
            if (ddf.getFieldDefn().getName().equals("FDR")) {
                List datasets = ddf.getSubfields("NAM");

                for (Iterator it2 = datasets.iterator(); it2.hasNext();) {
                    DDFSubfield ddfs = (DDFSubfield) it2.next();
                    String asrpdString = rootDir + "ASRP/" + ddfs.stringValue();
                    if (Debug.debugging("asrp")) {
                        Debug.output("TransmittalHeaderFile creating "
                                + asrpdString + " from " + rootDir + TRANS);
                    }
                    asrpDirs.add(new ASRPDirectory(asrpdString));
                }
            }
        }

        return asrpDirs;
    }

    public static void main(String[] argv) {
        Debug.init();

        if (argv.length < 1) {
            Debug.output("Usage: TransmittalHeaderFile filename");
        }

        try {
            TransmittalHeaderFile thf = new TransmittalHeaderFile(argv[0]);
            thf.dumpFields();
        } catch (IOException ioe) {
            Debug.error(ioe.getMessage());
        }
        System.exit(0);
    }
}