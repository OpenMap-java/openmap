//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/coords/NedFrame.java,v $
//$RCSfile: NedFrame.java,v $
//$Revision: 1.6 $
//$Date: 2009/02/25 22:34:04 $
//$Author: dietrick $
//
//***********************************************************

package com.bbn.openmap.proj.coords;

import com.bbn.openmap.proj.ProjMath;

/**
 * Encapsulates the NED (North-East-Down) coordinate system. This is also know
 * as local tangent plane (LTP).
 * <p>
 * Author: Robert Hayes
 * <p>
 */
public class NedFrame {

    /**
     * The x component of the coordinate (NORTH).
     */
    protected float x;
    /**
     * The y component of the coordinate (EAST).
     */
    protected float y;
    /**
     * The z component of the coordinate (DOWN).
     */
    protected float z;

    /**
     * Construct a default NedFrame. x,y,z are all set to zero.
     */
    public NedFrame() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;
    }

    /**
     * Construct a NedFrame from ECEF vector and latitude and longitude.
     * 
     * @param x ecef x.
     * @param y ecef y.
     * @param z ecef z.
     * @param lat = latitude in degrees.
     * @param lon = longitude in degrees.
     */
    public NedFrame(float x, float y, float z, double lat, double lon) {
        // All calculations are done using radians!

        double ecef[] = new double[3];
        double ned[] = new double[3];

        double latitude = ProjMath.degToRad(lat);
        double longitude = ProjMath.degToRad(lon);

        ecef[0] = x;
        ecef[1] = y;
        ecef[2] = z;

        ecef2ned(ned, latitude, longitude, ecef);

        this.x = (float) ned[0];
        this.y = (float) ned[1];
        this.z = (float) ned[2];
    }

    /**
     * Construct a NedFrame from a ECEF vector and a LatLonPoint.
     * 
     * @param ecefVector
     */
    public NedFrame(double[] ecefVector, LatLonPoint llpt) {
        // All calculations are done using radians!

        double ecef[] = new double[3];
        double ned[] = new double[3];
        double lat_ = llpt.getY();
        double lon_ = llpt.getX();
        double latitude = ProjMath.degToRad(lat_);
        double longitude = ProjMath.degToRad(lon_);

        ecef[0] = ecefVector[0];
        ecef[1] = ecefVector[1];
        ecef[2] = ecefVector[2];

        ecef2ned(ned, latitude, longitude, ecef);

        this.x = (float) ned[0];
        this.y = (float) ned[1];
        this.z = (float) ned[2];

    }

    /**
     * Convert to a geocentric frame using a LatLonPoint.
     * 
     * @param llpt
     * @return a vector of ecef values
     */
    public double[] toGeocentricFrame(LatLonPoint llpt) {
        // All calculations are done using radians!
        double ecef[] = new double[3];
        double ned[] = new double[3];
        double lat_ = llpt.getY();
        double lon_ = llpt.getX();
        double latitude = ProjMath.degToRad(lat_);
        double longitude = ProjMath.degToRad(lon_);

        ned2ecef(ned, latitude, longitude, ecef);

        return ecef;
    }

    /**
     * Internal conversion routine.
     * 
     * @param ned vector
     * @param latitude in radians.
     * @param longitude in radians
     * @param ecef vector
     */
    public void ecef2ned(double ned[], double latitude, double longitude,
                         double ecef[]) {

        double temp[][] = new double[3][3];

        double clat = Math.cos(latitude);
        double clon = Math.cos(longitude);
        double slat = Math.sin(latitude);
        double slon = Math.sin(longitude);

        temp[0][0] = -slat * clon;
        temp[0][1] = -slat * slon;
        temp[0][2] = clat;

        temp[1][0] = -slon;
        temp[1][1] = clon;
        temp[1][2] = 0.0;

        temp[2][0] = -clat * clon;
        temp[2][1] = -clat * slon;
        temp[2][2] = -slat;

        for (int j = 0; j < 3; ++j) {
            ned[j] = 0.0;
            for (int i = 0; i < 3; i++)
                ned[j] += temp[j][i] * ecef[i];
        }

    }

    /**
     * Internal conversion routine.
     * 
     * @param ned vector
     * @param latitude in radians.
     * @param longitude in radians
     * @param ecef vector
     */
    public void ned2ecef(double ned[], double latitude, double longitude,
                         double ecef[]) {

        double temp[][] = new double[3][3];

        double clat = Math.cos(latitude);
        double clon = Math.cos(longitude);
        double slat = Math.sin(latitude);
        double slon = Math.sin(longitude);

        ned[0] = this.x;
        ned[1] = this.y;
        ned[2] = this.z;

        temp[0][0] = -slat * clon;
        temp[1][0] = -slat * slon;
        temp[2][0] = clat;

        temp[0][1] = -slon;
        temp[1][1] = clon;
        temp[2][1] = 0.0;

        temp[0][2] = -clat * clon;
        temp[1][2] = -clat * slon;
        temp[2][2] = -slat;

        for (int j = 0; j < 3; ++j) {
            ecef[j] = 0.0;
            for (int i = 0; i < 3; i++)
                ecef[j] += temp[j][i] * ned[i];
        }
    }

    /**
     * Copy construct a NedFrame.
     * 
     * @param nedpt NedFrame
     */
    public NedFrame(NedFrame nedpt) {
        x = nedpt.x;
        y = nedpt.y;
        z = nedpt.z;
    }

    /**
     * 
     * @return String representation of NED vector.
     */
    public String toString() {
        return "NedFrame[N=" + x + ",E=" + y + ",D=" + z + "]";
    }

    /**
     * Set x.
     * 
     * @param pX in meters.
     */
    public void setX(float pX) {
        x = pX;
    }

    /**
     * Set y.
     * 
     * @param pY in meters.
     */
    public void setY(float pY) {
        y = pY;
    }

    /**
     * Set z.
     * 
     * @param pZ in meters.
     */
    public void setZ(float pZ) {
        z = pZ;
    }

    /**
     * Set x,y,z.
     * 
     * @param v1 North.
     * @param v2 East.
     * @param v3 Down.
     */
    public void setXYZ(float v1, float v2, float v3) {
        x = v1;
        y = v2;
        z = v3;
    }

    /**
     * Get x.
     * 
     * @return float x.
     */
    public float getX() {
        return x;
    }

    /**
     * Get Y.
     * 
     * @return float y.
     */
    public float getY() {
        return y;
    }

    /**
     * Get Z.
     * 
     * @return float z.
     */
    public float getZ() {
        return z;
    }

    /**
     * Get speed.
     * 
     * @return double speed.
     */
    public double toSpeed() {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Get heading.
     * 
     * @return double heading in degrees.
     */
    public double toHeading() {
        double radians = Math.atan2(y, x);
        double degrees = Math.toDegrees(radians);
        if (degrees < 0) {
            degrees += degrees + 360;
        }
        return degrees;
    }

    /*
     * public final static void main (String[] args) { // TEST1 double temp[] =
     * new double[3]; temp[0] = 13; temp[1] = 5; temp[2] = 8;
     * 
     * LatLonPoint llpt = new LatLonPoint(40.00,-74.500);
     * System.out.println("llpt" + llpt); System.out.println("ecef:" + temp[0] + " : " +
     * temp[1] + " : " + temp[2]);
     * 
     * NedFrame nedv = new NedFrame(temp,llpt); System.out.println("ned" +
     * nedv);
     * 
     * temp = nedv.toGeocentricFrame(llpt); System.out.println("ecef:" + temp[0] + " : " +
     * temp[1] + " : " + temp[2]); }
     */

}// class

