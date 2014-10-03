// **********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: GeoIntersectionLayer.java,v $
//$Revision: 1.7 $
//$Date: 2009/01/21 01:24:42 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.event.MapMouseEvent;
import com.bbn.openmap.geo.BoundaryCrossing;
import com.bbn.openmap.geo.BoundingCircle;
import com.bbn.openmap.geo.ExtentIndex;
import com.bbn.openmap.geo.ExtentIndexImpl;
import com.bbn.openmap.geo.Geo;
import com.bbn.openmap.geo.GeoPath;
import com.bbn.openmap.geo.GeoPoint;
import com.bbn.openmap.geo.GeoRegion;
import com.bbn.openmap.geo.GeoSegment;
import com.bbn.openmap.geo.Intersection;
import com.bbn.openmap.layer.editor.EditorLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRaster;
import com.bbn.openmap.omGraphics.OMTextLabeler;
import com.bbn.openmap.omGraphics.SinkGraphic;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * This layer demonstrates the use of the com.bbn.openmap.geo package to do
 * intersection calculations in lat/lon space. It allows you to load shape files
 * for sample data sets, and then draw lines, polygons and points on the map to
 * as test cases for intersections on the sample data sets. The ToolPanel will
 * hold controls for choosing what kind of things to draw, and how they should
 * be rendered. The palette for this layer controls the sample data sets,
 * letting you add and remove data files and change their colors.
 * <P>
 * 
 * If you draw a line, polyline or point, the shapes in the data sets that
 * intersect with them will be rendered in the 'select' colors. If you draw a
 * closed polygon with a fill color, the data set shapes inside the polygon will
 * also be selected. The palette has controls for showing the actual points of
 * intersection for paths and their sample data regions. There is also an option
 * to allow mouse clicks on a data set region to create an image over the
 * bounding rectangle for that region, checking the Geo point intersection
 * algorithm against the Java 2D algorithm for the shape in projected pixel
 * space. An all-green image is good, pixels where the algorithms differ will be
 * red.
 * <P>
 * 
 * The properties for this layer are:
 * 
 * <pre>
 *        geo.class=com.bbn.openmap.layer.test.GeoIntersectionLayer
 *        geo.prettyName=GEO Intersections
 *        geo.editor=com.bbn.openmap.layer.editor.DrawingEditorTool
 *        geo.showAttributes=true
 *        geo.loaders=lines polys points
 *        geo.mouseModes=Gestures
 *        geo.lines.class=com.bbn.openmap.tools.drawing.OMLineLoader
 *        geo.polys.class=com.bbn.openmap.tools.drawing.OMPolyLoader
 *        geo.points.class=com.bbn.openmap.tools.drawing.OMPointLoader
 *        geo.shapeFileList=geocounties geolakes geocountries
 *        geo.geocounties=/data/shape/usa/counties.shp
 *        geo.geolakes=/data/shape/world/lakes.shp
 *        geo.geocountries=/data/shape/world/cntry02/cntry02.shp
 *        # Colors for regular, unselected data shapes
 *        geo.fillColor=FF333399
 *        geo.selectColor=ffff9900
 *        geo.mattingColor=ffff9900
 *        # Colors for data shapes intersected by drawn shapes
 *        geo.selected.fillColor=FFFFFF00
 *        geo.selected.selectColor=ffff9900
 *        geo.selected.mattingColor=ffff9900
 * </pre>
 * 
 * @author dietrick
 */
public class GeoIntersectionLayer extends EditorLayer implements PropertyChangeListener {

    /** This list holds the OMGraphics that have been drawn. */
    protected OMGraphicList drawnList = new OMGraphicList();
    /** This list holds the EsriGraphicLists from the Shape files. */
    protected OMGraphicList fileDataList = new OMGraphicList();
    /**
     * This list holds the BoundaryCrossings and the image masks created from
     * Intersection queries.
     */
    protected OMGraphicList intersectionResultList = new OMGraphicList();
    /** The RegionIndex organizing the Shape OMGraphics for searching. */
    protected ExtentIndexImpl regionIndex = null;

    protected DrawingAttributes shapeDA = new DrawingAttributes();
    protected DrawingAttributes shapeDASelected = new DrawingAttributes();

    public final static String ShapeFileListProperty = "shapeFileList";
    public final static String ShapeFileProperty = "shapeFile";
    public final static String ShowCrossingPointsProperty = "showCrossingPoints";
    public final static String PointCheckProperty = "pointCheck";

    public final static String SHAPE_FILE_NAME_ATTRIBUTE = "SHAPE_FILE_NAME";
    public final static String SHAPE_VISIBILITY_CONTROL_ATTRIBUTE = "SHAPE_VISIBILITY_CONTROL";
    public final static String SHAPE_CONTROL_ATTRIBUTE = "SHAPE_CONTROL";

    protected boolean showCrossingPoints = false;
    protected boolean createPointCheck = false;

    public static boolean DEBUG = false;

    /**
     * 
     */
    public GeoIntersectionLayer() {
        super();
        DEBUG = Debug.debugging("geo");
        shapeDA.getPropertyChangeSupport().addPropertyChangeListener(this);
    }

    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        shapeDA.setProperties(prefix, props);
        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        shapeDASelected.setProperties(prefix + "selected", props);

        Vector<String> v = PropUtils.parseSpacedMarkers(props.getProperty(prefix
                + ShapeFileListProperty));

        for (String markerName : v) {
            String shapeFileName = props.getProperty(prefix + markerName);
            if (shapeFileName != null) {
                File sf = new File(shapeFileName);
                if (sf.exists()) {
                    addShapeFile(sf);
                }
            }
        }

    }

    public synchronized OMGraphicList prepare() {
        OMGraphicList list = getList();

        if (list == null) {
            list = new OMGraphicList();
            // If there isn't any data loaded, ask the user for a
            // file.
            if (fileDataList.isEmpty()) {
                addShapeFileFromUser();
            }
        } else {
            list.clear();
        }

        // If we created any pixel intersection images before, time to
        // get rid of them.

        calculateIntersectionsWithDrawnList();

        list.add(intersectionResultList);
        list.add(drawnList);
        if (DEBUG)
            Debug.output("GeoIntersectLayer(" + getName() + "): Adding lines to main list");
        list.add(fileDataList);
        if (DEBUG)
            Debug.output("GeoIntersectLayer(" + getName() + "): Adding shapes to main list");

        list.generate(getProjection());
        if (DEBUG)
            Debug.output("GeoIntersectLayer(" + getName() + "): Projected main list, returning");

        return list;

    }

    public void calculateIntersectionsWithDrawnList() {
        intersectionResultList.clear();
        ExtentIndex rIndex = getRegionIndex(true);

        for (OMGraphic omg : drawnList) {

            if (omg instanceof OMLine || (omg instanceof OMPoly && !((OMPoly) omg).isPolygon())) {

                if (DEBUG) {
                    Debug.output("GeoIntersectLayer(" + getName()
                            + "): Checking line against RegionIndex");
                }

                GeoPath path = getPathFromOMGraphic(omg);

                Iterator intrsctns = null;
                Iterator crssngs = null;

                if (showCrossingPoints) {
                    BoundaryCrossing.Collector results = BoundaryCrossing.getCrossings(path, rIndex);
                    intrsctns = results.iterator();
                    crssngs = results.getCrossings();
                } else {
                    intrsctns = Intersection.intersect(path, rIndex);
                }

                while (intrsctns.hasNext()) {

                    OMPolyRegion ompr = (OMPolyRegion) intrsctns.next();
                    setRegionAsSelected(ompr);

                    if (DEBUG) {
                        Debug.output("GeoIntersectLayer(" + getName() + "): Set Poly for hit");
                    }
                }

                int num = 0;

                while (crssngs != null && crssngs.hasNext()) {
                    BoundaryCrossing bc = (BoundaryCrossing) crssngs.next();
                    Geo geo = bc.getGeo();

                    OMPoint pgeo = new OMPoint((float) geo.getLatitude(), (float) geo.getLongitude());
                    pgeo.setFillPaint(Color.WHITE);
                    pgeo.putAttribute(OMGraphic.LABEL, new OMTextLabeler(Integer.toString(num++)));
                    intersectionResultList.add(pgeo);
                }

            } else if (omg instanceof OMPoly) {
                for (Iterator hits = Intersection.intersect(new OMPolyRegion((OMPoly) omg), rIndex); hits.hasNext();) {
                    setRegionAsSelected((OMPolyRegion) hits.next());

                    if (DEBUG) {
                        Debug.output("GeoIntersectLayer(" + getName() + "): Set Poly for hit");
                    }
                }
            } else if (omg instanceof OMPoint) {
                OMPoint omp = (OMPoint) omg;
                for (Iterator hits = Intersection.intersect(new GeoPoint.Impl(omp.getLat(), omp.getLon()), rIndex); hits.hasNext();) {
                    setRegionAsSelected((OMPolyRegion) hits.next());

                    if (DEBUG) {
                        Debug.output("GeoIntersectLayer(" + getName() + "): Set Poly for hit");
                    }
                }
            }
        }
    }

    protected void setRegionAsSelected(OMPolyRegion ompr) {
        shapeDASelected.setTo(ompr.poly);
    }

    protected GeoPath getPathFromOMGraphic(OMGraphic omg) {
        GeoPath path = null;

        if (omg instanceof OMLine) {
            path = getPath((OMLine) omg);
        } else if (omg instanceof OMPoly) {
            path = getPath((OMPoly) omg);
        }

        return path;
    }

    protected GeoPath getPath(OMLine oml) {
        GeoPath ret = null;
        if (oml.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            ret = new GeoPath.Impl(oml.getLL());
        }
        return ret;
    }

    protected GeoPath getPath(OMPoly omp) {
        GeoPath ret = null;
        if (omp.getRenderType() == OMGraphic.RENDERTYPE_LATLON) {
            ret = new GeoPath.Impl(omp.getLatLonArray(), false);
        }
        return ret;
    }

    /**
     * Query the user for a shape file, and add the contents to the region list
     * or line list if a valid file is selected.
     */
    public void addShapeFileFromUser() {

        String shpFileName = FileUtils.getFilePathToOpenFromUser("Pick Shape File", new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith("shp");
            }

            public String getDescription() {
                return "ESRI Shape (.shp) file";
            }

        });

        if (shpFileName != null) {
            addShapeFile(new File(shpFileName));
        }
    }

    /**
     * Add the data from a shape file to the region list or edge list, depending
     * on the content type.
     * 
     * @param shpFile
     */
    public void addShapeFile(File shpFile) {
        if (shpFile != null) {
            try {

                String shpFilePath = shpFile.getAbsolutePath();
                String shpFileName = shpFile.getName();

                DrawingAttributes da = new DrawingAttributes();
                da.setSelectPaint(new Color(200, 100, 100, 200));

                EsriGraphicList shapeList = EsriGraphicList.getEsriGraphicList(shpFile.toURI().toURL(), da, DbfTableModel.getDbfTableModel(new File(shpFilePath.replaceAll(".shp", ".dbf")).toURI().toURL()), coordTransform);

                if (DEBUG)
                    Debug.output("GeoIntersectLayer(" + getName() + "): Adding shapes from "
                            + shpFileName);

                JCheckBox visibilityControl = new JCheckBox("Show", true);
                visibilityControl.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        setShapeListVisibilityForCheckbox();
                        repaint();
                    }
                });

                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(new RemoveShapesActionListener(fileDataList, shapeList));

                JLabel label = new JLabel(shpFileName, JLabel.LEFT);
                JPanel panel = new JPanel();
                GridBagLayout gridbag = new GridBagLayout();
                panel.setLayout(gridbag);
                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 1.0;
                c.insets = new Insets(2, 2, 2, 2);
                c.anchor = GridBagConstraints.WEST;
                c.fill = GridBagConstraints.HORIZONTAL;
                gridbag.setConstraints(label, c);
                panel.add(label);
                c.weightx = 0;
                c.anchor = GridBagConstraints.EAST;
                c.fill = GridBagConstraints.NONE;
                gridbag.setConstraints(visibilityControl, c);
                panel.add(visibilityControl);
                gridbag.setConstraints(removeButton, c);
                panel.add(removeButton);

                shapeList.putAttribute(SHAPE_FILE_NAME_ATTRIBUTE, shpFileName);
                shapeList.putAttribute(SHAPE_VISIBILITY_CONTROL_ATTRIBUTE, visibilityControl);
                shapeList.putAttribute(SHAPE_CONTROL_ATTRIBUTE, panel);

                int type = shapeList.getType();
                if (type != EsriGraphicList.SHAPE_TYPE_POLYGON
                        && type != EsriGraphicList.SHAPE_TYPE_POLYLINE) {
                    fireRequestMessage("The type of shapes contained in the file\n"
                            + shpFileName
                            + "\nisn't handled by this layer.  Choose a file that\ncontains lines or polygons.");
                    return;
                }

                fileDataList.add(shapeList);
                rebuildFileListControl();
                if (getProjection() != null) {
                    doPrepare();
                }

            } catch (MalformedURLException murle) {
            }
        }
    }

    protected void setShapeListVisibilityForCheckbox() {
        for (OMGraphic obj : fileDataList) {
            if (obj instanceof OMGraphicList) {
                OMGraphicList omgl = (OMGraphicList) obj;
                JCheckBox jcb = (JCheckBox) omgl.getAttribute(SHAPE_VISIBILITY_CONTROL_ATTRIBUTE);
                if (jcb != null) {
                    omgl.setVisible(jcb.isSelected());
                }
            }

        }
    }

    public ExtentIndex getRegionIndex(boolean resetRegionSelection) {

        if (regionIndex == null) {
            regionIndex = new ExtentIndexImpl();
        }

        if (resetRegionSelection) {
            for (Iterator reset = regionIndex.iterator(); reset.hasNext();) {
                shapeDA.setTo(((OMPolyRegion) reset.next()).poly);
            }
            if (DEBUG)
                Debug.output("GeoIntersectLayer(" + getName() + "): Reset region fills");
        }

        return regionIndex;
    }

    protected void addToRegionIndex(OMPoly p, ExtentIndex regionIndex) {
        if (regionIndex.addExtent(new OMPolyRegion(p)) && DEBUG) {
            Debug.output("GeoIntersectLayer(" + getName() + "): Added poly region to RegionIndex");
        }
    }

    protected void addToRegionIndex(OMGraphicList omgl, ExtentIndex regionIndex) {
        for (Iterator it = omgl.iterator(); it.hasNext();) {
            Object someObj = it.next();
            if (someObj instanceof OMPoly) {
                addToRegionIndex((OMPoly) someObj, regionIndex);
            } else {
                addToRegionIndex((OMGraphicList) someObj, regionIndex);
            }
        }
    }

    public void drawingComplete(OMGraphic omg, OMAction action) {

        releaseProxyMouseMode();

        if ((omg instanceof OMLine || omg instanceof OMPoly || omg instanceof OMPoint)
                && drawnList != null) {
            drawnList.doAction(omg, action);
            deselect(drawnList);
            doPrepare();
        } else {
            Debug.error("GeoIntersectLayer(" + getName() + "):  received " + omg + " and " + action
                    + " with no list ready");
        }

        // This is important!!
        if (editorTool != null) {
            editorTool.drawingComplete(omg, action);
        }
    }

    public OMGraphicList getDrawnIntersectorList() {
        return drawnList;
    }

    public boolean receivesMapEvents() {
        return false;
    }

    public boolean mouseOver(MapMouseEvent mme) {

        if (regionIndex != null) {
            Point2D llp = mme.getLatLon();
            GeoPoint geop = new GeoPoint.Impl((float) llp.getY(), (float) llp.getX());
            for (Iterator hits = Intersection.intersect(geop, regionIndex); hits.hasNext();) {
                OMPolyRegion ompr = (OMPolyRegion) hits.next();
                ompr.poly.select();
                ompr.poly.generate(getProjection());
            }
            repaint();
        }

        return true;
    }

    public boolean isHighlightable(OMGraphic omg) {
        return createPointCheck || (drawnList != null && drawnList.contains(omg));
    }

    public String getToolTipTextFor(OMGraphic omg) {
        if (drawnList != null && drawnList.contains(omg)) {
            return super.getToolTipTextFor(omg);
        } else if (createPointCheck) {
            return "Click to create point test image mask";
        }
        return null;
    }

    public void highlight(OMGraphic omg) {
        omg.setMatted(true);
        super.highlight(omg);
    }

    public void unhighlight(OMGraphic omg) {
        omg.setMatted(false);
        super.unhighlight(omg);
    }

    public boolean isSelectable(OMGraphic omg) {
        return createPointCheck || (drawnList != null && drawnList.contains(omg));
    }

    public void select(OMGraphicList omgl) {
        for (OMGraphic omg : omgl) {
            if (drawnList != null && drawnList.contains(omg)) {
                super.select(omgl);
            } else if (createPointCheck) {
                intersectionResultList.add(getPointIntersectionImage(omg));
            }
        }
        repaint();
    }

    public void deselect(OMGraphicList omgl) {
        intersectionResultList.clear();
        repaint();
    }

    public OMGraphic getPointIntersectionImage(OMGraphic omg) {
        Shape s = omg.getShape();
        Projection p = getProjection();
        if (s != null && p != null && omg instanceof OMPoly) {
            Rectangle r = s.getBounds();

            double x = r.getX();
            double y = r.getY();
            double h = r.getHeight();
            double w = r.getWidth();
            double[] rawll = ((OMPoly) omg).getLatLonArray();
            Point2D llHolder = new Point2D.Double();
            Geo g = new Geo(0, 0);
            int[] pix = new int[(int) (h * w)];

            for (double j = 0; j < h; j++) {
                for (double i = 0; i < w; i++) {

                    boolean inShape = s.contains(i + x, j + y);
                    p.inverse((int) (i + x), (int) (j + y), llHolder);

                    g.initialize(llHolder.getY(), llHolder.getX());
                    boolean inGeo = Intersection.isPointInPolygon(g, rawll, false);

                    int val = 0;
                    if (inShape == inGeo) {
                        val = 0x6200FF00;
                    } else {
                        val = 0x62ff0000;
                    }
                    pix[(int) (w * j + i)] = val;
                }
            }

            OMRaster omr = new OMRaster((int) x, (int) y, (int) w, (int) h, pix);
            omr.setSelectPaint(OMColor.clear);
            // omr.setSelected(true);
            omr.generate(p);
            return omr;
        }

        return SinkGraphic.getSharedInstance();
    }

    protected JPanel getFileListControl() {
        if (fileListControl == null) {
            fileListControl = PaletteHelper.createHorizontalPanel("Shape Files Being Used for Intersections");
        }
        return fileListControl;
    }

    public void rebuildFileListControl() {
        JPanel p = getFileListControl();
        p.setBackground(Color.white);
        Color light = Color.white;
        Color dark = Color.LIGHT_GRAY;
        Color current = dark;

        p.removeAll();
        GridBagLayout gridbag = new GridBagLayout();
        p.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        getRegionIndex(false).clear();
        for (OMGraphic obj : fileDataList) {

            if (obj instanceof EsriGraphicList) {
                EsriGraphicList shapeList = (EsriGraphicList) obj;
                JPanel control = (JPanel) shapeList.getAttribute(SHAPE_CONTROL_ATTRIBUTE);
                if (control != null) {
                    control.setBackground(current);
                    Component[] comps = control.getComponents();
                    for (int i = 0; i < comps.length; i++) {
                        comps[i].setBackground(current);
                    }
                    if (current == dark)
                        current = light;
                    else
                        current = dark;
                    gridbag.setConstraints(control, c);
                    p.add(control);
                }

                addToRegionIndex(shapeList, getRegionIndex(false));
            }
        }

        if (fileDataList.isEmpty()) {
            JLabel label = new JLabel("No Shape Files Loaded", JButton.CENTER);
            c.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(label, c);
            p.add(label);
        }

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        JLabel filler = new JLabel("");
        gridbag.setConstraints(filler, c);
        p.add(filler);

        p.revalidate();
    }

    JPanel fileListControl;
    JPanel panel = null;
    JCheckBox showCrossingsButton;
    JCheckBox pointCheckButton;

    public Component getGUI() {
        if (panel == null) {
            panel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();

            panel.setLayout(gridbag);
            c.gridx = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            c.insets = new Insets(1, 5, 1, 5);
            c.fill = GridBagConstraints.HORIZONTAL;
            JPanel daPanel1 = PaletteHelper.createHorizontalPanel("Paint Settings for Shapes");
            daPanel1.add(shapeDA.getGUI());
            JPanel daPanel2 = PaletteHelper.createHorizontalPanel("Paint Settings for Intersected Shapes");
            daPanel2.add(shapeDASelected.getGUI());

            gridbag.setConstraints(daPanel1, c);
            gridbag.setConstraints(daPanel2, c);
            panel.add(daPanel1);
            panel.add(daPanel2);

            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            JPanel tablePanel = getFileListControl();
            gridbag.setConstraints(tablePanel, c);
            panel.add(tablePanel);

            JPanel checkPanel = new JPanel();
            GridBagLayout gb = new GridBagLayout();
            checkPanel.setLayout(gb);
            GridBagConstraints c2 = new GridBagConstraints();

            c2.anchor = GridBagConstraints.WEST;
            c2.gridx = GridBagConstraints.REMAINDER;
            showCrossingsButton = new JCheckBox("Show Crossing Points", showCrossingPoints);
            showCrossingsButton.setToolTipText("<html>Show ordered points where drawn lines cross Shapes.");
            showCrossingsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setShowCrossingPoints(((JCheckBox) ae.getSource()).isSelected());
                    doPrepare();
                }
            });
            gb.setConstraints(showCrossingsButton, c2);
            checkPanel.add(showCrossingsButton);

            pointCheckButton = new JCheckBox("Click Creates Image Mask", showCrossingPoints);
            pointCheckButton.setToolTipText("<html>When clicking on Shape, create image mask that shows Geo point<br>intersection vs. Java 2D. Green is good.");
            pointCheckButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setCreatePointCheck(((JCheckBox) ae.getSource()).isSelected());
                    doPrepare();
                }
            });
            gb.setConstraints(pointCheckButton, c2);
            checkPanel.add(pointCheckButton);

            c.weightx = 0;
            c.weighty = 0;
            c.fill = GridBagConstraints.NONE;
            gridbag.setConstraints(checkPanel, c);
            panel.add(checkPanel);

            JButton addButton = new JButton("Add Shape File...");
            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    addShapeFileFromUser();
                }
            });
            c.insets = new Insets(5, 5, 5, 5);
            gridbag.setConstraints(addButton, c);
            panel.add(addButton);
        }
        return panel;
    }

    public static class OMPolyRegion extends GeoRegion.Impl {

        public OMPoly poly;

        public OMPolyRegion(OMPoly omp) {
            super(omp.getLatLonArray(), false);
            poly = omp;
        }

        public Object getID() {
            return GeoIntersectionLayer.OMPolyRegion.this;
        }

    }

    public static class OMLineSegment implements GeoSegment {
        Geo[] geos;
        double[] segArray;

        public OMLineSegment(OMLine oml) {
            segArray = oml.getLL();
            geos = new Geo[2];
            geos[0] = new Geo(segArray[0], segArray[1]);
            geos[1] = new Geo(segArray[2], segArray[3]);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bbn.openmap.geo.GeoSegment#getSeg()
         */
        public Geo[] getSeg() {
            return geos;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bbn.openmap.geo.GeoSegment#getSegArray()
         */
        public double[] getSegArray() {
            return segArray;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.bbn.openmap.geo.GeoExtent#getID()
         */
        public Object getID() {
            return this;
        }

        public BoundingCircle getBoundingCircle() {
            return new BoundingCircle.Impl(getSeg());
        }

    }

    protected class RemoveShapesActionListener implements ActionListener {
        protected OMGraphicList mainDataList;
        protected OMGraphicList toBeRemoved;

        public RemoveShapesActionListener(OMGraphicList mdl, OMGraphicList tbr) {
            mainDataList = mdl;
            toBeRemoved = tbr;
        }

        public void actionPerformed(ActionEvent ae) {
            mainDataList.remove(toBeRemoved);
            rebuildFileListControl();
            GeoIntersectionLayer.this.doPrepare();
        }
    }

    public boolean isShowCrossingPoints() {
        return showCrossingPoints;
    }

    public void setShowCrossingPoints(boolean showCrossingPoints) {
        this.showCrossingPoints = showCrossingPoints;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        shapeDA.setTo(fileDataList);
        repaint();
    }

    public DrawingAttributes getShapeDA() {
        return shapeDA;
    }

    public void setShapeDA(DrawingAttributes shapeDA) {
        this.shapeDA = shapeDA;
    }

    public DrawingAttributes getShapeDASelected() {
        return shapeDASelected;
    }

    public void setShapeDASelected(DrawingAttributes shapeDASelected) {
        this.shapeDASelected = shapeDASelected;
    }

    public boolean isCreatePointCheck() {
        return createPointCheck;
    }

    public void setCreatePointCheck(boolean createPointCheck) {
        this.createPointCheck = createPointCheck;
    }

    public void runGeoTests(int numIterations, int numToSkipAtStart) {
        Projection proj = new Mercator(new LatLonPoint.Float(35f, -90f), 100000000, 800, 800);

        double[] results = new double[7];

        for (int i = 0; i < numIterations; i++) {

            boolean countThisIteration = (i >= numToSkipAtStart);

            long startTime = System.currentTimeMillis();
            setProjection(proj.makeClone());
            calculateIntersectionsWithDrawnList();
            long endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[0] += endTime - startTime;
            }

            OMGraphic omg = new OMLine(20f, -125f, 30f, -70f, OMGraphic.LINETYPE_GREATCIRCLE);
            getDrawnIntersectorList().add(omg);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[1] += endTime - startTime;
            }

            setShowCrossingPoints(true);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[2] += endTime - startTime;
            }

            getDrawnIntersectorList().clear();
            setShowCrossingPoints(false);
            double[] coords = new double[] { 33.4f, -77.2f, 34f, -79.5f, 35f, -90f, 40f, -100f,
                    45f, -101f, 50f, -83.2f, 35f, -65.7f, -34f, -70.5f, 33.4f, -77.2f };

            omg = new OMPoly(coords, OMPoly.DECIMAL_DEGREES, OMGraphic.LINETYPE_GREATCIRCLE);
            getDrawnIntersectorList().add(omg);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[3] += endTime - startTime;
            }

            setShowCrossingPoints(true);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[4] += endTime - startTime;
            }

            omg.setFillPaint(Color.red);
            setShowCrossingPoints(false);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[5] += endTime - startTime;
            }

            setShowCrossingPoints(true);
            startTime = System.currentTimeMillis();
            calculateIntersectionsWithDrawnList();
            endTime = System.currentTimeMillis();
            if (countThisIteration) {
                results[6] += endTime - startTime;
            }

            System.out.print(".");
            System.out.flush();

        }

        double numIterationsCounted = numIterations - numToSkipAtStart;
        Debug.output("For " + numIterationsCounted + " iterations");
        Debug.output(" avg time to calculate without Intersection: "
                + (results[0] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection line: "
                + (results[1] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection line with crossing points: "
                + (results[2] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection poly: "
                + (results[3] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection poly with crossing points: "
                + (results[4] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection Containment poly: "
                + (results[5] / numIterationsCounted) + " ms");
        Debug.output(" avg time to calculate with Intersection Containment poly and crossing points: "
                + (results[6] / numIterationsCounted) + " ms");

    }

    public static void main(String[] argv) {
        Debug.init();
        ArgParser argp = new ArgParser("GeoIntersectionLayer");

        argp.add("shape", "Shape file to use for GeoRegions in index.", 1);

        argp.parse(argv);
        String[] files = argp.getArgValues("shape");
        if (files != null && files.length > 0 && files[0].endsWith(".shp")) {
            File file = new File(files[0]);

            GeoIntersectionLayer gil = new GeoIntersectionLayer();
            Debug.output("Loading shape file: " + file.getName());
            long startTime = System.currentTimeMillis();
            gil.addShapeFile(file);
            long endTime = System.currentTimeMillis();

            Debug.output(" time to load file: " + (endTime - startTime) + " ms");

            gil.runGeoTests(25, 3);

        } else {
            argp.printUsage();
            System.exit(0);
        }
    }
}
