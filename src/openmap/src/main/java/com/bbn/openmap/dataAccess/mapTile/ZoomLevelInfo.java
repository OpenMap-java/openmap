//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.dataAccess.mapTile;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.bbn.openmap.OMComponent;

/**
 * The ZoomLevelInfo class is used by the TileMaker and handles how tiles are
 * defined and created for a particular zoom level. It handles any bounds
 * restrictions, what layers should be rendered at this zoom level, and the path
 * to the tiles from the root directory. The properties for this component are:
 * <p>
 * 
 * <pre>
 * #Needed for property file creation of TileMaker
 * zoomMarker.class=com.bbn.openmap.image.ZoomLevelInfo
 * #Optional, to limit tile areas created, in sets of 4
 * zoomMarker.bounds=lat lon lat lon lat lon lat lon
 * zoomMarker.description=Tiles for zoom level 4
 * #Marker names for layers to be rendered, the property prefixes for the layers held by TileMaker
 * zoomMarker.layers=lakes shape
 * zoomMarker.name=ZoomLayerInfo 4
 * zoomMarker.zoomLevel=4
 * 
 * </pre>
 * 
 * Note that the zoomMarker keyword should be stored in the TileMaker zoomLevels
 * property list.
 * 
 * @author dietrick
 */
public class ZoomLevelInfo
      extends OMComponent {

   protected int zoomLevel = 0;
   protected float scale = -1f;

   /**
    * Default constructor.
    */
   public ZoomLevelInfo() {
   }

   /**
    * @return the current zoom level.
    */
   public int getZoomLevel() {
      return zoomLevel;
   }

   /**
    * @param zoomLevel the current zoom level.
    */
   public void setZoomLevel(int zoomLevel) {
      this.zoomLevel = zoomLevel;
   }

   /**
    * Return the current scale set in this object.
    * 
    * @return scale setting for zoom level
    */
   public float getScale() {
       return scale;
   }

   /**
    * Set the current scale to use for calculating the zoom level.
    * 
    * @param scale to set
    */
   public void setScale(float scale) {
       this.scale = scale;
   }
   
   /**
    * @return the number of tiles across or down for the current zoom level.
    */
   public int getEdgeTileCount() {
      return (int) Math.pow(2, zoomLevel);
   }

   /**
    * Given a bounds containing a lat/lon box (x, y, w, h in decimal degrees)
    * and a transform for the MapTileCoordinates being used for the tiles
    * (OpenStreetMap or Google (MTS)), provide the UV tile coordinates for the
    * the bounding box.
    * 
    * @param bounds decimal degree bounds
    * @param mtct MapTileCoordinateTransform (OSMMapTileCoordinateTransform or
    *        TMSMapTileCoordinateTransform).
    * @return UV tile coordinates for bounds.
    */
   public Rectangle2D getUVBounds(Rectangle2D bounds, MapTileCoordinateTransform mtct, int zoomLevel) {
      double x = bounds.getX();
      double y = bounds.getY();
      double h = bounds.getHeight();
      double w = bounds.getWidth();
      Point2D point1 = new Point2D.Double(x, y + h);
      Point2D point2 = new Point2D.Double(x + w, y);
      Point2D uluv = mtct.latLonToTileUV(point1, zoomLevel);
      Point2D lruv = mtct.latLonToTileUV(point2, zoomLevel);
      x = Math.floor(uluv.getX());
      y = Math.floor(uluv.getY());
      w = Math.ceil(lruv.getX() - x);
      h = Math.ceil(lruv.getY() - y);

      return new Rectangle2D.Double(x, y, w, h);
   }

   /**
    * Creates the parent directory of the file with the current zoom level set
    * in the ZoomLevelInfo.
    * 
    * @param rootDir path to root of dir structire, with no file separator at
    *        the end.
    * @param uvx uv x coordinate of map tile.
    * @param uvy uv y coordinate of map tile.
    * @return path of parent directory of the file (no y coordinate).
    */
   public String formatParentDirectoryName(String rootDir, int uvx, int uvy) {
      return rootDir + "/" + getZoomLevel() + "/" + uvx;
   }

   /**
    * Creates file path given a root directory plus current zoom level, x, and
    * y. Ready for file extension, which should have a period on it.
    * 
    * @param rootDir path to root of dir structure, with no file separator at
    *        the end.
    * @param uvx uv x coordinate of the map tile.
    * @param uvy uv y coordinate of the map tile
    * @return a file path, sans file extension.
    */
   public String formatImageFilePath(String rootDir, int uvx, int uvy) {
      return rootDir + "/" + getZoomLevel() + "/" + uvx + "/" + uvy;
   }
}