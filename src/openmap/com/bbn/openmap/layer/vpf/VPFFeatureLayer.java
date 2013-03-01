// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/vpf/VPFLayer.java,v $
// $RCSfile: VPFLayer.java,v $
// $Revision: 1.21 $
// $Date: 2006/03/06 16:13:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.vpf;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.GeoProj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * The VPFFeaureLayer renders VPF data with features being rendered in the order
 * and style specified by the GeoSym specification. It uses the
 * VPFAutoFeatureWarehouse, which knows how to use GeoSym data files to manage
 * desired features. These data files can be modified to adjust which features
 * are displayed.
 * 
 * The properties for this layer are:
 * 
 * <pre>
 * vpfPath=path to vpf library directory, parent of DHT/LAT file.  Multiple paths can be specified, separated by ;
 * libraryName=name of library to use, since multiple libraries can be specified in a top level vpf directory.  Wildcards accepted.
 * 
 * # VFPAutoFeatureGraphicWarehouse options:
 * cgmDirectory=parent directory of cgm files used for symbology
 * faccLookupFile=path to csv file that ties FACC codes to symbol file names
 * # The priority file is the file to adjust to customize display...
 * priorityFile=path to csv file specifying which FACC codes, types should be rendered and in what order.
 * featureInfoHandler=class of object to handle attributes
 * </pre>
 * 
 * @author dietrick
 */
public class VPFFeatureLayer
      extends OMGraphicHandlerLayer
      implements ProjectionListener, ActionListener, Serializable {

   private static final long serialVersionUID = 1L;

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.vpf.VPFFeatureLayer");

   /** property extension used to set the VPF root directory */
   public static final String pathProperty = "vpfPath";
   /** Property for setting VPF cutoff scale */
   public static final String cutoffScaleProperty = "cutoffScale";
   /** Property for setting VPF library name to use */
   public static final String LibraryNameProperty = "libraryName";
   /** the object that knows all the nitty-gritty vpf stuff */
   protected transient LibrarySelectionTable lst;
   /** our own little graphics factory */
   protected transient VPFAutoFeatureGraphicWarehouse warehouse;

   /**
    * hang onto prefix used to initialize warehouse in setProperties()
    */
   protected String prefix;
   /** hang onto properties file used to initialize warehouse */
   protected Properties props;

   /** the path to the root VPF directory */
   protected String[] dataPaths = null;

   protected int cutoffScale = LibrarySelectionTable.DEFAULT_BROWSE_CUTOFF;
   /** the library name to focus on */
   protected String libraryName = null;

   /**
    * Construct a VPF layer.
    */
   public VPFFeatureLayer() {
      setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
      setMouseModeIDsForEvents(new String[] {
         "Gestures"
      });
      warehouse = new VPFAutoFeatureGraphicWarehouse();
   }

   /**
    * Construct a VPFLayer, and sets its name.
    * 
    * @param name the name of the layer.
    */
   public VPFFeatureLayer(String name) {
      this();
      setName(name);
   }

   /**
    * Another way to set the parameters of the VPFLayer.
    */
   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      setAddToBeanContext(true);

      String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);

      cutoffScale = PropUtils.intFromProperties(props, realPrefix + cutoffScaleProperty, cutoffScale);

      libraryName = props.getProperty(realPrefix + LibraryNameProperty, libraryName);

      String path[] = PropUtils.initPathsFromProperties(props, realPrefix + pathProperty);

      if (path != null && path.length != 0) {
         setPath(path);
      }

      // need to save these so we can call setProperties on the
      // warehouse,
      // which we probably can't construct yet
      this.prefix = prefix;
      this.props = props;

      if (warehouse != null) {
         warehouse.setProperties(prefix, props);
         warehouse.setUseLibraries(PropUtils.parseSpacedMarkers(libraryName));
      }
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);

      String realPrefix = PropUtils.getScopedPropertyPrefix(this);

      props.put(realPrefix + cutoffScaleProperty, Integer.toString(cutoffScale));

      StringBuffer paths = new StringBuffer();
      String[] ps = getPath();

      for (int i = 0; ps != null && i < ps.length; i++) {
         paths.append(ps[i]);
         if (i < ps.length - 1)
            paths.append(";");
      }

      props.put(realPrefix + pathProperty, paths.toString());

      // For the library in a vpf package
      props.put(realPrefix + LibraryNameProperty, PropUtils.unnull(libraryName));

      if (warehouse != null) {
         warehouse.getProperties(props);
      }

      return props;
   }

   /** Where we store our default properties once we've loaded them. */
   private Properties defaultProps;

   /**
    * Return our default properties for vpf land.
    */
   public Properties getDefaultProperties() {
      if (defaultProps == null) {
         try {
            InputStream in = VPFFeatureLayer.class.getResourceAsStream("defaultVPFlayers.properties");
            // use a temporary so other threads won't see an
            // empty properties file
            Properties tmp = new Properties();
            if (in != null) {
               tmp.load(in);
               in.close();
            } else {
               logger.warning("can't load default properties file");
               // just use an empty properties file
            }
            defaultProps = tmp;
         } catch (IOException io) {
            logger.warning("can't load default properties: " + io);
            defaultProps = new Properties();
         }
      }
      return defaultProps;
   }

   /**
    * Set the data path to a single place.
    */
   public void setPath(String newPath) {
      logger.fine("setting paths to " + newPath);
      setPath(new String[] {
         newPath
      });
   }

   /**
    * Set the data path to multiple places.
    */
   public void setPath(String[] newPaths) {
      dataPaths = newPaths;

      lst = null;
      initLST();
   }

   /**
    * Returns the list of paths we use to look for data.
    * 
    * @return the list of paths. Don't modify the array!
    */
   public String[] getPath() {
      return dataPaths;
   }

   /**
    * initialize the library selection table.
    */
   protected void initLST() {
      logger.fine("initializing Library Selection Table (LST)");

      try {
         if (lst == null) {
            if (dataPaths == null) {
               logger.info("VPFLayer|" + getName() + ": path not set");
            } else {
               logger.fine("VPFLayer.initLST(dataPaths)");
               lst = new LibrarySelectionTable(dataPaths);
               lst.setCutoffScale(cutoffScale);
            }
         }
      } catch (com.bbn.openmap.io.FormatException f) {
         throw new java.lang.IllegalArgumentException(f.getMessage());
         // } catch (NullPointerException npe) {
         // throw new
         // java.lang.IllegalArgumentException("VPFLayer|" +
         // getName() +
         // ": path name not valid");
      }
   }

   public VPFAutoFeatureGraphicWarehouse getWarehouse() {
      return warehouse;
   }

   /**
    * If the warehouse gets set as a result of this method being called, the
    * properties will beed to be reset on it.
    * 
    * @param sbf Search by features.
    */
   public void checkWarehouse(boolean sbf) {
      if (warehouse == null) {
         logger.fine("need to create warehouse");
         warehouse = new VPFAutoFeatureGraphicWarehouse();
      }
   }

   /**
    * Create the OMGraphicList to use on the map. OMGraphicHandler methods call
    * this.
    */
   public synchronized OMGraphicList prepare() {
      if (lst == null) {
         try {
            initLST();
         } catch (IllegalArgumentException iae) {
            logger.warning("VPFLayer.prepare: Illegal Argument Exception.\n\nPerhaps a file not found.  Check to make sure that the paths to the VPF data directories are the parents of \"lat\" or \"lat.\" files. \n\n"
                  + iae);
            return null;
         }

         if (lst == null) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("VPFLayer| " + getName() + " prepare(), Library Selection Table not set.");
            }

            return null;
         }
      }

      if (warehouse == null) {
         StringBuffer dpb = new StringBuffer();
         if (dataPaths != null) {
            for (int num = 0; num < dataPaths.length; num++) {
               if (num > 0) {
                  dpb.append(":");
               }
               dpb.append(dataPaths[num]);
            }
         }

         logger.warning("VPFLayer.getRectangle:  Data path probably wasn't set correctly (" + dpb.toString()
               + ").  The warehouse not initialized.");
         return null;
      }

      Projection p = getProjection();

      if (p == null || !(p instanceof GeoProj)) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("VPFLayer.getRectangle() called with a projection (" + p + ") set in the layer, which isn't being handled.");
         }
         return new OMGraphicList();
      }

      LatLonPoint ll1 = p.getUpperLeft();
      LatLonPoint ll2 = p.getLowerRight();

      // Check both dynamic args and palette values when
      // deciding what to draw.
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("calling draw with boundaries: " + ll1 + " " + ll2);
      }
      long start = System.currentTimeMillis();

      OMGraphicList omgList = new OMGraphicList();
      try {
         omgList = warehouse.getFeatures(lst, ll1, ll2, p, omgList);
      } catch (FormatException fe) {
         logger.warning("Caught FormatException reading features: " + fe.getMessage());
      }

      long stop = System.currentTimeMillis();

      if (logger.isLoggable(Level.FINE)) {
         logger.fine("read time: " + ((stop - start) / 1000d) + " seconds");
      }

      return omgList;
   }

   public String getToolTipTextFor(OMGraphic omg) {
      return (String) omg.getAttribute(OMGraphicConstants.TOOLTIP);
   }

   public String getInfoText(OMGraphic omg) {
      return (String) omg.getAttribute(OMGraphicConstants.INFOLINE);
   }

   public boolean isHighlightable(OMGraphic omg) {
      VPFFeatureInfoHandler vfih = warehouse.getFeatInfoHandler();
      if (vfih != null) {
         return vfih.isHighlightable(omg);
      }

      return false;
   }
   
   /**
    * Fleeting change of appearance for mouse movements over an OMGraphic.
    */
   public void highlight(OMGraphic omg) {
      VPFFeatureInfoHandler vfih = warehouse.getFeatInfoHandler();
      if (vfih != null && vfih.shouldPaintHighlight(omg)) {
         super.highlight(omg);
      }
   }

   /**
    * Notification to set OMGraphic to normal appearance.
    */
   public void unhighlight(OMGraphic omg) {
      VPFFeatureInfoHandler vfih = warehouse.getFeatInfoHandler();
      if (vfih != null && vfih.shouldPaintHighlight(omg)) {
         super.unhighlight(omg);
      }
   }

}