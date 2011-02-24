/**
 * 
 */
package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public interface MatchCollector {
    /**
     * collect an indication that the query object a (or some part of it)
     * matches object b in some way, presumably by intersection.
     * 
     * @param a
     * @param b
     */
    void collect(Object a, Object b);

    /** @return an iterator over the previously collected elements * */
    Iterator iterator();

    /**
     * A MatchCollector that collects a list of pairs of the matching objects
     */
    public static class PairArrayMatchCollector
            implements MatchCollector {
        protected final ArrayList result = new ArrayList();

        public void collect(Object a, Object b) {
            result.add(new MatchCollector.Pair(a, b));
        }

        public Iterator iterator() {
            return result.iterator();
        }
    }

    public static class Pair {
        private Object a;
        private Object b;

        public Pair(Object a, Object b) {
            this.a = a;
            this.b = b;
        }

        public Object getA() {
            return a;
        }

        public Object getB() {
            return b;
        }
    }

    public static class SetMatchCollector
            implements MatchCollector {
        protected final HashSet result = new HashSet();

        public void collect(Object a, Object b) {
            result.add(b);
        }

        public Iterator iterator() {
            return result.iterator();
        }
    }

    public static class CollectionMatchCollector
            implements MatchCollector {
        protected final Collection c;

        public CollectionMatchCollector(Collection c) {
            this.c = c;
        }

        public void collect(Object a, Object b) {
            c.add(b);
        }

        public Iterator iterator() {
            return c.iterator();
        }
    }

}