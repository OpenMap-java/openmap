/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.vpf;

import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;

/**
 * An object the VPFFeatureLayer can use to manage how attribute information is
 * displayed for OMGraphics representing the feature.
 * 
 * @author dietrick
 */
public interface VPFFeatureInfoHandler {

   void updateInfoForOMGraphic(OMGraphic omg, FeatureClassInfo fci, List<Object> fcirow);

   /**
    * Check to see if the omg should react to mouse movement events.
    * 
    * @param omg
    * @return true for tooltips, infoline, highlight actions.
    */
   boolean isHighlightable(OMGraphic omg);

   /**
    * Check to see if the OMGraphic should be specially painted in a mouse over.
    * 
    * @param omg
    * @return true if omg should be repainted with its select color.
    */
   boolean shouldPaintHighlight(OMGraphic omg);

}
