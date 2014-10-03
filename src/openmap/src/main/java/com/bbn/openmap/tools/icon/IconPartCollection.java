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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bbn.openmap.omGraphics.DrawingAttributes;

/**
 * A collection of IconParts, available by name.
 */
public class IconPartCollection extends IconPartCollectionEntry {

    protected Map<String, IconPartCollectionEntry> entryMap;
    protected List<IconPartCollection> collections;

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
                getCollections().add((IconPartCollection)entry);
            }
        }
    }

    /**
     * Remove an entry from the collection.
     */
    public Object remove(IconPartCollectionEntry entry) {
        return getEntryMap().remove(entry.getName());
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
    public Set<String> keySet() {
        return getEntryMap().keySet();
    }

    /**
     * Get an icon part for the given name set with the given rendering
     * attributes. Calls get(name);
     */
    public IconPart get(String name, DrawingAttributes da) {
        IconPart ip = get(name);
        if (ip != null) {
            ip.setRenderingAttributes(da);
        }
        return ip;
    }

    /**
     * Get an icon part for the given name. The top level
     * IconPartCollectionEntries are checked for the name. Will return null if
     * no IconPart exists for the name. If the name is a collection name, null
     * will be returned. However, before returning null, any IconPartCollection
     * added to this collection will be checked, too, and any hits will be
     * returned.
     */
    public IconPart get(String name) {
        IconPartCollectionEntry entry = getEntryMap().get(name.intern());
        IconPart part = null;

        if (entry != null) {
            part = (IconPart) entry.getIconPart().clone();
        }

        if (part == null) {
            for (IconPartCollection ipc : collections) {
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
     * IconPartCollectionEntries are checked for the name. Will return null if
     * no entry exists for the name. If the name is a collection name, the
     * description of the collection will be returned. Before returning null,
     * any IconPartCollection added to this collection will be checked, too, and
     * any hits will be returned.
     */
    public String getDescription(String name) {
        IconPartCollectionEntry entry = getEntryMap().get(name.intern());
        String desc = null;

        if (entry != null) {
            desc = entry.getDescription();
        }

        if (desc == null) {
            for (IconPartCollection ipc : collections) {
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
     * @param list a List of Strings, with the strings being names of entries
     *        into this collection.
     */
    public IconPart compose(List<String> list) {
        IconPartList ipl = new IconPartList();

        for (String partName : list) {
            IconPart part = get(partName);
            if (part != null) {
                ipl.add(part);
            }
        }

        return ipl;
    }

    /**
     * Get a list of description Strings from a list of entry names.
     * 
     * @param list a List of Strings, with the strings being names of entries
     *        into this collection.
     * @return a List of description Strings for the given list of names.
     */
    public List<String> composeDescription(List<String> list) {
        LinkedList<String> ll = new LinkedList<String>();

        for (String entry : list) {
            String des = getDescription(entry);
            if (des != null) {
                ll.add(des);
            }
        }
        return ll;
    }

    /**
     * Set the entry Map.
     */
    protected void setEntryMap(Map<String, IconPartCollectionEntry> map) {
        entryMap = map;
    }

    /**
     * Get the entry Map.
     */
    protected Map<String, IconPartCollectionEntry> getEntryMap() {
        if (entryMap == null) {
            entryMap = new Hashtable<String, IconPartCollectionEntry>();
        }
        return entryMap;
    }

    /**
     * Set the List to be used for holding IconPartCollections added to this
     * collection.
     */
    protected void setCollections(List<IconPartCollection> list) {
        collections = list;
    }

    /**
     * Get the List of IconPartCollections that have been added.
     */
    protected List<IconPartCollection> getCollections() {
        if (collections == null) {
            collections = new LinkedList<IconPartCollection>();
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
