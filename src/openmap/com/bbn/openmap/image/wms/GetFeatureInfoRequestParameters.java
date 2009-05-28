package com.bbn.openmap.image.wms;

import java.util.ArrayList;
import java.util.List;

class GetFeatureInfoRequestParameters extends GetMapRequestParameters {

    public int x;

    public int y;

    public final List<String> queryLayerNames = new ArrayList<String>();

    public String infoFormat;

}