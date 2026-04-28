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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/asrp/ASRPConstants.java,v
// $
// $RCSfile: ASRPConstants.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:40 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.asrp;

public interface ASRPConstants {

    /** File suffix for General Information File. */
    public final static String GEN_NAME = "GEN";
    /** File suffix for Geo Reference File. */
    public final static String GER_NAME = "GER";
    /** File suffix for Source File. */
    public final static String SOURCE_NAME = "SOU";
    /** File suffix for Quality File. */
    public final static String QAL_NAME = "QAL";
    /** Transmitter header file name. Always. */
    public final static String TRANS = "TRANSH01.THF";
    /** File suffix for Main Raster Image. */
    public final static String IMAGE_NAME = "IMG";

    // The Raster legend image is Lcc, where 'cc' is the number
    // assigned to the image's source graphic.

}