/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.policy;

import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;

/**
 * Dummy ProjectionChangePolicy, for those layers who prefer to ignore
 * projection changes completely.
 */
public class NullProjectionChangePolicy
      implements ProjectionChangePolicy {

   /**
    * The OMGraphicHandlerLayer using this policy. Don't let this be null.
    */
   protected OMGraphicHandlerLayer layer;
   
   public OMGraphicHandlerLayer getLayer() {
      return layer;
   }

   public void projectionChanged(ProjectionEvent pe) {
      Projection proj = layer.setProjection(pe);
   }

   public void setLayer(OMGraphicHandlerLayer layer) {
      this.layer = layer;
   }

   public void workerComplete(OMGraphicList aList) {
      if (layer != null) {
         layer.setList(aList);
      }
   }

}
