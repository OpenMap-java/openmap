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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/dted/DTEDCoverageManager.java,v $
// $RCSfile: DTEDCoverageManager.java,v $
// $Revision: 1.6 $
// $Date: 2005/12/09 21:09:05 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.dted;

/*  Java Core  */
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.wanderer.Wanderer;
import com.bbn.openmap.util.wanderer.WandererCallback;

/**
 * A DTEDCoverageManager knows how to look at DTED data and figure out what
 * coverage is available.
 */
public class DTEDCoverageManager
      extends OMGraphicList
      implements WandererCallback, PropertyConsumer {

   protected I18n i18n = Environment.getI18n();

   public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.dted.DTEDCoverageManager");

   protected String[] paths;

   /** The default line color for level 0. */
   public final static String defaultLevel0ColorString = "CE4F3F"; // redish
   /** The default line color for level 1. */
   public final static String defaultLevel1ColorString = "339159"; // greenish
   /** The default line color for level 2. */
   public final static String defaultLevel2ColorString = "0C75D3"; // bluish

   /** The array of coverage for level 0 data. */
   protected boolean[][] level0Frames = null;
   /** The array of coverage for level 1 data. */
   protected boolean[][] level1Frames = null;
   /** The array of coverage for level 2 data. */
   protected boolean[][] level2Frames = null;

   protected DrawingAttributes level0Attributes = DrawingAttributes.getDefaultClone();
   protected DrawingAttributes level1Attributes = DrawingAttributes.getDefaultClone();
   protected DrawingAttributes level2Attributes = DrawingAttributes.getDefaultClone();

   protected OMGraphicList level0Rects = new OMGraphicList();
   protected OMGraphicList level1Rects = new OMGraphicList();
   protected OMGraphicList level2Rects = new OMGraphicList();

   public DTEDCoverageManager(String[] paths) {
      this.paths = paths;
      level0Attributes.setLinePaint(PropUtils.parseColor(defaultLevel0ColorString));
      level1Attributes.setLinePaint(PropUtils.parseColor(defaultLevel1ColorString));
      level2Attributes.setLinePaint(PropUtils.parseColor(defaultLevel2ColorString));

   }

   public void reset() {
      level0Frames = null;
      level1Frames = null;
      level2Frames = null;
      clear();
   }

   /**
    * The method that cycles through all the paths, looking for the frames. This
    * takes time, so it's only done when a coverage file can't be found.
    * 
    * @param paths paths to the level 0, 1 and 2 dted root directory.
    */
   public void checkOutCoverage(String[] paths) {

      level0Frames = new boolean[180][360];
      level1Frames = new boolean[180][360];
      level2Frames = new boolean[180][360];

      int maxNumPaths = 0;

      if (paths != null)
         maxNumPaths = paths.length;

      if (paths == null || maxNumPaths == 0) {
         logger.warning("No paths for DTED data given.");
         return;
      }

      logger.fine("checking out DTED at paths:");
      for (int d1 = 0; d1 < paths.length; d1++) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("       " + paths[d1]);
         }
         if (!BinaryFile.exists(paths[d1])) {
            paths[d1] = null;
            logger.fine("       - path invalid, ignoring.");
         }
      }

      Wanderer wanderer = new Wanderer(this);

      for (int pathNum = 0; pathNum < maxNumPaths; pathNum++) {
         wanderer.handleEntry(new File(paths[pathNum]));
      }
   }

   protected int curLon = Integer.MAX_VALUE;

   public boolean handleDirectory(File directory) {
      String name = directory.getName().toLowerCase();
      char hemi = name.charAt(0);
      if (name.length() == 4 && (hemi == 'e' || hemi == 'w')) {
         try {
            // Get the longitude index right, use hemi to set the +/-, and
            // then add 180 to get indexy.
            curLon = (hemi == 'w' ? -1 : 1) * Integer.parseInt(name.substring(1)) + 180;

         } catch (NumberFormatException nfe) {
            curLon = Integer.MAX_VALUE;
            logger.warning("Can't process " + name);
         }
      }
      return true;
   }

   public boolean handleFile(File file) {
      if (curLon != Integer.MAX_VALUE) {
         String name = file.getName().toLowerCase();
         char hemi = name.charAt(0);
         char level = name.charAt(name.length() - 1);
         if (name.length() == 7 && name.charAt(name.length() - 4) == '.' && (hemi == 'n' || hemi == 's')) {

            try {
               int curLat = (hemi == 's' ? -1 : 1) * Integer.parseInt(name.substring(1, name.length() - 4)) + 90;

               if (level == '0') {
                  level0Frames[curLat][curLon] = true;
               } else if (level == '1') {
                  level1Frames[curLat][curLon] = true;
               } else if (level == '2') {
                  level2Frames[curLat][curLon] = true;
               }

            } catch (NumberFormatException nfe) {
               logger.warning("Can't process " + name);
            }

         }

      }

      return true;
   }

   /**
    * Method organizes the query based on the projection, and returns the
    * applicable rectangles representing the frame coverage. If the coverage
    * spans over the date line, then two queries are performed, one for each
    * side of the date line.
    * 
    * @param proj the projection of the screen
    * @return an array of lists, one for each level of dted data.
    */
   public OMGraphicList getCoverageRects(Projection proj) {
      if (level0Frames == null) {

         logger.fine("Scanning for frames - This could take several minutes!");
         checkOutCoverage(paths);
      }

      if (isEmpty()) {
         getCoverageRects(-180, -90, 179, 89, OMGraphic.LINETYPE_RHUMB, proj);
      } else {
         generate(proj);
      }
      return this;
   }

   /**
    * Get a percentage value of how much of the map is covered for a projection.
    * 
    * @param proj
    * @return float[] with percentages, float[0] is level 0 coverage, 1 is level
    *         1, 2 is level 2.
    */
   public float[] getCoverage(Projection proj) {
      float[] ret = new float[3];
      if (level0Frames != null) {
         Point pnt1 = new Point();
         Point pnt2 = new Point();
         int height = proj.getHeight();
         int width = proj.getWidth();
         // Number frames possible on map
         int total = 0;
         for (int x = -180; x < 180; x++) {
            for (int y = -90; y < 89; y++) {
               proj.forward((float) y, (float) x, pnt1);
               proj.forward((float) (y + 1), (float) (x + 1), pnt2);

               double x1 = pnt1.getX();
               double y1 = pnt1.getY();
               double x2 = pnt2.getX();
               double y2 = pnt2.getY();

               boolean someX = (x1 >= 0 && x1 <= width) || (x2 >= 0 && x2 <= width);
               boolean someY = (y1 >= 0 && y1 <= height) || (y2 >= 0 && y2 <= height);

               boolean onMap = someX && someY;

               if (onMap) {
                  int xIndex = x + 180;
                  int yIndex = y + 90;
                  total++;
                  if (level0Frames[yIndex][xIndex])
                     ret[0] += 1f;
                  if (level1Frames[yIndex][xIndex])
                     ret[1] += 1f;
                  if (level2Frames[yIndex][xIndex])
                     ret[2] += 1f;
               }
            }
         }

         logger.info("Total frames: " + total + " " + ret[0] + ", " + ret[1] + ", " + ret[2]);

         ret[0] = ret[0] / total * 100f;
         ret[1] = ret[1] / total * 100f;
         ret[2] = ret[2] / total * 100f;
      }
      return ret;
   }

   /**
    * Method looks at the coverage arrays, and returns the applicable rectangles
    * representing the frame coverages.
    * 
    * @param startx the western-most longitude.
    * @param starty the southern-most latitude.
    * @param endx the eastern-most longitude.
    * @param endy the northern-most latitude.
    * @param LineType the type of line to use on the rectangles - Cylindrical
    *        projections can use straight lines, but other projections should
    *        use Rhumb lines.
    * @return an array of lists, one for each level of dted data.
    */
   public OMGraphicList getCoverageRects(int startx, int starty, int endx, int endy, int LineType, Projection proj) {
      clear();
      OMRect rect;
      level0Rects.clear();
      level1Rects.clear();
      level2Rects.clear();
      level0Rects.setVague(true);
      level1Rects.setVague(true);
      level2Rects.setVague(true);

      for (int lat = starty; lat <= endy && lat < 90; lat++) {
         for (int lon = startx; lon <= endx && lon < 180; lon++) {
            if (level0Frames[lat + 90][lon + 180]) {
               rect = new OMRect((double) lat, (double) lon, (double) lat + 1, (double) lon + 1, LineType);
               level0Attributes.setTo(rect);
               rect.generate(proj);
               level0Rects.add(rect);
            }

            if (level1Frames[lat + 90][lon + 180]) {
               rect = new OMRect((double) lat + .1f, (double) lon + .1f, (double) lat + .9f, (double) lon + .9f, LineType);
               level1Attributes.setTo(rect);
               rect.generate(proj);
               level1Rects.add(rect);
            }

            if (level2Frames[lat + 90][lon + 180]) {
               rect = new OMRect((double) lat + .2f, (double) lon + .2f, (double) lat + .8f, (double) lon + .8f, LineType);
               level2Attributes.setTo(rect);
               rect.generate(proj);
               level2Rects.add(rect);
            }
         }
      }

      add(level0Rects);
      add(level1Rects);
      add(level2Rects);

      return this;
   }

   protected String prefix;

   public Properties getProperties(Properties getList) {
      if (getList == null) {
         getList = new Properties();
      }

      level0Attributes.getProperties(getList);
      level1Attributes.getProperties(getList);
      level2Attributes.getProperties(getList);

      return getList;
   }

   public Properties getPropertyInfo(Properties list) {
      return list;
   }

   public String getPropertyPrefix() {
      return prefix;
   }

   public void setProperties(Properties setList) {
      setProperties(null, setList);
   }

   public void setProperties(String prefix, Properties setList) {
      setPropertyPrefix(prefix);

      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      level0Attributes.setProperties(prefix + "0", setList);
      level1Attributes.setProperties(prefix + "1", setList);
      level2Attributes.setProperties(prefix + "2", setList);
   }

   public void setPropertyPrefix(String prefix) {
      this.prefix = prefix;
   }

   protected JPanel panel;

   public Component getGUI() {
      if (panel == null) {

         String interString = i18n.get(DTEDCoverageManager.class, "title", "DTED Coverage");

         panel = PaletteHelper.createVerticalPanel(interString);
         JPanel pane = new JPanel();
         interString = i18n.get(DTEDCoverageManager.class, "level0title", "Level 0: ");
         pane.add(new JLabel(interString));
         String showString = i18n.get(DTEDCoverageManager.class, "show", "Show");
         JCheckBox jcb = new JCheckBox(showString, level0Rects.isVisible());
         jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               level0Rects.setVisible(((JCheckBox) ae.getSource()).isSelected());
            }
         });
         pane.add(jcb);
         pane.add(level0Attributes.getGUI());
         panel.add(pane);
         pane = new JPanel();
         interString = i18n.get(DTEDCoverageManager.class, "level2title", "Level 1: ");
         pane.add(new JLabel(interString));
         jcb = new JCheckBox(showString, level1Rects.isVisible());
         jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               level1Rects.setVisible(((JCheckBox) ae.getSource()).isSelected());
            }
         });
         pane.add(jcb);
         pane.add(level1Attributes.getGUI());
         panel.add(pane);
         pane = new JPanel();
         interString = i18n.get(DTEDCoverageManager.class, "level2title", "Level 2: ");
         pane.add(new JLabel(interString));
         jcb = new JCheckBox(showString, level2Rects.isVisible());
         jcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               level2Rects.setVisible(((JCheckBox) ae.getSource()).isSelected());
            }
         });
         pane.add(jcb);
         pane.add(level2Attributes.getGUI());
         panel.add(pane);
      }

      return panel;
   }

}