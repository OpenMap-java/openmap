package com.bbn.openmap.layer.e00;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.InfoDisplayEvent;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.event.MapMouseListener;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.layer.location.BasicLocation;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


/**
 * Description of the Class an Layer showing data from an .e00 file
 * data is extracted with E00Parser class possibilities to filter arcs
 * according to their types or value. <P>
 *
 * Examples of properties for OpenMap:<P><pre>
 * ### E00 layer
 * e00.class=E00.E00Layer
 * e00.prettyName=E00 file
 * e00.FileName=data/france/hynet.e00
 * ### E00 layer
 * es00.class=E00.E00Layer
 * es00.prettyName=ES00 file
 * es00.FileName=data/france/rdline.e00
 * es00.ArcColors= FF0000FF ,FFEE5F3C, FFFFCC00,FF339700,FFFFFFFF,FFFFFFFF,FFFFFFFF,FFFFFFFF,FF666666,FFFFFFFF
 * es00.ArcVisible=true
 * es00.LabVisible=false
 * es00.Tx7Visible=true
 * es00.LabFont =Arial 10 ITALIC BOLD
 * ###other properties Tx7Color LabColors SelectTx7Color SelectLabColor SelectArcColor LabTextColor Tx7Font
 *  </pre>
 * @since OpenMap 4.5.5
 * @author Thomas Paricaud
 */
public class E00Layer extends Layer implements MapMouseListener, ActionListener {

    protected OMGraphicList graphics, arcs, labs, tx7;
    protected boolean ArcVisible = true, LabVisible = true, Tx7Visible = true;
    protected boolean cancelled = false;
    protected JPanel gui;
    protected JLabel label;

    Paint[] ArcColors, LabColors;
    Paint Tx7Color;
    Paint SelectTx7Color, SelectLabColor, SelectArcColor, LabTextColor;
    OMGraphic LabMarker;
    Font LabFont, Tx7Font;
    int filtreValeur = Integer.MIN_VALUE;
    int filtreType = Integer.MIN_VALUE;
    JFileChooser fileChooser;
    File E00File;
    E00Worker currentWorker;

    private Projection projection;

    public E00Layer() {
	super();
    }


    /**
     * Sets the properties for the <code>Layer</code>.  This allows
     * <code>Layer</code>s to get a richer set of parameters than the
     * <code>setArgs</code> method.
     *
     * @param  prefix  the token to prefix the property names
     * @param  props   the <code>Properties</code> object
     * @since
     */
    public void setProperties(String prefix, java.util.Properties props) {
	super.setProperties(prefix, props);
	String E00FileName = props.getProperty(prefix + ".FileName");
	ArcVisible = LayerUtils.booleanFromProperties(props, prefix + ".ArcVisible", ArcVisible);
	LabVisible = LayerUtils.booleanFromProperties(props, prefix + ".LabVisible", LabVisible);
	Tx7Visible = LayerUtils.booleanFromProperties(props, prefix + ".Tx7Visible", Tx7Visible);

	Paint dfault = null;
	OMGraphic LabMarker;
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
     *  Sets the Cancelled attribute of the E00Layer object
     *
     * @param  set  The new Cancelled value
     * @since
     */
    public synchronized void setCancelled(boolean set) {
	cancelled = set;
    }


    /**
     *  Gets the Cancelled attribute of the E00Layer object
     *
     * @return    The Cancelled value
     * @since
     */
    public synchronized boolean isCancelled() {
	return cancelled;
    }


    /**
     *  Gets the GUI attribute of the E00Layer object
     *
     * @return    The GUI value
     * @since
     */
    public Component getGUI() {
	if (gui == null) {
	    gui = PaletteHelper.createPaletteJPanel("E00");
	    label = new JLabel((E00File != null) ? E00File.getName() : "       ");
	    gui.add(label);
	    addGui(new JButton("OPEN"), "OPEN");
	    addGui(new JCheckBox("Arcs", ArcVisible), "ARCS");
	    addGui(new JCheckBox("Labs", LabVisible), "LABS");
	    addGui(new JCheckBox("Tx7", Tx7Visible), "TX7");
	    gui.add(new JLabel("Filter"));
	    gui.add(new JLabel(" By Value"));
	    addGui(new JTextField(10), "VALEUR");
	    gui.add(new JLabel(" By Type"));
	    addGui(new JTextField(10), "TYPE");
	    addGui(new JButton("ExpArcs"), "ExpArcs");
	    addGui(new JButton("ExpPoints"), "ExpPoints");
	}
	return gui;
    }



    /**
     *  Gets the MapMouseListener attribute of the E00Layer object
     *
     * @return    The MapMouseListener value
     * @since
     */
    public MapMouseListener getMapMouseListener() {
	return this;
    }


    //----------------------------------------------------------------------
    // MapMouseListener interface implementation
    //----------------------------------------------------------------------


    /**
     *  Gets the MouseModeServiceList attribute of the E00Layer object
     *
     * @return    The MouseModeServiceList value
     * @since
     */
    public String[] getMouseModeServiceList() {
	String[] ret = new String[1];
	ret[0] = new String("Gestures");
	return ret;
    }


    /**
     * Invoked when the projection has changed or this Layer has been
     * added to the MapBean.
     *
     * @param  e  ProjectionEvent
     * @since
     */
    public void projectionChanged(ProjectionEvent e) {
	//System.out.print("E00: Projection change");
	projection = e.getProjection();
	if (graphics != null)
	    graphics.generate(projection);
	repaint();

	if (projection == null && Debug.debugging("e00")) {
	    Debug.output("E00Layer.projectionChanged(): projection null");
	}

    }


    /**
     * Paints the layer.
     *
     * @param  g  the Graphics context for painting
     * @since
     */
    public void paint(Graphics g) {
	if (graphics != null)
	    graphics.render(g);
    }


    /**
     *  Description of the Method
     *
     * @param  cont  Description of Parameter
     * @since
     */
    public void added(Container cont) {
	fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @since
     */
    public void actionPerformed(ActionEvent e) {
	String Action = e.getActionCommand();
	if ("OPEN".equals(Action))
	    openFile(chooseFile(0));
	else if ("ExpArcs".equals(Action))
	    exportArcs1();
	else if ("ExpPoints".equals(Action))
	    exportPoints();
	else if ("VALEUR".equals(Action)) {
	    filtreValeur = getVal(e);
	    filtre();
	}
	else if ("TYPE".equals(Action)) {
	    filtreType = getVal(e);
	    filtre();
	}
	else if ("ARCS".equals(Action)) {
	    JCheckBox cb = (JCheckBox) e.getSource();
	    ArcVisible = cb.isSelected();
	    if (arcs != null)
		arcs.setVisible(ArcVisible);
	    repaint();
	}
	else if ("LABS".equals(Action)) {
	    JCheckBox cb = (JCheckBox) e.getSource();
	    LabVisible = cb.isSelected();
	    if (labs != null)
		labs.setVisible(LabVisible);
	    repaint();
	}
	else if ("TX7".equals(Action)) {
	    JCheckBox cb = (JCheckBox) e.getSource();
	    Tx7Visible = cb.isSelected();
	    if (tx7 != null)
		tx7.setVisible(Tx7Visible);
	    repaint();
	}
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    public boolean mousePressed(MouseEvent e) {
	int x = e.getX();
	int y = e.getY();
	OMGraphic g = null;
	String t;

// 	if (e.getButton() == MouseEvent.BUTTON1) { // jdk 1.4
	if ((e.getModifiers() & InputEvent.BUTTON1_MASK) > 0) {
	    if (arcs != null)
		g = arcs.findClosest(x, y, 10f);
	    t = "arc";
	}
	else {
	    if (labs != null)
		g = labs.findClosest(x, y, 5f);
	    t = "point";
	}
	if (g != null) {
	    g.setSelected(!g.isSelected());
	    E00Data d = (E00Data) g.getAppObject();
	    if (d != null)
		fireRequestInfoLine(t + d);
	    else
		fireRequestInfoLine("");
	    repaint();
	    return true;
	}
	fireRequestInfoLine("");
	return false;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    public boolean mouseReleased(MouseEvent e) {
	return false;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    public boolean mouseClicked(MouseEvent e) {
	return false;
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @since
     */
    public void mouseEntered(MouseEvent e) {
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @since
     */
    public void mouseExited(MouseEvent e) {
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    public boolean mouseDragged(MouseEvent e) {
	return false;
    }


    /**
     *  Description of the Method
     *
     * @since
     */
    public void mouseMoved() {
    }


    /**
     *  Description of the Method
     *
     * @param  e  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    public boolean mouseMoved(MouseEvent e) {
	return false;
    }


    /**
     *  Gets the Frame attribute of the E00Layer object
     *
     * @return    The Frame value
     * @since
     */
    protected Frame getFrame() {
	if (gui == null)
	    return null;
	for (Container p = gui.getParent(); p != null; p = p.getParent())
	    if (p instanceof Frame)
		return (Frame) p;

	return null;
    }


    /**
     *  Description of the Method
     *
     * @param  worker  Description of Parameter
     * @since
     */
    protected synchronized void workerComplete(E00Worker worker) {
	if (!isCancelled()) {
	    currentWorker = null;
	    graphics = (OMGraphicList) worker.get();
	    if (projection != null)
		graphics.generate(projection);
	    repaint();
	    //System.out.print("E00 worker complete");
	    OMGraphic og = graphics.getOMGraphicWithAppObject("ARCS");
	    if (og != null) {
		arcs = (OMGraphicList) og;
		arcs.setVisible(ArcVisible);
	    }
	    else
		arcs = null;
	    og = graphics.getOMGraphicWithAppObject("LABS");
	    if (og != null) {
		labs = (OMGraphicList) og;
		labs.setVisible(LabVisible);
	    }
	    else
		labs = null;
	    og = graphics.getOMGraphicWithAppObject("TX7");
	    if (og != null) {
		tx7 = (OMGraphicList) og;
		tx7.setVisible(Tx7Visible);
	    }
	    else
		tx7 = null;
	}
	else {
	    setCancelled(false);
	    currentWorker = new E00Worker(E00File);
	    currentWorker.execute();
	}
    }


    /**
     *  Sets the LineColor attribute of the E00Layer object
     *
     * @param  C  The new LineColor value
     * @since
     */
    void setLineColor(Color C) {
	if (graphics != null)
	    graphics.setLinePaint(C);
	repaint();
    }


    /**
     *  Gets the Val attribute of the E00Layer object
     *
     * @param  e  Description of Parameter
     * @return    The Val value
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


    /**
     *  Description of the Method
     *
     * @param  f  Description of Parameter
     * @return    Description of the Returned Value
     * @since
     */
    OMGraphicList prepare(File f) {
	OMGraphicList g = null;
	try {
	    if (isCancelled())
		return null;
	    E00Parser SP = new E00Parser(E00File);
	    SP.setPaints(ArcColors, LabColors, Tx7Color,
			 SelectTx7Color, SelectLabColor, SelectArcColor,
			 LabTextColor);
	    SP.setLabMarker(LabMarker);
	    SP.setFonts(LabFont, Tx7Font);
	    g = SP.getOMGraphics();
	    if (isCancelled())
		return null;
	} catch (Exception ex) {
	    ex.printStackTrace(System.out);
	    Debug.error("E00Layer: " + ex.getMessage());
	}
	return g;
    }


    /**
     *  Description of the Method
     *
     * @param  f  Description of Parameter
     * @since
     */
    void openFile(File f) {
	if (f == null)
	    return;
	if (!f.exists()) {
	    Debug.output("E00: missing file");
	    return;
	}
	E00File = f;
	if (gui != null)
	    label.setText(E00File.getName());
	if (currentWorker == null) {
	    fireStatusUpdate(LayerStatusEvent.START_WORKING);
	    currentWorker = new E00Worker(f);
	    currentWorker.execute();
	}
	else
	    setCancelled(true);
    }


    /**
     *  Adds a feature to the Gui attribute of the E00Layer object
     *
     * @param  b    The feature to be added to the Gui attribute
     * @param  cmd  The feature to be added to the Gui attribute
     * @since
     */
    void addGui(AbstractButton b, String cmd) {
	b.setActionCommand(cmd);
	b.addActionListener(this);
	gui.add(b);
    }


    /**
     *  Adds a feature to the Gui attribute of the E00Layer object
     *
     * @param  b    The feature to be added to the Gui attribute
     * @param  cmd  The feature to be added to the Gui attribute
     * @since
     */
    void addGui(JTextField b, String cmd) {
	b.setActionCommand(cmd);
	b.addActionListener(this);
	gui.add(b);
    }


    /**
     *  Description of the Method
     *
     * @param  type  Description of Parameter
     * @return       Description of the Returned Value
     * @since
     */
    File chooseFile(int type) {
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
     *  Description of the Method
     *
     * @since
     */
    void filtre() {
	System.out.print("E00: filter type=" + filtreType + "  value=" + filtreValeur);
	OMGraphic og;
	Object O;
	int count = 0;
	OMGraphicList g = arcs;
	int n = arcs.size();
	if ((filtreType == Integer.MIN_VALUE) && (filtreValeur == Integer.MIN_VALUE)) {
	    for (int i = 0; i < n; i++)
		g.getOMGraphicAt(i).setVisible(true);
	    count = n;
	}
	else
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
     *  Description of the Method
     *
     * @since
     */
    void exportArcs() {
	if (arcs == null)
	    return;
	OMGraphic og;

	Vector V = new Vector();
	int n = arcs.size();
	float ll[];
	int llsize;
	double lnmax = Double.MIN_VALUE;
	double
	    lnmin = Double.MAX_VALUE;
	double
	    ltmin = lnmin;
	double
	    ltmax = lnmax;
	double
	    lt;
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
	    boolean closed = true;
	    if ((ll[0] != ll[llsize - 2]) || (ll[1] != ll[llsize - 1])) {
		// contour non clos;
		float[] coords = new float[]{ll[0], ll[1], ll[llsize - 2], ll[llsize - 1]};
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
	System.out.println("#minmax " + lnmin + " " + lnmax + " " + ltmin + " " + ltmax);
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
	float lt1;
	float lg1;
	float lt2;
	float lg2;
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
		}
		else if ((lt1 == d1.coords[2]) && (lg1 == d1.coords[3])) {
		    d1.setF(d0);
		    d0.setC(d1);
		}
		if ((lt2 == d1.coords[0]) && (lg2 == d1.coords[1])) {
		    d1.setC(d0);
		    d0.setF(d1);
		}
		else if ((lt2 == d1.coords[2]) && (lg2 == d1.coords[3])) {
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
	    double z2 = data.valeur2 * 0.304;
	    boolean closed = true;
	    float[] coords = null;
	    if (data instanceof ArcData) {
		ArcData dn = (ArcData) data;
		coords = dn.coords;
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
     *  Description of the Method
     *
     * @since
     */
    void exportArcs1() {
	OMGraphic og;
	PrintStream out = null;
	double lt;
	double ln;
	float ll[];
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
	    boolean closed = true;
	    out.println(oj.getLinePaint());
	    if ((ll[0] != ll[llsize - 2]) || (ll[1] != ll[llsize - 1]))
		out.print("MetaPolyline: ");
	    else {
		llsize -= 2;
		out.print("MetaPolyline: ");
	    }
	    out.print(llsize / 2);
	    Object o = oj.getAppObject();
	    if (o != null && o instanceof String)
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
     *  Description of the Method
     *
     * @since
     */
    void exportPoints() {
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
	    lt = oj.lat / 180 * Math.PI;
	    ln = oj.lon / 180 * Math.PI;
	    z = data.valeur * 0.304;
	    System.out.println("c " + ln + " " + lt + " " + z);
	}

    }


    /**
     *  Description of the Method
     *
     * @param  props   Description of Parameter
     * @param  prefix  Description of Parameter
     * @param  prop    Description of Parameter
     * @param  dfault  Description of Parameter
     * @return         Description of the Returned Value
     * @since
     */
    private Paint parseColor(Properties props, String prefix, String prop, Paint dfault) {
	try {
	    return LayerUtils.parseColorFromProperties(props, prefix + "." + prop, dfault);
	} catch (NumberFormatException exc) {
	    System.out.println("Color Error " + prefix + "." + prop);
	}
	return dfault;
    }


    /**
     *  Description of the Method
     *
     * @param  props   Description of Parameter
     * @param  prefix  Description of Parameter
     * @param  prop    Description of Parameter
     * @param  err     Description of Parameter
     * @return         Description of the Returned Value
     * @since
     */
    private Paint[] parseColors(Properties props, String prefix, String prop, Paint err) {
	Paint[] colors = null;
	String[] colorStrings = LayerUtils.stringArrayFromProperties(props, prefix + "." + prop, " ,");
	if (colorStrings != null) {
	    colors = new Color[colorStrings.length];
	    for (int i = 0; i < colorStrings.length; i++)
		try {
		    colors[i] = LayerUtils.parseColor(colorStrings[i]);
		} catch (NumberFormatException exc) {
		    System.out.println("Colors Error " + prefix + "." + prop + " " + i);
		    colors[i] = err;
		}

	}
	return colors;
    }


    /**
     *  Description of the Method
     *
     * @param  props   Description of Parameter
     * @param  prefix  Description of Parameter
     * @param  prop    Description of Parameter
     * @param  dfault  Description of Parameter
     * @return         Description of the Returned Value
     * @since
     */
    private Font parseFont(Properties props, String prefix, String prop, Font dfault) {
	String[] fontItems = LayerUtils.stringArrayFromProperties(props, prefix + "." + prop, " ,");
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


    /**
     *  Description of the Class
     *
     * @author     tparicau
     * @created    19 décembre 2002
     */
    class E00Worker extends SwingWorker {
	File file;


	/**
	 * Constructor used to create a worker thread.
	 *
	 * @param  f  Description of Parameter
	 * @since
	 */
	public E00Worker(File f) {
	    super();
	    file = f;
	}


	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 *
	 * @return    Description of the Returned Value
	 * @since
	 */
	public Object construct() {
	    Debug.message("e00", getName() + "|E00Worker.construct()");
	    fireStatusUpdate(LayerStatusEvent.START_WORKING);
	    try {
		return prepare(file);
	    } catch (OutOfMemoryError e) {
		String msg = getName() + "|E00Layer.E00Worker.construct(): " + e;
		Debug.error(msg);
		fireRequestMessage(new InfoDisplayEvent(this, msg));
		fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
		return null;
	    }
	}


	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.
	 *
	 * @since
	 */
	public void finished() {
	    workerComplete(this);
	    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
	}
    }

}

