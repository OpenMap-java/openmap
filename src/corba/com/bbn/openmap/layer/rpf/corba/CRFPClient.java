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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/rpf/corba/CRFPClient.java,v $
// $RCSfile: CRFPClient.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:15 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.rpf.corba;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.bbn.openmap.Environment;
import com.bbn.openmap.layer.rpf.RpfCoverageBox;
import com.bbn.openmap.layer.rpf.RpfFrameProvider;
import com.bbn.openmap.layer.rpf.RpfIndexedImageData;
import com.bbn.openmap.layer.rpf.RpfViewAttributes;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPCADRGProjection;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPCoverageBox;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.CRFPViewAttributes;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.LLPoint;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.RawImage;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.Server;
import com.bbn.openmap.layer.rpf.corba.CRpfFrameProvider.ServerHelper;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.CADRG;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * An implementation of the RpfFrameProvider interface that uses CORBA to get
 * the subframe data via a server. The image data is transmitted in jpeg format.
 * This class requires the sunw package that handles jpeg encoding/decoding.
 * <P>
 * 
 * The client can connect to the server in two different ways. The client can
 * locate the server using an IOR file that the server has written. This IOR
 * file is read using an URL. The server can also be located using the CORBA
 * naming service. The name should be in a three part fomat <ROOT name>/
 * <PART2>/ <PART3>. The root name has to be known by the nameserver and the
 * entire string has to be used by the server on startup. If both the IOR and
 * name string are set, the IOR is the thing that gets used.
 */
public class CRFPClient
        implements RpfFrameProvider {

    /** The property specifying the IOR URL. */
    public static final String iorUrlProperty = "ior";
    /** The name of the server, using the name service. */
    public static final String nameProperty = "name";
    /** The property specifying the initial JPEG quality. */
    public static final String JPEGQualityProperty = "jpegQuality";
    /** The CRFPServer. */
    protected transient Server server = null;
    /** The string used for the CORBA naming service. */
    protected String naming = null;
    /** The URL used for the IOR, to connect to the server that way. */
    protected URL iorURL = null;
    private String clientID = Environment.generateUniqueString();
    /**
     * The compression quality of the images. Lower quality images are smaller.
     */
    public float jpegQuality = .8f;

    /**
     * We'll set up the connection to the server when it's needed, but not here.
     */
    public CRFPClient() {
    }

    /**
     * Set the JPEG quality parameter for subframe transfer.
     * 
     * @param jq number between 0 and 1, should be between .4 and .8. Anything
     *        else is a waste.
     */
    public void setJpegQuality(float jq) {
        jpegQuality = jq;
    }

    /**
     * Get the quality setting for JPEG subframe retrieval.
     * 
     * @return float reflecting JPEG quality.
     */
    public float getJpegQuality() {
        return jpegQuality;
    }

    /**
     * Set the name used for the CORBA naming service.
     */
    public void setNaming(String CORBAName) {
        naming = CORBAName;
    }

    /**
     * Get the name used for the CORBA naming service.
     */
    public String getNaming() {
        return naming;
    }

    /**
     * If you want to connect to the server using an ior, set the URL where it
     * is located.
     */
    public void setIorURL(URL iorurl) {
        iorURL = iorurl;
    }

    /**
     * Get the URL for the ior.
     */
    public URL getIorURL() {
        return iorURL;
    }

    /**
     * Get the clientID string that is used by the server to keep track of
     * clients. This string in internally generated.
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Set all the RPF properties from a properties object.
     */
    public void setProperties(String prefix, java.util.Properties properties) {

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        jpegQuality = PropUtils.floatFromProperties(properties, prefix + JPEGQualityProperty, .8f);

        String url = properties.getProperty(prefix + iorUrlProperty);
        if (url != null) {
            try {
                iorURL = PropUtils.getResourceOrFileOrURL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("\"" + url + "\"" + " is malformed.");
            }
        }

        naming = properties.getProperty(prefix + nameProperty);
    }

    /**
     * When the client is deleted, it should sign off from the server, so that
     * it can free up it's cache for it.
     */
    protected void dispose() {
        if (Debug.debugging("crfp")) {
            Debug.output("CRFPClient.finalize(): calling shutdown");
        }
        try {
            if (server != null) {
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
     * Returns true because the view attributes should be set if they change at
     * the RpfCacheHandler/RpfCacheManager.
     */
    public boolean needViewAttributeUpdates() {
        return true;
    }

    /**
     * Set the RpfViewAttribute object parameters, which describes alot about
     * what you'll be asking for later.
     * 
     * @param rva the view attributes.
     */
    public void setViewAttributes(RpfViewAttributes rva) {
        Server serv = getServer();
        if (serv == null || rva == null) {
            return;
        }

        try {
            serv.setViewAttributes(new CRFPViewAttributes((short) rva.numberOfColors, (short) rva.opaqueness, rva.scaleImages,
                                                          rva.imageScaleFactor, rva.chartSeries), clientID);
            Debug.message("crfp", "CRFPClient: setting attributes.");
        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
        }
    }

    /**
     * Given a projection that describes a map or geographical area, return
     * RpfCoverageBoxes that let you know how to locate and ask for
     * RpfSubframes.
     * 
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     */
    public Vector getCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p) {

        CRFPCoverageBox[] boxes;
        Server serv = getServer();

        if (serv == null)
            return new Vector();

        Point2D center = p.getCenter();
        LLPoint llpoint = new LLPoint((float) center.getY(), (float) center.getX());

        CADRG cadrg = CADRG.convertProjection(p);
        CRFPCADRGProjection proj =
                new CRFPCADRGProjection(llpoint, (short) cadrg.getHeight(), (short) cadrg.getWidth(), cadrg.getScale(),
                                        (short) cadrg.getZone());

        Debug.message("crfp", "CRFPClient: getting coverage from server.");

        try {
            boxes = serv.getCoverage(ullat, ullon, lrlat, lrlon, proj, clientID);
            return translateCRFPCoverageBoxes(boxes);
        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
        }

        return new Vector();
    }

    /**
     * Given a projection that describes a map or geographical area, return
     * RpfCoverageBoxes that let you know what bounding boxes of data are
     * available.
     * 
     * @param ullat NW latitude.
     * @param ullon NW longitude
     * @param lrlat SE latitude
     * @param lrlon SE longitude
     * @param p a CADRG projection
     */
    public Vector getCatalogCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p, String chartSeriesCode) {
        CRFPCoverageBox[] boxes;
        Server serv = getServer();

        if (serv == null)
            return new Vector();

        Point2D center = p.getCenter();
        LLPoint llpoint = new LLPoint((float) center.getY(), (float) center.getX());

        CADRG cadrg = CADRG.convertProjection(p);
        CRFPCADRGProjection proj =
                new CRFPCADRGProjection(llpoint, (short) cadrg.getHeight(), (short) cadrg.getWidth(), cadrg.getScale(),
                                        (short) cadrg.getZone());

        Debug.message("crfp", "CRFPClient: getting catalog coverage from server.");
        try {
            boxes = serv.getCatalogCoverage(ullat, ullon, lrlat, lrlon, proj, chartSeriesCode, clientID);
            return translateCRFPCoverageBoxes(boxes);
        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
        }

        return new Vector();
    }

    /**
     * Given an area and a two-letter chart series code, find the percentage of
     * coverage on the map that that chart series can offer. If you want
     * specific coverage information, use the getCatalogCoverage call.
     * 
     * @see #getCatalogCoverage(float ullat, float ullon, float lrlat, float
     *      lrlon, Projection p, String chartSeriesCode)
     */
    public float getCalculatedCoverage(float ullat, float ullon, float lrlat, float lrlon, Projection p, String chartSeries) {
        if (chartSeries.equalsIgnoreCase(RpfViewAttributes.ANY)) {
            return 0f;
        }

        Vector results = getCatalogCoverage(ullat, ullon, lrlat, lrlon, p, chartSeries);

        int size = results.size();

        if (size == 0) {
            return 0f;
        }

        // Now interpret the results and figure out the real total
        // percentage coverage for the chartSeries. First need to
        // figure out the current size of the subframes. Then create
        // a boolean matrix of those subframes that let you figure out
        // how many of them are available. Calculate the percentage
        // off that.
        // int pZone = p.getZone();
        int i, x, y;

        double frameLatInterval = Double.MAX_VALUE;
        double frameLonInterval = Double.MAX_VALUE;
        RpfCoverageBox rcb;
        for (i = 0; i < size; i++) {
            rcb = (RpfCoverageBox) results.elementAt(i);
            if (rcb.subframeLatInterval < frameLatInterval) {
                frameLatInterval = rcb.subframeLatInterval;
            }
            if (rcb.subframeLonInterval < frameLonInterval) {
                frameLonInterval = rcb.subframeLonInterval;
            }
        }

        if (frameLatInterval == Double.MAX_VALUE || frameLonInterval == Double.MAX_VALUE) {
            return 0.0f;
        }

        int numHFrames = (int) Math.ceil((lrlon - ullon) / frameLonInterval);
        int numVFrames = (int) Math.ceil((ullat - lrlat) / frameLatInterval);

        boolean[][] coverage = new boolean[numHFrames][numVFrames];
        for (i = 0; i < size; i++) {

            rcb = (RpfCoverageBox) results.elementAt(i);
            if (rcb.percentCoverage == 100) {
                return 1.0f;
            }

            for (y = 0; y < numVFrames; y++) {
                for (x = 0; x < numHFrames; x++) {
                    // degree location of indexs
                    float yFrameLoc = (float) (lrlat + (y * frameLatInterval));
                    float xFrameLoc = (float) (ullon + (x * frameLonInterval));
                    if (coverage[x][y] == false) {
                        if (rcb.within(yFrameLoc, xFrameLoc)) {
                            coverage[x][y] = true;
                        }
                    }
                }
            }
        }

        float count = 0;

        for (y = 0; y < numVFrames; y++) {
            for (x = 0; x < numHFrames; x++) {
                if (coverage[x][y] == true) {
                    // System.out.print("X");
                    count++;
                } else {
                    // System.out.print(".");
                }
            }
            // Debug.output("");
        }

        return count / (float) (numHFrames * numVFrames);
    }

    /**
     * Convert CRFPCoverageBox[] to vector of RpfCoverageBox.
     * 
     * @param boxes CRFPCoverageBox[].
     * @return java.util.Vector
     */
    protected Vector translateCRFPCoverageBoxes(CRFPCoverageBox[] boxes) {

        Vector vector = new Vector();
        for (int i = 0; i < boxes.length; i++) {
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
     * Given the indexes to a certain RpfTocEntry within a certain A.TOC, find
     * the frame/subframe data, decompress it, and return image pixels. The
     * tocNumber and entryNumber are given within the RpfCoverageBox received
     * from a getCoverage call. With the CORBA implementation, we are assuming
     * that the byte array is an encoded jpeg image.
     * 
     * @param tocNumber the toc id for a RpfTocHandler for a particular frame
     *        provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     *        particular frame provider.
     * @param x the horizontal subframe index, from the left side of a boundary
     *        rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a boundary
     *        rectangle of the entry.
     * @see #getCoverage(float ullat, float ullon, float lrlat, float lrlon,
     *      CADRG p)
     * @return integer pixel data.
     */
    public int[] getSubframeData(int tocNumber, int entryNumber, int x, int y) {
        Server serv = getServer();
        if (serv == null)
            return null;
        byte[] jpegData;
        Debug.message("crfp", "CRFPClient: getting subframe data from server.");

        try {
            jpegData = serv.getSubframeData((short) tocNumber, (short) entryNumber, (short) x, (short) y, jpegQuality, clientID);
            if (Debug.debugging("crfpdetail")) {
                Debug.output("CRFPClient: got subframe data length " + jpegData.length);
            }

            // Need to check for the corba rendition of an allowable
            // null image (length 0)
            if (jpegData.length == 0)
                return null;

            ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpegData));
            BufferedImage bi = ImageIO.read(iis);
            int height = bi.getHeight();
            int width = bi.getWidth();
            int[] pixels = bi.getRGB(0, 0, width, height, null, 0, width);
            return pixels;
        } catch (IOException ioe) {
            Debug.error("CRFPClient: IOException decoding jpeg bytes");
        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
        }
        return null;
    }

    public RpfIndexedImageData getRawSubframeData(int tocNumber, int entryNumber, int x, int y) {
        Server serv = getServer();
        if (serv == null)
            return null;

        Debug.message("crfp", "CRFPClient: getting raw subframe data from server.");

        try {
            RawImage ri = serv.getRawSubframeData((short) tocNumber, (short) entryNumber, (short) x, (short) y, clientID);

            // Need to check for the corba rendition of an allowable
            // null image (length 0)
            if (ri.imagedata.length == 0 || ri.colortable.length == 0) {
                return null;
            }

            RpfIndexedImageData riid = new RpfIndexedImageData();
            riid.imageData = ri.imagedata;
            riid.colortable = new OMColor[ri.colortable.length];

            for (int i = 0; i < riid.colortable.length; i++) {
                riid.colortable[i] = new OMColor(ri.colortable[i]);
            }
            return riid;

        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
            return null;
        }
    }

    /**
     * Given the indexes to a certain RpfTocEntry within a certain A.TOC, find
     * the frame and return the attribute information. The tocNumber and
     * entryNumber are given within the RpfCoverageBox received from a
     * getCoverage call.
     * 
     * @param tocNumber the toc id for a RpfTocHandler for a particular frame
     *        provider.
     * @param entryNumber the RpfTocEntry id for a RpfTocHandler for a
     *        particular frame provider.
     * @param x the horizontal subframe index, from the left side of a boundary
     *        rectangle of the entry.
     * @param y the vertical subframe index, from the top side of a boundary
     *        rectangle of the entry.
     * @see #getCoverage(float ullat, float ullon, float lrlat, float lrlon,
     *      Projection p)
     * @return string.
     */
    public String getSubframeAttributes(int tocNumber, int entryNumber, int x, int y) {

        Server serv = getServer();
        if (serv == null)
            return "";

        Debug.message("crfp", "CRFPClient: getting subframe attributes from server.");
        try {
            return serv.getSubframeAttributes((short) tocNumber, (short) entryNumber, (short) x, (short) y, clientID);
        } catch (org.omg.CORBA.SystemException e) {
            handleCORBAError(e);
        }
        return "";
    }

    // ////////////// Corba management

    /**
     * get the server proxy.
     * 
     * @return Server server or null if error.
     * 
     */
    public Server getServer() {
        if (server == null)
            initServer();
        return server;
    }

    /**
     * bind to the server.
     * 
     */
    private void initServer() {
        String ior = null;
        org.omg.CORBA.Object object = null;

        com.bbn.openmap.util.corba.CORBASupport cs = new com.bbn.openmap.util.corba.CORBASupport();

        try {
            object = cs.readIOR(iorURL);
            server = ServerHelper.narrow(object);
        } catch (IOException ioe) {
            if (Debug.debugging("crfp")) {
                Debug.output("CRFPClient.initServer() IO Exception with ior: " + iorURL);
            }
            server = null;
            return;
        }

        if (server == null) {
            object = cs.resolveName(naming);

            if (object != null) {
                server = ServerHelper.narrow(object);
                if (Debug.debugging("crfp")) {
                    Debug.output("Have a RPF server:");
                    Debug.output("*** Server: is a " + server.getClass().getName() + "\n" + server);
                }
            }
        }

        if (Debug.debugging("crfp")) {
            if (server == null) {
                Debug.error("CRFPClient.initServer: null server!\n  IOR=" + ior + "\n  Name = " + naming);
            } else {
                Debug.output("CRFPClient: server is golden.");
            }
        }
    }

    protected void handleCORBAError(org.omg.CORBA.SystemException e) {
        // don't freak out if we were only interrupted...
        if (e.toString().indexOf("InterruptedIOException") != -1) {
            Debug.error("CRFPClient server communication interrupted!");
        } else {
            Debug.error("CRFPClient caught CORBA exception: " + e + "\n" + "CRFPClient Exception class: " + e.getClass().getName()
                    + "\n" + e.getMessage());
            e.printStackTrace();
        }

        server = null;// dontcha just love CORBA? reinit later
    }

}