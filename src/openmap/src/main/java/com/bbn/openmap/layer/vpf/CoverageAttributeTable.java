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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/CoverageAttributeTable.java,v $
// $Revision: 1.8 $ $Date: 2005/08/09 19:29:39 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.Debug;

/**
 * Handle the library level VPF directory. "noamer" in DCW is an example of the
 * library level data. This class loads the associated tiling information, and
 * the coverage types, and make them available to the client.
 */
public class CoverageAttributeTable {

   /** the name of the library we are, for example "noamer" in DCW */
   final protected String libraryname;
   /** the path to our directory */
   final protected String dirpath;
   /** are we tiled or untiled coverage */
   private boolean isTiled = false;
   /** coverage name to CoverageEntry map */
   final private Map<String, CoverageEntry> coverages = new HashMap<String, CoverageEntry>();
   protected DataBounds bounds;
   /**
    * The tiles that compose our coverage area. The size of the array is going
    * to be set to record count + 1, and the tiles will have their ID number as
    * their index.
    */
   private TileDirectory containedTiles[];
   /** the column names in the cat. file */
   private final static String CATColumns[] = {
      Constants.CAT_COVNAME,
      Constants.CAT_DESC,
      Constants.CAT_LEVEL
   };
   /** expected schema types for the cat. file */
   private final static char CATschematype[] = {
      DcwColumnInfo.VPF_COLUMN_TEXT,
      DcwColumnInfo.VPF_COLUMN_TEXT,
      DcwColumnInfo.VPF_COLUMN_INT_OR_SHORT
   };
   /** expected schema lengths for cat. file */
   private final static int CATschemalength[] = {
      -1 /* 8 */,
      -1 /* 50 */,
      1
   };

   /**
    * Construct a new coverage attribute table
    * 
    * @param libname the name of the library
    * @param dcwpath the path to the library
    * @exception FormatException may throw FormatExceptions
    */
   public CoverageAttributeTable(String dcwpath, String libname)
         throws FormatException {
      libraryname = libname;
      dirpath = dcwpath + "/" + libraryname;
      String cat = dirpath + "/cat";
      if (!BinaryFile.exists(cat)) {
         cat += ".";
      }

      DcwRecordFile rf = new DcwRecordFile(cat);
      int catcols[] = rf.lookupSchema(CATColumns, true, CATschematype, CATschemalength, false);

      List<Object> l = new ArrayList<Object>(rf.getColumnCount());
      while (rf.parseRow(l)) {
         int topL = ((Number) l.get(catcols[2])).intValue();
         String desc = (String) l.get(catcols[1]);
         String covtype = ((String) l.get(catcols[0])).toLowerCase().intern();
         coverages.put(covtype, new CoverageEntry(topL, desc));
      }
      rf.close();
      rf = null;

      doTileRefStuff(dirpath + "/tileref");
   }

   /**
    * is this library tiled
    * 
    * @return <code>true</code> for tiled coverage. <code>false</code> else
    */
   public final boolean isTiledCoverage() {
      return isTiled;
   }

   /**
    * the name of the library
    * 
    * @return the name of the library
    */
   public String getLibraryName() {
      return libraryname;
   }

   /** the columns we need in fbr for tiling */
   private static final String[] fbrColumns = {
      Constants.FBR_XMIN,
      Constants.FBR_YMIN,
      Constants.FBR_XMAX,
      Constants.FBR_YMAX
   };
   /** the columns we need in fcs for tiling */
   private static final String[] fcsColumns = {
      Constants.FCS_FEATURECLASS,
      Constants.FCS_TABLE1,
      Constants.FCS_TABLE1KEY,
      Constants.FCS_TABLE2,
      Constants.FCS_TABLE2KEY
   };
   /** the columns we need in fcs for tiling for DCW */
   private static final String[] fcsColumnsDCW = {
      Constants.FCS_FEATURECLASS,
      Constants.FCS_TABLE1,
      Constants.DCW_FCS_TABLE1KEY,
      Constants.FCS_TABLE2,
      Constants.DCW_FCS_TABLE2KEY
   };

   /**
    * an internal function to load the tiling information
    * 
    * @param pathname the path to the tile directory
    */
   private void doTileRefStuff(String pathname) {
      doTileRefStuff(pathname, false);
   }

   /**
    * an internal function to load the tiling information, with an option to use
    * DCW column names.
    * 
    * @param pathname the path to the tile directory
    * @param DCW use DCW column names.
    */
   private void doTileRefStuff(String pathname, boolean DCW) {
      String faceIDColumnName = null;
      // Figure out how files names should be constructed...
      boolean addSlash = true;
      // if (pathname.endsWith(File.separator)) {
      if (pathname.endsWith("/") || pathname.endsWith(File.separator)) {
         addSlash = false;
      }

      // read fcs to figure out what column in tileref.aft we need
      // to use to
      // read the fbr (face bounding rectangle) table
      try {

         String fcsFile = pathname + (addSlash ? "/" : "") + "fcs";

         if (!BinaryFile.exists(fcsFile)) {
            fcsFile += ".";
         }

         DcwRecordFile fcs = new DcwRecordFile(fcsFile);
         List<Object> fcsv = new ArrayList<Object>(fcs.getColumnCount());

         int fcscols[];

         if (!DCW) {
            fcscols = fcs.lookupSchema(fcsColumns, true);
         } else {
            fcscols = fcs.lookupSchema(fcsColumnsDCW, true);
         }

         while (fcs.parseRow(fcsv)) {
            String fclass = (String) fcsv.get(fcscols[0]);
            String table1 = (String) fcsv.get(fcscols[1]);
            String table1_key = (String) fcsv.get(fcscols[2]);
            /*
             * Not used String table2 = (String) fcsv.get(fcscols[3]); String
             * table2_key = (String) fcsv.get(fcscols[4]);
             */
            if ("tileref".equalsIgnoreCase(fclass) && "tileref.aft".equalsIgnoreCase(table1)) {
               faceIDColumnName = table1_key.toLowerCase();
               break;
            }
         }
         fcs.close();
      } catch (FormatException f) {
         // If DCW, we'll get here, need to try lookupSchema with
         // proper column names
         if (!DCW)
            doTileRefStuff(pathname, true);
         return;
         // either way, return. The recursive call may have worked.

      } catch (NullPointerException npe) {
         return; // file wasn't found...
      }

      if (faceIDColumnName == null) {
         return; // won't be able to read the tiling info. abort
      }

      isTiled = true;

      // Okay, we've got info on what column we use from tileref.aft
      // to index into the fbr.
      try {
         DcwRecordFile aft = new DcwRecordFile(pathname + (addSlash ? "/" : "") + "tileref.aft");
         int faceIDColumn = aft.whatColumn(faceIDColumnName.toLowerCase());
         int tileNameColumn = aft.whatColumn("tile_name");
         if ((faceIDColumn == -1) || (tileNameColumn == -1)) {
            aft.close();
            return;
         }

         String fbrFile = pathname + (addSlash ? "/" : "") + "fbr";
         if (!BinaryFile.exists(fbrFile)) {
            fbrFile += ".";
         }
         DcwRecordFile fbr = new DcwRecordFile(fbrFile);
         int fbrIDColumn = fbr.whatColumn(Constants.ID);

         List<Object> aftv = new ArrayList<Object>(aft.getColumnCount());
         List<Object> fbrv = new ArrayList<Object>(fbr.getColumnCount());
         int fbrcols[] = fbr.lookupSchema(fbrColumns, true);

         // set the array size to record count + 1, to be able to
         // use the tileID as the index into the array

         // aft.getRecordCount() is not reliable if file is being
         // read with a network input stream. So, we have to
         // create the TileDirectory[] a different way.
         // containedTiles = new TileDirectory[aft.getRecordCount()
         // + 1];
         // This is part of that solution...
         ArrayList<Object> tileArrayList = new ArrayList<Object>(500);
         Object nullTile = new Object();

         while (aft.parseRow(aftv)) {
            int fac_num = ((Number) aftv.get(faceIDColumn)).intValue();
            fbr.getRow(fbrv, fac_num); // mutates fbrv
            int tileid = ((Number) aftv.get(fbrIDColumn)).intValue();
            String tilename = (String) aftv.get(tileNameColumn);

            char chs[] = tilename.toCharArray();
            boolean goodTile = false;
            for (int i = 0; i < chs.length; i++) {
               if ((chs[i] != '\\') && (chs[i] != ' ')) {
                  goodTile = true;
               }
               if (chs[i] == '\\') {
                  // chs[i] = File.separatorChar;
                  chs[i] = '/'; // we're using BinaryFile, in
                  // java land...
               }
            }
            tilename = new String(chs);

            // Part of the URL solution...
            // This makes sure that the tileid can be used
            // for the index. If the tile is not good, then
            // nullTile will be set. If it is good, it will be
            // replaced. This will end up putting nullTile at
            // index 1 if the tileid is 1.
            while (tileid > tileArrayList.size() - 1) {
               tileArrayList.add(nullTile);
            }
            // End of solution addition part...

            if (!goodTile) {
               // Commenting out line is part of the solution,
               // the
               // spot is already marked with a nullTile object.
               // containedTiles[tileid] = null;
               continue;
            }

            float westlon = ((Number) fbrv.get(fbrcols[0])).floatValue();
            float southlat = ((Number) fbrv.get(fbrcols[1])).floatValue();
            float eastlon = ((Number) fbrv.get(fbrcols[2])).floatValue();
            float northlat = ((Number) fbrv.get(fbrcols[3])).floatValue();

            if (bounds == null) {
               bounds = new DataBounds(westlon, southlat, eastlon, northlat);
            } else {
               bounds.add(westlon, southlat);
               bounds.add(eastlon, northlat);
            }

            // Again, URL solution...
            // containedTiles[tileid] = new
            // TileDirectory(tilename, tileid,
            // northlat, southlat,
            // eastlon, westlon);

            tileArrayList.set(tileid, new TileDirectory(tilename, tileid, northlat, southlat, eastlon, westlon));

         }
         aft.close();
         fbr.close();

         // And this is the resolution of the solution, taking
         // the ArrayList and converting it to a TileDirectory
         // array.
         containedTiles = new TileDirectory[tileArrayList.size()];
         Iterator<Object> it = tileArrayList.iterator();
         int cnt = 0;
         while (it.hasNext()) {
            Object obj = it.next();
            if (obj == nullTile) {
               containedTiles[cnt++] = null;
            } else {
               containedTiles[cnt++] = (TileDirectory) obj;
            }
         }

      } catch (FormatException f) {
         // probably (hopefully?) untiled coverage...
         containedTiles = null;
      }
   }

   /**
    * Get the description of a coverage type
    * 
    * @param covname the name of the coverage type
    * @return the coverage description from the VPF database. A null return
    *         value indicates an unknown coverage type
    */
   public String getCoverageDescription(String covname) {
      CoverageEntry ce = (CoverageEntry) coverages.get(covname);
      return (ce == null) ? null : ce.getDescription();
   }

   /**
    * Get the topology level of a coverage.
    * 
    * @param covname the name of the coverage type
    * @return the topology level of the coverage (-1 if not a valid coverage)
    */
   public int getCoverageTopologyLevel(String covname) {
      CoverageEntry ce = (CoverageEntry) coverages.get(covname);
      return (ce == null) ? -1 : ce.getTopologyLevel();
   }

   /**
    * Get the CoverageTable for a particular coverage type
    * 
    * @param covname the name of the coverage type
    * @return the associated coverage table (possibly null)
    */
   public CoverageTable getCoverageTable(String covname) {
      CoverageEntry ce = (CoverageEntry) coverages.get(covname);
      if (ce != null) {
         if (ce.getCoverageTable() == null) {
            ce.setCoverageTable(new CoverageTable(dirpath, covname.intern(), this));
            if (Debug.debugging("vpf")) {
               Debug.output("Created new CoverageTable for " + covname + ": " + ce.description);
            }
         } else {
            if (Debug.debugging("vpf")) {
               Debug.output("Using cached CoverageTable for " + covname + ": " + ce.description);
            }
         }
         return ce.getCoverageTable();
      }
      return null;
   }

   public CoverageTable getCoverageTableForFeature(String featureName) {
      for (String key : coverages.keySet()) {
         CoverageEntry ce = coverages.get(key);
         Debug.output("CoverageTable: got " + ce + " for " + key);
         CoverageTable ct = ce.getCoverageTable();
         if (ct != null) {
            if (ct.getFeatureClassInfo(featureName) != null) {
               return ct;
            }
         } else {
            Debug.output("no coverage table for " + ce);
         }
      }
      return null;
   }

   /**
    * get a list of tiles in the bounding region
    * 
    * @param n northern boundary
    * @param s southern boundary
    * @param e eating foundry
    * @param w wheat bread
    * @return a vector of TileDirectories
    */
   public List<TileDirectory> tilesInRegion(float n, float s, float e, float w) {
      if (containedTiles == null) {
         return null;
      }
      List<TileDirectory> retval = new ArrayList<TileDirectory>();
      int numTiles = containedTiles.length;
      for (int i = 0; i < numTiles; i++) {
         TileDirectory tile = containedTiles[i];
         if (tile != null && tile.inRegion(n, s, e, w)) {
            retval.add(tile);
         }
      }
      return retval;
   }

   /**
    * Get the TileDirectory with the given ID number.
    */
   public TileDirectory getTileWithID(int id) {
      try {
         return containedTiles[id];
      } catch (ArrayIndexOutOfBoundsException aioobe) {
         return null;
      } catch (NullPointerException npe) {
         return null;
      }
   }

   /**
    * Know that the tile id are the integers used in the tileref.aft file. May
    * return null if the format of the id is bad, or if the tile doesn't really
    * exist (that really shouldn't happen).
    */
   public TileDirectory getTileWithID(String id) {
      try {
         return getTileWithID(Integer.parseInt(id));
      } catch (NumberFormatException nfe) {
         return null;
      }
   }

   /**
    * Find out if this library uses tiled data
    * 
    * @return true for tiled data
    */
   public boolean isTiledData() {
      return (containedTiles != null);

   }

   /**
    * Return the list of coverages this library has
    * 
    * @return the list of coverages (DCW would include "po", "dn"; VMAP would
    *         have "bnd", "tran", etc.)
    */
   public String[] getCoverageNames() {
      return (String[]) coverages.keySet().toArray(Constants.EMPTY_STRING_ARRAY);
   }

   /**
    * Gets a DataBounds object that specifies what the CAT covers.
    * 
    * @return DataBounds
    */
   public DataBounds getBounds() {
      return bounds;
   }

   /**
    * A utility class to hold information about one coverage type. Only the
    * associated coverage table may be modified after construction.
    */
   public static class CoverageEntry {
      /** the VPF topology level of this coverage type */
      private final int tLevel;
      /** the VPF description string of this coverage type */
      private final String description;
      /** the CoverageTable for this coverage type */
      private CoverageTable covtable;

      /**
       * Create a coverage entry without a coverage table
       * 
       * @param topologyLevel the topology level for this coverageentry
       * @param desc the description for this entry
       */
      public CoverageEntry(int topologyLevel, String desc) {
         this(topologyLevel, desc, null);
      }

      /**
       * Create a coverage entry with an initial coverage table
       * 
       * @param topologyLevel the topology level for this coverageentry
       * @param desc the description for this entry
       * @param covtable the coveragetable for this entry
       */
      public CoverageEntry(int topologyLevel, String desc, CoverageTable covtable) {
         this.tLevel = topologyLevel;
         this.description = desc;
         this.covtable = covtable;
      }

      /**
       * Get the topology level for this entry
       */
      public int getTopologyLevel() {
         return tLevel;
      }

      /**
       * Get the description for this entry
       */
      public String getDescription() {
         return description;
      }

      /**
       * Get the associated coveragetable
       */
      public CoverageTable getCoverageTable() {
         return covtable;
      }

      /**
       * Set the associated coveragetable
       * 
       * @param covtable the new coveragetable
       */
      /* package */void setCoverageTable(CoverageTable covtable) {
         this.covtable = covtable;
      }
   }
}