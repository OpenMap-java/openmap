// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/Tokenizer.java,v $
// $RCSfile: Tokenizer.java,v $
// $Revision: 1.4 $
// $Date: 2008/02/27 01:05:52 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

/**
 * Tokenizer provides some tools useful for defining tokenizers. You
 * get 1 character pushback.
 */
public class Tokenizer extends java.io.PushbackReader {
    StringBuffer b;
    int lineCount = 0;

    public Tokenizer(java.io.Reader in) {
        super(in, 2);
        this.b = new StringBuffer(80);
    }

    // KRA 25Oct98: class Match requires access to NEWLINE and EOF,
    // YOW!
    public final static Object NEWLINE = new Object() {
        public String toString() {
            return "<newline>";
        }
    };

    public final static Object EOF = new Object() {
        public String toString() {
            return "<EOF>";
        }
    };
    
    public final static Object EMPTY = new Object() {
        public String toString() {
            return "";
        }
    };

    public boolean isNewline(Object o) {
        return o == NEWLINE;
    }

    public boolean isEOF(Object o) {
        return o == EOF;
    }

    public boolean isAny(int c) {
        return c != -1;
    }

    public boolean isAlpha(int c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    public boolean isDigit(int c) {
        return c >= '0' && c <= '9';
    }

    public boolean isAlphanumeric(int c) {
        return isAlpha(c) || isDigit(c);
    }

    public void bpush(int c) {
        this.b.append((char) c);
    } // Yow!

    public String bclear() {
        // YOW! Carefully copy string so it won't have 80 charaters
        // under it.
        String result = this.b.toString();
        this.b.setLength(0);
        int L = result.length();
        char[] chars = new char[L];
        result.getChars(0, L, chars, 0);
        return new String(chars);
    }

    /**
     * Read the next character. Convert alternative line breaks to
     * '\n'. Thank you Bill Gates!
     */
    public int next() {
        int c;
        try {
            c = this.read();
            if (c == '\r') {
                int c1 = this.read();
                if (c1 == '\n') {
                    c = '\n';
                } else {
                    this.unread(c1);
                    c = '\n';
                }
            }
            if (c == '\n')
                this.lineCount++;
            //_ System.out.print((char) c + "_");
            return c;
        } catch (java.io.IOException e) {
            throw new HandleError(e);
        }
    }

    public void putback(int c) {
        //      System.out.println("putback: '" + (char) c + "'");
        try {
            if (c != -1)
                this.unread(c);
        } catch (java.io.IOException e) {
            throw new HandleError(e);
        }
    }

    public Object error(String s) {
        throw new HandleError("at line " + this.lineCount + ": " + s);
    }

}