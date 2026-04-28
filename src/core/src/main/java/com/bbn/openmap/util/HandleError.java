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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/HandleError.java,v $
// $RCSfile: HandleError.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/**
 * Use this class to catch a checked exception and throw it as an
 * uncheck one. This way, people will find out about it, but don't
 * have to explicitly handle it.
 */
public class HandleError extends RuntimeException {
    public HandleError(Exception e) {
        super(e.toString());
    }

    public HandleError(String s) {
        super(s);
    }
}