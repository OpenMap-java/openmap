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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/FileUtils.java,v
// $
// $RCSfile: FileUtils.java,v $
// $Revision: 1.3 $
// $Date: 2005/02/11 22:42:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.bbn.openmap.Environment;

public class FileUtils {

    public static String getFilePathToSaveFromUser(String title) {
        JFileChooser chooser = getChooser(title);
        chooser.setDialogTitle(title);
        int state = chooser.showSaveDialog(null);
        String ret = handleResponse(chooser, state);
        return ret;
    }

    public static String getFilePathToOpenFromUser(String title) {
        return getFilePathToOpenFromUser(title, null);
    }

    public static String getFilePathToOpenFromUser(String title, FileFilter ff) {
        JFileChooser chooser = getChooser(title);
        chooser.setDialogTitle(title);
        if (ff != null) {
            chooser.setFileFilter(ff);
        }
        int state = chooser.showOpenDialog(null);
        String ret = handleResponse(chooser, state);
        return ret;
    }

    protected static JFileChooser getChooser(String title) {
        //setup the file chooser
        File startingPoint = new File(Environment.get("lastchosendirectory",
                System.getProperty("user.home")));
        return new JFileChooser(startingPoint);
    }

    protected static String handleResponse(JFileChooser chooser, int state) {
        String ret = null;
        try {
            //only bother trying to read the file if there is one
            //for some reason, the APPROVE_OPTION said it was a
            //boolean during compile and didn't work in this next
            //statement
            if ((state != JFileChooser.CANCEL_OPTION)
                    && (state != JFileChooser.ERROR_OPTION)) {

                ret = chooser.getSelectedFile().getCanonicalPath();

                int dirIndex = ret.lastIndexOf(File.separator);
                if (dirIndex >= 0) {
                    //store the selected file for later
                    Environment.set("lastchosendirectory", ret.substring(0,
                            dirIndex));
                }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null,
                    ioe.getMessage(),
                    "Error picking file",
                    JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }
        return ret;
    }
}