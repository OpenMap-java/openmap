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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LibrarySelectionTable.java,v $
// $RCSfile: LibrarySelectionTable.java,v $
// $Revision: 1.6 $
// $Date: 2003/12/29 20:35:09 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.io.File;
import java.util.*;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.io.*;
import com.bbn.openmap.util.Debug;

/**
 * Reads the VPF LibraryAttribute table and constructs
 * CoverageAttributeTables for each of the library coverages (north
 * america, browse, etc) that exist.
 *
 * <p>NOTE: This class maintains a whole bunch of cached information, and
 * also hangs onto references to classes that cache even more information.
 * When using this class, you are much better off sharing an instance of 
 * this class, rather than creating multiple instantiations of it for the
 * same VPF data directory.
 *
 * @see CoverageAttributeTable
 */
public class LibrarySelectionTable { 

    /** cutoff scale for browse coverage.*/
    public final static int DEFAULT_BROWSE_CUTOFF = 31000000;

    protected int BROWSE_CUTOFF = DEFAULT_BROWSE_CUTOFF;

    /** the names of the VPF libraries listed in the library attribute table */
    private String libraryname[] = null;  //library [i]
    /** the bounding rectangle of the respective libraries */
    private Map boundrec = new HashMap();//bounding rect as [W,S,E,N]
    /** the CoverageAttributeTables corresponding to the different libs */
    private Map CATs = new HashMap();
    /** the names of the lat columns */
    final private static String LATColumns[] = {Constants.LAT_LIBNAME, Constants.LAT_XMIN, Constants.LAT_YMIN, Constants.LAT_XMAX, Constants.LAT_YMAX};
    /** the expected schema types for the library attribute table */
    final private static char LATschematype[] = {'T', 'F', 'F', 'F', 'F'};
    /** the expected schema lengths for the library attribute table */
    final private static int LATschemalength[] = {-1 /*8*/, 1, 1, 1, 1};
    /** the name of the database */
    private String databaseName;
    /** the database description of itself */
    private String databaseDesc;
    /** 
     * Construct a LibrarySelectionTable without a path to data.
     */
    public LibrarySelectionTable() {
    }
  
    /**
     * Construct a LibrarySelectionTable with a path to data.
     *
     * @param vpfpath the path to the base data directory; the file opened
     * is <code>vpfpath</code>/lat.
     * @exception FormatException some error was encountered while trying to 
     * handle the file.
     */
    public LibrarySelectionTable(String vpfpath) throws FormatException {
	addDataPath(vpfpath);
    }
  
    /**
     * Construct a LibrarySelectionTable with a path to data.
     *
     * @param vpfpaths the paths to the data directories; the file opened
     * is <code>vpfpath</code>/lat.
     * @exception FormatException some error was encountered while trying to 
     * handle the file.
     */
    public LibrarySelectionTable(String vpfpaths[]) throws FormatException {
        for (int i = 0; i < vpfpaths.length; i++) {
 	    addDataPath(vpfpaths[i]);
	}
    }

    /**
     * Set the cutoff scale where if the map scale number is larger
     * (smaller overall map scale), the coverage won't be returned.
     * For example, if the scale cutoff is 30000000, if the map scale
     * is 1:31000000, no map data will be returned.
     */
    public void setCutoffScale(int scale) {
	BROWSE_CUTOFF = scale;
    }

    /**
     * Get the cutoff scale where data will be retrieved.
     */
    public int getCutoffScale() {
	return BROWSE_CUTOFF;
    }

    /**
     * add a path to LibrarySelectionTable.  Adding different types of 
     * VPF libraries to the same LST is likely to cause trouble.  (e.g.
     * it would be bad to add both DCW and VMAP paths to the same LST.
     * adding each DCW disk separately is why this method exists.)
     * 
     * @param vpfpath the path to the base DCW directory; the file opened
     * is <code>vpfpath</code>/lat.
     * @exception FormatException some error was encountered while trying to 
     * handle the file.
     */
    public void addDataPath(String vpfpath) throws FormatException {
	if (Debug.debugging("vpf")) {
	    Debug.output("LST.addDataPath(" + vpfpath + ")");
	}
	// Figure out how files names should be constructed...
	boolean addSlash = true;

// 	if (vpfpath.endsWith(File.separator)) {
	if (vpfpath.endsWith("/") || vpfpath.endsWith(File.separator)) {
	    addSlash = false;
	}

	String latf = vpfpath + (addSlash?"/":"") + "lat";
	String dhtf = vpfpath + (addSlash?"/":"") + "dht";

	if (!BinaryFile.exists(latf)) {
	    latf = latf + ".";
	    dhtf = dhtf + ".";
	}

	DcwRecordFile latrf = new DcwRecordFile(latf);
	DcwRecordFile dhtrf = new DcwRecordFile(dhtf);

	List databaseVec = dhtrf.parseRow();
	int dcol = dhtrf.whatColumn("database_name");
	if (dcol != -1) {
	    databaseName = (String)databaseVec.get(dcol);
	}
	dcol = dhtrf.whatColumn("database_desc");
	if (dcol != -1) {
	    databaseDesc = (String)databaseVec.get(dcol);
	}
	dhtrf.close();
	dhtrf = null;
	
	int latcols[] = latrf.lookupSchema(LATColumns, true,
					   LATschematype,
					   LATschemalength, false);

	Debug.message("vpf", "lst.adp: looked up schema");
	List l = new ArrayList(latrf.getColumnCount());
	while (latrf.parseRow(l)) {
	    String lname = ((String)l.get(latcols[0])).toLowerCase();
	    float br[] = new float[] {
		((Float)l.get(latcols[1])).floatValue(),
		((Float)l.get(latcols[2])).floatValue(),
		((Float)l.get(latcols[3])).floatValue(),
		((Float)l.get(latcols[4])).floatValue()};
	    try {
	        CoverageAttributeTable table = new CoverageAttributeTable(vpfpath, lname);
		CATs.put(lname, table);
		boundrec.put(lname, br);
		if (Debug.debugging("vpf")) {
  		    Debug.output(lname + " "  + br[0] + " " + br[1] + " "
				 + br[2] + " " + br[3]);
		}
	    } catch (FormatException fe) {
		if (Debug.debugging("vpf")){
		    Debug.output("*****\nVPFLayer.LST: Couldn't create CoverageAttributeTable for " + vpfpath + " " + lname + " " + fe.getMessage() + "\n--- Not a problem if you have multiple paths, and " + lname + " is included in another path ---\n*****");
		    fe.printStackTrace();
		} else {
		    Debug.output("VPFLayer.LST: CAT discrepancy (run with -Ddebug.vpf for more details)");
		}
	    }
	}
	latrf.close();
	latrf=null;
    }
    
    /**
     * Return the list of libraries that this database has.
     * @return the list of libraries.  for DCW, this is typically NOAMER, 
     * BROWSE, etc.
     */
    public String[] getLibraryNames() {
        return (String[])CATs.keySet().toArray(Constants.EMPTY_STRING_ARRAY);
    }

    /**
     * Return the name of the database we are reading from.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Return the description of the database we are reading from.
     */
    public String getDatabaseDescription() {
        return databaseDesc;
    }

    /**
     * Return the coverage attribute table (list of coverages available
     * for the given library) for the given library name.
     *
     * @param library the name of the library to get the CAT for
     * @return the CoverageAttributeTable requested (null if the library 
     *   requested doesn't exist in the database)
     * @exception FormatException exceptions from opening the CAT for 
     *   the library
     */
    public CoverageAttributeTable getCAT(String library)
	throws FormatException {
        return (CoverageAttributeTable)CATs.get(library);
    }


    /**
     *
     */
    public void drawTile(int scale, int screenwidth, int screenheight,
			 String covname, VPFGraphicWarehouse warehouse,
			 LatLonPoint ll1, LatLonPoint ll2) {

	if (Debug.debugging("vpf")) {
	    Debug.output("Library selection table coverage: " + covname);
	    Debug.output("Library selection table - edges: " +
			 warehouse.drawEdgeFeatures());
	    Debug.output("Library selection table - text: " +
			 warehouse.drawTextFeatures());
	    Debug.output("Library selection table - areas: " +
			 warehouse.drawAreaFeatures());
	    Debug.output("Warehouse: " + warehouse);
	    Debug.output("Warehouse: cutoff scale " + BROWSE_CUTOFF);
	}

	// handle Dateline
	if ((scale < BROWSE_CUTOFF) && (ll1.getLongitude() > ll2.getLongitude())) {
	    drawTile(scale, screenwidth, screenheight, covname, warehouse, ll1, new LatLonPoint(ll2.getLatitude(), 180f-.00001f)/*180-epsilon*/);
	    drawTile(scale, screenwidth, screenheight, covname, warehouse,
		     new LatLonPoint(ll1.getLatitude(), -180f), ll2);
	    return;
	}

	if (Debug.debugging("vpf")) {
	    Debug.output("LST.drawTile() with scale of " + scale);
	}

	float dpplat = Math.abs((ll1.getLatitude() - ll2.getLatitude()) 
				/ screenheight);
	float dpplon = Math.abs((ll1.getLongitude() - ll2.getLongitude()) 
				/ screenwidth);
	
	int inArea = 0;
	CoverageTable redrawUntiled = null;
	String useLibrary = warehouse.getUseLibrary();

	for (Iterator i = CATs.values().iterator(); i.hasNext();) {

	    CoverageAttributeTable cat = (CoverageAttributeTable)i.next();
	    if (Debug.debugging("vpf")) {
		Debug.output("LST: checking library: " + cat.getLibraryName());
	    }

	    if (useLibrary != null && !useLibrary.equalsIgnoreCase(cat.getLibraryName())) {
		continue;
	    }

	    warehouse.resetForCAT();

	    List tiles = cat.tilesInRegion(ll1.getLatitude(),
					   ll2.getLatitude(),
					   ll2.getLongitude(),
					   ll1.getLongitude());

	    if (tiles == null) {
		redrawUntiled = cat.getCoverageTable(covname);
	    } else if (cat.isTiledData()  && (scale < BROWSE_CUTOFF)) {

  		if (!tiles.isEmpty()) {
		    CoverageTable c = cat.getCoverageTable(covname);
		    if (c == null) {
			if (Debug.debugging("vpf")) {
			    Debug.output("|LST.drawTile(): Couldn't get coverage table for " + covname + " " + cat.getLibraryName());
			}
			continue;
		    }

		    if (Debug.debugging("vpf")) {
			Debug.output("Using coverage table for "
				     + covname + " " + cat.getLibraryName());
		    }

		    inArea++;
		    for (Iterator tile = tiles.iterator(); tile.hasNext(); ) {
			c.drawTile((TileDirectory)tile.next(), warehouse,
				   ll1, ll2, dpplat, dpplon);
		    }
  		}
	    }
	}
	if ((redrawUntiled != null) && (inArea == 0)) {
	    if (Debug.debugging("vpf")) {
		Debug.output("LST drawing untiled browse data");
	    }
	    redrawUntiled.drawTile(new TileDirectory(), warehouse, ll1, ll2,
				   dpplat, dpplon);
	}
    }

    /**
     *
     */
    public void drawFeatures(int scale, int screenwidth, int screenheight,
			     String covname, VPFFeatureWarehouse warehouse,
			     LatLonPoint ll1, LatLonPoint ll2) {
	
	if (Debug.debugging("vpf")) {
	    Debug.output("LST.drawFeatures(): Library selection table coverage: " + 
			 covname);
	    Debug.output("Library selection table - edges: " +
			 warehouse.drawEdgeFeatures());
	    Debug.output("Library selection table - text: " +
			 warehouse.drawTextFeatures());
	    Debug.output("Library selection table - areas: " +
			 warehouse.drawAreaFeatures());
	    Debug.output("Warehouse: " + warehouse);
	}
	
	// handle Dateline
	if ((scale < BROWSE_CUTOFF) && (ll1.getLongitude() > ll2.getLongitude())) {
	    drawFeatures(scale, screenwidth, screenheight, covname, warehouse,
			 ll1,
			 new LatLonPoint(ll2.getLatitude(), 180f-.00001f)//180-epsilon
			 );
	    drawFeatures(scale, screenwidth, screenheight, covname, warehouse,
			 new LatLonPoint(ll1.getLatitude(), -180f), ll2);
	    return;
	}
	
	if (Debug.debugging("vpf")) {
	    Debug.output("LST.drawFeatures() with scale of " + scale);
	}

	float dpplat = Math.abs((ll1.getLatitude() - ll2.getLatitude()) 
				/ screenheight);
	float dpplon = Math.abs((ll1.getLongitude() - ll2.getLongitude()) 
				/ screenwidth);
	
	int inArea = 0;
	CoverageTable redrawUntiled = null;
	String useLibrary = warehouse.getUseLibrary();

	for (Iterator i = CATs.values().iterator(); i.hasNext();) {
	    CoverageAttributeTable cat = (CoverageAttributeTable)i.next();

	    if (useLibrary != null && !useLibrary.equalsIgnoreCase(cat.getLibraryName())) {
		continue;
	    }

	    if (scale < BROWSE_CUTOFF) {

		CoverageTable c = cat.getCoverageTable(covname);
		if (c == null) {
		    if (Debug.debugging("vpf")) {
			Debug.output("LST.getFeatures(): Couldn't get coverage table for " +
				     covname + " " + cat.getLibraryName());
		    }
		    continue;
		}

		if (Debug.debugging("vpf")) {
		    Debug.output("Using coverage table for " + covname + " " 
				 + cat.getLibraryName());
		}

		c.drawFeatures(warehouse, ll1, ll2, dpplat, dpplon);
		inArea++;
	    } else {
		// Set up to draw browse coverage, or untiled coverage
		if (Debug.debugging("vpf")) {
		    Debug.output("LST.drawTile(): Scale too small (probably) or no tiles in region.");
		}		
		redrawUntiled = cat.getCoverageTable(covname);
	    }
	}
	if ((redrawUntiled != null) && (inArea == 0)) {
	    redrawUntiled.drawFeatures(warehouse, ll1, ll2, dpplat, dpplon);
	}
    }
  
    /**
     * Given a string for a coverage type or feature type, return the
     * description for that string.  Return null if the code string
     * isn't found.
     *
     * @param coverageOrFeatureType string ID for coverage or Feature type.
     */
    public String getDescription(String coverageOrFeatureType) 
	throws FormatException {
	boolean DEBUG = Debug.debugging("vpf.lst");

 	if (DEBUG) 
	    Debug.output("LST.getDescription: " + coverageOrFeatureType);

	String[] libraries = getLibraryNames();
	for (int i = 0; i < libraries.length; i++) {
	    CoverageAttributeTable cat = getCAT(libraries[i]);
	    if (cat == null) {
		continue;
	    }
	    String[] coverages = cat.getCoverageNames();
	    for (int j = 0; j < coverages.length; j++) {
		String covname = coverages[j];
		if (coverageOrFeatureType.equalsIgnoreCase(covname)) {
 		    if (DEBUG) Debug.output("** Matches coverage " + covname);
		    return cat.getCoverageDescription(covname);
		} else {
 		    if (DEBUG) Debug.output("   Checking in coverage table " + covname);
		    CoverageTable ct = cat.getCoverageTable(covname);
		    Hashtable info = ct.getFeatureTypeInfo();
		    for (Enumeration enum = info.elements(); enum.hasMoreElements();) {
			CoverageTable.FeatureClassRec fcr = (CoverageTable.FeatureClassRec)enum.nextElement();
			String name = fcr.feature_class;
			if (coverageOrFeatureType.equalsIgnoreCase(name)) {
 			    if (DEBUG) Debug.output("** Found feature " + name);
			    return fcr.description;
			}
 			if (DEBUG) Debug.output("   checked " + name);
		    }
		}
	    }
	}
	if (DEBUG) Debug.output("-- No matches found.");
	return null;
    }

    /**
     * Just a test main to parse vpf datafiles
     * param args files to parse, plus other command line flags
     * 
     * @param args command line arguments args[0] is a path to the VPF root
     */
    public static void main (String[] args) {
	Debug.init();
	Debug.put("vpf");
        String dcwbase = null;
        if (args.length > 0) {
	    dcwbase = args[0];
	} else {
	    System.out.println("Need a path to the VPF lat. file");
	}

	try {
	    LibrarySelectionTable lst = new LibrarySelectionTable(dcwbase);
	    System.out.println("Database Name " + lst.getDatabaseName());
	    
	    String liblist[] = lst.getLibraryNames();
	    for (int j = 0; j < liblist.length; j++) {
		System.out.println("Library " + liblist[j]);
		lst.getCAT(liblist[j]);
	    }
	} catch (FormatException f) { 
	    System.err.println("*****************************************"); 
	    System.err.println("*---------------------------------------*"); 
	    System.err.println("Format error in dealing with LST");
	    System.err.println(f.getMessage()); 
	    System.err.println("*---------------------------------------*"); 
	    System.err.println("*****************************************"); 
	} 
    }
}
