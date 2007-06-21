/*
 * OMGraphicHash.java
 *
 * Created on August 18, 2006, 9:29 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.bbn.openmap.omGraphics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is an OMGraphicsList cub-class with a Map interface. Access is backed by
 * a HashMap. Insertion and removal of OMGraphics is through the Map interface.
 * <p>
 * The add(OMGraphic), addOMGraphic(OMGraphic), remove(OMGraphic), and
 * removeOMGraphicAt(int) method are disabled and will throw a RuntimeException
 * if called.
 * <p>
 * This class is suited for use as the top OMGraphicList in an OMHandlerLayer
 * that has a large number of OMGraphic objects and needs to access those
 * objects using a unique key.
 * <p>
 * 
 * @see com.bbn.openmap.omGraphics.OMGraphicList
 * @author David J. Ward
 */
public class OMGraphicHash extends OMGraphicList implements Map {

    private HashMap graphicHash = new HashMap();

    /**
     * Creates a new instance of OMGraphicHash
     */
    public OMGraphicHash() {
        super();
    }

    /**
     * Construct an OMGraphicList with an initial capacity.
     * 
     * @param initialCapacity the initial capacity of the list
     */
    public OMGraphicHash(int initialCapacity) {
        super(initialCapacity);
    };

    /**
     * Returns the value to which this map maps the specified key. Returns
     * <tt>null</tt> if the map contains no mapping for this key. A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>. The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     * 
     * <p>
     * More formally, if this map contains a mapping from a key <tt>k</tt> to
     * a value <tt>v</tt> such that <tt>(key==null ? k==null :
     * key.equals(k))</tt>,
     * then this method returns <tt>v</tt>; otherwise it returns
     * <tt>null</tt>. (There can be at most one such mapping.)
     * 
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         <tt>null</tt> if the map contains no mapping for this key.
     * 
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *         does not permit <tt>null</tt> keys (optional).
     * 
     * @see #containsKey(Object)
     */
    public Object get(Object obj) {
        return graphicHash.get(obj);
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key. More formally, returns <tt>true</tt> if and only if this map
     * contains a mapping for a key <tt>k</tt> such that
     * <tt>(key==null ? k==null : key.equals(k))</tt>. (There can be at most
     * one such mapping.)
     * 
     * @param key key whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key.
     * 
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *         does not permit <tt>null</tt> keys (optional).
     */
    public boolean containsKey(Object obj) {
        return graphicHash.containsKey(obj);
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value. More formally, returns <tt>true</tt> if and only if
     * this map contains at least one mapping to a value <tt>v</tt> such that
     * <tt>(value==null ? v==null : value.equals(v))</tt>. This operation
     * will probably require time linear in the map size for most
     * implementations of the <tt>Map</tt> interface.
     * 
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     * @throws ClassCastException if the value is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the value is <tt>null</tt> and this map
     *         does not permit <tt>null</tt> values (optional).
     */
    public boolean containsValue(Object obj) {
        return graphicHash.containsValue(obj);
    }

    /**
     * Returns a set view of the mappings contained in this map. Each element in
     * the returned set is a {@link Map.Entry}. The set is backed by the map,
     * so changes to the map are reflected in the set, and vice-versa. If the
     * map is modified while an iteration over the set is in progress (except
     * through the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the iterator)
     * the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations. It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     * 
     * @return a set view of the mappings contained in this map.
     */
    public java.util.Set entrySet() {
        return graphicHash.entrySet();
    }

    /**
     * Returns a set view of the keys contained in this map. The set is backed
     * by the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt> <tt>retainAll</tt>, and <tt>clear</tt>
     * operations. It does not support the add or <tt>addAll</tt> operations.
     * 
     * @return a set view of the keys contained in this map.
     */
    public java.util.Set keySet() {
        return graphicHash.keySet();
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation). If the map previously contained a mapping for this
     * key, the old value is replaced by the specified value. (A map <tt>m</tt>
     * is said to contain a mapping for a key <tt>k</tt> if and only if
     * {@link #containsKey(Object) m.containsKey(k)} would return <tt>true</tt>.))
     * 
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key. A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key, if the implementation supports
     *         <tt>null</tt> values.
     * 
     * @throws UnsupportedOperationException if the <tt>put</tt> operation is
     *         not supported by this map.
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map.
     * @throws IllegalArgumentException if some aspect of this key or value
     *         prevents it from being stored in this map.
     * @throws NullPointerException if this map does not permit <tt>null</tt>
     *         keys or values, and the specified key or value is <tt>null</tt>.
     */
    public Object put(Object key, Object graphic) {
        if (!(graphic instanceof OMGraphic)) {
            throw new ClassCastException("Value is not an OMGraphic");
        }

        // first remove the OMGraphic from the list
        // Don't allow duplicate graphics in hash, that is multiple
        // key resolving to a single graphic. This would be very bad.
        if (graphicHash.containsValue(graphic)) {
            // now we know that we have this graphic in the
            // hash, now lets find the associated key.
            // This requires walkin the keySet.
            ArrayList keysToRemove = new ArrayList();
            for (Iterator it = graphicHash.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if (entry.getValue().equals(graphic)) {
                    keysToRemove.add(entry.getKey());
                }
            }
            for (Iterator it = keysToRemove.iterator(); it.hasNext();) {
                graphicHash.remove(it.next());
            }

            super.remove((OMGraphic) graphic);
        }
        if (graphicHash.containsKey(key)) {
            super.remove((OMGraphic) graphic);
        }
        OMGraphic previous = (OMGraphic) graphicHash.put(key, graphic);
        super.add((OMGraphic) graphic);
        return previous;
    }

    /**
     * Copies all of the mappings from the specified map to this map (optional
     * operation). The effect of this call is equivalent to that of calling
     * {@link #put(Object,Object) put(k, v)} on this map once for each mapping
     * from key <tt>k</tt> to value <tt>v</tt> in the specified map. The
     * behavior of this operation is unspecified if the specified map is
     * modified while the operation is in progress.
     * 
     * @param t Mappings to be stored in this map.
     * 
     * @throws UnsupportedOperationException if the <tt>putAll</tt> method is
     *         not supported by this map.
     * 
     * @throws ClassCastException if the class of a key or value in the
     *         specified map prevents it from being stored in this map.
     * 
     * @throws IllegalArgumentException some aspect of a key or value in the
     *         specified map prevents it from being stored in this map.
     * @throws NullPointerException if the specified map is <tt>null</tt>, or
     *         if this map does not permit <tt>null</tt> keys or values, and
     *         the specified map contains <tt>null</tt> keys or values.
     */
    public void putAll(Map map) {
        graphicHash.putAll(map);
    }

    /**
     * Removes the mapping for this key from this map if it is present (optional
     * operation). More formally, if this map contains a mapping from key
     * <tt>k</tt> to value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is
     * removed. (The map can contain at most one such mapping.)
     * 
     * <p>
     * Returns the value to which the map previously associated the key, or
     * <tt>null</tt> if the map contained no mapping for this key. (A
     * <tt>null</tt> return can also indicate that the map previously
     * associated <tt>null</tt> with the specified key if the implementation
     * supports <tt>null</tt> values.) The map will not contain a mapping for
     * the specified key once the call returns.
     * 
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     * 
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the key is <tt>null</tt> and this map
     *         does not permit <tt>null</tt> keys (optional).
     * @throws UnsupportedOperationException if the <tt>remove</tt> method is
     *         not supported by this map.
     */
    public Object remove(Object obj) {
        OMGraphic graphic = (OMGraphic) graphicHash.get(obj);
        super.remove(graphic);
        return graphicHash.remove(obj);
    }

    /**
     * Returns a collection view of the values contained in this map. The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa. If the map is modified while an iteration
     * over the collection is in progress (except through the iterator's own
     * <tt>remove</tt> operation), the results of the iteration are undefined.
     * The collection supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>
     * and <tt>clear</tt> operations. It does not support the add or
     * <tt>addAll</tt> operations.
     * 
     * @return a collection view of the values contained in this map.
     */
    public java.util.Collection values() {
        return graphicHash.values();
    }

    /** used to displable the IllegalArgumentException during cloning */
    private boolean cloningInProgress;

    /**
     * @return a duplicate list full of shallow copies of each of the OMGraphics
     *         contained on the list.
     */
    public synchronized Object clone() {
        cloningInProgress = true;
        OMGraphicHash omgl = (OMGraphicHash) super.clone();
        omgl.graphicHash = (HashMap) graphicHash.clone();
        cloningInProgress = false;
        return omgl;
    }

    /**
     * Add an OMGraphic to the GraphicList. The OMGraphic must not be null.
     * <p>
     * For OMGraphicHash this method will throw an
     * <code>IllegalArgumentException</code> because adding a graphic must be
     * done through <code>put(key,value)</code>.
     * <p>
     * 
     * @param g the non-null OMGraphic to add
     * @exception IllegalArgumentException if OMGraphic is null
     */
    public void addOMGraphic(OMGraphic g) {
        // Prevent adding a graphic using the OMGraphic List.
        // OMGraphicHash entry must be added through the Map interface.
        if (cloningInProgress) {
            super.addOMGraphic(g);
            return;
        }
        throw new RuntimeException("addOMGraphic() not permitted for OMGraphicHash(). Use put(key, OMGraphic) instead.");

    }

    /**
     * Add an OMGraphic to the list.
     * <p>
     * For OMGraphicHash this method will throw an
     * <code>IllegalArgumentException</code> because adding a graphic must be
     * done through <code>put(key,value)</code>.
     */
    public void add(OMGraphic g) {
        // Prevent adding a graphic using the OMGraphic List.
        // OMGraphicHash entry must be added through the Map interface.
        if (cloningInProgress) {
            super.add(g);
            return;
        }
        throw new RuntimeException("addOMGraphic() not permitted for OMGraphicHash(). Use put(key, OMGraphic) instead.");
    }

    /**
     * Remove the graphic at the location number.
     * 
     * <p>
     * For OMGraphicHash this method will throw an
     * <code>IllegalArgumentException</code> because removing a graphic must
     * be done through <code>remove(key)</code>.
     * 
     * @param location the location of the OMGraphic to remove.
     */
    public void removeOMGraphicAt(int location) {
        // Prevent removing a specific graphic using the OMGraphic List.
        // OMGraphicHash entry must be remove using remove(Object).
        throw new RuntimeException("removeOMGraphicAt() not permitted for OMGraphicHash(). Use remove(key) instead.");
    }

    /**
     * Remove the graphic.
     * 
     * <p>
     * For OMGraphicHash this method will throw an
     * <code>IllegalArgumentException</code> because removing a graphic must
     * be done through <code>remove(key)</code>.
     * 
     * @param graphic the OMGraphic object to remove.
     * @return true if graphic was on the list, false if otherwise.
     */
    public boolean remove(OMGraphic graphic) {
        // Prevent removing a specific graphic using the OMGraphic List.
        // OMGraphicHash entry must be remove using remove(Object).
        throw new RuntimeException("remove(OMGRaphic) not permitted for OMGraphicHash(). Use remove(key) instead.");
    }

    /**
     * Remove all elements from the graphic list.
     */
    public void clear() {
        super.clear();
        graphicHash.clear();
    }
}
