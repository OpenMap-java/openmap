/*
 * <copyright> Copyright 1997-2003 BBNT Solutions, LLC under
 * sponsorship of the Defense Advanced Research Projects Agency
 * (DARPA).
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Cougaar Open Source License as
 * published by DARPA on the Cougaar Open Source Website
 * (www.cougaar.org).
 * 
 * THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 * PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 * IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 * ANY WARRANTIES AS TO NON-INFRINGEMENT. IN NO EVENT SHALL COPYRIGHT
 * HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 * TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THE COUGAAR SOFTWARE. </copyright>
 */
package com.bbn.openmap.dataAccess.cgm;

import java.awt.Polygon;
import java.io.DataInputStream;
import java.io.IOException;

public class PolygonElement extends Command {
    int X[], Y[];
    Polygon P;

    public PolygonElement(int ec, int eid, int l, DataInputStream in)
            throws IOException {

        super(ec, eid, l, in);
        int n = args.length / 4;
        X = new int[n];
        Y = new int[n];
        for (int i = 0; i < n; i++) {
            X[i] = makeInt(2 * i);
            Y[i] = makeInt(2 * i + 1);
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer("Polygon");
        for (int i = 0; i < X.length; i++)
            s.append(" [").append(X[i]).append(",").append(Y[i]).append("]");
        return s.toString();
    }

    public void scale(CGMDisplay d) {
        P = new Polygon();
        for (int i = 0; i < X.length; i++) {
            P.addPoint(d.x(X[i]), d.y(Y[i]));
        }
    }

    public void paint(CGMDisplay d) {
        if (d.getFilled()) {
            d.graphics().setColor(d.getFillColor());
            d.graphics().fillPolygon(P);
        } else {
            d.graphics().setColor(d.getFillColor());
            if (!d.getEdge())
                d.graphics().drawPolygon(P);
        }

        if (d.getEdge()) {
            d.graphics().setColor(d.getEdgeColor());
            d.graphics().drawPolygon(P);
        }
    }
}