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
// /cvs/darwars/ambush/OM/src/com/bbn/ambush/gui/EventListPanel.java,v
// $
// $RCSfile: EventListPresenter.java,v $
// $Revision: 1.2 $
// $Date: 2007/09/27 23:12:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.event.OMEventComparator;
import com.bbn.openmap.event.OMEventHandler;
import com.bbn.openmap.event.OMEventSelectionCoordinator;
import com.bbn.openmap.event.OMEventSelectionListener;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.time.Clock;
import com.bbn.openmap.time.TimeBoundsEvent;
import com.bbn.openmap.time.TimeBoundsListener;
import com.bbn.openmap.time.TimeEvent;
import com.bbn.openmap.time.TimeEventListener;
import com.bbn.openmap.tools.icon.BasicIconPart;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The EventListPresenter looks for OMEventHandlers, and creates a presentable
 * list of the events listed in them.
 */
public class EventListPresenter extends AbstractEventPresenter implements
        EventPresenter, ListSelectionListener, PropertyChangeListener,
        MouseListener, MouseMotionListener, TimeBoundsListener,
        TimeEventListener {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.event.EventListPresenter");

    protected LinkedList<OMEventHandler> eventHandlers;
    protected LinkedList macroFilters;
    protected EventPanel parentPanel;
    protected JList displayList;
    protected Clock clock = null;
    protected MapBean map = null;
    protected List activeFilters;
    protected Hashtable filters = new Hashtable();
    protected JPanel filterPanel;
    protected long displayTimeWindow = 1000;

    protected JLabel detailSpace;
    protected JPanel detailSpacePanel;

    protected TreeSet<OMEvent> activeEvents;
    protected TreeSet<OMEvent> allEvents;

    protected OMEventSelectionCoordinator aesc;

    public static Color fontColor = Color.BLACK;
    public static Color altFontColor = Color.BLACK;
    public static Color selectColor = Color.GRAY;
    public static Color timeWindowColor = Color.LIGHT_GRAY;
    public static Color regularBackgroundColor = Color.WHITE;

    public static final String DisplayIntervalProperty = "displayInterval";
    public static final String FontColorProperty = "fontColor";
    public static final String AltFontColorProperty = "altFontColor";
    public static final String SelectColorProperty = "selectColor";
    public static final String TimeWindowColorProperty = "timeWindowColor";
    public static final String BackgroundColorProperty = "color";
    /**
     * A property string to use for PropertyChangeListeners listening for when
     * the list contents have changed, either due to filtering or the
     * availability of new EventHandlers.
     */
    public final static String ActiveEventsProperty = "activeEvents";
    /**
     * A property string to use for PropertyChangeListeners interested in
     * knowing what events are currently selected by the user.
     */
    public final static String SelectedEventsProperty = "selectedEvents";
    /**
     * A property string used when event attributes (ratings, play filter
     * settings) have been changed.
     */
    public final static String EventAttributesUpdatedProperty = "eventAttributesUpdated";

    private OMEvent lastSelectedEvent;

    /**
     * Create a new EventListPresenter with a BorderLayout. The scrolled pane
     * containing the list is in the main body, and controls for bringing up the
     * filter controls are display in the NORTH.
     */
    public EventListPresenter() {
        eventHandlers = new LinkedList<OMEventHandler>();
        macroFilters = new LinkedList();
        initIcons();
        setLayout(new BorderLayout());
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        displayTimeWindow = PropUtils.longFromProperties(props, prefix
                + DisplayIntervalProperty, displayTimeWindow);

        fontColor = (Color) PropUtils.parseColorFromProperties(props, prefix
                + FontColorProperty, fontColor);
        altFontColor = (Color) PropUtils.parseColorFromProperties(props, prefix
                + AltFontColorProperty, altFontColor);
        selectColor = (Color) PropUtils.parseColorFromProperties(props, prefix
                + SelectColorProperty, selectColor);
        timeWindowColor = (Color) PropUtils.parseColorFromProperties(props,
                prefix + TimeWindowColorProperty,
                timeWindowColor);
        regularBackgroundColor = (Color) PropUtils.parseColorFromProperties(props,
                prefix + BackgroundColorProperty,
                regularBackgroundColor);
    }

    public void addEventHandler(OMEventHandler aeh) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("adding " + aeh.getClass().getName());
        }
        eventHandlers.add(aeh);
        retrieveFiltersFromEventHandlers();
        updateInterface(false);
    }

    public void removeEventHandler(OMEventHandler aeh) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removing " + aeh.getClass().getName());
        }
        eventHandlers.remove(aeh);
        retrieveFiltersFromEventHandlers();
        updateInterface();
    }

    public void clearEventHandlers() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("clearing event handlers");
        }
        eventHandlers.clear();
        retrieveFiltersFromEventHandlers();
        updateInterface(false);
    }

    public void rebuildEventList() {
        rebuildEventList(true);
    }

    /**
     * This is the method that rebuilds the list of visible events.
     */
    public void rebuildEventList(boolean resetSelected) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("rebuilding list");
        }
        // If allEvents isn't recreated here, weird things start happening with
        // selection. I seems that this gets the entire event list reset for the
        // interface.
        allEvents = null;
        activeEvents = null;
        hideDetails();
        initInterface(getActiveEvents(), resetSelected);
        highlightCurrentEvent(currentTime);
        firePropertyChange(ActiveEventsProperty, null, getActiveEvents());
    }

    /**
     * This is the method that creates a sorted list of all events.
     * 
     * @returns Iterator of OMEvents.
     */
    public synchronized Iterator<OMEvent> getAllEvents() {

        if (allEvents == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("fetching all events from event handlers");
            }

            allEvents = new TreeSet<OMEvent>(new OMEventComparator());
            for (Iterator<OMEventHandler> it = eventHandlers.iterator(); it.hasNext();) {
                List<OMEvent> eventList = it.next().getEventList();
                if (eventList != null) {
                    try {
                        allEvents.addAll(eventList);
                    } catch (NullPointerException npe) {
                        return getAllEvents();
                    }
                }
            }
        }
        return allEvents.iterator();
    }

    public boolean isEventActive(OMEvent OMe) {
        if (activeEvents != null) {
            for (Iterator<OMEvent> it = getActiveEvents(); it.hasNext();) {
                if (it.next() == OMe) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This is the method that creates a sorted list of visible events.
     * 
     * @returns Iterator of OMEvents.
     */
    public synchronized Iterator<OMEvent> getActiveEvents() {
        if (activeEvents == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("building list of active events");
            }

            activeEvents = new TreeSet<OMEvent>(new OMEventComparator());
            List<OMEvent> activeFilters = getActiveFilters();
            for (Iterator<OMEventHandler> it = eventHandlers.iterator(); it.hasNext();) {
                OMEventHandler aeh = (OMEventHandler) it.next();

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("getting filtered list from ("
                            + aeh.getClass().getName() + ")");
                }

                // Pass a filter to the OMEventHandler to get a
                // filtered list back if desired. Need to ask
                // MissionEventHandler for what filters it can handle
                // (with pretty name) so that the filters can be created
                // by the presenter and passed back

                List<OMEvent> eventList = aeh.getEventList(activeFilters);
                if (eventList != null) {
                    for (Iterator<OMEvent> it2 = eventList.iterator(); it2.hasNext();) {
                        if (logger.isLoggable(Level.FINER)) {
                            logger.finer("adding OM event");
                        }

                        OMEvent me = it2.next();
                        if (me != null) {
                            activeEvents.add(me);
                        } else {
                            logger.info("The " + aeh.getClass().getName()
                                    + " is providing null events.");
                        }
                    }
                }
            }

            if (logger.isLoggable(Level.FINER)) {
                logger.finer("--------");
                for (Iterator<OMEvent> it = activeEvents.iterator(); it.hasNext();) {
                    OMEvent eve = (OMEvent) it.next();
                    logger.finer(eve.getTimeStamp() + " " + eve);
                }

                logger.finer("--------");
            }
        }

        return activeEvents.iterator();
    }

    protected ToolTipManager ttmanager;

    /**
     * Resets the event list.
     * 
     * @param it Iterator over all visible events (active)
     * @param setSelected select the last currently selected on the list, has
     *        the side effect of resetting the clock. You want this to be false
     *        when event handlers are being added.
     */
    protected synchronized void initInterface(Iterator<OMEvent> it, boolean setSelected) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("initing interface");
        }

        DefaultListModel listModel = new DefaultListModel();

        int selectedIndex = -1;
        int curIndex = 0;
        while (it.hasNext()) {
            // if (logger.isLoggable(Level.FINER)) {
            // logger.finer("adding event to list model");
            // }
            OMEvent curEvent = it.next();
            listModel.addElement(curEvent);
            if (lastSelectedEvent == curEvent && lastSelectedEvent != null) {
                selectedIndex = curIndex;
            }
            curIndex++;
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("added " + curIndex + " events to list");
        }

        // This code below will cause the first visible event to be marked as
        // selected
        // if the list contents change and the previous selected version is no
        // longer visible.

        // if (selectedIndex < 0) {
        // selectedIndex = 0;
        // }

        if (displayList == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Creating gui components");
            }
            JPanel wrapper = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            wrapper.setLayout(gridbag);

            c.fill = GridBagConstraints.BOTH;
            c.weighty = 1f;
            c.weightx = 1f;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets(0, 2, 0, 2);
            // Don't show the panel if there are no events to show.
            // if (listModel.getSize() > 0) {
            if (ttmanager == null) {
                ttmanager = ToolTipManager.sharedInstance();
                ttmanager.setEnabled(true);
                ttmanager.setInitialDelay(1);
            } else if (displayList != null) {
                ttmanager.unregisterComponent(displayList);
            }

            displayList = new JList(listModel);
            displayList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION/* SINGLE_SELECTION */);
            displayList.addListSelectionListener(this);
            displayList.addMouseListener(this);
            displayList.addMouseMotionListener(this);
            displayList.setCellRenderer(new EventListCellRenderer());

            ttmanager.registerComponent(displayList);

            JScrollPane listScrollPane = new JScrollPane(displayList);
            listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            gridbag.setConstraints(listScrollPane, c);
            wrapper.add(listScrollPane);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0f;
            // Initialize the detail space. We need to embed it in a JPanel
            // so the background color fills the area.
            detailSpace = new JLabel("");
            detailSpacePanel = new JPanel(new BorderLayout());
            detailSpacePanel.setBackground(new Color(0xffffcc));
            detailSpacePanel.setVisible(false);
            detailSpacePanel.add(detailSpace, BorderLayout.CENTER);
            gridbag.setConstraints(detailSpacePanel, c);
            wrapper.add(detailSpacePanel);

            JComponent ecp = getEventControlPanel();
            gridbag.setConstraints(ecp, c);
            wrapper.add(ecp);

            add(wrapper, BorderLayout.CENTER);
            validate();
            repaint();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Done creating gui components");
            }
        } else {
            setListModel(listModel);
            displayList.revalidate();
            displayList.repaint();
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("done initing");
        }

        if (selectedIndex >= 0 && setSelected) {
            // only select the event if it's there. Otherwise, the list and
            // timeline jump inexplicitly to the top.

            displayList.setSelectedIndex(selectedIndex);
        } else {
            lastSelectedEvent = null;
        }

    }

    /**
     * @param string adding a filter string to the list of presentable filters
     *        available from one of the MissionEventHandler.
     */
    protected void addFilter(String string, Boolean value) {
        filters.put(string, value);
    }

    /**
     * Clear out the list of filters.
     */
    protected void clearFilters() {
        filters.clear();
    }

    public JPanel getFilterPanel() {
        if (filterPanel == null) {
            filterPanel = new JPanel();
        }
        return filterPanel;
    }

    /**
     * Rebuilds the contents of the panel in the popup window for the filters
     * supplied by the event handlers.
     */
    protected void rebuildFilterInterface() {
        JPanel filterPanel = getFilterPanel();
        filterPanel.removeAll();

        // Set up the overall panel layout
        GridBagLayout gb1 = new GridBagLayout();
        GridBagConstraints c1 = new GridBagConstraints();
        filterPanel.setLayout(gb1);
        c1.insets = new Insets(10, 10, 10, 10);
        c1.gridwidth = GridBagConstraints.REMAINDER;
        c1.fill = GridBagConstraints.VERTICAL;
        c1.weighty = .33;

        // Set up the layout of the filter options
        JPanel filterSubPanel = new JPanel();
        GridBagLayout gb2 = new GridBagLayout();
        GridBagConstraints c2 = new GridBagConstraints();
        filterSubPanel.setLayout(gb2);
        c2.anchor = GridBagConstraints.WEST;
        c2.gridwidth = GridBagConstraints.REMAINDER;

        // Set up the filter options, add them to panel that centers
        // itself in the window while aligning all on the left.
        try {
            Set keys = filters.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String title = (String) it.next();
                JCheckBox jcb = new JCheckBox(title, ((Boolean) filters.get(title)).booleanValue());
                jcb.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        JCheckBox jcb = (JCheckBox) ae.getSource();
                        String title = jcb.getText();
                        setFilterValue(title, jcb.isSelected());
                        rebuildEventList();
                    }
                });

                gb2.setConstraints(jcb, c2);
                filterSubPanel.add(jcb);
            }
        } catch (ConcurrentModificationException cme) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("ConcurrentModificationException caught while rebuilding the event list");
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("ArrayIndexOutOfBoundsException caught while rebuilding the event list: "
                        + aioobe.getMessage());
            }
        }

        // Add filter choices to the overall panel
        gb1.setConstraints(filterSubPanel, c1);
        filterPanel.add(filterSubPanel);

        // Create the buttons to turn them all on or off
        JButton allFiltersOnButton = new JButton(EventPanel.SHOW_ALL_EVENTS_STRING);
        allFiltersOnButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                resetFilters(Boolean.TRUE);
            }
        });

        JButton allFiltersOffButton = new JButton(EventPanel.HIDE_ALL_EVENTS_STRING);
        allFiltersOffButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                resetFilters(Boolean.FALSE);
            }
        });

        // Need to put these on/off buttons in their own panel so they
        // float in the middle of all the extra space in the window
        // without resizing themselves to fill in the space.
        JPanel resetButtonPanel = new JPanel();
        GridBagLayout gb3 = new GridBagLayout();
        GridBagConstraints c3 = new GridBagConstraints();
        resetButtonPanel.setLayout(gb3);
        gb3.setConstraints(allFiltersOnButton, c3);
        gb3.setConstraints(allFiltersOffButton, c3);
        resetButtonPanel.add(allFiltersOnButton);
        resetButtonPanel.add(allFiltersOffButton);

        c1.fill = GridBagConstraints.BOTH;
        c1.weighty = .66;
        c1.weightx = 1;
        c1.anchor = GridBagConstraints.CENTER;
        gb1.setConstraints(resetButtonPanel, c1);

        filterPanel.add(resetButtonPanel);
        filterPanel.revalidate();
    }

    /**
     * @param title
     * @param b
     */
    protected void setFilterValue(String title, boolean b) {
        setFilterValue(title, b ? Boolean.TRUE : Boolean.FALSE);
    }

    protected void setFilterValue(String title, Boolean val) {
        filters.put(title, val);

        // Tell all the OMEventHandlers that the setting has been
        // updated, let the one that cares update itself.
        for (Iterator<OMEventHandler> it = eventHandlers.iterator(); it.hasNext();) {
            OMEventHandler eh = it.next();
            // EventHandlers should only let this be set if they
            // control the events described by the filter.
            eh.setFilterState(title, val);
        }

        activeFilters = null;
    }

    /**
     * Set the filters so that all event handlers will return anything they
     * have, and rebuild interface.
     */
    public void resetFilters() {
        resetFilters(Boolean.TRUE);
    }

    /**
     * Set the filters on/off so that all event handlers will return anything
     * they have, and rebuild interface.
     */
    public void resetFilters(Boolean enabled) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("enabled = " + enabled);
        }
        Set keys = filters.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String title = (String) it.next();
            setFilterValue(title, enabled);
        }
        updateInterface();
    }

    public void updateInterface() {
        updateInterface(true);
    }

    public void updateInterface(boolean resetSelected) {
        activeFilters = null;
        rebuildFilterInterface();
        rebuildEventList(resetSelected);
        if (parentPanel instanceof EventPanel) {
            ((EventPanel) parentPanel).initInterface();
        }

        // Tell everyone interested in the event list to check the events.
    }

    /**
     * @return a Hashtable containing filter strings as keys, with Boolean
     *         TRUE/FALSE for whether they are enabled.
     */
    public Hashtable getFilters() {
        return filters;
    }

    /**
     * @return a List of Strings for the filters that have been turned on.
     */
    public List getActiveFilters() {
        if (activeFilters == null) {
            activeFilters = new LinkedList();
            Set filterNames = filters.keySet();
            for (Iterator it = filterNames.iterator(); it.hasNext();) {
                String filterName = (String) it.next();
                if (filters.get(filterName) == Boolean.TRUE) {
                    activeFilters.add(filterName);
                }
            }
        }
        return activeFilters;
    }

    /**
     * Contacts the OMEventHandlers and gets their filters from them.
     * Re-initializes the filter GUI.
     */
    public void retrieveFiltersFromEventHandlers() {
        clearFilters();
        for (Iterator<OMEventHandler> it = eventHandlers.iterator(); it.hasNext();) {

            OMEventHandler meh = it.next();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Checking out OM event handler "
                        + meh.getClass().getName());
            }
            // While we're checking out the OMEventHandler, might
            // as well ask it for it's filter strings, and reset the
            // filter interface.
            List filters = meh.getFilters();

            if (filters != null && filters.size() != 0) {
                for (Iterator it2 = filters.iterator(); it2.hasNext();) {
                    Object filterObj = it2.next();
                    if (filterObj instanceof String) {
                        Boolean val = meh.getFilterState((String) filterObj);
                        addFilter((String) filterObj, val != null ? val
                                : Boolean.FALSE);
                    }
                }
            }
        }
        rebuildFilterInterface();
    }

    public void clearSelection() {
        if (displayList != null) {
            displayList.clearSelection();
        }
    }

    protected synchronized void setListModel(ListModel lm) {
        displayList.setModel(lm);
    }

    protected synchronized ListModel getListModel() {
        return displayList.getModel();
    }

    /**
     * This method is required by ListSelectionListener. It causes events to
     * happen when something in the list is clicked on.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false && displayList != null) {
            int[] indicies = displayList.getSelectedIndices();
            if (indicies.length > 0) {
                ListModel listModel = getListModel();
                OMEvent selectedEvent = (OMEvent) listModel.getElementAt(indicies[0]);

                lastSelectedEvent = selectedEvent;

                if (selectedEvent != null) {
                    if (clock != null) {
                        clock.setTime((long) selectedEvent.getTimeStamp());
                        Point2D location = selectedEvent.getLocation();
                        if (location != null && map != null) {
                            map.setCenter(location);
                        }
                    }

                    String details = selectedEvent.getDetailedInformation();
                    if (details != null) {
                        showDetails(details);
                    } else {
                        hideDetails();
                    }
                }

                for (Iterator<OMEvent> it = getAllEvents(); it.hasNext();) {
                    ((OMEvent) it.next()).putAttribute(OMEvent.ATT_KEY_SELECTED,
                            null);
                }

                Vector<OMEvent> v = new Vector<OMEvent>();
                OMEvent firstInRangeEvent = null;
                OMEvent lastSelectedEvent = null;
                int lastIndex = -2;
                boolean inRange = false;

                for (int i = 0; i < indicies.length; i++) {
                    int curIndex = indicies[i];
                    selectedEvent = (OMEvent) listModel.getElementAt(curIndex);

                    selectedEvent.putAttribute(OMEvent.ATT_KEY_SELECTED,
                            OMEvent.ATT_VAL_SELECTED);

                    if (curIndex == lastIndex + 1) {
                        inRange = true;
                        if (firstInRangeEvent == null) {
                            firstInRangeEvent = lastSelectedEvent;
                            firstInRangeEvent.putAttribute(OMEvent.ATT_KEY_SELECTED,
                                    OMEvent.ATT_VAL_SELECTED_START_RANGE);
                        }
                    } else if (inRange) {
                        lastSelectedEvent.putAttribute(OMEvent.ATT_KEY_SELECTED,
                                OMEvent.ATT_VAL_SELECTED_END_RANGE);
                        inRange = false;
                    }

                    v.add(selectedEvent);
                    // forward the call back to the thing that provided
                    // the event, so it knows that the event was selected
                    // if it cares.
                    Object src = selectedEvent.getSource();
                    if (src instanceof OMEventSelectionListener) {
                        ((OMEventSelectionListener) src).selected(selectedEvent);
                    }

                    lastSelectedEvent = selectedEvent;
                    lastIndex = curIndex;
                }

                if (inRange) {
                    lastSelectedEvent.putAttribute(OMEvent.ATT_KEY_SELECTED,
                            OMEvent.ATT_VAL_SELECTED_END_RANGE);
                }

                if (aesc != null) {
                    aesc.eventsSelected(v);
                }
            }
        }
    }

    public boolean selectEvent(OMEvent event) {
        boolean ret = false;
        if (displayList != null) {
            displayList.setSelectedValue(event, true);

            String details = event.getDetailedInformation();
            if (details != null) {
                showDetails(details);
            } else {
                hideDetails();
            }

            ret = true;
        }

        return ret;
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof OMEventHandler) {
            addEventHandler((OMEventHandler) someObj);
        }

        if (someObj instanceof Clock) {
            setClock((Clock) someObj);
        }

        if (someObj instanceof MapBean) {
            setMap((MapBean) someObj);
        }

        if (someObj instanceof FilterPresenter && someObj != this) {
            macroFilters.add(someObj);
            ((FilterPresenter) someObj).addPropertyChangeListener(this);
        }

        if (someObj instanceof EventPanel) {
            parentPanel = (EventPanel) someObj;
        }

        if (someObj instanceof OMEventSelectionCoordinator && aesc == null) {
            aesc = (OMEventSelectionCoordinator) someObj;
            aesc.addPropertyChangeListener(this);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof OMEventHandler) {
            removeEventHandler((OMEventHandler) someObj);
        }

        if (someObj == getClock()) {
            setClock(null);
        }

        if (someObj instanceof MapBean) {
            setMap((MapBean) null);
        }

        if (someObj instanceof FilterPresenter && someObj != this) {
            macroFilters.remove(someObj);
            ((FilterPresenter) someObj).removePropertyChangeListener(this);
        }

        if (someObj == parentPanel) {
            parentPanel = null;
        }

        if (someObj == aesc) {
            aesc.removePropertyChangeListener(this);
            aesc = null;
        }
    }

    public void setClock(Clock cl) {
        if (clock != null) {
            clock.removeTimeEventListener(this);
            clock.removeTimeBoundsListener(this);
        }
        clock = cl;

        if (clock != null) {
            clock.addTimeEventListener(this);
            clock.addTimeBoundsListener(this);
        }
    }

    public Clock getClock() {
        return clock;
    }

    public void setMap(MapBean map) {
        this.map = map;
    }

    public MapBean getMap() {
        return map;
    }

    /**
     * @return Returns the displayTimeWindow.
     */
    public long getDisplayTimeWindow() {
        return displayTimeWindow;
    }

    /**
     * @param displayTimeWindow The displayTimeWindow to set.
     */
    public void setDisplayTimeWindow(long displayTimeWindow) {
        this.displayTimeWindow = displayTimeWindow;
    }

    public void updateTimeBounds(TimeBoundsEvent tbe) {
        // This section used to be called when the Clock.TIME_BOUNDS_PROPERTY
        // was sent.
        allEvents = null;

        if (displayList != null) {
            displayList.repaint();
        }
        // /////

        // This part got called when a TimeBoundsProvider was activated or
        // deactivated.

        // This gets called when the events change! Need to tell the
        // timelines to recharge...
        lastSelectedEvent = null;
        // Resets active and all event lists.
        rebuildEventList();
        firePropertyChange(EventAttributesUpdatedProperty, null, Boolean.TRUE);
        // ///////////
    }

    public void updateTime(TimeEvent te) {
        highlightCurrentEvent(te.getSystemTime());

        if (displayList != null) {
            displayList.repaint();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String eventPropertyName = evt.getPropertyName();

        if (eventPropertyName == FilterPresenter.FILTER_STATE) {
            boolean rebuildFilters = ((Boolean) evt.getNewValue()).booleanValue();

            if (rebuildFilters) {
                logger.fine(eventPropertyName
                        + " rebuilding filters and updating interface (list rebuild to follow)");
                retrieveFiltersFromEventHandlers();
                updateInterface();
            } else {
                logger.fine(eventPropertyName
                        + " clearing active filters and rebuilding list");
                activeFilters = null;
                rebuildEventList();
            }
        }

        if (eventPropertyName == OMEventSelectionCoordinator.EventsSelectedProperty) {
            scrollToSelected((List<OMEvent>) evt.getNewValue());
            displayList.repaint();
        }

    }

    protected void scrollToSelected(List<OMEvent> selectedEvents) {
        if (selectedEvents != null && selectedEvents.size() > 0) {
            OMEvent event = selectedEvents.get(0);
            lastSelectedEvent = event;
            // need to check timestamps because the first selected event might
            // not be visible, so if we come across a dlm timestamp that's
            // greater than this one, that should be the first selected event.
            long timeStamp = event.getTimeStamp();

            OMEvent dlmEvent = null;
            ListModel dlm = getListModel();
            int fvi = displayList.getFirstVisibleIndex();
            int lvi = displayList.getLastVisibleIndex();
            int numRowsVisible = lvi - fvi;
            int size = dlm.getSize();
            for (int i = 0; i < size; i++) {
                dlmEvent = (OMEvent) dlm.getElementAt(i);
                if (dlmEvent == event || dlmEvent.getTimeStamp() > timeStamp) {

                    if (i < fvi || i > lvi - 1) {
                        fvi = i;

                        if (size - i > numRowsVisible) {
                            lvi = i + numRowsVisible - 1;
                        } else {
                            lvi = size;
                        }
                        Rectangle rect = displayList.getCellBounds(fvi, lvi);
                        if (rect != null) {
                            displayList.scrollRectToVisible(rect);
                        }
                    }
                    break;
                }
            }
        }

    }

    protected long currentTime;

    /**
     * @param newValue
     */
    protected synchronized void highlightCurrentEvent(long newCurrentTime) {
        currentTime = newCurrentTime;

        if (displayList == null)
            return;

        ListModel dlm = displayList.getModel();
        boolean timeMarked = false;
        OMEvent lastClosest = null;
        int size = dlm.getSize();
        for (int i = 0; i < size; i++) {
            OMEvent event = (OMEvent) dlm.getElementAt(i);
            long timeStamp = event.getTimeStamp();

            boolean atCurrentTime = (Math.abs(timeStamp - currentTime) <= displayTimeWindow);
            event.setAtCurrentTime(atCurrentTime);
            // This work is done to mark the closest, but not over,
            // event marking where the time is.
            timeMarked = atCurrentTime || timeMarked;
            if (!timeMarked) {
                if (timeStamp < currentTime) {
                    lastClosest = event;
                } else if (lastClosest != null) {
                    lastClosest.setAtCurrentTime(true);
                    timeMarked = true;
                }
            }
        }
    }

    public static class EventListCellRenderer extends JPanel implements
            ListCellRenderer {

        protected JLabel label = new JLabel();
        protected JLabel timeMark = new JLabel();
        protected JLabel ratingMark = new JLabel();

        public EventListCellRenderer() {
            setOpaque(true);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout(gridbag);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            gridbag.setConstraints(label, c);
            this.add(label);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0f;
            gridbag.setConstraints(ratingMark, c);
            this.add(ratingMark);
            gridbag.setConstraints(timeMark, c);
            this.add(timeMark);

            Font f = label.getFont();
            f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
            label.setFont(f);

            setPreferredSize(new Dimension(20, buttonSize));
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            label.setText(value.toString());

            if (value instanceof OMEvent) {
                OMEvent OMe = (OMEvent) value;
                isSelected = OMe.getAttribute(OMEvent.ATT_KEY_SELECTED) != null;

                timeMark.setIcon(OMe.getAttribute(OMEvent.ATT_KEY_PLAY_FILTER) == Boolean.TRUE ? clockImage
                        : invisibleImage);

                Object rating = OMe.getAttribute(OMEvent.ATT_KEY_RATING);
                if (rating == OMEvent.ATT_VAL_BAD_RATING) {
                    ratingMark.setIcon(thumbsDownImage);
                } else if (rating == OMEvent.ATT_VAL_GOOD_RATING) {
                    ratingMark.setIcon(thumbsUpImage);
                } else {
                    ratingMark.setIcon(invisibleImage);
                }

                if (OMe.isAtCurrentTime()) {
                    if (isSelected) {
                        setBackground(selectColor);
                    } else {
                        setBackground(timeWindowColor);
                    }
                    setForeground(altFontColor);
                    return this;
                }

            } else {
                timeMark.setIcon(invisibleImage);
                ratingMark.setIcon(invisibleImage);
            }

            setBackground(isSelected ? selectColor : regularBackgroundColor);
            setForeground(isSelected ? altFontColor : fontColor);
            return this;
        }
    }

    public void mouseClicked(MouseEvent e) {
    // hideDetails();
    //
    // if (e.getButton() == MouseEvent.BUTTON3 && displayList != null) {
    // // Right click
    // int index = getDisplayListIndex(e);
    // lastIndexOfCellDetail = index;
    // ListModel model = displayList.getModel();
    // Object obj = model.getElementAt(index);
    //
    // String labelContents = null;
    //
    // if (obj instanceof OMEvent) {
    // OMEvent ae = (OMEvent) obj;
    // labelContents = ae.getDetailedInformation();
    // }
    //
    // if (labelContents == null) {
    // labelContents = " No further information available. ";
    // }
    //
    // showDetails(labelContents);
    // }

    }

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {
    // if (lastIndexOfCellDetail != -1
    // && lastIndexOfCellDetail != getDisplayListIndex(e)) {
    // hideDetails();
    // }
    }

    protected int lastIndexOfCellDetail = -1;

    public void showDetails(String contents) {

        if (contents != null) {
            detailSpace.setText("<html><body bgcolor=\"#ffffcc\"> <font style=\"plain\">"
                    + contents + "</html>");
        }

        detailSpacePanel.setVisible(contents != null);
    }

    public void hideDetails() {
        if (detailSpacePanel != null) {
            detailSpacePanel.setVisible(false);
        }

        lastIndexOfCellDetail = -1;
    }

    protected int getDisplayListIndex(MouseEvent e) {
        int index = 0;

        if (displayList != null) {
            double height = getDisplayListCellHeight();

            index = e.getY() / (int) height;

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("For " + e.getY() + ", the first vis: "
                        + displayList.getFirstVisibleIndex() + ", heights are "
                        + height + ", so index of click is " + index);
            }
        }

        return index;
    }

    protected double getDisplayListCellHeight() {
        double height = 0;
        if (displayList != null) {
            int rlFVI = displayList.getFirstVisibleIndex();
            Rectangle bounds = displayList.getCellBounds(rlFVI, rlFVI);

            height = bounds.getHeight();
        }
        return height;
    }

    JPanel eventControlPanel = null;

    protected JComponent getEventControlPanel() {
        if (eventControlPanel == null) {
            Dimension buttonDim = new Dimension(buttonSize, buttonSize);

            eventControlPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            eventControlPanel.setLayout(gridbag);

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(2, 2, 2, 2);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.5f;
            JPanel timeFilterPanel = new JPanel();
            gridbag.setConstraints(timeFilterPanel, c);
            eventControlPanel.add(timeFilterPanel);

            // JLabel timerLabel = new JLabel("Play Selection:");
            // timeFilterPanel.add(timerLabel);

            JButton clockButton = new JButton(clockImage);
            clockButton.setPreferredSize(buttonDim);
            clockButton.setToolTipText("Mark event(s) for play filtering.");
            clockButton.setBorderPainted(false);
            clockButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setSelectedEventsAttribute(OMEvent.ATT_KEY_PLAY_FILTER,
                            Boolean.TRUE);
                }
            });
            timeFilterPanel.add(clockButton);

            JButton timeClearAllButton = new JButton(xImage);
            timeClearAllButton.setPreferredSize(buttonDim);
            timeClearAllButton.setToolTipText("Clear selected events from play filtering.");
            timeClearAllButton.setBorderPainted(false);
            timeClearAllButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setSelectedEventsAttribute(OMEvent.ATT_KEY_PLAY_FILTER,
                            null);
                }
            });
            timeFilterPanel.add(timeClearAllButton);

            c.weightx = 1.0f;
            c.insets = new Insets(2, 0, 2, 2);
            // JLabel ratingsLabel = new JLabel("Rating:",
            // SwingConstants.RIGHT);
            // gridbag.setConstraints(ratingsLabel, c);
            // eventControlPanel.add(ratingsLabel);

            c.weightx = 0.5f;
            JPanel ratingsPanel = new JPanel();
            gridbag.setConstraints(ratingsPanel, c);
            eventControlPanel.add(ratingsPanel);

            JButton thumbsUpButton = new JButton(thumbsUpImage);
            thumbsUpButton.setPreferredSize(buttonDim);
            thumbsUpButton.setToolTipText("Flag selected event(s) as positive.");
            thumbsUpButton.setBorderPainted(false);
            thumbsUpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING,
                            OMEvent.ATT_VAL_GOOD_RATING);
                }
            });
            ratingsPanel.add(thumbsUpButton);

            JButton thumbsDownButton = new JButton(thumbsDownImage);
            thumbsDownButton.setPreferredSize(buttonDim);
            thumbsDownButton.setToolTipText("Flag selected event(s) as negative.");
            thumbsDownButton.setBorderPainted(false);
            thumbsDownButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING,
                            OMEvent.ATT_VAL_BAD_RATING);
                }
            });
            ratingsPanel.add(thumbsDownButton);

            JButton clearAllRatingsButton = new JButton(xImage);
            clearAllRatingsButton.setPreferredSize(buttonDim);
            clearAllRatingsButton.setToolTipText("Clear ratings of selected events.");
            clearAllRatingsButton.setBorderPainted(false);
            clearAllRatingsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING, null);
                }
            });
            ratingsPanel.add(clearAllRatingsButton);

        }

        JToolBar jsp = new JToolBar();
        jsp.add(eventControlPanel);
        jsp.setFloatable(false);
        jsp.setBorder(BorderFactory.createEmptyBorder());

        // return eventControlPanel;
        return jsp;
    }

    public void setSelectedEventsAttribute(Object key, Object value) {
        for (Iterator<OMEvent> it = getActiveEvents(); it.hasNext();) {
            OMEvent OMe = it.next();

            if (OMe.getAttribute(OMEvent.ATT_KEY_SELECTED) != null) {
                OMe.putAttribute(key, value);
            }
        }

        resolveSelectionForHiddenEvents(key, value);

        firePropertyChange(EventAttributesUpdatedProperty, null, Boolean.TRUE);

        displayList.repaint();
    }

    public void resolveSelectionForHiddenEvents(Object key, Object value) {

        boolean inRange = false;
        for (Iterator<OMEvent> iIt = getAllEvents(); iIt.hasNext();) {
            OMEvent iEvent = (OMEvent) iIt.next();

            if (iEvent.getAttribute(OMEvent.ATT_KEY_SELECTED) == OMEvent.ATT_VAL_SELECTED_START_RANGE) {
                inRange = true;
            } else if (iEvent.getAttribute(OMEvent.ATT_KEY_SELECTED) == OMEvent.ATT_VAL_SELECTED_END_RANGE) {
                inRange = false;
            } else if (inRange) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.finer("setting " + iEvent.getClass().getName() + " "
                            + key + ", " + value);
                }

                iEvent.putAttribute(key, value);
            }
        }
    }

    public void setAllEventsAttribute(Object key, Object value) {
        for (Iterator<OMEvent> it = getActiveEvents(); it.hasNext();) {
            ((OMEvent) it.next()).putAttribute(key, value);
        }
        displayList.repaint();
    }

    public static int buttonSize = 16;

    public static ImageIcon xImage;
    public static ImageIcon clockImage;
    public static ImageIcon thumbsUpImage;
    public static ImageIcon thumbsDownImage;
    public static ImageIcon invisibleImage;

    protected static void initIcons() {

        DrawingAttributes greyDa = new DrawingAttributes();
        Color gry = new Color(0x99999999, true);
        greyDa.setFillPaint(gry);
        greyDa.setLinePaint(gry);

        DrawingAttributes whtDa = new DrawingAttributes();
        whtDa.setLinePaint(Color.white);
        whtDa.setStroke(new BasicStroke(2));

        DrawingAttributes handsDa = new DrawingAttributes();
        handsDa.setStroke(new BasicStroke(2));

        DrawingAttributes invisDa = new DrawingAttributes();
        invisDa.setLinePaint(OMColor.clear);
        invisDa.setFillPaint(OMColor.clear);

        DrawingAttributes timeDa = new DrawingAttributes();
        timeDa.setLinePaint(OMColor.blue);
        timeDa.setFillPaint(OMColor.blue);

        IconPart ip = new BasicIconPart(new Rectangle2D.Double(0, 0, 100, 100), invisDa);
        invisibleImage = OMIconFactory.getIcon(buttonSize, buttonSize, ip);

        IconPartList ipl = new IconPartList();
        ipl.add(new BasicIconPart(new Ellipse2D.Double(5, 5, 90, 90), greyDa));
        ipl.add(new BasicIconPart(new Line2D.Double(30, 30, 70, 70), whtDa));
        ipl.add(new BasicIconPart(new Line2D.Double(30, 70, 70, 30), whtDa));
        xImage = OMIconFactory.getIcon(buttonSize, buttonSize, ipl);

        ipl = new IconPartList();
        ipl.add(new BasicIconPart(new Ellipse2D.Double(10, 10, 80, 80), handsDa));
        ipl.add(new BasicIconPart(new Line2D.Double(50, 50, 50, 15), handsDa));
        ipl.add(new BasicIconPart(new Line2D.Double(50, 50, 70, 50), handsDa));
        clockImage = OMIconFactory.getIcon(buttonSize, buttonSize, ipl);

        DrawingAttributes goodDa = new DrawingAttributes();
        goodDa.setFillPaint(Color.green);
        goodDa.setLinePaint(Color.green.darker().darker());
        ip = new BasicIconPart(new Polygon(new int[] { 50, 90, 10, 50 }, new int[] {
                10, 90, 90, 10 }, 4), goodDa);
        thumbsUpImage = OMIconFactory.getIcon(buttonSize, buttonSize, ip);

        DrawingAttributes badDa = new DrawingAttributes();
        badDa.setFillPaint(Color.red);
        badDa.setLinePaint(Color.red.darker().darker());
        ip = new BasicIconPart(new Polygon(new int[] { 10, 90, 50, 10 }, new int[] {
                10, 10, 90, 10 }, 4), badDa);
        thumbsDownImage = OMIconFactory.getIcon(buttonSize, buttonSize, ip);

    }

    public static void main(String[] argv) {
        javax.swing.JFrame frame = new javax.swing.JFrame("EventListPresenter");
        EventListPresenter elp = new EventListPresenter();
        frame.add(elp);
        frame.pack();
        frame.setVisible(true);
    }

}