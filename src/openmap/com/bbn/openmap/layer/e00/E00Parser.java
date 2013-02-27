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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/e00/E00Parser.java,v $
// $RCSfile: E00Parser.java,v $
// $Revision: 1.9 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.e00;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.bbn.openmap.layer.location.BasicLocation;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMText;
import com.bbn.openmap.util.Debug;

/**
 * A parser for an E00 file. Description of the Class parses an E00 file and
 * provides as result an OMGraphicList containing up to 3 OMGraphicLists:
 * 
 * <pre>
 * 
 *   - arcs : OMPoly read in ARC records
 *   - labs : BasicLocations read in LAB records
 *   - tx7  : OMPolys and BasicLocation read in TX7 records
 * 
 * </pre>
 * 
 * PAl,LOG,SIN,PRJ,TOL records are ignored. <br>
 * From IFO records (if available) :<br>
 * - each arc gets an AppObject including the type and a value (generally an
 * altitude) <br>
 * - each lab gets an AppObject including the type, 2 values and the String to
 * display if available - the type is used to decide the color, from the Color
 * array. Color and String may be also extracted from PAT or AAT records. <br>
 * 
 * This software is provided as it is. No warranty of any kind, and in
 * particular I don't know at all if it meets e00 file specification. It works
 * quite good on files from GIS data depot .
 * 
 * 
 * @author paricaud
 */
public class E00Parser {
    protected OMGraphicList labs, arcs, tx7;
    protected BufferedReader isr;
    protected String prefix;
    protected int narc = 1, npoint = 1, unClosedCount = 0;
    protected Paint[] ArcColors = defaultColors;
    protected Paint[] LabColors = defaultColors;
    protected Paint tx7Color;
    protected Paint SelectTX7Color, SelectLabColor, SelectArcColor, LabTextColor;
    protected Font labFont, tx7Font;
    protected OMGraphic LabMarker;
    protected Color defaultcolor = Color.blue;
    public final static Color[] defaultColors = { Color.black, Color.blue, Color.cyan,
            Color.darkGray, Color.gray, Color.green, Color.lightGray, Color.magenta, Color.orange,
            Color.pink, Color.red, Color.white, Color.yellow };

    protected static E00Record infoRecord = new E00Record(new int[] { 0, 30, 34, 38, 42, 46, 56 }, new int[] {
            20, 20, 50, 50, 50, 50 }, null);
    protected static E00Record itemRecord = new E00Record(new int[] { 0, 14, 19, 21, 26, 28, 32,
            34, 37, 39, 43, 47, 49, 69 }, new int[] { 20, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
            50, 50 }, null);

    /**
     * Constructor for the E00Parser object
     * 
     * @param mdname File Name to parse
     * @exception IOException
     * @since
     */
    public E00Parser(String mdname) throws IOException {
        isr = new BufferedReader(new FileReader(mdname));
        setPrefix(mdname);
    }

    /**
     * Constructor for the E00Parser object
     * 
     * @param f File to parse
     * @exception IOException
     * @since
     */
    public E00Parser(File f) throws IOException {
        isr = new BufferedReader(new FileReader(f));
        setPrefix(f.getName());
    }

    /**
     * Sets the Prefix attribute of the E00Parser object
     * 
     * @param S The new Prefix value
     * @since
     */
    public void setPrefix(String S) {
        int n = S.indexOf('.');
        if (n == -1)
            prefix = S.toUpperCase();
        else
            prefix = S.substring(0, n).toUpperCase();
    }

    /**
     * Sets the Colors attribute of the E00Parser object
     * 
     * @param ArcColors Paint array for arcs
     * @param LabColors Paint array for labs marker
     * @param tx7Color Paint for tx7
     * @param SelectTX7Color Paint for tx7 when selected
     * @param SelectLabColor Paint for labs when selected (not working ?)
     * @param SelectArcColor Paint for arcs when selected
     * @param LabTextColor Paint for labs text . If null, text has same paint as
     *        marker
     * @since
     */
    public void setPaints(Paint[] ArcColors, Paint[] LabColors, Paint tx7Color,
                          Paint SelectTX7Color, Paint SelectLabColor, Paint SelectArcColor,
                          Paint LabTextColor) {

        this.ArcColors = (ArcColors == null) ? defaultColors : ArcColors;
        this.LabColors = (LabColors == null) ? defaultColors : LabColors;
        this.tx7Color = tx7Color;
        this.SelectTX7Color = SelectTX7Color;
        this.SelectLabColor = SelectLabColor;
        this.SelectArcColor = SelectArcColor;
        this.LabTextColor = LabTextColor;
    }

    /**
     * Sets the Fonts attribute of the E00Parser object
     * 
     * @param labFont font for labs text
     * @param tx7Font font for tx7 text
     * @since
     */
    public void setFonts(Font labFont, Font tx7Font) {
        this.labFont = labFont;
        this.tx7Font = tx7Font;
    }

    /**
     * Sets the LabMarker attribute of the E00Parser object
     * 
     * @param marker The new LabMarker value
     * @since
     */
    public void setLabMarker(OMGraphic marker) {
        LabMarker = marker;
    }

    /**
     * Gets the result of the parse process
     * 
     * @return The OMGraphics value
     * @exception IOException
     * @since
     */
    public OMGraphicList getOMGraphics() throws IOException {
        OMGraphicList WV = new OMGraphicList();
        isr.readLine();
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            // System.out.println("E00 "+S);
            if (S.startsWith("ARC"))
                readARC();
            else if (S.startsWith("LAB"))
                readLAB();
            else if (S.startsWith("IFO"))
                readIFO();
            else if (S.startsWith("LOG"))
                readLOG();
            else if (S.startsWith("PRJ"))
                readPRJ();
            else if (S.startsWith("CNT"))
                readCNT();
            else if (S.startsWith("PAL"))
                readPAL();
            else if (S.startsWith("SIN"))
                readSIN();
            else if (S.startsWith("TOL"))
                readTOL();
            else if (S.startsWith("TX7"))
                readTX7();
            else if (S.startsWith("EOS"))
                break;
            // System.out.println("E00 "+S+" fin");
        }

        if (labs != null) {
            labs.setAppObject("LABS");
            WV.add(labs);
        }
        if (arcs != null) {
            arcs.setAppObject("ARCS");
            WV.add(arcs);
        }
        if (tx7 != null) {
            tx7.setAppObject("TX7");
            WV.add(tx7);
        }

        return WV;
    }

    /**
     * Gets the LabMarker attribute of the E00Parser object
     * 
     * @return The LabMarker value
     * @since
     */
    public OMGraphic getLabMarker() {
        return LabMarker;
    }

    /**
     * read from a string an array of int each float being represented by l
     * characters
     * 
     * @param S the String to parse
     * @param l the length of int representation
     * @param I Description of Parameter
     * @since
     */
    void parseString(String S, int[] I, int l) {
        int i = 0;
        for (int j = 0; i < I.length && j < S.length(); j += l)
            I[i++] = Integer.parseInt(S.substring(j, j + l).trim());
    }

    /**
     * read from a string an array of float each float being represented by 14
     * characters
     * 
     * @param S the String to parse
     * @param F the float array receiving the result
     * @since
     */
    void parseString(String S, double[] F) {
        int i = 0;
        for (int j = 0; i < F.length && j < S.length(); j += 14)
            F[i++] = Float.parseFloat(S.substring(j, j + 14).trim());

    }

    /**
     * read SIN records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readSIN() throws IOException {
        while (true) {
            String S = isr.readLine();
            if (S == null)
                return;
            if (S.startsWith("EOX"))
                return;
        }
    }

    /**
     * read CNT records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readCNT() throws IOException {
        int[] header = new int[1];
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            parseString(S, header, 10);
            int n = header[0];
            if (n == -1)
                break;
            for (int i = 0; i < n; i++)
                isr.readLine();
        }
    }

    /**
     * read TOL records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readTOL() throws IOException {
        int[] header = new int[1];
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            parseString(S, header, 10);
            if (header[0] == -1)
                break;
        }
    }

    /**
     * read PAL records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readPAL() throws IOException {
        int[] header = new int[1];
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            parseString(S, header, 10);
            int n = header[0];
            if (n == -1)
                break;
            for (int i = 0; i < n; i += 2)
                isr.readLine();
        }
    }

    /**
     * read TX7 records
     * 
     * @exception IOException
     * @since
     */
    void readTX7() throws IOException {
        Debug.message("e00", "E00: read TX7");
        tx7 = new OMGraphicList();
        int[] header = new int[8];
        double[] coords = new double[2];
        isr.readLine();
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            parseString(S, header, 10);
            if (header[0] == -1)
                break;
            int n = header[2];
            for (int i = 0; i < 8; i++)
                isr.readLine();
            double[] llpoints = new double[2 * n];
            int k = 0;
            for (int j = 0; j < n; j++) {
                S = isr.readLine();
                if (S == null)
                    return;
                parseString(S, coords);
                llpoints[k++] = coords[1];
                llpoints[k++] = coords[0];
            }
            S = isr.readLine();
            /*
             * OMPoly P = new OMPoly(llpoints, OMGraphic.DECIMAL_DEGREES,
             * OMGraphic.LINETYPE_STRAIGHT); / llpoints is so transformed to
             * radians P.setLinePaint(Color.red); tx7.add(P); BasicLocation bl =
             * new BasicLocation(coords[1], coords[0], S, null);
             * bl.setShowLocation(true); bl.setShowName(true); tx7.add(bl);
             */
            TX7 t = new TX7(llpoints, S, false, tx7Font);
            // decimal degrees
            if (tx7Color != null)
                t.setLinePaint(tx7Color);
            if (SelectTX7Color != null)
                t.setSelectPaint(SelectTX7Color);

            tx7.add(t);
        }
    }

    /**
     * read LOG records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readLOG() throws IOException {
        while (true) {
            String S = isr.readLine();
            if (S == null)
                return;
            if (S.startsWith("EOL"))
                return;
        }
    }

    /**
     * read PRJ records (in fact does nothing)
     * 
     * @exception IOException
     * @since
     */
    void readPRJ() throws IOException {
        while (true) {
            String S = isr.readLine();
            if (S == null)
                return;
            if (S.startsWith("EOP"))
                return;
        }
    }

    /**
     * read LAB records
     * 
     * @exception IOException
     * @since
     */
    void readLAB() throws IOException {
        Debug.message("e00", "E00: read LAB");
        labs = new OMGraphicList();
        double[] coords = new double[2];
        int[] header = new int[1];
        while (true) {
            String S = isr.readLine();
            if (S == null)
                break;
            parseString(S, header, 10);
            int id = header[0];
            if (id == -1)
                break;
            S = isr.readLine();
            if (S == null)
                break;
            parseString(S, coords);
            BasicLocation bl = new BasicLocation(coords[1], coords[0], "", LabMarker);
            setLocationColor(bl, 0);
            bl.setShowLocation(true);
            labs.add(bl);
            bl.setAppObject(new E00Data(id));
        }
    }

    /**
     * read ARC records
     * 
     * @exception IOException
     * @since
     */
    void readARC() throws IOException {
        Debug.message("e00", "E00: read ARC");
        arcs = new OMGraphicList();
        int narc = 1;
        int[] header = new int[7];
        double[] coords = new double[4];
        while (true) {
            String S = isr.readLine();
            if (S == null)
                return;
            // System.out.println("E00: F "+S);
            parseString(S, header, 10);
            if (header[0] == -1)
                break;
            int n = header[6];
            double[] llpoints = new double[2 * n];
            int k = 0;
            for (int j = 0; j < n; j++) {
                S = isr.readLine();
                if (S == null)
                    return;
                parseString(S, coords);
                llpoints[k++] = coords[1];
                llpoints[k++] = coords[0];
                if (++j >= n)
                    break;
                llpoints[k++] = coords[3];
                llpoints[k++] = coords[2];
            }
            // System.out.print("f ");
            // System.out.println(" # "+narc++ +" nb:"+npoint);
            OMPoly P = new OMPoly(llpoints, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_STRAIGHT);
            P.setLinePaint(getArcPaint(0));
            if (SelectArcColor != null)
                P.setSelectPaint(SelectArcColor);

            arcs.add(P);
            P.setAppObject(new E00Data(narc++));
        }
    }

    /**
     * read AAT records
     * 
     * @exception IOException
     * @since
     */
    void readAAT() throws IOException {
        String S;
        String C = prefix + ".AAT";
        do {
            S = isr.readLine();
            if (S == null)
                return;
        } while (!S.startsWith(C));
        int n = Integer.parseInt(S.substring(46).trim());
        Debug.message("e00", "E00: read " + C + " " + n + " points");
        for (int i = 0; i < 9; i++)
            isr.readLine();
        int[] I = new int[2];
        for (int i = 0; i < n; i++) {
            isr.readLine();
            S = isr.readLine();
            parseString(S, I, 2);
            arcs.getOMGraphicAt(i).setLinePaint(getArcPaint(I[0]));
        }
    }

    /**
     * read PAT records
     * 
     * @exception IOException
     * @since
     */
    void readPAT() throws IOException {
        String S;
        String C = prefix + ".PAT";
        do {
            S = isr.readLine();
            if (S == null)
                return;
        } while (!S.startsWith(C));
        int n = Integer.parseInt(S.substring(46).trim());
        Debug.message("e00", "E00: read " + C + " " + n + " points");
        for (int i = 0; i < 7; i++)
            isr.readLine();
        int[] I = new int[1];
        for (int i = 0; i < n; i++) {
            S = isr.readLine();
            if (S == null)
                break;
            String name = S.substring(50);
            // System.out.print(i+" "+S);
            S = isr.readLine();
            if (S == null)
                break;
            // System.out.println(" "+S);
            parseString(S, I, 14);
            BasicLocation bl = (BasicLocation) labs.getOMGraphicAt(i);
            if (S.length() > 0)
                bl.setName(name);
            else
                bl.setLabel(null);
            setLocationColor(bl, I[0]);
            bl.setShowLocation(true);
        }
    }

    /**
     * read IFO information
     * 
     * @exception IOException
     * @since
     */
    void readIFO() throws IOException {
        while (true) {
            infoRecord.read(isr);
            String info = infoRecord.getStringField(0).trim();
            if (info.startsWith("EOI"))
                break;
            int n = infoRecord.getIntField(5);
            E00Record r = getRecord();
            if (!info.startsWith(prefix))
                readANY(r, n);
            else {
                String suffix = info.substring(prefix.length() + 1);
                if (suffix.equals("PAT"))
                    readPAT(r, n);
                else if (suffix.equals("AAT"))
                    readAAT(r, n);
                else if (suffix.equals("BND"))
                    readANY(r, n);
                else if (suffix.equals("TIC"))
                    readANY(r, n);
                else
                    readANY(r, n);
            }
            Debug.message("e00", "E00: " + info + "  " + n);
        }
    }

    /**
     * read other records from IFO (does nothing else)
     * 
     * @param r record structure
     * @param n number of records
     * @exception IOException
     * @since
     */
    void readANY(E00Record r, int n) throws IOException {
        for (int i = 0; i < n; i++)
            r.read(isr);
    }

    /**
     * read form IFO, PAT records extract data from them and put this data in an
     * E00data structure associated with the graphic object
     * 
     * @param r PTT/IFO structure
     * @param n record number to read
     * @exception IOException
     * @since
     */
    void readPAT(E00Record r, int n) throws IOException {
        int itype = r.getItemIndex(prefix.substring(0, 2) + "PTTYPE");
        int ival = r.getItemIndex(prefix.substring(0, 2) + "PTVAL");
        int ival2 = r.getItemIndex(prefix.substring(0, 2) + "PYTYPE");
        int iname = r.getItemIndex(prefix.substring(0, 2) + "PTNAME");
        // Unused
        // int iflag = r.getItemIndex(prefix.substring(0, 2) + "PTFLAG");

        // System.out.println(itype+" "+iname+" "+iflag);
        for (int i = 0; i < n; i++) {
            r.read(isr);
            BasicLocation bl = (BasicLocation) labs.getOMGraphicAt(i);
            String S = r.getStringField(iname).trim();
            if (bl == null)
                continue;
            if (S.length() > 0) {
                Debug.message("e00", S);
                bl.setName(S);
                bl.setShowName(true);
            } else
                bl.setLabel(null);
            E00Data d = (E00Data) bl.getAppObject();
            if (itype != -1)
                d.type = r.getIntField(itype);
            if (ival != -1)
                d.valeur = r.getIntField(ival);
            if (ival2 != -1)
                d.valeur2 = r.getIntField(ival2);
            else
                d.valeur2 = d.valeur;
            if (itype >= 0)
                setLocationColor(bl, r.getIntField(itype));
        }
    }

    /**
     * read form IFO, AAT records extract data from them and put this data in an
     * E00data structure associated with the graphic object
     * 
     * @param r AAT/IFO structure
     * @param n record number to read
     * @exception IOException
     * @since
     */
    void readAAT(E00Record r, int n) throws IOException {
        OMGraphic og;
        int type = Integer.MIN_VALUE;
        int val = Integer.MIN_VALUE;
        int itype = r.getItemIndex(prefix.substring(0, 2) + "LNTYPE");
        int ival = r.getItemIndex(prefix.substring(0, 2) + "LNVAL");
        int iID = r.getItemIndex(prefix.substring(0, 2) + "NET-ID");
        int ID = -1;
        for (int i = 0; i < n; i++) {
            r.read(isr);
            if ((itype == -1) && (ival == -1))
                continue;
            og = arcs.getOMGraphicAt(i);
            if (itype != -1) {
                type = r.getIntField(itype);
                og.setLinePaint(getArcPaint(type));
            }
            if (ival != -1)
                val = r.getIntField(ival);

            if (iID != -1)
                ID = r.getIntField(iID);

            E00Data data = (E00Data) og.getAppObject();
            data.type = type;
            data.valeur = val;
            data.valeur2 = val;
            data.ID = ID;
        }
    }

    /**
     * Sets the Location Color anf font attributes
     * 
     * @param bl basic location
     * @param t color index
     * @since
     */
    private void setLocationColor(BasicLocation bl, int t) {
        if (bl == null)
            return;
        Paint c = getLabPaint(t);
        OMText label = bl.getLabel();
        if (label != null) {
            if (LabTextColor != null)
                label.setLinePaint(LabTextColor);
            else
                label.setLinePaint(c);
            if (labFont != null)
                label.setFont(labFont);
        }
        bl.setLocationPaint(c);
        if (SelectLabColor != null)
            bl.setSelectPaint(SelectLabColor);
    }

    /**
     * get the arc color associated with a type value .
     * 
     * @param i the type value
     * @return The Color value
     * @since
     */
    private Paint getArcPaint(int i) {
        if (i >= ArcColors.length)
            i = ArcColors.length - 1;
        if (i < 0)
            i = 0;
        return ArcColors[i];
    }

    /**
     * get the lab color associated with a type value .
     * 
     * @param i the type value
     * @return The Color value
     * @since
     */
    private Paint getLabPaint(int i) {
        if (i >= LabColors.length)
            i = LabColors.length - 1;
        if (i < 0)
            i = 0;
        return LabColors[i];
    }

    /**
     * set the itemRecord structure from the data read with infoRecord
     * 
     * @return The itemRecord set
     * @exception IOException
     * @since
     */
    private E00Record getRecord() throws IOException {
        int itemNumber = infoRecord.getIntField(2);
        int positions[] = new int[itemNumber + 1];
        int types[] = new int[itemNumber];
        String[] names = new String[itemNumber];
        for (int i = 0; i < itemNumber; i++) {
            itemRecord.read(isr);
            types[i] = itemRecord.getIntField(7);
            names[i] = itemRecord.getStringField(0).trim();
            int p = positions[i];
            switch (types[i]) {
            case 20:
                positions[i + 1] = p + itemRecord.getIntField(5);
                break;
            case 30:
                positions[i + 1] = p + itemRecord.getIntField(5);
                break;
            case 50:
                positions[i + 1] = p + 11;
                break;
            case 60:
                positions[i + 1] = p + 14;
            }
        }
        return new E00Record(positions, types, names);
    }

    /**
     * Description of the Class
     * 
     * a class used to - describe the structure of records (fixed columns) -
     * parse records according to the structure - deliver data on le last persed
     * record
     * 
     * @author tparicau
     */
    static class E00Record {
        String[] itemName;
        int n;
        int[] fieldType, fieldPosition;
        private String[] stringField;
        private float[] floatField;
        private int[] intField;

        /**
         * Constructor for the E00Record object
         * 
         * @param positions Description of Parameter
         * @param types Description of Parameter
         * @param names Description of Parameter
         * @since
         */
        E00Record(int[] positions, int[] types, String[] names) {
            fieldType = types;
            fieldPosition = positions;
            itemName = names;
            n = positions.length - 1;
            if (types.length < n) {
                n = types.length;
                System.err.println("E00Record err 1");
            }
            stringField = new String[n];
            floatField = new float[n];
            intField = new int[n];
            // print();
        }

        /**
         * delivers the int in fth position
         * 
         * @param f the position
         * @return The int value
         * @since
         */
        int getIntField(int f) {
            if (f < 0)
                return Integer.MIN_VALUE;
            return intField[f];
        }

        /**
         * delivers the float in fth position
         * 
         * @param f the position
         * @return The float value
         * @since
         */
        float getFloatField(int f) {
            if (f < 0)
                return Float.MIN_VALUE;
            return floatField[f];
        }

        /**
         * delivers the String in fth position
         * 
         * @param f the position
         * @return The String
         * @since
         */

        String getStringField(int f) {
            if (f < 0)
                return "";
            return stringField[f];
        }

        /**
         * Gets the ItemIndex attribute of the E00Record object
         * 
         * @param S the name of item
         * @return the index of item
         * @since
         */
        int getItemIndex(String S) {
            if (itemName == null)
                return -1;
            for (int i = 0; i < itemName.length; i++)
                if (itemName[i].equals(S))
                    return i;
            return -1;
        }

        /**
         * read a record according with the structure described in thi E00record
         * 
         * @param isr reader where to read data
         * @exception IOException
         * @since
         */
        void read(BufferedReader isr) throws IOException {
            String Line = isr.readLine();
            if (Line == null)
                return;
            int delta = 0;
            for (int i = 0; i < n; i++) {
                int n1 = fieldPosition[i] - delta;
                int n2 = fieldPosition[i + 1] - delta;
                if (n1 > 80) {
                    delta += 80;
                    n1 -= 80;
                    n2 -= 80;
                    Line = isr.readLine();
                }
                String S;
                if (Line == null) {
                    break;
                }
                if (n1 >= Line.length())
                    S = "";
                else if (n2 < Line.length())
                    S = Line.substring(n1, n2);
                else
                    S = Line.substring(n1);
                if (n2 > 80) {
                    delta += 80;
                    n2 -= 80;
                    Line = isr.readLine();
                    if (Line == null) {
                        break;
                    }
                    if (n2 < Line.length())
                        S += Line.substring(0, n2);
                    else {
                        S = Line;
                        Debug.message("e00", "??" + S);
                    }
                }
                try {
                    switch (fieldType[i]) {
                    case 20:
                        stringField[i] = S;
                        break;
                    case 30:
                    case 50:
                        intField[i] = Integer.parseInt(S.trim());
                        break;
                    case 60:
                        floatField[i] = Float.parseFloat(S.trim());
                    }
                } catch (NumberFormatException e) {
                    if (!Line.startsWith("EOI")) {
                        Debug.message("e00", "E00:parserr " + i + " " + fieldPosition[i] + " "
                                + fieldPosition[i + 1] + " " + S);
                        Debug.message("e00", ">" + Line);
                    }
                }
            }

        }

        /**
         * Print the record structure
         * 
         * @since
         */
        void print() {
            System.out.print("Record ");
            for (int i = 0; i < n + 1; i++)
                System.out.print(fieldPosition[i] + " ");

            System.out.println();
            System.out.print("       ");
            for (int i = 0; i < n; i++)
                System.out.print(fieldType[i] + " ");

            System.out.println();
        }
    }

    /**
     * @return OMGraphicsList of arcs.
     */
    public OMGraphicList getArcList() {
        return arcs;
    }

    /**
     * @return OMGraphicList of lab.
     */
    public OMGraphicList getLabList() {
        return labs;
    }

    /**
     * @return OMGraphicList of Tx7.
     */
    public OMGraphicList getTx7List() {
        return tx7;
    }
}
