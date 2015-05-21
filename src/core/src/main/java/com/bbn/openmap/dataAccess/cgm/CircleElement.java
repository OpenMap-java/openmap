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

import java.io.DataInputStream;
import java.io.IOException;

public class CircleElement extends Command {
    int X1, Y1, R1;
    int X, Y, R;

    public CircleElement(int ec, int eid, int l, DataInputStream in)
            throws IOException {
        super(ec, eid, l, in);
        X1 = makeInt(0);
        Y1 = makeInt(1);
        R1 = makeInt(2);
    }

    public String toString() {
        return "Circle [" + X1 + "," + Y1 + "] " + R;
    }

    public void scale(CGMDisplay d) {
        X = d.x(X1);
        Y = d.y(Y1);
        R = (int) (d.factorX() * R1);
        X -= R;
        Y -= R;
        R = 2 * R - 1;
    }

    public void paint(CGMDisplay d) {
        if (d.getFilled()) {
            d.graphics().setColor(d.getFillColor());
            d.graphics().fillOval(X, Y, R, R);
        } else {
            d.graphics().setColor(d.getFillColor());
            if (!d.getEdge())
                d.graphics().drawOval(X, Y, R, R);
        }
        if (d.getEdge()) {
            d.graphics().setColor(d.getEdgeColor());
            d.graphics().drawOval(X, Y, R, R);
        }
    }
}