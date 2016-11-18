/**
 * 
 */
package com.bbn.openmap.geo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface MatchCollector<T> {
	/**
	 * collect an indication that the query object a (or some part of it)
	 * matches object b in some way, presumably by intersection.
	 * 
	 * @param a
	 * @param b
	 */
	void collect(T a, T b);

	/** @return an iterator over the previously collected elements * */
	Iterator<T> iterator();

	/**
	 * A MatchCollector that collects a list of pairs of the matching objects
	 */
	public static class PairArrayMatchCollector<T> implements MatchCollector<T> {
		protected final List<Pair<T>> result = new ArrayList<Pair<T>>();

		public void collect(T a, T b) {
			result.add(new MatchCollector.Pair<T>(a, b));
		}

		/**
		 * This returns an iterator over a List of Pair<T>
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Iterator iterator() {
			return result.iterator();
		}
	}

	public static class Pair<T> {
		private T a;
		private T b;

		public Pair(T a, T b) {
			this.a = a;
			this.b = b;
		}

		public T getA() {
			return a;
		}

		public T getB() {
			return b;
		}
	}

	public static class SetMatchCollector<T> implements MatchCollector<T> {
		protected final Set<T> result = new HashSet<T>();

		public void collect(T a, T b) {
			result.add(b);
		}

		public Iterator<T> iterator() {
			return result.iterator();
		}
	}

	public static class CollectionMatchCollector<T> implements MatchCollector<T> {
		protected final Collection<T> c;

		public CollectionMatchCollector(Collection<T> c) {
			this.c = c;
		}

		public void collect(T a, T b) {
			c.add(b);
		}

		public Iterator<T> iterator() {
			return c.iterator();
		}
	}

}