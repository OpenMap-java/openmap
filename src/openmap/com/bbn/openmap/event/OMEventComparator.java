/*
 */
package com.bbn.openmap.event;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class OMEventComparator implements Comparator<OMEvent> {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.event.OMEventComparator");

    /**
     * 
     */
    public OMEventComparator() {
        super();
    }

    public int compare(OMEvent obj1, OMEvent obj2) {
        long ts1 = Long.MAX_VALUE, ts2 = Long.MAX_VALUE;
        short tsc1 = 0, tsc2 = 0;
        int ret = 0;

        if (obj1 instanceof OMEvent) {
            ts1 = ((OMEvent) obj1).getTimeStamp();
            tsc1 = ((OMEvent) obj1).getTimeStampComparator();
        }

        if (obj2 instanceof OMEvent) {
            ts2 = ((OMEvent) obj2).getTimeStamp();
            tsc2 = ((OMEvent) obj2).getTimeStampComparator();
        }

        // ts1 is the one being tested/added to the TreeSet, so we
        // want later items with the same time being added after
        // previous items in the file with the same time.

        if (ts1 < ts2) {
            ret = -1;
            ((OMEvent) obj1).setSorted(true);
        } else if (ts1 > ts2) {
            ret = 1;
        } else {

            boolean logging = false;
            String id1 = null, id2 = null;

            if (logger.isLoggable(Level.FINE)) {
                id1 = ((OMEvent) obj1).getDescription();
                id2 = ((OMEvent) obj2).getDescription();

                logging = true;
            }

            if (tsc1 == 0) {
                tsc1 = tsc2;
                if (logging) {
                    logger.info("new event..." + id1);
                }
            } else {
                ((OMEvent) obj1).setSorted(true);
            }
            

            if (logging) {
                logger.info("comparing [" + id1 + "] at " + tsc1 + " to ["
                        + id2 + "] at " + tsc2);
            }

            // Since time stamps are equal, go to the time stamp comparator
            // settings.
            if (tsc1 < tsc2 && ((OMEvent) obj1).isSorted()) {
                ret = -1;
                if (logging) {
                    logger.info("-----");
                }
            } else if (tsc1 > tsc2) {
                ret = 1;
                if (logging) {
                    logger.info("+++++");
                }
            } else {
                // If they have the same setting, the second time stamp
                // comparator will get bumped up. If there has already been more
                // than one record with the same time, then this should keep
                // happening until the last one in has the highest tsc value;
                tsc1 = (short) (tsc2 + 1);
                ((OMEvent) obj2).setTimeStampComparator(tsc1);
                ret = 1;
                if (logging) {
                    logger.info("^^^^^ " + ts1 + ", upping [" + id1 + "] to "
                            + tsc1 + " over [" + id2 + "]");
                }
            }
        }

        return ret;
    }

    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}