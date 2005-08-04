/*********************************************************************
 * Copyright (c) 1999, Frank Warmerdam
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ********************************************************************/

package com.bbn.openmap.dataAccess.iso8211;

import com.bbn.openmap.layer.vpf.MutableInt;

public class DDFUtils {
    /** ********************************************************************* */
    /* DDFScanVariable() */
    /*                                                                      */
    /* Establish the length of a variable length string in a */
    /* record. */
    /** ********************************************************************* */

    public static int scanVariable(byte[] pszRecord, int nMaxChars,
                                   char nDelimChar) {
        int i;
        for (i = 0; i < nMaxChars - 1 && pszRecord[i] != nDelimChar; i++) {
        }
        return i;
    }

    /** ********************************************************************* */
    /* DDFFetchVariable() */
    /*                                                                      */
    /* Fetch a variable length string from a record, and allocate */
    /* it as a new string (with CPLStrdup()). */
    /** ********************************************************************* */

    public static String fetchVariable(byte[] pszRecord, int nMaxChars,
                                       char nDelimChar1, char nDelimChar2,
                                       MutableInt pnConsumedChars) {
        int i;

        for (i = 0; i < nMaxChars - 1 && pszRecord[i] != nDelimChar1
                && pszRecord[i] != nDelimChar2; i++) {
        }

        pnConsumedChars.value = i;
        if (i < nMaxChars
                && (pszRecord[i] == nDelimChar1 || pszRecord[i] == nDelimChar2)) {
            pnConsumedChars.value++;
        }

        byte[] pszReturnBytes = new byte[i];
        System.arraycopy(pszRecord, 0, pszReturnBytes, 0, i);

        return new String(pszReturnBytes);
    }
}