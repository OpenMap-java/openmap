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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/EditableOMRangeRings.java,v $
// $RCSfile: EditableOMRangeRings.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.util.stateMachine.State;
import com.bbn.openmap.omGraphics.editable.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 */
public class EditableOMRangeRings extends EditableOMCircle {

    /**
     * Create the EditableOMRangeRings, setting the state machine to create
     * the circle off of the gestures.  
     */
    public EditableOMRangeRings() {
	createGraphic(null);
    }

    /**
     * Create an EditableOMRangeRings with the circleType and renderType
     * parameters in the GraphicAttributes object.
     */
    public EditableOMRangeRings(GraphicAttributes ga) {
	createGraphic(ga);
    }

    /**
     * Create the EditableOMRangeRings with an OMCircle already defined, ready
     * for editing.
     *
     * @param omc OMCircle that should be edited.
     */
    public EditableOMRangeRings(OMRangeRings omc) {
	setGraphic(omc);
    }

    /**
     * Create and set the graphic within the state machine.  The
     * GraphicAttributes describe the type of circle to create. 
     */
    public void createGraphic(GraphicAttributes ga) {
	init();
	stateMachine.setUndefined();
	int renderType = OMGraphic.RENDERTYPE_LATLON;

	if (ga != null) {
	    renderType = ga.getRenderType();
	}

	if (Debug.debugging("eomc")) {
	    Debug.output("EditableOMRangeRings.createGraphic(): rendertype = " +
			 renderType);
	}

	circle = new OMRangeRings(90f, -180f, 0f);

	if (ga != null) {
	    ga.setTo(circle);
	}
    }

    /**
     * If this EditableOMGraphic has parameters that can be
     * manipulated that are independent of other EditableOMGraphic
     * types, then you can provide the widgets to control those
     * parameters here.  By default, returns the GraphicAttributes GUI
     * widgets.  If you don't want a GUI to appear when a widget is
     * being created/edited, then don't call this method from the
     * EditableOMGraphic implementation, and return a null Component
     * from getGUI.
     * @param graphicAttributes the GraphicAttributes to use to get
     * the GUI widget from to control those parameters for this EOMG.
     * @return java.awt.Component to use to control parameters for this EOMG.
     */
    public java.awt.Component getGUI(GraphicAttributes graphicAttributes) {
	Debug.message("eomg", "EditableOMRangeRings.getGUI");
	if (showGUI) {
	    return buildGUI(graphicAttributes);
	}
	return null;
    }

    public void updateInterval(int val) {
	((OMRangeRings)circle).setInterval(val);
	if (intervalField != null) {
	    intervalField.setText(Integer.toString(val));
	}
    }

    protected JTextField intervalField = null;
    protected transient java.text.DecimalFormat df = new java.text.DecimalFormat();

    protected java.awt.Component buildGUI(GraphicAttributes graphicAttributes) {
	javax.swing.Box attributeBox = 
	    javax.swing.Box.createVerticalBox();
	
	if (graphicAttributes != null) {
	    attributeBox.add(graphicAttributes.getGUI());
	}
	
	JPanel intervalPanel = PaletteHelper.createPaletteJPanel("Interval");
	intervalField = new JTextField(Integer.toString(((OMRangeRings)circle).getInterval()));
	intervalField.setToolTipText("Value for interval between rings.");
	intervalField.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    String intervalStr = ((JTextField)(ae.getSource())).getText();

		    int oldValue = ((OMRangeRings)circle).getInterval();
		    int value = oldValue;

		    try {
			if (intervalStr.toLowerCase().endsWith("m")) {
			    intervalStr = intervalStr.substring(0, intervalStr.length()-1);
			    value = (int)df.parse(intervalStr).intValue() * 1000000;
			}
			else if (intervalStr.toLowerCase().endsWith("k")) {
			    intervalStr = intervalStr.substring(0, intervalStr.length()-1);
			    value = df.parse(intervalStr).intValue() * 1000;
			}
			else if (intervalStr.trim().equals("")) {
			    value = oldValue;
			}
			else {
			    value = df.parse(intervalStr).intValue();
			}
		    } catch (java.text.ParseException e) {
			System.err.println("OMToolSet.setValue(): invalid value: " +
					   intervalStr);
		    } catch (NumberFormatException e) {
			System.err.println("OMToolSet.setValue(): invalid value: " +
					   intervalStr);
		    }

		    if (value <= 0) {
			value = oldValue;
		    }

		    updateInterval(value);
		}
	    });
	intervalPanel.add(intervalField);
	
// 	JSlider intervalSlide = new JSlider(
// 	    JSlider.HORIZONTAL, 1/*min*/, 200/*max*/, 
// 	    ((OMRangeRings)circle).getInterval()/*inital*/);
// 	java.util.Hashtable dict = new java.util.Hashtable();
// 	dict.put(new Integer(1), new JLabel("1"));
// 	dict.put(new Integer(50), new JLabel("50"));
// 	dict.put(new Integer(100), new JLabel("100"));
// 	dict.put(new Integer(150), new JLabel("150"));
// 	dict.put(new Integer(200), new JLabel("200"));
// 	intervalSlide.setLabelTable(dict);
// 	intervalSlide.setPaintLabels(true);
// 	intervalSlide.setMajorTickSpacing(10);
// 	intervalSlide.setPaintTicks(true);
// 	intervalSlide.setSnapToTicks(false);
// 	intervalSlide.addChangeListener(new ChangeListener() {
// 		public void stateChanged(ChangeEvent ce) {
// 		    JSlider slider = (JSlider) ce.getSource();
// 		    if (slider.getValueIsAdjusting()) {
// 			((OMRangeRings)circle).setInterval(slider.getValue());
// 		    }
// 		}
// 	    });
// 	intervalPanel.add(intervalSlide);


	attributeBox.add(intervalPanel);
	
	JPanel unitPanel = PaletteHelper.createPaletteJPanel("Unit Type");
	
	Length[] available = Length.getAvailable();
	
	String[] unitStrings = new String[available.length + 1];
	
	String current = null;
	Length l = ((OMRangeRings)circle).getIntervalUnits();
	if (l != null) {
	    current = l.toString();
	}	       
	
	int currentIndex = unitStrings.length - 1;
	
	for (int i = 0; i < available.length; i++) {
	    unitStrings[i] = available[i].toString();
	    if (current != null && unitStrings[i].equals(current)) {
		currentIndex = i;
	    }
	}
	unitStrings[unitStrings.length - 1] = "None";
	
	JComboBox unitList = new JComboBox(unitStrings);
	unitList.setSelectedIndex(currentIndex);
	unitList.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    JComboBox jcb = (JComboBox) e.getSource();
		    ((OMRangeRings)circle).setIntervalUnits(Length.get((String)jcb.getSelectedItem()));
		    
		}
	    });
	unitPanel.add(unitList);
	attributeBox.add(unitPanel);
	
	return attributeBox;
    }
}




















