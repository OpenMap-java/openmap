/*
 *  File: EsriShapeExport.java
 *  OptiMetrics, Inc.
 *  2107 Laurel Bush Road - Suite 209
 *  Bel Air, MD 21015
 *  (410)569 - 6081
 */
package com.bbn.openmap.dataAccess.shape;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bbn.openmap.dataAccess.shape.output.DbfOutputStream;
import com.bbn.openmap.dataAccess.shape.output.ShpOutputStream;
import com.bbn.openmap.dataAccess.shape.output.ShxOutputStream;
import com.bbn.openmap.omGraphics.BasicStrokeEditor;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMCircle;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMPoly;
import com.bbn.openmap.omGraphics.OMRangeRings;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.ArgParser;
import com.bbn.openmap.util.ColorFactory;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PropUtils;

/**
 * Provides methods for saving OMGraphicLists as ShapeFiles. This code was
 * originally submitted by Karl Stuempfle of OptiMetrics, and I modified it a
 * little to add a user interface to modify the DBF files if the user wants to.
 * <P>
 * 
 * Since Shape files can only hold one type of graphic, this class will create
 * one to three different lists as needed, for points, lines and polygons.
 * <P>
 * 
 * If the OMGraphicList's AppObject holds a DbfTableModel, it will be used for
 * the shape file database file.
 */
public class EsriShapeExport implements ShapeConstants, OMGraphicConstants {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.shape.EsriShapeExport");

    /**
     * The source graphics to write to a shape file.
     */
    protected OMGraphicList graphicList = null;

    /**
     * The optional DbfTableModel that describes properties for the OMGraphics.
     * This should be set as the DBF_ATTRIBUTE attribute of the OMGraphicList.
     */
    protected DbfTableModel masterDBF = null;

    /**
     * The projection needed to convert other OMGraphicTypes to polygons.
     */
    protected Projection projection;

    /**
     * The path where the shape files should be written.
     */
    protected String filePath;

    /**
     * Gets set automatically if Logger.isLoggable(Level.INFO);
     */
    protected boolean DEBUG = false;

    /**
     * A list of ESEInterface classes, holding information for different type
     * ESRIGraphicLists created from the OMGraphicList.
     */
    protected ArrayList<ESEInterface> eseInterfaces = new ArrayList<ESEInterface>();

    /**
     * Flag for whether the DBF file should be written when the OMGraphicList is
     * exported to a .shp/.shx file. The .dbf file will be created if set to
     * true, and this is true by default.
     */
    protected boolean writeDBF = true;

    /**
     * Flad to note whether, if a DbfTableModel is set, to add the rendering
     * information (DrawingAttributes contents) about the OMGraphics to the
     * contents of the DbfTableModel. False by default. Doesn't do anything yet.
     */
    protected boolean dbfHasRenderingInfo = false;

    /**
     * A GeoCoordTransform to use to convert Lat/Lon values in EsriGraphics to
     * projected coordinates.
     */
    protected GeoCoordTransformation transform;

    /**
     * Create an EsriShapeExport object.
     * 
     * @param list
     *            the OMGraphicList to export.
     * @param proj
     *            the Projection of the map, needed to convert some OMGraphic
     *            types to OMPolys.
     * @param pathToFile
     *            the file path of the shape file to save to. If null, the user
     *            will be queried. If not null, the files will be saved without
     *            any GUI confirmation.
     */
    public EsriShapeExport(OMGraphicList list, Projection proj, String pathToFile) {

        setGraphicList(list);
        projection = proj;
        filePath = pathToFile;
        DEBUG = logger.isLoggable(Level.FINE);
    }

    /**
     * Create an EsriShapeExport object.
     * 
     * @param list
     *            the EsriGraphicList to export.
     * @param dbf
     *            the DbfTableModel holding the attributes for the list objects.
     * @param pathToFile
     *            the file path of the shape file to save to. If null, the user
     *            will be queried. If not null, the files will be saved without
     *            any GUI confirmation.
     */
    public EsriShapeExport(EsriGraphicList list, DbfTableModel dbf, String pathToFile) {

        setGraphicList(list);
        setMasterDBF(dbf);
        filePath = pathToFile;
        DEBUG = logger.isLoggable(Level.FINE);
    }

    /**
     * Set the OMGraphicList to use for export. If the AppObject in the
     * OMGraphicList holds a DbfTableModel, it will be used in the export.
     */
    public void setGraphicList(OMGraphicList list) {
        graphicList = list;

        if (list != null) {
            Object obj = list.getAttribute(DBF_ATTRIBUTE);
            // Do this check for backward compatibility
            if (obj == null) {
                obj = list.getAppObject();
            }

            if (obj instanceof DbfTableModel) {
                masterDBF = (DbfTableModel) obj;
                logger.fine("Setting master DBF in ESE");
            }
        }
    }

    public OMGraphicList getGraphicList() {
        return graphicList;
    }

    public void setProjection(Projection proj) {
        projection = proj;
    }

    public Projection getProjection() {
        return projection;
    }

    public void setFilePath(String pathToFile) {
        filePath = pathToFile;
    }

    public String getFilePath() {
        return filePath;
    }

    public GeoCoordTransformation getTransform() {
        return transform;
    }

    public void setTransform(GeoCoordTransformation transform) {
        this.transform = transform;
    }

    protected EsriPolygonList polyList = null;
    protected EsriPolylineList lineList = null;
    protected EsriPointList pointList = null;

    /**
     * Return the polygon list, create it if needed.
     */
    protected EsriPolygonList getPolyList() {
        if (polyList == null) {
            polyList = new EsriPolygonList();
            polyList.setTable(getMasterDBFHeaderClone());
        }
        return polyList;
    }

    /**
     * Return the line list, create it if needed.
     */
    protected EsriPolylineList getLineList() {
        if (lineList == null) {
            lineList = new EsriPolylineList();
            lineList.setTable(getMasterDBFHeaderClone());
        }
        return lineList;
    }

    /**
     * Return the point list, create it if needed. If the masterDBF object
     * exists, then a new one is created, which matching structure, and put in
     * the AppObject of the new list that is returned. If there isn't a
     * masterDBF object, then the AppObject is set to null, and a default one
     * will be created.
     */
    protected EsriPointList getPointList() {
        if (pointList == null) {
            pointList = new EsriPointList();
            pointList.setTable(getMasterDBFHeaderClone());
        }
        return pointList;
    }

    /**
     * Add a graphic to the list, and add the record to the list's DbfTableModel
     * if both exist.
     */
    protected void addGraphic(EsriGraphicList egl, OMGraphic graphic, List<Object> record) {
        egl.add(graphic);
        DbfTableModel dtm = egl.getTable();
        if (dtm != null && record != null) {
            dtm.addRecord(record);
        }
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addPolygon(OMGraphic graphic, List<Object> record) {
        addGraphic(getPolyList(), graphic, record);
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addLine(OMGraphic graphic, List<Object> record) {
        addGraphic(getLineList(), graphic, record);
    }

    /** Scoping method to call addGraphic with the right list. */
    protected void addPoint(OMGraphic graphic, List<Object> record) {
        addGraphic(getPointList(), graphic, record);
    }

    /**
     * Set the DbfTableModel representing the dbf file for the main
     * OMGraphicList. Can also be passed to this object as an attribute in the
     * EsriGraphicList under the DBF_ATTRIBUTE key.
     */
    public void setMasterDBF(DbfTableModel dbf) {
        masterDBF = dbf;
    }

    /**
     * Get the DbfTableModel representing the dbf file for the main
     * OMGraphicList.
     */
    public DbfTableModel getMasterDBF() {
        return masterDBF;
    }

    /**
     * Set whether the DBF file should be written when the OMGraphicList is
     * exported to a .shp/.shx file. The .dbf file will be created if set to
     * true, and this is true by default.
     */
    public void setWriteDBF(boolean value) {
        writeDBF = value;
    }

    /**
     * Get whether the DBF file should be written when the OMGraphicList is
     * exported to a .shp/.shx file.
     */
    public boolean getWriteDBF() {
        return writeDBF;
    }

    /**
     * Get whether the DBF file should have the DrawingAttributes information
     * added to the DbfTableModel if it isn't already there.
     */
    public void setDBFHasRenderingInfo(boolean value) {
        dbfHasRenderingInfo = value;
    }

    /**
     * Get whether the DBF file should have the DrawingAttributes information
     * added to the DbfTableModel if it isn't already there.
     */
    public boolean getDBFHasRenderingInfo() {
        return dbfHasRenderingInfo;
    }

    /**
     * If the OMGraphicList has a DbfTableModel in its AppObject slot, a new
     * DbfTableModel is created that has the same structure.
     * 
     * @return DbfTableModel that matches the structure that is in the
     *         OMGraphicList AppObject.
     */
    protected DbfTableModel getMasterDBFHeaderClone() {
        if (masterDBF != null) {
            return masterDBF.headerClone();
        }
        return null;
    }

    /**
     * Gets the DbfTableModel record at the index. Used when the OMGraphicList
     * contents are being split up into different type EsriGraphicLists, and the
     * records are being split into different tables, too. Remember, the index
     * array starts at 0 for the first row.
     */
    protected List<Object> getMasterDBFRecord(int index) {
        try {
            if (masterDBF != null) {
                return masterDBF.getRecord(index);
            }
        } catch (IndexOutOfBoundsException ioobe) {
        }
        return null;
    }

    /**
     * Separates the graphics from the OMGraphicList into Polygon, Polyline and
     * Point lists, then passes the desired shape lists to their respective
     * export functions to be added to an EsriLayer of the same type and
     * prepared for export. OMGraphics that are on sublists within the top-level
     * OMGraphicList will be simply written to the appropriate list at the top
     * level of that list. They will be handled as multi-part geometries.
     * <p>
     * 
     * Separating the graphics into the three types is necessary due to shape
     * file specification limitations which will only allow shape files to be of
     * one type.
     * <P>
     * 
     * For OMGraphicLists that are actually EsriGraphicLists, this export method
     * will be redirected to a different method that will handle
     * sub-OMGraphicLists as multi-part geometries.
     * <P>
     * 
     * If you want to write out multi-part geometries and have a regular
     * OMGraphicList, you have to convert them to EsriGraphicLists first (and
     * OMGraphics to EsriGraphics), which forces you to group shapes into like
     * types (points, polylines and polygons).
     */
    public void export() {
        OMGraphicList list = getGraphicList();
        if (list == null) {
            logger.warning("no graphic list to export!");
            return;
        }

        export(list, null, true);
    }

    /**
     * A counter for graphics that are not RENDERTYPE_LATLON.
     */
    int badGraphics;

    /**
     * This method is intended to allow embedded OMGraphicLists to be handled.
     * The record should be set if the list is an embedded list, reusing a
     * record from the top level iteration. Set the record to null at the top
     * level iteration to cause the method to fetch the record from the
     * masterDBF, if it exists.
     * 
     * @param list
     *            the OMGraphicList to write
     * @param record
     *            the record for the current list, used if the list is actually
     *            a multipart geometry for the overall list. May be null anyway,
     *            though.
     * @deprecated use export(OMGraphicList, ArrayList, boolean) instead.
     * @see #export(OMGraphicList list, List masterRecord, boolean writeFiles)
     */
    protected void export(OMGraphicList list, List<Object> record) {
        export(list, record, true);
    }

    /**
     * This method is intended to allow embedded OMGraphicLists to be handled.
     * The record should be set if the list is an embedded list, reusing a
     * record from the top level iteration. Set the record to null at the top
     * level iteration to cause the method to fetch the record from the
     * masterDBF, if it exists. If the list is an EsriGraphicList, then the
     * export for EsriGraphicLists will be called. The DbfTableModel for the
     * list should be stored as an attribute in the EsriGraphicList under the
     * DBF_ATTRIBUTE key.
     * 
     * @param list
     *            the OMGraphicList to write.
     * @param masterRecord
     *            the record for the current list, used if the list is actually
     *            a multipart geometry for the overall list. May be null anyway,
     *            though.
     * @param writeFiles
     *            Flag to note when this method is being called iteratively,
     *            which is when record is not null. If it is iterative, then the
     *            writing of the files should not be performed on this round of
     *            the method call.
     */
    protected void export(OMGraphicList list, List<Object> masterRecord,
                          boolean writeFiles) {
        badGraphics = 0;

        if (list == null) {
            return;
        } else if (list instanceof EsriGraphicList) {
            export((EsriGraphicList) list);
            return;
        }

        int dbfIndex = 0;

        // parse the graphic list and fill the individual lists with
        // the appropriate shape types
        for (OMGraphic dtlGraphic : list) {

            // Reset the record to the master flag record, which will
            // cause a new record to be read for the top level list
            // contents, but duplicate the current masterRecord for
            // iterative contents.
            List<Object> record = masterRecord;

            // We're trying to be cute here. We'll ask the shape if it knows
            // what index it is. If it doesn't, we'll tell it. They should be
            // the same anyway.
            Integer recIndex = (Integer) dtlGraphic.getAttribute(SHAPE_INDEX_ATTRIBUTE);
            if (recIndex == null) {
                recIndex = dbfIndex;
            }

            if (record == null) {
                record = getMasterDBFRecord(recIndex);
            }
            dbfIndex++; // increment for the next round.

            // If we have an OMGraphicList, iterate through that one
            // as well. We're not handling multi-part geometries yet.
            if (dtlGraphic instanceof OMGraphicList) {
                if (DEBUG)
                    logger.fine("ESE: handling OMGraphicList");
                export((OMGraphicList) dtlGraphic, record, false);
                continue;
            }

            // check to be sure the graphic is rendered in LAT/LON
            if (dtlGraphic.getRenderType() != RENDERTYPE_LATLON) {
                badGraphics++;
                continue;
            }

            if (dtlGraphic instanceof OMPoly) {
                OMPoly omPoly = (OMPoly) dtlGraphic;
                // verify that this instance of OMPoly is a polygon
                if (isPolygon(omPoly)) {
                    if (DEBUG)
                        logger.fine("ESE: handling OMPoly polygon");
                    addPolygon(dtlGraphic, record);
                }
                // if it is not it must be a polyline and therefore
                // added to the line list
                else {
                    if (DEBUG)
                        logger.fine("ESE: handling OMPoly line");
                    addLine(dtlGraphic, record);
                }
            }
            // (end)if (dtlGraphic instanceof OMPoly)
            // add all other fully enclosed graphics to the polyList
            else if (dtlGraphic instanceof OMRect) {
                if (DEBUG)
                    logger.fine("ESE: handling OMRect");
                addPolygon((OMGraphic) EsriPolygonList.convert((OMRect) dtlGraphic),
                           record);
            } else if (dtlGraphic instanceof OMCircle) {
                if (DEBUG)
                    logger.fine("ESE: handling OMCircle");
                addPolygon((OMGraphic) EsriPolygonList.convert((OMCircle) dtlGraphic,
                                                               projection), record);

            } else if (dtlGraphic instanceof OMRangeRings) {
                if (DEBUG)
                    logger.fine("ESE: handling OMRangeRings");
                export(EsriPolygonList.convert((OMRangeRings) dtlGraphic, projection),
                       record, false);

            }

            // add lines to the lineList
            else if (dtlGraphic instanceof OMLine) {
                if (DEBUG)
                    logger.fine("ESE: handling OMLine");
                addLine((OMGraphic) EsriPolylineList.convert((OMLine) dtlGraphic), record);
            }
            // add points to the pointList
            else if (dtlGraphic instanceof OMPoint) {
                if (DEBUG)
                    logger.fine("ESE: handling OMPoint");
                addPoint(dtlGraphic, record);
            }
        }
        // (end)for (int i = 0; i < dtlGraphicList.size(); i++)

        if (badGraphics > 0) {
            // Information popup provider, it's OK that this gets
            // dropped.
            DrawingToolRenderException.notifyUserOfNonLatLonGraphics(badGraphics);
        }

        if (!writeFiles) {
            // Punch the stack back up so that the initial call will
            // write the files.
            return;
        }

        boolean needConfirmation = false;
        // call the file chooser if no path is given
        if (filePath == null) {
            filePath = getFilePathFromUser();

            if (filePath == null) {
                return; // User canceled.
            }

            needConfirmation = true;
        }

        if (DEBUG)
            logger.fine("ESE: writing files...");

        boolean needTypeSuffix = false;

        // (end)if (filePath == null) call the appropriate methods to
        // set up the shape files of their respective types
        if (polyList != null) {
            eseInterfaces.add(new ESEInterface(polyList, filePath, null));
            needTypeSuffix = true;
        }

        if (lineList != null) {
            eseInterfaces.add(new ESEInterface(lineList, filePath,
                    (needTypeSuffix ? LineSuffix : null)));
            needTypeSuffix = true;
        }

        if (pointList != null) {
            eseInterfaces.add(new ESEInterface(pointList, filePath,
                    (needTypeSuffix ? PointSuffix : null)));
        }

        if (needConfirmation) {
            showGUI();
        } else {
            writeFiles();
        }
    }

    public final static String LineSuffix = "Lines";
    public final static String PointSuffix = "Pts";

    /**
     * Writes out EsriGraphicLists as shape files, assumes that the
     * DbfTableModel representing the attribute data for the list objects is
     * stored as an attribute in the EsriGraphicList under the DBF_ATTRIBUTE
     * key. This export handles multi-part geometries, because it's assumed that
     * the sorting of the graphic types have been handled and that any sub-lists
     * are meant to be multi-part geometries. If the filePath hasn't been set in
     * the EsriShapeExport class, the user will be asked to provide it.
     */
    protected void export(EsriGraphicList egList) {

        Object obj = egList.getAttribute(DBF_ATTRIBUTE);
        // Backward compatibility
        if (obj == null) {
            obj = egList.getAppObject();
        }

        if (obj == null) {
            egList.putAttribute(DBF_ATTRIBUTE, getMasterDBF());
            // egList.setAppObject(getMasterDBF());
        }

        eseInterfaces.add(new ESEInterface(egList, filePath, null));
        writeFiles();
    }

    /**
     * The the Iterator of ESEIterators.
     */
    protected Iterator<ESEInterface> getInterfaces() {
        return eseInterfaces.iterator();
    }

    /**
     * Just write the files from the ESEInterfaces.
     */
    protected void writeFiles() {
        for (ESEInterface eseInterface : eseInterfaces) {
            eseInterface.write();
        }
    }

    protected JFrame frame = null;

    /**
     * Show the GUI for saving the Shape files.
     */
    public void showGUI() {

        if (frame == null) {
            frame = new JFrame("Saving Shape Files");

            frame.getContentPane().add(getGUI(), BorderLayout.CENTER);
            // frame.setSize(400, 300);
            frame.pack();
        }

        frame.setVisible(true);
    }

    /**
     * Hide the Frame holding the GUI.
     */
    public void hideGUI() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    /**
     * Create the GUI for managing the different ESEIterators.
     */
    public Component getGUI() {

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel interfacePanel = new JPanel();
        interfacePanel.setLayout(new GridLayout(0, 1));

        int count = 0;
        for (ESEInterface eseInterface : eseInterfaces) {
            interfacePanel.add(eseInterface.getGUI());
            count++;
        }
        panel.add(interfacePanel, BorderLayout.CENTER);

        if (count > 1) {
            JLabel notification = new JLabel("  " + count + " Shape file sets needed:");
            panel.add(notification, BorderLayout.NORTH);
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                writeFiles();
                hideGUI();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                hideGUI();
            }
        });
        JPanel controlPanel = new JPanel();
        controlPanel.add(saveButton);
        controlPanel.add(cancelButton);
        panel.add(controlPanel, BorderLayout.SOUTH);
        return panel;
    }

    /**
     * Prepares and returns a 7 column DbfTableModel to accept input for columns
     * of TYPE_CHARACTER. <br>
     * <br>
     * The default model used holds most of the DrawingAttributes of the
     * OMGraphics.
     * 
     * 
     * @param list
     *            the EsriGraphicList to create a DbfTableModel from.
     * @return The completed DbfTableModel.
     */
    public static DbfTableModel createDefaultModel(EsriGraphicList list) {
        if (logger.isLoggable(Level.FINE))
            logger.fine("ESE: creating DbfTableModel");

        DbfTableModel _model = new DbfTableModel(7);
        // Setup table structure
        // column 0
        // The first parameter, 0, represents the first column
        _model.setLength(0, (byte) 50);
        _model.setColumnName(0, SHAPE_DBF_DESCRIPTION);
        _model.setType(0, (byte) DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(0, (byte) 0);
        // column 1
        // The first parameter, 1, represents the second column
        _model.setLength(1, (byte) 10);
        _model.setColumnName(1, SHAPE_DBF_LINECOLOR);
        _model.setType(1, (byte) DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(1, (byte) 0);
        // column2
        // The first parameter, 2, represents the third column
        _model.setLength(2, (byte) 10);
        _model.setColumnName(2, SHAPE_DBF_FILLCOLOR);
        _model.setType(2, (byte) DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(2, (byte) 0);
        // column3
        // The first parameter, 3, represents the fourth column
        _model.setLength(3, (byte) 10);
        _model.setColumnName(3, SHAPE_DBF_SELECTCOLOR);
        _model.setType(3, (byte) DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(3, (byte) 0);
        // column4
        // The first parameter, 4, represents the fifth column
        _model.setLength(4, (byte) 4);
        _model.setColumnName(4, SHAPE_DBF_LINEWIDTH);
        _model.setType(4, (byte) DbfTableModel.TYPE_NUMERIC);
        _model.setDecimalCount(4, (byte) 0);
        // column5
        // The first parameter, 5, represents the sixth column
        _model.setLength(5, (byte) 20);
        _model.setColumnName(5, SHAPE_DBF_DASHPATTERN);
        _model.setType(5, (byte) DbfTableModel.TYPE_CHARACTER);
        _model.setDecimalCount(5, (byte) 0);
        // column6
        // The first parameter, 6, represents the seventh column
        _model.setLength(6, (byte) 10);
        _model.setColumnName(6, SHAPE_DBF_DASHPHASE);
        _model.setType(6, (byte) DbfTableModel.TYPE_NUMERIC);
        _model.setDecimalCount(6, (byte) 4);

        // At a later time, more stroke parameters can be added, like
        // dash phase, end cap, line joins, and dash pattern.
        // While we're here, add index attribute into OMGraphics if they don't
        // have them.
        int count = 0;
        for (OMGraphic omg : list) {
            Object index = omg.getAttribute(SHAPE_INDEX_ATTRIBUTE);
            if (index == null) {
                index = new Integer(count);
                omg.putAttribute(SHAPE_INDEX_ATTRIBUTE, index);
            }
            count++;

            List<Object> record = new ArrayList<Object>();

            // Description
            Object obj = omg.getAppObject();
            if (obj instanceof String) {
                record.add(obj);
            } else {
                record.add("");
            }

            record.add(ColorFactory.getHexColorString(omg.getLineColor()));
            record.add(ColorFactory.getHexColorString(omg.getFillColor()));
            record.add(ColorFactory.getHexColorString(omg.getSelectColor()));
            BasicStroke bs = (BasicStroke) omg.getStroke();
            record.add(new Double(bs.getLineWidth()));
            String dp = BasicStrokeEditor.dashArrayToString(bs.getDashArray());
            if (dp == BasicStrokeEditor.NONE) {
                dp = "";
            }
            record.add(dp);
            record.add(new Double(bs.getDashPhase()));
            _model.addRecord(record);
            if (logger.isLoggable(Level.FINER))
                logger.finer("ESE: adding record: " + record);
        }

        return _model;
    }

    public static void syncDrawingAttributesToTableModel(DbfTableModel model,
                                                         OMGraphicList list,
                                                         boolean clearUpdatedStatus) {

        int count = 0;

        for (OMGraphic omg : list) {
            Object indexObj = omg.getAttribute(SHAPE_INDEX_ATTRIBUTE);
            if (indexObj == null || !(indexObj instanceof Integer)) {
                indexObj = new Integer(count);
                omg.putAttribute(SHAPE_INDEX_ATTRIBUTE, indexObj);
            }
            count++;
            int index = ((Integer) indexObj).intValue();
            Object updatedStatus = omg.getAttribute(OMGraphicConstants.UPDATED);
            if (updatedStatus == Boolean.TRUE) {
                List<Object> record = model.getRecord(index);

                index = model.getColumnIndexForName(SHAPE_DBF_LINECOLOR);
                if (index >= 0) {
                    record.set(index, ColorFactory.getHexColorString(omg.getLineColor()));
                }
                index = model.getColumnIndexForName(SHAPE_DBF_FILLCOLOR);
                if (index >= 0) {
                    record.set(index, ColorFactory.getHexColorString(omg.getFillColor()));
                }
                index = model.getColumnIndexForName(SHAPE_DBF_SELECTCOLOR);
                if (index >= 0) {
                    record.set(index,
                               ColorFactory.getHexColorString(omg.getSelectColor()));
                }

                BasicStroke bs = (BasicStroke) omg.getStroke();
                index = model.getColumnIndexForName(SHAPE_DBF_LINEWIDTH);
                if (index >= 0) {
                    record.set(index, new Double(bs.getLineWidth()));
                }
                String dp = BasicStrokeEditor.dashArrayToString(bs.getDashArray());
                if (dp == BasicStrokeEditor.NONE) {
                    dp = "";
                }
                index = model.getColumnIndexForName(SHAPE_DBF_DASHPATTERN);
                if (index >= 0) {
                    record.set(index, dp);
                }
                index = model.getColumnIndexForName(SHAPE_DBF_DASHPHASE);
                if (index >= 0) {
                    record.set(index, new Double(bs.getDashPhase()));
                }

                if (logger.isLoggable(Level.FINER))
                    logger.finer("ESE: updating record for OMGraphic: " + indexObj);

                if (clearUpdatedStatus) {
                    omg.removeAttribute(OMGraphicConstants.UPDATED);
                }
            }
        }
    }

    /**
     * Takes an OMPoly as the parameter and checks whether or not it is a
     * polygon or polyline. <br>
     * <br>
     * This method incorporates the OMPoly.isPolygon() method which returns true
     * if the fill color is not clear, but also checks the first set and last
     * set of lat/lon points of the float[] defined by OMPoly.getLatLonArray().
     * Returns true for a polygon and false for a polyline.
     * 
     * @param omPoly
     *            the OMPoly object to be verified
     * @return The polygon value
     */
    public static boolean isPolygon(OMPoly omPoly) {
        boolean isPolygon = false;
        // get the array of lat/lon points
        double[] points = omPoly.getLatLonArray();
        int i = points.length;

        // compare the first and last set of points, equal points
        // verifies a polygon.
        if (points[0] == points[i - 2] && points[1] == points[i - 1]) {

            isPolygon = true;
        }
        // check OMPoly's definition of a polygon
        if (omPoly.isPolygon()) {
            isPolygon = true;
        }

        return isPolygon;
    }

    /**
     * Generic error handling, puts up an error window.
     */
    protected void handleException(Exception e) {
        // System.out.println(e);
        StringBuffer sb = new StringBuffer("ShapeFile Export Error:");
        sb.append("\nProblem with creating the shapefile set.");
        sb.append("\n").append(e.toString());

        JOptionPane.showMessageDialog(null, sb.toString(), "ESRI Shape Export to File",
                                      JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    /**
     * Fetches a file path from the user, via a JFileChooser. Returns null if
     * the user cancels.
     * 
     * @see com.bbn.openmap.util.FileUtils#getFilePathToSaveFromUser(String)
     */
    public String getFilePathFromUser() {
        return FileUtils.getFilePathToSaveFromUser("Select Name for Shape File Set...");
    }

    /**
     * The main function is a test, reads in a Shape file (with the .shx and
     * .dbf files) and writes them back out.
     */
    public static void main(String[] argv) {

        ArgParser ap = new ArgParser("EsriShapeExport");
        ap.add("shp", "A URL to a shape file (.shp).", 1);

        if (argv.length < 1) {
            ap.bail("", true);
        }

        ap.parse(argv);

        String[] files = ap.getArgValues("shp");
        if (files != null && files[0] != null) {
            String shp = files[0];
            String dbf = null;

            try {
                dbf = shp.substring(0, shp.lastIndexOf('.') + 1) + PARAM_DBF;

                DbfTableModel model = DbfTableModel.getDbfTableModel(PropUtils.getResourceOrFileOrURL(dbf));
                EsriGraphicList list = EsriGraphicList.getEsriGraphicList(
                                                                          PropUtils.getResourceOrFileOrURL(shp),
                                                                          null, null);

                logger.info(list.getDescription());

                EsriShapeExport ese = new EsriShapeExport(list, model, null);
                ese.export();

            } catch (MalformedURLException murle) {
                logger.warning("EsriShapeExport: Malformed URL Exception\n"
                        + murle.getMessage());
            } catch (NullPointerException npe) {
                logger.warning("EsriShapeExport: Path to shape file isn't good enough to find .dbf file and .shx file.");
            } catch (Exception exception) {
                logger.warning("EsriShapeExport: Exception\n" + exception.getMessage());
                exception.printStackTrace();
            }

        } else {
            ap.bail("Need a path to a Shape file (.shp)", true);
        }
        System.exit(0);
    }

    public static OMGraphicList read(URL shpFileURL, DrawingAttributes drawingAttributes,
                                     GeoCoordTransformation coordTransform) {
        OMGraphicList ret = null;

        if (shpFileURL != null) {
            EsriGraphicList regList = EsriGraphicList.getEsriGraphicList(
                                                                         shpFileURL,
                                                                         drawingAttributes,
                                                                         coordTransform);
            ret = new OMGraphicList();
            ret.addAll(regList);

            DbfTableModel regDbf = (DbfTableModel) regList.getAttribute(DBF_ATTRIBUTE);
            ret.putAttribute(DBF_ATTRIBUTE, regDbf);

            // Now check for lines and pnts files
            String shpFilePath = shpFileURL.getPath();
            if (shpFilePath.endsWith(".shp")) {
                String linesPath = shpFilePath.replace(".shp", "_" + LineSuffix + ".shp");
                String pntsPath = shpFilePath.replace(".shp", "_" + PointSuffix + ".shp");

                logger.fine("looking for " + linesPath + " and " + pntsPath);

                try {
                    URL linesURL = PropUtils.getResourceOrFileOrURL(linesPath);
                    if (linesURL != null) {
                        EsriGraphicList lineList = EsriGraphicList.getEsriGraphicList(
                                                                                      linesURL,
                                                                                      drawingAttributes,
                                                                                      coordTransform);
                        if (lineList != null) {
                            DbfTableModel dbf = (DbfTableModel) lineList.getAttribute(DBF_ATTRIBUTE);
                            if (regDbf != null && dbf != null && regDbf.matches(dbf)) {
                                regDbf.append(dbf);
                                ret.addAll(lineList);
                            }
                        }
                    }
                    URL pntsURL = PropUtils.getResourceOrFileOrURL(pntsPath);
                    if (pntsURL != null) {
                        EsriGraphicList pntsList = EsriGraphicList.getEsriGraphicList(
                                                                                      pntsURL,
                                                                                      drawingAttributes,
                                                                                      coordTransform);
                        if (pntsList != null) {
                            DbfTableModel dbf = (DbfTableModel) pntsList.getAttribute(DBF_ATTRIBUTE);
                            if (regDbf != null && dbf != null && regDbf.matches(dbf)) {
                                regDbf.append(dbf);
                                ret.addAll(pntsList);
                            }
                        }
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /**
     * A helper class to manage a specific instance of a EsriGraphicList, it's
     * data model, etc. Provides a GUI to display and change the name of the
     * file, and the DbfTableModel GUI, and also writes the files out.
     */
    public class ESEInterface {

        protected EsriGraphicList list;
        protected DbfTableModel model;
        protected String suffix;
        protected String filePath;

        File shpFile = null;
        File shxFile = null;
        File dbfFile = null;

        protected JTextField filePathField;

        public ESEInterface(EsriGraphicList eglist, String filePathString,
                String fileNameSuffix) {
            list = eglist;
            filePath = filePathString;

            model = eglist.getTable();

            if (model == null) {
                model = createDefaultModel(list);
            } else {
                syncDrawingAttributesToTableModel(model, list, true);
            }
            model.setWritable(true);

            suffix = (fileNameSuffix == null ? "" : "_" + fileNameSuffix);
            if (DEBUG) {
                logger.fine("suffix being used for " + filePathString + ": " + suffix);
            }
        }

        public Component getGUI() {
            JPanel panel = new JPanel();

            int type = list.getType();
            String sectionTitle;
            switch (type) {
            case (SHAPE_TYPE_POINT):
                sectionTitle = "Point Shape File";
                break;
            case (SHAPE_TYPE_POLYLINE):
                sectionTitle = "Line Shape File";
                break;
            case (SHAPE_TYPE_POLYGON):
                sectionTitle = "Polygon Shape File";
                break;
            default:
                sectionTitle = "Shape File";
            }

            panel.setBorder(BorderFactory.createTitledBorder(
                                                             BorderFactory.createEtchedBorder(),
                                                             sectionTitle));

            panel.setLayout(new GridLayout(0, 1));
            JPanel pathPanel = new JPanel();
            filePathField = new JTextField(20);
            filePathField.setText(filePath + suffix);
            JButton filePathChooserLauncher = new JButton("Change Path");
            filePathChooserLauncher.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setFilePath(getFilePathFromUser());
                }
            });

            panel.add(filePathField);

            JButton editDBFButton = new JButton("Edit the Attribute File...");
            editDBFButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    model.showGUI(getFilePath() + " Attributes", DbfTableModel.DONE_MASK
                            | DbfTableModel.MODIFY_COLUMN_MASK);
                }
            });

            pathPanel.add(editDBFButton);
            pathPanel.add(filePathChooserLauncher);

            panel.add(pathPanel);

            return panel;
        }

        protected void setFilePath(String path) {
            filePath = path;
        }

        public void write() {

            if (filePathField != null) {
                filePath = filePathField.getText();
            }

            if (filePath == null) {
                filePath = getFilePathFromUser();
                if (filePath == null) {
                    return;
                }
            }

            // This helps when a file chooser has been used to pick a file to be
            // replaced.
            if (filePath.endsWith(".shp") || filePath.endsWith(".shx")
                    || filePath.endsWith(".dbf")) {
                filePath = filePath.substring(0, filePath.length() - 4);
            }

            filePath += suffix;

            shpFile = new File(filePath + ".shp");
            shxFile = new File(filePath + ".shx");
            dbfFile = new File(filePath + ".dbf");

            try {

                // create an esriGraphicList and export it to the
                // shapefile set
                if (DEBUG)
                    logger.fine("ESE writing: " + list.size() + " elements in "
                            + shpFile.getAbsolutePath());

                ShpOutputStream pos = new ShpOutputStream(new FileOutputStream(shpFile));

                if (transform != null) {
                    pos.setTransform(transform);
                }

                int[][] indexData = pos.writeGeometry(list);

                ShxOutputStream xos = new ShxOutputStream(new FileOutputStream(shxFile));
                xos.writeIndex(indexData, list.getType());

                if (getWriteDBF()) {
                    FileOutputStream fos = new FileOutputStream(dbfFile);
                    DbfOutputStream dos = new DbfOutputStream(fos);
                    dos.writeModel(model);
                    fos.close();
                }

            } catch (Exception e) {
                handleException(e);
            }
        }

    }
}
