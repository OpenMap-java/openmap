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

public class EllipticalArcElement extends Command {
    int X, Y, X1, Y1, X2, Y2, SX1, SY1, SX2, SY2;

    public EllipticalArcElement(int ec, int eid, int l, DataInputStream in)
            throws IOException {
        super(ec, eid, l, in);
        X = makeInt(0);
        Y = makeInt(1);
        X1 = makeInt(2);
        Y1 = makeInt(3);
        X2 = makeInt(4);
        Y2 = makeInt(5);
        SX1 = makeInt(6);
        SY1 = makeInt(7);
        SX2 = makeInt(8);
        SY2 = makeInt(9);
    }

    public String toString() {
        return "Ellipse [" + X + "," + Y + "] [" + X1 + "," + Y1 + "] [" + X2
                + "," + Y2 + "] [" + SX1 + "," + SY1 + "] [" + SX2 + "," + SY2
                + "]";
    }
}