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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/FileUtils.java,v $
// $RCSfile: FileUtils.java,v $
// $Revision: 1.1 $
// $Date: 2004/09/17 19:07:27 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import com.bbn.openmap.Environment;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileUtils {

    public static String getFilePathFromUser(String title) {
        String ret = null;
        try {
            //setup the file chooser
            File startingPoint = new File(Environment.get("lastchosendirectory", System.getProperty("user.home")));
            JFileChooser chooser = new JFileChooser(startingPoint);

            chooser.setDialogTitle(title);

            int state = chooser.showSaveDialog(null);
            //only bother trying to read the file if there is one
            //for some reason, the APPROVE_OPTION said it was a
            //boolean during compile and didn't work in this next
            //statement
            if ((state != JFileChooser.CANCEL_OPTION) && 
                (state != JFileChooser.ERROR_OPTION)) {

                ret = chooser.getSelectedFile().getCanonicalPath();

                //store the selected file for later
                Environment.set("lastchosendirectory", ret);
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error picking file", JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }

        return ret;
    }

}