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

/**
 * Utility class for reading the contents of a JAR file.
 *  
 */

import java.beans.BeanInfo;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarLoader {

    private InputStream jarStream; // Jar input stream
    private String jarName; // name of the jar file
    private static boolean warnedAboutNoBeans;

    /**
     * Create a JarLoader to read a JAR and to process its contents.
     * Classes and resources are loaded against a single common class
     * loader instance so that things like "adaptor class
     * instantiaton" can work.
     * 
     * Loading is started with loadIt()
     */
    public JarLoader(String jarName) throws FileNotFoundException {
        // wil check that this file exists, and that is about it.
        //System.out.println("Created JarLoader for file: " +
        // jarName);
        this.jarName = jarName;
        InputStream is = new FileInputStream(jarName);
        jarStream = new BufferedInputStream(is);
    }

    /*
     * In here for compatibility with older versions of JDK1.1
     */
//    private String guessContentTypeFromStream(InputStream is)
//            throws IOException {
//        String type;
//        type = URLConnection.guessContentTypeFromStream(is);
//        // that should be taught about serialized objects.
//
//        if (type == null) {
//            is.mark(10);
//            int c1 = is.read();
//            int c2 = is.read();
//            int c3 = is.read();
//            int c4 = is.read();
//            int c5 = is.read();
//            int c6 = is.read();
//            is.reset();
//            if (c1 == 0xAC && c2 == 0xED) {
//                type = "application/java-serialized-object";
//            }
//        }
//        return type;
//    }

    /**
     * Load the classes, resources, etc.
     */
    public JarInfo loadJar() throws IOException {
        //System.out.println("Enter> JarLoader.loadJar()");
        ZipInputStream zis = null;
        Manifest mf = null;

        boolean empty = true;

        try {
            zis = new ZipInputStream(jarStream);
            ZipEntry ent = null;

            while ((ent = zis.getNextEntry()) != null) {
                empty = false;

                String name = ent.getName();

                if (Manifest.isManifestName(name)) {
                    /* the object we're loading */
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte buffer[] = new byte[1024];

                    for (;;) {
                        int len = zis.read(buffer);
                        if (len < 0) {
                            break;
                        }
                        baos.write(buffer, 0, len);
                    }

                    byte[] buf = baos.toByteArray();
                    mf = new Manifest(buf);
                }

            }

        } catch (IOException ex) {
            throw ex;
        } catch (Throwable th) {
            th.printStackTrace();
            throw new IOException("loadJar caught: " + th);
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

        // Unfortunately ZipInputStream doesn't throw an exception if
        // you hand
        // it a non-Zip file. Our only way of spotting an invalid Jar
        // file
        // is if there no ZIP entries.
        if (empty) {
            throw new IOException("JAR file is corrupt or empty");
        }

        JarInfo ji = createJarInfo(mf);
        //System.out.println("Exit> JarLoader.loadJar()");
        return ji;
    }

    /**
     * Load the JAR file, then apply an action to each bean found
     */
    public static void loadJarDoOnBean(String jarFile, DoOnBean action)
            throws java.io.IOException {
        JarLoader jl = new JarLoader(jarFile);
        JarInfo ji = jl.loadJar();
        if (ji == null) {
            System.err.println("JAR file " + jarFile
                    + " did not load properly!");
            System.err.println("Check for error messages possibly regarding");
            System.err.println("problems defining classes");
            return;
        }
        if (ji.getCount() == 0) {
            System.err.println("Jar file " + jarFile
                    + " didn't have any beans!");
            if (!warnedAboutNoBeans) {
                // We only print this explanatory message once.
                warnedAboutNoBeans = true;
                System.err.println();
                System.err.println("Each jar file needs to contain a manifest file describing which entries are");
                System.err.println("beans.  You can should provide a suitable manifest when you create the jar.");
                System.err.println();
            }
        }

        for (int i = 0; i < ji.getCount(); i++) {
            String beanName = ji.getName(i);
            BeanInfo bi = ji.getBeanInfo(i);
            Class bc = ji.getBeanClass(i);
            if (bi == null || bc == null) {
                // We couldn't load the bean.
                continue;
            }

            action.action(ji, bi, bc, beanName);
        }
    }

    /**
     * Create a JarInfo from a manifest and a class list
     */

    private JarInfo createJarInfo(Manifest mf) {
        //System.out.println("Enter> JarLoader.createJarInfo()");
        Hashtable beans;
        Hashtable headersTable = new Hashtable();
        if (mf == null) {
            // Beans are only identified through a manifest entry.
            // If we don't have a manifest, the beans hashtable
            // should remain empty.
            beans = new Hashtable();
        } else {
            beans = new Hashtable();
            for (Enumeration entries = mf.entries(); entries.hasMoreElements();) {
                MessageHeader mh = (MessageHeader) entries.nextElement();
                String name = mh.findValue("Name");
                String isBean = mh.findValue("Java-Bean");
                if (isBean != null && isBean.equalsIgnoreCase("True")) {
                    String beanName;
                    boolean fromPrototype = true;
                    if (name.endsWith(".class")) {
                        fromPrototype = false;
                        beanName = name.substring(0, name.length() - 6);
                    } else if (name.endsWith(".ser")) {
                        beanName = name.substring(0, name.length() - 4);
                    } else {
                        beanName = name;
                    }
                    beanName = beanName.replace('/', '.');
                    beans.put(beanName, new Boolean(fromPrototype));
                    headersTable.put(beanName, mh);
                }
            }
        }

        String beanNames[] = new String[beans.size()];
        boolean fromPrototype[] = new boolean[beans.size()];
        MessageHeader headers[] = new MessageHeader[beans.size()];
        Enumeration keys;
        int i;
        for (keys = beans.keys(), i = 0; keys.hasMoreElements(); i++) {
            String key = (String) keys.nextElement();
            beanNames[i] = key;
            fromPrototype[i] = ((Boolean) beans.get(key)).booleanValue();
            headers[i] = (MessageHeader) headersTable.get(key);
        }

        //System.out.println("Exiting> JarLoader.createJarInfo()");
        return new JarInfo(jarName, beanNames, fromPrototype, headers);
    }
}