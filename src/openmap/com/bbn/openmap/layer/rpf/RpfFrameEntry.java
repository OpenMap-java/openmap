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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFrameEntry.java,v $
// $RCSfile: RpfFrameEntry.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:10 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.bbn.openmap.util.*;

/**
 * The RpfFrameEntry is a description of a RPF frame file that can be
 * used to quickly gain status about the frame.  It is mainly created
 * by the table of contents handler (RpfTocHandler), and passed, in an
 * array of brothers in a RpfTocEntry, to the cache handler.  The cache
 * handler will use the RpfTocEntry to figure out which frames are
 * needed to get the subframes it wants, and the RpfFrameEntry supplies
 * information to assist in loading that frame file.
 */
public class RpfFrameEntry {
    
    /** Whether the file exists or not.*/
    public boolean exists;
    /** Absolute path to Rpf dir. */
    public String rpfdir; 
    /** Rpf to frame dir path. */
    public String directory;
    /** Frame file name. */
    public String filename; // [16]
    /** Real path to the frame file. */
    public String framePath;
    /**Used by the RpfTocHandler to create disk usage estimates. */
    public long diskspace;

    public RpfFrameEntry(){
        exists = false;
        directory = null;
    }
    
    public String toString(){
        StringBuffer s = new StringBuffer();
        s.append("File Name: " + filename + "\n");
        s.append("In Directory: " + directory + "\n");
        s.append("Is Located At: " + framePath + "\n");
        s.append("Exists: " + exists + "\n");
        s.append("Size: " + diskspace);
        return s.toString();
    }
}


