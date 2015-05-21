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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapConnector.java,v $
// $RCSfile: NetMapConnector.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/09 17:46:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.util.PaletteHelper;
import com.bbn.openmap.util.PropUtils;

/**
 * The NetMapConnector is the bridge between the parser from the
 * output of the NetMap server (NetMapReader), and whoever wants the
 * OMGraphicList that is being managed. It forwards the list on to
 * the NetMapListReceiver that wants the list. The NetMapConnector
 * will create it's NetMapReader to control and use. If you have a
 * component that wants to receive updates from the reader, then you
 * can register as an NetMapListener with the NetMapConnector.
 * 
 * The NetMapConnector can be used in conjunction with the
 * NetMapConnectionHandler. The NetMapConnectionHandler will look for
 * NetMapConnectors in the BeanContext (MapHandler), and create a
 * NetMapGraphicLoader for it. That will set off the GraphicLoader ->
 * GraphicLoaderConnector -> GraphicLoaderPlugIn -> PlugInLayer
 * creation chain, if the GraphicLoaderConnector is also in the
 * MapHandler.
 * 
 * The following properties can be set:
 * 
 * <pre>
 * 
 * 
 *  server=hostname of the NetMap server
 *  port=port of NetMap server
 *  defaultView=default view for NetMap server stream
 *  
 * </pre>
 */
public class NetMapConnector implements ActionListener, NetMapConstants,
        PropertyConsumer {

    public final static String ServerConnectCmd = "Connect";
    public final static String ServerDisconnectCmd = "Disconnect";
    public final static String LoadViewCmd = "Load View";
    public final static String GetViewsCmd = "Get Network Views";
    public final static String ServerProperty = "server";
    public final static String PortProperty = "port";
    public final static String DefaultViewProperty = "defaultView";

    public final static String STATUS_CONNECTING = "  Connecting  ";
    public final static String STATUS_CONNECTED = "   Connected  ";
    public final static String STATUS_IDLE = "  Idle  ";

    private JPanel serverPanel = null;

    private JTextField serverAddrField = null;
    private JTextField serverPortField = null;

    private JLabel connectedStatus = null;

    private Choice viewChoice = null;
    private ChoiceList viewList = null;
    private JButton controlButton = null;

    protected String server = DEFAULT_SERVER;
    protected String port = DEFAULT_PORT;

    /**
     * The NetMap server has a notion of views that represent nodes
     * and links, and these are names. If using a GUI, the view names
     * will show up because they are received from the NetMap server.
     * Programmatically, you can set what the default view should be.
     */
    protected String defaultView = null;

    protected String propertyPrefix = null;

    /**
     * The component that listens to the NetMap server and parses the
     * stream.
     */
    NetMapReader reader = null;

    /**
     * Support for sending new NetMap events to listeners.
     */
    NetMapListenerSupport listenerSupport = null;

    public NetMapConnector() {
        listenerSupport = new NetMapListenerSupport(this);
    }

    /**
     * Set the hostname or IP address to use to contact the NetMap
     * server.
     */
    public void setServer(String sName) {
        server = sName;
    }

    /**
     * Get the hostname or IP address of the NetMap server.
     */
    public String getServer() {
        return server;
    }

    /**
     * Set the port that the NetMap server is running on.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get the port that the NetMap server is running on.
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the defaultView to use for NetMap server queries.
     */
    public void setDefaultView(String view) {
        defaultView = view;
    }

    /**
     * Get the defaultView to use for NetMap server queries.
     */
    public String getDefaultView() {
        return defaultView;
    }

    /**
     * Add a NetMapListener to receive NetMapEvents.
     */
    public void addNetMapListener(NetMapListener nml) {
        listenerSupport.addNetMapListener(nml);
    }

    /**
     * Remove a NetMapListener from the list to receive NetMapEvents.
     */
    public void removeNetMapListener(NetMapListener nml) {
        listenerSupport.removeNetMapListener(nml);
    }

    /**
     * Clear all NetMapListeners from receiving NetMapEvents.
     */
    public void clearNetMapListeners() {
        listenerSupport.clearNetMapListeners();
    }

    /**
     * Called by the NetMapReader so a parsed line, representing an
     * event, can be dispersed to the listeners.
     */
    protected void distributeEvent(Properties netmapProps) {
        listenerSupport.fireNetMapEvent(netmapProps);
    }

    /** Act on GUI commands controlling the NetMapReader. */
    public void actionPerformed(java.awt.event.ActionEvent ae) {
        String cmd = ae.getActionCommand();

        server = serverAddrField.getText();
        port = serverPortField.getText();

        if (cmd == GetViewsCmd) {
            connectedStatus.setText(STATUS_CONNECTING);

            viewList = getViews();

            if (viewList == null) {
                Debug.message("netmap", "Can't get view list from " + server
                        + ":" + port);
                disconnect();
            }

        } else if (cmd == ServerDisconnectCmd) {

            Debug.message("netmap", "Disconnecting from server " + server + ":"
                    + port);
            disconnect();

        } else if (cmd == LoadViewCmd) {

            ChoiceItem ci = viewList.get(viewChoice.getSelectedItem());

            if (ci == null) {
                disconnect();
                return;
            }

            String view = ((String) ci.value()).trim();

            Debug.message("netmap", "Loading view " + view);
            connect(view);
        }
    }

    /**
     * Callback for the NetMapReader to let it provide the connector
     * with connection status.
     */
    protected void connectionUp() {
        if (connectedStatus != null) {
            connectedStatus.setText(STATUS_CONNECTED);
            connectedStatus.setBackground(Color.green);
        }
    }

    /**
     * Callback for the NetMapReader to let it provide the connector
     * with connection status.
     */
    protected void connectionDown() {
        if (connectedStatus != null) {
            connectedStatus.setText(STATUS_IDLE);
            connectedStatus.setBackground(Color.red);
        }
    }

    /**
     * Resets the controls to the disconnected mode.
     */
    public void disconnect() {
        if (reader != null) {
            reader.shutdown();
        }

        reader = null;

        if (serverPanel != null) {
            serverAddrField.setEnabled(true);
            serverPortField.setEnabled(true);

            viewChoice.setEnabled(false);

            controlButton.setText(GetViewsCmd);
            controlButton.setActionCommand(GetViewsCmd);
            connectedStatus.setText(STATUS_IDLE);
        }
    }

    /**
     * Gets a list of possible views.
     * 
     * @return ChoiceList of possible views retrieved from the NetMap
     *         server.
     */
    public ChoiceList getViews() {

        ChoiceList views = null;
        try {
            reader = new NetMapReader(server, port, this);
        } catch (IOException e) {
            Debug.message("netmap", "Can't start reader: " + e);
        }

        Debug.message("netmap", "Checking for views...");

        // reader will be null if server or port is bad...
        if (reader != null) {
            views = reader.getViewList(server, port);
        }

        if (serverPanel != null) {
            viewChoice.removeAll();

            if (views != null) {
                for (int i = 0; i < views.size(); i++) {
                    if (Debug.debugging("netmap")) {
                        Debug.output("Adding view: " + views.labelAt(i));
                    }
                    viewChoice.add(views.labelAt(i));
                }

                serverAddrField.setEnabled(false);
                serverPortField.setEnabled(false);

                viewChoice.setEnabled(true);

                controlButton.setText(LoadViewCmd);
                controlButton.setActionCommand(LoadViewCmd);
            }
        }

        return views;
    }

    /**
     * Connects to the NetMap server to get messages about the given
     * view.
     */
    public void connect(String view) {
        try {

            reader = new NetMapReader(server, port, this, view);
            reader.start();

            if (serverPanel != null) {
                serverAddrField.setEnabled(false);
                serverPortField.setEnabled(false);

                controlButton.setText(ServerDisconnectCmd);
                controlButton.setActionCommand(ServerDisconnectCmd);
            }

        } catch (IOException e) {
            Debug.message("netmap", "Can't start reader: " + e);
            disconnect();
        }
    }

    /**
     * Complete disconnect, sends clear command to NetMapListeners,
     * resets GUI if it's being used.
     */
    public void reset() {
        disconnect();

        Properties rp = new Properties();
        rp.setProperty(COMMAND_FIELD, CLEAR);
        distributeEvent(rp);

        if (serverPanel != null) {
            viewChoice.removeAll();
            connectedStatus.setText(STATUS_IDLE);
        }
    }

    /**
     * Gets the GUI control for the NetMapReader, creates it if it
     * doesn't exist.
     */
    public Component getGUI() {
        if (serverPanel != null) {
            return serverPanel;
        }

        serverAddrField = new JTextField(server);
        serverPortField = new JTextField(port);

        /*
         * Make the NETMAP Server address entry field
         */
        JPanel serverAddrPanel = new JPanel(new GridLayout(0, 2));
        serverAddrPanel.add(new JLabel("Name or IP Addr: "));
        serverAddrPanel.add(serverAddrField);
        /*
         * Make the NETMAP Server port entry field
         */
        JPanel serverPortPanel = new JPanel(new GridLayout(0, 2));
        serverPortPanel.add(new JLabel("Port: "));
        serverPortPanel.add(serverPortField);

        /*
         */
        JPanel statusPanel = PaletteHelper.createHorizontalPanel("Server Connection");
        connectedStatus = new JLabel(STATUS_IDLE);
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                reset();
            }
        });

        statusPanel.add(connectedStatus);
        statusPanel.add(resetButton);

        /*
         * Make the toplevel input panel
         */
        //      JPanel netmapPanel = new JPanel(new GridLayout(0, 1));
        JPanel netmapPanel = PaletteHelper.createVerticalPanel("Server Settings");

        netmapPanel.add(serverAddrPanel);
        netmapPanel.add(serverPortPanel);

        /*
         * Make the "Load View" panel
         */
        viewChoice = new Choice();
        viewList = new ChoiceList();
        viewChoice.setEnabled(false);

        controlButton = new JButton(GetViewsCmd);
        controlButton.setActionCommand(GetViewsCmd);
        controlButton.addActionListener(this);

        //      JPanel viewPanel = new JPanel(new GridLayout(0, 1));
        JPanel viewPanel = PaletteHelper.createVerticalPanel(null);

        viewPanel.add(new JLabel("Available Views"));
        viewPanel.add(viewChoice);
        viewPanel.add(controlButton);

        serverPanel = new JPanel();
        Box box = Box.createVerticalBox();
        box.add(netmapPanel);
        box.add(statusPanel);
        box.add(viewPanel);
        serverPanel.add(box);

        return serverPanel;
    }

    /**
     * Method to set the properties in the PropertyConsumer. It is
     * assumed that the properties do not have a prefix associated
     * with them, or that the prefix has already been set.
     * 
     * @param setList a properties object that the PropertyConsumer
     *        can use to retrieve expected properties it can use for
     *        configuration.
     */
    public void setProperties(Properties setList) {
        setProperties(null, setList);
    }

    /**
     * Method to set the properties in the PropertyConsumer. The
     * prefix is a string that should be prepended to each property
     * key (in addition to a separating '.') in order for the
     * PropertyConsumer to uniquely identify properties meant for it,
     * in the midst of of Properties meant for several objects.
     * 
     * @param prefix a String used by the PropertyConsumer to prepend
     *        to each property value it wants to look up -
     *        setList.getProperty(prefix.propertyKey). If the prefix
     *        had already been set, then the prefix passed in should
     *        replace that previous value.
     * @param setList a Properties object that the PropertyConsumer
     *        can use to retrieve expected properties it can use for
     *        configuration.
     */
    public void setProperties(String prefix, Properties setList) {
        setPropertyPrefix(prefix);

        prefix = PropUtils.getScopedPropertyPrefix(prefix);

        server = setList.getProperty(prefix + ServerProperty);
        if (server == null) {
            server = DEFAULT_SERVER;
        }

        port = setList.getProperty(prefix + PortProperty);
        if (port == null) {
            port = DEFAULT_PORT;
        }
    }

    /**
     * Method to fill in a Properties object, reflecting the current
     * values of the PropertyConsumer. If the PropertyConsumer has a
     * prefix set, the property keys should have that prefix plus a
     * separating '.' prepended to each property key it uses for
     * configuration.
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getProperties(Properties list) {
        if (list == null) {
            list = new Properties();
        }
        String prefix = PropUtils.getScopedPropertyPrefix(this);

        list.put(prefix + ServerProperty, server);
        list.put(prefix + PortProperty, port);
        return list;
    }

    /**
     * Method to fill in a Properties object with values reflecting
     * the properties able to be set on this PropertyConsumer. The key
     * for each property should be the raw property name (without a
     * prefix) with a value that is a String that describes what the
     * property key represents, along with any other information about
     * the property that would be helpful (range, default value,
     * etc.).
     * 
     * @param list a Properties object to load the PropertyConsumer
     *        properties into. If getList equals null, then a new
     *        Properties object should be created.
     * @return Properties object containing PropertyConsumer property
     *         values. If getList was not null, this should equal
     *         getList. Otherwise, it should be the Properties object
     *         created by the PropertyConsumer.
     */
    public Properties getPropertyInfo(Properties list) {
        if (list == null) {
            list = new Properties();
        }

        list.put(ServerProperty, "The hostname or IP for NetMap server");
        list.put(PortProperty, "The port number for NetMap server");
        return list;
    }

    /**
     * Set the property key prefix that should be used by the
     * PropertyConsumer. The prefix, along with a '.', should be
     * prepended to the property keys known by the PropertyConsumer.
     * 
     * @param prefix the prefix String.
     */
    public void setPropertyPrefix(String prefix) {
        propertyPrefix = prefix;
    }

    /**
     * Get the property key prefix that is being used to prepend to
     * the property keys for Properties lookups.
     * 
     * @return the property prefix
     */
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
}

