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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/Attic/LineChoiceComboBox.java,v $
// $RCSfile: LineChoiceComboBox.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.URL;
import javax.swing.*;

/**
 * The LineChoiceComboBox presents a pop-up menu of lines with
 * different widths.  Given a DrawingAttributes object to control, it
 * will update the BasicStroke in the DrawingAttributes object when
 * different LineWidths are chosen.
 * @deprecated - Use BasicStrokeEditor instead.
 */
public class LineChoiceComboBox implements ActionListener {

    public final static LineChoice ONEPT = new LineChoice(new BasicStroke(1), 
							  "Line Width of 1");
    public final static LineChoice THREEPT = new LineChoice(new BasicStroke(3),
							    "Line Width of 3");
    public final static LineChoice FIVEPT = new LineChoice(new BasicStroke(5),
							   "Line Width of 5");
    public final static LineChoice SEVENPT = new LineChoice(new BasicStroke(7),
							    "Line Width of 7");
    public final static LineChoice NINEPT = new LineChoice(new BasicStroke(9),
							   "Line Width of 9");
    public final static LineChoice ELEVENPT = new LineChoice(new BasicStroke(11),
							     "Line Width of 11");

    /**
     * The DrawingAttributes to notify when things change. 
     */
    protected DrawingAttributes drawingAttributes;
	
    public LineChoiceComboBox() {}

    /*
     * Create and retrieve the JComboBox for the linewidths.
     */
    public JComboBox getComboBox(DrawingAttributes da) {
	drawingAttributes = da;

	JPanel lineWidthPanel = new JPanel();

	Object[] lineWidths = { ONEPT.getIcon(),
				THREEPT.getIcon(), 
				FIVEPT.getIcon(),
				SEVENPT.getIcon(),
				NINEPT.getIcon(), 
				ELEVENPT.getIcon() };
	
	JComboBox lineWidthList = new JComboBox(lineWidths);
	lineWidthList.setToolTipText("Line Width");
	
	int mWidth = 0;
	int mHeight = 0;
	
	for (int i = 0; i < lineWidths.length; i++) {
	    ImageIcon icon = (ImageIcon)lineWidths[i];
	    if (icon.getIconWidth() > mWidth) mWidth = icon.getIconWidth();
	    if (icon.getIconHeight() > mHeight) mHeight = icon.getIconHeight();
	}
	
	lineWidthList.setMaximumSize(new Dimension(mWidth + 2 /*border*/, mHeight));
	lineWidthList.setSelectedIndex(getLineWidthSelection(da.getStroke()));
	lineWidthList.addActionListener(this);
	return lineWidthList;
    }
    
    /**
     * Look at the line width of the current stroke, and send back an
     * index of the lineWidthList that best matches it.  It's a hack
     * because it knows the line width of the selection at an
     * index. 
     */
    public static int getLineWidthSelection(Stroke stroke) {
	if (stroke == null || !(stroke instanceof BasicStroke)) {
	    return 0;
	}

	float lw = ((BasicStroke)stroke).getLineWidth();
	int currentChoice = 0;
	if (lw >= (ONEPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 0;
	} 
	if (lw >= (THREEPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 1;
	} 
	if (lw >= (FIVEPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 2;
	} 
	if (lw >= (SEVENPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 3;
	} 
	if (lw >= (NINEPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 4;
	} 
	if (lw >= (ELEVENPT.getStroke().getLineWidth() - 1)) {
	    currentChoice = 5;
	}

	return currentChoice;
    }
    
    public void actionPerformed(ActionEvent e) {
	if (drawingAttributes == null) {
	    return;
	}

	BasicStroke currentStroke = (BasicStroke)drawingAttributes.getStroke();

	if (currentStroke == null) {
	    drawingAttributes.setStroke(new BasicStroke(1));
	    return;
	}

	float[] dash = currentStroke.getDashArray();
	float phase = currentStroke.getDashPhase();
	int cap =  currentStroke.getEndCap();
	int join =  currentStroke.getLineJoin();
	float miterLimit =  currentStroke.getMiterLimit();

	float lineWidth = ONEPT.getStroke().getLineWidth();

	JComboBox jcb = (JComboBox) e.getSource();
	int newView = jcb.getSelectedIndex();
	switch(newView) {
	case 1: 
	    lineWidth = THREEPT.getStroke().getLineWidth();
	    break;
	case 2:
	    lineWidth = FIVEPT.getStroke().getLineWidth(); 
	    break;
	case 3:
	    lineWidth = SEVENPT.getStroke().getLineWidth(); 
	    break;
	case 4:
	    lineWidth = NINEPT.getStroke().getLineWidth(); 
	    break;
	case 5: 
	    lineWidth = ELEVENPT.getStroke().getLineWidth(); 
	    break;
	default: 
	    lineWidth = ONEPT.getStroke().getLineWidth();
	}
	
	drawingAttributes.setStroke(new BasicStroke(lineWidth, cap, join, miterLimit, dash, phase));
	
    }
}

