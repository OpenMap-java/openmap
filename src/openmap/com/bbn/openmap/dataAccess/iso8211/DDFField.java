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

import com.bbn.openmap.layer.vpf.MutableInt;

/**
 * This object represents one field in a DDFRecord.  This
 * models an instance of the fields data, rather than it's data definition
 * which is handled by the DDFFieldDefn class.  Note that a DDFField
 * doesn't have DDFSubfield children as you would expect.  To extract
 * subfield values use GetSubfieldData() to find the right data pointer and
 * then use ExtractIntData(), ExtractFloatData() or ExtractStringData().
 */
public class DDFField {

    /**
     * Return the pointer to the entire data block for this record. This
     * is an internal copy, and shouldn't be freed by the application.
     */
    public byte[] getData() { 
        return pachData; 
    }

    /** Return the number of bytes in the data block returned by GetData(). */
    public int getDataSize() { 
        return pachData.length; 
    }

    /** Fetch the corresponding DDFFieldDefn. */
    public DDFFieldDefinition getFieldDefn() { 
        return poDefn; 
    }
    
    protected DDFFieldDefinition poDefn;
    protected byte[] pachData;


    public DDFField() {}

    public DDFField(DDFFieldDefinition poDefnIn, byte[] pachDataIn) {
        initialize(poDefnIn, pachDataIn);
    }

    // Note, we implement no constructor for this class to make instantiation
    // cheaper.  It is required that the Initialize() be called before anything
    // else.

    public void initialize(DDFFieldDefinition poDefnIn, byte[] pachDataIn) {
        pachData = pachDataIn;
        poDefn = poDefnIn;
    }

    /**
     * Creates a string with variety of information about this field,
     * and all it's subfields is written to the given debugging file
     * handle.  Note that field definition information (ala
     * DDFFieldDefn) isn't written.
     *
     * @return String containing info.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("  DDFField:\n");
        buf.append("      Tag = " + poDefn.getName() + "\n");
        int size = getDataSize();
        buf.append("      DataSize = " + size + "\n");

        buf.append("      Data = `");
        for (int i = 0; i < Math.min(size, 40); i++) {
            if (pachData[i] < 32 || pachData[i] > 126) {
                buf.append("\\" + (char)pachData[i]);
            } else {
                buf.append(pachData[i]);
            }
        }

        if (size > 40) buf.append("...");
        buf.append("\n");

        /* -------------------------------------------------------------------- */
        /*      dump the data of the subfields.                                 */
        /* -------------------------------------------------------------------- */
        int iOffset = 0;
        MutableInt nBytesConsumed = new MutableInt(0);

        for (int nLoopCount = 0; nLoopCount < getRepeatCount(); nLoopCount++) {
            if (nLoopCount > 8) {
                buf.append("      ...\n");
                break;
            }
        
            for (int i = 0; i < poDefn.getSubfieldCount(); i++) {
                byte[] subPachData = new byte[pachData.length - iOffset];
                System.arraycopy(pachData, iOffset, subPachData, 0, subPachData.length);

                buf.append(poDefn.getSubfield(i).dumpData(subPachData, subPachData.length));
                
                poDefn.getSubfield(i).getDataLength(subPachData, subPachData.length, 
                                                    nBytesConsumed);
                iOffset += nBytesConsumed.value;
            }
        }

        return buf.toString();
    }

    /**
     * Fetch raw data pointer for a particular subfield of this field.
     *
     * The passed DDFSubfieldDefn (poSFDefn) should be acquired from the
     * DDFFieldDefn corresponding with this field.  This is normally done
     * once before reading any records.  This method involves a series of
     * calls to DDFSubfield::GetDataLength() in order to track through the
     * DDFField data to that belonging to the requested subfield.  This can
     * be relatively expensive.<p>
     *
     * @param poSFDefn The definition of the subfield for which the raw
     * data pointer is desired.
     * @param pnMaxBytes The maximum number of bytes that can be accessed from
     * the returned data pointer is placed in this int, unless it is null.
     * @param iSubfieldIndex The instance of this subfield to fetch.  Use zero
     * (the default) for the first instance.
     *
     * @return A pointer into the DDFField's data that belongs to the subfield.
     * This returned pointer is invalidated by the next record read
     * (DDFRecord::ReadRecord()) and the returned pointer should not be freed
     * by the application.
     */
    public byte[] getSubfieldData(DDFSubfieldDefinition poSFDefn,
                                  MutableInt pnMaxBytes, int iSubfieldIndex) {
        int iOffset = 0;
    
        if (poSFDefn == null)
            return null;

        if (iSubfieldIndex > 0 && poDefn.getFixedWidth() > 0) {
            iOffset = poDefn.getFixedWidth() * iSubfieldIndex;
            iSubfieldIndex = 0;
        }

        MutableInt nBytesConsumed = new MutableInt(0);
        while (iSubfieldIndex >= 0) {
            for (int iSF = 0; iSF < poDefn.getSubfieldCount(); iSF++) {
                DDFSubfieldDefinition poThisSFDefn = poDefn.getSubfield(iSF);
            
                byte[] subPachData = new byte[pachData.length - iOffset];
                System.arraycopy(pachData, iOffset, subPachData, 0, subPachData.length);

                if (poThisSFDefn == poSFDefn && iSubfieldIndex == 0) {

                    if (pnMaxBytes != null) {
                        pnMaxBytes.value = pachData.length - iOffset;
                    }
                    
                    return subPachData;
                }
            
                poThisSFDefn.getDataLength(subPachData, subPachData.length, 
                                           nBytesConsumed);

                iOffset += nBytesConsumed.value;
            }
            
            iSubfieldIndex--;
        }

        // We didn't find our target subfield or instance!
        return null;
    }

    /**
     * How many times do the subfields of this record repeat?  This    
     * will always be one for non-repeating fields.
     *
     * @return The number of times that the subfields of this record occur
     * in this record.  This will be one for non-repeating fields.
     *
     * @see <a href="example.html">8211view example program</a>
     * for demonstation of handling repeated fields properly.
     */
    public int getRepeatCount() {
        if (!poDefn.isRepeating())
            return 1;

        /* -------------------------------------------------------------------- */
        /*      The occurance count depends on how many copies of this          */
        /*      field's list of subfields can fit into the data space.          */
        /* -------------------------------------------------------------------- */
        if (poDefn.getFixedWidth() != 0) {
            return pachData.length / poDefn.getFixedWidth();
        }

        /* -------------------------------------------------------------------- */
        /*      Note that it may be legal to have repeating variable width      */
        /*      subfields, but I don't have any samples, so I ignore it for     */
        /*      now.                                                            */
        /*                                                                      */
        /*      The file data/cape_royal_AZ_DEM/1183XREF.DDF has a repeating    */
        /*      variable length field, but the count is one, so it isn't        */
        /*      much value for testing.                                         */
        /* -------------------------------------------------------------------- */
        int iOffset = 0;
        int iRepeatCount = 1;
        MutableInt nBytesConsumed = new MutableInt(0);

        while (true) {
            for (int iSF = 0; iSF < poDefn.getSubfieldCount(); iSF++) {
                DDFSubfieldDefinition poThisSFDefn = poDefn.getSubfield(iSF);
                
                if (poThisSFDefn.getWidth() > pachData.length - iOffset) {
                    nBytesConsumed.value = poThisSFDefn.getWidth();
                } else {
                    byte[] tempData = new byte[pachData.length - iOffset];
                    System.arraycopy(pachData, iOffset, tempData, 0, tempData.length);
                    poThisSFDefn.getDataLength(tempData, tempData.length,
                                               nBytesConsumed);
                }

                iOffset += nBytesConsumed.value;
                if (iOffset > pachData.length) {
                    return iRepeatCount - 1;
                }
            }

            if (iOffset > pachData.length - 2)
                return iRepeatCount;

            iRepeatCount++;
        }
    }
}



