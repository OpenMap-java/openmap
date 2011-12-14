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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/image/MapRequestHandler.java,v $
// $RCSfile: MapRequestHandler.java,v $
// $Revision: 1.14 $
// $Date: 2006/02/15 22:28:39 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.image;

import java.awt.Paint;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.Environment;
import com.bbn.openmap.Layer;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.PropertyStringFormatException;
import com.bbn.openmap.util.http.HttpConnection;
/**
 * The MapRequestHandler is the front end for String requests to the
 * ImageServer. It's goal is to be able to handle OpenGIS WMT mapserver
 * requests, so the String request format is in the same format. We've included
 * some OpenMap extensions to that format, so an OpenMap projection can be
 * defined.
 * <P>
 * 
 * The MapRequestHandler should be able to handle map requests, resulting in a
 * map image, and capabilities requests, so a client can find out what layers,
 * projection types and image formats are available.
 * <P>
 * 
 * If the 'layers' property is not defined the openmap.properties file, then the
 * 'openmap.layers' property will be used, and the 'openmap.startUpLayers'
 * property will be used to define the default set of layers. This lets there be
 * more layers available to the client than would be sent by default (if the
 * client doesn't specify layers).
 * <P>
 * 
 * The MapRequestHandler assumes that the ProjectionFactory shared instance has
 * ProjectionLoaders added to it to let it know how to handle different
 * projection names. A call to ProjectionFactory.loadDefaultProjections() will
 * take care of this requirement if you just want the standard projections
 * loaded.
 */
public class MapRequestHandler extends ImageServer implements
        ImageServerConstants {

    public final static String valueSeparator = ",";
    public final static String hexSeparator = "%";

    public final static String defaultLayersProperty = OpenMapPrefix
            + "startUpLayers";

    /**
     * The real new property for doing this. The old property,
     * defaultLayersProperty set to openmap.startUpLayers, will work if this is
     * not set. At some point, we should all start using this one, it's just
     * right. (defaultLayers)
     */
    public final static String DefaultLayersProperty = "defaultLayers";
    /**
     * The property for using visibility of the layers to mark the default
     * layers. The order that they are used depends on how they are specified in
     * the layers property.
     */
    public final static String UseVisibilityProperty = "useLayerVisibility";

    /**
     * Comma-separated list of layer scoping prefixes that specify what layers,
     * and their order, should be used for default images where layers are not
     * specified in the request.
     */
    protected String defaultLayers;

    /**
     * The default projection that provides projection parameters that are
     * missing from the request.
     */
    protected Projection defaultProjection;

    /**
     * The layers' visibility will be set to true at initialization if this
     * property is set, and the layers' visibility will determine if a layer
     * will be part of the image. If you set this flag, then you have to set the
     * layers' visibility yourself. This property takes precedence over the
     * default layers property if both are defined.
     */
    protected boolean useVisibility = false;

    public MapRequestHandler(Properties props) throws IOException {
        this(null, props);
    }

    public MapRequestHandler(String prefix, Properties props)
            throws IOException {
        setProperties(prefix, props);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        defaultProjection = initProjection(props);
        defaultLayers = props.getProperty(prefix + DefaultLayersProperty);

        if (defaultLayers == null) {
            defaultLayers = props.getProperty(defaultLayersProperty);
        }

        setUseVisibility(PropUtils.booleanFromProperties(props, prefix
                + UseVisibilityProperty, getUseVisibility()));
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        // Still have to do projection, and default layers.

        props.put(UseVisibilityProperty,
                "Flag to use layer visibility settings to determine default layers");

        return props;
    }

    /**
     * Set whether the layer visibility is used in order to determine default
     * layers for the image.
     */
    public void setUseVisibility(boolean value) {
        useVisibility = value;
    }

    public boolean getUseVisibility() {
        return useVisibility;
    }

    /**
     * Set up the default projection, which parts are used if any parts of a
     * projection are missing on an image request.
     * 
     * @param props the properties to look for openmap projection parameters.
     * @return a projection created from the properties. A mercator projection
     *         is created if no properties pertaining to a projection are found.
     */
    protected Projection initProjection(Properties props) {
        loadProjections(props);
        Projection proj = new ProjectionFactory().getDefaultProjectionFromEnvironment(Environment.getInstance());

        if (Debug.debugging("imageserver")) {
            Debug.output("MRH starting with default projection = " + proj);
        }
        return proj;
    }

    /**
     * Method called from initProjection to initialize the ProjectionFactory. By
     * default, the standard OpenMap projections are loaded into the
     * ProjectionFactory. If you want those projections replaced or add more
     * projections to the default set, override this method and do what you
     * need. *
     * 
     * @param props Properties that can be used to figure out what to do. This
     *        implementation of the method doesn't use the Properties.
     */
    protected void loadProjections(Properties props) {
        ProjectionFactory.loadDefaultProjections();
    }

    /**
     * Set the default projection to grab parameters from in case some
     * projection terms are missing from the request string.
     */
    public void setDefaultProjection(Projection proj) {
        defaultProjection = proj;
    }

    /**
     * Get the Projection being used for parameters in case some parameters are
     * missing from request strings.
     */
    public Projection getDefaultProjection() {
        return defaultProjection;
    }

    /**
     * Set the default layers that will be used for requests that don't specify
     * layers. The String should be a comma separated list of prefix scoping
     * strings for the layer (layer.getPropertyPrefix()).
     */
    public void setDefaultLayers(String dLayers) {
        defaultLayers = dLayers;
    }

    public String getDefaultLayers() {
        return defaultLayers;
    }

    /**
     * Get a list of all the layer identifiers that can be used in a request,
     * for the current configuration of the MapRequestHandler.
     */
    public String getAllLayerNames() {
        Layer[] layers = getLayers();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < layers.length; i++) {
            sb.append((i > 0 ? " " : "")).append(layers[i].getPropertyPrefix());
        }
        return sb.toString();
    }

    protected Properties convertRequestToProps(String request)
            throws MapRequestFormatException {
        try {
            // Convert any %XX to the real ASCII value.
            request = URLDecoder.decode(request, "UTF-8");

            Properties requestProperties = PropUtils.parsePropertyList(request);

            if (Debug.debugging("imageserver")) {
                Debug.output("MRH: parsed request " + requestProperties);
            }

            return requestProperties;
        } catch (PropertyStringFormatException psfe) {
            throw new MapRequestFormatException(psfe.getMessage());
        } catch (Exception e) {
            throw new MapRequestFormatException(e.getMessage());
        }
    }

    /**
     * Given a general request, parse it and handle it. This is the method that
     * servlets should call. Currently only handles image requests.
     * 
     * @param request the request string of key value pairs.
     * @return a byte[] for the image.
     */
    public byte[] handleRequest(String request) throws IOException,
            MapRequestFormatException {

        Properties requestProperties = convertRequestToProps(request);
        String requestType = requestProperties.getProperty(REQUEST);

        if (requestType != null) {
            if (requestType.equalsIgnoreCase(MAP)) {
                Debug.message("imageserver", "MRH: Map request...");
                return handleMapRequest(requestProperties);
                // } else if
                // (requestType.equalsIgnoreCase(CAPABILITIES)) {
                // Debug.message("imageserver", "MRH: Capabilities
                // request...");
                // handleCapabilitiesRequest(requestProperties, out);
            } else {
                throw new MapRequestFormatException("Request type not handled: "
                        + requestType);
            }
        } else {
            throw new MapRequestFormatException("Request not understood: "
                    + request);
        }
    }

    /**
     * Given a general request, parse it and handle it. Appends a content type
     * to the output stream which may mess things up for servlets asking for
     * images.
     * 
     * @param request the request string of key value pairs.
     * @param out OutputStream to reply on.
     */
    public void handleRequest(String request, OutputStream out)
            throws IOException, MapRequestFormatException {

        Properties requestProperties = convertRequestToProps(request);

        String requestType = requestProperties.getProperty(REQUEST);
        if (requestType != null) {
            if (requestType.equalsIgnoreCase(MAP)) {
                Debug.message("imageserver", "MRH: Map request...");
                handleMapRequest(requestProperties, out);
            } else if (requestType.equalsIgnoreCase(CAPABILITIES)) {
                Debug.message("imageserver", "MRH: Capabilities request...");
                handleCapabilitiesRequest(requestProperties, out);
            } else if (requestType.equalsIgnoreCase(PAN)) {
                Debug.message("imageserver", "MRH: Pan request...");
                handlePanRequest(requestProperties, out);
            } else if (requestType.equalsIgnoreCase(RECENTER)) {
                Debug.message("imageserver", "MRH: Recenter request...");
                handleRecenterRequest(requestProperties, out);

            } else {
                throw new MapRequestFormatException("Request type not handled: "
                        + requestType);
            }
        } else {
            throw new MapRequestFormatException("Request not understood: "
                    + request);
        }

    }

    /**
     * Handle a map request, and create and image for it.
     * 
     * @param requestProperties the request in properties format.
     * @return byte[] of formatted image.
     */
    public byte[] handleMapRequest(Properties requestProperties)
            throws IOException, MapRequestFormatException {

        Proj projection = ImageServerUtils.createOMProjection(requestProperties,
                defaultProjection);

        // Old way, bad, bad. Can't set the background for the whole image
        // server for one request, it wacks the default.

        // setBackground(ImageServerUtils.getBackground(requestProperties,
        // getBackground()));

        // New way, passed along to ImageServer methods.
        Paint bgPaint = ImageServerUtils.getBackground(requestProperties,
                getBackground());

        boolean formatFound = false;

        String format = requestProperties.getProperty(FORMAT);
        if (format != null) {
            formatFound = setFormatter(format.toUpperCase());
            formatFound = true;
            Debug.message("imageserver", "Format requested " + format);
        }

        if (Debug.debugging("imageserver")) {
            if (format == null || formatFound == false) {
                Debug.output("MRH: no formatter defined, using default");
            }
            
            Debug.output("Creating image with projection: " + projection);
        }

        byte[] image;

        // We need to think about using the layer mask, parsing it
        // intelligently, and not using it if it's a little freaky.

        // String strLayerMask =
        // requestProperties.getProperty(LAYERMASK);
        // // default is to show all the layers server knows about.
        // int layerMask = 0xFFFFFFFF;
        // if (strLayerMask != null) {
        // if (Debug.debugging("imageserver") {
        // Debug.output("MRH.handleMapRequest: LayerMask unsigned int
        // is " +
        // strLayerMask);
        // }
        // layerMask = Integer.parseInt(strLayerMask);
        // }

        String strLayers = requestProperties.getProperty(LAYERS);

        // Pass any properties to the layers??? Maybe if another
        // property is set, to bother with taking up the time to run
        // through all of this...

        if (strLayers != null) {

            Vector<String> layers = PropUtils.parseMarkers(strLayers, ",");
            if (Debug.debugging("imageserver")) {
                Debug.output("MRH.handleMapRequest: requested layers >> "
                        + layers);
            }
            image = createImage(projection, -1, -1, layers, bgPaint);
        } else {
            // if LAYERS property is not specified
            // Check default layers or if visibility should be used to
            // determine default

            if (getUseVisibility()) {
                if (Debug.debugging("imageserver")) {
                    Debug.output("MRH.handleMapRequest: Using visibility to determine layers");
                }
                image = createImage(projection,
                        -1,
                        -1,
                        calculateVisibleLayerMask(),
                        bgPaint);
            } else {
                Vector<String> layers = PropUtils.parseMarkers(defaultLayers, " ");
                if (Debug.debugging("imageserver")) {
                    Debug.output("MRH.handleMapRequest: requested layers >> "
                            + layers + " out of " + getAllLayerNames());
                }
                image = createImage(projection, -1, -1, layers, bgPaint);
            }
        }
        return image;
    }

    /**
     * Handle a Map Request. Appends a content type to the output stream which
     * may mess things up for servlets.
     */
    public void handleMapRequest(Properties requestProperties, OutputStream out)
            throws IOException, MapRequestFormatException {

        byte[] image = handleMapRequest(requestProperties);

        if (Debug.debugging("imageserver")) {
            Debug.output("MRH: have completed image, size " + image.length);
        }

        String contentType = getFormatterContentType(getFormatter());

        if (contentType == null) {
            contentType = HttpConnection.CONTENT_PLAIN;
        }

        Debug.message("imageserver", "MRH: have type = " + contentType);

        HttpConnection.writeHttpResponse(out, contentType, image);
    }

    /**
     * Handle a Pan Request.
     */
    public void handlePanRequest(Properties requestProperties, OutputStream out)
            throws IOException, MapRequestFormatException {

        Proj projection = ImageServerUtils.createOMProjection(requestProperties,
                defaultProjection);

        String contentType = HttpConnection.CONTENT_PLAIN;
        String response;
        float panAzmth;

        try {
            panAzmth = Float.parseFloat(requestProperties.getProperty(AZIMUTH));
            projection.pan(panAzmth);
        } catch (Exception exc) {
            Debug.output("MSH: Invalid Azimuth");
        }

        response = Math.round(projection.getCenter().getY() * 100.0) / 100.0
                + ":" + Math.round(projection.getCenter().getX() * 100.0)
                / 100.0;

        HttpConnection.writeHttpResponse(out, contentType, response);
    }

    /**
     * Handle a Recenter Request. Appends a content type to the output stream
     * which may mess things up for servlets.
     */
    public void handleRecenterRequest(Properties requestProperties,
                                      OutputStream out) throws IOException,
            MapRequestFormatException {

        Proj projection = ImageServerUtils.createOMProjection(requestProperties,
                defaultProjection);

        String contentType = HttpConnection.CONTENT_PLAIN;

        String response;

        try {
            int x = Integer.parseInt(requestProperties.getProperty(X));
            int y = Integer.parseInt(requestProperties.getProperty(Y));
            projection.setCenter(projection.inverse(x, y));
        } catch (Exception exc) {
            Debug.output("MSH: Invalid Azimuth");
        }

        response = Math.round(projection.getCenter().getY() * 100.0) / 100.0
                + ":" + Math.round(projection.getCenter().getX() * 100.0)
                / 100.0;

        HttpConnection.writeHttpResponse(out, contentType, response);
    }

    /**
     * Given an ImageFormatter, get the HttpConnection content type that matches
     * it.
     */
    public String getFormatterContentType(ImageFormatter formatter) {
        String ret = null;
        String label = formatter.getFormatLabel();

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
     * Handle a capabilities request.
     */
    public void handleCapabilitiesRequest(Properties requestProperties,
                                          OutputStream out) throws IOException,
            MapRequestFormatException {

        if (Debug.debugging("imageserver")) {
            Debug.output("MRH.handleCapabilitiesRequest: unimplemented");
        }

        throw new MapRequestFormatException("Capabilities request currently not handled");
    }
}