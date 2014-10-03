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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/FeatureClassInfo.java,v $
// $RCSfile: FeatureClassInfo.java,v $
// $Revision: 1.8 $
// $Date: 2006/06/20 20:14:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.Debug;

/**
 * This class wraps a feature type file (potext.tft, polbndl.lft, etc) from VPF.
 * It maintains sufficient information about the table it is indexed from so
 * that it can take a List of values, rather than a single value. It also knows
 * about its containing CoverageTable so it can look up information in int.vdt
 * and char.vdt.
 */
public class FeatureClassInfo extends DcwRecordFile implements TerminatingRunnable,
        com.bbn.openmap.io.Closable {

    /** the table to look up .vdt info from */
    final private CoverageTable ctable;
    /**
     * The name of our column from the primitive table (e.g. potext.tft_id).
     * This is the name of the column that will let you know, in the primitive
     * file (like edg), what type of primitive (featurewise) is being
     * represented on that row. This field does not always exist! If it doesn't,
     * all the features in the file are rendered.
     */
    final private String columnname;

    /** the column number in the primitive table for columnname */
    private int mycolumn = -1;
    /** true means the object has run(), false otherwise */
    private boolean fullInit = false;

    /** things constructed with deferred initialization get queued here */
    // private static RunQueue tq = new RunQueue(true, Thread.MIN_PRIORITY,
    // true);

    /** temporary list for use in getDescription() */
    final private List<Object> tmpVec = new ArrayList<Object>();

    // Feature Classes cross reference each other. For any feature
    // class name, you can have:
    //
    // 1) a DcwRecordFile (EdgeTable, NodeTable, AreaTable,
    // TextTable), with a certain column used as an ID, reference a
    // FeatureTable with a column that uses that ID.
    //
    // 2) a FeatureTable (.pft, .aft, .lft, .tft) using a column
    // holding an ID, referencing a DcwRecordFile with a column
    // holding that ID.
    //
    // The FeatureTable shows, for a particular feature type, the
    // individual primitive features for that particular feature type,
    // their FACC code, what tile that feature resides in, and the
    // feature ID number in that tile.
    //
    // The DcwRecordFile contains the actual data for the primitive,
    // and each DceRecordFile contains like feature primitives (edges,
    // areas, text, points). Each line in the DcwRecordFile contains
    // the ID number of the primitive,

    /**
     * Construct a FeatureClassInfo.
     * 
     * @param cthis the CoverageTable to use for vdt lookups
     * @param colname the column name from the primitive table
     * @param tablepath the directory of the feature table
     * @param ftname the name of the feature type
     * @exception FormatException some error was encountered
     */
    public FeatureClassInfo(CoverageTable cthis, String colname, String tablepath, String ftname)
            throws FormatException {
        super(tablepath + ftname, true); // defer initialization

        if (Debug.debugging("vpf.fci")) {
            Debug.output("FCI: set to peruse (" + filename + ")\n\tcreated with colname ("
                    + colname + ")\n\ttablepath (" + tablepath + ")\n\tftname (" + ftname + ")");
        }

        ctable = cthis;
        columnname = colname.toLowerCase().intern();
    }

    /** the name of the primitive file: edg, fac, end, cnd */
    protected String tileFileName;
    /** the name of the column with the primitive id */
    protected String tileFileColName;
    /** the type of feature this table represents */
    protected char featureType;

    /**
     * Construct a FeatureClassInfo that can be used for feature search
     * 
     * @param cthis the CoverageTable to use for vdt lookups
     * @param colname the column name from the primitive table
     * @param tablepath the directory of the feature table
     * @param ftname the name of the feature type
     * @param tileDirFile the name of the primitive file
     * @param tileDirFileColName the name of the primitive id column
     * @exception FormatException some error was encountered
     */
    public FeatureClassInfo(CoverageTable cthis, String colname, String tablepath, String ftname,
            String tileDirFile, String tileDirFileColName) throws FormatException {

        super(tablepath + ftname, false); // don't defer
        // initialization
        fullInit = true;

        ctable = cthis;
        columnname = colname.toLowerCase().intern();

        tileFileName = tileDirFile;
        tileFileColName = tileDirFileColName;

        if ("fac".equals(tileFileName)) {
            featureType = CoverageTable.AREA_FEATURETYPE;
        } else if ("end".equals(tileFileName)) {
            featureType = CoverageTable.EPOINT_FEATURETYPE;
        } else if ("cnd".equals(tileFileName)) {
            featureType = CoverageTable.CPOINT_FEATURETYPE;
        } else if ("txt".equals(tileFileName)) {
            featureType = CoverageTable.TEXT_FEATURETYPE;
        } else if ("edg".equals(tileFileName)) {
            featureType = CoverageTable.EDGE_FEATURETYPE;
        } else {
            featureType = CoverageTable.SKIP_FEATURETYPE;
        }

        if (Debug.debugging("vpf.fci")) {
            Debug.output("FCI: set to peruse (" + filename + ")\n\tcreated with column name ("
                    + colname + ")\n\ttile directory file (" + tileDirFile
                    + ")\n\ttile id column (" + tileDirFileColName + ")");
        }
    }

    /**
     * Returns a TilingAdapter suitable for retrieving primitive ids from
     * records in this feature table.
     * 
     * @return a tilingadapter or null
     */
    public TilingAdapter getTilingAdapter() {
        return getTilingAdapter(TILE_ID_COLUMN_NAME, tileFileColName);
    }

    /** the name of the column where tiling information lives */
    public final static String TILE_ID_COLUMN_NAME = "tile_id";

    /**
     * Returns the file name (no path info) of the thematic index for the
     * tile_id column.
     */
    public String getTileThematicFileName() {
        if (columnInfo != null) {
            int colId = getTileIdIndex();
            if (colId != -1) {
                return columnInfo[colId].getThematicIndexName();
            }
        }
        return null;
    }

    /** the thematic index for the tile_id column */
    protected DcwThematicIndex thematicIndex = null;

    /**
     * Causes the thematic index for the tile_id column to be initialized.
     * 
     * @param path the path to the directory where the index lives
     * @return true if a thematic index is available, false if not
     */
    public synchronized boolean initThematicIndex(String path) {
        try {
            if (thematicIndex == null) {
                // See if we can use the thematic index to see which
                // tiles
                // have the features we want.
                String thematicIndexName = getTileThematicFileName();
                if (thematicIndexName != null) {
                    thematicIndex = new DcwThematicIndex(path + thematicIndexName, byteorder);
                }
            }
        } catch (FormatException fe) {
            if (Debug.debugging("vpf.FormatException")) {
                Debug.output("FeatureClassInfo.initTI: " + fe.getClass() + " " + fe.getMessage());
            }
            return false;
        }
        return (thematicIndex != null);
    }

    /**
     * Returns the thematic index for the tile_id column, if it has been
     * initialized.
     * 
     * @return null or a themaitc index for the column
     */
    public DcwThematicIndex getThematicIndex() {
        return thematicIndex;
    }

    /**
     * Returns the column position of the tile_id column.
     * 
     * @see DcwRecordFile#whatColumn(String)
     */
    public int getTileIdIndex() {
        return whatColumn(TILE_ID_COLUMN_NAME);
    }

    /**
     * Returns the column position of the f_code column.
     * 
     * @see DcwRecordFile#whatColumn(String)
     */
    public int getFaccIndex() {
        return whatColumn("f_code");
    }

    /**
     * Returns the column position of the primitive id column.
     * 
     * @see DcwRecordFile#whatColumn(String)
     */
    public int getTilePrimitiveIdColIndex() {
        return whatColumn(tileFileColName);
    }

    /**
     * Return the type of feature this table is for. Returns one of the
     * featuretype codes in CoverageTable.
     * 
     * @see CoverageTable#AREA_FEATURETYPE
     */
    public char getFeatureType() {
        return featureType;
    }

    /**
     * Complete the initialization of the FeatureClassInfo. This function can be
     * called more than once.
     */
    public synchronized void run() {
        if (fullInit == true) {// run already ran, or the file didn't
            // exist
            return;
        }

        try {
            fullInit = true;
            finishInitialization(); // finish initialization of table

            // The list isn't be closed as it's supposed to, and this
            // is causing a leak. We'll just avoid the list for now
            // and just close the files after we've read them.

            // BinaryFile.addClosable(this);
        } catch (FormatException f) {
            // close(); //invalidate some stuff
        }
        close();
    }

    /**
     * Implement the Closable interface
     */
    public boolean close(boolean done) {
        close();
        if (thematicIndex != null) {
            try {
                thematicIndex.close();
            } catch (FormatException fe) {
                // ignored
            }
        }
        return true;
    }

    /**
     * Probe the DcwRecordFile looking for what column we are in. (Info needed
     * later to getDescription with the data list.)
     * 
     * @param rf the primitive data table we'll get rows from
     */
    public void findYourself(DcwRecordFile rf) {
        mycolumn = rf.whatColumn(columnname);
    }

    /**
     * Given a row from the primitive table, this function returns a full string
     * description of the row
     * 
     * @param l the record list from the primitive table
     * @param type the first integral type
     * @return the description string for the list
     */
    public synchronized String getDescription(List<Object> l, MutableInt type) {
        checkInit();
        if (mycolumn == -1) {
            return null;
        }
        int i = VPFUtil.objectToInt(l.get(mycolumn));
        if (i <= 0) {
            return null;
        }
        return getDescription(i, type);
    }

    /**
     * Given a row from the primitive table, this function returns a full string
     * description of the row
     * 
     * @param ftid the record list from the primitive table
     * @param colIndex column index for attribute to return
     * @param type the first integral type
     * @return the description string for the list
     */
    // public synchronized String getAttribute(List l, int colIndex,
    public synchronized String getAttribute(int ftid, int colIndex, MutableInt type) {
        checkInit();
        // if (mycolumn == -1) {
        // return null;
        // }
        // int ftid = VPFUtil.objectToInt(l.get(mycolumn));
        if (ftid <= 0) {
            return null;
        }

        try {
            if (!getRow(tmpVec, ftid)) {
                return null;
            }
        } catch (FormatException fe) {
            if (Debug.debugging("vpf")) {
                fe.printStackTrace();
            }
        }

        return getAttribute(columnInfo[colIndex], tmpVec.get(colIndex), type);
    }

    /**
     * Check to see if the file has been fully initialized, call run() to do
     * that if needed.
     */
    public synchronized void checkInit() {
        if (fullInit == false) {
            if (Debug.debugging("vpf")) {
                Debug.output("FCI.checkInit() forcing init " + columnname + " " + tablename);
            }
            run();
        }
    }

    /**
     * Given an primary key (row id) for the feature table, return the string
     * description. If made public, this function would need to be synchronized
     * and check for proper initialization. But since it is always called from a
     * method that does that, its okay.
     * 
     * @param ftid the row id for our feature table
     * @param type the first integral type
     * @return the description string for the list
     */
    private synchronized String getDescription(int ftid, MutableInt type) {
        StringBuffer retval = null;
        try {
            if (!getRow(tmpVec, ftid)) {
                return null;
            }

            // boolean haveivdtindex = false;

            for (int i = 0; i < columnInfo.length; i++) {
                DcwColumnInfo dci = columnInfo[i];

                String s = getAttribute(dci, tmpVec.get(i), type);
                // ////////
                // String s = null;
                // String dciVDT = dci.getVDT();
                // if (dciVDT == Constants.intVDTTableName) {
                // int val = VPFUtil.objectToInt(tmpVec.get(i));
                // if (val == Integer.MIN_VALUE) {//VPF null
                // continue;
                // }
                // if (!haveivdtindex) {
                // type.value = (short) val;
                // haveivdtindex = true;
                // }
                // s = ctable.getDescription(tablename,
                // dci.getColumnName(),
                // val);
                // if (s == null) {
                // s = "[" + val + "]";
                // }
                // } else if (dciVDT == Constants.charVDTTableName) {
                // String val = (String) tmpVec.get(i);
                // s = ctable.getDescription(tablename,
                // dci.getColumnName(),
                // val);
                // if (s == null) {
                // s = "[" + val + "]";
                // }
                // } else if (dci.isNonKey()) {
                // s = tmpVec.get(i).toString();
                // }
                // //////

                if (s != null) {
                    if (retval == null) {
                        retval = new StringBuffer(s);
                    } else {
                        retval.append("; ").append(s);
                    }
                }
            }
        } catch (FormatException e) {
            if (Debug.debugging("vpf")) {
                e.printStackTrace();
            }
        }
        return ((retval == null) ? null : retval.toString());
    }

    protected String getAttribute(DcwColumnInfo dci, Object colObj, MutableInt type) {

        if (colObj == null) {
            return null;
        }
        
        String dciVDT = dci.getVDT();
        if (Constants.intVDTTableName.equals(dciVDT)) {
            int val = VPFUtil.objectToInt(colObj);
            if (val == Integer.MIN_VALUE) {// VPF null
                return null;
            }

            if (type != null) {
                type.value = (short) val;
            }

            return ctable.getDescription(tablename, dci.getColumnName(), val);

        } else if (Constants.charVDTTableName.equals(dciVDT)) {
            return (String) colObj;
            // } else if (dci.isNonKey()) {
            // s = colObj.toString();
        }

        return colObj.toString();
    }

    /**
     * @return space separated list of column names, used mostly for debugging
     *         feature attribute lookups.
     */
    public String columnNameString() {
        StringBuffer sBuf = new StringBuffer();
        for (DcwColumnInfo dci : getColumnInfo()) {
            sBuf.append(dci.getColumnName()).append(' ');
        }
        return sBuf.toString().trim();
    }

}