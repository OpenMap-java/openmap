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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/time/TimeBounds.java,v $
// $RCSfile: TimeBounds.java,v $
// $Revision: 1.1 $
// $Date: 2007/09/25 17:30:35 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.time;

import java.io.Serializable;

import com.bbn.openmap.util.HashCodeUtil;

/**
 * A class for holding a range of times.
 */
public class TimeBounds
      implements Serializable {

   protected long startTime;
   protected long endTime;

   /**
    * Creates a TimeBounds object ready for times to be added to it. The
    * starting time is set to Long.MAX_VALUE, the end time is set to
    * Long.MIN_VALUE, so any time addition will cause start and end times to be
    * replaced.
    */
   public TimeBounds() {
      setTimes(Long.MAX_VALUE, Long.MIN_VALUE);
   }

   public TimeBounds(long start, long end) {
      setTimes(start, end);
   }

   public void setTimes(long start, long end) {
      startTime = start;
      endTime = end;
   }

   public long getStartTime() {
      return startTime;
   }

   public long getEndTime() {
      return endTime;
   }

   public void reset() {
      startTime = Long.MAX_VALUE;
      endTime = Long.MIN_VALUE;
   }

   /**
    * Add a time to the bounds, resetting the start and end time as necessary.
    * 
    * @param timeStamp in milliseconds
    */
   public void addTimeToBounds(long timeStamp) {

      if (timeStamp < startTime) {
         startTime = timeStamp;
      }

      if (timeStamp > endTime) {
         endTime = timeStamp;
      }
   }

   public String toString() {
      return "TimeBounds[start=" + startTime + ", end=" + endTime + "]";
   }

   /**
    * Add the start and end times of provided TimeBounds to this TimeBounds.
    * 
    * @param timeBounds
    */
   public void addTimeToBounds(TimeBounds timeBounds) {
      addTimeToBounds(timeBounds.getStartTime());
      addTimeToBounds(timeBounds.getEndTime());
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      final TimeBounds timeBounds = (TimeBounds) obj;
      return (startTime == timeBounds.getStartTime() && endTime == timeBounds.getEndTime());
   }

   public boolean isUnset() {
      return startTime == Long.MAX_VALUE && endTime == Long.MIN_VALUE;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      int result = HashCodeUtil.SEED;
      // collect the contributions of various fields
      result = HashCodeUtil.hash(result, startTime);
      result = HashCodeUtil.hash(result, endTime);
      return result;
   }
}