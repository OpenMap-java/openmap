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
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:59 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link.shape;

import java.util.Properties;
import java.net.Socket;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.Environment;

import com.bbn.openmap.layer.link.*;

/**
 * This class starts ShapeLinkServers on a per-client basis. A usage
 * statement is printed out if you run this class without arguments.
 */
public class ShapeServerStarter extends LinkServerStarter {

    protected String shapeFile;
    protected String shapeIndex;

    public ShapeServerStarter(int port, String shapeFile, String shapeIndex) {
        super(port);
        this.shapeFile = shapeFile;
        this.shapeIndex = shapeIndex;
    }

    public Thread startNewServer(Socket sock) {
        return (new ShapeLinkServer(sock, shapeFile, shapeIndex));
    }

    public static void main(String[] argv) {
        Properties p = System.getProperties();
        // First initialize debugging
        Debug.init(p);
        Environment.init(p);

        int pnumber = -1;
        String ssx = null;
        String shp = null;
        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-port") && argv.length > i + 1) {
                try {
                    pnumber = Integer.parseInt(argv[i + 1]);
                    break;
                } catch (NumberFormatException e) {
                    pnumber = -1;
                }
            } else if (argv[i].indexOf(".ssx") != -1
                    || argv[i].indexOf(".SSX") != -1) {
                ssx = argv[i];
            } else if (argv[i].indexOf(".shp") != -1
                    || argv[i].indexOf(".SHP") != -1) {
                shp = argv[i];
            }
        }

        if (pnumber < 0 || ssx == null || shp == null) {
            System.out.println("Need to start the server with a port number, shape file and spatial index file.");
            System.err.println("Usage: java com.bbn.openmap.layer.link.shape.ShapeServerStarter <ShapeFile Name.shp> <ShapeFile Spatial Index File.ssx> -port <port number>");
            System.exit(-1);
        }

        System.out.println("ShapeServerStarter: Starting up on port " + pnumber
                + ".");
        ShapeServerStarter serverStarter = new ShapeServerStarter(pnumber, argv[0], argv[1]);

        while (true) {
            serverStarter.run();
        }
    }
}