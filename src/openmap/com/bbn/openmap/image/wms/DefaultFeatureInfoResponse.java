package com.bbn.openmap.image.wms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.bbn.openmap.layer.util.http.HttpConnection;

public class DefaultFeatureInfoResponse implements FeatureInfoResponse {

    private List layerFeatureInfoResponses = new ArrayList();

    public void add(LayerFeatureInfoResponse layerFeatureInfoResponse) {
        layerFeatureInfoResponses.add(layerFeatureInfoResponse);
    }

    public void output(String contentType, StringBuffer out) {
        // TODO: user controllable header and footer

        boolean isHtml = contentType.equals(HttpConnection.CONTENT_HTML);
        if (isHtml) {
            out.append("<html><head>\n");
            out.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n");
            out.append("</head><body>\n");
        }

        for (Iterator it = layerFeatureInfoResponses.iterator(); it.hasNext();) {
            LayerFeatureInfoResponse layerResponse = (LayerFeatureInfoResponse) it.next();
            layerResponse.output(contentType, out);
        }

        if (isHtml) {
            out.append("</body></html>");
        }
    }

}
