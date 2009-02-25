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
//$Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/coords/EnuFrame.java,v $
//$RCSfile: EnuFrame.java,v $
//$Revision: 1.7 $
//$Date: 2009/02/25 22:34:04 $
//$Author: dietrick $
//
//***********************************************************

package com.bbn.openmap.proj.coords;

import com.bbn.openmap.proj.ProjMath;

/**
 * Encapsulates the ENU (East-North-Up) coordinate system.
 * <p>
 * I like the idea of hiding the detail of the transformation from the user. The
 * user needs no detailed knowledge of mathematics and if we change the
 * algorithm, nobody will be affected.
 * <p>
 * Author: Robert Hayes
 */
public class EnuFrame {

    /**
     * The x component of the coordinate (EAST).
     */
    protected float x;
    /**
     * The y component of the coordinate (NORTH).
     */
    protected float y;
    /**
     * The z component of the coordinate (UP).
     */
    protected float z;

    /**
     * Construct a default EnuFrame. x,y,z are all set to zero.
     */
    public EnuFrame() {
        x = 0.0f;
        y = 0.0f;
        z = 0.0f;

    }

    /**
     * Construct a EnuFrame from a ECEF vector and a LatLonPoint.
     * 
     * @param llpt LatLonPoint.
     * @param ecefv representing an ecef vector.
     */
    public EnuFrame(double[] ecefv, LatLonPoint llpt) {
        double lat_ = llpt.getY();
        double lon_ = llpt.getX();

        double latitude = ProjMath.degToRad(lat_);
        double longitude = ProjMath.degToRad(lon_);

        double ecefVector[] = new double[3];
        double enuVector[] = new double[3];

        ecefVector[0] = ecefv[0];
        ecefVector[1] = ecefv[1];
        ecefVector[2] = ecefv[2];

        ecef2enu(ecefVector, latitude, longitude, enuVector);

        this.x = (float) enuVector[0];
        this.y = (float) enuVector[1];
        this.z = (float) enuVector[2];

    }

    /**
     * Convert to geocentric frame using a LatLonPoint.
     * 
     * @param llpt
     * @return a double of ecef values
     */
    public double[] toGeocentricFrame(LatLonPoint llpt) {
        double lat_ = (double) llpt.getY();
        double lon_ = (double) llpt.getX();

        double latitude = ProjMath.degToRad(lat_);
        double longitude = ProjMath.degToRad(lon_);

        double enuVector[] = new double[3];
        double ecefVector[] = new double[3];

        enuVector[0] = this.x;
        enuVector[1] = this.y;
        enuVector[2] = this.z;

        enu2ecef(ecefVector, latitude, longitude, enuVector);

        return ecefVector;
    }

    /**
     * Internal conversion routine.
     * 
     * @param ecefVector vector
     * @param latitude in radians.
     * @param longitude in radians
     * @param enuVector vector
     */
    public void ecef2enu(double ecefVector[], double latitude,
                         double longitude, double enuVector[]) {

        double temp[][] = new double[3][3];
        double slat = Math.sin(latitude);
        double clat = Math.cos(latitude);
        double slon = Math.sin(longitude);
        double clon = Math.cos(longitude);

        temp[0][0] = -slon;
        temp[0][1] = clon;
        temp[0][2] = 0.0;

        temp[1][0] = -clon * slat;
        temp[1][1] = -slon * slat;
        temp[1][2] = clat;

        temp[2][0] = clon * clat;
        temp[2][1] = slon * clat;
        temp[2][2] = slat;

        for (int j = 0; j < 3; ++j) {
            enuVector[j] = 0.0;
            for (int i = 0; i < 3; i++) {
                enuVector[j] += temp[j][i] * ecefVector[i];
            }
        }
    }

    /**
     * Internal conversion routine.
     * 
     * @param ecefVector vector
     * @param latitude in radians.
     * @param longitude in radians
     * @param enuVector vector
     */
    protected void enu2ecef(double ecefVector[], double latitude,
                            double longitude, double enuVector[]) {

        double temp[][] = new double[3][3];
        double clat = Math.cos(latitude);
        double slat = Math.sin(latitude);
        double clon = Math.cos(longitude);
        double slon = Math.sin(longitude);

        temp[0][0] = -slon;
        temp[0][1] = -clon * slat;
        temp[0][2] = clon * clat;

        temp[1][0] = clon;
        temp[1][1] = -slon * slat;
        temp[1][2] = slon * clat;

        temp[2][0] = 0.0;
        temp[2][1] = clat;
        temp[2][2] = slat;

        for (int j = 0; j < 3; ++j) {
            ecefVector[j] = 0.0;
            for (int i = 0; i < 3; i++) {
                ecefVector[j] += temp[j][i] * enuVector[i];
            }
        }

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
     * @param v1 East.
     * @param v2 North.
     * @param v3 Up.
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
     * To get the direction as a vector.
     * 
     * @param degrees is heading 0-360
     * @param latitude in degrees.
     * @param longitude in degrees.
     * @param ecefVector vector
     */
    public void toDirectionVector(double degrees, double latitude,
                                  double longitude, double ecefVector[]) {

        double radians = Math.toRadians(degrees) - 360;
        double east = Math.sin(radians);
        double north = Math.cos(radians);
        double lat_ = Math.toRadians(latitude);
        double lon_ = Math.toRadians(longitude);
        double up = 0.0;
        double enuVector[] = { east, north, up };
        this.enu2ecef(ecefVector, lat_, lon_, enuVector);
    }

    /*
     * public static void main(String[] args) {
     * 
     * double ecefVector[] = new double[3]; double enuVector[] = new double[3];
     * double longitude = Math.toRadians(40.00); double latitude =
     * Math.toRadians(-74.50)
     * 
     * EnuFrame test = new EnuFrame();
     * 
     * ecefVector[0] = 13; ecefVector[1] = 5; ecefVector[2] = 8;
     * 
     * enuVector[0] = 0.e0; enuVector[0] = 0.e0; enuVector[0] = 0.e0;
     * 
     * System.err.println("lat" + latitude + " lon" + longitude);
     * //System.err.println("" + ecefVector[0] + ":" + ecefVector[2]+
     * ecefVector[2] ); //System.err.println("" + enuVector[0] + ":" +
     * enuVector[2]+ enuVector[2] );
     *  // TEST1 test.ecef2enu(ecefVector, latitude, longitude, enuVector);
     * System.err.println("TEST1:"); System.err.println("ECEF:" + ecefVector[0] + " : " +
     * ecefVector[1]+ " :" +ecefVector[2] ); System.err.println("ENU:" +
     * enuVector[0] + " : " + enuVector[1]+ " : "+enuVector[2] );
     * 
     * ecefVector[0] = 0.e0; ecefVector[1] = 0.e0; ecefVector[2] = 0.e0; //
     * TEST2 test.enu2ecef(ecefVector, latitude, longitude, enuVector);
     * System.err.println("TEST2:"); System.err.println("ENU:" + enuVector[0] + " : " +
     * enuVector[1]+ " : "+enuVector[2] ); System.err.println("ECEF:" +
     * ecefVector[0] + " : " + ecefVector[1]+ " :" +ecefVector[2] ); }
     */

}// class

