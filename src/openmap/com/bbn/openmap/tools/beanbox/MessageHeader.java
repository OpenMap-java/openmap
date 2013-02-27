/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.io.InputStream;
import java.io.PrintWriter;

/**
 * An RFC 844 or MIME message header. Includes methods for parsing
 * headers from incoming streams, fetching values, setting values, and
 * printing headers. Key values of null are legal: they indicate lines
 * in the header that don't have a valid key, but do have a value
 * (this isn't legal according to the standard, but lines like this
 * are everywhere).
 */
public class MessageHeader {
    private String keys[];
    private String values[];
    private int nkeys;

    public MessageHeader() {
        grow();
    }

    public MessageHeader(InputStream is) throws java.io.IOException {
        parseHeader(is);
    }

    /**
     * Find the value that corresponds to this key. It finds only the
     * first occurrence of the key.
     * 
     * @param k the key to find.
     * @return null if not found.
     */
    public String findValue(String k) {
        if (k == null) {
            for (int i = nkeys; --i >= 0;)
                if (keys[i] == null)
                    return values[i];
        } else
            for (int i = nkeys; --i >= 0;) {
                if (k.equalsIgnoreCase(keys[i]))
                    return values[i];
            }
        return null;
    }

    public String getKey(int n) {
        if (n < 0 || n >= nkeys)
            return null;
        return keys[n];
    }

    public String getValue(int n) {
        if (n < 0 || n >= nkeys)
            return null;
        return values[n];
    }

    /**
     * Find the next value that corresponds to this key. It finds the
     * first value that follows v. To iterate over all the values of a
     * key use:
     * 
     * <pre>
     * 
     *           for(String v=h.findValue(k); v!=null; v=h.findNextValue(k, v)) {
     *               ...
     *           }
     *   
     * </pre>
     */
    public String findNextValue(String k, String v) {
        boolean foundV = false;
        if (k == null) {
            for (int i = nkeys; --i >= 0;)
                if (keys[i] == null)
                    if (foundV)
                        return values[i];
                    else if (values[i].equalsIgnoreCase(v))
                        foundV = true;
        } else
            for (int i = nkeys; --i >= 0;)
                if (k.equalsIgnoreCase(keys[i]))
                    if (foundV)
                        return values[i];
                    else if (values[i].equalsIgnoreCase(v))
                        foundV = true;
        return null;
    }

    /**
     * Prints the key-value pairs represented by this header. Also
     * prints the RFC required blank line at the end. Omits pairs with
     * a null key.
     */
    public void print(PrintWriter p) {
        for (int i = 0; i < nkeys; i++)
            if (keys[i] != null)
                p.println(keys[i] + (values[i] != null ? ": " + values[i] : "")
                        + "\r");
        p.println("\r");
        p.flush();
    }

    /**
     * Adds a key value pair to the end of the header. Duplicates are
     * allowed.
     */
    public void add(String k, String v) {
        grow();
        keys[nkeys] = k;
        values[nkeys] = v;
        nkeys++;
    }

    /**
     * Prepends a key value pair to the beginning of the header.
     * Duplicates are allowed.
     */
    public void prepend(String k, String v) {
        grow();
        for (int i = nkeys; i > 0; i--) {
            keys[i] = keys[i - 1];
            values[i] = values[i - 1];
        }
        keys[0] = k;
        values[0] = v;
        nkeys++;
    }

    /**
     * Overwrite the previous key/val pair at location 'i' with the
     * new k/v. If the index didn't exist before the key/val is simply
     * tacked onto the end.
     */
    public void set(int i, String k, String v) {
        grow();
        if (i < 0) {
            return;
        } else if (i > nkeys) {
            add(k, v);
        } else {
            keys[i] = k;
            values[i] = v;
        }
    }

    /**
     * Grow the key/value arrays as needed
     */
    private void grow() {
        if (keys == null || nkeys >= keys.length) {
            String[] nk = new String[nkeys + 4];
            String[] nv = new String[nkeys + 4];
            if (keys != null)
                System.arraycopy(keys, 0, nk, 0, nkeys);
            if (values != null)
                System.arraycopy(values, 0, nv, 0, nkeys);
            keys = nk;
            values = nv;
        }
    }

    /**
     * Sets the value of a key. If the key already exists in the
     * header, it's value will be changed. Otherwise a new key/value
     * pair will be added to the end of the header.
     */
    public void set(String k, String v) {
        for (int i = nkeys; --i >= 0;)
            if (k.equalsIgnoreCase(keys[i])) {
                values[i] = v;
                return;
            }
        add(k, v);
    }

    /**
     * Convert a message-id string to canonical form (strips off
     * leading and trailing <>s)
     */
    public static String canonicalID(String id) {
        if (id == null)
            return "";
        int st = 0;
        int len = id.length();
        boolean substr = false;
        int c;
        while (st < len && ((c = id.charAt(st)) == '<' || c <= ' ')) {
            st++;
            substr = true;
        }
        while (st < len && ((c = id.charAt(len - 1)) == '>' || c <= ' ')) {
            len--;
            substr = true;
        }
        return substr ? id.substring(st, len) : id;
    }

    /** Parse a MIME header from an input stream. */
    public void parseHeader(InputStream is) throws java.io.IOException {
        nkeys = 0;
        if (is == null)
            return;
        char s[] = new char[10];
        int firstc = is.read();
        while (firstc != '\n' && firstc != '\r' && firstc >= 0) {
            int len = 0;
            int keyend = -1;
            int c;
            boolean inKey = firstc > ' ';
            s[len++] = (char) firstc;
            parseloop: {
                parseloop2: while ((c = is.read()) >= 0) {
                    switch (c) {
                    case ':':
                        if (inKey && len > 0)
                            keyend = len;
                        inKey = false;
                        break;
                    case '\t':
                        c = ' ';
                    case ' ':
                        inKey = false;
                        break;
                    case '\r':
                    case '\n':
                        firstc = is.read();
                        if (c == '\r' && firstc == '\n') {
                            firstc = is.read();
                            if (firstc == '\r')
                                firstc = is.read();
                        }
                        if (firstc == '\n' || firstc == '\r' || firstc > ' ')
                            break parseloop;
                        /* continuation */
                        continue parseloop2;
                    }
                    if (len >= s.length) {
                        char ns[] = new char[s.length * 2];
                        System.arraycopy(s, 0, ns, 0, len);
                        s = ns;
                    }
                    s[len++] = (char) c;
                }
                firstc = -1;
            }
            while (len > 0 && s[len - 1] <= ' ')
                len--;
            String k;
            if (keyend <= 0) {
                k = null;
                keyend = 0;
            } else {
                k = String.copyValueOf(s, 0, keyend);
                if (keyend < len && s[keyend] == ':')
                    keyend++;
                while (keyend < len && s[keyend] <= ' ')
                    keyend++;
            }
            String v;
            if (keyend >= len)
                v = new String();
            else
                v = String.copyValueOf(s, keyend, len - keyend);
            add(k, v);
        }
    }

    public String toString() {
        StringBuffer result = new StringBuffer(super.toString());
        for (int i = 0; i < keys.length; i++) {
            result.append("{").append(keys[i]).append(": ").append(values[i]).append("}");
        }
        return result.toString();
    }
}