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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDLocator.java,v $
// $RCSfile: DTEDLocator.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/13 01:21:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import java.io.*;
import java.util.*;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.wanderer.Wanderer;
import com.bbn.openmap.util.wanderer.WandererCallback;

/** 
 * DTEDLocator finds DTED frame files, and organizes them by level,
 * longitude and latitude.<P>
 * <pre>
 * Usage: java com.bbn.openmap.dataAccess.dted.DTEDLocator (dir path) ...
 * </pre>
 */
public class DTEDLocator extends Wanderer implements WandererCallback {

    boolean DEBUG = false;

    protected DTEDNameTranslator translator;

    protected LinkedList filenames = new LinkedList();

    protected File[][][] files = null;
    protected int numLevels = 3;

    /**
     * Create a DTEDLocator, expect to set the top level DTED
     * directory later.
     */
    public DTEDLocator() {
	super();
	if (Debug.debugging("dted")) {
	    DEBUG = true;
	}
	setCallback(this);
    }

    /**
     * Create the DTEDLocator and start searching from the directory
     * specificed.
     */
    public DTEDLocator(String directory) {
	this(new File(directory));
    }

    /**
     * Create the DTEDLocator and start searching from the directory
     * specificed.
     */
    public DTEDLocator(File dtedDir) {
	this();
	handleEntry(dtedDir);
    }

    /**
     * Initialize the holding arrays.
     */
    protected void initFileHolder() {
	files = new File[numLevels][180][360]; // level,lat, lon
    }

    /**
     * Does nothing, nothing is done for directories.
     */
    public void handleDirectory(File directory) {
	if (DEBUG) {
	    Debug.output("DTEDLocator: skipping: " + directory.getAbsolutePath());
	}
	// Do nothing to directories
    }

    /**
     * When a file is found, add it.
     */
    public void handleFile(File file) {
	if (DEBUG) {
	    Debug.output("DTEDLocator: searching finds: " + file.getAbsolutePath());
	}
	filenames.add(file);
    }

    /**
     * Get the DTEDNameTranslator that knows how to interpret where a
     * DTED file covers based on its name.
     */
    public DTEDNameTranslator getTranslator() {
	if (translator == null) {
	    translator = new StandardDTEDNameTranslator();
	}

	return translator;
    }

    /**
     * Set the DTEDNameTranslator that knows how to interpret where a
     * DTED file covers based on its name.
     */
    public void setTranslator(DTEDNameTranslator dnt) {
	translator = dnt;
    }

    /**
     * After all the files have been located, organized them spatially
     * in the 3D array.
     */
    public void organize() {
	if (DEBUG) {
	    Debug.output("DTEDLocator: organizing frames...");
	}
	initFileHolder();
	Iterator it = filenames.iterator();
	DTEDNameTranslator dnt = getTranslator();
	
	while (it.hasNext()) {
	    File file = (File) it.next();
	    String filename = file.getAbsolutePath();
	    try {
		dnt.set(filename);
		
		int l = dnt.getLevel();
		int lt = (int)(dnt.getLat() + 90);
		int ln = (int)(dnt.getLon() + 180);

		if (DEBUG) {
		    Debug.output("  placing " + filename + " at files[" + l +
				 "][" + lt + "][" + ln + "]");
		}

		files[l][lt][ln] = file;

	    } catch (FormatException fe) {
		continue;
	    } catch (ArrayIndexOutOfBoundsException aioobe) {
		continue;
	    }
	}
    }

    /**
     * Get the File object for a latitude, longitude and level.
     */
    public File get(float lat, float lon, int level) {
	// Need to offset lat/lon to indexes.

	try {
	    return files[level][(int)(lat + 90)][(int)(lon + 180)];
	} catch (NullPointerException npe) {
	    organize();
	    return get(lat, lon, level);
	} catch (ArrayIndexOutOfBoundsException aioobe) {
	    
	}
	return null;
    }

    /**
     * Given a set of files or directories, parade through them to
     * find files that end with '`', or files that start with '.#',
     * and delete them.
     * @param argv paths to files or directories, use -h to get a
     * usage statement.  
     */
    public static void main(String[] argv) {
	Debug.init();
	boolean toUpper = true;

	ArgParser ap = new ArgParser("DTEDLocator");

	if (argv.length == 0) {
	    ap.bail("", true);
	}

	DTEDLocator locator = new DTEDLocator();

	// Assume that the arguments are paths to directories or
	// files.
	for (int i = 0; i < argv.length; i++) {
	    locator.handleEntry(new File(argv[i]));
	}
    }
}
