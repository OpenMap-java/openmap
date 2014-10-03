// **********************************************************************
//
// <copyright>
//
// BBN Technologies
// 10 Moulton Street
// Cambridge, MA 02138
// (617) 873-8000
//
// Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/coords/MGRSPoint.java,v $
// $RCSfile: MGRSPoint.java,v $
// $Revision: 1.20 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.proj.coords;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;

/**
 * A class representing a MGRS coordinate that has the ability to provide the
 * decimal degree lat/lon equivalent, as well as the UTM equivalent. This class
 * does not do checks to see if the MGRS coordinates provided actually make
 * sense. It assumes that the values are valid.
 */
public class MGRSPoint
      extends ZonedUTMPoint {

   /**
    * UTM zones are grouped, and assigned to one of a group of 6 sets.
    */
   protected final static int NUM_100K_SETS = 6;
   /**
    * The column letters (for easting) of the lower left value, per set.
    */
   public final static int[] SET_ORIGIN_COLUMN_LETTERS = {
      'A',
      'J',
      'S',
      'A',
      'J',
      'S'
   };
   /**
    * The row letters (for northing) of the lower left value, per set.
    */
   public final static int[] SET_ORIGIN_ROW_LETTERS = {
      'A',
      'F',
      'A',
      'F',
      'A',
      'F'
   };
   /**
    * The column letters (for easting) of the lower left value, per set,, for
    * Bessel Ellipsoid.
    */
   public final static int[] BESSEL_SET_ORIGIN_COLUMN_LETTERS = {
      'A',
      'J',
      'S',
      'A',
      'J',
      'S'
   };
   /**
    * The row letters (for northing) of the lower left value, per set, for
    * Bessel Ellipsoid.
    */
   public final static int[] BESSEL_SET_ORIGIN_ROW_LETTERS = {
      'L',
      'R',
      'L',
      'R',
      'L',
      'R'
   };

   public final static int SET_NORTHING_ROLLOVER = 20000000;
   /**
    * Use 5 digits for northing and easting values, for 1 meter accuracy of
    * coordinate.
    */
   public final static int ACCURACY_1_METER = 5;
   /**
    * Use 4 digits for northing and easting values, for 10 meter accuracy of
    * coordinate.
    */
   public final static int ACCURACY_10_METER = 4;
   /**
    * Use 3 digits for northing and easting values, for 100 meter accuracy of
    * coordinate.
    */
   public final static int ACCURACY_100_METER = 3;
   /**
    * Use 2 digits for northing and easting values, for 1000 meter accuracy of
    * coordinate.
    */
   public final static int ACCURACY_1000_METER = 2;
   /**
    * Use 1 digits for northing and easting values, for 10000 meter accuracy of
    * coordinate.
    */
   public final static int ACCURACY_10000_METER = 1;

   /** The set origin column letters to use. */
   protected int[] originColumnLetters = SET_ORIGIN_COLUMN_LETTERS;
   /** The set origin row letters to use. */
   protected int[] originRowLetters = SET_ORIGIN_ROW_LETTERS;

   public final static int A = 'A';
   public final static int I = 'I';
   public final static int O = 'O';
   public final static int V = 'V';
   public final static int Z = 'Z';

   protected boolean DEBUG = false;
   protected final static Logger logger = Logger.getLogger("com.bbn.openmap.proj.coords.MGRSPoint");

   /** The String holding the MGRS coordinate value. */
   protected String mgrs;

   /**
    * Controls the number of digits that the MGRS coordinate will have, which
    * directly affects the accuracy of the coordinate. Default is
    * ACCURACY_1_METER, which indicates that MGRS coordinates will have 10
    * digits (5 easting, 5 northing) after the 100k two letter code, indicating
    * 1 meter resolution.
    */
   protected int accuracy = ACCURACY_1_METER;

   /**
    * Point to create if you are going to use the static methods to fill the
    * values in.
    */
   public MGRSPoint() {
      DEBUG = logger.isLoggable(Level.FINE);
   }

   /**
    * Constructs a new MGRS instance from a MGRS String, validating the string
    * as a MGRS coordinate.
    */
   public MGRSPoint(String mgrsString)
         throws NumberFormatException {
      this();
      setMGRS(mgrsString);
   }

   /**
    * Constructs a new MGRSPoint instance from values in another MGRSPoint.
    */
   public MGRSPoint(MGRSPoint point) {
      this();
      mgrs = point.mgrs;
      northing = point.northing;
      easting = point.easting;
      zone_number = point.zone_number;
      zone_letter = point.zone_letter;
      accuracy = point.accuracy;
   }

   /**
    * Create a MGRSPoint from standard values.
    * 
    * @param northing northing offset
    * @param easting easting offset
    * @param zoneNumber the MGRS zone number
    * @param zoneLetter the MGRS zone letter.
    */
   public MGRSPoint(double northing, double easting, int zoneNumber, char zoneLetter) {
      super(northing, easting, zoneNumber, zoneLetter);
   }

   /**
    * Construct a MGRSPoint from a LatLonPoint, assuming a WGS_84 ellipsoid.
    */
   public MGRSPoint(LatLonPoint llpoint) {
      this(llpoint, Ellipsoid.WGS_84);
   }

   /**
    * Construct a MGRSPoint from a LatLonPoint and a particular ellipsoid.
    */
   public MGRSPoint(LatLonPoint llpoint, Ellipsoid ellip) {
      this();
      LLtoMGRS(llpoint, ellip, this);
   }

   /**
    * Set the MGRS value for this Point. Will be decoded, and the MGRS values
    * figured out. You can call toLatLonPoint() to translate it to lat/lon
    * decimal degrees.
    */
   public void setMGRS(String mgrsString)
         throws NumberFormatException {
      try {
         mgrs = mgrsString.toUpperCase(); // Just to make sure.
         decode(mgrs);
      } catch (StringIndexOutOfBoundsException sioobe) {
         throw new NumberFormatException("MGRSPoint has bad string: " + mgrsString);
      } catch (NullPointerException npe) {
         // Blow off
      }
   }

   /**
    * Get the MGRS string value - the honkin' coordinate value.
    */
   public String getMGRS() {
      if (mgrs == null) {
         resolve();
      }
      return mgrs;
   }

   /**
    * Convert this MGRSPoint to a LatLonPoint, and assume a WGS_84 ellipsoid.
    */
   public LatLonPoint toLatLonPoint() {
      return toLatLonPoint(Ellipsoid.WGS_84, new LatLonPoint.Double());
   }

   /**
    * Convert this MGRSPoint to a LatLonPoint, and use the given ellipsoid.
    */
   public LatLonPoint toLatLonPoint(Ellipsoid ellip) {
      return toLatLonPoint(ellip, new LatLonPoint.Double());
   }

   /**
    * Fill in the given LatLonPoint with the converted values of this MGRSPoint,
    * and use the given ellipsoid.
    */
   public LatLonPoint toLatLonPoint(Ellipsoid ellip, LatLonPoint llpoint) {
      return MGRStoLL(this, ellip, llpoint);
   }

   /**
    * Returns a string representation of the object.
    * 
    * @return String representation
    */
   public String toString() {
      return "MGRSPoint[" + mgrs + "]";
   }

   /**
    * Create a LatLonPoint from a MGRSPoint.
    * 
    * @param mgrsp to convert.
    * @param ellip Ellipsoid for earth model.
    * @param llp a LatLonPoint to fill in values for. If null, a new LatLonPoint
    *        will be returned. If not null, the new values will be set in this
    *        object, and it will be returned.
    * @return LatLonPoint with values converted from MGRS coordinate.
    */
   public static LatLonPoint MGRStoLL(MGRSPoint mgrsp, Ellipsoid ellip, LatLonPoint llp) {
      return UTMtoLL(ellip, mgrsp.northing, mgrsp.easting, mgrsp.zone_number, MGRSPoint.MGRSZoneToUTMZone(mgrsp.zone_letter), llp);
   }

   /**
    * Create a LatLonPoint from a MGRSPoint.
    * 
    * @param ellip ellipsoid
    * @param northing northing in meters
    * @param easting easting in meters
    * @param zoneNumber zone number for mgrs
    * @param zoneLetter zone letter for mgrs
    * @param llp LatLonPoint object to use for answer, ok if null.
    * @return LatLonPoint from MGRSPoint
    */
   public static LatLonPoint MGRStoLL(Ellipsoid ellip, double northing, double easting, int zoneNumber, char zoneLetter,
                                      LatLonPoint llp) {
      return UTMtoLL(ellip, northing, easting, zoneNumber, MGRSPoint.MGRSZoneToUTMZone(zoneLetter), llp);
   }

   /**
    * Converts a LatLonPoint to a MGRS Point, assuming the WGS_84 ellipsoid.
    * 
    * @return MGRSPoint, or null if something bad happened.
    */
   public static MGRSPoint LLtoMGRS(LatLonPoint llpoint) {
      return LLtoMGRS(llpoint, Ellipsoid.WGS_84, new MGRSPoint());
   }

   /**
    * Converts a LatLonPoint to a MGRS Point.
    * 
    * @param llpoint the LatLonPoint to convert.
    * @param mgrsp a MGRSPoint to put the results in. If it's null, a MGRSPoint
    *        will be allocated.
    * @return MGRSPoint, or null if something bad happened. If a MGRSPoint was
    *         passed in, it will also be returned on a successful conversion.
    */
   public static MGRSPoint LLtoMGRS(LatLonPoint llpoint, MGRSPoint mgrsp) {
      return LLtoMGRS(llpoint, Ellipsoid.WGS_84, mgrsp);
   }

   /**
    * Create a MGRSPoint from a LatLonPoint.
    * 
    * @param llp LatLonPoint to convert.
    * @param ellip Ellipsoid for earth model.
    * @param mgrsp a MGRSPoint to fill in values for. If null, a new MGRSPoint
    *        will be returned. If not null, the new values will be set in this
    *        object, and it will be returned.
    * @return MGRSPoint with values converted from lat/lon.
    */
   public static MGRSPoint LLtoMGRS(LatLonPoint llp, Ellipsoid ellip, MGRSPoint mgrsp) {

      if (mgrsp == null || !(mgrsp instanceof MGRSPoint)) {
         mgrsp = new MGRSPoint();
      }

      // Calling LLtoUTM here results in N/S zone letters! wrong!
      mgrsp = (MGRSPoint) LLtoUTM(llp, ellip, mgrsp);
      // Need to add this to set the right letter for the latitude.
      mgrsp.zone_letter = mgrsp.getLetterDesignator(llp.getLatitude());
      mgrsp.resolve();
      return mgrsp;
   }

   /**
    * Convert MGRS zone letter to UTM zone letter, N or S.
    * 
    * @param mgrsZone
    * @return N of given zone is equal or larger than N, S otherwise.
    */
   public static char MGRSZoneToUTMZone(char mgrsZone) {
      if (Character.toUpperCase(mgrsZone) >= 'N') {
         return 'N';
      } else {
         return 'S';
      }
   }

   /**
    * Method that provides a check for MGRS zone letters. Returns an uppercase
    * version of any valid letter passed in.
    */
   protected char checkZone(char zone) {
      zone = Character.toUpperCase(zone);

      if (zone <= 'A' || zone == 'B' || zone == 'Y' || zone >= 'Z' || zone == 'I' || zone == 'O') {
         throw new NumberFormatException("Invalid MGRSPoint zone letter: " + zone);
      }

      return zone;
   }

   /**
    * Set the number of digits to use for easting and northing numbers in the
    * mgrs string, which reflects the accuracy of the coordinate. From 5 (1
    * meter) to 1 (10,000 meter).
    */
   public void setAccuracy(int value) {
      accuracy = value;
      mgrs = null;
   }

   public int getAccuracy() {
      return accuracy;
   }

   /**
    * Set the MGRS parameters from a MGRS coordinate string.
    * 
    * @param mgrsString an UPPERCASE coordinate string is expected.
    */
   protected void decode(String mgrsString)
         throws NumberFormatException {

      if (mgrsString == null || mgrsString.length() == 0) {
         throw new NumberFormatException("MGRSPoint coverting from nothing");
      }

      // Ensure an upper-case string
      mgrsString = mgrsString.toUpperCase();

      int length = mgrsString.length();

      String hunK = null;
      StringBuffer sb = new StringBuffer();
      char testChar;
      int i = 0;

      // get Zone number
      while (!Character.isLetter(testChar = mgrsString.charAt(i))) {
         if (i >= 2) {
            throw new NumberFormatException("MGRSPoint bad conversion from: " + mgrsString
                  + ", first two characters need to be a number between 1-60.");
         }
         sb.append(testChar);
         i++;
      }

      zone_number = Integer.parseInt(sb.toString());

      if (zone_number < 1 || zone_number > 60) {
         throw new NumberFormatException("MGRSPoint bad conversion from: " + mgrsString
               + ", first two characters need to be a number between 1-60.");
      }

      if (i == 0 || i + 3 > length) {
         // A good MGRS string has to be 4-5 digits long,
         // ##AAA/#AAA at least.
         throw new NumberFormatException("MGRSPoint bad conversion from: " + mgrsString
               + ", MGRS string must be at least 4-5 digits long");
      }

      zone_letter = mgrsString.charAt(i++);

      // Should we check the zone letter here? Why not.
      if (zone_letter <= 'A' || zone_letter == 'B' || zone_letter == 'Y' || zone_letter >= 'Z' || zone_letter == 'I'
            || zone_letter == 'O') {
         throw new NumberFormatException("MGRSPoint zone letter " + (char) zone_letter + " not handled: " + mgrsString);
      }

      hunK = mgrsString.substring(i, i += 2);

      // Validate, check the zone, make sure each letter is between A-Z, not I
      // or O
      char char1 = hunK.charAt(0);
      char char2 = hunK.charAt(1);
      if (char1 < 'A' || char2 < 'A' || char1 > 'Z' || char2 > 'Z' || char1 == 'I' || char2 == 'I' || char1 == 'O' || char2 == 'O') {
         throw new NumberFormatException("MGRSPoint bad conversion from " + mgrsString + ", invalid 100k designator");
      }

      int set = get100kSetForZone(zone_number);

      float east100k = getEastingFromChar(char1, set);
      float north100k = getNorthingFromChar(char2, set);

      // We have a bug where the northing may be 2000000 too low.
      // How do we know when to roll over?

      while (north100k < getMinNorthing(zone_letter)) {
         north100k += 2000000;
      }

      // calculate the char index for easting/northing separator
      int remainder = length - i;

      if (remainder % 2 != 0) {
         throw new NumberFormatException(
                                         "MGRSPoint has to have an even number \nof digits after the zone letter and two 100km letters - front \nhalf for easting meters, second half for \nnorthing meters"
                                               + mgrsString);
      }

      int sep = remainder / 2;

      float sepEasting = 0f;
      float sepNorthing = 0f;

      if (sep > 0) {
         if (DEBUG)
            logger.fine(" calculating e/n from " + mgrs.substring(i));
         float accuracyBonus = 100000f / (float) Math.pow(10, sep);
         if (DEBUG)
            logger.fine(" calculated accuracy bonus as  " + accuracyBonus);
         String sepEastingString = mgrsString.substring(i, i + sep);
         if (DEBUG)
            logger.fine(" parsed easting as " + sepEastingString);
         sepEasting = Float.parseFloat(sepEastingString) * accuracyBonus;
         String sepNorthingString = mgrsString.substring(i + sep);
         if (DEBUG)
            logger.fine(" parsed northing as " + sepNorthingString);
         sepNorthing = Float.parseFloat(sepNorthingString) * accuracyBonus;
      }

      easting = sepEasting + east100k;
      northing = sepNorthing + north100k;

      if (DEBUG) {
         logger.fine("Decoded " + mgrsString + " as zone number: " + zone_number + ", zone letter: " + zone_letter + ", easting: "
               + easting + ", northing: " + northing + ", 100k: " + hunK);
      }
   }

   /**
    * Create the mgrs string based on the internal MGRS parameter settings,
    * should be called if the accuracy changes.
    * 
    * @param digitAccuracy The number of digits to use for the northing and
    *        easting numbers. 5 digits reflect a 1 meter accuracy, 4 - 10 meter,
    *        3 - 100 meter, 2 - 1000 meter, 1 - 10,000 meter.
    */
   public void resolve(int digitAccuracy) {
      setAccuracy(digitAccuracy);
      resolve();
   }

   /**
    * Create the mgrs string based on the internal MGRS parameter settings,
    * using the accuracy set in the MGRSPoint.
    */
   public void resolve() {
      if (zone_letter == 'Z') {
         mgrs = "Latitude limit exceeded";
      } else {
         StringBuffer sb =
               new StringBuffer(Integer.toString(zone_number)).append(zone_letter)
                                                              .append(get100kID(easting, northing, zone_number));
         StringBuffer seasting = new StringBuffer(Integer.toString((int) easting));
         StringBuffer snorthing = new StringBuffer(Integer.toString((int) northing));

         if (DEBUG) {
            logger.fine(" Resolving MGRS from easting: " + seasting + " derived from " + easting + ", and northing: " + snorthing
                  + " derived from " + northing);
         }

         while (accuracy + 1 > seasting.length()) {
            seasting.insert(0, '0');
         }

         // We have to be careful here, the 100k values shouldn't
         // be
         // used for calculating stuff here.

         while (accuracy + 1 > snorthing.length()) {
            snorthing.insert(0, '0');
         }

         while (snorthing.length() > 6) {
            snorthing.deleteCharAt(0);
         }

         if (DEBUG) {
            logger.fine(" -- modified easting: " + seasting + " and northing: " + snorthing);
         }

         try {
            sb.append(seasting.substring(1, accuracy + 1)).append(snorthing.substring(1, accuracy + 1));

            mgrs = sb.toString();
         } catch (IndexOutOfBoundsException ioobe) {
            mgrs = null;
         }
      }
   }

   /**
    * Given a MGRS/UTM zone number, figure out the MGRS 100K set it is in.
    */
   protected int get100kSetForZone(int i) {
      int set = i % NUM_100K_SETS;
      if (set == 0)
         set = NUM_100K_SETS;
      return set;
   }

   /**
    * Provided so that extensions to this class can provide different origin
    * letters, in case of different ellipsoids. The int[] represents all of the
    * first letters in the bottom left corner of each set box, as shown in an
    * MGRS 100K box layout.
    */
   protected int[] getOriginColumnLetters() {
      return originColumnLetters;
   }

   /**
    * Provided so that extensions to this class can provide different origin
    * letters, in case of different ellipsoids. The int[] represents all of the
    * first letters in the bottom left corner of each set box, as shown in an
    * MGRS 100K box layout.
    */
   protected void setOriginColumnLetters(int[] letters) {
      originColumnLetters = letters;
   }

   /**
    * Provided so that extensions to this class can provide different origin
    * letters, in case of different ellipsoids. The int[] represents all of the
    * second letters in the bottom left corner of each set box, as shown in an
    * MGRS 100K box layout.
    */
   protected int[] getOriginRowLetters() {
      return originRowLetters;
   }

   /**
    * Provided so that extensions to this class can provide different origin
    * letters, in case of different ellipsoids. The int[] represents all of the
    * second letters in the bottom left corner of each set box, as shown in an
    * MGRS 100K box layout.
    */
   protected void setOriginRowLetters(int[] letters) {
      originRowLetters = letters;
   }

   /**
    * Get the two letter 100k designator for a given MGRS/UTM easting, northing and
    * zone number value.
    */
   protected String get100kID(double easting, double northing, int zone_number) {
      int set = get100kSetForZone(zone_number);
      int setColumn = ((int) easting / 100000);
      int setRow = ((int) northing / 100000) % 20;
      return get100kID(setColumn, setRow, set);
   }

   /**
    * Given the first letter from a two-letter MGRS 100k zone, and given the
    * MGRS table set for the zone number, figure out the easting value that
    * should be added to the other, secondary easting value.
    */
   protected float getEastingFromChar(char e, int set) {
      int baseCol[] = getOriginColumnLetters();
      // colOrigin is the letter at the origin of the set for the
      // column
      int curCol = baseCol[set - 1];
      float eastingValue = 100000f;
      boolean rewindMarker = false;

      while (curCol != e) {
         curCol++;
         if (curCol == I)
            curCol++;
         if (curCol == O)
            curCol++;
         if (curCol > Z) {
            if (rewindMarker) {
               throw new NumberFormatException("Bad character: " + e);
            }
            curCol = A;
            rewindMarker = true;
         }
         eastingValue += 100000f;
      }

      if (DEBUG) {
         logger.fine("Easting value for " + (char) e + " from set: " + set + ", col: " + curCol + " is " + eastingValue);
      }
      return eastingValue;
   }

   /**
    * Given the second letter from a two-letter MGRS 100k zone, and given the
    * MGRS table set for the zone number, figure out the northing value that
    * should be added to the other, secondary northing value. You have to
    * remember that Northings are determined from the equator, and the vertical
    * cycle of letters mean a 2000000 additional northing meters. This happens
    * approx. every 18 degrees of latitude. This method does *NOT* count any
    * additional northings. You have to figure out how many 2000000 meters need
    * to be added for the zone letter of the MGRS coordinate.
    * 
    * @param n second letter of the MGRS 100k zone
    * @param set the MGRS table set number, which is dependent on the MGRS/UTM zone
    *        number.
    */
   protected float getNorthingFromChar(char n, int set) {

      if (n > 'V') {
         throw new NumberFormatException("MGRSPoint given invalid Northing " + n);
      }

      int baseRow[] = getOriginRowLetters();
      // rowOrigin is the letter at the origin of the set for the
      // column
      int curRow = baseRow[set - 1];
      float northingValue = 0f;
      boolean rewindMarker = false;

      while (curRow != n) {
         curRow++;
         if (curRow == I)
            curRow++;
         if (curRow == O)
            curRow++;
         // fixing a bug making whole application hang in this loop
         // when 'n' is a wrong character
         if (curRow > V) {
            if (rewindMarker) { // making sure that this loop ends
               throw new NumberFormatException("Bad character: " + n);
            }
            curRow = A;
            rewindMarker = true;
         }
         northingValue += 100000f;
      }

      if (DEBUG) {
         logger.fine("Northing value for " + (char) n + " from set: " + set + ", row: " + curRow + " is " + northingValue);
      }

      return northingValue;
   }

   /**
    * Get the two-letter MGRS 100k designator given information translated from
    * the MGRS/UTM northing, easting and zone number.
    * 
    * @param setColumn the column index as it relates to the MGRS 100k set
    *        spreadsheet, created from the MGRS/UTM easting. Values are 1-8.
    * @param setRow the row index as it relates to the MGRS 100k set
    *        spreadsheet, created from the MGRS/UTM northing value. Values are from
    *        0-19.
    * @param set the set block, as it relates to the MGRS 100k set spreadsheet,
    *        created from the MGRS/UTM zone. Values are from 1-60.
    * @return two letter MGRS 100k code.
    */
   protected String get100kID(int setColumn, int setRow, int set) {

      if (DEBUG) {
         System.out.println("set (" + set + ") column = " + setColumn + ", row = " + setRow);
      }

      int baseCol[] = getOriginColumnLetters();
      int baseRow[] = getOriginRowLetters();

      // colOrigin and rowOrigin are the letters at the origin of
      // the set
      int colOrigin = baseCol[set - 1];
      int rowOrigin = baseRow[set - 1];

      if (DEBUG) {
         System.out.println("starting at = " + (char) colOrigin + (char) rowOrigin);
      }

      // colInt and rowInt are the letters to build to return
      int colInt = colOrigin + setColumn - 1;
      int rowInt = rowOrigin + setRow;
      boolean rollover = false;

      if (colInt > Z) {
         colInt = colInt - Z + A - 1;
         rollover = true;
         if (DEBUG)
            System.out.println("rolling over col, new value: " + (char) colInt);
      }

      if (colInt == I || (colOrigin < I && colInt > I) || ((colInt > I || colOrigin < I) && rollover)) {
         colInt++;
         if (DEBUG)
            System.out.println("skipping I in col, new value: " + (char) colInt);
      }
      if (colInt == O || (colOrigin < O && colInt > O) || ((colInt > O || colOrigin < O) && rollover)) {
         colInt++;
         if (DEBUG)
            System.out.println("skipping O in col, new value: " + (char) colInt);
         if (colInt == I) {
            colInt++;
            if (DEBUG)
               System.out.println("  hit I, new value: " + (char) colInt);
         }
      }

      if (colInt > Z) {
         colInt = colInt - Z + A - 1;
         if (DEBUG)
            System.out.println("rolling(2) col, new value: " + (char) rowInt);
      }

      if (rowInt > V) {
         rowInt = rowInt - V + A - 1;
         rollover = true;
         if (DEBUG)
            System.out.println("rolling over row, new value: " + (char) rowInt);
      } else {
         rollover = false;
      }

      if (rowInt == I || (rowOrigin < I && rowInt > I) || ((rowInt > I || rowOrigin < I) && rollover)) {
         rowInt++;
         if (DEBUG)
            System.out.println("skipping I in row, new value: " + (char) rowInt);
      }

      if (rowInt == O || (rowOrigin < O && rowInt > O) || ((rowInt > O || rowOrigin < O) && rollover)) {
         rowInt++;
         if (DEBUG)
            System.out.println("skipping O in row, new value: " + (char) rowInt);
         if (rowInt == I) {
            rowInt++;
            if (DEBUG)
               System.out.println("  hit I, new value: " + (char) rowInt);
         }
      }

      if (rowInt > V) {
         rowInt = rowInt - V + A - 1;
         if (DEBUG)
            System.out.println("rolling(2) row, new value: " + (char) rowInt);
      }

      String twoLetter = (char) colInt + "" + (char) rowInt;

      if (DEBUG) {
         System.out.println("ending at = " + twoLetter);
      }

      return twoLetter;
   }

   /**
    * Testing method, used to print out the MGRS 100k two letter set tables.
    */
   protected void print100kSets() {
      StringBuffer sb = null;
      for (int set = 1; set <= 6; set++) {
         System.out.println("-------------\nFor 100K Set " + set + ":\n-------------\n");
         for (int i = 19; i >= 0; i -= 1) {
            sb = new StringBuffer(Integer.toString(i * 100000)).append("\t| ");

            for (int j = 1; j <= 8; j++) {
               sb.append(" ").append(get100kID(j, i, set));
            }

            sb.append(" |");
            System.out.println(sb);
         }
      }
   }

   /**
    * The function getMinNorthing returns the minimum northing value of a MGRS
    * zone.
    * 
    * portted from Geotrans' c Latitude_Band_Value structure table. zoneLetter :
    * MGRS zone (input)
    */

   protected float getMinNorthing(char zoneLetter)
         throws NumberFormatException {
      float northing;
      switch (zoneLetter) {
         case 'C':
            northing = 1100000.0f;
            break;
         case 'D':
            northing = 2000000.0f;
            break;
         case 'E':
            northing = 2800000.0f;
            break;
         case 'F':
            northing = 3700000.0f;
            break;
         case 'G':
            northing = 4600000.0f;
            break;
         case 'H':
            northing = 5500000.0f;
            break;
         case 'J':
            northing = 6400000.0f;
            break;
         case 'K':
            northing = 7300000.0f;
            break;
         case 'L':
            northing = 8200000.0f;
            break;
         case 'M':
            northing = 9100000.0f;
            break;
         case 'N':
            northing = 0.0f;
            break;
         case 'P':
            northing = 800000.0f;
            break;
         case 'Q':
            northing = 1700000.0f;
            break;
         case 'R':
            northing = 2600000.0f;
            break;
         case 'S':
            northing = 3500000.0f;
            break;
         case 'T':
            northing = 4400000.0f;
            break;
         case 'U':
            northing = 5300000.0f;
            break;
         case 'V':
            northing = 6200000.0f;
            break;
         case 'W':
            northing = 7000000.0f;
            break;
         case 'X':
            northing = 7900000.0f;
            break;
         default:
            northing = -1.0f;
      }
      if (northing >= 0.0) {
         return northing;
      } else {
         throw new NumberFormatException("Invalid zone letter: " + zone_letter);
      }

   }

   private static void runTests(String fName, String inType) {

      LineNumberReader lnr = null;
      PrintStream pos = null;
      String record = null;
      StringBuffer outStr1 = new StringBuffer();
      StringBuffer outStr2 = new StringBuffer();

      try {

         /*
          * File inFile = new File(fName + ".dat"); File outFile = new
          * File(fName + ".out"); FileInputStream fis = new
          * FileInputStream(inFile); FileOutputStream fos = new
          * FileOutputStream(outFile); BufferedInputStream bis = new
          * BufferedInputStream(fis);
          */
         pos = new PrintStream(new FileOutputStream(new File(fName + ".out")));
         lnr = new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(fName)))));

         if (inType.equalsIgnoreCase("MGRS")) {
            outStr1.append("MGRS to LatLonPoint\n\tMGRS\t\tLatitude   Longitude\n");
            outStr2.append("MGRS to UTM\n\tMGRS\t\tZone Easting Northing\n");
         } else if (inType.equalsIgnoreCase("UTM")) {
            outStr1.append("UTM to LatLonPoint\n\tUTM\t\tLatitude   Longitude\n");
            outStr2.append("UTM to MGRS\n\tUTM\t\tMGRS\n");
         } else if (inType.equalsIgnoreCase("LatLon")) {
            outStr1.append("LatLonPoint to UTM\nLatitude   Longitude\t\tZone Easting Northing     \n");
            outStr2.append("LatLonPoint to MGRS\nLatitude   Longitude\t\tMGRS\n");
         }

         while ((record = lnr.readLine()) != null) {
            if (inType.equalsIgnoreCase("MGRS")) {
               try {
                  MGRSPoint mgrsp = new MGRSPoint(record);
                  record = record.trim();
                  mgrsp.decode(record);

                  outStr1.append(record).append(" is ").append(mgrsp.toLatLonPoint()).append("\n");
                  outStr2.append(record).append(" to UTM: ").append(mgrsp.zone_number).append(" ").append(mgrsp.easting)
                         .append(" ").append(mgrsp.northing).append("\n");
               } catch (NumberFormatException nfe) {
                  logger.warning(nfe.getMessage());
               }

            } else if (inType.equalsIgnoreCase("UTM")) {
               MGRSPoint mgrsp;
               UTMPoint utmp;
               float e, n;
               int z;
               char zl;
               String tmp;
               record = record.trim();
               tmp = record.substring(0, 2);
               z = Integer.parseInt(tmp);
               tmp = record.substring(5, 11);
               e = Float.parseFloat(tmp);
               tmp = record.substring(12, 19);
               n = Float.parseFloat(tmp);
               zl = record.charAt(3);
               utmp = new UTMPoint(n, e, z, zl);
               LatLonPoint llp = utmp.toLatLonPoint();
               mgrsp = LLtoMGRS(llp);
               outStr1.append(record).append(" is ").append(llp).append(" back to ").append(LLtoUTM(llp)).append("\n");
               outStr2.append(record).append(" is ").append(mgrsp).append("\n");
            } else if (inType.equalsIgnoreCase("LatLon")) {
               float lat, lon;
               int index;
               String tmp;
               record = record.trim();
               index = record.indexOf("\040");
               if (index < 0) {
                  index = record.indexOf("\011");
               }
               tmp = record.substring(0, index);
               lat = Float.parseFloat(tmp);
               tmp = record.substring(index);
               lon = Float.parseFloat(tmp);
               LatLonPoint llp = new LatLonPoint.Double(lat, lon);
               // UTMPoint utmp = LLtoUTM(llp);
               MGRSPoint mgrsp = LLtoMGRS(llp);
               outStr1.append(record).append(" to UTM: ").append(mgrsp.zone_number).append(" ").append(mgrsp.easting).append(" ")
                      .append(mgrsp.northing).append("\n");
               outStr2.append(record).append("    ->    ").append(mgrsp.mgrs).append("\n");
            }

         }

      } catch (IOException e) {
         // catch io errors from FileInputStream or readLine()
         System.out.println("IO error: " + e.getMessage());

      } finally {
         if (pos != null) {
            pos.print(outStr1.toString());
            pos.print("\n");
            pos.print(outStr2.toString());
            pos.close();
         }
         // if the file opened okay, make sure we close it
         if (lnr != null) {
            try {
               lnr.close();
            } catch (IOException ioe) {
            }
         }

      }

   }

   public static void main(String[] argv) {
      Debug.init();

      ArgParser ap = new ArgParser("MGRSPoint");
      ap.add("mgrs", "Print Latitude and Longitude for MGRS value", 1);
      ap.add("latlon", "Print MGRS for Latitude and Longitude values", 2, true);
      ap.add("sets", "Print the MGRS 100k table");
      ap.add("altsets", "Print the MGRS 100k table for the Bessel ellipsoid");
      ap.add("rtc", "Run test case, with filename and input data type [MGRS | UTM | LatLon]", 2);

      if (!ap.parse(argv)) {
         ap.printUsage();
         System.exit(0);
      }

      String arg[];
      arg = ap.getArgValues("sets");
      if (arg != null) {
         new MGRSPoint().print100kSets();
      }

      arg = ap.getArgValues("altsets");
      if (arg != null) {
         MGRSPoint mgrsp = new MGRSPoint();
         mgrsp.setOriginColumnLetters(BESSEL_SET_ORIGIN_COLUMN_LETTERS);
         mgrsp.setOriginRowLetters(BESSEL_SET_ORIGIN_ROW_LETTERS);
         mgrsp.print100kSets();
      }

      arg = ap.getArgValues("mgrs");
      if (arg != null) {
         try {
            MGRSPoint mgrsp = new MGRSPoint(arg[0]);
            Debug.output(arg[0] + " is " + mgrsp.toLatLonPoint());
         } catch (NumberFormatException nfe) {
            Debug.error(nfe.getMessage());
         }
      }

      arg = ap.getArgValues("latlon");
      if (arg != null) {
         try {

            float lat = Float.parseFloat(arg[0]);
            float lon = Float.parseFloat(arg[1]);

            LatLonPoint llp = new LatLonPoint.Double(lat, lon);
            MGRSPoint mgrsp = LLtoMGRS(llp);
            UTMPoint utmp = LLtoUTM(llp);

            if (utmp.zone_letter == 'Z') {
               Debug.output(llp + "to UTM: latitude limit exceeded.");
            } else {
               Debug.output(llp + " is " + utmp);
            }

            Debug.output(llp + " is " + mgrsp);

         } catch (NumberFormatException nfe) {
            Debug.error("The numbers provided:  " + argv[0] + ", " + argv[1] + " aren't valid");
         }
      }

      arg = ap.getArgValues("rtc");
      if (arg != null) {
         runTests(arg[0], arg[1]);
      }

   }
}
