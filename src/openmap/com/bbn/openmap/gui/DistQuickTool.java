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

package com.bbn.openmap.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.bbn.openmap.BufferedMapBean;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.CoordMouseMode;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.event.MapMouseMode;
import com.bbn.openmap.event.OMMouseMode;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * The distance quicktool is a Tool object that uses an embedded mouse mode to
 * measure distance on the map. It's intended to be used with the OMMouseMode,
 * using it as a proxy, and listens to the MouseDelegator to make sure the
 * OMMouseMode is the active one while the dist quicktool button is enabled.
 */
public class DistQuickTool
      extends OMToolComponent
      implements Tool, PropertyChangeListener {

   private static final long serialVersionUID = 1L;
   protected Logger logger = Logger.getLogger("com.bbn.openmap.gui.DistQuickTool");

   protected MouseMode mouseMode = null;
   protected JButton launchButton = null;
   protected boolean ommmActive = false;

   public DistQuickTool() {
      mouseMode = getMouseMode();
      try {
         setOpaque(false);
         URL url = PropUtils.getResourceOrFileOrURL("com/bbn/openmap/tools/drawing/distance.png");
         ImageIcon ii = new ImageIcon(url);
         launchButton = new JButton(ii);
         launchButton.setPreferredSize(new Dimension(25, 25));
         launchButton.setToolTipText("Measure Distance");
         launchButton.setFocusable(false);
         launchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
               go();
            }
         });

         add(launchButton);
      } catch (MalformedURLException e) {

         e.printStackTrace();
      }
   }

   protected void go() {
      MouseDelegator mouseDelegator = ((MapHandler) getBeanContext()).get(MouseDelegator.class);
      if (mouseDelegator != null) {
         MapMouseMode proxyParent = mouseDelegator.getActiveMouseMode();
         if (proxyParent instanceof OMMouseMode) {
            ommmActive = true;
            mouseMode.go(proxyParent);
            launchButton.setEnabled(false);
         }
      }
   }

   public void setProperties(String prefix, Properties props) {
      super.setProperties(prefix, props);
      mouseMode.setProperties(prefix, props);
   }

   public Properties getProperties(Properties props) {
      props = super.getProperties(props);
      mouseMode.getProperties(props);
      return props;
   }

   public Properties getPropertyInfo(Properties props) {
      props = super.getPropertyInfo(props);
      mouseMode.getPropertyInfo(props);
      return props;
   }

   public void findAndInit(Object someObj) {
      super.findAndInit(someObj);

      if (someObj instanceof MouseDelegator) {
         ((MouseDelegator) someObj).addPropertyChangeListener(this);
         
         ommmActive = ((MouseDelegator)someObj).getActiveMouseMode() instanceof OMMouseMode;
      }
   }

   public void findAndUndo(Object someObj) {
      super.findAndUndo(someObj);

      if (someObj instanceof MouseDelegator) {
         ((MouseDelegator) someObj).removePropertyChangeListener(this);
      }
   }
   
   public MouseMode getMouseMode() {
      if (mouseMode == null) {
         mouseMode = new MouseMode();
      }
      return mouseMode;
   }

   public class MouseMode
         extends CoordMouseMode
         implements ProjectionListener {

      private static final long serialVersionUID = 1L;

      public final static String UnitProperty = "units";
      public final static String ShowCircleProperty = "showCircle";
      public final static String ShowAngleProperty = "showAngle";

      public final static transient String modeID = "Distance";

      public transient DecimalFormat df = new DecimalFormat("0");
      // The unit type, default mile
      private Length unit = Length.MILE;
      // Flag to display the azimuth angle. Default true
      boolean showAngle = true;

      /**
       * rPoint1 is the anchor point of a line segment
       */
      public Point2D rPoint1;
      /**
       * rPoint2 is the new (current) point of a line segment
       */
      public Point2D rPoint2;
      /**
       * Flag, true if the mouse has already been pressed
       */
      public boolean mousePressed = false;
      /**
       * Vector to store all distance segments, first point and last point pairs
       */
      public Vector<Point2D> segments = new Vector<Point2D>();
      /**
       * Distance of the current segment
       */
      public double distance = 0;
      /**
       * The cumulative distance from the first mouse click
       */
      public double totalDistance = 0;
      /**
       * To display the rubberband circle, default true
       */
      private boolean displayCircle = true;
      /**
       * Special units value for displaying all units ... use only in properties
       * file
       */
      public final static String AllUnitsPropertyValue = "all";
      protected BufferedMapBean theMap = null;
      protected String coordString = null;

      protected OMGraphicList distanceList;

      protected MapMouseMode proxyParent = null;

      public MouseMode() {
         super(modeID, true);
      }

      public void setProperties(String prefix, Properties props) {
         super.setProperties(prefix, props);
         prefix = PropUtils.getScopedPropertyPrefix(prefix);

         String name = props.getProperty(prefix + UnitProperty);
         if (name != null) {
            Length length = Length.get(name);
            if (length != null) {
               setUnit(length);
            } else if (name.equals(AllUnitsPropertyValue)) {
               setUnit(null);
            }
         }

         setDisplayCircle(PropUtils.booleanFromProperties(props, prefix + ShowCircleProperty, isDisplayCircle()));
         setShowAngle(PropUtils.booleanFromProperties(props, prefix + ShowAngleProperty, isShowAngle()));

      }

      public Properties getProperties(Properties props) {
         props = super.getProperties(props);
         String prefix = PropUtils.getScopedPropertyPrefix(this);
         String unitValue = (unit != null ? unit.toString() : AllUnitsPropertyValue);
         props.put(prefix + UnitProperty, unitValue);
         props.put(prefix + ShowCircleProperty, new Boolean(isDisplayCircle()).toString());
         props.put(prefix + ShowAngleProperty, new Boolean(isShowAngle()).toString());
         return props;
      }

      public Properties getPropertyInfo(Properties props) {
         props = super.getPropertyInfo(props);

         PropUtils.setI18NPropertyInfo(i18n, props, DistQuickTool.class, UnitProperty, "Units",
                                       "Units to use for measurements, from Length.name possibilities.", null);

         PropUtils.setI18NPropertyInfo(i18n, props, DistQuickTool.class, ShowCircleProperty, "Show Distance Circle",
                                       "Flag to set whether the range circle is drawn at the end of the line (true/false).",
                                       "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

         PropUtils.setI18NPropertyInfo(i18n, props, DistQuickTool.class, ShowAngleProperty, "Show Angle",
                                       "Flag to note the azimuth angle of the line in the information line (true/false).",
                                       "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

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
       *      redrawing when the mouse is move, but, I need to repain the
       *      original image.
       */
      public void mouseDragged(MouseEvent arg0) {

         if (theMap == null) {
            // OMMouseMode needs a BufferedMapBean
            return;
         }

         if (rPoint1 == null) {
            rPoint1 = theMap.getCoordinates(arg0);

         } else {
            // right mouse click, measure
            double lat1, lat2, long1, long2;
            // erase the old line and circle first
            // paintRubberband(rPoint1, rPoint2, coordString);
            // get the current mouse location in latlon
            rPoint2 = theMap.getCoordinates(arg0);

            lat1 = rPoint1.getY();
            long1 = rPoint1.getX();
            // lat, lon of current mouse position
            lat2 = rPoint2.getY();
            long2 = rPoint2.getX();
            // calculate great circle distance in nm
            // distance = getGreatCircleDist(lat1, long1,
            // lat2, long2, Length.NM);
            distance =
                  GreatCircle.sphericalDistance(ProjMath.degToRad(lat1), ProjMath.degToRad(long1), ProjMath.degToRad(lat2),
                                                ProjMath.degToRad(long2));

            // calculate azimuth angle dec deg
            double azimuth = getSphericalAzimuth(lat1, long1, lat2, long2);
            coordString = createDistanceInformationLine(rPoint2, distance, azimuth);

            // paint the new line and circle up to the current
            // mouse location
            paintRubberband(rPoint1, rPoint2, coordString);
            theMap.repaint();
         }
      }

      /**
       * Process a mouse pressed event. Add the mouse location to the segment
       * vector. Calculate the cumulative total distance.
       * 
       * @param e mouse event.
       */
      public void mousePressed(MouseEvent e) {
         e.getComponent().requestFocus();

         mousePressed = true;

         BufferedMapBean mb = getBufferedMapBean(e);
         if (mb == null) {
            // OMMouseMode needs a BufferedMapBean
            return;
         }

         theMap = mb;
         theMap.addPaintListener(this);

         if (mb != null) {
            // anchor the new first point of the line
            rPoint1 = theMap.getCoordinates(e);
            // ensure the second point is not yet set.
            rPoint2 = null;
            // add the distance to the total distance
            totalDistance = 0;
         }

      }

      public void mouseClicked(MouseEvent e) {
      }

      /**
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       *      Make Pan event for the map.
       */
      public void mouseReleased(MouseEvent arg0) {

         reset();
      }

      public void go(MapMouseMode proxyParent) {
         proxyParent.actAsProxyFor(this);
         this.proxyParent = proxyParent;
      }

      protected void reset() {

         if (theMap != null) {
            distanceList = null;
            // cleanup the drawing OMGraphics
            cleanUp();
            theMap.removePaintListener(this);
            theMap = null;
         }

         if (proxyParent != null && proxyParent.isProxyFor(mouseMode)) {
            proxyParent.releaseProxy();
            proxyParent = null;
         }

         launchButton.setEnabled(ommmActive);
      }

      /**
       * PaintListener interface, notifying the MouseMode that the MapBean has
       * repainted itself. Useful if the MouseMode is drawing stuff.
       */
      public void listenerPaint(java.awt.Graphics g) {
         if (distanceList != null) {
            distanceList.render(g);
         }
      }

      public void projectionChanged(ProjectionEvent e) {
         Projection p = e.getProjection();
         if (p != null && distanceList != null) {
            distanceList.generate(p);
         }
      }

      /**
       * Draw a rubberband line and circle between two points
       * 
       * @param pt1 the anchor point.
       * @param pt2 the current (mouse) position.
       */
      @SuppressWarnings("serial")
      public void paintRubberband(Point2D pt1, Point2D pt2, String coordString) {
         if (distanceList == null) {
            distanceList = new OMGraphicList() {
               public void render(Graphics g) {
                  Graphics g2 = g.create();
                  g2.setXORMode(java.awt.Color.lightGray);

                  for (OMGraphic omg : this) {
                     if (omg instanceof OMText) {
                        omg.render(g);
                     } else {
                        omg.render(g2);
                     }
                  }

                  g2.dispose();
               }
            };
         }

         distanceList.clear();

         paintLine(pt1, pt2);
         paintCircle(pt1, pt2);
         paintText(pt1, pt2, coordString);
      }

      /**
       * Draw a rubberband line between two points
       * 
       * @param pt1 the anchor point.
       * @param pt2 the current (mouse) position.
       */
      public void paintLine(Point2D pt1, Point2D pt2) {
         if (pt1 != null && pt2 != null) {
            // the line connecting the segments
            OMLine cLine = new OMLine(pt1.getY(), pt1.getX(), pt2.getY(), pt2.getX(), OMGraphic.LINETYPE_GREATCIRCLE);
            // get the map projection
            Projection proj = theMap.getProjection();
            // prepare the line for rendering
            cLine.generate(proj);

            distanceList.add(cLine);
         }
      }

      public void paintText(Point2D base, Point2D pt1, String coordString) {
         if (coordString != null) {

            base = theMap.getProjection().forward(base);
            pt1 = theMap.getProjection().forward(pt1);

            if (base.distance(pt1) > 3) {
               // g.drawString(coordString, (int) pt1.getX() + 5, (int) pt1
               // .getY() - 5);

               OMText text = new OMText((int) pt1.getX() + 5, (int) pt1.getY() - 5, coordString, OMText.JUSTIFY_LEFT);

               Font font = text.getFont();
               text.setFont(font.deriveFont(Font.BOLD, font.getSize() + 4));

               text.setLinePaint(Color.BLACK);

               text.setTextMatteColor(Color.WHITE);
               text.setTextMatteStroke(new BasicStroke(5));
               text.setMattingPaint(OMColor.clear);

               text.generate(theMap.getProjection());
               distanceList.add(text);
            }

         }
      }

      /**
       * Draw a rubberband circle between two points
       * 
       * @param pt1 the anchor point.
       * @param pt2 the current (mouse) position.
       */
      public void paintCircle(Point2D pt1, Point2D pt2) {
         // do all this only if want to display the rubberband circle
         if (displayCircle) {
            if (pt1 != null && pt2 != null) {
               // first convert degrees to radians
               double radphi1 = ProjMath.degToRad(pt1.getY());
               double radlambda0 = ProjMath.degToRad(pt1.getX());
               double radphi = ProjMath.degToRad(pt2.getY());
               double radlambda = ProjMath.degToRad(pt2.getX());
               // calculate the circle radius
               double dRad = GreatCircle.sphericalDistance(radphi1, radlambda0, radphi, radlambda);
               // convert into decimal degrees
               double rad = ProjMath.radToDeg(dRad);
               // make the circle
               OMCircle circle = new OMCircle(pt1.getY(), pt1.getX(), rad);
               // get the map projection
               Projection proj = theMap.getProjection();
               // prepare the circle for rendering
               circle.generate(proj);
               distanceList.add(circle);
            }
         } // end if(displayCircle)
      }

      /**
       * Reset the segments and distances
       */
      public void cleanUp() {
         // a quick way to clean the vector
         segments = new Vector<Point2D>();
         // reset the total distance
         totalDistance = 0.0;
         distance = 0.0;
         coordString = null;
      }

      /**
       * Return the azimuth angle in decimal degrees from north. Based on
       * spherical_azimuth. See class GreatCircle.java
       * 
       * @param phi1 latitude in decimal degrees of start point
       * @param lambda0 longitude in decimal degrees of start point
       * @param phi latitude in decimal degrees of end point
       * @param lambda longitude in decimal degrees of end point
       * @return float azimuth angle in degrees
       */
      public double getSphericalAzimuth(double phi1, double lambda0, double phi, double lambda) {
         // convert arguments to radians
         double radphi1 = ProjMath.degToRad(phi1);
         double radlambda0 = ProjMath.degToRad(lambda0);
         double radphi = ProjMath.degToRad(phi);
         double radlambda = ProjMath.degToRad(lambda);
         // get the spherical azimuth in radians between the two points
         double az = GreatCircle.sphericalAzimuth(radphi1, radlambda0, radphi, radlambda);
         return ProjMath.radToDeg(az);
      }

      protected String createDistanceInformationLine(Point2D llp, double distance, double azimuth) {
         // setup the distance info to be displayed
         String unitInfo = null;
         // what unit is asked for
         if (unit == null) {
            unitInfo =
                  df.format(Length.NM.fromRadians((float) distance)) + Length.NM.getAbbr() + ",  "
                        + df.format(Length.KM.fromRadians((float) distance)) + Length.KM.getAbbr() + ",  "
                        + df.format(Length.MILE.fromRadians((float) distance)) + Length.MILE.getAbbr() + "  ";
         } else {
            unitInfo = df.format(unit.fromRadians(distance)) + " " + unit.getAbbr();
         }

         return unitInfo;
      }

      /**
       * Set the unit of distance to be displayed: Length.NM, Length.KM or
       * Length.MILE. If null, displays all of them.
       */
      public void setUnit(Length units) {
         unit = units;
      }

      public boolean isShowAngle() {
         return showAngle;
      }

      public void setShowAngle(boolean showAngle) {
         this.showAngle = showAngle;
      }

      public boolean isDisplayCircle() {
         return displayCircle;
      }

      public void setDisplayCircle(boolean displayCircle) {
         this.displayCircle = displayCircle;
      }

      /**
       * Return the unit of distance being displayed: Length.NM, Length.KM or
       * Length.MILE. If null, displays all of them.
       */
      public Length getUnit() {
         return unit;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
    * PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent evt) {
      Object obj = evt.getSource();
      if (obj instanceof MouseDelegator) {
         String propName = evt.getPropertyName();
         if (propName.equals(MouseDelegator.ActiveModeProperty)) {

            Object newVal = evt.getNewValue();
            ommmActive = newVal instanceof OMMouseMode;
            launchButton.setEnabled(ommmActive);

            if (mouseMode != null) {
               mouseMode.reset();
            }
         } else if (propName.equals(MouseDelegator.ProxyMouseModeProperty)) {
            if (!this.equals(evt.getNewValue())) {
               mouseMode.reset();
            }

            ommmActive = ((MouseDelegator)obj).getActiveMouseMode() instanceof OMMouseMode;
            launchButton.setEnabled(evt.getNewValue() == null && ommmActive);
         }
      }
   }
}
