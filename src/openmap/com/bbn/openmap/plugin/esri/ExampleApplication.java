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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/ExampleApplication.java,v $
// $RCSfile: ExampleApplication.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.plugin.esri;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.Layer;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MouseDelegator;
import com.bbn.openmap.event.*;
import com.bbn.openmap.gui.*;
import com.bbn.openmap.gui.OMToolSet;
import com.bbn.openmap.gui.ToolPanel;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.dataAccess.shape.*;
import com.bbn.openmap.dataAccess.shape.input.*;
import com.bbn.openmap.dataAccess.shape.output.*;
import com.bbn.openmap.proj.GreatCircle;

/*
 * ExampleApplication illustrates three uses of the EsriLayer: 1) how
 * to create new geometry and tabular data at run time, 2) how to load
 * geometry and tabular data from a file system or a web server, and
 * 3) how to save a layer conforming to Esri's shape file format
 * specification.
 * @author Doug Van Auken 
 */
public class ExampleApplication extends JFrame {
    private EsriLayer _drawableLayer = null;
    private MapBean _mapBean = null;
    private Layer _layers[] = null;
    private LayerHandler _layerHandler = null;
    private OMToolSet _omts = null;
    private ToolPanel _toolPanel = null;
    private JMenuBar _menuBar = null;
    private MouseDelegator _mouseDelegator = null;
    private MouseModePanel _mouseModePanel = null;

    /* Menu bar declarations */
    private DefaultHelpMenu _defaultHelpMenu = null;
    private JMenu _fileMenu = null;
    private LayersMenu _layersMenu = null;
    private JMenuItem _openFileChooser = null;
    private JMenuItem _saveFileChooser = null;
    private JMenuItem _showTable = null;
    private JMenuItem _httpExample = null;
    private JMenuItem _exit = null;
    private JMenuItem _addShape = null;
    private JMenuItem _setModel = null;
    private JMenuItem _createEsriFile = null;

    /*
     * Contructor.
     */
    public ExampleApplication() {
	_mapBean = new MapBean();
	_menuBar = new JMenuBar();
	_toolPanel = new ToolPanel();
	_omts = new OMToolSet();
	_omts.findAndInit(_mapBean);
	_mouseDelegator = new MouseDelegator(_mapBean);
	_mouseModePanel = new MouseModePanel(_mouseDelegator);

	NavMouseMode nmm = new NavMouseMode();
	SelectMouseMode smm = new SelectMouseMode();
	_mouseDelegator.addMouseMode(nmm);
	_mouseDelegator.addMouseMode(smm);

	setupUI();
    }

    /**
     * Presents a JOptionPane to the user to enable them to specify
     * which file to retreive
     * @return A string denoting the location of the file to retreive 
     */
    public String getRemoteFile() {
	Object urls[] = {"http://www.somewebserver.com/omdemo/resources/cities", "http://www.somewebserver.com/omdemo/resources/rivers", "http://www.somewebserver.com/omdemo/resources/southwest", "http://www.somewebserver.com/omdemo/resources/states"};
	String url = (String)JOptionPane.showInputDialog(null, "Select a url", "Input", JOptionPane.INFORMATION_MESSAGE, null, urls, urls[0]);
	return url;
    }

    /**
     * Presents a JOptionPane to the user to enable them to pick which
     * layer should be persisted to file.
     * @return The layer the user selected 
     */
    private EsriLayer pickEsriLayer() {
	Layer[] layers = _layerHandler.getLayers();
	Vector vector = new Vector();
	for(int n=0; n<=layers.length - 1; n++) {
	    if (layers[n] instanceof EsriLayer) {
		String name = layers[n].getName();
		vector.add(name);
	    }
	}
	Object[] objects = vector.toArray();
	if (objects.length > 0) {
	    String selected = (String)JOptionPane.showInputDialog(null, "Choose one", "Input", JOptionPane.INFORMATION_MESSAGE, null, objects, objects[0]);
	    for(int n=0; n<=layers.length -1; n++) {
		String name = layers[n].getName();
		if (name.equalsIgnoreCase(selected)) {
		    return (EsriLayer)layers[n];
		}
	    }
	}
	return null;
    }

    /**
     * Sets up user interface.
     */
    public void setupUI() {
	getContentPane().setLayout(new BorderLayout());
	setSize(640, 480);
	_mapBean.setCenter(new LatLonPoint(43.0f, -95.0f));
	_mapBean.setScale(120000000f);
	_layers = new Layer[1];

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

	//Finish setting up the GUI.
	_layerHandler = new LayerHandler(_layers);
	_layerHandler.addLayerListener(_mapBean);
	_layersMenu = new LayersMenu(_layerHandler);
	_fileMenu = new JMenu("File");

	_httpExample = new JMenuItem("HTTP Example");
	_httpExample.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try{
			String url = stripExtension(getRemoteFile(), "shp");
			URL dbf = null, shp = null, shx = null;
			dbf = new URL(url + ".dbf");
			shp = new URL(url + ".shp");
			shx = new URL(url + ".shx");
			EsriLayer layer = new EsriLayer(url, dbf, shp, shx);
			_layerHandler.addLayer(layer);
		    }
		    catch(Exception exception) {
			System.out.println(exception);
		    }
		}
	    });

	_showTable = new JMenuItem("Show Table");
	_showTable.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    EsriLayer layer = pickEsriLayer();
		    showTable(layer);
		}
	    });

	_setModel = new JMenuItem("Set Model");
	_setModel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    try{
			_drawableLayer = new EsriLayer("Drawable Layer", 3, 1);   //Create a polyline layer
			_drawableLayer.setName("Drawable Layer");
			_layerHandler.addLayer(_drawableLayer);
			DbfTableModel model = new DbfTableModel(1);

			model.setDecimalCount(0, (byte)0);
			model.setLength(0, (byte)10);
			model.setColumnName(0, "Column1");
			model.setType(0, (byte)DbfTableModel.TYPE_CHARACTER);

			_drawableLayer.setModel(model);
			_addShape.setEnabled(true);
			_setModel.setEnabled(false);
		    }
		    catch(Exception exception) {
			System.out.println(exception);
		    }

		}
	    });

	_addShape = new JMenuItem("Add Shape");
	_addShape.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    EsriPolylineList shapeData = new EsriPolylineList();
		    ArrayList tabularData = new ArrayList();
		    float[] part0 = new float[]{35.0f, -120.0f, -25.0f, -95.0f, 56.0f, -30.0f};
		    float[] part1 = new float[]{-15.0f, -110.0f, 13.0f, -80.0f, -25.0f, 10.0f};
		    EsriPolyline poly0 = new EsriPolyline(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
		    EsriPolyline poly1 = new EsriPolyline(part1, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
		    shapeData.add(poly0);  //part 1
		    shapeData.add(poly1);  //part 2
		    shapeData.generate(_mapBean.getProjection());
		    tabularData.add(0, "a value");
		    _drawableLayer.addRecord(shapeData, tabularData);
		    _drawableLayer.repaint();
		}
	    });

	_openFileChooser = new JMenuItem("Add Shape File");
	_openFileChooser.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JFileChooser fileChooser = new JFileChooser();
		    fileChooser.setFileFilter(new EsriFilter());
		    int returnVal = fileChooser.showOpenDialog(null);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			try{
			    File shp = fileChooser.getSelectedFile();
			    String s = shp.getCanonicalPath();
			    int pos1 = s.lastIndexOf('.');
			    String name = s.substring(0, pos1);
			    File shx = new File(s.substring(0, pos1) + ".shx");
			    File dbf = new File(s.substring(0, pos1) + ".dbf");
			    EsriLayer layer = new EsriLayer(name, dbf.toURL(), shp.toURL(), shx.toURL());
			    _layerHandler.addLayer(layer);
			}
			catch(Exception exception) {
			    System.out.println(exception);
			}
		    }
		}
	    });

	_exit = new JMenuItem("Exit");
	_exit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    System.exit(0);
		}
	    });

	_saveFileChooser = new JMenuItem("Save File");
	_saveFileChooser.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    EsriLayer layer = pickEsriLayer();
		    if (layer != null) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new EsriFilter());
			int returnVal = fileChooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    try{
				File file = fileChooser.getSelectedFile();
				String path = file.getCanonicalPath();
				EsriGraphicList list = layer.getEsriGraphicList();

				ShpOutputStream pos = new ShpOutputStream(new FileOutputStream(path + ".shp"));
				int[][] indexData = pos.writeGeometry(list);

				ShxOutputStream xos = new ShxOutputStream(new FileOutputStream(path + ".shx"));
				xos.writeIndex(indexData, list.getType(), list.getExtents());

				DbfOutputStream dos = new DbfOutputStream(new FileOutputStream(path + ".dbf"));
				dos.writeModel(layer.getModel());
			    }
			    catch(Exception exception) {
				System.out.println(exception);
			    }
			}
		    }
		    else{
			// Add a dialog
		    }
		}
	    });
	_fileMenu.add(_openFileChooser);
	_fileMenu.add(_saveFileChooser);
	_fileMenu.add(new JSeparator());
	_fileMenu.add(_showTable);
	_fileMenu.add(new JSeparator());
	_fileMenu.add(_httpExample);
	_fileMenu.add(new JSeparator());
	_fileMenu.add(_setModel);
	_addShape.setEnabled(false);   //Disable ability for user to add a shape until they initialize the DbfTableModel
	_fileMenu.add(_addShape);
	_fileMenu.add(new JSeparator());
	_fileMenu.add(_exit);

	_defaultHelpMenu = new DefaultHelpMenu();

	_menuBar.add(_fileMenu);
	_menuBar.add(_layersMenu);
	_menuBar.add(_defaultHelpMenu);

	_omts.add(_mouseModePanel);;
	_toolPanel.add((Tool)_omts);

	setJMenuBar(_menuBar);
	getContentPane().add(_toolPanel, BorderLayout.NORTH);
	getContentPane().add(_mapBean, BorderLayout.CENTER);
	setVisible(true);
    }

    /**
     * Displays a new window containing the tabular data for the
     * passed-in layer
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
		    //Ignore extra messages.
		    if (e.getValueIsAdjusting()) {
			return;
		    }
		    ListSelectionModel lsm2 = (ListSelectionModel)e.getSource();
		    if (lsm2.isSelectionEmpty()) {
			//no rows are selected
		    }
		    else {
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
     * Strips file extension from a string that represents a file
     * @param file The file reference from which to strip file extensions
     * @param extension The extension to check for and to strip if it exists
     * @return A string less the respective extension
     */
    private String stripExtension(String file, String extension) {
	if (file.endsWith(extension)) {
	    int index = file.lastIndexOf('.');
	    String s = file.substring(0, index);
	    return s;
	}
	return file;
    }

    /**
     * Main method to facilitate testing and to run as stand alone application.
     */
    public static void main(String args[]) {
	com.bbn.openmap.util.Debug.init();
	ExampleApplication example = new ExampleApplication();
	example.setVisible(true);
    }

    /**
     * Used by the JFileChooser component to list only files whose extension
     * ends in '.shp'.
     */
    private class EsriFilter extends javax.swing.filechooser.FileFilter {
	public boolean accept(File f) {
	    if (f.isDirectory()) {
		return true;
	    }

	    String extension = getExtension(f);
	    if (extension != null) {
		if (extension.equalsIgnoreCase("shp")) {
		    return true;
		}
		else {
		    return false;
		}
	    }
	    return false;
	}

	/**
	 * Extracts the extension from the given file
	 * @param f The file from whose extension to extract
	 */
	private String getExtension(File f) {
	    String ext = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');

	    if (i > 0 &&  i < s.length() - 1) {
		ext = s.substring(i+1).toLowerCase();
	    }
	    return ext;
	}

	/**
	 * Sets the description string that will appear on the JFileChooser
	 * component as its initialized
	 */
	public String getDescription() {
	    return "Feature Data Source";
	}
    }
}
