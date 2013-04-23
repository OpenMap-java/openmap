package com.bbn.openmap.dataAccess.mapTile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.bbn.openmap.I18n;
import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.OMComponentPanel;
import com.bbn.openmap.omGraphics.DrawingAttributes;
import com.bbn.openmap.omGraphics.OMAction;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMRect;
import com.bbn.openmap.proj.Projection;

/**
 * A class that visually manages the settings for a ZoomLevelInfo object. Works
 * inside the MapTileMakerComponent.
 * 
 * @author dietrick
 */
public class ZoomLevelMakerFace
        extends OMComponentPanel {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.ZoomLevelMakerFace");

    private static final long serialVersionUID = 1L;
    protected ZoomLevelMaker zfi;
    boolean active = false;
    boolean include = false;

    protected JList boundsList;
    protected List<BoundsObject> boundsObjectList;
    protected BoundsListModel boundsModel;
    protected OMGraphicList boundaries = new OMGraphicList();

    protected List<LayerObject> layerList = new ArrayList<LayerObject>();
    protected MapTileMakerComponent organizer;

    protected JPanel layerPanel;
    protected JButton createBoundaryButton;
    protected JButton editBoundaryButton;
    protected JButton deleteBoundaryButton;
    protected JLabel intro;
    protected JLabel tileDimensions;
    protected JButton scaleButton;
    JSpinner rangeLevelChoice;

    public ZoomLevelMakerFace(ZoomLevelMaker zfi, MapTileMakerComponent mtmc) {

        this.zfi = zfi;
        this.organizer = mtmc;
        setLayout(new GridBagLayout());

        JPanel introPanel = new JPanel(new GridBagLayout());

        JLabel intro = new JLabel(i18n.get(ZoomLevelMakerFace.class, "zoom_level", "Make tiles for zoom levels"));
        JLabel tileDimLabel = new JLabel(i18n.get(ZoomLevelMakerFace.class, "tile_dim", "Tile Dimensions"));
        JLabel baseScaleLabel = new JLabel(i18n.get(ZoomLevelMakerFace.class, "base_scale", "Base Scale for Zoom Level"));

        JSpinner zoomLevelChoice = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1.0)) {
            public void setValue(Object value) {
                if (value instanceof Number) {
                    value = new Double(((Number) value).intValue());
                }
                super.setValue(value);

                updateZoomLevel(((Double) getValue()).intValue());
            }
        };
        String upperZoomLevelTT = i18n.get(ZoomLevelMakerFace.class, "upper_zoom_level", I18n.TOOLTIP, "Upper zoom level");
        String lowerZoomLevelTT = i18n.get(ZoomLevelMakerFace.class, "lower_zoom_level", I18n.TOOLTIP, "Lower zoom level");
        zoomLevelChoice.setToolTipText(upperZoomLevelTT);

        tileDimensions = new JLabel();
        scaleButton = new JButton();
        scaleButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        scaleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                MapHandler mHandler = (MapHandler) organizer.getBeanContext();
                MapBean map = mHandler.get(MapBean.class);
                map.setScale(getZoomLevelMaker().getScale());
            }
        });

        rangeLevelChoice = new JSpinner(new SpinnerNumberModel(0, 0, 20, 1.0)) {
            public void setValue(Object value) {
                if (value instanceof Number) {
                    int newVal = ((Number) value).intValue();
                    int limit = getZoomLevelMaker().getZoomLevel();
                    if (newVal > limit) {
                        newVal = limit;
                    }
                    value = new Double(newVal);

                    getZoomLevelMaker().setRange(newVal);
                }
                super.setValue(value);
            }
        };
        rangeLevelChoice.setToolTipText(lowerZoomLevelTT);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);

        JPanel zoomHolder = new JPanel();
        zoomHolder.add(zoomLevelChoice, c);
        zoomHolder.add(new JLabel(" to "), c);
        zoomHolder.add(rangeLevelChoice, c);

        addIntroEntry(introPanel, intro, zoomHolder, 0, new Insets(0, 0, 0, 0));
        addIntroEntry(introPanel, tileDimLabel, tileDimensions, 1, new Insets(0, 0, 5, 0));
        addIntroEntry(introPanel, baseScaleLabel, scaleButton, 2, new Insets(0, 0, 5, 0));

        updateZoomLevel(0);
        add(introPanel, c);

        c.gridx = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;

        include = true;

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0f;

        layerPanel = new JPanel(new GridBagLayout());
        setLayers(layerList);

        JPanel outerLayerPanel = new JPanel(new GridBagLayout());
        String layers_for_title = i18n.get(ZoomLevelMakerFace.class, "layers_for_title", "Layers");
        outerLayerPanel.setBorder(BorderFactory.createTitledBorder(layers_for_title));
        JScrollPane jsp =
                new JScrollPane(layerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c.weighty = 1.0f;
        c.insets = new Insets(0, 0, 0, 0);
        outerLayerPanel.add(jsp, c);
        c.weighty = .8f;
        c.insets = new Insets(3, 0, 3, 0);
        add(outerLayerPanel, c);

        JPanel boundsPanel = new JPanel(new BorderLayout());
        String boundaries_for_title = i18n.get(ZoomLevelMakerFace.class, "boundaries_for_title", "Boundaries");
        boundsPanel.setBorder(BorderFactory.createTitledBorder(boundaries_for_title));
        boundsObjectList = new ArrayList<BoundsObject>();
        boundsModel = new BoundsListModel();
        boundsList = new JList(boundsModel);
        boundsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        boundsList.setLayoutOrientation(JList.VERTICAL);
        boundsList.setVisibleRowCount(4);
        boundsList.addListSelectionListener(new SelectionListener());
        boundsList.addMouseListener(new ListMouseListener());

        JScrollPane scrollableBoundsList =
                new JScrollPane(boundsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        boundsPanel.add(scrollableBoundsList, BorderLayout.CENTER);

        JPanel boundsButtonPanel = new JPanel();

        ImageIcon ii = createImageIcon("add_16x16.png");
        createBoundaryButton = new JButton(ii);
        String create_a_boundary_rectangle =
                i18n.get(ZoomLevelMakerFace.class, "create_a_boundary_rectangle", "Create a boundary rectangle");
        createBoundaryButton.setToolTipText(create_a_boundary_rectangle);
        createBoundaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                organizer.createRectangle();
            }
        });
        boundsButtonPanel.add(createBoundaryButton);

        ii = createImageIcon("edit_16x16.png");
        editBoundaryButton = new JButton(ii);
        String edit_a_selected_boundary_rectangle =
                i18n.get(ZoomLevelMakerFace.class, "edit_a_selected_boundary_rectangle", "Edit a selected boundary rectangle");
        editBoundaryButton.setToolTipText(edit_a_selected_boundary_rectangle);
        editBoundaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (boundsList.getSelectedIndex() != -1) {
                    BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
                    organizer.edit(selected.bounds, null);
                }
            }
        });
        boundsButtonPanel.add(editBoundaryButton);

        ii = createImageIcon("remov_16x16.png");
        deleteBoundaryButton = new JButton(ii);
        String delete_a_selected_boundary_rectangle =
                i18n.get(ZoomLevelMakerFace.class, "delete_a_selected_boundary_rectangle", "Delete a selected boundary rectangle");
        deleteBoundaryButton.setToolTipText(delete_a_selected_boundary_rectangle);
        deleteBoundaryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (boundsList.getSelectedIndex() != -1) {
                    BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
                    boundaries.remove(selected.bounds);
                    boundsObjectList.remove(selected);
                    boundsList.repaint();

                    if (organizer.drawingTool.isActivated()) {
                        organizer.drawingTool.deactivate(OMAction.DELETE_GRAPHIC_MASK);
                    }

                    ((MapHandler) organizer.getBeanContext()).get(MapBean.class).repaint();
                }
            }
        });
        boundsButtonPanel.add(deleteBoundaryButton);
        boundsPanel.add(boundsButtonPanel, BorderLayout.SOUTH);
        c.weighty = .8f;
        add(boundsPanel, c);
    }

    protected void addIntroEntry(JPanel panel, JComponent left, JComponent right, int y, Insets i) {
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        c.insets = i;

        panel.add(left, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1f;
        panel.add(Box.createHorizontalGlue(), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0f;
        c.anchor = GridBagConstraints.EAST;
        panel.add(right, c);
    }

    public void updateZoomLevel(int zoomLevel) {
        DecimalFormat df = new DecimalFormat("000,000");
        zfi.setZoomLevel(zoomLevel);
        
        zfi.setScale(new OSMMapTileCoordinateTransform().getScaleForZoom(zoomLevel));
        
        int etc = zfi.getEdgeTileCount();

        tileDimensions.setText(etc + " x " + etc);

        String scale = "1:" + df.format(zfi.getScale());

        scaleButton.setText(scale);
        String tooltip = i18n.get(ZoomLevelMakerFace.class, "set_map_scale_to", I18n.TOOLTIP, "Set map scale to");
        scaleButton.setToolTipText(tooltip + " " + scale);

        Object obj = rangeLevelChoice.getValue();
        if (obj instanceof Number) {
            int rangeVal = ((Number) obj).intValue();
            if (rangeVal > zoomLevel) {
                rangeVal = zoomLevel;
                rangeLevelChoice.setValue(new Integer(zoomLevel));
                zfi.setRange(rangeVal);
            }
        }

    }

    protected void enableBoundaryButtons(boolean setting) {
        createBoundaryButton.setEnabled(setting);

        boolean somethingSelected = boundsList.getSelectedIndex() != -1;

        editBoundaryButton.setEnabled(setting && somethingSelected);
        deleteBoundaryButton.setEnabled(setting && somethingSelected);
    }

    /**
     * Given a set of Layers, look at the internal list and make sure there are
     * layer objects that match. Purges LayerObjects that don't represent
     * layers, and adds LayerObjects as needed. Calls setLayers with
     * LayerObjects.
     * 
     * @param layers
     */
    protected void setLayers(Layer[] layers) {

        List<LayerObject> layerObjects = new ArrayList<LayerObject>();

        for (Layer layer : layers) {
            boolean foundOne = false;
            for (LayerObject lo : layerList) {
                if (lo.getLayer().equals(layer)) {
                    foundOne = true;
                    layerObjects.add(lo);
                    break;
                }
            }

            if (!foundOne) {
                layerObjects.add(new LayerObject(layer));
            }
        }

        setLayers(layerObjects);
    }

    /**
     * Update the layer panel to have buttons for the layer objects. Doesn't do
     * any checking, just adds them to the layerPanel, and adds the button that
     * pushes this ZLIF's layer settings to all other layers.
     * 
     * @param layerObjects
     */
    protected void setLayers(List<LayerObject> layerObjects) {
        this.layerList = layerObjects;

        if (layerPanel == null) {
            return;
        }

        layerPanel.removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0f;
        c.anchor = GridBagConstraints.WEST;

        for (LayerObject lo : layerObjects) {

            String layerMarker = lo.getLayer().getPropertyPrefix();
            if (layerMarker != null) {
                lo.setSelected(zfi.getLayers().contains(layerMarker));
            }

            layerPanel.add(lo, c);
        }

        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0f;
        layerPanel.add(Box.createGlue(), c);
    }

    /**
     * Given a list of LayerObjects, make the visibility of the internal layers
     * match the list.
     * 
     * @param layerObjects
     */
    protected void matchObjects(List<LayerObject> layerObjects) {

        for (LayerObject lo : layerList) {
            for (LayerObject toMatch : layerObjects) {
                Layer matchedLayer = toMatch.getLayer();
                if (lo.getLayer().equals(matchedLayer)) {

                    boolean turnOn = toMatch.isSelected();
                    String layerMarker = matchedLayer.getPropertyPrefix();

                    lo.setSelected(turnOn);

                    if (turnOn) {
                        if (!zfi.getLayers().contains(layerMarker)) {
                            zfi.getLayers().add(layerMarker);
                        }
                    } else {
                        zfi.getLayers().remove(layerMarker);
                    }
                }
            }

            setInclude(!zfi.getLayers().isEmpty());
        }
    }

    public void matchBounds(List<BoundsObject> bounds) {
        boundsModel.clear();
        boundaries.clear();

        for (BoundsObject bo : bounds) {
            BoundsObject copy = bo.clone();
            boundsModel.addElement(copy);
            boundaries.add(bo.bounds);
        }
    }

    /**
     * Whether this ZLIF is the active tab in the MapTileMakerComponent.
     * 
     * @return true if face is active
     */
    protected boolean isActive() {
        return active;
    }

    /**
     * Set this as the active ZLIF in MapTileMakerComponent.
     * 
     * @param active
     */
    protected void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Whether this ZoomLevel should be used in the tile creation or skipped.
     * 
     * @return if zoom level should be included in tile creation.
     */
    public boolean isInclude() {
        return include;
    }

    /**
     * Set whether this ZoomLevel should be used in the tile creation.
     * 
     * @param include
     */
    public void setInclude(boolean include) {
        this.include = include;
        // includeButton.setSelected(include);
    }

    ZoomLevelMaker getZoomLevelMaker() {
        return zfi;
    }

    void setZoomLevelMaker(ZoomLevelMaker zfi) {
        this.zfi = zfi;
    }

    /**
     * Called from MapTileMakerComponent if this is the active ZLIF when the
     * drawing tool completes, assigning the boundary to this ZLIF (or whatever
     * action is provided).
     * 
     * @param omg
     * @param action
     */
    public void handleBoundary(OMGraphic omg, OMAction action) {
        boundaries.doAction(omg, action);

        boundsModel.clear();
        int count = 1;
        for (OMGraphic omr : boundaries) {
            if (omr instanceof OMRect) {
                String bounding_rectangle = i18n.get(ZoomLevelMakerFace.class, "bounding_rectangle", "Bounding Rectangle");
                boundsModel.addElement(new BoundsObject((OMRect) omr, bounding_rectangle + " " + (count++)));
            }
        }

    }

    /**
     * Called from the MapTileMakerComponent, so this ZLIF is ready to paint its
     * boundaries if it is activated.
     * 
     * @param proj Projection to use to render boundaries.
     * @return true if boundaries can be generated for given projection
     */
    protected boolean generate(Projection proj) {
        if (boundaries != null) {
            return boundaries.generate(proj);
        }
        return false;
    }

    /**
     * Called from the MapTileMakerComponent, when this is the active ZLIF so
     * the current boundaries are painted on top of the map.
     * 
     * @param graphics
     */
    protected void paintBoundaries(Graphics graphics) {
        if (boundaries != null) {
            boundaries.render(graphics);
        }
    }

    /**
     * Bounds list model for boundary JList.
     * 
     * @author dietrick
     */
    private final class BoundsListModel
            extends AbstractListModel {
        private static final long serialVersionUID = 1L;

        public int getSize() {
            return boundsObjectList.size();
        }

        public Object getElementAt(int index) {
            return boundsObjectList.get(index);
        }

        public void editElement(int index) {
            fireContentsChanged(this, index, index);
        }

        public void insertElement(BoundsObject obj, int index) {
            boundsObjectList.add(index, obj);
            fireIntervalAdded(this, index, index);
        }

        public void addElement(BoundsObject obj) {
            int index = getSize();
            boundsObjectList.add(obj);
            fireIntervalAdded(this, index, index);
        }

        public BoundsObject removeElementAt(int index) {
            BoundsObject obj = boundsObjectList.remove(index);
            fireIntervalRemoved(this, index, index);
            return obj;
        }

        public void clear() {
            int size = boundsObjectList.size();
            boundsObjectList.clear();
            fireIntervalRemoved(this, 0, size);
        }
    }

    /**
     * The list object used to represent a boundary.
     * 
     * @author dietrick
     */
    public class BoundsObject
            implements Cloneable {
        protected OMRect bounds;
        protected String name;

        public BoundsObject(OMRect rect, String displayName) {
            bounds = rect;
            name = displayName;
        }

        public String toString() {
            return name;
        }

        public BoundsObject clone() {
            OMRect copy =
                    new OMRect(bounds.getNorthLat(), bounds.getWestLon(), bounds.getSouthLat(), bounds.getEastLon(),
                               OMGraphic.LINETYPE_RHUMB);
            DrawingAttributes atts = DrawingAttributes.getDefaultClone();
            atts.setFrom(bounds);
            atts.setTo(copy);
            return new BoundsObject(bounds, name);
        }
    }

    /**
     * A component used to represent a layer/layer setting in the face.
     * 
     * @author dietrick
     */
    public class LayerObject
            extends JCheckBox {
        private static final long serialVersionUID = 1L;
        protected Layer layer;

        public LayerObject(Layer layer) {
            super(layer.getName(), layer.isVisible());
            this.layer = layer;
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String pp = getLayer().getPropertyPrefix();
                    if (((JCheckBox) ae.getSource()).isSelected()) {
                        if (!zfi.getLayers().contains(pp)) {
                            zfi.getLayers().add(pp);
                        }
                    } else {
                        zfi.getLayers().remove(pp);
                    }

                    if (active /* duh */&& organizer != null) {
                        organizer.shuffleLayers(ZoomLevelMakerFace.this);
                        setInclude(zfi.getLayers() != null && !zfi.getLayers().isEmpty());
                    }
                }
            });
        }

        Layer getLayer() {
            return layer;
        }

        public String toString() {
            return layer.getName();
        }
    }

    /**
     * A class that listens for selections on the boundary list.
     * 
     * @author dietrick
     */
    private final class SelectionListener
            implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                boolean somethingSelected = boundsList.getSelectedIndex() != -1;
                editBoundaryButton.setEnabled(somethingSelected);
                deleteBoundaryButton.setEnabled(somethingSelected);
            }
        }
    }

    /**
     * A class that listens for double-clicks on the boundary list, launching an
     * editor for that rectangle.
     * 
     * @author dietrick
     */
    private class ListMouseListener
            extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int index = boundsList.locationToIndex(e.getPoint());
                if (index != -1) {
                    BoundsObject selected = (BoundsObject) boundsList.getSelectedValue();
                    organizer.edit(selected.bounds, null);
                }
            }
        }
    }

    public ImageIcon createImageIcon(String path) {
        URL imgURL = ZoomLevelMakerFace.class.getClassLoader().getResource("com/bbn/openmap/dataAccess/mapTile/" + path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
