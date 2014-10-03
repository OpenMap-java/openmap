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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/ScaleTextPanel.java,v $
// $RCSfile: ScaleTextPanel.java,v $
// $Revision: 1.5 $
// $Date: 2005/02/02 13:14:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JTextField;

import com.bbn.openmap.I18n;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.event.ZoomEvent;
import com.bbn.openmap.event.ZoomListener;
import com.bbn.openmap.event.ZoomSupport;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * The ScaleTextPanel is a JPanel holding a JTextField that controls
 * and responds to the scale setting of a MapBean's projection. It is
 * also a Tool, so it can be added to the ToolPanel.
 */
public class ScaleTextPanel extends OMToolComponent implements Serializable,
        ActionListener, ProjectionListener {

    public final static String defaultScaleTextPanelKey = "scaletext";
    public transient final static String setScaleCmd = "setScale";

    protected transient JTextField scaleField = null;
    /** Support for zooming when text field is used. */
    protected transient ZoomSupport zoomDelegate = null;
    /**
     * The last projection received from the MapBean, so it can be
     * used to compare it to any more that come in. The ScaleTextPanel
     * listens to the MapBean for projection changes so it can keep up
     * with any scale changes later.
     */
    protected Projection projection;

    protected transient java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###");

    /**
     * Create the ScaleTextPanel
     */
    public ScaleTextPanel() {
        super();
        setKey(defaultScaleTextPanelKey);

        zoomDelegate = new ZoomSupport(this);

        String entry = "";
        String info = "Scale";
        String command = setScaleCmd;

        scaleField = new JTextField(entry, 10);
        //        scaleField.setToolTipText(info);
        scaleField.setToolTipText(i18n.get(ScaleTextPanel.class,
                command,
                I18n.TOOLTIP,
                info));
        scaleField.setMargin(new Insets(0, 0, 0, 0));
        scaleField.setActionCommand(command);
        scaleField.addActionListener(this);
        scaleField.setHorizontalAlignment(JTextField.RIGHT);

        gridbag.setConstraints(scaleField, c);

        add(scaleField);
    }

    /**
     * Called to set the scale setting on the scale text object.
     */
    public synchronized void setProjection(Projection aProjection) {
        projection = aProjection;
        if (Debug.debugging("scaletextpanel")) {
            System.out.println("ScaleTextPanel.setProjection(): scale is "
                    + projection.getScale() + " \""
                    + String.valueOf(projection.getScale()) + "\"");
        }

        String oldScale = scaleField.getText();
        String newScale = df.format(projection.getScale());

        if (!oldScale.equals(newScale)) {
            scaleField.setText("1:" + newScale);
        }
    }

    /**
     * Add a ZoomListener to the listener list.
     * 
     * @param listener The ZoomListener to be added
     */
    public synchronized void addZoomListener(ZoomListener listener) {
        zoomDelegate.add(listener);
    }

    /**
     * Remove a ZoomListener from the listener list.
     * 
     * @param listener The ZoomListener to be removed
     */
    public synchronized void removeZoomListener(ZoomListener listener) {
        zoomDelegate.remove(listener);
    }

    /**
     * Convenience function to set up listeners of the components. If
     * you are hooking the MapBean up to the ScaleTextPanel, this is
     * one of two methods you need to call. The other is
     * addMouseModes(), if the MapBean has more than one Mouse Mode.
     * 
     * @param aMap a map object.
     */
    public void setupListeners(MapBean aMap) {
        if (aMap != null) {
            // Wire up the beans for event passing
            addZoomListener((ZoomListener) aMap);
            aMap.addProjectionListener(this);
            // set the scaleEntry
            scaleField.setText("1:" + String.valueOf(aMap.getScale()));
        }
    }

    /**
     * This function removes the mapBean object from its set of
     * Listeners. An inverse of setupListeners() method.
     * 
     * @param mapBean a map object.
     */
    public void removeFromAllListeners(MapBean mapBean) {
        if (mapBean != null) {
            // Unwire the mapBean from these listeners
            removeZoomListener((ZoomListener) mapBean);
            mapBean.removeProjectionListener(this);
            // set the scaleEntry to 0
            scaleField.setText("----"/* String.valueOf(0) */);
        }
    }

    /**
     * Get the scale field widget.
     * 
     * @return JTextField that is rigged to set the scale for the map.
     */
    public JTextField getScaleField() {
        return scaleField;
    }

    /**
     * ActionListener interface.
     * 
     * @param e ActionEvent
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();

        if (Debug.debugging("scaletextpanel")) {
            Debug.output("ScaleTextPanel.actionPerformed(): " + command);
        }

        if (command.equals(setScaleCmd)) {
            setScale(scaleField.getText());
        }
    }

    protected void fireScaleChange(float scale) {
        if (Debug.debugging("scaletextpanel")) {
            Debug.output("ScaleTextPanel setting scale to " + scale);
        }
        zoomDelegate.fireZoom(ZoomEvent.ABSOLUTE, scale);
    }

    //set the scale of the map.
    private void setScale(String strscale) {
        float scale;

        int colon = strscale.indexOf(':');

        if (colon > -1) {
            strscale = strscale.substring(colon + 1);
        }

        try {
            if (strscale.toLowerCase().endsWith("m")) {
                strscale = strscale.substring(0, strscale.length() - 1);
                scale = df.parse(strscale).floatValue() * 1000000f;
                if (scale < 1f)
                    System.err.println("ScaleTextPanel.applyScale(): problem");
                else
                    fireScaleChange(scale);
            } else if (strscale.toLowerCase().endsWith("k")) {
                strscale = strscale.substring(0, strscale.length() - 1);
                scale = df.parse(strscale).floatValue() * 1000f;
                if (scale < 1f)
                    System.err.println("ScaleTextPanel.applyScale(): problem");
                else
                    fireScaleChange(scale);
            } else if (strscale.trim().length() == 0) {
                return; // ignore empty string
            } else {
                scale = df.parse(strscale).floatValue();
                if (scale < 1f)
                    System.err.println("ScaleTextPanel.applyScale(): problem");
                else
                    fireScaleChange(scale);
            }
        } catch (java.text.ParseException e) {
            System.err.println("ScaleTextPanel.setScale(): invalid scale: "
                    + strscale);
        } catch (NumberFormatException e) {
            System.err.println("ScaleTextPanel.setScale(): invalid scale: "
                    + strscale);
        }
    }

    //------------------------------------------------------------
    // ProjectionListener interface
    //------------------------------------------------------------

    /**
     * ProjectionListener interface method.
     * 
     * @param e ProjectionEvent
     */
    public void projectionChanged(ProjectionEvent e) {
        if (Debug.debugging("scaletextpanel")) {
            System.out.println("ScaleTextPanel.projectionChanged()");
        }
        Projection newProj = e.getProjection();
        if (projection == null || (!projection.equals(newProj))) {
            setProjection((Projection) newProj.makeClone());
        }
    }

    public void findAndInit(Object someObj) {
        if (someObj instanceof MapBean) {
            setupListeners((MapBean) someObj);
        }
    }

    public void findAndUndo(Object someObj) {
        if (someObj instanceof MapBean) {
            removeFromAllListeners((MapBean) someObj);
        }
    }

}