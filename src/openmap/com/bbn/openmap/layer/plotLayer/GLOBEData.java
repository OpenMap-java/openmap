// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/plotLayer/GLOBEData.java,v $
// $RCSfile: GLOBEData.java,v $
// $Revision: 1.3 $
// $Date: 2004/01/26 18:18:10 $
// $Author: dietrick $
// 
// **********************************************************************



package com.bbn.openmap.layer.plotLayer;

import java.net.*;
import java.io.*;

public abstract class GLOBEData {
    
    public GLOBEData() {}

    public void loadData(InputStream instream) 
        throws IOException {
        readDataFromStream(instream);
    }

    protected abstract void parseDataFromStream(String line);


    public void readDataFromStream(InputStream istream)
        throws IOException {

        int lines_read = 0;
        BufferedReader buffstream = 
            new BufferedReader(new InputStreamReader(istream), 65536);

        while (true) { 
            String line = buffstream.readLine();
            if (line == null)
                break;
            line = line.trim();
            // ignore comments
            if (line.equals("") || line.startsWith("#")) {
                continue;
            }
            parseDataFromStream(line);
            lines_read++;
//          if (lines_read % 1000 == 0) {
//              System.out.println("Read " + lines_read + " lines");
//          }
        }
//      System.out.println("Read " + lines_read + " total lines");
    }


    /*
    public static void main (String argv[]) 
    {
        try {
            System.out.println("Getting URL: " + argv[0]);
            GLOBEData datafile = new GLOBEData(argv[0]);
            datafile.loadData();
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
    */

}
