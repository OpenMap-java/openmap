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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/IconPartCollectionEntry.java,v $
// $RCSfile: IconPartCollectionEntry.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

/**
 * An entry into an IconPartCollection, providing an IconPart with a
 * name and description.
 */
public class IconPartCollectionEntry {

    protected IconPart part;
    protected String name;
    protected String description;

    protected IconPartCollectionEntry() {}

    public IconPartCollectionEntry(String name, String description,
            IconPart part) {
        this.name = name;
        this.description = description;
        this.part = part;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setIconPart(IconPart part) {
        this.part = part;
    }

    public IconPart getIconPart() {
        return part;
    }
}