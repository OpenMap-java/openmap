//**********************************************************************
//
//<copyright>
//
//BBN Technologies
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: MissionHandler.java,v $
//$Revision: 1.10 $
//$Date: 2004/10/21 20:08:31 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.gui.event;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.OMComponent;
import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.tools.icon.BasicIconPart;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.util.PropUtils;

public class EventListIconPackage extends OMComponent {
    protected int buttonSize = 16;

    protected ImageIcon xImage;
    protected ImageIcon clockImage;
    protected ImageIcon thumbsUpImage;
    protected ImageIcon thumbsDownImage;
    protected ImageIcon invisibleImage;

    public final static String ShowRatingsProperty = "showRatings";
    public final static String ShowPlayFilterProperty = "showPlayFilter";
    public final static String ButtonSizeProperty = "buttonSize";

    protected boolean showRatings = true;
    protected boolean showPlayFilter = true;

    public EventListIconPackage() {
        initIcons();
    }

    public EventListIconPackage(int buttonSize) {
        setButtonSize(buttonSize);
        initIcons();
    }

    public int getButtonSize() {
        return buttonSize;
    }

    public void setButtonSize(int buttonSize) {
        this.buttonSize = buttonSize;
    }

    public boolean isShowRatings() {
        return showRatings;
    }

    public void setShowRatings(boolean showRatings) {
        this.showRatings = showRatings;
    }

    public boolean isShowPlayFilter() {
        return showPlayFilter;
    }

    public void setShowPlayFilter(boolean showPlayFilter) {
        this.showPlayFilter = showPlayFilter;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        showRatings = PropUtils.booleanFromProperties(props, prefix
                + ShowRatingsProperty, showRatings);
        showPlayFilter = PropUtils.booleanFromProperties(props, prefix
                + ShowPlayFilterProperty, showPlayFilter);
        buttonSize = PropUtils.intFromProperties(props, prefix
                + ButtonSizeProperty, buttonSize);
    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.setProperty(prefix + ShowRatingsProperty,
                Boolean.toString(showRatings));
        props.setProperty(prefix + ShowPlayFilterProperty,
                Boolean.toString(showPlayFilter));
        props.setProperty(prefix + ButtonSizeProperty,
                Integer.toString(buttonSize));

        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                this.getClass(),
                ShowRatingsProperty,
                "Show Ratings",
                "Show controls for adding ratings to events",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                this.getClass(),
                ShowPlayFilterProperty,
                "Show Play Filter",
                "Show controls for restricting playback to certain events",
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n,
                props,
                this.getClass(),
                ButtonSizeProperty,
                "Button Size",
                "Pixel size for buttons",
                null);

        return props;
    }

    protected void initIcons() {

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

    /**
     * Should only be called with a reference to a dedicated EventListPresenter
     * for this IconPackage.
     * 
     * @param elp EventListPresenter supplying events
     * @return JPanel that contains components for controlling events.
     */
    protected JPanel createEventControlPanel(EventListPresenter elp) {

        Dimension buttonDim = new Dimension(buttonSize, buttonSize);
        final EventListPresenter eventListPresenter = elp;

        JPanel eventControlPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        eventControlPanel.setLayout(gridbag);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5f;

        if (showPlayFilter) {
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
                    eventListPresenter.setSelectedEventsAttribute(OMEvent.ATT_KEY_PLAY_FILTER,
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
                    eventListPresenter.setSelectedEventsAttribute(OMEvent.ATT_KEY_PLAY_FILTER,
                            null);
                }
            });
            timeFilterPanel.add(timeClearAllButton);
        }

        c.weightx = 1.0f;
        c.insets = new Insets(2, 0, 2, 2);
        // JLabel ratingsLabel = new JLabel("Rating:",
        // SwingConstants.RIGHT);
        // gridbag.setConstraints(ratingsLabel, c);
        // eventControlPanel.add(ratingsLabel);

        if (showRatings) {
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
                    eventListPresenter.setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING,
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
                    eventListPresenter.setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING,
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
                    eventListPresenter.setSelectedEventsAttribute(OMEvent.ATT_KEY_RATING,
                            null);
                }
            });
            ratingsPanel.add(clearAllRatingsButton);
        }

        return eventControlPanel;
    }
}
