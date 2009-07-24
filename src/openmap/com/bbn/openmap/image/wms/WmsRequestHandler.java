/*
 * $Header: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/wms/WmsRequestHandler.java,v 1.5 2008/10/16 03:30:35 dietrick Exp $
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.image.ImageFormatter;
import com.bbn.openmap.image.ImageServer;
import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.image.ImageServerUtils;
import com.bbn.openmap.image.MapRequestFormatException;
import com.bbn.openmap.layer.util.http.HttpConnection;
import com.bbn.openmap.layer.util.http.IHttpResponse;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.proj.AspectRatioProjection;
import com.bbn.openmap.proj.GeoProj;
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

    private Map<String, IWmsLayer> wmsLayerByName = new HashMap<String, IWmsLayer>();

    private List<IWmsLayer> wmsLayers = new ArrayList<IWmsLayer>();

    private WmsLayerFactory wmsLayerFactory;

    private Map<String, ImageFormatter> imageFormatterByContentType = new HashMap<String, ImageFormatter>();

    /**
     * Creates a new WmsRequestHandler object.
     * 
     * @param port
     * @param props
     *            Properties from openmap.properties
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
        // create corresponding IWmsLayer which contains all necessary
        // information required by
        // WMS (e.g getCapabilities method)
        wmsLayerFactory = new WmsLayerFactory(props);
        createWmsLayers();

        // create a Map of all formatters by their contentType
        for (ImageFormatter formatter : getFormatters().values()) {
            imageFormatterByContentType.put(formatter.getContentType(), formatter);
        }

        // read from configuration fixed part of Capabilities Document returned
        // in getCapabilities method
        capabilities = new CapabilitiesSupport(props, wmsScheme, wmsHostName, wmsPort, wmsUrlPath);
        List<String> formatsList = new ArrayList<String>(imageFormatterByContentType.keySet());
        capabilities.setFormats(CapabilitiesSupport.FMT_GETMAP, formatsList);
    }

    /**
     * For each layer managed by ImageServer create corresponding IWmsLayer
     * which contains additional information for WMS service about given openmap
     * layer.
     * 
     * For Layers that already implement IWmsLayer, the instances will be the
     * same.
     */
    protected void createWmsLayers() {
        wmsLayerByName.clear();
        wmsLayers.clear();
        for (int i = 0; i < layers.length; i++) {
            Layer layer = layers[i];
            createWmsLayers(wmsLayerFactory.createWmsLayer(layer));
        }
    }

    private void createWmsLayers(IWmsLayer layer) {
        wmsLayerByName.put(layer.getWmsName(), layer);
        wmsLayers.add(layer);
        if (layer instanceof IWmsNestedLayer) {
            IWmsNestedLayer n = (IWmsNestedLayer) layer;
            if (n.getNestedLayers() != null) {
                for (int i = 0; i < n.getNestedLayers().length; i++) {
                    createWmsLayers(n.getNestedLayers()[i]);
                }
            }
        }
    }

    /**
     * Set the request parameters on all the layers
     * 
     * @see IWmsLayer#setRequestParameters(Properties)
     * @param requestProperties
     */
    protected void setRequestParametersOnLayers(Properties requestProperties) {
        // use a Set to make sure we only set it once for each layer
        Set<String> handledNames = new HashSet<String>();
        for (IWmsLayer wmsLayer : wmsLayers) {
            if (!handledNames.contains(wmsLayer.getWmsName())) {
                wmsLayer.setRequestParameters(requestProperties);
                handledNames.add(wmsLayer.getWmsName());
            }
            if (wmsLayer instanceof IWmsNestedLayer) {
                IWmsNestedLayer nestedLayer = (IWmsNestedLayer) wmsLayer;
                // make sure the top layer also get info about the request
                // parameters
                if (!handledNames.contains(nestedLayer.getTopLayer().getWmsName())) {
                    nestedLayer.getTopLayer().setRequestParameters(requestProperties);
                    handledNames.add(nestedLayer.getTopLayer().getWmsName());
                }
            }
        }
    }

    protected IWmsLayer getLayerByName(String wmsName) {
        return (IWmsLayer) wmsLayerByName.get(wmsName);
    }

    /**
     * Return the top OpenMap {@link Layer} for the given wms layer name.
     * 
     * @param name
     * @return
     */
    protected Layer getTopLayerByName(String wmsName) {
        IWmsLayer layer = getLayerByName(wmsName);
        if (layer == null) {
            return null;
        }
        if (layer instanceof IWmsNestedLayer) {
            layer = ((IWmsNestedLayer) layer).getTopLayer();
        }
        if (layer instanceof DefaultLayerAdapter) {
            return ((DefaultLayerAdapter) layer).layer;
        }
        if (layer instanceof Layer) {
            return (Layer) layer;
        }
        throw new IllegalStateException("Top layer must be a OpenMap Layer, not "
                + layer.getClass());
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
            setRequestParametersOnLayers(requestProperties);
            if (requestType.equalsIgnoreCase(GETMAP)) {
                Debug.message("ms", "OGCMRH: GetMap request...");
                handleGetMapRequest(requestProperties, httpResponse);
            } else if (requestType.equals(GETCAPABILITIES)) {
                Debug.message("ms", "OGCMRH: GetCapabilities request...");
                handleGetCapabilitiesRequest(requestProperties, httpResponse);
            } else if (requestType.equalsIgnoreCase(GETFEATUREINFO)) {
                Debug.message("ms", "OGCMRH: GetFeatureInfo request...");
                handleGetFeatureInfoRequest(requestProperties, httpResponse);
            } else if (requestType.equalsIgnoreCase(GETLEGENDGRAPHIC)) {
                Debug.message("ms", "OGCMRH: GetFeatureInfo request...");
                handleGetLegendGraphicRequest(requestProperties, httpResponse);
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
        String contentType = getFormatter().getContentType();
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
        checkWidthAndHeight(requestProperties, parameters);
        checkBoundingBox(requestProperties, parameters);
        Proj projection = createProjection(requestProperties, parameters);

        checkLayersAndStyles(requestProperties, parameters);

        Debug.message("ms", "handleGetMapRequest: createImage layers:"
                + parameters.topLayerNames.toString());
        return createImage(projection, parameters.width, parameters.height,
                parameters.topLayerNames, bgPaint);
    }

    public byte[] handleGetLegendGraphicRequest(Properties requestProperties) throws IOException,
            MapRequestFormatException, WMSException {
        GetLegendGraphicRequestParameters parameters = new GetLegendGraphicRequestParameters();

        checkWidthAndHeight(requestProperties, parameters);
        checkFormat(requestProperties, parameters);
        setFormatter(parameters.getFormatter());
        checkLayerAndStyle(requestProperties, parameters);

        Debug.message("ms", "handleGetLegendGraphic: createImage layer:" + parameters.layerName);

        IWmsLayer layer = wmsLayerByName.get(parameters.layerName);

        ImageFormatter imageFormatter = formatter.makeClone();
        java.awt.Graphics graphics = createGraphics(imageFormatter, parameters.getWidth(),
                parameters.getHeight());

        if (graphics == null) {
            return new byte[0];
        }

        Legend legend = layer.getLegend();
        if (legend != null) {
            legend.setSize(parameters.getWidth(), parameters.getHeight());
            legend.paint(graphics);
        }
        
        byte[] formattedImage = getFormattedImage(imageFormatter, parameters.getWidth(), parameters
                .getHeight());
        graphics.dispose();
        
        return formattedImage;
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
     * Get the {@link CapabilitiesSupport} object. The
     * {@link CapabilitiesSupport} object can be modified and will be kept as
     * long as the {@link WmsRequestHandler}.
     * 
     * @return
     */
    public CapabilitiesSupport getCapabilities() {
        return capabilities;
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

        Layer[] layers = getLayers();
        for (int i = 0; i < layers.length; i++) {
            if (layers[i].getPropertyPrefix() != null) {
                getCapabilities().addLayer(wmsLayerFactory.createWmsLayer(layers[i]));
            }
        }

        try {
            return getCapabilities().generateXMLString();
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param requestProperties
     * @param out
     * @throws IOException
     * @throws MapRequestFormatException
     * @throws WMSException
     */
    public void handleGetLegendGraphicRequest(Properties requestProperties,
            IHttpResponse httpResponse) throws IOException, MapRequestFormatException, WMSException {
        byte[] image = handleGetLegendGraphicRequest(requestProperties);
        String contentType = getFormatter().getContentType();
        httpResponse.writeHttpResponse(contentType, image);
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
        checkWidthAndHeight(requestProperties, parameters);
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

        for (String queryLayerName : parameters.queryLayerNames) {
            IWmsLayer wmslayer = (IWmsLayer) wmsLayerByName.get(queryLayerName);
            Layer layer = getTopLayerByName(queryLayerName);

            layer.setProjection(new ProjectionEvent(this, projection));

            LayerFeatureInfoResponse layerResponse = wmslayer.query(parameters.x, parameters.y);
            featureInfoResponse.add(layerResponse);
        }

        StringBuffer out = new StringBuffer();
        featureInfoResponse.output(parameters.infoFormat, out);
        return out.toString();
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
    @Override
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

    private void checkWidthAndHeight(Properties requestProperties,
            WidthAndHeightRequestParameters parameters) throws WMSException {
        String strWidth = requestProperties.getProperty(WIDTH);
        if (strWidth == null) {
            throw new WMSException("Missing WIDTH parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }
        String strHeight = requestProperties.getProperty(HEIGHT);
        if (strHeight == null) {
            throw new WMSException("Missing HEIGHT parameter.", WMSException.MISSINGDIMENSIONVALUE);
        }

        parameters.setWidth(0);
        try {
            parameters.setWidth(Integer.parseInt(strWidth));
            if (parameters.getWidth() <= 0) {
                throw new WMSException("Invalid value encountered while parsing WIDTH parameter.");
            }
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid value encountered while parsing WIDTH parameter.");
        }
        parameters.setHeight(0);
        try {
            parameters.setHeight(Integer.parseInt(strHeight));
            if (parameters.getHeight() <= 0) {
                throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
            }
        } catch (NumberFormatException e) {
            throw new WMSException("Invalid value encountered while parsing HEIGHT parameter.");
        }

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
    }

    private void checkLayersAndStyles(Properties requestProperties,
            GetMapRequestParameters parameters) throws WMSException {
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

            // wms-1.1.1 7.2.3.4. "If all layers are
            // to be shown using the default style, either the form "STYLES=" or
            // "STYLES=,,," is valid."
            if (strStyles.equals("")) {
                styles_in = new String[layers_in.length];
                Arrays.fill(styles_in, "");
            }

            if (styles_in.length != layers_in.length) {
                throw new WMSException(
                        "Number of specified styles does not match the number of specified layers.");
            }
        }

        parameters.topLayerNames.clear();
        parameters.layerNames.clear();

        // odwróciæ kolejnoœæ warstw, bo WMS powinien renderowaæ pierwsz¹
        // warstwê na samym dole, drug¹ wy¿ej itd
        // imageserver renderuje w odwrotnej kolejnoœci
        // przy okazji sprawdziæ, czy podane warstwy istniej¹
        for (int i = layers_in.length - 1; i >= 0; i--) {
            String layerName = layers_in[i];

            IWmsLayer wmsLayer = (IWmsLayer) wmsLayerByName.get(layerName);
            if (wmsLayer == null) {
                throw new WMSException("Unknown layer specified (" + layerName + ").",
                        WMSException.LAYERNOTDEFINED);
            }

            if (wmsLayer instanceof IWmsNestedLayer) {
                IWmsNestedLayer nestedLayer = (IWmsNestedLayer) wmsLayer;
                String topLayerName = nestedLayer.getTopLayer().getWmsName();
                if (!parameters.topLayerNames.contains(topLayerName)) {
                    parameters.topLayerNames.add(topLayerName);
                }
                nestedLayer.setIsActive(true);
            } else {
                if (!parameters.topLayerNames.contains(layerName)) {
                    parameters.topLayerNames.add(layerName);
                }
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

    private void checkLayerAndStyle(Properties requestProperties,
            GetLegendGraphicRequestParameters parameters) throws WMSException {

        String layerName = requestProperties.getProperty(LAYER);
        if (layerName == null) {
            throw new WMSException(LAYER + " not specified.", WMSException.LAYERNOTDEFINED);
        }

        IWmsLayer wmsLayer = wmsLayerByName.get(layerName);
        if (wmsLayer == null) {
            throw new WMSException("Unknown layer specified (" + layerName + ").",
                    WMSException.LAYERNOTDEFINED);
        }
        parameters.layerName = layerName;

        // apply style to layer
        String styleName = requestProperties.getProperty(STYLE);
        if (styleName == null) {
            wmsLayer.setDefaultStyle();
        } else if (styleName.equals("")) {
            wmsLayer.setDefaultStyle();
        } else if (wmsLayer.isStyleSupported(styleName)) {
            wmsLayer.setStyle(styleName);
        } else {
            throw new WMSException("Unknown style specified (" + styleName + ").",
                    WMSException.STYLENOTDEFINED);
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

            if (!parameters.layerNames.contains(layerName)) {
                throw new WMSException("Layers missing Query Layer " + layerName + ".",
                        WMSException.LAYERNOTDEFINED);
            }

            IWmsLayer layer = (IWmsLayer) wmsLayerByName.get(layerName);
            if (layer == null) {
                throw new WMSException("Could not find layer " + layerName);
            }

            if (!layer.isQueryable()) {
                throw new WMSException("Layer " + layerName + " is not queryable");
            }

            parameters.queryLayerNames.add(layerName);
        }

    }

    /**
     * Create and return a Projection object based on the wms request
     * parameters.
     * 
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

        GeoProj projection = parameters.crs.createProjection(projProps);
        parameters.crs.prepareProjection(projection);
        projection.setScale(projection.getMinScale());

        LatLonPoint llp1 = parameters.bboxLatLonMinXY;
        LatLonPoint llp2 = parameters.bboxLatLonMaxXY;
        System.out.println("bbox toLatLon: 1: " + llp1 + ", 2: " + llp2 + ", center: "
                + parameters.bboxLatLonCenter);

        // guess a center value
        // TODO: calculate this from bbox values instead of after latlon
        // converting?
        // float centerLat = ((llp2.getLatitude() - llp1.getLatitude()) / 2) +
        // llp1.getLatitude();
        // float centerLon = ((llp2.getLongitude() - llp1.getLongitude()) / 2) +
        // llp1.getLongitude();
        // projection.setCenter(centerLat, centerLon);
        projection.setCenter(parameters.bboxLatLonCenter);

        // Debug.output("L1: " + llp1.toString()+", L2: " + llp2.toString();
        // TODO: need to set projection.center before using the projection

        int intnewwidth = parameters.width;
        int intnewheight = parameters.height;

        float newscale = projection.getScale(llp1, llp2, new Point(0, 0), new Point(intnewwidth,
                intnewheight));
        projection.setScale(newscale);

        // OGC 01-068r3 (wms 1.1.1) 7.2.3.8. "In the case where the aspect ratio
        // of the BBOX and the ratio width/height are different, the WMS shall
        // stretch the returned map so that the resulting
        // pixels could themselves be rendered in the aspect ratio of the BBOX"
        Point2D xyp1 = projection.forward(llp1);
        Point2D xyp2 = projection.forward(llp2);
        int w = (int) (xyp2.getX() - xyp1.getX());
        int h = (int) (xyp1.getY() - xyp2.getY());
        if (Math.abs(w - parameters.width) > 2 || Math.abs(h - parameters.height) > 2) {
            Debug.output("use aspect ratio fix");
            projection.setWidth(w);
            projection.setHeight(h);
            projection.setCenter(parameters.bboxLatLonCenter);
            float underlyingScale = projection.getScale(llp1, llp2, new Point(0, 0),
                    new Point(w, h));
            projection.setScale(underlyingScale);
            AspectRatioProjection p = new AspectRatioProjection(projection, parameters.width,
                    parameters.height);
            projection = p;
        }

        return projection;
    }

    /**
     * @param requestProperties
     * @param parameters
     * @throws WMSException
     */
    private void checkFormat(Properties requestProperties, FormatRequestParameter parameters)
            throws WMSException {
        String format = requestProperties.getProperty(FORMAT);

        // hack to handle WMS clients like ArcGIS 9.2 that are issuing
        // GetFeatureInfo without FORMAT parameter
        if ((format == null) && (parameters instanceof GetFeatureInfoRequestParameters)) {
            parameters.setFormatter(getFormatters().values().iterator().next());
            format = parameters.getFormatter().getContentType();
        }

        // tu zbadaæ podany format i ewentualnie rzuciæ WMSException
        if (format == null) {
            throw new WMSException("Missing FORMAT parameter.", WMSException.INVALIDFORMAT);
        }

        parameters.setFormatter(imageFormatterByContentType.get(format));
        if (parameters.getFormatter() == null) {
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

        Collection<String> okFormats = Arrays.asList(new String[] { HttpConnection.CONTENT_PLAIN,
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

}
