/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *        restricted rights as set forth in the DFARS.
 *  
 *          BBNT Solutions LLC
 *              A Part of 
 *                  Verizon      
 *           10 Moulton Street
 *          Cambridge, MA 02138
 *           (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.tools.beanbox;

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.Introspector;
import java.util.Hashtable;

/**
 * Utility class for representing a loaded Jar file.
 */
public class JarInfo {

    private String[] beanNames;
    private BeanInfo[] beanInfos;
    private Class[] beanClasses;
    private boolean[] fromPrototype;
    private MessageHeader[] manifestData;
    private String jarName;
    private static Hashtable beanToJar = new Hashtable();

    public JarInfo(String jarName, String[] beanNames, boolean[] fromPrototype,
            MessageHeader[] manifestData) {
        if (beanNames.length != fromPrototype.length) {
            throw new Error("beanNames and fromPrototype need to have the same length");
        }
        //System.out.println("Created JarInfo");
        this.jarName = jarName;
        this.beanNames = beanNames;
        this.fromPrototype = fromPrototype;
        this.manifestData = manifestData;
        this.beanInfos = new BeanInfo[beanNames.length];
        this.beanClasses = new Class[beanNames.length];
        for (int i = 0; i < beanNames.length; i++) {
            beanToJar.put(beanNames[i], jarName); // record where this
                                                  // beanName came
                                                  // from
            if (fromPrototype[i]) {
                // delay instantiating it
                continue;
            }
            // get the BeanInfo data
            Class c;
            //System.out.println("Loading bean: " + beanNames[i]);
            try {
                c = Class.forName(beanNames[i]);
                beanClasses[i] = c;
                //System.out.println(" succeeded.");
            } catch (Exception ex) {
                // We don't print an error at this point. Instead we
                // print
                // an error later, in JarInfo.getInstance.
                System.err.println("Could not load " + beanNames[i] + " from "
                        + jarName);
                continue;
            } catch (Error er) {
                System.out.println(er);
                //er.printStackTrace();
                System.err.println("Could not load " + beanNames[i] + " in "
                        + jarName);
                continue;
            }

            BeanInfo bi;
            try {
                //System.out.println("Getting beanInfo for: " + c);
                bi = (BeanInfo) BeanPanel.findBeanInfo(beanNames[i]);
            } catch (Exception ex) {
                System.err.println("JarInfo: couldn't find BeanInfo for " + c
                        + "; caught " + ex);
                continue;
            } catch (Error er) {
                System.out.println(er.getMessage());
                er.printStackTrace();
                System.err.println("Could not load beanInfo for "
                        + beanNames[i] + " in " + jarName);
                continue;
            }

            if (bi != null) {
                beanInfos[i] = bi;
                //System.out.println(" succeeded.");
            } //else
            //System.out.println(" failed.");
        }
        //System.out.println("Exit JarInfo");
    }

    /**
     * Gets the name of the jar file that the bean came from.
     */
    public static String getJarName(String beanName) {
        return (String) beanToJar.get(beanName);
    }

    /**
     * Get the name of the file containing this jar file
     */
    public String getJarName() {
        return jarName;
    }

    /**
     * Get the number of beans in this Jar file
     */
    public int getCount() {
        return beanNames.length;
    }

    /**
     * Get the bean class for the ith bean in this file
     */
    Class getBeanClass(int i) {
        return beanClasses[i];
    }

    /**
     * Get the BeanInfo for the ith bean in this jar file
     */
    BeanInfo getBeanInfo(int i) {
        if (beanInfos[i] != null) {
            return beanInfos[i];
        } else {
            Object b = getInstance(beanNames[i]);
            if (b != null) {
                Class c = b.getClass();
                BeanInfo bi;
                try {
                    bi = Introspector.getBeanInfo(c);
                } catch (Exception ex) {
                    System.err.println("JarInfo: couldn't find BeanInfo for "
                            + c + "; caught " + ex);
                    return null;
                }

                beanInfos[i] = bi;
                return bi;
            }
            return null;
        }
    }

    /**
     * The bean name of the ith stored bean.
     */
    String getName(int i) {
        return beanNames[i];
    }

    /**
     * True if the bean from a serialized prototype.
     */
    public boolean isFromPrototype(String name) {
        return fromPrototype[indexForName(name)];
    }

    /**
     * Get Manifest Headers for the specified bean name
     */
    public MessageHeader getManifestData(String name) {
        return manifestData[indexForName(name)];
    }

    /**
     * Get a new Bean instance given its name
     */
    public Object getInstance(String name) {
        try {
            return Beans.instantiate(null, name);
        } catch (Throwable th) {
            if (com.bbn.openmap.util.Debug.debugging("beanbox")) {
                System.err.println(th);
                th.printStackTrace();
                if (name.indexOf('\\') >= 0) {
                    System.err.println("    Note that file names in manifests must use forward "
                            + "slashes \"/\" \n    rather than back-slashes \"\\\"");
                }
            }
            return null;
        }
    }

    private int indexForName(String name) {
        for (int i = 0; i < beanNames.length; i++) {
            if (beanNames[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}