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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/Attic/ProjectionMenu.java,v $
// $RCSfile: ProjectionMenu.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;

/**
 *  Provides ProjectionMenu items
 */
public class ProjectionMenu extends AbstractOpenMapMenu 
  implements ActionListener, ProjectionListener {

    protected ProjectionSupport projectionSupport= new ProjectionSupport(this);
    protected transient Projection projection;
    protected transient java.awt.Component projComponent;
    public final static transient String projCmd = "setProj";
    
    
    /**
     * Create the projection submenu.
     */
    public ProjectionMenu() {
	super();
	setText("Projection");
	JRadioButtonMenuItem rb;
	
	ButtonGroup group = new ButtonGroup();
	String availableProjections[] = ProjectionFactory.getAvailableProjections();
	
	for (int i=0; i < availableProjections.length; i++) {
	    rb = (JRadioButtonMenuItem) add(
		new JRadioButtonMenuItem(
		    I18N.get("menu.navigate.proj."+availableProjections[i], availableProjections[i])));
	    rb.setActionCommand(projCmd);
	    rb.setName(""+ProjectionFactory.getProjType(availableProjections[i]));
	    rb.addActionListener(this);
	    group.add(rb);
	}	
    }
    
    public void actionPerformed(ActionEvent ae) {
    	String command = ae.getActionCommand();
	
	Debug.message("projectionmenu", "ProjectionMenu.actionPerformed(): " + command);
	
	if (command.startsWith(projCmd)) {
	    JRadioButtonMenuItem rb = (JRadioButtonMenuItem)(ae.getSource());
	    int projType = Short.parseShort(rb.getName());
	    Debug.message("projectionmenu", "ProjectionMenu.projType: " + projType);
	    Projection newProj =
		ProjectionFactory.makeProjection(projType, projection);
	    fireProjectionChanged(newProj);
	}
	
    }
    
    //------------------------------------------------------------
    // ProjectionListener interface
    //------------------------------------------------------------
    
    /**
     * The Map projection has changed.
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
	if (Debug.debugging("projectionmenu")) {
	    System.out.println("ProjectionMenu.projectionChanged()");
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
    
    /**
     * Set the projection.
     * This changes the setting of the projection radio button menu.
     * @param aProjection Projection
     */
    protected synchronized void setProjection(Projection aProjection) {
	projection = aProjection;
	
	int projType = projection.getProjectionType();
	
	// Change the selected projection type menu item
	for (int i=0; i<getItemCount(); i++) {
	    try {
		JMenuItem item = getItem(i);
		int projID = Integer.parseInt(item.getName());
		if (projID == projType) {
		    getItem(i).setSelected(true);
		}
	    } catch (NumberFormatException e) {
		e.printStackTrace();
	    }
	}
    }
    
    
    
    /** 
     *  Convenience function for setting up listeners
     */
    public void setupListeners(MapBean map) {  
	Debug.message("projectionmenu","ProjectionMenu | setupListeners");
	addProjectionListener(map);   
	map.addProjectionListener(this);    
    }
    
    /** 
     *  Convenience function for undoing set up listeners
     */
    public void undoListeners(MapBean map) {
	removeProjectionListener(map);   
	map.removeProjectionListener(this);   
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
    
    public void findAndInit(Iterator it) {
	Object someObj;
	while (it.hasNext()) {
	    someObj = it.next();
	    if (someObj instanceof MapBean) {
		setupListeners((MapBean)someObj);
	    }
	}
    }
    
    public void findAndUnInit(Iterator it) {
    }
}
