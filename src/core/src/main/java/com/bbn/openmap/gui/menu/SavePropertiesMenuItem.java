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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SavePropertiesMenuItem.java,v $
// $RCSfile: SavePropertiesMenuItem.java,v $
// $Revision: 1.5 $
// $Date: 2006/02/27 15:11:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.bbn.openmap.PropertyHandler;
import com.bbn.openmap.util.Debug;

/**
 * A Menu item that generates a Properties object when it is clicked
 * upon. It needs BeanContext object. It iterates through objects in
 * BeanContext and looks for objects that implement PropertyConsumer
 * interface and generates Properties object from them.Wwhen it has
 * found all the Properties, it writes them to a file by providing the
 * user with a file dialog box.
 */
public class SavePropertiesMenuItem extends MapHandlerMenuItem implements
        ActionListener {

    public SavePropertiesMenuItem() {
        super("Save Map Properties");
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        //Collect properties
        if (mapHandler == null) {
            if (Debug.debugging("menu")) {
                Debug.error("SavePropertiesMenuItem: no Map Handler to use");
            }
            return;
        }

        FileDialog fd = new FileDialog(new Frame(), "Saving the map as Properties file...", FileDialog.SAVE);
        fd.setVisible(true);

        String fileName = fd.getFile();
        String dirName = fd.getDirectory();

        if (fileName == null) {
            Debug.message("savepropertiesmenuitem",
                    "User did not select any file");
            return;
        }

        Debug.message("savepropertiesmenuitem", "User selected file " + dirName
                + File.separator + fileName);

        File file = new File(new File(dirName), fileName);

        FileOutputStream fos;
        PrintStream ps = null;
        try {
            fos = new FileOutputStream(file);
        } //catch (FileNotFoundException fnfe) {
        /*
         * In JDK 1.2, the FileOutputStream(File) constructor throws a
         * IOException, but in JDK1.3 it throws FileNotFoundException.
         * So in 1.2 it is insufficient to catch
         * FileNotFoundException, you need to catch IOException. But
         * in 1.3, catching FileNotFoundException is enough.
         */
        catch (IOException fnfe) {
            System.err.println(fnfe.getMessage());
            return;
        }

        ps = new PrintStream(fos);

        PropertyHandler.createOpenMapProperties(mapHandler, ps);
        ps.close();
    }
}