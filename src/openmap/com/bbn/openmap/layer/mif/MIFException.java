package com.bbn.openmap.layer.mif;

/**
 * Custom Exception class for exceptions within the loading of MIF files
 * @author Simon Bowen
 */
public class MIFException extends RuntimeException {

    public MIFException() {
        super();
    }

    /**
     * @param arg0
     */
    public MIFException(String arg0) {
        super(arg0);	
    }

    /**
     * @param arg0
     * @param arg1
     */
    public MIFException(String arg0, Throwable arg1) {
        super(arg0, arg1);	
    }

    /**
     * @param arg0
     */
    public MIFException(Throwable arg0) {
        super(arg0);	
    }
}
/** Last Line of file **/
