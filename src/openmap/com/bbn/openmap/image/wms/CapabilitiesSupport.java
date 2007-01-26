/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/CapabilitiesSupport.java,v 1.1 2007/01/26 15:04:22 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.image.WMTConstants;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/CapabilitiesSupport.java,v
 *          1.1 2006/03/21 10:27:54 tomrak Exp $
 * @author pitek
 */
public class CapabilitiesSupport implements ImageServerConstants {

    public static final String WMSPrefix = ImageServer.OpenMapPrefix + "wms.";

    public static final int FMT_GETCAPS = 0;

    public static final int FMT_GETMAP = 1;

    public static final int FMT_GETFEATUREINFO = 2;

    public static final int FMT_EXCEPTIONS = 3;

    public static final int FMT_MAIN = 3;

    private Document doc;

    private Element root;

    private Node layers = null;

    private ArrayList[] formatsList = { null, null, null, null };

    private String[] onlineResourcesList = { null, null, null, null };

    private List keywordsList = null;

    public String wmsTitle = null;

    public String wmsAbstract = null;

    private int updateSequence = 1;

    /**
     * Creates a new instance of CapabilitiesSupport
     * 
     * @param requestHandler
     * @param requestProperties
     */
    public CapabilitiesSupport(Properties props, int port, String path) throws WMSException {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.newDocument();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new WMSException("Cannot create new Xml Document:" + ex.getMessage());
        }
        this.clearLayers();

        wmsTitle = props.getProperty(WMSPrefix + "Title", "Sample Title");
        wmsAbstract = props.getProperty(WMSPrefix + "Abstract", "Sample Abstract");
        String[] strKeywords = props.getProperty(WMSPrefix + "Keyword", "").split(" ");
        List keywords = Arrays.asList(strKeywords);
        setKeywords(keywords);

        String hostAddress = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            hostAddress = address.getHostAddress();
        } catch (Exception ex) {
            throw new WMSException("Unable to get own IP address " + ex.getMessage(),
                    WMSException.INTERNALERROR);
        }
        String url = "http://" + hostAddress + ":" + port + path;
        setOnlineResource(FMT_MAIN, url);
        setOnlineResource(FMT_GETMAP, url);
        setOnlineResource(FMT_GETCAPS, url);
        setOnlineResource(FMT_GETFEATUREINFO, url);

        ArrayList al = new ArrayList();
        al.add("application/vnd.ogc.wms_xml");
        setFormats(FMT_GETCAPS, al);

        al.clear();
        al.add("text/plain");
        setFormats(FMT_GETFEATUREINFO, al);

        al.clear();
        al.add("application/vnd.ogc.se_xml");
        setFormats(FMT_EXCEPTIONS, al);
    }

    /**
     * @return
     */
    private Document generateCapabilitiesDocument() {
        Element e = null;
        Element e1 = null;

        if (root != null) {
            doc.removeChild(root);
        }
        root = doc.createElement("WMT_MS_Capabilities");
        root.setAttribute("version", "1.1.1");
        root.setAttribute("updateSequence", Integer.toString(updateSequence));

        e = doc.createElement("Service");
        e.appendChild(textnode("Name", "OGC:WMS"));
        e.appendChild(textnode("Title", wmsTitle));
        e.appendChild(textnode("Abstract", wmsAbstract));

        if (!keywordsList.isEmpty()) {
            e1 = doc.createElement("KeywordList");
            for (int i = 0; i < keywordsList.size(); i++) {
                e1.appendChild(textnode("Keyword", (String) keywordsList.get(i)));
            }
            e.appendChild(e1);
        }

        e1 = doc.createElement("OnlineResource");
        e1.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        e1.setAttribute("xlink:type", "simple");
        e1.setAttribute("xlink:href", onlineResourcesList[FMT_MAIN]);
        e.appendChild(e1);

        e.appendChild(textnode("Fees", "none"));
        e.appendChild(textnode("AccessConstraints", "none"));
        root.appendChild(e);

        Node Capability = doc.createElement("Capability");
        e1 = doc.createElement("Request");

        e1.appendChild(requestcap(WMTConstants.GETCAPABILITIES, formatsList[FMT_GETCAPS], "Get",
                onlineResourcesList[FMT_GETCAPS]));
        e1.appendChild(requestcap(WMTConstants.GETMAP, formatsList[FMT_GETMAP], "Get",
                onlineResourcesList[FMT_GETMAP]));
        e1.appendChild(requestcap(WMTConstants.GETFEATUREINFO, formatsList[FMT_GETFEATUREINFO],
                "Get", onlineResourcesList[FMT_GETFEATUREINFO]));
        Capability.appendChild(e1);

        e1 = doc.createElement("Exception");
        for (int i = 0; i < formatsList[FMT_EXCEPTIONS].size(); i++) {
            e1.appendChild(textnode("Format", (String) formatsList[FMT_EXCEPTIONS].get(i)));
        }

        Capability.appendChild(e1);
        Capability.appendChild(layers);
        root.appendChild(Capability);
        root.appendChild(e);
        doc.appendChild(root);

        return doc;
    }

    /**
     */
    public void clearLayers() {
        layers = doc.createElement("Layer");
    }

    /**
     */
    public void incUpdateSequence() {
        updateSequence++;
    }

    /**
     * @param request
     * @param formats
     * @return
     */
    public boolean setFormats(int request, ArrayList formats) {
        switch (request) {
        case FMT_GETMAP:
        case FMT_GETCAPS:
        case FMT_GETFEATUREINFO:
        case FMT_EXCEPTIONS:
            formatsList[request] = (ArrayList) formats.clone();
            break;
        default:
            return false;
        }
        return true;
    }

    /**
     * @param which
     * @param url
     * @return
     */
    public boolean setOnlineResource(int which, String url) {
        switch (which) {
        case FMT_GETMAP:
        case FMT_GETCAPS:
        case FMT_GETFEATUREINFO:
        case FMT_MAIN:
            onlineResourcesList[which] = url;
            break;
        default:
            return false;
        }
        return true;
    }

    /**
     * @param keywordsList
     * @return
     */
    public boolean setKeywords(List keywordsList) {
        this.keywordsList = keywordsList;
        return true;
    }

    /**
     * @param srsList
     * @return
     */
    public boolean setSRS(ArrayList srsList) {
        return true;
    }

    public boolean addLayer(IWmsLayer wmsLayer) {
        org.w3c.dom.Element e = (org.w3c.dom.Element) node("Layer");
        e.setAttribute("queryable", wmsLayer.isQueryable() ? "1" : "0");
        e.setAttribute("opaque", wmsLayer.isOpaque() ? "1" : "0");
        e.setAttribute("cascaded", wmsLayer.isCascaded() ? "1" : "0");
        e.setAttribute("noSubsets", wmsLayer.isNoSubsets() ? "1" : "0");
        e.setAttribute("fixedWidth", Integer.toString(wmsLayer.getFixedWidth()));
        e.setAttribute("fixedHeight", Integer.toString(wmsLayer.getFixedHeight()));

        e.appendChild(textnode("Name", wmsLayer.getName()));
        e.appendChild(textnode("Title", wmsLayer.getTitle()));
        e.appendChild(textnode("Abstract", wmsLayer.getAbstract()));

        // put styles
        IWmsLayerStyle[] styles = wmsLayer.getStyles();
        for (int i = 0; i < styles.length; i++) {
            IWmsLayerStyle style = styles[i];
            org.w3c.dom.Element styleElement = (org.w3c.dom.Element) node("Style");
            styleElement.appendChild(textnode("Name", style.getName())); // "default"
            styleElement.appendChild(textnode("Title", style.getTitle())); // "Default
                                                                            // style"
            if (style.getAbstract() != null) {
                styleElement.appendChild(textnode("Abstract", style.getAbstract()));
            }
            e.appendChild(styleElement);
        }
        layers.appendChild(e);
        return true;
    }

    public boolean setLayersTitle(String title) {
        layers.appendChild(textnode("Title", title));
        return true;
    }

    public boolean setProjections(Collection projections) {
        for (Iterator it = projections.iterator(); it.hasNext();) {
            layers.appendChild(textnode("SRS", (String) it.next()));
        }
        return true;
    }

    public boolean setBoundingBox(String minx, String miny, String maxx, String maxy) {
        org.w3c.dom.Element e1 = (org.w3c.dom.Element) node("LatLonBoundingBox");
        e1.setAttribute("minx", minx);
        e1.setAttribute("miny", miny);
        e1.setAttribute("maxx", maxx);
        e1.setAttribute("maxy", maxy);
        layers.appendChild(e1);
        return true;
    }

    // Generate String out of the XML document object
    /**
     * @throws IOException
     */
    public String generateXMLString() throws IOException {
        StringWriter strWriter = new StringWriter();
        XMLSerializer probeMsgSerializer = new XMLSerializer();
        OutputFormat outFormat = new OutputFormat();

        // Setup format settings
        outFormat.setEncoding("UTF-8");
        outFormat.setVersion("1.0");
        outFormat.setIndenting(true);
        outFormat.setIndent(2);

        probeMsgSerializer.setOutputCharStream(strWriter);
        probeMsgSerializer.setOutputFormat(outFormat);

        // Serialize XML Document
        Document document = generateCapabilitiesDocument();
        probeMsgSerializer.serialize(document);
        String xmlStr = strWriter.toString();
        strWriter.close();
        return xmlStr;
    }

    /**
     * @param Name
     * @param Text
     * @return
     */
    public Node textnode(String Name, String Text) {
        Element e1 = doc.createElement(Name);
        Node n = doc.createTextNode(Text);
        e1.appendChild(n);
        return e1;
    }

    /**
     * @param Name
     * @return
     */
    public Node node(String Name) {
        return doc.createElement(Name);
    }

    /**
     * @param requestName
     * @param formatList
     * @param methodName
     * @param url
     * @return
     */
    private Node requestcap(String requestName, ArrayList formatList, String methodName, String url) {
        Element e = doc.createElement("OnlineResource");
        e.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        e.setAttribute("xlink:type", "simple");
        e.setAttribute("xlink:href", url);
        Element e1 = doc.createElement(methodName);
        e1.appendChild(e);
        e = e1;
        e1 = doc.createElement("HTTP");
        e1.appendChild(e);
        e = e1;
        e1 = doc.createElement("DCPType");
        e1.appendChild(e);
        e = e1;
        e1 = doc.createElement(requestName);
        e1.appendChild(e);

        for (int i = 0; i < formatList.size(); i++) {
            e1.appendChild(textnode("Format", (String) formatList.get(i)));
        }

        return e1;
    }

}
