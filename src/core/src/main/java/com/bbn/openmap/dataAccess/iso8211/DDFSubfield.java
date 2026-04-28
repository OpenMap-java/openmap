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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/iso8211/DDFSubfield.java,v $
// $RCSfile: DDFSubfield.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:43 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.iso8211;

import com.bbn.openmap.layer.vpf.MutableInt;

/**
 * Class containing subfield information for a DDFField object.
 */
public class DDFSubfield {

    /**
     * A DDFSubfieldDefinition defining the admin part of the file
     * that contains the subfield data.
     */
    protected DDFSubfieldDefinition defn;
    /**
     * The object containing the value of the field.
     */
    protected Object value;
    /**
     * The number of bytes the field took up in the data file.
     */
    protected int byteSize;

    protected DDFSubfield() {}

    /**
     * Create a subfield with a definition and a value.
     */
    public DDFSubfield(DDFSubfieldDefinition ddfsd, Object value) {
        setDefn(ddfsd);
        setValue(value);
    }

    /**
     * Create a subfield with a definition and the bytes containing
     * the information for the value. The definition parameters will
     * tell the DDFSubfield what kind of object to create for the
     * data.
     */
    public DDFSubfield(DDFSubfieldDefinition poSFDefn, byte[] pachFieldData,
            int nBytesRemaining) {
        defn = poSFDefn;
        MutableInt nBytesConsumed = new MutableInt();
        DDFDataType ddfdt = poSFDefn.getType();

        if (ddfdt == DDFDataType.DDFInt) {
            setValue(new Integer(defn.extractIntData(pachFieldData,
                    nBytesRemaining,
                    nBytesConsumed)));
        } else if (ddfdt == DDFDataType.DDFFloat) {
            setValue(new Double(defn.extractFloatData(pachFieldData,
                    nBytesRemaining,
                    nBytesConsumed)));
        } else if (ddfdt == DDFDataType.DDFString
                || ddfdt == DDFDataType.DDFBinaryString) {
            setValue(defn.extractStringData(pachFieldData,
                    nBytesRemaining,
                    nBytesConsumed));
        }

        byteSize = nBytesConsumed.value;
    }

    public int getByteSize() {
        return byteSize;
    }

    public void setDefn(DDFSubfieldDefinition ddsfd) {
        defn = ddsfd;
    }

    public DDFSubfieldDefinition getDefn() {
        return defn;
    }

    /**
     * Set the value of the subfield.
     */
    public void setValue(Object o) {
        value = o;
    }

    /**
     * Get the value of the subfield.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Get the value of the subfield as an int. Returns 0 if the value
     * is 0 or isn't a number.
     */
    public int intValue() {
        Object obj = getValue();
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return 0;
    }

    /**
     * Get the value of the subfield as a float. Returns 0f if the
     * value is 0 or isn't a number.
     */
    public float floatValue() {
        Object obj = getValue();
        if (obj instanceof Number) {
            return ((Number) obj).floatValue();
        }
        return 0f;
    }

    public String stringValue() {
        Object obj = getValue();

        if (obj != null) {
            return obj.toString();
        }

        return "";
    }

    /**
     * Return a string 'key = value', describing the field and its
     * value.
     */
    public String toString() {
        if (defn != null) {
            return defn.getName() + " = " + value;
        }
        return "";
    }
}