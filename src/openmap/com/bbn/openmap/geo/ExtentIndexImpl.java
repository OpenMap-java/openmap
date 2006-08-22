//**********************************************************************
//
//<copyright>
//
//BBN Technologies, a Verizon Company
//10 Moulton Street
//Cambridge, MA 02138
//(617) 873-8000
//
//Copyright (C) BBNT Solutions LLC. All rights reserved.
//
//</copyright>
//**********************************************************************
//
//$Source:
///cvs/darwars/ambush/aar/src/com/bbn/ambush/mission/MissionHandler.java,v
//$
//$RCSfile: ExtentIndexImpl.java,v $
//$Revision: 1.5 $
//$Date: 2006/08/22 16:42:54 $
//$Author: mthome $
//
//**********************************************************************

package com.bbn.openmap.geo;

/**
 * Separable indexed database for Regional BoundingCircles. This is
 * currently a simple longitude coverage map of the world, broken into
 * buckets covering 1 degrees. A given BoundingCircle will show up in
 * every bucket that it touches or comes within the margin.
 * <p>
 * This class is now a trivial extension of ExtentIndex.ArrayListExtentIndexImpl, which
 * should probably be used instead.
 */
public class ExtentIndexImpl extends ExtentIndex.ArrayListExtentIndexImpl {
  public ExtentIndexImpl() {
    this(D_NBUCKETS, D_MARGIN);
  }
  
  public ExtentIndexImpl(int nb) {
    this(nb, D_MARGIN);
  }

  public ExtentIndexImpl(double m) {
    this(D_NBUCKETS, m);
  }
  
  public ExtentIndexImpl(int nb, double m) {
    super(nb, m);
  }
}