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
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/util/cacheHandler/CacheObject.java,v
// $
// $RCSfile: CacheObject.java,v $
// $Revision: 1.2 $
// $Date: 2006/12/15 18:39:52 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.cacheHandler;

public class CacheObject {

    public Object obj = null;
    public int cachedTime = 0;
    public Object id = null;

    /**
     * New object, set the local clock to zero
     */
    public CacheObject(Object identifier, Object cachedObject) {
        id = identifier;
        obj = cachedObject;
    }

    public boolean match(Object queryID) {
        return (queryID.equals(id));
    }

    public boolean older(int time) {
        return (cachedTime < time);
    }
}

