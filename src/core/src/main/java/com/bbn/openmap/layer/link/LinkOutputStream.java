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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/LinkOutputStream.java,v $
// $RCSfile: LinkOutputStream.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:56 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Extend DataOutputStream so we can reset the written byte count.
 * 
 * @see java.io.DataOutputStream
 */
public class LinkOutputStream extends DataOutputStream {
    /**
     * Creates a new link output stream to write data to the specified
     * underlying output stream.
     * 
     * @param out the underlying output stream, to be saved for later
     *        use.
     */
    public LinkOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Reset the written bytecount back to 0.
     * 
     * @return the previous value of <code>written</code>
     */
    public int clearWritten() {
        int temp = written;
        written = 0;
        return temp;
    }
}