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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/rpf/corba/CRFPClient.java,v $
// $RCSfile: CRFPClient.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:47 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf.corba;

import java.awt.Point;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.swing.ImageIcon;

import com.bbn.openmap.Environment;
import com.bbn.openmap.layer.rpf.*;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.util.Debug;

import com.sun.image.codec.jpeg.*;

/*  CORBA  */
import org.omg.CORBA.ORB;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;
import org.omg.CosNaming.*;

/** 
 * An implementation of the RpfFrameProvider interface that uses CORBA
 * to get the subframe data via a server.  The image data is
 * transmitted in jpeg format.  This class requires the sunw package
 * that handles jpeg encoding/decoding.<P>
 *
 * The client can connect to the server in two different ways.  The
 * client can locate the server using an IOR file that the server has
 * written.  This IOR file is read using an URL.  The server can also
 * be located using the CORBA naming service.  The name should be in a
 * three part fomat <ROOT name>/<PART2>/<PART3>.  The root name has to
 * be known by the nameserver and the entire string has to be used by
 * the server on startup.  If both the IOR and name string are set,
 * the IOR is the thing that gets used.
 */
public class CRFPClient implements RpfFrameProvider {

    /** The property specifying the IOR URL. */
    public static final String iorUrlProperty = ".ior";
    /** The name of the server, using the name service.*/
    public static final String nameProperty = ".name";
    /** The property specifying the initial JPEG quality. */
    public static final String JPEGQualityProperty = ".jpegQuality";
    /** The CRFPServer. */
    protected transient Server server = null;
    private transient ORB orb;
    /** The string used for the CORBA naming service. */
    protected String naming = null;
    private String clientID = Environment.generateUniqueString();
    /** The URL used for the IOR, to connect to the server that way. */
    protected URL iorURL = null;
    /** The compression quality of the images. Lower quality images
     *  are smaller. */
    public float jpegQuality = .8f;

    /**  
     * We'll set up the connection to the server when it's needed,
     * but not here. 
     */
    public CRFPClient(){}

    /**
     * Set the JPEG quality parameter for subframe transfer.
     * @param jq number between 0 and 1, should be between .4 and .8.
     * Anything else is a waste.  
     */
    public void setJpegQuality(float jq){
	jpegQuality = jq;
    }

    /**
     * Get the quality setting for JPEG subframe retrieval.
     * @return float reflecting JPEG quality.
     */
    public float getJpegQuality(){
	return jpegQuality;
    }

    /**
     * Set the name used for the CORBA naming service.
     */
    public void setNaming(String CORBAName){
	naming = CORBAName;
    }

    /**
     * Get the name used for the CORBA naming service.
     */
    public String getNaming(){
	return naming;
    }

    /**
     * If you want to connect to the server using an ior, set the URL
     * where it is located.  
     */
    public void setIorURL(URL iorurl){
	iorURL = iorurl;
    }

    /**
     * Get the URL for the ior.
     */
    public URL getIorURL(){
	return iorURL;
    }

    /** 
     * Get the clientID string that is used by the server to keep
     * track of clients.  This string in internally generated.  
     */
    public String getClientID(){
	return clientID;
    }

    /**
     * Set all the RPF properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

	jpegQuality = LayerUtils.floatFromProperties(properties,
						     prefix + JPEGQualityProperty,
						     .8f);

	String url = properties.getProperty(prefix + iorUrlProperty);
	if (url != null) {
	    try {
		iorURL = new URL(url);
	    } catch (MalformedURLException e) {
		throw new IllegalArgumentException("\"" + url + "\""
						   + " is malformed.");
	    }
	}

	naming = properties.getProperty(prefix + nameProperty);
    }

    /**
     * When the client is deleted, it should sign off from the
     * server, so that it can free up it's cache for it.
     */
    protected void finalize() {
	if (Debug.debugging("crfp")){
	    Debug.output("CRFPClient.finalize(): calling shutdown");
	}
        try {
            if (server != null){
                server.signoff(clientID);
	    }
            server = null;
        } catch (org.omg.CORBA.SystemException e) {
            Debug.error("CRFPClient.finalize(): " + e);
        } catch (Throwable t) {
            Debug.error("CRFPClient.finalize(): " + t);
	}
    }

    /** 
     * Returns true because the view attributes should be set if they
     * change at the RpfCacheHandler/RpfCacheManager.  
     */
    public boolean needViewAttributeUpdates(){
	return true;
    }
    
    /**
     * Set the RpfViewAttribute object parameters, which describes
     * alot about what you'll be asking for later.  
     * @param rva the view attributes.
     */
    public void setViewAttributes(RpfViewAttributes rva){
	Server serv = getServer();
	if (serv == null || rva == null) {
	    return;
	}

	try {
	    serv.setViewAttributes(new CRFPViewAttributes((short)rva.numberOfColors,
							  (short)rva.opaqueness,
							  rva.scaleImages,
							  rva.imageScaleFactor,
							  rva.chartSeries), clientID);
	    Debug.message("crfp", "CRFPClient: setting attributes.");
	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	}
    }

    /** 
     * Given a projection that describes a map or geographical area,
     * return RpfCoverageBoxes that let you know how to locate and ask
     * for RpfSubframes.
     *
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     */
    public Vector getCoverage(float ullat, float ullon,
			      float lrlat, float lrlon, 
			      CADRG p) {

	CRFPCoverageBox[] boxes;
	Server serv = getServer();

	if (serv == null) return new Vector();

	LLPoint llpoint = new LLPoint(p.getCenter().getLatitude(),
				      p.getCenter().getLongitude());

	CRFPCADRGProjection proj = new CRFPCADRGProjection(llpoint,
							   (short)p.getHeight(),
							   (short)p.getWidth(),
							   p.getScale(),
							   (short)p.getZone());

	Debug.message("crfp", "CRFPClient: getting coverage from server.");

	try {
	    boxes = serv.getCoverage(ullat, ullon, lrlat, lrlon,
				     proj, clientID);
	    return translateCRFPCoverageBoxes(boxes);
	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	}
	
	return new Vector();
    }

    /**
     * Given a projection that describes a map or geographical area,
     * return RpfCoverageBoxes that let you know what bounding boxes
     * of data are available.
     *
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     */
    public Vector getCatalogCoverage(float ullat, float ullon,
				     float lrlat, float lrlon,
				     CADRG p, String chartSeriesCode) {
	CRFPCoverageBox[] boxes;
	Server serv = getServer();

	if (serv == null) return new Vector();

	LLPoint llpoint = new LLPoint(p.getCenter().getLatitude(),
				      p.getCenter().getLongitude());

	CRFPCADRGProjection proj = new CRFPCADRGProjection(llpoint,
							   (short)p.getHeight(),
							   (short)p.getWidth(),
							   p.getScale(),
							   (short)p.getZone());

	Debug.message("crfp", "CRFPClient: getting catalog coverage from server.");
	try {
	    boxes = serv.getCatalogCoverage(ullat, ullon, lrlat, lrlon,
					    proj, chartSeriesCode, clientID);
	    return translateCRFPCoverageBoxes(boxes);
	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	}

	return new Vector();
    }

   /**
     * Given an area and a two-letter chart series code, find the
     * percentage of coverage on the map that that chart series can
     * offer.  If you want specific coverage information, use the
     * getCatalogCoverage call.
     * @see #getCatalogCoverage(float ullat, float ullon, float lrlat, float lrlon, CADRG p, String chartSeriesCode)
     */
    public float getCalculatedCoverage(float ullat, float ullon,
				       float lrlat, float lrlon,
				       CADRG p, String chartSeries){
	if (chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)){
	    return 0f;
	}

	Vector results = getCatalogCoverage(ullat, ullon, lrlat, lrlon,
					    p, chartSeries);

	int size = results.size();

	if (size == 0){
	    return 0f;
	}

	// Now interpret the results and figure out the real total
	// percentage coverage for the chartSeries.  First need to
	// figure out the current size of the subframes.  Then create
	// a boolean matrix of those subframes that let you figure out
	// how many of them are available.  Calculate the percentage
	// off that.
	int pZone = p.getZone();
	int i, x, y;
	
	double frameLatInterval = Double.MAX_VALUE;
	double frameLonInterval = Double.MAX_VALUE;
	RpfCoverageBox rcb;
	for (i = 0; i < size; i++){
	    rcb = (RpfCoverageBox)results.elementAt(i);
	    if (rcb.subframeLatInterval < frameLatInterval){
		frameLatInterval = rcb.subframeLatInterval;
	    }
	    if (rcb.subframeLonInterval < frameLonInterval){
		frameLonInterval = rcb.subframeLonInterval;
	    }
	}

	if (frameLatInterval == Double.MAX_VALUE || 
	    frameLonInterval == Double.MAX_VALUE){
	    return 0.0f;
	}

	int numHFrames = (int) Math.ceil((lrlon - ullon)/frameLonInterval);
	int numVFrames = (int) Math.ceil((ullat- lrlat)/frameLatInterval);

	boolean[][] coverage = new boolean[numHFrames][numVFrames];
	for (i = 0; i < size; i++){

	    rcb = (RpfCoverageBox)results.elementAt(i);
	    if (rcb.percentCoverage == 100){
		return 1.0f;
	    }

	    for (y = 0; y < numVFrames; y++){
		for (x = 0; x < numHFrames; x++){
		    // degree location of indexs
		    float yFrameLoc = (float)(lrlat + (y*frameLatInterval));
		    float xFrameLoc = (float)(ullon + (x*frameLonInterval));
		    if (coverage[x][y] == false){
			if (rcb.within(yFrameLoc, xFrameLoc)){
			    coverage[x][y] = true;
			}
		    }
		}
	    }
	}
	
	float count = 0;

	for (y = 0; y < numVFrames; y++){
	    for (x = 0; x < numHFrames; x++){
		if (coverage[x][y] == true){
// 		    System.out.print("X");
		    count++;
		} else {
// 		    System.out.print(".");
		}
	    }
// 	    Debug.output("");
	}	
	
	return count/(float)(numHFrames*numVFrames);
    }

    /**
     * Convert CRFPCoverageBox[] to vector of RpfCoverageBox.
     * @param boxes CRFPCoverageBox[].
     * @return java.util.Vector
     */
    protected Vector translateCRFPCoverageBoxes(CRFPCoverageBox[] boxes){

	Vector vector = new Vector();
	for (int i = 0; i < boxes.length; i++){
	    CRFPCoverageBox box = boxes[i];
	    RpfCoverageBox rcb = new RpfCoverageBox();
	    rcb.nw_lat = box.nw_lat;
	    rcb.nw_lon = box.nw_lon;
	    rcb.se_lat = box.se_lat;
	    rcb.se_lon = box.se_lon;
	    rcb.subframeLatInterval = box.subframeLatInterval;
	    rcb.subframeLonInterval = box.subframeLonInterval;
	    rcb.chartCode = box.chartCode;
	    rcb.startIndexes = new Point(box.startIndexes.x, box.startIndexes.y);
	    rcb.endIndexes = new Point(box.endIndexes.x, box.endIndexes.y);
	    rcb.tocNumber = (int) box.tocNumber;
	    rcb.entryNumber = (int) box.entryNumber;
	    rcb.scale = box.scale;
	    rcb.percentCoverage = box.percentCoverage;
	    rcb.zone = box.zone;
	    vector.addElement(rcb);
	}
	return vector;
    }

    /**
     * Given the indexes to a certain RpfTocEntry within a certain
     * A.TOC, find the frame/subframe data, decompress it, and return
     * image pixels.  The tocNumber and entryNumber are given within
     * the RpfCoverageBox received from a getCoverage call.  With the
     * CORBA implementation, we are assuming that the byte array is an
     * encoded jpeg image.
     *
     * @param tocNumber the toc id for a RpfTocHandler for a
     * particular frame provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     * particular frame provider.
     * @param x the horizontal subframe index, from the left side of a
     * boundary rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a
     * boundary rectangle of the entry.
     * @see #getCoverage(float ullat, float ullon, float lrlat, float lrlon, CADRG p) 
     * @return integer pixel data.  
     */
    public int[] getSubframeData(int tocNumber, int entryNumber, int x, int y){
	Server serv = getServer();
	if (serv == null) return null;
	byte[] jpegData;
	Debug.message("crfp", "CRFPClient: getting subframe data from server.");

	try {
	    jpegData = serv.getSubframeData((short) tocNumber,
					    (short) entryNumber,
					    (short) x, (short) y, 
					    jpegQuality, clientID);
	    if (Debug.debugging("crfpdetail")){
		Debug.output("CRFPClient: got subframe data length " + 
			     jpegData.length);
	    }

	    // Need to check for the corba rendition of an allowable
	    // null image (length 0)
	    if (jpegData.length == 0) return null;
	    
	    ByteArrayInputStream bais = new ByteArrayInputStream(jpegData);
	    JPEGImageDecoder jid = JPEGCodec.createJPEGDecoder(bais);

	    BufferedImage bi = jid.decodeAsBufferedImage();
	    int height = bi.getHeight();
	    int width = bi.getWidth();
	    int[] pixels = bi.getRGB(0, 0, width, height, null, 0, width);
	    return pixels;
	} catch (IOException ioe){
	    Debug.error("CRFPClient: IOException decoding jpeg bytes");
	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	}
	return null;
    }
    
    public RpfIndexedImageData getRawSubframeData(int tocNumber, int entryNumber, 
						  int x, int y){
	Server serv = getServer();
	if (serv == null) return null;
	
	Debug.message("crfp", "CRFPClient: getting raw subframe data from server.");

	try {
	    RawImage ri = serv.getRawSubframeData((short)tocNumber,
						  (short) entryNumber,
						  (short) x, (short)y,
						  clientID);
	    
	    // Need to check for the corba rendition of an allowable
	    // null image (length 0)
	    if (ri.imagedata.length == 0 || ri.colortable.length == 0){
		return null;
	    }

	    RpfIndexedImageData riid = new RpfIndexedImageData();
	    riid.imageData = ri.imagedata;
	    riid.colortable = new OMColor[ri.colortable.length];

	    for (int i = 0; i < riid.colortable.length; i++){
		riid.colortable[i] = new OMColor(ri.colortable[i]);
	    }
	    return riid;

	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	    return null;
	}
    }

   /**
     * Given the indexes to a certain RpfTocEntry within a certain
     * A.TOC, find the frame and return the attribute information.
     * The tocNumber and entryNumber are given within the
     * RpfCoverageBox received from a getCoverage call.
     *
     * @param tocNumber the toc id for a RpfTocHandler for a
     * particular frame provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     * particular frame provider.
     * @param x the horizontal subframe index, from the left side of a
     * boundary rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a
     * boundary rectangle of the entry.
     * @see #getCoverage(float ullat, float ullon, float lrlat, float lrlon, CADRG p)
     * @return string.  
     */
    public String getSubframeAttributes(int tocNumber, int entryNumber, 
					int x, int y){

	Server serv = getServer();
	if (serv == null) return "";
	
	Debug.message("crfp", "CRFPClient: getting subframe attributes from server.");
	try {
	    return serv.getSubframeAttributes((short)tocNumber,(short) entryNumber,
					      (short) x, (short)y, clientID);
	} catch (org.omg.CORBA.SystemException e){
	    handleCORBAError(e);
	}
	return "";
    }


    ////////////////  Corba management

    /**
     * get the server proxy.
     *
     * @return Server server or null if error.
     *
     */
    public Server getServer () {
	if (server == null)
	    initServer();
	return server;
    }


    protected void connect(org.omg.CORBA.ORB orb, String ior) {
	org.omg.CORBA.Object object = orb.string_to_object(ior); 
	server = ServerHelper.narrow(object); 
    }

    /**
     * bind to the server.
     *
     */
    private void initServer() {
	String ior = null;

	if (iorURL != null) {
	    try {
		URLConnection urlConnection = iorURL.openConnection();
		// 	    urlConnection.setDefaultUseCaches(false);
		// 	    urlConnection.setUseCaches(false);
		// 	    urlConnection.connect();
		
		InputStream is = urlConnection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		ior = reader.readLine();
		reader.close();
	    } catch (java.io.IOException e) {
		Debug.error("CRFPClient.initServer(): IOException reading IOR from \"" + iorURL + "\"");
	    }
	}
	
	try {
	    orb = CORBAManager.getORB();
	    
	    if (ior != null){
		
		// HACK seem to need this for vbj33 applet...
// 		((com.visigenic.vbroker.orb.ORB)orb).proxy(false);
// 		Debug.output("ORB PROXY="+
// 			     ((com.visigenic.vbroker.orb.ORB)orb).proxy());
		connect(orb, ior);
	    }
	    
	    if (server == null && naming != null){
		// Get the root context of the name service.
		Debug.message("crfp", "CRFPClient: Binding to the server objects... " );
		org.omg.CORBA.Object obj = null;

		if (Debug.debugging("crfp")){
		    String[] services = orb.list_initial_services();
		    if (services != null){
			Debug.output("CRFPClient: Listing services:");
			
			for (int k = 0; k < services.length; k++){
			    Debug.output("  service " + k + ": " +
					 services[k]);
			}
		    } else {
			Debug.output("CRFPClient: no services available");
		    }
		    
		}

		try {
		    obj = orb.resolve_initial_references( "NameService" );
		} catch( Exception e ) {
		    Debug.error("Error getting root naming context: \n  message - " + e.getMessage());
		}
		NamingContext rootContext = NamingContextHelper.narrow( obj );
// 		if (Debug.debugging("crfp") && rootContext == null) {
// 		    Debug.output("CRFPClient: null root context for nameservice");
// 		}
		
		// Resolve the specialist
		String temp = naming;
		
		if (Debug.debugging("crfp")){
		    Debug.output("CRFPClient: Name of server: " + 
				 naming);
		}
		
		Vector components = new Vector();
		int numcomponents = 0;
		String temporaryTemp = null;
		
		int tindex = temp.indexOf("/");
		while (tindex != -1) {
		    numcomponents++;
		    temporaryTemp=temp.substring(0,tindex);

		    if (Debug.debugging("crfp")){
			Debug.output("CRFPClient: Adding Name component: " + 
				     temporaryTemp);
		    }

		    components.addElement(temporaryTemp);
		    temp=temp.substring(tindex+1);
		    tindex = temp.indexOf("/");
		}
		if (Debug.debugging("crfp")){
		    Debug.output("CRFPClient: Adding final Name component: " + temp);
		}
		components.addElement(temp);
		
		NameComponent[] serverName = 
		    new NameComponent[components.size()];
		for (int i=0; i<components.size(); i++) {
		    serverName[i] = new NameComponent((String)(components.elementAt(i)), "");
		}
		
		org.omg.CORBA.Object serverObject = null;
		try {
// 		    if (rootContext != null){
			serverObject = rootContext.resolve( serverName );
// 		    } else {
// 			Debug.error("CRFPClient: root context is null");
// 		    }
		    Debug.message("crfpdetail", "CRFPClient: Got past servername resolve");
		} catch (Exception e ) {
// 		    Debug.error("CRFPClient: Error resolving the server: \n" +
// 				e.getMessage());
		}
		
		if (serverObject == null) {
		    Debug.error("CRFPClient: null serverObject!  Couldn't resolve server name");
		    return;
		} else {
		    if (Debug.debugging("crfpdetail")) {
			Debug.output("objtostring:" + 
				     orb.object_to_string(serverObject));
		    }
		    server = ServerHelper.narrow(serverObject);
		}
	    }
	    
	} catch (NullPointerException npe){
	} catch (org.omg.CORBA.SystemException e) {
	    Debug.error("CRFPClient.initServer(): " + e + "\n" +
			"CORBA Exception while initializing server:\n" + 
			e.getClass().getName());
	    server = null;
	} catch (Throwable t) {
	    Debug.error("CRFPClient.initServer(): " + t + "\n" +
			    "Exception while initializing\n server:\n" + 
			t.getClass().getName());
	    server = null;
	}
	
	
	if (server == null) {
	    Debug.error("CRFPClient.initServer: null server!");
	}
	
	if (Debug.debugging("crfp")){
	    if (server != null){
		Debug.output("CRFPClient: server is golden.");
		
	    }
	}
    }
    
    protected void handleCORBAError(org.omg.CORBA.SystemException e){
	// don't freak out if we were only interrupted...
	if (e.toString().indexOf("InterruptedIOException") != -1) {
	    Debug.error("CRFPClient server communication interrupted!");
	} else {
	    Debug.error("CRFPClient caught CORBA exception: " + e + "\n" +
			"CRFPClient Exception class: " + 
			e.getClass().getName() + "\n" +
			e.getMessage());
	    e.printStackTrace();
	}

	server = null;// dontcha just love CORBA? reinit later
    }

}
