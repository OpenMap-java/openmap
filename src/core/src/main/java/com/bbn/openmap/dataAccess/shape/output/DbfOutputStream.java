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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/output/DbfOutputStream.java,v $
// $RCSfile: DbfOutputStream.java,v $
// $Revision: 1.18 $
// $Date: 2009/02/05 18:46:11 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;

/**
 * Writes date in a DbfTableModel to a file, conforming to the DBF III file
 * format specification
 * 
 * @author Doug Van Auken
 */
public class DbfOutputStream {
    /**
     * An outputstream that writes primitive data types in little endian or big
     * endian
     */
    private LittleEndianOutputStream _leos;

    /**
     * Creates a DbfOutputStream
     */
    public DbfOutputStream(OutputStream os) {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        _leos = new LittleEndianOutputStream(bos);
    }

    /**
     * Writes the model out on the stream. The stream is closed automatically
     * after the write.
     * 
     * @param model the tablemodel to write
     */
    public void writeModel(DbfTableModel model) throws IOException {
        int rowCount = model.getRowCount();
        short headerLength = calcHeaderLength(model);
        short recordLength = calcRecordLength(model);

        writeHeader(rowCount, headerLength, recordLength);
        writeFieldDescriptors(model);
        writeRecords(model);

        close();
    }

    /**
     * Calculates the length of the record by aggregating the length of each
     * field
     * 
     * @param model The DbfTableModel for which to calculate the record length
     * @return The length of a record
     */
    public short calcRecordLength(DbfTableModel model) {
        int length = 0;
        int columnCount = model.getColumnCount();
        for (int i = 0; i <= columnCount - 1; i++) {
            length += model.getLength(i);
        }
        length += 1;
        Integer integer = new Integer(length);
        return integer.shortValue();
    }

    /**
     * Calculates the length of the header in terms of bytes
     * 
     * @param model The DbfTableModel for which to calculate header length
     * @return The header length
     */
    public short calcHeaderLength(DbfTableModel model) {
        int length = 0;
        length += model.getColumnCount() * 32; // 32 bytest for each
        // record
        length += 32; // 32 bytes for the record
        length += 1; // 1 byte for header terminator
        Integer integer = new Integer(length);
        return integer.shortValue();
    }

    /**
     * Writes the header to the class scope LittleEndianOutputStream
     * 
     * @param rowCount The number of records
     * @param headerLength The length, in terms of bytes, of the header section
     * @param recordLength The length, in terms of bytes, of each records
     */
    private void writeHeader(int rowCount, short headerLength,
                             short recordLength) throws IOException {

        _leos.writeByte(3); // byte 0
        _leos.writeByte(96); // Byte 1 - Year
        _leos.writeByte(4); // Byte 2 - Month
        _leos.writeByte(30); // Byte 3 - Day
        _leos.writeLEInt(rowCount); // Byte 4 Number of records in the
        // table
        _leos.writeLEShort(headerLength); // byte 8 Number of bytes in
        // the header
        _leos.writeLEShort(recordLength); // byte 10 Number of bytes
        // in the record
        _leos.writeByte(0); // Byte 12
        _leos.writeByte(0); // Byte 13
        _leos.writeByte(0); // Byte 14
        _leos.writeByte(0); // Byte 15
        _leos.writeByte(0); // Byte 16
        _leos.writeByte(0); // Byte 17
        _leos.writeByte(0); // Byte 18
        _leos.writeByte(0); // Byte 19
        _leos.writeByte(0); // Byte 20
        _leos.writeByte(0); // Byte 21
        _leos.writeByte(0); // Byte 22
        _leos.writeByte(0); // Byte 23
        _leos.writeByte(0); // Byte 24
        _leos.writeByte(0); // Byte 25
        _leos.writeByte(0); // Byte 26
        _leos.writeByte(0); // Byte 27
        _leos.writeByte(0); // Byte 28
        _leos.writeByte(0); // Byte 29
        _leos.writeByte(0); // Byte 30
        _leos.writeByte(0); // Byte 31
    }

    /**
     * Iterates through the DbfTableModel's collection of columns and calls the
     * writeFieldDescriptor method for each column
     * 
     * @param model The DbfTableModel
     */
    private void writeFieldDescriptors(DbfTableModel model) throws IOException {
        int columnCount = model.getColumnCount();
        for (int i = 0; i <= columnCount - 1; i++) {
            String name = model.getColumnName(i);
            int length = model.getLength(i);
            byte decimalCount = model.getDecimalCount(i);
            byte type = model.getType(i);
            writeFieldDescriptor(name, type, length, decimalCount);
        }
        _leos.writeByte(13);
    }

    /**
     * Writes records to the LittleEndianOutputStream
     * 
     * @param name The field name
     * @param type The field type
     * @param length The field length
     * @param decimalPlaces The number of decimal places for each field
     */
    private void writeFieldDescriptor(String name, byte type, int length,
                                      byte decimalPlaces) throws IOException {
        _leos.writeString(name, 11); // Byte 0-10
        _leos.writeByte(type); // Byte 11
        _leos.writeByte(0); // Byte 12 Field data address(0)
        _leos.writeByte(0); // Byte 13 Field data address(1)
        _leos.writeByte(0); // Byte 14 Field data address(2)
        _leos.writeByte(0); // Byte 15 Field data address(3)
        _leos.writeByte(length); // Byte 16 Field length in bytes
        _leos.writeByte(decimalPlaces); // Byte 17 Field decimal
        // places
        _leos.writeByte(0); // Byte 18 Reserved for dBASE III PLUS on
        // a LAN(0)
        _leos.writeByte(0); // Byte 19 Reserved for dBASE III PLUS on
        // a LAN(1)
        _leos.writeByte(0); // Byte 20 Work area 1D
        _leos.writeByte(0); // Byte 21 Reserved for dBASE III PLUS on
        // a LAN(0)
        _leos.writeByte(0); // Byte 22 Reserved for dBASE III PLUS on
        // a LAN(1)
        _leos.writeByte(0); // Byte 23 SET FIELDS Flag
        _leos.writeByte(0); // Byte 24 Reserved Bytes(0) #24
        _leos.writeByte(0); // Byte 25 Reserved Bytes(0) #25
        _leos.writeByte(0); // Byte 26 Reserved Bytes(0) #26
        _leos.writeByte(0); // Byte 27 Reserved Bytes(0) #27
        _leos.writeByte(0); // Byte 28 Reserved Bytes(0) #28
        _leos.writeByte(0); // Byte 29 Reserved Bytes(0) #29
        _leos.writeByte(0); // Byte 30 Reserved Bytes(0) #30
        _leos.writeByte(0); // Byte 31 Reserved Bytes(0) #31
    }

    public void writeRecords(DbfTableModel model) throws IOException {

        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);
        df.setDecimalFormatSymbols(dfs);
        df.setGroupingUsed(false);
        
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();
        for (int r = 0; r <= rowCount - 1; r++) {
            _leos.writeByte(32);
            for (int c = 0; c <= columnCount - 1; c++) {
                byte type = model.getType(c);
                int columnLength = model.getLength(c);
                int numDecSpaces = model.getDecimalCount(c);
                df.setMaximumFractionDigits(numDecSpaces);
                df.setGroupingUsed(false);
                String value = DbfTableModel.getStringForType(model.getValueAt(r, c),
                        type,
                        df, columnLength);

                int length = model.getLength(c);
                _leos.writeString(value, length);
            }
        }
    }

    public void close() throws IOException {
        _leos.writeByte(26);
        _leos.flush();
        _leos.close();
    }
}
