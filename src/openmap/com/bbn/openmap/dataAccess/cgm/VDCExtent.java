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

public class VDCExtent extends Command {
    int X1, X2, Y1, Y2;

    public VDCExtent(int ec, int eid, int l, DataInputStream in)
            throws IOException {
        super(ec, eid, l, in);
        X1 = makeInt(0);
        Y1 = makeInt(1);
        X2 = makeInt(2);
        Y2 = makeInt(3);
    }

    public String toString() {
        return "VDC Extent [" + X1 + "," + Y1 + "] [" + X2 + "," + Y2 + "]";
    }

    public int[] extent() {
        int x[] = new int[4];
        x[0] = X1;
        x[1] = Y1;
        x[2] = X2;
        x[3] = Y2;
        return x;
    }
}