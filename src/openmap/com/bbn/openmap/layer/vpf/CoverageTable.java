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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/CoverageTable.java,v $
// $RCSfile: CoverageTable.java,v $
// $Revision: 1.2 $
// $Date: 2003/09/25 02:48:22 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.layer.vpf;

import java.io.*;
import java.util.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.io.*;

/**
 * Encapsulate a VPF coverage directory.  This class handles requests
 * that happen for a particular coverage type (political boundary,
 * road, etc.) for a particular library (north america, browse, etc.).
 */
public class CoverageTable {

    /** our coverage type - such as "po", "bnd", "hydro" */
    final public String covtype;
    /** the directory for our coverage type */
    final protected String tablepath;
    /** a table to cache int.vdt information */
    final private Hashtable intvdtrec = new Hashtable();
    /** a table to cache char.vdt information */
    final private Hashtable charvdtrec = new Hashtable();

    /** hack - used by EdgeTable */
    public int cachedLineSchema[] = null;
    /** hack - used by AreaTable */
    public int cachedAreaSchema[] = null; //used by areatable
    /** hack - used by TextTable */
    public int cachedTextSchema[] = null; //used by texttable
    /** hack - used by nodetable */
    public int cachedEPointSchema[] = null;
    /** hack - used by nodetable */
    public int cachedCPointSchema[] = null;

    /** featureclasses used for the line feature type */
    public FeatureClassInfo lineinfo[] = new FeatureClassInfo[0];
    /** featureclasses used for the area feature type */
    public FeatureClassInfo areainfo[] = new FeatureClassInfo[0];
    /** featureclasses used for the text feature type */
    public FeatureClassInfo textinfo[] = new FeatureClassInfo[0];
    /** featureclasses used for the entity node feature  type */
    public FeatureClassInfo epointinfo[] = new FeatureClassInfo[0];
    /** featureclasses used for the connected node feature type */
    public FeatureClassInfo cpointinfo[] = new FeatureClassInfo[0];

    /**
     * Feature classes to look up FeatureClassInfo via feature name.
     */
    protected Hashtable featureTypes = new Hashtable();
    protected Hashtable featureTypeInfo;

    /** do we need to append a '.' to three-character file names */
    public boolean appendDot = false;
    /** Need this in case we have to go from the coverage
     *  type->feature type->tile */
    protected CoverageAttributeTable cat;

    /** hackage for the antactica polygon in DCW browse coverage */
    final public boolean doAntarcticaWorkaround;

    public static final char EDGE_FEATURETYPE = 'L';
    public static final char AREA_FEATURETYPE = 'A';
    public static final char TEXT_FEATURETYPE = 'T';
    public static final char UPOINT_FEATURETYPE = 'P';
    public static final char EPOINT_FEATURETYPE = 'E';
    public static final char CPOINT_FEATURETYPE = 'N';
    public static final char COMPLEX_FEATURETYPE = 'C';
    public static final char SKIP_FEATURETYPE = 'S';

    /**
     * Construct a CoverageTable object.  Data is expected to be in
     * a directory called path/covtype.
     * @param path the path to the parent directory of where our data resides
     * @param covtype the subdirectory name for the coverage data
     */
    public CoverageTable(String path, String covtype) {
	this.covtype = covtype;
	tablepath = path + "/" + covtype + "/";

	doAntarcticaWorkaround = (tablepath.indexOf("browse") >= 0);

	internSchema();

	loadIntVDT();
	loadCharVDT();

	featureTypeInfo = getFeatureTypeInfo();
    }

    /**
     * Construct a CoverageTable object.  Data is expected to be in
     * a directory called path/covtype.
     * @param path the path to the parent directory of where our data resides
     * @param covtype the subdirectory name for the coverage data
     * @param cat the CoverageAttributeTable reference, in case we
     * need to backtrack the tiles through the feature tables.
     */
    public CoverageTable(String path, String covtype, 
			 CoverageAttributeTable cat) {

	this(path, covtype);
	this.cat = cat;
    }

    /** required column names for char.vdt and int.vdt files */
    public final static String VDTColumnNames[] = {Constants.VDT_TABLE, Constants.VDT_ATTRIBUTE, Constants.VDT_VALUE, Constants.VDT_DESC};

    /** expected schema types for int.vdt files */
    public final static char intVDTschematype[] = {'I', 'T', 'T', 'i', 'T'};
    /** expected schema lengths for int.vdt files */
    public final static int intVDTschemalength[] = {1, -1 /*12*/, -1 /*16*/,
						     1, -1 /*50*/};
    
    private void loadIntVDT() {
	try {
	    String vdt = tablepath + Constants.intVDTTableName;
	    if (BinaryFile.exists(vdt)) {
		DcwRecordFile intvdt = new DcwRecordFile(vdt);
		int cols[] = intvdt.lookupSchema(VDTColumnNames, true,
						 intVDTschematype,
						 intVDTschemalength, false);
	
		List l = new ArrayList(intvdt.getColumnCount());
		while (intvdt.parseRow(l)) {
		    String tab = (String)l.get(cols[0]);
		    String attr = (String)l.get(cols[1]);
		    int val = ((Number)l.get(cols[2])).intValue();
		    String desc = ((String)l.get(cols[3])).intern();
		    intvdtrec.put(new CoverageIntVdt(tab, attr, val), desc);
		}
		intvdt.close();
	    }
	} catch (FormatException f) {
	}

    }

    /** expected schema types for char.vdt files */
    public final static char charVDTschematype[] = {'I', 'T', 'T', 'T', 'T'};
    /** expected schema lengths for char.vdt files */
    public final static int charVDTschemalength[] = {1, -1 /*12*/, -1 /*16*/,
						      -1 /*2*/, -1 /*50*/};
    private void loadCharVDT() {
	try {
	    String vdt = tablepath + Constants.charVDTTableName;
	    if (BinaryFile.exists(vdt)) {
		DcwRecordFile charvdt = new DcwRecordFile(vdt);
		int cols[] = charvdt.lookupSchema(VDTColumnNames, true,
						  charVDTschematype,
						  charVDTschemalength, false);

		List l = new ArrayList(charvdt.getColumnCount());
		while (charvdt.parseRow(l)) {
		    String tab = (String)l.get(cols[0]);
		    String attr = (String)l.get(cols[1]);
		    String val = (String)l.get(cols[2]);
		    String desc = ((String)l.get(cols[3])).intern();
		    charvdtrec.put(new CoverageCharVdt(tab, attr, val), desc);
		}
		charvdt.close();
	    }
	} catch (FormatException f) {
	}
    }
    
    private FeatureClassInfo[] internSchema(FeatureClassInfo[] fti,
					    String foreign_key,
					    String tablename)
	throws FormatException
    {
	FeatureClassInfo rv[] = new FeatureClassInfo[fti.length + 1];
	System.arraycopy(fti, 0, rv, 0, fti.length);
	rv[fti.length] = new FeatureClassInfo(this, foreign_key.intern(),
					      tablepath, tablename.intern());
	return rv;
    }

    /** the columns of the fcs file we are interested in */
    private static final String[] fcsColumns = {
	Constants.FCS_FEATURECLASS,
	Constants.FCS_TABLE1, Constants.FCS_TABLE1KEY,
	Constants.FCS_TABLE2, Constants.FCS_TABLE2KEY};
    /** the columns we need in fcs for tiling for DCW */
    private static final String[] fcsColumnsDCW = {
	Constants.FCS_FEATURECLASS,
	Constants.FCS_TABLE1, Constants.DCW_FCS_TABLE1KEY,
	Constants.FCS_TABLE2, Constants.DCW_FCS_TABLE2KEY};

    /**
     * This method reads the feature class schema (fcs) file
     * to discover the inter-table relations (joins, in database parlance).
     * As a side effect, this method also sets the appendDot member.
     */
    private void internSchema() {
	internSchema(false);
    }

    /**
     * This method reads the feature class schema (fcs) file to
     * discover the inter-table relations (joins, in database
     * parlance).  As a side effect, this method also sets the
     * appendDot member.  The DCW option refers to if the DCW column
     * names should be used, for DCW data.  This is only true if a
     * problem occurs, and then this method is called recursively.
     */
    private void internSchema(boolean DCW) {

	// Figure out how files names should be constructed...
	boolean addSlash = true;
// 	if (tablepath.endsWith(File.separator)) {
	if (tablepath.endsWith("/") || tablepath.endsWith(File.separator)) {
	    addSlash = false;
	}

	try {
	    String filename = tablepath + (addSlash?"/":"") + "fcs";
	    if (!BinaryFile.exists(filename)) {
		filename = filename + ".";
		appendDot = true;
	    }
	    DcwRecordFile fcs = new DcwRecordFile(filename);
	    List fcsrec = new ArrayList(fcs.getColumnCount());
	    
	    int[] fcscols;

	    if (!DCW) {
		fcscols = fcs.lookupSchema(fcsColumns, true);
	    } else {
		fcscols = fcs.lookupSchema(fcsColumnsDCW, true);
	    }

	    while (fcs.parseRow(fcsrec)) {
		String feature_class = (String)fcsrec.get(fcscols[0]);
		String table1 = (String)fcsrec.get(fcscols[1]);
		String foreign_key = (String)fcsrec.get(fcscols[2]);
		String table2 = (String)fcsrec.get(fcscols[3]);
		String primary_key = (String)fcsrec.get(fcscols[4]);
		internSchema(feature_class.toLowerCase(),
			     table1.toLowerCase(), foreign_key.toLowerCase(),
			     table2.toLowerCase(), primary_key.toLowerCase());
	    }
	    fcs.close();
	} catch (FormatException f) {
	    if (!DCW) {
		internSchema(true);
	    } else {
		System.out.println("CoverageTable: " + f.getMessage());
	    }
	}
    }

    private void internSchema(String feature_class,
			      String table1, String foreign_key,
			      String table2, String primary_key) {
	try {
	    if (table1.equals("fac")) {
		areainfo = internSchema(areainfo, foreign_key, table2);
	    } else if (table1.equals("edg")) {
		lineinfo = internSchema(lineinfo, foreign_key, table2);
	    } else if (table1.equals("end")) {
		epointinfo = internSchema(epointinfo, foreign_key, table2);
	    } else if (table1.equals("cnd")) {
		cpointinfo = internSchema(cpointinfo, foreign_key, table2);
	    } else if (table1.equals("txt")) {
		textinfo = internSchema(textinfo, foreign_key, table2);
	    } else if (table1.startsWith(feature_class) &&
		       (foreign_key.equals("end_id") ||
			foreign_key.equals("cnd_id") ||
			foreign_key.equals("fac_id") ||
			foreign_key.equals("edg_id") ||
			foreign_key.equals("txt_id"))) {
		if (Debug.debugging("vpf")) {
		    Debug.output("Found entry in the CoverageTable for: " +
				 feature_class + ": " + table1  + "|" +
				 foreign_key + "|" + table2 + "|" +
				 primary_key);
		}

		FeatureClassInfo featureClass = 
		    new FeatureClassInfo(this, 
 					 foreign_key.intern(), 
					 tablepath.intern(), 
					 table1.intern(),
					 table2.intern(), 
					 foreign_key.intern());
					 
		featureTypes.put(feature_class.intern(), featureClass);

	    } else {
	        // nothing else that we care about for now
		// symbol.rat could show up here, for example
	    }
	} catch (FormatException f) {
	    System.out.println("internSchema: " + f.getMessage());
	}
    }
  
    /**
     * Get the path for this coverage
     */
    public String getDataPath() {
        return tablepath;
    }

    /**
     * Returns all the feature classes
     */
    public Map getFeatureClasses() {
	return Collections.unmodifiableMap(featureTypes);
    }
    
    /**
     * Returns the FeatureClassInfo object corresponding to the
     * feature type.  Returns null if the featureType doesn't exist.
     * @returns the feature class object for the feature type
     * @param featureType the name of the feature to get
     */
    public FeatureClassInfo getFeatureClassInfo(String featureType) {
	return (FeatureClassInfo)featureTypes.get(featureType);
    }

    public String getDescription(String t, String a, int v) {
	CoverageIntVdt civ = new CoverageIntVdt(t,a,v);
	return (String)intvdtrec.get(civ);
    }
  
    public String getDescription(String t, String a, String v) {
	CoverageCharVdt civ = new CoverageCharVdt(t,a,v);
	return (String)charvdtrec.get(civ);
    }

    private String getDescription(List id, FeatureClassInfo fti[],
				  MutableInt ret) {
	if ((fti == null) || (fti.length == 0)) {
	    return null;
	}
	StringBuffer foo = null;
	for (int i = 0; i < fti.length; i++) {
	    String desc = fti[i].getDescription(id, ret);
	    if (desc != null) {
		if (foo == null) {
		    foo = new StringBuffer(desc);
		} else {
		    foo.append(";; ").append(desc);
		}
	    }
	}
	return((foo == null)?null:foo.toString());
    }

    public String getLineDescription(List lineid, MutableInt retval) {
	return getDescription(lineid, lineinfo, retval);
    }

    public String getTextDescription(List textid, MutableInt retval) {
	return getDescription(textid, textinfo, retval);
    }

    public String getEPointDescription(List pointid, MutableInt retval) {
	return getDescription(pointid, epointinfo, retval);
    }

    public String getCPointDescription(List pointid, MutableInt retval) {
	return getDescription(pointid, cpointinfo, retval);
    }

    public String getAreaDescription(List areaid, MutableInt retval) {
	return getDescription(areaid, areainfo, retval);
    }

    public void setCoverateAttributeTable(CoverageAttributeTable cat) {
	this.cat = cat;
    }

    public CoverageAttributeTable getCoverageAttributeTable() {
	return cat;
    }

    /**
     * Given a tile directory, go through the entries in the
     * edg/fac/txt files, and send those entries to the warehouse.
     * The warehouse will check their feature names with the feature
     * names given to it in its properties, and eliminate the ones
     * that it shouldn't draw.
     */
    public void drawTile(TileDirectory drawtd,
			 VPFGraphicWarehouse warehouse,
			 LatLonPoint ll1, LatLonPoint ll2,
			 float dpplat, float dpplon)
    {
	if (Debug.debugging("vpf.tile")) {
	    Debug.output("Drawtile for " + drawtd);
	}

	TableHolder tables = new TableHolder(this);
	tables.drawTile(drawtd, warehouse, ll1, ll2, dpplat, dpplon);
    }

    /**
     * This function uses the warehouse to get a list of features, and
     * then looks in the featureList to see what feature tables handle
     * those features.  Using the appropriate feature table, the
     * function then tracks down the tile that contains that feature,
     * and the feature index into that tile file, and then contacts
     * the warehouse to get that feature created into a graphic.
     */
    public boolean drawFeatures(VPFFeatureWarehouse warehouse,
				LatLonPoint ll1, LatLonPoint ll2,
				float dpplat, float dpplon) {

	boolean didSomething = false;

	TableHolder tables = new TableHolder(this);

	// First, find the list of features, and interate through
	// them.  Use each one to go through it's feature table, if it
	// exists.  Then, from the feature table (perhaps check the
	// FACC code, too), get the tile number and feature ID.
	// Access the CoverageAttributeTable to get the tile (using a
	// table from above), and check if it is within bounds.  If it
	// is, seek to get the feature.  Once the feature has been
	// read, contact the warehouse to use the row to build a
	// graphic.  Hold on to the tile in case (and it's likely)
	// that the next feature desired is in the same tile).  If it
	// isn't, then dump the tile.

	// Should sort the feature types by area, text and then lines,
	// to do them in that order.

	List featureList = warehouse.getFeatures();

	for (Iterator enum = featureList.iterator(); enum.hasNext();) {
	    String currentFeature = (String)enum.next();
	    
	    // Figure out if the feature should be rendered, depending
	    // on what the warehouse settings are (drawedges,
	    // drawareas, drawtext).
	    char featureType = whatFeatureType(warehouse, currentFeature);

	    if (featureType == SKIP_FEATURETYPE) {
		// Blow off this feature type.
		continue;
	    }

	    if (Debug.debugging("vpf")){
		Debug.output("CoverageTable getting " + 
			     currentFeature + " features");
	    }
	    // Get the feature class for this feature type.
	    FeatureClassInfo fci = getFeatureClassInfo(currentFeature);
	    
	    if ((fci == null) || (cat == null)) {
		continue; //don't have enough info to procede
		//in an untiled coverage, we could probably work without
		//the cat
	    }

	    if (drawFeaturesFromThematicIndex(fci, warehouse, tables, 
					      ll1, ll2, dpplat, dpplon, 
					      currentFeature, featureType)) {
		didSomething = true;
		continue;
	    }
	    //couldn't use the tile_id thematic index, so just parse the
	    //whole file
	    
	    boolean needToFindOurselves = true;

	    TilingAdapter fciTilingAdapter = fci.getTilingAdapter();
	    if (fciTilingAdapter == null) {
		//no way to find primitives
		continue;
	    }

	    TileDirectory currentTile = null;

	    // There are going to be a variable number of columns.
	    // We're interested in the f_code, tile_id, and the
	    // primitive id (fci independent depending on type).

	    int oldTileID = -2; //-1 is "untiled" tile_id
//  	    int faccIndex = fci.getFaccIndex()
	    List fcirow = new ArrayList();  // hold fci row contents

	    // OK, now we are looking in the Feature class file.
	    try {
		int getrow = 1;
		while (fci.getRow(fcirow, getrow++)) {
		    if (Debug.debugging("vpfdetail")) {
			Debug.output("CoverageTable new feature " + fcirow);
		    }

		    int tileID = fciTilingAdapter.getTileId(fcirow);

//  		    String facc = (String)fcirow.get(faccIndex);

		    // With tileID, find the tile and figure out if it is needed.
		    if (tileID != oldTileID) {
			tables.close();

			if (Debug.debugging("vpf.tile")){
			    Debug.output("CoverageTable.drawFeatures(): opening new tile (" + tileID + ")");
			}

			currentTile = (tileID==-1) ? new TileDirectory() : cat.getTileWithID(tileID);

			if (currentTile == null) {
			    Debug.error("VPFLayer|CoverageTable.drawFeatures: null tile from bogus ID (" + tileID + ") from " + fci.filename + ", skipping...");
			    continue;
			}

			if ((tileID == -1) ||
			    currentTile.inRegion(ll1.getLatitude(), 
						 ll2.getLatitude(),
						 ll2.getLongitude(),
						 ll1.getLongitude())) {

			    if (Debug.debugging("vpf.tile")) {
				Debug.output("Drawing " + featureType+
					     " features for " +
					     currentTile);
			    }

			    tables.setTables(featureType, currentTile);
				
			    // Only need to do this once for a new fci...
			    if (needToFindOurselves) {
				needToFindOurselves = false;
				tables.findYourself(fci);
			    }
				
			} else {
			    tables.close();
			}
			oldTileID = tileID;
		    }

		    // If currentTile == null, then the tile
		    // wasn't found, or it is outside the area of
		    // interest. The tables in the TableHolder
		    // (tables) will all be null, and the tables
		    // drawFeature will return false...

		    int primitiveID = fciTilingAdapter.getPrimId(fcirow);

		    if (tables.drawFeature(primitiveID, warehouse, ll1, ll2, 
					   dpplat, dpplon, currentFeature)) {
			didSomething = true;
		    }
		    
		}

	    }  catch (FormatException f) {
		if (Debug.debugging("vpf.FormatException")) {
		    Debug.output("Creating table: " + f.getClass()
				 + " " + f.getMessage());
		}
	    }
	    fci.close();
	}
	tables.close();

	return didSomething;
    }


    /**
     * This function gets the thematic index from the FeatureClassInfo
     * object, and uses it to look up the tiles that contain the
     * currentFeature.  Then, that tile is checked to see if it is on
     * the map.  If it is, then that row in the thematic index is read
     * to get the feature id numbers.  The feature table is referenced
     * for the feature ID number in the tile, and then the feature is
     * drawn.
     *
     * @param fci the FeatureClassInfo (feature table)
     * @param warehouse the VPFFeatureGraphicWarehouse to use to draw
     * the graphics.
     * @param ll1 the upper left corner of the map.
     * @param ll2 the lower right corner of the map.
     * @param dpplat degrees per pixel latitude direction.
     * @param dpplon degrees per pixel longitude direction.
     * @param currentFeature the feature string (roadl)
     * @param featureType the CoverageTable letter representation of
     * the feature type.  
     */
    protected boolean drawFeaturesFromThematicIndex(FeatureClassInfo fci, 
		VPFFeatureWarehouse warehouse, TableHolder tables,
		LatLonPoint ll1, LatLonPoint ll2, 
		float dpplat, float dpplon,
		String currentFeature, char featureType) {

	if (!fci.initThematicIndex(tablepath)) {
	    return false;
	}

	List v = new ArrayList();  // hold fci row contents

	try {
	    int primitiveIdColIndex = fci.getTilePrimitiveIdColIndex();
	    DcwThematicIndex thematicIndex = fci.getThematicIndex();
	    Object[] indexes = thematicIndex.getValueIndexes();

	    fci.reopen(1);

	    // We just know that these values are tile IDs.
	    for (int i = 0; i < indexes.length; i++ ) {
		int tileID = VPFUtil.objectToInt(indexes[i]);
		TileDirectory currentTile = cat.getTileWithID(tileID);

		if (currentTile == null) {
		    Debug.error("VPFLayer|CoverageTable.drawFeatures: null tile from bogus ID (" + tileID + ") from " + fci.filename + ", skipping...");
		    continue;
		}

		if (currentTile.inRegion(ll1.getLatitude(), 
					 ll2.getLatitude(),
					 ll2.getLongitude(),
					 ll1.getLongitude())) {
		
		    tables.setTables(featureType, currentTile);
		    tables.findYourself(fci);

		    int[] featureID = thematicIndex.get(indexes[i]);

		    if (Debug.debugging("vpf.tile")) {
			Debug.output("Drawing " + featureID.length + " " +
				     featureType + " features for " +
				     tileID + " " + currentTile);
		    }

		    for (int j = 0; j < featureID.length; j++) {

			if (!fci.getRow(v, featureID[j])) {
			    //couldn't get row for some reason
			    continue;
			}

			int primitiveID = 
			    VPFUtil.objectToInt(v.get(primitiveIdColIndex));
			
			tables.drawFeature(primitiveID, warehouse,
					   ll1, ll2, dpplat, dpplon, 
					   currentFeature);
		    }

		    tables.close();
		} else {
		    if (Debug.debugging("vpf.tile")) {
			Debug.output("Skipping " + featureType + 
				     " features for " + tileID + 
				     ", not on map");
		    }
		}
	    }
	    return true;
	}  catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("CoverageTable.DFFTI: Format Exception creating features: " + f.getClass() + " " + f.getMessage());
	    }
	    return false;
	} finally {
	    fci.close();
	}
    }

    /**
     * Given a feature type name, figure out if the warehouse thinks
     * it should *NOT* be drawn.
     * @param warehouse the warehouse to build the graphics.
     * @param featureName the VPF name of the feature (polbndl, for example).
     * @return SKIP_FEATURETYPE if the feature should not be drawn.
     */
    protected char whatFeatureType(VPFWarehouse warehouse,
				   String featureName) {
	// Test for the feature kind (edge, area, text, points) and
	// don't continue if that type is not needed.

	char featureType = SKIP_FEATURETYPE;

	// Get the feature class for this feature type.
	FeatureClassInfo fci = getFeatureClassInfo(featureName);
	
	if (fci == null) {
	    return featureType;
	}

	char type = fci.getFeatureType();

	if ((type == AREA_FEATURETYPE && warehouse.drawAreaFeatures()) ||
	    (type == TEXT_FEATURETYPE && warehouse.drawTextFeatures()) ||
	    (type == EDGE_FEATURETYPE && warehouse.drawEdgeFeatures()) ||
	    (type == EPOINT_FEATURETYPE && warehouse.drawEPointFeatures()) ||
	    (type == CPOINT_FEATURETYPE && warehouse.drawCPointFeatures())) {
	    featureType = type;
	}

	return featureType;
    }
    
    /**
     * Feature Type Information read from VPF fca files.
     */
    public static class FeatureClassRec {
	/** the name of the feature class */
	final public String feature_class;
	/** the type of the feature */
	final public char type;
	/** a short text description */
	final public String description;
	/**
	 * Construct an instance of the class
	 * @param fclass the feature class name
	 * @param type the feature type
	 * @param desc the feature description
	 */
	public FeatureClassRec(String fclass, char type, String desc) {
	    feature_class = fclass;
	    this.type = type;
	    description = desc;
	}
    }

    /**
     * Returns a map from feature name to FeatureClassRec
     */
    public Hashtable getFeatureTypeInfo() {
	if (featureTypeInfo == null) {
	    featureTypeInfo = new Hashtable();
	    
	    String path = getDataPath();
	    boolean addSlash = true;
// 	    if (path.endsWith(File.separator)) {
	    if (path.endsWith("/") || path.endsWith(File.separator)) {
		addSlash = false;
	    }

	    String fca = path + (addSlash?"/":"") + "fca";
	    if (!BinaryFile.exists(fca)) {
		fca = fca + ".";
	    }

	    if (BinaryFile.exists(fca)) {
		try {
		    DcwRecordFile fcadesc = new DcwRecordFile(fca);
		    int fclass = fcadesc.whatColumn("fclass");
		    int type = fcadesc.whatColumn("type");
		    int descr = fcadesc.whatColumn("descr");
		    ArrayList al = new ArrayList(fcadesc.getColumnCount());
		    while (fcadesc.parseRow(al)) {
			String fname = ((String)al.get(fclass)).toLowerCase().intern();
			char ftype = ((String)al.get(type)).charAt(0);
			String fdesc = (String)al.get(descr);
			FeatureClassRec fcr = new FeatureClassRec(fname, ftype,
								  fdesc);
			featureTypeInfo.put(fname, fcr);
		    }
		    fcadesc.close();
		} catch (FormatException fe) {
		    //nevermind, skip it
		}
	    }
	}
	return featureTypeInfo;
    }
    
    /*
    public static void main(String [] args) {
	if (args.length != 5) {
	    System.out.println("This main() is just assorted test code.");
	    System.out.println("Usage: java classname librarypath coveragename");
	    System.out.println("    tablename attribute value");
	    System.out.println("Result: Prints the corresponding value in int.vdt");
	} else {
	    CoverageTable ct = new CoverageTable(args[0], args[1]);
	    String desc = ct.getDescription(args[2], args[3],
					    Integer.parseInt(args[4]));
	    System.out.println(desc);
	}
    }
    */
}

/**
 * The TableHolder is a utility class that manages the EdgeTable,
 * TextTable and AreaTable that are needed by the CoverageTable to use
 * the warehouse to create graphics.
 */
class TableHolder {

    EdgeTable edg = null;
    TextTable tft = null;
    AreaTable aft = null;
    NodeTable ent = null;
    NodeTable cnt = null;

    /** Used as a preallocated list to read feature tables. */
    List primitiveVector = new ArrayList();
    CoverageTable coverageTable;

    /**
     * Construct the TableHandler with the CoverageTable it is
     * helping.
     */
    protected TableHolder(CoverageTable ct){
	coverageTable = ct;
    }

    /**
     * When drawing features (CoverageTable.drawFeatures()), sets up
     * the TableHolder tables so that the right types are used.
     *
     * @param featureType from the CoverageTable, either
     * AREA_FEATURETYPE, EDGE_FEATURETYPE or TEXT_FEATURETYPE.
     * @param tilePath the path to the tile directory that needs to be
     * used when fetching graphics from the appropriate files.
     */
    protected void setTables(char featureType, TileDirectory tile) 
	throws FormatException {

	if (featureType == CoverageTable.EDGE_FEATURETYPE) {
	    edg = new EdgeTable(coverageTable, tile);
	}
	if (featureType == CoverageTable.TEXT_FEATURETYPE) {
	    tft = new TextTable(coverageTable, tile);
	}
	if (featureType == CoverageTable.AREA_FEATURETYPE) {
	    aft = new AreaTable(coverageTable, null, tile);
	    edg = null;
	}
	if (featureType == CoverageTable.EPOINT_FEATURETYPE) {
	    ent = new NodeTable(coverageTable, tile, true);
	}
	if (featureType == CoverageTable.CPOINT_FEATURETYPE) {
	    cnt = new NodeTable(coverageTable, tile, false);
	}
    }

    /**
     * Should be called once per FeatureClassInfo, after the tables
     * have been set.  Lets the tables figure out which columns to use
     * as an index.
     */
    protected void findYourself(FeatureClassInfo fci) {
	if (aft != null) {
	    fci.findYourself(aft);
	} else if (tft != null) {
	    fci.findYourself(tft);
	} else if (edg != null) {
	    fci.findYourself(edg);
	} else if (ent != null) {
	    fci.findYourself(ent);
	}
    }

    /**
     * Should be called once per feature, after the tables have been
     * set (setTables()), and findYourself() has been called.  The
     * appropriate table will use the warehouse to create proper
     * OMGraphic.
     */
    protected boolean drawFeature(int primitiveID, 
				  VPFFeatureWarehouse warehouse,
				  LatLonPoint ll1, LatLonPoint ll2,
				  float dpplat, float dpplon,
				  String currentFeature) throws FormatException {
	
	if (aft != null || tft != null || edg != null || ent != null || cnt != null) {
	    // OK, now check to see what table is being
	    // used.  if the tile is being reused, the
	    // table will be reused.

	    if ((aft != null) && aft.getRow(primitiveVector, primitiveID)) {
		aft.drawFeature(warehouse, dpplat, dpplon, ll1, ll2, 
				primitiveVector, currentFeature);
	    }
	    if ((tft != null) && tft.getRow(primitiveVector, primitiveID)) {
		tft.drawFeature(warehouse, dpplat, dpplon, ll1, ll2, 
				primitiveVector, currentFeature);
	    }
	    if ((ent != null) && ent.getRow(primitiveVector, primitiveID)) {
		ent.drawFeature(warehouse, dpplat, dpplon, ll1, ll2, 
				primitiveVector, currentFeature);
	    }
	    if ((cnt != null) && cnt.getRow(primitiveVector, primitiveID)) {
		cnt.drawFeature(warehouse, dpplat, dpplon, ll1, ll2, 
				primitiveVector, currentFeature);
	    }
	    if ((edg != null) && edg.getRow(primitiveVector, primitiveID)) {
		edg.drawFeature(warehouse, dpplat, dpplon, ll1, ll2,
				primitiveVector, currentFeature);
	    }
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Only call once per tile.  It will parse all the needed data in
     * the tile.  Does not require setTables() or findYourself().
     */
    protected void drawTile(TileDirectory tile, 
			    VPFGraphicWarehouse warehouse, 
			    LatLonPoint ll1, LatLonPoint ll2, 
			    float dpplat, float dpplon) {

        boolean drawedge = warehouse.drawEdgeFeatures();
	boolean drawtext = warehouse.drawTextFeatures();
	boolean drawarea = warehouse.drawAreaFeatures();
	boolean drawepoint = warehouse.drawEPointFeatures();
	boolean drawcpoint = warehouse.drawCPointFeatures();
	close();

	try {
	    if (drawedge || drawarea) {
		edg = new EdgeTable(coverageTable, tile);
	    }
	} catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("EdgeTable: " + f.getClass()
			     + " " + f.getMessage());
	    }
	}

	try {
	    if (drawtext) {
		tft = new TextTable(coverageTable, tile);
	    }
	} catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("TextTable: " + f.getClass()
			     + " " + f.getMessage());
	    }
	}

	try {
	    if (drawepoint) {
		ent = new NodeTable(coverageTable, tile, true);
	    }
	} catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("NodeTable: " + f.getClass()
			     + " " + f.getMessage());
	    }
	}

	try {
	    if (drawcpoint) {
		cnt = new NodeTable(coverageTable, tile, false);
	    }
	} catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("NodeTable: " + f.getClass()
			     + " " + f.getMessage());
	    }
	}

	try {
	    if (drawarea && (edg != null)) {
		aft = new AreaTable(coverageTable, edg, tile);
	    }
	} catch (FormatException f) {
	    if (Debug.debugging("vpf.FormatException")) {
		Debug.output("AreaTable: " + f.getClass()
			     + " " + f.getMessage());
	    }
	}

	if ((aft != null) && drawarea) {
	    for (int i = 0; i < coverageTable.areainfo.length; i++) {
		coverageTable.areainfo[i].findYourself(aft);
	    }
	    aft.drawTile(warehouse, dpplat, dpplon, ll1, ll2);
	}
	if ((tft != null) && drawtext) {
	    for (int i = 0; i < coverageTable.textinfo.length; i++) {
		coverageTable.textinfo[i].findYourself(tft);
	    }
 	    tft.drawTile(warehouse, dpplat, dpplon, ll1, ll2);
	}
	if ((edg != null) && drawedge) {
	    for (int i = 0; i < coverageTable.lineinfo.length; i++) {
		coverageTable.lineinfo[i].findYourself(edg);
	    }
 	    edg.drawTile(warehouse, dpplat, dpplon, ll1, ll2);
	}
	if ((ent != null) && drawepoint) {
	    for (int i = 0; i < coverageTable.epointinfo.length; i++) {
		coverageTable.epointinfo[i].findYourself(ent);
	    }
 	    ent.drawTile(warehouse, dpplat, dpplon, ll1, ll2);
	}
	if ((cnt != null) && drawcpoint) {
	    for (int i = 0; i < coverageTable.cpointinfo.length; i++) {
		coverageTable.cpointinfo[i].findYourself(cnt);
	    }
 	    cnt.drawTile(warehouse, dpplat, dpplon, ll1, ll2);
	}
//	if (Debug.On && Debug.debugging("vpf.tile"))
// 	    Debug.output(drawtd.toString() + " " + edgecount[0] +
// 			      " polys with " + edgecount[1] +
// 			      " points (cumulative)\n" +
// 			      drawtd.toString() + " " + textcount[0] +
// 			      " texts with " + textcount[1] +
// 			      " points (cumulative)\n" +
// 			      drawtd.toString() + " " + areacount[0] +
// 			      " areas with " + areacount[1] +
// 			      " points (cumulative)");
	close();
    }


    /**
     *  Close any of these tables that may be in use.
     */
    protected void close() {
	if (Debug.debugging("vpf.tile")) {
	    Debug.output("CoverageTable closing tile tables");
	}
	if (edg != null) {
	    edg.close();
	}
	if (tft != null) {
	    tft.close();
	}
	if (aft != null) {
	    aft.close();
	}
	aft = null;
	tft = null;
	edg = null;
    }

}

/**
 * A utility class used to map information from a VPF feature table to
 * its associated value in an int.vdt file.
 */
class CoverageIntVdt {
    /** the name of the table we are looking up (table is interned) */
    final String table;
    /** the name of the attribute we are looking up (attribute is interned) */
    final String attribute;
    /** the integer value we are looking up */
    final int value;
  
    /** 
     * Construct a new object
     * @param t value for the table member
     * @param a the value for the attribute member
     * @param v the value for the value member */
    public CoverageIntVdt(String t, String a, int v) {
	table = t.toLowerCase().intern();
	attribute = a.toLowerCase().intern();
	value = v;
    }

    /**
     * Override the equals method. Two CoverageIntVdts are equal if
     * and only iff their respective table, attribute and value
     * members are equal.
     */
    public boolean equals(Object o) {
	if (o instanceof CoverageIntVdt) {
	    CoverageIntVdt civ = (CoverageIntVdt)o;
	    //we can use == rather than String.equals(String) since
	    //table and attribute are interned.
	    return((table == civ.table) && (attribute == civ.attribute) &&
		   (value == civ.value));
	} else {
	    return false;
	}
    }
    
    /**
     * Override hashcode.  Compute a hashcode based on our member
     * values, rather than our (base class) object identity.
     **/
    public int hashCode() {
	return((table.hashCode() ^ attribute.hashCode()) ^ value);
    }
}

/**
 * A utility class used to map information from a VPF feature table to
 * its associated value in an char.vdt file.
 */
class CoverageCharVdt {
    /** the name of the table we are looking up (table is interned) */
    final String table;
    /** the name of the attribute we are looking up (attribute is interned) */
    final String attribute;
    /** the character value we are looking up (value is interned) */
    final String value;
  
    /**
     * Construct a new object
     * @param t value for the table member
     * @param a the value for the attribute member
     * @param v the value for the value member
     */
    public CoverageCharVdt(String t, String a, String v) {
	table = t.toLowerCase().intern();
	attribute = a.toLowerCase().intern();
	value = v.intern();
    }

    /**
     * Override the equals method. Two CoverageIntVdts are equal if
     * and only iff their respective table, attribute and value
     * members are equal.
     */
    public boolean equals(Object o) {
        if (o instanceof CoverageCharVdt) {
	    CoverageCharVdt civ = (CoverageCharVdt)o;
	    //we can use == rather than String.equals(String) since
	    //table, attribute, and value are interned.
	    return((table == civ.table) && (attribute == civ.attribute) &&
		   (value == civ.value));
	} else {
	    return false;
	}
    }

    /**
     * Override hashcode.  Compute a hashcode based on our member
     * values, rather than our (base class) object identity.
     */
    public int hashCode() {
	return((table.hashCode() ^ attribute.hashCode()) ^ value.hashCode());
    }
}
