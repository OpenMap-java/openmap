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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.bbn.openmap.event.OMEvent;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

public class EventListCellRenderer extends OMComponentPanel implements
        ListCellRenderer {

    protected JLabel label = new JLabel();
    protected JLabel timeMark = new JLabel();
    protected JLabel ratingMark = new JLabel();

    public Color fontColor = Color.BLACK;
    public Color altFontColor = Color.BLACK;
    public Color selectColor = Color.GRAY;
    public Color timeWindowColor = Color.LIGHT_GRAY;
    public Color regularBackgroundColor = Color.WHITE;

    public static final String FontColorProperty = "fontColor";
    public static final String AltFontColorProperty = "altFontColor";
    public static final String SelectColorProperty = "selectColor";
    public static final String TimeWindowColorProperty = "timeWindowColor";
    public static final String BackgroundColorProperty = "color";
    public static final String IconPackageClassProperty = "iconPackageClass";

    /**
     * This cell renderer is pretty closely tied to the icon package. If you
     * want to change one, you'll probably want to change the other, too.
     */
    protected EventListIconPackage iconPackage;

    public EventListCellRenderer() {
        init();
    }

    protected void init() {
        removeAll();
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

        // This gets icon package set up
        setPreferredSize(new Dimension(20, getIconPackage().getButtonSize()));
    }

    public EventListIconPackage getIconPackage() {
        if (iconPackage == null) {
            iconPackage = new EventListIconPackage();
        }
        return iconPackage;
    }

    public void setIconPackage(EventListIconPackage iconPackage) {
        this.iconPackage = iconPackage;
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

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
        
        String crc = props.getProperty(prefix + IconPackageClassProperty);
        if (crc != null) {
            iconPackage = (EventListIconPackage) ComponentFactory.create(crc, prefix, props);
            init();
        }

    }

    public DrawingAttributes setRenderingAttributes(
                                                    DrawingAttributes drawingAttributes) {

        if (drawingAttributes == null) {
            drawingAttributes = DrawingAttributes.getDefaultClone();
        }

        drawingAttributes.setFillPaint(regularBackgroundColor);
        drawingAttributes.setSelectPaint(selectColor);
        drawingAttributes.setLinePaint(fontColor);
        drawingAttributes.setMattingPaint(timeWindowColor);
        return drawingAttributes;
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        label.setText(value.toString());

        if (value instanceof OMEvent) {
            OMEvent OMe = (OMEvent) value;
            isSelected = OMe.getAttribute(OMEvent.ATT_KEY_SELECTED) != null;

            timeMark.setIcon(OMe.getAttribute(OMEvent.ATT_KEY_PLAY_FILTER) == Boolean.TRUE ? iconPackage.clockImage
                    : iconPackage.invisibleImage);

            Object rating = OMe.getAttribute(OMEvent.ATT_KEY_RATING);
            if (rating == OMEvent.ATT_VAL_BAD_RATING) {
                ratingMark.setIcon(iconPackage.thumbsDownImage);
            } else if (rating == OMEvent.ATT_VAL_GOOD_RATING) {
                ratingMark.setIcon(iconPackage.thumbsUpImage);
            } else {
                ratingMark.setIcon(iconPackage.invisibleImage);
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
            timeMark.setIcon(iconPackage.invisibleImage);
            ratingMark.setIcon(iconPackage.invisibleImage);
        }

        setBackground(isSelected ? selectColor : regularBackgroundColor);
        setForeground(isSelected ? altFontColor : fontColor);
        return this;
    }

}
