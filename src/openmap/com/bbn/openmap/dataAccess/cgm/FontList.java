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

public class FontList extends Command {
    String S[];

    public FontList(int ec, int eid, int l, DataInputStream in)
            throws IOException {
        super(ec, eid, l, in);
        int count = 0, i = 0;
        while (i < args.length) {
            count++;
            i += args[i] + 1;
        }
        S = new String[count];
        count = 0;
        i = 0;
        while (i < args.length) {
            char a[] = new char[args[i]];
            for (int j = 0; j < args[i]; j++)
                a[j] = (char) args[i + j + 1];
            S[count] = new String(a);
            count++;
            i += args[i] + 1;
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer("Font List: ");
        for (int i = 0; i < S.length - 1; i++)
            s.append(S[i]).append(", ");
        s.append(S[S.length - 1]);
        return s.toString();
    }
}