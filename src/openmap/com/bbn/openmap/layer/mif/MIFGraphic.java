package com.bbn.openmap.layer.mif;

/**
 * Interface that defines behavior specific to OMGraphics that are
 * specific to primitves created from MapInfo MIF files
 * 
 * @author Simon Bowen
 */
public interface MIFGraphic {
    /**
     * sets the scale at which the graphic becomes visible, if set to -1
     * the graphic is viaible at all scale levels.
     * 
     * @param scale
     */
    public void setVisibleScale(float visibleScale);

    public float getVisibleScale();

}
/** Last line of file **/
