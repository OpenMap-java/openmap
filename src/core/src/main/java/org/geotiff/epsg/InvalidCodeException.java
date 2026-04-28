package org.geotiff.epsg;

/**
 * Represents the Exception when an invalid coordinate system code is entered.
 * 
 * @author: Niles D. Ritter
 */

public class InvalidCodeException extends Exception {

    public InvalidCodeException() {}

    public InvalidCodeException(String msg) {
        super(msg);
    }
}
