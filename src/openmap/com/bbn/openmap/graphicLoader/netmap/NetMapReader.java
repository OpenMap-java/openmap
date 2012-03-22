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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/netmap/NetMapReader.java,v $
// $RCSfile: NetMapReader.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/09 17:46:33 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.graphicLoader.netmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.Socket;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.util.Debug;

/**
 * The NetMapReader is the class that makes actual contact with the
 * NetMap server and maintains the connection to it. It is controlled
 * by the NetMapConnector, and is created by it as well. The
 * NetMapReader understands that the NetMap server maintains a notion
 * of views, and that to get information from the server, it has to
 * request information about a particular view. Luckily, a list of
 * views can be requested from the server as well.
 * <P>
 * 
 * The NetMap server will provide a text output stream of lines, with
 * each line representing an event. The NetMapReader has an implicit
 * understanding of the format of different line types, and creates a
 * java Properties object for each line, categorizing each field, and
 * putting it in the Properties object as (Field Header key, Field
 * value). This Properties object is sent to the NetMapConnector,
 * where it is distributed to the NetMapEventListeners. Each
 * NetMapEventListener is free to peruse the Properties object in the
 * event, and get information it needs from each one by asking for
 * specific field values. Some fields may not be present in all
 * Properties objects, but the fields should be consistent for the
 * same event type. At some point, we'll include a description of the
 * events and the fields that can be expected.
 * 
 * In general, there are two things you can do. First, you can create
 * a NetMapReader without a view, and then call getViewList() on it to
 * get a list of possible views. The NetMapReader will disconnect
 * itself after that request. Second, you can create a NetMapReader
 * with a specific view (or set the view later), and then call start()
 * on it to begin receiving events about that view. Call disconnect()
 * when you want it to stop.
 * <P>
 */
public class NetMapReader extends Thread implements NetMapConstants {

    private static Object EOF = new Object();
    private static Object LP = new Object();
    private static Object RP = new Object();

    StreamTokenizer st = null;
    boolean shutdown = false;
    String viewName = null;
    Socket s = null;

    /**
     * This is the component that is organizing the dispersal of the
     * events as a result of reading stuff off the NetMap connection.
     */
    NetMapConnector netmapConn;

    boolean DEBUG = false;
    boolean DEBUG_VERBOSE = false;

    /**
     * Create a NetMapReader to listen to a host on a port, with a
     * NetMapConnector to get back in touch with. This method just
     * connects, finds out the views, and closes the connection to the
     * server.
     */
    public NetMapReader(String host, String port, NetMapConnector connector)
            throws IOException {

        this(host, port, connector, null);
    }

    /**
     * Create a NetMapReader to listen to a NetMapServer running on a
     * port, and parse the stream relating information about the given
     * view. If the view is null, then the NetMapReader will get a
     * view list and disconnect.
     */
    public NetMapReader(String host, String port, NetMapConnector connector,
            String view) throws IOException {

        netmapConn = connector;

        if (view != null) {
            setView(view);
        }

        DEBUG = Debug.debugging("netmap");
        DEBUG_VERBOSE = Debug.debugging("netmap_verbose");

        try {
            s = connect(host, port);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Set the view that will be requested when the reader is started,
     * via reader.start().
     */
    public void setView(String view) {
        this.viewName = view;
    }

    /**
     * A general connection method that returns a socket for a host
     * and port.
     */
    private Socket connect(String host, String portString) throws IOException {

        int port = 0;
        Socket sock = null;

        boolean DEBUG = Debug.debugging("netmap");

        try {
            port = Integer.parseInt(portString, 10);
        } catch (NumberFormatException e) {
            if (DEBUG)
                Debug.output("Illegal name " + host + ":" + portString);
            throw new IOException("Illegal port: " + portString);
        }

        if (DEBUG)
            Debug.output("Connecting to server " + host + ":" + port);

        try {
            sock = new Socket(host, port);
        } catch (IOException e) {
            if (sock != null)
                sock.close();

            if (DEBUG) {
                Debug.output("Can't connect to " + host + ":" + port + "\n   "
                        + e);
            }

            throw e;
        }

        return sock;
    }

    /**
     * For an established NetMapReader, get the list of views that the
     * NetMapReader knows about. The NetMapReader will disconnect
     * after this query.
     */
    public ChoiceList getViewList(String host, String port) {
        BufferedReader in = null;
        PrintWriter out = null;
        ChoiceList viewList = null;
        Socket sock = null;

        boolean DEBUG = Debug.debugging("netmap");

        try {
            sock = connect(host, port);

            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true);

            // Now load the views from the server
            out.println(JMAP_VIEW_CMD);
        } catch (IOException e) {
            Debug.error("NetMapReader: " + e);
            return null;
        }

        viewList = new ChoiceList();

        try {
            String line = null;

            while ((line = in.readLine()) != null) {
                line = line.trim();

                int labelStart = line.indexOf("\'");
                int labelEnd = line.lastIndexOf("\'");

                String viewLabel = null;
                String viewLine = null;

                if (DEBUG)
                    Debug.output("View definition: " + line);

                if (labelStart < 0) {
                    viewLine = line;
                    viewLabel = line;

                } else {

                    viewLine = line.substring(0, labelStart) + " "
                            + line.substring(labelEnd + 1);

                    viewLabel = line.substring((labelStart + 1), labelEnd);
                }

                viewList.add(viewLabel.trim(), viewLine.trim());
            }
        } catch (IOException e) {
            Debug.error("NetMapReader: Input error: " + e);

            viewList.removeAllElements();
            viewList = null;
        }

        try {
            sock.close();
            netmapConn.connectionDown();
        } catch (IOException e) {
        }

        return viewList;
    }

    /**
     * A queue command that lets the NetMapReader know to disconnect
     * when it has the opportunity to.
     */
    public void shutdown() {
        this.shutdown = true;
    }

    /**
     * Called when NetMapReader.start() is called. This makes the
     * NetMapReader call the NetMap server to get information about
     * the view that is set. This assumes that the view has been set.
     * The NetMapConnector will be called back with Properties objects
     * describing events received.
     */
    public void run() {
        BufferedReader tdin = null;
        PrintWriter tdout = null;

        if (viewName == null) {
            Debug.error("NetMapReader not given a view name to request from the NETMAP server.");
            return;
        }

        while (!this.shutdown) {
            if (DEBUG)
                Debug.output("NetMapReader attempting connection");

            try {
                tdin = new BufferedReader(new InputStreamReader(s.getInputStream()));
                tdout = new PrintWriter(s.getOutputStream(), true);

                if (DEBUG)
                    Debug.output("Loading view: " + viewName);
                tdout.println("jmap '" + viewName + "' 768");

                s.setSoTimeout(500);
            } catch (InterruptedIOException eConnectInterrupted) {
                continue;
            } catch (IOException eConnect) {
                Debug.error("NetMapReader: " + eConnect.getMessage()
                        + "; NetMapReader sleeping");

                try {
                    Thread.sleep(40000);
                } catch (Exception eSleep) {
                }

                continue;
            }

            if (netmapConn == null) {
                continue;
            }

            netmapConn.connectionUp();

            while (!this.shutdown) {
                try {
                    String line = null;

                    if ((line = tdin.readLine()) == null)
                        break;

                    if (DEBUG_VERBOSE)
                        Debug.output("  read: " + line);
                    Properties eventProps = procline(line);
                    if (DEBUG_VERBOSE)
                        Debug.output("  processed...");
                    if (!eventProps.isEmpty()) {
                        netmapConn.distributeEvent(eventProps);
                        if (DEBUG_VERBOSE)
                            Debug.output("  distributed...");
                    } else {
                        if (DEBUG_VERBOSE)
                            Debug.output("  ignored...");
                    }
                } catch (InterruptedIOException eReadInterrupted) {
                    continue;
                } catch (Exception e) {
                    Debug.error("NetMapReader exception: " + e.getMessage()
                            + "; in NetMapReader run. ");

                    // Which is better?
                    //                  break;
                    continue;
                }

            }

            try {
                s.close();
                netmapConn.connectionDown();
            } catch (Exception eShutdown) {
            }
        }
    }

    /**
     * Used to represent Double values read off the stream, converted
     * from the tokenized vector.
     */
    int[] nargs = new int[60];

    /** Process a line from NetMap input stream. */
    protected Properties procline(String cmdline) {

        Vector v = tokenize(cmdline);

        // Right now, nargs[] gets set in tokenize() with values
        // gleamed off Doubles in v.

        if (DEBUG_VERBOSE)
            Debug.output("  parsed: " + v.toString());

        Properties eventProps = new Properties();

        String cmd = v.firstElement().toString();
        int shape = nargs[4];

        if (cmd.equals(NODE_OBJECT)) {
            eventProps.put(COMMAND_FIELD, NODE_OBJECT);
            eventProps.put(INDEX_FIELD, Integer.toString(nargs[1]));
            eventProps.put(SHAPE_FIELD, Integer.toString(nargs[4]));

            if (v.elementAt(4) instanceof String) {
                String icon = (String) v.elementAt(4);

                eventProps.put(SHAPE_FIELD, "11");
                eventProps.put(ICON_FIELD, icon);

                if (DEBUG)
                    Debug.output("NetMapReader: jimage  " + icon);
            }

            if (shape == NODE_DELETE) { // Delete

                eventProps.put(SHAPE_FIELD, NODE_DELETE_STRING);

            } else {

                if (shape == NODE_MOVE) { // move

                    // nobj 13 342 432 0 lat=42.3876343
                    // lon=-71.1457977 elev=0.00 1038000904
                    // cmd, index, posx, posy, node_move, lat, lon,
                    // elevation, time

                    eventProps.put(SHAPE_FIELD, NODE_MOVE_STRING);
                    eventProps.put(TIME_FIELD, Integer.toString(nargs[8]));

                } else {

                    // nobj 1 13 12 50 10 10 1 5 '11' 0 'NODE_11_'
                    // lat=40.0295830 lon=-74.3184204 ip=10.0.0.11
                    // cmd, index, posx, posy, icon, width, height,
                    // status (color), menu, label, joffset, name,
                    // lat, lon

                    // Define a new entry if "shape" is anything
                    // else...
                    eventProps.put(WIDTH_FIELD, Integer.toString(nargs[5]));
                    eventProps.put(HEIGHT_FIELD, Integer.toString(nargs[6]));
                    eventProps.put(STATUS_FIELD, Integer.toString(nargs[7]));
                    eventProps.put(MENU_FIELD, Integer.toString(nargs[8]));
                    eventProps.put(LABEL_FIELD, (String) v.elementAt(9));
                    eventProps.put(NAME_FIELD, (String) v.elementAt(11));
                    eventProps.put(JOFFSET_FIELD, Integer.toString(nargs[10]));
                }

                eventProps.put(POSX_FIELD, Integer.toString(nargs[2]));
                eventProps.put(POSY_FIELD, Integer.toString(nargs[3]));

                String elev = null;
                if ((elev = getVal("elev=", cmdline)) != null) {
                    eventProps.put(ELEVATION_FIELD, elev);
                }

                String geo = null;

                if ((geo = getVal("lat=", cmdline)) != null) {
                    eventProps.put(LAT_FIELD, geo);
                }

                if ((geo = getVal("lon=", cmdline)) != null) {
                    eventProps.put(LON_FIELD, geo);
                }

                if ((geo = getVal("ip=", cmdline)) != null) {
                    eventProps.put(IP_FIELD, geo);
                }

            }

        } else if (cmd.equals(NODE_OBJECT_STATUS)) {

            eventProps.put(COMMAND_FIELD, NODE_OBJECT_STATUS);
            eventProps.put(INDEX_FIELD, Integer.toString(nargs[1]));
            eventProps.put(STATUS_FIELD, Integer.toString(nargs[2]));

        } else if (cmd.equals(LINK_OBJECT_STATUS)) {

            eventProps.put(COMMAND_FIELD, LINK_OBJECT_STATUS);
            eventProps.put(INDEX_FIELD, Integer.toString(nargs[1]));
            eventProps.put(STATUS_FIELD, Integer.toString(nargs[2]));

        } else if (cmd.equals(LINK_OBJECT)) {

            eventProps.put(COMMAND_FIELD, LINK_OBJECT);
            eventProps.put(INDEX_FIELD, Integer.toString(nargs[1]));
            eventProps.put(SHAPE_FIELD, Integer.toString(nargs[2]));

            if (shape != -1) {
                eventProps.put(STATUS_FIELD, Integer.toString(nargs[5]));
                eventProps.put(LINK_NODE1_FIELD, Integer.toString(nargs[3]));
                eventProps.put(LINK_NODE2_FIELD, Integer.toString(nargs[4]));
            }
        } else if (cmd.equals(REFRESH)) {
            eventProps.put(COMMAND_FIELD, REFRESH);
        } else if (cmd.equals(UPDATE)) {
            eventProps.put(COMMAND_FIELD, UPDATE);
        }

        return eventProps;
    }

    /**
     * Given a line, break it up into a Vector representing the String
     * parts, and the int[] containing the number parts. The Vector
     * will contain String representations of the numbers. Should be
     * called before procline() is called, and in fact is called from
     * within procline().
     */
    protected Vector tokenize(String line) {
        Object ob;

        Vector v = new Vector(12, 10);

        unitInit(new StringReader(line));

        int cnt = 0;
        while ((ob = unit()) != EOF) {
            v.addElement(ob);

            if (ob instanceof Double)
                nargs[cnt] = ((Number) ob).intValue();
            else
                nargs[cnt] = 0;

            cnt++;
        }
        return v;
    }

    /**
     * Initialize the StringTokenizer.
     */
    protected void unitInit(StringReader rdr) {
        st = new StreamTokenizer(rdr);

        st.commentChar('%');
        st.slashSlashComments(true);
        st.slashStarComments(true);

        st.wordChars('/', '/'); // disable default special handling
        st.wordChars('=', '='); // disable default special handling
        st.wordChars(':', ':'); // disable default special handling
    }

    /**
     * Break the next token into an Object, with some addition
     * semantic functionality to interpret EOF and parenthesis.
     */
    protected Object unit() {
        Object p = next();
        if (p == EOF)
            return EOF;

        if (p == LP) {
            Object r;
            Vector l = new Vector(2, 4);

            while (true) {
                r = unit();

                if (r == RP)
                    return l;

                if (r == EOF)
                    return EOF;

                l.addElement(r);
            }
        }

        return p;
    }

    /**
     * Break the next token into an Object.
     */
    protected Object next() {
        int i = 0;
        char[] c;

        try {
            i = st.nextToken();
        } catch (IOException e) {
            Debug.error("NetMapReader: " + e.toString() + " in toktest\n");
        }

        if ((i == StreamTokenizer.TT_EOF) || (i == 0))
            return EOF;

        if (i == StreamTokenizer.TT_WORD)
            return new Symbol(st.sval, 1);

        if ((i == '\'') || (i == '\"'))
            return st.sval;

        if (i == StreamTokenizer.TT_NUMBER)
            return new Double(st.nval);

        if ((i == '(') || (i == '[') || (i == '{'))
            return LP;

        if ((i == ')') || (i == ']') || (i == '}'))
            return RP;

        c = new char[1];
        c[0] = (char) i;

        return new Symbol(new String(c), 2);
    }

    protected String getVal(String marker, String line) {
        int sTok = 0;
        int eTok = 0;

        if ((sTok = line.toLowerCase().indexOf(marker)) < 0)
            return null;

        if (((eTok = line.indexOf(" ", sTok)) < 0)
                && ((eTok = line.indexOf("\t", sTok)) < 0)) {
            eTok = line.length();
        }

        return (line.substring(sTok + marker.length(), eTok));
    }

    // All this stuff below may be used later for more JMAP
    // integration...

    //     jicon jget(String name) {
    //      jicon x = (jicon)jicons.get(name);

    //      if (x == null) {
    //          x = new jicon(name);
    //          jicons.put(name, x);

    //          x.icon = can.loadImage("images/"+name);

    //          if (jmap.on && jmap.dbgmode)
    //              System.err.println("new jicon " + name + " " + x.icon);
    //      }

    //      return x;
    //     }

    /**
     * Got a response from our cexec request
     */
    //     private void cexec(String a) {
    //      f.status("exec " + a); // show message in status line
    //      try {
    //          Runtime.getRuntime().exec(a);
    //      } catch(IOException err) {
    //          if (jmap.on)
    //              System.err.println(err.toString());
    //      }
    //     }
    /**
     * Got a response from our cshow request
     */
    //     public void cshow(String url) {
    //      // show message in status line
    //      f.status("show " + url);
    //      try {
    //          AppletContext apcon = jmap.getAppletContext();

    //          if (url.substring(0, 2).equals("r ")) { // relative
    //              apcon.showDocument(new URL(jmap.getDocumentBase(),
    //                                         url.substring(2)));
    //          }
    //          else
    //              apcon.showDocument(new URL(url));
    //      } catch (java.net.MalformedURLException err) {
    //          if (jmap.on)
    //              System.err.println(err.toString());
    //      }
    //     }
    /**
     * We got a popup menu from NetMap that we had requested a bit
     * ago. jmenu 2 SNMP (('show name' MCMD 'echo $NAME') ('ping' MCMD
     * 'pingf $NAME'))
     */
    //     private void jmenu(String line) {
    //      PopupMenu tmenuh;
    //      int type = nargs[ 1 ];
    //      if (jmap.on && jmap.dbgmode)
    //          System.err.println("Got jmenu type " + type);
    //      Vector l;
    //      Vector r = new Vector(4, 4);

    //      if (f.pmenu[ type ] == null) { // if not already saved
    //          // create menu; store it in our global array
    //          l = (Vector)v.elementAt(3);
    //          tmenuh = new PopupMenu(v.elementAt(2).toString());
    //          popupButtons(tmenuh, l, r);
    //          f.pmenu[ type ] = tmenuh;
    //          f.pmenuV[ type ] = r;
    //          can.add(tmenuh);
    //      }
    //      if (f.tmenutypeDesired == type) { // if still desired
    //          // install menu in ourjmap window
    //          f.tmenu = f.pmenu[ type ];
    //          f.tmenutype = type;
    //          f.tmenutypeDesired = 0;
    //          f.tmenu.show(can, f.tmenux, f.tmenuy);
    //      }
    //     }
    //     private void jpulldowns(String line) {
    //      if (pulldowns)
    //          return; // already have pulldowns; skip this
    //      pulldowns = true;
    //      Menu tmenuh;
    //      int type = nargs[1 ];
    //      Vector l;
    //      Vector r= new Vector(4, 4);
    //      Vector g = (Vector)v.elementAt(1);
    //      Enumeration e = g.elements();
    //      while (e.hasMoreElements()) {
    //          tmenuh = new Menu(e.nextElement().toString());
    //          l = (Vector)e.nextElement();
    //          popupButtons(tmenuh, l, r);
    //          f.mb.add(tmenuh);
    //      }
    //      f.pulldownV = r; // save action data for menu operations
    //     }
    /**
     * Recursively create menu from list specification Vector r is
     * result lookup list to correlate MenuItem with details
     */
    //     private void popupButtons(Menu menu, Vector l, Vector r) {
    //      Object ob, ob2;
    //      int i;
    //      Vector m;
    //      Menu submenu;
    //      MenuItem mi;
    //      for (i = 0; i < l.size(); i++) {
    //          m = (Vector)l.elementAt(i);
    //          ob = m.elementAt(0);
    //          ob2 = m.elementAt(1);
    //          if (ob2 instanceof Symbol) {
    //              mi = new MenuItem((String)ob);
    //              m.setElementAt(mi, 0);
    //              // replace first element with MenuItem
    //              menu.add(mi);
    //              // save for use by jmapFrame event handler
    //              r.addElement(m);
    //          }
    //          else if (ob2 instanceof Vector) {
    //              submenu = new Menu((String)ob);
    //              menu.add(submenu);
    //              m.removeElementAt(0);
    //              popupButtons(submenu, m, r);
    //          }
    //          else if (jmap.on && jmap.dbgmode)
    //              System.err.println("Invalid menu item " + m);
    //      }
    //      menu.addActionListener(f);
    //     }
    //     // got a response from our mcmd request
    //     private void mcmd(String a) {
    //      // f.status(a); // show message in status line
    //      if ((v.size() > 4) &&
    //          (v.elementAt(1).toString().equals("host"))) {
    //          String b = v.elementAt(5).toString();
    //          /*
    //            if (b.equals("Up"))
    //            new jmapSoundPlayer(f.jmap, "up.au");
    //            else if (b.equals("not"))
    //            new jmapSoundPlayer(f.jmap, "noanswer.au");
    //          */
    //      }
    //     }
}