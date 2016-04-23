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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/MenuList.java,v $
// $RCSfile: MenuList.java,v $
// $Revision: 1.6 $
// $Date: 2007/03/08 17:35:30 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.gui.MenuBar;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The MenuList is a component that creates a set of JMenus from Properties, and
 * can provide a JMenuBar or JMenu with those JMenus. Used by the MapPanel.
 * <P>
 * 
 * The MenuList can be used in lieu of defining the OpenMap MenuBar and each
 * Menu in the openmap.components property. The MenuList can be defined instead,
 * with the menus it should create. It has one property:
 * 
 * <pre>
 *   
 *   
 *    menulist.menus=menu1 menu2 menu3
 *    menu1.class=classname of menu1
 *    menu2.class=classname of menu2
 *    menu3.class=classname of menu3
 * 
 * 
 * </pre>
 * 
 * When the MenuList.setBeanContext() method gets called, the MenuList will add
 * its menus to that MapHandler/BeanContext.
 * 
 * By default, the MenuList will provide an OpenMap MenuBar when asked for a
 * JMenuBar, which will figure out if one of the child menus is a HelpMenu and
 * place it at the end of the MenuBar menus.
 */
public class MenuList extends OMComponent {

	public final static String MenusProperty = "menus";
	public final static String MenuNameProperty = "name";

	protected List<JMenu> menuList;

	protected String name = "Map";

	/**
	 * Create an empty MenuList.
	 */
	public MenuList() {
		menuList = new ArrayList<JMenu>();
	}

	/**
	 * Get a MenuBar with JMenus on it. If the MenuList has been given a
	 * MapHandler, the Menus will have been added to it, and therefore will be
	 * connected to OpenMap components. The MenuBar is not added to the
	 * MapHandler and probably shouldn't be, since it will find and re-add the
	 * Menus it finds there in some random order.
	 */
	public JMenuBar getMenuBar() {
		MenuBar menuBar = new MenuBar();

		for (JMenu menuu : menuList) {
			menuBar.add(menuu);
		}

		return menuBar;
	}

	/**
	 * Get a JMenu with JMenus on it as sub-menus. If the MenuList has been
	 * given a MapHandler, the Menus will have been added to it, and therefore
	 * will be connected to OpenMap components. This menu will be named "Map",
	 * but you can rename it if you want.
	 */
	public JMenu getMenu() {
		JMenu menu = new JMenu(name);

		for (JMenu menuu : menuList) {
			menu.add(menuu);
		}

		return menu;
	}

	/**
	 * The MenuList will look for the "menus" property and build its menus.
	 */
	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		String realPrefix = PropUtils.getScopedPropertyPrefix(prefix);
		name = props.getProperty(prefix + MenuNameProperty, name);

		Vector<String> menuItems = PropUtils.parseSpacedMarkers(props.getProperty(realPrefix + MenusProperty));
		if (!menuItems.isEmpty()) {

			for (String itemPrefix : menuItems) {
				String classProperty = itemPrefix + ".class";
				String className = props.getProperty(classProperty);
				if (className == null) {
					Debug.error("MenuList.setProperties(): Failed to locate property \"" + classProperty
							+ "\"\n  Skipping menu \"" + itemPrefix + "\"");
					continue;
				}

				Object obj = ComponentFactory.create(className, itemPrefix, props);
				if (obj instanceof JMenu) {
					menuList.add((JMenu) obj);
				}
			}
		} else {
			if (Debug.debugging("menu")) {
				Debug.output("MenuList created without menus in properties");
			}
		}
	}

	/**
	 * PropertyConsumer interface method.
	 */
	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		StringBuffer itemList = new StringBuffer();

		for (JMenu menu : menuList) {
			if (menu instanceof PropertyConsumer) {
				PropertyConsumer ps = (PropertyConsumer) menu;
				String prefix = ps.getPropertyPrefix();
				if (prefix == null) {
					prefix = menu.getText().toLowerCase();
					ps.setPropertyPrefix(prefix);
				}

				itemList.append(prefix).append(" ");
				ps.getProperties(props);
			}
		}

		String prefix = PropUtils.getScopedPropertyPrefix(this);
		props.put(prefix + MenusProperty, itemList.toString().trim());
		props.put(prefix + MenuNameProperty, PropUtils.unnull(name));

		return props;
	}

	/**
	 * PropertyConsumer interface method.
	 */
	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);
		PropUtils.setI18NPropertyInfo(i18n, props, MenuList.class, MenusProperty, "List of Menus",
				"List of marker names for menu component properties.", null);
		PropUtils.setI18NPropertyInfo(i18n, props, MenuList.class, MenuNameProperty, "Name",
				"Name of the Menu provided by the MenuList.", null);
		return props;
	}

	/**
	 * Called when the MenuList is added to the MapHandler/BeanContext. The
	 * MenuList will add its menus to the BeanContext.
	 */
	public void setBeanContext(BeanContext bc) throws PropertyVetoException {

		super.setBeanContext(bc);
		if (bc != null) {
			for (JMenu menu : menuList) {
				bc.add(menu);
			}
		}
	}

	public void findAndInit(Object obj) {
		if (obj instanceof JMenu) {
			menuList.add((JMenu) obj);
		}
	}

	public void findAndUndo(Object obj) {
		if (obj instanceof JMenu) {
			menuList.remove((JMenu) obj);
		}
	}

	public void add(JMenu menu) {
		menuList.add(menu);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Builder method.
	 * 
	 * @param name used on Map menu.
	 * @return this.
	 */
	public MenuList withName(String name) {
		setName(name);
		return this;
	}

	/**
	 * Return a MenuList with a standard load of menus.
	 * 
	 * @return MenuList
	 */
	public static MenuList standardConfig() {
		MenuList menuList = new MenuList();
		menuList.add(new com.bbn.openmap.gui.FileMenu());
		menuList.add(new com.bbn.openmap.gui.NavigateMenu());
		menuList.add(new com.bbn.openmap.gui.ControlMenu());
		menuList.add(new com.bbn.openmap.gui.LayersMenu());
		menuList.add(new com.bbn.openmap.gui.GoToMenu());
		return menuList;
	}
}
