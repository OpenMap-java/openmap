/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/CapabilitiesSupport.java,v 1.5 2009/01/15 19:38:33 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.WMTConstants;
import com.bbn.openmap.proj.coords.AxisOrder;
import com.bbn.openmap.proj.coords.BoundingBox;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap
 *          /wms/CapabilitiesSupport.java,v 1.1 2006/03/21 10:27:54 tomrak Exp $
 * @author pitek
 */
public class CapabilitiesSupport {

    public static final String WMSPrefix = ImageServer.OpenMapPrefix + "wms.";

    public static final int FMT_GETMAP = 0;

    public static final int FMT_GETFEATUREINFO = 1;

    private Map<Integer, List<String>> formatsList = new HashMap<Integer, List<String>>();
    
    private String onlineResource;

    private List<String> keywordsList = null;

    private String wmsTitle = null;

    private String wmsAbstract = null;

    private int updateSequence = 1;

    private List<IWmsLayer> wmslayers = new ArrayList<IWmsLayer>();

    private String layersTitle;

    private Collection<String> crsCodes = CoordinateReferenceSystem.getCodes();

    /**
     * Creates a new instance of CapabilitiesSupport
     * 
     * @param props
     * @param scheme
     * @param hostName
     * @param port
     * @param path
     * @throws WMSException
     */
    CapabilitiesSupport(Properties props, String scheme, String hostName, int port, String path)
            throws WMSException {

        wmsTitle = props.getProperty(WMSPrefix + "Title", "Sample Title");
        wmsAbstract = props.getProperty(WMSPrefix + "Abstract", "Sample Abstract");
        layersTitle = props.getProperty(WMSPrefix + "LayersTitle", "Sample Layer List");
        String[] strKeywords = props.getProperty(WMSPrefix + "Keyword", "").split(" ");
        List<String> keywords = Arrays.asList(strKeywords);
        setKeywords(keywords);

        setUrl(scheme, hostName, port, path);

        List<String> al = new ArrayList<String>();
        setFormats(FMT_GETMAP, al);
        setFormats(FMT_GETFEATUREINFO, al);
    }

    /**
     * Set url to wms servlet.
     * 
     * @param scheme
     * @param hostName
     * @param port
     * @param path a String like "/myproject/wms"
     */
    public void setUrl(String scheme, String hostName, int port, String path) {
        StringBuilder url = new StringBuilder();
        url.append(scheme);
        url.append("://");
        url.append(hostName);
        if (!((scheme.equals("http") && (port == 80)) || (scheme.equals("https") && (port == 443)))) {
            url.append(":");
            url.append(port);
        }
        url.append(path);
        setUrl(url.toString());
    }

    /**
     * Set url to wms servlet like "http://myserver/myproject/wms"
     * 
     * @param url
     */
    public void setUrl(String url) {
       this.onlineResource = url;
    }

    /**
     * @return Document specifying capabilities.
     */
    private Document generateCapabilitiesDocument(Version version) {

        Document doc = version.createCapabilitiesDocumentStart();

        Element root = doc.getDocumentElement();
        root.setAttribute("version", version.getVersionString());
        root.setAttribute("updateSequence", Integer.toString(updateSequence));

        Element service = doc.createElement("Service");
        service.appendChild(textnode(doc, "Name", version.getServiceName()));
        service.appendChild(textnode(doc, "Title", wmsTitle));
        service.appendChild(textnode(doc, "Abstract", wmsAbstract));

        if (!keywordsList.isEmpty()) {
            Element keywordListElement = doc.createElement("KeywordList");
            for (int i = 0; i < keywordsList.size(); i++) {
                keywordListElement.appendChild(textnode(doc, "Keyword", (String) keywordsList.get(i)));
            }
            service.appendChild(keywordListElement);
        }

        service.appendChild(onlineResource(doc, onlineResource));

        service.appendChild(textnode(doc, "Fees", "none"));
        service.appendChild(textnode(doc, "AccessConstraints", "none"));
        root.appendChild(service);

        Node capability = doc.createElement("Capability");
        Element request = doc.createElement("Request");

        request.appendChild(requestcap(doc, WMTConstants.GETCAPABILITIES, version.getCapabiltiesFormats(), "Get",
                                       onlineResource));
        request.appendChild(requestcap(doc, WMTConstants.GETMAP, formatsList.get(FMT_GETMAP), "Get",
                                       onlineResource));
        request.appendChild(requestcap(doc, WMTConstants.GETFEATUREINFO, formatsList.get(FMT_GETFEATUREINFO), "Get",
                                       onlineResource));
        capability.appendChild(request);

        Element exceptionElement = doc.createElement("Exception");
        for (String format : version.getExceptionFormats()) {
            exceptionElement.appendChild(textnode(doc, "Format", format));
        }
        capability.appendChild(exceptionElement);

        capability.appendChild(createLayersElement(doc, version));
        root.appendChild(capability);

        return doc;
    }

    private Element createLayersElement(Document doc, Version version) {
        Element layers = doc.createElement("Layer");
        layers.appendChild(textnode(doc, "Title", layersTitle));
        for (Iterator<String> it = crsCodes.iterator(); it.hasNext();) {
            layers.appendChild(textnode(doc, version.getCoordinateReferenceSystemAcronym(), it.next()));
        }

        // append bounding boxes
        layers.appendChild(version.createLatLonBoundingBox(doc));
        for (Iterator<String> it = crsCodes.iterator(); it.hasNext();) {
            appendSRSBoundingBox(doc, layers, it.next(), version);
        }

        // append layers
        // in OpenMap, the layer on top is listed first, but in WMS
        // Capabilities, the layer on top is listed at the bottom
        List<IWmsLayer> reverseLayers = new ArrayList<IWmsLayer>(wmslayers);
        Collections.reverse(reverseLayers);
        for (IWmsLayer wmsLayer : reverseLayers) {
            createLayerElement(doc, layers, wmsLayer, version);
        }
        return layers;
    }

    private void createLayerElement(Document doc, Element layers, IWmsLayer wmsLayer, Version version) {
        org.w3c.dom.Element layerElement = (org.w3c.dom.Element) node(doc, "Layer");
        layerElement.setAttribute("queryable", wmsLayer.isQueryable() ? "1" : "0");
        // implied layerElement.setAttribute("cascaded", "0");
        layerElement.setAttribute("opaque", "0");
        layerElement.setAttribute("noSubsets", "0");
        // implied layerElement.setAttribute("fixedWidth", "0");
        // implied layerElement.setAttribute("fixedHeight", "0");

        layerElement.appendChild(textnode(doc, "Name", wmsLayer.getWmsName()));
        layerElement.appendChild(textnode(doc, "Title", wmsLayer.getTitle()));
        if (wmsLayer.getAbstract() != null) {
            layerElement.appendChild(textnode(doc, "Abstract", wmsLayer.getAbstract()));
        }

        // add styles
        IWmsLayerStyle[] styles = wmsLayer.getStyles();
        if (styles != null) {
            for (int i = 0; i < styles.length; i++) {
                IWmsLayerStyle style = styles[i];
                org.w3c.dom.Element styleElement = (org.w3c.dom.Element) node(doc, "Style");
                styleElement.appendChild(textnode(doc, "Name", style.getName())); // "default"
                styleElement.appendChild(textnode(doc, "Title", style.getTitle())); // "Default
                // style"
                if (style.getAbstract() != null) {
                    styleElement.appendChild(textnode(doc, "Abstract", style.getAbstract()));
                }

                // tell the layer about the style so the style can used legend
                // graphics
                wmsLayer.setStyle(style.getName());
                Legend legend = wmsLayer.getLegend();
                if (legend != null) {
                    for (Dimension2D dimension : legend.getSizeHints()) {
                        int width = (int) dimension.getWidth();
                        int height = (int) dimension.getHeight();
                        String format = "image/png";

                        Element legendURLElement = (Element) node(doc, "LegendURL");
                        legendURLElement.setAttribute("width", Integer.toString(width));
                        legendURLElement.setAttribute("height", Integer.toString(height));
                        legendURLElement.appendChild(textnode(doc, "Format", format));

                        StringBuilder url = new StringBuilder();
                        // would be nicer to use FMT_GETLEGENDGRAPHIC, but it
                        // may not be listed
                        url.append(onlineResource);
                        url.append("?").append(WMTConstants.SERVICE).append("=WMS");
                        url.append("&").append(WMTConstants.VERSION).append("=");
                        url.append(version.getVersionString());
                        url.append("&").append(WMTConstants.REQUEST).append("=");
                        url.append(WMTConstants.GETLEGENDGRAPHIC);
                        url.append("&").append(WMTConstants.LAYER).append("=");
                        url.append(wmsLayer.getWmsName());
                        url.append("&").append(WMTConstants.STYLE).append("=").append(style.getName());
                        url.append("&").append(WMTConstants.FORMAT).append("=").append(format);
                        url.append("&").append(WMTConstants.WIDTH).append("=").append(width);
                        url.append("&").append(WMTConstants.HEIGHT).append("=").append(height);

                        legendURLElement.appendChild(onlineResource(doc, url.toString()));

                        styleElement.appendChild(legendURLElement);
                    }
                }

                layerElement.appendChild(styleElement);
            }
        }

        // add nested layers
        if (wmsLayer instanceof IWmsNestedLayer) {
            IWmsLayer[] nestedLayers = ((IWmsNestedLayer) wmsLayer).getNestedLayers();
            if (nestedLayers != null) {
                for (int i = 0; i < nestedLayers.length; i++) {
                    createLayerElement(doc, layerElement, nestedLayers[i], version);
                }
            }
        }

        layers.appendChild(layerElement);
    }

    /**
     */
    public void incUpdateSequence() {
        updateSequence++;
    }

    /**
     * @param request
     * @param formats
     * @return true if request type handled
     */
    public boolean setFormats(int request, Collection<String> formats) {
        switch (request) {
            case FMT_GETMAP:
            case FMT_GETFEATUREINFO:
                formatsList.put(request, new ArrayList<String>(formats));
                return true;
            default:
                return false;
        }
    }

    /**
     * @param url
     */
    public void setOnlineResource(String url) {
       this.onlineResource = url;
    }

    /**
     * @param keywordsList
     */
    public void setKeywords(List<String> keywordsList) {
        this.keywordsList = keywordsList;
    }

    public void addLayer(IWmsLayer wmsLayer) {
        wmslayers.add(wmsLayer);
    }

    public void setLayersTitle(String title) {
        this.layersTitle = title;
    }

    private void appendSRSBoundingBox(Document doc, Element layers, String crsCode, Version version) {
        CoordinateReferenceSystem crs = CoordinateReferenceSystem.getForCode(crsCode);
        BoundingBox bbox = crs.getBoundingBox();
        if (bbox == null) {
            return;
        }
        org.w3c.dom.Element e1 = (org.w3c.dom.Element) node(doc, "BoundingBox");
        e1.setAttribute(version.getCoordinateReferenceSystemAcronym(), crs.getCode());

        if (version.usesAxisOrder() && (crs.getAxisOrder() == AxisOrder.northBeforeEast)) {
            // wms 1.3.0. 6.7.4 EXAMPLE 2
            // "A <BoundingBox> representing the entire Earth in the EPSG:4326
            // Layer CRS would be written as <BoundingBox CRS="EPSG:4326"
            // minx="-90" miny="-180" maxx="90" maxy="180">. A BBOX parameter
            // requesting a map of the entire Earth would be written in this CRS
            // as
            // BBOX=-90,-180,90,180."
            e1.setAttribute("minx", Double.toString(bbox.getMinY()));
            e1.setAttribute("miny", Double.toString(bbox.getMinX()));
            e1.setAttribute("maxx", Double.toString(bbox.getMaxY()));
            e1.setAttribute("maxy", Double.toString(bbox.getMaxX()));
        } else {
            e1.setAttribute("minx", Double.toString(bbox.getMinX()));
            e1.setAttribute("miny", Double.toString(bbox.getMinY()));
            e1.setAttribute("maxx", Double.toString(bbox.getMaxX()));
            e1.setAttribute("maxy", Double.toString(bbox.getMaxY()));
        }
        layers.appendChild(e1);
    }

    /**
     * Generate String out of the XML document object
     * 
     * @throws IOException, TransformerException,
     *         TransformerConfigurationException
     */
    String generateXMLString(Version version)
            throws IOException, TransformerConfigurationException, TransformerException {

        StringWriter strWriter = new StringWriter();
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tr.setOutputProperty(OutputKeys.VERSION, "1.0");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        Document document = generateCapabilitiesDocument(version);

        // system id not transformed by default transformer
        if (document.getDoctype() != null) {
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, document.getDoctype().getSystemId());
        }

        // Serialize XML Document
        tr.transform(new DOMSource(document), new StreamResult(strWriter));
        return strWriter.toString();
    }

    /**
     * @param Name
     * @param Text
     * @return Node created for doc with name
     */
    private Node textnode(Document doc, String Name, String text) {
        Element e1 = doc.createElement(Name);
        if (text == null) {
            text = "";
        }
        Node n = doc.createTextNode(text);
        e1.appendChild(n);
        return e1;
    }

    /**
     * @param doc Document
     * @param Name name 
     * @return Node created for doc with name
     */
    private Node node(Document doc, String Name) {
        return doc.createElement(Name);
    }

    private Node onlineResource(Document doc, String url) {
        Element onlineResource = doc.createElement("OnlineResource");
        onlineResource.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        onlineResource.setAttribute("xlink:type", "simple");
        onlineResource.setAttribute("xlink:href", url);
        return onlineResource;
    }

    /**
     * @param doc
     * @param requestName like "GetMap"
     * @param formatList
     * @param methodName like "Get" or "Post"
     * @param url
     * @return Node
     */
    private Node requestcap(Document doc, String requestName, Collection<String> formatList, String methodName, String url) {
        Element methodNode = doc.createElement(methodName);
        methodNode.appendChild(onlineResource(doc, url));

        Element httpNode = doc.createElement("HTTP");
        httpNode.appendChild(methodNode);

        Element dcpTypeNode = doc.createElement("DCPType");
        dcpTypeNode.appendChild(httpNode);

        Element requestNameNode = doc.createElement(requestName);
        for (String format : formatList) {
            requestNameNode.appendChild(textnode(doc, "Format", format));
        }
        requestNameNode.appendChild(dcpTypeNode);

        return requestNameNode;
    }

}
