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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/DemoLayer.java,v $
// $RCSfile: DemoLayer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.bbn.openmap.omGraphics.awt.TextShapeDecoration;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.EditableOMPoly;
import com.bbn.openmap.omGraphics.GraphicAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMBitmap;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMDecoratedSpline;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.omGraphics.OMSpline;
import com.bbn.openmap.omGraphics.labeled.LabeledOMSpline;
import com.bbn.openmap.omGraphics.meteo.OMColdSurfaceFront;
import com.bbn.openmap.omGraphics.meteo.OMHotSurfaceFront;
import com.bbn.openmap.omGraphics.meteo.OMOcclusion;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.tools.drawing.DrawingTool;
import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
import com.bbn.openmap.tools.drawing.OMDrawingTool;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;

/**
 * This layer demonstrates interactive capabilities of OpenMap.
 * Instantiating this layer should show an icon loaded using HTTP
 * Protocol, which represents Boston, MA in USA.  Above Boston it
 * should show a square that would change color when mouse is moved
 * over it in 'Gesture' mode.  Also clicking once brings up a message
 * box and more than once brings up browser.  <P>
 *
 * The DemoLayer has also been modified to demonstrate the first uses
 * of the OMDrawingTool.  The Palette has buttons that can be used to
 * start the tool in several different ways.
 * 
 * @see com.bbn.openmap.layer.DemoLayer
 * 
 * Just added some decorated splines to test them. EL
 */
public class DemoLayer extends OMGraphicHandlerLayer
	implements MapMouseListener, DrawingToolRequestor {

    public void init() {

	OMGraphicList omList = (OMGraphicList) getList();

	// 	Location loc = new URLRasterLocation(42.3583f,-71.06f,"Boston,Massachusetts,USA","http://javamap.bbn.com:4711/appletimages/city.gif");
	// 	//loc.setLocationColor(Color.blue);
	// 	loc.setShowLocation(true);
	// 	loc.setShowName(true);
	// 	//loc.setDetails("Details");
	// 	omList.add(loc);

	int bytearrsize = (16 * 16) / 8;
	byte[] bytearr = new byte[bytearrsize];

	for (int i = 0; i < bytearr.length; i++) {
	    bytearr[i] = (byte) 0xffffffff;
	}

	OMBitmap omb = new OMBitmap(45.3583f, -71.06f, 16, 16, bytearr);
	omb.setLinePaint(Color.red);
	omb.setFillPaint(null);
	omb.setSelectPaint(Color.blue);
	omb.setRotationAngle(Math.PI / 2);
	omList.add(omb);

	OMPoint point = new OMPoint(42f, -72f, 14);
	point.setFillPaint(Color.green);
	point.setOval(true);
	omList.add(point);

	OMCircle circle = new OMCircle(40f, -70f, 50, 200);
	circle.setRotationAngle(com.bbn.openmap.MoreMath.HALF_PI / 2f);
	omList.add(circle);

	int[] llPointsx = new int[5];
	int[] llPointsy = new int[5];
	llPointsy[0] = 10;
	llPointsx[0] = 170;
	llPointsy[1] = 42;
	llPointsx[1] = 273;
	llPointsy[2] = 38;
	llPointsx[2] = 374;
	llPointsy[3] = 78;
	llPointsx[3] = 468;
	llPointsy[4] = 84;
	llPointsx[4] = 369;

	LabeledOMSpline spline =
	    new LabeledOMSpline(
		40f,
		-72,
		llPointsx,
		llPointsy,
		OMPoly.COORDMODE_ORIGIN);
	spline.setText("Testing");
	//	spline.setLocateAtCenter(true);
	spline.setIndex(2);
	omList.add(spline);

	OMSpline spline2 = new OMSpline(llPointsx, llPointsy);
	omList.add(spline2);

	float[] llPoints =
	    { 55.0f, -10.0f, 50.0f, -5.0f, 45.0f, -7.0f, 43.0f, -12.0f };
	OMColdSurfaceFront cf =
	    new OMColdSurfaceFront(
		llPoints,
		OMSpline.DECIMAL_DEGREES,
		OMSpline.LINETYPE_STRAIGHT);
	omList.add(cf);
	float[] llPoints2 =
	    { 55.0f, -12.0f, 50.0f, -7.0f, 45.0f, -9.0f, 43.0f, -14.0f };
	OMHotSurfaceFront hf =
	    new OMHotSurfaceFront(
		llPoints2,
		OMSpline.DECIMAL_DEGREES,
		OMSpline.LINETYPE_STRAIGHT);
	omList.add(hf);
	float[] llPoints3 =
	    { 55.0f, -14.0f, 50.0f, -9.0f, 45.0f, -11.0f, 43.0f, -16.0f };
	OMOcclusion oc =
	    new OMOcclusion(
		llPoints3,
		OMSpline.DECIMAL_DEGREES,
		OMSpline.LINETYPE_STRAIGHT);
	omList.add(oc);

	float[] llPoints4 =
	    { 55.0f, -16.0f, 50.0f, -11.0f, 45.0f, -13.0f, 43.0f, -18.0f };
	OMSpline spline3 =
	    new OMDecoratedSpline(
		llPoints4,
		OMSpline.DECIMAL_DEGREES,
		OMSpline.LINETYPE_STRAIGHT) {
		protected void initDecorations() {

		    getDecorator().addDecoration(
			new TextShapeDecoration(
			    " This one has a text ",
			    new Font("arial", Font.PLAIN, 10),
			    TextShapeDecoration.LEFT_TO_RIGHT
			    + TextShapeDecoration.FOLLOW_POLY,
			    TextShapeDecoration.CENTER));
		}
	    };
	omList.add(spline3);

	OMLine line =
	    new OMLine(40f, -75f, 42f, -70f, OMGraphic.LINETYPE_STRAIGHT);
	line.addArrowHead(true);
	line.setStroke(new BasicStroke(2));
	omList.add(line);

	for (int i = 0; i < 100; i++) {
	    point =
		new OMPoint(
		    (float) (Math.random() * 89f),
		    (float) (Math.random() * -179f),
		    3);
	    point.setSelectPaint(Color.yellow);
	    omList.add(point);
	}
    }

    /**
     * Overriding what happens to the internal OMGraphicList when the
     * projection changes.  For this layer, we don't want anything to
     * happen.
     */
    protected void resetListForProjectionChange() {}

    public void setProperties(String prefix, Properties props) {
	super.setProperties(prefix, props);
	init();
	setAddToBeanContext(true);
    }

    /**
     * Overriding the OMGraphicHandlerMethod, creating a list if it's null.
     */
    public OMGraphicList getList() {
	OMGraphicList list = super.getList();
	if (list == null) {
	    list = new OMGraphicList();
	    super.setList(list);
	}
	return list;
    }

    protected final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer =
	this;

    protected final static String internalKey = "ik";
    protected final static String externalKey = "ek";
    protected GraphicAttributes filterGA = null;

    protected GraphicAttributes getFilterGA() {
	if (filterGA == null) {
	    filterGA = new GraphicAttributes();
	    filterGA.setLinePaint(Color.red);
	    filterGA.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	    filterGA.setLineType(OMGraphic.LINETYPE_GREATCIRCLE);
	    BasicStroke filterStroke =
		new BasicStroke(
		    1f,
		    BasicStroke.CAP_SQUARE,
		    BasicStroke.JOIN_MITER,
		    10f,
		    new float[] { 3, 3 },
		    0f);
	    filterGA.setStroke(filterStroke);
	}
	return filterGA;
    }

    public java.awt.Component getGUI() {

	JPanel box = PaletteHelper.createVerticalPanel("Filtering Demo");
	box.setLayout(new java.awt.GridLayout(0, 1));

	//  	JButton button = new JButton("Add and Edit Offset Line");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    if (dt != null) {

	//  			OMLine line = new OMLine(42f, -72f, -50, -70, 200, 200);
	//  			line.setStroke(new java.awt.BasicStroke(5));
	//  			line.setLinePaint(java.awt.Color.red);
	//  			line.setFillPaint(java.awt.Color.green);

	//  			line  = (OMLine) getDrawingTool().edit(line, layer);
	//  			if (line != null) {
	//  			    getList().add(line);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMLine");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Add and Edit XY Line");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    if (dt != null) {

	//  			OMLine line = new OMLine(200, 200, 420, 520);
	//  			line.setLinePaint(java.awt.Color.blue);
	//  			line.setFillPaint(java.awt.Color.green);

	//  			line  = (OMLine) getDrawingTool().edit(line, layer);
	//  			if (line != null) {
	//  			    getList().add(line);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMLine");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Add and Edit LatLon Line, no GUI");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    if (dt != null) {
	//    			OMLine line = new OMLine(30f, -60f, 42f, -72f, 
	//  						 OMGraphic.LINETYPE_GREATCIRCLE);
	//  			line.setStroke(new java.awt.BasicStroke(5));
	//  			line.setLinePaint(java.awt.Color.red);
	//  			line.setFillPaint(java.awt.Color.green);

	//  			line  = (OMLine) getDrawingTool().edit(line, layer, false);
	//  			if (line != null) {
	//  			    getList().add(line);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMLine");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create XY Line");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    if (dt != null) {
	//    			OMLine line  = (OMLine) getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine", layer);
	//  			if (line != null) {
	//  			    getList().add(line);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMLine");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create Offset Line");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
	//  		    if (dt != null) {
	//    			OMLine line  = (OMLine) getDrawingTool().create("com.bbn.openmap.omGraphics.OMLine", ga, layer);
	//  			if (line != null) {
	//  			    getList().add(line);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMLine");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create Lat/Lon Circle");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	//  		    if (dt != null) {
	//    			OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
	//  			if (circle != null) {
	//  			    getList().add(circle);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMCircle");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create XY Circle");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_XY);
	//  		    if (dt != null) {
	//    			OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
	//  			if (circle != null) {
	//  			    getList().add(circle);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMCircle");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create Offset Circle");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
	//  		    ga.setFillPaint(Color.red);
	//  		    if (dt != null) {
	//    			OMCircle circle  = (OMCircle) getDrawingTool().create("com.bbn.openmap.omGraphics.OMCircle", ga, layer);
	//  			if (circle != null) {
	//  			    getList().add(circle);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMCircle");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	JButton button = new JButton("Create Containing Rectangle Filter");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    DrawingTool dt = getDrawingTool();
		    if (dt != null) {
			GraphicAttributes fga = getFilterGA();
			fga.setFillPaint(new OMColor(0x0c0a0a0a));

			OMRect rect =
			    (OMRect) getDrawingTool().create(
				"com.bbn.openmap.omGraphics.OMRect",
				fga,
				layer,
				false);
			if (rect != null) {
			    rect.setAppObject(internalKey);
			}
			else {
			    Debug.error(
				"DemoLayer: Drawing tool can't create OMRect");
			}
		    }
		    else {
			Debug.output("DemoLayer can't find a drawing tool");
		    }
		}
	    });
	box.add(button);

	button = new JButton("Create Containing Polygon Filter");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    DrawingTool dt = getDrawingTool();
		    if (dt != null) {
			GraphicAttributes fga = getFilterGA();
			fga.setFillPaint(OMColor.clear);

			EditableOMPoly eomp = new EditableOMPoly(fga);
			eomp.setEnclosed(true);
			eomp.setShowGUI(false);

			dt.setBehaviorMask(OMDrawingTool.DEFAULT_BEHAVIOR_MASK);
			OMPoly poly = (OMPoly) getDrawingTool().edit(eomp, layer);

			if (poly != null) {
			    poly.setIsPolygon(true);
			    poly.setAppObject(internalKey);
			}
			else {
			    Debug.error(
				"DemoLayer: Drawing tool can't create OMPoly");
			}
		    }
		    else {
			Debug.output("DemoLayer can't find a drawing tool");
		    }
		}
	    });
	box.add(button);

	button = new JButton("Create Excluding Rectangle Filter");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    DrawingTool dt = getDrawingTool();
		    if (dt != null) {
			GraphicAttributes fga = getFilterGA();
			fga.setFillPaint(OMColor.clear);

			OMRect rect =
			    (OMRect) getDrawingTool().create(
				"com.bbn.openmap.omGraphics.OMRect",
				fga,
				layer,
				false);
			if (rect != null) {
			    rect.setAppObject(externalKey);
			}
			else {
			    Debug.error(
				"DemoLayer: Drawing tool can't create OMRect");
			}
		    }
		    else {
			Debug.output("DemoLayer can't find a drawing tool");
		    }
		}
	    });
	box.add(button);

	button = new JButton("Reset filter");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    resetFiltering();
		    repaint();
		}
	    });
	box.add(button);

	//  	button = new JButton("Create XY Rect");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_XY);
	//  		    if (dt != null) {
	//    			OMRect rect  = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", ga, layer);
	//  			if (rect != null) {
	//  			    getList().add(rect);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMRect");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create Offset Rect");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
	//  		    ga.setFillPaint(Color.red);
	//  		    if (dt != null) {
	//    			OMRect rect  = (OMRect) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRect", ga, layer);
	//  			if (rect != null) {
	//  			    getList().add(rect);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMRect");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create RangeRings");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setLinePaint(Color.yellow);
	//  		    if (dt != null) {
	//    			OMRangeRings rr  = (OMRangeRings) getDrawingTool().create("com.bbn.openmap.omGraphics.OMRangeRings", ga, layer);
	//  			if (rr != null) {
	//  //   			    rr.setInterval(25, Length.MILE);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMRangeRings");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create XY Poly");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_XY);
	//  		    ga.setLinePaint(Color.red);
	//  		    ga.setFillPaint(Color.red);
	//  		    if (dt != null) {
	//    			OMPoly point  = (OMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly", ga, layer);
	//  			if (point != null) {
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMPoly");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create LatLon Labeled Poly");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_LATLON);
	//  		    ga.setLinePaint(Color.green);
	//  		    ga.setFillPaint(Color.green);
	//  		    if (dt != null) {

	//    			LabeledOMPoly point  = (LabeledOMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.labeled.LabeledOMPoly", ga, layer);

	//  			if (point != null) {
	//  //  			    point.setOval(true);
	//  //  			    point.setRadius(8);
	//  			    point.setText("Active Testing");
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMPoly");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	//  	button = new JButton("Create LatLon Offset Poly");
	//  	button.addActionListener(new ActionListener() {
	//  		public void actionPerformed(ActionEvent event) {
	//  		    DrawingTool dt = getDrawingTool();
	//  		    GraphicAttributes ga = new GraphicAttributes();
	//  		    ga.setRenderType(OMGraphic.RENDERTYPE_OFFSET);
	//  		    ga.setLinePaint(Color.green);
	//  		    ga.setFillPaint(Color.green);
	//  		    if (dt != null) {
	//    			OMPoly point  = (OMPoly) getDrawingTool().create("com.bbn.openmap.omGraphics.OMPoly", ga, layer);
	//  			if (point != null) {
	//  //  			    rr.setInterval(25, Length.MILE);
	//  			} else {
	//  			    Debug.error("DemoLayer: Drawing tool can't create OMPoly");
	//  			} 
	//  		    } else {
	//  			Debug.output("DemoLayer can't find a drawing tool");
	//  		    }
	//  		}
	//  	    });
	//  	box.add(button);

	return box;
    }

    protected DrawingTool drawingTool;

    public DrawingTool getDrawingTool() {
	return drawingTool;
    }

    public void setDrawingTool(DrawingTool dt) {
	drawingTool = dt;
    }

    public void drawingComplete(OMGraphic omg, OMAction action) {
	Debug.message("demo", "DemoLayer: DrawingTool complete");

	Object obj = omg.getAppObject();

	if (obj != null
	    && (obj == internalKey || obj == externalKey)
	    && !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {
	    java.awt.Shape filterShape = omg.getShape();

	    filter(filterShape, (omg.getAppObject() == internalKey));
	}
	else {
	    getList().doAction(omg, action);
	}

	repaint();
    }

    /**
     * Called when a component that is needed, and not available with
     * an appropriate interator from the BeanContext.  This lets this
     * object hook up with what it needs.  For Layers, this method
     * doesn't do anything by default.  If you need your layer to get
     * ahold of another object, then you can use the Iterator to go
     * through the objects to look for the one you need.
     */
    public void findAndInit(Object someObj) {
	if (someObj instanceof DrawingTool) {
	    Debug.message("demo", "DemoLayer: found a drawing tool");
	    setDrawingTool((DrawingTool) someObj);
	}
    }

    /**
     * BeanContextMembershipListener method.  Called when a new object
     * is removed from the BeanContext of this object.  For the Layer,
     * this method doesn't do anything.  If your layer does something
     * with the childrenAdded method, or findAndInit, you should take
     * steps in this method to unhook the layer from the object used
     * in those methods.
     */
    public void findAndUndo(Object someObj) {
	if (someObj instanceof DrawingTool) {
	    if (getDrawingTool() == (DrawingTool) someObj) {
		setDrawingTool(null);
	    }
	}
    }

    /**
     * Note: A layer interested in receiving amouse events should
     * implement this function .  Otherwise, return the default, which
     * is null.
     */
    public synchronized MapMouseListener getMapMouseListener() {
	return this;
    }

    /**
     * Return a list of the modes that are interesting to the
     * MapMouseListener.  You MUST override this with the modes you're
     * interested in.
     */
    public String[] getMouseModeServiceList() {
	String[] services = { "Gestures", "Navigation" };
	// what are other possibilities in OpenMap
	return services;
    }

    ////////////////////////
    // Mouse Listener events
    ////////////////////////

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mousePressed(MouseEvent e) {
	return false;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseReleased(MouseEvent e) {
	return false;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseClicked(MouseEvent e) {
	OMGraphic omgr =
	    ((OMGraphicList) getList()).findClosest(e.getX(), e.getY(), 4);
	if (omgr != null) {
	    DrawingTool dt = getDrawingTool();
	    if (dt != null) {
		if (dt.edit(omgr, layer) == null) {
		    fireRequestInfoLine("Can't figure out how to modify this object.");
		}
	    }
	}
	else {
	    return false;
	}

	return true;
    }

    /**
     * Invoked when the mouse enters a component.
     * @param e MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
	return;
    }

    /**
     * Invoked when the mouse exits a component.
     * @param e MouseEvent
     */
    public void mouseExited(MouseEvent e) {
	return;
    }

    ///////////////////////////////
    // Mouse Motion Listener events
    ///////////////////////////////

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  The listener will receive these events if it
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseDragged(MouseEvent e) {
	return false;
    }

    OMGraphic lastSelected = null;

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons down).
     * @param e MouseEvent
     * @return false
     */
    public boolean mouseMoved(MouseEvent e) {

	OMGraphic omgr =
	    ((OMGraphicList) getList()).findClosest(e.getX(), e.getY(), 4.0f);
	boolean ret = false;

	if (omgr != null) {
	    fireRequestInfoLine("Click to edit graphic.");
	    fireRequestToolTip(e, "Demo Layer Object");
	    ret = true;
	}
	else {
	    fireRequestInfoLine("");
	    fireHideToolTip(e);
	    if (lastSelected != null) {
		lastSelected.deselect();
		lastSelected.generate(getProjection());
		lastSelected = null;
		repaint();
		//System.out.println("MouseMove Kicking repaint");
	    }
	}

	if (omgr instanceof OMBitmap) {
	    omgr.select();
	    omgr.generate(getProjection());
	    lastSelected = omgr;
	    //System.out.println("MouseMove Kicking repaint");
	    repaint();
	}

	return ret;
    }

    /**
     * Handle a mouse cursor moving without the button being pressed.
     * Another layer has consumed the event.
     */
    public void mouseMoved() {}
}
