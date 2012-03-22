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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/DcwColumnInfo.java,v $
// $Revision: 1.6 $ $Date: 2007/01/26 15:57:18 $ $Author: dietrick $
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.EOFException;

import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;

/**
 * Encapsulate the information about a particular column in a vpf
 * table. This class can read both VPF V1 (MIL-STD-600006, dated 1992)
 * and VPF V2 (MIL-STD-2407, dated 1996, supercedes V1)
 */
public class DcwColumnInfo {
    /** the name of the column */
    final private String columnName;
    /** the fieldtype of the contained data */
    final private char fieldType;
    /** the number of values (-1 indicates variable) */
    final private int numberOfElements;
    /** the keytype (primary key, non-key, foreign key) */
    final private char keyType;
    /** optional text description of what the column is for */
    final private String columnDescription;
    /**
     * optional table that provides descriptions of what the values in
     * this column are
     */
    private String valueDescriptionTable = null;
    /** name of the optional thematic index created for this column */
    private String thematicIndexName = null;
    /** name of the optional narrative table for this column */
    private String narrativeTable = null;

    /** VPF Column Type Constants */
    public static final char VPF_COLUMN_TEXT = 'T';
    public static final char VPF_COLUMN_TEXTL1 = 'L';
    public static final char VPF_COLUMN_TEXTL2 = 'M';
    public static final char VPF_COLUMN_TEXTL3 = 'N';
    public static final char VPF_COLUMN_FLOAT = 'F';
    public static final char VPF_COLUMN_DOUBLE = 'R';
    public static final char VPF_COLUMN_SHORT = 'S';
    public static final char VPF_COLUMN_INT = 'I';
    public static final char VPF_COLUMN_FLOAT_2COORD = 'C';
    public static final char VPF_COLUMN_DOUBLE_2COORD = 'B';
    public static final char VPF_COLUMN_FLOAT_3COORD = 'Z';
    public static final char VPF_COLUMN_DOUBLE_3COORD = 'Y';
    public static final char VPF_COLUMN_DATE = 'D';
    public static final char VPF_COLUMN_NULL = 'X';
    public static final char VPF_COLUMN_TRIPLET = 'K';
    /**
     * VPF Column Type Constant for a column that can be either int or
     * short. This value will never be read from a VPF file, its a
     * special value that is accepted by lookupSchema
     */
    public static final char VPF_COLUMN_INT_OR_SHORT = 'i';

    /** VPF Column Key Type Constants */
    public static final char VPF_COLUMN_PRIMARY_KEY = 'P';
    public static final char VPF_COLUMN_FOREIGN_KEY = 'F';
    public static final char VPF_COLUMN_NON_KEY = 'N';

    /**
     * Construct a DcwColumnInfo from the specified input stream.
     *
     * @param inputFile the filestream to construct from
     * @exception EOFException when the first character read is a ';',
     *            indicating that we've reached the end of the column
     *            list; also thrown for an end of file
     * @exception FormatException some error was detected while
     *            reading the info for the column.
     */
    public DcwColumnInfo(BinaryFile inputFile) throws EOFException,
            FormatException {
        char delim = inputFile.readChar();
        if (delim == ';')
            throw new EOFException();

        StringBuffer buildstring = new StringBuffer();
        do {
            buildstring.append(Character.toLowerCase(delim));
        } while ((delim = inputFile.readChar()) != '=');

        columnName = buildstring.toString().trim().intern();// Collapse all blanks

        fieldType = inputFile.readChar();

        delim = inputFile.readChar();
        if (delim != ',') { //only legal delimiter
            if (delim != ' ') { //one DCW file uses this instead
                throw new com.bbn.openmap.io.InvalidCharException("Illegal delimiter character", delim);
            }
        }

        buildstring = new StringBuffer();
        while ((delim = inputFile.readChar()) != ',') {
            // field length occasionally has trailing whitespace...
            if (!Character.isWhitespace(delim)) {
                buildstring.append(delim); //assumes not like "1 4"
            }
        }
        String nEls = buildstring.toString();
        numberOfElements = (nEls.equals("*")) ? -1 : Integer.parseInt(nEls);

        // Sanity check the column schema... a few VPF primitives are
        // not
        // allowed to show up in arrays. complain about that now...
        if (numberOfElements != 1) {
            switch (fieldType) {
            case VPF_COLUMN_FLOAT:
            case VPF_COLUMN_DOUBLE:
            case VPF_COLUMN_SHORT:
            case VPF_COLUMN_INT:
            case VPF_COLUMN_DATE:
            case VPF_COLUMN_NULL:
            case VPF_COLUMN_TRIPLET:
                throw new FormatException("Illegal array type: " + fieldType
                        + "for column " + columnName);
            default:
                //legal
                break;
            }
        }

        String tmpkeyType = readColumnText(inputFile);
        if (tmpkeyType == null) {
            throw new FormatException("keyType is required column info");
        }
        tmpkeyType = tmpkeyType.trim();
        if (tmpkeyType.length() == 1) {
            keyType = tmpkeyType.charAt(0);
        } else {
            throw new FormatException("keyType is supposed to be 1 character");
        }
        columnDescription = readColumnText(inputFile);
        if (columnDescription == null) {
            return;
        }

        valueDescriptionTable = readColumnTextLowerCase(inputFile);
        if (valueDescriptionTable == null) {
            return;
        }
        if (valueDescriptionTable.equals("-")) {
            valueDescriptionTable = null;
        } else {
            valueDescriptionTable = valueDescriptionTable.intern();
        }

        thematicIndexName = readColumnTextLowerCase(inputFile);
        if (thematicIndexName == null) {
            return;
        }
        if (thematicIndexName.equals("-")) {
            thematicIndexName = null;
        } else {
            thematicIndexName = thematicIndexName.intern();
        }

        narrativeTable = readColumnTextLowerCase(inputFile);
        if (narrativeTable == null) {
            return;
        }
        if (narrativeTable.equals("-")) {
            narrativeTable = null;
        } else {
            narrativeTable = narrativeTable.intern();
        }

        inputFile.assertChar(':');
    }

    /**
     * Reads a string until the field separator is detected, the
     * column record separator is detected, or and end-of-file is hit.
     *
     * @return the string read from the file
     * @param inputFile the file to read the field from
     * @param toLower convert the string to lower-case
     * @exception FormatException ReadChar IOExceptions rethrown as
     *            FormatExceptions
     */
    private String readColumnText(BinaryFile inputFile) throws FormatException {
        StringBuffer buildretval = new StringBuffer();
        boolean skipnext = false;
        char tmp;
        try {
            while ((tmp = inputFile.readChar()) != ',') {
                if ((tmp == ':') && !skipnext) {
                    return null;
                }
                if (tmp == '\\') {
                    skipnext = true;
                } else {
                    skipnext = false;
                    buildretval.append(tmp);
                }
            }
        } catch (EOFException e) {
            //allowable
        }
        return buildretval.toString();
    }

    /**
     * Reads a string until the field separator is detected, the
     * column record separator is detected, or and end-of-file is hit,
     * and converts in to lowercase.
     *
     * @return the string read from the file, all in lowercase
     * @param inputFile the file to read the field from
     * @param toLower convert the string to lower-case
     * @exception FormatException ReadChar IOExceptions rethrown as
     *            FormatExceptions
     */
    private String readColumnTextLowerCase(BinaryFile inputFile)
            throws FormatException {
        StringBuffer buildretval = new StringBuffer();
        boolean skipnext = false;
        char tmp;
        try {
            while ((tmp = inputFile.readChar()) != ',') {
                if ((tmp == ':') && !skipnext) {
                    return null;
                }
                if (tmp == '\\') {
                    skipnext = true;
                } else {
                    skipnext = false;
                    buildretval.append(Character.toLowerCase(tmp));
                }
            }
        } catch (EOFException e) {
            //allowable
        }
        return buildretval.toString();
    }

    /**
     * Claim that the column has a particular schema
     *
     * @param type the FieldType (datatype) this column is expected to
     *        contain legal values are specified by the VPF standard.
     *        the non-standard value 'i' is also accepted (equivalent
     *        to 'I' or 'S'), indicating an integral type.
     * @param length the number of elements in this column
     * @param strictlength false means that variable length columns
     *        can be fixed length instead
     * @exception FormatException the column is not of the particular
     *            type/length
     */
    public void assertSchema(char type, int length, boolean strictlength)
            throws FormatException {
        if ((type != fieldType)
                && !((type == 'i') && ((fieldType == VPF_COLUMN_INT) || (fieldType == VPF_COLUMN_SHORT)))) {
            throw new FormatException("AssertSchema failed on fieldType!");
        }
        if ((strictlength && (length != numberOfElements))
                || (!strictlength && (length != -1) && (length != numberOfElements))) {
            throw new FormatException("AssertSchema failed on length!");
        }
    }

    /**
     * the number of bytes a field of this type takes in the input
     * file
     *
     * @return the number of bytes (-1 for a variable-length field)
     * @exception FormatException the FieldType of this Column is not
     *            a valid VPF fieldtype
     */
    public int fieldLength() throws FormatException {
        if (numberOfElements == -1) {
            return -1;
        }

        switch (fieldType) {
        case VPF_COLUMN_TEXT:
        case VPF_COLUMN_TEXTL1:
        case VPF_COLUMN_TEXTL3:
        case VPF_COLUMN_TEXTL2: //various text string types
            return numberOfElements;
        case VPF_COLUMN_FLOAT: //floats
            return 4;
        case VPF_COLUMN_DOUBLE: //doubles
            return 8;
        case VPF_COLUMN_SHORT: //shorts
            return 2;
        case VPF_COLUMN_INT: //ints
            return 4;
        case VPF_COLUMN_FLOAT_2COORD: //2-coord floats
            return numberOfElements * 8;
        case VPF_COLUMN_DOUBLE_2COORD: //2-coord doubles
            return numberOfElements * 16;
        case VPF_COLUMN_FLOAT_3COORD: //3-coord floats
            return numberOfElements * 12;
        case VPF_COLUMN_DOUBLE_3COORD: //3-coord doubles
            return numberOfElements * 24;
        case VPF_COLUMN_DATE: //dates
            return 20;
        case VPF_COLUMN_NULL: //nulls
            return 0;
        case VPF_COLUMN_TRIPLET: //cross-tile identifiers
            return -1; //variable length
        default: {
            throw new FormatException("Unknown field type: " + fieldType);
        }
        }
        //unreached
    }

    /**
     * get the name of the column
     *
     * @return the name of the column
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * get the VPF datatype of the column
     *
     * @return the VPF datatype
     */
    public char getFieldType() {
        return fieldType;
    }

    /**
     * get the number of elements
     *
     * @return the number of elements
     */
    public int getNumberOfElements() {
        return numberOfElements;
    }

    /**
     * get the VPF key type (one of VPF_COLUMN_PRIMARY_KEY,
     * VPF_COLUMN_FOREIGN_KEY, or VPF_COLUMN_NON_KEY)
     *
     * @return the vpf key type
     */
    public char getKeyType() {
        return keyType;
    }

    /**
     * Return <code>true</code> if this column is a primary key. For
     * any valid column, exactly one of isPrimaryKey, isForeignKey and
     * isNonKey will be <code>true</code>.
     *
     * @return true for a primary key, false otherwise.
     * @see #isForeignKey()
     * @see #isNonKey()
     */
    public boolean isPrimaryKey() {
        return (keyType == VPF_COLUMN_PRIMARY_KEY);
    }

    /**
     * Return <code>true</code> if this column is a foreign key. For
     * any valid column, exactly one of isPrimaryKey, isForeignKey and
     * isNonKey will be <code>true</code>.
     *
     * @return true for a foreign key, false otherwise.
     * @see #isPrimaryKey()
     * @see #isNonKey()
     */
    public boolean isForeignKey() {
        return (keyType == VPF_COLUMN_FOREIGN_KEY);
    }

    /**
     * Return <code>true</code> if this column is not a key column.
     * For any valid column, exactly one of isPrimaryKey, isForeignKey
     * and isNonKey will be <code>true</code>.
     *
     * @return false for a primary or foreign key, true otherwise.
     * @see #isForeignKey()
     * @see #isPrimaryKey()
     */
    public boolean isNonKey() {
        return (keyType == VPF_COLUMN_NON_KEY);
    }

    /**
     * Get the column description
     *
     * @return the column description (possibly <code>null</code>)
     */
    public String getColumnDescription() {
        return columnDescription;
    }

    /**
     * Get the name of the value description table
     *
     * @return the name of the value description table (possibly
     *         <code>null</code>). The same as getVDT()
     * @see #getVDT()
     */
    public String getValueDescriptionTable() {
        return valueDescriptionTable;
    }

    /**
     * Get the name of the value description table
     *
     * @return the name of the value description table (possibly
     *         <code>null</code>). The same as
     *         getValueDescriptionTable
     * @see #getValueDescriptionTable()
     */
    public String getVDT() {
        return valueDescriptionTable;
    }

    /**
     * get the name of the thematic index
     *
     * @return the thematic index name (possibly <code>null</code>)
     */
    public String getThematicIndexName() {
        return thematicIndexName;
    }

    /**
     * get the name of the narrative table
     *
     * @return the name of the narrative table (possibly
     *         <code>null</code>)
     */
    public String getNarrativeTable() {
        return narrativeTable;
    }

    /**
     * Read an element of the type specified by the column
     *
     * @return the value read from the input file
     * @exception EOFException an end-of-file was encountered before
     *            reading any of the field
     * @exception FormatException some data-consistency check failed
     *            while reading the data, or an end-of-file condition
     *            popped up in the middle of reading a field (partial
     *            read)
     */
    public Object parseField(BinaryFile inputFile) throws EOFException,
            FormatException {
        // See table 56, p 79 of MIL-STD-600006 (1992 VPF Standard)
        // See table 10, p 51 of MIL-STD-2407 (1996 VPF Standard
        // supercedes 600006)
        boolean haveElements = (numberOfElements != -1);
        int numels = numberOfElements;

        switch (fieldType) {
        case VPF_COLUMN_TEXT: {
            if (!haveElements) {//Variable length string
                numels = inputFile.readInteger();
            }
            if (numels == 0) {
                return "";
            }
            String s = inputFile.readFixedLengthString(numels);
            if (haveElements) {//Fixed Length Strings loose trailing
                               // whitespace
                s = s.trim();
            }
            return s;
        }
        case VPF_COLUMN_TEXTL1: {
            if (!haveElements) {//Variable length string
                numels = inputFile.readInteger();
            }
            if (numels == 0) {
                return "";
            }
            byte[] str = inputFile.readBytes(numels, false);
            try {
                String s = new String(str, "ISO8859_1");
                if (haveElements) {//Fixed Length Strings loose
                                   // trailing whitespace
                    s = s.trim();
                }
                return s;
            } catch (java.io.UnsupportedEncodingException uee) {
                return str;
            }
        }
        case VPF_COLUMN_TEXTL2:
        case VPF_COLUMN_TEXTL3: {
            if (!haveElements) {//Variable length string
                numels = inputFile.readInteger();
            }
            if (numels == 0) {
                return new byte[0];
            }
            return inputFile.readBytes(numels, false);
        }
        case VPF_COLUMN_FLOAT: {
            return new Float(inputFile.readFloat());
        }
        case VPF_COLUMN_DOUBLE: {
            return new Double(inputFile.readDouble());
        }
        case VPF_COLUMN_SHORT: {
            return new Short(inputFile.readShort());
        }
        case VPF_COLUMN_INT: {
            return new Integer(inputFile.readInteger());
        }
        case VPF_COLUMN_FLOAT_2COORD: { //2-coord floats
            if (!haveElements) {
                numels = inputFile.readInteger();
            }
            return new CoordFloatString(numels, 2, inputFile);
        }
        case VPF_COLUMN_DOUBLE_2COORD: { //2-coord doubles
            if (!haveElements) {
                numels = inputFile.readInteger();
            }
            return new CoordDoubleString(numels, 2, inputFile);
        }
        case VPF_COLUMN_FLOAT_3COORD: { //3-coord floats
            if (!haveElements) {
                numels = inputFile.readInteger();
            }
            return new CoordFloatString(numels, 3, inputFile);
        }
        case VPF_COLUMN_DOUBLE_3COORD: { //3-coord doubles
            if (!haveElements) {
                numels = inputFile.readInteger();
            }
            return new CoordDoubleString(numels, 3, inputFile);
        }
        case VPF_COLUMN_DATE: {
            inputFile.readBytes(20, false);
            return "[skipped date]";
        }
        case VPF_COLUMN_NULL: {
            return "[Null Field Type]";
        }
        case VPF_COLUMN_TRIPLET: {
            return new DcwCrossTileID(inputFile);
        }
        default: {
            throw new FormatException("Unknown field type: " + fieldType);
        }
        }
        //unreached
    }

    /**
     * produce a nice printed version of all our contained information
     *
     * @return a nice little string
     */
    public String toString() {
        StringBuffer output = new StringBuffer();
        output.append(columnName).append(" ").append(fieldType).append(" ");
        output.append(numberOfElements).append(" ");
        output.append(keyType).append(" ");
        output.append(columnDescription).append(" ").append(valueDescriptionTable).append(" ");
        output.append(thematicIndexName).append(" ").append(narrativeTable);
        return output.toString();
    }
}
