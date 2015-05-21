/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.layer.postgis;

/**
 * An exception thrown when the where clauses detect failure conditions, such as
 * a problem with projection.
 * 
 * @author dietrick
 */
public class QueryException extends RuntimeException {

    public QueryException(String message) {
        super(message);
    }
}
