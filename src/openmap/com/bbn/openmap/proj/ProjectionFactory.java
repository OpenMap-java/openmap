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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionFactory.java,v $
// $RCSfile: ProjectionFactory.java,v $
// $Revision: 1.3 $
// $Date: 2004/02/06 19:03:04 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.proj;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

/**
 * Create Projections.
 */
public class ProjectionFactory {

    /**
     * PropertyChangeSupport for letting listeners know about new
     * projections that are available from the factory.
     */
    protected PropertyChangeSupport pcs;

    /**
     * Memory saving center used for new projections.
     */
    protected LatLonPoint llp = new LatLonPoint();

    /**
     * Singleton instance.
     */
    protected static ProjectionFactory instance;

    protected Hashtable projLoaders;

    /**
     * Singleton constructor.
     */
    private ProjectionFactory() {
	pcs = new PropertyChangeSupport(this);
	projLoaders = new Hashtable();
    }

    /**
     * Get the singleton instance of the ProjectionFactory.
     */
    public static ProjectionFactory getInstance() {
	if (instance == null) {
	    instance = new ProjectionFactory();
	}

	return instance;
    }

    /**
     * Create a projection.
     * @param projType projection type
     * @param centerLat center latitude in decimal degrees
     * @param centerLon center latitude in decimal degrees
     * @param scale float scale
     * @param width pixel width of projection
     * @param height pixel height of projection
     * @return Projection
     * @deprecated The notion of a projection type number is going
     * away, use the class name of the projection instead.
     */
    public static Projection makeProjection (int projType,
                                             float centerLat,
                                             float centerLon,
                                             float scale,
                                             int width,
                                             int height)
    {

	String classname = null;
	switch (projType) {
	case CADRG.CADRGType:
	    classname = "com.bbn.openmap.proj.CADRG";
	    break;
	case Mercator.MercatorType:
	    classname = "com.bbn.openmap.proj.Mercator";
	    break;
	case MercatorView.MercatorViewType:
	    classname = "com.bbn.openmap.proj.MercatorView";
	    break;
	case LLXY.LLXYType:
	    classname = "com.bbn.openmap.proj.LLXY";
	    break;
	case LLXYView.LLXYViewType:
	    classname = "com.bbn.openmap.proj.LLXYView";
	    break;
	case Orthographic.OrthographicType:
	    classname = "com.bbn.openmap.proj.Orthographic";
	    break;
	case OrthographicView.OrthographicViewType:
	    classname = "com.bbn.openmap.proj.OrthographicView";
	    break;
// 	case MassStatePlane.MassStatePlaneType:
// 	    classname = "com.bbn.openmap.proj.MassStatePlane";
// 	    break;
	case Gnomonic.GnomonicType:
	    classname = "com.bbn.openmap.proj.Gnomonic";
	    break;
	default:
	    System.err.println("Unknown projection type " + projType +
			       " in ProjectionFactory.create()");
	}
	return makeProjection(classname, centerLat, centerLon, 
			      scale, width, height);
    }

    /**
     * Create a projection.
     * @param projType projection type
     * @param centerLat center latitude in decimal degrees
     * @param centerLon center latitude in decimal degrees
     * @param scale float scale
     * @param width pixel width of projection
     * @param height pixel height of projection
     * @return Projection
     * @deprecated The notion of a projection type number is going
     * away, use the class name of the projection instead.
     */
    public static Projection makeProjection(String projClassName,
					    float centerLat,
					    float centerLon,
					    float scale,
					    int width,
					    int height)	{

	Projection proj = null;

	if (projClassName == null) {
	    projClassName = "com.bbn.openmap.proj.Mercator";
	}
	    
	Object[] args = new Object[] {
	    new LatLonPoint(centerLat, centerLon), 
	    new Float(scale),
	    new Integer(width), 
	    new Integer(height)};

	Class[] argClasses = new Class[] {
	    args[0].getClass(), Float.TYPE, Integer.TYPE, Integer.TYPE};

	try {
	    proj = (Projection)ComponentFactory.create(projClassName, args, argClasses);
	} catch (ClassCastException cce) {
	    proj = null;
	}

	if (proj == null) {
	    Debug.error("ProjectionFactory.makeProjection() tried to create a Projection from a " + projClassName + ", failed.");
	}

	return proj;
    }

    /**
     * Makes a new projection based on the given projection and given type.
     * <p>
     * The <code>centerLat</code>, <code>centerLon</code>, <code>scale</code>,
     * <code>width</code>, and <code>height</code> parameters are taken from
     * the given projection, and the type is taken from the type argument.
     * @param newProjType the type for the resulting projection
     * @param p the projection from which to copy other parameters
     * @deprecated The notion of a projection type number is going
     * away, use the class name of the projection instead.
     */
    public static Projection makeProjection (int newProjType, Projection p) {
        LatLonPoint ctr = p.getCenter();
        return makeProjection(newProjType,
                              ctr.getLatitude(), ctr.getLongitude(),
                              p.getScale(), p.getWidth(), p.getHeight());
    }

    /** 
     * Return an int representing the OpenMap projection, given the
     * name of the projection. Useful for setting a projection based
     * on the name stated in a properties file.
     * @deprecated The notion of a projection type number is going
     * away, use the class name of the projection instead.
     */
    public static int getProjType(String projName) {

        int projType = Mercator.MercatorType;

        if (projName == null){}
        else if (projName.equalsIgnoreCase(Mercator.MercatorName))
            projType = Mercator.MercatorType;
        else if (projName.equalsIgnoreCase(MercatorView.MercatorViewName))
            projType = MercatorView.MercatorViewType;
        else if (projName.equalsIgnoreCase(Orthographic.OrthographicName))
            projType = Orthographic.OrthographicType;
        else if (projName.equalsIgnoreCase(OrthographicView.OrthographicViewName))
            projType = OrthographicView.OrthographicViewType;
        else if (projName.equalsIgnoreCase(LLXY.LLXYName))
            projType = LLXY.LLXYType;
        else if (projName.equalsIgnoreCase(LLXYView.LLXYViewName))
            projType = LLXYView.LLXYViewType;
        else if (projName.equalsIgnoreCase(CADRG.CADRGName))
            projType = CADRG.CADRGType;
        else if (projName.equalsIgnoreCase(Gnomonic.GnomonicName))
            projType = Gnomonic.GnomonicType;

        return projType;
    }

    /**
     * Returns an array of Projection names available from this factory.
     */
    public static String[] getAvailableProjections() {
      // For all the possible projections we have available:
//      int nProjections = 8;
//      String projNames[] = new String[nProjections];
//      projNames[0] = Mercator.MercatorName;
//      projNames[1] = MercatorView.MercatorViewName;
//      projNames[2] = Orthographic.OrthographicName;
//      projNames[3] = OrthographicView.OrthographicViewName;
//      projNames[4] = LLXY.LLXYName;
//      projNames[5] = LLXYView.LLXYViewName;
//      projNames[6] = CADRG.CADRGName;
//      projNames[7] = Gnomonic.GnomonicName;

        int nProjections = 5;
        String projNames[] = new String[nProjections];
        projNames[0] = Mercator.MercatorName;
        projNames[1] = Orthographic.OrthographicName;
        projNames[2] = LLXY.LLXYName;
        projNames[3] = CADRG.CADRGName;
        projNames[4] = Gnomonic.GnomonicName;
        return projNames;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
	pcs.addPropertyChangeListener(pcl);
    }

    public void addPropertyChangeListener(String propertyName,
					  PropertyChangeListener pcl) {
	pcs.addPropertyChangeListener(propertyName, pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
	pcs.removePropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(String propertyName, 
					     PropertyChangeListener pcl) {
	pcs.removePropertyChangeListener(propertyName, pcl);
    }

}
