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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/terrain/TerrainLayer.java,v $
// $RCSfile: TerrainLayer.java,v $
// $Revision: 1.10 $
// $Date: 2005/08/09 18:50:21 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.terrain;

/*  Java Core  */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.dataAccess.dted.DTEDFrameCache;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The Terrain Layer is an example of creating a layer that acts as a tool that
 * defines and area (via user gestures) and presents a result of the analysis of
 * the data. In this case, Elevation data is used in two different ways. The
 * Profile tool lets you draw a line on the map, and then uses DTED data to
 * create a GIF image that shows the terrain profile along the drawn line. The
 * LOS (line-of-sight) tool lets you define a circle, and then calculates the
 * places on the ground that are within sight of the center of the circle. The
 * result is shown with the visible points being colored green, and all other
 * points being clear. The LOS tool lets you use a height slider on its palette
 * to define additional height at the point, representing a tower, building, or
 * location of an aircraft.
 * 
 * <P>
 * The tools require you to be in the gesture mode of OpenMap.
 * 
 * <P>
 * The TerrainLayer needs a DTEDFrameCache. It can be added to the layer
 * programmatically, or the layer will find it if the DTEDFrameCache is added to
 * the MapHandler. To do that in the OpenMap application, add the DTEDFrameCache
 * to the openmap.components property in the openmap.properties file.
 * 
 * <pre>
 * 
 *  #----------------------------------------------------------------------
 *  # Properties file for TerrainLayer
 *  #----------------------------------------------------------------------
 *  # The default tool to use for the terrain layer.  Can be PROFILE or
 *  LOS.  terrain.default.mode=PROFILE
 *  #----------------------------------------------------------------------
 *  # End of properties file for TerrainLayer
 *  #----------------------------------------------------------------------
 * 
 * </pre>
 * 
 * @see com.bbn.openmap.dataAccess.dted.DTEDFrameCache
 */
public class TerrainLayer extends OMGraphicHandlerLayer implements ActionListener, MapMouseListener {
    /** The cache that knows how to handle DTED requests. */
    public DTEDFrameCache frameCache;

    /** Which tool is being used. */
    protected int mode;
    /** The code number for the profile tool. */
    public final static int PROFILE = 0;
    /** The code number for the perspective tool (unimplemented). */
    public final static int PERSPECTIVE = 1;
    /** The code number for the LOS tool. */
    public final static int LOS = 2;
    /** The current tool being used. */
    public TerrainTool currentTool;
    public ProfileGenerator profileTool;
    public LOSGenerator LOSTool;

    public static final String defaultModeProperty = "default.mode";

    public final static String clearCommand = "clearTool";
    public final static String createCommand = "createTool";

    /**
     * The default constructor for the Layer. All of the attributes are set to
     * their default values.
     */
    public TerrainLayer() {
        setName("Terrain");
        init();
    }

    /** Creates new tools. */
    public void init() {
        profileTool = new ProfileGenerator(this);
        LOSTool = new LOSGenerator(this);
        setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }

    /**
     * Sets the default values for the variables, if the properties are not
     * found, or are invalid. Usually not a good idea.
     */
    protected void setDefaultValues() {
        mode = PROFILE;
    }

    public void setFrameCache(DTEDFrameCache dfc) {
        frameCache = dfc;
    }

    public DTEDFrameCache getFrameCache() {
        return frameCache;
    }

    /**
     * Set all the TerrainLayer properties from a properties object
     * 
     * @param prefix a string that gets set to individualize the properties to a
     *        specific layer.
     * @param properties the properties object
     */
    public void setProperties(String prefix, java.util.Properties properties) {

        super.setProperties(prefix, properties);
        setDefaultValues();
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String defaultModeString = properties.getProperty(prefix + defaultModeProperty, "PROFILE");
        if (defaultModeString.equalsIgnoreCase("LOS")) {
            setMode(LOS);
        } else {
            setMode(PROFILE);
        }
    }

    /**
     * Prepares the graphics for the layer. This is where the getRectangle()
     * method call is made on the dted.
     * <p>
     * Occasionally it is necessary to abort a prepare call. When this happens,
     * the map will set the cancel bit in the LayerThread, (the thread that is
     * running the prepare). If this Layer needs to do any cleanups during the
     * abort, it should do so, but return out of the prepare asap.
     */
    public synchronized OMGraphicList prepare() {

        if (isCancelled()) {
            Debug.message("dted", getName() + "|TerrainLayer.prepare(): aborted.");
            return null;
        }

        Projection projection = getProjection();

        if (projection == null) {
            System.err.println("Terrain Layer needs to be added to the MapBean before it can be used!");
            return new OMGraphicList();
        }

        Debug.message("basic", getName() + "|TerrainLayer.prepare(): doing it");

        // Setting the OMGraphicsList for this layer. Remember, the
        // OMGraphicList is made up of OMGraphics, which are generated
        // (projected) when the graphics are added to the list. So,
        // after this call, the list is ready for painting.

        profileTool.setScreenParameters(projection);
        LOSTool.setScreenParameters(projection);
        return currentTool.getGraphics();
    }

    // ----------------------------------------------------------------------
    // GUI
    // ----------------------------------------------------------------------

    /** The user interface palette for the Terrain layer. */
    protected Box paletteBox = null;
    private String profileCommand = "setModeToProfile";
    private String losCommand = "setModeToLos";

    /** Creates the interface palette. */
    public java.awt.Component getGUI() {

        if (paletteBox == null) {
            if (Debug.debugging("terrain"))
                System.out.println("TerrainLayer: creating Terrain Palette.");

            paletteBox = Box.createVerticalBox();

            // palette = new JPanel();
            // palette.setLayout(new GridLayout(0, 1));

            // The Terrain Level selector
            JPanel modePanel = PaletteHelper.createPaletteJPanel("Tool Mode");
            ButtonGroup modes = new ButtonGroup();

            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String ac = e.getActionCommand();
                    if (ac.equalsIgnoreCase(losCommand)) {
                        setMode(LOS);
                    } else {
                        setMode(PROFILE);
                    }
                }
            };

            JRadioButton profileModeButton = new JRadioButton("Profile");
            profileModeButton.addActionListener(al);
            profileModeButton.setActionCommand(profileCommand);
            JRadioButton losModeButton = new JRadioButton("LOS");
            losModeButton.addActionListener(al);
            losModeButton.setActionCommand(losCommand);

            modes.add(profileModeButton);
            modes.add(losModeButton);

            switch (mode) {
            case LOS:
                losModeButton.setSelected(true);
                break;
            case PROFILE:
            default:
                profileModeButton.setSelected(true);
            }

            modePanel.add(profileModeButton);
            modePanel.add(losModeButton);

            // The LOS Height Adjuster
            JPanel centerHeightPanel = PaletteHelper.createPaletteJPanel("LOS Center Object Height");
            JSlider centerHeightSlide = new JSlider(JSlider.HORIZONTAL, 0/* min */, 500/* max */, 0/* initial */);
            java.util.Hashtable dict = new java.util.Hashtable();
            dict.put(new Integer(0), new JLabel("0 ft"));
            dict.put(new Integer(500), new JLabel("500 ft"));
            centerHeightSlide.setLabelTable(dict);
            centerHeightSlide.setPaintLabels(true);
            centerHeightSlide.setMajorTickSpacing(50);
            centerHeightSlide.setPaintTicks(true);
            centerHeightSlide.setSnapToTicks(false);
            centerHeightSlide.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JSlider slider = (JSlider) ce.getSource();
                    if (slider.getValueIsAdjusting()) {
                        fireRequestInfoLine("TerrainLayer - center height value = "
                                + slider.getValue());
                        LOSTool.setLOSobjectHeight(slider.getValue());
                    }
                }
            });
            centerHeightPanel.add(centerHeightSlide);

            JPanel profileControlPanel = PaletteHelper.createPaletteJPanel("Tool Commands");
            JButton clearButton = new JButton("Clear/Reset Tool");
            clearButton.setActionCommand(clearCommand);
            clearButton.addActionListener(this);
            JButton createButton = new JButton("Create");
            createButton.setActionCommand(createCommand);
            createButton.addActionListener(this);

            profileControlPanel.add(clearButton);
            profileControlPanel.add(createButton);

            JButton redraw = new JButton("Redraw Terrain Layer");
            redraw.setActionCommand(RedrawCmd);
            redraw.addActionListener(this);

            paletteBox.add(modePanel);
            paletteBox.add(centerHeightPanel);
            paletteBox.add(profileControlPanel);
            // palette.add(redraw);
        }

        return paletteBox;
    }

    // ----------------------------------------------------------------------
    // ActionListener interface implementation
    // ----------------------------------------------------------------------

    /**
     * The reaction handler for the buttons being pressed on the palette.
     */
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        String ac = e.getActionCommand();
        if (ac.equalsIgnoreCase(RedrawCmd)) {
            doPrepare();
        } else {
            currentTool.getState().actionPerformed(e);
        }
    }

    // ----------------------------------------------------------------------
    // MapMouseListener interface implementation
    // ----------------------------------------------------------------------

    public synchronized MapMouseListener getMapMouseListener() {
        return this;
    }

    /**
     * Tells the MouseDelegator which Mouse Modes we're interested in receiving
     * events from. In this case, just the "Gestures" mode.
     */
    public String[] getMouseModeServiceList() {
        String[] services = { SelectMouseMode.modeID };
        return services;
    }

    public boolean mousePressed(MouseEvent e) {
        return currentTool.getState().mousePressed(e);
    }

    public boolean mouseReleased(MouseEvent e) {
        return currentTool.getState().mouseReleased(e);
    }

    public boolean mouseClicked(MouseEvent e) {
        return currentTool.getState().mouseClicked(e);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public boolean mouseDragged(MouseEvent e) {
        return currentTool.getState().mouseDragged(e);
    }

    public boolean mouseMoved(MouseEvent e) {
        return false;
    }

    public void mouseMoved() {
    }

    /**
     * Little math utility that both tools use, that just implements the
     * pathagorean theorem to do the number of pixels between two screen points.
     */
    public static int numPixelsBetween(int x1, int y1, int x2, int y2) {
        return (int) Math.sqrt(Math.pow((double) (x1 - x2), 2.0)
                + Math.pow((double) (y1 - y2), 2.0));
    }

    /** Set the current tool to be used. */
    public void setMode(int m) {
        mode = m;
        if (currentTool != null)
            currentTool.reset();
        if (m == PROFILE) {
            currentTool = profileTool;
            // System.out.println("Changing mode to PROFILE");
        }
        if (m == LOS) {
            currentTool = LOSTool;
            // System.out.println("Changing mode to LOS");
        }
        if (currentTool != null) {
            currentTool.reset();
            setList(currentTool.getGraphics());
        }
    }

    public int getMode() {
        return mode;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof DTEDFrameCache) {
            setFrameCache((DTEDFrameCache) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj == getFrameCache()) {
            setFrameCache(null);
        }
    }

}
