package org.geotiff.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * The <code>KeyMap</code> represents the two-way mapping between a single set
 * of GeoTIFF codes and their symbolic string names. The
 * <code>KeyRegistry</code> keeps track of all of these.
 * 
 * @see KeyRegistry
 */
public class KeyMap extends Properties {

//    private String name;

    private HashMap inverse = new HashMap();

    /** Empty Constructor for serialization */
    public KeyMap() {
        super();
    }

    /** Constructor for named resource file */
    public KeyMap(String resourceName) throws IOException {
        super();
        InputStream propfile = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (propfile == null) {
            throw new IOException("Resource not found");
        }
        load(propfile);

        // Construct the inverse of the table
        Iterator iter = entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            try {
                inverse.put(entry.getValue(), entry.getKey());
            } catch (Exception e) {
                // okay to fail for repeated.
            }
        }
    }

    /**
     * Gets property from key; if value starts with a $ it is an alias to
     * another property. The method is non-recursive, and only follows one level
     * of indirection.
     * 
     * @param key the symbolic key to resolve
     */
    public String getProperty2(String key) {
        String val = getProperty(key);
        if (val == null)
            return val;
        if (val.startsWith("$")) {
            val = getProperty(val.substring(1));
        }
        return val;
    }

    /**
     * Gets integer code from named key
     * 
     * @param key the symbolic key to resolve
     */
    int getCode(String key) {
        if (key == null)
            return -1;
        String sval = getProperty2(key);
        if (sval == null)
            return -1;
        else
            return Integer.parseInt(sval.trim());
    }

    /**
     * Gets primary string key from code value.
     * 
     * @param code the numeric code to lookup.
     */
    String getKey(int code) {
        return (String) inverse.get((new Integer(code)).toString());
    }

}
