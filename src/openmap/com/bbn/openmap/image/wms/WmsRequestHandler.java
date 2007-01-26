/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WmsRequestHandler.java,v 1.1 2007/01/26 15:04:22 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.bbn.openmap.Layer;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.image.ImageServerUtils;
import com.bbn.openmap.image.MapRequestFormatException;
import com.bbn.openmap.image.WMTConstants;
import com.bbn.openmap.layer.util.http.HttpConnection;
import com.bbn.openmap.layer.util.http.IHttpResponse;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;

/**
 * @version $Header:
 *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/WmsRequestHandler.java,v
 *          1.2 2006/03/27 10:51:13 tomrak Exp $
 * @author Adam Dawidowski
 * @author wachu
 * @author pitek
 */
public class WmsRequestHandler extends ImageServer implements ImageServerConstants {

    /**
     */
    private CapabilitiesSupport capabilities;

    private Map projectionMap;

    private Map wmsLayers;

    private WmsLayerFactory wmsLayerFactory;

    private Projection defaultProjection;

    /**
     * Creates a new WmsRequestHandler object.
     * 
     * @param port
     * @param props
     * @param mapLayerHandler
     * @param wmsLayersMap
     * @throws IOException
     */
    public WmsRequestHandler(int wmsPort, String wmsUrlPath, Properties props) throws IOException,
            WMSException {

        super(props);
        setProperties(null, props);

        // prepare information about supported Projections
        ProjectionFactory.loadDefaultProjections();
        String[] projs = ProjectionFactory.getAvailableProjections();
        projectionMap = new HashMap();
        Debug.message("ms", "Projections: " + projs.length);
        for (int i = 0; i < projs.length; i++) {
            Debug.message("ms", "projection: " + projs[i]);
            String projKey = CapabilitiesSupport.WMSPrefix + "projmap."
            + projs[i];
            String straliases = props.getProperty(projKey);
            if (straliases != null) {
                String[] aliases = straliases.split(" +");
                for (int j = 0; j < aliases.length; j++) {
                    projectionMap.put(aliases[j], projs[i]);
                }
            } else {
                Debug.message("ms", "Missing parameter " + projKey + ". ignore.");
            }
        }
        defaultProjection = ProjectionFactory.getDefaultProjectionFromEnvironment();

        // for each Openmap Layer created by ImageServer (defined in properties)
        // create corresponding IWmsLayer which contains all neccesary
        // information required by
        // WMS (e.g getCapabilities method)
        wmsLayerFactory = new WmsLayerFactory(props);
        createWmsLayers();

        // read from configuration fixed part of Capabilities Document returned
        // in getCapabilities method
        capabilities = new CapabilitiesSupport(props, wmsPort, wmsUrlPath);
        // set list of available formatters
        Iterator fmts_enum = getFormatters().values().iterator();
        ArrayList formatsList = new ArrayList();
        while (fmts_enum.hasNext()){
            formatsList.add(getFormatterContentType((ImageFormatter)fmts_enum.next()));
        }
        capabilities.setFormats(CapabilitiesSupport.FMT_GETMAP, formatsList);
    }

    /**
     * For each layer managed by ImageServer create corresponding IWmsLayer
     * which contains additional information for WMS service about given openmap
     * layer.
     */
    protected void createWmsLayers() {
        wmsLayers = new HashMap();
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            IWmsLayer wmsLayer = wmsLayerFactory.createWmsLayer(layer);
            wmsLayers.put(wmsLayer.getName(), wmsLayer);
        }
    }

    /**
     * @param request
     * @param out
     * @throws IOException
     * @throws MapRequestFormatException
     */
    public void handleRequest(Properties requestProperties, IHttpResponse httpResponse)
            throws IOException, MapRequestFormatException {
        try {
            String requestType = requestProperties.getProperty(REQUEST);
            checkRequest(requestProperties);
            if (requestType == null) {
                throw new WMSException("Missing REQUEST type parameter");
            }
            if (requestType.equalsIgnoreCase(GETMAP)) {
                Debug.message("ms", "OGCMRH: GetMap request...");
                handleGetMapRequest(requestProperties, httpResponse);
            } else if (requestType.equals(GETCAPABILITIES)) {
                Debug.message("ms", "OGCMRH: GetCapabilities request...");
                handleGetCapabilitiesRequest(requestProperties, httpResponse);
            } else if (requestType.equalsIgnoreCase(GETFEATUREINFO)) {
                Debug.message("ms", "OGCMRH: GetFeatureInfo request...");
                handleGetFeatureInfoRequest(requestProperties, httpResponse);
            } else {
                throw new WMSException("Invalid REQUEST parameter: " + requestType,
                        WMSException.OPERATIONNOTSUPPORTED);
            }
        } catch (WMSException e) {
            Debug.output("WMSException(" + e.getCode() + "): " + e.getMessage());
            httpResponse.writeHttpResponse("application/vnd.ogc.se_xml", e.getXML());
        }
    }

    /**
     * @param requestProperties
     * @param out
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public void handleGetMapRequest(Properties requestProperties, IHttpResponse httpResponse)
            throws IOException, MapRequestFormatException, WMSException {
        byte[] image = handleGetMapRequest(requestProperties);
        if (Debug.debugging("imageserver")) {
            Debug.output("OGCMRH: have completed image, size " + image.length);
        }
        String contentType = getFormatterContentType(getFormatter());
        if (contentType == null) {
            contentType = HttpConnection.CONTENT_PLAIN;
        }
        httpResponse.writeHttpResponse(contentType, image);
    }

    /**
     * @param requestProperties
     * @return
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public byte[] handleGetMapRequest(Properties requestProperties) throws IOException,
            MapRequestFormatException, WMSException {
        GetMapRequestParameters parameters = new GetMapRequestParameters();

        checkFormat(requestProperties, parameters);
        setFormatter(parameters.formatter);

        checkBackground(requestProperties, parameters);
        Paint bgPaint = parameters.background;

        checkProjectionType(requestProperties, parameters);
        checkBoundingBox(requestProperties, parameters);
        Proj projection = createProjection(requestProperties, parameters);

        // najpierw warstwy ...
        String strLayers = requestProperties.getProperty(LAYERS);
        if (strLayers == null) {
            throw new WMSException("LAYERS not specified.", WMSException.LAYERNOTDEFINED);
        }
        if (Debug.debugging("imageserver")) {
            Debug.output("OGCMRH.handleGetMapRequest: requested layers >> " + strLayers);
        }
        String[] layers_in = strLayers.replace('\"', '\0').split(",", -1);
        // ... i style
        String strStyles = requestProperties.getProperty(STYLES);
        String[] styles_in = null;
        if (strStyles != null) {
            styles_in = strStyles.replace('\"', '\0').split(",", -1);
            if (styles_in.length != layers_in.length) {
                throw new WMSException(
                        "Number of specified styles does not match the number of specified layers.");
            }
        }
        // nazwy warstw, które maj¹ byæ rysowane
        List layers_out = new ArrayList();
        // odwróciæ kolejnoœæ warstw, bo WMS powinien renderowaæ pierwsz¹
        // warstwê na samym dole, drug¹ wy¿ej itd
        // imageserver renderuje w odwrotnej kolejnoœci
        // przy okazji sprawdziæ, czy podane warstwy istniej¹
        for (int i = layers_in.length - 1; i >= 0; i--) {
            String layerName = layers_in[i];
            IWmsLayer wmsLayer = (IWmsLayer) wmsLayers.get(layerName);
            if (wmsLayer == null) {
                throw new WMSException("Unknown layer specified (" + layerName + ").",
                        WMSException.LAYERNOTDEFINED);
            }
            // apply style to layer
            if (styles_in == null) {
                wmsLayer.setDefaultStyle();
            } else {
                String styleName = styles_in[i];
                if (styleName.equals("")) {
                    wmsLayer.setDefaultStyle();
                } else if (wmsLayer.isStyleSupported(styleName)) {
                    wmsLayer.setStyle(styleName);
                } else {
                    throw new WMSException("Unknown style specified (" + styleName + ").",
                            WMSException.STYLENOTDEFINED);
                }
            }
            layers_out.add(layerName);
        }
        if (layers_out.size() == 0) {
            throw new WMSException("LAYERS not specified.", WMSException.LAYERNOTDEFINED);
        }
        Debug.message("ms", "handleGetMapRequest: createImage layers:" + layers_out.toString());
        return createImage(projection, parameters.intWidth, parameters.intHeight, layers_out, bgPaint);
    }

    /**
     * @param requestProperties
     * @param out
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public void handleGetCapabilitiesRequest(Properties requestProperties,
            IHttpResponse httpResponse) throws IOException, MapRequestFormatException, WMSException {
        String response = handleGetCapabilitiesRequest(requestProperties);
        httpResponse.writeHttpResponse(HttpConnection.CONTENT_XML, response);
    }

    /**
     * @param requestProperties
     * @return
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public String handleGetCapabilitiesRequest(Properties requestProperties) throws IOException,
            MapRequestFormatException, WMSException {

        String format = requestProperties.getProperty(FORMAT);
        if (format != null && !format.equals("application/vnd.ogc.wms_xml")) {
            throw new WMSException("Invalid FORMAT parameter.", WMSException.INVALIDFORMAT);
        }

        capabilities.clearLayers();
        Layer[] layers = getLayers();
        for (int i = 0; i < layers.length; i++) {
            if (layers[i].getPropertyPrefix() != null) {
                capabilities.addLayer(wmsLayerFactory.createWmsLayer(layers[i]));
            }
        }
        capabilities.setLayersTitle("MapServer Layers List");
        capabilities.setProjections(projectionMap.keySet());
        capabilities.setBoundingBox("-180", "-90", "180", "90");
        try {
            return capabilities.generateXMLString();
        } catch (IOException e) {
            // nie ma takiego kodu b³êdu, ale s¹dzê, ¿e powinien byæ
            throw new WMSException("Unable to compile a response due to server misconfiguration.",
                    WMSException.INTERNALERROR);
        }
    }

    /**
     * @param requestProperties
     * @param out
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public void handleGetFeatureInfoRequest(Properties requestProperties, IHttpResponse httpResponse)
            throws IOException, MapRequestFormatException, WMSException {
        String asd = new String(handleGetFeatureInfoRequest(requestProperties));
        httpResponse.writeHttpResponse(HttpConnection.CONTENT_XML, asd);
    }

    /**
     * <ul>
     * <li>VERSION - checked
     * <li>REQUEST - checked
     * <li>EXCEPTIONS - checked
     * <li>all from <code>GetMap</code> except VERSION and REQUEST
     * <li>QUERY_LAYERS - specific
     * <li>INFO_FORMAT - specific
     * <li>FEATURE_COUNT - specific
     * <li>I,J - specific
     * </ul>
     * 
     * @param requestProperties
     * @return
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public byte[] handleGetFeatureInfoRequest(Properties requestProperties) throws IOException,
            MapRequestFormatException, WMSException {
        throw new WMSException("GetFeatureInfo is not implemented by this server",
                WMSException.OPERATIONNOTSUPPORTED);
    }

    /**
     * metoda wywo³ywana po odczycie nowych danych o celach , w celu wgrania
     * nowo utworzonych warstw graficznych (przy wykorzystaniu layerHandlera) na
     * podstawie wczeœniej za³adowach danych o celach przy czym warstwy ze
     * statycznymi danymi tzn mapami portow , nie s¹ uaktualniane, a tylko
     * pozosta³e warstwy (czytane z LayerHandler'a) s¹ uaktualniane b¹dz
     * dodawane do tablicy layers. Tablica layers przechowuje warstwy
     * wykorzystywane przez image serwera do generowania obrazka.
     */
    public void updateLayers() {
    }

    /**
     * @todo ta metoda pokrywa odpowiednik z klasy bazowej i zwraca rysunek,
     *       który nie jest przeskalowywany. To mo¿e byæ problem. Przeskalowanie
     *       zosta³o zablokowane, poniewa¿ tworzy³o rysunek bez kana³u alfa, a
     *       przynajmniej na to wygl¹da³o.
     * @param formatter
     * @param scaledWidth
     * @param scaledHeight
     * @return
     */
    protected byte[] getFormattedImage(ImageFormatter formatter, int scaledWidth, int scaledHeight) {
        Debug.message("imageserver", "ImageServer: using full scale image (unscaled).");
        byte[] formattedImage = formatter.getImageBytes();
        return formattedImage;
    }

    /**
     * @param requestProperties
     * @throws WMSException
     */
    private void checkRequest(Properties requestProperties) throws WMSException {
        String version = requestProperties.getProperty(VERSION);
        String service = requestProperties.getProperty(SERVICE);
        String requestType = requestProperties.getProperty(REQUEST);

        boolean getcaps = ((requestType != null) && requestType.equals(GETCAPABILITIES));
        if (!getcaps) {
            // check version string - use a default for clients that does not
            // specify
            if (version == null) {
                version = "1.1.1";
                Debug.message("wms", "missing version string. use " + version);
            }
            // TODO: handle other versions?
            if (!version.equals("1.1.1")) {
                throw new WMSException("Unsupported protocol version: " + version);
            }
            String ex = requestProperties.getProperty(EXCEPTIONS);
            // if ((ex != null) && !ex.equals("application/vnd.ogc.se_xml")) {
            // Poprawka wacha
            // poni¿szy warunek roszszerzy³em o application/vnd.ogc.se_inimage
            // gdy¿ WMSplugin wymaga tego formatu exception'ów , który jest
            // formatem opcjonalnym dla WMS
            if ((ex != null)
                    && (!ex.equals("application/vnd.ogc.se_inimage") && !ex
                            .equals("application/vnd.ogc.se_xml"))) {
                throw new WMSException("Invalid EXCEPTIONS value: " + ex);
            }
        } else {
            if ((service == null) || !service.equals("WMS")) {
                throw new WMSException("Unsupported service name: " + service);
            }
        }
    }

    /**
     * @param requestProperties
     * @param parameters
     * @throws WMSException
     */
    private void checkProjectionType(Properties requestProperties,
            GetMapRequestParameters parameters) throws WMSException {
        String strSRS = requestProperties.getProperty(SRS);
        if (strSRS == null) {
            strSRS = requestProperties.getProperty(CRS);
        }
        if (strSRS == null) {
            throw new WMSException("Missing SRS parameter.");
        }
        String projType = (String) projectionMap.get(strSRS);
        if (projType == null) {
            // fail if the projection parameter is not listed in openmap.properties. does not
            // check that a valid Projections object is possible to retrive.
            throw new WMSException("Invalid SRS/CRS parameter: " + strSRS, WMSException.INVALIDSRS);
        }
        parameters.projType = projType;
    }

    /**
     * To w ogólnoœci powinno zale¿eæ od typu porojekcji.
     * 
     * @param requestProperties
     * @param parameters
     * @throws WMSException
     */
    private void checkBoundingBox(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        parameters.strWidth = requestProperties.getProperty(WIDTH);
        if (parameters.strWidth == null) {
            throw new WMSException("Missing WIDTH parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }
        parameters.strHeight = requestProperties.getProperty(HEIGHT);
        if (parameters.strHeight == null) {
            throw new WMSException("Missing HEIGHT parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }
        String strBBox = requestProperties.getProperty(BBOX);
        if (strBBox == null) {
            throw new WMSException("Missing BBOX parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }
        String[] arrayBBox = strBBox.split(",");
        if (arrayBBox.length != 4) {
            throw new WMSException(
                    "Invalid BBOX parameter. BBOX must contain exactly 4 values separated with comas.",
                    WMSException.INVALIDDIMENSIONVALUE);
        }

        // FIXME - sprawdzenia poni¿ej s¹ poprawne TYLKO dla WGS-84 (SRS =
        // EPSG:4326)
        // dla innych projekcji (AUTO2) mog¹ przyjmowaæ inne wartoœci

        try {
            for (int i = 0; i < 4; i++) {
                parameters.doubleBBox[i] = Double.parseDouble(arrayBBox[i]);
                // Debug.output("doubleBBox[" + i + "] = " + doubleBBox[i]);
            }
            if (LatLonPoint.isInvalidLatitude((float) parameters.doubleBBox[1])
                    || LatLonPoint.isInvalidLatitude((float) parameters.doubleBBox[3])) {
                throw new WMSException(
                        "Invalid BBOX parameter. Latitudes must be in the range -90.0 - 90.0.",
                        WMSException.INVALIDDIMENSIONVALUE);
            }
            if (LatLonPoint.isInvalidLongitude((float) parameters.doubleBBox[1])
                    || LatLonPoint.isInvalidLongitude((float) parameters.doubleBBox[3])) {
                throw new WMSException(
                        "Invalid BBOX parameter. Longitudes must be in the range -180.0 - 180.0.",
                        WMSException.INVALIDDIMENSIONVALUE);
            }

            if ((parameters.doubleBBox[0] >= parameters.doubleBBox[2])
                    || (parameters.doubleBBox[1] >= parameters.doubleBBox[3])) {
                throw new WMSException("Invalid BBOX parameter. First two values must be less than"
                        + "last two coresponding values.", WMSException.INVALIDDIMENSIONVALUE);
            }
        } catch (NumberFormatException e) {
            throw new WMSException(
                    "Invalid BBOX parameter. BBOX parameter must be in the form of LON,LAT,LON,LAT"
                            + " where LON is longitude and LAT is latitude of two points.",
                    WMSException.INVALIDDIMENSIONVALUE);
        }
        parameters.intWidth = 0;
        try {
            parameters.intWidth = Integer.parseInt(parameters.strWidth);
            if (parameters.intWidth <= 0) {
                throw new WMSException("Invalid values encountered while parsing WIDTH parameter.");
            }
            // Debug.output("intWidth = " + intWidth);
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid values encountered while parsing WIDTH parameter.");
        }
        parameters.intHeight = 0;
        try {
            parameters.intHeight = Integer.parseInt(parameters.strHeight);
            if (parameters.intHeight <= 0) {
                throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
            }
            // Debug.output("intHeight = " + intHeight);
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
        }
        parameters.aspect = Math.abs((parameters.doubleBBox[1] - parameters.doubleBBox[3])
                / (parameters.doubleBBox[0] - parameters.doubleBBox[2]));
    }

    /**
     * @param requestProperties
     * @param parameters
     * @return
     * @throws WMSException
     */
    private Proj createProjection(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        Properties newprops = new Properties();
        newprops.setProperty(PROJTYPE, parameters.projType);
        newprops.setProperty(LAT, "0.0");
        newprops.setProperty(LON, "0.0");
        // Ca³y "myk" polega na tym, ¿eby dostarczyæ klientowi obrazek o
        // dowolnych proporcjach obrazu
        // i wymiarów geograficznych. W tym celu wewnêtrznie tworzymy obraz o
        // proporcjach ¿¹danego BBOX'a
        // i pozwalamy createImage przeskalowaæ obrazek do zadanego
        // WIDTH/HEIGHT.
        // poproszê projektor o wygenerowanie obrazu o takich wymiarach:
        // przyj¹³em metodê nadpróbkowania obrazu, tj jeœli potrzebujê obraz o Y
        // > X to zwiêkszam Y, a nie zmniejszam X
        if (parameters.aspect > 1.0) {
            newprops.setProperty(WIDTH, parameters.strWidth);
            newprops.setProperty(HEIGHT, Integer
                    .toString((int) ((double) parameters.intWidth * parameters.aspect)));
        } else if (parameters.aspect < 1.0) {
            newprops.setProperty(WIDTH, Integer
                    .toString((int) ((double) parameters.intHeight / parameters.aspect)));
            newprops.setProperty(HEIGHT, parameters.strHeight);
        } else {
            newprops.setProperty(WIDTH, parameters.strHeight); // bo aspect ==
                                                                // 1.0 (kwadrat)
            newprops.setProperty(HEIGHT, parameters.strHeight);
        }
        Proj projection = ImageServerUtils.createOMProjection(newprops, defaultProjection);
        //setBackground(ImageServerUtils.getBackground(requestProperties));

        projection.setScale(projection.getMinScale());
        // w ogólnoœci, ¿eby okreœliæ œrodek we wspó³rzêdnych geograficznych
        // trzeba rzutowaæ punkty graniczne
        // na przestrzeñ obrazu (piksele), obliczyæ œrodek i rzutowaæ go
        // spowrotem na wspó³rzêdne geograficzne
        LatLonPoint llp1 = new LatLonPoint.Double(parameters.doubleBBox[1], parameters.doubleBBox[0]);
        LatLonPoint llp2 = new LatLonPoint.Double(parameters.doubleBBox[3], parameters.doubleBBox[2]);
        // Debug.output("L1: " + llp1.toString()+", L2: " + llp2.toString();
        Point2D p1 = projection.forward(llp1);
        Point2D p2 = projection.forward(llp2);
        // Debug.output("p1: " + p1.toString() + "\np2: " + p2.toString());
        LatLonPoint lcenter = (LatLonPoint) projection.inverse((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2, new LatLonPoint.Double());

        // Drugi sposób wyliczania œrodka na podstawie odleg³oœci i kursu ale
        // przy Mercatorze nie widaæ poprawy
        // i tak sa du¿e skoki przy przesuwaniu mapy w pionie dla LXY wszystko
        // jest ok
        // a poza tym przy korzystaniu z Udiga przy poborze mapy ca³y œwiat ten
        // sposób siê nie sprawdza.
        // LatLonPoint lcenter2= llp1.getPoint(llp1.distance(llp2)/2,
        // llp1.azimuth(llp2));
        // Debug.output("LCenter2: " + lcenter2.toString());

        // Debug.output("LCenter: " + lcenter.toString());
        projection.setCenter(lcenter);
        int intnewwidth;
        int intnewheight;
        if (parameters.aspect > 1.0) {
            intnewwidth = parameters.intWidth;
            intnewheight = (int) ((double) parameters.intWidth * parameters.aspect);
            projection.setHeight(intnewheight);
            // Debug.output("newHeight: " + intnewheight);
        } else if (parameters.aspect < 1.0) {
            intnewwidth = (int) ((double) parameters.intHeight / parameters.aspect);
            intnewheight = parameters.intHeight;
            projection.setWidth(intnewwidth);
            // Debug.output("newWidth: " + intnewwidth);
        } else {
            intnewwidth = parameters.intHeight; // bo aspect == 1.0 (kwadrat)
            intnewheight = parameters.intHeight;
        }
        float newscale = projection.getScale(llp1, llp2, new Point(0, 0), new Point(intnewwidth,
                intnewwidth /* czy tu nie jest b³¹d? */));
        projection.setScale(newscale);
        return projection;
    }

    /**
     * @param requestProperties
     * @param parameters
     * @throws WMSException
     */
    private void checkFormat(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        String format = requestProperties.getProperty(FORMAT);

        // tu zbadaæ podany format i ewentualnie rzuciæ WMSException
        if (format == null) {
            throw new WMSException("Missing FORMAT parameter.", WMSException.INVALIDFORMAT);
        }
        Iterator fmts_enum = getFormatters().values().iterator();
        boolean found = false;
        while (fmts_enum.hasNext()) {
            parameters.formatter = (ImageFormatter) fmts_enum.next();
            String contentType = getFormatterContentType(parameters.formatter);
            if (format.equals(contentType)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new WMSException("Invalid FORMAT parameter: " + format,
                    WMSException.INVALIDFORMAT);
        }
    }

    /**
     * @param requestProperties
     * @param parameters
     * @throws WMSException
     */
    private void checkBackground(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        String test = requestProperties.getProperty(TRANSPARENT);
        if ((test != null)
                && (test.equals("0") && test.equals("1") && test.equalsIgnoreCase("TRUE") && test
                        .equalsIgnoreCase("FALSE"))) {
            throw new WMSException(
                    "Invalid TRANSPARENT format. Please specify a boolean value (0,1,FALSE,TRUE)");
        }
        test = requestProperties.getProperty(BGCOLOR);
        if ((test != null) && !Pattern.matches("0x[0-9a-fA-F]{6}", test)) {
            throw new WMSException("Invalid BGCOLOR format. Please specify an hexadecimal"
                    + " number in the form 0xXXXXXX, where X is a hexadecimal digit (0..9,A-F)");
        }
        parameters.background = ImageServerUtils.getBackground(requestProperties, OMColor.clear);
    }

    /**
     * Given an ImageFormatter, get the HttpConnection content type that matches
     * it.
     */
    public String getFormatterContentType(ImageFormatter formatter) {
        String ret = null;
        String label = translateFormat(formatter.getFormatLabel());

        String[] knownContentTypes = HttpConnection.getAllContentTypes();

        for (int i = 0; i < knownContentTypes.length; i++) {
            if (knownContentTypes[i].indexOf(label.toLowerCase()) != -1) {
                ret = knownContentTypes[i];
                break;
            }
        }
        return ret;
    }

    /**
     * Converts format defined in {@link WMTConstants} into standard
     * <i>ContentType</i> form defined in {@link HttpConnection}.
     * 
     * @param format
     *            image format to translate, eg. <i>GIF</i>
     * @return translated image format eg. <i>image/gif</i>
     */
    private String translateFormat(String format) {
        if (format.equals(WMTConstants.IMAGEFORMAT_JPEG)) {
            return HttpConnection.CONTENT_JPEG;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_PNG)) {
            return HttpConnection.CONTENT_PNG;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_GIF)) {
            return HttpConnection.CONTENT_GIF;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_TIFF)) {
            return HttpConnection.CONTENT_TIFF;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_GEOTIFF)) {
            return HttpConnection.CONTENT_GEOTIFF;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_PPM)) {
            return HttpConnection.CONTENT_PPM;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_WBMP)) {
            return HttpConnection.CONTENT_WBMP;
        } else if (format.equals(WMTConstants.IMAGEFORMAT_SVG)) {
            return HttpConnection.CONTENT_SVG;
        } else {
            return format;
        }
    }

    /*
     * @version $Header:
     *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/WmsRequestHandler.java,v
     *          1.2 2006/03/27 10:51:13 tomrak Exp $
     * @version $Header:
     *          /cvs/CVS_LEBA/external/openmap/openmap/src/openmap/com/bbn/openmap/wms/WmsRequestHandler.java,v
     *          1.2 2006/03/27 10:51:13 tomrak Exp $ @author
     */
    private static class GetMapRequestParameters {

        /**
         */
        public int intWidth;

        /**
         */
        public int intHeight;

        /**
         */
        public String strWidth;

        /**
         */
        public String strHeight;

        /**
         */
        public double aspect;

        /**
         */
        public String projType;

        /**
         */
        public double[] doubleBBox = new double[4];

        /**
         */
        public ImageFormatter formatter;

        /**
         */
        public Paint background;
    }
}
