/* **********************************************************************
 *
 * <rrl>
 * =====================================================================
 * Copyright 2002  by BBNT Solutions, LLC.
 * =====================================================================
 * </rrl>
 *
 *  $Id: DistanceEditorTool.java,v 1.1.1.1 2003/02/14 21:35:48 dietrick Exp $
 *
 * ***********************************************************************/

package com.bbn.openmap.layer.editor;

import com.bbn.openmap.tools.drawing.*;
import com.bbn.openmap.util.Debug;

/**
 * An Editor Tool for the EditorLayer that handles creating distance
 * measurements.
 * @author Ben Lubin
 * @version $Revision: 1.1.1.1 $ on $Date: 2003/02/14 21:35:48 $
 * @since 1/3/03
 **/
public class DistanceEditorTool extends AbstractDrawingEditorTool {

    public DistanceEditorTool(EditorLayer layer) {
	super(layer);

	EditToolLoader etl = new OMDistanceLoader();
	loaderList.add(etl);
	drawingTool.addLoader(etl);
    }
}
