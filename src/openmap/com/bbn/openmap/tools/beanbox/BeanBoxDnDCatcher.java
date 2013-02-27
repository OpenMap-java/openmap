/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                          A Part of Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.PropertyChangeListener;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextMembershipListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.bbn.openmap.Layer;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.SoloMapComponent;
import com.bbn.openmap.event.LayerListener;
import com.bbn.openmap.event.ProjectionListener;
import com.bbn.openmap.tools.dnd.ComponentDragGestureListener;
import com.bbn.openmap.tools.dnd.DefaultDnDCatcher;
import com.bbn.openmap.tools.dnd.DefaultTransferableObject;
import com.bbn.openmap.util.Debug;

/**
 * The BeanBoxDnDCatcher class manages all Java Drag-and-Drop events associated
 * with openmap layers that implement the
 * {@link com.bbn.openmap.tools.beanbox.BeanBoxHandler}interface.
 */
public class BeanBoxDnDCatcher extends DefaultDnDCatcher implements
        SoloMapComponent, BeanContextChild, BeanContextMembershipListener,
        PropertyChangeListener, Serializable, ProjectionListener,
        LayerListener, ActionListener {

    static {

        setDefaultIcon();

    }

    private Vector transferData;
    private Point dropLocation;

    /** holds the currently selected bean */
    protected Object selectedBean = null;

    /** holds the serialized version of currently selected bean */
    protected ByteArrayOutputStream serBean = null;

    /** holds the map location of the currently selected bean */
    protected Point selectedBeanLocation = null;

    /**
     * holds the {@link com.bbn.openmap.tools.beanbox.BeanBox}that manages the
     * currently selected bean
     */
    protected BeanBox selectedBeanBox = null;

    /**
     * holds the openmap layer that contains the currently selected bean
     */
    protected Layer selectedBeanLayer = null;

    /** holds the currently cut bean, if any */
    Object cutBean = null;

    /**
     * contains BeanInfo objects hashed by the class names of the associated
     * bean classes
     */
    protected HashMap beanInfoMap = null;

    /**
     * Constructs a new {@link com.bbn.openmap.tools.dnd.DnDListener} object.
     */
    public BeanBoxDnDCatcher() {
        this(new DragSource());
    }

    /**
     * Constructs a new MouseDragGestureRecognizer given the DragSource for the
     * Component.
     * 
     * @param ds the DragSource for the Component
     */
    public BeanBoxDnDCatcher(DragSource ds) {
        this(ds, null);
    }

    /**
     * Construct a new MouseDragGestureRecognizer given the DragSource for the
     * Component c, and the Component to observe.
     * 
     * @param ds the DragSource for the Component c
     * @param c the Component to observe
     */
    public BeanBoxDnDCatcher(DragSource ds, Component c) {
        this(ds, c, DnDConstants.ACTION_MOVE);
    }

    /**
     * Construct a new MouseDragGestureRecognizer given the DragSource for the
     * Component c, and the Component to observe and the drag-and-drop action.
     * 
     * @param ds the DragSource for the Component c
     * @param c the Component to observe
     * @param act the drag-and-drop action
     */
    public BeanBoxDnDCatcher(DragSource ds, Component c, int act) {
        this(ds, c, act, null);
    }

    /**
     * Construct a new MouseDragGestureRecognizer given the DragSource for the
     * Component c, and the Component to observe. the drag-and-drop action and a
     * DragGestureListener
     * 
     * @param ds the DragSource for the Component c
     * @param c the Component to observe
     * @param act the drag-and-drop action
     * @param dgl the DragGestureListener
     */
    public BeanBoxDnDCatcher(DragSource ds, Component c, int act,
            DragGestureListener dgl) {
        super(ds, c, act, dgl);
        dragSource = getDragSource();
        dragGestureListener = new ComponentDragGestureListener(this, this);
        setSourceActions(DnDConstants.ACTION_MOVE);

        beanInfoMap = new HashMap();
    }

    /**
     * Calls superclass method and then adds the KeyListener to someObj if
     * someObj is of type OpenMapFrame.
     */
    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof MapBean) {

            ((MapBean) someObj).addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    if (evt.getModifiers() == InputEvent.CTRL_MASK
                            && evt.getKeyCode() == KeyEvent.VK_C)
                        copySelectedBean();
                    else if (evt.getModifiers() == InputEvent.CTRL_MASK
                            && evt.getKeyCode() == KeyEvent.VK_V)
                        pasteSelectedBean();
                    else if (evt.getModifiers() == InputEvent.CTRL_MASK
                            && evt.getKeyCode() == KeyEvent.VK_X)
                        cutSelectedBean();
                    else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
                        unCutSelectedBean();
                    else if (evt.getKeyCode() == KeyEvent.VK_DELETE)
                        deleteSelectedBean();
                }
            });
        }
    }

    /**
     * This method is called when the user chooses to copy a bean by some means
     * such by by pressing Ctrl-C. This method tries to serialize the selected
     * bean. If no bean is selected or the bean is not serializable, this method
     * is a no-op.
     */
    protected void copySelectedBean() {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> copySelectedBean");
        if (selectedBean == null || selectedBeanLocation == null) {
            clearSelection();
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBean=" + selectedBean);
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBeanLocation=" + selectedBeanLocation);
            return;
        }

        try {
            serBean = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(serBean);
            oos.writeObject(selectedBean);
        } catch (Exception e) {
            e.printStackTrace();
            clearSelection();
            if (Debug.debugging("beanbox"))
                Debug.output("Exit> copySelectedBean");
            return;
        }

        cutBean = null;

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> copySelectedBean");
    }

    /**
     * This method is called when the user chooses to paste by some means (such
     * by pressing Ctrl-V) a previously copied or cut bean. This method tries to
     * deserialize the previously serialized bean. If the bean in question was
     * cut, this method also removes it from from the source beanbox. The paste
     * operation is treated the same as a drop operation. If no bean was
     * previously copied or cut or if an error occurs during deserialization,
     * this method is a no-op.
     */
    protected void pasteSelectedBean() {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> pasteSelectedBean");

        if (serBean == null) {
            clearSelection();
            if (Debug.debugging("beanbox"))
                Debug.output("Exit> pasteSelectedBean");
            return;
        }

        BeanInfo beanInfo = (BeanInfo) beanInfoMap.get(selectedBean.getClass()
                .getName());
        if (beanInfo == null) {
            System.out.println("ERROR> BBDnDC::pasteSelectedBean: "
                    + "no cached BeanInfo found for bean " + selectedBean);
            clearSelection();
            return;
        }

        // if bean was cut, remove it from its present location
        if (cutBean != null) {
            selectedBeanBox.removeBean(selectedBean);
        }

        Object deserBean = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(serBean.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            deserBean = ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            clearSelection();
            if (Debug.debugging("beanbox"))
                Debug.output("Exit> pasteSelectedBean");
            return;
        }

        // construct a transferData object
        transferData = new Vector();
        transferData.add(deserBean);
        transferData.add(beanInfo);
        transferData.add(new Boolean(false)); // bean not being moved

        // let dropLocation be selectedBeanLocation
        dropLocation = selectedBeanLocation;

        showPopUp(selectedBeanLayer);

        cutBean = null;
        if (Debug.debugging("beanbox"))
            Debug.output("Exit> pasteSelectedBean");
    }

    /**
     * This method is called when the user chooses to cut a bean by some means
     * such by by pressing Ctrl-X. This method tries to serialize the selected
     * bean. If no bean is selected or the bean is not serializable, this method
     * is a no-op.
     */
    protected void cutSelectedBean() {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> cutSelectedBean");

        if (selectedBean == null || selectedBeanLocation == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBean=" + selectedBean);
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBeanLocation=" + selectedBeanLocation);
            clearSelection();
            return;
        }

        try {
            serBean = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(serBean);
            oos.writeObject(selectedBean);
        } catch (Exception e) {
            e.printStackTrace();
            clearSelection();
            if (Debug.debugging("beanbox"))
                Debug.output("Exit> copySelectedBean");
            return;
        }

        cutBean = selectedBean;

        selectedBeanBox.showCut(selectedBean);

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> cutSelectedBean");
    }

    /**
     * This method is called when the user chooses to cancel a cut operation on
     * a bean by some means such by by pressing ESC. If no bean was marked for
     * cutting this method is a no-op.
     */
    protected void unCutSelectedBean() {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> unCutSelectedBean");

        if (selectedBean == null || selectedBeanLocation == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBean=" + selectedBean);
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBeanLocation=" + selectedBeanLocation);
            clearSelection();
            return;
        }

        selectedBeanBox.showUnCut(selectedBean);

        clearSelection();

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> unCutSelectedBean");
    }

    private void clearSelection() {
        cutBean = null;
        selectedBean = null;
        selectedBeanLocation = null;
        selectedBeanBox = null;
        selectedBeanLayer = null;
        serBean = null;
    }

    /**
     * This method is called when the user chooses to delete a bean by some
     * means such by by pressing DEL. This method removes the selected bean from
     * its beanbox. If no bean is selected this method is a no-op.
     */
    protected void deleteSelectedBean() {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> deleteSelectedBean");

        if (selectedBean == null || selectedBeanLocation == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBean=" + selectedBean);
            if (Debug.debugging("beanbox"))
                Debug.output("selectedBeanLocation=" + selectedBeanLocation);
            return;
        }

        selectedBeanBox.removeBean(selectedBean);
        cutBean = null;

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> deleteSelectedBean");
    }

    /**
     * The drag operation has terminated with a drop on this
     * <code>DropTarget</code>. This method is responsible for undertaking the
     * transfer of the data associated with the gesture. The
     * <code>DropTargetDropEvent</code> provides a means to obtain a
     * <code>Transferable</code> object that represents the data object(s) to be
     * transfered.
     * <P>
     * <P>
     * 
     * @param dtde the <code>DropTargetDropEvent</code>
     */
    public void drop(DropTargetDropEvent dtde) {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> drop");

        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
        extractTransferData(dtde);
        extractDropLocation(dtde);

        if (transferData == null || dropLocation == null)
            return;

        Component parent = ((DropTarget) dtde.getSource()).getComponent();

        dtde.dropComplete(true);

        showPopUp(parent);
        if (Debug.debugging("beanbox"))
            Debug.output("Exit> drop");
    }

    private void showPopUp(Component parent) {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> showPopUp");
        JPopupMenu popup = new JPopupMenu();
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
                "Available Drop Targets:");

        titledBorder.setTitleColor(Color.gray);
        popup.setBorder(titledBorder);

        Border compoundborder = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(2, 2, 2, 2));

        Enumeration keys = layers.keys();
        while (keys.hasMoreElements()) {
            String layerName = keys.nextElement().toString();
            Layer omlayer = (Layer) layers.get(layerName);

            if (omlayer.isVisible()) {
                JMenuItem menuItem = new JMenuItem(layerName);
                menuItem.setHorizontalTextPosition(SwingConstants.CENTER);
                menuItem.setBorder(compoundborder);
                menuItem.addActionListener(this);
                popup.add(menuItem);
            }
        }

        popup.addSeparator();

        JMenuItem menuItem = new JMenuItem("CANCEL");
        menuItem.setForeground(Color.red);
        menuItem.setHorizontalTextPosition(SwingConstants.CENTER);
        menuItem.setBorder(compoundborder);

        popup.add(menuItem);

        if (Debug.debugging("beanbox"))
            Debug.output("showing popup");

        popup.show(parent, dropLocation.x, dropLocation.y);
        if (Debug.debugging("beanbox"))
            Debug.output("Exit> showPopUp");
    }

    /**
     * Displays a {@link com.bbn.openmap.tools.beanbox.GenericPropertySheet}if
     * mouse click is on a bean in some layer. In case of overlapping beans,
     * chooses the first bean found to be under the mouse, which is usually a
     * bean in the top most visible layer.
     */
    public void mouseClicked(MouseEvent evt) {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> mouseClicked");

        Point srcLocation = evt.getPoint();

        Enumeration keys = layers.keys();
        while (keys.hasMoreElements()) {
            String layerName = keys.nextElement().toString();
            selectedBeanLayer = (Layer) layers.get(layerName);
            if (!selectedBeanLayer.isVisible())
                continue;
            selectedBeanBox = ((BeanBoxHandler) selectedBeanLayer).getBeanBox();
            if (selectedBeanBox == null)
                continue;
            selectedBean = selectedBeanBox.getBeanAtLocation(srcLocation);
            if (selectedBean != null) {
                break;
            }
        }

        if (selectedBean == null) {
            clearSelection();
            return;
        }

        selectedBeanLocation = srcLocation;

        selectedBeanBox.showSelected(selectedBean);

        if (Debug.debugging("beanbox"))
            Debug.output("selectedBean=" + selectedBean);

        if (evt.getModifiers() != InputEvent.BUTTON1_MASK) {
            GenericPropertySheet propertySheet = new GenericPropertySheet(selectedBean, 575, 20, null, selectedBeanBox);
            propertySheet.setVisible(true);
        }

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> mouseClicked");
    }

    /**
     * This method is called whenever the user choose a layer to drop or move a
     * bean to. This method adds the bean to the layer or moves the beans to or
     * within the selected layer.
     */
    public void actionPerformed(ActionEvent evt) {

        if (Debug.debugging("beanbox"))
            Debug.output("Enter> actionPerformed");
        Object source = evt.getSource();
        if (!(source instanceof JMenuItem))
            return;

        JMenuItem mi = (JMenuItem) source;
        String name = mi.getText();
        Layer targetLayer = (Layer) layers.get(name);

        if (targetLayer == null) {
            System.out.println("ERROR> BBDnDC::actionPerformed: "
                    + "no layer found with name " + name);
            return;
        }

        BeanBox targetBeanBox = ((BeanBoxHandler) targetLayer).getBeanBox();

        Object bean = transferData.get(0);
        BeanInfo beanInfo = (BeanInfo) transferData.get(1);
        Boolean wasBeanMoved = (Boolean) transferData.get(2);

        if (wasBeanMoved.booleanValue()) {

            String sourceLayerName = (String) transferData.get(3);
            if (sourceLayerName.equals(targetLayer.getName())) {
                targetBeanBox.relocateBean(bean, beanInfo, dropLocation);
            } else {
                Layer sourceLayer = (Layer) layers.get(sourceLayerName);
                BeanBox sourceBeanBox = ((BeanBoxHandler) sourceLayer).getBeanBox();
                sourceBeanBox.removeBean(bean);
                Vector object = new Vector();
                object.add(bean);
                object.add(beanInfo);
                object.add(dropLocation);
                targetBeanBox.addBean(object);
            }
        } else {
            Vector object = new Vector();
            object.add(bean);
            object.add(beanInfo);
            object.add(dropLocation);
            targetBeanBox.addBean(object);
        }
    }

    /**
     * Asscoiates a DropTarget with each layer. Also caches all layers that
     * implement the BeanBoxHandler interface.
     */
    public void setLayers(Layer[] allLayers) {
        layers.clear();
        if (allLayers != null) {
            for (int i = 0; i < allLayers.length; i++) {
                new DropTarget(allLayers[i], DnDConstants.ACTION_MOVE, this);
                if (allLayers[i] instanceof BeanBoxHandler) {
                    Debug.message("DnDCatcher", "Layers changed");
                    layers.put(allLayers[i].getName(), allLayers[i]);
                }
            }
        }
    }

    /**
     * Invoked on dragGestureRecognized
     */
    public void startDragAction(DragGestureEvent dge, DragSourceListener dsl) {
        if (Debug.debugging("beanbox"))
            Debug.output("Enter> startDragAction");

        Object selectedBean = null;
        BeanBox selectedBeanBox = null;
        Layer selectedLayer = null;

        Point srcLocation = dge.getDragOrigin();

        Enumeration keys = layers.keys();
        while (keys.hasMoreElements()) {
            String layerName = keys.nextElement().toString();
            Layer omLayer = (Layer) layers.get(layerName);
            BeanBox beanBox = ((BeanBoxHandler) omLayer).getBeanBox();
            selectedBean = beanBox.getBeanAtLocation(srcLocation);
            if (selectedBean != null) {
                selectedBeanBox = beanBox;
                selectedLayer = omLayer;
                break;
            }
        }

        if (Debug.debugging("beanbox"))
            Debug.output("selectedBean=" + selectedBean);

        if (selectedBean == null || selectedBeanBox == null
                || selectedLayer == null) {
            if (Debug.debugging("beanbox"))
                Debug.output("Exit> startDragAction, selected bean/beanbox/layer is null");
            return;
        }

        Image dragImage = selectedBeanBox.getDragImage(selectedBean);

        super.setCursor(dragImage, DragSource.DefaultMoveDrop);

        BeanInfo beanInfo = selectedBeanBox.getBeanInfoForBean(selectedBean.getClass()
                .getName());

        Vector beanTransferData = new Vector();
        beanTransferData.add(selectedBean);
        beanTransferData.add(beanInfo);
        beanTransferData.add(new Boolean(true));
        beanTransferData.add(selectedLayer.getName());

        dragSource.startDrag(dge,
                super.getCursor(DragSource.DefaultMoveDrop),
                new DefaultTransferableObject(beanTransferData),
                dsl);

        if (Debug.debugging("beanbox"))
            Debug.output("Exit> startDragAction");
    }

    private void extractTransferData(DropTargetDropEvent dtde) {
        if (dtde == null) {
            System.out.println("ERROR> BDnDC::getTransferData(): dropEvent is null");
            return;
        }

        Transferable tr = dtde.getTransferable();
        try {
            transferData = (Vector) tr.getTransferData(DefaultTransferableObject.OBJECT_FLAVOR);

            // cache beanInfos
            if (transferData.size() >= 2) {
                Object bean = transferData.get(0);
                BeanInfo beanInfo = (BeanInfo) transferData.get(1);
                beanInfoMap.put(bean.getClass().getName(), beanInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractDropLocation(DropTargetDropEvent dtde) {
        if (dtde == null) {
            System.out.println("ERROR> BDnDC::getTransferData(): dropEvent is null");
            return;
        }

        dropLocation = dtde.getLocation();
    }

    private static void setDefaultIcon() {
        if (BeanPanel.defaultBeanIcon == null) {
            URL url = BeanPanel.class.getResource("bluebean.gif");
            if (url != null)
                BeanPanel.defaultBeanIcon = new ImageIcon(url);
        }
    }
}
