// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
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
// $Revision: 1.1 $
// $Date: 2004/03/02 20:45:06 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.dataAccess.iso8211;

import com.bbn.openmap.layer.vpf.MutableInt;

public class DDFSubfield {

    protected DDFSubfieldDefinition defn;
    protected Object value;
    protected int byteSize;

    protected DDFSubfield() {}

    public DDFSubfield(DDFSubfieldDefinition ddfsd, Object value) {
        setDefn(ddfsd);
        setValue(value);
    }

    public DDFSubfield(DDFSubfieldDefinition poSFDefn,
                       byte[] pachFieldData,
                       int nBytesRemaining) {
        defn = poSFDefn;
        MutableInt nBytesConsumed = new MutableInt();
        DDFDataType ddfdt = poSFDefn.getType();

        if (ddfdt == DDFDataType.DDFInt) {
            setValue(new Integer(defn.extractIntData(pachFieldData, nBytesRemaining, nBytesConsumed)));
        } else if (ddfdt == DDFDataType.DDFFloat) {
            setValue(new Double(defn.extractFloatData(pachFieldData, nBytesRemaining, nBytesConsumed)));
        } else if (ddfdt == DDFDataType.DDFString || ddfdt == DDFDataType.DDFBinaryString) {
            setValue(defn.extractStringData(pachFieldData, nBytesRemaining, nBytesConsumed));
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

    public void setValue(Object o) {
        value = o;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        if (defn != null) {
            return defn.getName() + " = " + value;
        }
        return "";
    }
}