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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/BasicProjectionLoader.java,v $
// $RCSfile: BasicProjectionLoader.java,v $
// $Revision: 1.6 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.I18n;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * The BasicProjectionLoader is a base implementation of the
 * ProjectionLoader interface that conveniently handles the
 * PropertyConsumer methods for the ProjectionLoader. There are two
 * basic properties built into this base class that can be modified in
 * a properties file. You can add ProjectionLoaders to the MapHandler,
 * and if the ProjectionFactory singleton instance has been added to
 * it as well, it will be picked up and the projection made available
 * to the application. For example, the BasicProjectionFactory allows
 * you to set thse properties for the Mercator projection:
 * 
 * <pre>
 *   
 *    
 *    
 *     projLoader.class=com.bbn.openmap.proj.MercatorLoader
 *     projLoader.prettyName=Mercator
 *     projLoader.description=Mercator Projection.
 *    
 *     
 *    
 * </pre>
 * 
 * The prettyName and description properties should have defaults, but
 * internationalized strings can be substituted as needed.
 */
public abstract class BasicProjectionLoader extends OMComponent implements
        ProjectionLoader {

    protected Class<? extends Projection> projClass;
    protected String prettyName;
    protected String description;

    public final static String PrettyNameProperty = "prettyName";
    public final static String DescriptionProperty = "description";

    /**
     * Set the basic parameters needed for a ProjectionLoader.
     */
    public BasicProjectionLoader(Class<? extends Projection> pClass, String pName, String pDescription) {
        projClass = pClass;
        prettyName = pName;
        description = pDescription;
    }

    /**
     * Get a class name to use for the projection. This will be used
     * as a key in the projection factory.
     */
    public Class<? extends Projection> getProjectionClass() {
        return projClass;
    }

    /**
     * Get a pretty name for the projection.
     */
    public String getPrettyName() {
        return prettyName;
    }

    /**
     * Set a pretty name for the projection.
     */
    public void setPrettyName(String pn) {
        prettyName = pn;
    }

    /**
     * Get a description for the projection.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set a description for the projection.
     */
    public void setDescription(String desc) {
        description = desc;
    }

    /**
     * Create the projection with the given parameters.
     * 
     * @throws exception if a parameter is missing or invalid.
     */
    public abstract Projection create(Properties props)
            throws ProjectionException;

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        prettyName = props.getProperty(prefix + PrettyNameProperty, prettyName);
        description = props.getProperty(prefix + DescriptionProperty,
                description);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + PrettyNameProperty, prettyName);
        props.put(prefix + DescriptionProperty, description);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        String internString = i18n.get(BasicProjectionLoader.class,
                PrettyNameProperty,
                "Projection Name");
        props.put(PrettyNameProperty + LabelEditorProperty, internString);

        internString = i18n.get(BasicProjectionLoader.class,
                PrettyNameProperty,
                I18n.TOOLTIP,
                "Presentable name for Projection");
        props.put(PrettyNameProperty, internString);

        internString = i18n.get(BasicProjectionLoader.class,
                DescriptionProperty,
                "Projection Description");
        props.put(DescriptionProperty + LabelEditorProperty, internString);

        internString = i18n.get(ProjectionLoader.class,
                DescriptionProperty,
                I18n.TOOLTIP,
                "Presentable description name for Projection");
        props.put(DescriptionProperty, internString);

        props.put(initPropertiesProperty, PrettyNameProperty + " "
                + DescriptionProperty);

        return props;
    }

    public LatLonPoint convertToLLP(Point2D pt) {
        return LatLonPoint.getDouble(pt);
    }

}