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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/GoToMenu.java,v $
// $RCSfile: GoToMenu.java,v $
// $Revision: 1.20 $
// $Date: 2008/01/29 22:04:13 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.gui.menu.DataBoundsViewMenuItem;
import com.bbn.openmap.gui.menu.OMBasicMenu;
import com.bbn.openmap.proj.Mercator;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.proj.ProjectionFactory;
import com.bbn.openmap.util.DataBoundsProvider;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PropUtils;

/**
 * Menu that keeps track of different saved map views (lat/lon, scale and
 * projection type), and provides a way to set the map projection to those
 * views. There is a set of optional default views, but new views can be added.
 * If these views are added to the properties file, they will be added to the
 * menu automatically for later uses. This menu can understand a set of
 * properties:
 * 
 * <pre>
 * 
 * 
 *   gotoMenu.class=com.bbn.openmap.gui.GoToMenu
 *   #Add the default, world view option
 *   gotoMenu.addDefaults=true
 *   #Add the menu for DataBoundsProviders
 *   gotoMenu.addDataViews=true
 *   #Additional views
 *   goto.views=Argentina India United_States Caspian_Sea
 *   Argentina.latitude=-39.760445
 *   Argentina.longitude=-65.92294
 *   Argentina.name=Argentina
 *   Argentina.projection=Mercator
 *   Argentina.scale=5.0E7
 *   India.latitude=20.895763
 *   India.longitude=80.437485
 *   India.name=India
 *   India.projection=Mercator
 *   India.scale=3.86688E7
 *   United_States.latitude=38.82259
 *   United_States.longitude=-96.74999
 *   United_States.name=United States
 *   United_States.projection=Mercator
 *   United_States.scale=5.186114E7
 *   Caspian_Sea.name=Caspian Sea
 *   Caspian_Sea.latitude=40f
 *   Caspian_Sea.longitude=47f
 *   Caspian_Sea.scale=1000000
 *   Caspian_Sea.projection=CADRG
 * 
 * 
 * </pre>
 */
public class GoToMenu extends AbstractOpenMapMenu {

    private String defaultText = "Views";
    private String defaultMnemonic = "V";

    protected Hashtable dataBoundsProviders = new Hashtable();
    protected OMBasicMenu dataBoundsMenu;
    protected MapBean map;

    /**
     * A space separated list of marker names for the views to be loaded from
     * the properties.
     */
    public final static String ViewListProperty = "views";
    /** The name of the view to use in the GUI. */
    public final static String NameProperty = "name";
    /** The center latitude of the view projection. */
    public final static String LatProperty = "latitude";
    /** The center longitude of the view projection. */
    public final static String LonProperty = "longitude";
    /** The scale of the view projection. */
    public final static String ScaleProperty = "scale";
    /** The projection type of the view projection. */
    public final static String ProjectionTypeProperty = "projection";
    /** Flag to use to add default views (World, each continent. */
    public final static String AddDefaultListProperty = "addDefaults";
    /**
     * Flag to use to enable/disable the gathering of DataBoundsProviders.
     */
    public final static String AddDataViewsProperty = "addDataViews";

    protected boolean addDefaults = true;
    protected boolean addDataViews = true;

    public GoToMenu() {
        super();
        setText(i18n.get(this, "goto", defaultText));
        // setMnemonic(i18n.get(this, "goto", I18n.MNEMONIC, defaultMnemonic)
        // .charAt(0));

        // dataBoundsMenu = new OMBasicMenu("Go Over Data");
        dataBoundsMenu = new OMBasicMenu(i18n.get(this,
                "goOverData",
                "Go Over Data"));

        // add(new AddNewViewButton("Add Saved View..."));
        add(new AddNewViewButton(i18n.get(this,
                "addSavedView",
                "Add Saved View...")));
        add(dataBoundsMenu);
        add(new JSeparator());
    }

    public void findAndUndo(Object someObj) {
        super.findAndUndo(someObj);
        if (someObj instanceof MapBean) {
            // do the initializing that need to be done here
            if (getMap() == (MapBean) someObj) {
                setMap(null);
            }
        }

        if (someObj instanceof DataBoundsProvider) {
            removeDataBoundsProvider((DataBoundsProvider) someObj);
        }
    }

    public void findAndInit(Object someObj) {
        super.findAndInit(someObj);
        if (someObj instanceof MapBean) {
            // do the initializing that need to be done here
            setMap((MapBean) someObj);
        }

        if (someObj instanceof DataBoundsProvider) {
            addDataBoundsProvider((DataBoundsProvider) someObj);
        }
    }

    /** Set the map to control. */
    public void setMap(MapBean mb) {
        map = mb;
    }

    public MapBean getMap() {
        return map;
    }

    /** PropertyConsumer interface method. */
    public void setProperties(String prefix, Properties props) {
        super.setProperties(prefix, props);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        addDefaults = PropUtils.booleanFromProperties(props, prefix
                + AddDefaultListProperty, addDefaults);

        addDataViews = PropUtils.booleanFromProperties(props, prefix
                + AddDataViewsProperty, addDataViews);

        dataBoundsMenu.setVisible(addDataViews);

        if (addDefaults) {
            addDefaultLocations();
            add(new JSeparator());
        }

        String locationList = props.getProperty(prefix + ViewListProperty);

        if (locationList != null) {
            Vector views = PropUtils.parseSpacedMarkers(locationList);
            for (Iterator it = views.iterator(); it.hasNext();) {
                String viewPrefix = (String) it.next();
                addLocationItem(viewPrefix, props);
            }
        }
    }

    /** PropertyConsumer interface method. */
    public Properties getProperties(Properties props) {
        props = super.getProperties(props);

        String prefix = PropUtils.getScopedPropertyPrefix(this);

        props.put(prefix + AddDefaultListProperty,
                new Boolean(addDefaults).toString());
        props.put(prefix + AddDataViewsProperty,
                new Boolean(addDataViews).toString());

        StringBuffer viewList = new StringBuffer();

        for (Iterator it = ensureCustomViews().iterator(); it.hasNext();) {
            GoToButton gtb = (GoToButton) it.next();

            String sanitizedName = gtb.getText().replace(' ', '_');
            viewList.append(" ").append(sanitizedName);

            sanitizedName = PropUtils.getScopedPropertyPrefix(sanitizedName);

            props.put(sanitizedName + NameProperty, gtb.getText());
            props.put(sanitizedName + LatProperty,
                    new Float(gtb.latitude).toString());
            props.put(sanitizedName + LonProperty,
                    new Float(gtb.longitude).toString());
            props.put(sanitizedName + ScaleProperty,
                    new Float(gtb.scale).toString());
            props.put(sanitizedName + ProjectionTypeProperty, gtb.projectionID);

        }

        props.put(prefix + ViewListProperty, viewList.toString());

        return props;
    }

    /** PropertyConsumer interface method. */
    public Properties getPropertyInfo(Properties props) {
        props = super.getPropertyInfo(props);

        props.put(ViewListProperty,
                "Space-separated marker list of different views");
        props.put(AddDefaultListProperty,
                "Flag to add default views (true/false).");
        props.put(AddDefaultListProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        props.put(AddDataViewsProperty,
                "Flag to add views from some data components.");
        props.put(AddDataViewsProperty + ScopedEditorProperty,
                "com.bbn.openmap.util.propertyEditor.YesNoPropertyEditor");
        props.put(NameProperty, "The formal name of the view for the user.");
        props.put(LatProperty, "The latitude of the center of the view.");
        props.put(LonProperty, "The longitude of the center of the view.");
        props.put(ScaleProperty, "The scale of the view.");
        props.put(ProjectionTypeProperty, "The projection name of the view");

        return props;
    }

    /** Add the default views to the menu. */
    public void addDefaultLocations() {
        add(new GoToButton(i18n.get(this, "world", "World"), 0, 0, Float.MAX_VALUE, Mercator.MercatorName));
    }

    protected List customViews;

    /**
     * An internal callback method that creates the custom views List object.
     * Override to change what kind of object gets created (it's a Vector by
     * default).
     * 
     * @return List
     */
    protected List createCustomViews() {
        return new Vector();
    }

    public List getCustomViews() {
        return customViews;
    }

    /**
     * Should call this when dealing with the custom views list, will always
     * return a non-null answer.
     * 
     * @return List
     */
    public List ensureCustomViews() {
        if (customViews == null) {
            customViews = createCustomViews();
        }
        return customViews;
    }

    public void setCustomViews(List views) {
        customViews = views;
    }

    /**
     * Parse and add the view from properties.
     */
    public void addLocationItem(String prefix, Properties props) {
        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        String locationName = props.getProperty(prefix + NameProperty);
        String latString = props.getProperty(prefix + LatProperty);
        String lonString = props.getProperty(prefix + LonProperty);
        String scaleString = props.getProperty(prefix + ScaleProperty);
        String projID = props.getProperty(prefix + ProjectionTypeProperty);

        if (Debug.debugging("goto")) {
            Debug.output("GoToMenu: adding view - " + locationName + ", "
                    + latString + ", " + lonString + ", " + scaleString + ", "
                    + projID);
        }

        try {
            float lat = new Float(latString).floatValue();
            float lon = new Float(lonString).floatValue();
            float scale = new Float(scaleString).floatValue();
            GoToButton gtb = new GoToButton(locationName, lat, lon, scale, projID);
            ensureCustomViews().add(gtb);
            add(gtb);

        } catch (NumberFormatException nfe) {
            return;
        }
    }

    public void addDataBoundsProvider(DataBoundsProvider provider) {
        if (!dataBoundsProviders.containsKey(provider)) {
            DataBoundsViewMenuItem dbvmi = new DataBoundsViewMenuItem(provider);
            dataBoundsProviders.put(provider, dbvmi);
            dbvmi.findAndInit(getBeanContext());
            dataBoundsMenu.add(dbvmi);
        }
    }

    public void removeDataBoundsProvider(DataBoundsProvider provider) {
        JMenuItem item = (DataBoundsViewMenuItem) dataBoundsProviders.remove(provider);
        if (item != null) {
            dataBoundsMenu.remove(item);
        }
    }

    /**
     * Add a button to the menu that will set the map to a particular view.
     */
    public void addView(GoToButton newOne) {
        ensureCustomViews().add(newOne);
        add(newOne);
        revalidate();
    }

    /**
     * This is the button that will bring up the dialog to actually name a new
     * view being added. The new view will be the current projection of the map.
     */
    public class AddNewViewButton extends JMenuItem implements ActionListener {

        public AddNewViewButton(String title) {
            super(title);
            this.addActionListener(this);
        }

        public void actionPerformed(ActionEvent ae) {
            if (map != null) {
                Projection proj = map.getProjection();
                Point2D llp = proj.getCenter();
                new GoToButton((float) llp.getY(), (float) llp.getX(), proj.getScale(), proj.getName());
            }
        }
    }

    /**
     * This button contains the trigger for a saved view.
     */
    public class GoToButton extends JMenuItem implements ActionListener {

        public float latitude;
        public float longitude;
        public float scale;
        public String projectionID;

        GoToMenu menu;

        public GoToButton(String title, float lat, float lon, float s,
                String projID) {
            super(title);
            init(lat, lon, s, projID);
        }

        public GoToButton(float lat, float lon, float s, String projID) {
            init(lat, lon, s, projID);
            NameFetcher nf = new NameFetcher(this);
            if (map != null) {
                Point p = map.getLocationOnScreen();
                int x = (int) p.getX();
                int y = (int) p.getY();
                int w = map.getWidth();
                int h = map.getHeight();
                nf.setLocation(x + (w - nf.getWidth()) / 2, y
                        + (h - nf.getHeight()) / 2);
            }
            nf.setVisible(true);
        }

        public void init(float lat, float lon, float s, String projID) {
            latitude = lat;
            longitude = lon;
            scale = s;
            projectionID = projID;
            this.addActionListener(this);
        }

        /**
         * Gets called when the NameFetcher successfully retrieves a name for the
         * view.
         * 
         * @param name new name for the button.
         */
        public void setNameAndAdd(String name) {
            this.setText(name);
            GoToMenu.this.addView(this);
        }

        public void actionPerformed(ActionEvent ae) {
            if (map != null) {
                Projection oldProj = map.getProjection();
                ProjectionFactory projFactory = map.getProjectionFactory();
                Class<? extends Projection> projClass = projFactory.getProjClassForName(projectionID);

                if (projClass == null) {
                    projClass = com.bbn.openmap.proj.Mercator.class;
                }

                Projection newProj = projFactory.makeProjection(projClass,
                        new Point2D.Float(longitude, latitude),
                        scale,
                        oldProj.getWidth(),
                        oldProj.getHeight());

                map.setProjection(newProj);
            }
        }
    }

    /**
     * Brings up a GUI to name a new view.
     */
    public class NameFetcher extends JDialog implements ActionListener {

        JTextField nameField;
        JLabel label;
        JButton closebutton, applybutton;
        GoToButton notifyThis;

        public NameFetcher(GoToButton buttonToName) {

            notifyThis = buttonToName;
            setTitle(i18n.get(GoToMenu.class, "addViewTitle", "Add View"));
            JPanel palette = new JPanel();
            palette.setLayout(new BoxLayout(palette, BoxLayout.Y_AXIS));

            JPanel namePanel = new JPanel();
            namePanel.setLayout(new FlowLayout());

            label = new JLabel(i18n.get(GoToMenu.class,
                    "nameOfView",
                    "Name of View: "));
            nameField = new JTextField("", 20);

            namePanel.add(label);
            namePanel.add(nameField);

            palette.add(namePanel);

            JPanel buttonPanel = new JPanel();
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();

            buttonPanel.setLayout(gridbag);

            applybutton = new JButton(i18n.get(GoToMenu.class,
                    "addView",
                    "Add View"));
            applybutton.addActionListener(this);
            gridbag.setConstraints(applybutton, c);

            closebutton = new JButton(i18n.get(GoToMenu.class, "close", "Close"));
            closebutton.addActionListener(this);
            c.gridx = GridBagConstraints.RELATIVE;
            gridbag.setConstraints(closebutton, c);

            buttonPanel.add(applybutton);
            buttonPanel.add(closebutton);

            palette.add(buttonPanel);

            this.getContentPane().add(palette);
            this.pack();
        }

        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == applybutton) {
                String newName = nameField.getText();
                if (newName != null && !(newName.length() == 0)) {
                    notifyThis.setNameAndAdd(newName);
                }
            }
            this.setVisible(false);
        }
    }
}
