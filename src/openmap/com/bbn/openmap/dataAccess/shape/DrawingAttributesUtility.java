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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/dataAccess/shape/DrawingAttributesUtility.java,v $
// $RCSfile: DrawingAttributesUtility.java,v $
// $Revision: 1.2 $
// $Date: 2004/01/26 18:18:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.dataAccess.shape;

import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.util.ColorFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.*;

/**
 * A class to help out with looking in a DBF file, and setting
 * OMGraphics with DrawingAttributes settings that may reside in the
 * DBF.  The DBF column header names should be the same as the
 * DrawingAttributes property strings.
 */
public class DrawingAttributesUtility implements ShapeConstants {

    protected DrawingAttributes da = new DrawingAttributes();

    protected int desColumn = -1;
    protected int lineColorColumn = -1;
    protected int fillColorColumn = -1;
    protected int selectColorColumn = -1;
    protected int lineWidthColumn = -1;
    protected int dashPatternColumn = -1;
    protected int dashPhaseColumn = -1;

    protected DbfTableModel model = null;

    protected DrawingAttributes defaultDA = DrawingAttributes.DEFAULT;

    protected DrawingAttributesUtility(DbfTableModel model) {

        this.model = model;

        Hashtable columnNames = new Hashtable();
        int numColumns = model.getColumnCount();
        for (int i = 0; i < numColumns; i++) {
            String colName = model.getColumnName(i);
            columnNames.put(colName, new Integer(i));
        }

        Integer value;

        value = (Integer)columnNames.get(SHAPE_DBF_DESCRIPTION);
        if (value != null) desColumn=value.intValue();
        
        value = (Integer)columnNames.get(SHAPE_DBF_LINECOLOR);
        if (value != null) lineColorColumn=value.intValue();
        value = (Integer)columnNames.get(SHAPE_DBF_FILLCOLOR);
        if (value != null) fillColorColumn=value.intValue();
        value = (Integer)columnNames.get(SHAPE_DBF_SELECTCOLOR);
        if (value != null) selectColorColumn=value.intValue();
        value = (Integer)columnNames.get(SHAPE_DBF_LINEWIDTH);
        if (value != null) lineWidthColumn=value.intValue();
        value = (Integer)columnNames.get(SHAPE_DBF_DASHPATTERN);
        if (value != null) dashPatternColumn=value.intValue();
        value = (Integer)columnNames.get(SHAPE_DBF_DASHPHASE);
        if (value != null) dashPhaseColumn=value.intValue();

        // OK, the column names should be known;
    }

    protected Color parseColor(String colorString, Color def) {
        Color ret;
        try {
            ret = ColorFactory.parseColor(colorString, true);
        } catch (NumberFormatException nfe) {
            ret = def;
        }
        return ret;
    }

    public void setDefaultAttributes(DrawingAttributes dda) {
        if (dda != null) {
            defaultDA = dda;
        } else {
            defaultDA = DrawingAttributes.DEFAULT;
        }
    }

    public DrawingAttributes getDefaultAttributes() {
        return defaultDA;
    } 

    protected void configureForRecord(OMGraphic graphic, ArrayList record) {
        if (desColumn != -1) {
            String ret = (String)record.get(desColumn);
            if (graphic.getAppObject() == null) {
                graphic.setAppObject(ret);
            }
        }

        getDefaultAttributes().setTo(da);

        if (lineColorColumn != -1) {
            da.setLinePaint(parseColor((String)record.get(lineColorColumn),
                                       (Color)defaultDA.getLinePaint()));
        }           

        if (fillColorColumn != -1) {
            da.setFillPaint(parseColor((String)record.get(fillColorColumn),
                                       (Color)defaultDA.getFillPaint()));
        }

        if (selectColorColumn != -1) {
            da.setSelectPaint(parseColor((String)record.get(selectColorColumn),
                                         (Color)defaultDA.getSelectPaint()));
        }

        int lineWidth = 1;
        float[] dashPattern = null;
        float dashPhase = 0f;

        if (lineWidthColumn != -1) {
            lineWidth = ((Double)record.get(lineWidthColumn)).intValue();
        }

        if (dashPatternColumn != -1) {
            String dp = (String)record.get(dashPatternColumn);
            if (dp.intern() == "") {
                dp = BasicStrokeEditor.NONE;
            }
            dashPattern = BasicStrokeEditor.stringToDashArray(dp);
        }

        if (dashPhaseColumn != -1) {
            dashPhase = ((Double)record.get(lineWidthColumn)).floatValue();
        }
        
        da.setStroke(new BasicStroke(lineWidth, 
                                     BasicStroke.CAP_BUTT, 
                                     BasicStroke.JOIN_MITER, 10.0f, 
                                     dashPattern, dashPhase));
        da.setTo(graphic);
    }

    protected void setDrawingAttributes(OMGraphic graphic, int index) {

        if (model != null) {
            ArrayList record = (ArrayList)model.getRecord(index);
            if (record != null) {
                configureForRecord(graphic, record);
            }
        }
    }

    /**
     * Iterate through the list, and look in the DbfTableModel for
     * DrawingAttributes parameters, setting the DrawingAttributes on
     * the list contents with anything found in the DBF file.
     * Otherwise, use the DrawingAttributes.DEFAULT settings if no
     * attribute parameters found.
     */
    public static void setDrawingAttributes(EsriGraphicList list, 
                                            DbfTableModel model) {
        setDrawingAttributes(list, model, DrawingAttributes.DEFAULT);
    }

    /**
     * Iterate through the list, and look in the DbfTableModel for
     * DrawingAttributes parameters, setting the DrawingAttributes on
     * the list contents with anything found in the DBF file.
     * Otherwise, use the default DrawingAttributes settings if no
     * attribute parameters found.
     */
    public static void setDrawingAttributes(EsriGraphicList list, 
                                            DbfTableModel model, 
                                            DrawingAttributes defaultDA) {
        // Set it up;
        DrawingAttributesUtility dau = new DrawingAttributesUtility(model);
        dau.setDefaultAttributes(defaultDA);

        Iterator graphics = list.iterator();
        int index = 0;
        while (graphics.hasNext()) {
            OMGraphic graphic = (OMGraphic)graphics.next();
            dau.setDrawingAttributes(graphic, index++);
        }
    }
}
