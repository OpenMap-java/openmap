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
// $Source: /cvs/darwars/ambush/aar/src/com/bbn/hotwash/gui/HotwashPanel.java,v $
// $RCSfile: HotwashPanel.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.util.logging.Logger;

import javax.swing.JSplitPane;

import com.bbn.openmap.util.Debug;

/**
 * The HotwashPanel is a HotwashPanel and OMComponentPanel that is the heart of
 * the OpenMap application framework. It can be used in a application or applet.
 * The Panel has a BorderLayout. It creates a MapHandler to use to hold all of
 * its OpenMap components, and uses the PropertyHandler given to it in its
 * constructor to create and configure all of the application components. The
 * best way to add components to the HotwashPanel is to get the MapHandler from
 * it and add the component to that. The BasocAARPanel also adds itself to its
 * MapHandler, so when the PropertyHandler adds components to the MapHandler,
 * the HotwashPanel is able to find them via the findAndInit method. By default,
 * the HotwashPanel looks for MapPanelChildren and asks them for where they
 * would prefer to be located (BorderLayout.NORTH, BorderLayout.SOUTH,
 * BorderLayout.EAST, BorderLayout.WEST).
 */
public class HotwashPanel extends OMComponentPanel {
    public static Logger logger = Logger.getLogger("HotwashPanel");
    protected JSplitPane leftSlider;
    protected JSplitPane rightSlider;
    protected JSplitPane bottomSlider;

    /**
     * Create an empty HotwashPanel that creates its own empty PropertyHandler.
     */
    public HotwashPanel() {
        create();
    }

    /**
     * The method that triggers setLayout() and createComponents() to be called.
     * If you've told the HotwashPanel to delay creation, you should call this
     * method to trigger the PropertyHandler to create components based on the
     * contents of its properties.
     */
    public void create() {
        WindowSupport.setDefaultWindowSupportDisplayType(WindowSupport.Dlg.class);
        setLayout(createLayoutManager());
        leftSlider = new JSplitPane();
        leftSlider.setBorder(null);
        leftSlider.setResizeWeight(0);
        leftSlider.setOneTouchExpandable(true);

        rightSlider = new JSplitPane();
        rightSlider.setBorder(null);
        rightSlider.setResizeWeight(1);
        rightSlider.setOneTouchExpandable(true);

        rightSlider.setLeftComponent(leftSlider);
        rightSlider.setRightComponent(null);

        if (false) {
            bottomSlider = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            bottomSlider.setBorder(null);
            bottomSlider.setResizeWeight(1);
            bottomSlider.setOneTouchExpandable(true);
            leftSlider.setRightComponent(bottomSlider);
        }

        super.add(rightSlider, BorderLayout.CENTER);
    }

    public Component add(Component comp, String location) {
        if (location == BorderLayout.EAST) {
            rightSlider.setRightComponent(comp);
        } else if (location == BorderLayout.WEST) {
            leftSlider.setLeftComponent(comp);
        } else if (location == BorderLayout.CENTER) {
            if (bottomSlider != null) {
                bottomSlider.setTopComponent(comp);
            } else {
                leftSlider.setRightComponent(comp);
            }
        } else if (location == BorderLayout.NORTH && bottomSlider != null) {
            bottomSlider.setBottomComponent(comp);
        } else {
            super.add(comp, location);
        }
        return comp;
    }

    /**
     * The constructor calls this method that sets the LayoutManager for this
     * HotwashPanel. It returns a BorderLayout by default, but this method can
     * be overridden to change how the HotwashPanel places components. If you
     * change what this method returns, you should also change how components
     * are added in the findAndInit() method.
     */
    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    // Map Component Methods:
    // //////////////////////

    /**
     * The HotwashPanel looks for MapPanelChild components, finds out from them
     * where they prefer to be placed, and adds them.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof MapPanelChild && someObj instanceof Component) {
            String parentName = ((MapPanelChild) someObj).getParentName();
            String myName = getPropertyPrefix();

            boolean makeMyChild = (myName != null && parentName != null && myName.equalsIgnoreCase(parentName))
                    || (myName == null && parentName == null);

            if (makeMyChild) {

                logger.info("HotwashPanel: adding "
                        + someObj.getClass().getName() + " to "
                        + ((MapPanelChild) someObj).getPreferredLocation());

                MapPanelChild mpc = (MapPanelChild) someObj;
                addMapPanelChild(mpc);
                revalidate();
            }
        }

        if (someObj instanceof MapPanel) {
            add((Component) someObj, BorderLayout.CENTER);
        }

    }

    /**
     * Add a child to the HotwashPanel.
     */
    protected void addMapPanelChild(MapPanelChild mpc) {
        add((Component) mpc, mpc.getPreferredLocation());
    }

    /**
     * The HotwashPanel looks for MapPanelChild components and removes them from
     * iteself.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapPanelChild && someObj instanceof Component) {
            if (Debug.debugging("basic")) {
                Debug.output("HotwashPanel: removing "
                        + someObj.getClass().getName());
            }
            remove((Component) someObj);
            invalidate();
        }

    }

    public JSplitPane getBottomSlider() {
        return bottomSlider;
    }

    public JSplitPane getLeftSlider() {
        return leftSlider;
    }

    public JSplitPane getRightSlider() {
        return rightSlider;
    }

}