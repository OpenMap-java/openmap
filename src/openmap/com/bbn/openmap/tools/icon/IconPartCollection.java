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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/tools/icon/IconPartCollection.java,v $
// $RCSfile: IconPartCollection.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.icon;

import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A collection of IconParts, available by name.
 */
public class IconPartCollection extends IconPartCollectionEntry {

    protected Map entryMap;
    protected List collections;

    protected IconPartCollection() {}

    /**
     * Create a collection with a name and description.
     */
    public IconPartCollection(String name, String description) {
        setName(name);
        setDescription(description);
    }

    /**
     * Add an entry to the collection.
     */
    public void add(IconPartCollectionEntry entry) {
        if (entry != null) {
            String entryName = entry.getName();
            if (entryName != null) {
                getEntryMap().put(entryName.intern(), entry);
            }

            if (entry instanceof IconPartCollection) {
                getCollections().add(entry);
            }
        }
    }

    /**
     * Remove an entry from the collection.
     */
    public Object remove(IconPartCollectionEntry entry) {
        return getEntryMap().remove(entry);
    }

    /**
     * Clear the entries in the collection.
     */
    public void clear() {
        getEntryMap().clear();
    }

    /**
     * Get the set of names for the entries of this collection.
     */
    public Set keySet() {
        return getEntryMap().keySet();
    }

    /**
     * Get an icon part for the given name set with the given
     * rendering attributes. Calls get(name);
     */
    public IconPart get(String name,
                        com.bbn.openmap.omGraphics.DrawingAttributes da) {
        IconPart ip = get(name);
        if (ip != null) {
            ip.setRenderingAttributes(da);
        }
        return ip;
    }

    /**
     * Get an icon part for the given name. The top level
     * IconPartCollectionEntries are checked for the name. Will return
     * null if no IconPart exists for the name. If the name is a
     * collection name, null will be returned. However, before
     * returning null, any IconPartCollection added to this collection
     * will be checked, too, and any hits will be returned.
     */
    public IconPart get(String name) {
        Object entry = getEntryMap().get(name.intern());
        IconPart part = null;

        if (entry != null) {
            part = (IconPart) ((IconPart) ((IconPartCollectionEntry) entry).getIconPart()).clone();
        }

        if (part == null) {
            Iterator it = collections.iterator();
            while (it.hasNext()) {
                IconPartCollection ipc = (IconPartCollection) it.next();
                part = ipc.get(name);
                if (part != null) {
                    break;
                }
            }
        }

        return part;
    }

    /**
     * Get a description for the given name. The top level
     * IconPartCollectionEntries are checked for the name. Will return
     * null if no entry exists for the name. If the name is a
     * collection name, the description of the collection will be
     * returned. Before returning null, any IconPartCollection added
     * to this collection will be checked, too, and any hits will be
     * returned.
     */
    public String getDescription(String name) {
        Object entry = getEntryMap().get(name.intern());
        String desc = null;

        if (entry != null) {
            desc = ((IconPartCollectionEntry) entry).getDescription();
        }

        if (desc == null) {
            Iterator it = collections.iterator();
            while (it.hasNext()) {
                IconPartCollection ipc = (IconPartCollection) it.next();
                desc = ipc.getDescription(name);
                if (desc != null) {
                    break;
                }
            }
        }

        return desc;
    }

    /**
     * Create an IconPart from the list of entry names.
     * 
     * @param list a List of Strings, with the strings being names of
     *        entries into this collection.
     */
    public IconPart compose(List list) {
        IconPartList ipl = new IconPartList();

        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof String) {
                IconPart part = get((String) obj);
                if (part != null) {
                    ipl.add(part);
                }
            }
        }
        return ipl;
    }

    /**
     * Get a list of description Strings from a list of entry names.
     * 
     * @param list a List of Strings, with the strings being names of
     *        entries into this collection.
     * @return a List of description Strings for the given list of
     *         names.
     */
    public List composeDescription(List list) {
        LinkedList ll = new LinkedList();

        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof String) {
                String des = getDescription((String) obj);
                if (des != null) {
                    ll.add(des);
                }
            }
        }
        return ll;
    }

    /**
     * Set the entry Map.
     */
    protected void setEntryMap(Map map) {
        entryMap = map;
    }

    /**
     * Get the entry Map.
     */
    protected Map getEntryMap() {
        if (entryMap == null) {
            entryMap = new Hashtable();
        }
        return entryMap;
    }

    /**
     * Set the List to be used for holding IconPartCollections added
     * to this collection.
     */
    protected void setCollections(List list) {
        collections = list;
    }

    /**
     * Get the List of IconPartCollections that have been added.
     */
    protected List getCollections() {
        if (collections == null) {
            collections = new LinkedList();
        }
        return collections;
    }

    public void setIconPart(IconPart part) {
        this.part = part;
    }

    public IconPart getIconPart() {
        return part;
    }
}

