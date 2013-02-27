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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/OMGraphicDeleteTool.java,v $
// $RCSfile: OMGraphicDeleteTool.java,v $
// $Revision: 1.9 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.bbn.openmap.I18n;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.event.SelectionEvent;
import com.bbn.openmap.omGraphics.event.SelectionListener;
import com.bbn.openmap.omGraphics.event.SelectionProvider;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;

/**
 * The OMGraphicDeleteTool is a Swing component that contains a button that
 * listens for notifications that tell it that an OMGraphic has been 'selected',
 * and provides the capability to delete that OMGraphic from the component that
 * manages it. The OMGraphicDeleteTool gathers SelectionEvents, which provide it
 * information about the OMGraphic and the DrawingToolRequestor that can delete
 * it from the map. If multiple events are received, pressing the button will
 * cause notifications to be sent to all the DrawingToolRequestors to delete all
 * the OMGraphics that are currently selected. This component is also a
 * com.bbn.openmap.gui.Tool, so if the ToolPanel sees it in the MapHandler, the
 * button will automatically be added to it.
 * <p>
 * To add the button to the OpenMap application, it just needs to be added to
 * the openmap.components property in the openmap.properties file.
 */
public class OMGraphicDeleteTool extends OMToolComponent implements SelectionListener,
        ActionListener, KeyListener {

    protected JButton deleteButton = null;
    protected String defaultKey = "omgraphicdeletetool";

    protected Hashtable<OMGraphic, SelectionEvent> deleteList;
    protected List<DrawingToolRequestor> requestors;
    protected JToolBar jToolBar;

    public OMGraphicDeleteTool() {
        super();
        setKey(defaultKey);
        Debug.message("deletebutton", "OMGTL()");

        setLayout(new java.awt.GridLayout());
        jToolBar = new JToolBar();
        jToolBar.setFloatable(false);

        deleteList = new Hashtable<OMGraphic, SelectionEvent>();
        requestors = new ArrayList<DrawingToolRequestor>();

        java.net.URL url = this.getClass().getResource("delete.gif");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            deleteButton = new JButton(icon);
        } else {
            deleteButton = new JButton("Delete");
        }

        deleteButton.addActionListener(this);
        // deleteButton.setToolTipText("Delete selected map graphic");
        deleteButton.setToolTipText(i18n.get(OMGraphicDeleteTool.class, "deleteButton", I18n.TOOLTIP, "Delete selected map graphic"));
        deleteButton.setEnabled(false);

        jToolBar.add(deleteButton);
        add(jToolBar);
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE) || (e.getKeyCode() == KeyEvent.VK_DELETE)) {
            deleteSelected();
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void actionPerformed(ActionEvent ae) {
        Debug.message("deletebutton", "OMGDT.actionPerformed()");
        deleteSelected();
    }

    public void deleteSelected() {

        for (SelectionEvent item : deleteList.values()) {
            Object itemSource = item.getSource();

            // Too specific?
            if (itemSource instanceof OMDrawingTool) {
                // This should notify the requestor...
                ((OMDrawingTool) itemSource).deactivate(OMGraphicConstants.DELETE_GRAPHIC_MASK);

            } else {

                DrawingToolRequestor requestor = item.getRequestor();
                OMGraphic omg = item.getOMGraphic();
                if (requestor != null) {
                    requestor.drawingComplete(omg, new OMAction(OMGraphicConstants.DELETE_GRAPHIC_MASK));
                } else {
                    // if there isn't a requestor specified, tell
                    // anyone who will listen.
                    for (DrawingToolRequestor reqstor : requestors) {
                        reqstor.drawingComplete(omg, new OMAction(OMGraphicConstants.DELETE_GRAPHIC_MASK));
                    }
                }

            }
        }

        // Should we just clear the list now?
        deleteList.clear();
        deleteButton.setEnabled(!deleteList.isEmpty());
    }

    public void selectionNotification(SelectionEvent event) {
        if (event.isSelected() && event.getOMGraphic() != null) {
            Debug.message("deletebutton", "OMGDT.selection notification: adding selected to list.");
            deleteList.put(event.getOMGraphic(), event);
        } else if (!event.isSelected()) {
            Debug.message("deletebutton", "OMGDT.selection notification: removing selected from list.");
            deleteList.remove(event.getOMGraphic());
        } else {
            Debug.message("deletebutton", "OMGDT.selection notification: omgraphic missing from notification.");
        }

        deleteButton.setEnabled(!deleteList.isEmpty());
    }

    // /////////////////////////////////////////////////////////////////////////
    // // MapHandlerChild methods to make the tool work with
    // // the MapHandler to find any SelectionProviders.
    // /////////////////////////////////////////////////////////////////////////

    public void findAndInit(Object obj) {
        if (obj instanceof SelectionProvider) {
            Debug.message("deletebutton", "OMGDT.findAndInit() found selection provider");
            ((SelectionProvider) obj).addSelectionListener(this);
        }

        if (obj instanceof DrawingToolRequestor) {

            // Keep track of them so if the requestor is not known,
            // just
            // tell everyone who may be listening to delete the
            // OMGraphic in question and let them react accordingly.
            // If they don't already have the OMGraphic, they should
            // just ignore the request.
            requestors.add((DrawingToolRequestor) obj);
        }

        if (obj instanceof MapBean) {
            ((MapBean) obj).addKeyListener(this);
        }
    }

    public void findAndUndo(Object obj) {
        if (obj instanceof SelectionProvider) {
            ((SelectionProvider) obj).removeSelectionListener(this);
        }

        if (obj instanceof DrawingToolRequestor) {
            requestors.remove((DrawingToolRequestor) obj);
        }

        if (obj instanceof MapBean) {
            ((MapBean) obj).removeKeyListener(this);
        }
    }

}
