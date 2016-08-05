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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/Length.java,v $
// $RCSfile: Length.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.util.logging.Logger;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;

/**
 * Length is a convenience class used for a couple of things. It can be used to
 * specify unit type, and can be used for conversion from radians to/from
 * whatever units are represented by the implemented class.
 */
public enum Length {

    /** Miles, in WGS 84 spherical earth model units. */
    MILE("mile", "miles", Planet.wgs84_earthEquatorialCircumferenceMiles_D),
    /** Feet, in WGS 84 spherical earth model units. */
    FEET("feet", "ft", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0),
    /** Feet, in WGS 84 spherical earth model units. */
    YARD("yards", "yd", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0 / 3.0),
    /** Meters, in WGS 84 Spherical earth model units. */
    METER("meter", "m", Planet.wgs84_earthEquatorialCircumferenceMeters_D),
    /** Kilometers, in WGS 84 Spherical earth model units. */
    KM("kilometer", "km", Planet.wgs84_earthEquatorialCircumferenceKM_D),
    /** Nautical Miles, in WGS 84 Spherical earth model units. */
    NM("nautical mile", "nm", Planet.wgs84_earthEquatorialCircumferenceNMiles_D),
    /** Decimal Degrees, in WGS 84 Spherical earth model units. */
    DECIMAL_DEGREE("decimal degree", "deg", 360.0),
    /** Data Mile, in WGS 84 spherical earth model units. */
    DM("datamile", "dm", Planet.wgs84_earthEquatorialCircumferenceMiles_D * 5280.0 / 6000.0),

    /** Radians, in terms of a spherical earth. */
    RADIAN("radian", "rad", com.bbn.openmap.MoreMath.TWO_PI_D) {
        public float toRadians(float numUnits) {
            return numUnits;
        }

        public double toRadians(double numUnits) {
            return numUnits;
        }

        public float fromRadians(float numRadians) {
            return numRadians;
        }

        public double fromRadians(double numRadians) {
            return numRadians;
        }
    };

    /** Unit/radians */
    protected final double constant;
    protected final String name;
    protected final String abbr;
    protected double unitEquatorCircumference;
    protected transient I18n i18n = Environment.getI18n();

    /**
     * Create a Length, with a name an the number of it's units that go around
     * the earth at its equator. The name and abbreviation are converted to
     * lower case for consistency.
     */
    private Length(String name, String abbr, double unitEquatorCircumference) {
        this.name = i18n.get(this, abbr + ".name", name).toLowerCase().intern();
        this.unitEquatorCircumference = unitEquatorCircumference;
        this.constant = unitEquatorCircumference / com.bbn.openmap.MoreMath.TWO_PI_D;
        this.abbr = abbr.toLowerCase().intern();
    }

    /**
     * Given a number of units provided by this Length, convert to a number of
     * radians.
     */
    public float toRadians(float numUnits) {
        getLogger().fine("Translating " + name + " from radians");
        return numUnits / (float) constant;
    }

    public double toRadians(double numUnits) {
        getLogger().fine("Translating " + name + " from radians");
        return numUnits / constant;
    }

    /**
     * Given a number of radians, convert to the number of units represented by
     * this length.
     */
    public float fromRadians(float numRadians) {
        getLogger().fine("Translating radians from " + name);
        return numRadians * (float) constant;
    }

    /**
     * Given a number of radians, convert to the number of units represented by
     * this length.
     */
    public double fromRadians(double numRadians) {
        getLogger().fine("Translating radians from " + name);
        return numRadians * constant;
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
     * Get the Length object with the given name or abbreviation. If nothing
     * exists with that name, then return null. The lower case version of the
     * name or abbreviation is checked against the available options.
     */
    public static Length get(String name) {
        if (name != null) {
            for (Length length : values()) {
                if (length.name.equalsIgnoreCase(name) || length.abbr.equalsIgnoreCase(name)) {
                    return length;
                }
            }
        }
        return null;
    }

    // <editor-fold defaultstate="collapsed" desc="Logger Code">
    /**
     * Holder for this class's Logger. This allows for lazy initialization of
     * the logger.
     */
    private static final class LoggerHolder {
        /**
         * The logger for this class
         */
        private static final Logger LOGGER = Logger.getLogger(Length.class.getName());

        /**
         * Prevent instantiation
         */
        private LoggerHolder() {
            throw new AssertionError("This should never be instantiated");
        }
    }

    /**
     * Get the logger for this class.
     *
     * @return logger for this class
     */
    private static Logger getLogger() {
        return LoggerHolder.LOGGER;
    }
    // </editor-fold>
}