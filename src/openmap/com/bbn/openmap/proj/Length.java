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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Length.java,v $
// $RCSfile: Length.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:14 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.proj;

import com.bbn.openmap.util.Debug;


/** 
 * Length is a convenience class used for a couple of things.  It can
 * be used to specifiy unit type, and can be used for conversion from
 * radians to/from whatever units are represented by the implemented
 * class.  
 */
public class Length {

    /** Miles, in WGS 84 spherical earth model units. */
    public final static Length MILE = new Length("mile", "miles",  Planet.wgs84_earthEquatorialCircumferenceMiles);
    /** Feet, in WGS 84 spherical earth model units. */
    public final static Length FEET = new Length("feet", "feet",  Planet.wgs84_earthEquatorialCircumferenceMiles * 5280f);
    /** Meters, in WGS 84 Spherical earth model units. */
    public final static Length METER = new Length("meter", "m", Planet.wgs84_earthEquatorialCircumferenceMeters);
    /** Kilometers, in WGS 84 Spherical earth model units. */
    public final static Length KM = new Length("kilometer", "km", Planet.wgs84_earthEquatorialCircumferenceKM);
    /** Nautical Miles, in WGS 84 Spherical earth model units. */
    public final static Length NM = new Length("nautical mile", "nm", Planet.wgs84_earthEquatorialCircumferenceNMiles);
    /** Decimal Degrees, in WGS 84 Spherical earth model units. */
    public final static Length DECIMAL_DEGREE = new Length("decimal degree", "deg", 360f);
    /** Radians, in terms of a spherical earth. */
    public final static Length RADIAN = new Length("radian", "rad", com.bbn.openmap.MoreMath.TWO_PI);
    /** Data Mile, in WGS 84 spherical earth model units. */
    public final static Length DM = new Length("datamile", "dm", Planet.wgs84_earthEquatorialCircumferenceMiles * 5280f/6000f);

    /** Unit/radians */
    protected final float constant;
    protected final String name;
    protected final String abbr;
    
    /**
     * Create a Length, with a name an the number of it's units that
     * go around the earth at its equator.  The name and abbreviation
     * are converted to lower case for consistency.
     */
    public Length(String name, String abbr, float unitEquatorCircumference) {
        this.name = name.toLowerCase().intern();
        constant = unitEquatorCircumference/com.bbn.openmap.MoreMath.TWO_PI;
        this.abbr = abbr.toLowerCase().intern();
    }

    /**
     * Given a number of units provided by this Length, convert
     * to a number of radians.
     */
    public float toRadians(float numUnits) {
        if (Debug.debugging("length")) {
            Debug.output("Translating " + name + " from radians");
        }

        return numUnits/constant;
    }

    /**
     * Given a number of radians, convert to the number of units
     * represented by this length.
     */
    public float fromRadians(float numRadians) {
        if (Debug.debugging("length")) {
            Debug.output("Translating radians from " + name);
        }
        
        return numRadians*constant;
    }

    /**
     * Return the name for this length type.
     */
    public String toString() {
        return name;
    }

    /**
     * Return the abbreviation for this length type.
     */
    public String getAbbr() {
        return abbr;
    }
    
    /** 
     * Get a list of the Lengths currently defined as static
     * implementations of this class.
     */
    public static Length[] getAvailable() {
        return new Length[] {METER, KM, FEET, MILE, DM, NM, DECIMAL_DEGREE};
    }

    /**
     * Get the Length object with the given name or abbreviation.  If
     * nothing exists with that name, then return null.  The lower
     * case version of the name or abbreviation is checked against the
     * available options.
     */
    public static Length get(String name) {
        Length[] choices = getAvailable();

        for (int i = 0; i < choices.length; i++) {
            if (name.toLowerCase().intern() == choices[i].toString() ||
                name.toLowerCase().intern() == choices[i].getAbbr()) {
                return choices[i];
            }
        }
        return null;
    }
}
