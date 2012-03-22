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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/AbstractToolLoader.java,v $
// $RCSfile: AbstractToolLoader.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.util.HashMap;

import javax.swing.ImageIcon;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.util.Debug;

/**
 * The AbstractToolLoader groups together some of the code that was
 * being duplicated in the different EditToolLoaders. It uses a
 * EditClassWrapper to describe how to edit/create/represent graphics,
 * and keeps track of multiple EditClassWrappers in case the
 * EditToolLoader can handle multiple graphic types.
 */
public abstract class AbstractToolLoader implements EditToolLoader {

    protected HashMap graphicInfo;
    protected I18n i18n = Environment.getI18n();

    /**
     * Each subclass should initialize the graphicInfo HashMap by
     * calling addEditClassWrapper(EditClassWrapper).
     */
    public abstract void init();

    public void addEditClassWrapper(EditClassWrapper ecw) {
        if (graphicInfo == null) {
            graphicInfo = new HashMap();
        }

        if (ecw != null) {
            graphicInfo.put(ecw.getClassName().intern(), ecw);
        }
    }

    public void removeEditClassWrapper(EditClassWrapper ecw) {
        if (graphicInfo != null && ecw != null) {
            graphicInfo.remove(ecw.getClassName().intern());
        }
    }

    /**
     * Get the classnames that the loader is able to create
     * EditableOMGraphics for.
     */
    public String[] getEditableClasses() {
        String[] strings = null;
        if (graphicInfo != null) {
            Object[] keys = graphicInfo.keySet().toArray();
            strings = new String[keys.length];
            for (int i = 0; i < keys.length; i++) {
                strings[i] = (String) keys[i];
            }
        }
        return strings;
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic.
     */
    public EditableOMGraphic getEditableGraphic(String classname) {
        EditableOMGraphic eomg = null;

        if (graphicInfo != null) {
            EditClassWrapper ecw = (EditClassWrapper) graphicInfo.get(classname.intern());
            if (ecw != null) {
                String ecn = ecw.getEditableClassName();
                try {
                    Object obj = Class.forName(ecn).newInstance();
                    if (obj instanceof EditableOMGraphic) {
                        eomg = (EditableOMGraphic) obj;
                    }
                } catch (ClassNotFoundException cnfe) {
                    Debug.error("AbstractToolLoader can't get editable graphic for "
                            + classname
                            + "\n    ClassNotFoundException caught.");
                } catch (InstantiationException ie) {
                    Debug.error("AbstractToolLoader can't get editable graphic for "
                            + classname
                            + "\n    InstantiationException caught.");
                } catch (IllegalAccessException iae) {
                    Debug.error("AbstractToolLoader can't get editable graphic for "
                            + classname
                            + "\n    IllegalAccessException caught.");
                }
            }
        }

        return eomg;
    }

    /**
     * Give the classname of a graphic to create, returning an
     * EditableOMGraphic for that graphic. The GraphicAttributes
     * object lets you set some of the initial parameters of the
     * point, like point type and rendertype.
     */
    public EditableOMGraphic getEditableGraphic(String classname,
                                                GraphicAttributes ga) {
        EditableOMGraphic eomg = getEditableGraphic(classname);
        if (eomg != null && ga != null) {
            // This is a little redundant - the graphic is created
            // with the call to getEditableGraphic(classname), but is
            // then destroyed and created again with the
            // GraphicAttributes settings. I'm not sure how to get
            // around this at this point in a generic fashion. Before
            // the AbstractToolLoader was created, each EditToolLoader
            // called the EditableOMGraphic constructor with the
            // GraphicAttributes as an argument. Hard to do when you
            // only have a editableClassName.
            eomg.createGraphic(ga);
        }
        return eomg;
    }

    /**
     * Give an OMGraphic to the EditToolLoader, which will create an
     * EditableOMGraphic for it.
     */
    public abstract EditableOMGraphic getEditableGraphic(OMGraphic graphic);

    /**
     * Get an Icon for a classname.
     */
    public ImageIcon getIcon(String classname) {
        if (graphicInfo != null) {
            EditClassWrapper ecw = (EditClassWrapper) graphicInfo.get(classname.intern());
            if (ecw != null) {
                return ecw.getIcon();
            }
        }
        return null;
    }

    /**
     * Get the pretty name, suitable for a GUI, for a classname.
     */
    public String getPrettyName(String classname) {
        if (graphicInfo != null) {
            EditClassWrapper ecw = (EditClassWrapper) graphicInfo.get(classname.intern());
            if (ecw != null) {
                return ecw.getPrettyName();
            }
        }
        return null;
    }
}