/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bbn.openmap.layer.shape;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.dataAccess.shape.EsriPolygon;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMAreaList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMShape;
import com.bbn.openmap.omGraphics.util.RibbonMaker;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.util.PropUtils;

/**
 * An OpenMap Layer that displays outlines of a shapefile, extended or shrunk by
 * some amount ["ribbon"]. You can tell it which specific shape you want in a
 * shapefile with dbfcolumnchoice and defaultdbfcolumnvalue or includeAllShapes
 * to avoid filtering. The gui for the layer is populated with values from the
 * requested DBF column.
 * 
 * <p>
 * <code><pre>
        
 * ############################
 * # Example Properties for a shape ribbon layer, using political boundaries
 * # Common properties for all layers
 * shapeRibbonLayer.class=com.bbn.openmap.layer.shape.ShapeRibbonLayer
 * shapeRibbonLayer.prettyName=Country Ribbons
 * 
 * # Properties for configuring shape and layer choices
 * # Assumes that .shp is accompanied by .dbf and .ssx files
 * shapeRibbonLayer.shapeFile=data/shape/cntry02/cntry02.shp
 * shapeRibbonLayer.dbfColumnChoice=CNTRY_NAME
 * shapeRibbonLayer.defaultdbfColumnValue=United States
 * # Be warned that on big complex sets of shapes, this can take a while
 * includeAllShapes=false
 * 
 * # Properties for editing the ribbon itself
 * shapeRibbonLayer.rangeunits=km
 * shapeRibbonLayer.range=100
 * shapeRibbonLayer.lineColor=0000ff
 * shapeRibbonLayer.fillColor=330000ff
 * shapeRibbonLayer.fillRibbon=true
 * # If a filled color is set, this can remove the shape itself from the filled area
 * shapeRibbonLayer.removeShapeFromFill=true
 
 * ############################
 * </pre></code>
 * 
 * @author Gary Briggs <chunky@icculus.org>
 */
public class ShapeRibbonLayer extends OMGraphicHandlerLayer {

	// Use EsriLayer to do all the legwork loading files for us
	EsriLayer esriLayer = null;

	Double rangeRadians = Length.KM.toRadians(100.0);
	boolean removeShapeFromFill = true;
	boolean fillRibbon = true;
	String shapeFile = "data/shape/cntry02/cntry02.shp";
	String dbfColumnChoice = "CNTRY_NAME";
	String chosenColumnName = "United States";
	Boolean includeAllShapes = false;
	Length units = Length.KM;
	DrawingAttributes da = null;

	// Numerical range from the shape
	public final static String rangePropertyProperty = "range";
	public final static String rangeUnitsProperty = "rangeunits";
	public final static String shapeFileProperty = "shapeFile";
	public final static String removeShapeFromFillProperty = "removeShapeFromFill";
	public final static String fillRibbonProperty = "fillRibbon";
	public final static String chosendbfColumnNameProperty = "defaultdbfColumnValue";
	public final static String dbfColumnChoiceProperty = "dbfColumnChoice";
	public final static String includeAllShapesProperty = "includeAllShapes";

	@Override
	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		String realPrefix = PropUtils.getScopedPropertyPrefix(this);

		String lengthUnitProp = props.getProperty(realPrefix + rangeUnitsProperty, units.getAbbr());
		Length testLengthUnit = Length.get(lengthUnitProp);
		if (null != testLengthUnit) {
			units = testLengthUnit;
		}

		shapeFile = props.getProperty(realPrefix + shapeFileProperty, shapeFile);
		dbfColumnChoice = props.getProperty(realPrefix + dbfColumnChoiceProperty, dbfColumnChoice);
		chosenColumnName = props.getProperty(realPrefix + chosendbfColumnNameProperty, chosenColumnName);

		double originalRangeValue = PropUtils.doubleFromProperties(props, realPrefix + rangePropertyProperty,
				units.fromRadians(rangeRadians));
		rangeRadians = units.toRadians(originalRangeValue);

		removeShapeFromFill = PropUtils.booleanFromProperties(props, realPrefix + removeShapeFromFillProperty,
				removeShapeFromFill);
		includeAllShapes = PropUtils.booleanFromProperties(props, realPrefix + includeAllShapesProperty,
				includeAllShapes);
		fillRibbon = PropUtils.booleanFromProperties(props, realPrefix + fillRibbonProperty, fillRibbon);

		da = new DrawingAttributes(realPrefix, props);
		loadEsriData();
	}

	/**
	 * Create the esriLayer object. Assume that .shp is accompanied by .dbf and
	 * .shx files
	 */
	private void loadEsriData() {

		String prefix = shapeFile.replaceAll(".shp$", "");
		File dbf = new File(prefix.concat(".dbf"));
		File shp = new File(prefix.concat(".shp"));
		File shx = new File(prefix.concat(".shx"));
		String name = shp.getName();

		try {
			esriLayer = new EsriLayer(name, dbf.toURI().toURL(), shp.toURI().toURL(), shx.toURI().toURL());
		} catch (MalformedURLException ex) {
			Logger.getLogger(ShapeRibbonLayer.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public Component getGUI() {
		JPanel guiPanel = new JPanel(new GridLayout(0, 2));
		if (null == esriLayer) {
			guiPanel.add(new JLabel("No data loaded"));
			return guiPanel;
		}

		guiPanel.add(new JLabel(String.format("Choose Range (%s)", units.getAbbr())));
		final JSpinner rangeSpinner = new JSpinner(
				new SpinnerNumberModel(units.fromRadians(rangeRadians), -1000.0, 1000.0, 100.0));
		guiPanel.add(rangeSpinner);

		DbfTableModel model = esriLayer.getModel();

		int columnIndexForName = model.getColumnIndexForName(dbfColumnChoice);
		String[] knownValues = new String[model.getRowCount()];
		for (int i = 0; i < model.getRowCount(); i++) {
			List<Object> record = model.getRecord(i);
			String value = (String) record.get(columnIndexForName);
			knownValues[i] = value;
		}
		Arrays.sort(knownValues);
		int chosenidx = Arrays.binarySearch(knownValues, chosenColumnName);

		final JComboBox<String> combo = new JComboBox<String>(knownValues);
		combo.setSelectedIndex(chosenidx);
		guiPanel.add(new JLabel("Choose a shape:"));
		guiPanel.add(combo);

		guiPanel.add(new JLabel("Fill Ribbon"));
		final JCheckBox fillRibbonCheck = new JCheckBox("", fillRibbon);
		guiPanel.add(fillRibbonCheck);

		guiPanel.add(new JLabel("Remove Shape From Fill"));
		final JCheckBox removeShapeFromFillCheck = new JCheckBox("", removeShapeFromFill);
		guiPanel.add(removeShapeFromFillCheck);

		guiPanel.add(new JLabel("Include All Shapes"));
		final JCheckBox includeAllShapesCheck = new JCheckBox("", includeAllShapes);
		guiPanel.add(includeAllShapesCheck);

		// Set up listeners so the gui elements update the screen automatically
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chosenColumnName = (String) combo.getSelectedItem();
				fillRibbon = fillRibbonCheck.isSelected();
				removeShapeFromFill = removeShapeFromFillCheck.isSelected();
				includeAllShapes = includeAllShapesCheck.isSelected();
				doPrepare();
			}
		};
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Double val = (Double) rangeSpinner.getValue();
				rangeRadians = units.toRadians(val);
				doPrepare();
			}
		};
		rangeSpinner.addChangeListener(changeListener);
		fillRibbonCheck.addActionListener(actionListener);
		includeAllShapesCheck.addActionListener(actionListener);
		removeShapeFromFillCheck.addActionListener(actionListener);
		combo.addActionListener(actionListener);
		return guiPanel;
	}

	/**
	 * Pass a distRadians of zero to generate an Area
	 * 
	 * @param g
	 * @param distRadians
	 * @return
	 */
	public Area createAreaFromEsri(OMGraphic g, double distRadians) {
		Area a = new Area();
		if (g instanceof EsriGraphicList) {
			// Recurse if necessary
			EsriGraphicList esriList = (EsriGraphicList) g;
			for (OMGraphic omg : esriList) {
				a.add(createAreaFromEsri(omg, distRadians));
			}
		} else if (g instanceof EsriPolygon) {
			EsriPolygon esriGraphic = (EsriPolygon) g;
			double epsilon = 4.778825E-10; // Borrowed from ribbonmaker

			if (Math.abs(distRadians) <= epsilon) {
				esriGraphic.generate(getProjection());
				GeneralPath shape = esriGraphic.getShape();
				Area oneArea = new Area(shape);
				a.add(oneArea);
			} else {
				double[] ll_arr = esriGraphic.getLatLonArray();
				// Wind the array backwards for negative values. This costs no
				// extra memory
				if (distRadians < 0) {
					distRadians = -distRadians;
					// Flip the poly so it winds the other way
					final int numPoints = ll_arr.length / 2;
					for (int i = 0; i < numPoints / 2; i++) {
						double tmplat = ll_arr[2 * i];
						double tmplon = ll_arr[2 * i + 1];
						ll_arr[2 * i] = ll_arr[2 * (numPoints - i - 1)];
						ll_arr[2 * i + 1] = ll_arr[2 * (numPoints - i - 1) + 1];
						ll_arr[2 * (numPoints - i - 1)] = tmplat;
						ll_arr[2 * (numPoints - i - 1) + 1] = tmplon;
					}
				}
				final RibbonMaker ribbon = RibbonMaker.createFromRadians(ll_arr);
				OMAreaList outerRing = ribbon.getOuterRing(distRadians);
				outerRing.generate(getProjection());
				final GeneralPath oneRingShape = outerRing.getShape();
				if (null != oneRingShape) {
					a.add(new Area(oneRingShape));
				}
			}
		}
		return a;
	}

	@Override
	public synchronized OMGraphicList prepare() {
		OMGraphicList l = new OMGraphicList();
		if (null == esriLayer) {
			// Don't actually have any data
			return l;
		}

		Area a = null;

		DbfTableModel model = esriLayer.getModel();
		if (includeAllShapes) {
			a = new Area();
			for (int i = 0; i < model.getRowCount(); i++) {
				System.out.println(String.format("%d/%d", i, model.getRowCount()));
				a.add(getOneRibbonedAreaFromShapeFile(i));
			}
		} else {
			// Find the row index we need in the shape data
			int columnIndexForName = model.getColumnIndexForName(dbfColumnChoice);
			int rownum = -1;
			for (int row = 0; row < model.getRowCount(); row++) {
				Object valueAt = model.getValueAt(row, columnIndexForName);
				if (valueAt.equals(chosenColumnName)) {
					rownum = row;
					break;
				}
			}

			// Assuming we found it...
			if (rownum >= 0) {
				a = getOneRibbonedAreaFromShapeFile(rownum);
			}
		}
		l.add(new OMShape.PROJECTED(a));
		if (fillRibbon) {
			l.setFillPaint(da.getFillPaint());
		}
		l.setLinePaint(da.getLinePaint());

		return l;
	}

	/**
	 * From the esri layer, create an Area using the current settings
	 * 
	 * @param rownum
	 * @return
	 */
	private Area getOneRibbonedAreaFromShapeFile(int rownum) {
		OMGraphic sourceGraphic = esriLayer.getEsriGraphicList().get(rownum);

		// Create the graphics representing the ribbon
		Area ribbonArea = createAreaFromEsri(sourceGraphic, rangeRadians);

		if (null != ribbonArea) {
			if (removeShapeFromFill || rangeRadians < 0) {
				Area countryArea = createAreaFromEsri(sourceGraphic, 0.0);
				if (rangeRadians > 0) {
					ribbonArea.subtract(countryArea);
				} else {
					// If the ribbon is inside, always subtract the ribbon from
					// the shape
					countryArea.subtract(ribbonArea);
					ribbonArea = countryArea;
				}
			}
		}
		return ribbonArea;
	}
}
