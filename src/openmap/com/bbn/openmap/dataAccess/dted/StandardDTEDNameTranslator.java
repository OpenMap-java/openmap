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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/StandardDTEDNameTranslator.java,v $
// $RCSfile: StandardDTEDNameTranslator.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/13 01:21:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.dted;

import java.io.File;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.dted.DTEDFrameUtil;
import com.bbn.openmap.util.Debug;

/**
 * An class implemented the DTEDNameTranslator interface that handles
 * DTED file names as specified in the DTED specification. These file
 * names follow this naming convention:<P>
 * <pre>
 *
 * dted/[e|w]xxx/[n|s]yy.dt[level]
 *
 * </pre>
 */
public class StandardDTEDNameTranslator implements DTEDNameTranslator {
    protected boolean DEBUG = Debug.debugging("dtedname");

    protected String filename = null;
    protected String subDirs = null;
    protected String dtedDir = "";
    protected double lat;
    protected double lon;
    protected int level;

    public StandardDTEDNameTranslator() {}

    public StandardDTEDNameTranslator(String dtedDir, double lat, double lon, int level) {
	set(dtedDir, lat, lon, level);
    }

    public StandardDTEDNameTranslator(double lat, double lon, int level) {
	set(null, lat, lon, level);
    }

    public void set(String filePath) throws FormatException {
	evaluate(filePath);
    }

    public void set(String dtedDir, double lat, double lon, int level) {
	setDTEDDir(dtedDir);
	filename = evaluate(lat, lon, level);
    }

    public void set(double lat, double lon, int level) {
	set(null, lat, lon, level);
    }

    public void setLat(double latitude) {
	filename = evaluate(latitude, getLon(), getLevel());
    }
    
    public double getLat() {
	return lat;
    }

    public double getLon() {
	return lon;
    }

    public void setLon(double longitude) {
	filename = evaluate(getLat(), longitude, getLevel());
    }

    public int getLevel() {
	return level;
    }

    public void setLevel(int level) {
	filename = evaluate(getLat(), getLon(), level);
    }

    public String getSubDirs() {
	return subDirs;
    }

    public String getName() {
	return filename;
    }

    public void setName(String fileName) throws FormatException {
	evaluate(fileName);
    }

    public void setDTEDDir(String dtedDirectory) {
	if (dtedDirectory == null) {
	    dtedDirectory = "";
	}
	dtedDir = dtedDirectory;
    }

    public String getDTEDDir() {
	return dtedDir;
    }

    protected String evaluate(double lat, double lon, int level) {
	this.lat = Math.floor(lat);
	this.lon = Math.floor(lon);
	this.level = level;
	return DTEDFrameUtil.lonToFileString((float)lon) + "/" +
	    DTEDFrameUtil.latToFileString((float)lat, level);
    }

    protected void evaluate(String filePath) throws FormatException {

	try {
	    int latSlash = filePath.lastIndexOf("/");
	    if (latSlash > 1) {
		if (DEBUG) {
		    Debug.output("Have lat index of " + latSlash);
		}
		String lonSearch = filePath.substring(0, latSlash);
		
		if (DEBUG) {
		    Debug.output("Searching for lon index in " + lonSearch);
		}
		int lonSlash = lonSearch.lastIndexOf("/");
		if (lonSlash > 1) {
		    filename = filePath.substring(latSlash + 1);
		    String latString = filename.toUpperCase();

		    if (DEBUG) {
			Debug.output("have lat " + latString);
		    }

		    int dotIndex = latString.indexOf(".");
		    if (dotIndex > 0) {

			lat = Double.parseDouble(latString.substring(1, dotIndex));
			if (latString.charAt(0) == 'S') {
			    lat *= -1;
			}

			subDirs = filePath.substring(lonSlash + 1, latSlash);
			String dd = filePath.substring(0, lonSlash + 1);
			if (dd.length() > 0) {
			    dtedDir = dd;
			}

			String lonString = subDirs.toUpperCase();

			if (DEBUG) {
			    Debug.output("have lon " + lonString);
			}

			lon = Double.parseDouble(lonString.substring(1));
			if (lonString.charAt(0) == 'W') {
			    lon *= -1;
			}

			level = (int) Integer.parseInt(filePath.substring(filePath.length() - 1));
			if (DEBUG) {
			    Debug.output("have level " + level);
			}
			return;
		    }
		}
	    }
	} catch (NumberFormatException nfe) {

	}

	throw new FormatException("StandardDTEDNameTranslator couldn't convert " + filePath + 
				  " to valid parameters");
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("For file: " + filename + "\n");
	sb.append("  lat = " + lat + "\n");
	sb.append("  lon = " + lon + "\n");
	sb.append("  level = " + level + "\n");
	String dd = getDTEDDir();
	if (dd.length() > 0) {
	    dd += "/";
	}
	sb.append("  path = " + dd + getSubDirs() + "/" + getName() + "\n");
	return sb.toString();
    }

    public static void main(String[] argv) {
	Debug.init();
	if (argv.length == 0) {
	    Debug.output("Usage:  StandardDTEDNameTranslator <dted file path>");
	    System.exit(0);
	}

	StandardDTEDNameTranslator sdnt = new StandardDTEDNameTranslator();
	try {
	    sdnt.set(argv[0]);
	    Debug.output(sdnt.toString());
	} catch (FormatException fe) {
	    Debug.output(fe.getMessage());
	}
    }

}
