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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/plugin/esri/EsriPlugIn.java,v $
// $RCSfile: EsriPlugIn.java,v $
// $Revision: 1.17 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.plugin.esri;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.Layer;
import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.DrawingAttributesUtility;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.dataAccess.shape.EsriPointList;
import com.bbn.openmap.dataAccess.shape.EsriPolygonList;
import com.bbn.openmap.dataAccess.shape.EsriPolylineList;
import com.bbn.openmap.dataAccess.shape.EsriShapeExport;
import com.bbn.openmap.dataAccess.shape.ShapeConstants;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.plugin.BeanContextAbstractPlugIn;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.DataBounds;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * EsriPlugIn loads Esri shape file sets from web servers or local file systems,
 * and it enables the creation of shape file sets. It needs to be inserted into
 * a PlugInLayer to use within OpenMap.
 * <P>
 * To create a file from a remote location: <code><pre>
 * 
 * URL shp = new URL(&quot;http://www.webserver.com/file.shp&quot;);
 * URL dbf = new URL(&quot;http://www.webserver.com/file.dbf&quot;);
 * URL shx = new URL(&quot;http://www.webserver.com/file.shx&quot;);
 * EsriPlugIn epi = new EsriPlugIn(&quot;name&quot;, dbf, shp, shx);
 * PlugInLayer pil = new PlugInLayer();
 * pil.setPlugIn(epi);
 * 
 * </pre></code>
 * 
 * To open a shape file set from the local file system: <code><pre>
 * 
 * File dbf = new File(&quot;c:/data/file.dbf&quot;);
 * File shp = new File(&quot;c:/data/file.shp&quot;);
 * File shx = new File(&quot;c:/data/file.shx&quot;);
 * EsriPlugIn epi = new EsriPlugIn(&quot;name&quot;, dbf.toURI().toURL(), shp.toURI().toURL(), shx.toURI().toURL());
 * PlugInLayer pil = new PlugInLayer();
 * pil.setPlugIn(epi);
 * 
 * </pre></code>
 * 
 * To create a zero content shape file set from which the user can add shapes at
 * runtime: <code><pre>
 * EsriPlugIn epi = new EsriPlugIn(&quot;name&quot;, EsriLayer.TYPE_POLYLINE);
 * 
 * </pre></code>
 * 
 * To add features to an EsriLayer: <code><pre>
 * 
 * OMGraphicList shapeData = new OMGraphicList();
 * ArrayList tabularData = new ArrayList();
 * float[] part0 = new float[] { 35.0f, -120.0f, -25.0f, -95.0f, 56.0f, -30.0f };
 * float[] part1 = new float[] { -15.0f, -110.0f, 13.0f, -80.0f, -25.0f, 10.0f };
 * OMPoly poly0 = new OMPoly(part0, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
 * OMPoly poly1 = new OMPoly(part1, OMGraphic.DECIMAL_DEGREES, OMGraphic.LINETYPE_RHUMB);
 * shapeData.add(poly0); //part 1
 * shapeData.add(poly1); //part 2
 * shapeData.generate(_mapBean.getProjection());
 * tabularData.add(0, &quot;a value&quot;);
 * plugin.addRecord(shapeData, tabularData);
 * plugin.repaint(); // assumes that plugin added to PlugInLayer
 * 
 * </pre></code>
 * 
 * To configure an EsriLayer through a properties file, specify file references
 * in terms of URLs, full or relative file paths.
 * 
 * To reference a file on Windows 2000: <code><pre>
 *    
 *    
 *       esri.class = com.bbn.openmap.plugin.esri.EsriPlugIn
 *       esri.prettyName = Esri Example
 *       esri.shp = file:///c:/data/shapefile.shp
 *     # -or-
 *       esri.shp = c:/data/shapefile.shp
 *    
 *       esri.dbf = file:///c:/data/shapefile.dbf
 *       esri.shx = file:///c:/data/shapefile.shx
 *    
 *     
 * </pre></code>
 * 
 * To reference a file on RedHat Linux 6.2: <code><pre>
 *    
 *    
 *       esri.class = com.bbn.openmap.plugin.esri.EsriPlugIn
 *       esri.prettyName = Esri Example
 *       esri.shp = file:///home/dvanauke/resources/shapefile.shp
 *     # - or -
 *       esri.shp = /home/dvanauke/resources/shapefile.shp
 *    
 *       esri.dbf = file:///home/dvanauke/resources/shapefile.dbf
 *       esri.shx = file:///home/dvanauke/resources/shapefile.shx
 *    
 *     
 * </pre></code>
 * 
 * To reference a file on a web server: <code><pre>
 *    
 *    
 *       esri.class = com.bbn.openmap.plugin.esri.EsriPlugIn
 *       esri.prettyName = Esri Example
 *       esri.shp = http://www.webserver.com/shapefile.shp
 *       esri.dbf = http://www.webserver.com/shapefile.dbf
 *       esri.shx = http://www.webserver.com/shapefile.shx
 *    
 *     
 * </pre></code>
 * 
 * The PlugIn has been updated to use the properties from the DrawingAttributes
 * object in order to specify how it's objects should be rendered: <code><pre>
 *    
 *    
 *       esri.class = com.bbn.openmap.plugin.esri.EsriPlugIn
 *       esri.prettyName = Esri Example
 *       esri.lineColor = AARRGGBB (hex ARGB color)
 *       esri.fillColor = AARRGGBB (hex ARGB color)
 *       esri.selectColor = AARRGGBB (hex ARGB color)
 *       esri.lineWidth = AARRGGBB (hex ARGB color)
 *    
 *     
 * </pre></code>
 * 
 * See DrawingAttributes for more options. Also, as of OpenMap 4.5.4, you don't
 * have to specify the location of the .dbf and .shx files. If you don't, the
 * plugin assumes that those files are next to the .shp file.
 * 
 * @author Doug Van Auken
 * @author Don Dietrick
 * @author Lonnie Goad from OptiMetrics provided selection bug solution and GUI
 *         interaction.
 */
public class EsriPlugIn extends BeanContextAbstractPlugIn implements
        ShapeConstants, DataBoundsProvider {

    private EsriGraphicList _list = null;
    private DbfTableModel _model = null;
    private int _type = -1;

    private String dbf;
    private String shx;
    private String shp;

    /**
     * A simple list mechanism that will let selected OMGraphics to be drawn on
     * top of all the others. Using this list instead of changing the order of
     * the esri graphic list maintains the order of that list. We clear out this
     * and add to it as necessary.
     */
    protected OMGraphicList selectedGraphics = new OMGraphicList();

    /** The last projection. */
    protected Projection proj;

    protected DrawingAttributes drawingAttributes = DrawingAttributes.getDefaultClone();

    /**
     * Creates an EsriPlugIn that will be configured through the
     * <code>setProperties()</code> method
     */
    public EsriPlugIn() {
        Debug.message("esri", "EsriPlugIn: default constructor");
    }

    /**
     * Creates an empty EsriPlugIn, usable for adding features at run-time.
     * 
     * @param name The name of the layer
     * @param type The type of layer
     * @param columnCount The number of columns in the dbf model
     */
    public EsriPlugIn(String name, int type, int columnCount) throws Exception {

        switch (type) {
        case SHAPE_TYPE_POINT:
            _list = new EsriPointList();
            break;
        case SHAPE_TYPE_POLYGON:
            _list = new EsriPolygonList();
            break;
        case SHAPE_TYPE_POLYLINE:
            _list = new EsriPolylineList();
            break;
        default:
            _list = null;
        }

        _model = new DbfTableModel(columnCount);
        this.name = name;
    }

    /**
     * Creates an EsriPlugIn from a set of shape files
     * 
     * @param name The name of the layer that may be used to reference the layer
     * @param dbf The url referencing the dbf extension file
     * @param shp The url referencing the shp extension file
     * @param shx The url referencing the shx extension file
     */
    public EsriPlugIn(String name, URL dbf, URL shp, URL shx) {

        this.dbf = dbf.toString();
        this.shp = shp.toString();
        this.shx = shx.toString();

        _list = getGeometry(shp);
        _model = getDbfTableModel(dbf);
        _list.putAttribute(DBF_ATTRIBUTE, _model);
        this.name = name;
    }

    /**
     * Set the drawing attributes for the graphics on the list.
     */
    public void setDrawingAttributes(DrawingAttributes da) {
        drawingAttributes = da;

        if (_list != null) {
            drawingAttributes.setTo(_list);
        }
    }

    /**
     * Get the drawing attributes for the graphics on the list.
     */
    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    /**
     * Handles adding records to the geometry list and the DbfTableModel.
     * 
     * @param graphic An OMGraphic to add the graphics list
     * @param record A record to add to the DbfTableModel
     */
    public void addRecord(OMGraphic graphic, ArrayList<Object> record) {
        OMGraphicList list = getEsriGraphicList();

        // Associate the record directly in the OMGraphic
        graphic.putAttribute(SHAPE_DBF_INFO_ATTRIBUTE, record);

        // If list == null, model will be too.
        if (list != null) {
            // Might as well set the index
            graphic.putAttribute(SHAPE_INDEX_ATTRIBUTE, new Integer(list.size()));
            list.add(graphic);
            _model.addRecord(record);
        } else {
            Debug.error("EsriPlugIn.addRecord(): invalid data files!");
        }
    }

    /**
     * Creates a DbfTableModel for a given .dbf file
     * 
     * @param dbf The url of the file to retrieve.
     * @return The DbfTableModel for this layer, null if something went badly.
     */
    private DbfTableModel getDbfTableModel(URL dbf) {
        return DbfTableModel.getDbfTableModel(dbf);
    }

    /**
     * Returns the EsriGraphicList for this layer
     * 
     * @return The EsriGraphicList for this layer
     */
    public EsriGraphicList getEsriGraphicList() {
        if (_list == null) {
            try {
                // _model = getDbfTableModel(new URL(dbf));
                // _list = getGeometry(new URL(shp), new URL(shx));

                // Changed so that shp, dbf and shx can be named as
                // resource, a file path, or a URL. Also, if the dbf
                // and shx file are not provided, look for them next
                // to the shape file. - DFD

                if ((shx == null || shx.length() == 0) && shp != null) {
                    shx = shp.substring(0, shp.lastIndexOf('.') + 1)
                            + PARAM_SHX;
                }

                if ((dbf == null || dbf.length() == 0) && shp != null) {
                    dbf = shp.substring(0, shp.lastIndexOf('.') + 1)
                            + PARAM_DBF;
                }

                _model = getDbfTableModel(PropUtils.getResourceOrFileOrURL(dbf));
                _list = getGeometry(PropUtils.getResourceOrFileOrURL(shp));

                if (_model != null) {
                    DrawingAttributesUtility.setDrawingAttributes(_list,
                            _model,
                            getDrawingAttributes());
                }
            } catch (MalformedURLException murle) {
                Debug.error("EsriPlugIn|" + getName()
                        + " Malformed URL Exception\n" + murle.getMessage());
            } catch (Exception exception) {
                Debug.error("EsriPlugIn|" + getName() + " Exception\n"
                        + exception.getMessage());
                exception.printStackTrace();
            }
        }

        return _list;
    }

    public static void main(String[] argv) {
        if (argv.length == 0) {
            System.out.println("Give EsriPlugIn a path to a shape file, and it'll print out the graphics.");
            System.exit(0);
        }

        Debug.init();

        EsriPlugIn epi = new EsriPlugIn();
        Properties props = new Properties();
        props.put(PARAM_SHP, argv[0]);
        epi.setProperties(props);

        OMGraphicList list = epi.getEsriGraphicList();

        if (list != null) {
            Debug.output(list.getDescription());

        }

        String dbfFileName = argv[0].substring(0, argv[0].lastIndexOf('.') + 1)
                + "dbf";

        try {
            DbfTableModel dbf = epi.getDbfTableModel(PropUtils.getResourceOrFileOrURL(epi,
                    dbfFileName));
            if (list != null)
                list.putAttribute(DBF_ATTRIBUTE, dbf);
            Debug.output("Set list in table");
            dbf.showGUI(dbfFileName, 0);
        } catch (Exception e) {
            Debug.error("Can't read .dbf file for .shp file: " + dbfFileName
                    + "\n" + e.getMessage());
            System.exit(0);
        }

        EsriShapeExport ese = new EsriShapeExport(list, null, "./ese");
        Debug.output("Exporting...");
        ese.export();
        Debug.output("Done.");
    }

    /**
     * The getRectangle call is the main call into the PlugIn module. The module
     * is expected to fill a graphics list with objects that are within the
     * screen parameters passed. It's assumed that the PlugIn will call
     * generate(projection) on the OMGraphics returned! If you don't call
     * generate on the OMGraphics, they will not be displayed on the map.
     * 
     * @param p projection of the screen, holding scale, center coords, height,
     *        width. May be null if the parent component hasn't been given a
     *        projection.
     */
    public OMGraphicList getRectangle(Projection p) {
        OMGraphicList list = getEsriGraphicList();
        proj = p;

        if (list != null) {
            list.generate(p);

            // Setting the list up so that if anything is "selected",
            // it will also be drawn on top of all the other
            // OMGraphics. This maintains order while also making any
            // line edge changes more prominent.
            OMGraphicList parent = new OMGraphicList();
            parent.add(selectedGraphics);
            parent.add(list);
            list = parent;
        }

        return list;
    }

    /**
     * Reads the contents of the SHX and SHP files. The SHX file will be read
     * first by utilizing the ShapeIndex.open method. This method will return a
     * list of offsets, which the AbstractSupport.open method will use to
     * iterate through the contents of the SHP file.
     * 
     * @param shp The url of the SHP file
     * @param shx The url of the SHX file (not used, OK if null).
     * @return A new EsriGraphicList, null if something went badly.
     * @deprecated Use getGeometry(URL) instead, the shx file isn't used.
     */
    public EsriGraphicList getGeometry(URL shp, URL shx) {
        return EsriGraphicList.getEsriGraphicList(shp,
                getDrawingAttributes(),
                getModel(),
                parentLayer.getCoordTransform());
    }

    /**
     * Reads the contents of the SHP file.
     * 
     * @param shp The url of the SHP file
     * @return A new EsriGraphicList, null if something went badly.
     */
    public EsriGraphicList getGeometry(URL shp) {
        if (parentLayer == null) {
            Component comp = getComponent();
            if (comp instanceof Layer) {
                parentLayer = (Layer) comp;
            }
        }

        GeoCoordTransformation coordTransform = null;

        if (parentLayer != null) {
            coordTransform = parentLayer.getCoordTransform();
        }

        return EsriGraphicList.getEsriGraphicList(shp,
                getDrawingAttributes(),
                getModel(),
                coordTransform);
    }

    /**
     * Returns the associated table model for this layer
     * 
     * @return The associated table model for this layer
     */
    public DbfTableModel getModel() {
        return _model;
    }

    /**
     * Returns whether this layer is of type 0 (point), 3 (polyline), or
     * 5(polygon)
     * 
     * @return An int representing the type of layer, as specified in Esri's
     *         shape file format specification
     */
    public int getType() {
        return _type;
    }

    /**
     * Filters the DbfTableModel given a SQL like string
     * 
     * @param query A SQL like string to filter the DbfTableModel
     */
    public void query(String query) {
    // to be implemented
    }

    /**
     * Sets the DbfTableModel
     * 
     * @param model The DbfModel to set for this layer
     */
    public void setModel(DbfTableModel model) {
        if (_model != null) {
            _model = model;
            _list.putAttribute(DBF_ATTRIBUTE, model);
        }
    }

    /**
     * Sets the properties for the <code>Layer</code>.
     * 
     * @param prefix the token to prefix the property names
     * @param properties the <code>Properties</code> object
     */
    public void setProperties(String prefix, Properties properties) {
        super.setProperties(prefix, properties);

        drawingAttributes.setProperties(prefix, properties);

        // This fixes a hole that was exposed when the PlugIn had the
        // files set directly, and then had properties set for drawing
        // attributes later.
        if (_list != null) {
            if (_model != null) {
                DrawingAttributesUtility.setDrawingAttributes(_list,
                        _model,
                        drawingAttributes);
            } else {
                drawingAttributes.setTo(_list);
            }
        }

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        shp = properties.getProperty(prefix + PARAM_SHP);
        shx = properties.getProperty(prefix + PARAM_SHX);
        dbf = properties.getProperty(prefix + PARAM_DBF);

    }

    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + PARAM_SHP, PropUtils.unnull(shp));
        props.put(prefix + PARAM_SHX, PropUtils.unnull(shx));
        props.put(prefix + PARAM_DBF, PropUtils.unnull(dbf));

        // Need to make sure they line up.
        drawingAttributes.setPropertyPrefix(getPropertyPrefix());
        drawingAttributes.getProperties(props);
        return props;
    }

    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        props.put(initPropertiesProperty, PARAM_SHP + " " + PARAM_DBF + " "
                + PARAM_SHX + drawingAttributes.getInitPropertiesOrder() + " "
                + Layer.AddToBeanContextProperty);

        props.put(PARAM_SHP, "Location of a shape (.shp) file (path or URL)");
        props.put(PARAM_SHX,
                "Location of a index file (.shx) for the shape file (path or URL, optional)");
        props.put(PARAM_DBF,
                "Location of a database file (.dbf) for the shape file (path or URL, optional)");
        props.put(PARAM_SHP + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FDUPropertyEditor");
        props.put(PARAM_DBF + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FDUPropertyEditor");
        props.put(PARAM_SHX + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FDUPropertyEditor");

        drawingAttributes.getPropertyInfo(props);

        return props;
    }

    public Component getGUI() {

        JPanel holder = new JPanel(new BorderLayout());

        holder.add(drawingAttributes.getGUI(), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1));

        JButton redrawSelected = new JButton("Set Colors for Selected");
        btnPanel.add(redrawSelected);

        JButton redrawAll = new JButton("Set Colors For All");
        btnPanel.add(redrawAll);

        JButton tableTrigger = new JButton("Show Data Table");
        btnPanel.add(tableTrigger);

        holder.add(btnPanel, BorderLayout.SOUTH);

        redrawSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!(graphicIndex < 0)) {
                    OMGraphic omg = getEsriGraphicList().getOMGraphicAt(graphicIndex);
                    repaintGraphics(omg);
                }
            }
        });

        redrawAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaintGraphics(getEsriGraphicList());
            }
        });

        tableTrigger.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                showTable();
            }
        });
        return holder;
    }

    /**
     * Sets the drawing attributes to those of a particular OMGraphic.
     */
    public void setDrawingAttributes(OMGraphic omg) {
        if (drawingAttributes != null && omg != null) {
            drawingAttributes.setFrom(omg);
        }
    }

    /**
     * Repaints the currently selected OMGraphic or the OMGraphicList to the
     * current DrawingAttributes
     * 
     * @param omg the OMGraphic to repaint
     */
    private void repaintGraphics(OMGraphic omg) {
        drawingAttributes.setTo(omg);
        doPrepare();
    }

    protected JTable table = null;
    protected ListSelectionModel lsm = null;

    /**
     * Needs to be called before displaying the DbfTableModel.
     */
    public JTable getTable() {

        if (table == null) {
            lsm = new DefaultListSelectionModel();
            table = new JTable();
            table.setModel(getModel());
            table.setSelectionModel(lsm);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            lsm.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    // Ignore extra messages.
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    ListSelectionModel lsm2 = (ListSelectionModel) e.getSource();
                    if (lsm2.isSelectionEmpty()) {
                        // no rows are selected
                    } else {
                        int index = lsm2.getMinSelectionIndex();
                        selectGraphic(index);
                        getComponent().repaint();
                    }
                }
            });
        }

        return table;
    }

    /**
     * Mark a graphic as selected on the map.
     * 
     * @param index the index, from 0, of the graphic on the list.
     */
    public void selectGraphic(int index) {
        EsriGraphicList list = getEsriGraphicList();
        list.deselect();
        // Clear out the selected graphics list
        selectedGraphics.clear();
        selectGraphic(list.getOMGraphicAt(index));
        graphicIndex = index;
        list.regenerate(proj);
    }

    /**
     * Mark the graphic as selected, and generate if necessary.
     */
    public void selectGraphic(OMGraphic graphic) {
        if (graphic != null) {
            graphic.select();
            graphic.regenerate(proj);
            // Set the selected OMGraphic on the selected list.
            selectedGraphics.add(graphic);
        }
    }

    /**
     * Given a graphic, highlight its entry in the table.
     */
    public void selectEntry(OMGraphic graphic) {
        if (lsm == null) {
            getTable();
        }

        lsm.setSelectionInterval(graphicIndex, graphicIndex);
        // scroll to the appropriate row in the table
        getTable().scrollRectToVisible(getTable().getCellRect(graphicIndex,
                0,
                true));
    }

    /**
     * Show the table in its own frame.
     */
    public void showTable() {
        if (tableFrame == null) {
            String tableTitle = (this.name != null) ? this.name : "";
            tableFrame = new JFrame(tableTitle + " Shape Data Attributes");

            JScrollPane pane = new JScrollPane(getTable(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

            tableFrame.getContentPane().add(pane, BorderLayout.CENTER);

            tableFrame.setSize(400, 300);
        }

        tableFrame.setVisible(true);
        tableFrame.toFront();
    }

    /**
     * Handle a mouse click on the map.
     */
    public boolean mouseClicked(MouseEvent e) {
        EsriGraphicList list = getEsriGraphicList();
        boolean ret = false;
        graphicIndex = -1;

        if (list != null) {
            OMGraphic omg = list.selectClosest(e.getX(), e.getY(), 4);
            if (omg != null) {
                // graphicIndex has to be set before selectEntry
                // called.
                graphicIndex = list.indexOf(omg);
                selectEntry(omg);

                ret = true;
            } else {
                if (lsm == null)
                    getTable();
                lsm.clearSelection();
                list.deselect();
                selectedGraphics.clear();
                repaint();
            }
        }
        return ret;
    }

    protected Layer parentLayer = null;

    /**
     * Handle mouse moved events (Used for firing tool tip descriptions over
     * graphics)
     */
    public boolean mouseMoved(MouseEvent e) {
        EsriGraphicList list = getEsriGraphicList();
        boolean ret = false;
        if (list != null) {
            OMGraphic omg = list.findClosest(e.getX(), e.getY(), 4);
            if (omg != null) {
                int index;

                Integer I = ((Integer) omg.getAttribute(SHAPE_INDEX_ATTRIBUTE));
                if (I != null) {
                    index = I.intValue();
                } else {
                    index = list.indexOf(omg);
                }

                if (parentLayer == null) {
                    Component comp = getComponent();
                    if (comp instanceof Layer) {
                        parentLayer = (Layer) comp;
                    }
                }

                if (parentLayer != null) {
                    parentLayer.fireRequestToolTip(getDescription(index));
                }

                ret = true;
            } else if (parentLayer != null) {
                parentLayer.fireHideToolTip();
            }
        }
        return ret;
    }

    /**
     * Builds a description in HTML for a tool tip for the specified OMGraphic
     * 
     * @param index the index of the graphic in the table
     */
    public String getDescription(int index) {
        StringBuffer v = new StringBuffer();

        v.append("<HTML><BODY>");
        for (int i = 0; i < getTable().getColumnCount(); i++) {
            try {
                String column = getTable().getColumnName(i);
                String value = (String) (getTable().getValueAt(index, i) + "");
                v.append((i == 0 ? "<b>" : "<BR><b>")).append(column)
                        .append(":</b> ").append(value);
            } catch (NullPointerException npe) {
            } catch (IndexOutOfBoundsException obe) {
            }
        }

        v.append("</BODY></HTML>");

        return v.toString();
    }

    protected JPanel daGUI = null;
    protected JFrame tableFrame = null;
    /** This marks the index of the OMGraphic that is "selected" */
    protected int graphicIndex = -1;

    /**
     * DataBoundsInformer interface.
     */
    public DataBounds getDataBounds() {
        DataBounds box = null;
        if (_list == null) {
            _list = getEsriGraphicList();
        }

        if (_list != null) {
            double[] extents = _list.getExtents();
            box = new DataBounds((double) extents[1], (double) extents[0], (double) extents[3], (double) extents[2]);
        }
        return box;
    }
}
