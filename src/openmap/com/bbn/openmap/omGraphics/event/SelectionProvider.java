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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/omGraphics/event/SelectionProvider.java,v $
// $RCSfile: SelectionProvider.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:49 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.omGraphics.event;

/**
 * An interface to define an object that can generate SelectionEvents
 * for OMGraphics.  A SelectionListener would hook up to a
 * SelectionProvider to find out when OMGraphics are selected and
 * deselected.
 */
public interface SelectionProvider {

    public void addSelectionListener(SelectionListener listener);

    public void removeSelectionListener(SelectionListener listener);

    public void clearSelectionListeners();
}
