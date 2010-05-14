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

package com.bbn.openmap.event;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.image.ImageScaler;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * OMMouseMode is a combination of the PanMouseMode, NavMouseMode and
 * SelectMouseMode. Press and drag to pan. Double click to re-center, CTRL
 * double click to re-center and zoom. Shift-CTRL-Double click to center and
 * zoom out. Double click to select OMGraphics. Right click for pop-up menu.
 */
public class OMMouseMode
      extends CoordMouseMode {

   /**
	 * 
	 */
   private static final long serialVersionUID = 1L;

   public final static String OpaquenessProperty = "opaqueness";
   public final static String LeaveShadowProperty = "leaveShadow";
   public final static String UseCursorProperty = "useCursor";
   public final static float DEFAULT_OPAQUENESS = 1.0f;

   public final static transient String modeID = "Gestures";

   private boolean isPanning = false;
   private int oX, oY;
   private float opaqueness = DEFAULT_OPAQUENESS;
   private boolean leaveShadow = false;
   private boolean useCursor;

   /**
    * rPoint1 is the anchor point of a line segment
    */
   public Point2D rPoint1;
   /**
    * rPoint2 is the new (current) point of a line segment
    */
   public Point2D rPoint2;

   protected BufferedMapBean theMap = null;
   protected String coordString = null;

   public OMMouseMode() {
      super(modeID, true);
      setUseCursor(false);
      setLeaveShadow(true);
      setOpaqueness(DEFAULT_OPAQUENESS);
   }

   /**
    * @return Returns the useCursor.
    */
   public boolean isUseCursor() {
      return useCursor;
   }

   /**
    * @param useCursor The useCursor to set.
    */
   public void setUseCursor(boolean useCursor) {
      this.useCursor = useCursor;
      if (useCursor) {
         /*
          * For who like make his CustomCursor
          */
         try {
            Toolkit tk = Toolkit.getDefaultToolkit();
            ImageIcon pointer = new ImageIcon(getClass().getResource("Gestures.gif"));
            Dimension bestSize = tk.getBestCursorSize(pointer.getIconWidth(), pointer.getIconHeight());
            Image pointerImage =
                  ImageScaler.getOptimalScalingImage(pointer.getImage(), (int) bestSize.getWidth(), (int) bestSize.getHeight());
            Cursor cursor = tk.createCustomCursor(pointerImage, new Point(0, 0), "PP");
            setModeCursor(cursor);
            return;
         } catch (Exception e) {
            // Problem finding image probably, just move on.
         }
      }

      setModeCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      opaqueness = PropUtils.floatFromProperties(props, prefix + OpaquenessProperty, opaqueness);
      leaveShadow = PropUtils.booleanFromProperties(props, prefix + LeaveShadowProperty, leaveShadow);

      setUseCursor(PropUtils.booleanFromProperties(props, prefix + UseCursorProperty, isUseCursor()));
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      String prefix = PropUtils.getScopedPropertyPrefix(this);
      props.put(prefix + OpaquenessProperty, Float.toString(getOpaqueness()));
      props.put(prefix + LeaveShadowProperty, Boolean.toString(isLeaveShadow()));
      props.put(prefix + UseCursorProperty, Boolean.toString(isUseCursor()));
      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      props = super.getPropertyInfo(props);

      PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class, OpaquenessProperty, "Transparency",
                                    "Transparency level for moving map, between 0 (clear) and 1 (opaque).", null);
      PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class, LeaveShadowProperty, "Leave Shadow",
                                    "Display current map in background while panning.",
                                    "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
      PropUtils.setI18NPropertyInfo(i18n, props, OMMouseMode.class, UseCursorProperty, "Use Cursor",
                                    "Use hand cursor for mouse mode.", "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

      return props;
   }

   /**
    * Checks the MouseEvent to see if a BufferedMapBean can be found.
    * 
    * @param evt MouseEvent, or a MapMouseEvent
    * @return BufferedMapBean, or null if source is not a BufferedMapBean.
    */
   protected BufferedMapBean getBufferedMapBean(MouseEvent evt) {
      if (evt instanceof MapMouseEvent) {
         MapBean mb = ((MapMouseEvent) evt).getMap();
         if (mb instanceof BufferedMapBean) {
            return (BufferedMapBean) mb;
         }
      } else {
         Object src = evt.getSource();
         if (src instanceof BufferedMapBean) {
            return (BufferedMapBean) src;
         }
      }

      return null;
   }

   /**
    * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
    *      The first click for drag, the image is generated. This image is
    *      redrawing when the mouse is move, but, I need to repain the original
    *      image.
    */
   public void mouseDragged(MouseEvent arg0) {
      super.mouseDragged(arg0);

      if (mouseSupport.proxy == null) {

         BufferedMapBean mb = getBufferedMapBean(arg0);
         if (mb == null) {
            // OMMouseMode needs a BufferedMapBean
            return;
         }

         // Left mouse click, pan
         if (SwingUtilities.isLeftMouseButton(arg0)) {

            Point2D pnt = mb.getNonRotatedLocation(arg0);
            int x = (int) pnt.getX();
            int y = (int) pnt.getY();

            if (!isPanning) {

               oX = x;
               oY = y;

               isPanning = true;

            } else {

               mb.setPanningTransform(AffineTransform.getTranslateInstance(x - oX, y - oY));
               mb.repaint();
            }
         }
      }
   }

   /**
    * Process a mouse pressed event. Add the mouse location to the segment
    * vector. Calculate the cumulative total distance.
    * 
    * @param e mouse event.
    */
   public void mousePressed(MouseEvent e) {
      mouseSupport.fireMapMousePressed(e);
      e.getComponent().requestFocus();

      if (mouseSupport.proxy == null) {

         if (SwingUtilities.isRightMouseButton(e)) {
            // mouse has now been pressed
            // mousePressed = true;

            MapBean mb = theMap;
            if (mb == null && e.getSource() instanceof MapBean) {
               mb = (MapBean) e.getSource();
            }

            if (mb != null) {
               // anchor the new first point of the line
               rPoint1 = mb.getCoordinates(e);
               // ensure the second point is not yet set.
               rPoint2 = null;
            }
         }
      }
   }

   public void mouseClicked(MouseEvent e) {
      Object obj = e.getSource();

      boolean consumed = mouseSupport.fireMapMouseClicked(e);

      if (consumed || !(obj instanceof MapBean) || e.getClickCount() < 2)
         return;

      MapBean map = (MapBean) obj;
      Projection projection = map.getProjection();
      Proj p = (Proj) projection;

      Point2D llp = map.getCoordinates(e);

      boolean shift = e.isShiftDown();

      if (shift) {
         p.setScale(p.getScale() * 2.0f);
      } else {
         p.setScale(p.getScale() / 2.0f);
      }

      p.setCenter(llp);
      map.setProjection(p);
   }

   /**
    * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
    *      Make Pan event for the map.
    */
   public void mouseReleased(MouseEvent arg0) {

      if (mouseSupport.proxy == null && isPanning) {

         BufferedMapBean mb = getBufferedMapBean(arg0);
         if (mb == null) {
            return;
         }

         Projection proj = mb.getProjection();
         Point2D center = proj.forward(proj.getCenter());

         Point2D pnt = mb.getNonRotatedLocation(arg0);
         int x = (int) pnt.getX();
         int y = (int) pnt.getY();

         center.setLocation(center.getX() - x + oX, center.getY() - y + oY);
         mb.setCenter(proj.inverse(center));

         mb.setPanningTransform(null);

         isPanning = false;
         // bufferedMapImage = null; //clean up when not active...
      }

      super.mouseReleased(arg0);
   }

   public boolean isLeaveShadow() {
      return leaveShadow;
   }

   public void setLeaveShadow(boolean leaveShadow) {
      this.leaveShadow = leaveShadow;
   }

   public float getOpaqueness() {
      return opaqueness;
   }

   public void setOpaqueness(float opaqueness) {
      this.opaqueness = opaqueness;
   }

   public boolean isPanning() {
      return isPanning;
   }

   public int getOX() {
      return oX;
   }

   public int getOY() {
      return oY;
   }

}
