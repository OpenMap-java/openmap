package com.bbn.openmap.image.wms;

import com.bbn.openmap.image.ImageFormatter;

class GetLegendGraphicRequestParameters extends WmsRequestParameters implements FormatRequestParameter,
        WidthAndHeightRequestParameters {

    private int width;

    private int height;

    private ImageFormatter formatter;

    public String layerName;

    public ImageFormatter getFormatter() {
        return formatter;
    }

    public void setFormatter(ImageFormatter formatter) {
        this.formatter = formatter;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

}
