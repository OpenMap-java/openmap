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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/shape/SpatialIndexHandler.java,v $
// $RCSfile: SpatialIndexHandler.java,v $
// $Revision: 1.13 $
// $Date: 2008/10/16 03:26:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.shape;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.dataAccess.shape.DbfHandler;
import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.BinaryFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.layer.shape.SpatialIndex.Entry;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.coords.GeoCoordTransformation;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * The SpatialIndexHandler keeps track of all the stuff dealing with a
 * particular shape file - file names, colors, etc. You can ask it to create
 * OMGraphics based on a bounding box, and make adjustments to it through its
 * GUI. This is the object to use if you just want to deal with the contents of
 * a shape file but not display them.
 */
public class SpatialIndexHandler implements PropertyConsumer {
    protected SpatialIndex spatialIndex;
    protected String shapeFileName = null;
    protected String imageURLString = null;
    protected GeoCoordTransformation coordTranslator;
    protected String prettyName = null;
    protected DrawingAttributes drawingAttributes;
    protected boolean enabled = true;
    protected boolean buffered = false;
    protected String propertyPrefix;

    public final static String EnabledProperty = "enabled";
    public final static String BufferedProperty = "buffered";

    // for internationalization
    protected I18n i18n = Environment.getI18n();

    public SpatialIndexHandler() {}

    public SpatialIndexHandler(String prefix, Properties props) {
        setProperties(prefix, props);
    }

    public static SpatialIndex create(String prefix, Properties props) {
        SpatialIndexHandler sih = new SpatialIndexHandler(prefix, props);
        return sih.getSpatialIndex();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("For ").append(prettyName).append(":\n");
        sb.append("  Shape file name: ").append(shapeFileName).append("\n");
        sb.append("  Spatal index file name: ")
                .append(SpatialIndex.ssx(shapeFileName)).append("\n");
        sb.append("  image URL: ").append(imageURLString).append("\n");
        sb.append("  drawing attributes: ").append(drawingAttributes).append("\n");
        return sb.toString();
    }

    /**
     * Get the GUI that controls the attributes of the handler.
     */
    public JComponent getGUI() {
        JPanel stuff = new JPanel();
        stuff.setBorder(BorderFactory.createRaisedBevelBorder());
        // stuff.add(new JLabel(prettyName));
        stuff.add(drawingAttributes.getGUI());

        JPanel checks = new JPanel(new GridLayout(0, 1));
        JCheckBox enableButton = new JCheckBox(i18n.get(SpatialIndexHandler.class,
                "enableButton",
                "Show"));
        enableButton.setSelected(enabled);
        enableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JCheckBox jcb = (JCheckBox) ae.getSource();
                enabled = jcb.isSelected();
            }
        });
        checks.add(enableButton);

        JCheckBox bufferButton = new JCheckBox(i18n.get(SpatialIndexHandler.class,
                "bufferButton",
                "Buffer"));
        bufferButton.setSelected(buffered);
        bufferButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                JCheckBox jcb = (JCheckBox) ae.getSource();
                buffered = jcb.isSelected();
            }
        });
        checks.add(bufferButton);
        stuff.add(checks);

        return stuff;
    }

    /** Property Consumer method. */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /** Property Consumer method. */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }

    /** Property Consumer method. */
    public void setProperties(Properties props) {
        setProperties(null, props);
    }

    /** Property Consumer method. */
    public void setProperties(String prefix, Properties props) {
        setPropertyPrefix(prefix);
        String realPrefix = PropUtils.getScopedPropertyPrefix(this);
        prettyName = props.getProperty(realPrefix + Layer.PrettyNameProperty);
        String dataPathPrefix = props.getProperty(prefix
                + Layer.DataPathPrefixProperty);

        if (dataPathPrefix != null && dataPathPrefix.length() > 0) {
            dataPathPrefix += "/";
        } else {
            dataPathPrefix = "";
        }

        shapeFileName = dataPathPrefix
                + props.getProperty(realPrefix + ShapeLayer.shapeFileProperty);

        if (shapeFileName != null && shapeFileName.endsWith(".shp")) {

            spatialIndex = SpatialIndex.locateAndSetShapeData(shapeFileName);
            String dbfFileName = SpatialIndex.dbf(shapeFileName);

            try {
                if (BinaryFile.exists(dbfFileName)) {
                    BinaryBufferedFile bbf = new BinaryBufferedFile(dbfFileName);
                    DbfHandler dbfh = new DbfHandler(bbf);
                    dbfh.setProperties(realPrefix, props);
                    spatialIndex.setDbf(dbfh);
                }
            } catch (FormatException fe) {
                if (Debug.debugging("shape")) {
                    Debug.error("ShapeLayer: Couldn't create DBF handler for "
                            + dbfFileName + ", FormatException: "
                            + fe.getMessage());
                }
            } catch (IOException ioe) {
                if (Debug.debugging("shape")) {
                    Debug.error("ShapeLayer: Couldn't create DBF handler for "
                            + dbfFileName + ", IOException: "
                            + ioe.getMessage());
                }
            }

            imageURLString = props.getProperty(realPrefix
                    + ShapeLayer.pointImageURLProperty);

            try {
                if (imageURLString != null && imageURLString.length() > 0) {
                    URL imageURL = PropUtils.getResourceOrFileOrURL(this,
                            imageURLString);
                    ImageIcon imageIcon = new ImageIcon(imageURL);
                    spatialIndex.setPointIcon(imageIcon);
                }
            } catch (MalformedURLException murle) {
                Debug.error("MultiShapeLayer.setProperties(" + realPrefix
                        + ": point image URL not so good: \n\t"
                        + imageURLString);

            } catch (NullPointerException npe) {
                // May happen if not connected to the internet.
                Debug.error("Can't access icon image: \n" + imageURLString);
            }

        } else {
            Debug.error(realPrefix + ": No shape file name provided:");
            Debug.error("\t" + realPrefix + ShapeLayer.shapeFileProperty);
        }

        drawingAttributes = new DrawingAttributes(realPrefix, props);

        enabled = PropUtils.booleanFromProperties(props, realPrefix
                + EnabledProperty, enabled);
        buffered = PropUtils.booleanFromProperties(props, realPrefix
                + BufferedProperty, buffered);

        String transClassName = props.getProperty(realPrefix
                + ShapeLayer.TransformProperty);
        if (transClassName != null) {
            try {
                coordTranslator = (GeoCoordTransformation) ComponentFactory.create(transClassName,
                        realPrefix + ShapeLayer.TransformProperty,
                        props);
            } catch (ClassCastException cce) {

            }
        }
    }

    /** Property Consumer method. */
    public Properties getProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }

        String prefix = PropUtils.getScopedPropertyPrefix(this);
        props.put(prefix + ShapeLayer.shapeFileProperty,
                (shapeFileName == null ? "" : shapeFileName));
        props.put(prefix + ShapeLayer.pointImageURLProperty,
                (imageURLString == null ? "" : imageURLString));

        if (drawingAttributes != null) {
            drawingAttributes.getProperties(props);
        } else {
            DrawingAttributes da = (DrawingAttributes) DrawingAttributes.DEFAULT.clone();
            da.setPropertyPrefix(prefix);
            da.getProperties(props);
        }
        props.put(prefix + EnabledProperty, new Boolean(enabled).toString());
        props.put(prefix + BufferedProperty, new Boolean(buffered).toString());

        if (spatialIndex != null) {
            DbfHandler dbfh = spatialIndex.getDbf();
            if (dbfh != null) {
                dbfh.getProperties(props);
            }
        }

        return props;
    }

    /** Property Consumer method. */
    public Properties getPropertyInfo(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        String interString;

        // those strings are already internationalized in ShapeLayer.
        // So only thing to do is use
        // keys and values from there.The main question is: what about
        // .class?
        // What should I use as requestor field when calling
        // i18n.get(...) ? DFD - use the ShapeLayer class, so you
        // only have to modify one properties file with the
        // translation.

        interString = i18n.get(ShapeLayer.class,
                ShapeLayer.shapeFileProperty,
                I18n.TOOLTIP,
                "Location of Shape file - .shp (File, URL or relative file path).");
        props.put(ShapeLayer.shapeFileProperty, interString);
        interString = i18n.get(ShapeLayer.class,
                ShapeLayer.shapeFileProperty,
                ShapeLayer.shapeFileProperty);
        props.put(ShapeLayer.shapeFileProperty + LabelEditorProperty,
                interString);
        props.put(ShapeLayer.shapeFileProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        // interString = i18n.get(ShapeLayer.class,
        // ShapeLayer.spatialIndexProperty,
        // I18n.TOOLTIP,
        // "Location of Spatial Index file - .ssx (File, URL or relative file
        // path).");
        // props.put(ShapeLayer.spatialIndexProperty, interString);
        // interString = i18n.get(ShapeLayer.class,
        // ShapeLayer.spatialIndexProperty,
        // ShapeLayer.spatialIndexProperty);
        // props.put(ShapeLayer.spatialIndexProperty + LabelEditorProperty,
        // interString);
        // props.put(ShapeLayer.spatialIndexProperty + ScopedEditorProperty,
        // "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        interString = i18n.get(ShapeLayer.class,
                ShapeLayer.pointImageURLProperty,
                I18n.TOOLTIP,
                "Image file to use for map location of point data (optional).");
        props.put(ShapeLayer.pointImageURLProperty, interString);
        interString = i18n.get(ShapeLayer.class,
                ShapeLayer.pointImageURLProperty,
                ShapeLayer.pointImageURLProperty);
        props.put(ShapeLayer.pointImageURLProperty + LabelEditorProperty,
                interString);
        props.put(ShapeLayer.pointImageURLProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.FUPropertyEditor");

        if (drawingAttributes != null) {
            drawingAttributes.getPropertyInfo(props);
        } else {
            DrawingAttributes.DEFAULT.getPropertyInfo(props);
        }
        interString = i18n.get(SpatialIndexHandler.class,
                EnabledProperty,
                I18n.TOOLTIP,
                "Show file contents");
        props.put(EnabledProperty, interString);
        interString = i18n.get(SpatialIndexHandler.class,
                EnabledProperty,
                EnabledProperty);
        props.put(EnabledProperty + LabelEditorProperty, interString);
        props.put(EnabledProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        interString = i18n.get(SpatialIndexHandler.class,
                BufferedProperty,
                I18n.TOOLTIP,
                "Read and hold entire file contents (may be faster)");
        props.put(BufferedProperty, interString);
        interString = i18n.get(SpatialIndexHandler.class,
                BufferedProperty,
                BufferedProperty);
        props.put(BufferedProperty + LabelEditorProperty, interString);
        props.put(BufferedProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");

        return props;
    }

    public GeoCoordTransformation getCoordTranslator() {
        return coordTranslator;
    }

    public void setCoordTranslator(GeoCoordTransformation coordTranslator) {
        this.coordTranslator = coordTranslator;
    }

    /**
     * Create the OMGraphics out of the records that fall inside the bounding
     * box.
     * 
     * @param xmin double for the min horizontal limit of the bounding box.
     * @param ymin double for the min vertical limit of the bounding box.
     * @param xmax double for the max horizontal limit of the bounding box.
     * @param ymax double for the max vertical limit of the bounding box.
     */
    public OMGraphicList getGraphics(double xmin, double ymin, double xmax,
                                     double ymax) throws IOException,
            FormatException {
        return getGraphics(xmin,
                ymin,
                xmax,
                ymax,
                (OMGraphicList) null,
                (Projection) null);
    }

    /**
     * Given a bounding box, create OMGraphics from the ESRI records in the
     * shape file.
     * 
     * @param xmin double for the min horizontal limit of the bounding box.
     * @param ymin double for the min vertical limit of the bounding box.
     * @param xmax double for the max horizontal limit of the bounding box.
     * @param ymax double for the max vertical limit of the bounding box.
     * @param list OMGraphic list to add the new OMGraphics too. If null, a new
     *        OMGraphicList will be created.
     * @return OMGraphicList containing the new OMGraphics.
     */
    public OMGraphicList getGraphics(double xmin, double ymin, double xmax,
                                     double ymax, OMGraphicList list)
            throws IOException, FormatException {
        return getGraphics(xmin, ymin, xmax, ymax, list, (Projection) null);
    }

    /**
     * Given a bounding box, create OMGraphics from the ESRI records in the
     * shape file.
     * 
     * @param xmin double for the min horizontal limit of the bounding box.
     * @param ymin double for the min vertical limit of the bounding box.
     * @param xmax double for the max horizontal limit of the bounding box.
     * @param ymax double for the max vertical limit of the bounding box.
     * @param list OMGraphic list to add the new OMGraphics too. If null, a new
     *        OMGraphicList will be created.
     * @param proj the projection to use to generate the OMGraphics.
     * @return OMGraphicList containing the new OMGraphics.
     */
    public OMGraphicList getGraphics(double xmin, double ymin, double xmax,
                                     double ymax, OMGraphicList list,
                                     Projection proj) throws IOException,
            FormatException {
        if (list == null) {
            list = new OMGraphicList();
        }

        if (!buffered) {

            // Clean up if buffering turned off.
            if (bufferedList != null) {
                bufferedList = null;
            }

            spatialIndex.getOMGraphics(xmin,
                    ymin,
                    xmax,
                    ymax,
                    list,
                    drawingAttributes,
                    proj,
                    coordTranslator);

        } else {

            if (bufferedList == null) {
                bufferedList = getWholePlanet(coordTranslator);
            }

            checkSpatialIndexEntries(xmin, ymin, xmax, ymax, list, proj);

        }

        return list;
    }

    /**
     * Checks the buffered list of OMGraphics from the shp file and figures out
     * of they intersect the provided bounds.
     * 
     * @param xmin minimum longitude, decimal degrees.
     * @param ymin minimum latitude, decimal degrees.
     * @param xmax maximum longitude, decimal degrees.
     * @param ymax maximum latitude, decimal degrees.
     * @param retList the list that passing OMGraphics will be added to.
     * @param proj the current map projection.
     */
    protected void checkSpatialIndexEntries(double xmin, double ymin,
                                            double xmax, double ymax,
                                            OMGraphicList retList,
                                            Projection proj) {
        // There should be the same number of objects in both iterators.
        Iterator<?> entryIt = spatialIndex.entries.iterator();
        Iterator<?> omgIt = bufferedList.iterator();

        OMGraphicList labels = null;
        if (spatialIndex.getDbf() != null) {
            labels = new OMGraphicList();
            retList.add(labels);
        }

        while (entryIt.hasNext() && omgIt.hasNext()) {
            Entry entry = (Entry) entryIt.next();
            OMGraphic omg = (OMGraphic) omgIt.next();
            if (entry.intersects(xmin, ymin, xmax, ymax)) {
                // We want to set attributes before the evaluate method is
                // called, since there might be special attributes set on the
                // omg based on dbf contents.
                drawingAttributes.setTo(omg);
                omg = spatialIndex.evaluate(omg, labels, proj);

                // omg can be null from the evaluate method, if the omg doesn't
                // pass proj and rule tests.
                if (omg != null) {
                    omg.generate(proj);
                    retList.add(omg);
                }
            }
        }
    }

    /**
     * Master list for buffering. Only used if buffering is enabled.
     */
    protected OMGraphicList bufferedList = null;

    /**
     * Get the graphics for the entire planet.
     */
    protected OMGraphicList getWholePlanet() throws IOException,
            FormatException {
        return getWholePlanet(coordTranslator);
    }

    /**
     * Get the graphics for the entire planet.
     */
    protected OMGraphicList getWholePlanet(GeoCoordTransformation dataTransform)
            throws IOException, FormatException {
        // Sets the entries
        spatialIndex.readIndexFile(null, dataTransform);
        return spatialIndex.getAllOMGraphics((OMGraphicList) null,
                drawingAttributes,
                (Projection) null,
                dataTransform);
    }

    public void setPrettyName(String set) {
        prettyName = set;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public void setBuffered(boolean set) {
        buffered = set;
    }

    public boolean getBuffered() {
        return buffered;
    }

    public void setDrawingAttributes(DrawingAttributes set) {
        drawingAttributes = set;
    }

    public DrawingAttributes getDrawingAttributes() {
        return drawingAttributes;
    }

    public SpatialIndex getSpatialIndex() {
        return spatialIndex;
    }

    public void setSpatialIndex(SpatialIndex spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    public String getShapeFileName() {
        return shapeFileName;
    }

    public void setShapeFileName(String shapeFileName) {
        this.shapeFileName = shapeFileName;
    }

    public String getImageURLString() {
        return imageURLString;
    }

    public void setImageURLString(String imageURLString) {
        this.imageURLString = imageURLString;
    }

    public void setEnabled(boolean set) {
        enabled = set;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public boolean close(boolean done) {
        if (spatialIndex != null) {
            return spatialIndex.close(done);
        }
        return false;
    }
}