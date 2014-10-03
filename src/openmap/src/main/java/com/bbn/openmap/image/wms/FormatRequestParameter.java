package com.bbn.openmap.image.wms;

import com.bbn.openmap.image.ImageFormatter;

/**
 * A wms request parameter object that contain FORMAT element
 */
interface FormatRequestParameter {

    public void setFormatter(ImageFormatter formatter);

    public ImageFormatter getFormatter();

}
