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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsMenu.java,v
// $
// $RCSfile: SaveAsMenu.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 17:51:43 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import javax.swing.JMenu;

import com.bbn.openmap.gui.AbstractOpenMapMenu;
import com.bbn.openmap.image.AbstractImageFormatter;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;

public class SaveAsMenu extends AbstractOpenMapMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 977535233425016853L;

	public SaveAsMenu() {
		this("Save Map As");
	}

	public SaveAsMenu(String title) {
		super(title);
		add(new SaveAsJpegMenuItem());
		add(new SaveAsGifMenuItem());
		add(new SaveAsPngMenuItem());

		addSVGMenuItem(this);
	}

	/**
	 * Add the SVG menu item to the given menu if Batik can be found in the
	 * classpath.
	 * 
	 * @param menu JMenu to add SVG option to
	 */
	protected void addSVGMenuItem(JMenu menu) {
		try {

			// This is a test to see if the batik package is
			// available. If it isn't, this statement should
			// throw an exception, and the SVG option will not be
			// added to the SaveAs Menu item.

			if (Class.forName("org.apache.batik.swing.JSVGCanvas") != null
					&& Class.forName("org.w3c.dom.ElementTraversal") != null
					&& Class.forName("com.bbn.openmap.image.SVGFormatter") != null) {

				// Need to do this in a way that allows it to be created if it's
				// there, but gracefully skipped from compilation if the batik
				// library is unavailable
				Object obj = ComponentFactory.create("com.bbn.openmap.image.SVGFormatter");
				if (obj instanceof AbstractImageFormatter) {
					menu.add(new SaveAsImageMenuItem("SVG", (AbstractImageFormatter) obj));
				}
			}
			return;

		} catch (ClassNotFoundException cnfe) {
		} catch (NoClassDefFoundError ncdfe) {
		}

		if (Debug.debugging("basic")) {
			Debug.output("SVG not added to the Save As options, because Batik was not found in classpath.");
		}
	}
}