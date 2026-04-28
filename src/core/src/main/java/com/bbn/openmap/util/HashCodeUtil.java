package com.bbn.openmap.util;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 * Algorithm from Effective Java by Joshua Bloch. Implementation found at
 * http://www.javapractices.com.
 * 
 * Example use case:
 * 
 * <pre>
 * public int hashCode() {
 *     int result = HashCodeUtil.SEED;
 *     // collect the contributions of various fields
 *     result = HashCodeUtil.hash(result, fPrimitive);
 *     result = HashCodeUtil.hash(result, fObject);
 *     result = HashCodeUtil.hash(result, fArray);
 *     return result;
 * }
 * </pre>
 */
public final class HashCodeUtil {

    /**
     * An initial value for a <code>hashCode</code>, to which is added
     * contributions from fields. Using a non-zero value decreases collisons of
     * <code>hashCode</code> values.
     */
    public static final int SEED = 17;

    /**
     * hash booleans.
     * 
     * @param aSeed seeding int to start with
     * @param aBoolean object to hash
     * @return hash for boolean
     */
    public static int hash(int aSeed, boolean aBoolean) {
        return firstTerm(aSeed) + (aBoolean ? 1 : 0);
    }

    /**
     * hash chars.
     * 
     * @param aSeed seeding int to start with
     * @param aChar object to hash
     * @return hash for char
     */
    public static int hash(int aSeed, char aChar) {
        return firstTerm(aSeed) + (int) aChar;
    }

    /**
     * hash ints.
     * 
     * @param aSeed seeding int to start with
     * @param aInt object to hash
     * @return hash for int
     */
    public static int hash(int aSeed, int aInt) {
        /*
         * Implementation Note Note that byte and short are handled by this
         * method, through implicit conversion.
         */
        return firstTerm(aSeed) + aInt;
    }

    /**
     * hash longs.
     * 
     * @param aSeed seeding int to start with
     * @param aLong object to hash
     * @return hash for long
     */
    public static int hash(int aSeed, long aLong) {
        return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
    }

    /**
     * hash floats.
     * 
     * @param aSeed seeding int to start with
     * @param aFloat object to hash
     * @return hash for float
     */
    public static int hash(int aSeed, float aFloat) {
        return hash(aSeed, Float.floatToIntBits(aFloat));
    }

    /**
     * hash for doubles
     * 
     * @param aSeed seeding int to start with
     * @param aDouble to hash
     * @return hash for double
     */
    public static int hash(int aSeed, double aDouble) {
        return hash(aSeed, Double.doubleToLongBits(aDouble));
    }

    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an
     * array.
     * 
     * If <code>aObject</code> is an array, then each element may be a primitive
     * or a possibly-null object.
     * 
     * @param aSeed seeding int to start with
     * @param aObject object to hash
     * @return hash for aObject
     */
    public static int hash(int aSeed, Object aObject) {
        int result = aSeed;
        if (aObject == null) {
            result = hash(result, 0);
        } else if (!isArray(aObject)) {
            result = hash(result, aObject.hashCode());
        } else {
            int length = Array.getLength(aObject);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(aObject, idx);
                // recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }

    // / PRIVATE ///
    private static final int fODD_PRIME_NUMBER = 31;

    private static int firstTerm(int aSeed) {
        return fODD_PRIME_NUMBER * aSeed;
    }

    private static boolean isArray(Object aObject) {
        return aObject.getClass().isArray();
    }
}
