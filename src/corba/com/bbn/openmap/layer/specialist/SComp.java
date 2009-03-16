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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/SComp.java,v $
// $RCSfile: SComp.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:36 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.corba.CSpecialist.EComp;
import com.bbn.openmap.corba.CSpecialist._CompStub;

/**
 * A SComp class is used to provide additional functionality for a
 * graphics object. It can be used to send a URL to a client, or to
 * tie objects together.
 */
public class SComp extends _CompStub {

    /** where we fill ourselves */
    protected EComp self;

    /** Construct ourselves without an ID */
    public SComp() {
        this("");
    }

    /**
     * Construct ourselves with an ID
     * 
     * @param cID our ID
     */
    public SComp(String cID) {
        self = new EComp(this, cID);
    }

    public com.bbn.openmap.corba.CSpecialist.ActionUnion[] sendGesture(
                                                                 com.bbn.openmap.corba.CSpecialist.MouseEvent gesture,
                                                                 java.lang.String uniqueID) {
        return new com.bbn.openmap.corba.CSpecialist.ActionUnion[0];
    }

    /**
     * Change our ID - not a good idea
     * 
     * @param cID our new ID
     */
    public void cID(String cID) {
        self.cID = cID;
    }

    /**
     * get our ID
     * 
     * @return our ID
     */
    public String cID() {
        return self.cID;
    }

    /**
     * return a struct containing info about ourselves <b>modifying
     * this struct will modify the object that created it </b>
     */
    public EComp fill() {
        return self;
    }
}