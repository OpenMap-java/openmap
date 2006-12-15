package org.geotiff.epsg;

/**
 * Represents the Geographic (Datum) coordinate system.
 * 
 * @author: Niles D. Ritter
 */

public class GeographicCS extends HorizontalCS {

    /**
     * Protected Constructor; use the factory method in HorizontalCS to make
     * this.
     */
    protected GeographicCS(int code) {
        super(code);
    }

    /**
     * Standard accessor.
     */
    public HorizontalCS getGeographicCS() {
        return this;
    }

}
