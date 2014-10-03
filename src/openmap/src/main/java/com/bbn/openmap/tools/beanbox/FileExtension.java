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

import java.io.File;
import java.io.FilenameFilter;

/**
 * File filter utility class. Used by the BeanPanel.
 */
public class FileExtension implements FilenameFilter {
    private String extension;

    /**
     * Constructor taking a file extension as argument.
     */
    public FileExtension(String ext) {
        this.extension = ext;
    }

    /**
     * Accept file name if it has the stored extension.
     */
    public boolean accept(File dir, String name) {
        return name.endsWith(extension);
    }
}