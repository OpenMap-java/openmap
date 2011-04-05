// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
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
// $Revision: 1.7 $
// $Date: 2009/01/21 01:24:42 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.mif;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.Debug;

/**
 * A loader class for MIF files. Each MIF layer loading a file will create an
 * instance of this The class uses SwingWorker to do processing in a thread Only
 * the MIF PLine and Region options are implemented
 * 
 * 27th January 2004 - added some support for TEXT and POINT options
 * 
 * @author Colin Mummery, modified January 2004 by Simon Bowen
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
    final static String POINT_WORD = "Point";
    final static String TEXT_WORD = "Text";

    BufferedReader br;
    OMGraphicList list;

    // if true we do a much faster line only rendering of the regions
    boolean accurate;

    // MIF CoordSys value for a Latitude Longitude coordinate system
    private static final String LATLONG_COORDSYS_DEF = "Earth Projection 1";

    private float pointVisible = -1; // default is -1
    private float textVisible = -1; // default is -1

    /**
     * Loads a MIF file from the Reader and placing the appropriate OMGraphics
     * on the OMGraphicList * Parsing is done by a simple loop and switch
     * statements
     * 
     * @param br
     *            BufferedReader to read the MIF file
     * @param accurate
     *            if true we do a much faster line only rendering of the regions
     * @param textVisible
     *            the scale at which TEXT primitives should be rendered
     * @param pointVisible
     *            the scale at which POINT primitives should be rendered
     */
    public MIFLoader(BufferedReader br, boolean accurate, float textVisible,
            float pointVisible) {
        super();
        this.br = br;
        this.accurate = accurate;
        this.pointVisible = pointVisible;
        this.textVisible = textVisible;
    }

    public boolean isLoaded() {
        return list != null;
    }

    /**
     * Get the OMGraphicList from the loader, creating it from the file if it
     * hasn't been created yet.
     */
    public OMGraphicList getList() {
        return getList(false);
    }

    /**
     * Get the OMGraphicList from the loader, with the option of forcing it to
     * be recreated from the source file if desired.
     */
    public OMGraphicList getList(boolean reloadList) {
        try {
            if (reloadList || !isLoaded()) {
                if (isLoaded())
                    list.clear();
                list = loadFile();
            }
            return list;
        } catch (IOException ioe) {
            list = null;
            // } catch(MIFException mex) {
            // list = null;
            // Debug.error(mex.getMessage());
            // mex.printStackTrace();
        }
        return null;
    }

    public OMGraphicList loadFile() throws IOException, MIFException {
        double[] ptarray = null;

        // Used by region to do the polygon calculation
        double[] latpts = null;
        double[] lonpts = null;

        // Specifies the expected next action in the loop
        int action = PROCESS_HEADER;
        int number = 0;
        int count = 0;
        int multiple = 0;
        int multicnt = 0;

        // setting to true means we don't read the same line again
        boolean pushback;
        StringTokenizer st = null;
        String tok = null;
        pushback = false;
        int idx;
        OMPoly omp = null;
        OMLine oml = null;
        MIFPoint ompoint = null;
        OMText omtext = null;
        boolean ismultiple = false;

        OMGraphicList aList = new OMGraphicList();

        // a vector of omgraphics for regions that allows adding and
        // deleting
        Vector omgs = new Vector();

        MAIN_LOOP: while (true) {

            if (!pushback) {
                // if it's null then there's no more
                if ((st = getTokens(br)) == null)
                    break MAIN_LOOP;

                tok = st.nextToken();
            } else {
                pushback = false; // pushback was true so make it
                // false so it doesn't happen twice
            }

            SWITCH: switch (action) {

            case PROCESS_HEADER:
                if (isSame(tok, DATA_WORD)) {
                    action = PROCESS_DATA;
                } else if (isSame(tok, VERSION_WORD)) {
                } else if (isSame(tok, DELIMITER_WORD)) {
                } else if (isSame(tok, COORDSYS_WORD)) {
                    // check the CoordSys header, OpenMap only
                    // directly
                    // supports LatLong type of coordsys
                    StringBuilder sb = new StringBuilder(COORDSYS_WORD);
                    while (st != null && st.hasMoreElements()) {
                        sb.append(' ').append(st.nextElement());
                    }

                    String coordSysLine = sb.toString();
                    String goodCoordSys = COORDSYS_WORD + " "
                            + LATLONG_COORDSYS_DEF;
                    if (goodCoordSys.length() < coordSysLine.length()) {
                        coordSysLine = coordSysLine.substring(0, goodCoordSys
                                .length());
                    } else {
                        goodCoordSys = goodCoordSys.substring(0, coordSysLine
                                .length());
                    }

                    // check that the CoordSys header matches the MIF
                    // specification for LatLong type
                    if (!isSame(coordSysLine, goodCoordSys)) {
                        Debug.error("MIFLoader file has coordinate system: "
                                + coordSysLine + ", requires " + goodCoordSys);
                        // raise error, as the coordsys header was
                        // invalid
                        throw new MIFException(
                                "File appears to contain objects with an incompatible coordinate system (Must be Lat/Lon).");
                    }

                }
                break SWITCH;

            case PROCESS_DATA:
                omgs.clear();
                if (st != null) {
                    if (isSame(tok, PLINE_WORD)) {
                        tok = st.nextToken();
                        if (isSame(tok, MULTIPLE_WORD)) {
                            multiple = Integer.parseInt(st.nextToken());
                            multicnt = 0;
                            action = PROCESS_MULTIPLE;
                            ismultiple = true;
                        } else {
                            number = Integer.parseInt(tok);
                            ptarray = new double[number + number];
                            count = 0;
                            action = PROCESS_PLINE;
                        }
                    } else if (isSame(tok, REGION_WORD)) {
                        multiple = Integer.parseInt(st.nextToken());
                        multicnt = 0;
                        action = PROCESS_REGION_HEADER;
                    } else if (isSame(tok, LINE_WORD)) {
                        float lon1 = Float.parseFloat(st.nextToken());
                        float lat1 = Float.parseFloat(st.nextToken());
                        float lon2 = Float.parseFloat(st.nextToken());
                        float lat2 = Float.parseFloat(st.nextToken());

                        oml = new OMLine(lat1, lon1, lat2, lon2,
                                OMGraphicConstants.LINETYPE_STRAIGHT);

                        action = PROCESS_POST_LINE;
                    } else if (isSame(tok, POINT_WORD)) // handle a MIF
                    // POINT primitive
                    {
                        // get the coordinates
                        float lon1 = Float.parseFloat(st.nextToken());
                        float lat1 = Float.parseFloat(st.nextToken());

                        // construct the OM graphic
                        ompoint = new MIFPoint(lat1, lon1, pointVisible);
                        st = getTokens(br);

                        // set the graphics attributes
                        this.processSymbolWord(st, ompoint);

                        // add to the graphic list for this layer
                        aList.add(ompoint);
                        action = PROCESS_DATA;
                    } else if (isSame(tok, TEXT_WORD)) // handle a MIF
                    // TEXT primitive
                    {
                        StringBuilder sb = new StringBuilder();

                        // if the actual text is not on the same line as
                        // the primitive declaration
                        if (st.countTokens() < 1) {
                            // get the next line
                            st = getTokens(br);
                        }
                        // build up the display text string,
                        while (st != null && st.hasMoreTokens()) {
                            sb.append(st.nextToken());
                        }

                        String textString = sb.toString();
                        if (textString.length() >= 1) {
                            // remove any surrounding " characters
                            textString = textString.substring(1, textString
                                    .length() - 1);
                        }
                        // get the next line, it contains the coordinates
                        st = getTokens(br);

                        float lon1 = Float.parseFloat(st.nextToken());
                        float lat1 = Float.parseFloat(st.nextToken());
                        /* float lon2 = */Float.parseFloat(st.nextToken());
                        /* float lat2 = */Float.parseFloat(st.nextToken());
                        // create the OMGraphic for the text object
                        omtext = new MIFText(lat1, lon1, textString,
                                OMText.JUSTIFY_CENTER, textVisible);

                        // the next line contains the text attributes
                        st = getTokens(br);
                        // set the attributes agains the omgraphic
                        this.processFontWord(st, omtext);
                        // add to the layers graphic list
                        aList.add(omtext);

                        action = PROCESS_DATA;
                    }
                }
                break SWITCH;

            // We have a line, tok is the first coord and the next
            // token is the second
            case PROCESS_PLINE:
                idx = count + count;
                if (ptarray != null && st != null) {
                    ptarray[idx + 1] = Float.parseFloat(tok);
                    ptarray[idx] = Float.parseFloat(st.nextToken());
                    count++;
                    if (count == number) {
                        omp = new OMPoly(ptarray, OMGraphic.DECIMAL_DEGREES,
                                OMGraphic.LINETYPE_STRAIGHT);

                        aList.add(omp);
                        if (!ismultiple) {
                            action = PROCESS_POST_PLINE;
                        } else {
                            omgs.add(omp);
                            action = PROCESS_MULTIPLE;
                        }
                    }
                }
                break SWITCH;

            case PROCESS_MULTIPLE:
                multicnt++;
                if (multicnt > multiple) { // No more multiples so we
                    // can pushback
                    pushback = true;
                    multiple = 0;
                    action = PROCESS_POST_PLINE;
                    break SWITCH;
                }
                number = Integer.parseInt(tok);
                count = 0;
                ptarray = new double[number + number];
                action = PROCESS_PLINE;
                break SWITCH;

            case PROCESS_POST_PLINE:
                if (isSame(tok, PEN_WORD)) {
                    if (ismultiple) {
                        processPenWord(st, omgs);
                    } else {
                        processPenWord(st, omp);
                    }
                } else if (isSame(tok, SMOOTH_WORD)) {
                    // Smooth unimplemented
                } else {
                    ismultiple = false;
                    pushback = true;
                    action = PROCESS_DATA;
                }
                break SWITCH;

            // SCN to support lines
            case PROCESS_POST_LINE:
                if (isSame(tok, PEN_WORD)) {
                    processPenWord(st, oml);
                    aList.add(oml);
                } else {
                    ismultiple = false;
                    pushback = true;
                    action = PROCESS_DATA;
                }
                break SWITCH;

            case PROCESS_REGION_HEADER: // This processes the number
                // at the top of each region
                // sub-block
                multicnt++;
                if (multicnt > multiple) {
                    multiple = 0;
                    action = PROCESS_POST_REGION;

                    // Add this point the region is finished so add
                    // the
                    // vector contents to list
                    int len = omgs.size();
                    for (int i = 0; i < len; i++) {
                        aList.add((OMGraphic) omgs.elementAt(i));
                    }
                    break SWITCH;
                }
                number = Integer.parseInt(tok);
                count = 0;
                ptarray = new double[number + number];
                latpts = new double[number];
                lonpts = new double[number];
                action = PROCESS_REGION;
                break SWITCH;

            case PROCESS_REGION:
                idx = count + count;
                if (ptarray != null && lonpts != null && latpts != null
                        && st != null) {
                    lonpts[count] = ptarray[idx + 1] = Float.parseFloat(tok);
                    latpts[count] = ptarray[idx] = Float.parseFloat(st
                            .nextToken());
                    count++;
                    if (count == number) {
                        // This polygon is complete so add it and process
                        // the next

                        // Use this code if we just want polygons which is
                        // much
                        // faster
                        if (accurate) {
                            omgs.add(new OMSubtraction(latpts, lonpts));

                        } else {
                            // Produces accurate MapInfo type rendering
                            // but very
                            // slow with complex regions like streets
                            int end = latpts.length - 1;

                            for (int i = 0; i < end; i++) {
                                omgs.add(new OMLine(latpts[i], lonpts[i],
                                        latpts[i + 1], lonpts[i + 1],
                                        OMGraphic.LINETYPE_STRAIGHT));
                            }
                            omgs.add(new OMLine(latpts[end], lonpts[end],
                                    latpts[0], lonpts[0],
                                    OMGraphic.LINETYPE_STRAIGHT));
                        }
                        action = PROCESS_REGION_HEADER;
                    }
                }
                break SWITCH;

            // There is one pen,brush,center block at the end of a
            // region
            case PROCESS_POST_REGION:
                if (isSame(tok, PEN_WORD)) {
                    processPenWord(st, omgs);
                } else if (isSame(tok, BRUSH_WORD)) {
                    processBrushWord(st, omgs);
                } else if (isSame(tok, CENTER_WORD)) {
                } else {
                    pushback = true;
                    action = PROCESS_DATA;
                }
                break SWITCH;

            } // end of switch
        } // end of while loop

        br.close();

        return aList;
    }

    /*
     * Processes an instance of the Pen directive for a single OMGraphic
     */
    private void processPenWord(StringTokenizer st, OMGraphic omg) {
        if (omg == null)
            return;
        int width = Integer.parseInt(st.nextToken());
        omg.setStroke(new BasicStroke(width));
        /* int pattern = */Integer.parseInt(st.nextToken());
        Color col = convertColor(Integer.parseInt(st.nextToken()));
        omg.setLinePaint(col);
    }

    /*
     * Processes an instance of the Pen directive for a vector of OMGraphics
     */
    private void processPenWord(StringTokenizer st, Vector vals) {
        int width = Integer.parseInt(st.nextToken());
        /* int pattern = */Integer.parseInt(st.nextToken());
        Color col = convertColor(Integer.parseInt(st.nextToken()));
        int len = vals.size();
        OMGraphic omg = null;
        for (int i = 0; i < len; i++) {
            omg = (OMGraphic) vals.elementAt(i);
            omg.setLinePaint(col);
            omg.setStroke(new BasicStroke(width));
        }
    }

    /*
     * Processes an instance of the Brush directive
     */
    private void processBrushWord(StringTokenizer st, Vector vals) {

        int pattern = Integer.parseInt(st.nextToken());
        Color foreground = convertColor(Integer.parseInt(st.nextToken()));

        /*
         * background appears to be ignored by MapInfo but I grab it anyway
         */
        // Color background = null;
        // if (st.hasMoreTokens()) {
        // background = convertColor(Integer.parseInt(st.nextToken()));
        // }
        int len = vals.size();
        OMGraphic omg;
        for (int i = 0; i < len; i++) {
            omg = (OMGraphic) vals.elementAt(i);
            omg.setLinePaint(foreground);

            switch (pattern) {
            case 1:
                break; // No fill so do nothing
            case 2:
                omg.setFillPaint(foreground);
                break;
            }
        }
    }

    /**
     * process the MIF SYMBOL element.
     * 
     * The MIF format for SYMBOL element is <code>
     *  SYMBOL (shape, color, size, fontname, fontstyle, rotation)
     * </code> or <code>
     *  SYMBOL (filename, color, size, customstyle)
     * </code>
     * 
     * currently only the color attribute is considered and a default OMPoint
     * symbol and size is adopted.
     * 
     * @param st
     *            tokenizer containing the "SYMBOL" MIF elements
     * @param omg
     *            the OMGraphic object to attribute with the setting from the
     *            MIF line
     */
    private void processSymbolWord(StringTokenizer st, OMPoint omg) {
        /* String symbolStr = */st.nextToken(); // should be "SYMBOL"

        /* int symbol = */Integer.parseInt(st.nextToken());
        Color color = convertColor(Integer.parseInt(st.nextToken()));

        /* int size = */Integer.parseInt(st.nextToken());

        omg.setFillPaint(color);
    }

    /**
     * process the MIF FONT element. currently only PLAIN (0), BOLD(1), ITALIC
     * (2) and BOLD ITALIC(3) are supported. Font size is hardcoded to 10, and
     * backcolor is ignored.
     * 
     * The MIF format for FONT is <code>
     *  FONT ("fontname", style, size, forecolor [, backcolor] )
     * </code>
     * 
     * Within a MIF file size will always be 0, it's up to the renderer to
     * determine the size. Background color is optional
     * 
     * style is determined as follows, to specify 2 or more style attributes, add
     * the values from each style, e.g. BOLD ALLCAPS = 513
     * 
     * value style =================== 0 PLAIN 1 BOLD 2 ITALIC 4 UNDERLINE 16
     * OUTLINE 32 SHADOW 256 HALO 512 ALL CAPS 1024 Expanded
     * 
     * 
     * @param st
     *            tokenizer containing the "FONT" MIF elements
     * @param omTxt
     *            the OMGraphic object to attribute with the setting from the
     *            MIF line
     */
    private void processFontWord(StringTokenizer st, OMText omTxt) {
        /* String fontStr = */st.nextToken(); // should be "FONT"
        String fontName = st.nextToken();
        int style = Integer.parseInt(st.nextToken());
        /* int size = */Integer.parseInt(st.nextToken());
        Color foreColor = convertColor(Integer.parseInt(st.nextToken()));

        // last token is optional background color
        // Color bgColor = null;
        // if (st.hasMoreTokens()) {
        // bgColor = convertColor(Integer.parseInt(st.nextToken()));
        // }

        int fontStyle = Font.PLAIN;
        switch (style) {
        case 0:
            fontStyle = Font.PLAIN;
            break;
        case 1:
            fontStyle = Font.BOLD;
            break;
        case 2:
            fontStyle = Font.ITALIC;
            break;
        case 3:
            fontStyle = Font.BOLD & Font.ITALIC;
            break;
        }

        omTxt.setFillPaint(foreColor);
        omTxt.setFont(new Font(fontName.substring(1, fontName.length() - 1),
                fontStyle, 10));
    }

    /*
     * Creates a tokenizer for each line of input
     */
    private StringTokenizer getTokens(BufferedReader br) throws IOException {
        String line;
        WHILE: while ((line = br.readLine()) != null) {

            if (line.length() == 0)
                continue WHILE; // skip blank lines

            // should return the tokenizer as soon as we have a line
            return new StringTokenizer(line, " \t\n\r\f,()");
        }
        return null;
    }

    /*
     * Utility for doing case independent string comparisons... it's neater this
     * way
     */
    private boolean isSame(String str1, String str2) {
        return str1.equalsIgnoreCase(str2);
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
            red = rem / 65536;
            rem -= red * 65536;
        }
        if (rem >= 255) {
            green = rem / 256;
            rem -= green * 256;
        }
        if (rem > 0)
            blue = rem;

        return new Color(red, green, blue);
    }
}

/* Last line of file */
