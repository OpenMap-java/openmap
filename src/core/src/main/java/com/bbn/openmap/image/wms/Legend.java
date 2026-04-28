package com.bbn.openmap.image.wms;

import java.awt.Graphics;
import java.awt.geom.Dimension2D;
import java.util.Collection;

/**
 * An interface for providing legend graphics to wms clients
 */
public interface Legend {

    public Collection<? extends Dimension2D> getSizeHints();

    public void setSize(int width, int height);

    public void paint(Graphics g);

}
