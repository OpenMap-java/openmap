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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/Specialist.java,v $
// $RCSfile: Specialist.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:37 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import com.bbn.openmap.corba.CSpecialist.ActionUnion;
import com.bbn.openmap.corba.CSpecialist.CProjection;
import com.bbn.openmap.corba.CSpecialist.GraphicChange;
import com.bbn.openmap.corba.CSpecialist.LLPoint;
import com.bbn.openmap.corba.CSpecialist.MouseEvent;
import com.bbn.openmap.corba.CSpecialist.ServerPOA;
import com.bbn.openmap.corba.CSpecialist.UGraphic;
import com.bbn.openmap.corba.CSpecialist.UWidget;
import com.bbn.openmap.corba.CSpecialist.UpdateRecord;
import com.bbn.openmap.corba.CSpecialist.WidgetChange;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.corba.CORBASupport;

/**
 * The Specialist is the base class for all specialists. It assists
 * the developer in creating a specialist and sending back graphics to
 * OpenMap(TM) or MATT. This class performs management of the client
 * graphics for multiple clients, and there are functions in the class
 * specifically designed to do that. These functions have not been
 * declared final in order to allow you to extend their functionality,
 * depending on the behavior you want in your specialist.
 * 
 * <P>
 * To create your own specialist, you need to create a class that
 * inherits from this one. Then, you need to define fillRectangle,
 * which loads the list of graphics to send back to the client. For
 * threading reasons, you must maintain the list of graphics yourself.
 * The GraphicList class can assist you in that. signOff() should be
 * overloaded to receive information about which client have stopped
 * using the specialist.
 * 
 * <P>
 * If you want a palette, define a new version of makePalette(), using
 * addPalette() to add widgets to the palette widget list.
 * 
 * <P>
 * If you want gesture information, overload receiveGesture().
 * 
 * <P>
 * As for running a specialist, in the specialist main() function, you
 * only need to create an instance of your specialist, and then call
 * the start() function that is a part of this class. start(),
 * parseArgs() and printHelp() all are written here for simple
 * execution. If your specialist has more complicated options and
 * help, you'll need to overload these functions.
 * 
 * @see GraphicList
 */
public abstract class Specialist extends ServerPOA {

    protected static String iorfile = null;
    protected static String naming = null;

    Dictionary clientPaletteLists;
    Dictionary clientGestureActionLists;

    protected Vector currentPaletteList;
    protected Vector currentGestureActionList;

    private short selectionDistance;
    private boolean wantAreaEvents;
    protected Vector graphicUpdates;

    /**
     * Default constructor. This is used to load specialist classes
     * directly into the OpenMap VM.
     */
    public Specialist() {
        this("Default", (short) 0, false);
    }

    /** The argument to the constructor is the name of the specialist. */
    public Specialist(String name) {
        this(name, (short) 0, false);
    }

    public Specialist(String name, short sd, boolean wae) {
        super();
        clientPaletteLists = new Hashtable();
        clientGestureActionLists = new Hashtable();
        graphicUpdates = new Vector();
        setSelectionDistance(sd);
        setWantAreaEvents(wae);
    }

    /**
     * This is the call for graphics that is made to the specialist.
     * You should never do anything with this method, because it does
     * the management of different clients for you. All of the
     * client's <b>getRectangle </b> requests are forwarded to
     * <b>fillRectangle </b>, which is the method you should override.
     * <P>
     */
    public UGraphic[] getRectangle(
                                   CProjection p,
                                   LLPoint llnw,
                                   LLPoint llse,
                                   String staticArgs,
                                   org.omg.CORBA.StringHolder dynamicArgs,
                                   org.omg.CORBA.ShortHolder graphicSelectableDistance,
                                   org.omg.CORBA.BooleanHolder areaEvents,
                                   GraphicChange notifyOnChange, String uniqueID) {

        try {
            UGraphic gl[] = fillRectangle(p,
                    llnw,
                    llse,
                    staticArgs,
                    dynamicArgs,
                    notifyOnChange,
                    uniqueID);
            graphicSelectableDistance.value = selectionDistance;
            areaEvents.value = wantAreaEvents;

            return gl;
        } catch (Throwable t) {
            Debug.error("Specialist.getRectangle(): " + t);
            t.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * The signoff function lets the specialist know that a client is
     * checking out. You should not override this method, because this
     * is where the specialist cleans up lists that have been used
     * internally to handle bookkeeping functions. <b>signoff </b>
     * calls <b>signOff </b> (big O) which is the specialist-specific
     * call to let a programmer clean up after a client.
     */
    public void signoff(String uniqueID) {
        signOff(uniqueID); // call to the specialist specific version
        clientPaletteLists.remove(uniqueID);
    }

    /**
     * getPaletteConfig is the idl call to get a palette. Palettes are
     * now managed by the specialist internally, and the specialist
     * only needs to implement <b>makePalette </b> instead. Shouldn't
     * be modified or overridden.
     */
    public UWidget[] getPaletteConfig(WidgetChange notifyOnChange,
                                      String staticArgs,
                                      org.omg.CORBA.StringHolder dynamicArgs,
                                      String uniqueID) {
        currentPaletteList = (Vector) clientPaletteLists.get(uniqueID);

        if (currentPaletteList == null) {
            currentPaletteList = new Vector();
            clientPaletteLists.put(uniqueID, currentPaletteList);
        }

        makePalette(notifyOnChange, staticArgs, dynamicArgs, uniqueID);
        return packPalette();
    }

    /*
     * packPalette is used internally, to create the UWidget[] out of
     * the Dictionary UWidgets.
     */
    protected UWidget[] packPalette() {
        int num_widgets = currentPaletteList.size();
        UWidget[] widgets = new UWidget[num_widgets];
        for (int i = 0; i < num_widgets; i++)
            widgets[i] = (UWidget) currentPaletteList.elementAt(i);
        return widgets;
    }

    /**
     * addPalette adds a palette widget to the list of palette
     * widgets. Should be called during <b>makePalette </b>.
     */
    public void addPalette(UWidget uwidget) {
        if (uwidget == null)
            return;
        currentPaletteList.addElement(uwidget);
    }

    /**
     * Sets the number of elements in the current palette widget list
     * to zero.
     */
    public void clearPalette() {
        currentPaletteList.removeAllElements();
    }

    /**
     * <b>sendGesture </b> is the arrival point for gesture
     * notifications. Again, this should not be overridden because
     * there is bookkeeping going on. All requests are sent on to
     * <b>receiveGesture </b>, which should be overridden.
     */
    public ActionUnion[] sendGesture(MouseEvent gesture, String uniqueID) {
        currentGestureActionList = (Vector) clientGestureActionLists.get(uniqueID);

        if (currentGestureActionList == null) {
            currentGestureActionList = new Vector();
            clientGestureActionLists.put(uniqueID, currentGestureActionList);
        }

        graphicUpdates.removeAllElements();
        currentGestureActionList.removeAllElements();

        try {
            receiveGesture(gesture, uniqueID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Handle the graphics object updates as a special case,
        // because
        // the ActionUnion sequence can be made up of several objects.
        // All of the other types of gesture responses should have
        // been
        // loaded via the helper functions below.
        int num_graphicUpdates = graphicUpdates.size();
        if (num_graphicUpdates > 0) {
            UpdateRecord[] urs = new UpdateRecord[num_graphicUpdates];
            for (int i = 0; i < num_graphicUpdates; i++)
                urs[i] = (UpdateRecord) graphicUpdates.elementAt(i);
            ActionUnion au = new ActionUnion();
            au.ginfo(urs);
            currentGestureActionList.addElement(au);
        }
        return packGestures();
    }

    protected ActionUnion[] packGestures() {
        int num_actions = currentGestureActionList.size();
        ActionUnion[] actions = new ActionUnion[num_actions];
        for (int i = 0; i < num_actions; i++)
            actions[i] = (ActionUnion) currentGestureActionList.elementAt(i);
        return actions;
    }

    /**
     * Sets the number of elements in the current gesture action union
     * list to zero.
     */
    public void clearGesture() {
        currentGestureActionList.removeAllElements();
    }

    /**
     * The <b>itext </b> string will appear in the information window
     * of the client.
     */
    public void addInfoText(String itext) {
        ActionUnion ret = new ActionUnion();
        ret.itext(itext);
        currentGestureActionList.addElement(ret);
    }

    public void addPlainText(String ptext) {
        ActionUnion ret = new ActionUnion();
        ret.ptext(ptext);
        currentGestureActionList.addElement(ret);
    }

    /** The HTML string should be HTML formatted. */
    public void addHTMLText(String htext) {
        ActionUnion ret = new ActionUnion();
        ret.htext(htext);
        currentGestureActionList.addElement(ret);
    }

    /**
     * The URL string should also be formatted, as if you were passing
     * it to a browser (which you are!).
     */
    public void addURL(String url) {
        ActionUnion ret = new ActionUnion();
        ret.url(url);
        currentGestureActionList.addElement(ret);
    }

    /**
     * Get the UpdateRecord from the getGraphicUpdates method in the
     * graphic that was updated. The <b>UpdateRecord </b> can be
     * obtained by calling the <b>getGraphicUpdates </b> method of the
     * graphic object.
     */
    public void addGraphic(UpdateRecord ur) {
        graphicUpdates.addElement(ur);
    }

    /**
     * <b>fillRectangle </b> is the method that the specialist needs
     * to overridden to add the graphics to the list to be sent back.
     * The CProjection structure can provide the lat/lon center,
     * height, width, projection type and scale of the screen of the
     * client. <b>llnw </b> is the upper left coordinate of the
     * screen, and <b>llse </b> is the south east. For an OpenMap or
     * Matt client, <b>staticArgs </b> are defined in the overlay
     * table, while <b>dynamicArgs </b> are defined for the specialist
     * by the client.
     */
    public abstract UGraphic[] fillRectangle(
                                             CProjection p,
                                             LLPoint llnw,
                                             LLPoint llse,
                                             String staticArgs,
                                             org.omg.CORBA.StringHolder dynamicArgs,
                                             GraphicChange notifyOnChange,
                                             String uniqueID);

    /**
     * <b>receiveGesture </b> is the arrival point for gesture
     * notifications. Use the calls defined above to add object
     * updates, URLs, HTML text, etc, as return actions.
     * 
     * @see #addInfoText
     * @see #addPlainText
     * @see #addHTMLText
     * @see #addURL
     * @see #addGraphic
     */
    public void receiveGesture(MouseEvent gesture, String uniqueID) {}

    /**
     * <b>makePalette </b> is used to create the palette for the
     * specialist. The palette widgets are: <b>SRadioButton,
     * SCheckBox, SRadioButton, SButtonBox, SSlider, SListBox </b>.
     * Add them to the palette using <b>addPalette </b>.
     * 
     * @see #addPalette
     */
    public void makePalette(WidgetChange notifyOnChange, String staticArgs,
                            org.omg.CORBA.StringHolder dynamicArgs,
                            String uniqueID) {}

    protected void setWantAreaEvents(boolean setting) {
        wantAreaEvents = setting;
    }

    protected void setSelectionDistance(short value) {
        selectionDistance = value;
    }

    protected boolean getWantAreaEvents() {
        return wantAreaEvents;
    }

    protected short getSelectionDistance() {
        return selectionDistance;
    }

    /**
     * SignOff is called when a client stops. Use this to clean up
     * after a particular client.
     */
    public abstract void signOff(String uniqueID);

    /**
     * This is a default start method that initializes the specialist,
     * and handles the boa. It also handles parsing simple line
     * options (-ior and -help). If your specialist needs more
     * options, copy this function and add what you need. The args are
     * command line arguments, and the ior filename is really being
     * looked for here.
     */
    public void start(String[] args) {
        CORBASupport cs = new CORBASupport();

        if (args != null) {
            parseArgs(args);
        }

        cs.start(this, args, iorfile, naming);
    }

    /**
     * <b>parseArgs </b> should reflect the needs of your specialist.
     * It is presently defined to run with the default start() method,
     * looking for -ior and -help strings.
     */
    public void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-ior")) {
                iorfile = args[++i];
            } else if (args[i].equalsIgnoreCase("-name")) {
                naming = args[++i];
            } else if (args[i].equalsIgnoreCase("-help")) {
                printHelp();
            } else if (args[i].equalsIgnoreCase("-h")) {
                printHelp();
            }
        }

        // must specify an iorfile
        if (iorfile == null && naming == null) {
            Debug.error("IOR file and name service name are null!  Use `-ior' or '-name' flag!");
        }
    }

    /**
     * <b>printHelp </b> should print a usage statement which reflects
     * the command line needs of your specialist.
     */
    public void printHelp() {
        Debug.output("usage: java <specialist> [-ior <file> || -name <NAME>]");
        System.exit(1);
    }

}