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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFLoader.java,v $
// $RCSfile: MIFLoader.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.mif;


import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * A loader class for MIF files. Each MIF layer loading a file will
 * create an instance of this The class uses SwingWorker to do
 * processing in a thread Only the MIF PLine and Region options are
 * implemented
 * @author Colin Mummery 
 */
public class MIFLoader {
    
    final static int PROCESS_HEADER = 0;
    final static int PROCESS_DATA = 1;
    final static int PROCESS_PLINE = 2;
    final static int PROCESS_POST_PLINE = 3;
    final static int PROCESS_MULTIPLE = 4;
    final static int PROCESS_REGION = 5;
    final static int PROCESS_REGION_HEADER = 6;
    final static int PROCESS_POST_REGION = 7;
    final static int PROCESS_POST_LINE = 8;
    
    final static String DATA_WORD = "Data";
    final static String VERSION_WORD = "Version";
    final static String DELIMITER_WORD = "Delimiter";
    final static String COORDSYS_WORD = "Coordsys";
    final static String PLINE_WORD = "PLine";
    final static String LINE_WORD = "Line";
    final static String MULTIPLE_WORD = "Multiple";
    final static String PEN_WORD = "Pen";
    final static String SMOOTH_WORD = "Smooth";
    final static String REGION_WORD = "Region";
    final static String BRUSH_WORD = "Brush";
    final static String CENTER_WORD = "Center";
    
    BufferedReader br;
    OMGraphicList list;
    
    //if true we do a much faster line only rendering of the regions
    boolean accurate; 

    boolean loaded = false;

    /**
     * Loads a MIF file from the Reader and placing the appropriate
     * OMGraphics on the OMGraphicList * Parsing is done by a simple
     * loop and switch statements 
     */
    public MIFLoader(BufferedReader br,boolean accurate) {
	super(); 
	this.br = br; 
	this.accurate = accurate;
	list = new OMGraphicList();
    }

    public boolean isLoaded() {
	return loaded;
    }
    
    public OMGraphicList getList() {
	try{
	    loadFile();
	    loaded = true;
	    return list;
	} catch(IOException ioe) {
	    loaded = false;
	    return null;
	}
    }
    
    public void loadFile() throws IOException {
	
	float[] ptarray = null;
	
	//Used by region to do the polygon calculation
	float[] latpts = null; 
	float[] lonpts = null;
	
	//Specifies the expected next action in the loop
	int action = PROCESS_HEADER; 
	int number = 0;
	int count = 0;
	int multiple = 0;
	int multicnt = 0;
	
	//setting to true means we don't read the same line again
	boolean pushback; 
	String empty = "";
	StringTokenizer st = null; 
	String tok = null;
	pushback = false; int idx; 
	OMPoly omp = null;
	OMLine oml = null;
	boolean ismultiple = false;
	
	
	// a vector of omgraphics for regions that allows adding and
	// deleting
	Vector omgs = new Vector(); 
	
    MAIN_LOOP:
	while(true) {
	    
	    if (!pushback) {
				//if it's null then there's no more
		if ((st = getTokens(br)) == null)
		    break MAIN_LOOP; 
		
		tok = st.nextToken();
	    } else {
		pushback = false; //pushback was true so make it false so it doesn't happen twice
	    }
	    
	SWITCH: 
	    switch(action) {
		
	    case PROCESS_HEADER:
		if (isSame(tok,DATA_WORD)) {action = PROCESS_DATA;}
		else if (isSame(tok,VERSION_WORD)) {}
		else if (isSame(tok,DELIMITER_WORD)) {}
		else if (isSame(tok,COORDSYS_WORD)) {}
		break SWITCH;
		
	    case PROCESS_DATA:
		omgs.clear();
		if (isSame(tok,PLINE_WORD)) {
		    tok = st.nextToken();
		    if (isSame(tok,MULTIPLE_WORD)) {
			multiple = Integer.parseInt(st.nextToken()); 
			multicnt = 0; 
			action = PROCESS_MULTIPLE;
			ismultiple = true;
		    }
		    else {
			number = Integer.parseInt(tok);
			ptarray = new float[number+number];
			count = 0; 
			action = PROCESS_PLINE;
		    }
		}
		else if (isSame(tok,REGION_WORD)) {
		    multiple = Integer.parseInt(st.nextToken()); 
		    multicnt = 0;
		    action = PROCESS_REGION_HEADER;
		}
		else if( isSame( tok, LINE_WORD))  {
		    float lon1  =  Float.parseFloat( st.nextToken());
		    float lat1  =  Float.parseFloat( st.nextToken());
		    float lon2  =  Float.parseFloat( st.nextToken());
		    float lat2  =  Float.parseFloat( st.nextToken());
		    oml  =  new OMLine(lat1, lon1, lat2, lon2, 
				     OMGraphicConstants.LINETYPE_STRAIGHT);
		    action  =  PROCESS_POST_LINE;	
		}			
		break SWITCH;
		
		//We have a line, tok is the first coord and the next
		//token is the second
	    case PROCESS_PLINE: 
		idx = count+count;
		ptarray[idx+1] = Float.parseFloat(tok); 
		ptarray[idx] = Float.parseFloat(st.nextToken());
		count++;
		if (count == number) {
		    omp  =  new OMPoly(ptarray,
				     OMGraphic.DECIMAL_DEGREES,
				     OMGraphic.LINETYPE_STRAIGHT);
		    list.add(omp);
		    if (!ismultiple) {
			action = PROCESS_POST_PLINE;
		    } else {
			omgs.add(omp); 
			action = PROCESS_MULTIPLE;
		    }
		}
		break SWITCH;
		
		
		
	    case PROCESS_MULTIPLE:
		multicnt++;
		if (multicnt > multiple) { //No more multiples so we can pushback
		    pushback = true; 
		    multiple = 0; 
		    action = PROCESS_POST_PLINE; 
		    break SWITCH;
		}
		number = Integer.parseInt(tok); 
		count = 0;
		ptarray = new float[number+number]; 
		action = PROCESS_PLINE;
		break SWITCH;
		
	    case PROCESS_POST_PLINE:
		if (isSame(tok,PEN_WORD)) {
		    if (ismultiple) {
			processPenWord(st,omgs); 
		    } else {
			processPenWord(st,omp);
		    }
		} else if (isSame(tok,SMOOTH_WORD)) {
		    //Smooth unimplemented
		} else {
		    ismultiple = false; 
		    pushback = true; 
		    action = PROCESS_DATA;
		}
		break SWITCH;
		
		
		// SCN to support lines
	    case PROCESS_POST_LINE:
		if( isSame(tok,PEN_WORD)) {
		    processPenWord(st,oml);
		    list.add( oml);
		} else {
		    ismultiple = false;
		    pushback = true;
		    action = PROCESS_DATA;
		}
		break SWITCH;
		
	    case PROCESS_REGION_HEADER: //This processes the number at the top of each region sub-block
		multicnt++;
		if (multicnt > multiple) {
		    multiple = 0; 
		    action = PROCESS_POST_REGION;
		    
		    //Add this point the region is finished so add the
		    //vector contents to list
		    int len = omgs.size(); 
		    for(int i = 0;i<len;i++) {
			list.add((OMGraphic)omgs.elementAt(i));
		    }
		    break SWITCH;
		}
		number = Integer.parseInt(tok);
		count = 0;
		ptarray = new float[number+number]; 
		latpts = new float[number]; 
		lonpts = new float[number];
		action = PROCESS_REGION;
		break SWITCH;
		
	    case PROCESS_REGION:
		idx = count+count;
		lonpts[count] = ptarray[idx+1] = Float.parseFloat(tok);
		latpts[count] = ptarray[idx] = Float.parseFloat(st.nextToken());
		count++;
		if (count == number) { 
		    
		    // This polygon is complete so add it and process the next
		    // At this point we need to go through the omgs vector
		    // looking for containing polygons and we replace the
		    // containing polygon with an instance of OMSubtraction we
		    // assume at this time that we only ever find one polygon
		    // within another Assume that if any point in this polygon
		    // is inside another polygon then it's contained
		    
		    //Use this code if we just want polygons which is much
		    //faster
		    if (accurate) { 
			int listln = omgs.size();
			for(int i = 0;i<listln;i++) { //Go through the list
			    OMSubtraction oms = (OMSubtraction)omgs.elementAt(i);
			    if (oms.contains(latpts,lonpts)) {
				action = PROCESS_REGION_HEADER;
				break SWITCH;
			    }
			}
			omgs.add(new OMSubtraction(latpts,lonpts));
		    }
		    else {
			
				// Produces accurate MapInfo type rendering but very
				// slow with complex regions like streets
			int end = latpts.length-1;
			for(int i = 0;i<end;i++) {
			    omgs.add(new OMLine(latpts[i],
						lonpts[i],
						latpts[i+1],lonpts[i+1],
						OMGraphic.LINETYPE_STRAIGHT));
			}
			omgs.add(new OMLine(latpts[end],
					    lonpts[end],
					    latpts[0],
					    lonpts[0],
					    OMGraphic.LINETYPE_STRAIGHT));
			
		    }
		    action = PROCESS_REGION_HEADER;
		}
		break SWITCH;
		
		// There is one pen,brush,center block at the end of a
		// region
	    case PROCESS_POST_REGION: 
		if (isSame(tok,PEN_WORD)) {
		    processPenWord(st,omgs);
		} else if (isSame(tok,BRUSH_WORD)) {
		    processBrushWord(st,omgs);
		} else if (isSame(tok,CENTER_WORD)) {
		} else {
		    pushback = true; 
		    action = PROCESS_DATA;
		}
		break SWITCH;
		
	    }// end of switch
	}//end of while loop
    }
    
    /*
     *  Processes an instance of the Pen directive for a single
     *  OMGraphic 
     */
    private void processPenWord(StringTokenizer st,OMGraphic omg) {
	if (omg == null) return;
	int width = Integer.parseInt(st.nextToken());
	omg.setStroke(new BasicStroke(width));
	int pattern = Integer.parseInt(st.nextToken());
	Color col = convertColor(Integer.parseInt(st.nextToken()));
	omg.setLinePaint(col);
    }
    
    /*
     *  Processes an instance of the Pen directive for a vector of
     *  OMGraphics 
     */
    private void processPenWord(StringTokenizer st,Vector vals) {
	int width = Integer.parseInt(st.nextToken());
	int pattern = Integer.parseInt(st.nextToken());
	Color col = convertColor(Integer.parseInt(st.nextToken()));
	int len = vals.size(); 
	OMGraphic omg = null;
	for(int i = 0;i<len;i++) {
	    omg = (OMGraphic)vals.elementAt(i); 
	    omg.setLinePaint(col);
	    omg.setStroke(new BasicStroke(width));
	}
    }
    
    /*
     * Processes an instance of the Brush directive
     */
    private void processBrushWord(StringTokenizer st,Vector vals) {
	
	int pattern = Integer.parseInt(st.nextToken());
	Color foreground = convertColor(Integer.parseInt(st.nextToken()));
	Color background = null;
	
	//background appears to be ignored by MapInfo but I grab it anyway
	if (st.hasMoreTokens())
	    background = convertColor(Integer.parseInt(st.nextToken()));
	int len = vals.size(); OMGraphic omg;
	for(int i = 0;i<len;i++) {
	    omg = (OMGraphic)vals.elementAt(i);
	    omg.setLinePaint(foreground); 
	    
	    switch(pattern) {
	    case 1: break; //No fill so do nothing
	    case 2:
		omg.setFillPaint(foreground);
		break;
	    }
	}
    }
    
    /*
     * Creates a tokenizer for each line of input
     */
    private StringTokenizer getTokens(BufferedReader br) throws IOException {
	String line;
    WHILE: 
	while((line = br.readLine())!= null) {
	    
	    if (line.equals("")) continue WHILE; //skip blank lines
	    
	    //should return the tokenizer as soon as we have a line
	    return new StringTokenizer(line," \t\n\r\f,()"); 
	}
	return null; 
    }
    
    /*
     * Utility for doing case independant string comparisons... it's
     * neater this way 
     */
    private boolean isSame(String str1,String str2) {
	if (str1.compareToIgnoreCase(str2) == 0) {
	    return true; 
	} else {
	    return false;
	}
    }
    
    /*
     * Converts MIF file color to Java Color object
     */
    private Color convertColor(int val) {
	int red = 0; 
	int green = 0; 
	int blue = 0;
	int rem = val;
	if (rem >= 65536) {
	    red = rem/65536; 
	    rem = rem-red*65536;
	}
	if (rem >= 255) {
	    green = rem/256; 
	    rem = rem-green*256;
	}
	if (rem > 0) blue = rem;
	
	return new Color(red,green,blue);
    }

}
