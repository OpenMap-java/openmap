/******************************************************************************
 * Copyright (c) 1999, Frank Warmerdam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package com.bbn.openmap.dataAccess.iso8211;

import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.layer.vpf.MutableInt;
import com.bbn.openmap.util.Debug;

/**
 * Contains instance data from one data record (DR). The data is
 * contained as a list of DDFField instances partitioning the raw data
 * into fields. Class contains one DR record from a file. We read into
 * the same record object repeatedly to ensure that repeated leaders
 * can be easily preserved.
 */
public class DDFRecord implements DDFConstants {

    protected DDFModule poModule;
    protected boolean nReuseHeader;
    protected int nFieldOffset; // field data area, not dir entries.
    protected int nDataSize; // Whole record except leader with header
    protected byte[] pachData;

    protected int nFieldCount;
    protected Vector paoFields; // Vector of DDFFields

    protected boolean bIsClone = false;

    public DDFRecord(DDFModule poModuleIn) {
        poModule = poModuleIn;
        nReuseHeader = false;
        nFieldOffset = -1;
        nDataSize = 0;
        pachData = null;
        nFieldCount = 0;
        paoFields = null;
        bIsClone = false;
    }

    /** Get the number of DDFFields on this record. */
    public int getFieldCount() {
        return nFieldCount;
    }

    /** Fetch size of records raw data (GetData()) in bytes. */
    public int getDataSize() {
        return nDataSize;
    }

    /**
     * Fetch the raw data for this record. The returned pointer is
     * effectively to the data for the first field of the record, and
     * is of size GetDataSize().
     */
    public byte[] getData() {
        return pachData;
    }

    /**
     * Fetch the DDFModule with which this record is associated.
     */
    public DDFModule getModule() {
        return poModule;
    }

    /**
     * Write out record contents to debugging file.
     * 
     * A variety of information about this record, and all it's fields
     * and subfields is written to the given debugging file handle.
     * Note that field definition information (ala DDFFieldDefn) isn't
     * written.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("DDFRecord:\n");
        buf.append("    ReuseHeader = ").append(nReuseHeader).append("\n");
        buf.append("    DataSize = ").append(nDataSize).append("\n");

        if (paoFields != null) {
            for (Iterator it = paoFields.iterator(); it.hasNext();) {
                buf.append((DDFField) it.next());
            }
        }
        return buf.toString();
    }

    /**
     * Read a record of data from the file, and parse the header to
     * build a field list for the record (or reuse the existing one if
     * reusing headers). It is expected that the file pointer will be
     * positioned at the beginning of a data record. It is the
     * DDFModule's responsibility to do so.
     * 
     * This method should only be called by the DDFModule class.
     */
    protected boolean read() {
        /* -------------------------------------------------------------------- */
        /* Redefine the record on the basis of the header if needed. */
        /*
         * As a side effect this will read the data for the record as
         * well.
         */
        /* -------------------------------------------------------------------- */
        if (!nReuseHeader) {
            Debug.message("iso8211",
                    "DDFRecord reusing header, calling readHeader()");
            return readHeader();
        }

        /* -------------------------------------------------------------------- */
        /* Otherwise we read just the data and carefully overlay it on */
        /*
         * the previous records data without disturbing the rest of
         * the
         */
        /* record. */
        /* -------------------------------------------------------------------- */

        byte[] tempData = new byte[nDataSize - nFieldOffset];
        int nReadBytes = poModule.read(tempData, 0, tempData.length);
        System.arraycopy(pachData, nFieldOffset, tempData, 0, tempData.length);

        if (nReadBytes != (int) (nDataSize - nFieldOffset) && nReadBytes == -1) {

            return false;
        } else if (nReadBytes != (int) (nDataSize - nFieldOffset)) {
            Debug.error("DDFRecord: Data record is short on DDF file.");
            return false;
        }

        // notdef: eventually we may have to do something at this
        // point to
        // notify the DDFField's that their data values have changed.
        return true;
    }

    /**
     * Clear any information associated with the last header in
     * preparation for reading a new header.
     */
    public void clear() {
        if (paoFields != null) {
            paoFields = null;
        }

        paoFields = null;
        nFieldCount = 0;

        pachData = null;
        nDataSize = 0;
        nReuseHeader = false;
    }

    /**
     * This perform the header reading and parsing job for the read()
     * method. It reads the header, and builds a field list.
     */
    protected boolean readHeader() {

        /* -------------------------------------------------------------------- */
        /* Clear any existing information. */
        /* -------------------------------------------------------------------- */
        clear();

        /* -------------------------------------------------------------------- */
        /* Read the 24 byte leader. */
        /* -------------------------------------------------------------------- */
        byte[] achLeader = new byte[DDF_LEADER_SIZE];

        int nReadBytes = poModule.read(achLeader, 0, DDF_LEADER_SIZE);
        if (nReadBytes == -1) {
            return false;
        } else if (nReadBytes != (int) DDF_LEADER_SIZE) {
            Debug.error("DDFRecord.readHeader(): Leader is short on DDF file.");
            return false;
        }

        /* -------------------------------------------------------------------- */
        /* Extract information from leader. */
        /* -------------------------------------------------------------------- */
        int _recLength, _fieldAreaStart, _sizeFieldLength;
        int _sizeFieldPos, _sizeFieldTag;
        byte _leaderIden;

        try {
            String recLength = new String(achLeader, 0, 5);
            String fieldAreaStart = new String(achLeader, 12, 5);
            _recLength = Integer.valueOf(recLength).intValue();
            _fieldAreaStart = Integer.valueOf(fieldAreaStart).intValue();
        } catch (NumberFormatException nfe) {

            // Turns out, this usually indicates the end of the header
            // information,
            // with "^^^^^^^" being in the file. This is filler.
            if (Debug.debugging("iso8211")) {
                Debug.output("Finished reading headers");
            }
            if (Debug.debugging("iso8211detail")) {
                Debug.error("DDFRecord.readHeader(): " + nfe.getMessage());
                nfe.printStackTrace();
            } else {
                //                 Debug.output("Data record appears to be corrupt on
                // DDF file.\n -- ensure that the files were
                // uncompressed without modifying\n carriage
                // return/linefeeds (by default WINZIP does this).");
            }

            return false;
        }

        _leaderIden = achLeader[6];
        _sizeFieldLength = achLeader[20] - '0';
        _sizeFieldPos = achLeader[21] - '0';
        _sizeFieldTag = achLeader[23] - '0';

        if (_leaderIden == 'R') {
            nReuseHeader = true;
        }

        nFieldOffset = _fieldAreaStart - DDF_LEADER_SIZE;

        if (Debug.debugging("iso8211")) {
            Debug.output("\trecord length [0,5] = " + _recLength);
            Debug.output("\tfield area start [12,5]= " + _fieldAreaStart);
            Debug.output("\tleader id [6] = " + (char) _leaderIden
                    + ", reuse header = " + nReuseHeader);
            Debug.output("\tfield length [20] = " + _sizeFieldLength);
            Debug.output("\tfield position [21] = " + _sizeFieldPos);
            Debug.output("\tfield tag [23] = " + _sizeFieldTag);
        }

        boolean readSubfields = false;

        /* -------------------------------------------------------------------- */
        /* Is there anything seemly screwy about this record? */
        /* -------------------------------------------------------------------- */
        if (_recLength == 0) {
            // Looks like for record lengths of zero, we really want
            // to consult the size of the fields before we try to read
            // in all of the data for this record. Most likely, we
            // don't, and want to access the data later only when we
            // need it.

            nDataSize = _fieldAreaStart - DDF_LEADER_SIZE;
        } else if (_recLength < 24 || _recLength > 100000000
                || _fieldAreaStart < 24 || _fieldAreaStart > 100000) {

            Debug.error("DDFRecord: Data record appears to be corrupt on DDF file.\n -- ensure that the files were uncompressed without modifying\n carriage return/linefeeds (by default WINZIP does this).");
            return false;
        } else {
            /* -------------------------------------------------------------------- */
            /* Read the remainder of the record. */
            /* -------------------------------------------------------------------- */
            nDataSize = _recLength - DDF_LEADER_SIZE;
            readSubfields = true;
        }

        pachData = new byte[nDataSize];

        if (poModule.read(pachData, 0, nDataSize) != nDataSize) {
            Debug.error("DDFRecord: Data record is short on DDF file.");
            return false;
        }

        /* -------------------------------------------------------------------- */
        /*
         * Loop over the directory entries, making a pass counting
         * them.
         */
        /* -------------------------------------------------------------------- */
        int i;
        int nFieldEntryWidth;

        nFieldEntryWidth = _sizeFieldLength + _sizeFieldPos + _sizeFieldTag;
        nFieldCount = 0;
        for (i = 0; i < nDataSize; i += nFieldEntryWidth) {
            if (pachData[i] == DDF_FIELD_TERMINATOR)
                break;

            nFieldCount++;
        }

        /* ==================================================================== */
        /* Allocate, and read field definitions. */
        /* ==================================================================== */
        paoFields = new Vector(nFieldCount);

        for (i = 0; i < nFieldCount; i++) {
            String szTag;
            int nEntryOffset = i * nFieldEntryWidth;
            int nFieldLength, nFieldPos;

            /* -------------------------------------------------------------------- */
            /* Read the position information and tag. */
            /* -------------------------------------------------------------------- */
            szTag = new String(pachData, nEntryOffset, _sizeFieldTag);

            nEntryOffset += _sizeFieldTag;
            nFieldLength = Integer.valueOf(new String(pachData, nEntryOffset, _sizeFieldLength))
                    .intValue();

            nEntryOffset += _sizeFieldLength;
            nFieldPos = Integer.valueOf(new String(pachData, nEntryOffset, _sizeFieldPos))
                    .intValue();

            /* -------------------------------------------------------------------- */
            /* Find the corresponding field in the module directory. */
            /* -------------------------------------------------------------------- */
            DDFFieldDefinition poFieldDefn = poModule.findFieldDefn(szTag);

            if (poFieldDefn == null) {
                Debug.error("DDFRecord: Undefined field " + szTag
                        + " encountered in data record.");
                return false;
            }

            DDFField ddff = null;

            if (readSubfields) {

                /* -------------------------------------------------------------------- */
                /* Assign info the DDFField. */
                /* -------------------------------------------------------------------- */
                byte[] tempData = new byte[nFieldLength];
                System.arraycopy(pachData, _fieldAreaStart + nFieldPos
                        - DDF_LEADER_SIZE, tempData, 0, tempData.length);

                ddff = new DDFField(poFieldDefn, tempData, readSubfields);

            } else {

                // Save the info for reading later directly out of the
                // field.
                ddff = new DDFField(poFieldDefn, nFieldPos, nFieldLength);
                ddff.setHeaderOffset(poModule._recLength + _fieldAreaStart);
            }
            paoFields.add(ddff);
        }

        return true;
    }

    /**
     * Find the named field within this record.
     * 
     * @param pszName The name of the field to fetch. The comparison
     *        is case insensitive.
     * @param iFieldIndex The instance of this field to fetch. Use
     *        zero (the default) for the first instance.
     * 
     * @return Pointer to the requested DDFField. This pointer is to
     *         an internal object, and should not be freed. It remains
     *         valid until the next record read.
     */
    public DDFField findField(String pszName, int iFieldIndex) {
        for (Iterator it = paoFields.iterator(); it.hasNext();) {
            DDFField ddff = (DDFField) it.next();
            if (pszName.equalsIgnoreCase(ddff.getFieldDefn().getName())) {
                if (iFieldIndex == 0) {
                    return ddff;
                } else {
                    iFieldIndex--;
                }
            }
        }
        return null;
    }

    /**
     * Fetch field object based on index.
     * 
     * @param i The index of the field to fetch. Between 0 and
     *        GetFieldCount()-1.
     * 
     * @return A DDFField pointer, or null if the index is out of
     *         range.
     */
    public DDFField getField(int i) {
        try {
            return (DDFField) paoFields.elementAt(i);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }
    }

    /**
     * Get an iterator over the fields.
     */
    public Iterator iterator() {
        if (paoFields != null) {
            return paoFields.iterator();
        }
        return null;
    }

    /**
     * Fetch value of a subfield as an integer. This is a convenience
     * function for fetching a subfield of a field within this record.
     * 
     * @param pszField The name of the field containing the subfield.
     * @param iFieldIndex The instance of this field within the
     *        record. Use zero for the first instance of this field.
     * @param pszSubfield The name of the subfield within the selected
     *        field.
     * @param iSubfieldIndex The instance of this subfield within the
     *        record. Use zero for the first instance.
     * @return The value of the subfield, or zero if it failed for
     *         some reason.
     */
    public int getIntSubfield(String pszField, int iFieldIndex,
                              String pszSubfield, int iSubfieldIndex) {
        DDFField poField;

        /* -------------------------------------------------------------------- */
        /* Fetch the field. If this fails, return zero. */
        /* -------------------------------------------------------------------- */
        poField = findField(pszField, iFieldIndex);
        if (poField == null) {
            return 0;
        }

        /* -------------------------------------------------------------------- */
        /* Get the subfield definition */
        /* -------------------------------------------------------------------- */

        DDFSubfieldDefinition poSFDefn = poField.getFieldDefn()
                .findSubfieldDefn(pszSubfield);

        if (poSFDefn == null) {
            return 0;
        }

        /* -------------------------------------------------------------------- */
        /* Get a pointer to the data. */
        /* -------------------------------------------------------------------- */
        MutableInt nBytesRemaining = new MutableInt();

        byte[] pachData = poField.getSubfieldData(poSFDefn,
                nBytesRemaining,
                iSubfieldIndex);

        /* -------------------------------------------------------------------- */
        /* Return the extracted value. */
        /* -------------------------------------------------------------------- */

        return poSFDefn.extractIntData(pachData, nBytesRemaining.value, null);
    }

    /**
     * Fetch value of a subfield as a float (double). This is a
     * convenience function for fetching a subfield of a field within
     * this record.
     * 
     * @param pszField The name of the field containing the subfield.
     * @param iFieldIndex The instance of this field within the
     *        record. Use zero for the first instance of this field.
     * @param pszSubfield The name of the subfield within the selected
     *        field.
     * @param iSubfieldIndex The instance of this subfield within the
     *        record. Use zero for the first instance.
     * @return The value of the subfield, or zero if it failed for
     *         some reason.
     */
    public double getFloatSubfield(String pszField, int iFieldIndex,
                                   String pszSubfield, int iSubfieldIndex) {
        DDFField poField;

        /* -------------------------------------------------------------------- */
        /* Fetch the field. If this fails, return zero. */
        /* -------------------------------------------------------------------- */
        poField = findField(pszField, iFieldIndex);
        if (poField == null) {
            return 0;
        }

        /* -------------------------------------------------------------------- */
        /* Get the subfield definition */
        /* -------------------------------------------------------------------- */
        DDFSubfieldDefinition poSFDefn = poField.getFieldDefn()
                .findSubfieldDefn(pszSubfield);

        if (poSFDefn == null) {
            return 0;
        }

        /* -------------------------------------------------------------------- */
        /* Get a pointer to the data. */
        /* -------------------------------------------------------------------- */
        MutableInt nBytesRemaining = new MutableInt();

        byte[] pachData = poField.getSubfieldData(poSFDefn,
                nBytesRemaining,
                iSubfieldIndex);

        /* -------------------------------------------------------------------- */
        /* Return the extracted value. */
        /* -------------------------------------------------------------------- */
        return poSFDefn.extractFloatData(pachData, nBytesRemaining.value, null);
    }

    /**
     * Fetch value of a subfield as a string. This is a convenience
     * function for fetching a subfield of a field within this record.
     * 
     * @param pszField The name of the field containing the subfield.
     * @param iFieldIndex The instance of this field within the
     *        record. Use zero for the first instance of this field.
     * @param pszSubfield The name of the subfield within the selected
     *        field.
     * @param iSubfieldIndex The instance of this subfield within the
     *        record. Use zero for the first instance.
     * @return The value of the subfield, or null if it failed for
     *         some reason. The returned pointer is to internal data
     *         and should not be modified or freed by the application.
     */

    String getStringSubfield(String pszField, int iFieldIndex,
                             String pszSubfield, int iSubfieldIndex) {

        DDFField poField;

        /* -------------------------------------------------------------------- */
        /* Fetch the field. If this fails, return zero. */
        /* -------------------------------------------------------------------- */
        poField = findField(pszField, iFieldIndex);
        if (poField == null) {
            return null;
        }

        /* -------------------------------------------------------------------- */
        /* Get the subfield definition */
        /* -------------------------------------------------------------------- */
        DDFSubfieldDefinition poSFDefn = poField.getFieldDefn()
                .findSubfieldDefn(pszSubfield);

        if (poSFDefn == null) {
            return null;
        }

        /* -------------------------------------------------------------------- */
        /* Get a pointer to the data. */
        /* -------------------------------------------------------------------- */
        MutableInt nBytesRemaining = new MutableInt();

        byte[] pachData = poField.getSubfieldData(poSFDefn,
                nBytesRemaining,
                iSubfieldIndex);

        /* -------------------------------------------------------------------- */
        /* Return the extracted value. */
        /* -------------------------------------------------------------------- */

        return poSFDefn.extractStringData(pachData, nBytesRemaining.value, null);
    }

}