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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/graphicLoader/scenario/TimeStamp.java,v $
// $RCSfile: TimeStamp.java,v $
// $Revision: 1.3 $
// $Date: 2004/10/14 18:05:47 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.omGraphics.time;

import java.awt.geom.Point2D;

import com.bbn.openmap.util.HashCodeUtil;

/**
 * A TemporalPoint is a representation of something occurring at a location and
 * time. The time is expected to be millisecond offsets from some starting time.
 */
public class TemporalPoint
      implements TemporalRecord {

   protected Point2D location;
   protected long time;

   /**
    * Create a TimeStamp to be used as a position by ScenarioPoints.
    */
   public TemporalPoint(Point2D location, long t) {
      this.location = location;
      time = t;
   }

   public String toString() {
      return "TimeStamp [" + location + ", time=" + time + " ]";
   }

   public void setTime(long t) {
      time = t;
   }

   public long getTime() {
      return time;
   }

   public void setLocation(Point2D loc) {
      location = loc;
   }

   public Point2D getLocation() {
      return location;
   }

   /**
    * Indicates whether some other object is "equal to" this Comparator. Assumes
    * that the other object is a TimeStamp object. Compares time fields.
    */
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final TemporalPoint other = (TemporalPoint) obj;
      return other.time == time;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = HashCodeUtil.SEED;
      if (location != null) {
         result = HashCodeUtil.hash(result, location);
      }
      result = HashCodeUtil.hash(result, time);
      return result;
   }
}