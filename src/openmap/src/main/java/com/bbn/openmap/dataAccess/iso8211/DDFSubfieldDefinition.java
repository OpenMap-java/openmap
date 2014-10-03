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

import com.bbn.openmap.MoreMath;
import com.bbn.openmap.layer.vpf.MutableInt;
import com.bbn.openmap.util.Debug;

/**
 * Information from the DDR record describing one subfield of a DDFFieldDefn.
 * All subfields of a field will occur in each occurrence of that field (as a
 * DDFField) in a DDFRecord. Subfield's actually contain formatted data (as
 * instances within a record).
 * 
 * @author Guillaume Pelletier provided fix for Big Endian support (important
 *         for S-57)
 */
public class DDFSubfieldDefinition implements DDFConstants {

    protected String pszName; // a.k.a. subfield mnemonic
    protected String pszFormatString;
    protected DDFDataType eType;
    protected int eBinaryFormat;

    /**
     * bIsVariable determines whether we using the chFormatDelimeter (true), or
     * the fixed width (false).
     */
    protected boolean bIsVariable;

    protected char chFormatDelimeter;
    protected int nFormatWidth;

    public interface DDFBinaryFormat {
        public final static int NotBinary = 0;
        public final static int UInt = 1;
        public final static int SInt = 2;
        public final static int FPReal = 3;
        public final static int FloatReal = 4;
        public final static int FloatComplex = 5;
    }

    public int getWidth() {
        return nFormatWidth;
    }

    /** Get pointer to subfield name. */
    public String getName() {
        return pszName;
    }

    /** Get pointer to subfield format string */
    public String getFormat() {
        return pszFormatString;
    }

    /**
     * Get the general type of the subfield. This can be used to determine which
     * of ExtractFloatData(), ExtractIntData() or ExtractStringData() should be
     * used.
     * 
     * @return The subfield type. One of DDFInt, DDFFloat, DDFString or
     *         DDFBinaryString.
     */
    public DDFDataType getType() {
        return eType;
    }

    public DDFSubfieldDefinition() {
        pszName = null;

        bIsVariable = true;
        nFormatWidth = 0;
        chFormatDelimeter = DDF_UNIT_TERMINATOR;
        eBinaryFormat = DDFBinaryFormat.NotBinary;
        eType = DDFDataType.DDFString;

        pszFormatString = new String("");
    }

    /**
     * Set the name of the subfield.
     */
    public void setName(String pszNewName) {
        pszName = pszNewName.trim();
    }

    /**
     * While interpreting the format string we don't support:
     * <UL>
     * <LI>Passing an explicit terminator for variable length field.
     * <LI>'X' for unused data ... this should really be filtered
     * <LI>out by DDFFieldDefinition.applyFormats(), but isn't.
     * <LI>'B' bitstrings that aren't a multiple of eight.
     * </UL>
     */
    public boolean setFormat(String pszFormat) {
        pszFormatString = pszFormat;

        if (Debug.debugging("iso8211")) {
            Debug.output("DDFSubfieldDefinition.setFormat(" + pszFormat + ")");
        }

        /* -------------------------------------------------------------------- */
        /* These values will likely be used. */
        /* -------------------------------------------------------------------- */
        if (pszFormatString.length() > 1 && pszFormatString.charAt(1) == '(') {

            // Need to loop through characters to grab digits, and
            // then get integer version. If we look a the atoi code,
            // it checks for non-digit characters and then stops.
            int i = 3;
            for (; i < pszFormat.length()
                    && Character.isDigit(pszFormat.charAt(i)); i++) {
            }

            nFormatWidth = Integer.parseInt(pszFormat.substring(2, i));
            bIsVariable = (nFormatWidth == 0);
        } else {
            bIsVariable = true;
        }

        /* -------------------------------------------------------------------- */
        /* Interpret the format string. */
        /* -------------------------------------------------------------------- */
        switch (pszFormatString.charAt(0)) {

        case 'A':
        case 'C': // It isn't clear to me how this is different than
            // 'A'
            eType = DDFDataType.DDFString;
            break;

        case 'R':
            eType = DDFDataType.DDFFloat;
            break;

        case 'I':
        case 'S':
            eType = DDFDataType.DDFInt;
            break;

        case 'B':
        case 'b':
            // Is the width expressed in bits? (is it a bitstring)
            bIsVariable = false;
            if (pszFormatString.charAt(1) == '(') {

                int numEndIndex = 2;
                for (; numEndIndex < pszFormatString.length()
                        && Character.isDigit(pszFormatString
                                .charAt(numEndIndex)); numEndIndex++) {
                }

                String numberString = pszFormatString.substring(2, numEndIndex);
                nFormatWidth = Integer.valueOf(numberString).intValue();

                if (nFormatWidth % 8 != 0) {
                    Debug
                            .error("DDFSubfieldDefinition.setFormat() problem with "
                                    + pszFormatString.charAt(0)
                                    + " not being modded with 8 evenly");
                    return false;
                }

                nFormatWidth = Integer.parseInt(numberString) / 8;

                eBinaryFormat = DDFBinaryFormat.SInt; // good
                // default,
                // works for
                // SDTS.

                if (nFormatWidth < 5) {
                    eType = DDFDataType.DDFInt;
                } else {
                    eType = DDFDataType.DDFBinaryString;
                }

            } else { // or do we have a binary type indicator? (is it binary)

                eBinaryFormat = (int) (pszFormatString.charAt(1) - '0');

                int numEndIndex = 2;
                for (; numEndIndex < pszFormatString.length()
                        && Character.isDigit(pszFormatString
                                .charAt(numEndIndex)); numEndIndex++) {
                }

                nFormatWidth = Integer.valueOf(
                                               pszFormatString
                                                       .substring(2,
                                                                  numEndIndex))
                        .intValue();

                if (eBinaryFormat == DDFBinaryFormat.SInt
                        || eBinaryFormat == DDFBinaryFormat.UInt) {

                    eType = DDFDataType.DDFInt;
                } else {
                    eType = DDFDataType.DDFFloat;
                }
            }
            break;

        case 'X':
            // 'X' is extra space, and shouldn't be directly assigned
            // to a
            // subfield ... I haven't encountered it in use yet
            // though.
            Debug.error("DDFSubfieldDefinition: Format type of "
                    + pszFormatString.charAt(0) + " not supported.");

            return false;
        default:
            Debug.error("DDFSubfieldDefinition: Format type of "
                    + pszFormatString.charAt(0) + " not recognised.");
            return false;
        }

        return true;
    }

    /**
     * Write out subfield definition info. A variety of information about this
     * field definition is written to the give debugging file handle.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("    DDFSubfieldDefn:\n");
        buf.append("        Label = ").append(pszName).append("\n");
        buf.append("        FormatString = ").append(pszFormatString).append("\n");
        return buf.toString();
    }

    /**
     * Scan for the end of variable length data. Given a pointer to the data for
     * this subfield (from within a DDFRecord) this method will return the
     * number of bytes which are data for this subfield. The number of bytes
     * consumed as part of this field can also be fetched. This number may be
     * one longer than the length if there is a terminator character used.
     * <p>
     * 
     * This method is mainly for internal use, or for applications which want
     * the raw binary data to interpret themselves. Otherwise use one of
     * ExtractStringData(), ExtractIntData() or ExtractFloatData().
     * 
     * @param pachSourceData
     *            The pointer to the raw data for this field. This may have come
     *            from DDFRecord::GetData(), taking into account skip factors
     *            over previous subfields data.
     * @param nMaxBytes
     *            The maximum number of bytes that are accessible after
     *            pachSourceData.
     * @param pnConsumedBytes
     *            the number of bytes used.
     * 
     * @return The number of bytes at pachSourceData which are actual data for
     *         this record (not including unit, or field terminator).
     */
    public int getDataLength(byte[] pachSourceData, int nMaxBytes,
                             MutableInt pnConsumedBytes) {
        if (!bIsVariable) {
            if (nFormatWidth > nMaxBytes) {
                Debug.error("DDFSubfieldDefinition: Only " + nMaxBytes
                        + " bytes available for subfield " + pszName
                        + " with format string " + pszFormatString
                        + " ... returning shortened data.");

                if (pnConsumedBytes != null) {
                    pnConsumedBytes.value = nMaxBytes;
                }

                return nMaxBytes;
            } else {

                if (pnConsumedBytes != null) {
                    pnConsumedBytes.value = nFormatWidth;
                }

                return nFormatWidth;
            }

        } else {

            int nLength = 0;
            boolean bCheckFieldTerminator = true;

            /*
             * We only check for the field terminator because of some buggy
             * datasets with missing format terminators. However, we have found
             * the field terminator is a legal character within the fields of
             * some extended datasets (such as JP34NC94.000). So we don't check
             * for the field terminator if the field appears to be multi-byte
             * which we established by the first character being out of the
             * ASCII printable range (32-127).
             */

            if (pachSourceData[0] < 32 || pachSourceData[0] >= 127) {
                bCheckFieldTerminator = false;
            }

            while (nLength < nMaxBytes
                    && pachSourceData[nLength] != chFormatDelimeter) {

                if (bCheckFieldTerminator
                        && pachSourceData[nLength] == DDF_FIELD_TERMINATOR)
                    break;

                nLength++;
            }

            if (pnConsumedBytes != null) {
                if (nMaxBytes == 0) {
                    pnConsumedBytes.value = nLength;
                } else {
                    pnConsumedBytes.value = nLength + 1;
                }
            }

            return nLength;
        }
    }

    /**
     * Extract a zero terminated string containing the data for this subfield.
     * Given a pointer to the data for this subfield (from within a DDFRecord)
     * this method will return the data for this subfield. The number of bytes
     * consumed as part of this field can also be fetched. This number may be
     * one longer than the string length if there is a terminator character
     * used.
     * <p>
     * 
     * This function will return the raw binary data of a subfield for types
     * other than DDFString, including data past zero chars. This is the
     * standard way of extracting DDFBinaryString subfields for instance.
     * <p>
     * 
     * @param pachSourceData
     *            The pointer to the raw data for this field. This may have come
     *            from DDFRecord::GetData(), taking into account skip factors
     *            over previous subfields data.
     * @param nMaxBytes
     *            The maximum number of bytes that are accessible after
     *            pachSourceData.
     * @param pnConsumedBytes
     *            Pointer to an integer into which the number of bytes consumed
     *            by this field should be written. May be null to ignore. This
     *            is used as a skip factor to increment pachSourceData to point
     *            to the next subfields data.
     * 
     * @return A pointer to a buffer containing the data for this field. The
     *         returned pointer is to an internal buffer which is invalidated on
     *         the next ExtractStringData() call on this DDFSubfieldDefn(). It
     *         should not be freed by the application.
     */
    String extractStringData(byte[] pachSourceData, int nMaxBytes,
                             MutableInt pnConsumedBytes) {
        int oldConsumed = 0;
        if (pnConsumedBytes != null) {
            oldConsumed = pnConsumedBytes.value;
        }

        int nLength = getDataLength(pachSourceData, nMaxBytes, pnConsumedBytes);
        String ns = new String(pachSourceData, 0, nLength);

        if (Debug.debugging("iso8211detail") && pnConsumedBytes != null) {
            Debug.output("        extracting string data from " + nLength
                    + " bytes of " + pachSourceData.length + ": " + ns
                    + ": consumed " + pnConsumedBytes.value + " vs. "
                    + oldConsumed + ", max = " + nMaxBytes);
        }

        return ns;
    }

    /**
     * Extract a subfield value as a float. Given a pointer to the data for this
     * subfield (from within a DDFRecord) this method will return the floating
     * point data for this subfield. The number of bytes consumed as part of
     * this field can also be fetched. This method may be called for any type of
     * subfield, and will return zero if the subfield is not numeric.
     * 
     * @param pachSourceData
     *            The pointer to the raw data for this field. This may have come
     *            from DDFRecord::GetData(), taking into account skip factors
     *            over previous subfields data.
     * @param nMaxBytes
     *            The maximum number of bytes that are accessible after
     *            pachSourceData.
     * @param pnConsumedBytes
     *            Pointer to an integer into which the number of bytes consumed
     *            by this field should be written. May be null to ignore. This
     *            is used as a skip factor to increment pachSourceData to point
     *            to the next subfields data.
     * 
     * @return The subfield's numeric value (or zero if it isn't numeric).
     */
    public double extractFloatData(byte[] pachSourceData, int nMaxBytes,
                                   MutableInt pnConsumedBytes) {

        switch (pszFormatString.charAt(0)) {
        case 'A':
        case 'I':
        case 'R':
        case 'S':
        case 'C':
            String dataString = extractStringData(pachSourceData, nMaxBytes,
                                                  pnConsumedBytes);

            if (dataString.length() == 0) {
                return 0;
            }

            try {
                return Double.parseDouble(dataString);
            } catch (NumberFormatException nfe) {
                if (Debug.debugging("iso8211")) {
                    Debug
                            .output("DDFSubfieldDefinition.extractFloatData: number format problem: "
                                    + dataString);
                }
                return 0;
            }

        case 'B':
        case 'b':
            byte[] abyData = new byte[8];

            if (pnConsumedBytes != null) {
                pnConsumedBytes.value = nFormatWidth;
            }

            if (nFormatWidth > nMaxBytes) {
                Debug
                        .error("DDFSubfieldDefinition: format width is greater than max bytes for float");
                return 0.0;
            }

            // Byte swap the data if it isn't in machine native
            // format. In any event we copy it into our buffer to
            // ensure it is word aligned.
            //
            // DFD - don't think this applies to Java, since it's
            // always big endian

            // if (pszFormatString.charAt(0) == 'B') ||
            // (pszFormatString.charAt(0) == 'b') {
            // for (int i = 0; i < nFormatWidth; i++) {
            // abyData[nFormatWidth-i-1] = pachSourceData[i];
            // }
            // } else {
            // System.arraycopy(pachSourceData, 0, abyData, 8-nFormatWidth,
            // nFormatWidth);
            System.arraycopy(pachSourceData, 0, abyData, 0, nFormatWidth);
            // }

            // Interpret the bytes of data.
            switch (eBinaryFormat) {
            case DDFBinaryFormat.UInt:
            case DDFBinaryFormat.SInt:
            case DDFBinaryFormat.FloatReal:
                return (int) pszFormatString.charAt(0) == 'B' ? MoreMath
                        .BuildIntegerBE(abyData) : MoreMath
                        .BuildIntegerLE(abyData);

                // if (nFormatWidth == 1)
                // return(abyData[0]);
                // else if (nFormatWidth == 2)
                // return(*((GUInt16 *) abyData));
                // else if (nFormatWidth == 4)
                // return(*((GUInt32 *) abyData));
                // else {
                // return 0.0;
                // }

                // case DDFBinaryFormat.SInt:
                // if (nFormatWidth == 1)
                // return(*((signed char *) abyData));
                // else if (nFormatWidth == 2)
                // return(*((GInt16 *) abyData));
                // else if (nFormatWidth == 4)
                // return(*((GInt32 *) abyData));
                // else {
                // return 0.0;
                // }

                // case DDFBinaryFormat.FloatReal:
                // if (nFormatWidth == 4)
                // return(*((float *) abyData));
                // else if (nFormatWidth == 8)
                // return(*((double *) abyData));
                // else {
                // return 0.0;
                // }

            case DDFBinaryFormat.NotBinary:
            case DDFBinaryFormat.FPReal:
            case DDFBinaryFormat.FloatComplex:
                return 0.0;
            }
            break;
        // end of 'b'/'B' case.

        default:

        }

        return 0.0;
    }

    /**
     * Extract a subfield value as an integer. Given a pointer to the data for
     * this subfield (from within a DDFRecord) this method will return the int
     * data for this subfield. The number of bytes consumed as part of this
     * field can also be fetched. This method may be called for any type of
     * subfield, and will return zero if the subfield is not numeric.
     * 
     * @param pachSourceData
     *            The pointer to the raw data for this field. This may have come
     *            from DDFRecord::GetData(), taking into account skip factors
     *            over previous subfields data.
     * @param nMaxBytes
     *            The maximum number of bytes that are accessible after
     *            pachSourceData.
     * @param pnConsumedBytes
     *            Pointer to an integer into which the number of bytes consumed
     *            by this field should be written. May be null to ignore. This
     *            is used as a skip factor to increment pachSourceData to point
     *            to the next subfields data.
     * 
     * @return The subfield's numeric value (or zero if it isn't numeric).
     */
    public int extractIntData(byte[] pachSourceData, int nMaxBytes,
                              MutableInt pnConsumedBytes) {

        switch (pszFormatString.charAt(0)) {
        case 'A':
        case 'I':
        case 'R':
        case 'S':
        case 'C':
            String dataString = extractStringData(pachSourceData, nMaxBytes,
                                                  pnConsumedBytes);
            if (dataString.length() == 0) {
                return 0;
            }

            try {
                return Double.valueOf(dataString).intValue();
            } catch (NumberFormatException nfe) {
                if (Debug.debugging("iso8211")) {
                    Debug
                            .output("DDFSubfieldDefinition.extractIntData: number format problem: "
                                    + dataString);
                }
                return 0;
            }

        case 'B':
        case 'b':
            byte[] abyData = new byte[4];
            if (nFormatWidth > nMaxBytes) {
                Debug
                        .error("DDFSubfieldDefinition: format width is greater than max bytes for int");
                return 0;
            }

            if (pnConsumedBytes != null) {
                pnConsumedBytes.value = nFormatWidth;
            }

            // System.arraycopy(pachSourceData, 0, abyData, 4-nFormatWidth,
            // nFormatWidth);
            System.arraycopy(pachSourceData, 0, abyData, 0, nFormatWidth);

            // Interpret the bytes of data.
            switch (eBinaryFormat) {
            case DDFBinaryFormat.UInt:
            case DDFBinaryFormat.SInt:
            case DDFBinaryFormat.FloatReal:
                return (int) pszFormatString.charAt(0) == 'B' ? MoreMath
                        .BuildIntegerBE(abyData) : MoreMath
                        .BuildIntegerLE(abyData);

                // case DDFBinaryFormat.UInt:
                // if (nFormatWidth == 4)
                // return((int) *((GUInt32 *) abyData));
                // else if (nFormatWidth == 1)
                // return(abyData[0]);
                // else if (nFormatWidth == 2)
                // return(*((GUInt16 *) abyData));
                // else {
                // CPLAssert(false);
                // return 0;
                // }

                // case DDFBinaryFormat.SInt:
                // if (nFormatWidth == 4)
                // return(*((GInt32 *) abyData));
                // else if (nFormatWidth == 1)
                // return(*((signed char *) abyData));
                // else if (nFormatWidth == 2)
                // return(*((GInt16 *) abyData));
                // else {
                // CPLAssert(false);
                // return 0;
                // }

                // case DDFBinaryFormat.FloatReal:
                // if (nFormatWidth == 4)
                // return((int) *((float *) abyData));
                // else if (nFormatWidth == 8)
                // return((int) *((double *) abyData));
                // else {
                // CPLAssert(false);
                // return 0;
                // }

            case DDFBinaryFormat.NotBinary:
            case DDFBinaryFormat.FPReal:
            case DDFBinaryFormat.FloatComplex:
                return 0;
            }
            break;
        // end of 'b'/'B' case.

        default:
            return 0;
        }

        return 0;
    }

    /**
     * Dump subfield value to debugging file.
     * 
     * @param pachData
     *            Pointer to data for this subfield.
     * @param nMaxBytes
     *            Maximum number of bytes available in pachData.
     */
    public String dumpData(byte[] pachData, int nMaxBytes) {
        StringBuffer sb = new StringBuffer();
        if (eType == DDFDataType.DDFFloat) {
            sb.append("      Subfield ").append(pszName).append("=")
                   .append(extractFloatData(pachData, nMaxBytes, null)).append("\n");
        } else if (eType == DDFDataType.DDFInt) {
            sb.append("      Subfield ").append(pszName).append("=")
                   .append(extractIntData(pachData, nMaxBytes, null)).append("\n");
        } else if (eType == DDFDataType.DDFBinaryString) {
            sb.append("      Subfield ").append(pszName).append("=")
                   .append(extractStringData(pachData, nMaxBytes, null)).append("\n");
        } else {
            sb.append("      Subfield ").append(pszName).append("=")
                   .append(extractStringData(pachData, nMaxBytes, null)).append("\n");
        }
        return sb.toString();
    }

}
