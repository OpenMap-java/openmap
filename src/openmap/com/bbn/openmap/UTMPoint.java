package com.bbn.openmap;

import com.bbn.openmap.proj.Ellipsoid;
import com.bbn.openmap.proj.ProjMath;

/**
 * A class representing a UTM co-ordinate <p> 
 *
 * Adapted to Java by Colin
 * Mummery (colin_mummery@yahoo.com) from C++ code by Chuck Gantz
 * (chuck.gantz@globalstar.com) 
 */
public class UTMPoint {
    
    /**
     * The northing component of the coordinate.
     */
    public float northing;
    /**
     * The easting component of the coordinate.
     */
    public float easting;
    /**
     * The zone number of the coordinate, must be between 1 and 60.
     */
    public int zone_number;
    /**
     * The zone letter of the coordinate.
     */
    public char zone_letter;
    
    /**
     * Point to create if you are going to use the static methods to
     * fill the values in.  
     */
    public UTMPoint() {}

    /**
     * Constructs a new UTM instance.
     *
     * @param northing The northing component.
     * @param easting The easting component.
     * @param zone_number The zone of the coordinate.
     * @param zone_number The zone of the coordinate.
     */
    public UTMPoint(float northing, float easting, 
		    int zone_number, char zone_letter) {
	this.northing=northing; 
	this.easting=easting; 
	this.zone_number=zone_number; 
	this.zone_letter=zone_letter;
    }

    /**
     * Contructs a new UTMPoint instance from values in another UTMPoint.
     */
    public UTMPoint(UTMPoint point) {
	this(point.northing, point.easting, 
	     point.zone_number, point.zone_letter);
    }

    /**
     * Contruct a UTMPoint from a LatLonPoint, assuming a WGS_84 ellipsoid.
     */
    public UTMPoint(LatLonPoint llpoint) {
	this(llpoint, Ellipsoid.WGS_84);
    }

    /**
     * Construct a UTMPoint from a LatLonPoint and a particular
     * ellipsoid.  
     */
    public UTMPoint(LatLonPoint llpoint, Ellipsoid ellip) {
	this();
	LLtoUTM(llpoint, ellip, this);
    }

    /**
     * Convert this UTMPoint to a LatLonPoint, and assume a WGS_84
     * ellisoid.
     */
    public LatLonPoint toLatLonPoint() {
	return UTMtoLL(this, Ellipsoid.WGS_84, new LatLonPoint());
    }

    /**
     * Convert this UTMPoint to a LatLonPoint, and use the given
     * ellipsoid.
     */
    public LatLonPoint toLatLonPoint(Ellipsoid ellip) {
	return UTMtoLL(this, ellip, new LatLonPoint());
    }

    /**
     * Fill in the given LatLonPoint with the converted values of this
     * UTMPoint, and use the given ellipsoid.  
     */
    public LatLonPoint toLatLonPoint(Ellipsoid ellip, LatLonPoint llpoint) {
	return UTMtoLL(this, ellip, llpoint);
    }

    /**
     * Returns a string representation of the object.
     * @return String representation
     */
    public String toString () {
	return "UTMPoint[northing=" + northing + ", easting=" + 
	    easting + ", zone_number="+zone_number+", zone_letter="+zone_letter+"]";
    }

    /**
     * Converts a LatLonPoint to a UTM Point, assuming the WGS_84 ellipsoid.
     * @return UTMPoint, or null if something bad happened.
     */
    public static UTMPoint LLtoUTM(LatLonPoint llpoint) {
	return LLtoUTM(llpoint, Ellipsoid.WGS_84, new UTMPoint());
    }

    /**
     * Converts a LatLonPoint to a UTM Point.
     * @param llpoint the LatLonPoint to convert.
     * @param utmpoint a UTMPoint to put the results in.  If it's
     * null, a UTMPoint will be allocated.
     * @return UTMPoint, or null if something bad happened.  If a
     * UTMPoint was passed in, it will also be returned on a
     * successful conversion.
     */
    public static UTMPoint LLtoUTM(LatLonPoint llpoint, UTMPoint utmpoint) {
	return LLtoUTM(llpoint, Ellipsoid.WGS_84, utmpoint);
    }

    /**
     * Converts a set of Longitude and Latitude co-ordinates to UTM given an ellipsoid
     *
     * @param ellip an ellipsoid definition.
     * @param Lat A float value for the latitude to be converted.
     * @param Long A float value for the longitude to be converted.
     * @param utmpoint A UTMPoint instance to put the results in.  If
     * null, a new UTMPoint will be allocated.
     * @return A UTM class instance containing the value of
     * <code>null</code> if conversion failed.    If you pass
     * in a UTMPoint, it will be returned as well if successful.
     */
    public static UTMPoint LLtoUTM(LatLonPoint llpoint, Ellipsoid ellip, UTMPoint utmpoint) {
	
	double Lat=llpoint.getLatitude(); 
	double Long=llpoint.getLongitude();
	double a = ellip.radius;
	double eccSquared = ellip.eccsq;
	double k0 = 0.9996;
	
	double LongOrigin;
	double eccPrimeSquared;
	double N, T, C, A, M;
	
	//Make sure the longitude is between -180.00 .. 179.9
	double LongTemp = (Long+180)-((int)((Long+180)/360))*360-180; // -180.00 .. 179.9;

	double LatRad = llpoint.radlat_;
	double LongRad = llpoint.radlon_;
	double LongOriginRad;
	int    ZoneNumber;
	
	ZoneNumber = (int)((LongTemp + 180)/6) + 1;
	
	if ( Lat >= 56.0f && Lat < 64.0f && 
	     LongTemp >= 3.0f && LongTemp < 12.0f ) {
	    ZoneNumber = 32;
	}
	
	// Special zones for Svalbard
	if( Lat >= 72.0f && Lat < 84.0f ) {
	    if(LongTemp >= 0.0f  && LongTemp <  9.0f ) ZoneNumber = 31;
	    else if( LongTemp >= 9.0f  && LongTemp < 21.0f ) ZoneNumber = 33;
	    else if( LongTemp >= 21.0f && LongTemp < 33.0f ) ZoneNumber = 35;
	    else if( LongTemp >= 33.0f && LongTemp < 42.0f ) ZoneNumber = 37;
	}
	LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
	LongOriginRad = ProjMath.degToRad(LongOrigin);
	
	eccPrimeSquared = (eccSquared)/(1-eccSquared);
	
	N = a/Math.sqrt(1-eccSquared*Math.sin(LatRad)*Math.sin(LatRad));
	T = Math.tan(LatRad)*Math.tan(LatRad);
	C = eccPrimeSquared*Math.cos(LatRad)*Math.cos(LatRad);
	A = Math.cos(LatRad)*(LongRad-LongOriginRad);
	
	M = a*((1 - eccSquared/4 - 3*eccSquared*eccSquared/64 -
		5*eccSquared*eccSquared*eccSquared/256)*LatRad - 
	       (3*eccSquared/8 + 3*eccSquared*eccSquared/32 + 
		45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(2*LatRad) +
	       (15*eccSquared*eccSquared/256 + 
		45*eccSquared*eccSquared*eccSquared/1024)*Math.sin(4*LatRad) - 
	       (35*eccSquared*eccSquared*eccSquared/3072)*Math.sin(6*LatRad));
	
	float UTMEasting = 
	    (float)(k0*N*(A+(1-T+C)*A*A*A/6.0d + 
			  (5-18*T+T*T+72*C-58*eccPrimeSquared)*A*A*A*A*A/120.0d) + 500000.0d);
	
	float UTMNorthing = 
	    (float)(k0*(M+N*Math.tan(LatRad)*
			(A*A/2+(5-T+9*C+4*C*C)*A*A*A*A/24.0d
			 + (61-58*T+T*T+600*C-330*eccPrimeSquared)*A*A*A*A*A*A/720.0d)));
	if(Lat < 0.0f) {
	    UTMNorthing += 10000000.0f; //10000000 meter offset for southern hemisphere
	}
	
	if (utmpoint != null) {
	    utmpoint.northing = UTMNorthing;
	    utmpoint.easting = UTMEasting;
	    utmpoint.zone_number = ZoneNumber;
	    utmpoint.zone_letter = UTMLetterDesignator(Lat);
	    return utmpoint;
	} else {
	    return new UTMPoint(UTMNorthing, UTMEasting, ZoneNumber, UTMLetterDesignator(Lat));
	}
    }

    /**
     * Determines the correct UTM letter designator for the given latitude
     * returns 'Z' if latitude is outside the UTM limits of 84N to 80S.
     * 
     * @param Long The float value of the latitude.
     *
     * @return A char value which is the UTM zone letter.
     */
    protected static char UTMLetterDesignator(double Lat) {

       //This is here as an error flag to show that the Latitude is
       //outside UTM limits
       char LetterDesignator='Z'; 
       
       if((84 >= Lat) && (Lat >= 72)) LetterDesignator = 'X';
       else if((72 > Lat) && (Lat >= 64)) LetterDesignator = 'W';
       else if((64 > Lat) && (Lat >= 56)) LetterDesignator = 'V';
       else if((56 > Lat) && (Lat >= 48)) LetterDesignator = 'U';
       else if((48 > Lat) && (Lat >= 40)) LetterDesignator = 'T';
       else if((40 > Lat) && (Lat >= 32)) LetterDesignator = 'S';
       else if((32 > Lat) && (Lat >= 24)) LetterDesignator = 'R';
       else if((24 > Lat) && (Lat >= 16)) LetterDesignator = 'Q';
       else if((16 > Lat) && (Lat >= 8)) LetterDesignator = 'P';
       else if(( 8 > Lat) && (Lat >= 0)) LetterDesignator = 'N';
       else if(( 0 > Lat) && (Lat >= -8)) LetterDesignator = 'M';
       else if((-8> Lat) && (Lat >= -16)) LetterDesignator = 'L';
       else if((-16 > Lat) && (Lat >= -24)) LetterDesignator = 'K';
       else if((-24 > Lat) && (Lat >= -32)) LetterDesignator = 'J';
       else if((-32 > Lat) && (Lat >= -40)) LetterDesignator = 'H';
       else if((-40 > Lat) && (Lat >= -48)) LetterDesignator = 'G';
       else if((-48 > Lat) && (Lat >= -56)) LetterDesignator = 'F';
       else if((-56 > Lat) && (Lat >= -64)) LetterDesignator = 'E';
       else if((-64 > Lat) && (Lat >= -72)) LetterDesignator = 'D';
       else if((-72 > Lat) && (Lat >= -80)) LetterDesignator = 'C';
       return LetterDesignator;
   }

    /**
     * Converts UTM coords to lat/long given an ellipsoid given an
     * instance of UTMPoint.
     *
     * @param utm_point A UTMPoint instance.
     * @param ellip a ellipsoid definition.
     * @param llpoint a LatLonPoint, if you want it to be filled in
     * with the results.  If null, a new LatLonPoint will be
     * allocated.
     * @return A LatLonPoint class instance containing the lat/long
     * value, or <code>null</code> if conversion failed.  If you pass
     * in a LatLonPoint, it will be returned as well, if successful.
     */
    public static LatLonPoint UTMtoLL(UTMPoint utm_point, Ellipsoid ellip, 
				      LatLonPoint llpoint) {
	return UTMtoLL(ellip, utm_point.northing, utm_point.easting, 
		       utm_point.zone_number, utm_point.zone_letter, 
		       llpoint);
    }

    /**
     * Converts UTM coords to lat/long given an ellipsoid. This is a
     * convenience class where the Zone can be specified as a single
     * string eg."61N" which is then broken down into the ZoneNumber
     * and ZoneLetter.
     *
     * @param ellip an ellipsoid definition.
     * @param UTMNorthing A float value for the northing to be
     * converted.
     * @param UTMEasting A float value for the easting to be converted.
     * @param UTMZone A String value for the UTM zone eg."61N".
     * @param llpoint a LatLonPoint, if you want it to be filled in
     * with the results.  If null, a new LatLonPoint will be
     * allocated.
     * @return A LatLonPoint class instance containing the lat/long
     * value, or <code>null</code> if conversion failed.  If you pass
     * in a LatLonPoint, it will be returned as well, if successful.
     */
    public static LatLonPoint UTMtoLL(Ellipsoid ellip,
				      float UTMNorthing,
				      float UTMEasting,
				      String UTMZone,
				      LatLonPoint llpoint) {
	
	//without the zone we can't calculate the Lat and Long
	if (UTMZone==null || UTMZone.equals("")) {
	    return null;
	}

	int ZoneNumber=1;
	char ZoneLetter='N'; //northern hemisphere by default if no character is found
	
	//Break out the Zone number and zone letter from the UTMZone
	//string We assume the string is a valid zone with a number
	//followed by a zone letter If there is no Letter we assume
	//that it's the Northern hemisphere
	int ln=UTMZone.length()-1;
	if (ln > 0) { 
	    //If it's Zero then there is only one character and it must be the Zone number
	    ZoneLetter = UTMZone.charAt(ln);
	    if(!Character.isLetter(ZoneLetter)) {
		//No letter so assume it's missing & default to 'N'
		ZoneLetter='N';
		ln++;
	    }
	}

	//convert the number but catch the exception if it's not valid
	try{
	    ZoneNumber = Integer.parseInt(UTMZone.substring(0,ln));
	} catch (NumberFormatException nfe) {
	    return null;
	}
	
	return UTMtoLL(ellip,
		       UTMNorthing, UTMEasting,
		       ZoneNumber, ZoneLetter, 
		       llpoint);
    }

    /**
     * Converts UTM coords to lat/long given an ellipsoid. This is a
     * convenience class where the exact Zone letter is not
     * known. Instead only the hemisphere needs to be indicated.
     *
     * @param ellip an ellipsoid definition.
     * @param UTMNorthing A float value for the northing to be
     * converted.
     * @param UTMEasting A float value for the easting to be converted.
     * @param ZoneNumber An int value indicating the float number.
     * @param isnorthern A boolean which is true for the northern
     * hemisphere otherwise false for the southern.
     * @param llpoint a LatLonPoint, if you want it to be filled in
     * with the results.  If null, a new LatLonPoint will be
     * allocated.
     * @return A LatLonPoint class instance containing the lat/long
     * value, or <code>null</code> if conversion failed.  If you pass
     * in a LatLonPoint, it will be returned as well, if successful.
     */
    public static LatLonPoint UTMtoLL(Ellipsoid ellip,
				      float UTMNorthing, float UTMEasting,
				      int ZoneNumber, boolean isnorthern, 
				      LatLonPoint llpoint) {

	return UTMtoLL(ellip, UTMNorthing, UTMEasting, ZoneNumber, 
		       (isnorthern)? 'N' : 'M', new LatLonPoint());
    }

    /**
     * Converts UTM coords to lat/long given an ellipsoid.<p>
     * Equations from USGS Bulletin 1532<br>
     * East Longitudes are positive, West longitudes are negative.<br>
     * North latitudes are positive, South latitudes are negative.<br>
     *
     * @param ellip an ellipsoid definition.
     * @param UTMNorthing A float value for the northing to be converted.
     * @param UTMEasting A float value for the easting to be converted.
     * @param ZoneNumber An int value specifiying the UTM zone number.
     * @param ZoneLetter A char value specifying the ZoneLetter within the ZoneNumber.
     * @param llpoint a LatLonPoint, if you want it to be filled in
     * with the results.  If null, a new LatLonPoint will be
     * allocated.
     * @return A LatLonPoint class instance containing the lat/long
     * value, or <code>null</code> if conversion failed.  If you pass
     * in a LatLonPoint, it will be returned as well, if successful.
     */
    public static LatLonPoint UTMtoLL(Ellipsoid ellip, 
				      float UTMNorthing, float UTMEasting,
				      int ZoneNumber, char ZoneLetter, 
				      LatLonPoint llpoint) {
	
	//check the ZoneNummber is valid
	if (ZoneNumber<0 || ZoneNumber>60) {
	    return null;
	}

	double k0 = 0.9996;
	double a = ellip.radius;
	double eccSquared = ellip.eccsq;
	double eccPrimeSquared;
	double e1 = (1-Math.sqrt(1-eccSquared))/(1+Math.sqrt(1-eccSquared));
	double N1, T1, C1, R1, D, M;
	double LongOrigin;
	double mu, phi1, phi1Rad;
	double x, y;
	int NorthernHemisphere; //1 for northern hemispher, 0 for southern
	
	x = UTMEasting - 500000.0d; //remove 500,000 meter offset for longitude
	y = UTMNorthing;
	
	//We must know somehow if we are in the Northern or Southern
	//hemisphere, this is the only time we use the letter So even
	//if the Zone letter isn't exactly correct it should indicate
	//the hemisphere correctly
	if(ZoneLetter >= 'N') {
	    NorthernHemisphere = 1;//point is in northern hemisphere
	} else {
	    NorthernHemisphere = 0;//point is in southern hemisphere
	    y -= 10000000.0d;//remove 10,000,000 meter offset used for southern hemisphere
	}
	
	//There are 60 zones with zone 1 being at West -180 to -174
	LongOrigin = (ZoneNumber - 1)*6 - 180 + 3;  //+3 puts origin in middle of zone
	
	eccPrimeSquared = (eccSquared)/(1-eccSquared);
	
	M = y / k0;
	mu = M/(a*(1-eccSquared/4-3*eccSquared*eccSquared/64-5*eccSquared*eccSquared*eccSquared/256));
	
	phi1Rad = mu + (3*e1/2-27*e1*e1*e1/32)*Math.sin(2*mu) + 
	    (21*e1*e1/16-55*e1*e1*e1*e1/32)*Math.sin(4*mu)+(151*e1*e1*e1/96)*Math.sin(6*mu);
	phi1 = ProjMath.radToDeg(phi1Rad);
	
	N1 = a/Math.sqrt(1-eccSquared*Math.sin(phi1Rad)*Math.sin(phi1Rad));
	T1 = Math.tan(phi1Rad)*Math.tan(phi1Rad);
	C1 = eccPrimeSquared*Math.cos(phi1Rad)*Math.cos(phi1Rad);
	R1 = a*(1-eccSquared)/Math.pow(1-eccSquared*Math.sin(phi1Rad)*Math.sin(phi1Rad), 1.5);
	D = x/(N1*k0);
	
	double Lat = phi1Rad - (N1*Math.tan(phi1Rad)/R1)*
	    (D*D/2-(5+3*T1+10*C1-4*C1*C1-9*eccPrimeSquared)*D*D*D*D/24
	     +(61+90*T1+298*C1+45*T1*T1-252*eccPrimeSquared-3*C1*C1)*D*D*D*D*D*D/720);
	Lat = ProjMath.radToDeg(Lat);
	
	double Long = (D-(1+2*T1+C1)*D*D*D/6+(5-2*C1+28*T1-3*C1*C1+8*eccPrimeSquared+24*T1*T1)
		       *D*D*D*D*D/120)/Math.cos(phi1Rad);
	Long = LongOrigin + ProjMath.radToDeg(Long);

	if (llpoint != null) {
	    llpoint.setLatLon((float)Lat,(float)Long);
	    return llpoint;
	} else {
	    return new LatLonPoint((float)Lat,(float)Long);
	}
   }
}






