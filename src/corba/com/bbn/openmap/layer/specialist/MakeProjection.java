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
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/layer/specialist/MakeProjection.java,v $
// $RCSfile: MakeProjection.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:47 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.specialist;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.proj.*;
import java.io.*;
import java.util.*;

public class MakeProjection{
  Projection p;

  public MakeProjection(com.bbn.openmap.CSpecialist.CProjection mp) {
      LatLonPoint center = new LatLonPoint(mp.center.lat, mp.center.lon);

      switch (mp.kind) {
      case Mercator.MercatorType:
	  p = new Mercator(center, mp.scale, mp.width, mp.height);
	  break;
      case CADRG.CADRGType:
	  p = new CADRG(center, mp.scale, mp.width, mp.height);
	  break;
      case Orthographic.OrthographicType:
	  p = new Orthographic(center, mp.scale, mp.width, mp.height);
	  break;
//      case MassStatePlane.MassStatePlaneType:
//	  p = new MassStatePlane(center, mp.scale, mp.width, mp.height);
//	  break;
      default:
	  p = new Mercator(center, mp.scale, mp.width, mp.height);
      }
  }
    
  public Projection getProj() {
      return p;
  }
}
