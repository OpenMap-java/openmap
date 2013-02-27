// **********************************************************************
//
// <copyright>
//
// BBN Technologies, a Verizon Company
// 10 Moulton Street
// Cambridge, MA 02138
// (617) 873-8000
//
// Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>

package com.bbn.openmap.gui.time;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.MoreMath;
import com.bbn.openmap.event.CenterListener;
import com.bbn.openmap.event.CenterSupport;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMScalingIcon;
import com.bbn.openmap.proj.Cartesian;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.time.Clock;
import com.bbn.openmap.time.TimeBounds;
import com.bbn.openmap.time.TimeBoundsEvent;
import com.bbn.openmap.time.TimeBoundsListener;
import com.bbn.openmap.time.TimeEvent;
import com.bbn.openmap.time.TimeEventListener;
import com.bbn.openmap.time.TimerStatus;
import com.bbn.openmap.tools.icon.BasicIconPart;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.OMIconFactory;

/**
 * Timeline layer
 * 
 * Render events and allow for their selection on a variable-scale timeline
 */
public class TimeSliderLayer
      extends OMGraphicHandlerLayer
      implements PropertyChangeListener, MapMouseListener, ComponentListener, TimeBoundsListener, TimeEventListener {

   protected static Logger logger = Logger.getLogger("com.bbn.openmap.gui.time.TimeSliderLayer");

   protected CenterSupport centerDelegate;

   protected ZoomSupport zoomDelegate;

   long currentTime = 0;

   long gameStartTime = 0;

   long gameEndTime = 0;

   // KMTODO package this up into a standalone widget?
   // Times are generally in minutes
   double selectionWidthMinutes = 1.0;
   double maxSelectionWidthMinutes = 1.0;

   double selectionCenter = 0;

   OMRect boundsRectLeftHandle; // handles for scaling selection

   OMRect boundsRectRightHandle;
   
   Color selectionPointLineColor = TimelineLayer.tint;
   Color selectionPointFillColor = TimelineLayer.tint;
   Color leftHandleFillColor = Color.black;
   Color rightHandleFillColor = Color.black;

   OMLine baseLine; // thick line along middle

   OMPoly contextPoly; // lines that relate time slider to timeline above.

   int sliderPointHalfWidth = 5;

   TimelinePanel timelinePanel;
   TimelineLayer timelineLayer;

   public static double magicScaleFactor = 100000000; // KMTODO (Don? I guess
   // this is to
   // address a precision issue?)

   Clock clock;

   LabelPanel labelPanel;

   private boolean isNoTime = true;

   // In realTimeMode, gameEndTime is the origin, rather than gameStartTime
   private final boolean realTimeMode;

   // Used to refrain from re-scaling every TimeBoundsUpdate (in realTimeMode
   // only)
   private boolean userHasChangedScale = false;

   private final List<ITimeBoundsUserActionsListener> timeBoundsUserActionsListeners =
         new ArrayList<ITimeBoundsUserActionsListener>();

   private final JButton zoomToSelection = new JButton("Zoom to Selection");
   private final JButton renderFixedSelection = new JButton("Show Entire Selection");
   
   /**
    * Construct the TimelineLayer.
    * 
    * @param realTimeMode TODO
    */
   public TimeSliderLayer(boolean realTimeMode) {
      this.realTimeMode = realTimeMode;
      setName("TimeSlider");

      // This is how to set the ProjectionChangePolicy, which
      // dictates how the layer behaves when a new projection is
      // received.
      setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, false));
      // Making the setting so this layer receives events from the
      // SelectMouseMode, which has a modeID of "Gestures". Other
      // IDs can be added as needed.
      setMouseModeIDsForEvents(new String[] {
         "Gestures"
      });

      centerDelegate = new CenterSupport(this);
      zoomDelegate = new ZoomSupport(this);
      addComponentListener(this);
   }

   public void findAndInit(Object someObj) {
      if (someObj instanceof Clock) {
         clock = ((Clock) someObj);
         clock.addTimeEventListener(this);
         clock.addTimeBoundsListener(this);

         gameStartTime = ((Clock) someObj).getStartTime();
         gameEndTime = ((Clock) someObj).getEndTime();

         // Just in case we missed an early TimeBoundEvent
         updateTimeBounds(gameStartTime, gameEndTime);
      }
      if (someObj instanceof CenterListener) {
         centerDelegate.add((CenterListener) someObj);
      }
      if (someObj instanceof ZoomListener) {
         zoomDelegate.add((ZoomListener) someObj);
      }
      if (someObj instanceof TimelinePanel.Wrapper) {
         timelinePanel = ((TimelinePanel.Wrapper) someObj).getTimelinePanel();
         timelinePanel.getMapBean().addPropertyChangeListener(this);
         timelineLayer = timelinePanel.getTimelineLayer();
      }
   }

   /**
    * Called with the projection changes, should just generate the current
    * markings for the new projection.
    */
   public synchronized OMGraphicList prepare() {
      OMGraphicList list = getList();

      if (list == null) {
         list = new OMGraphicList();
      } else {
         list.clear();
      }

      TimeDrape drape = new TimeDrape(0, 0, -1, -1);
      drape.setVisible(isNoTime);
      drape.setFillPaint(Color.gray);
      drape.generate(getProjection());
      list.add(drape);

      list.add(getControlWidgetList(getProjection()));

      return list;
   }

   /**
    * All we want to do here is reset the current position of all of the
    * widgets, and generate them with the projection for the new position. After
    * this call, the widgets are ready to paint.
    */
   public synchronized OMGraphicList getControlWidgetList(Projection proj) {

      Projection projection = getProjection();

      OMGraphicList controlWidgetList = new OMGraphicList();

      // triangle indicating center of selection
      ImageIcon selectionPointImage;

      DrawingAttributes da = new DrawingAttributes();

      da.setFillPaint(selectionPointFillColor);
      da.setLinePaint(selectionPointLineColor);
      IconPart ip = new BasicIconPart(new Polygon(new int[] {
         50,
         90,
         10,
         50
      }, new int[] {
         10,
         90,
         90,
         10
      }, 4), da);
      
      selectionPointImage = OMIconFactory.getIcon(32, 32, ip);
      OMScalingIcon selectionPoint = new OMScalingIcon(0f, 0f, 0, 6, selectionPointImage, 1.0f);
      final float selectionPointScale = 1.8f;
      selectionPoint.setMaxScale(selectionPointScale);
      selectionPoint.setMinScale(selectionPointScale);
      controlWidgetList.add(selectionPoint);

      boundsRectLeftHandle = new OMRect(0, 0, 0, 0);
      boundsRectLeftHandle.setFillPaint(leftHandleFillColor);
      controlWidgetList.add(boundsRectLeftHandle);

      boundsRectRightHandle = new OMRect(0, 0, 0, 0);
      boundsRectRightHandle.setFillPaint(rightHandleFillColor);
      controlWidgetList.add(boundsRectRightHandle);

      int[] xs = new int[8];
      int[] ys = new int[8];
      contextPoly = new OMPoly(xs, ys);
      contextPoly.setFillPaint(Color.white);
      controlWidgetList.add(contextPoly);

      baseLine = new OMLine(0, 0, 0, 0);
      baseLine.setLinePaint(Color.black);
      baseLine.setStroke(new BasicStroke(2));
      controlWidgetList.add(baseLine);
      
      if (projection == null) {
         return controlWidgetList; // Huhn?
      }

      double screenWidth = projection.getWidth();
      float scale =
            (float) (magicScaleFactor * (double) TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime) / screenWidth);
      Point2D projCenter = projection.getCenter();

      // TODO reconsider proper test here - for the moment, just brute-force
      // reproject always
      if (projCenter.getX() > selectionWidthMinutes || scale != projection.getScale()) {
         double nCenterLon = TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime) / 2f;
         projCenter.setLocation(nCenterLon, 0);
         projection = new Cartesian(projCenter, scale, projection.getWidth(), projection.getHeight());
         setProjection(projection);
      }

      // Reset primary handle
      int contextBuffer = (int) (projection.getHeight() * .4);

      // If 'selectionCenter' is outside current time bounds, first attempt to
      // recompute from currentTime
      if (selectionCenter * 60.0 * TimeUnit.SECONDS.toMillis(1) > (gameEndTime - gameStartTime) || selectionCenter < 0) {
         selectionCenter = TimelineLayer.forwardProjectMillis(currentTime);
      }

      // And if _that_ fails, just clamp
      // DFD changed from TimeUnit.MINUTE.toMillis(1) to MILLISECONDS because
      // Java 5 doesn't have MINUTE
      if (selectionCenter * TimeUnit.MILLISECONDS.toMillis(60000) > (gameEndTime - gameStartTime)) {
         selectionCenter = (gameEndTime - gameStartTime) / TimeUnit.MILLISECONDS.toMillis(60000);
      }

      if (selectionCenter < 0) {
         selectionCenter = 0;
      }

      int x = (int) projection.forward(0, selectionCenter).getX();

      // Reset bounds and handles

      Point2D sliderEndPoint = projection.forward(0, selectionWidthMinutes);
      Point2D origin = projection.forward(0, 0);
      int selectionHalfWidth = (int) ((sliderEndPoint.getX() - origin.getX()) / 2);

      int north = contextBuffer;
      int west = x - selectionHalfWidth;
      int south = projection.getHeight() - 1;
      int east = x + selectionHalfWidth;
      int mid = contextBuffer + 1 + (south - contextBuffer) / 2;

      if (logger.isLoggable(Level.FINE)) {
         logger.fine("selectionCenter:" + selectionCenter + ", selectionWidthMinutes:" + selectionWidthMinutes + ", x:" + x
               + ", origin:" + origin);
         logger.fine("  projection:" + projection);
      }

      selectionPoint.setLon((float) selectionCenter);
      selectionPoint.generate(projection);
      
      // and the two handles for the bounds

      int handleWest = west - sliderPointHalfWidth;
      int handleEast = west + sliderPointHalfWidth;
      
      final int sliderPointHalfHeight = 2;

      boundsRectLeftHandle.setLocation(handleWest, north + sliderPointHalfHeight, handleEast, south - sliderPointHalfHeight);
      boundsRectLeftHandle.generate(projection);

      handleWest = east - sliderPointHalfWidth;
      handleEast = east + sliderPointHalfWidth;

      boundsRectRightHandle.setLocation(handleWest, north + sliderPointHalfHeight, handleEast, south - sliderPointHalfHeight);
      boundsRectRightHandle.generate(projection);

      // and the context lines, that show how the current selection maps to
      // the timeline above

      xs = contextPoly.getXs();
      ys = contextPoly.getYs();

      xs[0] = 0;
      ys[0] = -1;
      xs[1] = 0;
      ys[1] = north;
      xs[2] = west;
      ys[2] = north;
      xs[3] = west;
      ys[3] = south;
      xs[4] = east;
      ys[4] = south;
      xs[5] = east;
      ys[5] = north;
      xs[6] = projection.getWidth() - 1;
      ys[6] = north;
      xs[7] = projection.getWidth() - 1;
      ys[7] = -1;
      contextPoly.generate(projection);

      baseLine.setPts(new int[] {
         0,
         mid,
         projection.getWidth(),
         mid
      });
      baseLine.generate(projection);

      return controlWidgetList;
   }

   protected void updateTimeline() {
      if (timelinePanel != null) {
         float scale = (float) (magicScaleFactor * selectionWidthMinutes / getProjection().getWidth());
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("Updating timeline with scale: " + scale);
         }
         timelinePanel.getMapBean().setScale(scale);
      }
   }

   public String getName() {
      return "TimelineLayer";
   }
   
   public void setSelectionPointFillColor(Color color) {
      selectionPointFillColor = color;
   }
   
   public void setSelectionPointLineColor(Color color) {
      selectionPointLineColor = color;
   }
   
   public void setLeftHandleFillColor(Color color) {
      leftHandleFillColor = color;
   }
   
   public void setRightHandleFillColor(Color color) {
      rightHandleFillColor = color;
   }

   /**
    * Updates zoom and center listeners with new projection information.
    * 
    */
   protected void finalizeProjection() {
      Projection projection = getProjection();
      Cartesian cartesian = (projection instanceof Cartesian) ? (Cartesian) projection : null;

      if (cartesian != null) {

         double screenWidth = cartesian.getWidth();
         cartesian.setLeftLimit(TimelineLayer.forwardProjectMillis(gameStartTime));
         cartesian.setRightLimit(TimelineLayer.forwardProjectMillis(gameEndTime));
         cartesian.setLimitAnchorPoint(new Point2D.Double(TimelineLayer.forwardProjectMillis(-gameStartTime), 0));

         float scale =
               (float) (magicScaleFactor * (double) TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime) / screenWidth);

         zoomDelegate.fireZoom(ZoomEvent.ABSOLUTE, scale);

         double nCenterLon = TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime) / 2f;

         logger.fine("Telling the center delegate that the new center is 0, " + nCenterLon);

         centerDelegate.fireCenter(0, nCenterLon);

         // We are getting really large values for the center point of the
         // projection that the layer knows about. The MapBean projection
         // gets set OK, but then the projection held by the layer wrong, and
         // it throws the whole widget out of wack. If we set
         // the center of the projection to what it should be (the center
         // point between the start and end times), then everything settles
         // out.
         double x = cartesian.getCenter().getX();
         if (!MoreMath.approximately_equal(x, nCenterLon)) {
            ((MapBean) ((MapHandler) getBeanContext()).get(MapBean.class)).setCenter(0, nCenterLon);
         }

         repaint();
      }
   }

   public void updateTime(TimeEvent te) {

      if (checkAndSetForNoTime(te)) {
         return;
      }

      TimerStatus timerStatus = te.getTimerStatus();

      if (timerStatus.equals(TimerStatus.STEP_FORWARD) || timerStatus.equals(TimerStatus.STEP_BACKWARD)
            || timerStatus.equals(TimerStatus.UPDATE)) {

         currentTime = te.getSystemTime();
         currentTime -= gameStartTime;

         selectionCenter = TimelineLayer.forwardProjectMillis(currentTime);
         doPrepare();
      }

      if (timerStatus.equals(TimerStatus.FORWARD) || timerStatus.equals(TimerStatus.BACKWARD)
            || timerStatus.equals(TimerStatus.STOPPED)) {
         if (logger.isLoggable(Level.FINE)) {
            logger.fine("updated time: " + te);
         }
         // Checking for a running clock prevents a time status
         // update after the clock is stopped. The
         // AudioFileHandlers don't care about the current time
         // if it isn't running.
         if (realTimeMode || ((Clock) te.getSource()).isRunning()) {
            // update(te.getSystemTime());
            currentTime = te.getSystemTime();
            currentTime -= gameStartTime;

            selectionCenter = TimelineLayer.forwardProjectMillis(currentTime);
            doPrepare();
         }
      }

   }

   /*
    * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
    * PropertyChangeEvent)
    */
   public void propertyChange(PropertyChangeEvent evt) {
      String propertyName = evt.getPropertyName();
      if (propertyName == MapBean.ProjectionProperty) {
         // This property should be from the TimelineLayer's MapBean, solely
         // for the scale measurement.
         logger.fine(propertyName + " from " + evt.getSource());
         Projection timeLineProj = (Projection) evt.getNewValue();
         // Need to solve for selectionWidthMinutes
         if (!realTimeMode || !userHasChangedScale) {
            selectionWidthMinutes = timeLineProj.getScale() * getProjection().getWidth() / magicScaleFactor;
         }

         if (selectionWidthMinutes > maxSelectionWidthMinutes + .0001
         /* || selectionWidthMinutes < .0001 */) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("resetting selectionWidthMinutes to max (projection change property change), was "
                     + selectionWidthMinutes + ", now " + maxSelectionWidthMinutes);
            }
            selectionWidthMinutes = maxSelectionWidthMinutes;
         }

         doPrepare();
      }
   }

   public boolean getUserHasChangedScale() {
      return userHasChangedScale;
   }

   public void setUserHasChangedScale(boolean userHasChangedScale) {
      this.userHasChangedScale = userHasChangedScale;
   }

   protected boolean checkAndSetForNoTime(TimeEvent te) {
      isNoTime = te == TimeEvent.NO_TIME;
      return isNoTime;
   }

   public MapMouseListener getMapMouseListener() {
      return this;
   }

   public String[] getMouseModeServiceList() {
      return getMouseModeIDsForEvents();
   }

   enum DragState {
      NONE, PRIMARY_HANDLE, LEFT_HANDLE, RIGHT_HANDLE
   }

   DragState dragState = DragState.NONE;

   public boolean mousePressed(MouseEvent e) {
      updateMouseTimeDisplay(e);
      clearFixedRenderRange();
      int x = e.getPoint().x;
      int y = e.getPoint().y;

      if (boundsRectLeftHandle.contains(x, y)) {
         dragState = DragState.LEFT_HANDLE;
         userHasChangedScale = true;
      } else if (boundsRectRightHandle.contains(x, y)) {
         dragState = DragState.RIGHT_HANDLE;
         userHasChangedScale = true;
      } else {
         dragState = DragState.PRIMARY_HANDLE;
         Projection projection = getProjection();

         if (projection != null) {
            Point2D invPnt = projection.inverse(x, y);
            setSelectionCenter(invPnt.getX());
            updateTimeline();
         }
      }
      return true;
   }

   public boolean mouseReleased(MouseEvent e) {
      updateMouseTimeDisplay(e);
      dragState = DragState.NONE;
      return false;
   }

   public boolean mouseClicked(MouseEvent e) {
      updateMouseTimeDisplay(e);
      return false;
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
      timelineLayer.updateMouseTimeDisplay(new Long(-1));
   }

   void setSelectionCenter(double newCenter) {

      selectionCenter = newCenter;

      if (selectionCenter < 0) {
         selectionCenter = 0;
      }

      double offsetEnd = (double) (gameEndTime - gameStartTime) / 1000.0 / 60.0;

      if (selectionCenter > offsetEnd) {
         selectionCenter = offsetEnd;
      }

      clock.setTime(gameStartTime + (long) (selectionCenter * 60 * 1000));
   }

   public boolean mouseDragged(MouseEvent e) {
      updateMouseTimeDisplay(e);
      Projection projection = getProjection();

      if (projection == null) {
         return false; // Huhn?
      }

      double worldMouse;
      Point2D invPnt;
      int x = e.getPoint().x;
      int y = e.getPoint().y;
      int selectionCenterX = (int) projection.forward(0, selectionCenter).getX();
      boolean scaleChange = false;

      switch (dragState) {

         case PRIMARY_HANDLE:
            invPnt = projection.inverse(x, y);
            setSelectionCenter(invPnt.getX());
            break;

         case LEFT_HANDLE:

            if (x >= selectionCenterX - sliderPointHalfWidth) {
               x = selectionCenterX - sliderPointHalfWidth;
            }

            invPnt = projection.inverse(x, y);
            worldMouse = invPnt.getX();
            selectionWidthMinutes = 2 * (selectionCenter - worldMouse);
            scaleChange = true;
            break;

         case RIGHT_HANDLE:

            if (x <= selectionCenterX + sliderPointHalfWidth) {
               x = selectionCenterX + sliderPointHalfWidth;
            }
            invPnt = projection.inverse(x, y);
            worldMouse = invPnt.getX();
            selectionWidthMinutes = 2 * (worldMouse - selectionCenter);
            scaleChange = true;
            break;
      }

      if(scaleChange) {
         if (selectionWidthMinutes > maxSelectionWidthMinutes) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("resetting selectionWidthMinutes to max, was " + selectionWidthMinutes + ", now "
                     + maxSelectionWidthMinutes);
            }
            selectionWidthMinutes = maxSelectionWidthMinutes;
         }
         updateTimeline();
         timelineLayer.doPrepare();
      }
      
      doPrepare();
      
      return true;
   }

   public boolean mouseMoved(MouseEvent e) {
      updateMouseTimeDisplay(e);
      return true;
   }

   public void mouseMoved() {
   }

   protected double updateMouseTimeDisplay(MouseEvent e) {
      Projection proj = getProjection();
      Point2D latLong = proj.inverse(e.getPoint());
      double lon = latLong.getX();
      double endTime = TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime);
      if (lon < 0) {
         lon = 0;
      } else if (lon > endTime) {
         lon = endTime;
      }

      long offsetMillis = TimelineLayer.inverseProjectMillis(lon);

      timelineLayer.updateMouseTimeDisplay(new Long(offsetMillis));

      return lon;
   }

   public void componentHidden(ComponentEvent e) {
   }

   public void componentMoved(ComponentEvent e) {
   }

   public void componentResized(ComponentEvent e) {
      finalizeProjection();
   }

   public void componentShown(ComponentEvent e) {
   }

   public LabelPanel getTimeLabels() {
      if (labelPanel == null) {
         labelPanel = new LabelPanel();
      }
      return labelPanel;
   }

   public void updateTimeLabels(long startTime, long endTime) {
      LabelPanel lp = getTimeLabels();
      lp.updateTimeLabels(startTime, endTime);
   }

   class LabelPanel
         extends JPanel
         implements com.bbn.openmap.gui.MapPanelChild {
      protected JComponent timeStartLabel;
      protected JComponent timeEndLabel;
      public final static String NO_TIME_STRING = "--/--/-- (--:--:--)";

      public LabelPanel() {
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         setLayout(gridbag);

         if (realTimeMode) {
            JButton timeStartLabelButton = new JButton(NO_TIME_STRING);
            timeStartLabelButton.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
                     listener.invokeDateSelectionGUI(false);
                  }
               }

            });
            timeStartLabel = timeStartLabelButton;
            Font f = timeStartLabel.getFont();
            f = new Font(f.getFamily(), f.getStyle(), f.getSize() - 1);
            timeStartLabel.setFont(f);
            gridbag.setConstraints(timeStartLabel, c);
            add(timeStartLabel);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            JLabel buffer = new JLabel();
            gridbag.setConstraints(buffer, c);
            add(buffer);

            renderFixedSelection.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  long selectionStart = timelineLayer.getSelectionStart();
                  long selectionEnd = timelineLayer.getSelectionEnd();
                  if (selectionStart > 0 && selectionEnd > 0) {
                     for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
                        listener.setFixedRenderRange(selectionStart, selectionEnd);
                     }
                  }
               }

            });

            renderFixedSelection.setEnabled(false);
            renderFixedSelection.setFont(f);
            c.weightx = 0f;
            gridbag.setConstraints(renderFixedSelection, c);
            add(renderFixedSelection);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            buffer = new JLabel();
            gridbag.setConstraints(buffer, c);
            add(buffer);

            zoomToSelection.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  long selectionStart = timelineLayer.getSelectionStart();
                  long selectionEnd = timelineLayer.getSelectionEnd();
                  if (selectionStart > 0 && selectionEnd > 0) {
                     timelineLayer.clearSelection();
                     userHasChangedScale = false;
                     for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
                        listener.setTimeBounds(selectionStart, selectionEnd);
                     }
                     updateTimeBounds(selectionStart, selectionEnd);
                     updateTimeline();
                  }
               }

            });
            zoomToSelection.setEnabled(false);
            zoomToSelection.setFont(f);
            c.weightx = 0f;
            gridbag.setConstraints(zoomToSelection, c);
            add(zoomToSelection);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            buffer = new JLabel();
            gridbag.setConstraints(buffer, c);
            add(buffer);

            JButton jumpToRealTime = new JButton("Jump to Real Time");
            jumpToRealTime.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  userHasChangedScale = false;
                  for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
                     listener.jumpToRealTime();
                  }
                  updateTimeline();
               }

            });
            jumpToRealTime.setFont(f);
            c.weightx = 0f;
            gridbag.setConstraints(jumpToRealTime, c);
            add(jumpToRealTime);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            buffer = new JLabel();
            gridbag.setConstraints(buffer, c);
            add(buffer);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0f;
            JButton timeEndLabelButton = new JButton(NO_TIME_STRING);
            timeEndLabelButton.addActionListener(new ActionListener() {

               public void actionPerformed(ActionEvent e) {
                  for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
                     listener.invokeDateSelectionGUI(true);
                  }
               }

            });
            timeEndLabel = timeEndLabelButton;
            timeEndLabel.setFont(f);
            gridbag.setConstraints(timeEndLabel, c);
            add(timeEndLabel);
         } else {
            timeStartLabel = new JLabel(NO_TIME_STRING);
            Font f = timeStartLabel.getFont();
            f = new Font(f.getFamily(), f.getStyle(), f.getSize() - 1);
            timeStartLabel.setFont(f);
            gridbag.setConstraints(timeStartLabel, c);
            add(timeStartLabel);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            JLabel buffer = new JLabel();
            gridbag.setConstraints(buffer, c);
            add(buffer);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0f;
            timeEndLabel = new JLabel(NO_TIME_STRING, JLabel.RIGHT);
            timeEndLabel.setFont(f);
            gridbag.setConstraints(timeEndLabel, c);
            add(timeEndLabel);
         }
      }

      public String getPreferredLocation() {
         return BorderLayout.SOUTH;
      }

      public void setPreferredLocation(String string) {
      }

      public void updateTimeLabels(long startTime, long endTime) {
         if (realTimeMode) {
            ((JButton) timeStartLabel).setText(getLabelStringForTime(startTime));
            ((JButton) timeEndLabel).setText(getLabelStringForTime(endTime));
         } else {
            ((JLabel) timeStartLabel).setText(getLabelStringForTime(startTime));
            ((JLabel) timeEndLabel).setText(getLabelStringForTime(endTime));
         }
      }

      public String getLabelStringForTime(long time) {
         String ret = NO_TIME_STRING;
         if (time != Long.MAX_VALUE && time != Long.MIN_VALUE) {
            Date date = new Date(time);
            ret = TimePanel.dateFormat.format(date);
         }
         return ret;

      }

      public String getParentName() {
         // TODO Auto-generated method stub
         return null;
      }
   }

   public void paint(Graphics g) {
      try {
         super.paint(g);
      } catch (Exception e) {
         if (logger.isLoggable(Level.FINE)) {
            logger.warning(e.getMessage());
            e.printStackTrace();
         }
      }
   }

   public static class TimeDrape
         extends OMRect {
      int lo;
      int to;
      int ro;
      int bo;

      public TimeDrape(int leftOffset, int topOffset, int rightOffset, int bottomOffset) {
         super(0, 0, 0, 0);
         lo = leftOffset;
         to = topOffset;
         ro = rightOffset;
         bo = bottomOffset;
      }

      public boolean generate(Projection proj) {
         setLocation(0 + lo, 0 + to, proj.getWidth() + ro, proj.getHeight() + bo);
         return super.generate(proj);
      }

   }

   public void updateTimeBounds(TimeBoundsEvent tbe) {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("updating time bounds: " + tbe);
      }
      TimeBounds timeBounds = (TimeBounds) tbe.getNewTimeBounds();

      if (timeBounds != null) {
         updateTimeBounds(timeBounds.getStartTime(), timeBounds.getEndTime());
      } else {
         // TODO handle when time bounds are null, meaning when no time
         // bounds providers are active.
      }
   }

   public void updateTimeBounds(long start, long end) {

      // Recall that currentTime is in relative space (assumes start == 0)
      long boundsStartOffset = start - gameStartTime;
      currentTime -= boundsStartOffset;
      selectionCenter = TimelineLayer.forwardProjectMillis(currentTime);

      gameStartTime = start;
      gameEndTime = end;

      updateTimeLabels(gameStartTime, gameEndTime);

      // DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy
      // HH:mm:ss");
      // Date date = new Date(gameStartTime);
      // String sts = dateFormat.format(date);
      // date.setTime(gameEndTime);
      // String ets = dateFormat.format(date);

      maxSelectionWidthMinutes = TimelineLayer.forwardProjectMillis(gameEndTime - gameStartTime);
      if (realTimeMode && !userHasChangedScale) {
         selectionWidthMinutes = maxSelectionWidthMinutes;
      } else {
         if (selectionWidthMinutes > maxSelectionWidthMinutes || selectionWidthMinutes < .0001) {
            if (logger.isLoggable(Level.FINE)) {
               logger.fine("resetting selectionWidthMinutes to max (time bounds property change), was " + selectionWidthMinutes
                     + ", now " + maxSelectionWidthMinutes);
            }
            selectionWidthMinutes = maxSelectionWidthMinutes;
         }
      }

      finalizeProjection();
      updateTimeline();
      doPrepare();
      repaint();
   }

   /**
    * Treat mouse wheel rotations like slider-handle drags.
    * 
    * @param rot The number of rotation clicks (positive for zoom in, negative
    *        for zoom out).
    */
   public void adjustZoomFromMouseWheel(int rot) {
      Projection projection = getProjection();

      if (projection == null) {
         return; // Huhn?
      }

      setUserHasChangedScale(true);

      // Determine the effect of growing / shrinking the slider scale by 'rot'
      // pixels
      // So given current selection width (in minutes) and minutes-per-pixel,
      // just
      // apply rot as a delta

      Point2D minutesPnt0 = projection.inverse(0, 0);
      Point2D minutesPnt1 = projection.inverse(1, 0);

      double minutesPerPixel = minutesPnt1.getX() - minutesPnt0.getX();
      double minSelectionWidthMinutes = minutesPerPixel * sliderPointHalfWidth * 2;
      double selectionWidthPixels = selectionWidthMinutes / minutesPerPixel;
      double multiplier = selectionWidthPixels / 40;

      // Use a multiplier
      selectionWidthMinutes += rot * minutesPerPixel * multiplier;

      if (selectionWidthMinutes < minSelectionWidthMinutes) {
         selectionWidthMinutes = minSelectionWidthMinutes;
      }

      if (selectionWidthMinutes > maxSelectionWidthMinutes) {
         selectionWidthMinutes = maxSelectionWidthMinutes;
      }

      updateTimeline();

      doPrepare();
   }

   public void addTimeBoundsUserActionsListener(ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
      timeBoundsUserActionsListeners.add(timeBoundsUserActionsListener);
   }

   public void removeTimeBoundsUserActionsListener(ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
      timeBoundsUserActionsListeners.remove(timeBoundsUserActionsListener);
   }

   public void clearFixedRenderRange() {
      for (ITimeBoundsUserActionsListener listener : timeBoundsUserActionsListeners) {
         listener.clearFixedRenderRange();
      }
   }

   void setSelectionValid(boolean valid) {
      zoomToSelection.setEnabled(valid);
      renderFixedSelection.setEnabled(valid);
   }
}
