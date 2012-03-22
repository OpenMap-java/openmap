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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/CSpecLayer.java,v $
// $RCSfile: CSpecLayer.java,v $
// $Revision: 1.9 $
// $Date: 2005/12/09 21:08:58 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

/*  Java Core  */
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ShortHolder;
import org.omg.CORBA.StringHolder;

import com.bbn.openmap.Environment;
import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.corba.CSpecialist.GraphicChange;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.Server;
import com.bbn.openmap.corba.CSpecialist.ServerHelper;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * CSpecLayer is a Layer which communicates to CORBA Specialists.
 * <P>
 * Properties:
 * <P>
 * 
 * <pre>
 * 
 *    # If you have an ior for the server:
 *    cspeclayermarker.ior= URL to ior
 *    # If you are using the Naming Service:
 *    cspeclayermarker.name= SERVER NAME
 *    # Static Arguments for the server, to be sent on every map request:
 *    cspeclayermarker.staticArgs= space separated arguments
 *    # If the network setup allows the server to contact the client (no firewall)
 *    cspeclayermarker.allowServerUpdates=true/false (false is default)
 * 
 * </pre>
 */
public class CSpecLayer
      extends OMGraphicHandlerLayer
      implements MapMouseListener {

   // private final static String[] debugTokens = { "debug.cspec" };

   /** The property specifying the IOR URL. */
   public static final String iorUrlProperty = "ior";
   public static final String namingProperty = "name";

   /** The property specifying the static arguments. */
   public static final String staticArgsProperty = "staticArgs";

   /**
    * The property to use for specifying whether the GraphicChange object should
    * be sent to the server. The server can use the GraphicChange object to
    * contact the client to notify it that updates are available. This should
    * only be true if the network setup allows it to be. Running the client
    * behind a firewall, taking with the server through a Gatekeeper, will not
    * allow the GraphicChange object to be set. You get a BOA instantiation
    * error.
    */
   public static final String serverUpdateProperty = "allowServerUpdates";
   /** IOR URL for the server. */
   protected URL iorURL = null;
   /** Name of the server. */
   protected String naming = null;

   /** Arguments passed in from the OverlayTable/properties file. */
   protected String staticArgs = null;

   /**
    * Arguments modified by the Layer, or set by the Bean, at runtime.
    * Historical, should use Properties instead.
    */
   protected String dynamicArgs = null;
   protected String clientID = Environment.generateUniqueString();

   protected UWidget[] widgets = null;
   protected transient CSpecPalette gui = null;
   protected transient Server specialist = null;

   protected ShortHolder selectDist = new ShortHolder();
   protected BooleanHolder wantAreaEvents = new BooleanHolder();
   protected GraphicChange notifyOnChange = null;
   protected MapGesture mapGesture = new MapGesture();

   /**
    * Used for the MapMouseListener interface, to track whether to listen to
    * mouse events, or not.
    */
   protected boolean acceptingEvents = false;
   /**
    * Used to track if a info line was sent, so that a clearing message can be
    * sent when it is no longer relevant.
    */
   protected boolean sentInfoLine = false;

   // all the dirty bits
   protected int dirtybits = 0;
   public final transient static int PALETTE_DIRTY = 0x1;
   public final transient static int PREMATURE_FINISH = 0x4;
   public final transient static int EXCEPTION = 0x8;
   public final transient static int DIRTYMASK = 0xFFFFFFFF;

   // new slots
   protected boolean showDialogs = Environment.getBoolean("com.bbn.openmap.ShowLayerMessages");

   /**
    * Default constructor, that sets the MapMouseListener for this layer to
    * itself.
    */
   public CSpecLayer() {
      handleGraphicChangeRequests(false);
      setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
   }

   /**
    * Sets whether the notifyOnChange object will actually be set to anything.
    * This object can be used to tell the CSpecLayer to go to the specialist
    * with a getRectangle. The Layer handles the creation of the object if this
    * is set to true. If you are working through a firewall, this might not be
    * allowed, especially if the client is behind the firewall.
    * 
    * @param setting if the object should be created or not.
    */
   public void handleGraphicChangeRequests(boolean setting) {
      if (setting) {
         if (notifyOnChange == null) {
            notifyOnChange = new JGraphicChange(this);
         }
      } else {
         notifyOnChange = null;
      }
   }

   /**
     * 
     */
   public void finalize() {
      if (Debug.debugging("cspec")) {
         Debug.output(getName() + "|CSpecLayer.finalize(): calling shutdown");
      }
      try {
         if (specialist != null)
            specialist.signoff(clientID);
         specialist = null;
      } catch (org.omg.CORBA.SystemException e) {
         System.err.println(getName() + "|CSpecLayer.finalize(): " + e);
      } catch (Throwable t) {
         System.err.println(getName() + "|CSpecLayer.finalize(): " + t);
      }
   }

   /**
    * Set the properties for the CSpecLayer.
    */
   public void setProperties(String prefix, java.util.Properties props) {

      super.setProperties(prefix, props);
      prefix = PropUtils.getScopedPropertyPrefix(prefix);

      String url = props.getProperty(prefix + iorUrlProperty);
      if (url != null) {
         try {
            setIorUrl(PropUtils.getResourceOrFileOrURL(null, url));
         } catch (MalformedURLException e) {
            throw new IllegalArgumentException("\"" + url + "\" is malformed.");
         }
      }

      // Get the naming context to get
      naming = props.getProperty(prefix + namingProperty);

      String staticArgValue = props.getProperty(prefix + staticArgsProperty);
      setStaticArgs(staticArgValue);

      handleGraphicChangeRequests(PropUtils.booleanFromProperties(props, prefix + serverUpdateProperty, notifyOnChange != null));
   }

   /**
    * Gets the argv for the layer from the pseudo-overlay-table. Expecting
    * <URL>&rest args.
    */
   public void setArgs(String argv[]) {
      int argc = argv.length;

      if (argc == 0) {
         // Do nothing.
         return;
      }

      String url = argv[0];
      StringBuffer argBuf = new StringBuffer();

      if (argc > 1) {
         // More arguments, append them into one string and
         // pass it off to setArgs.
         argBuf.append(argv[1]);
         for (int i = 2; i < argc; i++) {
            argBuf.append(" ").append(argv[i]);
         }
      }
      // dbg
      // Debug.output("----------------------------------------------");
      // dbg Debug.output("CSpecLayer " + getName() + ":");
      // dbg Debug.output("\tURL: " + url);
      // dbg Debug.output("\targs: " + argBuf);

      try {
         setIorUrl(new URL(url));
         if (Debug.debugging("cspec")) {
            Debug.output(getName() + "(CSpecLayer) using ior from " + url);
         }
      } catch (MalformedURLException e) {
         throw new IllegalArgumentException("\"" + url + "\"" + " is not a well formed URL");
      }

      setStaticArgs(argBuf.toString());
   }

   /**
    * get the specialist proxy.
    * 
    * @return Server specialist server or null if error.
    */
   public Server getSpecialist() {
      if (specialist == null) {
         initSpecialist();
      }
      return specialist;
   }

   /**
    * Bind to the specialist server.
    */
   private void initSpecialist() {
      String ior = null;
      org.omg.CORBA.Object object = null;

      com.bbn.openmap.util.corba.CORBASupport cs = new com.bbn.openmap.util.corba.CORBASupport();

      try {
         object = cs.readIOR(iorURL);
         specialist = ServerHelper.narrow(object);
      } catch (IOException ioe) {
         if (Debug.debugging("cspec")) {
            Debug.output(getName() + "(CSpecLayer).initSpecialist() IO Exception with ior: " + iorURL);
         }
         specialist = null;
         return;
      }

      if (specialist == null) {
         object = cs.resolveName(naming);

         if (object != null) {
            specialist = ServerHelper.narrow(object);
            if (Debug.debugging("cspec")) {
               Debug.output("Have a specialist:");
               Debug.output("*** Specialist Server: is a " + specialist.getClass().getName() + "\n" + specialist);
            }
         }
      }

      if (specialist == null) {
         if (Debug.debugging("cspec")) {
            System.err.println("CSpecLayer.initSpecialist: null specialist!\n  IOR=" + ior + "\n  Name = " + naming);
         }
      }
   }

   /**
    * Set the server, if you've taken special steps to create on, or want to
    * null out the current one to reset the connection.
    */
   public void setSpecialist(Server aSpecialist) {
      specialist = aSpecialist;
      if (specialist == null) {
         widgets = null;
         gui = null;
         setList(null);
      }
   }

   /**
    * Interface Layer method to get the dynamic args.
    * 
    * @return String args
    */
   public String getArgs() {
      return dynamicArgs;
   }

   /**
    * Method to set the dynamic args.
    * 
    * @param args String
    */
   public void setArgs(String args) {
      dynamicArgs = args;
   }

   /**
    * Interface Layer method to get the static args, which are usually set via
    * the openmap.properties file, or setProperties().
    */
   public String getStaticArgs() {
      return staticArgs;
   }

   /**
    * Interface Layer method to set the static args, which are usually set via
    * the openmap.properties file.
    */
   public void setStaticArgs(String args) {
      staticArgs = args;
   }

   public URL getIorUrl() {
      return iorURL;
   }

   public void setIorUrl(URL url) {
      iorURL = url;
   }

   /**
    * Perform the getRectangle() call on the specialist.
    * 
    * @param p Projection
    * @return UGraphic[] graphic list or null if error
    */
   protected UGraphic[] getSpecGraphics(Projection p) {
      CProjection cproj;
      LLPoint ll1, ll2;
      StringHolder dynamicArgsHolder;
      UGraphic[] graphics = null;
      Server spec = getSpecialist();
      if (Debug.debugging("cspec"))
         Debug.output(getName() + "|CSpecLayer.getSpecGraphics()");

      Point2D center = p.getCenter();
      cproj =
            new CProjection(MakeProjection.getProjectionType(p), new LLPoint((float) center.getY(), (float) center.getX()),
                            (short) p.getHeight(), (short) p.getWidth(), (int) p.getScale());

      // lat-lon "box", (depends on the projection)
      Point2D ul = p.getUpperLeft();
      Point2D lr = p.getLowerRight();

      ll1 = new LLPoint((float) ul.getY(), (float) ul.getX());
      ll2 = new LLPoint((float) lr.getY(), (float) lr.getX());

      // check for cancellation
      if (isCancelled()) {
         dirtybits |= PREMATURE_FINISH;
         if (Debug.debugging("cspec"))
            Debug.output(getName() + "|CSpecLayer.getSpecGraphics(): aborted.");
         return null;
      }
      // check for null specialist
      if (spec == null) {
         if (Debug.debugging("cspec")) {
            System.err.println(getName() + "|CSpecLayer.getSpecGraphics(): null specialist!");
         }
         return null;
      }

      try {
         // Keep the gestures up-to-date
         mapGesture.setProjection(p);

         // Static Args can't go out null....
         String staticArguments = getStaticArgs();
         if (staticArguments == null) {
            staticArguments = "";
            setStaticArgs(staticArguments);
         }

         // neither can dynamic args
         // Layer.getArgs() was deprecated and removed
         dynamicArgsHolder = new StringHolder(getArgs());
         if (dynamicArgsHolder.value == null) {
            dynamicArgsHolder.value = "";
         }

         // call getRectangle();
         if (Debug.debugging("cspec")) {
            Debug.output(getName() + "|CSpecLayer.getSpecGraphics():" + " calling getRectangle with projection: " + p + " ul=" + ul
                  + " lr=" + lr + " staticArgs=\"" + staticArguments + "\"" + " dynamicArgs=\"" + dynamicArgsHolder.value + "\""
                  + " notifyOnChange=\"" + notifyOnChange + "\"" + " clientID=" + clientID);
         }
         long start = System.currentTimeMillis();

         if (Debug.debugging("cspecdetail")) {
            Debug.output("*** Specialist Server: is a " + spec.getClass().getName() + "\n" + spec);
         }

         graphics =
               spec.getRectangle(cproj, ll1, ll2, staticArguments, dynamicArgsHolder, selectDist, wantAreaEvents, notifyOnChange,
                                 clientID);
         long stop = System.currentTimeMillis();

         if (Debug.debugging("cspec")) {
            Debug.output(getName() + "|CSpecLayer.getSpecGraphics(): got " + graphics.length + " graphics in "
                  + ((stop - start) / 1000d) + " seconds.");
         }
      } catch (org.omg.CORBA.SystemException e) {
         dirtybits |= EXCEPTION;
         // don't freak out if we were only interrupted...
         if (e.toString().indexOf("InterruptedIOException") != -1) {
            System.err.println(getName() + "|CSpecLayer.getSpecGraphics(): " + "getRectangle() call interrupted!");
         } else {
            System.err.println(getName() + "|CSpecLayer.getSpecGraphics(): " + "Caught CORBA exception: " + e);
            System.err.println(getName() + "|CSpecLayer.getSpecGraphics(): " + "Exception class: " + e.getClass().getName());
            e.printStackTrace();
         }

         // dontcha just love CORBA? reinit later
         setSpecialist(null);
         if (showDialogs)
            postCORBAErrorMsg("CORBA Exception while getting graphics from\n" + getName() + " specialist:\n"
                  + e.getClass().getName());
      }
      return graphics;
   }

   /**
    * Prepares the graphics for the layer.
    * <p>
    * Occasionally it is necessary to abort a prepare call. When this happens,
    * the doPrepare() call will set the cancel bit on the SwingWorker. The
    * worker will get restarted after it finishes doing its cleanup.
    * 
    * @return a JGraphicList from the server.
    */
   public synchronized OMGraphicList prepare() {
      JGraphicList emptyList = new JGraphicList();
      if (isCancelled()) {
         dirtybits |= PREMATURE_FINISH;
         if (Debug.debugging("basic")) {
            Debug.output(getName() + "|CSpecLayer.prepare(): aborted.");
         }
         return emptyList;
      }

      if (Debug.debugging("basic")) {
         Debug.output(getName() + "|CSpecLayer.prepare(): doing it");
      }

      dirtybits = 0;// reset the dirty bits

      // Now we're going to shut off event processing. The only
      // thing that turns them on again is finishing successfully.
      setAcceptingEvents(false);

      Projection projection = getProjection();

      // get the graphics from the specialist
      UGraphic[] specGraphics = getSpecGraphics(projection);
      if (isCancelled()) {
         dirtybits |= PREMATURE_FINISH;
         if (Debug.debugging("basic"))
            Debug.output(getName() + "|CSpecLayer.prepare(): " + "aborted during/after getRectangle().");
         return emptyList;
      }

      if (specGraphics == null) {
         return emptyList;
      }

      // process the graphics
      long start = System.currentTimeMillis();
      JGraphicList graphics = createGraphicsList(specGraphics, projection);
      long stop = System.currentTimeMillis();
      if (Debug.debugging("cspec")) {
         Debug.output(getName() + "|CSpecLayer.prepare(): generated " + specGraphics.length + " graphics in "
               + ((stop - start) / 1000d) + " seconds.");
      }

      if (isCancelled()) {
         dirtybits |= PREMATURE_FINISH;
         if (Debug.debugging("basic")) {
            Debug.output(getName() + "|CSpecLayer.prepare(): " + "aborted while generating graphics.");
         }
         return emptyList;
      }

      // safe quit
      if (Debug.debugging("basic")) {
         Debug.output(getName() + "|CSpecLayer.prepare(): finished preparing " + graphics.size() + " graphics");
      }
      setAcceptingEvents(true);
      return graphics;
   }

   /**
    * Create an JGraphicList based on UGraphics and a Projection.
    * <p>
    * This is public static to enable out-of-package delegation.
    * <p>
    * 
    * @param uGraphics UGraphic[]
    * @param proj Projection
    * @return JGraphicList
    */
   public static JGraphicList createGraphicsList(UGraphic[] uGraphics, Projection proj) {

      int nGraphics = uGraphics.length;
      JGraphicList graphics = new JGraphicList(nGraphics);
      graphics.setTraverseMode(OMGraphicList.LAST_ADDED_ON_TOP);

      // generate a JGraphic for each CSpecialist graphic and store
      // it
      for (int i = 0; i < nGraphics; i++) {
         switch (uGraphics[i].discriminator().value()) {
            case GraphicType._GT_Poly:
               JPoly jpoly = new JPoly(uGraphics[i].epoly());
               jpoly.generate(proj);
               graphics.add(jpoly);
               break;
            case GraphicType._GT_Raster:
               JRaster jraster = new JRaster(uGraphics[i].eras());
               jraster.generate(proj);
               graphics.add(jraster);
               break;
            case GraphicType._GT_Bitmap:
               JBitmap jbitmap = new JBitmap(uGraphics[i].ebit());
               jbitmap.generate(proj);
               graphics.add(jbitmap);
               break;
            case GraphicType._GT_Text:
               JText jtext = new JText(uGraphics[i].etext());
               jtext.generate(proj);
               graphics.add(jtext);
               break;
            case GraphicType._GT_Line:
               JLine jline = new JLine(uGraphics[i].eline());
               jline.generate(proj);
               graphics.add(jline);
               break;
            case GraphicType._GT_UnitSymbol:
               JUnit junit = new JUnit(uGraphics[i].eunit());
               junit.generate(proj);
               graphics.add(junit);
               break;
            case GraphicType._GT_2525Symbol:
               J2525 j2525 = new J2525(uGraphics[i].e2525());
               j2525.generate(proj);
               graphics.add(j2525);
               break;
            case GraphicType._GT_Rectangle:
               JRect jrect = new JRect(uGraphics[i].erect());
               jrect.generate(proj);
               graphics.add(jrect);
               break;
            case GraphicType._GT_Circle:
               JCircle jcircle = new JCircle(uGraphics[i].ecirc());
               jcircle.generate(proj);
               graphics.add(jcircle);
               break;
            case GraphicType._GT_NewGraphic:
            case GraphicType._GT_ReorderGraphic:
            default:
               System.err.println("JGraphic.generateGraphics: " + "ignoring invalid type");
               break;
         }
      }
      return graphics;
   }

   /**
    * Gets the palette associated with the layer.
    * <p>
    * 
    * @return Component or null
    */
   public Component getGUI() {
      if (specialist == null)
         initSpecialist();
      if (specialist == null) {
         if (Debug.debugging("cspec"))
            Debug.output(getName() + "|CSpecLayer.getGUI(): initSpecialist() unsuccessful!");
         return null;
      }

      try {
         if (widgets == null) {
            org.omg.CORBA.StringHolder paletteDynamicArgs = new org.omg.CORBA.StringHolder(getArgs());

            if (paletteDynamicArgs.value == null) {
               paletteDynamicArgs.value = "";
            }

            // Static Args can't go out null....
            String staticArguments = getStaticArgs();
            if (staticArguments == null) {
               staticArguments = "";
               setStaticArgs(staticArguments);
            }

            if (Debug.debugging("cspec")) {
               Debug.output(getName() + "|CSpecLayer.getGUI(): calling getPaletteConfig(" + staticArguments + ","
                     + paletteDynamicArgs.value + "," + clientID + ")");
            }

            try {

               widgets = specialist.getPaletteConfig(null/* widgetChange */, staticArguments, paletteDynamicArgs, clientID);

            } catch (org.omg.CORBA.SystemException e) {
               System.err.println(getName() + "|CSpecLayer.getGUI(): " + e);
               e.printStackTrace();
               setSpecialist(null);
               if (showDialogs) {
                  postCORBAErrorMsg("CORBA Exception while getting palette from\n" + getName() + " specialist:\n"
                        + e.getClass().getName());
               }
            }
            if (widgets == null || widgets.length == 0) {
               gui = null;
            } else {
               gui = new CSpecPalette(widgets, clientID, this);
            }
         }
      } catch (OutOfMemoryError e) {
         setSpecialist(null);
         System.err.println(getName() + "|CSpecLayer.getGUI(): " + e);
         if (showDialogs) {
            postMemoryErrorMsg("OutOfMemory while getting palette from\n" + getName() + " specialist.");
         }
      } catch (Throwable t) {
         setSpecialist(null);
         System.err.println(getName() + "|CSpecLayer.getGUI(): " + t);
         t.printStackTrace();
         if (showDialogs) {
            postException("Exception while getting palette from\n" + getName() + " specialist:\n" + t.getClass().getName());
         }
      }

      return gui;
   }

   /**
    * A palette button has changed (we should indeed prepare when we get the
    * call).
    * 
    * @param paletteIsDirty true or false
    */
   protected void setPaletteIsDirty(boolean paletteIsDirty) {
      if (paletteIsDirty) {
         dirtybits |= PALETTE_DIRTY;
      }
   }

   /**
    * Destroy the current palette stuff.
    */
   protected void forgetPalette() {
      widgets = null;
      gui = null;
   }

   /**
    * Used to set whether the MapMouseListener is listening for events or
    * ignoring them.
    * 
    * @param listening true if the listener should process mouse events.
    */
   public void setAcceptingEvents(boolean listening) {
      acceptingEvents = listening;
   }

   /**
    * Used to tell if the listener is accepting mouse events for processing.
    * 
    * @return true if the listener is processing mouse events.
    */
   public boolean isAcceptingEvents() {
      return acceptingEvents;
   }

   // Mouse Listener events
   // //////////////////////

   /**
    * Returns the MapMouseListener object (this object) that handles the mouse
    * events.
    * 
    * @return MapMouseListener this
    */
   public MapMouseListener getMapMouseListener() {
      return this;
   }

   public String[] getMouseModeServiceList() {
      String[] ret = new String[1];
      ret[0] = SelectMouseMode.modeID;
      return ret;
   }

   /**
    * Handle a mouse button being pressed.
    * 
    * @param e MouseListener MouseEvent to handle.
    * @return true if the listener was able to process the event.
    */
   public boolean mousePressed(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         return handleGesture(e, MapGesture.clickEvent, true);
      }
      return false;
   }

   /**
    * Handle a mouse button being released.
    * 
    * @param e MouseListener MouseEvent to handle.
    * @return true if the listener was able to process the event.
    */
   public boolean mouseReleased(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         return handleGesture(e, MapGesture.clickEvent, false);
      }
      return false;
   }

   /**
    * Handle a mouse button being clicked - pressed and released.
    * 
    * @param e MouseListener MouseEvent to handle.
    * @return true if the listener was able to process the event.
    */
   public boolean mouseClicked(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         return handleGesture(e, MapGesture.clickEvent, false);
      }
      return false;
   }

   /**
    * Handle a mouse cursor entering a window or area.
    * 
    * @param e MouseListener MouseEvent to handle.
    */
   public void mouseEntered(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         handleGesture(e, MapGesture.motionEvent, false);
      }
   }

   /**
    * Handle a mouse cursor leaving a window or area.
    * 
    * @param e MouseListener MouseEvent to handle.
    */
   public void mouseExited(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         handleGesture(e, MapGesture.motionEvent, false);
      }
   }

   // Mouse Motion Listener events
   // /////////////////////////////

   /**
    * Handle a mouse button being pressed while the mouse cursor is moving.
    * 
    * @param e MouseMotionListener MouseEvent to handle.
    * @return true if the listener was able to process the event.
    */
   public boolean mouseDragged(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         return handleGesture(e, MapGesture.motionEvent, true);
      }
      return false;
   }

   /**
    * Handle a mouse cursor moving without the button being pressed.
    * 
    * @param e MouseListener MouseEvent to handle.
    * @return true if the listener was able to process the event.
    */
   public boolean mouseMoved(MouseEvent e) {
      if (acceptingEvents && specialist != null) {
         return handleGesture(e, MapGesture.motionEvent, false);
      }
      return false;
   }

   /**
    * Handle a mouse cursor moving without the button being pressed, for events
    * that have been used by something else.
    */
   public void mouseMoved() {
      if (acceptingEvents && specialist != null) {
         handleGesture(null, MapGesture.motionEvent, false);
      }
   }

   /**
    * Relays user gestures to the specialist or to the mousable objects of the
    * CSpecLayer. The function finds the closest object and then its comp object
    * all by itself.
    * <p>
    * 
    * @param evt MouseEvent
    * @param MouseDown true if the mouse button is down
    * @return true if gesture was consumed, false if not.
    */
   public boolean handleGesture(MouseEvent evt, int eventType, boolean MouseDown) {

      boolean got_info = false;
      boolean got_the_stuff = false;
      boolean updated_graphics = false;
      OMGraphic moused = null;
      JGraphicList jjGraphics = (JGraphicList) getList();

      // Do this, so when there was a one-liner about a graphic (or
      // something) sent, and now there isn't a graphic associated
      // with the layer, to reset the message window to nothing, so
      // stale info just doesn't hang out.
      if (sentInfoLine) {
         fireRequestInfoLine("");
         sentInfoLine = false;
      }

      if (!isAcceptingEvents() || (jjGraphics == null)) {
         return false;
      }

      // This will need to change, when we figure out who to make a
      // Cspecialist capabile of handling a null event, so signify a
      // reset...
      if (evt == null) {
         if (Debug.debugging("cspec")) {
            Debug.output(getName() + "|CSpecLayer.handleGesture(): null evt!");
         }
         return false;// didn't consume gesture
      }

      try {
         mapGesture.setMouseEvent(evt, eventType, MouseDown);
         moused = jjGraphics.findClosest(evt.getX(), evt.getY(), selectDist.value);

         com.bbn.openmap.corba.CSpecialist.ActionUnion[] action = null;

         switch (mapGesture.getMode()) {
            case (short) MapGesture.Raw:
               // send the gesture to the comp object or the
               // specialist
               // if it wants area events.
               if (moused != null && ((JObjectHolder) moused).getObject().comp != null) {
                  action = ((JObjectHolder) moused).getObject().comp.sendGesture(JGraphic.constructGesture(mapGesture), clientID);
               } else if (specialist != null) {
                  action = wantAreaEvents.value ? specialist.sendGesture(JGraphic.constructGesture(mapGesture), clientID) : null;
                  if (action == null) {
                     if (Debug.debugging("cspec"))
                        Debug.output(getName() + "|CSpecLayer.handleGesture(): null action!");
                     return false; // didn't consume gesture
                  }
                  if (action.length == 0) {
                     return false; // didn't consume gesture
                  }
               }
               if (action == null) {
                  if (Debug.debugging("cspec")) {
                     System.err.println(getName() + "|CSpecLayer.handleGesture(): null action!");
                  }
                  return false; // didn't consume gesture
               }
               break;
            case (short) MapGesture.Cooked:
            default:
               System.err.println("CSpecLayer|" + getName() + "|handleGesture() - cooked modes not supported");
               break;
         }

         if (action == null) {
            return false;
         }

         // parse the action sequence, ignore duplicate action
         // directives
         mapGesture.actionType = new int[action.length];
         for (int i = 0; i < action.length; i++) {
            switch (action[i].discriminator().value()) {

               case MapGesture.NoAction:
                  break;
               case MapGesture.UpdateGraphics:
                  updated_graphics = true;
                  // now update the specified graphics
                  updateGraphics(action[i].ginfo());
                  break;
               case MapGesture.InfoText:
                  if (!got_info) { // can only have one instance
                     if (Debug.debugging("cspec")) {
                        Debug.output("CSpecLayer|" + getName() + "|handleGesture(): Requesting Info Text " + action[i].itext());
                     }
                     fireRequestInfoLine(action[i].itext());
                     sentInfoLine = true;
                     got_info = true;
                  }
                  break;
               case MapGesture.PlainText:
                  if (!got_the_stuff) {
                     if (Debug.debugging("cspec")) {
                        Debug.output("CSpecLayer|" + getName() + "|handleGesture(): Requesting Plain Text " + action[i].ptext());
                     }
                     fireRequestBrowserContent(action[i].ptext());
                     got_the_stuff = true;
                  }
                  break;
               case MapGesture.HTMLText:
                  if (!got_the_stuff) {
                     if (Debug.debugging("cspec")) {
                        Debug.output("CSpecLayer|" + getName() + "|handleGesture(): Requesting HTML Text " + action[i].htext());
                     }
                     fireRequestBrowserContent(action[i].htext());
                     got_the_stuff = true;
                  }
                  break;
               case MapGesture.URL:
                  if (!got_the_stuff) {
                     if (Debug.debugging("cspec")) {
                        Debug.output("CSpecLayer|" + getName() + "|handleGesture(): Requesting URL " + action[i].url());
                     }
                     fireRequestURL(action[i].url());
                     got_the_stuff = true;
                  }
                  break;
               case MapGesture.UpdatePalette:
               default:
                  System.err.println("CSpecLayer|" + getName() + "|handleGesture(): invalid ActionSeq");
                  break;
            }
         }
      } catch (org.omg.CORBA.SystemException e) {
         System.err.println(getName() + "|CSpecLayer.handleGesture(): " + e);
         if (showDialogs) {
            postCORBAErrorMsg("CORBA Exception while gesturing on\n" + getName() + " specialist:\n" + e.getClass().getName());
         }
         return false;
      } catch (OutOfMemoryError e) {
         setSpecialist(null);
         if (showDialogs) {
            postMemoryErrorMsg("OutOfMemory while gesturing on\n" + getName() + " specialist.");
         }
         return false;
      } catch (Throwable t) {
         if (showDialogs) {
            postException("Exception while gesturing on\n" + getName() + " specialist:\n" + t.getClass().getName());
         }
         t.printStackTrace();
         return false;
      }

      if (updated_graphics) {
         repaint();
      }
      return true;// consumed the gesture
   }

   /**
    * Changes attributes of existing graphics, or adds new graphics, or reorders
    * graphics.
    * <p>
    * 
    * @param updateRec com.bbn.openmap.corba.CSpecialist.UpdateRecord[]
    */
   protected void updateGraphics(com.bbn.openmap.corba.CSpecialist.UpdateRecord[] updateRec) {

      JGraphicList jGraphics = (JGraphicList) getList();
      Projection projection = getProjection();

      com.bbn.openmap.corba.CSpecialist.UpdateGraphic upgraphic = null;
      // parse updateRec (an array of UpdateRecord)
      for (int i = 0; i < updateRec.length; i++) {
         String gID = updateRec[i].gID; // get the graphic ID

         // parse the sequence of updates to perform on the
         // graphic. You need to do this because the types of
         // changes that can be made to each object can be part of
         // the specific object, like _GT_Bitmap (location, bits,
         // height/width), or part of the _GT_Graphic
         // (color/stipple changes), or _GT_ReorderGraphic, or
         // whatever.
         for (int j = 0; j < updateRec[i].objectUpdates.length; j++) {
            upgraphic = updateRec[i].objectUpdates[j];

            // determine the type of graphic update
            switch (upgraphic.discriminator().value()) {

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Graphic:
                  JObjectHolder graphic = (JObjectHolder) jGraphics.getOMGraphicWithId(gID);
                  if (graphic != null) {
                     graphic.update(upgraphic.gf_update());
                     ((OMGraphic) graphic).regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Bitmap:
                  JBitmap bitmap = (JBitmap) jGraphics.getOMGraphicWithId(gID);
                  if (bitmap != null) {
                     bitmap.update(upgraphic.bf_update());
                     bitmap.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Text:
                  JText text = (JText) jGraphics.getOMGraphicWithId(gID);
                  if (text != null) {
                     text.update(upgraphic.tf_update());
                     text.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Poly:
                  JPoly poly = (JPoly) jGraphics.getOMGraphicWithId(gID);
                  if (poly != null) {
                     poly.update(upgraphic.pf_update());
                     poly.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Line:
                  JLine line = (JLine) jGraphics.getOMGraphicWithId(gID);
                  if (line != null) {
                     line.update(upgraphic.lf_update());
                     line.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Rectangle:
                  JRect rect = (JRect) jGraphics.getOMGraphicWithId(gID);
                  if (rect != null) {
                     rect.update(upgraphic.rf_update());
                     rect.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Raster:
                  JRaster raster = (JRaster) jGraphics.getOMGraphicWithId(gID);
                  if (raster != null) {
                     raster.update(upgraphic.rasf_update());
                     raster.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_Circle:
                  JCircle circ = (JCircle) jGraphics.getOMGraphicWithId(gID);
                  if (circ != null) {
                     circ.update(upgraphic.cf_update());
                     circ.regenerate(projection);
                  }
                  break;

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_UnitSymbol:
                  JUnit unitsymbol = (JUnit) jGraphics.getOMGraphicWithId(gID);
                  if (unitsymbol != null) {
                     unitsymbol.update(upgraphic.usf_update());
                     unitsymbol.regenerate(projection);
                  }
                  break;

               // Uncomment when implemented!!!!

               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_2525Symbol:
               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_ForceArrow:
               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_NewGraphic:
               case com.bbn.openmap.corba.CSpecialist.GraphicPackage.GraphicType._GT_ReorderGraphic:
                  System.err.println("CSpecLayer|" + getName() + "|updateGraphics: Graphics Update Type not implemented.");
                  break;// HACK - unimplemented

               // unknown update
               default:
                  System.err.println("CSpecLayer|" + getName() + "|updateGraphics: ignoring weird update");
                  break;
            }
         }
      }
   }

   /**
    * Check if layer will show error dialogs.
    * 
    * @return boolean
    */
   public boolean getShowDialogs() {
      return showDialogs;
   }

   /**
    * Set showDialogs behavior.
    * 
    * @param show show dialog popups?
    */
   public void setShowDialogs(boolean show) {
      showDialogs = show;
   }

   /**
     * 
     */
   protected void postMemoryErrorMsg(String msg) {
      fireRequestMessage(new InfoDisplayEvent(this, msg));
   }

   /**
     * 
     */
   protected void postCORBAErrorMsg(String msg) {
      fireRequestMessage(new InfoDisplayEvent(this, msg));
   }

   /**
     * 
     */
   protected void postException(String msg) {
      fireRequestMessage(new InfoDisplayEvent(this, msg));
   }

   /**
    * Free up memory after being removed from the Map
    */
   public void removed(java.awt.Container cont) {

      if (Debug.debugging("cspec")) {
         Debug.output(getName() + "CSpecLayer.removed(): Nullifying graphics");
      }

      if (specialist != null) {
         specialist.signoff(clientID);
      }

      setSpecialist(null);
   }
}