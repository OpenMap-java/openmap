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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/iso8211/DDFDataType.java,v
// $
// $RCSfile: DDFDataType.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:05:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.iso8211;

public class DDFDataType {

    public final static DDFDataType DDFInt = new DDFDataType();
    public final static DDFDataType DDFFloat = new DDFDataType();
    public final static DDFDataType DDFString = new DDFDataType();
    public final static DDFDataType DDFBinaryString = new DDFDataType();

    protected DDFDataType() {}
}