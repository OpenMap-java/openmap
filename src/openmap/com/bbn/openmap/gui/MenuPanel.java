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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/MenuPanel.java,v $
// $RCSfile: MenuPanel.java,v $
// $Revision: 1.2 $
// $Date: 2003/04/05 05:39:01 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.beans.beancontext.*;
import java.util.*;

import javax.swing.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.image.SunJPEGFormatter;

/**
 * The MenuPanel is menu widget for the OpenMap system.
 * This class provides basic menu interface, and should be easy to extend.
 */
public class MenuPanel extends JMenuBar
    implements Serializable, ActionListener, PropertyChangeListener,
	       ProjectionListener, BeanContextChild, BeanContextMembershipListener
{

    // action command names
    public final static transient String projCmd = "setProj";
    public final static transient String zoomIn2Cmd = "zoomIn2Cmd";
    public final static transient String zoomIn4Cmd = "zoomIn4Cmd";
    public final static transient String zoomOut2Cmd = "zoomOut2Cmd";
    public final static transient String zoomOut4Cmd = "zoomOut4Cmd";
    public final static transient String overlayMgrCmd = "showOverlayMgr";
    public final static transient String mouseModeCmd = "setMouseMode";
    public final static transient String redrawCmd = "redraw";
    public final static transient String coordCmd = "coordinates";

    protected transient I18n I18N = null;
    protected transient CoordInternalFrame coordDialog=null;
    protected transient CoordDialog coordDialog2 = null;
    protected transient MouseDelegator mouseDelegator=null;
    protected transient JMenu mouseModeSubmenu = null;
    protected transient JMenu projsubmenu = null;
    protected transient JMenu debugMenu = null;
    protected transient JMenu helpMenu = null;
    protected transient InformationDelegator informationDelegator=null;
    // mouse mode widgets
    protected transient JRadioButtonMenuItem[] mouseModeButtons =
    new JRadioButtonMenuItem[0];
    protected transient ButtonGroup group2 = null; 

    protected transient Projection projection;
    protected transient java.awt.Component projComponent;
    protected ProjectionSupport projectionSupport;
    protected ZoomSupport zoomSupport;
    protected JDialog aboutBox;

    /**
     * BeanContext.
     */
    private BeanContextChildSupport beanContextChildSupport = new BeanContextChildSupport(this);

    /**
     * Construct a MenuPanel.
     */
    public MenuPanel() {
	super();
	Debug.message("menupanel", "MenuPanel()");

	projectionSupport = new ProjectionSupport(this);
	zoomSupport = new ZoomSupport(this);

	// HACK I18N needs to be redone.
	I18N = new I18n("GUI");		// get the GUI properties

	// create the default menu bar here.
	createMenuBar();
    }

    /**
     * Get the About Dialog box.
     * @return JDialog
     */
    public JDialog getAboutBox() {
	return aboutBox;
    }

    /**
     * Set the About Dialog box.
     * @param aboutBox JDialog
     */
    public void setAboutBox(JDialog aboutBox) {
	this.aboutBox = aboutBox;
    }

    /**
     * Set the projection.
     * This changes the setting of the projection radio button menu.
     * @param aProjection Projection
     */
    protected synchronized void setProjection(Projection aProjection) {
	projection = aProjection;

	int projType = projection.getProjectionType();

	// Change the selected projection type menu item
	for (int i=0; i<projsubmenu.getItemCount(); i++) {
	    try {
		JMenuItem item = projsubmenu.getItem(i);
		int projID = Integer.parseInt(item.getName());
		if (projID == projType) {
		    projsubmenu.getItem(i).setSelected(true);
		}
	    } catch (NumberFormatException e) {
		e.printStackTrace();
	    }
	}
    }


    /**
     * Sets up the MouseModes submenu.
     * @param md MouseDelegator
     */
    public void setMouseDelegator(MouseDelegator md) {
        mouseDelegator = md;
 	if (mouseDelegator != null) {
 	    mouseDelegator.addPropertyChangeListener(this);
 	    MapMouseMode[] modes = mouseDelegator.getMouseModes();
 	    String activeMode = mouseDelegator.getActiveMouseModeID();

 	    group2 = new ButtonGroup();

 	    mouseModeButtons = new JRadioButtonMenuItem[modes.length];

	    if (mouseModeSubmenu != null) {
		for (int mms = 0; mms < modes.length; mms++){
		    mouseModeButtons[mms] = 
			(JRadioButtonMenuItem) mouseModeSubmenu.add(
			    new JRadioButtonMenuItem(I18N.get(
				"menu.control.mode." 
				+ modes[mms].getID(), 
				modes[mms].getID())));
		    mouseModeButtons[mms].setActionCommand(mouseModeCmd);
		    mouseModeButtons[mms].setName(modes[mms].getID());
		    mouseModeButtons[mms].addActionListener(this);
		    group2.add(mouseModeButtons[mms]);
		    if (activeMode.equals(modes[mms].getID()))
			mouseModeButtons[mms].setSelected(true);
		}
	    }
 	}
    }

    public void unsetMouseDelegator(MouseDelegator md){
 	if (md != null) {
 	    mouseDelegator.removePropertyChangeListener(this);

	    if (mouseModeButtons != null) {
		for (int mms = 0; mms < mouseModeButtons.length; mms++){
		    mouseModeButtons[mms].removeActionListener(this);
		    group2.remove(mouseModeButtons[mms]);
		}
	    }
 	}
	group2 = null;
	mouseModeButtons = null;
        mouseDelegator = null;
    }

    /** 
     *  Convenience function for setting up listeners
     */
    public void setupListeners(MapBean map) {
        map.addPropertyChangeListener(this);
        addProjectionListener(map);
        addZoomListener(map);
        map.addProjectionListener(this);
	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    coordDialog.addCenterListener(map);
	} else {
	    coordDialog2.addCenterListener(map);
	}
    }

    /** 
     *  Convenience function for undoing set up listeners
     */
    public void undoListeners(MapBean map) {
        map.removePropertyChangeListener(this);
        removeProjectionListener(map);
        removeZoomListener(map);
        map.removeProjectionListener(this);
	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    coordDialog.removeCenterListener(map);
	} else {
	    coordDialog2.removeCenterListener(map);
	}
    }

    /*----------------------------------------------------------------------
     * Projection Support - for broadcasting projection changed events
     *----------------------------------------------------------------------*/

    /**
     *
     */
    protected synchronized void addProjectionListener(ProjectionListener l) {
	projectionSupport.addProjectionListener(l);
    }


    /**
     *
     */
    protected synchronized void removeProjectionListener(ProjectionListener l) {
	projectionSupport.removeProjectionListener(l);
    }


    /**
     *
     */
    public void fireProjectionChanged(Projection p) {
	projectionSupport.fireProjectionChanged(p);
    }




    /*----------------------------------------------------------------------
     * Zoom Support - for broadcasting zoom events
     *----------------------------------------------------------------------*/

    /**
     *
     */
    public synchronized void addZoomListener(ZoomListener l) {
	zoomSupport.addZoomListener(l);
    }


    /**
     *
     */
    public synchronized void removeZoomListener(ZoomListener l) {
	zoomSupport.removeZoomListener(l);
    }


    /**
     *
     */
    public void fireZoom(int zoomType, float amount) {
	zoomSupport.fireZoom(zoomType, amount);
    }



    protected JDialog createAboutBox() {
	java.awt.Container topContainer = getTopLevelAncestor();
	String title = "About " + Environment.get(Environment.Title);
	if (topContainer instanceof Frame) {
	    return new JDialog(
		(Frame)topContainer, title, true);
	} else {
	    JDialog d = new JDialog();
	    d.setTitle(title);
	    return d;
	}
    }



    protected JComponent createCopyrightViewer() {
	JTextArea viewer = new JTextArea(
	    MapBean.getCopyrightMessage()+
	    Environment.get("line.separator")+
	    Environment.get("line.separator")+
	    "Version " + Environment.get(Environment.Version)+
	    Environment.get("line.separator")+
	    "Build " + Environment.get(Environment.BuildDate, "<no build tag>")
	    );
	viewer.setEditable(false);
	JScrollPane scroller = new JScrollPane(viewer);
	return scroller;
    }

    protected Component createAboutControls(final JDialog window) {
	JButton button = new JButton("OK");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    window.setVisible(false);
		}
	    });
	Box box = Box.createHorizontalBox();
	box.add(button);
	return box;
    }


    /**
     * Create and add the File menu.
     */
    protected void createFileMenu() {
	// temp variables
	JCheckBoxMenuItem cb;
	JRadioButtonMenuItem rb;
	JMenuItem mi;

	// File Menu
	JMenu file = (JMenu) add(new JMenu("File"));
        file.setMnemonic('F');
        mi = (JMenuItem) file.add(new JMenuItem("About"));
        mi.setMnemonic('t');
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    if (aboutBox == null) {
			aboutBox = createAboutBox();
			aboutBox.getContentPane().setLayout(new BorderLayout());
			aboutBox.getContentPane().add(createCopyrightViewer(),
						      BorderLayout.CENTER);
			aboutBox.getContentPane().add(
		            createAboutControls(aboutBox),
			    BorderLayout.SOUTH
			    );
			aboutBox.pack();
		    }

		    aboutBox.setVisible(true);
		}
	    });

        file.add(new JSeparator());
        mi = (JMenuItem) file.add(new JMenuItem("Open"));
        mi.setMnemonic('O');
	mi.setEnabled(false);

	///////  For jdk 1.1.x compatability, comment this out, from here...
        JMenu saveMenu = (JMenu) file.add(new JMenu("Save As ..."));
	if (Environment.isApplication()) {
	    mi = saveMenu.add(new JMenuItem("JPEG"));
	    mi.setEnabled(true);
	    mi.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
			if (projComponent != null) {
			    if (projComponent instanceof MapBean) {
				try {
				    JFileChooser chooser = new JFileChooser();
				    int returnVal = chooser.showSaveDialog(getParent());
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
					String filename = chooser.getSelectedFile().getAbsolutePath();
					
					SunJPEGFormatter formatter = new SunJPEGFormatter();
					formatter.setImageQuality(.8f);
					byte[] imageBytes = 
					    formatter.getImageFromMapBean((MapBean)projComponent);
					
					FileOutputStream binFile = new FileOutputStream(filename);
					binFile.write(imageBytes);
					binFile.close();
					
					Debug.output("Created JPEG at " + filename);
				    }
				
				    return;
				} catch (IOException e) {
				    Debug.error("MenuPanel: " + e);
				}
			    }
			}
			Debug.error("MenuPanel: Cannot write to file.");
		    }
		});
	} else {
	    saveMenu.setEnabled(false);
	}
	///////////////////////  jdk1.1.x  comment out to here
	
        file.add(new JSeparator());
	if (Environment.isApplication()) {
	    mi = (JMenuItem) file.add(new JMenuItem("Quit"));
	    //         mi.setMnemonic('U');
	    mi.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			// HACK - need to call shutdown() on mapbean
			// actually we should broadcast a shutdown event so thato ther
			// gui components can clean up, and maybe only one can call
			// exit.
			System.exit(0);
		    }
		});
	}
    }


    /**
     * Create and add the Navigate menu.
     */
    protected void createNavigateMenu() {
	// temp variables
	JCheckBoxMenuItem cb;
	JMenuItem mi;

	// navigate menu
	JMenu navigateMenu = (JMenu) add(new JMenu(I18N.get(
	    "menu.navigate", "Navigate")));
	navigateMenu.setMnemonic('N');

        mi = (JMenuItem) navigateMenu.add(new JMenuItem(I18N.get(
	    "menu.navigate.coords", "Coordinates...")));
	mi.addActionListener(this);
	mi.setActionCommand(coordCmd);

	if (Environment.getBoolean(Environment.UseInternalFrames)){
	    coordDialog = new CoordInternalFrame();
	} else {
	    coordDialog2 = new CoordDialog();
	}


        projsubmenu = (JMenu) navigateMenu.add(new JMenu(I18N.get(
	    "menu.navigate.proj", "Projection")));

	createProjectionMenu();

	navigateMenu.add(new JSeparator());

        JMenu submenu = (JMenu) navigateMenu.add(new JMenu(
	    I18N.get("menu.navigate.proj.zoomin", "Zoom In")));
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.2X", "2X")));
	mi.setActionCommand(zoomIn2Cmd);
	mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.4X", "4X")));
	mi.setActionCommand(zoomIn4Cmd);
	mi.addActionListener(this);


        submenu = (JMenu) navigateMenu.add(new JMenu(
	    I18N.get("menu.navigate.proj.zoomout", "Zoom Out")));
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.2X", "2X")));
	mi.setActionCommand(zoomOut2Cmd);
	mi.addActionListener(this);
        mi = (JMenuItem) submenu.add(new JMenuItem(
	    I18N.get("menu.navigate.proj.4X", "4X")));
	mi.setActionCommand(zoomOut4Cmd);
	mi.addActionListener(this);
    }


    /**
     * Create the projection submenu.
     */
    public void createProjectionMenu() {
	JRadioButtonMenuItem rb;

	ButtonGroup group = new ButtonGroup();
	String availableProjections[] = ProjectionFactory.getAvailableProjections();

	for(int i=0;i<availableProjections.length;i++){
	    rb = (JRadioButtonMenuItem) projsubmenu.add(
		new JRadioButtonMenuItem(
		    I18N.get("menu.navigate.proj."+availableProjections[i], availableProjections[i])));
	    rb.setActionCommand(projCmd);
	    rb.setName(""+ProjectionFactory.getProjType(availableProjections[i]));
	    rb.addActionListener(this);
	    group.add(rb);
	}	
    }


    /**
     * Create and add the Control menu.
     */
    protected void createControlMenu() {
	// temp variables
	JCheckBoxMenuItem cb;
	JRadioButtonMenuItem rb;
	JMenuItem mi;

	// control menu
	JMenu controlMenu = (JMenu) add(new JMenu(I18N.get(
	    "menu.control", "Control")));
	controlMenu.setMnemonic('C');

        mouseModeSubmenu = (JMenu) controlMenu.add(new JMenu(
	    I18N.get("menu.control.mode", "Mouse Mode")));


	mi = (JMenuItem) controlMenu.add(new JMenuItem(
	    I18N.get("menu.control.redraw", "Redraw")));
	mi.setActionCommand(redrawCmd);
	mi.addActionListener(this);
    }
  
    public void setInformationDelegator(InformationDelegator in_id) {
	informationDelegator = in_id;
    }
    /**
     * Add the helpmenu to the bar.  The Help Menu uses the
     * InformationDelegator object, hence make sure you set the
     * information delegator.  
     */
    public void addHelpMenu() {
	helpMenu =  new JMenu("Help");
	helpMenu.setMnemonic('H');
	JMenuItem mi = helpMenu.add(new JMenuItem("OpenMap"));
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    //String command = e.getActionCommand();
		    if(informationDelegator!=null) {
			informationDelegator.displayURL(Environment.get(Environment.HelpURL, "http://javamap.bbn.com/projects/openmap/openmap_maindes.html"));
		    }
		}
	    });
	mi.setActionCommand("showHelp");
	//menu.setHelpMenu(helpMenu);Use this when Sun decides to implement it 
	add(helpMenu,-1);
    }

    /**
     * Creates the default MenuBar.
     */
    protected void createMenuBar() {

	// File
	createFileMenu();

	// Navigate
	createNavigateMenu();

	// Control Menu
	createControlMenu();
		
	// maybe add the Debug menu
	if (Debug.debugging("debugmenu")) {
	    createDebugMenu();
	}
    }


    /**
     * Add a menu for debugging purposes.
     */
    protected void createDebugMenu() {
	// temp variables
	JCheckBoxMenuItem cb;
	JRadioButtonMenuItem rb;
	JMenuItem mi;

	debugMenu = (JMenu) add(new JMenu("Debug"));

        mi = (JMenuItem) debugMenu.add(new JMenuItem("Dump Memory"));
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.out.println(
			"Total Memory: " + Runtime.getRuntime().totalMemory() +
			" Free Memory: " + Runtime.getRuntime().freeMemory());
		}
	    });

        mi = (JMenuItem) debugMenu.add(new JMenuItem("Dump Threads"));
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ThreadGroup tg = Thread.currentThread().getThreadGroup();
		    while (tg.getParent() != null) {
			tg = tg.getParent();
		    }
		    tg.list();
		}
	    });

        mi = (JMenuItem) debugMenu.add(new JMenuItem("Run GC"));
	mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.out.println("run GC");
		    Runtime.getRuntime().gc();
		    Runtime.getRuntime().runFinalization();
		    System.out.println("done GC");
		}
	    });

	debugMenu.add(new JSeparator());

	cb = (JCheckBoxMenuItem) debugMenu.add(
	    new JCheckBoxMenuItem("Basic"));
	cb.setSelected(Debug.debugging("basic"));
	cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		    if (cbmi.getState()) {
			Debug.put("basic");
		    } else {
			Debug.remove("basic");
		    }
		}
	    });

	cb = (JCheckBoxMenuItem) debugMenu.add(
	    new JCheckBoxMenuItem("MapBean"));
	cb.setSelected(Debug.debugging("mapbean"));
	cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		    if (cbmi.getState()) {
			Debug.put("mapbean");
		    } else {
			Debug.remove("mapbean");
		    }
		}
	    });


	cb = (JCheckBoxMenuItem) debugMenu.add(
	    new JCheckBoxMenuItem("Specialist Layer"));
	cb.setSelected(Debug.debugging("cspec"));
	cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		    if (cbmi.getState()) {
			Debug.put("cspec");
		    } else {
			Debug.remove("cspec");
		    }
		}
	    });


	cb = (JCheckBoxMenuItem) debugMenu.add(
	    new JCheckBoxMenuItem("OMGraphics"));
	cb.setSelected(Debug.debugging("omGraphics"));
	cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		    if (cbmi.getState()) {
			Debug.put("omGraphics");
		    } else {
			Debug.remove("omGraphics");
		    }
		}
	    });


	cb = (JCheckBoxMenuItem) debugMenu.add(
	    new JCheckBoxMenuItem("Projection"));
	cb.setSelected(Debug.debugging("proj"));
	cb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem)e.getSource();
		    if (cbmi.getState()) {
			Debug.put("proj");
		    } else {
			Debug.remove("proj");
		    }
		}
	    });
    }


    /**
     * ActionListener interface.
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
	// 	System.out.println("Action: " + e);
	String command = e.getActionCommand();

	Debug.message("menupanel", "MenuPanel.actionPerformed(): " + command);


	if (command.equals(zoomIn2Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 0.5f);
	}
	else if (command.equals(zoomIn4Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 0.25f);
	}
	else if (command.equals(zoomOut2Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 2.0f);
	}
	else if (command.equals(zoomOut4Cmd)) {
	    fireZoom(ZoomEvent.RELATIVE, 4.0f);
	}
	else if (command.equals(redrawCmd)) {
	    if (projComponent != null) {
		// We should consider doing a hard repaint here
		// instead of just calling repaint.  When we
		// get the map working with the offscreen buffer
		// this call might tell the map to regenerate
		// the offscreen buffer.  -tcm 5/9/98
	        if (projComponent instanceof MapBean) {
		    ((MapBean)projComponent).setBufferDirty(true);
		}
		projComponent.repaint();
	    }
	}
	else if (command.startsWith(projCmd)) {
	    JRadioButtonMenuItem rb = (JRadioButtonMenuItem)(e.getSource());
	    int projType = Short.parseShort(rb.getName());
	    Debug.message("menupanel", "MenuPanel.projType: " + projType);
	    Projection newProj =
		ProjectionFactory.makeProjection(projType, projection);
	    fireProjectionChanged(newProj);
	}
	else if (command.equals(coordCmd)) {
	    if (Environment.getBoolean(Environment.UseInternalFrames)){
		if (coordDialog.isIcon()) {
		    try {
			coordDialog.setIcon(false);
		    }
		    catch (PropertyVetoException pv ) {
			System.err.println("setIcon(false) vetoed!" +pv);
		    }
		} else {
		    Component obj = getParent();
		    while (!(obj instanceof JLayeredPane)) {
			obj = obj.getParent();
		    }
		    ((JLayeredPane)obj).add(coordDialog);
		}
	    } else {
		coordDialog2.setVisible(true);
	    }
	}
	else if (command.equals(mouseModeCmd)) {
	    JRadioButtonMenuItem rb = (JRadioButtonMenuItem)(e.getSource());
	    if (Debug.debugging("menupanel")) {
		System.out.println("MenuPanel.actionPerformed(): " + rb.getName());
	    }
 	    mouseDelegator.setActiveMouseModeWithID(rb.getName());
	}
    }

    /**
     * This method gets called when a bound property is changed.<p>
     * @param evt A PropertyChangeEvent object describing the event source 
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
	Debug.message("menupanel", "MenuPanel.propertyChange()");

 	if (evt.getPropertyName() == MouseDelegator.ActiveModeProperty) {
 	    // Mark the radio button representing the new mode as active
 	    String mmID = ((MapMouseMode)evt.getNewValue()).getID();
 	    for (int i=0; i < mouseModeButtons.length; i++) {
 		//System.out.println(mmID + " " +mouseModeButtons[i].getName());
 		if (mouseModeButtons[i].getName().equals(mmID)) {
 		    mouseModeButtons[i].setSelected(true);
 		    //System.out.println("MenuPanel: New Active Mode " + mmID);
 		    break;
 		}
 	    }
 	}

 	else if (evt.getPropertyName() == MouseDelegator.MouseModesProperty) {
	    if (mouseModeSubmenu != null) {
		// Redo the whole submenu
		for (int i=0; i < mouseModeButtons.length; i++) {
		    mouseModeSubmenu.remove(mouseModeButtons[i]);
		}
		MapMouseMode[] modes = mouseDelegator.getMouseModes();
		String activeMode = mouseDelegator.getActiveMouseModeID();
		mouseModeButtons = new JRadioButtonMenuItem[modes.length];
		for (int mms = 0; mms < modes.length; mms++){
		    mouseModeButtons[mms] = 
			(JRadioButtonMenuItem) mouseModeSubmenu.add(
			    new JRadioButtonMenuItem(I18N.get(
				"menu.control.mode." + modes[mms].getID(), 
				modes[mms].getID())));
		    mouseModeButtons[mms].setActionCommand(mouseModeCmd);
		    mouseModeButtons[mms].setName(modes[mms].getID());
		    mouseModeButtons[mms].addActionListener(this);
		    group2.add(mouseModeButtons[mms]);
		    if ((activeMode != null) &&
			activeMode.equals(modes[mms].getID()))
			mouseModeButtons[mms].setSelected(true);
		}
	    }
 	}
	//  	else if (evt.getPropertyName() == Map.ProjectionTypeProperty) {
	//  	    // Change the selected projection menu item
	//  	    for (int i=0; i<projsubmenu.getItemCount(); i++) {
	//  		if (projsubmenu.getItem(i).getName().equals(
	//  				    ((Short)evt.getNewValue()).toString())) 
	//  		{
	//  		    projsubmenu.getItem(i).setSelected(true);
	//  		    break;
	//  		}
	//  	    }
	//  	}


    }


    //------------------------------------------------------------
    // ProjectionListener interface
    //------------------------------------------------------------

    /**
     * The Map projection has changed.
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
	if (Debug.debugging("menupanel")) {
	    System.out.println("MenuPanel.projectionChanged()");
	}
	Projection newProj = e.getProjection();
	if (projection == null ||  (! projection.equals(newProj))) {
	    setProjection((Projection) newProj.makeClone());
	    Object source = e.getSource();
	    if (source instanceof java.awt.Component) {
		projComponent = (java.awt.Component)source;
	    }
	}
    }

    protected void findAndInit(Iterator it) {
	Object someObj;
	while(it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof LayersMenu) {
		Debug.message("menupanel","MenuPanel found LayerHandler object..getting layers menu");
		add((LayersMenu)someObj);
	    }
	    if (someObj instanceof MapBean) {
		Debug.message("menupanel","MenuPanel found MapBean object..setting up listeners with mapbean");
		setupListeners((MapBean)someObj);
	    }
	    if (someObj instanceof MouseDelegator) {
		// do the initializing that need to be done here
		Debug.message("menupanel","MenuPanel found a mouse delegator Object");
		setMouseDelegator((MouseDelegator)someObj);
	    }
	    if(someObj instanceof InformationDelegator) {
		informationDelegator = (InformationDelegator)someObj;
	    }
	    
	}
    }
    // Methods for BeanContextMembershipListener interface
    public void childrenAdded(BeanContextMembershipEvent bcme) {
	findAndInit(bcme.iterator());
    }
  
    public void childrenRemoved(BeanContextMembershipEvent bcme) {
	Iterator it = bcme.iterator();
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof com.bbn.openmap.MapBean) {
		Debug.message("layerhandler","MapBean object is being removed");
		undoListeners((MapBean)someObj);	      
	    }
	    if (someObj instanceof MouseDelegator) {
		unsetMouseDelegator((MouseDelegator) someObj);
	    }
	    if (someObj instanceof LayersMenu) {
		remove((LayersMenu)someObj);
	    }
	    if(someObj instanceof InformationDelegator) {
		informationDelegator = null;
	    }
	}
    }

    // Methods for BeanContextChild interface
    public BeanContext getBeanContext() {
	return beanContextChildSupport.getBeanContext();
    }
  
    public void setBeanContext(BeanContext in_bc) throws PropertyVetoException {

	if(in_bc != null) {
	    in_bc.addBeanContextMembershipListener(this);
	    beanContextChildSupport.setBeanContext(in_bc);
	    findAndInit(in_bc.iterator());
	}
    }
     
    public void addVetoableChangeListener(String propertyName, 
					  VetoableChangeListener in_vcl) {
	beanContextChildSupport.addVetoableChangeListener(propertyName, in_vcl);
    }
  
    public void removeVetoableChangeListener(String propertyName, 
					     VetoableChangeListener in_vcl) {
	beanContextChildSupport.removeVetoableChangeListener(propertyName, in_vcl);
    }

    /**
     * Report a vetoable property update to any registered listeners. 
     * If anyone vetos the change, then fire a new event 
     * reverting everyone to the old value and then rethrow 
     * the PropertyVetoException. <P>
     *
     * No event is fired if old and new are equal and non-null.
     * <P>
     * @param name The programmatic name of the property that is about to
     * change
     * 
     * @param oldValue The old value of the property
     * @param newValue - The new value of the property
     * 
     * @throws PropertyVetoException if the recipient wishes the property
     * change to be rolled back.
     */
    public void fireVetoableChange(String name, 
				   Object oldValue, 
				   Object newValue) 
	throws PropertyVetoException {
	super.fireVetoableChange(name, oldValue, newValue);
	beanContextChildSupport.fireVetoableChange(name, oldValue, newValue);
    }

}
