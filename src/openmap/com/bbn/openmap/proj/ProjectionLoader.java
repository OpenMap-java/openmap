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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionLoader.java,v $
// $RCSfile: ProjectionLoader.java,v $
// $Revision: 1.3 $
// $Date: 2005/08/11 20:39:16 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.util.Properties;

/**
 * A ProjectionLoader is a class that knows how to create projection
 * classes for given parameters. The ProjectionFactory used to have
 * projection classes hard-coded into it, but it now uses
 * ProjectionLoaders to create different projections for it. It can
 * provide the Class to use for a certain projection, and can provide
 * a name and description to use for GUIs. The ProjectionFactory looks
 * for these in the MapHandler.
 * 
 * @see ProjectionFactory
 * @see BasicProjectionLoader
 */
public interface ProjectionLoader {

    /**
     * Get a class name to use for the projection. This will be used
     * as a key in the projection factory.
     */
    public Class getProjectionClass();

    /**
     * Get a pretty name for the projection.
     */
    public String getPrettyName();

    /**
     * Get a description for the projection.
     */
    public String getDescription();

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public Projection create(Properties props) throws ProjectionException;

}