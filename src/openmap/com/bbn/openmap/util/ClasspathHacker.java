/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

/**************************************************************************************************
 * Copyright (c) 2004, Federal University of So Carlos                                           *
 *                                                                                                *
 * All rights reserved.                                                                           *
 *                                                                                                *
 * Redistribution and use in source and binary forms, with or without modification, are permitted *
 * provided that the following conditions are met:                                                *
 *                                                                                                *
 *     * Redistributions of source code must retain the above copyright notice, this list of      *
 *       conditions and the following disclaimer.                                                 *
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of   *
 *     * conditions and the following disclaimer in the documentation and/or other materials      *
 *     * provided with the distribution.                                                          *
 *     * Neither the name of the Federal University of So Carlos nor the names of its            *
 *     * contributors may be used to endorse or promote products derived from this software       *
 *     * without specific prior written permission.                                               *
 *                                                                                                *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS                            *
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT                              *
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR                          *
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR                  *
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,                          *
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,                            *
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR                             *
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF                         *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING                           *
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS                             *
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                                   *
 **************************************************************************************************/

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

/**
 * Useful class for dynamically changing the classpath, adding classes during
 * runtime.
 * 
 * @author unknown
 */
public class ClasspathHacker {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.util.ClasspathHacker");

    /**
     * Parameters of the method to add an URL to the System classes.
     */
    private static final Class<?>[] parameters = new Class[] {
        URL.class
    };

    /**
     * Adds a file to the classpath.
     * 
     * @param s a String pointing to the file
     * @throws IOException
     */
    public static void addFile(String s)
            throws IOException {
        File f = new File(s);
        addFile(f);
    }// end method

    /**
     * Adds a file to the classpath
     * 
     * @param f the file to be added
     * @throws IOException
     */
    public static void addFile(File f)
            throws IOException {
        addURL(f.toURI().toURL());
    }// end method

    /**
     * Adds the content pointed by the URL to the classpath.
     * 
     * @param u the URL pointing to the content to be added
     * @throws IOException
     */
    public static void addURL(URL u)
            throws IOException {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();

        Class<?> sysclass = URLClassLoader.class;
        try {
            boolean loadIt = true;
            URL[] currentPaths = sysloader.getURLs();

            for (URL cPath : currentPaths) {
                if (cPath.getFile().equals(u.getFile())) {
                    loadIt = false;
                }
            }

            if (loadIt) {
                Method method = sysclass.getDeclaredMethod("addURL", parameters);
                method.setAccessible(true);
                method.invoke(sysloader, new Object[] {
                    u
                });
                logger.fine("loaded jar file");
            } else {
                logger.fine("jar file already loaded, skipping");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }// end try catch
    }// end method
}
