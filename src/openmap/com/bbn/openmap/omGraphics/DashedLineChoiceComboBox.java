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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/Attic/DashedLineChoiceComboBox.java,v $
// $RCSfile: DashedLineChoiceComboBox.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:12 $
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
 * This JComboBox provides a very limited choice of BasicStroke types.
 * If you want more line types, you can just extend the list here, and
 * make the additions in the other parts of the class as well.
 * @deprecated Use the BasicStrokeEditor
 */
public class DashedLineChoiceComboBox implements ActionListener {

    public final static LineChoice ONE = new LineChoice(new BasicStroke(3), "Whole");
    public final static LineChoice TWO = new LineChoice(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {3, 3}, 0.0f), "3/3");
    public final static LineChoice THREE = new LineChoice(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {6, 6}, 0.0f), "6/6");
    public final static LineChoice FOUR = new LineChoice(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10, 5}, 3f), "10/5/3");
    public final static LineChoice FIVE = new LineChoice(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10, 5, 2, 5}, 3f), "10/5/3");

    /**
     * The DrawingAttributes to modify with different choices.
     */
    protected DrawingAttributes drawingAttributes;

    public DashedLineChoiceComboBox() {}

    /**
     * Get the JComboBox that shows the different line strokes.
     *
     * @param da the DrawingAttribute to adjust when changes are made.
     */
    public JComboBox getComboBox(DrawingAttributes da) {
        drawingAttributes = da;

        JPanel lineDashPanel = new JPanel();

        Object[] lineDashes = { ONE.getIcon(), 
                                TWO.getIcon(), 
                                THREE.getIcon(),
                                FOUR.getIcon(),
                                FIVE.getIcon() };
        
        JComboBox lineDashList = new JComboBox(lineDashes);

        lineDashList.setToolTipText("Line Dash Pattern");
        
        int mWidth = 0;
        int mHeight = 0;
        
        for (int i = 0; i < lineDashes.length; i++) {
            ImageIcon icon = (ImageIcon)lineDashes[i];
            if (icon.getIconWidth() > mWidth) mWidth = icon.getIconWidth();
            if (icon.getIconHeight() > mHeight) mHeight = icon.getIconHeight();
        }
        
        lineDashList.setMaximumSize(new Dimension(mWidth + 2 /*border*/, mHeight));
        lineDashList.setSelectedIndex(tryToDetermineStroke(da.getStroke()));
        lineDashList.addActionListener(this);

        return lineDashList;
    }

    /**
     * A feeble attempt at trying to display the correct selection
     * when a combo box is first brought up.  It tries to match the
     * dash pattern with the current choices.
     */
    public static int tryToDetermineStroke(Stroke stroke) {
        if (stroke == null || !(stroke instanceof BasicStroke)) {
            return 0;
        }
        BasicStroke bs = (BasicStroke) stroke;

        float[] dashArray = bs.getDashArray();

        int currentChoice = 0;
        if (dashArray == null) {
            return currentChoice;
        }
        
        float[] ta = TWO.getStroke().getDashArray();
        if (ta.length == dashArray.length && 
            dashArray[0] == ta[0] && dashArray[1] == ta[1]) {
            return 1;
        } 

        ta = THREE.getStroke().getDashArray();
        if (ta.length == dashArray.length && 
            dashArray[0] == ta[0] && dashArray[1] == ta[1]) {
            return 2;
        } 

        ta = FOUR.getStroke().getDashArray();
        if (ta.length == dashArray.length && 
            dashArray[0] == ta[0] && dashArray[1] == ta[1]) {
            return 3;
        } 

        ta = FIVE.getStroke().getDashArray();
        if (ta.length == dashArray.length && 
            dashArray[0] == ta[0] && dashArray[1] == ta[1]) {
            return 4;
        } 

        return currentChoice;
    }
    
    /**
     *  When a new choice is made, create the new BasicStroke, and
     *  send the changes back to the DrawingAttributes object.
     */
    public void actionPerformed(ActionEvent e) {
        if (drawingAttributes == null) {
            return;
        }

        BasicStroke currentStroke = 
            (BasicStroke)drawingAttributes.getStroke();

        if (currentStroke == null) {
            drawingAttributes.setStroke(new BasicStroke(1));
            return;
        }

        float lineWidth = currentStroke.getLineWidth();
        int cap =  currentStroke.getEndCap();
        int join =  currentStroke.getLineJoin();
        float miterLimit =  currentStroke.getMiterLimit();

        float[] dash = ONE.getStroke().getDashArray();
        float phase = ONE.getStroke().getDashPhase();
        JComboBox jcb = (JComboBox) e.getSource();
        int newView = jcb.getSelectedIndex();
        switch(newView) {
        case 1:
            dash = TWO.getStroke().getDashArray();
            phase = TWO.getStroke().getDashPhase();
            break;
        case 2:
            dash = THREE.getStroke().getDashArray();
            phase = THREE.getStroke().getDashPhase();
            break;
        case 3:
            dash = FOUR.getStroke().getDashArray();
            phase = FOUR.getStroke().getDashPhase();
            break;
        case 4:
            dash = FIVE.getStroke().getDashArray();
            phase = FIVE.getStroke().getDashPhase();
            break;
        default:
            dash = ONE.getStroke().getDashArray();
            phase = ONE.getStroke().getDashPhase();
        }

        drawingAttributes.setStroke(new BasicStroke(lineWidth, cap, join, miterLimit, dash, phase));
    }
}

