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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/time/TimePanel.java,v $
// $RCSfile: TimePanel.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:31:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.time;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;

import com.bbn.openmap.I18n;
import com.bbn.openmap.event.OMEventSelectionCoordinator;
import com.bbn.openmap.gui.MapPanelChild;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.gui.event.EventPresenter;
import com.bbn.openmap.time.Clock;
import com.bbn.openmap.time.TimeEvent;
import com.bbn.openmap.time.TimeEventListener;
import com.bbn.openmap.time.TimerRateHolder;
import com.bbn.openmap.util.PropUtils;

/**
 * The TimePanel is a GUI widget that provides assortment of Clock controls,
 * including play, step and reverse buttons, a rate controller, a current time
 * label and a time slider.
 * <P>
 * A Clock is needed to create an interface. If there is no clock, an empty
 * panel with a title will be displayed.
 * <P>
 * If you are using the openmap.properties file to configure your application
 * and want to control the parent component of the TimePanel, use the "parent"
 * property to specify the marker name of the parent component. The
 * BasicMapPanel asks MapPanelChildren for their parent's names when deciding
 * whether to add them or not, when it finds potential children in the 
 * findAndInit method.
 */
public class TimePanel extends OMComponentPanel implements MapPanelChild,
        PropertyChangeListener, TimeEventListener {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.gui.time.TimePanel");

    /**
     * This property is used to signify whether the play filter should be used.
     */
    public final static String PlayFilterProperty = "playfilter";
    public final static String NO_TIME_STRING = "--:--:-- (--:--:--)";
    public final static String PanelTitleProperty = "panelTitle";
    private String defaultPanelTitle = "  Timeline Controls  ";

    public final static String RealTimeModeProperty = "realTimeMode";
    private boolean realTimeMode = false;

    public final static String ShowPlayFilterProperty = "showPlayFilter";
    private boolean showPlayFilter = true;
    
    /**
     * The Clock object used by the TimePanel.
     */
    protected Clock clock;

    // / GUI ToolPanel widgets. Kept here to make their visibility
    // adjustable.
    protected JToggleButton timeWrapToggle;

    // protected JSlider timeSlider;

    protected JLabel timeLabel;
    protected JLabel mouseTimeLabel;
    protected JLabel eventDetailLabel;

    protected JCheckBox playFilter;

    protected HotwashTimerControlButtonPanel timerControl;

    protected TimerRateComboBox timerRateControl;

    protected String preferredLocation = BorderLayout.SOUTH;

    protected boolean useTimeWrapToggle = false;

    public transient DecimalFormat df = new DecimalFormat("00");

    // KMMOD
    TimelinePanel timelinePanel;

    TimeSliderPanel timeSliderPanel;

    protected String parentName;

    // Isolating formats that have anything to do with months+days, for International audiences
    final public static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy (HH:mm:ss)");
    final public static DateFormat dateFormat_realTime = new SimpleDateFormat("MM/dd/yyyy  HH:mm:ss");
    final public static DateFormat dayFormat = new SimpleDateFormat("MM/dd/yyyy");

    public TimePanel() {
    // Needs Clock to create interface.
    }

    public class NoBorder extends AbstractBorder {
        NoBorder() {}
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        parentName = props.getProperty(prefix
                + MapPanelChild.ParentNameProperty);
        
        defaultPanelTitle = props.getProperty(prefix
                + PanelTitleProperty,
                defaultPanelTitle);

        realTimeMode = PropUtils.booleanFromProperties(props, prefix
                + RealTimeModeProperty, realTimeMode);
        
        showPlayFilter = PropUtils.booleanFromProperties(props, prefix
                + ShowPlayFilterProperty, showPlayFilter);
        
        if(realTimeMode) {
            // At this point, we know we're going to need a TimeSliderPanel
            getTimeSliderPanel();
        }
    }

    /**
     * A Clock is needed to create an interface. If there is no clock, an empty
     * panel with a title will be displayed.
     */
    public void createInterface() {
        removeAll();

        String internString = i18n.get(this.getClass(),
                "timeline_controls",
                defaultPanelTitle);

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                internString));

        if (clock == null) {
            logger.info("No clock, not putting anything in interface.");
            return;
        }
        // javax.swing.Border debugBorder =
        // BorderFactory.createLineBorder(Color.red);
        JPanel leftPanel = new JPanel();
        GridBagLayout lgridbag = new GridBagLayout();
        leftPanel.setLayout(lgridbag);
        GridBagConstraints c = new GridBagConstraints();
        Insets insets = new Insets(2, 4, 2, 4);
        c.weightx = 0f;
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1f;
        c.gridx = GridBagConstraints.REMAINDER;
        c.insets = insets;

        internString = i18n.get(this.getClass(), "play_selected", "Play Filter");
        if(showPlayFilter) {
            playFilter = new JCheckBox(internString);
            internString = i18n.get(this.getClass(),
                    "play_selected",
                    I18n.TOOLTIP,
                    "Jump clock to events with play filter markings.");
            playFilter.setToolTipText(internString);
            playFilter.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JCheckBox jcb = (JCheckBox) ae.getSource();
                    firePropertyChange(PlayFilterProperty,
                            new Boolean(!jcb.isSelected()),
                            new Boolean(jcb.isSelected()));
                }
            });
            lgridbag.setConstraints(playFilter, c);
            playFilter.setVisible(false);
            leftPanel.add(playFilter);
        }
        
        c.fill = GridBagConstraints.NONE;
        c.weighty = 0f;
        
        JLabel dateTimeLabel = new JLabel("Date (Local Time)", SwingConstants.CENTER);
        lgridbag.setConstraints(dateTimeLabel, c);
        leftPanel.add(dateTimeLabel);

        timeLabel = new JLabel(NO_TIME_STRING, SwingConstants.CENTER);
        Font defaultFont = timeLabel.getFont();
        timeLabel.setFont(new java.awt.Font(defaultFont.getName(), defaultFont.getStyle(), 12));
        internString = i18n.get(this.getClass(), "time", I18n.TOOLTIP, "Time");
        timeLabel.setToolTipText(internString);
        lgridbag.setConstraints(timeLabel, c);
        leftPanel.add(timeLabel);

        timerControl = new HotwashTimerControlButtonPanel(clock);
        lgridbag.setConstraints(timerControl, c);
        leftPanel.add(timerControl);

        clock.addTimeEventListener(timerControl);

        /*
         * Not Used, but strangely enough needs to be created in order to tell
         * the clock how fast to run, one second per clock tick.
         */
        timerRateControl = new TimerRateComboBox(clock);
        internString = i18n.get(this.getClass(),
                "timer_rate_control",
                I18n.TOOLTIP,
                "Change Clock Rate For Timeline");
        timerRateControl.setToolTipText(internString);

        List<TimerRateHolder> timerRates = clock.getTimerRates();

        Iterator<TimerRateHolder> it = timerRates.iterator();
        while (it.hasNext()) {
            TimerRateHolder trh = it.next();
            timerRateControl.add(trh.getLabel(),
                    (int) trh.getClockInterval(),
                    (int) trh.getPace());
        }

        int si = timerRates.size() / 2;
        if (si > 0) {
            timerRateControl.setSelectedIndex(si);
        }

        // lgridbag.setConstraints(timerRateControl, c);
        // add(timerRateControl);

        // Right side
        insets = new Insets(0, 4, 0, 4);
        JPanel rightPanel = new JPanel();
        GridBagLayout rgridbag = new GridBagLayout();
        rightPanel.setLayout(rgridbag);
        c = new GridBagConstraints();
        c.insets = insets;
        c.fill = GridBagConstraints.NONE;
        c.weighty = 0.0;
        c.weightx = 0.0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        internString = i18n.get(this.getClass(), "mouse_time", "Mouse Time:");
        JLabel mouseTime = new JLabel(internString);
        rgridbag.setConstraints(mouseTime, c);
        rightPanel.add(mouseTime);

        mouseTimeLabel = new JLabel("");
        rgridbag.setConstraints(mouseTimeLabel, c);
        rightPanel.add(mouseTimeLabel);

        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        eventDetailLabel = new JLabel("", SwingConstants.RIGHT);
        rgridbag.setConstraints(eventDetailLabel, c);
        rightPanel.add(eventDetailLabel);

        TimelinePanel timelinePanel = getTimelinePanel();
        timelinePanel.addMapComponent(new TimePanel.Wrapper(this));

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1f;
        c.weightx = 1f;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy = 1;
        rgridbag.setConstraints(timelinePanel, c);
        rightPanel.add(timelinePanel);

        TimeSliderPanel timeSliderPanel = getTimeSliderPanel();
        // Slider needs to know about the timeline to set projection
        timeSliderPanel.addMapComponent(timelinePanel.getWrapper());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0f;
        c.gridy = 2;
        rgridbag.setConstraints(timeSliderPanel, c);
        rightPanel.add(timeSliderPanel);

        GridBagLayout gridbag = new GridBagLayout();
        setLayout(gridbag);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.weighty = 1f;
        c.insets = new Insets(0, 0, 0, 0);
        gridbag.setConstraints(leftPanel, c);
        add(leftPanel);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1f;
        c.insets = new Insets(0, 0, 4, 0);
        gridbag.setConstraints(rightPanel, c);
        add(rightPanel);
        
        revalidate();
    }

    public void updateEventDetailsDisplay(String details) {
        if (eventDetailLabel != null) {
            eventDetailLabel.setText(details);
        }
    }

    /**
     * Displays the provided offset time in the Mouse Time display label. Time
     * is expected to be milliseconds offset from the beginning of displayed
     * time.
     * 
     * @param mouseOffsetTime
     */
    public void updateMouseTimeDisplay(long mouseOffsetTime) {

        String mtds = "--";
        
        if (realTimeMode) {
            if (mouseOffsetTime > timelinePanel.getTimelineLayer().getEndTime()) {
                mouseOffsetTime = timelinePanel.getTimelineLayer().getEndTime();
            }
        } else {
            if (mouseOffsetTime < 0) {
                mouseOffsetTime = 0;
            }
        }

        if (mouseOffsetTime >= 0) {
            mtds = convertOffsetTimeToText(mouseOffsetTime);
        }

        if (mouseTimeLabel != null) {
            mouseTimeLabel.setText(mtds);
        }
    }

    public String convertOffsetTimeToText(long offsetTimeFromLeft) {
        
        long offsetTime = offsetTimeFromLeft;
        
        if(realTimeMode) {
            offsetTime -= timelinePanel.getTimelineLayer().getDuration();
        }
                
        String sign = "";
        if(offsetTime < 0) {
            sign = "-";
            offsetTime = -offsetTime;
        }
        int hours = (int) (offsetTime / (60 * 60 * 1000));
        int minutes = (int) Math.abs((offsetTime % (60 * 60 * 1000))
                / (60 * 1000));
        int seconds = (int) Math.abs((offsetTime % (60 * 1000)) / 1000);

        String relativeTime = sign + df.format(hours) + ":" + df.format(minutes) + ":" + df.format(seconds);
        
        if(realTimeMode) {
            Date date = new Date(offsetTimeFromLeft + timeSliderPanel.getTimeSliderLayer().gameStartTime);
            return TimePanel.dateFormat_realTime.format(date) + "   (" + relativeTime + ")";            
        } else {
            return relativeTime;
        }
    }

    public void setPreferredLocation(String loc) {
        preferredLocation = loc;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void updateTime(TimeEvent te) {

        if (checkAndSetForNoTime(te)) {
            return;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("TimePanel received TIMER_STATUS property update: "
                    + te);
        }

        updateTimeLabel(te.getSystemTime(), te.getOffsetTime());

    }

    /**
     * PropertyChangeListener method called when a Clock fires, or the Clock
     * time bounds change.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        String propertyName = pce.getPropertyName();
        Object newVal = pce.getNewValue();

        if (propertyName.equals(TimelineLayer.PlayFilterProperty)) {
            timerControl.enableForwardButton(((Boolean) newVal).booleanValue());
        } else if (propertyName.equals(TimelineLayer.MouseTimeProperty)) {
            updateMouseTimeDisplay(((Long) newVal).longValue());
        } else if (propertyName.equals(TimelineLayer.EventDetailsProperty)) {
            updateEventDetailsDisplay((String) newVal);
        }

        revalidate();
    }

    protected boolean checkAndSetForNoTime(TimeEvent te) {

        boolean isNoTime = te == TimeEvent.NO_TIME;

        if (isNoTime) {
            updateEventDetailsDisplay("");
            updateMouseTimeDisplay(0l);
            timeLabel.setText(NO_TIME_STRING);
        }
        // timerControl.setEnableState(!isNoTime);

        return isNoTime;
    }

    /**
     * Updates the timeLabel with the proper formats, dashes if needed.
     * 
     * @param sysTime
     * @param offsetTime
     */
    public void updateTimeLabel(long sysTime, long offsetTime) {
        // Do this so the label is reset if the time changes.
        if (timeLabel != null) {

            if (sysTime != Long.MAX_VALUE) {

                if(realTimeMode) {
                    Date date = new Date(sysTime);
                    String timeText = TimePanel.dateFormat_realTime.format(date);            
                    timeLabel.setText(timeText);
                } else {
                    Date date = new Date(sysTime);
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    String sts = dateFormat.format(date);
    
                    timeLabel.setText(sts + " ("
                            + convertOffsetTimeToText(offsetTime) + ")");
                }
            } else {
                timeLabel.setText(NO_TIME_STRING);
            }
        }
    }

    public void setClock(Clock cl) {
        logger.fine("found and setting clock: " + cl);
        if (clock != null) {
            clock.removeTimeEventListener(this);
        }
        clock = cl;
        createInterface(); // Moved here for distributed
        // configuration
        if (clock != null) {
            clock.addTimeEventListener(this);
        }

    }

    public void setPlayFilterVisible(boolean visible) {
        if (playFilter != null) {
            playFilter.setVisible(visible);
        }
    }

    public boolean isPlayFilterVisible() {
        return playFilter != null && playFilter.isVisible();
    }

    public Clock getClock() {
        return clock;
    }

    /**
     * OMComponentPanel method, called when new components are added to the
     * MapHandler. Lets the TimePanel register itself as PropertyChangeListener
     * to the Clock.
     */
    public void findAndInit(Object someObj) {
        if (someObj instanceof Clock) {
            setClock((Clock) someObj);
            getTimelinePanel().getMapHandler().add(someObj);
            getTimeSliderPanel().getMapHandler().add(someObj);
        }

        if (someObj instanceof OMEventSelectionCoordinator) {
            getTimelinePanel().getMapHandler().add(someObj);
        }

        if (someObj instanceof EventPresenter) {
            getTimelinePanel().getMapHandler().add(someObj);
        }
    }

    /**
     * OMComponentPanel method, called when new components are removed from the
     * MapHandler. Lets the TimePanel unregister itself as
     * PropertyChangeListener to the Clock.
     */
    public void findAndUndo(Object someObj) {
        if (someObj instanceof Clock && getClock() == someObj) {
            setClock(null);
        }
    }

    public static class Wrapper {
        TimePanel timePanel;

        public Wrapper(TimePanel panel) {
            this.timePanel = panel;
        }

        public TimePanel getTimePanel() {
            return timePanel;
        }
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String pName) {
        parentName = pName;
    }

    public TimelinePanel getTimelinePanel() {
        if (timelinePanel == null) {
            timelinePanel = new TimelinePanel();
            timelinePanel.setRealTimeMode(realTimeMode);
        }
        return timelinePanel;
    }

    public TimeSliderPanel getTimeSliderPanel() {
        if (timeSliderPanel == null) {
            timeSliderPanel = new TimeSliderPanel(realTimeMode);
        }
        return timeSliderPanel;
    }
    
    public void setUserHasChangedScale(boolean userHasChangedScale) {
        timeSliderPanel.setUserHasChangedScale(userHasChangedScale);
    }

    public void addTimeBoundsUserActionsListener(
            ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
        timeSliderPanel.addTimeBoundsUserActionsListener(timeBoundsUserActionsListener);
    }
    
    public void removeTimeBoundsUserActionsListener(
            ITimeBoundsUserActionsListener timeBoundsUserActionsListener) {
        timeSliderPanel.removeTimeBoundsUserActionsListener(timeBoundsUserActionsListener);
    }

}