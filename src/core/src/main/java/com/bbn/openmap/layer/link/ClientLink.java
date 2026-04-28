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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/link/ClientLink.java,v $
// $RCSfile: ClientLink.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:55 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.link;

import java.io.IOException;
import java.net.Socket;

/**
 * The ClientLink provides the method to close the link down, since it
 * makes that decision. The server should remain connected until the
 * client is finished. The server can request to be disconnected,
 * however, and the ClientLink provides a method for the client to
 * check if that request has been made.
 */
public class ClientLink extends Link {

    /** Constructor. */
    public ClientLink(Socket socket) throws IOException {
        super(socket);
    }

    /** Close the socket of the link. */
    public void close() throws IOException {
        socket.close();
    }

    /**
     * A method used by the client after a communication exchange.
     * Since the client is the side of the link that can close the
     * link, it can find out if the server wants to close down the
     * link with this method.
     * 
     * @return true if the link should be shut down.
     */
    public boolean isCloseLink() {
        return closeLink;
    }
}