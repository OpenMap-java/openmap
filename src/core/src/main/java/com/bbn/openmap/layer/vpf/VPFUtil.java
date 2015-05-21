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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFUtil.java,v $
// $RCSfile: VPFUtil.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:06:10 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import com.bbn.openmap.util.Debug;

/**
 * Miscellaneous utility functions in dealing with VPF data.
 */
public class VPFUtil {
    /**
     * all methods are static, no reason to construct
     */
    private VPFUtil() {
    }

    /**
     * returns a string with the elements of l separated by spaces
     * 
     * @param l the list to stringize
     * @return the string version of the list
     * @deprecated use listToString(List) instead
     */
    public static final String vectorToString(List<Object> l) {
        return listToString(l);
    }

    /**
     * returns a string with the elements of l separated by spaces
     * 
     * @param l the list to convert to string
     * @return the string version of the list
     */
    public static final String listToString(List<Object> l) {
        StringBuffer row = new StringBuffer();
        for (Object obj : l) {
            row.append(obj.toString()).append(" ");
        }
        return (row.toString());
    }

    /**
     * get the value contained in the object.
     * 
     * @param val returns the value of Shorts and Integers as an int. VPF null
     *        values get returned as Integer.MIN_VALUE, as do all other types
     * @return the value contained in val
     */
    public static final int objectToInt(Object val) {
        int v = Integer.MIN_VALUE;
        if (val instanceof Integer) {
            v = ((Integer) val).intValue();
            if (v == Integer.MIN_VALUE + 1) {
                v = Integer.MIN_VALUE;
            }
        } else if (val instanceof Short) {
            v = ((Short) val).shortValue();
            if (v == Short.MIN_VALUE + 1) {
                v = Integer.MIN_VALUE;
            }
        }
        return v;
    }

    /** some strings */
    public final static String Edge = "Edge";
    public final static String Edges = "Edges";
    public final static String Text = "Text";
    public final static String Area = "Area";
    // public final static String Point = "Point";
    public final static String EPoint = "EPoint";
    public final static String CPoint = "CPoint";

    /**
     * Parses dynamic args passed by specialist client. A <code>Hashtable</code>
     * is returned as a unified holder of all dynamic arguments.
     */
    public static Hashtable<String, Boolean> parseDynamicArgs(String args) {
        Hashtable<String, Boolean> dynArgs = new Hashtable<String, Boolean>();
        if (args != null) {
            String lowerArgs = args.toLowerCase();

            dynArgs.put(Edges, new Boolean(lowerArgs.indexOf(Edges) != -1));
            dynArgs.put(Text, new Boolean(lowerArgs.indexOf(Text) != -1));
            dynArgs.put(Area, new Boolean(lowerArgs.indexOf(Area) != -1));
            dynArgs.put(EPoint, new Boolean(lowerArgs.indexOf(EPoint) != -1));
            dynArgs.put(CPoint, new Boolean(lowerArgs.indexOf(CPoint) != -1));
        }
        return dynArgs;
    }

    /**
     * If <code>arg</code> maps to a <code>Boolean</code> in the Hashtable, that
     * value is returned, <code>false</code> otherwise.
     * 
     * @param dynArgs the Hashtable to look in
     * @param arg the argument to return
     */
    public static boolean getHashedValueAsBoolean(Hashtable<String, Boolean> dynArgs, String arg) {
        Object obj = dynArgs.get(arg);
        if (obj == null) {
            return false;
        } else if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else {
            return false;
        }
    }

    public static String getTypeForFeatureCode(String featureCode) {
        int lastCharIndex = featureCode.length() - 1;
        if (lastCharIndex >= 0) {
            char lastLetter = featureCode.charAt(lastCharIndex);

            if (lastLetter == 'l') {
                return VPFUtil.Edge;
            }

            if (lastLetter == 'a') {
                return VPFUtil.Area;
            }

            if (lastLetter == 't') {
                return VPFUtil.Text;
            }

            // if (lastLetter == 'p') {
            // // Can't tell at this point, it shouldn't matter for
            // // the feature cache stuff since the points are
            // return VPFUtil.EPoint;
            // }
        }

        return null;
    }

    /**
     * Return our default properties for vpf land.
     */
    public static Properties getDefaultProperties() {
        try {
            InputStream in = VPFLayer.class.getResourceAsStream("defaultVPFlayers.properties");
            // use a temporary so other threads won't see an
            // empty properties file
            Properties tmp = new Properties();
            if (in != null) {
                tmp.load(in);
                in.close();
            } else {
                Debug.error("VPFUtil.getDefaultProperties: can't load default properties file");
                // just use an empty properties file
            }
            return tmp;
        } catch (IOException io) {
            Debug.error("VPFUtil.getDefaultProperties: can't load default properties: " + io);
            return new Properties();
        }

    }
}