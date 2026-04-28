/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.vpf;

import java.util.List;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;

/**
 * A feature info handler that displays attribute information as an html
 * formatted tooltip.
 * 
 * @author dietrick
 */
public class TTFeatureInfoHandler
      implements VPFFeatureInfoHandler {

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.bbn.openmap.layer.vpf.VPFFeatureInfoHandler#updateInfoForOMGraphic
    * (com.bbn.openmap.omGraphics.OMGraphic,
    * com.bbn.openmap.layer.vpf.FeatureClassInfo, java.util.List)
    */
   public void updateInfoForOMGraphic(OMGraphic omg, FeatureClassInfo fci, List<Object> fcirow) {


      DcwColumnInfo[] colInfo = fci.getColumnInfo();
      int columnCount = colInfo.length;
      StringBuffer sBuf = new StringBuffer("<html><body>");
      for (int i = 0; i < columnCount; i++) {
         sBuf.append("<b>" + colInfo[i].getColumnDescription() + ":</b> " + fcirow.get(i).toString() + "<br>");
      }
      sBuf.append("</body></html>");

      omg.putAttribute(OMGraphicConstants.TOOLTIP, sBuf.toString());
   }

   public boolean isHighlightable(OMGraphic omg) {
      return true;
   }

   public boolean shouldPaintHighlight(OMGraphic omg) {
      return false;
   }

}
