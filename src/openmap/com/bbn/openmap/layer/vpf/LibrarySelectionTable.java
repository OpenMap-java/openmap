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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/LibrarySelectionTable.java,v $
// $Revision: 1.13 $ $Date: 2005/12/09 21:08:57 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * Reads the VPF LibraryAttribute table and constructs CoverageAttributeTables
 * for each of the library coverages (north america, browse, etc) that exist.
 * 
 * <p>
 * NOTE: This class maintains a whole bunch of cached information, and also
 * hangs onto references to classes that cache even more information. When using
 * this class, you are much better off sharing an instance of this class, rather
 * than creating multiple instantiations of it for the same VPF data directory.
 * 
 * @see CoverageAttributeTable
 */
public class LibrarySelectionTable {

    /** cutoff scale for browse coverage. */
    public final static int DEFAULT_BROWSE_CUTOFF = 31000000;

    protected int BROWSE_CUTOFF = DEFAULT_BROWSE_CUTOFF;

    /**
     * the names of the VPF libraries listed in the library attribute table
     */
    // private String libraryname[] = null; //library [i]
    /** the bounding rectangle of the respective libraries */
    private Map<String, float[]> boundrec = new HashMap<String, float[]>();// bounding
                                                                           // rect
                                                                           // as
                                                                           // [W,S,E,N]
    /** the CoverageAttributeTables corresponding to the different libs */
    private Map<String, CoverageAttributeTable> CATs = new HashMap<String, CoverageAttributeTable>();
    /**
     * A list of libraries in the same order as their data paths were added.
     */
    private List<String> orderedLibraryNameList = new ArrayList<String>();

    /** the names of the lat columns */
    final private static String LATColumns[] = { Constants.LAT_LIBNAME, Constants.LAT_XMIN,
            Constants.LAT_YMIN, Constants.LAT_XMAX, Constants.LAT_YMAX };
    /** the expected schema types for the library attribute table */
    final private static char LATschematype[] = { 'T', 'F', 'F', 'F', 'F' };
    /** the expected schema lengths for the library attribute table */
    final private static int LATschemalength[] = { -1 /* 8 */, 1, 1, 1, 1 };
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
     * @param vpfpath the path to the base data directory; the file opened is
     *        <code>vpfpath</code> /lat.
     * @exception FormatException some error was encountered while trying to
     *            handle the file.
     */
    public LibrarySelectionTable(String vpfpath) throws FormatException {
        addDataPath(vpfpath);
    }

    /**
     * Construct a LibrarySelectionTable with a path to data.
     * 
     * @param vpfpaths the paths to the data directories; the file opened is
     *        <code>vpfpath</code> /lat.
     * @exception FormatException some error was encountered while trying to
     *            handle the file.
     */
    public LibrarySelectionTable(String vpfpaths[]) throws FormatException {
        for (int i = 0; i < vpfpaths.length; i++) {
            addDataPath(vpfpaths[i]);
        }
    }

    /**
     * Set the cutoff scale where if the map scale number is larger (smaller
     * overall map scale), the coverage won't be returned. For example, if the
     * scale cutoff is 30000000, if the map scale is 1:31000000, no map data
     * will be returned.
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
     * add a path to LibrarySelectionTable. Adding different types of VPF
     * libraries to the same LST is likely to cause trouble. (e.g. it would be
     * bad to add both DCW and VMAP paths to the same LST. adding each DCW disk
     * separately is why this method exists.)
     * 
     * @param vpfpath the path to the base DCW directory; the file opened is
     *        <code>vpfpath</code> /lat.
     * @exception FormatException some error was encountered while trying to
     *            handle the file.
     */
    public void addDataPath(String vpfpath) throws FormatException {
        VPFLayer.logger.fine("LST.addDataPath(" + vpfpath + ")");

        // Figure out how files names should be constructed...
        boolean addSlash = true;

        if (vpfpath.endsWith("/") || vpfpath.endsWith(File.separator)) {
            addSlash = false;
        }

        String latf = vpfpath + (addSlash ? "/" : "") + "lat";
        String dhtf = vpfpath + (addSlash ? "/" : "") + "dht";

        if (!BinaryFile.exists(latf)) {
            latf += ".";
            dhtf += ".";
        }

        DcwRecordFile latrf = new DcwRecordFile(latf);
        DcwRecordFile dhtrf = new DcwRecordFile(dhtf);

        List<Object> databaseVec = dhtrf.parseRow();
        int dcol = dhtrf.whatColumn("database_name");
        if (dcol != -1) {
            databaseName = (String) databaseVec.get(dcol);
        }
        dcol = dhtrf.whatColumn("database_desc");
        if (dcol != -1) {
            databaseDesc = (String) databaseVec.get(dcol);
        }
        dhtrf.close();
        dhtrf = null;

        int latcols[] = latrf.lookupSchema(LATColumns, true, LATschematype, LATschemalength, false);

        VPFLayer.logger.fine("lst.adp: looked up schema");
        for (List<Object> l = new ArrayList<Object>(latrf.getColumnCount()); latrf.parseRow(l);) {
            String lname = ((String) l.get(latcols[0])).toLowerCase();
            float br[] = new float[] { ((Float) l.get(latcols[1])).floatValue(),
                    ((Float) l.get(latcols[2])).floatValue(),
                    ((Float) l.get(latcols[3])).floatValue(),
                    ((Float) l.get(latcols[4])).floatValue() };
            try {
                CoverageAttributeTable table = new CoverageAttributeTable(vpfpath, lname);
                CATs.put(lname, table);
                boundrec.put(lname, br);
                orderedLibraryNameList.add(lname);
                if (VPFLayer.logger.isLoggable(Level.FINE)) {
                    VPFLayer.logger.fine(lname + " " + br[0] + " " + br[1] + " " + br[2] + " "
                            + br[3]);
                }
            } catch (FormatException fe) {
                if (VPFLayer.logger.isLoggable(Level.FINER)) {
                    VPFLayer.logger.finer("*****\nVPFLayer.LST: Couldn't create CoverageAttributeTable for "
                            + vpfpath
                            + " "
                            + lname
                            + " "
                            + fe.getMessage()
                            + "\n--- Not a problem if you have multiple paths, and "
                            + lname
                            + " is included in another path ---\n*****");
                    fe.printStackTrace();
                } else {
                    VPFLayer.logger.fine("VPFLayer.LST: CAT discrepancy (run with finer logging level for more details)");
                }
            }
        }
        latrf.close();
        latrf = null;
    }

    /**
     * Return the list of libraries that this database has.
     * 
     * @return the list of libraries. for DCW, this is typically NOAMER, BROWSE,
     *         etc.
     */
    public List<String> getLibraryNames() {
        return new ArrayList<String>(orderedLibraryNameList);
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
     * Return the coverage attribute table (list of coverages available for the
     * given library) for the given library name.
     * 
     * @param library the name of the library to get the CAT for
     * @return the CoverageAttributeTable requested (null if the library
     *         requested doesn't exist in the database)
     * @exception FormatException exceptions from opening the CAT for the
     *            library
     */
    public CoverageAttributeTable getCAT(String library) throws FormatException {
        return CATs.get(library);
    }

    /**
     *  
     */
    public void drawTile(int scale, int screenwidth, int screenheight, String covname,
                         VPFGraphicWarehouse warehouse, LatLonPoint ll1, LatLonPoint ll2) {

        if (VPFLayer.logger.isLoggable(Level.FINE)) {
            VPFLayer.logger.fine("Library selection table coverage: " + covname);
            VPFLayer.logger.fine("Library selection table - edges: " + warehouse.drawEdgeFeatures());
            VPFLayer.logger.fine("Library selection table - text: " + warehouse.drawTextFeatures());
            VPFLayer.logger.fine("Library selection table - areas: " + warehouse.drawAreaFeatures());
            VPFLayer.logger.fine("Warehouse: " + warehouse);
            VPFLayer.logger.fine("Warehouse: cutoff scale " + BROWSE_CUTOFF);
        }

        // handle Dateline
        if ((scale < BROWSE_CUTOFF) && (ll1.getLongitude() > ll2.getLongitude())) {
            drawTile(scale, screenwidth, screenheight, covname, warehouse, ll1, new LatLonPoint.Float(ll2.getLatitude(), 180f - .00001f)/*
                                                                                                                                         * 180
                                                                                                                                         * -
                                                                                                                                         * epsilon
                                                                                                                                         */);
            drawTile(scale, screenwidth, screenheight, covname, warehouse, new LatLonPoint.Float(ll1.getLatitude(), -180f), ll2);
            return;
        }

        if (VPFLayer.logger.isLoggable(Level.FINE)) {
            VPFLayer.logger.fine("LST.drawTile() with scale of " + scale);
        }

        float dpplat = Math.abs((ll1.getLatitude() - ll2.getLatitude()) / screenheight);
        float dpplon = Math.abs((ll1.getLongitude() - ll2.getLongitude()) / screenwidth);

        int inArea = 0;
        CoverageTable redrawUntiled = null;

        for (CoverageAttributeTable cat : CATs.values()) {

            if (VPFLayer.logger.isLoggable(Level.FINE)) {
                VPFLayer.logger.fine("LST: checking library: " + cat.getLibraryName());
            }

            if (!warehouse.checkLibraryForUsage(cat.getLibraryName())) {
                continue;
            }

            warehouse.resetForCAT();

            List<TileDirectory> tiles = cat.tilesInRegion(ll1.getLatitude(), ll2.getLatitude(), ll2.getLongitude(), ll1.getLongitude());

            if (tiles == null) {
                redrawUntiled = cat.getCoverageTable(covname);
            } else if (cat.isTiledData() && (scale < BROWSE_CUTOFF)) {

                if (!tiles.isEmpty()) {
                    CoverageTable c = cat.getCoverageTable(covname);
                    if (c == null) {
                        if (VPFLayer.logger.isLoggable(Level.FINE)) {
                            VPFLayer.logger.fine("|LST.drawTile(): Couldn't get coverage table for "
                                    + covname + " " + cat.getLibraryName());
                        }
                        continue;
                    }

                    if (VPFLayer.logger.isLoggable(Level.FINE)) {
                        VPFLayer.logger.fine("Using coverage table for " + covname + " "
                                + cat.getLibraryName());
                    }

                    inArea++;
                    for (TileDirectory tileDirectory : tiles) {
                        c.drawTile(tileDirectory, warehouse, ll1, ll2, dpplat, dpplon);
                    }
                }
            }
        }
        if ((redrawUntiled != null) && (inArea == 0)) {
            if (VPFLayer.logger.isLoggable(Level.FINE)) {
                VPFLayer.logger.fine("LST drawing untiled browse data");
            }
            redrawUntiled.drawTile(new TileDirectory(), warehouse, ll1, ll2, dpplat, dpplon);
        }
    }

    /**
     *  
     */
    public void drawFeatures(int scale, int screenwidth, int screenheight, String covname,
                             VPFFeatureWarehouse warehouse, LatLonPoint ll1, LatLonPoint ll2) {

        if (VPFLayer.logger.isLoggable(Level.FINE)) {
            VPFLayer.logger.fine("LST.drawFeatures(): Coverage name: " + covname);
            VPFLayer.logger.fine("Library selection table - edges: " + warehouse.drawEdgeFeatures());
            VPFLayer.logger.fine("Library selection table - text: " + warehouse.drawTextFeatures());
            VPFLayer.logger.fine("Library selection table - areas: " + warehouse.drawAreaFeatures());
            VPFLayer.logger.fine("Warehouse: " + warehouse);
        }

        // handle Dateline
        if ((scale < BROWSE_CUTOFF) && (ll1.getLongitude() > ll2.getLongitude())) {
            drawFeatures(scale, screenwidth, screenheight, covname, warehouse, ll1, new LatLonPoint.Float(ll2.getLatitude(), 180f - .00001f)// 180-epsilon
            );
            drawFeatures(scale, screenwidth, screenheight, covname, warehouse, new LatLonPoint.Float(ll1.getLatitude(), -180f), ll2);
            return;
        }

        if (VPFLayer.logger.isLoggable(Level.FINE)) {
            VPFLayer.logger.fine("LST.drawFeatures() with scale of " + scale);
        }

        float dpplat = Math.abs((ll1.getLatitude() - ll2.getLatitude()) / screenheight);
        float dpplon = Math.abs((ll1.getLongitude() - ll2.getLongitude()) / screenwidth);

        int inArea = 0;
        CoverageTable redrawUntiled = null;

        for (CoverageAttributeTable cat : CATs.values()) {

            if (!warehouse.checkLibraryForUsage(cat.getLibraryName())) {
                continue;
            }

            if (cat.isTiledCoverage() && scale < BROWSE_CUTOFF) {

                CoverageTable c = cat.getCoverageTable(covname);
                if (c == null) {
                    if (VPFLayer.logger.isLoggable(Level.FINE)) {
                        VPFLayer.logger.fine("LST.getFeatures(): Couldn't get coverage table for "
                                + covname + " " + cat.getLibraryName());
                    }
                    continue;
                }

                if (VPFLayer.logger.isLoggable(Level.FINE)) {
                    VPFLayer.logger.fine("Using coverage table for " + covname + " "
                            + cat.getLibraryName());
                }

                c.drawFeatures(warehouse, ll1, ll2, dpplat, dpplon);
                inArea++;
            } else {
                // Set up to draw browse coverage, or non-tiled coverage
                if (VPFLayer.logger.isLoggable(Level.FINE)) {
                    VPFLayer.logger.fine("LST.drawTile(): Scale too small (probably) or no tiles in region.");
                }
                redrawUntiled = cat.getCoverageTable(covname);

                if (redrawUntiled != null) {
                    redrawUntiled.drawFeatures(warehouse, ll1, ll2, dpplat, dpplon);
                }
            }
        }

        // Moved this code up into the redrawUntiled section directly above
        // this. It looks like the code wanted to restrict how many un-tiled
        // coverage attribute tables would be consulted, but for certain kinds
        // of VPF data that gets distributed as a bunch of little libraries,
        // this seems to limit rendering to only one small area.

        // if ((redrawUntiled != null) && (inArea == 0)) {
        // redrawUntiled.drawFeatures(warehouse, ll1, ll2, dpplat, dpplon);
        // }
    }

    /**
     * Given a string for a coverage type or feature type, return the
     * description for that string. Return null if the code string isn't found.
     * 
     * @param coverageOrFeatureType string ID for coverage or Feature type.
     */
    public String getDescription(String coverageOrFeatureType) throws FormatException {
        boolean DEBUG = VPFLayer.logger.isLoggable(Level.FINE);

        if (DEBUG)
            VPFLayer.logger.fine("LST.getDescription: " + coverageOrFeatureType);

        for (String libraryName : getLibraryNames()) {
            CoverageAttributeTable cat = getCAT(libraryName);
            if (cat == null) {
                continue;
            }
            String[] coverages = cat.getCoverageNames();
            for (int j = 0; j < coverages.length; j++) {
                String covname = coverages[j];
                if (coverageOrFeatureType.equalsIgnoreCase(covname)) {
                    if (DEBUG)
                        VPFLayer.logger.fine("** Matches coverage " + covname);
                    return cat.getCoverageDescription(covname);
                } else {
                    if (DEBUG)
                        VPFLayer.logger.fine("   Checking in coverage table " + covname);
                    CoverageTable ct = cat.getCoverageTable(covname);
                    Hashtable<String, CoverageTable.FeatureClassRec> info = ct.getFeatureTypeInfo();
                    for (CoverageTable.FeatureClassRec fcr : info.values()) {
                        String name = fcr.feature_class;
                        if (coverageOrFeatureType.equalsIgnoreCase(name)) {
                            if (DEBUG)
                                VPFLayer.logger.fine("** Found feature " + name);
                            return fcr.description;
                        }
                        if (DEBUG)
                            VPFLayer.logger.fine("   checked " + name);
                    }
                }
            }
        }
        if (DEBUG)
            VPFLayer.logger.fine("-- No matches found.");
        return null;
    }

    /**
     * Just a test main to parse vpf datafiles param args files to parse, plus
     * other command line flags
     * 
     * @param args command line arguments args[0] is a path to the VPF root
     */
    public static void main(String[] args) {
        String dcwbase = null;
        if (args.length > 0) {
            dcwbase = args[0];

            try {
                LibrarySelectionTable lst = new LibrarySelectionTable(dcwbase);
                System.out.println("Database Name " + lst.getDatabaseName());

                for (String libraryName : lst.getLibraryNames()) {
                    System.out.println("Library " + libraryName);
                    lst.getCAT(libraryName);
                }
            } catch (FormatException f) {
                System.err.println("*****************************************");
                System.err.println("*---------------------------------------*");
                System.err.println("Format error in dealing with LST");
                System.err.println(f.getMessage());
                System.err.println("*---------------------------------------*");
                System.err.println("*****************************************");
            }
        } else {
            System.out.println("Need a path to the VPF lat. file");
        }
    }
}