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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/SoloMapComponent.java,v $
// $RCSfile: SoloMapComponent.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap;

/**
 * This interface is simply a way to mark a class as something that
 * should only exist once within a MapHandler.  When map components
 * are added to the MapHandler, the MapHandler will check to see if 1)
 * the component is a SoloMapComponent, and 2) if it is, depending on
 * the policy set in the MapHandler, that no other classes of the
 * same type are already added to the BeanContext.  
 */
public interface SoloMapComponent {}
