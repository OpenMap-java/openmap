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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/DbfFile.java,v $
// $RCSfile: DbfFile.java,v $
// $Revision: 1.4 $
// $Date: 2009/02/05 18:46:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bbn.openmap.dataAccess.shape.output.DbfOutputStream;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;

/**
 * An extension to DbfTableModel that handles reading only certain records when
 * needed, in conjunction with some spatial indexing mechanism. Can be told to
 * which columns to read and which to skip, so unnecessary/unused information
 * isn't held in memory.
 * 
 * @author dietrick
 */
public class DbfFile
        extends DbfTableModel {

    protected int _rowCount;
    protected int _recordLength;
    protected int _headerLength;
    /**
     * This _columnMask variable is an array that either contains a Boolean.TRUE
     * for the indexes for columns that should be read, or a Integer object that
     * contains the byte length of the column entry, so the reader will know how
     * many bytes to skip for columns being ignored. The _length array contents
     * were modified to reflect the lengths of only the columns being read when
     * the column mask was set, as was the _names, _types, and _decimalCount
     * arrays. The order and length of those arrays match the order of the
     * Boolean.TRUE objects in the _columnMask array.
     */
    protected Object[] _columnMask = null;
    protected java.text.DecimalFormat df;
    protected BinaryFile bf;

    protected DbfFile() {
        df = new java.text.DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
        df.setDecimalFormatSymbols(dfs);
    }

    /**
     * Creates a blank DbfTableModel
     * 
     * @param columnCount The number of columns this model will manage
     */
    public DbfFile(int columnCount) {
        this();
        _columnCount = columnCount;
        _records = new ArrayList();
        _lengths = new int[columnCount];
        _decimalCounts = new byte[columnCount];
        _types = new byte[columnCount];
        _names = new String[columnCount];
    }

    public DbfFile(BinaryFile bf)
            throws EOFException, FormatException, IOException {
        this();
        setBinaryFile(bf);
    }

    public void readHeader(BinaryFile bf)
            throws FormatException, IOException {
        try {
            bf.seek(0);
            bf.byteOrder(false);
            /* byte description = */bf.read();
            /* byte year = */bf.read();
            /* byte month = */bf.read();
            /* byte day = */bf.read();
            _rowCount = bf.readInteger();
            _headerLength = bf.readShort();
            _recordLength = bf.readShort();
            _columnCount = (_headerLength - 32 - 1) / 32;
            bf.skipBytes(20);

            _names = new String[_columnCount];
            _types = new byte[_columnCount];
            _lengths = new int[_columnCount];
            _decimalCounts = new byte[_columnCount];

            for (int n = 0; n <= _columnCount - 1; n++) {
                // 32 bytes for each column

                _names[n] = bf.readFixedLengthString(11);
                //
                // Some TIGER dbf files from ESRI have nulls
                // in the column names. Delete them.
                //
                int ix = _names[n].indexOf((char) 0);
                if (ix > 0) {
                    _names[n] = _names[n].substring(0, ix);
                }
                _types[n] = (byte) bf.read();
                bf.skipBytes(4);
                _lengths[n] = bf.readUnsigned();
                _decimalCounts[n] = (byte) bf.read();
                bf.skipBytes(14);
            }

            if (DEBUG && _headerLength != bf.getFilePointer()) {
                Debug.output("DbfFile: Header length specified in file doesn't match current pointer location");
            }

        } catch (EOFException eofe) {
            throw new FormatException(eofe.getMessage());
        }
    }

    /**
     * Tells the BinaryFile input reader to close, releasing the file pointer.
     * Will automatically reopen if necessary.
     */
    public void close() {
        if (bf != null) {
            try {
                bf.close();
            } catch (IOException e) {
                if (Debug.debugging("shape")) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads the data and puts data in an ArrayList of records.
     */
    public void readData()
            throws IOException, FormatException {
        readData(0, _rowCount);
    }

    /**
     * Read in a set of records from the dbf file, starting at the provided
     * index and continuing for the provided count.
     * 
     * @param startingRecordIndex , 0 is the first record index.
     * @param numRecordsToRead
     * @throws IOException
     * @throws FormatException
     */
    public void readData(int startingRecordIndex, int numRecordsToRead)
            throws IOException, FormatException {
        if (startingRecordIndex < 0) {
            startingRecordIndex = 0;
        }

        if (numRecordsToRead < 0 || numRecordsToRead > _rowCount - startingRecordIndex) {
            numRecordsToRead = _rowCount - startingRecordIndex;
        }

        _records = new ArrayList<List<Object>>(numRecordsToRead);
        for (int r = startingRecordIndex; r <= numRecordsToRead - 1; r++) {
            List<Object> record = getRecordData(r);
            _records.add(record);
        }
    }

    /**
     * Fetches the record data for the given index.
     * 
     * @param index the index of the data, starting at 0 for the first record.
     * @return List containing Strings and Numbers for the dbf entry for the
     *         record.
     * @throws IOException
     * @throws FormatException
     */
    public List<Object> getRecordData(int index)
            throws IOException, FormatException {
        if (bf == null) {
            throw new IOException("DbfFile not set with valid BinaryFile.");
        }

        bf.seek(_headerLength + index * _recordLength);

        /* int deleteFlag = */bf.read();
        int columnCount = _columnCount;
        if (_columnMask != null) {
            columnCount = _columnMask.length;
        }

        // Here, even with the columnMask, the _columnCount is the target number
        // of columns to be stored out of the dbf file. The _columnMask.length
        // is the number of columns actually in the file (if that array is not
        // null).
        ArrayList<Object> record = new ArrayList<Object>(_columnCount);
        int targetColumnIndex = 0;
        for (int c = 0; c <= columnCount - 1; c++) {

            if (_columnMask == null || _columnMask[c] == Boolean.TRUE) {
                int length = _lengths[targetColumnIndex];
                if (length == -1)
                    length = 255;
                int type = _types[targetColumnIndex];
                int numDecSpaces = _decimalCounts[targetColumnIndex];
                df.setMaximumFractionDigits(numDecSpaces);
                String cell = bf.readFixedLengthString(length).trim();
                Object obj = cell;
                try {
                    obj = getObjectForType(cell, type, df, length);
                } catch (ParseException pe) {
                    // Don't need to do anything, obj == cell;
                }
                record.add(targetColumnIndex, obj);
                targetColumnIndex++;
            } else {
                bf.skipBytes(((Integer) _columnMask[c]).intValue());
            }
        }
        return record;
    }

    /**
     * Clear the record information from memory.
     * 
     */
    public void clearRecords() {
        if (_records != null) {
            _records.clear();
        }
    }

    /**
     * Create another DbfTableModel with the same structure as this one (number
     * of columns, column names, lengths and decimal counts).
     */
    public DbfTableModel headerClone() {
        int size = getColumnCount();
        DbfFile dtm = new DbfFile(size);
        for (int i = 0; i < size; i++) {
            dtm.setColumnName(i, this.getColumnName(i));
            dtm.setDecimalCount(i, this.getDecimalCount(i));
            dtm.setLength(i, this.getLength(i));
            dtm.setType(i, this.getType(i));
        }
        return dtm;
    }

    /**
     * Creates a DbfTableModel for a given .dbf file
     * 
     * @param dbf The url of the file to retrieve.
     * @return The DbfTableModel, null if there is a problem.
     */
    public static DbfTableModel getDbfTableModel(URL dbf) {
        return getDbfTableModel(dbf.toString());
    }

    /**
     * Creates a DbfTableModel for a given .dbf file
     * 
     * @param dbf The path of the file to retrieve.
     * @return The DbfTableModel, null if there is a problem.
     */
    public static DbfTableModel getDbfTableModel(String dbf) {
        DbfFile model = null;
        try {
            BinaryBufferedFile bbf = new BinaryBufferedFile(dbf);
            model = new DbfFile(bbf);
            model.close();
        } catch (Exception exception) {
            if (Debug.debugging("shape")) {
                Debug.error("problem loading DBF file" + exception.getMessage());
            }
        }
        return model;
    }

    public static void main(String[] args) {
        Debug.init();
        ArgParser ap = new ArgParser("DbfFile");
        ap.add("columns", "Print field header information.");
        ap.add("mask", "Only show listed columns", -1);
        ap.add("source", "The dbf file to read.", 1);
        ap.add("target", "The dbf file to write, use with mask to remove columns into new dbf file.", 1);
        ap.add("num", "Specify the number of records to read and display (handy for large dbf files)", 1);

        if (!ap.parse(args)) {
            ap.printUsage();
            System.exit(0);
        }

        String source = null;
        String target = null;
        double num = Double.MAX_VALUE;

        String[] ags = ap.getArgValues("source");
        if (ags != null) {
            source = ags[0];
        } else {
            source = FileUtils.getFilePathToOpenFromUser("Choose DBF file");
            if (source == null) {
                System.exit(0);
            }
        }

        ags = ap.getArgValues("target");
        if (ags != null) {
            target = ags[0];
        }

        boolean readData = ap.getArgValues("columns") == null;

        if (!readData) {
            num = 0;
        } else {
            ags = ap.getArgValues("num");
            if (ags != null) {
                try {
                    num = Double.parseDouble(ags[0]);
                } catch (NumberFormatException nfe) {
                }
            }
        }

        String[] columnMask = ap.getArgValues("mask");
        String[] columns = ap.getArgValues("columns");

        try {

            DbfFile dtm = (DbfFile) DbfFile.getDbfTableModel(source);

            if (dtm == null) {
                System.out.println("Problem reading " + source);
                System.exit(-1);
            } else {

                if (columns != null) {
                    dtm.setColumnMask(columnMask);
                }

                dtm.readData(0, (int) num);

                if (target != null) {
                    OutputStream os = new FileOutputStream(target);
                    DbfOutputStream dos = new DbfOutputStream(os);
                    dos.writeModel(dtm);
                } else {
                    dtm.setWritable(true);
                    dtm.exitOnClose = true;
                    dtm.showGUI(args[0], MODIFY_ROW_MASK | MODIFY_COLUMN_MASK | SAVE_MASK);
                }
            }

        } catch (Exception e) {
            Debug.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks the _columnMask Object[] and looks for Boolean.TRUE objects,
     * indicating a column that should be used. Returns a boolean[] with trues
     * in the indexes for those columns.
     * 
     * @return boolean[] representing columns, true values should be used.
     */
    public boolean[] getColumnMask() {
        boolean[] columnMask = new boolean[_columnMask.length];
        for (int i = 0; i < _columnMask.length; i++) {
            columnMask[i] = _columnMask[i] == Boolean.TRUE;
        }
        return columnMask;
    }

    /**
     * Given a boolean[] where trues mark columns to keep, a _columnMask
     * Object[] is set on this object with Boolean.TRUE objects in that array
     * for the trues, and Integer objects representing the lengths of the false
     * columns. The lengths are used when reading the dbf file, so it's known
     * how many bytes to skip for that column.
     * 
     * @param mask
     */
    protected void createColumnMaskArray(boolean[] mask) {
        if (mask != null && mask.length <= _columnCount) {
            _columnMask = new Object[mask.length];
            for (int i = 0; i < mask.length; i++) {
                if (mask[i] == true) {
                    _columnMask[i] = Boolean.TRUE;
                } else {
                    _columnMask[i] = new Integer(_lengths[i]);
                }
            }
            resolveColumns();
        }
    }

    /**
     * Limit which columns are read from the dbf file using a boolean array
     * corresponding to the columns. For indexes in the array marked true, those
     * columns will be read. If the column mask has already been set, the dbf
     * file header will be re-read to reset the metadata for the file.
     * 
     * @param mask
     */
    public void setColumnMask(boolean[] mask) {
        try {
            if (_columnMask != null) {
                readHeader(bf);
            }
        } catch (Exception e) {
            Debug.error("problem setting column mask for DbfFile" + e.getMessage());
        }
        createColumnMaskArray(mask);
    }

    /**
     * Limit which columns are read from the dbf file using the column names. If
     * the column mask has already been set, the dbf file header will be re-read
     * to reset the metadata for the file.
     * 
     * @param columnNames
     */
    public void setColumnMask(String[] columnNames) {
        try {
            if (_columnMask != null) {
                readHeader(bf);
            }
        } catch (Exception e) {
            Debug.error("problem setting column mask for DbfFile" + e.getMessage());
        }
        if (columnNames != null && _names != null) {
            boolean[] mask = new boolean[_names.length];
            for (int j = 0; j < _names.length; j++) {
                for (int i = 0; i < columnNames.length; i++) {
                    if (_names[j].equalsIgnoreCase(columnNames[i])) {
                        mask[j] = true;
                        break;
                    }
                }
            }
            createColumnMaskArray(mask);
        }
    }

    /**
     * Sets the metadata for the dbf file to match the current _columnMask
     * settings.
     */
    protected void resolveColumns() {
        if (_columnMask != null && _columnMask.length == _columnCount) {
            int newColumnCount = 0;

            for (int i = 0; i < _columnMask.length; i++) {
                if (_columnMask[i] == Boolean.TRUE) {
                    newColumnCount++;
                }
            }

            ArrayList records = null;
            if (_records != null) {
                records = new ArrayList(_rowCount);
            }
            int[] lengths = new int[newColumnCount];
            byte[] decimalCounts = new byte[newColumnCount];
            byte[] types = new byte[newColumnCount];
            String[] names = new String[newColumnCount];
            int newIndex = 0;
            for (int i = 0; i < _columnMask.length; i++) {
                if (_columnMask[i] == Boolean.TRUE) {
                    lengths[newIndex] = _lengths[i];
                    decimalCounts[newIndex] = _decimalCounts[i];
                    types[newIndex] = _types[i];
                    names[newIndex] = _names[i];
                    if (records != null) {
                        records.add(_records.get(i));
                    }
                    newIndex++;
                }
            }

            _lengths = lengths;
            _decimalCounts = decimalCounts;
            _types = types;
            _names = names;
            _columnCount = newColumnCount;
            if (records != null) {
                _records = records;
            }
        }
    }

    public int getHeaderLength() {
        return _headerLength;
    }

    public void setHeaderLength(int length) {
        _headerLength = length;
    }

    public int getRecordLength() {
        return _recordLength;
    }

    public void setRecordLength(int length) {
        _recordLength = length;
    }

    public int getRowCount() {
        return _rowCount;
    }

    public void setRowCount(int count) {
        _rowCount = count;
    }

    public BinaryFile getBinaryFile() {
        return bf;
    }

    public void setBinaryFile(BinaryFile bf)
            throws EOFException, FormatException, IOException {        this.bf = bf;
        readHeader(bf);
    }

    public java.text.DecimalFormat getDecimalFormat() {
        return df;
    }

    public void setDecimalFormat(java.text.DecimalFormat df) {
        this.df = df;
    }
}