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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/ExampleApplet.java,v $
// $RCSfile: ExampleApplet.java,v $
// $Revision: 1.4 $
// $Date: 2005/08/09 20:33:38 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.esri;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.event.NavMouseMode;
import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.Tool;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;

/**
 * ExampleApplet is an example of how to use the EsriLayer in a JApplet. When
 * the user clicks on the "Add Layers" button, three sets of shape files are
 * streamed from a web server to the clients web browser. After the shape files
 * have been streamed to the client's browser, the user may click on the
 * "View Table" button. Clicking this button will display a new JFrame
 * containing a JTable. This JTable will contain data from a corresponding
 * layer's .dbf file. To enable the user to run this applet as an application
 * the user may check off a box reading "Running Locally". Checking this box,
 * will cause the applet to load files from the local file system.
 * 
 * @author Doug Van Auken
 */
public class ExampleApplet extends JApplet {
    private MapBean _mapBean = null;
    private Layer _layers[] = null;
    private LayerHandler _layerHandler = null;
    private OMToolSet _omts = null;
    private ToolPanel _toolPanel = null;
    private MouseDelegator _mouseDelegator = null;

    private JButton _cmdAddLayers = null;
    private JButton _cmdShowTable = null;

    private JCheckBox _runningLocally = null;

    /*
     * Constructor.
     */
    public ExampleApplet() {
        _mapBean = new MapBean();
        _toolPanel = new ToolPanel();
        _omts = new OMToolSet();
        _omts.findAndInit(_mapBean);
        _mouseDelegator = new MouseDelegator(_mapBean);

        NavMouseMode nmm = new NavMouseMode();
        SelectMouseMode smm = new SelectMouseMode();
        _mouseDelegator.addMouseMode(nmm);
        _mouseDelegator.addMouseMode(smm);

        setupUI();
    }

    /**
     * Provides a dialog box from which the user can pick the layer that they
     * would like to view table data for
     * 
     * @return The layer that user has selected
     */
    private EsriLayer pickEsriLayer() {
        Layer[] layers = _layerHandler.getLayers();
        Vector<String> vector = new Vector<String>();
        for (int n = 0; n <= layers.length - 1; n++) {
            String name = layers[n].getName();
            vector.add(name);
        }
        Object[] objects = vector.toArray();
        String selectedValue = (String) JOptionPane.showInputDialog(null,
                "Choose one",
                "Input",
                JOptionPane.INFORMATION_MESSAGE,
                null,
                objects,
                objects[0]);

        for (int n = 0; n <= layers.length - 1; n++) {
            String name = layers[n].getName();
            if (name.equalsIgnoreCase(selectedValue)) {
                return (EsriLayer) layers[n];
            }
        }
        return null;
    }

    /**
     * Sets up the user interface.
     */
    public void setupUI() {
        _runningLocally = new JCheckBox("Running Locally");

        getContentPane().setLayout(new BorderLayout());
        setSize(640, 480);
        _mapBean.setCenter(new LatLonPoint.Double(43.0f, -95.0f));
        _mapBean.setScale(120000000f);
        _layers = new Layer[1];

        _toolPanel = new ToolPanel();

        Properties props = new Properties();
        props.put("prettyName", "Graticule");
        props.put("showRuler", "true");
        props.put("show1And5Lines", "true");
        props.put("threshold", "2");
        props.put("10DegreeColor", "FF000000");
        props.put("5DegreeColor", "FF009900");
        props.put("1DegreeColor", "FF003300");
        props.put("equatorColor", "FFFF0000");
        props.put("dateLineColor", "FF000099");
        props.put("specialLineColor", "FF000000");
        props.put("textColor", "FF000000");
        GraticuleLayer graticule = new GraticuleLayer();
        graticule.setProperties(props);
        _layers[0] = graticule;

        // Finish setting up the GUI.
        _layerHandler = new LayerHandler(_layers);
        _layerHandler.addLayerListener(_mapBean);

        // _omts.add(_mouseModePanel);;

        _cmdAddLayers = new JButton("Add Layers");
        _cmdAddLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    EsriLayer rivers = null;
                    EsriLayer cities = null;
                    EsriLayer states = null;
                    URL dbf = null;
                    URL shx = null;
                    URL shp = null;

                    if (_runningLocally.isSelected()) {
                        dbf = new URL("file://localhost/c:/data/rivers.dbf");
                        shp = new URL("file://localhost/c:/data/rivers.shp");
                        shx = new URL("file://localhost/c:/data/rivers.shx");
                        rivers = new EsriLayer("Southwest", dbf, shp, shx);

                        dbf = new URL("file://localhost/c:/data/cities.dbf");
                        shp = new URL("file://localhost/c:/data/cities.shp");
                        shx = new URL("file://localhost/c:/data/cities.shx");
                        cities = new EsriLayer("Cities", dbf, shp, shx);

                        dbf = new URL("file://localhost/c:/data/states.dbf");
                        shp = new URL("file://localhost/c:/data/states.shp");
                        shx = new URL("file://localhost/c:/data/states.shx");
                        states = new EsriLayer("States", dbf, shp, shx);
                    } else {
                        dbf = new URL(getCodeBase(), "resources/rivers.dbf");
                        shp = new URL(getCodeBase(), "resources/rivers.shp");
                        shx = new URL(getCodeBase(), "resources/rivers.shx");
                        rivers = new EsriLayer("Rivers", dbf, shp, shx);

                        dbf = new URL(getCodeBase(), "resources/cities.dbf");
                        shp = new URL(getCodeBase(), "resources/cities.shp");
                        shx = new URL(getCodeBase(), "resources/cities.shx");
                        cities = new EsriLayer("Cities", dbf, shp, shx);

                        dbf = new URL(getCodeBase(), "resources/states.dbf");
                        shp = new URL(getCodeBase(), "resources/states.shp");
                        shx = new URL(getCodeBase(), "resources/states.shx");
                        states = new EsriLayer("States", dbf, shp, shx);
                    }

                    _layerHandler.addLayer(rivers);
                    _layerHandler.addLayer(cities);
                    _layerHandler.addLayer(states);
                    _cmdAddLayers.setEnabled(false);
                    _cmdShowTable.setEnabled(true);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        _cmdShowTable = new JButton("View Table");
        _cmdShowTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Layer[] layers = _layerHandler.getMapLayers();
                if (layers.length >= 4) {
                    EsriLayer layer = pickEsriLayer();
                    showTable(layer);
                    _cmdAddLayers.setEnabled(false);
                }
            }
        });

        _toolPanel.add(_runningLocally);
        _toolPanel.add(_cmdAddLayers, 0);
        _toolPanel.add(_cmdShowTable, 1);
        _toolPanel.add((Tool) _omts);

        getContentPane().add(_toolPanel, BorderLayout.NORTH);
        getContentPane().add(_mapBean, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Displays a new window containing the tabular data for the passed-in layer
     * 
     * @param layer The layer whose data is to be displayed
     */
    public void showTable(final EsriLayer layer) {
        JFrame frame = new JFrame("Table");
        DbfTableModel model = layer.getModel();
        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);
        frame.getContentPane().add(pane, BorderLayout.CENTER);

        ListSelectionModel lsm = table.getSelectionModel();
        lsm.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                ListSelectionModel lsm2 = (ListSelectionModel) e.getSource();
                if (lsm2.isSelectionEmpty()) {
                    // no rows are selected
                } else {
                    int index = lsm2.getMinSelectionIndex();
                    EsriGraphicList list = layer.getEsriGraphicList();
                    OMGraphic graphic = list.getOMGraphicAt(index);
                    graphic.select();
                    list.generate(_mapBean.getProjection());
                    layer.repaint();
                }
            }
        });
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    /**
     * Main method to facilitate testing and to run as stand alone application.
     */
    public static void main(String args[]) {
        ExampleApplet example = new ExampleApplet();
        JFrame frame = new JFrame();
        frame.getContentPane().add(example);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}