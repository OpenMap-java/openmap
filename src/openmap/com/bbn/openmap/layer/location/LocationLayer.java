// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/location/LocationLayer.java,v $
// $RCSfile: LocationLayer.java,v $
// $Revision: 1.4 $
// $Date: 2004/01/26 18:18:09 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.location;


/*  Java Core  */
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;

/* Openmap */
import com.bbn.openmap.*;
import com.bbn.openmap.util.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.*;
import com.bbn.openmap.layer.util.*;
import com.bbn.openmap.omGraphics.*;

/**
 * The LocationLayer is a layer that displays graphics supplied by
 * LocationHandlers.  When the layer receives a new projection, it
 * goes to each LocationHandler and asks it for additions to the
 * layer's graphic list.  The LocationHandlers maintain the graphics,
 * and the layer maintains the overall list.
 * 
 * The whole idea behind locations is that there are geographic places
 * that are to be marked with a graphic, and/or a text label.  The
 * location handler handles the interface with the source and type of
 * location to be displayed, and the LocationLayer deals with all the
 * locations in a generic way.  The LocationLayer is capable of using
 * more than one LocationHandler. <P>
 *
 * As a side note, a Link is nothing more than a connection between
 * Locations, and is an extension of the Location Class.  They have a
 * graphic representing the link, an optional label, and an extra set
 * of location coordinates. <P>
 *
 * The layer responds to gestures with pop-up menus.  Which menu appears
 * depends if the gesture affects a graphic. <P>
 *
 * The properties for this layer are:<P>
 * <pre>
 * ####################################
 * # Properties for LocationLayer
 * # Use the DeclutterMatrix to declutter the labels.
 * locationlayer.useDeclutter=false
 * # Which declutter matrix class to use.
 * locationlayer.declutterMatrix=com.bbn.openmap.layer.DeclutterMatrix
 * # Let the DeclutterMatrix have labels that run off the edge of the map.
 * locationlayer.allowPartials=true
 * # The list of location handler prefixes - each prefix should then
 * # be used to further define the location handler properties.
 * locationlayer.locationHandlers=handler1 handler2
 * # Then come the handler properties...
 * # At the least, each handler should have a .class property
 * handler1.class=<handler classname>
 * # plus any other properties handler1 needs - check the handler1 documentation.
 * #################################### 
 * </pre> 
 */
public class LocationLayer extends Layer implements MapMouseListener {

    /** The declutter matrix to use, if desired. */
    protected DeclutterMatrix declutterMatrix = null;
    /** Flag to use declutter matrix or not. */
    protected boolean useDeclutterMatrix = false;
    /** Flag to let objects appear partially off the edges of the map,
     *  when decluttering through the decluterr matrix. */
    protected boolean allowPartials = true;
    /** The graphic list of objects to draw. */
    protected Vector omGraphics;
    /** Handlers load the data, and manage it for the layer. */
    protected LocationHandler[] dataHandlers;
    /** Pretty names for the handlers, for GUIs and such. */
    protected String[] dataHandlerNames;

    /////////////////////
    // Variables to manage the gesturing mechanisms

    /** Used for recentering commands off the pop-up menu. */
    protected MapBean map;
    /** What pops up if someone clicks on the background. The handler
     *  is responsible for suppling the pop-up menu when one of its
     *  objects is selected.*/
    protected LocationPopupMenu backgroundMenu;
    /** What pops up if someone clicks on a location. */
    protected LocationPopupMenu locMenu;
    
    static final public String recenter = "Re-center map";
    static final public String cancel = "Cancel";

    public static final String UseDeclutterMatrixProperty = "useDeclutter";
    public static final String DeclutterMatrixClassProperty = "declutterMatrix";
    public static final String AllowPartialsProperty = "allowPartials";
    public static final String LocationHandlerListProperty = "locationHandlers";

    /**
     * The swing worker that goes off in it's own thread to get
     * graphics.
     */
    protected LocationWorker currentWorker;
    /**
     * Set when the projection has changed while a swing worker is
     * gathering graphics, and we want him to stop early. 
     */
    protected boolean cancelled = false;

    /**
     * Since we can't have the main thread taking up the time to
     * create images, we use this worker thread to do it.
     */
    class LocationWorker extends SwingWorker {
        /** Constructor used to create a worker thread. */
        public LocationWorker() {
            super();
        }

        /**
         * Compute the value to be returned by the <code>get</code>
         * method.  
         */
        public Object construct() {
            if (Debug.debugging("location")) {
                Debug.output(getName()+
                             "|LocationWorker.construct()");
            }
            fireStatusUpdate(LayerStatusEvent.START_WORKING);
            try {
                return prepare();
            } catch (OutOfMemoryError e) {
                String msg = getName() + 
                    "|LocationLayer.LocationWorker.construct(): " + e;
                Debug.error(msg);
                e.printStackTrace();
                fireRequestMessage(new InfoDisplayEvent(this, msg));
                fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
                return null;
            }
        }

        /**
         * Called on the event dispatching thread (not on the worker
         * thread) after the <code>construct</code> method has
         * returned.  
         */
        public void finished() {
            workerComplete(this);
            fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
        }
    }

    /** 
     * The default constructor for the Layer.  All of the attributes
     * are set to their default values.
     */
    public LocationLayer() {}

    /** 
     * The properties and prefix are managed and decoded here, for
     * the standard uses of the LocationLayer.
     *
     * @param prefix string prefix used in the properties file for this layer.
     * @param properties the properties set in the properties file.  
     */
    public void setProperties(String prefix,
                              Properties properties) {
        super.setProperties(prefix, properties);
        String realPrefix = "";

        if (prefix != null) {
            realPrefix = prefix + ".";
        }

        
        setLocationHandlers(realPrefix, properties);
        declutterMatrix = (DeclutterMatrix) LayerUtils.objectFromProperties(properties, realPrefix + DeclutterMatrixClassProperty);
        allowPartials = LayerUtils.booleanFromProperties(properties, realPrefix + AllowPartialsProperty, true);

        if (declutterMatrix != null) {
            useDeclutterMatrix = LayerUtils.booleanFromProperties(properties, realPrefix + UseDeclutterMatrixProperty, useDeclutterMatrix);
            declutterMatrix.setAllowPartials(allowPartials);
            Debug.message("location", "LocationLayer: Found DeclutterMatrix to use");
//          declutterMatrix.setXInterval(3);
//          declutterMatrix.setYInterval(3);
        } else {
            useDeclutterMatrix = false;
        }
    }

    /** 
     * Sets the current graphics list to the given list.
     *
     * @param aList a vector of OMGraphics 
     */
    public synchronized void setGraphicList(Vector aList) {
        omGraphics = aList;
    }

    /** 
     * Retrieves a vector of the current graphics list.  
     *
     * @return vector of OMGraphics.
     */
    public synchronized Vector getGraphicList() {
        return omGraphics;
    }

    public void setDeclutterMatrix(DeclutterMatrix dm) {
        declutterMatrix = dm;
    }

    public DeclutterMatrix getDeclutterMatrix() {
        return declutterMatrix;
    }

    public void setUseDeclutterMatrix(boolean set) {
        useDeclutterMatrix = set;
    }

    public boolean getUseDeclutterMatrix() {
        return useDeclutterMatrix;
    }

    /** 
     * Used to set the cancelled flag in the layer.  The swing worker
     * checks this once in a while to see if the projection has
     * changed since it started working.  If this is set to true, the
     * swing worker quits when it is safe. 
     */
    public synchronized void setCancelled(boolean set) {
        cancelled = set;
    }

    /** Check to see if the cancelled flag has been set. */
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    public synchronized MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Tell the location handlers to reload their data from their
     * sources.  If you want these changes to appear on the map, you
     * should call doPrepare() after this call.
     */
    public void reloadData() {
        if (dataHandlers != null) {
            for (int i = 0; i < dataHandlers.length; i++) {
                dataHandlers[i].reloadData();
            }
        }
    }

    /** 
     * Implementing the ProjectionPainter interface.
     */
    public synchronized void renderDataForProjection(Projection proj, java.awt.Graphics g) {
        if (proj == null) {
            Debug.error("LocationLayer.renderDataForProjection: null projection!");
            return;
        } else if (!proj.equals(getProjection())) {
            setProjection(proj.makeClone());
            setGraphicList(prepare());
        }
        paint(g);
    }

    /** 
     * The projectionListener interface method that lets the Layer
     * know when the projection has changes, and therefore new graphics
     * have to created /supplied for the screen.
     *
     * @param e The projection event, most likely fired from a map bean.
     */
    public void projectionChanged(ProjectionEvent e) {
        if (Debug.debugging("basic")) {
            Debug.output(getName()+"|LocationLayer.projectionChanged()");
        }
        
        if (setProjection(e) == null) {
            // Nothing to do, already have it and have acted on it...
            repaint();
            return;
        }
        setGraphicList(null);
        
        // If there isn't a worker thread working on this already,
        // create a thread that will do the real work. If there is
        // a thread working on this, then set the cancelled flag
        // in the layer.                
        
        doPrepare();
    }

    /**  
     * The LocationWorker calls this method on the layer when it is
     * done working.  If the calling worker is not the same as the
     * "current" worker, then a new worker is created.
     *
     * @param worker the worker that has the graphics.
     */
    protected synchronized void workerComplete(LocationWorker worker) {
        if (!isCancelled()) {
            currentWorker = null;
            setGraphicList((Vector)worker.get());
            repaint();
        }
        else{
            setCancelled(false);
            currentWorker = new LocationWorker();
            currentWorker.execute();
        }
    }

    /**
     * A method that will launch a Worker to fetch the data.  This is
     * the method to call if you want the layer to refresh it's
     * graphics from the location handlers.  The layer will repaint
     * itself automatically.
     */
    public void doPrepare() {
        // If there isn't a worker thread working on a projection
        // changed or other doPrepare call, then create a thread that
        // will do the real work. If there is a thread working on
        // this, then set the cancelled flag in the layer.
        if (currentWorker == null) {
            currentWorker = new LocationWorker();
            currentWorker.execute();
        }
        else setCancelled(true);
    }

    /**
     * Prepares the graphics for the layer.  This is where the
     * getRectangle() method call is made on the location.  <p>
     * Occasionally it is necessary to abort a prepare call.  When
     * this happens, the map will set the cancel bit in the
     * LayerThread, (the thread that is running the prepare).  If this
     * Layer needs to do any cleanups during the abort, it should do
     * so, but return out of the prepare asap.
     */
    public Vector prepare() {

        if (isCancelled()) {
            if (Debug.debugging("location")) {
                Debug.output(getName() + "|LocationLayer.prepare(): aborted.");
            }
            return null;
        }
        
        Vector omGraphicList = new Vector();
        Projection projection = getProjection();
        if (projection == null) {
            if (Debug.debugging("location")) {
                Debug.output(getName() + "|LocationLayer.prepare(): null projection, layer not ready.");
            }
            return omGraphicList;
        }

        if (Debug.debugging("location")) {
            Debug.output(getName()+"|LocationLayer.prepare(): doing it");
        }

        if (useDeclutterMatrix && declutterMatrix != null) {
            declutterMatrix.setWidth(projection.getWidth());
            declutterMatrix.setHeight(projection.getHeight());
            declutterMatrix.create();
        }
        
        // Setting the OMGraphicsList for this layer.  Remember, the
        // Vector is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list.  So,
        // after this call, the list is ready for painting.

        // call getRectangle();
        if (Debug.debugging("location")) {
            Debug.output(
                getName()+"|LocationLayer.prepare(): " +
                "calling prepare with projection: " + projection +
                " ul = " + projection.getUpperLeft() + " lr = " + 
                projection.getLowerRight()); 
        }

        LatLonPoint ul = projection.getUpperLeft();
        LatLonPoint lr = projection.getLowerRight();

        if (Debug.debugging("location")) {
            float delta = lr.getLongitude() - ul.getLongitude();
            Debug.output(getName()+ "|LocationLayer.prepare(): " +
                               " ul.lon =" + ul.getLongitude() +
                               " lr.lon = " + lr.getLongitude() +
                               " delta = " + delta); 
        }
        if (dataHandlers != null) {
            for (int i = 0; i < dataHandlers.length; i++) {
                ((LocationHandler) dataHandlers[i]).get(ul.getLatitude(),
                                                        ul.getLongitude(),
                                                        lr.getLatitude(), 
                                                        lr.getLongitude(), 
                                                        omGraphicList);
            }
        }

        /////////////////////
        // safe quit
        int size = 0;
        if (omGraphicList != null) {
            size = omGraphicList.size();        
            if (Debug.debugging("basic")) {
                Debug.output(getName()+
                             "|LocationLayer.prepare(): finished with " + 
                             size + " graphics");
            }

            // Don't forget to project them.  Since they are only
            // being recalled if the projection hase changed, then
            // we need to force a reprojection of all of them
            // because the screen position has changed.
            Enumeration things = omGraphicList.elements();
            while (things.hasMoreElements()) {
                OMGraphic thingy = (OMGraphic)things.nextElement();

                if (useDeclutterMatrix && thingy instanceof Location) {
                    Location loc = (Location) thingy;
                    loc.generate(projection, declutterMatrix);
                } else {
                    thingy.generate(projection);
                }
            }
        } else if (Debug.debugging("basic")) {
            Debug.output(getName()+
                         "|LocationLayer.prepare(): finished with null graphics list");
        }
        
        return omGraphicList;
    }

    /**
     * Paints the layer.
     *
     * @param g the Graphics context for painting
     */
    public void paint(java.awt.Graphics g) {    
        if (Debug.debugging("location")) {
            Debug.output(getName()+"|LocationLayer.paint()");
        }
        Vector vlist = getGraphicList();
        Object[] list = null;

        if (vlist != null)
            list = vlist.toArray();
        
        if (list != null) {
            int i;
            OMGraphic loc;
            // Draw from the bottom up, so it matches the palette, and
            // the order in which the handlers were loaded - the first
            // in the list is on top.

            // We need to go through list twice.  The first time, draw
            // all the regular OMGraphics, and also draw all of the
            // graphics for the locations.  The second time through,
            // draw the labels.  This way, the labels won't be covered
            // up by graphics.
            for (int j = 0; j < 2; j++) {
                for (i = list.length - 1; i >= 0; i--) {
                    loc = (OMGraphic) list[i];
                    if (j == 0) {
                        if (loc instanceof Location) {
                            ((Location)loc).renderLocation(g);
                        } else {
                            loc.render(g);
                        }
                    } else if (loc instanceof Location) {
                        ((Location)loc).renderName(g);
                    }
                }
            }
        } else {
            if (Debug.debugging("location")) {
                Debug.error(getName()+"|LocationLayer: paint(): Null list...");
            }
        }
    }

    /**
     * Parse the properties and set up the location handlers.  The
     * prefix will should be null, or a prefix string with a period at
     * the end, for scoping purposes.
     */
    protected void setLocationHandlers(String prefix, Properties p) {

        String handlersValue = p.getProperty(prefix + LocationHandlerListProperty);

        if (Debug.debugging("location")) {
            Debug.output(getName() + "| handlers = \"" + handlersValue + "\"");
        }

        if (handlersValue == null) {
            if (Debug.debugging("location")) {
                Debug.output("No property \"" + prefix + LocationHandlerListProperty
                             + "\" found in application properties.");
            }
            return;
        }

        // Divide up the names ...
        StringTokenizer tokens = new StringTokenizer(handlersValue, " ");
        Vector handlerNames = new Vector();
        while(tokens.hasMoreTokens()) {
            handlerNames.addElement(tokens.nextToken());
        }

        if (Debug.debugging("location")) {
            Debug.output("OpenMap.getLocationHandlers(): "+ handlerNames);
        }

        int nHandlerNames = handlerNames.size();
        Vector handlers = new Vector(nHandlerNames);
        Vector goodNames = new Vector(nHandlerNames);
        for (int i = 0; i < nHandlerNames; i++) {
            String handlerName = (String)handlerNames.elementAt(i);
            String classProperty = handlerName + ".class";
            String className = p.getProperty(classProperty);

            String nameProperty = handlerName + ".prettyName";
            String prettyName = p.getProperty(nameProperty);

            if (className == null) {
                Debug.error("Failed to locate property \""
                            + classProperty + "\"\nSkipping handler \"" + 
                            handlerName + "\"");
                continue;
            }
            try {
                if (Debug.debugging("location")) {
                    Debug.output(
                        "OpenMap.getHandlers():instantiating handler \""+
                        className+"\"");
                }

                Object obj = Class.forName(className).newInstance();// Works for applet!
                if (obj instanceof LocationHandler) {
                    LocationHandler lh = (LocationHandler) obj;
                    lh.setProperties(handlerName, p);
                    lh.setLayer(this);
                    handlers.addElement(lh);
                    goodNames.addElement(prettyName!=null?prettyName:"");
                }
                if (false) throw new java.io.IOException();//fool javac compiler
            } catch (java.lang.ClassNotFoundException e) {
                Debug.error("Handler class not found: \""
                            + className + "\"\nSkipping handler \"" + 
                            handlerName + "\"");
            } catch (java.io.IOException e) {
                Debug.error("IO Exception instantiating class \""
                            + className + "\"\nSkipping handler \"" + 
                            handlerName + "\"");
            } catch (Exception e) {
                Debug.error("Exception instantiating class \""
                            + className + "\": " + e);
            }
        }

        int nHandlers = handlers.size();

        dataHandlers = new LocationHandler[nHandlers];
        dataHandlerNames = new String[nHandlers];

        if (nHandlers != 0) {
            handlers.copyInto(dataHandlers);
            goodNames.copyInto(dataHandlerNames);
        }

    }

    /**
     * Let the LocationHandlers know that the layer has been removed.
     */
    public void removed(java.awt.Container cont) {
        if (dataHandlers != null) {
            for (int i = 0; i < dataHandlers.length; i++) {
                ((LocationHandler) dataHandlers[i]).removed(cont);
            }
        }
    }

    /**
     * Set the LocationHandlers for the layer.  Make sure you update
     * the LocationHandler names, too, so the names coorespond to these.
     * @param handlers an array of LocationHandlers.
     */
    public void setLocationHandlers(LocationHandler[] handlers) {
        dataHandlers = handlers;
        // Need to set the layer on the handlers.
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].setLayer(this);
        }
        resetPalette();
    }

    /**
     * Get the LocationHandlers for this layer.
     */
    public LocationHandler[] getLocationHandlers() {
        return dataHandlers;
    }

    /**
     * Set the LocationHandler names suitable for a GUI.  Make sure
     * these end up cooresponding to the LocationHandlers.
     * @param handlerNames an array of Strings.
     */
    public void setLocationHandlerNames(String[] handlerNames) {
        dataHandlerNames = handlerNames;
        resetPalette();
    }

    /**
     * Get the LocationHandlers for this layer.
     */
    public String[] getLocationHandlerNames() {
        return dataHandlerNames;
    }

    /**
     * Called when the LayerHandlers are reset, or their names are
     * reset, to refresh the palette with the new information.
     */
    protected void resetPalette() {
        box = null;
        super.resetPalette();
    }

    //----------------------------------------------------------------------
    // GUI
    //----------------------------------------------------------------------

    protected Box box = null;

    /** 
     * Provides the palette widgets to control the options of showing
     * maps, or attribute text.
     * 
     * @return Component object representing the palette widgets.
     */
    public java.awt.Component getGUI() {
        if (box == null) {
            box = Box.createVerticalBox();
            int nHandlers = 0;
            
            if (dataHandlers != null) {
                nHandlers = dataHandlers.length;
            }

            JPanel[] panels = new JPanel[nHandlers];

            for (int i = 0; i < nHandlers; i++) {

                String handlerName;
                if (dataHandlerNames != null && i < dataHandlerNames.length) {
                    handlerName = dataHandlerNames[i];
                } else {
                    handlerName = "";
                }

                panels[i] = PaletteHelper.createPaletteJPanel(handlerName);
                panels[i].add(dataHandlers[i].getGUI());
                box.add(panels[i]);
            }

            if (declutterMatrix != null) {
                JPanel dbp = new JPanel(new GridLayout(0, 1));
                JCheckBox declutterButton = new JCheckBox("Declutter Names", useDeclutterMatrix);
                declutterButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            JCheckBox jcb = (JCheckBox) ae.getSource();
                            useDeclutterMatrix = jcb.isSelected();
                            doPrepare();
                        }
                    });
                declutterButton.setToolTipText("<HTML><BODY>Move location names so they don't overlap.<br>This may take awhile if you are zoomed out.</BODY></HTML>");

                dbp.add(declutterButton);
                box.add(dbp);
            }
        }       
        return box;
    }

    //------------------------------------------------------------
    // MapMouseListener implementation
    //------------------------------------------------------------

    /** Given a mouse event, find the closest location on the screen. */
    public Location findClosestLocation(MouseEvent evt) {
        Vector graphics = getGraphicList();
        if (graphics != null) {
            int x = evt.getX();
            int y = evt.getY();
            float limit = 4.0f;
            Location ret = null;
            Location loc;

            float closestDistance = Float.MAX_VALUE;
            float currentDistance;
            int i;
            int size = graphics.size();

            for (i = 0; i < size; i++) {
                loc = (Location)graphics.elementAt(i);
                currentDistance = loc.distance(x, y);
                if (currentDistance < closestDistance) {
                    ret = loc;
                    closestDistance = currentDistance;
                }
            }
            if (closestDistance <= limit) return ret;
        } 
        return null;
    }

    public String[] getMouseModeServiceList() {
        String[] services = {SelectMouseMode.modeID};
        return services;
    }

    protected void showMapPopup(MouseEvent evt, MapBean map) {
        if (backgroundMenu == null) {
            backgroundMenu = new LocationPopupMenu();
            backgroundMenu.add(new LocationMenuItem(recenter, backgroundMenu, this));
            backgroundMenu.add(new LocationMenuItem(cancel, backgroundMenu, this));
            backgroundMenu.setMap(map);
        }
        backgroundMenu.setEvent(evt);
        backgroundMenu.show(this, evt.getX(), evt.getY());
    }

    protected void showLocationPopup(MouseEvent evt, Location loc, MapBean map) {
        if (locMenu == null) {
            locMenu = new LocationPopupMenu();
            locMenu.setMap(map);
        }
        locMenu.removeAll();

        locMenu.setEvent(evt);
        locMenu.setLoc(loc);

        locMenu.add(new LocationMenuItem(LocationLayer.recenter, locMenu, this));
        locMenu.add(new LocationMenuItem(LocationLayer.cancel, locMenu, this));
        locMenu.addSeparator();

        LocationHandler lh = loc.getLocationHandler();
        if (lh != null) {
            lh.fillLocationPopUpMenu(locMenu);
        }

        locMenu.show(this, evt.getX(), evt.getY());
    }

    public boolean mousePressed(MouseEvent evt) {
        if (!isVisible()) return false;

        Location loc = findClosestLocation(evt);
        if (map == null) {
            try{
                map = (MapBean) SwingUtilities.getAncestorOfClass(Class.forName("com.bbn.openmap.MapBean"), this);
            }
            catch (java.lang.ClassNotFoundException e) {
                Debug.error("LocationLayer: Whatza MapBean??");
            }
        }
        if (loc == null) {
            // user clicked on map
            Debug.message("location", "Clicked on background");
            showMapPopup(evt, map);
        } else {
            // user clicked on rate center
            Debug.message("location", "Clicked on location");
            showLocationPopup(evt, loc, map);
        }

        return true;
    }
  
    public boolean mouseReleased(MouseEvent e) { return false;}
    public boolean mouseClicked(MouseEvent evt) { return false;}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public boolean mouseDragged(MouseEvent e) { return false;}

    boolean setNameOnLine = false;

    public boolean mouseMoved(MouseEvent evt) {
        if (!isVisible()) return false;

        Location loc = findClosestLocation(evt);
        if (loc == null) {
            if (setNameOnLine) { // only do this once.
                fireRequestInfoLine("");
                setNameOnLine = false;
            }
            return false; //pass through!!
        } else {
            fireRequestInfoLine(loc.getName());
            setNameOnLine = true;
            return true;
        }
    }

    public void mouseMoved() {
//      fireRequestInfoLine("");
    }

    /**
     * PropertyConsumer method, to fill in a Properties object,
     * reflecting the current values of the layer.  If the
     * layer has a propertyPrefix set, the property keys should
     * have that prefix plus a separating '.' prepended to each
     * propery key it uses for configuration.
     *
     * @param props a Properties object to load the PropertyConsumer
     * properties into.  If props equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + UseDeclutterMatrixProperty, 
                  new Boolean(useDeclutterMatrix).toString());

        if (declutterMatrix != null) {
            props.put(prefix + DeclutterMatrixClassProperty, 
                      declutterMatrix.getClass().getName());
            props.put(prefix + AllowPartialsProperty, 
                      new Boolean(declutterMatrix.isAllowPartials()).toString());
        }

        StringBuffer handlerList = new StringBuffer();

        // Need to hand this off to the location handlers, and build a
        // list of marker names to use in the LocationLayer property list.
        if (dataHandlers != null) {
            for (int i = 0; i < dataHandlers.length; i++) {
                String pp = dataHandlers[i].getPropertyPrefix();
                handlerList.append(" " + pp);
                props.put(pp + ".prettyName", dataHandlerNames[i]);
                dataHandlers[i].getProperties(props);
            }
        }

        props.put(prefix + LocationHandlerListProperty, handlerList.toString());

        return props;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer.  The
     * key for each property should be the raw property name (without
     * a prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).  For Layer, this method should at least return the
     * 'prettyName' property.
     *
     * @param list a Properties object to load the PropertyConsumer
     * properties into.  If getList equals null, then a new Properties
     * object should be created.
     * @return Properties object containing PropertyConsumer property
     * values.  If getList was not null, this should equal getList.
     * Otherwise, it should be the Properties object created by the
     * PropertyConsumer. 
     */
    public Properties getPropertyInfo(Properties list) {
        list = super.getPropertyInfo(list);

        list.put(UseDeclutterMatrixProperty, "Flag for using the declutter matrix (true/false)");
        list.put(DeclutterMatrixClassProperty,"Class name of the declutter matrix to use");
        list.put(AllowPartialsProperty,"Flag to allow labels to run off the edge of the map (true/false)");
        list.put(LocationHandlerListProperty, "Space-separated list of unique names to use to scope the LayerHandler property definitions");

        if (dataHandlers != null) {
            for (int i = 0; i < dataHandlers.length; i++) {
                dataHandlers[i].getPropertyInfo(list);
            }
        }


        return list;
    }
}

