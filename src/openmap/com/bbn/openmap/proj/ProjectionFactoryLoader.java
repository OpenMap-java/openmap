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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionFactoryLoader.java,v $
// $RCSfile: ProjectionFactoryLoader.java,v $
// $Revision: 1.1 $
// $Date: 2004/05/15 02:21:47 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.proj;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

import java.beans.beancontext.BeanContext;
import java.beans.PropertyVetoException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

/**
 * All this class does is add the instance of the ProjectionFactory to
 * the MapHandler.  This class needs to be added to the MapHandler in
 * order for a ProjectionFactory to be created and the
 * ProjectionLoaders to be recognized by the application. To keep
 * multiple ProjectionLoaders out of the openmap.components property
 * for readability, this class has a property that lets you set which
 * ProjectionLoaders are created and added to the application:<pre>
 *
 * # For a projFactoryLoader marker name added to the openmap.components property.
 * projFactoryLoader.class=com.bbn.openmap.proj.ProjectionFactoryLoader
 * projFactoryLoader.class=com.bbn.openmap.proj.ProjectionFactoryLoader
 * projFactoryLoader.projectionLoaders=mercatorloader cadrgloader ortholoader llxyloader gnomonicloader
 * projFactoryLoader.mercatorloader.class=com.bbn.openmap.proj.MercatorLoader
 * projFactoryLoader.cadrgloader.class=com.bbn.openmap.proj.CADRGLoader
 * projFactoryLoader.ortholoader.class=com.bbn.openmap.proj.OrthographicLoader
 * projFactoryLoader.llxyloader.class=com.bbn.openmap.proj.LLXYLoader
 * projFactoryLoader.gnomonicloader.class=com.bbn.openmap.proj.GnomonicLoader
 *
 * </pre> The above properties create 5 ProjectionLoaders which are
 * added to the MapHandler, along with the ProjectionFactory singleton
 * instance.  The ProjectionLoader may have additional properties
 * which can be set as well, like their pretty GUI name and
 * description.
 */
public class ProjectionFactoryLoader extends OMComponent {

    protected Vector loaders;

    public final static String ProjectionLoadersProperty = "projectionLoaders";

    public ProjectionFactoryLoader() {
        super();
    }

    /**
     * When the BeanContext (MapHandler) gets set, the singleton
     * instance of the ProjectionFactory is created (asked for) and
     * added to the BeanContext.
     */
    public void setBeanContext(BeanContext in_bc) 
        throws PropertyVetoException {

        if (in_bc != null) {
            in_bc.add(ProjectionFactory.getInstance());

            if (loaders != null && loaders.size() > 0) {
                for (Iterator it = loaders.iterator(); it.hasNext(); in_bc.add(it.next())) {}
            }
        }
    }

    /**
     * Check the properties for those to create ProjectionLoaders.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String loaderPrefixesString = props.getProperty(prefix + ProjectionLoadersProperty);
        if (loaderPrefixesString != null) {
            Vector loaderPrefixes = PropUtils.parseSpacedMarkers(loaderPrefixesString);
            loaders = ComponentFactory.create(loaderPrefixes, prefix, props);
        }
    }

    /**
     * Create the properties to create ProjectionLoaders that this loader created.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        if (loaders != null) {
            StringBuffer markerlist = new StringBuffer();
            int count = 0;
            for (Iterator it = loaders.iterator(); it.hasNext();) {

                ProjectionLoader pl = (ProjectionLoader) it.next();
                String markerName;
                if (pl instanceof PropertyConsumer) {

                    PropertyConsumer pc = (PropertyConsumer)pl;
                    markerName = pc.getPropertyPrefix();

                    // Need to do this here before the marker name
                    // potentially changes
                    props.put(markerName + ".class", pl.getClass().getName());

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

                markerlist.append(" " + markerName);
            }

            props.put(prefix + ProjectionLoadersProperty, markerlist.toString());
        }

        return props;
    }

    /**
     * Create the property information reflecting those used to create loaders.
     */
    public Properties getPropertyInfo(Properties props) {
        // We don't handle this yet, because there isn't a good
        // mechanism for handling embedded objects defined in
        // properties for dynmaic property setting.
        return super.getPropertyInfo(props);
    }


}
