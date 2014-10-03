/* **********************************************************************
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/wms/WMSPlugIn.java,v $
 * $Revision: 1.9 $
 * $Date: 2008/09/19 18:13:34 $
 * $Author: dietrick $
 *
 * Code provided by Raj Singh, raj@rajsingh.org
 * Updates provided by Holger Kohler, Holger.Kohler@dsto.defence.gov.au
 * Raj Singh updates in July 2002 to:
 *   - support WMS versions 1.0.8, 1.1.0 and 1.1.1
 *   - make JPEG image quality setting adjustable
 * *********************************************************************
 */

package com.bbn.openmap.plugin.wms;

import java.awt.geom.Point2D;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import com.bbn.openmap.image.ImageServerConstants;
import com.bbn.openmap.image.WMTConstants;
import com.bbn.openmap.plugin.WebImagePlugIn;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * This class asks for an image from an OpenGIS compliant Web Map Server (WMS).
 * Make sure that OpenMap is using the LLXY projection, because this plugin is
 * only asking for images that are in the Spatial Reference System EPS 4326
 * projection, and anything else won't match up. This class will be growing to
 * be more interactive with the WMS.
 * 
 * It has some properties that you can set in the openmap.properties file:
 * 
 * <pre>
 * 
 *     #For the plugin layer, add wms_plugin to openmap.layers list
 *     wms_plugin=com.bbn.openmap.plugin.wms.WMSPlugIn
 *     wms_plugin.wmsserver=A URL for the WMS server (eg. http://host.domain.name/servlet/com.esri.wms.Esrimap)
 *     wms_plugin.wmsversion=OpenGIS WMS version number (eg. 1.1.1)
 *     wms_plugin.format=image format (eg. image/jpeg, image/png)
 *     wms_plugin.transparent=true or false, depends on imageformat
 *     wms_plugin.backgroundcolor=RGB hex string (RRGGBB)
 *     wms_plugin.layers=comma separated list of map layer names (eg. SDE.SASAUS_BND_COASTL,SDE.SASAUS_BND_POLBNDL)
 *     wms_plugin.styles=comma separated list of layer rendering styles corresponding to the layers listed
 *     wms_plugin.vendorspecificnames=comma separated list of vendor specific parameter names in order (eg. SERVICENAME)
 *     wms_plugin.vendorspecificvalues=comma separated list of vendor specific parameter values in order (eg. default)
 * 
 * </pre>
 * 
 * <p>
 * One of the best demo WMS servers can be found at:
 * http://demo.cubewerx.com/demo/cubeserv/cubeserv.cgi
 */
public class WMSPlugIn extends WebImagePlugIn implements ImageServerConstants {

    /** URL to the server script that responds to WMS map requests */
    protected String wmsServer = null;
    /** GIF, PNG, JPEG, etc. (anything the server supports) */
    protected String imageFormat = "image/png";
    /**
     * If using a lossy image format, such as jpeg, set this to high, medium or
     * low
     */
    protected String imageQuality = "MEDIUM";
    /** Specify the color for non-data areas of the image in r,g,b */
    protected String backgroundColor = "0x00FFFFFF";
    /** true=make the backgroundColor transparent */
    protected String transparent = "true";
    /** version of the Web map server spec the server supports */
    protected String wmsVersion = "1.1.1";
    /** Comma-separated list of layer names */
    protected String layers = null;
    /** Comma-separated list of style names */
    protected String styles = null;
    /** Comma-separated list of vendor specific parameter names */
    protected String vendorSpecificNames = null;
    /** Comma-separated list of vendor specific parameter values */
    protected String vendorSpecificValues = null;
    /** Same as wmsServer */
    protected String queryHeader = null;
    /** Keyword for map request. Changes to MAP for WMS version 1.0.0 */
    protected String mapRequestName = WMTConstants.GETMAP;
    /**
     * Keyword for error handling. Changes to INIMAGE for WMS version under
     * 1.1.0. Changes to application/vnd.ogc.se+inimage for versions greater
     * than 1.1.1
     */
    protected String errorHandling = "application/vnd.ogc.se_inimage";

    public final static String WMSNameProperty = "wmsname";
    public final static String WMSServerProperty = "wmsserver";
    public final static String ImageFormatProperty = "format";
    public final static String BackgroundColorProperty = "backgroundcolor";
    public final static String TransparentProperty = "transparent";
    public static final String WMSVersionProperty = "wmsversion";
    public static final String LayersProperty = "layers";
    public static final String StylesProperty = "styles";
    public static final String VendorSpecificNamesProperty = "vendorspecificnames";
    public static final String VendorSpecificValuesProperty = "vendorspecificvalues";
    /** integer identifier for high image quality */
    public static final int LOSSY_IMAGE_QUALITY_HIGH = 2;
    /** integer identifier for medium image quality */
    public static final int LOSSY_IMAGE_QUALITY_MEDIUM = 1;
    /** integer identifier for low image quality */
    public static final int LOSSY_IMAGE_QUALITY_LOW = 0;

    public WMSPlugIn() {
    }

    /**
     * Add new layers to the server request, using the default style.
     */
    public void addLayers(String[] ls) {
        addLayers(ls, null);
    }

    /**
     * Add new layers to the server request, using specified styles.
     */
    public void addLayers(String[] ls, String[] st) {

        // DFD - do they have to be the same length? How about we just
        // use the styles we have for that number of layers, and let
        // the defaults take over for the rest.

        // if (ls.length != st.length) {
        // return null;
        // }

        for (int j = 0; j < ls.length; j++) {
            layers += "," + ls[j];

            // Put some other checks in here instead of the length
            // check above.
            if (st == null || j >= st.length || st[j] == null) {
                styles += ",";
            } else {
                styles += "," + st[j];
            }
        }
    }

    /**
     * Create the query to be sent to the server, based on current settings.
     */
    public String createQueryString(Projection p) {

        if (queryHeader == null) {
            return null;
        }

        String bbox = "undefined";
        String height = "undefined";
        String width = "undefined";

        if (p != null) {
            Point2D ul = p.getUpperLeft();
            Point2D lr = p.getLowerRight();
            bbox = Double.toString(ul.getX()) + "," + Double.toString(lr.getY()) + ","
                    + Double.toString(lr.getX()) + "," + Double.toString(ul.getY());
            height = Integer.toString(p.getHeight());
            width = Integer.toString(p.getWidth());
        }

        StringBuffer buf = new StringBuffer(queryHeader);
        buf.append("?").append(WMTConstants.VERSION).append("=").append(wmsVersion).append("&").append(WMTConstants.REQUEST).append("=").append(mapRequestName).append("&").append(WMTConstants.SRS).append("=").append("EPSG:4326").append("&").append(WMTConstants.BBOX).append("=").append(bbox).append("&").append(WMTConstants.HEIGHT).append("=").append(height).append("&").append(WMTConstants.WIDTH).append("=").append(width).append("&").append(WMTConstants.EXCEPTIONS).append("=").append(errorHandling);

        if (imageFormat != null) {
            buf.append("&").append(WMTConstants.FORMAT).append("=").append(imageFormat);

            String baseImageFormat = imageFormat;
            if (baseImageFormat.indexOf('/') > 0)
                baseImageFormat = baseImageFormat.substring(baseImageFormat.indexOf('/'));
            if (baseImageFormat.equals(WMTConstants.IMAGEFORMAT_JPEG)) {
                buf.append("&quality=").append(imageQuality);
            }
        }

        if (transparent != null) {
            buf.append("&").append(WMTConstants.TRANSPARENT).append("=").append(transparent);
        }

        if (backgroundColor != null) {
            buf.append("&").append(WMTConstants.BGCOLOR).append("=").append(backgroundColor);
        }

        if (layers != null) {
            buf.append("&").append(WMTConstants.LAYERS).append("=").append(layers);
        }

        String cStyles = styles;
        if (cStyles == null) {
            cStyles = "";
        }

        // if (styles != null) {
        buf.append("&").append(WMTConstants.STYLES).append("=").append(cStyles);
        // }

        if (Debug.debugging("wms")) {
            Debug.output("query string: " + buf);
        }

        /*
         * Included to allow for one or more vendor specific parameters to be
         * specified such as ESRI's ArcIMS's "ServiceName" parameter.
         */
        if (vendorSpecificNames != null) {
            if (vendorSpecificValues != null) {
                StringTokenizer nameTokenizer = new StringTokenizer(vendorSpecificNames, ",");
                StringTokenizer valueTokenizer = new StringTokenizer(vendorSpecificValues, ",");
                String paramName = null;
                String paramValue = null;
                while (nameTokenizer.hasMoreTokens()) {
                    try {
                        paramName = nameTokenizer.nextToken();
                        paramValue = valueTokenizer.nextToken();
                        buf.append("&").append(paramName).append("=").append(paramValue);
                    } catch (NoSuchElementException e) {
                        if (Debug.debugging("wms")) {
                            Debug.output("WMSPlugIn.getRectangle(): " + "parameter \"" + paramName
                                    + "\" has no value");
                        }
                    }
                }
            }
        }
        return buf.toString();
    }

    /**
     * Method to set the properties in the PropertyConsumer. The prefix is a
     * string that should be prepended to each property key (in addition to a
     * separating '.') in order for the PropertyConsumer to uniquely identify
     * properties meant for it, in the midst of Properties meant for several
     * objects.
     * 
     * @param prefix a String used by the PropertyConsumer to prepend to each
     *        property value it wants to look up -
     *        setList.getProperty(prefix.propertyKey). If the prefix had already
     *        been set, then the prefix passed in should replace that previous
     *        value.
     * 
     * @param setList a Properties object that the PropertyConsumer can use to
     *        retrieve expected properties it can use for configuration.
     */
    public void setProperties(String prefix, Properties setList) {
        super.setProperties(prefix, setList);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        wmsServer = setList.getProperty(prefix + WMSServerProperty);
        if (wmsServer == null) {
            Debug.error("WMSPlugIn needs a WMS server.");
        }

        queryHeader = wmsServer;

        setImageFormat(setList.getProperty(prefix + ImageFormatProperty, getImageFormat()));

        setTransparent(setList.getProperty(prefix + TransparentProperty, getTransparent()));

        setBackgroundColor(setList.getProperty(prefix + BackgroundColorProperty, getBackgroundColor()));

        setWmsVersion(setList.getProperty(prefix + WMSVersionProperty, getWmsVersion()));

        layers = setList.getProperty(prefix + LayersProperty);
        styles = setList.getProperty(prefix + StylesProperty);

        /**
         * Include for vendor specific parameters
         */
        setVendorSpecificNames(setList.getProperty(prefix + VendorSpecificNamesProperty, getVendorSpecificNames()));
        setVendorSpecificValues(setList.getProperty(prefix + VendorSpecificValuesProperty, getVendorSpecificValues()));

    } // end setProperties

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + WMSServerProperty, PropUtils.unnull(wmsServer));
        props.put(prefix + ImageFormatProperty, PropUtils.unnull(imageFormat));
        props.put(prefix + TransparentProperty, PropUtils.unnull(transparent));
        props.put(prefix + BackgroundColorProperty, PropUtils.unnull(backgroundColor));
        props.put(prefix + WMSVersionProperty, PropUtils.unnull(wmsVersion));
        props.put(prefix + LayersProperty, PropUtils.unnull(layers));
        props.put(prefix + StylesProperty, PropUtils.unnull(styles));
        props.put(prefix + VendorSpecificNamesProperty, PropUtils.unnull(vendorSpecificNames));
        props.put(prefix + VendorSpecificValuesProperty, PropUtils.unnull(vendorSpecificValues));
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        props.put(initPropertiesProperty, WMSServerProperty + " " + WMSVersionProperty + " "
                + LayersProperty + " " + StylesProperty + " " + VendorSpecificNamesProperty + " "
                + VendorSpecificValuesProperty + " " + ImageFormatProperty + " "
                + TransparentProperty + " " + BackgroundColorProperty);

        props.put(WMSServerProperty, "URL to the server script that responds to WMS map requests");
        props.put(ImageFormatProperty, "Image format (GIF, PNG, JPEG)");
        props.put(TransparentProperty, "Flag to indicate that background of image should be tranparent");
        props.put(TransparentProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        props.put(BackgroundColorProperty, "The Background color for the image");
        props.put(BackgroundColorProperty + ScopedEditorProperty, "com.bbn.openmap.util.propertyEditor.ColorPropertyEditor");
        props.put(WMSVersionProperty, "The WMS specification version");
        props.put(LayersProperty, "A list of layers to use in the query");
        props.put(StylesProperty, "A list of layer styles to use in the query");
        props.put(VendorSpecificNamesProperty, "Vendor-specific capability names to use in the query");
        props.put(VendorSpecificValuesProperty, "Vendor-specific capability values for the names");
        return props;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String newImageFormat) {
        if (newImageFormat.indexOf('/') > 0) {
            imageFormat = newImageFormat;
        } else {
            // convert "PNG" to "image/png" to be compatible with old OpenMap
            // practice
            imageFormat = "image/" + newImageFormat.toLowerCase();
        }
    }

    public void setImageQuality(int newImageQuality) {
        if (newImageQuality == WMSPlugIn.LOSSY_IMAGE_QUALITY_HIGH)
            imageQuality = "HIGH";
        else if (newImageQuality == WMSPlugIn.LOSSY_IMAGE_QUALITY_MEDIUM)
            imageQuality = "MEDIUM";
        else if (newImageQuality == WMSPlugIn.LOSSY_IMAGE_QUALITY_LOW)
            imageQuality = "LOW";
    }

    public String getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(String imageQuality) {
        this.imageQuality = imageQuality;
    }

    public String getTransparent() {
        return transparent;
    }

    public void setTransparent(String transparent) {
        if (transparent != null) {
            transparent = Boolean.valueOf(transparent).toString().toUpperCase();
        }
        this.transparent = transparent;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        if (backgroundColor != null) {
            if (backgroundColor.length() > 6) {
                backgroundColor = backgroundColor.substring(backgroundColor.length() - 6);
            }

            if (!backgroundColor.startsWith("0x")) {
                backgroundColor = "0x" + backgroundColor;
            }
        }
        this.backgroundColor = backgroundColor;
    }

    public String getErrorHandling() {
        return errorHandling;
    }

    public void setErrorHandling(String errorHandling) {
        this.errorHandling = errorHandling;
    }

    public String getLayers() {
        return layers;
    }

    public void setLayers(String layers) {
        this.layers = layers;
    }

    public String getMapRequestName() {
        return mapRequestName;
    }

    public void setMapRequestName(String mapRequestName) {
        this.mapRequestName = mapRequestName;
    }

    public String getQueryHeader() {
        return queryHeader;
    }

    public void setQueryHeader(String queryHeader) {
        this.queryHeader = queryHeader;
    }

    public String getStyles() {
        return styles;
    }

    public void setStyles(String styles) {
        this.styles = styles;
    }

    public String getVendorSpecificNames() {
        return vendorSpecificNames;
    }

    public void setVendorSpecificNames(String vendorSpecificNames) {
        this.vendorSpecificNames = vendorSpecificNames;
    }

    public String getVendorSpecificValues() {
        return vendorSpecificValues;
    }

    public void setVendorSpecificValues(String vendorSpecificValues) {
        this.vendorSpecificValues = vendorSpecificValues;
    }

    public String getWmsServer() {
        return wmsServer;
    }

    public void setWmsServer(String wmsServer) {
        this.wmsServer = wmsServer;
    }

    // make this better!
    public String getServerName() {
        return wmsServer;
    }

    public String getWmsVersion() {
        return wmsVersion;
    }

    /**
     * Does more than just set the version, it also adjusts other parameters
     * based on version. Be careful calling this without knowing what it does
     * and how it affects other settings.
     * 
     * @param wmsVer
     */
    public void setWmsVersion(String wmsVer) {
        if (wmsVer == null || wmsVer.length() == 0) {
            wmsVer = "1.1.1";
            Debug.output("WMSPlugin: wmsVersion was null, now set to 1.1.1");
        }

        if (Debug.debugging("wms")) {
            Debug.output("WMSPlugIn: set up with header \"" + queryHeader + "\"");
        }

        java.util.StringTokenizer st = new java.util.StringTokenizer(wmsVer, ".");
        int majorVersion = Integer.parseInt(st.nextToken());
        int midVersion = Integer.parseInt(st.nextToken());
        int minorVersion = Integer.parseInt(st.nextToken());

        // set the REQUEST parameter
        if (majorVersion == 1 && midVersion == 0 && minorVersion < 3) {
            mapRequestName = WMTConstants.MAP;
        }

        // set the image type parameter
        if (majorVersion == 1 && minorVersion > 7 && !imageFormat.startsWith("image/")) {
            imageFormat = "image/" + imageFormat;
        }

        // set the error handling parameter
        if (majorVersion == 1 && midVersion == 0) {
            errorHandling = "INIMAGE";
        } else if (majorVersion == 1 && midVersion >= 1 && minorVersion > 1) {
            errorHandling = "application/vnd.ogc.se+inimage";
        } else if (majorVersion > 1) {
            errorHandling = "application/vnd.ogc.se+inimage";
        }

        this.wmsVersion = wmsVer;
    }

} // end WMSPlugin
