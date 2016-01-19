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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/wanderer/SLOC.java,v $
// $RCSfile: SLOC.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:06:32 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.wanderer;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.ArgParser;

/**
 * Count the source lines of code but going through the directory and counting ;
 * and }.
 */
public class SLOC implements WandererCallback {

	int sloc = 0;
	boolean DETAIL = false;

	public void setSLOC(int num) {
		sloc = num;
		DETAIL = getLogger().isLoggable(Level.FINE);
	}

	public int getSLOC() {
		return sloc;
	}

	// do nothing on directories
	public boolean handleDirectory(File directory) {
		return true;
	}

	// count the ; and } in each file.
	public boolean handleFile(File file) {
		if (!file.getName().endsWith(".java")) {
			return true;
		}

		if (DETAIL) {
			getLogger().info("Counting code in " + file.getName());
		}

		int count = 0;

		try {
			BinaryBufferedFile bbf = new BinaryBufferedFile(file);

			try {
				while (true) {
					char c = bbf.readChar();
					if (c == ';' || c == '}') {
						count++;
					}
				}
			} catch (EOFException eofe) {
			} catch (FormatException fe) {
			}
			bbf.close();

			if (DETAIL) {
				getLogger().info(file.getName() + " has " + count + " LOC");
			}

			sloc += count;

		} catch (IOException ioe) {
		}
		return true;
	}

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(SLOC.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("The LoggerHolder should never be instantiated");
		}
	}

	/**
	 * Get the logger for this class.
	 *
	 * @return logger for this class
	 */
	private static Logger getLogger() {
		return LoggerHolder.LOGGER;
	}

	// </editor-fold>
	
	/**
	 * Given a set of files or directories, parade through them to change their
	 * case.
	 * 
	 * @param argv
	 *            paths to files or directories, use -h to get a usage
	 *            statement.
	 */
	public static void main(String[] argv) {

		ArgParser ap = new ArgParser("SLOC");

		if (argv.length == 0) {
			ap.bail("Counts ';' and '}' to sum up Source Lines Of Code\nUsage: java com.bbn.openmap.util.wanderer.SLOC <dir>",
					false);
		}

		ap.parse(argv);

		String[] dirs = argv;

		SLOC sloc = new SLOC();
		Wanderer wanderer = new Wanderer(sloc);

		int runningTotal = 0;

		// Assume that the arguments are paths to directories or
		// files.
		for (int i = 0; i < dirs.length; i++) {
			sloc.setSLOC(0);
			wanderer.handleEntry(new File(dirs[i]));
			getLogger().info("Source Lines of Code in " + dirs[i] + " = " + sloc.getSLOC());
			runningTotal += sloc.getSLOC();
		}

		if (dirs.length > 1) {
			getLogger().info("Total Source Lines of Code in all directories = " + runningTotal);
		}
	}
}