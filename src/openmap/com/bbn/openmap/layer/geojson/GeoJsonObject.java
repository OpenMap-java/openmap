package com.bbn.openmap.layer.geojson;

import java.util.HashMap;
import java.util.Map;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(property = "type", use = Id.NAME)
@JsonSubTypes({ @Type(Feature.class), @Type(Polygon.class), @Type(MultiPolygon.class),
        @Type(FeatureCollection.class), @Type(Point.class), @Type(MultiPoint.class),
        @Type(MultiLineString.class), @Type(LineString.class) })
@JsonInclude(Include.NON_NULL)
public abstract class GeoJsonObject {

    private Crs crs;
    private double[] bbox;
    @JsonInclude(Include.NON_EMPTY)
    private Map<String, Object> properties = new HashMap<String, Object>();
    @JsonInclude(Include.NON_EMPTY)
    private Map<String, Object> metadata = new HashMap<String, Object>();

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs crs) {
        this.crs = crs;
    }

    public double[] getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = bbox;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public abstract OMGraphic convert();

}
