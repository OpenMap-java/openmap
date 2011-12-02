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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwRecordFile.java,v $
// $Revision: 1.6 $ $Date: 2005/01/10 16:36:21 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Read and encapsulate VPF table files.
 */
public class DcwRecordFile {

    /** input is read from this file */
    protected BinaryFile inputFile = null;
    /** the description of the table [read from the file] */
    protected String tableDescription = null;
    /** the name of another table that describes what this one is for */
    protected String documentationFileName = null;
    /** number of bytes consumed by the table header */
    private int headerLength = 4; //for the 4 bytes of the
    // headerlength field
    /**
     * big-endian (<code>true</code>) or little-endian (
     * <code>false</code>)
     */
    protected boolean MSBFirst = false;
    /** ordered set of columns (read from table header) */
    protected DcwColumnInfo[] columnInfo = null;
    /**
     * length of a record (<code>-1</code> indicates
     * variable-length record)
     */
    protected int recordLength = 0;
    /**
     * for tables with variable-length records, the corresponding
     * variable-length index
     */
    protected DcwVariableLengthIndexFile vli = null;
    /** the name of the file */
    final protected String filename;
    /** the name of the table */
    protected String tablename = null;
    /**
     * remember the byte order for later file openings, true for MSB
     * first
     */
    protected boolean byteorder = true;
    /** the record number that a call to parseRow() will return */
    int cursorRow = -1;

    /** the name of the row identifier column "id" */
    public static final String ID_COLUMN_NAME = "id";

    /**
     * Open a DcwRecordFile and completely initialize it
     * 
     * @param name the name of the file to use for input
     * @exception FormatException some problem was encountered dealing
     *            with the file
     */
    public DcwRecordFile(String name) throws FormatException {
        this(name, false);
    }

    /**
     * Open a DcwRecordFile
     * 
     * @param name the name of the file to use for input
     * @param deferInit if <code>true</code>, don't actually open
     *        files and initialize the object. In this state, the only
     *        method that should be called is finishInitialization.
     * @exception FormatException some problem was encountered dealing
     *            with the file
     * @see #finishInitialization()
     */
    public DcwRecordFile(String name, boolean deferInit) throws FormatException {
        this.filename = name;
        if (!deferInit) {
            finishInitialization();
        }
    }

    /**
     * Strip the tablename out of the filename. Strips both path
     * information and the trailing '.', if it exists.
     */
    private void internTableName() {
        int strlen = filename.length();
        int firstchar = filename.lastIndexOf('/');
        int lastchar = filename.endsWith(".") ? strlen - 1 : strlen;
        tablename = filename.substring(firstchar + 1, lastchar)
                .toLowerCase()
                .intern();
    }

    /**
     * Returns the File this instance is using
     * 
     * @return the File being read
     */
    public String getTableFile() {
        return filename;
    }

    /**
     * return the name of the table
     */
    public String getTableName() {
        return tablename;
    }

    /**
     * Complete initialization of this object. This function should
     * only be called once, and only if the object was constructed
     * with deferred initialization.
     * 
     * @exception FormatException some problem was encountered dealing
     *            with the file
     */
    public synchronized void finishInitialization() throws FormatException {
        internTableName();
        try {
            inputFile = new BinaryBufferedFile(filename);
        } catch (IOException e) {
            throw new FormatException(e.toString());
        }
        try {
            byte preHeaderLen[] = inputFile.readBytes(4, false);

            char delim = inputFile.readChar();
            switch (delim) {
            case 'L':
            case 'l':
                delim = inputFile.readChar();
            //Intentional fall through to set byteorder
            case ';': //default is LSB first
                byteorder = false;
                inputFile.byteOrder(byteorder);
                break;
            case 'M':
            case 'm': //alternatively, it can be MSB first
                byteorder = true;
                inputFile.byteOrder(byteorder);
                delim = inputFile.readChar();
                break;
            default:
                throw new FormatException("Invalid Byte Encoding Format");
            }
            headerLength += MoreMath.BuildInteger(preHeaderLen, byteorder);
            if (delim != ';') {//Sanity check the input
                throw new FormatException("Unexpected character in header");
            }
            tableDescription = inputFile.readToDelimiter(';');
            documentationFileName = inputFile.readToDelimiter(';');
            if ("-".equals(documentationFileName)) {
                documentationFileName = null;
            }

            ArrayList<Object> tmpcols = new ArrayList<Object>();
            try {
                while (true) {
                    DcwColumnInfo dci = new DcwColumnInfo(inputFile);
                    int collen = dci.fieldLength();
                    if ((collen == -1) || (recordLength == -1)) {
                        recordLength = -1;
                    } else {
                        recordLength += collen;
                    }
                    tmpcols.add(dci);
                }
            } catch (EOFException e) {
            }

            columnInfo = new DcwColumnInfo[tmpcols.size()];
            tmpcols.toArray(columnInfo);

            cursorRow = 1;
        } catch (EOFException e) {
            throw new FormatException("Caught EOFException: " + e.getMessage());
        } catch (NullPointerException npe) {
        }
    }

    /**
     * Returns a TilingAdapter for the selected column.
     * 
     * @param primColumnName the name of the primitive column
     * @return an appropriate TilingAdapter instance or null
     */
    public TilingAdapter getTilingAdapter(String primColumnName) {
        return getTilingAdapter(-1, whatColumn(primColumnName));
    }

    /**
     * Returns a TilingAdapter for the selected column.
     * 
     * @param primColumnName the name of the primitive column
     * @param tileColumnName the name of the tile_id column
     * @return an appropriate TilingAdapter instance or null
     */
    public TilingAdapter getTilingAdapter(String tileColumnName,
                                          String primColumnName) {
        return getTilingAdapter(whatColumn(tileColumnName),
                whatColumn(primColumnName));
    }

    /**
     * Returns a TilingAdapter for the selected column.
     * 
     * @param primColumn the position of the primitive column
     * @param tileColumn the position of the tile_id column
     * @return an appropriate TilingAdapter instance or null
     */
    public TilingAdapter getTilingAdapter(int tileColumn, int primColumn) {
        DcwColumnInfo tile = (tileColumn != -1) ? columnInfo[tileColumn] : null;
        if (primColumn == -1) {
            return null;
        }
        DcwColumnInfo prim = columnInfo[primColumn];
        TilingAdapter retval = null;
        char primFieldType = prim.getFieldType();
        if (tile == null) {
            if (primFieldType == 'K') {
                retval = new TilingAdapter.CrossTileAdapter(primColumn);
            } else if ((primFieldType == 'I') || (primFieldType == 'S')) {
                retval = new TilingAdapter.UntiledAdapter(primColumn);
            }
        } else {
            if (primFieldType == 'K') {
                //error??? duplicate tile data
                retval = new TilingAdapter.CrossTileAdapter(primColumn);
            } else if ((primFieldType == 'I') || (primFieldType == 'S')) {
                retval = new TilingAdapter.TiledAdapter(tileColumn, primColumn);
            }
        }
        return retval;
    }

    /**
     * Get the column number for a set of column names.
     * 
     * @param names the names of the columns
     * @return an array of column numbers
     * @exception FormatException the table does not match the
     *            specified schema
     */
    public int[] lookupSchema(String[] names, boolean mustExist)
            throws FormatException {
        int retval[] = new int[names.length];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = whatColumn(names[i]);
            if ((retval[i] == -1) && mustExist) {
                throw new FormatException("Column " + names[i]
                        + " doesn't exist");
            }
        }
        return retval;
    }

    /**
     * Get the column number for a set of column names.
     * 
     * @param names the names of the columns
     * @param type in same order as names
     * @param length in same order as names (-1 for a variable length
     *        column)
     * @param strictlength false means that variable length columns
     *        can be fixed-length instead
     * @param mustExist if true and a column doesn't exist, method
     *        returns null
     * @return an array of column numbers
     * @exception FormatException the table does not match the
     *            specified schema
     */
    public int[] lookupSchema(String[] names, boolean mustExist, char type[],
                              int length[], boolean strictlength)
            throws FormatException {
        int retval[] = lookupSchema(names, mustExist);
        if ((type.length == names.length) && (length.length == names.length)) {
            for (int i = 0; i < retval.length; i++) {
                if (retval[i] != -1) {
                    columnInfo[retval[i]].assertSchema(type[i],
                            length[i],
                            strictlength);
                }
            }
        }
        return retval;
    }

    /**
     * Good for looking at the contents of a data file, this method
     * dumps a bunch of rows to System.out. It parses all the lines of
     * the file.
     * 
     * @exception FormatException some kind of data format error was
     *            encountered while parsing the file
     */
    public void parseAllRowsAndPrintSome() throws FormatException {
        int row_id_column = whatColumn(ID_COLUMN_NAME);
        String vectorString = null;
        int rowcount = 0;
        for (List<Object> l = new ArrayList<Object>(getColumnCount()); parseRow(l);) {
            int cnt = ((Number) (l.get(row_id_column))).intValue();
            if (cnt != ++rowcount) {
                System.out.println("Non-consecutive row number.  Expected "
                        + rowcount + " got " + cnt);
            }
            vectorString = VPFUtil.listToString(l);
            if ((rowcount < 20) || (rowcount % 100 == 0)) {
                System.out.println(vectorString);
            }
        }
        if (rowcount > 20)
            System.out.println(vectorString);
    }

    /**
     * Good for looking at the contents of a data file, this method
     * dumps a bunch of rows to System.out. (Using seekToRow to move
     * between records
     * 
     * @exception FormatException some kind of data format error was
     *            encountered while parsing the file
     */
    public void parseSomeRowsAndPrint() throws FormatException {
        int row_id_column = whatColumn(ID_COLUMN_NAME);
        int rowcount = getRecordCount();
        for (int i = 1; i <= rowcount; i++) {
            if ((i > 10) && ((i % 100) != 0) && (i != rowcount)) {
                continue;
            }
            seekToRow(i);
            List<Object> l = parseRow();
            int cnt = ((Integer) (l.get(row_id_column))).intValue();
            if (cnt != i) {
                System.out.println("Possible incorrect seek for row number "
                        + i + " got " + cnt);
            }
            System.out.println(VPFUtil.listToString(l));
        }
    }

    /**
     * Return a row from the table. repeatedly calling parseRow gets
     * consecutive rows.
     * 
     * @return a List of fields read from the table
     * @exception FormatException an error was encountered reading the
     *            row
     */
    public List<Object> parseRow() throws FormatException {
        List<Object> retval = new ArrayList<Object>(getColumnCount());
        return parseRow(retval) ? retval : null;
    }

    /**
     * Return a row from the table. repeatedly calling parseRow gets
     * consecutive rows.
     * 
     * @param retval append the fields from a row in the table.
     *        clear() is called before any real work is done.
     * @return true is we read a row, false if no more rows are
     *         available
     * @exception FormatException an error was encountered reading the
     *            row
     * @see java.util.List#clear()
     */
    public synchronized boolean parseRow(List<Object> retval) throws FormatException {
        retval.clear();
        try {
            for (int i = 0; i < columnInfo.length; i++) {
                Object newobj = columnInfo[i].parseField(inputFile);
                retval.add(newobj);
            }
            cursorRow++;
            return true;
        } catch (FormatException f) {
            throw new FormatException("DcwRecordFile: parserow on table "
                    + filename + ": " + f.getMessage());
        } catch (EOFException e) {
            if (!retval.isEmpty()) {
                throw new FormatException("DcwRecordFile: hit EOF when list = "
                        + VPFUtil.listToString(retval));
            }
            try {
                if (inputFile.available() > 0) {
                    throw new FormatException("DcwRecordFile: hit EOF with available = "
                            + inputFile.available()
                            + " when list = "
                            + VPFUtil.listToString(retval));
                }
            } catch (IOException i) {
                throw new FormatException("IOException calling available()");
            }
            return false;
        }
    }

    /**
     * Returns the documentation file associated with this table.
     * 
     * @return the doc file - may be null
     */
    public String getDocumentationFilename() {
        return documentationFileName;
    }

    /**
     * Returns the table description for this table.
     * 
     * @return the table description - may be null
     */
    public String getDescription() {
        return tableDescription;
    }

    /**
     * get the length of a single record
     * 
     * @return -1 indicates a variably sized record
     */
    public int getRecordLength() {
        return recordLength;
    }

    /**
     * Gets the number of records in the table.
     * 
     * @return the number of records
     * @exception FormatException some problem was encountered dealing
     *            with the file
     */
    public int getRecordCount() throws FormatException {
        try {
            if (recordLength == -1) {
                return vli().getRecordCount();
            } else {
                return (int) (inputFile.length() - headerLength) / recordLength;
            }
        } catch (IOException i) {
            System.out.println("RecordCount: io exception " + i.getMessage());
        } catch (NullPointerException npe) {
        }
        return -1;
    }

    final private DcwVariableLengthIndexFile vli() throws FormatException,
            IOException {
        if (vli == null) {
            openVLI();
        }
        return vli;
    }

    /**
     * Opens the associated variable length index for the file
     * 
     * @exception FormatException an error.
     */
    private void openVLI() throws FormatException, IOException {
        String realfname = filename;
        boolean endwithdot = realfname.endsWith(".");
        String fopen;
        if (endwithdot) {
            StringBuffer newf = new StringBuffer(realfname.substring(0,
                    realfname.length() - 2));
            fopen = newf.append("x.").toString();
        } else {
            StringBuffer newf = new StringBuffer(realfname.substring(0,
                    realfname.length() - 1));
            fopen = newf.append("x").toString();
        }

        vli = new DcwVariableLengthIndexFile(new BinaryBufferedFile(fopen), byteorder);
    }

    /**
     * Parses the row specified by rownumber
     * 
     * @param rownumber the number of the row to return
     *        [1..recordCount]
     * @return the values contained in the row
     * @exception FormatException data format errors
     */
    public List<Object> getRow(int rownumber) throws FormatException {
        List<Object> l = new ArrayList<Object>(getColumnCount());
        return getRow(l, rownumber) ? l : null;
    }

    /**
     * Parses the row specified by rownumber
     * 
     * @param rownumber the number of the row to return
     *        [1..recordCount]
     * @param retval values contained in the row
     * @exception FormatException data format errors
     * @see #parseRow()
     */
    public synchronized boolean getRow(List<Object> retval, int rownumber)
            throws FormatException {
        if (inputFile == null) {
            reopen(rownumber);
        } else {
            seekToRow(rownumber);
        }
        return parseRow(retval);
    }

    /**
     * moves the input cursor to the specified row [affects subsequent
     * calls parseRow.]
     * 
     * @param recordNumber the number of the row to seek to
     * @exception FormatException data format errors
     * @exception IllegalArgumentException recordNumber less than 1
     */
    public synchronized void seekToRow(int recordNumber) throws FormatException {
        if (recordNumber <= 0) {
            throw new IllegalArgumentException("DcwRecordFile: seekToRow("
                    + recordNumber + "," + getRecordCount() + "," + filename
                    + ")");
        }
        if (recordNumber == cursorRow) {
            return;
        }
        cursorRow = recordNumber;
        int offset = 0;
        try {
            if ((recordLength == -1) && (recordNumber != 1)) {
                offset = vli().recordOffset(recordNumber);
            } else {
                offset = (recordLength * (recordNumber - 1)) + headerLength;
            }

            inputFile.seek(offset);
        } catch (IOException io) {
            throw new FormatException("SeekToRow IOException "
                    + io.getMessage() + " offset: " + offset + " " + tablename
                    + " " + filename);
        }
    }

    /**
     * Returns the index into columnInfo of the column with the
     * specified name
     * 
     * @param columnname the column name to match
     * @return an index into columnInfo (-1 indicates no such column)
     */
    public int whatColumn(String columnname) {
        for (int i = 0; i < columnInfo.length; i++) {
            if (columnInfo[i].getColumnName().equals(columnname)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the name of a column
     * 
     * @param index the column to get the name for
     * @return the columnName
     */
    public String getColumnName(int index) {
        return columnInfo[index].getColumnName();
    }

    /**
     * Prints the table information to System.out.
     * 
     * @exception FormatException some problem was encountered dealing
     *            with the file
     */
    public void printSchema() throws FormatException {
        System.out.println("File Name: " + filename + "\nTable name: "
                + tablename + "\nTable Description: " + tableDescription
                + "\nDocumentation File Name: " + documentationFileName
                + "\nRecord Length: " + recordLength + " Record Count: "
                + getRecordCount());
        for (int i = 0; i < columnInfo.length; i++) {
            System.out.println("Column " + i + " " + columnInfo[i].toString());
        }
    }

    /** Closes the associated input file. (may later get reopened) */
    public synchronized void close() {
        cursorRow = -1;
        try {
            if (inputFile != null) {
                inputFile.close();
            }
            inputFile = null;
        } catch (IOException i) {
            System.out.println("Caught ioexception " + i.getMessage());
        }
    }

    /**
     * Reopen the associated input file.
     * 
     * @param seekRow the row to seek to upon reopening the file. If
     *        seekRow is invalid (less than 1), then the input stream
     *        is in an undefined location, and seekToRow (or
     *        getRow(int)) must be called before parseRow
     * @exception FormatException some error was encountered in
     *            reopening file or seeking to the desired row.
     * @see #parseRow()
     * @see #getRow(int)
     * @see #close()
     */
    public synchronized void reopen(int seekRow) throws FormatException {
        try {
            if (inputFile == null) {
                inputFile = new BinaryBufferedFile(filename);
                inputFile.byteOrder(byteorder);
            }
            if (seekRow > 0) {
                seekToRow(seekRow);
            }
        } catch (IOException i) {
            throw new FormatException(i.getClass() + ": " + i.getMessage());
        }
    }

    /**
     * Returns the number of columns this table has
     */
    final public int getColumnCount() {
        return columnInfo.length;
    }

    /**
     * Return the column info for this table.
     * <p>
     * NOTE: modifying this array is likely to cause problems...
     */
    final public DcwColumnInfo[] getColumnInfo() {
        return columnInfo;
    }

    /** releases associated resources */
    protected void finalize() {
        close();
    }

    /**
     * An test main for parsing VPF table files.
     * 
     * @param args file names to be read
     */
    public static void main(String args[]) {
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
            try {
                DcwRecordFile foo = new DcwRecordFile(args[i]);
                foo.printSchema();
                foo.close();
                foo.reopen(1);
                for (List<Object> l = new ArrayList<Object>(); foo.parseRow(l);) {
                    System.out.println(VPFUtil.listToString(l));
                }
                foo.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}