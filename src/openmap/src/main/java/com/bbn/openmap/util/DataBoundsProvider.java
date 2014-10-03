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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/DataBoundsProvider.java,v $
// $RCSfile: DataBoundsProvider.java,v $
// $Revision: 1.2 $
// $Date: 2004/10/14 18:06:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/**
 * An interface describing a Component that can provide basic
 * information about the area its data covers.
 */
public interface DataBoundsProvider {

    /**
     * Returns a DataBounds object describing the area of coverage.
     * May be null if the data hasn't been evaluated yet.
     */
    public DataBounds getDataBounds();

    /**
     * A pretty name for the boundary, suitable for a GUI.
     */
    public String getName();

}