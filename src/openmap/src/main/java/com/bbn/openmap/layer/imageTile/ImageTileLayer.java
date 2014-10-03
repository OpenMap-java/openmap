//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: ImageTileLayer.java,v $
//$Revision: 1.4 $
//$Date: 2009/01/21 01:24:42 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.layer.imageTile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import com.bbn.openmap.Environment;
import com.bbn.openmap.I18n;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.dataAccess.image.ErrImageTile;
import com.bbn.openmap.dataAccess.image.ImageReader;
import com.bbn.openmap.dataAccess.image.ImageReaderLoader;
import com.bbn.openmap.dataAccess.image.ImageTile;
import com.bbn.openmap.event.LayerStatusEvent;
import com.bbn.openmap.gui.LayerControlButtonPanel;
import com.bbn.openmap.gui.LayersPanel;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.tools.icon.BasicIconPart;
import com.bbn.openmap.tools.icon.IconPart;
import com.bbn.openmap.tools.icon.IconPartList;
import com.bbn.openmap.tools.icon.OMIconFactory;
import com.bbn.openmap.util.ComponentFactory;
import com.bbn.openmap.util.PropUtils;

/**
 * The ImageTileLayer is a layer that manages georeferenced images over a map.
 * The layer uses ImageReaders to figure out how to load images from a file,
 * create an ImageTile object from the image data, and deduce where the
 * ImageTile should be located from the information provided with/in the image
 * data.
 * <P>
 * 
 * ImageReaderLoader objects are held by the layer to assist in finding the
 * appropriate ImageReader for an image file.
 * <P>
 * 
 * The properties for this layer are:
 * 
 * <pre>
 * # semi-colon separated paths to image files or directories containing images
 * imageTileLayer.imageFilePath=path/to/file1;path/to/directory;path/to/file2
 *                        
 * # optional - image cache size specifies how many images will be held in memory for fast retrieval.
 * imageTileLayer.imageCacheSize=20
 *                         
 * # optional - image cutoff ratio specifies the scale that images will not load when the projection is zoomed out from it.
 * imageTileLayer.imageCutoffRatio=5
 *                     
 * # optional - image Reader loaders specify which image files are handled
 * imageTileLayer.imageReaderLoaders=geotiff
 * imageTileLayer.geotiff=com.bbn.openmap.dataAccess.image.geotiff.GeoTIFFImageReader.Loader
 *                       
 * # optional - Drawing attributes properties for image highlighting
 * imageTileLayer.lineWidth=2
 * imageTileLayer.selectColor=FFFFFF00
 * </pre>
 * 
 * @author dietrick
 * 
 */
public class ImageTileLayer extends OMGraphicHandlerLayer {

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger("com.bbn.openmap.layer.imageTile.ImageTileLayer");

    public final static String ImageFilePathProperty = "imageFilePath";
    public final static String ImageReaderLoadersProperty = "imageReaderLoaders";
    public final static String ImageCacheSizeProperty = "imageCacheSize";
    public final static String ImageCutoffRatioProperty = "imageCutoffRatio";

    protected String SHOW_TILES_TITLE;
    protected String HIDE_TILES_TITLE;

    protected Vector<String> filePaths;

    protected Vector<ImageReaderLoader> imageReaderLoaders;

    protected ImageTile.Cache imageCache;

    protected DrawingAttributes selectedDrawingAttributes = DrawingAttributes.getDefaultClone();

    /**
     * Default constructor for layer, initializes tile cache.
     * 
     */
    public ImageTileLayer() {

        configureImageReaderLoaders();
        imageCache = new ImageTile.Cache();

        SHOW_TILES_TITLE = i18n.get(ImageTileLayer.class, "showTilesButton", "Show");
        HIDE_TILES_TITLE = i18n.get(ImageTileLayer.class, "hideTilesButton", "Hide");
    }

    /**
     * PropertyConsumer interface method.
     */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);
        selectedDrawingAttributes.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);
        filePaths = PropUtils.parseMarkers(props.getProperty(prefix + ImageFilePathProperty), ";");

        imageCache.resetCache(PropUtils.intFromProperties(props, prefix + ImageCacheSizeProperty, imageCache.getCacheSize()));

        imageCache.setCutoffScaleRatio(PropUtils.floatFromProperties(props, prefix
                + ImageCutoffRatioProperty, imageCache.getCutoffScaleRatio()));

        String imageReaderLoaderString = props.getProperty(prefix + ImageReaderLoadersProperty);

        if (imageReaderLoaders == null) {
            imageReaderLoaders = new Vector<ImageReaderLoader>();
        }

        if (imageReaderLoaderString != null) {
            imageReaderLoaders.clear();
            Vector<String> idls = PropUtils.parseSpacedMarkers(imageReaderLoaderString);
            for (String idlMarkerName : idls) {
                String idlClassName = props.getProperty(prefix + idlMarkerName);

                Object obj = ComponentFactory.create(idlClassName);
                if (obj instanceof ImageReaderLoader) {
                    imageReaderLoaders.add((ImageReaderLoader) obj);
                }
            }
        }
    }

    /**
     * Internal callback method for subclasses to use to be able to configure
     * imageReaderLoader Vector with specific ImageReaderLoaders. By default,
     * loads GeoTIFFImageReader.Loader.
     */
    protected void configureImageReaderLoaders() {
        imageReaderLoaders = new Vector<ImageReaderLoader>();

        ImageReaderLoader idl = (ImageReaderLoader) ComponentFactory.create("com.bbn.openmap.dataAccess.image.geotiff.GeoTIFFImageReaderLoader");

        if (idl != null) {
            imageReaderLoaders.add(idl);
        } else {
            logger.warning("ImageTileLayer needs JAI installed in order to use GeoTIFF Image Reader.");
        }

        idl = (ImageReaderLoader) ComponentFactory.create("com.bbn.openmap.dataAccess.image.WorldFileImageReaderLoader");
        if (idl != null) {
            imageReaderLoaders.add(idl);
        } else {
            logger.warning("ImageTileLayer needs JAI installed in order to use World File Image Reader.");
        }
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);
        selectedDrawingAttributes.getProperties(props);
        String prefix = PropUtils.getScopedPropertyPrefix(this);
        OMGraphicList list = getList();
        if (list != null) {
            StringBuffer buf = new StringBuffer();
            for (OMGraphic omg : list) {
                if (buf.length() > 0) {
                    buf.append(";");
                }

                ImageTile imageTile = (ImageTile) omg;
                String filePath = (String) imageTile.getAttribute(FILE_PATH_ATTRIBUTE);
                if (filePath != null) {
                    buf.append(filePath);
                }
            }
            props.put(prefix + ImageFilePathProperty, buf.toString());
        }

        if (imageReaderLoaders != null) {
            int count = 0;
            StringBuffer sbuf = null;
            for (ImageReaderLoader idl : imageReaderLoaders) {
                props.put(prefix + "idl" + count, idl.getClass().getName());

                if (sbuf == null) {
                    sbuf = new StringBuffer("idl").append(count);
                } else {
                    // Space separated for parsing on input
                    sbuf.append(" idl").append(count);
                }
            }

            if (sbuf != null) {
                props.put(prefix + ImageReaderLoadersProperty, sbuf.toString());
            }
        }

        props.put(prefix + ImageCacheSizeProperty, Integer.toString(imageCache.getCacheSize()));
        props.put(prefix + ImageCutoffRatioProperty, Float.toString(imageCache.getCutoffScaleRatio()));

        return props;
    }

    /**
     * PropertyConsumer interface method.
     */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        selectedDrawingAttributes.getPropertyInfo(props);

        PropUtils.setI18NPropertyInfo(i18n, props, ImageTileLayer.class, ImageFilePathProperty, "Images", "A list of images or directories to display (separated by ;).", "com.bbn.openmap.util.propertyEditor.MultiDirFilePropertyEditor");

        PropUtils.setI18NPropertyInfo(i18n, props, ImageTileLayer.class, ImageCacheSizeProperty, "Cache Size", "Number of images to keep in cache.", null);

        PropUtils.setI18NPropertyInfo(i18n, props, ImageTileLayer.class, ImageCutoffRatioProperty, "Cutoff Scale", "Projection scale where larger values won't cause images to be loaded and displayed.", null);

        String dummyMarker = PropUtils.getDummyMarkerForPropertyInfo(getPropertyPrefix(), null);

        PropUtils.setI18NPropertyInfo(i18n, props, ImageTileLayer.class, dummyMarker, "Highlight Settings", "Settings for annotations on highlighted images.", "com.bbn.openmap.omGraphics.DrawingAttributesPropertyEditor");

        props.put(initPropertiesProperty, ImageFilePathProperty + " " + ImageCacheSizeProperty
                + " " + ImageCutoffRatioProperty + " " + dummyMarker);
        return props;
    }

    /**
     * OMGraphicHandlerLayer method called when projection changes or when
     * doPrepare() is called. If the internal OMGraphicList is null the image
     * file paths will be used to read image files.
     */
    public synchronized OMGraphicList prepare() {
        OMGraphicList list = getList();

        if (list == null) {
            list = new OMGraphicList();
            setList(list);
            Thread loadThread = new Thread() {
                public void run() {
                    loadImages();
                    fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
                }
            };
            loadThread.start();
        } else {
            list.generate(getProjection());
        }

        return list;
    }

    /**
     * Gets the filePaths and loads the images found in those places. Should be
     * called in a non-AWT thread.
     * 
     * @return OMGraphicList retrieved from getList(), or a new list of that
     *         list is null.
     */
    protected OMGraphicList loadImages() {
        clearImageTileList();

        OMGraphicList ret = getList();

        if (ret == null) {
            ret = new OMGraphicList();
            setList(ret);
        } else {
            ret.clear();
        }

        if (filePaths != null) {
            for (String path : filePaths) {
                loadImage(path, ret);
            }
        }
        return ret;
    }

    /**
     * If filePath is a file, the ImageReaderLoaders are used to try to load and
     * place the image. If filePath is a directory, this method is called for
     * each file contained within. ImageTile objects are created from the image
     * files.
     * 
     * @param filePath
     * @param ret The OMGraphicList to add any ImageTiles to.
     */
    protected void loadImage(String filePath, OMGraphicList ret) {

        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            String[] files = file.list();
            for (int i = 0; i < files.length; i++) {
                loadImage(filePath + "/" + files[i], ret);
            }
        } else {

            fireStatusUpdate(LayerStatusEvent.START_WORKING);

            try {
                URL fileURL = PropUtils.getResourceOrFileOrURL(filePath);
                if (fileURL != null) {
                    if (imageReaderLoaders != null) {
                        ImageTile imageTile = null;
                        for (ImageReaderLoader idl : imageReaderLoaders) {
                            if (idl.isLoadable(filePath)) {
                                ImageReader id = idl.getImageReader(fileURL);
                                ImageTile tmpImageTile = id.getImageTile(imageCache);

                                if (imageTile == null) {
                                    imageTile = tmpImageTile;
                                } else if (tmpImageTile != null
                                        && imageTile instanceof ErrImageTile) {
                                    imageTile = tmpImageTile;
                                }

                                if (imageTile != null && !(imageTile instanceof ErrImageTile)) {
                                    break;
                                }
                            }
                        }

                        // Need to check for null in case none of the
                        // ImageReaders could handle the file.
                        if (imageTile != null) {
                            addImageToLists(imageTile, ret, fileURL);
                        }

                    } else {
                        logger.warning("ImageReaders not configured in " + getName()
                                + " ImageTileLayer.");
                    }
                } else {
                    logger.warning("Can't get URL from " + filePath);
                }

            } catch (MalformedURLException murle) {
            }
        }
    }

    /**
     * A method to handle a newly created ImageTile object from the loadImage
     * method.
     * 
     * @param imageTile The new ImageTile
     * @param ret An OMGraphicList to add the ImageTile to.
     * @param fileURL A URL describing the location of the source image file.
     */
    protected void addImageToLists(ImageTile imageTile, OMGraphicList ret, URL fileURL) {
        imageTile.generate(getProjection());
        ret.add(imageTile);
        addImageTileToList(imageTile);

        imageTile.putAttribute(FILE_PATH_ATTRIBUTE, fileURL.getPath());
        // Probably need to check for the last slash
        // and grab that part.
        imageTile.putAttribute(NAME_ATTRIBUTE, fileURL.getFile());

        selectedDrawingAttributes.setTo(imageTile);

        // Let's just assume that we're working with
        // the main list here at the top level, and we
        // can paint the images we have.
        repaint();
        if (resultsList != null) {
            resultsList.repaint();
        }
    }

    public final static String NAME_ATTRIBUTE = "NAME";
    public final static String FILE_PATH_ATTRIBUTE = "FILE_PATH";

    protected JPanel itPanel;
    protected JList resultsList;
    protected DefaultListModel listModel;
    protected ListManager listManager;

    JButton showHideButton;
    JButton gotoButton;
    JToggleButton locateButton;

    ImageControlButtonPanel icbp;

    /**
     * Gets the gui controls associated with the layer.
     * 
     * @return IPanel for layer controls.
     */
    public Component getGUI() {
        if (itPanel == null) {
            itPanel = new JPanel();
            GridBagConstraints c = new GridBagConstraints();
            GridBagLayout gridbag = new GridBagLayout();
            itPanel.setLayout(gridbag);

            icbp = new ImageControlButtonPanel();
            gridbag.setConstraints(icbp, c);

            itPanel.add(icbp);

            resultsList = new JList(getListModel());
            resultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            listManager = new ListManager();
            resultsList.addListSelectionListener(listManager);
            resultsList.addMouseListener(listManager);
            resultsList.addMouseMotionListener(listManager);
            resultsList.setCellRenderer(new ImageListCellRenderer());

            JScrollPane listScrollPane = new JScrollPane(resultsList);
            listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0f;
            c.weighty = 1.0f;
            c.insets = new Insets(5, 5, 5, 5);
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(listScrollPane, c);
            itPanel.add(listScrollPane);

            JPanel buttonPanel = new JPanel();
            GridBagLayout bGridbag = new GridBagLayout();
            GridBagConstraints bc = new GridBagConstraints();

            showHideButton = new JButton(HIDE_TILES_TITLE);
            showHideButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    showHideTiles(((JButton) ae.getSource()).getText(), getSelectedTiles());
                }
            });
            bGridbag.setConstraints(showHideButton, bc);
            buttonPanel.add(showHideButton);

            gotoButton = new JButton(i18n.get(ImageTileLayer.class, "gotoButton", "Go To"));
            gotoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    goTo(getSelectedTiles());
                }
            });
            bGridbag.setConstraints(gotoButton, bc);
            buttonPanel.add(gotoButton);

            locateButton = new JToggleButton(i18n.get(ImageTileLayer.class, "locateButton", "Highlight"));
            locateButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JToggleButton jtb = (JToggleButton) ae.getSource();
                    setSelection(getSelectedTiles(), jtb.isSelected());
                }
            });
            bGridbag.setConstraints(locateButton, bc);
            buttonPanel.add(locateButton);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            c.weighty = 0f;
            c.insets = new Insets(0, 5, 5, 5);
            gridbag.setConstraints(buttonPanel, c);
            itPanel.add(buttonPanel);

            setGUIButtonEnableState(false);
        }

        return itPanel;
    }

    /**
     * A modified LayerControlButtonPanel that is used to control which image
     * files are available and their display order relative to each other on the
     * image stack.
     * 
     * @author dietrick
     */
    class ImageControlButtonPanel extends LayerControlButtonPanel {

        private static final long serialVersionUID = 1L;

        public ImageControlButtonPanel() {
            super();

            add = new JButton(addgif);
            add.setActionCommand(LayersPanel.LayerAddCmd);
            add.setToolTipText(i18n.get(ImageTileLayer.class, "addImage", I18n.TOOLTIP, "Add images(s)"));
            add.addActionListener(ImageControlButtonPanel.this);
            add(add);

            // Fix the tooltips:
            delete.setToolTipText(i18n.get(ImageTileLayer.class, "deleteImage", I18n.TOOLTIP, "Remove image(s)"));
            delete.setEnabled(true);
            top.setToolTipText(i18n.get(ImageTileLayer.class, "moveImageToTop", I18n.TOOLTIP, "Move selected image(s) to top"));
            up.setToolTipText(i18n.get(ImageTileLayer.class, "moveImageUp", I18n.TOOLTIP, "Move selected image(s) up"));
            down.setToolTipText(i18n.get(ImageTileLayer.class, "moveImageDown", "Move selected image(s) down"));
            bottom.setToolTipText(i18n.get(ImageTileLayer.class, "moveImageToBottom", I18n.TOOLTIP, "Move selected image(s) to bottom"));
        }

        public void actionPerformed(ActionEvent ae) {
            String cmd = ae.getActionCommand();

            if (cmd == LayersPanel.LayerAddCmd) {
                addNewImagesWithFileChooser();
            } else if (cmd == LayersPanel.LayerRemoveCmd) {
                removeImages(getSelectedTiles());
            } else if (cmd == LayersPanel.LayerDownCmd) {
                moveOneSlotToBottom(getSelectedTiles());
                ImageTileLayer.this.repaint();
            } else if (cmd == LayersPanel.LayerBottomCmd) {
                moveToBottom(getSelectedTiles());
                ImageTileLayer.this.repaint();
            } else if (cmd == LayersPanel.LayerTopCmd) {
                moveToTop(getSelectedTiles());
                ImageTileLayer.this.repaint();
            } else if (cmd == LayersPanel.LayerUpCmd) {
                moveOneSlotToTop(getSelectedTiles());
                ImageTileLayer.this.repaint();
            }
        }

        public void setGUIButtonEnableState(boolean somethingSelected) {
            delete.setEnabled(somethingSelected);
            top.setEnabled(somethingSelected);
            up.setEnabled(somethingSelected);
            down.setEnabled(somethingSelected);
            bottom.setEnabled(somethingSelected);
        }

        public void setGUIDeleteButtonEnableState(boolean state) {
            delete.setEnabled(state);
        }
    }

    /**
     * Changes the visibility setting on all ImageTile objects.
     * 
     * @param visible
     */
    protected void setVisibilityOnAllTiles(boolean visible) {
        OMGraphicList list = getList();
        if (list != null) {
            list.setVisible(visible);
            repaint();
        }
    }

    /**
     * Action method called when the show/hide button is pressed.
     * 
     * @param text if SHOW_TILES_TITLE, tiles made visible.
     * @param selectedTiles2
     */
    protected void showHideTiles(String text, ImageTile[] selectedTiles2) {
        boolean isVisible = (SHOW_TILES_TITLE.equals(text));
        for (int i = 0; i < selectedTiles2.length; i++) {
            selectedTiles2[i].setVisible(isVisible);
        }

        checkShowHideStatus();
        repaint();
        if (resultsList != null) {
            resultsList.repaint();
        }
    }

    /**
     * Move all the selected tiles down one space.
     * 
     * @param selectedTiles2
     */
    protected void moveOneSlotToBottom(ImageTile[] selectedTiles2) {
        OMGraphicList list = getList();
        if (list != null && selectedTiles != null && selectedTiles.length > 0) {
            for (int i = selectedTiles2.length - 1; i >= 0; i--) {
                ImageTile tile = selectedTiles2[i];
                list.moveIndexedOneToBottom(list.indexOf(tile));
            }
            rebuildListModel();
        }
    }

    /**
     * Move all of the selected tiles to the bottom of the stack.
     * 
     * @param selectedTiles2
     */
    protected void moveToBottom(ImageTile[] selectedTiles2) {
        OMGraphicList list = getList();
        if (list != null && selectedTiles != null && selectedTiles.length > 0) {
            for (int i = 0; i < selectedTiles2.length; i++) {
                ImageTile tile = selectedTiles2[i];
                list.moveIndexedToBottom(list.indexOf(tile));
            }
            rebuildListModel();
        }
    }

    /**
     * Move all of the selected tiles up one space.
     * 
     * @param selectedTiles2
     */
    protected void moveOneSlotToTop(ImageTile[] selectedTiles2) {
        OMGraphicList list = getList();
        if (list != null && selectedTiles != null && selectedTiles.length > 0) {
            for (int i = 0; i < selectedTiles2.length; i++) {
                ImageTile tile = selectedTiles2[i];
                list.moveIndexedOneToTop(list.indexOf(tile));
            }
            rebuildListModel();
        }
    }

    /**
     * Move all of the selected tiles to the top of the stack.
     * 
     * @param selectedTiles2
     */
    protected void moveToTop(ImageTile[] selectedTiles2) {
        OMGraphicList list = getList();
        if (list != null && selectedTiles != null && selectedTiles.length > 0) {
            for (int i = selectedTiles2.length - 1; i >= 0; i--) {
                ImageTile tile = selectedTiles2[i];
                list.moveIndexedToTop(list.indexOf(tile));
            }
            rebuildListModel();
        }
    }

    /**
     * MapBean is used to reset the projection of the map over the selected
     * images.
     */
    protected MapBean mapBean;

    /**
     * Figure out where the images are and move the MapBean over them.
     * 
     * @param selectedTiles2
     */
    protected void goTo(ImageTile[] selectedTiles2) {
        if (mapBean == null) {
            MapHandler bc = (MapHandler) getBeanContext();
            if (bc != null) {
                mapBean = (MapBean) bc.get(com.bbn.openmap.MapBean.class);
            }
        }

        if (mapBean != null) {
            if (selectedTiles != null && selectedTiles.length > 0) {
                Rectangle2D rec = null;
                for (int i = selectedTiles2.length - 1; i >= 0; i--) {
                    ImageTile tile = selectedTiles2[i];

                    if (rec == null) {
                        rec = new Rectangle2D.Double(tile.getLRLon(), tile.getLRLat(), 0f, 0f);
                        rec.add(tile.getULLon(), tile.getULLat());
                    } else {
                        rec.add(tile.getULLon(), tile.getULLat());
                        rec.add(tile.getLRLon(), tile.getLRLat());
                    }
                }

                if (rec != null) {
                    Point2D center = new Point2D.Double(rec.getCenterX(), rec.getCenterY());
                    Point2D anchor1 = new Point2D.Double(rec.getMinX(), rec.getMaxY());
                    Point2D anchor2 = new Point2D.Double(rec.getMaxX(), rec.getMinY());

                    Proj proj = (Proj) mapBean.getProjection();
                    float scale = proj.getScale(anchor1, anchor2, proj.forward(anchor1), proj.forward(anchor2));
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("Images cover " + anchor1 + " to " + anchor2
                                + ", scale adjusted to " + scale);
                    }

                    proj.setCenter(center);
                    proj.setScale(scale);
                    mapBean.setProjection(proj);
                }
            }
        }
    }

    /**
     * Note the provided tiles as being highlighted. Selection, in this case,
     * means the OMGraphic selection.
     * 
     * @param selectedTiles2
     */
    protected void select(ImageTile[] selectedTiles2) {
        setSelection(selectedTiles2, true);
    }

    /**
     * Note the provided tiles as being highlighted or not. Selection, in this
     * case, means the OMGraphic selection.
     * 
     * @param selectedTiles2
     */
    protected void setSelection(ImageTile[] selectedTiles2, boolean isSelected) {
        for (int i = 0; i < selectedTiles2.length; i++) {
            selectedTiles2[i].setSelected(isSelected);
        }
        repaint();
    }

    /**
     * Un-highlight all of the tiles.
     */
    public void deselect() {
        OMGraphicList list = getList();
        if (list != null) {
            list.deselect();
            repaint();
        }
    }

    /**
     * Take the drawing attributes held by the layer and push the settings on
     * all of the ImageTiles.
     */
    public void resetSelectAttributes() {
        OMGraphicList list = getList();
        if (list != null) {
            for (OMGraphic omg : list) {
                selectedDrawingAttributes.setTo(omg);
            }
            repaint();
        }
    }

    /**
     * Remove the selected tiles from the image stack. Asks the user for
     * confirmation.
     * 
     * @param selectedTiles2
     */
    protected void removeImages(ImageTile[] selectedTiles2) {
        ImageTile[] selectedTiles = getSelectedTiles();
        if (selectedTiles != null && selectedTiles.length > 0) {
            String confirmStringMulti = i18n.get(ImageTileLayer.class, "removeConfirmMultiple", "Are you sure you want to remove these images from the layer?");
            String confirmStringSolo = i18n.get(ImageTileLayer.class, "removeConfirmSolo", "Are you sure you want to remove this image from the layer?");
            String confirmTitleString = i18n.get(ImageTileLayer.class, "removeConfirmTitle", "Remove Images?");
            int answer = JOptionPane.showConfirmDialog(this, (selectedTiles.length == 1 ? confirmStringSolo
                    : confirmStringMulti), confirmTitleString, JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                OMGraphicList list = getList();
                if (list != null) {
                    for (int i = 0; i < selectedTiles.length; i++) {
                        ImageTile selectedTile = selectedTiles[i];
                        list.remove(selectedTile);
                        ((DefaultListModel) getListModel()).removeElement(selectedTile);
                    }
                    if (resultsList != null) {
                        resultsList.repaint();
                    }
                    repaint();
                }
            }
        }
    }

    /**
     * Asks the user to choose a new file or directory to load. The
     * ImageReaderLoaders are consulted to only allow files that can be handled
     * to be selectable.
     */
    protected void addNewImagesWithFileChooser() {
        // Need to get File Chooser, and allow the user to add a directory or
        // file. We could even set up filters to check the ImageReaderLoaders
        // available to this Layer and limit file selection based on matches. We
        // should also report to the user what files were loaded using a dialog
        // window,
        // or we could simply select the new images and scroll the list to make
        // those images visible.
        File startingPoint = new File(Environment.get("lastchosendirectory", System.getProperty("user.home")));
        JFileChooser chooser = new JFileChooser(startingPoint);
        String title = i18n.get(ImageTileLayer.class, "addImagesWindowTitle", "Add Images");
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        chooser.setFileFilter(new ImageLoaderFileFilter(imageReaderLoaders));
        String acceptButtonText = i18n.get(ImageTileLayer.class, "acceptButtonText", "Add");
        int state = chooser.showDialog(null, acceptButtonText);

        try {
            // only bother trying to read the file if there is one
            // for some reason, the APPROVE_OPTION said it was a
            // boolean during compile and didn't work in this next
            // statement
            if ((state != JFileChooser.CANCEL_OPTION) && (state != JFileChooser.ERROR_OPTION)) {

                String newFile = chooser.getSelectedFile().getCanonicalPath();

                int dirIndex = newFile.lastIndexOf(File.separator);
                if (dirIndex >= 0) {
                    // store the selected file for later
                    Environment.set("lastchosendirectory", newFile.substring(0, dirIndex));
                }
                OMGraphicList list = getList();
                if (list == null) {
                    list = new OMGraphicList();
                    setList(list);
                }

                LoadImageThread lit = new LoadImageThread(newFile, list);
                lit.start();
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error picking file", JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }
    }

    /**
     * A special Thread subclass to handle image loading, so it's not managed by
     * the AWT thread.
     * 
     * @author dietrick
     */
    class LoadImageThread extends Thread {
        String fileToOpen;
        OMGraphicList listToAddTo;

        public LoadImageThread(String fto, OMGraphicList ltat) {
            fileToOpen = fto;
            listToAddTo = ltat;
        }

        public void run() {
            loadImage(fileToOpen, listToAddTo);
            fireStatusUpdate(LayerStatusEvent.FINISH_WORKING);
        }
    }

    /**
     * Set the GUI button state to be enabled or not based on something on the
     * list being selected.
     * 
     * @param somethingSelected whether something is selected.
     */
    protected void setGUIButtonEnableState(boolean somethingSelected) {
        if (icbp != null) {
            icbp.setGUIButtonEnableState(somethingSelected);

            showHideButton.setEnabled(somethingSelected);
            gotoButton.setEnabled(somethingSelected);
            locateButton.setEnabled(somethingSelected);
        }
    }

    /**
     * The ListModel used by the JList, displaying the images.
     * 
     * @return the ListModel
     */
    protected synchronized ListModel getListModel() {
        if (listModel == null) {
            listModel = new DefaultListModel();
        }

        return listModel;
    }

    /**
     * Add an ImageTile to the list model.
     * 
     * @param tile
     */
    protected void addImageTileToList(ImageTile tile) {
        ((DefaultListModel) getListModel()).addElement(tile);
    }

    /**
     * Clear the list model.
     */
    protected void clearImageTileList() {
        ((DefaultListModel) getListModel()).clear();
    }

    /**
     * Remove an ImageTile from the ListModel.
     * 
     * @param tile
     * @return true if removal was successful.
     */
    protected boolean removeImageTileFromList(ImageTile tile) {
        return ((DefaultListModel) getListModel()).removeElement(tile);
    }

    /**
     * Rebuild the list model contents based on the ImageTiles contained on the
     * OMGraphicList.
     * 
     */
    protected void rebuildListModel() {
        DefaultListModel dlm = (DefaultListModel) getListModel();

        OMGraphicList list = getList();
        int[] selectedIndicies = null;

        if (list != null) {

            if (selectedTiles != null && selectedTiles.length > 0) {
                selectedIndicies = new int[selectedTiles.length];
            }

            int tileCount = 0;
            int selectedIndex = 0;
            if (selectedIndicies != null) {
                for (OMGraphic omg : list) {
                    ImageTile imageTile = (ImageTile) omg;

                    if (imageTile.isSelected() && selectedIndex < selectedIndicies.length) {
                        selectedIndicies[selectedIndex++] = tileCount;
                    }

                    tileCount++;
                }
            }

            // Causes value changed() to be called, which then unsets selected
            // tiles. So we need to find out which tiles were selected above,
            // and then set them again later.
            dlm.clear();

            for (OMGraphic omg : list) {
                tileCount++;
                dlm.addElement(omg);
            }
        }

        if (resultsList != null) {

            if (selectedIndicies != null) {
                resultsList.setSelectedIndices(selectedIndicies);
            }

            resultsList.repaint();
        }
    }

    /**
     * The ImageTiles currently selected on the list in the GUI.
     */
    protected ImageTile[] selectedTiles;

    /**
     * @return the ImageTile[] of tiles currently selected in the GUI.
     */
    protected ImageTile[] getSelectedTiles() {
        return selectedTiles;
    }

    /**
     * Set the ImageTile[] of tiles currently selected in the GUI.
     * 
     * @param sTiles
     */
    protected void setSelectedTiles(ImageTile[] sTiles) {
        selectedTiles = sTiles;
        if (sTiles != null) {
            boolean allTilesDefective = areAllTilesDefective(sTiles);
            setGUIButtonEnableState(sTiles.length > 0 && !allTilesDefective);

            if (allTilesDefective && icbp != null) {
                icbp.setGUIDeleteButtonEnableState(allTilesDefective);
            }

            for (ImageTile it : sTiles) {
                it.select();
            }
        }

        checkShowHideStatus();
        doPrepare();
    }

    protected boolean areAllTilesDefective(ImageTile[] sTiles) {
        boolean allTilesDefective = false;
        if (sTiles != null && sTiles.length > 0) {
            allTilesDefective = true;
            for (int i = 0; i < sTiles.length; i++) {
                if (!(sTiles[i] instanceof ErrImageTile)) {
                    allTilesDefective = false;
                    break;
                }
            }
        }

        return allTilesDefective;
    }

    /**
     * Checks the selected tiles from the visible list and tallies their
     * visibility. If all of the tiles are invisible, the GUI button will allow
     * them to be made visible. If any of them are visible, all of them can be
     * made invisible before the button will change them to the visible.
     * 
     */
    public void checkShowHideStatus() {
        ImageTile[] sTiles = getSelectedTiles();

        boolean anyTilesVisible = (sTiles == null || sTiles.length == 0);
        if (sTiles != null) {
            for (int i = 0; i < sTiles.length; i++) {
                anyTilesVisible = sTiles[i].isVisible() || anyTilesVisible;
            }
        }

        showHideButton.setText(anyTilesVisible ? HIDE_TILES_TITLE : SHOW_TILES_TITLE);
    }

    /**
     * A list selection listener object for the JList.
     * 
     * @author dietrick
     */
    class ListManager implements ListSelectionListener, MouseListener, MouseMotionListener {
        public ListManager() {

        }

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false && resultsList != null) {
                // Reset the location selection;
                locateButton.setSelected(false);
                deselect();

                int[] indicies = resultsList.getSelectedIndices();
                ImageTile[] selectedTiles = new ImageTile[indicies.length];
                if (indicies.length > 0) {
                    ListModel listModel = getListModel();
                    for (int i = 0; i < indicies.length; i++) {
                        selectedTiles[i] = (ImageTile) listModel.getElementAt(indicies[i]);
                    }
                }
                setSelectedTiles(selectedTiles);
            }
        }

        public void mouseClicked(MouseEvent e) {

        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) {
            checkMouseSelection(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkMouseSelection(e);
        }

        protected void checkMouseSelection(MouseEvent e) {
            int selectedIndex = getResultListIndex(e);
            if (selectedIndex < 0) {
                resultsList.clearSelection();
            }
        }

        public void mouseDragged(MouseEvent e) {

        }

        public void mouseMoved(MouseEvent e) {
            int selectedIndex = getResultListIndex(e);
            if (selectedIndex >= 0) {
                Object it = getListModel().getElementAt(selectedIndex);
                if (it instanceof ErrImageTile) {
                    resultsList.setToolTipText(((ErrImageTile) it).getProblemMessage());
                    return;
                }
            }
            resultsList.setToolTipText(null);
        }
    }

    /**
     * find out which list object was moused.
     * 
     * @param e
     * @return index of list member moused on.
     */
    protected int getResultListIndex(MouseEvent e) {
        int index = -1;

        if (resultsList != null) {
            double height = getResultsListCellHeight();

            if (height == 0) {
                return index;
            }

            int nIndex = e.getY() / (int) height;

            if (nIndex < getListModel().getSize()) {
                index = nIndex;
            }
        }

        return index;
    }

    /**
     * Get the pixel height of each cell in the JList.
     * 
     * @return pixel height of cell
     */
    protected double getResultsListCellHeight() {
        double height = 0;
        if (resultsList != null) {
            int rlFVI = resultsList.getFirstVisibleIndex();
            Rectangle bounds = resultsList.getCellBounds(rlFVI, rlFVI);

            if (bounds != null) {
                height = bounds.getHeight();
            }
        }

        return height;
    }

    public static int buttonSize = 16;
    public static ImageIcon warningImage;
    public static ImageIcon invisibleImage;

    protected static void initIcons() {
        DrawingAttributes blackDa = new DrawingAttributes();

        DrawingAttributes invisDa = new DrawingAttributes();
        invisDa.setLinePaint(OMColor.clear);
        invisDa.setFillPaint(OMColor.clear);

        DrawingAttributes yellowDa = new DrawingAttributes();
        yellowDa.setLinePaint(OMColor.yellow);
        yellowDa.setFillPaint(OMColor.yellow);

        IconPart ip = new BasicIconPart(new Rectangle2D.Double(0, 0, 100, 100));
        ip.setRenderingAttributes(invisDa);
        invisibleImage = OMIconFactory.getIcon(buttonSize, buttonSize, ip);

        IconPartList ipl = new IconPartList();

        Polygon triangle = new Polygon(new int[] { 50, 90, 10, 50 }, new int[] { 10, 90, 90, 10 }, 4);

        BasicIconPart bip = new BasicIconPart(triangle);
        bip.setRenderingAttributes(yellowDa);
        ipl.add(bip);

        bip = new BasicIconPart(triangle);
        bip.setRenderingAttributes(yellowDa);
        ipl.add(bip);
        bip = new BasicIconPart(triangle);
        bip.setRenderingAttributes(blackDa);
        ipl.add(bip);
        bip = new BasicIconPart(new Line2D.Double(49, 35, 49, 65));
        bip.setRenderingAttributes(blackDa);
        ipl.add(bip);
        bip = new BasicIconPart(new Line2D.Double(49, 75, 49, 77));
        bip.setRenderingAttributes(blackDa);
        ipl.add(bip);
        bip = new BasicIconPart(new Line2D.Double(51, 35, 51, 65));
        bip.setRenderingAttributes(blackDa);
        ipl.add(bip);
        bip = new BasicIconPart(new Line2D.Double(51, 75, 51, 77));
        bip.setRenderingAttributes(blackDa);
        ipl.add(bip);
        warningImage = OMIconFactory.getIcon(buttonSize, buttonSize, ipl);

    }

    /**
     * Renders the JList cells.
     */
    public static class ImageListCellRenderer extends JPanel implements ListCellRenderer {

        private static final long serialVersionUID = 1L;
        protected int buttonSize = 16;
        protected JLabel label = new JLabel();
        protected JLabel statusMark = new JLabel();

        public static Color fontColor = Color.BLACK;
        public static Color altFontColor = Color.BLACK;
        public static Color selectColor = Color.GRAY;
        public static Color notVisibleColor = new Color(100, 100, 100);
        public static Color regularBackgroundColor = Color.WHITE;

        public ImageListCellRenderer() {
            if (warningImage == null) {
                initIcons();
            }

            setOpaque(true);
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout(gridbag);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0f;
            gridbag.setConstraints(label, c);
            this.add(label);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0f;
            gridbag.setConstraints(statusMark, c);
            this.add(statusMark);

            Font f = label.getFont();
            f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
            label.setFont(f);

            setPreferredSize(new Dimension(20, buttonSize));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            if (value instanceof ImageTile) {
                ImageTile imageTile = (ImageTile) value;
                label.setText((String) imageTile.getAttribute(NAME_ATTRIBUTE));

                if (!isSelected) {
                    label.setForeground(imageTile.isVisible() ? fontColor : notVisibleColor);
                }

                if (value instanceof ErrImageTile) {
                    statusMark.setIcon(warningImage);
                } else {
                    statusMark.setIcon(invisibleImage);
                }

            }

            setBackground(isSelected ? selectColor : regularBackgroundColor);
            return this;
        }
    }

    /**
     * File filter created based on what the ImageReaders can handle.
     * 
     * @author dietrick
     */
    class ImageLoaderFileFilter extends FileFilter {

        Vector<ImageReaderLoader> imageReaderLoaders;

        public ImageLoaderFileFilter(Vector<ImageReaderLoader> imgDcdrLdrs) {
            imageReaderLoaders = imgDcdrLdrs;
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            if (imageReaderLoaders != null) {
                for (ImageReaderLoader irl : imageReaderLoaders) {
                    if (irl.isLoadable(f.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }

        public String getDescription() {
            String description = i18n.get(ImageTileLayer.class, "fileFilterDescription", "Image File Formats Supported by Layer");
            return description;
        }

    }
}
