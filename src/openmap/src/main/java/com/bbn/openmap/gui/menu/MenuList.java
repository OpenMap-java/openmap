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
import java.util.Iterator;
import java.util.LinkedList;
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

    protected LinkedList menus;

    protected MenuBar menuBar;
    protected JMenu menu;
    protected String name = "Map";

    /**
     * Create an empty MenuList.
     */
    public MenuList() {
        menus = new LinkedList();
    }

    /**
     * Get a MenuBar with JMenus on it. If the MenuList has been given a
     * MapHandler, the Menus will have been added to it, and therefore will be
     * connected to OpenMap components. The MenuBar is not added to the
     * MapHandler and probably shouldn't be, since it will find and re-add the
     * Menus it finds there in some random order.
     */
    public JMenuBar getMenuBar() {
        if (menuBar == null) {
            menuBar = new MenuBar();
        }

        menuBar.removeAll();
        Iterator iterator = menus.iterator();
        while (iterator.hasNext()) {
            menuBar.findAndInit(iterator.next());
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
        if (menu == null) {
            menu = new JMenu(name);
        }

        menu.removeAll();
        Iterator iterator = menus.iterator();
        while (iterator.hasNext()) {
            menu.add((JMenu) iterator.next());
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

        Vector menuItems = PropUtils.parseSpacedMarkers(props.getProperty(realPrefix
                + MenusProperty));
        if (!menuItems.isEmpty()) {

            int nMenuItems = menuItems.size();

            if (Debug.debugging("menu")) {
                Debug.output("MenuList created with " + nMenuItems + " menus"
                        + (nMenuItems == 1 ? "" : "s") + " in properties");
            }

            for (int i = 0; i < nMenuItems; i++) {
                String itemPrefix = (String) menuItems.elementAt(i);
                String classProperty = itemPrefix + ".class";
                String className = props.getProperty(classProperty);
                if (className == null) {
                    Debug.error("MenuList.setProperties(): Failed to locate property \""
                            + classProperty
                            + "\"\n  Skipping menu \""
                            + itemPrefix + "\"");
                    continue;
                }

                Object obj = ComponentFactory.create(className,
                        itemPrefix,
                        props);
                if (obj instanceof JMenu) {
                    menus.add(obj);
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
        Iterator iterator = menus.iterator();
        while (iterator.hasNext()) {
            JMenu menu = (JMenu) iterator.next();

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
        props.put(prefix + MenusProperty, itemList.toString());
        props.put(prefix + MenuNameProperty, name);

        return props;
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                MenuList.class,
                MenusProperty,
                "List of Menus",
                "List of marker names for menu component properties.",
                null);
        PropUtils.setI18NPropertyInfo(i18n,
                props,
                MenuList.class,
                MenuNameProperty,
                "Name",
                "Name of the Menu provided by the MenuList.",
                null);
        return props;
    }

    /**
     * Called when the MenuList is added to the MapHandler/BeanContext. The
     * MenuList will add its menus to the BeanContext.
     */
    public void setBeanContext(BeanContext bc) throws PropertyVetoException {

        super.setBeanContext(bc);
        Iterator it = menus.iterator();
        while (bc != null && it.hasNext()) {
            bc.add(it.next());
        }
    }

}
