package com.bbn.openmap;

import java.awt.Paint;
import java.beans.PropertyChangeListener;

/**
 * This interface describes objects that help the MapBean do special stuff with
 * the background paint, dynamically. It was originally created to help set
 * GradientPaint on the map, given a color. One of the side effects of having a
 * gradient paint or image pattern set on the MapBean as a background is
 * apparent when the map is panned - you don't really want that background
 * shifting with the rest of the map, it looks weird and is jarring when the map
 * updates to the new location, as the background goes back to were it
 * originally was. The proper effect is having the background just stay in one
 * place, and have the other layers float on top when the map is panned. Anyway,
 * MapBeanBackgroundPolicy objects are supposed to help with that.
 * <P>
 * 
 * If the MapBeanBackgroundPolicy is required to modify any Paint object set on
 * the MapBean as its Bckgrnd, the policy should add itself to the MapBean as a
 * PropertyChangeLister for background paint change updates, and projection
 * changes if they are needed.
 * 
 * @author dietrick
 *
 */
public interface MapBeanBackgroundPolicy extends PropertyChangeListener {

	/**
	 * Get the modified background paint to use for the MapBean during paint.
	 * 
	 * @return Paint
	 */
	Paint getBckgrnd();

	/**
	 * Indicates whether the background should be included in image when the map
	 * is panned.
	 * 
	 * @return
	 */
	boolean includeInPan();
}
