/* 
 * <copyright>
 *  Copyright 2013 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.dataAccess.mapTile;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Properties;

import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * The GoogleMapTileFactory is a simple example of how to customize a
 * MapTileFactory to contact other services. This MTF is set up to contact the
 * Google static map server through the static maps API. If you don't provide
 * your API key you will be shut off. See the documentation at
 * https://developers.google.com/maps/documentation/staticmaps/, and read the
 * usage agreement.
 * 
 * Set the attribution property on the MapTileLayer, so the copyright shows up
 * when the data is displayed.
 * 
 * The additional properties of this class, which are added to the layer properties:
 * <pre>
 * 
 * key=google_api_key
 * mapType=roadmap, terrain, satellite or hybrid
 * 
 * </pre>
 * @author dietrick
 */
public class GoogleMapTileFactory extends ServerMapTileFactory {

    /**
     * Set the api key to use for tracking/billing in the properties.
     */
    public final static String API_KEY_PROPERTY = "key";
    /**
     * Set the map type displayed in the properties.
     */
    public final static String MAPTYPE_PROPERTY = "mapType";

    String apiKey = null;
    /* MapType options for Google are: roadmap, satellite, hybrid and terrain */
    String mapType = null;

    /**
     * The imageEdgeBuffer is used to fetch a slightly bigger image (N-S) so we
     * can clip it later.
     */
    int imageEdgeBuffer = 30;

    public GoogleMapTileFactory() {
        this.rootDir = "http://maps.googleapis.com/maps/api/staticmap?";
    }

    public String buildFilePath(int x, int y, int z, String fileExt) {
        StringBuilder ret = new StringBuilder(rootDir);
        int dim = 256 + imageEdgeBuffer * 2;
        LatLonPoint uLcoord = mtcTransform.tileUVToLatLon(new Point2D.Double(x, y), z, new LatLonPoint.Double());
        Mercator m = new Mercator(uLcoord, mtcTransform.getScaleForZoom(z), 256, 256);
        LatLonPoint nCenter = m.inverse(256, 256 + imageEdgeBuffer);

        ret.append("zoom=").append(z);
        ret.append("&center=").append(nCenter.getLatitude()).append(",").append(nCenter.getLongitude());
        ret.append("&size=").append(256).append("x").append(dim);
        if (mapType != null) {
            ret.append("&maptype=").append(mapType);
        }
        ret.append("&sensor=false");

        if (apiKey != null) {
            ret.append("&key=").append(apiKey);
        }

        return ret.toString();
    }

    protected BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
            throws InterruptedException {

        return BufferedImageHelper.getBufferedImage(origImage, 0, 0, 256, 256, BufferedImage.TYPE_INT_ARGB);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        apiKey = props.getProperty(prefix + API_KEY_PROPERTY, apiKey);
        mapType = props.getProperty(prefix + MAPTYPE_PROPERTY, mapType);
    }

    public Properties getProperties(Properties props) {
        props = getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + API_KEY_PROPERTY, PropUtils.unnull(apiKey));
        props.put(prefix + MAPTYPE_PROPERTY, PropUtils.unnull(mapType));

        return props;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return the mapType
     */
    public String getMapType() {
        return mapType;
    }

    /**
     * @param mapType the mapType to set
     */
    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

}
