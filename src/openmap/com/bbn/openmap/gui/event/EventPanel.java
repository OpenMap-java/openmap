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
// $Source:
// /cvs/darwars/ambush/aar/src/com/bbn/ambush/gui/EventPanel.java,v $
// $RCSfile: EventPanel.java,v $
// $Revision: 1.1 $
// $Date: 2007/08/16 22:15:20 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.MapPanelChild;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.gui.OpenMapFrame;
import com.bbn.openmap.gui.WindowSupport;
import com.bbn.openmap.util.PropUtils;

public class EventPanel extends OMComponentPanel implements MapPanelChild {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.event.EventPanel");

    public final static String SHOW_ALL_EVENTS_STRING = " Show All Events ";
    public final static String HIDE_ALL_EVENTS_STRING = " Hide All Events ";
    public final static String SET_FILTERS_STRING = " Filter Events... ";
    public final static String NO_EVENTS_STRING = "No Events";

    protected List<EventPresenter> eventPresenters;
    protected List<MacroFilter> macroFilters;
    protected String preferredLocation = BorderLayout.WEST;
    protected JPanel filterPanel;
    protected Hashtable eventPresenterComponentLookup;
    // Used to intellegently determine if filter callup buttons should
    // be displayed.
    protected boolean hasFilters = false;
    protected String parentName;

    public EventPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "  Events  "));
        eventPresenters = new LinkedList<EventPresenter>();
        macroFilters = new LinkedList<MacroFilter>();
        eventPresenterComponentLookup = new Hashtable();
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        parentName = props.getProperty(prefix
                + MapPanelChild.ParentNameProperty);
    }

    public void addEventPresenter(EventPresenter ep) {
        logger.info("adding " + ep.getClass().getName());
        eventPresenters.add(ep);
        initInterface();
    }

    public void removeEventPresenter(EventPresenter ep) {
        eventPresenters.remove(ep);
        initInterface();
    }

    public void clearEventPresenters() {
        eventPresenters.clear();
        eventPresenterComponentLookup.clear();
        initInterface();
    }

    public void addMacroFilter(MacroFilter mf) {
        macroFilters.add(mf);
        initInterface();
    }

    public void removeMacroFilter(MacroFilter mf) {
        macroFilters.remove(mf);
        initInterface();
    }

    public void clearMacroFilters() {
        macroFilters.clear();
        initInterface();
    }

    /**
     * Initialize the panel interface, showing the buttons for calling up the
     * filter panel and the event presenter lists in a tabbed pane (if there is
     * more than one, otherwise, just shows the component from that
     * EventPresenter).
     */
    public void initInterface() {
        logger.info("rebuilding interface");
        removeAll();

        int numPresenters = eventPresenters.size();
        hasFilters = false;
        EventPresenter eventPresenter = null;
        Component presenterComponent = null;

        if (numPresenters == 1) {
            eventPresenter = (EventPresenter) eventPresenters.get(0);
            presenterComponent = eventPresenter.getComponent();
            add(presenterComponent, BorderLayout.CENTER);
            eventPresenterComponentLookup.put(presenterComponent,
                    eventPresenter);
            hasFilters = hasFilters
                    || (eventPresenter.getFilters() != null && eventPresenter.getFilters()
                            .size() > 0);
            setActiveEventPresenter(eventPresenter);
        } else if (numPresenters > 1) {

            JTabbedPane jtb = new JTabbedPane();
            jtb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent ce) {
                    JTabbedPane jtb = (JTabbedPane) ce.getSource();
                    // Find out which EventPresenter panel is active,
                    // and set that interface in the filter window.
                    setActiveEventPresenter((EventPresenter) eventPresenterComponentLookup.get(jtb.getSelectedComponent()));
                }
            });
            for (Iterator<EventPresenter> it = eventPresenters.iterator(); it.hasNext();) {
                eventPresenter = it.next();
                String name = eventPresenter.getName();
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("\tEventPanel: adding presenter (" + name
                            + ") to tabbed pane");
                }
                presenterComponent = eventPresenter.getComponent();
                jtb.addTab(name, presenterComponent);
                eventPresenterComponentLookup.put(presenterComponent,
                        eventPresenter);
                hasFilters = hasFilters
                        || (eventPresenter.getFilters() != null && eventPresenter.getFilters()
                                .size() > 0);
            }
            add(jtb, BorderLayout.CENTER);
        }

        // Uses hasFilters
        updateFilterCalloutInterface();
        updateMacroFilterInterface();

        revalidate();

        logger.info("\tEventPanel: Done");

    }

    protected JPanel launchFilterPanel = null;
    protected JPanel macroPanel = null;

    /**
     * @param hasFilters2
     */
    protected void updateFilterCalloutInterface() {
        if (launchFilterPanel == null) {
            launchFilterPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(0, 2, 4, 2);
            launchFilterPanel.setLayout(gridbag);
            JButton launchFilterButton = new JButton(EventPanel.SET_FILTERS_STRING);
            launchFilterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    showFilterInterface();
                }
            });

            JButton clearFilterButton = new JButton(EventPanel.SHOW_ALL_EVENTS_STRING);
            clearFilterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    resetFilters(Boolean.TRUE);
                }
            });
            gridbag.setConstraints(clearFilterButton, c);
            gridbag.setConstraints(launchFilterButton, c);

            launchFilterPanel.add(clearFilterButton);
            launchFilterPanel.add(launchFilterButton);
        }

        add(launchFilterPanel, BorderLayout.NORTH);
        launchFilterPanel.setVisible(hasFilters);
    }

    public void updateMacroFilterInterface() {

        if (macroPanel == null) {
            macroPanel = new JPanel();
        }

        macroPanel.removeAll();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        macroPanel.setLayout(gridbag);

        c.insets = new Insets(10, 10, 0, 10);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        for (Iterator<MacroFilter> it = macroFilters.iterator(); it.hasNext();) {
            MacroFilter fp = it.next();
            JPanel fpp = fp.getFilterPanel();
            fpp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                    fp.getName()));
            gridbag.setConstraints(fpp, c);
            macroPanel.add(fpp);
        }

        getFilterPanel().add(macroPanel, BorderLayout.NORTH);
        macroPanel.setVisible(macroFilters.size() > 0);
        getFilterPanel().revalidate();

    }

    /**
     * Sets all the filters to be off (true, enabling events of those types
     * contained in each EventPresenter) or on.
     * 
     * @param true1 Boolean.TRUE for all events shown, Boolean.FALSE for all
     *        events hidden.
     */
    protected void resetFilters(Boolean true1) {
        for (Iterator<EventPresenter> it = eventPresenters.iterator(); it.hasNext();) {
            it.next().resetFilters(true1);
        }
    }

    /**
     * Sets the EventPresenter as the active on, which also sets its filter
     * panel in the overall filter panel.
     * 
     * @param object
     */
    protected void setActiveEventPresenter(EventPresenter eventPresenter) {
        JPanel filterPanel = getFilterPanel();
        filterPanel.add(eventPresenter.getFilterPanel(), BorderLayout.CENTER);
        filterPanel.revalidate();
    }

    /**
     * Displays the filter panel in the frame window.
     */
    protected void showFilterInterface() {
        if (windowSupport != null) {
            windowSupport.killWindow();
        }

        MapHandler beanContext = (MapHandler) getBeanContext();
        OpenMapFrame frame = (OpenMapFrame) beanContext.get(com.bbn.openmap.gui.OpenMapFrame.class);

        windowSupport = new WindowSupport(getFilterPanel(), new WindowSupport.Dlg(frame, i18n.get(EventPanel.class,
                "title",
                "Event Filters")));
        windowSupport.displayInWindow();
    }

    /**
     * This filter panel is the overall filter panel that is in the frame. It
     * houses the macro filter interface common to all EventPresenters, and the
     * filter panel of the active EventPresenter.
     * 
     * @return
     */
    protected JPanel getFilterPanel() {
        if (filterPanel == null) {
            filterPanel = new JPanel();
            filterPanel.setLayout(new BorderLayout());
        }
        return filterPanel;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof EventPresenter) {
            addEventPresenter((EventPresenter) someObj);
        }

        if (someObj instanceof MacroFilter) {
            addMacroFilter((MacroFilter) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof EventPresenter) {
            removeEventPresenter((EventPresenter) someObj);
        }

        if (someObj instanceof MacroFilter) {
            removeMacroFilter((MacroFilter) someObj);
        }
    }

    public void setPreferredLocation(String loc) {
        preferredLocation = loc;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String pName) {
        parentName = pName;
    }

}