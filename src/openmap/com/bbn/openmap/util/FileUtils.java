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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/FileUtils.java,v
// $
// $RCSfile: FileUtils.java,v $
// $Revision: 1.6 $
// $Date: 2006/07/10 23:22:28 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.bbn.openmap.Environment;

public class FileUtils {

    protected static Logger logger = Logger.getLogger("com.bbn.openmap.util.FileUtils");

    public static String getFilePathToSaveFromUser(String title) {
        OpenFileRunnable runnable = new OpenFileRunnable(title, null) {
            public void run() {
                JFileChooser chooser = getChooser(title);
                if (fileFilter != null) {
                    chooser.setFileFilter(fileFilter);
                }
                int state = chooser.showSaveDialog(null);
                result = handleResponse(chooser, state);
            }
        };
        runnable.invoke();
        return runnable.getResult();
    }

    public static String getFilePathToOpenFromUser(String title) {
        return getFilePathToOpenFromUser(title, null);
    }

    public static String getFilePathToOpenFromUser(String title, FileFilter ff) {
        OpenFileRunnable runnable = new OpenFileRunnable(title, ff);
        runnable.invoke();
        return runnable.getResult();
    }

    public static String getPathToOpenFromUser(String title, FileFilter ff, int fileSelectionMode,
                                               String acceptButtonText) {
        OpenPathRunnable runnable = new OpenPathRunnable(title, ff, fileSelectionMode, acceptButtonText);
        runnable.invoke();
        return runnable.getResult();
    }

    static class OpenFileRunnable implements Runnable {

        String title;
        FileFilter fileFilter;
        String result;

        OpenFileRunnable(String title, FileFilter ff) {
            this.title = title;
            this.fileFilter = ff;
        }

        public void run() {
            JFileChooser chooser = getChooser(title);
            if (fileFilter != null) {
                chooser.setFileFilter(fileFilter);
            }
            int state = chooser.showOpenDialog(null);
            this.result = handleResponse(chooser, state);
        }

        String getResult() {
            return result;
        }

        void invoke() {
            try {
                SwingUtilities.invokeAndWait(OpenFileRunnable.this);
            } catch (Exception e) {
                if (logger.isLoggable(Level.FINE)) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class OpenPathRunnable extends OpenFileRunnable {

        int fileSelectionMode;
        String acceptButtonText;

        OpenPathRunnable(String title, FileFilter ff, int fileSelectionMode, String acceptButtonText) {
            super(title, ff);
            this.fileSelectionMode = fileSelectionMode;
            this.acceptButtonText = acceptButtonText;
        }

        public void run() {
            JFileChooser chooser = getChooser(title);
            chooser.setFileSelectionMode(fileSelectionMode);
            if (fileFilter != null) {
                chooser.setFileFilter(fileFilter);
            }
            int state = chooser.showDialog(null, acceptButtonText);
            this.result = handleResponse(chooser, state);
        }
    }

    public static JFileChooser getChooser(String title) {
        // setup the file chooser
        File startingPoint = new File(Environment.get("lastchosendirectory", System.getProperty("user.home")));
        JFileChooser chooser = new JFileChooser(startingPoint);
        chooser.setDialogTitle(title);
        return chooser;
    }

    public static String handleResponse(JFileChooser chooser, int state) {
        String ret = null;
        try {
            // only bother trying to read the file if there is one
            // for some reason, the APPROVE_OPTION said it was a
            // boolean during compile and didn't work in this next
            // statement
            if ((state != JFileChooser.CANCEL_OPTION) && (state != JFileChooser.ERROR_OPTION)) {

                ret = chooser.getSelectedFile().getCanonicalPath();

                int dirIndex = ret.lastIndexOf(File.separator);
                if (dirIndex >= 0) {
                    // store the selected file for later
                    Environment.set("lastchosendirectory", ret.substring(0, dirIndex));
                }
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error picking file", JOptionPane.ERROR_MESSAGE);
            ioe.printStackTrace();
        }
        return ret;
    }

    /**
     * Copy a file to another location, byte-wise.
     * 
     * @param fromFile the File to copy from.
     * @param toFile the File to copy to.
     * @param bufSize the byte size of the transfer buffer.
     * @throws IOException Thrown if anything goes wrong.
     */
    public static void copy(File fromFile, File toFile, int bufSize) throws IOException {

        FileInputStream fis = new FileInputStream(fromFile);
        FileOutputStream fos = new FileOutputStream(toFile);

        if (bufSize <= 0) {
            bufSize = 1024;
        }

        byte[] bytes = new byte[bufSize];

        int numRead;

        while ((numRead = fis.read(bytes)) > 0) {
            fos.write(bytes, 0, numRead);
        }

        fis.close();
        fos.close();
    }

    /**
     * Delete a file or a directory, including its content.
     * 
     * @param file
     * @throws IOException
     */
    public static void deleteFile(File file) throws IOException {

        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : " + file.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    deleteFile(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();

                }
            }

        } else {
            // if file, then delete it
            file.delete();

        }
    }

    /**
     * Create a zip file containing the given File.
     * 
     * @param zipFileName The path to the zip file. If it doesn't end in .zip,
     *        .zip will be added to it.
     * @param toBeZipped The Path of the file/directory to be zipped.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void saveZipFile(String zipFileName, File toBeZipped)
            throws IOException, FileNotFoundException {

        try {

            if (!zipFileName.endsWith(".zip")) {
                zipFileName += ".zip";
            }

            File zipFile = new File(zipFileName);
            if (!zipFile.getParentFile().exists()) {
                zipFile.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zoStream = new ZipOutputStream(fos);
            // zoStream.setMethod(ZipOutputStream.STORED);
            writeZipEntry(toBeZipped, zoStream, toBeZipped.getParent().length() + 1);
            zoStream.close();
        } catch (SecurityException se) {
            logger.warning("Security Exception caught while creating " + zipFileName);
        }
    }

    /**
     * Create a zip file containing the files in the list. The entries will not
     * have their parent's file names in their path, they are stored with the
     * given file at the root of the zip/jar.
     * 
     * @param zipFileName The path to the zip/jar file.
     * @param toBeZipped The List of files to be placed in the zip/jar.
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void saveZipFile(String zipFileName, List<File> toBeZipped)
            throws IOException, FileNotFoundException {

        try {

            File zipFile = new File(zipFileName);
            if (!zipFile.getParentFile().exists()) {
                zipFile.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(zipFile);
            CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());
            ZipOutputStream zoStream = new ZipOutputStream(new BufferedOutputStream(checksum));
            // ZipOutputStream zoStream = new ZipOutputStream(fos);
            // zoStream.setMethod(ZipOutputStream.STORED);
            for (File file : toBeZipped) {
                writeZipEntry(file, zoStream, file.getParent().length() + 1);
            }
            zoStream.close();
        } catch (SecurityException se) {
            logger.warning("Security Exception caught while creating " + zipFileName);
        }
    }

    /**
     * Writes a file to the jar stream.
     * 
     * @param toBeZipped the file to be written
     * @param zoStream the stream to write it to, prepared for the
     *        ZipFile/JarFile
     * @param prefixTrimLength The number of characters to trim off the absolute
     *        path of the file to be zipped. Can be useful to adjust this to
     *        adjust the directory depth of the entry for when it is unpacked.
     *        If less than 0, only the file name will be used.
     * @throws IOException
     */
    public static void writeZipEntry(File toBeZipped, ZipOutputStream zoStream, int prefixTrimLength)
            throws IOException {

        if (toBeZipped.isDirectory()) {
            File[] files = toBeZipped.listFiles();
            for (int i = 0; i < files.length; i++) {
                writeZipEntry(files[i], zoStream, prefixTrimLength);
            }
        } else {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(toBeZipped + ", "
                        + toBeZipped.getAbsolutePath().substring(prefixTrimLength) + ")");
            }

            writeZipEntry(toBeZipped, zoStream, prefixTrimLength < 0 ? toBeZipped.getName()
                    : toBeZipped.getAbsolutePath().substring(prefixTrimLength));
        }
    }

    protected static void writeZipEntry(File fromFile, ZipOutputStream zoStream, String entryName)
            throws IOException {

        entryName = entryName.replace('\\', '/');

        // long size = fromFile.length();
        ZipEntry zEntry = new ZipEntry(entryName);
        // zEntry.setSize(size);
        // zEntry.setCrc(0);// Don't know what it these values are
        // right now, but zero works...
        zoStream.putNextEntry(zEntry);

        FileInputStream fis = new FileInputStream(fromFile);

        byte[] bytes = new byte[1024];

        int numRead;
        // CRC32 checksum = new CRC32();
        while ((numRead = fis.read(bytes)) > 0) {
            zoStream.write(bytes, 0, numRead);
            // checksum.update(bytes, 0, numRead);
        }
        // zEntry.setCrc(checksum.getValue());
        zoStream.closeEntry();
        fis.close();
    }

    /**
     * Unpack a zip file.
     * 
     * @param zipFileName The path name of the zip file to unpack.
     * @param toDir the directory to put the unpacked files in.
     * @param deleteAfter flag to delete the zip file when complete.
     */
    public static void openZipFile(String zipFileName, File toDir, boolean deleteAfter) {
        if (zipFileName != null) {
            try {
                InputStream in;

                if (!toDir.exists()) {
                    toDir.mkdirs();
                }

                URL zipurl = PropUtils.getResourceOrFileOrURL(zipFileName);
                if (zipurl != null) {

                    in = new BufferedInputStream(zipurl.openStream());

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(" unzipping " + zipFileName);
                    }
                    ZipInputStream zin = new ZipInputStream(in);
                    ZipEntry e;

                    while ((e = zin.getNextEntry()) != null) {

                        if (e.isDirectory()) {
                            new File(toDir, e.getName()).mkdirs();
                        } else {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine(" unzipping " + e.getName());
                            }
                            unzip(zin, new File(toDir, e.getName()));
                        }
                    }
                    zin.close();
                    if (deleteAfter) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("unzipping complete, deleting zip file");
                        }

                        File file = new File(zipurl.getFile());
                        if (file.exists()) {
                            file.delete();
                        }
                    } else if (logger.isLoggable(Level.FINE)) {
                        logger.fine("unzipping complete, leaving zip file");
                    }
                    return;
                }
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void unzip(ZipInputStream zin, File f) throws IOException {
        final int BUFFER = 2048;
        FileOutputStream out = new FileOutputStream(f);
        byte[] b = new byte[BUFFER];
        int len = 0;
        BufferedOutputStream dest = new BufferedOutputStream(out, BUFFER);
        while ((len = zin.read(b, 0, BUFFER)) != -1) {
            dest.write(b, 0, len);
        }
        dest.flush();
        dest.close();
    }

}