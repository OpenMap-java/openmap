/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WmsRequestHandler.java,v 1.2 2008/01/29 22:04:13 dietrick Exp $
 *
 * Copyright 2001-2005 OBR Centrum Techniki Morskiej, All rights reserved.
 *
 */
package com.bbn.openmap.image.wms;

import java.awt.Paint;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
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
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.proj.coords.CoordinateReferenceSystem;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

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

    private Map wmsLayers;

    private WmsLayerFactory wmsLayerFactory;

    /**
     * Creates a new WmsRequestHandler object.
     * 
     * @param port
     * @param props Properties from openmap.properties
     * @param mapLayerHandler
     * @param wmsLayersMap
     * @throws IOException
     */
    public WmsRequestHandler(String wmsScheme, String wmsHostName, int wmsPort, String wmsUrlPath,
            Properties props) throws IOException, WMSException {

        super(props);
        setProperties(null, props);
        
        // separate antialias property for wms.
        boolean antialias = PropUtils.booleanFromProperties(props, "openmap.wms."
                + AntiAliasingProperty, false);
        setDoAntiAliasing(antialias);

        // for each Openmap Layer created by ImageServer (defined in properties)
        // create corresponding IWmsLayer which contains all neccesary
        // information required by
        // WMS (e.g getCapabilities method)
        wmsLayerFactory = new WmsLayerFactory(props);
        createWmsLayers();

        // read from configuration fixed part of Capabilities Document returned
        // in getCapabilities method
        capabilities = new CapabilitiesSupport(props, wmsScheme, wmsHostName, wmsPort, wmsUrlPath);
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
     * 
     * For Layers that already implement IWmsLayer, the instances will be the same.
     */
    protected void createWmsLayers() {
        wmsLayers = new HashMap();
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            IWmsLayer wmsLayer = wmsLayerFactory.createWmsLayer(layer);
            wmsLayers.put(wmsLayer.getWmsName(), wmsLayer);
        }
    }
    
    protected Layer getLayerByName(String layerPropertyPrefix) {
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            if (layerPropertyPrefix.equals(layer.getPropertyPrefix())) {
                return layer;
            }
        }
        return null;
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
        
        checkLayersAndStyles(requestProperties, parameters);

        Debug.message("ms", "handleGetMapRequest: createImage layers:" + parameters.layerNames.toString());
        return createImage(projection, parameters.width, parameters.height, parameters.layerNames, bgPaint);
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
        httpResponse.writeHttpResponse(HttpConnection.CONTENT_XML, response.getBytes("UTF-8"));
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
        capabilities.setProjections(CoordinateReferenceSystem.getCodes());
        capabilities.setBoundingBox("-180", "-90", "180", "90");
        try {
            return capabilities.generateXMLString();
        } catch (Exception e) {
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
        
        String contentType = requestProperties.getProperty(INFO_FORMAT);
        String content = handleGetFeatureInfoRequest(requestProperties);
        
        byte[] contentBytes = content.getBytes("UTF-8");
        
        httpResponse.writeHttpResponse(contentType, contentBytes);
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
    public String handleGetFeatureInfoRequest(Properties requestProperties) throws IOException,
            MapRequestFormatException, WMSException {
        
        GetFeatureInfoRequestParameters parameters = new GetFeatureInfoRequestParameters();
        
        checkFormat(requestProperties, parameters);
        setFormatter(parameters.formatter);
        checkBackground(requestProperties, parameters);
        checkProjectionType(requestProperties, parameters);
        checkBoundingBox(requestProperties, parameters);
        checkFeatureInfoPoint(requestProperties, parameters);
        
        checkLayersAndStyles(requestProperties, parameters);
        checkQueryLayers(requestProperties, parameters);
        checkInfoFormat(requestProperties, parameters);

        Proj projection = createProjection(requestProperties, parameters);
        
        // TODO: get a user defined FeatureInfoResponse
        FeatureInfoResponse featureInfoResponse = null;
        if (featureInfoResponse == null) {
            // TODO: log that user defined was not found
            featureInfoResponse = new DefaultFeatureInfoResponse();
        }
        
        for(Iterator it = parameters.queryLayerNames.iterator(); it.hasNext();){
            String queryLayerName = (String)it.next();
            
            IWmsLayer wmslayer = (IWmsLayer) wmsLayers.get(queryLayerName);
            Layer layer = getLayerByName(queryLayerName);
            
            layer.setProjection(new ProjectionEvent(this, projection));
            
            LayerFeatureInfoResponse layerResponse = wmslayer.query(parameters.x, parameters.y);
            featureInfoResponse.add(layerResponse);
        }
        
        StringBuffer out = new StringBuffer();
        featureInfoResponse.output(parameters.infoFormat, out);
        return out.toString();
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
            // wms 1.3.0 uses CRS parameter instead of SRS
            strSRS = requestProperties.getProperty(CRS);
        }
        if (strSRS == null) {
            throw new WMSException("Missing SRS parameter.");
        }
        
        System.out.println("crs: " + strSRS);
        CoordinateReferenceSystem crs = CoordinateReferenceSystem.getForCode(strSRS);
        if (crs == null) {
            throw new WMSException("Invalid SRS/CRS parameter: " + strSRS, WMSException.INVALIDSRS);
        }
        parameters.crs = crs;
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
        String strWidth = requestProperties.getProperty(WIDTH);
        if (strWidth == null) {
            throw new WMSException("Missing WIDTH parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }
        String strHeight = requestProperties.getProperty(HEIGHT);
        if (strHeight == null) {
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

        try {
            // BBOX is minx, miny, maxx, maxy
            double minX = Double.parseDouble(arrayBBox[0]);
            double minY = Double.parseDouble(arrayBBox[1]);
            double maxX = Double.parseDouble(arrayBBox[2]);
            double maxY = Double.parseDouble(arrayBBox[3]);
            double medX = ((maxX - minX) / 2d) + minX;
            double medY = ((maxY - minY) / 2d) + minY;

            // use CRS to convert BBOX to latlon values
            CoordinateReferenceSystem crs = parameters.crs;
            parameters.bboxLatLonMinXY = crs.inverse(minX, minY);
            parameters.bboxLatLonMaxXY = crs.inverse(maxX, maxY);
            parameters.bboxLatLonCenter = crs.inverse(medX, medY);

            // TODO: use CRS to check value validity?
        } catch (NumberFormatException e) {
            throw new WMSException(
                    "Invalid BBOX parameter. BBOX parameter must be in the form of minx, miny, maxx, maxy"
                            + " confirming to the selected SRS/CRS.",
                    WMSException.INVALIDDIMENSIONVALUE);
        }
        parameters.width = 0;
        try {
            parameters.width = Integer.parseInt(strWidth);
            if (parameters.width <= 0) {
                throw new WMSException("Invalid values encountered while parsing WIDTH parameter.");
            }
            // Debug.output("intWidth = " + intWidth);
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid values encountered while parsing WIDTH parameter.");
        }
        parameters.height = 0;
        try {
            parameters.height = Integer.parseInt(strHeight);
            if (parameters.height <= 0) {
                throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
            }
            // Debug.output("intHeight = " + intHeight);
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
        }
    }
    
    private void checkLayersAndStyles(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        String strLayers = requestProperties.getProperty(LAYERS);
        if (strLayers == null) {
            throw new WMSException("LAYERS not specified.", WMSException.LAYERNOTDEFINED);
        }
        if (Debug.debugging("imageserver")) {
            Debug.output("OGCMRH.checkLayersAndStyles: requested layers >> " + strLayers);
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
        
        parameters.layerNames.clear();
        
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
            parameters.layerNames.add(layerName);
        }
        if (parameters.layerNames.isEmpty()) {
            throw new WMSException("LAYERS not specified.", WMSException.LAYERNOTDEFINED);
        }
    }
    
    private void checkQueryLayers(Properties requestProperties,
            GetFeatureInfoRequestParameters parameters) throws WMSException {

        String strLayers = requestProperties.getProperty(QUERY_LAYERS);
        if (strLayers == null) {
            throw new WMSException("QUERY_LAYERS not specified.", WMSException.LAYERNOTDEFINED);
        }
        if (Debug.debugging("imageserver")) {
            Debug.output("OGCMRH.checkQueryLayers: requested layers >> " + strLayers);
        }
        String[] layers_in = strLayers.replace('\"', '\0').split(",", -1);

        parameters.queryLayerNames.clear();

        for (int i = 0; i < layers_in.length; i++) {
            String layerName = layers_in[i];
            
            if(!parameters.layerNames.contains(layerName)){
                throw new WMSException("Layers missing Query Layer " + layerName + ".",
                        WMSException.LAYERNOTDEFINED);
            }

            IWmsLayer layer = (IWmsLayer)wmsLayers.get(layerName);
            if(layer == null){
                throw new WMSException("Could not find layer " + layerName);
            }
            
            if(!layer.isQueryable()){
                throw new WMSException("Layer " + layerName + " is not queryable");
            }
            
            parameters.queryLayerNames.add(layerName);
        }

    }
    

    /**
     * @param requestProperties
     * @param parameters
     * @return
     * @throws WMSException
     */
    private Proj createProjection(Properties requestProperties, GetMapRequestParameters parameters)
            throws WMSException {
        
        Properties projProps = new Properties();
        projProps.put(ProjectionFactory.CENTER, new LatLonPoint.Double(0f, 0f));
        projProps.setProperty(ProjectionFactory.WIDTH, Integer.toString(parameters.width));
        projProps.setProperty(ProjectionFactory.HEIGHT, Integer.toString(parameters.height));
        
        //ProjectionLoader projLoader = parameters.crs.projectionLoader();
        //Proj projection = (Proj) projLoader.create(projProps);
        Proj projection = parameters.crs.createProjection(projProps);
        parameters.crs.prepareProjection(projection);
        projection.setScale(projection.getMinScale());
        System.out.println("projection: " + projection.toString());

        // w ogólnoœci, ¿eby okreœliæ œrodek we wspó³rzêdnych geograficznych
        // trzeba rzutowaæ punkty graniczne
        // na przestrzeñ obrazu (piksele), obliczyæ œrodek i rzutowaæ go
        // spowrotem na wspó³rzêdne geograficzne
        
        LatLonPoint llp1 = parameters.bboxLatLonMinXY;
        LatLonPoint llp2 = parameters.bboxLatLonMaxXY;
        System.out.println("bbox toLatLon: 1: " + llp1 + ", 2: " + llp2 + ", center: " + parameters.bboxLatLonCenter);
        
        // guess a center value
        // TODO: calculate this from bbox values instead of after latlon converting?
        //float centerLat = ((llp2.getLatitude() - llp1.getLatitude()) / 2) + llp1.getLatitude();
        //float centerLon = ((llp2.getLongitude() - llp1.getLongitude()) / 2) + llp1.getLongitude();
        //projection.setCenter(centerLat, centerLon);
        projection.setCenter(parameters.bboxLatLonCenter);
        
        // Debug.output("L1: " + llp1.toString()+", L2: " + llp2.toString();
        // TODO: need to set projection.center before using the projection

        int intnewwidth = parameters.width;
        int intnewheight = parameters.height;
        
        float newscale = projection.getScale(llp1, llp2, new Point(0, 0), new Point(intnewwidth,
                intnewheight));
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
    
    private void checkFeatureInfoPoint(Properties requestProperties,
            GetFeatureInfoRequestParameters parameters) throws WMSException {

        parameters.x = -1;
        parameters.y = -1;
        
        try {
            parameters.x = Integer.parseInt(requestProperties.getProperty(X));
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid X parameter: " + requestProperties.getProperty(X),
                    WMSException.INVALIDPOINT);
        }

        try {
            parameters.y = Integer.parseInt(requestProperties.getProperty(Y));
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid Y parameter: " + requestProperties.getProperty(Y),
                    WMSException.INVALIDPOINT);
        }
    }
    
    private void checkInfoFormat(Properties requestProperties,
            GetFeatureInfoRequestParameters parameters) throws WMSException {
        
        
        Collection okFormats = Arrays.asList(new String[] { HttpConnection.CONTENT_PLAIN,
                HttpConnection.CONTENT_HTML });
        
        String format = requestProperties.getProperty(INFO_FORMAT);
        if (!okFormats.contains(format)) {
            // TODO: use correct message!
            throw new WMSException("Invalid value for " + INFO_FORMAT + ": "
                    + requestProperties.getProperty(INFO_FORMAT));
        }
        
        parameters.infoFormat = format;
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
}
