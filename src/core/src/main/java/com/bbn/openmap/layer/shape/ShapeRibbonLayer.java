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
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JTextArea;

/**
 * An OpenMap Layer that displays outlines of a shapefile, extended or shrunk by
 * some amount ["ribbon"]. You can tell it which specific shape you want in a
 * shapefile with dbfcolumnchoice and defaultdbfcolumnvalue or includeAllShapes
 * to avoid filtering. The gui for the layer is populated with values from the
 * requested DBF column.
 * 
 * <pre><code>
 *
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
 *
 * ############################
 * 
 * </code></pre>
 * 
 * @author Gary Briggs 
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

        // Internally, we store a copy of the poly projected forward
        List<List<LatLonPoint>> currAreaOutline = null;
        
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
            
                GridBagConstraints gbc_remainder = new GridBagConstraints();
                gbc_remainder.gridwidth = GridBagConstraints.REMAINDER;
                
                JPanel pan = new JPanel(new GridBagLayout());
                
		JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
		if (null == esriLayer) {
			optionsPanel.add(new JLabel("No data loaded"));
			return optionsPanel;
		}

		optionsPanel.add(new JLabel(String.format("Choose Range (%s)", units.getAbbr())));
		final JSpinner rangeSpinner = new JSpinner(
				new SpinnerNumberModel(units.fromRadians(rangeRadians), -1000.0, 1000.0, 100.0));
		optionsPanel.add(rangeSpinner);

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
		optionsPanel.add(new JLabel("Choose a shape:"));
		optionsPanel.add(combo);

		optionsPanel.add(new JLabel("Fill Ribbon"));
		final JCheckBox fillRibbonCheck = new JCheckBox("", fillRibbon);
		optionsPanel.add(fillRibbonCheck);

		optionsPanel.add(new JLabel("Remove Shape From Fill"));
		final JCheckBox removeShapeFromFillCheck = new JCheckBox("", removeShapeFromFill);
		optionsPanel.add(removeShapeFromFillCheck);

		optionsPanel.add(new JLabel("Include All Shapes"));
		final JCheckBox includeAllShapesCheck = new JCheckBox("", includeAllShapes);
		optionsPanel.add(includeAllShapesCheck);

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
                pan.add(optionsPanel, gbc_remainder);
                
                // Add a sub-poly calculator
                JPanel subPolyPanel = new JPanel(new GridBagLayout());
                
                subPolyPanel.add(new JLabel(String.format("Points every %s along the shape ribbon:", units.getAbbr())));
                final JSpinner pointsEverySpinner = new JSpinner(
				new SpinnerNumberModel(30.0, 0.1, 10000.0, 10.0));
		subPolyPanel.add(pointsEverySpinner, gbc_remainder);
                final JTextArea pointsEveryOutputText = new JTextArea();
                pointsEveryOutputText.setPreferredSize(new Dimension(400, 150));
                pointsEveryOutputText.setLineWrap(true);
                subPolyPanel.add(pointsEveryOutputText);
                
                pointsEverySpinner.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        double pointsEvery = ((SpinnerNumberModel)pointsEverySpinner.getModel()).getNumber().doubleValue();
                        double pointEveryRad = units.toRadians(pointsEvery);
                        List<LatLonPoint> pointsAroundPolys = getPointsAroundPolys(currAreaOutline, pointEveryRad);
                        StringBuilder sb = new StringBuilder();
                        for(LatLonPoint p : pointsAroundPolys) {
                            sb.append(p.getLatitude());
                            sb.append(",");
                            sb.append(p.getLongitude());
                            sb.append(";");
                        }
                        pointsEveryOutputText.setText(sb.toString());
                    }
                    
                });
                
                pan.add(subPolyPanel, gbc_remainder);
                
		return pan;
	}

	/**
	 * Pass a distRadians of zero to generate an Area
	 * 
	 * @param g
	 * @param distRadians
	 * @return
	 */
	public Area createAreaFromEsri(OMGraphic g, double distRadians, List<List<LatLonPoint>> polys) {
		Area a = new Area();
		if (g instanceof EsriGraphicList) {
			// Recurse if necessary
			EsriGraphicList esriList = (EsriGraphicList) g;
			for (OMGraphic omg : esriList) {
				a.add(createAreaFromEsri(omg, distRadians, polys));
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
                                
                                // Stash a copy, projected back into l/l
                                if(null != polys && null != oneRingShape) {
                                    PathIterator pathIterator = oneRingShape.getPathIterator(null);
                                    List<LatLonPoint> pointList = new ArrayList<LatLonPoint>();
                                    double[] coords = new double[6];
                                    
                                    Projection proj = getProjection();
                                    for(; !pathIterator.isDone(); pathIterator.next()) {
                                        LatLonPoint.Double llp = new LatLonPoint.Double();
                                        int segtype = pathIterator.currentSegment(coords);
                                        switch(segtype) {
                                            case PathIterator.SEG_MOVETO:
                                                // One point
                                                proj.inverse(coords[0], coords[1], llp);
                                                pointList.add(llp);
                                                break;
                                            case PathIterator.SEG_LINETO:
                                                // One point
                                                proj.inverse(coords[0], coords[1], llp);
                                                pointList.add(llp);
                                                break;
                                            case PathIterator.SEG_QUADTO:
                                            case PathIterator.SEG_CUBICTO:
                                            case PathIterator.SEG_CLOSE:
                                                // RibbonMaker doesn't generate these
                                            default:
                                                // *shrug*
                                        }
                                    }
                                    polys.add(pointList);
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
				a.add(getOneRibbonedAreaFromShapeFile(i, null));
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
                            currAreaOutline = new ArrayList<List<LatLonPoint>>();
                            a = getOneRibbonedAreaFromShapeFile(rownum, currAreaOutline);
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
         * Given a list of polygons represented by points, return a new set of
         * points that is "a point every pointEveryRad distance around those polys"
         * @param polys
         * @param pointEveryRad
         * @return 
         */
        private List<LatLonPoint> getPointsAroundPolys(List<List<LatLonPoint>> polys, double pointEveryRad) {
            List<LatLonPoint> retval = new ArrayList<LatLonPoint>();
            if(null == polys) {
                return retval;
            }
            
            for(List<LatLonPoint> onePoly : polys) {
                double currDistRad = 0.0;
                retval.add(onePoly.get(0));
                for(int i = 0; i < onePoly.size()-1; i++) {
                    LatLonPoint p1 = onePoly.get(i);
                    LatLonPoint p2 = onePoly.get(i+1);
                    double p1_p2_dist = p1.distance(p2);
                    double p1_p2_az = p1.azimuth(p2);
                    
                    while(currDistRad + p1_p2_dist >= pointEveryRad) {
                        LatLonPoint nextp = p1.getPoint(pointEveryRad - currDistRad, p1_p2_az);
                        retval.add(nextp);
                        p1 = nextp;
                        p1_p2_dist = p1.distance(p2);
                        currDistRad = 0.0;
                    }
                    
                    currDistRad += p1_p2_dist;
                }
            }
            return retval;
        }
        
	/**
	 * From the esri layer, create an Area using the current settings
	 * 
	 * @param rownum
	 * @return
	 */
	private Area getOneRibbonedAreaFromShapeFile(int rownum, List<List<LatLonPoint>> polys) {
		OMGraphic sourceGraphic = esriLayer.getEsriGraphicList().get(rownum);

		// Create the graphics representing the ribbon
		Area ribbonArea = createAreaFromEsri(sourceGraphic, rangeRadians, polys);

		if (null != ribbonArea) {
			if (removeShapeFromFill || rangeRadians < 0) {
				Area countryArea = createAreaFromEsri(sourceGraphic, 0.0, null);
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
