package com.bbn.openmap.dataAccess.geojson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;

public class FeatureCollection extends GeoJsonObject implements Iterable<Feature> {

    private List<Feature> features = new ArrayList<Feature>();

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public FeatureCollection add(Feature feature) {
        features.add(feature);
        return this;
    }

    public void addAll(Collection<Feature> features) {
        this.features.addAll(features);
    }

    public Iterator<Feature> iterator() {
        return features.iterator();
    }

    public OMGraphic convert() {
        OMGraphicList omgl = new OMGraphicList();
        for (Feature feature : this) {
            omgl.add(feature.convert());
        }
        omgl.getAttributes().putAll(getProperties());
        return omgl;
    }

    public void add(Geometry<?> geometry, OMGraphic omg) {
        Feature ret = new Feature();
        ret.setGeometry(geometry);
        ret.setProperties(OMGeoJSONUtil.convert(omg.getAttributes()));
        add(ret);
    }

    public void add(Point point, OMGraphic omg) {
        Feature ret = new Feature();
        ret.setGeometry(point);
        ret.setProperties(OMGeoJSONUtil.convert(omg.getAttributes()));
        add(ret);
    }
}
