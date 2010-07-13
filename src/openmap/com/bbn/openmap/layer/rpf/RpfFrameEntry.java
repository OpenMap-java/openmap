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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfFrameEntry.java,v $
// $RCSfile: RpfFrameEntry.java,v $
// $Revision: 1.4 $
// $Date: 2006/08/17 15:19:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

/**
 * The RpfFrameEntry is a description of a RPF frame file that can be used to
 * quickly gain status about the frame. It is mainly created by the table of
 * contents handler (RpfTocHandler), and passed, in an array of brothers in a
 * RpfTocEntry, to the cache handler. The cache handler will use the RpfTocEntry
 * to figure out which frames are needed to get the subframes it wants, and the
 * RpfFrameEntry supplies information to assist in loading that frame file.
 */
public class RpfFrameEntry {

    /** Whether the file exists or not. */
    public boolean exists;
    /** Real path to the frame file. */
    public String framePath;

    /**
     * Index of the start of the frame file. To get the frame name, call
     * framePath.substring(filenameIndex).
     */
    public short filenameIndex = 0;
    /**
     * Index to the start of the RPF directory in the name. To get the RPF
     * directory without the following slash, call framePath.substring(0,
     * rpfdirIndex + 2). Use 3 to get the slash. You can use this index + 3 as
     * the startIndex to get the relative path to the file from the RPF dir,
     * without the slash.
     * <P>
     * If this index is -1, that means that the complete pathname was specified
     * in the OpenMap-generated A.TOC file, and you'll have to do a search for
     * the last RPF instance yourself to get that index.
     */
    public short rpfdirIndex = -1;

    public RpfFrameEntry() {
        exists = false;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("Is Located At: ").append(framePath).append("\n");
        s.append("Exists: ").append(exists).append("\n");
        return s.toString();
    }
}
