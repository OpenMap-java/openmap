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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.bbn.openmap.layer.vpf.MutableInt;
import com.bbn.openmap.util.Debug;

/**
 * This object represents one field in a DDFRecord. This models an
 * instance of the fields data, rather than it's data definition which
 * is handled by the DDFFieldDefn class. Note that a DDFField doesn't
 * have DDFSubfield children as you would expect. To extract subfield
 * values use GetSubfieldData() to find the right data pointer and
 * then use ExtractIntData(), ExtractFloatData() or
 * ExtractStringData().
 */
public class DDFField {

    protected DDFFieldDefinition poDefn;
    protected byte[] pachData;
    protected Hashtable subfields;
    protected int dataPosition;
    protected int dataLength;
    protected int headerOffset;

    public DDFField() {}

    public DDFField(DDFFieldDefinition poDefnIn, int dataPositionIn,
            int dataLengthIn) {
        initialize(poDefnIn, null);
        dataPosition = dataPositionIn;
        dataLength = dataLengthIn;
    }

    public DDFField(DDFFieldDefinition poDefnIn, byte[] pachDataIn) {
        this(poDefnIn, pachDataIn, true);
    }

    public DDFField(DDFFieldDefinition poDefnIn, byte[] pachDataIn,
            boolean doSubfields) {
        initialize(poDefnIn, pachDataIn);
        if (doSubfields) {
            buildSubfields();
        }
    }

    public void initialize(DDFFieldDefinition poDefnIn, byte[] pachDataIn) {
        pachData = pachDataIn;
        poDefn = poDefnIn;
        subfields = new Hashtable();
    }

    /**
     * Set how many bytes to add to the data position for absolute
     * position in the data file for the field data.
     */
    protected void setHeaderOffset(int headerOffsetIn) {
        headerOffset = headerOffsetIn;
    }

    /**
     * Get how many bytes to add to the data position for absolute
     * position in the data file for the field data.
     */
    public int getHeaderOffset() {
        return headerOffset;
    }

    /**
     * Return the pointer to the entire data block for this record.
     * This is an internal copy, and shouldn't be freed by the
     * application. If null, then check the dataPosition and
     * daataLength for byte offsets for the data in the file, and go
     * get it yourself. This is done for really large files where it
     * doesn't make sense to load the data.
     */
    public byte[] getData() {
        return pachData;
    }

    /**
     * Return the number of bytes in the data block returned by
     * GetData().
     */
    public int getDataSize() {
        if (pachData != null) {
            return pachData.length;
        } else
            return 0;
    }

    /** Fetch the corresponding DDFFieldDefn. */
    public DDFFieldDefinition getFieldDefn() {
        return poDefn;
    }

    /**
     * If getData() returns null, it'll be your responsibilty to go
     * after the data you need for this field.
     * 
     * @return the byte offset into the source file to start reading
     *         this field.
     */
    public int getDataPosition() {
        return dataPosition;
    }

    /**
     * If getData() returns null, it'll be your responsibilty to go
     * after the data you need for this field.
     * 
     * @return the number of bytes contained in the source file for
     *         this field.
     */
    public int getDataLength() {
        return dataLength;
    }

    /**
     * Creates a string with variety of information about this field,
     * and all it's subfields is written to the given debugging file
     * handle. Note that field definition information (ala
     * DDFFieldDefn) isn't written.
     * 
     * @return String containing info.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("  DDFField:\n");
        buf.append("\tTag = ").append(poDefn.getName()).append("\n");
        buf.append("\tDescription = ").append(poDefn.getDescription()).append("\n");
        int size = getDataSize();
        buf.append("\tDataSize = ").append(size).append("\n");

        if (pachData == null) {
            buf.append("\tHeader offset = ").append(headerOffset).append("\n");
            buf.append("\tData position = ").append(dataPosition).append("\n");
            buf.append("\tData length = ").append(dataLength).append("\n");
            return buf.toString();
        }

        buf.append("\tData = ");
        for (int i = 0; i < Math.min(size, 40); i++) {
            if (pachData[i] < 32 || pachData[i] > 126) {
                buf.append(" | ").append((char) pachData[i]);
            } else {
                buf.append(pachData[i]);
            }
        }

        if (size > 40)
            buf.append("...");
        buf.append("\n");

        /* -------------------------------------------------------------------- */
        /* dump the data of the subfields. */
        /* -------------------------------------------------------------------- */
        if (Debug.debugging("iso8211.raw")) {
            int iOffset = 0;
            MutableInt nBytesConsumed = new MutableInt(0);

            for (int nLoopCount = 0; nLoopCount < getRepeatCount(); nLoopCount++) {
                if (nLoopCount > 8) {
                    buf.append("      ...\n");
                    break;
                }

                for (int i = 0; i < poDefn.getSubfieldCount(); i++) {
                    byte[] subPachData = new byte[pachData.length - iOffset];
                    System.arraycopy(pachData,
                            iOffset,
                            subPachData,
                            0,
                            subPachData.length);

                    buf.append(poDefn.getSubfieldDefn(i).dumpData(subPachData,
                            subPachData.length));

                    poDefn.getSubfieldDefn(i).getDataLength(subPachData,
                            subPachData.length,
                            nBytesConsumed);
                    iOffset += nBytesConsumed.value;
                }
            }
        } else {
            buf.append("      Subfields:\n");

            for (Enumeration enumeration = subfields.keys(); enumeration.hasMoreElements();) {
                Object obj = subfields.get(enumeration.nextElement());

                if (obj instanceof List) {
                    for (Iterator it = ((List) obj).iterator(); it.hasNext();) {
                        DDFSubfield ddfs = (DDFSubfield) it.next();
                        buf.append("        ").append(ddfs.toString()).append("\n");
                    }
                } else {
                    buf.append("        ").append(obj.toString()).append("\n");
                }
            }
        }

        return buf.toString();
    }

    /**
     * Will return an ordered list of DDFSubfield objects. If the
     * subfield wasn't repeated, it will provide a list containing one
     * object. Will return null if the subfield doesn't exist.
     */
    public List getSubfields(String subfieldName) {
        Object obj = subfields.get(subfieldName);
        if (obj instanceof List) {
            return (List) obj;
        } else if (obj != null) {
            LinkedList ll = new LinkedList();
            ll.add(obj);
            return ll;
        }

        return null;
    }

    /**
     * Will return a DDFSubfield object with the given name, or the
     * first one off the list for a repeating subfield. Will return
     * null if the subfield doesn't exist.
     */
    public DDFSubfield getSubfield(String subfieldName) {
        Object obj = subfields.get(subfieldName);
        if (obj instanceof List) {
            List l = (List) obj;
            if (!l.isEmpty()) {
                return (DDFSubfield) (l.get(0));
            }
            obj = null;
        }

        // May be null if subfield list above is empty. Not sure if
        // that's possible.
        return (DDFSubfield) obj;
    }

    /**
     * Fetch raw data pointer for a particular subfield of this field.
     * 
     * The passed DDFSubfieldDefn (poSFDefn) should be acquired from
     * the DDFFieldDefn corresponding with this field. This is
     * normally done once before reading any records. This method
     * involves a series of calls to DDFSubfield::GetDataLength() in
     * order to track through the DDFField data to that belonging to
     * the requested subfield. This can be relatively expensive.
     * <p>
     * 
     * @param poSFDefn The definition of the subfield for which the
     *        raw data pointer is desired.
     * @param pnMaxBytes The maximum number of bytes that can be
     *        accessed from the returned data pointer is placed in
     *        this int, unless it is null.
     * @param iSubfieldIndex The instance of this subfield to fetch.
     *        Use zero (the default) for the first instance.
     * 
     * @return A pointer into the DDFField's data that belongs to the
     *         subfield. This returned pointer is invalidated by the
     *         next record read (DDFRecord::ReadRecord()) and the
     *         returned pointer should not be freed by the
     *         application.
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
                DDFSubfieldDefinition poThisSFDefn = poDefn.getSubfieldDefn(iSF);

                byte[] subPachData = new byte[pachData.length - iOffset];
                System.arraycopy(pachData,
                        iOffset,
                        subPachData,
                        0,
                        subPachData.length);

                if (poThisSFDefn == poSFDefn && iSubfieldIndex == 0) {

                    if (pnMaxBytes != null) {
                        pnMaxBytes.value = pachData.length - iOffset;
                    }

                    return subPachData;
                }

                poThisSFDefn.getDataLength(subPachData,
                        subPachData.length,
                        nBytesConsumed);

                iOffset += nBytesConsumed.value;
            }

            iSubfieldIndex--;
        }

        // We didn't find our target subfield or instance!
        return null;
    }

    public void buildSubfields() {
        byte[] pachFieldData = pachData;
        int nBytesRemaining = pachData.length;

        for (int iRepeat = 0; iRepeat < getRepeatCount(); iRepeat++) {

            /* -------------------------------------------------------- */
            /* Loop over all the subfields of this field, advancing */
            /* the data pointer as we consume data. */
            /* -------------------------------------------------------- */
            for (int iSF = 0; iSF < poDefn.getSubfieldCount(); iSF++) {

                DDFSubfield ddfs = new DDFSubfield(poDefn.getSubfieldDefn(iSF), pachFieldData, nBytesRemaining);

                addSubfield(ddfs);

                // Reset data for next subfield;
                int nBytesConsumed = ddfs.getByteSize();
                nBytesRemaining -= nBytesConsumed;
                byte[] tempData = new byte[pachFieldData.length
                        - nBytesConsumed];
                System.arraycopy(pachFieldData,
                        nBytesConsumed,
                        tempData,
                        0,
                        tempData.length);
                pachFieldData = tempData;
            }
        }

    }

    protected void addSubfield(DDFSubfield ddfs) {
        if (Debug.debugging("iso8211")) {
            Debug.output("DDFField(" + getFieldDefn().getName()
                    + ").addSubfield(" + ddfs + ")");
        }

        String sfName = ddfs.getDefn().getName().trim().intern();
        Object sf = subfields.get(sfName);
        if (sf == null) {
            subfields.put(sfName, ddfs);
        } else {
            if (sf instanceof List) {
                ((List) sf).add(ddfs);
            } else {
                Vector subList = new Vector();
                subList.add(sf);
                subList.add(ddfs);
                subfields.put(sfName, subList);
            }
        }
    }

    /**
     * How many times do the subfields of this record repeat? This
     * will always be one for non-repeating fields.
     * 
     * @return The number of times that the subfields of this record
     *         occur in this record. This will be one for
     *         non-repeating fields.
     */
    public int getRepeatCount() {
        if (!poDefn.isRepeating()) {
            return 1;
        }

        /* -------------------------------------------------------------------- */
        /* The occurrence count depends on how many copies of this */
        /* field's list of subfields can fit into the data space. */
        /* -------------------------------------------------------------------- */
        if (poDefn.getFixedWidth() != 0) {
            return pachData.length / poDefn.getFixedWidth();
        }

        /* -------------------------------------------------------------------- */
        /* Note that it may be legal to have repeating variable width */
        /* subfields, but I don't have any samples, so I ignore it for */
        /* now. */
        /*                                                                      */
        /*
         * The file data/cape_royal_AZ_DEM/1183XREF.DDF has a
         * repeating
         */
        /* variable length field, but the count is one, so it isn't */
        /* much value for testing. */
        /* -------------------------------------------------------------------- */
        int iOffset = 0;
        int iRepeatCount = 1;
        MutableInt nBytesConsumed = new MutableInt(0);

        while (true) {
            for (int iSF = 0; iSF < poDefn.getSubfieldCount(); iSF++) {
                DDFSubfieldDefinition poThisSFDefn = poDefn.getSubfieldDefn(iSF);

                if (poThisSFDefn.getWidth() > pachData.length - iOffset) {
                    nBytesConsumed.value = poThisSFDefn.getWidth();
                } else {
                    byte[] tempData = new byte[pachData.length - iOffset];
                    System.arraycopy(pachData,
                            iOffset,
                            tempData,
                            0,
                            tempData.length);
                    poThisSFDefn.getDataLength(tempData,
                            tempData.length,
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

