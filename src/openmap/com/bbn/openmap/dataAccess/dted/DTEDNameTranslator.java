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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDNameTranslator.java,v $
// $RCSfile: DTEDNameTranslator.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/13 01:21:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.dted;

import java.io.File;

import com.bbn.openmap.io.FormatException;

/**
 * An interface that defines a object that knows how to define the
 * name of a DTED file from a set of coordinates and dted level, and
 * also knows how to translate the name of a DTED file to it's
 * coverage coordinates and level.  Keeps track of the file name, the
 * sub-directory path from the top level dted directory to the file,
 * and the location of the dted directory if it is specified.
 */
public interface DTEDNameTranslator {

    public void set(String filePath) throws FormatException;

    public void set(String dtedDir, double lat, double lon, int level);

    public void set(double lat, double lon, int level);

    public void setLat(double lat);

    public double getLat();

    public void setLon(double lon);

    public double getLon();

    public void setLevel(int level);

    public int getLevel();

    public String getName();

    public void setName(String fileName) throws FormatException;

    public String getSubDirs();

    public void setDTEDDir(String dtedDirectory);

    public String getDTEDDir();
}
