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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/dted/DTEDDirectoryHandler.java,v $
// $RCSfile: DTEDDirectoryHandler.java,v $
// $Revision: 1.3 $
// $Date: 2007/02/26 16:41:51 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.dted;

import java.util.Properties;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * DTEDDirectoryHandler is a wrapper for a DTEDNameTranslator used for a DTED
 * directory. The DTEDNameTranslator has the intelligence to know how file names
 * and paths are represented for different lat/lon/level combinations for DTED
 * files within a directory.
 */
public class DTEDDirectoryHandler implements PropertyConsumer {

    /**
     * The property for the path to the DTED directory.
     */
    public final static String PathProperty = "path";

    /**
     * The class name for the DTEDNameTranslator to be used for this path. If
     * none is provided, the StandardDTEDNameTranslator will be used, which
     * follows the DTED specification.
     */
    public final static String TranslatorClassProperty = "translator";

    /**
     * Property prefix for properties, used as a scoping mechanism.
     */
    protected String prefix;

    /**
     * The DTEDNameTranslator for this handler, which knows how to match up
     * lat/lons with a DTED file name. A StandardDTEDNameTranslator is set
     * initially, but the actual object can be reset programmatically or with
     * properties.
     */
    protected DTEDNameTranslator translator = new StandardDTEDNameTranslator();

    public DTEDDirectoryHandler() {}

    /**
     * Create a DTEDDirectoryHandler with a StandardDTEDNameTranslator, looking
     * at the provided dted directory.
     * 
     * @param dtedDirectory
     */
    public DTEDDirectoryHandler(String dtedDirectory) {
        setDirectoryPath(dtedDirectory);
    }

    /**
     * Set the DTEDNameTranslator for this DTEDDirectoryHandler.
     */
    public void setTranslator(DTEDNameTranslator dnt) {
        translator = dnt;
    }

    /**
     * Get the translator for this DTEDDirectoryHandler.
     */
    public DTEDNameTranslator getTranslator() {
        return translator;
    }

    /**
     * Sets the DTED directory path on the translator if it isn't null. If the
     * translator is null, this method does nothing.
     */
    public void setDirectoryPath(String path) {
        if (translator != null) {
            translator.setDTEDDir(path);
        }
    }

    /**
     * Gets the directory path from the translator. Will return null if the
     * translator is null.
     */
    public String getDirectoryPath() {
        if (translator != null) {
            return translator.getDTEDDir();
        }
        return null;
    }

    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);

        String scopedPrefix = PropUtils.getScopedPropertyPrefix(prefix);
        String path = props.getProperty(scopedPrefix + PathProperty);
        String translatorClassName = props.getProperty(scopedPrefix
                + TranslatorClassProperty);

        if (translatorClassName != null) {
            Object obj = ComponentFactory.create(translatorClassName,
                    prefix,
                    props);
            if (obj instanceof DTEDNameTranslator) {
                translator = (DTEDNameTranslator) obj;
            }
        } else if (translator == null) {
            translator = new StandardDTEDNameTranslator();
        }

        if (Debug.debugging("dtedfile")) {
            Debug.output("DTEDDirectoryHandler|" + prefix + ": "
                    + translator.getClass().getName() + " using " + path);
        }

        setDirectoryPath(path);
    }

    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + PathProperty, PropUtils.unnull(getDirectoryPath()));
        if (translator != null) {
            props.put(prefix + PathProperty, translator.getClass().getName());
            if (translator instanceof PropertyConsumer) {
                ((PropertyConsumer) translator).getProperties(props);
            }
        }

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        props.put(PathProperty, "Path to DTED directory");
        props.put(TranslatorClassProperty,
                "Class name of DTEDNameTranslator to use for directory");
        if (translator instanceof PropertyConsumer) {
            ((PropertyConsumer) translator).getPropertyInfo(props);
        }

        return props;
    }

    public void setPropertyPrefix(String propertyPrefix) {
        prefix = propertyPrefix;
    }

    public String getPropertyPrefix() {
        return prefix;
    }

}