/* **********************************************************************
 *
 *  ROLANDS & ASSOCIATES Corporation
 *  500 Sloat Avenue
 *  Monterey, CA 93940
 *  (831) 373-2025
 *
 *  Copyright (C) 2002, 2003 ROLANDS & ASSOCIATES Corporation. All rights reserved.
 *  Openmap is a trademark of BBN Technologies, A Verizon Company
 *
 *
 * **********************************************************************
 *
 * $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/ScaleDisplayLayer.java,v $
 * $Revision: 1.2 $
 * $Date: 2003/04/23 17:08:00 $
 * $Author: dietrick $
 *
 * **********************************************************************
 */

package com.bbn.openmap.layer;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.proj.GreatCircle;
import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.Projection;
import java.util.Properties;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Layer objects are components which can be added to the MapBean to
 * make a map.
 * <p>
 * Layers implement the ProjectionListener interface to listen for
 * ProjectionEvents.  When the projection changes, they may need to
 * refetch, regenerate their graphics, and then repaint themselves
 * into the new view.
 *<p>
 * ### Layer used by the overview handler<br>
 * scaleLayer.class=com.rolands.jtlsweb.map.layer.ScaleDisplayLayer<br>
 * scaleLayer.prettyName=Scale<br>
 * scaleLayer.lineColor=ff777777<br>
 * scaleLayer.textColor=ff000000<br>
 * scaleLayer.unitOfMeasure=nm<br>
 * scaleLayer.locationXoffset=-10<br>
 * scaleLayer.locationYoffset=-20<br>
 * scaleLayer.width=150<br>
 * scaleLayer.height=10<br>
 *<br>
 * unitOfMeasure - any com.bbn.openmap.proj.Length instance returned by Length.get(string).<br>
 * locationXoffset - offset in pixels from left/right, positive from left edge, negative from right edge<br>
 * locationYoffset - offset in pixels from top/bottom, positive from top edge, negative from bottom edge<br>
 * width - width of scale indidator bar in pixels<br>
 * height - height of scale indidator bar in pixels<br>
 * <br>
 */
public class ScaleDisplayLayer extends OMGraphicHandlerLayer  {
    
    public ScaleDisplayLayer() {
        super();
	setProjectionChangePolicy(new com.bbn.openmap.layer.policy.ListResetPCPolicy(this));
    }
    
    // Color variables for different line types
    protected java.awt.Color lineColor = null;
    protected java.awt.Color textColor = null;
    
    // Default colors to use, if not specified in the properties.
    protected String defaultLineColorString = "FFFFFF";
    protected String defaultTextColorString = "FFFFFF";
    protected String defaultUnitOfMeasureString = "km";
    protected int defaultLocationXoffset = -10;
    protected int defaultLocationYoffset = -10;
    protected int defaultWidth = 150;
    protected int defaultHeight = 10;
    
    // property text values
    public static final String LineColorProperty = "lineColor";
    public static final String TextColorProperty = "textColor";
    public static final String UnitOfMeasureProperty = "unitOfMeasure";
    public static final String LocationXOffsetProperty = "locationXoffset";
    public static final String LocationYOffsetProperty = "locationYoffset";
    public static final String WidthProperty = "width";
    public static final String HeightProperty = "height";
    
    protected String unitOfMeasure = null;
    protected Length uom = Length.get(defaultUnitOfMeasureString);
    protected String uomAbbr = uom.getAbbr();
    protected int locationXoffset = defaultLocationXoffset;
    protected int locationYoffset = defaultLocationYoffset;
    protected int width = defaultWidth;
    protected int height = defaultHeight;
    
    
    /**
     * Sets the properties for the <code>Layer</code>.  This allows
     * <code>Layer</code>s to get a richer set of parameters than the
     * <code>setArgs</code> method.
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);
        prefix = com.bbn.openmap.util.PropUtils.getScopedPropertyPrefix(prefix);
        
        lineColor = LayerUtils.parseColorFromProperties(
	    properties, prefix + LineColorProperty,
	    defaultLineColorString);
        
        textColor = LayerUtils.parseColorFromProperties(
	    properties, prefix + TextColorProperty,
	    defaultTextColorString);
        
        String unitOfMeasure =
	    properties.getProperty(prefix + UnitOfMeasureProperty);
        setUnitOfMeasure(unitOfMeasure);
        
        locationXoffset = LayerUtils.intFromProperties(
	    properties, prefix + LocationXOffsetProperty,
	    defaultLocationXoffset);
        
        locationYoffset = LayerUtils.intFromProperties(
	    properties, prefix + LocationYOffsetProperty,
	    defaultLocationYoffset);
        
        width = LayerUtils.intFromProperties(
	    properties, prefix + WidthProperty,
	    defaultWidth);
        
        height = LayerUtils.intFromProperties(
	    properties, prefix + HeightProperty,
	    defaultHeight);
    }
    
    public OMGraphicList prepare() {
        int w, h, left_x=0, right_x=0, lower_y=0, upper_y=0;
	Projection projection = getProjection();
	OMGraphicList graphics = new OMGraphicList();

        w = projection.getWidth();
        h = projection.getHeight();
        if (locationXoffset < 0) {
           left_x  = w + locationXoffset - width;
           right_x = w + locationXoffset;
        } else if (locationXoffset >= 0) {          
           left_x  = locationXoffset;
           right_x = locationXoffset + width;
        }
        if (locationYoffset < 0) {
           upper_y = h + locationYoffset - height;
           lower_y = h + locationYoffset;
        } else if (locationYoffset >= 0) {          
           upper_y = locationYoffset;
           lower_y = locationYoffset + height;
        }
        
        graphics.clear();
        
        OMLine line = new OMLine(left_x, lower_y, right_x, lower_y);
        line.setLinePaint(lineColor);
        graphics.add(line);
        
        line = new  OMLine(left_x, lower_y, left_x, upper_y);
        line.setLinePaint(lineColor);
        graphics.add(line);
        
        line = new  OMLine(right_x, lower_y, right_x, upper_y);
        line.setLinePaint(lineColor);
        graphics.add(line);
        
	LatLonPoint loc1 = projection.inverse(left_x, lower_y);
	LatLonPoint loc2 = projection.inverse(right_x, lower_y);
        
        float dist = GreatCircle.spherical_distance(loc1.radlat_, loc1.radlon_, 
						    loc2.radlat_, loc2.radlon_);
        dist = uom.fromRadians(dist);
        
        if (dist > 1) dist = (int) dist;
        String outtext = dist+" "+uomAbbr;
        
        OMText text = new OMText((left_x+right_x)/2, lower_y-3, ""+outtext, OMText.JUSTIFY_CENTER);
        text.setLinePaint(textColor);
        graphics.add(text);
        graphics.generate(projection);

	return graphics;
    }

    /** Getter for property unitOfMeasure.
     * @return Value of property unitOfMeasure.
     */
    public String getUnitOfMeasure() {
        return this.unitOfMeasure;
    }
    
    /** Setter for property unitOfMeasure.
     * @param unitOfMeasure New value of property unitOfMeasure.
     *
     * @throws PropertyVetoException
     */
    public void setUnitOfMeasure(String unitOfMeasure) {
        if (unitOfMeasure == null) unitOfMeasure = Length.KM.toString();
        this.unitOfMeasure = unitOfMeasure;
        
        //There is a bug in the Length.get() method that will not return
        //the correct (or any value) for a requested uom.
        //This does not work:
        //uom = com.bbn.openmap.proj.Length.get(unitOfMeasure);
        
        //Therefore, The following code correctly obtains the proper Length object.
        
        Length[] choices = Length.getAvailable();
        uom = null;
        for (int i = 0; i < choices.length; i++) {
            if (unitOfMeasure.equalsIgnoreCase(choices[i].toString()) ||
            unitOfMeasure.equalsIgnoreCase(choices[i].getAbbr())) {
                uom = choices[i];
                break;
            }
        }
        
        // of no uom is found assign Kilometers as the default.
        if (uom == null) uom = Length.KM;
        
        uomAbbr = uom.getAbbr();
        
    }
    
    javax.swing.Box palette;
    JRadioButton meterRadioButton;
    JRadioButton kmRadioButton;
    JRadioButton nmRadioButton;
    JRadioButton mileRadioButton;
    javax.swing.ButtonGroup uomButtonGroup;
    
    private JPanel jPanel3;
    private JPanel jPanel2;
    private JPanel jPanel1;
    
    /** Creates the interface palette. */
    public java.awt.Component getGUI() {
        
        if (palette == null){
            if (com.bbn.openmap.util.Debug.debugging("graticule"))
                com.bbn.openmap.util.Debug.output("GraticuleLayer: creating Graticule Palette.");
            
            palette = javax.swing.Box.createVerticalBox();
            uomButtonGroup = new javax.swing.ButtonGroup();
            jPanel1 = new JPanel();
            jPanel2 = new JPanel();
            jPanel3 = new JPanel();
            kmRadioButton = new JRadioButton();
            meterRadioButton = new JRadioButton();
            nmRadioButton = new JRadioButton();
            mileRadioButton = new JRadioButton();
            
            jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel2.setBorder(new javax.swing.border.TitledBorder("Unit Of Measure"));
            kmRadioButton.setText("KM");
            kmRadioButton.setToolTipText("Kilometers");
            uomButtonGroup.add(kmRadioButton);
            jPanel3.add(kmRadioButton);
            
            meterRadioButton.setText("M");
            meterRadioButton.setToolTipText("Meters");
            uomButtonGroup.add(meterRadioButton);
            jPanel3.add(meterRadioButton);
            
            nmRadioButton.setText("NM");
            nmRadioButton.setToolTipText("Nautical Miles");
            uomButtonGroup.add(nmRadioButton);
            jPanel3.add(nmRadioButton);
            
            mileRadioButton.setText("Mile");
            mileRadioButton.setToolTipText("Statute Miles");
            uomButtonGroup.add(mileRadioButton);
            jPanel3.add(mileRadioButton);
            
            jPanel2.add(jPanel3);
            
            jPanel1.add(jPanel2);
            
            palette.add(jPanel1);
            
            
            java.awt.event.ActionListener al = new java.awt.event.ActionListener(){
                public void actionPerformed(java.awt.event.ActionEvent e){
                    String ac = e.getActionCommand();
                    
                    if (ac.equalsIgnoreCase(UnitOfMeasureProperty)){
                        JRadioButton jrb = (JRadioButton)e.getSource();
                        setUnitOfMeasure(jrb.getText());
                        doPrepare();
                    } else {
                        com.bbn.openmap.util.Debug.error("Unknown action command \"" + ac +
                        "\" in GraticuleLayer.actionPerformed().");
                    }
                }
            };
            

            kmRadioButton.addActionListener(al);
            kmRadioButton.setActionCommand(UnitOfMeasureProperty);
            meterRadioButton.addActionListener(al);
            meterRadioButton.setActionCommand(UnitOfMeasureProperty);
            nmRadioButton.addActionListener(al);
            nmRadioButton.setActionCommand(UnitOfMeasureProperty);
            mileRadioButton.addActionListener(al);
            mileRadioButton.setActionCommand(UnitOfMeasureProperty);
            
         }
        if (unitOfMeasure.equalsIgnoreCase("km")) {
            kmRadioButton.setSelected(true);
        } else if (unitOfMeasure.equalsIgnoreCase("m")) {
            meterRadioButton.setSelected(true);
        } else if (unitOfMeasure.equalsIgnoreCase("nm")) {
            nmRadioButton.setSelected(true);
        } else if (unitOfMeasure.equalsIgnoreCase("mile")) {
            mileRadioButton.setSelected(true);
        }
        return palette;
    }
    
    
}
