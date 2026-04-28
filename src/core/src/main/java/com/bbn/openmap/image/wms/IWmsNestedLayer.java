package com.bbn.openmap.image.wms;

/**
 * A variant of {@link IWmsLayer} that allow for nesting of wms layers.
 * <p>
 * Nested wms layers below a OpenMap {@link com.bbn.openmap.Layer} may be used
 * to split up information in a single OpenMap Layer for wms users.
 * <p>
 * Example: A OpenMap city layer may have nested wms layers for the different
 * type of cities. That way a wms user can select only the large cities.
 * <p>
 * There is always one top level {@link IWmsLayer} that also is a OpenMap
 * {@link com.bbn.openmap.Layer} - see {@link #getTopLayer()}. There may be some
 * nested layers "below" this layer - see {@link #getNestedLayers()}
 */
public interface IWmsNestedLayer extends IWmsLayer {

    /**
     * Return an array of {@link IWmsLayer} for each of the nested layers.
     * Nested layers may also be {@link IWmsNestedLayer} to support multiple
     * nesting levels.
     * <p>
     * Null or an empty array is returned if no nested layers exist.
     * 
     * @return an array of {@link IWmsLayer} with info about nested layers
     */
    public IWmsNestedLayer[] getNestedLayers();

    /**
     * Return the top layer, that is the IWmsLayer that also is an OpenMap
     * {@link com.bbn.openmap.Layer}. For such a top level layer, this method
     * should return it self.
     */
    public IWmsNestedLayer getTopLayer();

    /**
     * Called by the outside to turn this nested layer on or off.
     * <p>
     * If this method is not called, then this nested layer is off.
     */
    public void setIsActive(boolean active);

}
