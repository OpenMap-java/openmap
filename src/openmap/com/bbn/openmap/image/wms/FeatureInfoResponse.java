package com.bbn.openmap.image.wms;



public interface FeatureInfoResponse {
    
    public void add(LayerFeatureInfoResponse layerFeatureInfoResponse);
    
    public void output(String contentType, StringBuffer out);

}
