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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/e00/E00Layer.java,v $
// $RCSfile: E00Layer.java,v $
// $Revision: 1.8 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.e00;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.bbn.openmap.event.SelectMouseMode;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.location.BasicLocation;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * Description of the Class an Layer showing data from an .e00 file
 * data is extracted with E00Parser class possibilities to filter arcs
 * according to their types or value.
 * <P>
 * 
 * Examples of properties for OpenMap:
 * <P>
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *     
 *      
 *       
 *        
 *         ### E00 layer
 *         e00.class=com.bbn.openmap.layer.e00.E00Layer
 *         e00.prettyName=E00 file
 *         e00.FileName=data/france/hynet.e00
 *         ### E00 layer
 *         es00.class=E00.E00Layer
 *         es00.prettyName=ES00 file
 *         es00.FileName=data/france/rdline.e00
 *         es00.ArcColors= FF0000FF,FFEE5F3C,FFFFCC00,FF339700,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FF666666,FFFFFFFF
 *         es00.ArcVisible=true
 *         es00.LabVisible=false
 *         es00.Tx7Visible=true
 *         es00.LabFont =Arial 10 ITALIC BOLD
 *         ###other properties Tx7Color LabColors SelectTx7Color SelectLabColor SelectArcColor LabTextColor Tx7Font
 *          
 *        
 *       
 *      
 *     
 *    
 *   
 *  
 * </pre>
 * 
 * @since OpenMap 4.5.5
 * @author Thomas Paricaud
 */
public class E00Layer extends OMGraphicHandlerLayer implements ActionListener {

    protected OMGraphicList arcs;
    protected OMGraphicList labs;
    protected OMGraphicList tx7;
    protected boolean ArcVisible = true;
    protected boolean LabVisible = true;
    protected boolean Tx7Visible = true;
    protected JPanel gui;
    protected JLabel label;

    protected Paint[] ArcColors, LabColors;
    protected Paint Tx7Color;
    protected Paint SelectTx7Color;
    protected Paint SelectLabColor;
    protected Paint SelectArcColor;
    protected Paint LabTextColor;
    protected OMGraphic LabMarker;
    protected Font LabFont, Tx7Font;
    protected int filtreValeur = Integer.MIN_VALUE;
    protected int filtreType = Integer.MIN_VALUE;
    protected JFileChooser fileChooser;
    protected File E00File;

    public E00Layer() {
        super();
        setMouseModeIDsForEvents(new String[] { SelectMouseMode.modeID });
    }

    /**
     * OMGraphicHandlerLayer method, get the OMGraphics from the data
     * in the file.
     */
    public synchronized OMGraphicList prepare() {
        OMGraphicList g = getList();

        if (g == null) {
            try {
                E00Parser parser = new E00Parser(E00File);
                parser.setPaints(ArcColors,
                        LabColors,
                        Tx7Color,
                        SelectTx7Color,
                        SelectLabColor,
                        SelectArcColor,
                        LabTextColor);
                parser.setLabMarker(LabMarker);
                parser.setFonts(LabFont, Tx7Font);

                g = parser.getOMGraphics();
                arcs = parser.getArcList();
                labs = parser.getLabList();
                tx7 = parser.getTx7List();

                setListVisibility();

            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                Debug.error("E00Layer|" + getName() + ".prepare(): "
                        + ex.getMessage());
            }
        }

        Projection proj = getProjection();
        if (proj != null && g != null) {
            g.generate(proj);
        }

        return g;
    }

    /**
     * Sets the properties for the <code>Layer</code>. This allows
     * <code>Layer</code> s to get a richer set of parameters than
     * the <code>setArgs</code> method.
     * 
     * @param prefix the token to prefix the property names
     * @param props the <code>Properties</code> object
     * @since
     */
    public void setProperties(String prefix, java.util.Properties props) {
        super.setProperties(prefix, props);
        String E00FileName = props.getProperty(prefix + ".FileName");
        ArcVisible = PropUtils.booleanFromProperties(props, prefix
                + ".ArcVisible", ArcVisible);
        LabVisible = PropUtils.booleanFromProperties(props, prefix
                + ".LabVisible", LabVisible);
        Tx7Visible = PropUtils.booleanFromProperties(props, prefix
                + ".Tx7Visible", Tx7Visible);

        Paint dfault = null;

        ArcColors = parseColors(props, prefix, "ArcColors", Color.black);
        LabColors = parseColors(props, prefix, "LabColors", Color.black);
        Tx7Color = parseColor(props, prefix, "Tx7Color", dfault);
        SelectTx7Color = parseColor(props, prefix, "SelectTx7Color", null);
        SelectLabColor = parseColor(props, prefix, "SelectLabColor", null);
        SelectArcColor = parseColor(props, prefix, "SelectArcColor", null);
        LabTextColor = parseColor(props, prefix, "LabTextColor", null);
        LabFont = parseFont(props, prefix, "LabFont", null);
        Tx7Font = parseFont(props, prefix, "tx7Font", null);

        try {
            openFile(new File(E00FileName));
        } catch (Exception ex) {
            Debug.error("E00Layer: error - " + ex.getMessage());
            if (Debug.debugging("e00")) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gets the GUI attribute of the E00Layer object
     * 
     * @return The GUI value
     * @since
     */
    public Component getGUI() {
        if (gui == null) {
            gui = PaletteHelper.createPaletteJPanel("E00");
            label = new JLabel((E00File != null) ? E00File.getName()
                    : "       ");
            gui.add(label);
            addToGUI(gui, new JCheckBox("Arcs", ArcVisible), "ARCS");
            addToGUI(gui, new JCheckBox("Points", LabVisible), "LABS");
            addToGUI(gui, new JCheckBox("Tx7", Tx7Visible), "TX7");
            gui.add(new JLabel("Filter"));
            gui.add(new JLabel(" By Value"));
            addToGUI(gui, new JTextField(10), "VALEUR");
            gui.add(new JLabel(" By Type"));
            addToGUI(gui, new JTextField(10), "TYPE");
            gui.add(new JSeparator());
            addToGUI(gui, new JButton("Open File"), "OPEN");
            addToGUI(gui, new JButton("Export Arcs"), "ExpArcs");
            addToGUI(gui, new JButton("Export Points"), "ExpPoints");
        }
        return gui;
    }

    /**
     * Adds a feature to the GUI attribute of the E00Layer object
     * 
     * @param b The feature to be added to the GUI attribute
     * @param cmd The feature to be added to the GUI attribute
     * @since
     */
    protected void addToGUI(JPanel gui, AbstractButton b, String cmd) {
        b.setActionCommand(cmd);
        b.addActionListener(this);
        gui.add(b);
    }

    /**
     * Adds a feature to the Gui attribute of the E00Layer object
     * 
     * @param b The feature to be added to the Gui attribute
     * @param cmd The feature to be added to the Gui attribute
     * @since
     */
    protected void addToGUI(JPanel gui, JTextField b, String cmd) {
        b.setActionCommand(cmd);
        b.addActionListener(this);
        gui.add(b);
    }

    /**
     * Description of the Method
     * 
     * @param e Description of Parameter
     * @since
     */
    public void actionPerformed(ActionEvent e) {
        String Action = e.getActionCommand();
        if ("OPEN".equals(Action)) {
            openFile(chooseFile(0));
            doPrepare();
        } else if ("ExpArcs".equals(Action)) {
            exportArcs1();
        } else if ("ExpPoints".equals(Action)) {
            exportPoints();
        } else if ("VALEUR".equals(Action)) {
            filtreValeur = getVal(e);
            filtre();
        } else if ("TYPE".equals(Action)) {
            filtreType = getVal(e);
            filtre();
        } else if ("ARCS".equals(Action)) {
            JCheckBox cb = (JCheckBox) e.getSource();
            ArcVisible = cb.isSelected();
            if (arcs != null)
                arcs.setVisible(ArcVisible);
            repaint();
        } else if ("LABS".equals(Action)) {
            JCheckBox cb = (JCheckBox) e.getSource();
            LabVisible = cb.isSelected();
            if (labs != null)
                labs.setVisible(LabVisible);
            repaint();
        } else if ("TX7".equals(Action)) {
            JCheckBox cb = (JCheckBox) e.getSource();
            Tx7Visible = cb.isSelected();
            if (tx7 != null)
                tx7.setVisible(Tx7Visible);
            repaint();
        }
    }

    public String getInfoText(OMGraphic omg) {
        String t = "";
        if (arcs != null && arcs.contains(omg)) {
            t = "arcs";
        } else if (labs != null && labs.contains(omg)) {
            t = "point";
        }

        E00Data d = (E00Data) omg.getAppObject();
        return t + d;
    }

    /**
     * Gets the Frame attribute of the E00Layer object
     * 
     * @return The Frame value
     * @since
     */
    protected Frame getFrame() {
        if (gui == null) {
            return null;
        }

        for (Container p = gui.getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }

        return null;
    }

    /**
     * Sets the LineColor attribute of the E00Layer object
     * 
     * @param C The new LineColor value
     * @since
     */
    void setLineColor(Color C) {
        OMGraphicList graphics = getList();
        if (graphics != null) {
            graphics.setLinePaint(C);
        }
        repaint();
    }

    /**
     * Gets the Val attribute of the E00Layer object
     * 
     * @param e Description of Parameter
     * @return The Val value
     * @since
     */
    int getVal(ActionEvent e) {
        int val;
        JTextField T = (JTextField) e.getSource();
        try {
            val = Integer.parseInt(T.getText());
        } catch (NumberFormatException ex) {
            val = Integer.MIN_VALUE;
        }
        return val;
    }

    public void setListVisibility() {

        if (arcs != null) {
            arcs.setVisible(ArcVisible);
        }

        if (labs != null) {
            labs.setVisible(LabVisible);
        }

        if (tx7 != null) {
            tx7.setVisible(Tx7Visible);
        }

    }

    /**
     * Description of the Method
     * 
     * @param f Description of Parameter
     * @since
     */
    protected void openFile(File f) {

        if (f == null) {
            return;
        }

        if (!f.exists()) {
            Debug.output("E00|" + getName() + ": missing file");
            return;
        }

        E00File = f;

        if (gui != null) {
            label.setText(E00File.getName());
        }
    }

    /**
     * Description of the Method
     * 
     * @param type Description of Parameter
     * @return Description of the Returned Value
     * @since
     */
    protected File chooseFile(int type) {
        Frame frame = getFrame();
        File f = null;
        if (fileChooser == null)
            fileChooser = new JFileChooser();

        fileChooser.setCurrentDirectory(E00File);
        if (type == 0)
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        else
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int retval = fileChooser.showDialog(frame, null);
        if (retval == JFileChooser.APPROVE_OPTION)
            f = fileChooser.getSelectedFile();

        return f;
    }

    /**
     * Description of the Method
     * 
     * @since
     */
    protected void filtre() {
        System.out.print("E00: filter type=" + filtreType + "  value="
                + filtreValeur);
        OMGraphic og;
        Object O;
        int count = 0;
        OMGraphicList g = arcs;
        int n = arcs.size();
        if ((filtreType == Integer.MIN_VALUE)
                && (filtreValeur == Integer.MIN_VALUE)) {
            for (int i = 0; i < n; i++)
                g.getOMGraphicAt(i).setVisible(true);
            count = n;
        } else
            for (int i = 0; i < n; i++) {
                og = g.getOMGraphicAt(i);
                O = og.getAppObject();
                if (O == null)
                    setVisible(false);
                else {
                    E00Data d = (E00Data) O;
                    boolean r = true;
                    if (filtreType != Integer.MIN_VALUE)
                        r &= (filtreType == d.type);
                    if (filtreValeur != Integer.MIN_VALUE)
                        r &= (filtreValeur == d.valeur);
                    og.setVisible(r);
                    if (r)
                        count++;
                }
            }

        Debug.output(" count:" + count);
        repaint();
    }

    /**
     * Description of the Method
     * 
     * @since
     */
    protected void exportArcs() {
        if (arcs == null)
            return;

        Vector V = new Vector();
        int n = arcs.size();
        double ll[];
        int llsize;
        double lnmax = Double.MIN_VALUE;
        double lnmin = Double.MAX_VALUE;
        double ltmin = lnmin;
        double ltmax = lnmax;
        double lt;
        double ln;
        for (int i = 0; i < n; i++) {
            OMPoly oj = (OMPoly) arcs.getOMGraphicAt(i);
            if (oj == null)
                continue;
            E00Data data = (E00Data) oj.getAppObject();
            if (data == null)
                continue;
            ll = oj.getLatLonArray();
            llsize = ll.length;
            if ((ll[0] != ll[llsize - 2]) || (ll[1] != ll[llsize - 1])) {
                // contour non clos;
                double[] coords = new double[] { ll[0], ll[1], ll[llsize - 2],
                        ll[llsize - 1] };
                ArcData dn = new ArcData(data);
                dn.coords = coords;
                oj.setAppObject(dn);
                V.add(dn);
            }
            int k = 0;
            while (k < llsize) {
                lt = ll[k++];
                ln = ll[k++];
                if (lt > ltmax)
                    ltmax = lt;
                else if (lt < ltmin)
                    ltmin = lt;
                if (ln > lnmax)
                    lnmax = ln;
                else if (ln < lnmin)
                    lnmin = ln;
            }
        }
        System.out.println("#minmax " + lnmin + " " + lnmax + " " + ltmin + " "
                + ltmax);
        int unClosedCount = V.size();
        ArcData[] unClosed = (ArcData[]) V.toArray(new ArcData[unClosedCount]);

        V.clear();
        V = null;
        //chercher les connections;
        ArcData d0;
        //chercher les connections;
        ArcData d1;
        //chercher les connections;
        ArcData d2;
        //chercher les connections;
        ArcData dx;
        double lt1;
        double lg1;
        double lt2;
        double lg2;
        for (int i = 0; i < unClosedCount; i++) {
            d0 = unClosed[i];
            d0.deja = 0;
            if (d0.type > 1)
                continue;
            lt1 = d0.coords[0];
            lg1 = d0.coords[1];
            lt2 = d0.coords[2];
            lg2 = d0.coords[3];
            for (int j = i + 1; j < unClosedCount; j++) {
                d1 = unClosed[j];
                if (d1.type > 1)
                    continue;
                if ((lt1 == d1.coords[0]) && (lg1 == d1.coords[1])) {
                    d1.setC(d0);
                    d0.setC(d1);
                } else if ((lt1 == d1.coords[2]) && (lg1 == d1.coords[3])) {
                    d1.setF(d0);
                    d0.setC(d1);
                }
                if ((lt2 == d1.coords[0]) && (lg2 == d1.coords[1])) {
                    d1.setC(d0);
                    d0.setF(d1);
                } else if ((lt2 == d1.coords[2]) && (lg2 == d1.coords[3])) {
                    d1.setF(d0);
                    d0.setF(d1);
                }
            }
        }
        for (int k = 0; k < unClosedCount; k++) {
            d0 = unClosed[k];
            if ((d0.type != 0) || (d0.deja != 0))
                continue;
            if ((d0.c0 == null) && (d0.f0 == null))
                continue;
            d1 = d0;
            d2 = (d1.c0 == null) ? d1.f0 : d1.c0;
            System.out.print("#contour ");
            System.out.print(d0.id);
            System.out.print(' ');
            int decount = unClosedCount * 3 / 2;
            do {
                System.out.print(d2.id);
                if (d2.deja != 0)
                    System.out.print('*');
                System.out.print(' ');
                dx = d2.visit(d1);
                d1 = d2;
                d2 = dx;
                if (decount-- < 0) {
                    System.out.print(" BOUCLE ");
                    break;
                }
            } while ((d2 != null) && (d2 != d0));
            if (d2 == null)
                System.out.print(" unclosed");
            System.out.println();
            System.out.println();
        }

        for (int i = 0; i < n; i++) {
            OMPoly oj = (OMPoly) arcs.getOMGraphicAt(i);
            if (oj == null)
                continue;
            E00Data data = (E00Data) oj.getAppObject();
            if (data == null)
                continue;
            ll = oj.getLatLonArray();
            llsize = ll.length;
            double z = data.valeur * 0.304;
//            double z2 = data.valeur2 * 0.304;
            boolean closed = true;
//            float[] coords = null;
            if (data instanceof ArcData) {
                ArcData dn = (ArcData) data;
//                coords = dn.coords;
                dn.coords = null;
                closed = false;
            }
            int k = 0;
            System.out.println("#type " + data.type);
            while (k < llsize) {
                lt = ll[k++];
                ln = ll[k++];
                System.out.println("c " + ln + " " + lt + " " + z);
            }
            System.out.println((closed) ? "#LC" : "#LX");
            System.out.println();
        }

    }

    /**
     * Description of the Method
     * 
     * @since
     */
    protected void exportArcs1() {
        PrintStream out = null;
        double lt;
        double ln;
        double ll[];
        int llsize;
        int n;
        File f = chooseFile(1);
        if (arcs == null)
            return;
        if (f == null)
            return;
        try {
            out = new PrintStream(new FileOutputStream(f));
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        n = arcs.size();
        for (int i = 0; i < n; i++) {
            OMPoly oj = (OMPoly) arcs.getOMGraphicAt(i);
            if (oj == null)
                continue;
            if (!oj.isSelected())
                continue;
            ll = oj.getLatLonArray();
            llsize = ll.length;
            out.println(oj.getLinePaint());
            if ((ll[0] != ll[llsize - 2]) || (ll[1] != ll[llsize - 1]))
                out.print("MetaPolyline: ");
            else {
                llsize -= 2;
                out.print("MetaPolyline: ");
            }
            out.print(llsize / 2);
            Object o = oj.getAppObject();
            if (o instanceof String)
                System.out.println(o);
            else
                out.println();
            int k = 0;
            while (k < llsize) {
                lt = ll[k++];
                ln = ll[k++];
                out.println(ln + " " + lt);
            }
        }
        out.close();

    }

    /**
     * Description of the Method
     * 
     * @since
     */
    protected void exportPoints() {
        if (labs == null)
            return;
        //OMGraphic oj ;
        BasicLocation oj;
        double lt;
        double ln;
        double z;
        int n = labs.size();
        System.out.println("#type 0");
        for (int i = 0; i < n; i++) {
            oj = (BasicLocation) labs.getOMGraphicAt(i);
            if (oj == null)
                continue;
            E00Data data = (E00Data) oj.getAppObject();
            if (data == null)
                continue;
            lt = Math.toRadians(oj.lat);
            ln = Math.toRadians(oj.lon);
            z = data.valeur * 0.304;
            System.out.println("c " + ln + " " + lt + " " + z);
        }

    }

    /**
     * Description of the Method
     * 
     * @param props Description of Parameter
     * @param prefix Description of Parameter
     * @param prop Description of Parameter
     * @param dfault Description of Parameter
     * @return Description of the Returned Value
     * @since
     */
    protected Paint parseColor(Properties props, String prefix, String prop,
                               Paint dfault) {
        try {
            return PropUtils.parseColorFromProperties(props, prefix + "."
                    + prop, dfault);
        } catch (NumberFormatException exc) {
            System.out.println("Color Error " + prefix + "." + prop);
        }
        return dfault;
    }

    /**
     * Description of the Method
     * 
     * @param props Description of Parameter
     * @param prefix Description of Parameter
     * @param prop Description of Parameter
     * @param err Description of Parameter
     * @return Description of the Returned Value
     * @since
     */
    protected Paint[] parseColors(Properties props, String prefix, String prop,
                                  Paint err) {
        Paint[] colors = null;
        String[] colorStrings = PropUtils.stringArrayFromProperties(props,
                prefix + "." + prop,
                " ,");
        if (colorStrings != null) {
            colors = new Color[colorStrings.length];
            for (int i = 0; i < colorStrings.length; i++)
                try {
                    colors[i] = PropUtils.parseColor(colorStrings[i]);
                } catch (NumberFormatException exc) {
                    System.out.println("Colors Error " + prefix + "." + prop
                            + " " + i);
                    colors[i] = err;
                }

        }
        return colors;
    }

    /**
     * Description of the Method
     * 
     * @param props Description of Parameter
     * @param prefix Description of Parameter
     * @param prop Description of Parameter
     * @param dfault Description of Parameter
     * @return Description of the Returned Value
     * @since
     */
    protected Font parseFont(Properties props, String prefix, String prop,
                             Font dfault) {
        String[] fontItems = PropUtils.stringArrayFromProperties(props, prefix
                + "." + prop, " ,");
        int style = 0;
        int size = 10;
        if (fontItems == null || fontItems.length == 0)
            return dfault;
        try {
            size = Integer.parseInt(fontItems[1]);
        } catch (Exception e) {
        }

        for (int i = 2; i < fontItems.length; i++) {
            String S = fontItems[i];
            if ("BOLD".equals(S))
                style |= Font.BOLD;
            else if ("ITALIC".equals(S))
                style |= Font.ITALIC;
            else if ("PLAIN".equals(S))
                style |= Font.PLAIN;
        }
        return new Font(fontItems[0], style, size);
    }

}

