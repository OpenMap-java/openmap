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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFException.java,v $
// $RCSfile: MIFException.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:00 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.mif;

/**
 * Custom Exception class for exceptions within the loading of MIF
 * files
 * 
 * @author Simon Bowen
 */
public class MIFException extends RuntimeException {

    public MIFException() {
        super();
    }

    /**
     * @param arg0
     */
    public MIFException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public MIFException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public MIFException(Throwable arg0) {
        super(arg0);
    }
}
/** Last Line of file * */
