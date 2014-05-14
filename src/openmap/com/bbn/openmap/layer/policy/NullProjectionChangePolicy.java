/*
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.policy;

import com.bbn.openmap.event.ProjectionEvent;

/**
 * Dummy ProjectionChangePolicy, for those layers who prefer to ignore
 * projection changes completely.
 */
public class NullProjectionChangePolicy extends AbstractProjectionChangePolicy {

    public void projectionChanged(ProjectionEvent pe) {
    }
}
