package com.bbn.openmap.image.wms;

import java.util.ArrayList;
import java.util.List;

class GetFeatureInfoRequestParameters extends GetMapRequestParameters {

    public int x;

    public int y;

    public final List queryLayerNames = new ArrayList();

    public String infoFormat;

}