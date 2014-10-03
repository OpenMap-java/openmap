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

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.util.Debug;

/**
 * The class that represents a ISO 8211 file.
 */
public class DDFModule implements DDFConstants {

    protected BinaryFile fpDDF;
    protected String fileName;
    protected long nFirstRecordOffset;

    protected byte _interchangeLevel;
    protected byte _inlineCodeExtensionIndicator;
    protected byte _versionNumber;
    protected byte _appIndicator;
    protected int _fieldControlLength;
    protected String _extendedCharSet; // 4 characters

    protected int _recLength;
    protected byte _leaderIden;
    protected int _fieldAreaStart;
    protected int _sizeFieldLength;
    protected int _sizeFieldPos;
    protected int _sizeFieldTag;

    protected Vector paoFieldDefns; //DDFFieldDefinitions
    protected DDFRecord poRecord;

    /**
     * The constructor. Need to call open() if this constructor is
     * used.
     */
    public DDFModule() {
        paoFieldDefns = null;
        poRecord = null;
        fpDDF = null;
    }

    public DDFModule(String ddfName) throws IOException {
        open(ddfName);
    }

    /**
     * Close an ISO 8211 file. Just close the file pointer to the
     * file.
     */
    public void close() {

        if (fpDDF != null) {
            try {
                fpDDF.close();
            } catch (IOException ioe) {
                Debug.error("DDFModule IOException when closing DDFModule file");
            }
            fpDDF = null;
        }
    }

    /**
     * Clean up, get rid of data and close file pointer.
     */
    public void destroy() {
        close();

        // Cleanup the working record.
        poRecord = null;
        // Cleanup the field definitions.
        paoFieldDefns = null;
    }

    /**
     * Open a ISO 8211 (DDF) file for reading, and read the DDR record
     * to build the field definitions.
     * 
     * If the open succeeds the data descriptive record (DDR) will
     * have been read, and all the field and subfield definitions will
     * be available.
     * 
     * @param pszFilename The name of the file to open.
     */
    public BinaryFile open(String pszFilename) throws IOException {

        fileName = pszFilename;

        fpDDF = new BinaryBufferedFile(pszFilename);

        // Read the 24 byte leader.
        byte[] achLeader = new byte[DDF_LEADER_SIZE];

        if (fpDDF.read(achLeader) != DDF_LEADER_SIZE) {
            destroy();
            if (Debug.debugging("iso8211")) {
                Debug.output("DDFModule: Leader is short on DDF file "
                        + pszFilename);
            }
            return null;
        }

        // Verify that this appears to be a valid DDF file.
        int i;
        boolean bValid = true;

        for (i = 0; i < (int) DDF_LEADER_SIZE; i++) {
            if (achLeader[i] < 32 || achLeader[i] > 126) {
                bValid = false;
            }
        }

        if (achLeader[5] != '1' && achLeader[5] != '2' && achLeader[5] != '3') {
            bValid = false;
        }

        if (achLeader[6] != 'L') {
            bValid = false;
        }

        if (achLeader[8] != '1' && achLeader[8] != ' ') {
            bValid = false;
        }

        // Extract information from leader.
        if (bValid) {
            _recLength = Integer.parseInt(new String(achLeader, 0, 5));
            _interchangeLevel = achLeader[5];
            _leaderIden = achLeader[6];
            _inlineCodeExtensionIndicator = achLeader[7];
            _versionNumber = achLeader[8];
            _appIndicator = achLeader[9];
            _fieldControlLength = Integer.parseInt(new String(achLeader, 10, 2));
            _fieldAreaStart = Integer.parseInt(new String(achLeader, 12, 5));
            _extendedCharSet = new String((char) achLeader[17] + ""
                    + (char) achLeader[18] + "" + (char) achLeader[19]);
            _sizeFieldLength = Integer.parseInt(new String(achLeader, 20, 1));
            _sizeFieldPos = Integer.parseInt(new String(achLeader, 21, 1));
            _sizeFieldTag = Integer.parseInt(new String(achLeader, 23, 1));

            if (_recLength < 12 || _fieldControlLength == 0
                    || _fieldAreaStart < 24 || _sizeFieldLength == 0
                    || _sizeFieldPos == 0 || _sizeFieldTag == 0) {
                bValid = false;
            }

            if (Debug.debugging("iso8211")) {
                Debug.output("bValid = " + bValid + ", from "
                        + new String(achLeader));
                Debug.output(toString());
            }
        }

        // If the header is invalid, then clean up, report the error
        // and return.
        if (!bValid) {
            destroy();

            if (Debug.debugging("iso8211")) {
                Debug.error("DDFModule: File " + pszFilename
                        + " does not appear to have a valid ISO 8211 header.");
            }
            return null;
        }

        if (Debug.debugging("iso8211")) {
            Debug.output("DDFModule:  header parsed successfully");

        }

        /* -------------------------------------------------------------------- */
        /* Read the whole record into memory. */
        /* -------------------------------------------------------------------- */
        byte[] pachRecord = new byte[_recLength];

        System.arraycopy(achLeader, 0, pachRecord, 0, achLeader.length);
        int numNewRead = pachRecord.length - achLeader.length;

        if (fpDDF.read(pachRecord, achLeader.length, numNewRead) != numNewRead) {
            if (Debug.debugging("iso8211")) {
                Debug.error("DDFModule: Header record is short on DDF file "
                        + pszFilename);
            }

            return null;
        }

        /* First make a pass counting the directory entries. */
        int nFieldEntryWidth = _sizeFieldLength + _sizeFieldPos + _sizeFieldTag;

        int nFieldDefnCount = 0;
        for (i = DDF_LEADER_SIZE; i < _recLength; i += nFieldEntryWidth) {
            if (pachRecord[i] == DDF_FIELD_TERMINATOR)
                break;

            nFieldDefnCount++;
        }

        /* Allocate, and read field definitions. */
        paoFieldDefns = new Vector();

        for (i = 0; i < nFieldDefnCount; i++) {
            if (Debug.debugging("iso8211")) {
                Debug.output("DDFModule.open: Reading field " + i);
            }

            byte[] szTag = new byte[128];
            int nEntryOffset = DDF_LEADER_SIZE + i * nFieldEntryWidth;
            int nFieldLength, nFieldPos;

            System.arraycopy(pachRecord, nEntryOffset, szTag, 0, _sizeFieldTag);

            nEntryOffset += _sizeFieldTag;
            nFieldLength = Integer.parseInt(new String(pachRecord, nEntryOffset, _sizeFieldLength));

            nEntryOffset += _sizeFieldLength;
            nFieldPos = Integer.parseInt(new String(pachRecord, nEntryOffset, _sizeFieldPos));

            byte[] subPachRecord = new byte[nFieldLength];
            System.arraycopy(pachRecord,
                    _fieldAreaStart + nFieldPos,
                    subPachRecord,
                    0,
                    nFieldLength);

            paoFieldDefns.add(new DDFFieldDefinition(this, new String(szTag, 0, _sizeFieldTag), subPachRecord));
        }

        // Free the memory...
        achLeader = null;
        pachRecord = null;

        // Record the current file offset, the beginning of the first
        // data record.
        nFirstRecordOffset = fpDDF.getFilePointer();

        return fpDDF;
    }

    /**
     * Write out module info to debugging file.
     * 
     * A variety of information about the module is written to the
     * debugging file. This includes all the field and subfield
     * definitions read from the header.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("DDFModule:\n");
        buf.append("    _recLength = ").append(_recLength).append("\n");
        buf.append("    _interchangeLevel = ").append(_interchangeLevel).append("\n");
        buf.append("    _leaderIden = ").append((char) _leaderIden).append("\n");
        buf.append("    _inlineCodeExtensionIndicator = ")
               .append(_inlineCodeExtensionIndicator).append("\n");
        buf.append("    _versionNumber = ").append(_versionNumber).append("\n");
        buf.append("    _appIndicator = ").append(_appIndicator).append("\n");
        buf.append("    _extendedCharSet = ").append(_extendedCharSet).append("\n");
        buf.append("    _fieldControlLength = ").append(_fieldControlLength).append("\n");
        buf.append("    _fieldAreaStart = ").append(_fieldAreaStart).append("\n");
        buf.append("    _sizeFieldLength = ").append(_sizeFieldLength).append("\n");
        buf.append("    _sizeFieldPos = ").append(_sizeFieldPos).append("\n");
        buf.append("    _sizeFieldTag = ").append(_sizeFieldTag).append("\n");
        return buf.toString();
    }

    public String dump() {
        StringBuffer buf = new StringBuffer(toString());

        DDFRecord poRecord;
        int iRecord = 0;
        while ((poRecord = readRecord()) != null) {
            buf.append("  Record ").append((iRecord++)).append("(")
                    .append(poRecord.getDataSize()).append(" bytes)\n");

            for (Iterator it = poRecord.iterator(); it.hasNext(); buf.append(((DDFField) it.next()).toString())) {
            }
        }
        return buf.toString();
    }

    /**
     * Fetch the definition of the named field.
     * 
     * This function will scan the DDFFieldDefn's on this module, to
     * find one with the indicated field name.
     * 
     * @param pszFieldName The name of the field to search for. The
     *        comparison is case insensitive.
     * 
     * @return A pointer to the request DDFFieldDefn object is
     *         returned, or null if none matching the name are found.
     *         The return object remains owned by the DDFModule, and
     *         should not be deleted by application code.
     */
    public DDFFieldDefinition findFieldDefn(String pszFieldName) {

        for (Iterator it = paoFieldDefns.iterator(); it.hasNext();) {
            DDFFieldDefinition ddffd = (DDFFieldDefinition) it.next();
            String pszThisName = ddffd.getName();

            if (Debug.debugging("iso8211detail")) {
                Debug.output("DDFModule.findFieldDefn(" + pszFieldName + ":"
                        + pszFieldName.length() + ") checking against ["
                        + pszThisName + ":" + pszThisName.length() + "]");
            }

            if (pszFieldName.equalsIgnoreCase(pszThisName)) {
                return ddffd;
            }
        }

        return null;
    }

    /**
     * Read one record from the file, and return to the application.
     * The returned record is owned by the module, and is reused from
     * call to call in order to preserve headers when they aren't
     * being re-read from record to record.
     * 
     * @return A pointer to a DDFRecord object is returned, or null if
     *         a read error, or end of file occurs. The returned
     *         record is owned by the module, and should not be
     *         deleted by the application. The record is only valid
     *         until the next ReadRecord() at which point it is
     *         overwritten.
     */
    public DDFRecord readRecord() {
        if (poRecord == null) {
            poRecord = new DDFRecord(this);
        }

        if (poRecord.read()) {
            return poRecord;
        } else {
            return null;
        }
    }

    /**
     * Method for other components to call to get the DDFModule to
     * read bytes into the provided array.
     * 
     * @param toData the bytes to put data into.
     * @param offset the byte offset to start reading from, whereever
     *        the pointer currently is.
     * @param length the number of bytes to read.
     * @return the number of bytes read.
     */
    public int read(byte[] toData, int offset, int length) {
        if (fpDDF == null) {
            reopen();
        }

        if (fpDDF != null) {
            try {
                return fpDDF.read(toData, offset, length);
            } catch (IOException ioe) {
                Debug.error("DDFModule.read(): IOException caught");
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                Debug.error("DDFModule.read(): "
                        + aioobe.getMessage()
                        + " reading from "
                        + offset
                        + " to "
                        + length
                        + " into "
                        + (toData == null ? "null byte[]" : "byte["
                                + toData.length + "]"));
                aioobe.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Convenience method to read a byte from the data file. Assumes
     * that you know what you are doing based on the parameters read
     * in the data file. For DDFFields that haven't loaded their
     * subfields.
     */
    public int read() {
        if (fpDDF == null) {
            reopen();
        }

        if (fpDDF != null) {
            try {
                return fpDDF.read();
            } catch (IOException ioe) {
                Debug.error("DDFModule.read(): IOException caught");
            }
        }
        return 0;
    }

    /**
     * Convenience method to seek to a location in the data file.
     * Assumes that you know what you are doing based on the
     * parameters read in the data file. For DDFFields that haven't
     * loaded their subfields.
     * 
     * @param pos the byte position to reposition the file pointer to.
     */
    public void seek(long pos) throws IOException {
        if (fpDDF == null) {
            reopen();
        }

        if (fpDDF != null) {
            fpDDF.seek(pos);
        } else {
            throw new IOException("DDFModule doesn't have a pointer to a file");
        }
    }

    /**
     * Fetch a field definition by index.
     * 
     * @param i (from 0 to GetFieldCount() - 1.
     * @return the returned field pointer or null if the index is out
     *         of range.
     */
    public DDFFieldDefinition getField(int i) {
        if (i >= 0 || i < paoFieldDefns.size()) {
            return (DDFFieldDefinition) paoFieldDefns.elementAt(i);
        }

        return null;
    }

    /**
     * Return to first record.
     * 
     * The next call to ReadRecord() will read the first data record
     * in the file.
     * 
     * @param nOffset the offset in the file to return to. By default
     *        this is -1, a special value indicating that reading
     *        should return to the first data record. Otherwise it is
     *        an absolute byte offset in the file.
     */
    public void rewind(long nOffset) throws IOException {
        if (nOffset == -1) {
            nOffset = nFirstRecordOffset;
        }

        if (fpDDF != null) {
            fpDDF.seek(nOffset);

            // Don't know what this has to do with anything...
            if (nOffset == nFirstRecordOffset && poRecord != null) {
                poRecord.clear();
            }
        }

    }

    public void reopen() {
        try {
            if (fpDDF == null) {
                fpDDF = new BinaryBufferedFile(fileName);
            }
        } catch (IOException ioe) {

        }
    }
}