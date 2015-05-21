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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/drawing/EditClassWrapper.java,v $
// $RCSfile: EditClassWrapper.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.drawing;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * The EditClassWrapper is used by the EditToolLoaders to keep graphic
 * classes associated with their class names, the editable class name,
 * a valid icon and pretty name to be used in a GUI.
 */
public class EditClassWrapper {

    protected String className = null;
    protected String editableClassName = null;
    protected ImageIcon icon = null;
    protected String prettyName;

    public EditClassWrapper(String classname, String editableclassname,
            String iconname, String prettyname) {
        className = classname;
        editableClassName = editableclassname;
        prettyName = prettyname;
        URL url = this.getClass().getResource(iconname);
        if (url != null) {
            icon = new ImageIcon(url);
        }
    }

    public void setClassName(String classname) {
        className = classname;
    }

    public String getClassName() {
        return className;
    }

    public void setEditableClassName(String editableclassname) {
        editableClassName = editableclassname;
    }

    public String getEditableClassName() {
        return editableClassName;
    }

    public void setPrettyName(String prettyname) {
        prettyName = prettyname;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public void setIcon(ImageIcon image) {
        icon = image;
    }

    public ImageIcon getIcon() {
        return icon;
    }
}