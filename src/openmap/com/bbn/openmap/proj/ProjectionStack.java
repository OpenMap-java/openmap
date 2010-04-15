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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/proj/ProjectionStack.java,v $
// $RCSfile: ProjectionStack.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.proj;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.OMComponent;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;

/**
 * Provides Projection Stack, to listen for projection changes and remember them
 * as they pass by. As a Tool, it provides a GUI so that past projections can be
 * retrieved, and, if a past projection is being displayed, a forward projection
 * stack is activated to provide a path to get to the last projection set in the
 * MapBean. ProjectionStackTriggers should hook themselves up to the
 * ProjectionStack. The ProjectionStack is responsible for finding and
 * connecting to the MapBean.
 */
public class ProjectionStack extends OMComponent implements ActionListener,
		ProjectionListener {

	private final static Logger logger = Logger
			.getLogger("com.bbn.openmap.proj.ProjectionStack");

	public final static int DEFAULT_MAX_SIZE = 10;
	public final static int REMEMBER_ALL = -1;
	/**
	 * The currentProjection should be the top item on the backStack.
	 */
	protected transient ProjHolder currentProjection;
	protected transient String currentProjectionID;
	protected transient Container face;
	protected transient MapBean mapBean;
	protected int stackSize = DEFAULT_MAX_SIZE;

	public final static transient String BackProjCmd = "backProjection";
	public final static transient String ForwardProjCmd = "forwardProjection";
	public final static transient String ClearBackStackCmd = "clearBackStack";
	public final static transient String ClearForwardStackCmd = "clearForwardStack";
	public final static transient String ClearStacksCmd = "clearStacks";

	protected Stack<ProjHolder> backStack;
	protected Stack<ProjHolder> forwardStack;

	protected ProjectionStackSupport triggers;

	/**
	 * Create the projection submenu.
	 */
	public ProjectionStack() {
	}

	public void setMapBean(MapBean map) {
		if (mapBean != null) {
			mapBean.removeProjectionListener(this);
		}

		if (map != null) {
			map.addProjectionListener(this);
		}
		mapBean = map;
	}

	public MapBean getMapBean() {
		return mapBean;
	}

	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand().intern();

		logger.fine("Received command: " + command);

		boolean changeProjection = false;

		// This is important. We need to set the current projection
		// before setting the projection in the MapBean. That way,
		// the projectionChanged method actions won't get fired
		if (command == BackProjCmd && backStack != null && backStack.size() > 1) {

			pop();
			currentProjection = backStack.peek();
			changeProjection = true;
		} else if (command == ForwardProjCmd && forwardStack != null
				&& !forwardStack.empty()) {

			currentProjection = backPop();
			changeProjection = true;
		} else {
			clearStacks(
					(command == ClearBackStackCmd || command == ClearStacksCmd),
					(command == ClearForwardStackCmd || command == ClearStacksCmd));
			// fireStackStatus is called in clearStacks
		}

		if (changeProjection && mapBean != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("changing mapbean projection to : "
						+ currentProjection);
			}

			Projection currProj = currentProjection.create(mapBean.getWidth(),
					mapBean.getHeight());
			mapBean.setProjection(currProj);
			fireStackStatus();
		}
	}

	// ------------------------------------------------------------
	// ProjectionListener interface
	// ------------------------------------------------------------

	protected ProjectionFactory getProjectionFactory(ProjectionEvent e) {
		Object obj = e.getSource();
		if (obj instanceof MapBean) {
			return ((MapBean) obj).getProjectionFactory();
		} else if (mapBean != null) {
			return mapBean.getProjectionFactory();
		}
		return ProjectionFactory.loadDefaultProjections();
	}

	/**
	 * The Map projection has changed.
	 * 
	 * @param e
	 *            ProjectionEvent
	 */
	public void projectionChanged(ProjectionEvent e) {
		logger.fine("ProjectionStack.projectionChanged()");
		Projection newProj = e.getProjection();

		// If the ProjectionStack doesn't already know about the
		// projection change, that means that it didn't instigate it,
		// and the new projection needs to get added to the stack,
		// with the forwardStack cleared.
		if (currentProjection == null || !currentProjection.equals(newProj)) {
			logger.fine("pushing projection on backStack");
			// push on the backStack, clear the forwardStack;
			currentProjection = push(new ProjHolder(newProj,
					getProjectionFactory(e)));
			if (forwardStack != null) {
				forwardStack.clear();
			}
			fireStackStatus();
		} else {
			logger
					.fine("new projection matches current projection, no action.");
		}
	}

	/**
	 * Clear out the chosen projection stacks and fire an event to update the
	 * triggers on stack status.
	 * 
	 * @param clearBackStack
	 *            clear out the backward projection stack.
	 * @param clearForwardStack
	 *            clear out the forward projection stack.
	 */
	public synchronized void clearStacks(boolean clearBackStack,
			boolean clearForwardStack) {

		if (clearBackStack && backStack != null) {
			ProjHolder currentProj = pop(); // current projection
			backStack.clear();
			push(currentProj);
		}

		if (clearForwardStack && forwardStack != null) {
			forwardStack.clear();
		}
		fireStackStatus();
	}

	/**
	 * Take a ProjHolder off the backStack, and push it on the forward stack.
	 * 
	 * @return the ProjHolder pushed onto the forwardStack.
	 */
	protected synchronized ProjHolder pop() {
		ProjHolder proj = backStack.pop();

		if (forwardStack == null) {
			forwardStack = new Stack<ProjHolder>();
		}

		while (forwardStack.size() >= stackSize) {
			forwardStack.removeElementAt(0);
		}
		forwardStack.push(proj);
		return proj;
	}

	/**
	 * Take a ProjHolder off the forwardStack, and push it on the backStack.
	 * 
	 * @return the ProjHolder pushed on the backStack.
	 */
	protected synchronized ProjHolder backPop() {
		ProjHolder proj = forwardStack.pop();

		// This has almost no chance of happening...
		if (backStack == null) {
			backStack = new Stack<ProjHolder>();
		}

		while (backStack.size() >= stackSize) {
			backStack.removeElementAt(0);
		}
		backStack.push(proj);
		return proj;
	}

	/**
	 * Put a new ProjHolder on the backStack, to remember for later in case we
	 * need to back up.
	 * 
	 * @param proj
	 *            ProjHolder.
	 * @return the ProjHolder pushed on the backStack.
	 */
	protected synchronized ProjHolder push(ProjHolder proj) {
		if (backStack == null) {
			backStack = new Stack<ProjHolder>();
		}

		if (backStack.size() >= stackSize) {
			backStack.removeElementAt(0);
		}
		return backStack.push(proj);
	}

	public void fireStackStatus() {
		fireStackStatus((backStack != null && backStack.size() > 1),
				(forwardStack != null && !forwardStack.empty()));
	}

	public void fireStackStatus(boolean enableBackButton,
			boolean enableForwardButton) {
		if (triggers != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("back enabled: " + enableBackButton
						+ ", forward enabled: " + enableForwardButton);
			}
			triggers.fireStackStatus(enableBackButton, enableForwardButton);
		}
	}

	/**
	 * ProjectionStackTriggers should call this method, and all will be well.
	 */
	public void addProjectionStackTrigger(ProjectionStackTrigger trigger) {
		trigger.addActionListener(this);
		if (triggers == null) {
			triggers = new ProjectionStackSupport();
		}
		triggers.add(trigger);
		trigger.updateProjectionStackStatus((backStack != null && backStack
				.size() > 1), (forwardStack != null && !forwardStack.empty()));
	}

	/**
	 * ProjectionStackTriggers should call this method, and all will be well.
	 */
	public void removeProjectionStackTrigger(ProjectionStackTrigger trigger) {
		trigger.removeActionListener(this);
		if (triggers != null) {
			triggers.remove(trigger);
			if (triggers.size() == 0) {
				triggers = null;
			}
		}
	}

	// ------------------------------------------------------------
	// BeanContextMembershipListener and BeanContextChild interface
	// ------------------------------------------------------------

	/**
	 * Look at the object received in a MapHandler status message and disconnect
	 * from it if necessary.
	 */
	public void findAndUndo(Object someObj) {
		if (someObj instanceof com.bbn.openmap.MapBean) {
			logger.fine("ProjectionStack removing a MapBean.");
			MapBean map = getMapBean();
			if (map != null && map == (MapBean) someObj) {
				setMapBean(null);
			}
		}
	}

	/**
	 * Look at the object received in a MapHandler status message and connect to
	 * it if necessary.
	 */
	public void findAndInit(Object someObj) {
		if (someObj instanceof com.bbn.openmap.MapBean) {
			logger.fine("ProjectionStack found a MapBean.");
			setMapBean((MapBean) someObj);
		}
	}

	public class ProjHolder {

		public Class<? extends Projection> projClass;
		public float scale;
		public Point2D center;
		protected Point tmpPoint1;
		protected Point tmpPoint2;
		protected ProjectionFactory projFactory;

		public ProjHolder(Projection proj, ProjectionFactory projectionFactory) {
			projClass = proj.getClass();
			scale = proj.getScale();
			center = proj.getCenter();
			projFactory = projectionFactory;
		}

		public boolean equals(Projection proj) {
			// For some reason, the ProjectionFactory can mess up the
			// center lat/lons, so that the center isn't EXACTLY what
			// they were when the projection was created. It's almost
			// like it decides what map it can draw, and then figures
			// out what the coordinate of the center pixel of the
			// projection it created was. Doing this projection hack
			// seems to accurately determine what projections are
			// actually identical visually, which is what you want to
			// know anyway.
			Point2D tmpPoint1 = proj.forward(proj.getCenter());
			Point2D tmpPoint2 = proj.forward(center);

			boolean same = (projClass == proj.getClass()
					&& scale == proj.getScale() &&

					// NOT GOOD ENOUGH! Sometimes, the
					// slighest difference causes a false
					// false.

					// MoreMath.approximately_equal(center.getLatitude(),
					// proj.getCenter().getLatitude(),
					// .00001f) &&
					// MoreMath.approximately_equal(center.getLongitude(),
					// proj.getCenter().getLongitude(),
					// .00001f)
					// This seems to work...
					tmpPoint1.getX() == tmpPoint2.getX() && tmpPoint1.getY() == tmpPoint2
					.getY());
			return same;
		}

		public Projection create(int width, int height) {
			return projFactory.makeProjection(projClass, center, scale, width,
					height);
		}

		public String toString() {
			return ("[ProjHolder: class(" + projClass.getName() + "), scale("
					+ scale + "), center(" + center + ")]");
		}

	}
}
