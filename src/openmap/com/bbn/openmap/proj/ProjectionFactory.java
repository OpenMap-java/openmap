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
// $Revision: 1.5 $
// $Date: 2004/05/15 02:21:47 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.proj;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * The ProjectionFactory creates Projections.  It used to have
 * Projection classes hard-coded into it which were accessable through
 * static methods, but this paradigm has been changed slightly so the
 * ProjectionFactory is actually a singleton which can be accessed by
 * a ProjectionFactory.getInstance() static method.  This was done so
 * that the singleton, when added to the MapHandler, can look for
 * ProjectionLoaders to dynamically add projections to the factory.
 * For convenience, there are still some static methods on the
 * ProjectionFactory class which use the singleton to create and
 * return projection objects.  Changes to the available projections
 * can be discovered through property changes.<P>
 *
 * The ProjectionFactory singleton instance can be added to the
 * MapHandler via the openmap.properties file by adding the
 * ProjectionFactoryLoader to the openmap.components property.  If you
 * are using the openmap.properties file to configure your
 * application, you MUST add the ProjectionFactoryLoader, or you won't
 * see any projections.
 */
public class ProjectionFactory extends OMComponent {

    /** Center lat/lon property parameter for new projections passed to ProjectionLoader. */
    public final static String CENTER = "CENTER";
    /** Scale property parameter for new projections passed to ProjectionLoader. */
    public final static String SCALE = "SCALE";
    /** 
     * Projeciton height (pixels) property parameter for new
     * projections passed to ProjectionLoader. 
     */
    public final static String HEIGHT = "HEIGHT";
    /**
     * Projection width (pixels) property parameter for new projections
     * passed to ProjectionLoader. 
     */
    public final static String WIDTH = "WIDTH";
    /**
     * Datum property parameter for new projections passed to ProjectionLoader. 
     */
    public final static String DATUM = "DATUM";

    /**
     * The property name that is fired when the list of available
     * projections has changed.
     */
    public final static String AvailableProjectionProperty = "AvailableProjections";

    /**
     * PropertyChangeSupport for letting listeners know about new
     * projections that are available from the factory.
     */
    protected PropertyChangeSupport pcs;

    /**
     * Singleton instance.
     */
    protected static ProjectionFactory instance;

    protected Vector projLoaders = new Vector();

    /**
     * Singleton constructor.
     */
    private ProjectionFactory() {
	pcs = new PropertyChangeSupport(this);
	projLoaders = new Vector();
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
     * Returns an array of Projection names available from this factory.
     */
    public static String[] getAvailableProjections() {
        ProjectionFactory factory = getInstance();

	int nProjections = factory.numProjections();
	String projNames[] = new String[nProjections];
        int i = 0;
        for (Iterator it = factory.iterator(); it.hasNext();
             projNames[i++] = ((ProjectionLoader)it.next()).getPrettyName());

	return projNames;
    }

    /** 
     * Return an int representing the OpenMap projection, given the
     * name of the projection. Useful for setting a projection based
     * on the name stated in a properties file.
     * @param projName the projection name from the Projection class.
     * @return the projection type number for that name.
     * @deprecated The notion of a projection type number is going
     * away, use the class of the projection instead.
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
     * Makes a new projection based on the given projection and given type.
     * <p>
     * The <code>centerLat</code>, <code>centerLon</code>, <code>scale</code>,
     * <code>width</code>, and <code>height</code> parameters are taken from
     * the given projection, and the type is taken from the type argument.
     * @param newProjType the type for the resulting projection
     * @param p the projection from which to copy other parameters
     * @deprecated The notion of a projection type number is going
     * away, use the class of the projection instead.
     */
    public static Projection makeProjection(int newProjType, Projection p) {
	LatLonPoint ctr = p.getCenter();
	return makeProjection(newProjType,
			      ctr.getLatitude(), ctr.getLongitude(),
			      p.getScale(), p.getWidth(), p.getHeight());
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
     * away, use the class of the projection instead.
     */
    public static Projection makeProjection(int projType,
					    float centerLat,
					    float centerLon,
					    float scale,
					    int width,
					    int height)
    {

	Class projClass = null;
	switch (projType) {
	case CADRG.CADRGType:
	    projClass = com.bbn.openmap.proj.CADRG.class;
	    break;
	case Mercator.MercatorType:
	    projClass = com.bbn.openmap.proj.Mercator.class;
	    break;
	case MercatorView.MercatorViewType:
	    projClass = com.bbn.openmap.proj.MercatorView.class;
	    break;
	case LLXY.LLXYType:
	    projClass = com.bbn.openmap.proj.LLXY.class;
	    break;
	case LLXYView.LLXYViewType:
	    projClass = com.bbn.openmap.proj.LLXYView.class;
	    break;
	case Orthographic.OrthographicType:
	    projClass = com.bbn.openmap.proj.Orthographic.class;
	    break;
	case OrthographicView.OrthographicViewType:
	    projClass = com.bbn.openmap.proj.OrthographicView.class;
	    break;
	case Gnomonic.GnomonicType:
	    projClass = com.bbn.openmap.proj.Gnomonic.class;
	    break;
	default:
	    System.err.println("Unknown projection type " + projType +
			       " in ProjectionFactory.create()");
	}

	return makeProjection(projClass, centerLat, centerLon, 
			      scale, width, height);
    }

    /**
     * Makes a new projection based on the given projection class name and
     * parameters from the given projection.
     */
    public static Projection makeProjection(String projClassName, Projection p) {

	LatLonPoint ctr = p.getCenter();
	return makeProjection(projClassName,
			      ctr.getLatitude(), ctr.getLongitude(),
			      p.getScale(), p.getWidth(), p.getHeight());
    }

    /**
     * Create a projection. If the Class for the classname can't be
     * found, a Mercator projection will be returned.
     * @param projClassName the classname of the projection.
     * @param centerLat center latitude in decimal degrees.
     * @param centerLon center latitude in decimal degrees.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @return Projection
     */
    public static Projection makeProjection(String projClassName,
					    float centerLat,
					    float centerLon,
					    float scale,
					    int width,
					    int height) {

        try {
            return makeProjection(Class.forName(projClassName),
                                  centerLat, centerLon,
                                  scale, width, height);
        } catch (ClassNotFoundException cnfe) {
            throw new ProjectionException("Projection class " + projClassName + " not found");
        }
    }

    /**
     * Create a projection.  If the class can't be found, a Mercator
     * projection will be returned.
     * @param projClass the class of the projection.
     * @param centerLat center latitude in decimal degrees.
     * @param centerLon center latitude in decimal degrees.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @return Projection
     */
    public static Projection makeProjection(Class projClass,
					    float centerLat,
					    float centerLon,
					    float scale,
					    int width,
					    int height) {

        ProjectionFactory factory = getInstance();

        ProjectionLoader loader = MercatorLoader.defaultMercator;

        for (Iterator it = factory.iterator(); it.hasNext(); ) {
            ProjectionLoader pl = (ProjectionLoader) it.next();
            if (pl.getProjectionClass() == projClass) {
                loader = pl;
            }
        }

        return factory.makeProjection(loader, centerLat, centerLon, scale, width, height);
    }

    /**
     * Call the provided ProjectionLoader to create the projection
     * with the given parameters.  The parameters are converted to
     * Properties before being passed to the ProjectionLoader.
     * @param centerLat center latitude in decimal degrees.
     * @param centerLon center latitude in decimal degrees.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     */
    public Projection makeProjection(ProjectionLoader loader,
                                     float centerLat,
                                     float centerLon,
                                     float scale,
                                     int width,
                                     int height) {
        return makeProjection(loader, centerLat, centerLon, scale, width, height, null);
    }

    /**
     * Call the provided ProjectionLoader to create the projection
     * with the given parameters.  The parameters are converted to
     * Properties before being passed to the ProjectionLoader.  The
     * ProjectionLoader should throw a ProjectionException from here
     * if it has a problem creating the projection with the provided
     * parameters.
     *
     * @param centerLat center latitude in decimal degrees.
     * @param centerLon center latitude in decimal degrees.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @param projProperties a Properties object to add the parameters
     * to, which can include extra parameters that are needed by this
     * particular projection loader.  If null, a Properties object
     * will be created.
     * @return projection, or null if the projection can't be created.
     */
    public Projection makeProjection(ProjectionLoader loader,
                                     float centerLat,
                                     float centerLon,
                                     float scale,
                                     int width,
                                     int height, 
                                     Properties projProps) {

	Projection proj = null;
        if (loader == null) {
	    Debug.error("ProjectionFactory.makeProjection() not given a ProjectionLoader to use to create a Projection");
            return proj;
        }
	    
        if (projProps == null) {
            projProps = new Properties();
        }

        projProps.put(CENTER, new LatLonPoint(centerLat, centerLon)); 
        projProps.put(SCALE, Float.toString(scale));
        projProps.put(WIDTH, Integer.toString(width));
        projProps.put(HEIGHT, Integer.toString(height));

        proj = loader.create(projProps);
        
	if (proj == null) {
	    Debug.error("ProjectionFactory.makeProjection() tried to create a Projection from a " + 
                        loader.getPrettyName() + ", " + loader.getProjectionClass().getName() + 
                        ", failed.");
	}

	return proj;
    }

    public void addProjectionLoader(ProjectionLoader loader) {
        projLoaders.add(loader);
        fireLoadersChanged();
    }

    public boolean removeProjectionLoader(ProjectionLoader loader) {
        boolean removed = projLoaders.remove(loader);
        if (removed) {
            fireLoadersChanged();
        }
        return removed;
    }

    public void clearProjectionLoaders() {
        if (projLoaders.size() > 0) {
            projLoaders.clear();
            fireLoadersChanged();
        }
    }

    public Iterator iterator() {
        return projLoaders.iterator();
    }

    public int numProjections() {
        return projLoaders.size();
    }
    
    protected void fireLoadersChanged() {
        pcs.firePropertyChange(AvailableProjectionProperty, null, projLoaders);
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

    /**
     * Using the MapHandler to find ProjectionLoaders being added
     * from the application.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof ProjectionLoader) {
            addProjectionLoader((ProjectionLoader)obj);
        }
    }

    /**
     * Using the MapHandler to find ProjectionLoaders being removed
     * from the application.
     */
    public void findAndUndo(Object obj) {
        if (obj instanceof ProjectionLoader) {
            removeProjectionLoader((ProjectionLoader)obj);
        }
    }
}
