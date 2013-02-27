/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Introspector;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.gui.OMToolComponent;
import com.bbn.openmap.tools.dnd.DefaultTransferableObject;
import com.bbn.openmap.util.Debug;

/**
 * The BeanPanel class is an openmap component that loads a set of
 * java bean classes upon startup and organizes them into one or more
 * tabbed panes. The organization of the tabs is specified in the
 * openmap properties file (see below). A bean loaded by the BeanPanel
 * is represented using information available in the BeanInfo. The
 * BeanPanel tries to represent the bean as an icon followed by the
 * bean's pretty name. It gets the (32 x 32 pixels size) color icon
 * from the BeanInfo and it gets the bean's pretty name from the
 * BeanDescriptor defined in the BeanInfo. If no icon is available,
 * the default bluebean.gif icon included in this package is used. If
 * no pretty name is available, the last portion of the bean's fully
 * qualified class name is used to represent the bean's name.
 * <p>
 * The BeanPanel uses Java Drag-And-Drop and is registered as the
 * DragSource for Drag-And-Drop events. A user can drag and drop a
 * bean from one of the tabs in the BeanPanel onto the map where the
 * {@link com.bbn.openmap.tools.beanbox.BeanBoxDnDCatcher}catches the
 * bean.
 * <p>
 * <p>
 * The following are the properties that the BeanPanel reads from the
 * openmap properties file:
 * <p>
 * 
 * <pre>
 * 
 *  #------------------------------
 *  # Properties for BeanPanel
 *  #------------------------------
 *  # This property should reflect the paths to the directories 
 *  # containing the bean jars, separated by a space.
 *  beanpanel.beans.path=g:/path-one/jars h:/path-two/lib
 *  
 *  # This property should reflect the logical names of tabs in the BeanPanel,
 *  # separated by a space. The order in which the tabs are specified in this
 *  property is the order in which they appear in the BeanPanel
 *  beanpanel.tabs=tab1 tab2
 * 
 *  # for each tab specified in the beanpabel.tabs property, the following
 *  # two properties should respectively reflect the pretty name of the tab and
 *  # the class names of the beans that should appear in the tab. Class names should
 *  # be separated by spaces.
 *  beanpanel.tab1.name=tab1-pretty-name
 *  beanpanel.tab1.beans=fully-qualified-bean-class-name fully-qualified-bean-class-name ...
 *  beanpanel.tab2.name=tab2-pretty-name
 *  beanpanel.tab2.beans=fully-qualified-bean-class-name fully-qualified-bean-class-name ...
 *  
 *  #-------------------------------------
 *  # End of properties for BeanPanel
 *  #-------------------------------------
 *  
 * <p>
 * <p>
 * 
 *  The BeanPanel looks for beanInfos in the same package as the associated
 *  bean as well as in the Introspector's search path. The Introspector's
 *  search path can be augmented by specifying a comma separated list of
 *  package names in the bean.infos.path system (-D) property.
 *  
 * <p><p>
 * 
 *  A BeanPanel can also be created and used as a standalone class, i.e. independent 
 *  of the openmap components architecture by using the BeanPanel constructor that
 *  takes a Properties object as an argument. This constructor creates and initializes 
 *  a BeanPanel object from properties in the Properties object. The format of the 
 *  properties is the same as the one specified in the openmap properties file.
 *  
 * </pre>
 */
public class BeanPanel extends OMToolComponent implements Serializable {

    /** Default icon for representing a bean */
    public static ImageIcon defaultBeanIcon;

    static {
        augmentBeanInfoSearchPath();
        setDefaultIcon();
    }

    /** Default key for the BeanPanel Tool. */
    public static final String defaultKey = "beanpanel";

    private BeanHelper helper = new BeanHelper();
    private Vector beanLabels = new Vector();
    private Vector beanNames = new Vector();
    private Vector beanIcons = new Vector();
    private Vector beanJars = new Vector();
    private Vector beanInfos = new Vector();

    private Vector beanPaths;
    private HashMap toolbarTabInfo;
    private Vector toolbarTabOrder;

    /** DnD source */
    private DragSource dragSource;

    private JTabbedPane tabbedPane;

    private Cursor customCursor;

    private JFrame beanFrame = null;

    /**
     * Constructs the BeanPanel component, creates a DragSource and
     * DragSourceListener objects and registers itself as the source
     * of Java drag events. Note that this constructor does not
     * initialize the BeanPanel GUI. Instead the GUI is initialized
     * lazily when the user clicks on the 'Face' of this object on the
     * openmap components bar. Thus, this constructor should not be
     * used to create a stand-alone BeanPanel. Use the parameterized
     * constructor to create a stand-alone BeanPanel.
     */
    public BeanPanel() {
        super();
        setKey(defaultKey);

        beanPaths = new Vector();
        toolbarTabInfo = new HashMap();
        toolbarTabOrder = new Vector();

        tabbedPane = new JTabbedPane();

        dragSource = new DragSource();
        ComponentDragSourceListener tdsl = new ComponentDragSourceListener();
        dragSource.createDefaultDragGestureRecognizer(tabbedPane,
                DnDConstants.ACTION_MOVE,
                new ComponentDragGestureListener(tdsl));

        if (Debug.debugging("beanpanel"))
            Debug.output("Created Bean Panel");
    }

    /**
     * This constructor does everything that the default constructor
     * does and in addition initializes the BeanPanel's properties
     * from the Properties object and initializes the BeanPanel GUI.
     * Use this constructor to create a stand-alone BeanPanel.
     */
    public BeanPanel(Properties props) {

        this();

        if (props == null)
            throw new IllegalArgumentException("null props");

        this.setProperties(props);

        this.initGui();

    }

    /**
     * Tool interface method. The retrieval tool's interface. This
     * method creates a button that will bring up the BeanPanel.
     * 
     * @return A container that will contain the 'face' of this panel
     *         on the OpenMap ToolPanel.
     */
    public Container getFace() {
        if (Debug.debugging("beanpanel"))
            Debug.output("Enter> BP::getFace");

        JButton button = null;

        if (defaultBeanIcon == null) {
            if (Debug.debugging("beanpanel"))
                Debug.output("Enter> null defaultBeanIcon!");
            button = new JButton("Bean Box");
        } else
            button = new JButton(defaultBeanIcon);

        button.setBorderPainted(false);
        button.setToolTipText("Bean Box");
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                showBeanPanel(true);
            }
        });

        if (Debug.debugging("beanpanel"))
            Debug.output("Exit> BP::getFace");

        button.setVisible(getUseAsTool());
        return button;
    }

    /**
     * Utility method for finding the BeanInfo associated with a bean
     * class name. This method first attaches the String "BeanInfo" to
     * the end of the class name and then searches the package of the
     * specified class for the BeanInfo class. If the BeanInfo is not
     * found in the bean class's package, then the method searches for
     * the BeanInfo in the Introspector search path.
     * 
     * @param beanClassName the fully qualified name of the bean class
     * @return an instance of the BeanInfo class for the specified
     *         class, if one is found, otherwise null.
     */
    public static synchronized BeanInfo findBeanInfo(String beanClassName) {
        //System.out.println("Finding beanInfo for " +
        // beanClassName);
        String[] beanInfoPaths = Introspector.getBeanInfoSearchPath();
        String infoClassName = beanClassName + "BeanInfo";
        Class infoClass = null;

        try {
            infoClass = Class.forName(infoClassName);
            //System.out.println("returning " + infoClass);
            return (BeanInfo) infoClass.newInstance();
        } catch (Exception ex) {
            //System.out.println ("Unable to find BeanInfo class for
            // " + infoClassName);
        }

        for (int i = 0; i < beanInfoPaths.length; i++) {
            //System.out.println ("Looking in " + beanInfoPaths[i]);
            int index = beanClassName.lastIndexOf(".");
            String classNameWithDot = beanClassName.substring(index);
            infoClassName = beanInfoPaths[i] + classNameWithDot + "BeanInfo";
            try {
                infoClass = Class.forName(infoClassName);
                break;
            } catch (ClassNotFoundException ex) {
                //System.out.println ("Unable to find BeanInfo class
                // for " + infoClassName);
            }
        }

        Object retval = null;

        if (infoClass != null) {
            try {
                retval = infoClass.newInstance();
            } catch (Exception ex) {
                //System.out.println("Unable to instantiate " +
                // infoClassName);
            }
        }

        //System.out.println("returning " + infoClass);

        return (BeanInfo) retval;
    }

    /**
     * Loads java beans from jar files. This method first gets the
     * locations of the jar files from the openmap properties file and
     * then loads the bean classes from them.
     */
    private void loadBeans() {

        Vector jarNames = getJarNames();
        for (int i = 0; i < jarNames.size(); i++) {
            String jarFileName = (String) jarNames.elementAt(i);
            try {
                JarLoader.loadJarDoOnBean(jarFileName, helper);
            } catch (Throwable th) {
                System.out.println("BP::loadBeans: jar load failed: "
                        + jarFileName);
                th.printStackTrace();
            }
        }
    }

    private Vector getJarNames() {
        Vector result = new Vector();

        if (beanPaths == null || beanPaths.isEmpty())
            return result;

        for (int i = 0; i < beanPaths.size(); i++) {
            String path = (String) beanPaths.get(i);
            File dir = new File(path);

            if (!dir.isDirectory()) {
                System.out.println("BP::getJarNames: " + dir
                        + " is not a directory!");
                continue;
            }

            String names[] = dir.list(new FileExtension(".jar"));

            for (int j = 0; j < names.length; j++)
                result.add(dir.getPath() + File.separatorChar + names[j]);
        }

        return result;
    }

    private JList createTab(Vector beanClassNames) {
        final JList list = new JList();

        if (beanClassNames == null || beanClassNames.isEmpty())
            return list;

        Vector labels = new Vector();

        for (int i = 0; i < beanClassNames.size(); i++) {
            String beanClassName = (String) beanClassNames.get(i);
            int index = beanNames.indexOf(beanClassName);

            if (index < 0) {
                System.out.println("BP::createTab: could not locate beanClass="
                        + beanClassName);
                continue;
            }

            String label = (String) beanLabels.get(index);
            labels.add(label);
        }

        list.setListData(labels);
        list.setCellRenderer(new MyCellRenderer());

        MouseEventForwarder forwarder = new MouseEventForwarder();
        list.addMouseListener(forwarder);
        list.addMouseMotionListener(forwarder);

        return list;
    }

    private synchronized void showBeanPanel(boolean isVisible) {

        if (beanFrame != null) {
            beanFrame.setVisible(isVisible);
            if (isVisible)
                beanFrame.toFront();
            return;
        }

        if (!isVisible)
            return;

        initGui();
        beanFrame = new JFrame("Bean Box");
        beanFrame.getContentPane()
                .setLayout(new BoxLayout(beanFrame.getContentPane(), BoxLayout.Y_AXIS));
        beanFrame.getContentPane().add(this);

        beanFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                beanFrame.setVisible(false);
            }
        });

        beanFrame.pack();
        beanFrame.setVisible(true);
    }

    private synchronized void initGui() {

        loadBeans();

        this.setLayout(new BorderLayout());

        if (toolbarTabOrder != null && !toolbarTabOrder.isEmpty()) {
            MouseEventForwarder forwarder = new MouseEventForwarder();
            tabbedPane.addMouseListener(forwarder);
            tabbedPane.addMouseMotionListener(forwarder);

            for (int i = 0; i < toolbarTabOrder.size(); i++) {
                String tabName = (String) toolbarTabOrder.get(i);
                Vector beanClassNames = (Vector) toolbarTabInfo.get(tabName);
                JList listTab = createTab(beanClassNames);
                tabbedPane.addTab(tabName, listTab);
            }

            JScrollPane sPane = new JScrollPane(tabbedPane);

            add(sPane, BorderLayout.CENTER);
        }

        setPreferredSize(new Dimension(400, 250));
        setMinimumSize(new Dimension(400, 250));

    }

    public synchronized void setProperties(String prefix, Properties props) {

        loadBeanPaths(props);

        loadToolBarTabInfo(props);
    }

    private void loadBeanPaths(Properties props) {
        if (Debug.debugging("beanpanel"))
            Debug.output("Enter> BP::loadBeanPaths");
        String beanPathsStr = props.getProperty("beanpanel.beans.path");

        if ((beanPathsStr != null)
                && !((beanPathsStr = beanPathsStr.trim()).length() == 0)) {
            StringTokenizer st = new StringTokenizer(beanPathsStr, " ");

            while (st.hasMoreTokens())
                beanPaths.add(st.nextToken());
        }

        if (Debug.debugging("beanpanel"))
            Debug.output("beanPaths=" + beanPaths);
        if (Debug.debugging("beanpanel"))
            Debug.output("Exit> BP::loadBeanPaths");
    }

    private void loadToolBarTabInfo(Properties props) {
        if (Debug.debugging("beanpanel"))
            Debug.output("Enter> BP::loadToolBarTabInfo");

        String tabsStr = props.getProperty("beanpanel.tabs");

        if ((tabsStr != null) && !((tabsStr = tabsStr.trim()).length() == 0)) {
            StringTokenizer st = new StringTokenizer(tabsStr, " ");

            while (st.hasMoreTokens()) {
                String tab = st.nextToken();
                String tabName = props.getProperty("beanpanel." + tab + ".name");
                String beanClassesStr = props.getProperty("beanpanel." + tab
                        + ".beans");

                if ((beanClassesStr != null)
                        && !((beanClassesStr = beanClassesStr.trim()).length() == 0)) {
                    StringTokenizer st2 = new StringTokenizer(beanClassesStr, " ");
                    Vector beanClassNames = new Vector();

                    while (st2.hasMoreTokens())
                        beanClassNames.add(st2.nextToken());

                    toolbarTabInfo.put(tabName, beanClassNames);
                    toolbarTabOrder.add(tabName);
                }
            }
        }

        if (Debug.debugging("beanpanel"))
            Debug.output("toolbarTabInfo=" + toolbarTabInfo);
        if (Debug.debugging("beanpanel"))
            Debug.output("toolbarTabOrder=" + toolbarTabOrder);
        if (Debug.debugging("beanpanel"))
            Debug.output("Exit> BP::loadToolBarTabInfo");
    }

    private void setDragCursor(int index) {
        ImageIcon icon = (ImageIcon) beanIcons.get(index);
        Point offset = new Point(0, 0);
        Image img = icon.getImage();

        customCursor = Toolkit.getDefaultToolkit().createCustomCursor(img,
                offset,
                "");
    }

    private class BeanHelper implements DoOnBean {

        public void action(JarInfo ji, BeanInfo bi, Class beanClass,
                           String beanName) {

            if (Debug.debugging("beanpanel"))
                Debug.output("Enter> ACTION: " + beanName);

            if (Debug.debugging("beanpanel"))
                Debug.output("ACTION: " + beanName);
            if (Debug.debugging("beanpanel"))
                Debug.output("bi: " + bi);
            if (Debug.debugging("beanpanel"))
                Debug.output("bi.getClass(): " + bi.getClass());

            String label;
            ImageIcon icon = null;

            if (beanName.equals(beanClass.getName())) {
                if (Debug.debugging("beanpanel"))
                    Debug.output("beanName=" + beanName);
                BeanDescriptor bd = bi.getBeanDescriptor();
                if (bd != null)
                    label = bd.getDisplayName();
                else {
                    int index = beanName.lastIndexOf(".");
                    if (index >= 0 && index < beanName.length() - 1)
                        label = beanName.substring(index + 1, beanName.length());
                    else
                        label = beanName;
                }
                if (Debug.debugging("beanpanel"))
                    Debug.output("label=" + label);
                Image img = bi.getIcon(BeanInfo.ICON_COLOR_32x32);
                if (Debug.debugging("beanpanel"))
                    Debug.output("img=" + img);

                if (img == null) {
                    URL url = this.getClass().getResource("bluebean.gif");
                    icon = new ImageIcon(url);
                } else
                    icon = new ImageIcon(img);
            } else {
                label = beanName;
                int ix = beanName.lastIndexOf('.');

                if (ix >= 0)
                    label = beanName.substring(ix + 1);
            }

            beanLabels.addElement(label);
            beanNames.addElement(beanClass.getName());
            beanIcons.addElement(icon);
            beanJars.addElement(ji);
            beanInfos.addElement(bi);

            if (Debug.debugging("beanpanel"))
                Debug.output("Exit> ACTION: " + beanName);
        }

        public void error(String message, Exception e) {
            if (Debug.debugging("beanpanel"))
                Debug.output("BP::BeanHelper:error " + message);
            e.printStackTrace();
        }

        public void error(String message) {
            if (Debug.debugging("beanpanel"))
                Debug.output("BP::BeanHelper:error " + message);
        }
    }

    private class MyCellRenderer implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, // value
                                                                                // to
                                                                                // display
                                                      int index, // cell
                                                                 // index
                                                      boolean isSelected, // is
                                                                          // the
                                                                          // cell
                                                                          // selected
                                                      boolean cellHasFocus) // the
                                                                            // list
                                                                            // and
                                                                            // the
                                                                            // cell
                                                                            // have
                                                                            // the
                                                                            // focus
        {
            String s = value.toString();

            JLabel label = new JLabel(s);
            label.setHorizontalAlignment(JLabel.LEFT);
            int i = beanLabels.indexOf(s);
            ImageIcon icon = (ImageIcon) beanIcons.get(i);
            label.setIcon(icon);

            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }

            label.setEnabled(list.isEnabled());
            label.setFont(list.getFont());
            label.setOpaque(true);
            return label;
        }
    }

    /* DnD Listeners */

    private class ComponentDragSourceListener implements DragSourceListener {
        public void dragDropEnd(DragSourceDropEvent dsde) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dragDropEnd (drag)");
        }

        public void dragEnter(DragSourceDragEvent dsde) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dragEnter (drag)");
            int action = dsde.getDropAction();
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(customCursor);
            } else {
                dsde.getDragSourceContext()
                        .setCursor(DragSource.DefaultCopyNoDrop);
            }
        }

        public void dragOver(DragSourceDragEvent dsde) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dragOver (drag)");
            int action = dsde.getDropAction();
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(customCursor);
            } else {
                dsde.getDragSourceContext()
                        .setCursor(DragSource.DefaultCopyNoDrop);
            }
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dropActionChanged (drag)");
            int action = dsde.getDropAction();
            if (action == DnDConstants.ACTION_MOVE) {
                dsde.getDragSourceContext().setCursor(customCursor);
            } else {
                dsde.getDragSourceContext()
                        .setCursor(DragSource.DefaultCopyNoDrop);
            }
        }

        public void dragExit(DragSourceEvent dse) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dragExit (drag)");
            dse.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
        }
    }

    private class ComponentDragGestureListener implements DragGestureListener {
        ComponentDragSourceListener tdsl;

        public ComponentDragGestureListener(ComponentDragSourceListener tdsl) {
            this.tdsl = tdsl;
        }

        public void dragGestureRecognized(DragGestureEvent dge) {
            if (Debug.debugging("beanpanel"))
                Debug.output("dragGestureRecognized");
            JList list = (JList) tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
            String label = null;
            label = (String) list.getSelectedValue();
            if (label != null) {
                int index = beanLabels.indexOf(label);
                if (index == -1) {
                    System.out.println("ERROR> BP::dragGestureRecognized: "
                            + "no beanlabel found for label=" + label);
                    return;
                }

//                JarInfo ji = (JarInfo) beanJars.get(index);
                String beanName = (String) beanNames.get(index);

                Object bean = null;
                try {
                    bean = Beans.instantiate(null, beanName);
                    if (Debug.debugging("beanpanel"))
                        Debug.output("Instantiated bean: " + bean);
                    setDragCursor(index);

                } catch (Exception ex) {
                    System.out.println("ERROR> BP::dragGestureRecognized: "
                            + " error instantiating bean");
                    ex.printStackTrace();
                    return;
                }

                BeanInfo bi = (BeanInfo) beanInfos.get(index);

                Vector beanTransferData = new Vector();
                beanTransferData.add(bean);
                beanTransferData.add(bi);
                beanTransferData.add(new Boolean(false));
                dragSource.startDrag(dge,
                        customCursor,
                        new DefaultTransferableObject(beanTransferData),
                        tdsl);

                revalidate();
                repaint();
            }
        }
    }

    private class MouseEventForwarder extends MouseInputAdapter {

        public void mousePressed(MouseEvent e) {
            Component comp = (Component) e.getSource();
            Container parent = comp.getParent();
            if (parent != null) {
                Point newPoint = SwingUtilities.convertPoint(comp,
                        e.getPoint(),
                        parent);
                e.translatePoint(newPoint.x - e.getX(), newPoint.y - e.getY());
                MouseEvent me = new MouseEvent(parent, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
                parent.dispatchEvent(me);
            }
        }

        public void mouseReleased(MouseEvent e) {
            Component comp = (Component) e.getSource();
            Container parent = comp.getParent();
            if (parent != null) {
                Point newPoint = SwingUtilities.convertPoint(comp,
                        e.getPoint(),
                        parent);
                e.translatePoint(newPoint.x - e.getX(), newPoint.y - e.getY());
                MouseEvent me = new MouseEvent(parent, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
                parent.dispatchEvent(me);
            }
        }

        public void mouseDragged(MouseEvent e) {
            Component comp = (Component) e.getSource();
            Container parent = comp.getParent();
            if (parent != null) {
                Point newPoint = SwingUtilities.convertPoint(comp,
                        e.getPoint(),
                        parent);
                e.translatePoint(newPoint.x - e.getX(), newPoint.y - e.getY());
                MouseEvent me = new MouseEvent(parent, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), e.isPopupTrigger());
                parent.dispatchEvent(me);
            }
        }
    }

    private static void setDefaultIcon() {
        if (BeanPanel.defaultBeanIcon == null) {
            URL url = BeanPanel.class.getResource("bluebean.gif");
            if (url != null)
                BeanPanel.defaultBeanIcon = new ImageIcon(url);
        }
    }

    private static void augmentBeanInfoSearchPath() {

        if (Debug.debugging("beanpanel"))
            Debug.output("Enter> augmentBeanInfoSearchPath");

        String beanInfoPath = System.getProperty("bean.infos.path");

        if (Debug.debugging("beanpanel"))
            Debug.output("beanInfoPath=" + beanInfoPath);

        if (beanInfoPath == null
                || (beanInfoPath = beanInfoPath.trim()).length() == 0)
            return;

        String[] oldPath = java.beans.Introspector.getBeanInfoSearchPath();

        Vector newPath = new Vector();

        if (oldPath != null && oldPath.length > 0)
            newPath.addAll(Arrays.asList(oldPath));

        if (Debug.debugging("beanpanel"))
            Debug.output("oldPath=" + newPath);

        StringTokenizer st = new StringTokenizer(beanInfoPath, ", ");

        while (st.hasMoreTokens()) {
            String path = st.nextToken();

            if (newPath.contains(path))
                continue;

            newPath.add(path);
        }

        java.beans.Introspector.setBeanInfoSearchPath((String[]) newPath.toArray(new String[newPath.size()]));

        if (Debug.debugging("beanpanel"))
            Debug.output("UPDATED> beanInfo search path to: " + newPath);

        if (Debug.debugging("beanpanel"))
            Debug.output("Exit> augmentBeanInfoSearchPath");
    }

    public static void main(String[] args) {

        Properties props = new Properties();
        props.setProperty("beanpanel.beans.path", "");
        props.setProperty("beanpanel.tabs", "tab1 tab2 tab3");
        props.setProperty("beanpanel.tab1.name", "Generic");
        props.setProperty("beanpanel.tab1.beans",
                "com.bbn.openmap.examples.beanbox.SimpleBeanObject");
        props.setProperty("beanpanel.tab2.name", "Container");
        props.setProperty("beanpanel.tab2.beans",
                "com.bbn.openmap.examples.beanbox.SimpleBeanContainer");
        props.setProperty("beanpanel.tab3.name", "Military");
        props.setProperty("beanpanel.tab3.beans",
                "com.bbn.openmap.examples.beanbox.Fighter");

        BeanPanel bp = new BeanPanel(props);
        JFrame beanFrame = new JFrame("Bean Box");
        beanFrame.getContentPane().add(bp);
        beanFrame.pack();
        beanFrame.setVisible(true);

        try {
            Thread.sleep(2000);
            beanFrame.setVisible(false);
            Thread.sleep(2000);
            beanFrame.setVisible(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

