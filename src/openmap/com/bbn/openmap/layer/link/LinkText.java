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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkText.java,v $
// $RCSfile: LinkText.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.link;

import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.LatLonPoint;

import java.awt.BasicStroke;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 */
public class LinkText implements LinkGraphicConstants, LinkPropertiesConstants {

    public static String DEFAULT_FONT = "-*-SansSerif-normal-o-normal--12-*-*-*-*-*-*";

   /**
     * Creates a text object, with Lat/Lon placement.
     * @param latPoint latitude of the string, in decimal degrees.
     * @param lonPoint longitude of the string, in decimal degrees.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string.
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(float latPoint, float lonPoint,
			     String stuff, String font, int just, 
			     LinkProperties properties, 
			     DataOutputStream dos)
	throws IOException {
	
	dos.write(Link.TEXT_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_TEXT);
	dos.writeInt(RENDERTYPE_LATLON);
	dos.writeFloat(latPoint);
	dos.writeFloat(lonPoint);
	dos.writeInt(just);

	properties.setProperty(LPC_LINKTEXTSTRING, stuff);
	properties.setProperty(LPC_LINKTEXTFONT, font);
	properties.write(dos);
    }

    /**
     * Creates a text object, with XY placement, and default SansSerif
     * font. 
     * @param x1 horizontal window pixel location of the string.
     * @param y1 vertical window pixel location of the string.
     * @param stuff the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(int x1, int y1, String stuff, String font, int just, 
			     LinkProperties properties, DataOutputStream dos)
	throws IOException {

	dos.write(Link.TEXT_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_TEXT);
	dos.writeInt(RENDERTYPE_XY);
	dos.writeInt(x1);
	dos.writeInt(y1);
	dos.writeInt(just);

	properties.setProperty(LPC_LINKTEXTSTRING, stuff);
	properties.setProperty(LPC_LINKTEXTFONT, font);
	properties.write(dos);
    }

    /**
     * Rendertype is RENDERTYPE_OFFSET.
     *
     * @param latPoint latitude of center of text/ellipse.
     * @param lonPoint longitude of center of text/ellipse.
     * @param offset_x1 # pixels to the right the center will be moved
     * from lonPoint.
     * @param offset_y1 # pixels down that the center will be moved
     * from latPoint.
     * @param aString the string to be displayed.
     * @param font the Font description for the string.
     * @param just the justification of the string.
     * @param properties attributes for the graphic.
     * @param dos DataOutputStream
     * @throws IOException.
     */
    public static void write(float latPoint, float lonPoint,
			     int offset_x1, int offset_y1, 
			     String stuff, String font, int just, 
			     LinkProperties properties, 
			     DataOutputStream dos)
	throws IOException {

	dos.write(Link.TEXT_HEADER.getBytes());
	dos.writeInt(GRAPHICTYPE_TEXT);
	dos.writeInt(RENDERTYPE_OFFSET);
	dos.writeFloat(latPoint);
	dos.writeFloat(lonPoint);
	dos.writeInt(offset_x1);
	dos.writeInt(offset_y1);

	properties.setProperty(LPC_LINKTEXTSTRING, stuff);
	properties.setProperty(LPC_LINKTEXTFONT, font);
	properties.write(dos);
    }

    /**
     * Write a text to the link.
     */
    public static void write(OMText text, Link link, LinkProperties props) 
	throws IOException {

	switch (text.getRenderType()) {
	case OMText.RENDERTYPE_LATLON:
	    write(text.getLat(), text.getLon(), text.getData(), 
		  OMText.fontToXFont(text.getFont()), text.getJustify(),
		  props, link.dos);
	    break;
	case OMText.RENDERTYPE_XY:
	    write(text.getX(), text.getY(), text.getData(), 
		  OMText.fontToXFont(text.getFont()), text.getJustify(),
		  props, link.dos);
	    break;
	case OMText.RENDERTYPE_OFFSET:
	    write(text.getLat(), text.getLon(), text.getX(), text.getY(),
		  text.getData(), OMText.fontToXFont(text.getFont()), 
		  text.getJustify(), props, link.dos);
	    break;
	default:
	    Debug.error("LinkText.write: text rendertype unknown.");
	}
    }

    public static OMText read(DataInputStream dis)
	throws IOException {

	OMText text = null;
	float lat = 0;
	float lon = 0;
	int x = 0;
	int y = 0; 
	int i = 0;
	int just = 0;
	int length;
	String string, font;

	int renderType = dis.readInt();
	
	switch (renderType){
	case RENDERTYPE_OFFSET:
	    lat = dis.readFloat();
	    lon = dis.readFloat();	    
	case RENDERTYPE_XY:
	    x = dis.readInt();
	    y = dis.readInt();	   
	    break;
	case RENDERTYPE_LATLON:
	default:
	    lat = dis.readFloat();
	    lon = dis.readFloat();	    
	}
	
	just = dis.readInt();
	
	LinkProperties properties = new LinkProperties(dis);

	string = properties.getProperty(LPC_LINKTEXTSTRING);
	font = properties.getProperty(LPC_LINKTEXTFONT);
	
	if (string == null) string = "";
	if (font == null) font = DEFAULT_FONT;
	
	switch (renderType){
	case RENDERTYPE_OFFSET:
	    text = new OMText(lat, lon, x, y, string, OMText.rebuildFont(font), just);
	    break;
	case RENDERTYPE_XY:
	    text = new OMText(x, y, string, OMText.rebuildFont(font), just);
	    break;
	case RENDERTYPE_LATLON:
	default:
	    text = new OMText(lat, lon, string, OMText.rebuildFont(font), just);
	}
	
	if (text != null){
	    text.setLinePaint(ColorFactory.parseColorFromProperties(
		properties, LPC_LINECOLOR,
		BLACK_COLOR_STRING, true));
	    text.setFillPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_FILLCOLOR,
		CLEAR_COLOR_STRING, true));
	    text.setSelectPaint(ColorFactory.parseColorFromProperties(
		properties, LPC_HIGHLIGHTCOLOR,
		BLACK_COLOR_STRING, true));
	    text.setStroke(new BasicStroke(LayerUtils.intFromProperties(
		properties, LPC_LINEWIDTH, 1)));
	    text.setAppObject(properties);
	}

	return text;
    }


}
