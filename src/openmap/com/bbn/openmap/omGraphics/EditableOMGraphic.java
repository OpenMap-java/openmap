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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMGraphic.java,v $
// $RCSfile: EditableOMGraphic.java,v $
// $Revision: 1.11 $
// $Date: 2005/12/09 21:09:04 $
// $Author: dietrick $
//
// **********************************************************************
package com.bbn.openmap.omGraphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.MissingResourceException;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.MapMouseAdapter;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.event.UndoEvent;
import com.bbn.openmap.event.UndoStack;
import com.bbn.openmap.gui.GridBagToolBar;
import com.bbn.openmap.omGraphics.editable.EOMGStateMachine;
import com.bbn.openmap.omGraphics.event.EOMGEvent;
import com.bbn.openmap.omGraphics.event.EOMGListener;
import com.bbn.openmap.omGraphics.event.EOMGListenerSupport;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * The EditableOMGraphic is a shell that controls actions to edit or create a graphic. This class contains a state
 * machine that defines how mouse events will be interpreted to modify the OMGraphic contained within. Any class that
 * extends this one is responsible for assigning the appropriate state machine and OMGraphic to itself. Also, an
 * EditableOMGraphic has a notion of a list of GrabPoints, which can be used as handles to the OMGraphic to provide
 * controlled modifications.
 */
public abstract class EditableOMGraphic
        extends MapMouseAdapter {

   /**
    * If the grab points should be rendered differently than the default, the DrawingAttributes should be stored in the
    * edited OMGraphic under this attribute key.
    */
   public final static String GRAB_POINT_DRAWING_ATTRIBUTES_ATTRIBUTE = "gpdaa";
   /**
    * If the EditableOMGraphic supports the notion of a selected GrabPoint (to highlight a node), and that selection
    * should change the appearance of a GrabPoint, the DrawingAttributes for that selection appearance should be stored
    * in the edited OMGraphic attributes under this attribute key.
    */
   public final static String SELECTED_GRAB_POINT_DRAWING_ATTRIBUTES_ATTRIBUTE = "sgpdaa";
   /**
    * The state machine that interprets the mouse events (and other events) and modifies the OMGraphics accordingly.
    *
    * @see com.bbn.openmap.util.stateMachine.StateMachine
    */
   protected EOMGStateMachine stateMachine;
   /**
    * This is here for the MapMouseListener interface. This may not be important, depending on what is funneling mouse
    * events to the graphic.
    */
   protected String[] mouseModeServiceList;
   /**
    * The array of GrabPoints.
    */
   protected GrabPoint[] gPoints;
   /**
    * The projection of the map. This can be retrieved from the mouse events, provided that the mouse events source is
    * the MapBean.
    */
   protected Projection projection;
   /**
    * This GrabPoint is one that has been grabbed by the mouse, and is being moved.
    */
   protected GrabPoint movingPoint = null;
   protected EOMGListenerSupport listeners = null;
   /**
    * Flag to indicate whether a GUI for this EOMG should be presented to allow edits to it's attributes.
    */
   protected boolean showGUI = true;
   /**
    * Flag to let states know if the edges of the graphic can be grabbed directly, for movement or manipulation, as
    * opposed to just allowing those actions through the grab points.
    */
   protected boolean canGrabGraphic = true;
   /**
    * The component to notify for changes made to the OMGraphic, so they can be undone if desired.
    */
   protected UndoStack undoStack;
   protected I18n i18n = Environment.getI18n();
   /**
    * A little flag to let the EOMG that a popup menu is up on the map. If the menu is up, and the menu is not clicked
    * on, we don't really want to deactivate the drawing tool right then - let a free click go by first, which will
    * dismiss the menu.
    */
   protected boolean popupIsUp = false;
   /**
    * Action mask for this graphic. Used as a holder for modifying objects to let this EditableOMGraphic know what is
    * being done to it.
    */
   protected int actionMask = 0;
   protected boolean DEBUG = false;
   protected boolean DEBUG_DETAIL = false;
   protected boolean xorRendering = true;

   protected EditableOMGraphic() {
      DEBUG = Debug.debugging("eomg");
      DEBUG_DETAIL = Debug.debugging("eomgdetail");
   }

   /**
    * Set the StateMachine for this EditableOMGraphic.
    *
    * @param sm StateMachine.
    * @see com.bbn.openmap.util.stateMachine.StateMachine
    */
   public void setStateMachine(EOMGStateMachine sm) {
      stateMachine = sm;
   }

   /**
    * Get the state machine for this EditableOMGraphic.
    */
   public EOMGStateMachine getStateMachine() {
      return stateMachine;
   }

   /**
    * Set the list of MouseMode names that this EditableOMGraphic will respond to, if it is dealing directly with a
    * MouseDelegator.
    */
   public void setMouseModeServiceList(String[] list) {
      mouseModeServiceList = list;
   }

   /**
    * Get the list of MouseMode names that this EditableOMGraphic will respond to, if it is dealing directly with a
    * MouseDelegator.
    */
   public String[] getMouseModeServiceList() {
      return mouseModeServiceList;
   }

   /**
    * Set whether this EOMG should provide a user interface to have the attributes modified.
    *
    * @param set true if the GUI should be shown.
    */
   public void setShowGUI(boolean set) {
      showGUI = set;
      OMGraphic graphic = getGraphic();
      if (graphic != null) {
         graphic.setShowEditablePalette(set);
      }
   }

   public boolean getShowGUI() {
      if (getGraphic() != null) {
         return getGraphic().getShowEditablePalette();
      } else {
         return showGUI;
      }
   }

   /**
    * Set whether a graphic can be manipulated by its edges, rather than just by its grab points. Used internally.
    */
   public void setCanGrabGraphic(boolean set) {
      canGrabGraphic = set;
   }

   /**
    * Get whether a graphic can be manipulated by its edges, rather than just by its grab points.
    */
   public boolean getCanGrabGraphic() {
      return canGrabGraphic;
   }

   /**
    * Set the OMGraphic that is being modified by the EditableOMGraphic. The type of OMGraphic needs to match what the
    * EditableOMGraphic is expecting. Assume that if the graphic passed in is null, that a proper graphic will be
    * created.
    *
    * @param graphic OMGraphic.
    */
   public abstract void setGraphic(OMGraphic graphic);

   /**
    * Create the OMGraphic that is to be modified by the EditableOMGraphic.
    *
    * @param ga GraphicAttributes, describing the graphic to be created.
    */
   public abstract void createGraphic(GraphicAttributes ga);

   /**
    * Get the OMGraphic that is being created/modified by the EditableOMGraphic.
    */
   public abstract OMGraphic getGraphic();

   /**
    * Remove all changes and put graphic as it was before modifications. If the graphic is being created, start over.
    */
   public void reset() {
      Debug.output("EditableOMGraphic.reset(): not yet supported");
   }

   /**
    * Set the grab point objects within the EditableOMGraphic array. The size and layout of the points in the array are
    * carefully determined by the EditableOMGraphic, so this method merely replaces objects within the array, not
    * replacing the array itself, so that you cannot reset the number of grab points an EditableOMGraphic uses for a
    * particular OMGraphic.
    *
    * @param points a GrabPoint[]
    * @return true if the grab point array was exactly what the EditableOMGraphic was expecting, in terms of length of
    * the GrabPoint array length. The method copies the array values that fit into the resident array.
    */
   public boolean setGrabPoints(GrabPoint[] points) {
      if (points == null || gPoints == null) {
         return false;
      }

      System.arraycopy(points, 0, gPoints, 0, Math.min(points.length, gPoints.length));

      return (points.length == gPoints.length);
   }

   /**
    * Method to allow objects to set OMAction masks on this editable graphic.
    */
   public void setActionMask(int mask) {
      actionMask = mask;
   }

   /**
    * Get the OMAction mask for this graphic.
    */
   public int getActionMask() {
      return actionMask;
   }

   /**
    * Tells the EditableOMGraphic that the locations of the grab points have been modified, and that the parameters of
    * the OMGraphic need to be modified accordingly.
    */
   public abstract void setGrabPoints();

   /**
    * Get the array of grab points used for the EditableOMGraphic. Given a mouse event, you can see if one of these is
    * affected, and move it accordingly. Call setGrabPoints() when modifications are done, so that the OMGraphic is
    * modified.
    */
   public GrabPoint[] getGrabPoints() {
      return gPoints;
   }

   /**
    * Set the GrabPoint at a particule index of the array. This can be used to tie two different grab points together.
    *
    * @param gb GrabPoint to assign within array.
    * @param index the index of the array to put the GrabPoint. The EditableOMGraphic should be able to provide the
    * description of the proper placement indexes.
    * @return If the grab point or array is null, or if the index is outside the range of the array, false is returned.
    * If everything goes OK, then true is returned.
    */
   public boolean setGrabPoint(GrabPoint gb, int index) {
      if (gPoints != null && gb != null && index >= 0 && index < gPoints.length) {

         gPoints[index] = gb;
         return true;
      } else {
         return false;
      }
   }

   /**
    * Return a particular GrabPoint at a particular point in the array. The EditableOMGraphic should describe which
    * indexes refer to which grab points in the EOMG GrabPoint array. If the index is outside the range of the array,
    * null is returned.
    */
   public GrabPoint getGrabPoint(int index) {
      if (gPoints != null && index >= 0 && index < gPoints.length) {
         return gPoints[index];
      } else {
         return null;
      }
   }

   /**
    * Attach to the Moving OffsetGrabPoint so if it moves, it will move this EditableOMGraphic with it.
    * EditableOMGraphic version doesn't do anything, each subclass has to decide which of its OffsetGrabPoints should be
    * attached to it.
    */
   public void attachToMovingGrabPoint(OffsetGrabPoint gp) {
   }

   /**
    * Detach from a Moving OffsetGrabPoint. The EditableOMGraphic version doesn't do anything, each subclass should
    * remove whatever GrabPoint it would have attached to an OffsetGrabPoint.
    */
   public void detachFromMovingGrabPoint(OffsetGrabPoint gp) {
   }

   /**
    * Set the GrabPoint that is in the middle of being modified, as a result of a mouseDragged event, or other
    * selection.
    */
   public void setMovingPoint(GrabPoint gp) {
      movingPoint = gp;
   }

   /**
    * Get the GrabPoint that is being moved. If it's null, then there isn't one.
    */
   public GrabPoint getMovingPoint() {
      return movingPoint;
   }

   /**
    * Notification that a MouseEvent was used to trigger creation or edit of this EditableOMGraphic, and this is the
    * first MouseEvent received. If the EditableOMGraphic can handle it, it should. Otherwise, it should put itself in
    * the right state to let the user know it's active.
    */
   public void handleInitialMouseEvent(MouseEvent e) {
      getStateMachine().setEdit();
      if (e != null) {
         GrabPoint gp = getMovingPoint(e);
         if (gp == null) {
            move(e);
         } else {
            getStateMachine().setSelected();
         }
      } else {
         getStateMachine().setSelected();
      }
   }

   /**
    * Given a MouseEvent, find a GrabPoint that it is touching, and set the moving point to that GrabPoint. Called when
    * a MouseEvent happens like a mousePressed or mouseReleased, and you want to find out if a GrabPoint should be used
    * to make modifications to the graphic or its position. This method should only be called to establish a moving
    * point. getMovingPoint() should be called to check to see if one has been established, and then redraw(MouseEvent)
    * would be called to move that moving point.
    *
    * @param e MouseEvent
    * @return GrabPoint that is touched by the MouseEvent, null if none are.
    */
   public GrabPoint getMovingPoint(MouseEvent e) {
      return _getMovingPoint(e);
   }

   /**
    * Given a MouseEvent, find a GrabPoint that it is touching, and set the moving point to that GrabPoint. A version
    * for grandchild classes.
    *
    * @param e MouseEvent that the GrabPoint should attach to.
    * @see #getMovingPoint(MouseEvent)
    */
   public GrabPoint _getMovingPoint(MouseEvent e) {
      movingPoint = null;

      GrabPoint[] gb = getGrabPoints();
      Point2D pnt = getProjectionPoint(e);

      double x = pnt.getX();
      double y = pnt.getY();

      for (int i = gb.length - 1; i >= 0; i--) {
         if (gb[i] != null && gb[i].distance(x, y) == 0) {
            setMovingPoint(gb[i]);
            // in case the points are on top of each other, the
            // last point in the array will take precedence.
            return gb[i];
         }
      }

      setMovingPoint(null);
      return null;
   }

   /**
    * Called to set the OffsetGrabPoint to the current mouse location, and update the OffsetGrabPoint with all the other
    * GrabPoint locations, so everything can shift smoothly. Should also set the OffsetGrabPoint to the movingPoint.
    */
   public abstract void move(MouseEvent e);

   /**
    * Clean the surface all the painting is taking place over.
    */
   public void cleanMap(MouseEvent e) {
      Object obj = e.getSource();
      if (!(obj instanceof MapBean)) {
         return;
      }

      // Could call repaint(), but I think we should paint in this
      // thread...
      MapBean map = (MapBean) obj;
      // Gets the buffer cleaned out.
      map.setBufferDirty(true);
      map.paintChildren(map.getGraphics(true));
   }

   /**
    * Same as redraw(e, false)
    */
   public void redraw(MouseEvent e) {
      redraw(e, false);
   }

   public void redraw(MouseEvent e, boolean firmPaint) {
      redraw(e, firmPaint, true);
   }
   /**
    * A DrawingAttributes object used to hold OMGraphic settings while it is being moved. When an OMGraphic is being
    * moved, basic (DEFAULT) settings are put on the OMGraphic to make it as light and uncomplicated as possible.
    */
   protected DrawingAttributes holder = new DrawingAttributes();

   /**
    * Given a MouseEvent, check the source, and if it's a MapBean, then grab the projection and java.awt.Graphics from
    * it to use for generation and rendering of the EditableOMGraphic objects.
    *
    * @param e MouseEvent
    * @param firmPaint true if the graphic is being rendered at rest, with fill colors and true colors, with the grab
    * point if the state allows it. If false, then the fill color will not be used, and just the graphic will be drawn.
    * Use false for graphics that are moving.
    */
   public void redraw(MouseEvent e, boolean firmPaint, boolean drawXOR) {
      if (DEBUG) {
         Debug.output("EditableOMGraphic.redraw(" + (firmPaint ? "firmPaint)" : ")"));
      }

      drawXOR = drawXOR && isXorRendering();

      if (e == null) {
         if (lastMouseEvent == null) {
            return;
         }
         e = lastMouseEvent;
      }

      Object obj = e.getSource();
      if (!(obj instanceof MapBean)) {
         return;
      }

      MapBean map = (MapBean) obj;
      Graphics g = map.getGraphics(true);

      OMGraphic graphic = getGraphic();

      // Seeing if we can make for a better rendering look by repainting the
      // mapBean all the time. With tiling, it works.
      if (!isXorRendering()) {
         map.repaint();
      }

      if (firmPaint) {
         // So, with a firm paint, we want to clean the screen. If
         // the map is being buffered, we need to clean out the
         // buffer, which is why we set the Request paint to true,
         // to get the image rebuilt. Otherwise, a copy of the
         // graphic remains.
         map.setBufferDirty(true);
         graphic.generate(getProjection());
         map.repaint();
      } else {
         // If we get here, we are painting a moving object, so we
         // only want to do the outline to make it as fast as
         // possible.
         holder.setFrom(graphic);
         DrawingAttributes.DEFAULT.setTo(graphic);

         modifyOMGraphicForEditRender();
         graphic.regenerate(getProjection());

         if (drawXOR) {
            g.setXORMode(Color.lightGray);
            g.setColor((Color) graphic.getDisplayPaint());

            render(g);
         }

         GrabPoint gp = getMovingPoint();
         if (gp != null) {
            Point2D pnt = getProjectionPoint(e);
            double x = pnt.getX();
            double y = pnt.getY();

            gp.set((int) x, (int) y);

            if (gp instanceof OffsetGrabPoint) {
               ((OffsetGrabPoint) gp).moveOffsets();
            }
            setGrabPoints();
         }
      }

      if (!firmPaint) {
         generate(getProjection());
         render(g);
         holder.setTo(graphic);
      }

      resetOMGraphicAfterEditRender();
      g.dispose();

      lastMouseEvent = e;
   }
   protected MouseEvent lastMouseEvent;

   /**
    * A convenience method that gives an EditableOMGraphic a chance to modify the OMGraphic so it can be drawn quickly,
    * by turning off labels, etc, right before the XORpainting happens. The OMGraphic should be configured so that the
    * render method does the least amount of painting possible. Note that the DrawingAttributes for the OMGraphic have
    * already been set to DrawingAttributes.DEFAULT (black line, clear fill).
    */
   protected void modifyOMGraphicForEditRender() {
   }

   /**
    * A convenience method that gives an EditableOMGraphic a chance to reset the OMGraphic so it can be rendered
    * normally, after it has been modified for quick paints. The DrawingAttributes for the OMGraphic have already been
    * reset to their normal settings, from the DrawingAttributes.DEFAULT settings that were used for the quick paint.
    */
   protected void resetOMGraphicAfterEditRender() {
   }

   public void repaint() {
      if (lastMouseEvent != null) {
         redraw(lastMouseEvent, true);
      }
   }

   protected void finalize() {
      if (getGraphic() != null) {
         getGraphic().setVisible(true);
      }
      if (Debug.debugging("gc")) {
         Debug.output("EditableOMGraphic gone.");
      }
   }

   /**
    * Use the current projection to place the graphics on the screen. Has to be called to at least assure the graphics
    * that they are ready for rendering. Called when the graphic position changes.
    *
    * @param proj com.bbn.openmap.proj.Projection
    * @return true
    */
   public abstract boolean generate(Projection proj);

   /**
    * Given a new projection, the grab points may need to be repositioned off the current position of the graphic.
    * Called when the projection changes. IMPORTANT! Set the GrabPoints for the graphic here.
    */
   public abstract void regenerate(Projection proj);

   public void repaintRender(Graphics g) {
      render(g);
   }

   /**
    */
   public abstract void render(Graphics g);

   public boolean isXorRendering() {
      return xorRendering;
   }

   /**
    * Set whether the painting will occur using XOR rendering. If false, the mapbean will be repainted on every
    * movement. Looks better, but you need to make sure the repaint burden on the mapbean doesn't slow the drawing down
    * too much.
    *
    * @param xorRendering if true, XOR rendering will be used. Otherwise, the old location won't be drawn.
    */
   public void setXorRendering(boolean xorRendering) {
      this.xorRendering = xorRendering;
   }

   /**
    * Set the current projection.
    */
   public void setProjection(Projection proj) {
      projection = proj;
      // This is important. In the EditableOMGraphics, the
      // GrabPoints are set when regenerate is called.
      regenerate(proj);
   }

   /**
    * Get the current projection.
    */
   public Projection getProjection() {
      return projection;
   }

   // Mouse Listener events
   // //////////////////////
   /**
    */
   public boolean mousePressed(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mousePressed()");
      }
      if (!mouseOnMap) {
         return false;
      }
      return stateMachine.getState().mousePressed(e);
   }

   /**
    */
   public boolean mouseReleased(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseReleased()");
      }
      if (!mouseOnMap) {
         return false;
      }
      return stateMachine.getState().mouseReleased(e);
   }

   /**
    */
   public boolean mouseClicked(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseClicked()");
      }
      if (!mouseOnMap) {
         return false;
      }
      return stateMachine.getState().mouseClicked(e);
   }
   boolean mouseOnMap = true;

   /**
    */
   public void mouseEntered(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseEntered()");
      }
      mouseOnMap = true;
      stateMachine.getState().mouseEntered(e);
   }

   /**
    */
   public void mouseExited(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseExited()");
      }
      mouseOnMap = false;
      stateMachine.getState().mouseExited(e);
   }

   // Mouse Motion Listener events
   // /////////////////////////////
   /**
    */
   public boolean mouseDragged(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseDragged()");
      }
      if (!mouseOnMap) {
         return false;
      }
      return stateMachine.getState().mouseDragged(e);
   }

   /**
    */
   public boolean mouseMoved(MouseEvent e) {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseMoved()");
      }
      if (!mouseOnMap) {
         return false;
      }
      return stateMachine.getState().mouseMoved(e);
   }

   /**
    */
   public void mouseMoved() {
      if (DEBUG_DETAIL) {
         Debug.output(getClass().getName() + ".mouseMoved()");
      }
      if (!mouseOnMap) {
         return;
      }
      stateMachine.getState().mouseMoved();
   }

   /**
    * Add a EOMGListener.
    *
    * @param l EOMGListener
    */
   public synchronized void addEOMGListener(EOMGListener l) {
      if (listeners == null) {
         listeners = new EOMGListenerSupport(this);
      }
      listeners.add(l);
   }

   /**
    * Remove a EOMGListener.
    *
    * @param l EOMGListener
    */
   public synchronized void removeEOMGListener(EOMGListener l) {
      if (listeners == null) {
         return;
      }
      listeners.remove(l);
   }

   /**
    * The method to call if you want to let listeners know that the state has changed. Usually called when a graphic is
    * selected or not, so that GUIs can be directed.
    */
   public void fireEvent(EOMGEvent event) {
      if (listeners != null) {
         listeners.fireEvent(event);
      }

      if (event.getStatus() == EOMGEvent.EOMG_UNDO) {
         updateCurrentState(null);
      }
   }

   /**
    * Create the event with a Cursor and/or message, and then fire it.
    *
    * @param cursor Cursor to be used.
    * @param message an instruction/error to be displayed to the user.
    * @param status the current status of the EditableOMGraphic.
    */
   public void fireEvent(Cursor cursor, String message, int status) {
      fireEvent(cursor, message, null, status);
   }

   /**
    * Create the event with the Cursor, message and/or MouseEvent.
    *
    * @param cursor Cursor to be used.
    * @param message an instruction/error to be displayed to the user.
    * @param mouseEvent where that caused the EOMGEvent. May be null.
    * @param status the current status of the EditableOMGraphic.
    */
   public void fireEvent(Cursor cursor, String message, MouseEvent mouseEvent, int status) {
      if (listeners != null) {
         EditableOMGraphic theSource = listeners.getEOMG();
         EOMGEvent event = new EOMGEvent(theSource, cursor, message, mouseEvent, status);
         fireEvent(event);
      }
   }

   /**
    * Create the event with no cursor change or message to be displayed.
    */
   public void fireEvent(int status) {
      fireEvent(null, null, null, status);
   }

   /**
    * If this EditableOMGraphic has parameters that can be manipulated that are independent of other EditableOMGraphic
    * types, then you can provide the widgets to control those parameters here. By default, this method returns null,
    * which indicates that you can extend this method to return a Component that controls parameters for the
    * EditableOMGraphic other than the GraphicAttribute parameters. Should return something like a toolbar, small.
    *
    * @return Component to control EOMG parameters, without the GraphicAttribute GUI.
    */
   public Component getGUI() {
      return getGUI(null);
   }

   /**
    * If this EditableOMGraphic has parameters that can be manipulated that are independent of other EditableOMGraphic
    * types, then you can provide the widgets to control those parameters here. By default, returns the
    * GraphicAttributes GUI widgets. If you don't want a GUI to appear when a widget is being created/edited, then don't
    * call this method from the EditableOMGraphic implementation, and return a null Component from getGUI.
    *
    * @param graphicAttributes the GraphicAttributes that could be used to get the GUI widget from to control those
    * parameters for this EOMG. The GraphicAttributes used to provide the GUI widget, but it doesn't anymore. Subclasses
    * can take this opportunity to reset the GraphicAttributes interface for a new OMGraphic.
    *
    * @return Component to use to control parameters for this EOMG, generally a JPanel with a toolbar.
    */
   public Component getGUI(GraphicAttributes graphicAttributes) {
      if (graphicAttributes != null) {
         graphicAttributes.setLineMenuAdditions(null);
         // Used to return the toolbar gui, now the color and line options provided on the right click menu.
         // return graphicAttributes.getGUI();
      }
      return null;
   }

   public Point2D getProjectionPoint(MouseEvent e) {

      Point2D pnt = null;
      if (e instanceof MapMouseEvent && ((MapMouseEvent) e).mapIsRotated()) {
         MapMouseEvent mme = (MapMouseEvent) e;
         pnt = mme.getProjectedLocation();
      }

      if (pnt == null) {
         pnt = new Point2D.Double(e.getX(), e.getY());
      }

      return pnt;
   }

   public boolean isMouseEventTouching(MouseEvent e) {
      Point2D pnt = getProjectionPoint(e);
      return getGraphic().distance(pnt.getX(), pnt.getY()) <= 2;
   }

   public boolean isMouseEventTouchingTheEdge(MouseEvent e) {
      Point2D pnt = getProjectionPoint(e);
      return getGraphic().distanceToEdge(pnt.getX(), pnt.getY()) <= 2;
   }

   /**
    * This method should be overwritten for each EditableOMGraphic to save the state of the current OMGraphic, in case
    * the user wants to revert to this state. Called from the updateCurrentState method.
    *
    * @param whatHappened String describing what got you here. You can leave this null if you just want to go with the default.
    * @return UndoEvent reflecting current state. May be null if undo isn't handled. Returning null is the default
    * action.
    */
   protected UndoEvent createUndoEventForCurrentState(String whatHappened) {
      if (whatHappened == null) {
         whatHappened = i18n.get(this.getClass(), "omgraphicUndoString", "Edit");
      }
      try {
         return new OMGraphicUndoEvent(this, whatHappened);
      } catch (MissingResourceException mre) {
         // Return null so event won't be registered.
         return null;
      }
   }

   /**
    * Called by anything that knows that the EOMG has arrived at a stable state that should be kept for Undo actions.
    *
    * @param whatHappened a description of was done to get to this state. If null, the EOMG will create a default string
    * to use.
    */
   public void updateCurrentState(String whatHappened) {
      UndoEvent undoEvent = createUndoEventForCurrentState(whatHappened);

      if (undoEvent != null && undoStack != null) {
         undoStack.setTheWayThingsAre(undoEvent);
      }
   }

   /**
    * @return the undoStack
    */
   public UndoStack getUndoStack() {
      return undoStack;
   }

   /**
    * @param undoStack the undoStack to set
    */
   public void setUndoStack(UndoStack undoStack) {
      this.undoStack = undoStack;
   }

   /**
    * A little flag to let the EOMG that a popup menu is up on the map. If the menu is up, and the menu is not clicked
    * on, we don't really want to deactivate the drawing tool right then - let a free click go by first, which will
    * dismiss the menu.
    *
    * @param popupIsUp
    */
   public void setPopupIsUp(boolean popupIsUp) {
      this.popupIsUp = popupIsUp;
   }

   public boolean isPopupIsUp() {
      return this.popupIsUp;
   }

   public JComponent createAttributePanel(GraphicAttributes graphicAttributes) {
//      JPanel palette = new JPanel();

//      GridBagLayout gridbag = new GridBagLayout();
//      GridBagConstraints c = new GridBagConstraints();
//      palette.setLayout(gridbag);

      JToolBar toolbar = new GridBagToolBar();

      if (graphicAttributes != null) {
         int orientation = graphicAttributes.getOrientation();
         toolbar.setOrientation(orientation);
//         if (orientation == SwingConstants.VERTICAL) {
//            c.gridwidth = GridBagConstraints.REMAINDER;
//         }
      }


//      gridbag.setConstraints(toolbar, c);
//      palette.add(toolbar);
      return toolbar;
   }

   /**
    * Generic undo event for basic OMGraphics.
    *
    * @author dietrick
    */
   public static class OMGraphicUndoEvent
           implements UndoEvent {

      protected EditableOMGraphic eomg;
      protected OMGraphic stateHolder;
      protected String description;

      public OMGraphicUndoEvent(EditableOMGraphic eomg, String description)
              throws MissingResourceException {
         this.eomg = eomg;
         this.description = description;

         OMGraphic omg = eomg.getGraphic();
         stateHolder = (OMGraphic) ComponentFactory.create(omg.getClass().getName());

         if (stateHolder != null) {
            // stateHolder = new OMPoly();
            stateHolder.restore(eomg.getGraphic());
         } else {
            throw new MissingResourceException(eomg.getClass().getName() + " can't provide UndoEvents", eomg.getClass().getName(), "");
         }
      }

      /*
       * (non-Javadoc)
       *
       * @see com.bbn.openmap.event.UndoEvent#getDescription()
       */
      public String getDescription() {
         return description;
      }

      /*
       * (non-Javadoc)
       *
       * @see com.bbn.openmap.event.UndoEvent#setState()
       */
      public void setState() {
         this.eomg.getGraphic().restore(stateHolder);
         setSubclassState();
         this.eomg.regenerate(this.eomg.getProjection());
         this.eomg.repaint();
      }

      /**
       * Called from setState before repaint() is called, so subclasses can update anything in their EditableOMGraphic
       * state for the restored OMGraphic.
       */
      protected void setSubclassState() {
         // noop, for subclasses to use so we don't waste a repaint.
      }

      /**
       * @return the stateHolder
       */
      public OMGraphic getStateHolder() {
         return stateHolder;
      }

      /**
       * @param stateHolder the stateHolder to set
       */
      public void setStateHolder(OMGraphic stateHolder) {
         this.stateHolder = stateHolder;
      }

      /**
       * @return the eomg
       */
      public EditableOMGraphic getEomg() {
         return eomg;
      }

      /**
       * @param description the description to set
       */
      public void setDescription(String description) {
         this.description = description;
      }
   }
}