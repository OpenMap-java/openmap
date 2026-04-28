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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/shape/ShapeServerStarter.java,v $
// $RCSfile: ShapeServerStarter.java,v $
// $Revision: 1.4 $
// $Date: 2008/07/20 05:46:31 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.net.Socket;
import java.util.Properties;

import com.bbn.openmap.Environment;
import com.bbn.openmap.layer.link.LinkServerStarter;
import com.bbn.openmap.util.Debug;

/**
 * This class starts ShapeLinkServers on a per-client basis. A usage
 * statement is printed out if you run this class without arguments.
 */
public class ShapeServerStarter extends LinkServerStarter {

    protected String shapeFile;

    public ShapeServerStarter(int port, String shapeFile, String shapeIndex) {
        this(port, shapeFile);
    }
    
    public ShapeServerStarter(int port, String shapeFile) {
        super(port);
        this.shapeFile = shapeFile;
    }

    public Thread startNewServer(Socket sock) {
        return (new ShapeLinkServer(sock, shapeFile));
    }

    public static void main(String[] argv) {
        Properties p = System.getProperties();
        // First initialize debugging
        Debug.init(p);
        Environment.init(p);

        int pnumber = -1;
        String shp = null;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-port") && argv.length > i + 1) {
                try {
                    pnumber = Integer.parseInt(argv[i + 1]);
                    break;
                } catch (NumberFormatException e) {
                    pnumber = -1;
                }
            } else if (argv[i].indexOf(".shp") != -1
                    || argv[i].indexOf(".SHP") != -1) {
                shp = argv[i];
            }
        }

        if (pnumber < 0 || shp == null) {
            System.out.println("Need to start the server with a port number and shape file.");
            System.err.println("Usage: java com.bbn.openmap.layer.link.shape.ShapeServerStarter <ShapeFile Name.shp> -port <port number>");
            System.exit(-1);
        }

        System.out.println("ShapeServerStarter: Starting up on port " + pnumber
                + ".");
        ShapeServerStarter serverStarter = new ShapeServerStarter(pnumber, shp);

        while (true) {
            serverStarter.run();
        }
    }
}