//**********************************************************************
//
//<copyright>
//
//BBN Technologies
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
//$RCSfile: ProjectionChangeVetoException.java,v $
//$Revision: 1.1 $
//$Date: 2006/09/12 17:46:47 $
//$Author: dietrick $
//
//**********************************************************************

package com.bbn.openmap.event;

import java.awt.geom.Point2D;
import java.util.Properties;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.proj.Proj;
import com.bbn.openmap.proj.Projection;

/**
 * An exception used by PropertyChangeListeners on the MapBean to veto a
 * projection change on the MapBean. Should be used by components that want to
 * limit the range of projection changes allowed in a application, or during
 * certain application conditions. These exceptions should be used by
 * PropertyChangeListeners on the MapBean listening for
 * MapBean.ProjectionProperty changes, because the MapBean notifies the
 * PropertyChangeListeners of projection changes before it notifies the
 * ProjectionListeners (the layers). If the MapBean catches one of these
 * exceptions from a PropertyChangeListener, the projection parameters will be
 * changed to the values provided, all of the PropertyChangeListeners will be
 * notified again, and the layers will be finally be notified when the
 * PropertyChangeListeners are happy. Only projection changes relating to scale,
 * center and type can be vetoed. The size of the projection (pixel width and
 * height) is dictated by the MapBean's parent.
 * 
 * @author dietrick
 */
public class ProjectionChangeVetoException extends RuntimeException {

    protected Properties parameters;

    public final static String CENTER = "center";
    public final static String SCALE = "scale";
    public final static String PROJECTION_TYPE = "projType";

    /**
     * Constructs a new projection change veto exception with <code>null</code>
     * as its detail message and no suggested changes. The cause is not
     * initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     */
    public ProjectionChangeVetoException() {
        super();
    }

    /**
     * Constructs a new projection change veto exception with the specified
     * detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     * 
     * @param message the detail message, with no suggested changes.
     */
    public ProjectionChangeVetoException(String message) {
        super(message);
    }

    /**
     * Constructs a new projection change veto exception with the specified
     * detail message and common parameter objects for OpenMap projections that
     * can be cause for veto. The Throwable cause object is not initialized, and
     * may subsequently be initialized by a call to {@link #initCause}. The
     * objects provided as arguments will be added to a Properties object set in
     * the exception.
     * 
     * @param message the detail message.
     * @param projectionType set to null if the new projection class type is OK,
     *        otherwise set the projection class that should be used.
     * @param center set to null of the new projection center is OK, otherwise
     *        set to the new center point of the projection.
     * @param scale set to null if the new projection scale is OK, otherwise
     *        provide a new scale value.
     */
    public ProjectionChangeVetoException(String message,
            Class<? extends Proj> projectionType, Point2D center, Number scale) {
        super(message);
        parameters = new Properties();

        if (projectionType != null) {
            parameters.put(PROJECTION_TYPE, projectionType);
        }
        if (center != null) {
            parameters.put(CENTER, center);
        }
        if (scale != null) {
            parameters.put(SCALE, scale);
        }
    }

    /**
     * Constructs a new projection change veto exception with the specified
     * detail message and a Properties object containing parameter objects
     * representing new settings for the rejected projection. The Throwable
     * cause object is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     * 
     * @param message the detail message.
     * @param suggestedChanges A Properties object holding CENTER, SCALE,
     *        PROJECTION_TYPE changes, or any other parameters that can be used
     *        by the event in the updateWithParameters method.
     */
    public ProjectionChangeVetoException(String message,
            Properties suggestedChanges) {
        parameters = suggestedChanges;
    }

    /**
     * @param parameter the key for the projection parameter.
     * @return the Object for the given key.
     */
    public Object getSuggested(String parameter) {
        return parameters.get(parameter);
    }

    /**
     * Remove a suggested parameter from the event.
     * 
     * @param parameter
     */
    public void removeSuggested(String parameter) {
        parameters.remove(parameter);
    }

    /**
     * A helper function for the MapBean. The Exception object can update the
     * projection for a MapBean, and then call MapBean.setProjection() with the
     * new settings. This method was intended to be called from the
     * MapBean.fireProjectionChange() method after this Exception has been
     * caught, and can be overridden for new/updated Projection types and for
     * different suggestion parameters that may be contained in the Exception
     * properties.
     * 
     * @param mapBean
     */
    public void updateWithParameters(MapBean mapBean) {
        Proj projection = (Proj) mapBean.getProjection();
        Object suggested = getSuggested(PROJECTION_TYPE);
        if (suggested instanceof Class && suggested != projection.getClass()) {

            projection = (Proj) mapBean.getProjectionFactory()
                    .makeProjection((Class<? extends Projection>) suggested,
                            projection.getCenter(),
                            projection.getScale(),
                            projection.getWidth(),
                            projection.getHeight());
        }

        suggested = getSuggested(CENTER);
        if (suggested instanceof Point2D) {
            projection.setCenter((Point2D) suggested);
        }

        suggested = getSuggested(SCALE);
        if (suggested instanceof Number) {
            projection.setScale(((Number) suggested).floatValue());
        }

        mapBean.setProjection(projection);
    }

    public String toString() {
        String message = getMessage();
        if (message == null) {
            message = "[no message]";
        }

        return "ProjectionChangeVetoException: " + message
                + ", suggested parameters: " + parameters;
    }

}
