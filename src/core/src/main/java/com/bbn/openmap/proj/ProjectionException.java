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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionException.java,v $
// $RCSfile: ProjectionException.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:23 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

/**
 * A RuntimeException subclass to handle problems creating a
 * projection with the ProjectionFactory.
 */
public class ProjectionException extends RuntimeException {

    public ProjectionException(Exception e) {
        super(e.toString());
    }

    public ProjectionException(String s) {
        super(s);
    }
}