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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionFactory.java,v $
// $RCSfile: ProjectionFactory.java,v $
// $Revision: 1.15 $
// $Date: 2007/04/17 20:25:08 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.SoloMapComponent;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The ProjectionFactory creates Projections. It used to have Projection classes
 * hard-coded into it which were accessible through static methods, but this
 * paradigm has been changed slightly so the ProjectionFactory is a
 * SoloMapComponent added to the MapHandler. It will attach itself to a MapBean
 * if it finds one in the MapHandler.
 * <P>
 * 
 * The ProjectionFactory can look for ProjectionLoaders in the MapHandler to
 * dynamically add projections to the factory. The ProjectionHandler can also
 * create ProjectionLoaders from property settings. Changes to the available
 * projections can be discovered through property changes.
 * <P>
 */
public class ProjectionFactory extends OMComponent implements SoloMapComponent {

    /**
     * Center lat/lon property parameter for new projections passed to
     * ProjectionLoader.
     */
    public final static String CENTER = "CENTER";
    /**
     * Scale property parameter for new projections passed to ProjectionLoader.
     */
    public final static String SCALE = "SCALE";
    /**
     * Projection height (pixels) property parameter for new projections passed
     * to ProjectionLoader.
     */
    public final static String HEIGHT = "HEIGHT";
    /**
     * Projection width (pixels) property parameter for new projections passed
     * to ProjectionLoader.
     */
    public final static String WIDTH = "WIDTH";
    /**
     * Datum property parameter for new projections passed to ProjectionLoader.
     */
    public final static String DATUM = "DATUM";

    /**
     * The property name that is fired when the list of available projections
     * has changed.
     */
    public final static String AvailableProjectionProperty = "AvailableProjections";

    /**
     * ProjectionFactory property used to designate new projection loaders that
     * should be created from properties.
     */
    public final static String ProjectionLoadersProperty = "projectionLoaders";

    /**
     * PropertyChangeSupport for letting listeners know about new projections
     * that are available from the factory.
     */
    protected PropertyChangeSupport pcs;

    // /**
    // * Singleton instance.
    // */
    // protected static ProjectionFactory instance;

    protected Vector<ProjectionLoader> projLoaders = new Vector<ProjectionLoader>();

    /**
     * 
     */
    public ProjectionFactory() {
        pcs = new PropertyChangeSupport(this);
    }

    /**
     * Check the properties for those to create ProjectionLoaders.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String loaderPrefixesString = props.getProperty(prefix
                + ProjectionLoadersProperty);
        if (loaderPrefixesString != null) {
            Vector<String> loaderPrefixes = PropUtils.parseSpacedMarkers(loaderPrefixesString);
            Vector<?> loaders = ComponentFactory.create(loaderPrefixes,
                    prefix,
                    props);

            for (Iterator<?> it = loaders.iterator(); it.hasNext();) {
                Object obj = it.next();
                if (obj instanceof ProjectionLoader) {
                    projLoaders.add((ProjectionLoader) obj);
                }
            }
        }
    }

    /**
     * Create the properties to create ProjectionLoaders that this loader
     * created.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        if (projLoaders != null) {
            StringBuffer markerlist = new StringBuffer();
            int count = 0;
            for (Iterator<ProjectionLoader> it = projLoaders.iterator(); it.hasNext();) {

                ProjectionLoader pl = it.next();
                String markerName;
                if (pl instanceof PropertyConsumer) {

                    PropertyConsumer pc = (PropertyConsumer) pl;
                    markerName = PropUtils.getScopedPropertyPrefix(pc.getPropertyPrefix());

                    // Need to do this here before the marker name
                    // potentially changes
                    props.put(markerName + "class", pl.getClass().getName());

                    if (markerName.startsWith(prefix)) {
                        markerName = markerName.substring(prefix.length());
                    }
                    pc.getProperties(props);

                } else {

                    markerName = "projectionLoader" + (count++);
                    // Need to do this here for any projection loaders
                    // that aren't property consumers.
                    props.put(markerName + ".class", pl.getClass().getName());

                }

                markerlist.append(" ").append(markerName);
            }

            props.put(prefix + ProjectionLoadersProperty, markerlist.toString());
        }

        return props;
    }

    // /**
    // * Get the singleton instance of the ProjectionFactory.
    // */
    // public static ProjectionFactory getInstance() {
    // if (instance == null) {
    // instance = new ProjectionFactory();
    // }
    //
    // return instance;
    // }

    /**
     * Returns an array of Projection names available from this factory.
     */
    public String[] getAvailableProjections() {
        // duplicate List to handle multiple simultaneous requests
        List<ProjectionLoader> projLoaders = new ArrayList<ProjectionLoader>(getProjectionLoaders());
        int nProjections = projLoaders.size();
        String projNames[] = new String[nProjections];
        int i = 0;

        for (Iterator<ProjectionLoader> it = projLoaders.iterator(); it.hasNext();) {
            projNames[i++] = it.next().getPrettyName();
        }

        return projNames;
    }

    /**
     * Return the Projection Class with the given pretty name.
     * 
     * @param name the name of the projection, set in the pretty name of it's
     *        ProjectionLoader.
     * @return Class of Projection, or null if not found.
     */
    public Class<? extends Projection> getProjClassForName(String name) {
        if (name != null) {
            for (Iterator<ProjectionLoader> it = getProjectionLoaders().iterator(); it.hasNext();) {
                ProjectionLoader loader = it.next();
                if (name.equalsIgnoreCase(loader.getPrettyName())) {
                    return loader.getProjectionClass();
                }
            }

            // If there wasn't a class with the pretty name, check to
            // make sure it wasn't a class name itself. If it fails,
            // return null. We just want to do this in case people
            // start using class names for pretty names.
            try {
                return (Class<? extends Projection>) Class.forName(name);
            } catch (ClassNotFoundException cnfe) {
            }

        }
        return null;
    }

    /**
     * Makes a new projection based on the given projection class name and
     * parameters from the given projection.
     */
    public Projection makeProjection(String projClassName, Projection p) {

        Point2D ctr = p.getCenter();
        return makeProjection(projClassName,
                ctr,
                p.getScale(),
                p.getWidth(),
                p.getHeight());
    }

    /**
     * Create a projection. If the Class for the classname can't be found, a
     * Mercator projection will be returned.
     * 
     * @param projClassName the classname of the projection.
     * @param center Point2D center of the projection.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @return Projection
     */
    public Projection makeProjection(String projClassName, Point2D center,
                                     float scale, int width, int height) {

        if (projClassName == null) {
            throw new ProjectionException("No projection class name specified");
        }

        try {
            return makeProjection((Class<? extends Projection>) Class.forName(projClassName),
                    center,
                    scale,
                    width,
                    height);
        } catch (ClassNotFoundException cnfe) {
            throw new ProjectionException("Projection class " + projClassName
                    + " not found");
        }
    }

    /**
     * Create a projection. If the class can't be found, a Mercator projection
     * will be returned.
     * 
     * @param projClass the class of the projection.
     * @param center Point2D center of the projection.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @return Projection
     */
    public Projection makeProjection(Class<? extends Projection> projClass,
                                     Point2D center, float scale, int width,
                                     int height) {

        ProjectionLoader loader = MercatorLoader.defaultMercator;

        for (Iterator<ProjectionLoader> it = iterator(); it.hasNext();) {
            ProjectionLoader pl = (ProjectionLoader) it.next();
            if (pl.getProjectionClass() == projClass) {
                loader = pl;
            }
        }

        return makeProjection(loader, center, scale, width, height);
    }

    /**
     * Looks at the Environment settings for the default projection and returns
     * a Projection suited for those settings. If there is a problem creating
     * the projection, the default projection of the MapBean will be returned.
     * The ProjectionFactory needs to be loaded with the Projection class
     * described in the properties before this will return an expected
     * projection.
     * 
     * @return Projection from Environment settings.
     */
    public Projection getDefaultProjectionFromEnvironment(Environment e) {
        return getDefaultProjectionFromEnvironment(e, 0, 0);
    }

    /**
     * Looks at the Environment settings for the default projection and returns
     * a Projection suited for those settings. If there is a problem creating
     * the projection, the default projection of the MapBean will be returned.
     * The ProjectionFactory needs to be loaded with the Projection class
     * described in the properties before this will return an expected
     * projection.
     * 
     * @param width pixel height of projection. If 0 or less, the
     *        Environment.Width value will be used.
     * @param height pixel height of projection. If 0 or less, the
     *        Environment.Height value will be used.
     * @return Projection from Environment settings, fit for the pixel height
     *         and width provided.
     */
    public Projection getDefaultProjectionFromEnvironment(
                                                          Environment environment,
                                                          int width, int height) {
        // Initialize the map projection, scale, center
        // with user prefs or defaults
        Projection proj = null;

        int w = (width <= 0) ? Environment.getInteger(Environment.Width,
                MapBean.DEFAULT_WIDTH) : width;
        int h = (height <= 0) ? Environment.getInteger(Environment.Height,
                MapBean.DEFAULT_HEIGHT) : height;

        try {
            proj = makeProjection(Environment.get(Environment.Projection),
                    new LatLonPoint.Float(environment.getFloat(Environment.Latitude,
                            0f), environment.getFloat(Environment.Longitude, 0f)),
                    environment.getFloat(Environment.Scale,
                            Float.POSITIVE_INFINITY),
                    w,
                    h);

        } catch (com.bbn.openmap.proj.ProjectionException pe) {
            if (Debug.debugging("proj")) {
                Debug.output("ProjectionFactory.getDefaultProjectionFromEnvironment(): Can't use ("
                        + Environment.Projection
                        + " = "
                        + Environment.get(Environment.Projection)
                        + ") property as a projection class, need a class name instead.  Using default of com.bbn.openmap.proj.Mercator.");
            }
            proj = makeProjection(Mercator.class,
                    new LatLonPoint.Float(environment.getFloat(Environment.Latitude,
                            0f), environment.getFloat(Environment.Longitude, 0f)),
                    environment.getFloat(Environment.Scale,
                            Float.POSITIVE_INFINITY),
                    w,
                    h);
        }

        return proj;
    }

    /**
     * Call the provided ProjectionLoader to create the projection with the
     * given parameters. The parameters are converted to Properties before being
     * passed to the ProjectionLoader.
     * 
     * @param loader ProjectionLoader for projection type
     * @param center center coordinate for projection
     * @param scale scale for projection
     * @param width pixel width of projection
     * @param height pixel height of projection
     * @return Projection
     */
    public Projection makeProjection(ProjectionLoader loader, Point2D center,
                                     float scale, int width, int height) {
        return makeProjection(loader, center, scale, width, height, null);
    }

    /**
     * Call the provided ProjectionLoader to create the projection with the
     * given parameters. The parameters are converted to Properties before being
     * passed to the ProjectionLoader. The ProjectionLoader should throw a
     * ProjectionException from here if it has a problem creating the projection
     * with the provided parameters.
     * 
     * @param loader projection loader to use.
     * @param center Point2D center of the projection.
     * @param scale float scale.
     * @param width pixel width of projection.
     * @param height pixel height of projection.
     * @param projProps a Properties object to add the parameters to, which can
     *        include extra parameters that are needed by this particular
     *        projection loader. If null, a Properties object will be created.
     * @return projection, or null if the projection can't be created.
     */
    public Projection makeProjection(ProjectionLoader loader, Point2D center,
                                     float scale, int width, int height,
                                     Properties projProps) {

        Projection proj = null;
        if (loader == null) {
            Debug.error("ProjectionFactory.makeProjection() not given a ProjectionLoader to use to create a Projection");
            return proj;
        }

        if (projProps == null) {
            projProps = new Properties();
        }

        projProps.put(CENTER, center);
        projProps.put(SCALE, Float.toString(scale));
        projProps.put(WIDTH, Integer.toString(width));
        projProps.put(HEIGHT, Integer.toString(height));

        proj = loader.create(projProps);

        if (proj == null) {
            Debug.error("ProjectionFactory.makeProjection() tried to create a Projection from a "
                    + loader.getPrettyName()
                    + ", "
                    + loader.getProjectionClass().getName() + ", failed.");
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
        if (!projLoaders.isEmpty()) {
            projLoaders.clear();
            fireLoadersChanged();
        }
    }

    public Iterator<ProjectionLoader> iterator() {
        return projLoaders.iterator();
    }

    public int numProjections() {
        return projLoaders.size();
    }

    public Collection<ProjectionLoader> getProjectionLoaders() {
        return Collections.unmodifiableCollection(projLoaders);
    }

    protected void fireLoadersChanged() {
        pcs.firePropertyChange(AvailableProjectionProperty, null, projLoaders);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (pcl != null) {
            pcs.addPropertyChangeListener(pcl);
            pcl.propertyChange(new PropertyChangeEvent(this, AvailableProjectionProperty, null, projLoaders));
        }
    }

    public void addPropertyChangeListener(String propertyName,
                                          PropertyChangeListener pcl) {
        if (pcl != null) {
            pcs.addPropertyChangeListener(propertyName, pcl);
            pcl.propertyChange(new PropertyChangeEvent(this, AvailableProjectionProperty, null, projLoaders));
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(String propertyName,
                                             PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(propertyName, pcl);
    }

    /**
     * Using the MapHandler to find ProjectionLoaders being added from the
     * application.
     */
    public void findAndInit(Object obj) {
        if (obj instanceof ProjectionLoader) {
            addProjectionLoader((ProjectionLoader) obj);
        }

        if (obj instanceof MapBean) {
            ((MapBean) obj).setProjectionFactory(this);
        }
    }

    /**
     * Using the MapHandler to find ProjectionLoaders being removed from the
     * application.
     */
    public void findAndUndo(Object obj) {
        if (obj instanceof ProjectionLoader) {
            removeProjectionLoader((ProjectionLoader) obj);
        }
    }

    /**
     * Convenience method to load a ProjectionFactory and default projections
     * into the provided MapHandler.
     * 
     * @param mapHandler the MapHandler to receive the default ProjectionFactory
     */
    public static void loadDefaultProjections(MapHandler mapHandler) {
        mapHandler.add(loadDefaultProjections(new ProjectionFactory()));
    }

    /**
     * Convenience method to load default projections into a ProjectionFactory
     * that will be created.
     * 
     * @return ProjectionFactory
     */
    public static ProjectionFactory loadDefaultProjections() {
        return loadDefaultProjections(new ProjectionFactory());
    }

    /**
     * Convenience method to load default projections into a ProjectionFactory.
     * 
     * @param pf
     * @return ProjectionFactory
     */
    public static ProjectionFactory loadDefaultProjections(ProjectionFactory pf) {
        if (pf != null && pf.numProjections() == 0) {
            pf.addProjectionLoader(new com.bbn.openmap.proj.MercatorLoader());
            pf.addProjectionLoader(new com.bbn.openmap.proj.OrthographicLoader());
            pf.addProjectionLoader(new com.bbn.openmap.proj.CADRGLoader());
            pf.addProjectionLoader(new com.bbn.openmap.proj.LLXYLoader());
            pf.addProjectionLoader(new com.bbn.openmap.proj.GnomonicLoader());
            pf.addProjectionLoader(new com.bbn.openmap.proj.CartesianLoader());
        }
        return pf;
    }
}