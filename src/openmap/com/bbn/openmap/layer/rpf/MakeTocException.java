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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/MakeTocException.java,v $
// $RCSfile: MakeTocException.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.rpf;

/**
 * An exception that gets thrown if there is a problem with the A.TOC
 * file is getting created from MakeToc.
 */
public class MakeTocException extends Exception {

    public MakeTocException() {
        super();
    }

    public MakeTocException(String statement) {
        super(statement);
    }
}
